package observer.maven.telegram.handlers

import com.github.aymanizz.ktori18n.R
import com.github.aymanizz.ktori18n.i18n
import com.github.aymanizz.ktori18n.t
import io.ktor.server.application.ApplicationCall
import observer.maven.database.TelegramChat
import observer.maven.library.LibraryMediator
import observer.maven.telegram.rest.TelegramMessageSender
import observer.maven.telegram.rest.TelegramReceivedMessage
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.Locale

class TelegramMessageHandler(
    private val telegramMessageSender: TelegramMessageSender,
    private val libraryMediator: LibraryMediator,
    private val telegramRawMessageHandler: TelegramRawMessageHandler,
) {

    suspend fun handle(message: TelegramReceivedMessage, call: ApplicationCall) {
        when (message.text) {
            TelegramBotConstants.START -> {
                transaction {
                    TelegramChat.new {
                        chatId = message.chat.id.id
                        language = message.from.language
                    }
                }
                telegramMessageSender.sendMessage(
                    chatId = message.chat.id,
                    text = call.t(R("hello")),
                )
            }

            else -> {
                telegramRawMessageHandler.handle(message.text).forEach { libraryId ->
                    libraryMediator.addLibrary(libraryId, message.chat.id)
                }
            }
        }
    }
}