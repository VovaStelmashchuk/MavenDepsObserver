package observer.maven.maven.rest

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import observer.maven.maven.LibraryCoordinate
import org.junit.Test

class MavenUtilsTest {

    @Test
    fun `Verify build correctly group and artifact ids`() {
        buildMavenArtifactPath(LibraryCoordinate("com.example:example")) shouldBe "com/example/example"
    }

    @Test
    fun `Verify build un correctly group and artifact ids`() {
        shouldThrow<IllegalStateException> { buildMavenArtifactPath(LibraryCoordinate("com.example_example")) }
    }
}
