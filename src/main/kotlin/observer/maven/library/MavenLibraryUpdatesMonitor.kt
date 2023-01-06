package observer.maven.library

import com.vdurmont.semver4j.Semver
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import observer.maven.Version
import observer.maven.maven.LibraryId
import observer.maven.maven.rest.MavenService
import observer.maven.maven.rest.buildMavenArtifactPath
import org.jetbrains.exposed.sql.Column

interface LibraryUpdatesMonitor {
    fun flow(): Flow<MavenLibraryUpdatesMonitor.LibraryVersionChanges>
}

class MavenLibraryUpdatesMonitor(
    private val libraryDataBaseRepository: LibraryDataBaseRepository,
    private val mavenService: MavenService,
    private val interval: Long,
) : LibraryUpdatesMonitor {

    private val flow: Flow<LibraryVersionChanges> = flow {
        while (true) {
            libraryDataBaseRepository.getAll().forEach {
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
        val libraryId: Long,
        val stable: Semver.VersionDiff,
        val unstable: Semver.VersionDiff,
    ) {
        fun hasChanges(): Boolean {
            return stable != Semver.VersionDiff.NONE || unstable != Semver.VersionDiff.NONE
        }
    }

    private suspend fun updateLibrary(library: Library): LibraryVersionChanges {
        val libraryMetaData = mavenService.getLibrary(buildMavenArtifactPath(library.libraryId))
        val mavenLastVersion = libraryMetaData.versions.getMax()
        val mavenLastStableVersion = libraryMetaData.versions.getMaxStable()

        return LibraryVersionChanges(
            libraryId = library.id,
            stable = updateVersionIfNeed(
                library.libraryId,
                library.lastStableVersion,
                mavenLastStableVersion,
                Libraries.lastStableVersion
            ),
            unstable = updateVersionIfNeed(
                library.libraryId,
                library.lastVersion,
                mavenLastVersion,
                Libraries.lastVersion
            ),
        )
    }

    private fun updateVersionIfNeed(
        id: LibraryId,
        localVersion: Version,
        mavenVersion: Version,
        column: Column<String>
    ): Semver.VersionDiff {
        return if (localVersion != mavenVersion) {
            libraryDataBaseRepository.updateLibraryVersion(
                id,
                mavenVersion,
                column
            )

            mavenVersion.diff(localVersion)
        } else {
            Semver.VersionDiff.NONE
        }
    }
}
