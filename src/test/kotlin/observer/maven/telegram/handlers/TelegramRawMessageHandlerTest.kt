package observer.maven.telegram.handlers

import observer.maven.maven.LibraryCoordinate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TelegramRawMessageHandlerTest {

    @Test
    fun `should return empty list when message is empty`() {
        val handler = TelegramRawMessageHandler()
        val result = handler.handle("")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return empty list when message numbers`() {
        val handler = TelegramRawMessageHandler()
        val result = handler.handle("12")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return empty list when message is blank`() {
        val handler = TelegramRawMessageHandler()
        val result = handler.handle(" ")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return empty list when message is not a library coordinate`() {
        val handler = TelegramRawMessageHandler()
        val result = handler.handle("not a library coordinate")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return library coordinate when message is a library coordinate with version`() {
        val handler = TelegramRawMessageHandler()
        val result = handler.handle("org.jetbrains.kotlin:kotlin-stdlib:1.4.10")
        assertEquals(listOf(LibraryCoordinate("org.jetbrains.kotlin:kotlin-stdlib")), result)
    }

    @Test
    fun `should return library coordinate when message is a library coordinate without version`() {
        val handler = TelegramRawMessageHandler()
        val result = handler.handle("org.jetbrains.kotlin:kotlin-stdlib")
        assertEquals(listOf(LibraryCoordinate("org.jetbrains.kotlin:kotlin-stdlib")), result)
    }

    @Test
    fun `should return library coordinate when message has block of gradle dependencies block kotlin`() {
        val handler = TelegramRawMessageHandler()
        val result = handler.handle(
            """
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.10")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
            }
            """.trimIndent()
        )

        assertEquals(
            listOf(
                LibraryCoordinate("org.jetbrains.kotlin:kotlin-stdlib"),
                LibraryCoordinate("org.jetbrains.kotlinx:kotlinx-coroutines-core"),
            ),
            result
        )
    }

    @Test
    fun `should return library coordinate when message has block of gradle dependencies block groovy`() {
        val handler = TelegramRawMessageHandler()
        val result = handler.handle(
            """
            dependencies {
                implementation 'org.jetbrains.kotlin:kotlin-stdlib:1.4.10'
                implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2'
            }
            """.trimIndent()
        )

        assertEquals(
            listOf(
                LibraryCoordinate("org.jetbrains.kotlin:kotlin-stdlib"),
                LibraryCoordinate("org.jetbrains.kotlinx:kotlinx-coroutines-core"),
            ),
            result
        )
    }

    @Test
    fun `should return library coordinate when message has block of gradle dependencies part of block kotlin`() {
        val handler = TelegramRawMessageHandler()
        val result = handler.handle(
            """
                implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.10")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
            """.trimIndent()
        )

        assertEquals(
            listOf(
                LibraryCoordinate("org.jetbrains.kotlin:kotlin-stdlib"),
                LibraryCoordinate("org.jetbrains.kotlinx:kotlinx-coroutines-core"),
            ),
            result
        )
    }

    @Test
    fun `should return library coordinate when message has implementation by grop and name`() {
        val handler = TelegramRawMessageHandler()
        val result = handler.handle(
            """
                implementation("org.jetbrains.exposed", "exposed-core", "0.38.2")
                implementation("org.jetbrains.exposed", "exposed-dao", "0.38.2")
            """.trimIndent()
        )

        assertEquals(
            listOf(
                LibraryCoordinate("org.jetbrains.exposed:exposed-core"),
                LibraryCoordinate("org.jetbrains.exposed:exposed-dao"),
            ),
            result
        )
    }

    @Test
    fun `should return library coordinate when message has testImplementation`() {
        val handler = TelegramRawMessageHandler()
        val result = handler.handle(
            """
                testImplementation("io.mockk:mockk:1.13.2")
                testImplementation("io.kotest:kotest-runner-junit5:5.5.4")
            """.trimIndent()
        )

        assertEquals(
            listOf(
                LibraryCoordinate("io.mockk:mockk"),
                LibraryCoordinate("io.kotest:kotest-runner-junit5"),
            ),
            result
        )
    }
}
