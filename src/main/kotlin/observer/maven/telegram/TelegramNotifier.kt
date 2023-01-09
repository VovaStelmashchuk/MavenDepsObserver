package observer.maven.telegram

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import observer.maven.database.Library
import observer.maven.library.LibraryUpdatesMonitor
import observer.maven.library.MavenLibraryUpdatesMonitor
import observer.maven.telegram.rest.TelegramMessageSender
import org.jetbrains.exposed.sql.transactions.transaction

class TelegramNotifier(
    private val telegramMessageSender: TelegramMessageSender,
    private val libraryUpdatesMonitor: LibraryUpdatesMonitor,
    scope: CoroutineScope,
) {

    init {
        transaction {
            scope.launch {
                libraryUpdatesMonitor.flow().collect {
                    processLibraryUpdate(it)
                }
            }
        }
    }

    private suspend fun processLibraryUpdate(libraryVersionChanges: MavenLibraryUpdatesMonitor.LibraryVersionChanges) {
        val library = transaction { Library[libraryVersionChanges.libraryId] }

        library.chats.forEach { chat ->
            telegramMessageSender.sendLibraryUpdates(library.id.value, chat.id.value)
        }
    }
}
