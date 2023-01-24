package observer.maven.telegram.handlers

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class UpdateVerifier {

    fun isUpdateValid(updateId: Long): Boolean {
        val result = transaction {
            return@transaction HandledUpdates.select { HandledUpdates.updateId eq updateId }.count()
        }

        if (result == 0L) {
            transaction {
                HandledUpdates.insert {
                    it[HandledUpdates.updateId] = updateId
                }
            }
        }
        return result == 0L
    }
}

object HandledUpdates : Table() {
    val updateId = long("update_id")
}
