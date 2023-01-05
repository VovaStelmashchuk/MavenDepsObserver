package observer.maven.library

import observer.maven.Version
import observer.maven.maven.LibraryId

data class Library(
    val id: Long,
    val libraryId: LibraryId,
    val lastStableVersion: Version,
    val lastVersion: Version,
)