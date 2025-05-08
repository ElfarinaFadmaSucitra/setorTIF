package setor.surah.tif.tampilan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import setor.surah.tif.viewmodel.DashboardState
import setor.surah.tif.viewmodel.HomeViewModel
import setor.surah.tif.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val dashboardViewModel: HomeViewModel = viewModel(factory = HomeViewModel.getFactory(context))
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.getFactory(context))
    val dashboardState by dashboardViewModel.dashboardState.collectAsState()
    val userName by dashboardViewModel.userName.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        dashboardViewModel.fetchSetoranSaya()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Setoran Hafalan") },
                actions = {
                    IconButton(onClick = {
                        loginViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout"
                        )
                    }
                }
            )

        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                userName?.let {
                    Text(
                        text = "Selamat datang, $it!",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                when (val state = dashboardState) {
                    is DashboardState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    is DashboardState.Success -> {
                        val data = state.data.data
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Nama: ${data.info.nama}", style = MaterialTheme.typography.titleMedium)
                        Text(text = "NIM: ${data.info.nim}")
                        Text(text = "Email: ${data.info.email}")
                        Text(text = "Angkatan: ${data.info.angkatan}")
                        Text(text = "Semester: ${data.info.semester}")
                        Text(text = "Dosen Pembimbing: ${data.info.dosen_pa.nama}")
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Progres: ${data.setoran.info_dasar.persentase_progres_setor}%")
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Daftar Setoran:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        LazyColumn {
                            items(data.setoran.detail) { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(text = item.nama, style = MaterialTheme.typography.bodyLarge)
                                        Text(text = "Label: ${item.label}")
                                        Text(text = "Status: ${if (item.sudah_setor) "Sudah Setor" else "Belum Setor"}")
                                        item.info_setoran?.let {
                                            Text(text = "Tanggal Setoran: ${it.tgl_setoran}")
                                            Text(text = "Dosen: ${it.dosen_yang_mengesahkan.nama}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is DashboardState.Error -> {
                        LaunchedEffect(dashboardState) {
                            scope.launch {
                                snackbarHostState.showSnackbar(state.message)
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    )
}