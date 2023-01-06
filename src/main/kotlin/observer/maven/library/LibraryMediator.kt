package observer.maven.library

import observer.maven.maven.LibraryId
import observer.maven.maven.Maven
import observer.maven.telegram.ChatRepository
import observer.maven.telegram.TelegramButtonBuilder
import observer.maven.telegram.rest.TelegramChatId
import observer.maven.telegram.rest.TelegramRepository

class LibraryMediator(
    private val maven: Maven,
    private val telegramRepository: TelegramRepository,
    private val chatRepository: ChatRepository,
    private val telegramButtonBuilder: TelegramButtonBuilder,
) {

    suspend fun addLibrary(libraryId: LibraryId, chatId: TelegramChatId) {
        when (val res = maven.add(libraryId)) {
            is Maven.AddLibraryResult.LibraryAdded -> libraryAdded(chatId, res.library)
            is Maven.AddLibraryResult.LibraryAlreadyExist -> libraryAdded(chatId, res.library)
            Maven.AddLibraryResult.InValidLibraryId -> sendToTelegram(chatId, "InValidLibraryId")
            Maven.AddLibraryResult.LibraryNotFoundAdded -> sendToTelegram(chatId, "LibraryNotFoundAdded")
            Maven.AddLibraryResult.MavenCentralUnAvailable -> sendToTelegram(chatId, "MavenCentralUnAvailable")
        }
    }

    private suspend fun libraryAdded(chatId: TelegramChatId, library: Library) {
        chatRepository.attachLibrary(chatId, library.id, ChatRepository.ObservableStrategy.STABLE)

        telegramRepository.sendMessage(
            chatId = chatId,
            text = "The library ${library.libraryId} was added the last version is ${library.lastVersion} " +
                    "the last stable version is ${library.lastStableVersion}. " +
                    "I will notify you when a new version is released.",
            buttons = telegramButtonBuilder.buildButton(
                ChatRepository.ObservableStrategy.STABLE,
                library.libraryId,
            )
        )
    }

    private suspend fun sendToTelegram(chatId: TelegramChatId, text: String) {
        telegramRepository.sendMessage(
            chatId = chatId,
            text = text
        )
    }
}