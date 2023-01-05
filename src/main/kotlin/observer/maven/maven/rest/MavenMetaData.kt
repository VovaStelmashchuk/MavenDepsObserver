package observer.maven.maven.rest

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import observer.maven.Version

@Serializable
@XmlSerialName("metadata", "", "")
data class MavenMetaData(
    @XmlSerialName("versioning", "", "")
    val versions: MavenVersion,
) {
    @Serializable
    data class MavenVersion(
        @XmlChildrenName("version", "", "")
        val versions: List<Version>,
    )
}