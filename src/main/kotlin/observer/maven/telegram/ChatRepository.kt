package observer.maven.telegram

import observer.maven.library.Libraries
import observer.maven.maven.LibraryId
import observer.maven.telegram.rest.TelegramChatId
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class ChatRepository {

    enum class ObservableStrategy {
        ALL,
        STABLE,
        MAJOR,
    }

    fun getChats(libraryId: Long): List<TelegramChatLibraryInfo> {
        return transaction {
            ChatToLibraries
                .select { ChatToLibraries.libraryId eq libraryId }
                .map {
                    TelegramChatLibraryInfo(
                        chatId = TelegramChatId(it[ChatToLibraries.chatId]),
                        libraryId = it[ChatToLibraries.libraryId],
                        observingStrategy = it[ChatToLibraries.observingStrategy],
                    )
                }
        }
    }

    fun removeChat(chatId: TelegramChatId, libraryId: LibraryId) {
        transaction {
            Libraries.select { Libraries.libraryId eq libraryId.value }
                .firstOrNull()
                ?.let {
                    ChatToLibraries.deleteWhere {
                        (ChatToLibraries.chatId eq chatId.id) and
                                (ChatToLibraries.libraryId eq it[Libraries.id])
                    }
                }
        }
    }

    fun changeObservability(chatId: TelegramChatId, libraryId: LibraryId, observingStrategy: ObservableStrategy) {
        transaction {
            Libraries.select { Libraries.libraryId eq libraryId.value }
                .firstOrNull()
                ?.let {
                    ChatToLibraries
                        .update({
                            (ChatToLibraries.chatId eq chatId.id) and
                                    (ChatToLibraries.libraryId eq it[Libraries.id])
                        }) {
                            it[ChatToLibraries.observingStrategy] = observingStrategy
                        }
                }
        }
    }

    fun attachLibrary(
        chatId: TelegramChatId,
        libraryId: Long,
        observingStrategy: ObservableStrategy,
    ) {
        transaction {
            if (ChatToLibraries
                    .select { (ChatToLibraries.chatId eq chatId.id) and (ChatToLibraries.libraryId eq libraryId) }
                    .count() == 0L
            ) {
                ChatToLibraries.insert {
                    it[ChatToLibraries.chatId] = chatId.id
                    it[ChatToLibraries.libraryId] = libraryId
                    it[ChatToLibraries.observingStrategy] = observingStrategy
                }
            }
        }
    }
}

data class TelegramChatLibraryInfo(
    val chatId: TelegramChatId,
    val libraryId: Long,
    val observingStrategy: ChatRepository.ObservableStrategy,
)

object ChatToLibraries : Table("chattolibraries") {
    val chatId = long("chat_id")
    val libraryId = long("library_id")
    val observingStrategy = enumeration<ChatRepository.ObservableStrategy>("observing_strategy")
    override val primaryKey = PrimaryKey(chatId, libraryId)
}
