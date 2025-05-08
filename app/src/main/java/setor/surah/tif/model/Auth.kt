package setor.surah.tif.model

data class Auth(
    val access_token: String,
    val refresh_token: String,
    val id_token: String,
    val expires_in: Int,
    val refresh_expires_in: Int,
    val token_type: String,
    val scope: String
)