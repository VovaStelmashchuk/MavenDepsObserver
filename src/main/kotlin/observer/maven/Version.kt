package observer.maven

import com.vdurmont.semver4j.Semver
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class Version(val value: String) : Comparable<Version> {

    val isStable: Boolean
        get() = Semver(value).isStable

    override operator fun compareTo(other: Version): Int {
        return Semver(value).compareTo(Semver(other.value))
    }

    fun diff(other: Version): Semver.VersionDiff {
        return Semver(value).diff(Semver(other.value))
    }

    override fun toString(): String {
        return value
    }
}