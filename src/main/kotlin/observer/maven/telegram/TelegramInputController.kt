package observer.maven.telegram

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.log
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import observer.maven.telegram.rest.TelegramCommand

fun Application.configureTelegramInputController(
    telegramComponent: TelegramComponent,
) {
    routing {
        post("/handleTelegramCommand") {
            val command = call.receive<TelegramCommand>()

            this@configureTelegramInputController.log.info("telegram request $command")

            when {
                command.message != null -> {
                    telegramComponent.messageHandler.handle(command.message, call)
                }

                command.callbackQuery != null -> {
                    telegramComponent.telegramCallbackHandler.handleCallback(command.callbackQuery)
                }
            }

            call.respond(HttpStatusCode.Accepted)
        }
    }
}
