package observer.maven.telegram

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import observer.maven.telegram.rest.TelegramCommand

fun Application.configureTelegramInputController() {

    val botToken = environment.config.property("ktor.telegram.botToken").getString()

    val syncInterval = environment.config.property("ktor.setting.syncInterval").getString().toLong()

    val telegramComponent = TelegramComponent(botToken, syncInterval)

    routing {
        post("/handleTelegramCommand") {
            val command = call.receive<TelegramCommand>()

            when {
                command.message != null -> {
                    telegramComponent.messageHandler.handle(command.message)
                }

                command.callbackQuery != null -> {
                    telegramComponent.telegramCallbackHandler.handleCallback(command.callbackQuery)
                }
            }

            call.respond("Ok")
        }
    }
}
