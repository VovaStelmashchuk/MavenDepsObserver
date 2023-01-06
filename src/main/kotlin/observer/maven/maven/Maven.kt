package observer.maven.maven

import observer.maven.library.Library
import observer.maven.library.LibraryDataBaseRepository
import observer.maven.maven.rest.MavenService
import observer.maven.maven.rest.buildMavenArtifactPath
import retrofit2.HttpException

class Maven(
    private val mavenService: MavenService,
    private val libraryDataBaseRepository: LibraryDataBaseRepository,
) {

    sealed class AddLibraryResult {
        data class LibraryAdded(val library: Library) : AddLibraryResult()

        data class LibraryAlreadyExist(val library: Library) : AddLibraryResult()

        object InValidLibraryId : AddLibraryResult()
        object LibraryNotFoundAdded : AddLibraryResult()
        object MavenCentralUnAvailable : AddLibraryResult()
    }

    suspend fun add(libraryId: LibraryId): AddLibraryResult {
        return try {
            val library = libraryDataBaseRepository.get(libraryId)

            if (library != null) {
                return AddLibraryResult.LibraryAlreadyExist(library)
            } else {
                val libraryMetaData = mavenService.getLibrary(buildMavenArtifactPath(libraryId))
                val lastVersion = libraryMetaData.versions.getMax()
                val lastStableVersion = libraryMetaData.versions.getMaxStable()

                val result = libraryDataBaseRepository.add(libraryId, lastStableVersion, lastVersion)

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