package observer.maven.endtoend

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import observer.maven.Version
import observer.maven.maven.rest.MavenMetaData
import observer.maven.maven.rest.MavenService
import observer.maven.plugins.configureSerialization
import observer.maven.telegram.TelegramComponent
import observer.maven.telegram.configureTelegramInputController
import observer.maven.telegram.rest.TelegramChatId
import observer.maven.telegram.rest.TelegramSendMessageCommand
import observer.maven.telegram.rest.TelegramService
import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class VerifyLibraryUpdates {

    @Suppress("MemberVisibilityCanBePrivate")
    val database =
        Database.connect("jdbc:h2:mem:test_db_22;DB_CLOSE_DELAY=-1;IGNORECASE=true;")

    @Test
    fun `Verify library add and updates`() {
        @Language("json")
        val telegramHook = """
            {
              "update_id": 132326919,
              "message": {
                "message_id": 89,
                "from": {
                  "id": 88512517,
                  "is_bot": false,
                  "first_name": "Volodymyr",
                  "last_name": "Stelmashchuk",
                  "username": "smallstells",
                  "language_code": "en",
                  "is_premium": true
                },
                "chat": {
                  "id": 88512517,
                  "first_name": "Volodymyr",
                  "last_name": "Stelmashchuk",
                  "username": "smallstells",
                  "type": "private"
                },
                "date": 1673000043,
                "text": "org.postgresql:postgresql"
              }
            }
        """.trimIndent()

        val telegramService = mockk<TelegramService>()

        val mavenService = mockk<MavenService>()

        testApplication {
            application {
                configureSerialization()
                val telegramComponent = TelegramComponent(
                    1000,
                    telegramService,
                    mavenService,
                    this,
                )
                configureTelegramInputController(telegramComponent)
            }

            client.post("/handleTelegramCommand") {
                setBody(telegramHook)
            }

            coEvery {
                mavenService.getLibrary("org/postgresql/postgresql")
            } returnsMany (listOf(
                MavenMetaData(
                    versions = MavenMetaData.MavenVersion(
                        listOf(
                            Version("1.0.0"),
                            Version("1.0.1"),
                        )
                    )
                ),
                MavenMetaData(
                    versions = MavenMetaData.MavenVersion(
                        listOf(
                            Version("1.0.0"),
                            Version("1.0.1"),
                            Version("1.0.2"),
                        )
                    )
                )
            ))

            coVerify {
                telegramService.sendMessage(
                    TelegramSendMessageCommand(
                        chatId = TelegramChatId(88512517),
                        text = "org.postgresql:postgresql:1.0.1",
                        replyMarkup = any(),
                    )
                )
            }
        }
    }

    @AfterEach
    fun tearDown() {
        TransactionManager.closeAndUnregister(database)
    }

}