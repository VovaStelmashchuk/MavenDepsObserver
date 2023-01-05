package observer.maven.telegram.rest

import observer.maven.telegram.rest.TelegramSendMessageCommand
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface TelegramService {
    @POST("/{botToken}/sendMessage")
    suspend fun sendMessage(
        @Path("botToken") botToken: String,
        @Body message: TelegramSendMessageCommand
    )
}