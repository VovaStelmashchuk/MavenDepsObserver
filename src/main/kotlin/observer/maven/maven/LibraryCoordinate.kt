package observer.maven.maven

@JvmInline
value class LibraryCoordinate(val value: String) {
    override fun toString(): String {
        return value
    }
}
