package observer.maven.database

import io.ktor.server.application.Application
import observer.maven.library.Libraries
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    val databaseUrl = environment.config.property("ktor.database.url").getString()
    val user = environment.config.property("ktor.database.user").getString()
    val password = environment.config.propertyOrNull("ktor.database.password")?.getString().orEmpty()

    Database.connect(
        url = "jdbc:postgresql://$databaseUrl",
        user = user,
        password = password,
    )

    transaction {
        Libraries.selectAll().forEach {
            println(it)
        }
    }
}
