package observer.maven.maven.rest

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import observer.maven.maven.LibraryId
import org.junit.Test

class MavenUtilsTest {

    @Test
    fun `Verify build correctly group and artifact ids`() {
        buildMavenArtifactPath(LibraryId("com.example:example")) shouldBe "com/example/example"
    }

    @Test
    fun `Verify build un correctly group and artifact ids`() {
        shouldThrow<IllegalStateException> { buildMavenArtifactPath(LibraryId("com.example_example")) }
    }
}
