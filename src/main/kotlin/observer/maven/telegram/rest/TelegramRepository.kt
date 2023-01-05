package observer.maven.telegram.rest

class TelegramRepository(
    private val telegramService: TelegramService,
    private val botToken: String,
) {

    suspend fun sendMessage(chatId: TelegramChatId, text: String) {
        telegramService.sendMessage(
            botToken,
            TelegramSendMessageCommand(
                chatId = chatId,
                text = text,
                replyMarkup = null,
            )
        )
    }

    suspend fun sendMessage(
        chatId: TelegramChatId,
        text: String,
        buttons: List<TelegramInlineKeyboardButton>
    ) {
        telegramService.sendMessage(
            botToken,
            TelegramSendMessageCommand(
                chatId = chatId,
                text = text,
                replyMarkup = TelegramReplyMarkup(listOf(buttons)),
            )
        )
    }

}