package observer.maven

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.UnknownChildHandler
import nl.adaptivity.xmlutil.serialization.XML
import observer.maven.database.connectToDatabase
import observer.maven.maven.rest.MavenService
import observer.maven.plugins.configureLocalization
import observer.maven.plugins.configureSerialization
import observer.maven.telegram.TelegramComponent
import observer.maven.telegram.configureTelegramInputController
import observer.maven.telegram.rest.TelegramAddBotTokenInterseptor
import observer.maven.telegram.rest.TelegramService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit


fun main() {
    embeddedServer(Netty, environment = applicationEngineEnvironment {
        config = HoconApplicationConfig(ConfigFactory.load())

        val port = config.property("ktor.connector.port").getString().toInt()
        val host = config.property("ktor.connector.host").getString()

        connector {
            this.port = port
            this.host = host
        }

        val botToken = config.property("ktor.telegram.botToken").getString()
        val syncInterval = config.property("ktor.setting.syncInterval").getString().toLong()

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

        @OptIn(ExperimentalSerializationApi::class)
        val telegramService: TelegramService = Retrofit.Builder()
            .client(
                okHttpClient.newBuilder()
                    .addInterceptor(TelegramAddBotTokenInterseptor(botToken))
                    .build()
            )
            .baseUrl("https://api.telegram.org/")
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(TelegramService::class.java)

        @OptIn(ExperimentalSerializationApi::class, ExperimentalXmlUtilApi::class)
        val mavenService: MavenService = Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(XML {
                unknownChildHandler = UnknownChildHandler { _, _, _, _, _ -> emptyList() }
            }.asConverterFactory("application/xml".toMediaType()))
            .baseUrl("https://repo1.maven.org/maven2/")
            .build()
            .create(MavenService::class.java)

        module {
            configureLocalization()
            configureSerialization()
            connectToDatabase()

            val telegramComponent = TelegramComponent(syncInterval, telegramService, mavenService, this)
            configureTelegramInputController(telegramComponent)
        }
    }).start(wait = true)
}
