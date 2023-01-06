package observer.maven.telegram.rest

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TelegramCommand(
    @SerialName("message") val message: TelegramReceivedMessage? = null,
    @SerialName("callback_query") val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String,
    @SerialName("message")
    val message: TelegramReceivedMessage,
)

@Serializable
data class TelegramReceivedMessage(
    @SerialName("chat") val chat: TelegramChat,
    @SerialName("text") val text: String,
)

@Serializable
data class TelegramChat(
    @SerialName("id")
    val id: TelegramChatId,
)

@Suppress("DataClassPrivateConstructor")
@Serializable
data class TelegramSendMessageCommand constructor(
    @SerialName("chat_id")
    val chatId: TelegramChatId,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: TelegramReplyMarkup? = null,
)

@Serializable
data class TelegramReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<TelegramInlineKeyboardButton>>,
)

@Serializable
data class TelegramInlineKeyboardButton(
    @SerialName("text")
    val text: String,
    @SerialName("callback_data")
    val callbackData: String,
)

@JvmInline
@Serializable
value class TelegramChatId(val id: Long)
