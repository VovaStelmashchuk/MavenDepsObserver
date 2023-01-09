package observer.maven.telegram

import observer.maven.database.TelegramChat
import observer.maven.database.TelegramChats
import observer.maven.maven.LibraryCoordinate
import observer.maven.telegram.rest.CallbackQuery
import observer.maven.telegram.rest.TelegramMessageSender
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction

class TelegramCallbackHandler(
    private val telegramMessageSender: TelegramMessageSender,
) {

    suspend fun handleCallback(callback: CallbackQuery) {
        when {
            callback.data.startsWith(TelegramBotConstants.REMOVE_PREFIX) -> {
                val library = LibraryCoordinate(callback.data.removePrefix(TelegramBotConstants.REMOVE_PREFIX).trim())
                transaction {
                    val chat = TelegramChat.find { TelegramChats.chatId eq callback.message.chat.id.id }.first()

                    chat.libraries =
                        SizedCollection(chat.libraries.filter { it.libraryCoordinate != library.value }.toSet())
                }

                telegramMessageSender.sendMessage(
                    chatId = callback.message.chat.id,
                    text = "$library removed from your library list",
                )
            }
        }
    }
}
