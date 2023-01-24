package observer.maven.maven.rest

import observer.maven.maven.LibraryCoordinate

fun buildMavenArtifactPath(libraryId: LibraryCoordinate): String {
    return buildMavenArtifactPath(libraryId.value)
}

fun buildMavenArtifactPath(libraryId: String): String {
    val array = libraryId.split(":")
    check(array.size == 2) { "Invalid library id: $libraryId" }
    val groupIdPath = array[0].replace(".", "/")
    val artifactId = array[1]
    return buildString {
        append(groupIdPath)
        append("/")
        append(artifactId)
    }
}
