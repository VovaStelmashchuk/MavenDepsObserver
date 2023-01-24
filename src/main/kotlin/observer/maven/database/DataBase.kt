package observer.maven.database

import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database

fun Application.connectToDatabase() {
    val databaseUrl = environment.config.property("ktor.database.url").getString()
    val user = environment.config.property("ktor.database.user").getString()
    val password = environment.config.propertyOrNull("ktor.database.password")?.getString().orEmpty()

    Database.connect(
        url = "jdbc:postgresql://$databaseUrl",
        user = user,
        password = password,
    )
}

