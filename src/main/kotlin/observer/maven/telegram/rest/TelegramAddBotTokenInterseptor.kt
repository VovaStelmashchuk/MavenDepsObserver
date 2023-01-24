package observer.maven.telegram.rest

import okhttp3.Interceptor
import okhttp3.Response

class TelegramAddBotTokenInterseptor(
    private val botToken: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url

        val builder = url.newBuilder()
        (0 until url.pathSize).forEach {
            builder.removePathSegment(it)
        }

        listOf(botToken).plus(url.pathSegments)
            .forEach {
                builder.addPathSegment(it)
            }

        return chain.proceed(chain.request().newBuilder().url(builder.build()).build())
    }
}