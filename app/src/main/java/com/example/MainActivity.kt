package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.TuitionDashboardScreen
import com.example.ui.theme.EduManagerTheme
import com.example.ui.viewmodel.TuitionViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MainActivity : ComponentActivity() {

    private val viewModel: TuitionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFirebase()
        enableEdgeToEdge()
        setContent {
            EduManagerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EduManagerApp(viewModel = viewModel)
                }
            }
        }
    }

    private fun initFirebase() {
        try {
            if (FirebaseApp.getApps(applicationContext).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyDvES9MCp_o0DRQ_K1aX_gEqI9yIAQmYJY")
                    .setApplicationId("1:318717678552:web:10fe68260ca3c79dea98ad")
                    .setProjectId("sithuminituition")
                    .setGcmSenderId("318717678552")
                    .setStorageBucket("sithuminituition.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(applicationContext, options)
                Log.d("MainActivity", "FirebaseApp successfully initialized with sithuminituition config.")
            }
        } catch (e: Throwable) {
            Log.w("MainActivity", "FirebaseApp init error: ${e.message}")
        }
    }
}

@Composable
fun EduManagerApp(viewModel: TuitionViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()
    val loginError by viewModel.loginError.collectAsStateWithLifecycle()

    Crossfade(targetState = currentUser != null, label = "auth_screen_fade") { isAuthenticated ->
        if (isAuthenticated) {
            TuitionDashboardScreen(viewModel = viewModel)
        } else {
            LoginScreen(
                isLoading = isLoading,
                errorMessage = loginError,
                onLoginClick = { email, pass -> viewModel.login(email, pass) },
                onDemoLoginClick = { viewModel.loginDemo() }
            )
        }
    }
}
