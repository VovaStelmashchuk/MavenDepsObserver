package observer.maven.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

object TelegramChats : IntIdTable("telegram_chats") {
    val chatId = long("chat_id")
    val language = text("language")
}

class TelegramChat(
    id: EntityID<Int>
) : IntEntity(id) {
    var chatId: Long by TelegramChats.chatId
    var language by TelegramChats.language

    var libraries by Library via ChatLibrary

    companion object : IntEntityClass<TelegramChat>(TelegramChats)
}

object Libraries : IntIdTable("libraries") {
    val libraryCoordinate = text("library_coordinate")
    val lastStableVersion = text("last_stable_version")
    val lastVersion = text("last_version")
}

class Library(
    id: EntityID<Int>,
) : IntEntity(id) {
    var libraryCoordinate: String by Libraries.libraryCoordinate
    var lastStableVersion: String by Libraries.lastStableVersion
    var lastVersion: String by Libraries.lastVersion
    var chats by TelegramChat via ChatLibrary

    companion object : IntEntityClass<Library>(Libraries)
}

object ChatLibrary : Table() {
    val chat = reference("chat", TelegramChats)
    val library = reference("library", Libraries)
    override val primaryKey = PrimaryKey(chat, library, name = "PK_ChatLibrary")
}