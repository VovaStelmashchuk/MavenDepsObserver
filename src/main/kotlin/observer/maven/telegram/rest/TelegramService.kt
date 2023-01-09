package observer.maven.telegram.rest

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface TelegramService {
    @POST("/sendMessage")
    suspend fun sendMessage(
        @Body message: TelegramSendMessageCommand
    )
}
