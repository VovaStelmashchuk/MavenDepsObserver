package observer.maven.telegram.handlers

import observer.maven.maven.LibraryCoordinate
import java.util.Locale

class TelegramRawMessageHandler {

    fun handle(message: String): List<LibraryCoordinate> {
        if (message.isBlank()) {
            return emptyList()
        }
        val rows = message.split("\n")

        return rows
            .map { it.replace(" ", "") }
            .mapNotNull { row ->
                val transformedRow = row
                    .lowercase(Locale.US)
                    .removePrefix("implementation")
                    .removePrefix("api")
                    .removePrefix("testimplementation")
                    .removePrefix("(\"")
                    .removeSuffix("\")")
                    .removePrefix("'")
                    .removeSuffix("'")

                if (transformedRow.contains(":")) {
                    val array = transformedRow.split(":")
                    if (array.size >= 2) {
                        LibraryCoordinate(buildString {
                            append(array[0])
                            append(":")
                            append(array[1])
                        })
                    } else {
                        null
                    }
                } else {
                    if (transformedRow.contains("\",\"")) {
                        val array = transformedRow.split("\",\"")
                        if (array.size >= 2) {
                            LibraryCoordinate(buildString {
                                append(array[0])
                                append(":")
                                append(array[1])
                            })
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                }
            }
    }
}
