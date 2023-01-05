package observer.maven.telegram

import observer.maven.maven.LibraryId
import observer.maven.telegram.rest.TelegramInlineKeyboardButton

class TelegramButtonBuilder {

    private fun removeButton(libraryId: LibraryId): TelegramInlineKeyboardButton {
        return TelegramInlineKeyboardButton(
            "Stop observing",
            "${TelegramBotConstants.REMOVE_PREFIX}${libraryId.value}"
        )
    }

    fun buildButton(
        strategy: ChatRepository.ObservableStrategy,
        libraryId: LibraryId
    ): List<TelegramInlineKeyboardButton> {
        return when (strategy) {
            ChatRepository.ObservableStrategy.ALL -> {
                listOf(
                    TelegramInlineKeyboardButton(
                        "Observe only stable", buildCallback(ChatRepository.ObservableStrategy.STABLE, libraryId),
                    ),
                    TelegramInlineKeyboardButton(
                        "Observe only major", buildCallback(ChatRepository.ObservableStrategy.MAJOR, libraryId),
                    )
                )
            }

            ChatRepository.ObservableStrategy.STABLE -> {
                listOf(
                    TelegramInlineKeyboardButton(
                        "Start observe all changes",
                        buildCallback(ChatRepository.ObservableStrategy.ALL, libraryId),
                    ),
                    TelegramInlineKeyboardButton(
                        "Observe only major changes", buildCallback(ChatRepository.ObservableStrategy.MAJOR, libraryId),
                    )
                )
            }

            ChatRepository.ObservableStrategy.MAJOR -> {
                listOf(
                    TelegramInlineKeyboardButton(
                        "Observe only stable changes",
                        buildCallback(ChatRepository.ObservableStrategy.STABLE, libraryId),
                    ),
                    TelegramInlineKeyboardButton(
                        "Observe all changes", buildCallback(ChatRepository.ObservableStrategy.ALL, libraryId),
                    )
                )
            }
        }.plus(removeButton(libraryId))
    }

    private fun buildCallback(strategy: ChatRepository.ObservableStrategy, libraryId: LibraryId): String {
        return buildString {
            append(TelegramBotConstants.CHANGE_OBSERVING_STRATEGY_PREFIX)
            append(strategy.name)
            append(libraryId.value)
        }
    }
}
