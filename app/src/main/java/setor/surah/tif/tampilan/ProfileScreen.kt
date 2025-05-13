package setor.surah.tif.tampilan

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import setor.surah.tif.viewmodel.DashboardState
import setor.surah.tif.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val dashboardViewModel: HomeViewModel = viewModel(factory = HomeViewModel.getFactory(context))
    val dashboardState by dashboardViewModel.dashboardState.collectAsState()
    val TAG = "ProfileScreen"

    // Panggil fetchSetoranSaya saat layar dibuka
    LaunchedEffect(Unit) {
        Log.d(TAG, "Memulai pengambilan data setoran")
        dashboardViewModel.fetchSetoranSaya()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Profil Mahasiswa",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        when (val state = dashboardState) {
            is DashboardState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is DashboardState.Success -> {
                val info = state.data.data.info
                Log.d(TAG, "Data profil: $info")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Informasi Pribadi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoRow(label = "Nama", value = info.nama)
                        InfoRow(label = "NIM", value = info.nim)
                        InfoRow(label = "Email", value = info.email)
                        InfoRow(label = "Angkatan", value = info.angkatan)
                        InfoRow(label = "Semester", value = info.semester.toString())
                        InfoRow(label = "Dosen Pembimbing", value = info.dosen_pa.nama)
                        InfoRow(label = "Email Dosen", value = info.dosen_pa.email)
                    }
                }
            }
            is DashboardState.Error -> {
                Log.e(TAG, "Error: ${state.message}")
                Text(
                    text = "Gagal memuat profil: ${state.message}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is DashboardState.Idle -> {
                Log.d(TAG, "Status idle, belum ada data")
                Text(
                    text = "Menunggu data...",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value ?: "-",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
