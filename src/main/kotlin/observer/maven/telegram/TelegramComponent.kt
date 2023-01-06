package observer.maven.telegram

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.UnknownChildHandler
import nl.adaptivity.xmlutil.serialization.XML
import observer.maven.library.LibraryDataBaseRepository
import observer.maven.library.LibraryMediator
import observer.maven.library.MavenLibraryUpdatesMonitor
import observer.maven.maven.Maven
import observer.maven.maven.rest.MavenService
import observer.maven.telegram.rest.TelegramRepository
import observer.maven.telegram.rest.TelegramService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

class TelegramComponent(
    botToken: String,
    syncInterval: Long,
) {

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()

    @OptIn(ExperimentalSerializationApi::class)
    private val telegramService: TelegramService = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("https://api.telegram.org/")
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(TelegramService::class.java)

    private val telegramRepository =
        TelegramRepository(telegramService, botToken)

    @OptIn(ExperimentalSerializationApi::class, ExperimentalXmlUtilApi::class)
    private val mavenService = Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(XML {
            unknownChildHandler = UnknownChildHandler { _, _, _, _, _ -> emptyList() }
        }.asConverterFactory("application/xml".toMediaType()))
        .baseUrl("https://repo1.maven.org/maven2/")
        .build()
        .create(MavenService::class.java)

    private val maven = Maven(
        mavenService,
        LibraryDataBaseRepository(),
    )

    private val libraryMediator = LibraryMediator(maven, telegramRepository, ChatRepository(), TelegramButtonBuilder())

    private val mavenLibraryUpdatesMonitor = MavenLibraryUpdatesMonitor(
        LibraryDataBaseRepository(),
        mavenService,
        syncInterval,
    )

    private val chatRepository = ChatRepository()

    val telegramCallbackHandler = TelegramCallbackHandler(chatRepository, TelegramRepository(telegramService, botToken))

    val messageHandler = TelegramMessageHandler(
        telegramRepository = telegramRepository,
        libraryMediator = libraryMediator,
    )

    init {
        TelegramNotifier(
            chatRepository,
            telegramRepository,
            mavenLibraryUpdatesMonitor,
            LibraryDataBaseRepository(),
            TelegramButtonBuilder(),
            CoroutineScope(Dispatchers.Default + SupervisorJob()),
        )
    }
}
