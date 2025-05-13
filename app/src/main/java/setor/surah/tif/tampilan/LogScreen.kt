package setor.surah.tif.tampilan

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import setor.surah.tif.model.SetoranLog
import setor.surah.tif.viewmodel.DashboardState
import setor.surah.tif.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(navController: NavController) {
    val context = LocalContext.current
    val dashboardViewModel: HomeViewModel = viewModel(factory = HomeViewModel.getFactory(context))
    val dashboardState by dashboardViewModel.dashboardState.collectAsState()
    val TAG = "LogScreen"

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
            text = "Log Setoran",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        when (val state = dashboardState) {
            is DashboardState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is DashboardState.Success -> {
                val data = state.data.data
                val logs = data.setoran.log
                Log.d(TAG, "Jumlah log setoran: ${logs.size}")
                // Ringkasan Progres Setoran
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Ringkasan Progres",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Progres Keseluruhan: ${data.setoran.info_dasar.persentase_progres_setor}%",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = data.setoran.info_dasar.persentase_progres_setor / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Terakhir Setor: ${data.setoran.info_dasar.tgl_terakhir_setor ?: "Belum ada setoran"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                if (logs.isEmpty()) {
                    Text(
                        text = "Belum ada aktivitas setoran. Silakan lakukan setoran pertama Anda!",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn {
                        items(logs) { log ->
                            LogItemCard(log, data.setoran.ringkasan)
                        }
                    }
                }
            }
            is DashboardState.Error -> {
                Log.e(TAG, "Error: ${state.message}")
                Text(
                    text = "Gagal memuat log: ${state.message}",
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
fun LogItemCard(log: SetoranLog, ringkasan: List<setor.surah.tif.model.Ringkasan>) {
    // Tentukan apakah aksi bersifat positif (misalnya, validasi setoran)
    val isPositiveAction = log.aksi.contains("validasi", ignoreCase = true) || log.aksi.contains("setor", ignoreCase = true)
    // Cari label yang relevan dari ringkasan (misalnya, berdasarkan keterangan log)
    val relatedLabel = ringkasan.find { log.keterangan.contains(it.label, ignoreCase = true) }?.label ?: "Umum"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPositiveAction) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ikon berdasarkan jenis aksi
            Icon(
                imageVector = if (isPositiveAction) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = if (isPositiveAction) "Aksi Positif" else "Aksi Lain",
                tint = if (isPositiveAction) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Aksi: ${log.aksi}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = relatedLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Keterangan: ${log.keterangan}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Tanggal: ${log.timestamp}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Dosen: ${log.dosen_yang_mengesahkan.nama}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}