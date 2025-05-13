package setor.surah.tif.navigasi

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import setor.surah.tif.tampilan.HomeScreen
import setor.surah.tif.tampilan.LogScreen
import setor.surah.tif.tampilan.LoginScreen
import setor.surah.tif.tampilan.ProfileScreen

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable(
            route = "dashboard",
            enterTransition = { slideInHorizontally { it } + fadeIn() },
            exitTransition = { slideOutHorizontally { -it } + fadeOut() }
        ) {
            HomeScreen(navController = navController)
        }
        composable(
            route = "log",
            enterTransition = { slideInHorizontally { it } + fadeIn() },
            exitTransition = { slideOutHorizontally { -it } + fadeOut() }
        ) {
            LogScreen(navController = navController)
        }
        composable(
            route = "profile",
            enterTransition = { slideInHorizontally { it } + fadeIn() },
            exitTransition = { slideOutHorizontally { -it } + fadeOut() }
        ) {
            ProfileScreen(navController = navController)
        }
    }
}