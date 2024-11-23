package io.github.takusan23.androidbleanduwbsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.takusan23.androidbleanduwbsample.ui.screen.ControleeScreen
import io.github.takusan23.androidbleanduwbsample.ui.screen.ControllerScreen
import io.github.takusan23.androidbleanduwbsample.ui.screen.HomeScreen
import io.github.takusan23.androidbleanduwbsample.ui.theme.AndroidBleAndUwbSampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidBleAndUwbSampleTheme {
                MainScreen()
            }
        }
    }
}

@Composable
private fun MainScreen() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onControllerClick = { navController.navigate("controller") },
                onControleeClick = { navController.navigate("controlee") }
            )
        }
        composable("controller") {
            ControllerScreen()
        }
        composable("controlee") {
            ControleeScreen()
        }
    }
}