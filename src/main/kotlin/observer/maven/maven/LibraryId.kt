package observer.maven.maven

@JvmInline
value class LibraryId(val value: String) {
    override fun toString(): String {
        return value
    }
}
