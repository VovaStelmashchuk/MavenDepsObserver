package observer.maven.telegram

import observer.maven.maven.LibraryId
import observer.maven.telegram.rest.CallbackQuery
import observer.maven.telegram.rest.TelegramRepository

class TelegramCallbackHandler(
    private val chatRepository: ChatRepository,
    private val telegramRepository: TelegramRepository,
) {

    suspend fun handleCallback(callback: CallbackQuery) {
        when {
            callback.data.startsWith(TelegramBotConstants.REMOVE_PREFIX) -> {
                val library = LibraryId(callback.data.removePrefix(TelegramBotConstants.REMOVE_PREFIX).trim())
                chatRepository.removeChat(
                    chatId = callback.message.chat.id,
                    libraryId = library,
                )
                telegramRepository.sendMessage(
                    chatId = callback.message.chat.id,
                    text = "$library removed from your library list",
                )
            }

            callback.data.startsWith(TelegramBotConstants.CHANGE_OBSERVING_STRATEGY_PREFIX) -> {
                val withoutCommandPrefix =
                    callback.data.removePrefix(TelegramBotConstants.CHANGE_OBSERVING_STRATEGY_PREFIX).trim()
                ChatRepository.ObservableStrategy.values().find { observableStrategy ->
                    withoutCommandPrefix.startsWith(observableStrategy.name)
                }?.let { observableStrategy ->
                    val library = LibraryId(withoutCommandPrefix.removePrefix(observableStrategy.name).trim())
                    chatRepository.changeObservability(
                        chatId = callback.message.chat.id,
                        libraryId = library,
                        observingStrategy = observableStrategy,
                    )

                    telegramRepository.sendMessage(
                        chatId = callback.message.chat.id,
                        text = "Observing strategy for $library changed to ${observableStrategy.name}",
                    )
                }
            }
        }
    }
}
