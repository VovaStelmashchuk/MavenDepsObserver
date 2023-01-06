package observer.maven.telegram

import observer.maven.library.LibraryMediator
import observer.maven.maven.LibraryId
import observer.maven.telegram.rest.TelegramReceivedMessage
import observer.maven.telegram.rest.TelegramRepository

class TelegramMessageHandler(
    private val telegramRepository: TelegramRepository,
    private val libraryMediator: LibraryMediator,
) {

    suspend fun handle(message: TelegramReceivedMessage) {
        when (message.text) {
            TelegramBotConstants.START -> {
                telegramRepository.sendMessage(
                    chatId = message.chat.id,
                    text = "Hello I am a bot that will notify you about new versions of libraries in maven " +
                            "central repository, just send me a library name and I will notify you about " +
                            "new versions of it. Example: com.squareup.retrofit2:retrofit",
                )
            }

            else -> {
                val libraryId = LibraryId(message.text)
                libraryMediator.addLibrary(libraryId, message.chat.id)
            }
        }
    }
}
