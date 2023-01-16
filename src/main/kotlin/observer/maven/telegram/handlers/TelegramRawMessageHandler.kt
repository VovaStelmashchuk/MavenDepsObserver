package observer.maven.telegram.handlers

import observer.maven.maven.LibraryCoordinate

class TelegramRawMessageHandler {

    fun handle(message: String): List<LibraryCoordinate> {
        if (message.isBlank()) {
            return emptyList()
        }
        val rows = message.split("\n")

        return rows
            .map { it.replace(" ", "") }
            .mapNotNull { row ->
                val transformedRow = if (row.startsWith("implementation")) {
                    row.removePrefix("implementation")
                        .removePrefix("(\"")
                        .removeSuffix("\")")
                        .removePrefix("'")
                        .removeSuffix("'")
                } else {
                    row
                }
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
            }
    }
}
