package observer.maven.maven.rest

import retrofit2.http.GET
import retrofit2.http.Path

interface MavenService {

    @GET("{path}/maven-metadata.xml")
    suspend fun getLibrary(@Path("path") path: String): MavenMetaData
}
