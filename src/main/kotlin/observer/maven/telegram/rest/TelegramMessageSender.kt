package observer.maven.telegram.rest

import com.github.aymanizz.ktori18n.I18n
import com.github.aymanizz.ktori18n.R
import observer.maven.database.Library
import observer.maven.database.TelegramChat
import observer.maven.telegram.handlers.TelegramBotConstants
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.Locale

class TelegramMessageSender(
    private val telegramService: TelegramService,
    private val i18n: I18n,
) {

    suspend fun sendLibraryInfo(libraryId: Int, chatId: Int) {
        val chat = transaction { TelegramChat[chatId] }
        val library = transaction { Library[libraryId] }
        val locale = Locale(chat.language)

        val text = i18n.t(
            locale,
            R("library.info"),
            library.libraryCoordinate,
            library.lastVersion,
            library.lastStableVersion
        )

        telegramService.sendMessage(
            TelegramSendMessageCommand(
                chatId = TelegramChatId(chat.chatId),
                text = text,
                replyMarkup = TelegramReplyMarkup(
                    listOf(
                        listOf(
                            TelegramInlineKeyboardButton(
                                "Stop observing",
                                "${TelegramBotConstants.REMOVE_PREFIX}${libraryId}"
                            ),
                        )
                    ),
                )
            )
        )
    }

    suspend fun sendLibraryUpdates(libraryId: Int, chatId: Int) {
        val chat = transaction { TelegramChat[chatId] }
        val library = transaction { Library[libraryId] }
        val locale = Locale(chat.language)

        val text = i18n.t(
            locale,
            R("library.updated"),
            library.libraryCoordinate,
            library.lastVersion,
            library.lastStableVersion
        )

        telegramService.sendMessage(
            TelegramSendMessageCommand(
                chatId = TelegramChatId(chat.chatId),
                text = text,
                replyMarkup = TelegramReplyMarkup(
                    listOf(
                        listOf(
                            TelegramInlineKeyboardButton(
                                "Stop observing",
                                "${TelegramBotConstants.REMOVE_PREFIX}${libraryId}"
                            ),
                        )
                    ),
                )
            )
        )
    }

    suspend fun sendLibraryAdded(chatId: Int, libraryId: Int) {
        val chat = transaction { TelegramChat[chatId] }
        val library = transaction { Library[libraryId] }
        val locale = Locale(chat.language)
        val text = i18n.t(
            locale,
            R("library.added"),
            library.libraryCoordinate,
            library.lastVersion,
            library.lastStableVersion
        )
        telegramService.sendMessage(
            TelegramSendMessageCommand(
                chatId = TelegramChatId(chat.chatId),
                text = text,
                replyMarkup = TelegramReplyMarkup(
                    listOf(
                        listOf(
                            TelegramInlineKeyboardButton(
                                "Stop observing",
                                "${TelegramBotConstants.REMOVE_PREFIX}${libraryId}"
                            ),
                        )
                    ),
                )
            )
        )
    }

    suspend fun sendMessage(chatId: TelegramChatId, text: String) {
        telegramService.sendMessage(
            TelegramSendMessageCommand(
                chatId = chatId,
                text = text,
                replyMarkup = null,
            )
        )
    }
}
