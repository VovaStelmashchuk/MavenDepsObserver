package observer.maven.library

import com.vdurmont.semver4j.Semver
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import observer.maven.Version
import observer.maven.database.Libraries
import observer.maven.database.Library
import observer.maven.maven.rest.MavenService
import observer.maven.maven.rest.buildMavenArtifactPath
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

interface LibraryUpdatesMonitor {
    fun flow(): Flow<MavenLibraryUpdatesMonitor.LibraryVersionChanges>
}

class MavenLibraryUpdatesMonitor(
    private val mavenService: MavenService,
    private val interval: Long,
) : LibraryUpdatesMonitor {

    private val flow: Flow<LibraryVersionChanges> = flow {
        while (true) {
            transaction {
                Library.all().toList()
            }
                .forEach {
                    val libraryVersionChanges = updateLibrary(it)
                    if (libraryVersionChanges.hasChanges()) {
                        emit(libraryVersionChanges)
                    }
                }

            delay(interval)
        }
    }

    override fun flow(): Flow<LibraryVersionChanges> {
        return flow
    }

    data class LibraryVersionChanges(
        val libraryId: Int,
        val stable: Semver.VersionDiff,
        val unstable: Semver.VersionDiff,
    ) {
        fun hasChanges(): Boolean {
            return stable != Semver.VersionDiff.NONE || unstable != Semver.VersionDiff.NONE
        }
    }

    private suspend fun updateLibrary(library: Library): LibraryVersionChanges {
        val libraryMetaData = mavenService.getLibrary(buildMavenArtifactPath(library.libraryCoordinate))
        val mavenLastVersion = libraryMetaData.versions.getMax()
        val mavenLastStableVersion = libraryMetaData.versions.getMaxStable()

        return LibraryVersionChanges(
            libraryId = library.id.value,
            stable = updateVersionIfNeed(
                library.libraryCoordinate,
                library.lastStableVersion,
                mavenLastStableVersion,
                Libraries.lastStableVersion
            ),
            unstable = updateVersionIfNeed(
                library.libraryCoordinate,
                library.lastVersion,
                mavenLastVersion,
                Libraries.lastVersion
            ),
        )
    }

    private fun updateVersionIfNeed(
        coordinate: String,
        localVersion: String,
        mavenVersion: Version,
        column: Column<String>
    ): Semver.VersionDiff {
        return if (Version(localVersion) != mavenVersion) {
            Libraries.update({ Libraries.libraryCoordinate eq coordinate }) {
                it[column] = mavenVersion.toString()
            }
            mavenVersion.diff(Version(localVersion))
        } else {
            Semver.VersionDiff.NONE
        }
    }
}
