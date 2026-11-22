package com.sophi.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.sophi.sample.feature.EmbeddedChatScreen
import com.sophi.sample.feature.HomeScreen
import com.sophi.sample.feature.LaunchScreen
import com.sophi.sample.ui.theme.SophiAndroidTheme
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable data object Home : Route
    @Serializable data object Launch : Route
    @Serializable data class EmbeddedChat(val message: String? = null, val productId: String? = null) : Route
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SophiAndroidTheme { SampleNavHost() }
        }
    }
}

@Composable
private fun SampleNavHost() {
    val navController = rememberNavController()
    val back: () -> Unit = { if (navController.previousBackStackEntry != null) navController.popBackStack() }
    NavHost(navController, startDestination = Route.Home) {
        composable<Route.Home> { HomeScreen(onOpen = { navController.navigate(it) }) }
        composable<Route.Launch> { LaunchScreen(onBack = back) }
        composable<Route.EmbeddedChat> { entry ->
            val route = entry.toRoute<Route.EmbeddedChat>()
            EmbeddedChatScreen(route.message, route.productId, onBack = back)
        }
    }
}
