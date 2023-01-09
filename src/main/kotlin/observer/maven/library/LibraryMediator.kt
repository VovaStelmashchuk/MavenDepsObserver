package observer.maven.library

import observer.maven.database.Library
import observer.maven.database.TelegramChat
import observer.maven.database.TelegramChats
import observer.maven.maven.LibraryCoordinate
import observer.maven.maven.Maven
import observer.maven.telegram.rest.TelegramChatId
import observer.maven.telegram.rest.TelegramMessageSender
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction

class LibraryMediator(
    private val maven: Maven,
    private val telegramMessageSender: TelegramMessageSender,
) {

    suspend fun addLibrary(libraryId: LibraryCoordinate, chatId: TelegramChatId) {
        when (val res = maven.add(libraryId)) {
            is Maven.AddLibraryResult.LibraryAdded -> libraryAdded(chatId, res.library)
            is Maven.AddLibraryResult.LibraryAlreadyExist -> libraryAdded(chatId, res.library)
            Maven.AddLibraryResult.InValidLibraryId -> sendToTelegram(chatId, "InValidLibraryId")
            Maven.AddLibraryResult.LibraryNotFoundAdded -> sendToTelegram(chatId, "LibraryNotFoundAdded")
            Maven.AddLibraryResult.MavenCentralUnAvailable -> sendToTelegram(chatId, "MavenCentralUnAvailable")
        }
    }

    private suspend fun libraryAdded(chatId: TelegramChatId, library: Library) {
        val chat = transaction {
            val chat = TelegramChat.find { TelegramChats.chatId eq chatId.id }.first()

            chat.libraries = SizedCollection(chat.libraries + library)
            chat
        }
        telegramMessageSender.sendLibraryAdded(chat.id.value, library.id.value)
    }

    private suspend fun sendToTelegram(chatId: TelegramChatId, text: String) {
        telegramMessageSender.sendMessage(
            chatId = chatId,
            text = text
        )
    }
}
