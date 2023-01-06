package observer.maven

import com.vdurmont.semver4j.Semver
import com.vdurmont.semver4j.SemverException
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class Version(val value: String) : Comparable<Version> {

    val isStable: Boolean
        get() = try {
            Semver(value).isStable
        } catch (_: SemverException) {
            false
        }

    override operator fun compareTo(other: Version): Int {
        return try {
            Semver(value).compareTo(Semver(other.value))
        } catch (_: SemverException) {
            return value.compareTo(other.value)
        }
    }

    fun diff(other: Version): Semver.VersionDiff {
        return Semver(value).diff(Semver(other.value))
    }

    override fun toString(): String {
        return value
    }
}
