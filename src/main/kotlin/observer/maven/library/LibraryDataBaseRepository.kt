package observer.maven.library

import observer.maven.Version
import observer.maven.maven.LibraryId
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class LibraryDataBaseRepository {

    fun add(libraryId: LibraryId, lastStableVersion: Version, lastVersion: Version): Library {
        transaction {
            Libraries.insert {
                it[Libraries.libraryId] = libraryId.value
                it[Libraries.lastStableVersion] = lastStableVersion.value
                it[Libraries.lastVersion] = lastVersion.value
            }
        }
        return get(libraryId)!!
    }

    fun get(libraryId: Long): Library {
        return transaction {
            val row = Libraries.select { Libraries.id eq libraryId }.first()

            Library(
                id = row[Libraries.id],
                libraryId = LibraryId(row[Libraries.libraryId]),
                lastStableVersion = Version(row[Libraries.lastStableVersion]),
                lastVersion = Version(row[Libraries.lastVersion]),
            )
        }
    }

    fun get(libraryId: LibraryId): Library? {
        return transaction {
            Libraries.select { Libraries.libraryId eq libraryId.value }
                .map {
                    Library(
                        it[Libraries.id],
                        LibraryId(it[Libraries.libraryId]),
                        Version(it[Libraries.lastStableVersion]),
                        Version(it[Libraries.lastVersion])
                    )
                }
                .firstOrNull()
        }
    }

    fun updateLibraryVersion(libraryId: LibraryId, version: Version, column: Column<String>) {
        transaction {
            Libraries.update(where = { Libraries.libraryId eq libraryId.value }) {
                it[column] = version.value
            }
        }
    }

    fun getAll(): List<Library> {
        return transaction {
            Libraries.selectAll()
                .map {
                    Library(
                        it[Libraries.id],
                        LibraryId(it[Libraries.libraryId]),
                        Version(it[Libraries.lastStableVersion]),
                        Version(it[Libraries.lastVersion])
                    )
                }
        }
    }
}

object Libraries : Table("libraries") {
    val id = long("id").autoIncrement()
    val libraryId = text("library_id")
    val lastStableVersion = text("last_stable_version")
    val lastVersion = text("last_version")
}
