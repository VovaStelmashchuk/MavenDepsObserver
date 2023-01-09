package observer.maven.maven

import observer.maven.database.Libraries
import observer.maven.database.Library
import observer.maven.maven.rest.MavenService
import observer.maven.maven.rest.buildMavenArtifactPath
import org.jetbrains.exposed.sql.transactions.transaction
import retrofit2.HttpException

class Maven(
    private val mavenService: MavenService,
) {

    sealed class AddLibraryResult {
        data class LibraryAdded(val library: Library) : AddLibraryResult()

        data class LibraryAlreadyExist(val library: Library) : AddLibraryResult()

        object InValidLibraryId : AddLibraryResult()
        object LibraryNotFoundAdded : AddLibraryResult()
        object MavenCentralUnAvailable : AddLibraryResult()
    }

    suspend fun add(libraryCoordinate: LibraryCoordinate): AddLibraryResult {
        return try {
            val library = transaction {
                Library.find {
                    Libraries.libraryCoordinate eq libraryCoordinate.value
                }.firstOrNull()
            }

            if (library != null) {
                return AddLibraryResult.LibraryAlreadyExist(library)
            } else {
                val libraryMetaData =
                    mavenService.getLibrary(buildMavenArtifactPath(libraryCoordinate))
                val lastVersion = libraryMetaData.versions.getMax()
                val lastStableVersion = libraryMetaData.versions.getMaxStable()

                val result = transaction {
                    Library.new {
                        this.libraryCoordinate = libraryCoordinate.value
                        this.lastStableVersion = lastStableVersion.value
                        this.lastVersion = lastVersion.value
                    }
                }

                AddLibraryResult.LibraryAdded(result)
            }
        } catch (error: IllegalStateException) {
            println(error)
            AddLibraryResult.InValidLibraryId
        } catch (error: HttpException) {
            println(error)
            if (error.code() == NOT_FOUND_CODE) {
                AddLibraryResult.LibraryNotFoundAdded
            } else {
                AddLibraryResult.MavenCentralUnAvailable
            }
        }
    }

    private companion object {
        const val NOT_FOUND_CODE = 404
    }
}
