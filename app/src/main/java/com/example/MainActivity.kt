package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.DashboardScreen
import com.example.ui.GoalDisplayScreen
import com.example.ui.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.WaterViewModel
import com.example.viewmodel.WaterViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Get the application repository
                val app = applicationContext as WaterApplication
                val factory = WaterViewModelFactory(app.repository, app.applicationContext)
                val viewModel: WaterViewModel = viewModel(factory = factory)

                // High fidelity scaffold
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WaterTrackerAppContent(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun WaterTrackerAppContent(
    viewModel: WaterViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Real-world runtime permission request for local notification delivery on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Crossfade for smooth screen transition durations
    Crossfade(
        targetState = currentScreen,
        modifier = modifier.fillMaxSize(),
        label = "screen_crossfade"
    ) { screen ->
        when (screen) {
            AppScreen.Onboarding -> OnboardingScreen(viewModel = viewModel)
            AppScreen.GoalDisplay -> MyGoalDisplayContainer(viewModel = viewModel)
            AppScreen.Dashboard -> DashboardScreen(viewModel = viewModel)
        }
    }
}

@Composable
fun MyGoalDisplayContainer(
    viewModel: WaterViewModel
) {
    GoalDisplayScreen(viewModel = viewModel)
}
