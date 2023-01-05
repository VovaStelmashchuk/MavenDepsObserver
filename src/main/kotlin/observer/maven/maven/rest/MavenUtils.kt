package observer.maven.maven.rest

import observer.maven.maven.LibraryId

fun buildMavenArtifactPath(libraryId: LibraryId): String {
    val array = libraryId.value.split(":")
    check(array.size == 2) { "Invalid library id: $libraryId" }
    val groupIdPath = array[0].replace(".", "/")
    val artifactId = array[1]
    return buildString {
        append(groupIdPath)
        append("/")
        append(artifactId)
    }
}
