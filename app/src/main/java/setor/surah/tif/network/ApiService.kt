package setor.surah.tif.network

import setor.surah.tif.model.Auth
import retrofit2.Response
import retrofit2.http.*
import setor.surah.tif.model.SetoranResponse

interface ApiService {

    @FormUrlEncoded
    @POST("/realms/dev/protocol/openid-connect/token")
    suspend fun login(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("scope") scope: String
    ): Response<Auth>

    @FormUrlEncoded
    @POST("/realms/dev/protocol/openid-connect/token")
    suspend fun refreshToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String
    ): Response<Auth>

    // Endpoint setoran-saya tanpa parameter apikey
    @GET("mahasiswa/setoran-saya")
    suspend fun getSetoranSaya(
        @Header("Authorization") token: String
    ): Response<SetoranResponse>
}