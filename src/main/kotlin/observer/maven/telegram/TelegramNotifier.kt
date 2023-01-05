package observer.maven.telegram

import com.vdurmont.semver4j.Semver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import observer.maven.library.LibraryDataBaseRepository
import observer.maven.library.LibraryUpdatesMonitor
import observer.maven.library.MavenLibraryUpdatesMonitor
import observer.maven.telegram.rest.TelegramRepository

class TelegramNotifier(
    private val chatRepository: ChatRepository,
    private val telegramRepository: TelegramRepository,
    private val libraryUpdatesMonitor: LibraryUpdatesMonitor,
    private val libraryDataBaseRepository: LibraryDataBaseRepository,
    private val telegramButtonBuilder: TelegramButtonBuilder,
    scope: CoroutineScope,
) {

    init {
        scope.launch {
            libraryUpdatesMonitor.flow().collect {
                processLibraryUpdate(it)
            }
        }
    }

    private suspend fun processLibraryUpdate(libraryVersionChanges: MavenLibraryUpdatesMonitor.LibraryVersionChanges) {
        val library = libraryDataBaseRepository.get(libraryVersionChanges.libraryId)

        chatRepository.getChats(libraryVersionChanges.libraryId).forEach { chat ->
            val text = when {
                chat.observingStrategy == ChatRepository.ObservableStrategy.ALL ->
                    "New version of ${library.libraryId.value} available: ${library.lastVersion}"

                chat.observingStrategy == ChatRepository.ObservableStrategy.STABLE
                        && libraryVersionChanges.stable in listOf(
                    Semver.VersionDiff.MAJOR,
                    Semver.VersionDiff.MINOR,
                    Semver.VersionDiff.PATCH
                ) -> {
                    "New version of ${library.libraryId.value} available: ${library.lastStableVersion}"
                }

                chat.observingStrategy == ChatRepository.ObservableStrategy.MAJOR
                        && libraryVersionChanges.stable == Semver.VersionDiff.MAJOR -> {
                    "New version of ${library.libraryId.value} available: ${library.lastVersion}"
                }

                else -> null
            }

            text?.let {
                val buttons = telegramButtonBuilder.buildButton(chat.observingStrategy, library.libraryId)
                telegramRepository.sendMessage(chat.chatId, text, buttons)
            }
        }
    }
}

