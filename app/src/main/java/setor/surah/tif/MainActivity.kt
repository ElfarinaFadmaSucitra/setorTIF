package setor.surah.tif

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import setor.surah.tif.navigasi.SetupNavGraph
import setor.surah.tif.ui.theme.SetorTIFTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SetorTIFTheme {
                val navController = rememberNavController()
                SetupNavGraph(navController = navController)
                }
            }
        }
    }

