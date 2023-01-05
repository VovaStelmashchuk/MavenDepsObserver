package observer.maven

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import observer.maven.database.configureDatabase
import observer.maven.plugins.configureSerialization
import observer.maven.telegram.configureTelegramInputController


fun main() {
    embeddedServer(Netty, environment = applicationEngineEnvironment {
        config = HoconApplicationConfig(ConfigFactory.load())

        val port = config.property("ktor.connector.port").getString().toInt()
        val host = config.property("ktor.connector.host").getString()

        connector {
            this.port = port
            this.host = host
        }

        module {
            configureSerialization()
            configureDatabase()
            configureTelegramInputController()
        }

    }).start(wait = true)
}
