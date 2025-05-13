package setor.surah.tif.tampilan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Setoran Hafalan") },
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
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = currentRoute == "dashboard",
                    onClick = {
                        navController.navigate("dashboard") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Log") },
                    label = { Text("Log") },
                    selected = currentRoute == "log",
                    onClick = {
                        navController.navigate("log") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                    label = { Text("Profil") },
                    selected = currentRoute == "profile",
                    onClick = {
                        navController.navigate("profile") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
        },
        content = { padding ->
            when (currentRoute) {
                "dashboard" -> DashboardContent(
                    navController = navController,
                    dashboardViewModel = dashboardViewModel,
                    snackbarHostState = snackbarHostState,
                    scope = scope,
                    padding = padding
                )
                "log" -> LogScreen(navController = navController)
                "profile" -> ProfileScreen(navController = navController)
                else -> DashboardContent(
                    navController = navController,
                    dashboardViewModel = dashboardViewModel,
                    snackbarHostState = snackbarHostState,
                    scope = scope,
                    padding = padding
                )
            }
        }
    )
}

@Composable
fun DashboardContent(
    navController: NavController,
    dashboardViewModel: HomeViewModel,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope,
    padding: PaddingValues
) {
    val dashboardState by dashboardViewModel.dashboardState.collectAsState()
    val userName by dashboardViewModel.userName.collectAsState()

    LaunchedEffect(Unit) {
        dashboardViewModel.fetchSetoranSaya()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp)
    ) {
        userName?.let {
            Text(
                text = "Selamat datang, $it!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        when (val state = dashboardState) {
            is DashboardState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is DashboardState.Success -> {
                val data = state.data.data
                // Progres Setoran
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
                            text = "Progres Setoran",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Progres: ${data.setoran.info_dasar.persentase_progres_setor}%",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Ringkasan Label
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
                            text = "Ringkasan Setoran",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(data.setoran.ringkasan.sortedBy { it.label }) { ringkasan ->
                                LabelBox(ringkasan)
                            }
                        }
                    }
                }

                // Daftar Setoran
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
                            text = "Daftar Setoran",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn {
                            items(data.setoran.detail) { item ->
                                SetoranItemCard(item)
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

@Composable
fun InfoRow(label: String, value: String) {
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
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun LabelBox(ringkasan: setor.surah.tif.model.Ringkasan) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = ringkasan.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${ringkasan.persentase_progres_setor}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Sudah: ${ringkasan.total_sudah_setor}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Belum: ${ringkasan.total_belum_setor}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun SetoranItemCard(item: setor.surah.tif.model.SetoranItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.sudah_setor) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.nama,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Status: ${if (item.sudah_setor) "Sudah Setor" else "Belum Setor"}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (item.sudah_setor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            item.info_setoran?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tanggal Setoran: ${it.tgl_setoran}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Dosen: ${it.dosen_yang_mengesahkan.nama}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}