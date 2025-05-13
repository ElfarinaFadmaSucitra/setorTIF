package setor.surah.tif.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.auth0.android.jwt.JWT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import setor.surah.tif.model.SetoranResponse
import setor.surah.tif.network.RetrofitInstance
import setor.surah.tif.network.Token

class HomeViewModel(private val tokenManager: Token) : ViewModel() {

    private val _dashboardState = MutableStateFlow<DashboardState>(DashboardState.Idle)
    val dashboardState: StateFlow<DashboardState> = _dashboardState
    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName
    private val TAG = "HomeViewModel"

    init {
        val idToken = tokenManager.getIdToken()
        if (idToken != null) {
            try {
                val decodedJwt = JWT(idToken)
                val name = decodedJwt.getClaim("name").asString()
                    ?: decodedJwt.getClaim("preferred_username").asString()
                    ?: "Tidak Diketahui"
                _userName.value = name
                Log.d(TAG, "Nama pengguna dari id_token: $name")
            } catch (e: Exception) {
                Log.e(TAG, "Gagal memparsing id_token: ${e.message}")
                _userName.value = "Tidak Diketahui"
            }
        } else {
            Log.w(TAG, "id_token tidak ditemukan")
            _userName.value = "Tidak Diketahui"
        }
    }

    fun fetchSetoranSaya() {
        viewModelScope.launch {
            _dashboardState.value = DashboardState.Loading
            try {
                val token = tokenManager.getAccessToken()
                if (token == null) {
                    Log.e(TAG, "Access token tidak ditemukan")
                    _dashboardState.value = DashboardState.Error("Otentikasi diperlukan")
                    return@launch
                }

                val response = RetrofitInstance.apiService.getSetoranSaya(
                    token = "Bearer $token"
                )

                if (response.isSuccessful) {
                    response.body()?.let { setoran ->
                        Log.d(TAG, "Data setoran berhasil diambil: ${setoran.message}")
                        _dashboardState.value = DashboardState.Success(setoran)
                    } ?: run {
                        Log.e(TAG, "Respons kosong dari server")
                        _dashboardState.value = DashboardState.Error("Respons kosong dari server")
                    }
                } else {
                    handleErrorResponse(response.code(), response.errorBody()?.string(), response.message())
                }
            } catch (e: HttpException) {
                Log.e(TAG, "Pengecualian HTTP: ${e.code()}, pesan: ${e.message()}")
                _dashboardState.value = DashboardState.Error("Kesalahan HTTP: ${e.message()} (Kode: ${e.code()})")
            } catch (e: Exception) {
                Log.e(TAG, "Pengecualian saat mengambil data: ${e.message}", e)
                _dashboardState.value = DashboardState.Error("Kesalahan jaringan: ${e.message}")
            }
        }
    }

    private fun handleErrorResponse(code: Int, errorBody: String?, message: String) {
        when (code) {
            401 -> {
                Log.w(TAG, "Token tidak valid, mencoba refresh token")
                refreshTokenAndRetry()
            }
            403 -> {
                Log.e(TAG, "Akses ditolak: $errorBody")
                _dashboardState.value = DashboardState.Error("Akses ditolak: Tidak diotorisasi (Kode: 403)")
            }
            404 -> {
                Log.e(TAG, "Endpoint tidak ditemukan: $message")
                _dashboardState.value = DashboardState.Error("Endpoint tidak ditemukan (Kode: 404)")
            }
            else -> {
                Log.e(TAG, "Gagal mengambil data: $message, kode: $code, body: $errorBody")
                _dashboardState.value = DashboardState.Error("Gagal mengambil data: $message (Kode: $code)")
            }
        }
    }

    private fun refreshTokenAndRetry() {
        viewModelScope.launch {
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken == null) {
                Log.e(TAG, "Refresh token tidak ditemukan")
                _dashboardState.value = DashboardState.Error("Otentikasi diperlukan")
                return@launch
            }

            try {
                val refreshResponse = RetrofitInstance.kcApiService.refreshToken(
                    clientId = "setoran-mobile-dev",
                    clientSecret = "aqJp3xnXKudgC7RMOshEQP7ZoVKWzoSl",
                    grantType = "refresh_token",
                    refreshToken = refreshToken
                )

                if (refreshResponse.isSuccessful) {
                    refreshResponse.body()?.let { auth ->
                        Log.d(TAG, "Token berhasil diperbarui")
                        tokenManager.saveTokens(auth.access_token, auth.refresh_token, auth.id_token)
                        retryFetchSetoran(auth.access_token)
                    } ?: run {
                        Log.e(TAG, "Respons refresh kosong")
                        _dashboardState.value = DashboardState.Error("Gagal memperbarui token: Respons kosong")
                    }
                } else {
                    Log.e(TAG, "Gagal refresh token, kode: ${refreshResponse.code()}, pesan: ${refreshResponse.message()}")
                    _dashboardState.value = DashboardState.Error("Gagal memperbarui token (Kode: ${refreshResponse.code()})")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Pengecualian saat refresh token: ${e.message}")
                _dashboardState.value = DashboardState.Error("Gagal memperbarui token: ${e.message}")
            }
        }
    }

    private suspend fun retryFetchSetoran(accessToken: String) {
        try {
            val retryResponse = RetrofitInstance.apiService.getSetoranSaya(
                token = "Bearer $accessToken"
            )
            if (retryResponse.isSuccessful) {
                retryResponse.body()?.let { setoran ->
                    Log.d(TAG, "Data setoran berhasil diambil setelah refresh: ${setoran.message}")
                    _dashboardState.value = DashboardState.Success(setoran)
                } ?: run {
                    Log.e(TAG, "Respons kosong setelah refresh")
                    _dashboardState.value = DashboardState.Error("Respons kosong setelah refresh")
                }
            } else {
                Log.e(TAG, "Gagal setelah refresh, kode: ${retryResponse.code()}, pesan: ${retryResponse.message()}")
                _dashboardState.value = DashboardState.Error("Gagal mengambil data setelah refresh (Kode: ${retryResponse.code()})")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Pengecualian saat mencoba ulang: ${e.message}")
            _dashboardState.value = DashboardState.Error("Kesalahan jaringan setelah refresh: ${e.message}")
        }
    }

    companion object {
        fun getFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                        return HomeViewModel(Token(context)) as T
                    }
                    throw IllegalArgumentException("Kelas ViewModel tidak dikenal")
                }
            }
        }
    }
}

sealed class DashboardState {
    object Idle : DashboardState()
    object Loading : DashboardState()
    data class Success(val data: SetoranResponse) : DashboardState()
    data class Error(val message: String) : DashboardState()
}