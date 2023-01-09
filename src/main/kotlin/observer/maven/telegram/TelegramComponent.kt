package observer.maven.telegram

import com.github.aymanizz.ktori18n.i18n
import io.ktor.server.application.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import observer.maven.database.ChatLibrary
import observer.maven.database.Libraries
import observer.maven.database.TelegramChats
import observer.maven.library.LibraryMediator
import observer.maven.library.MavenLibraryUpdatesMonitor
import observer.maven.maven.Maven
import observer.maven.maven.rest.MavenService
import observer.maven.telegram.rest.TelegramMessageSender
import observer.maven.telegram.rest.TelegramService
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class TelegramComponent(
    syncInterval: Long,
    telegramService: TelegramService,
    mavenService: MavenService,
    application: Application,
) {

    private val telegramMessageSender = TelegramMessageSender(telegramService, application, application.i18n)

    private val maven = Maven(
        mavenService,
    )

    private val libraryMediator = LibraryMediator(maven, telegramMessageSender)

    private val mavenLibraryUpdatesMonitor = MavenLibraryUpdatesMonitor(
        mavenService,
        syncInterval,
    )


    val telegramCallbackHandler = TelegramCallbackHandler(telegramMessageSender)

    val messageHandler = TelegramMessageHandler(
        telegramMessageSender = telegramMessageSender,
        libraryMediator = libraryMediator,
    )

    init {
        transaction {
            SchemaUtils.create(Libraries, TelegramChats, ChatLibrary)
        }
        TelegramNotifier(
            telegramMessageSender,
            mavenLibraryUpdatesMonitor,
            CoroutineScope(Dispatchers.Default + SupervisorJob()),
        )
    }
}
