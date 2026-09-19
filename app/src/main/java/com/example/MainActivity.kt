package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.TuitionDashboardScreen
import com.example.ui.theme.EduManagerTheme
import com.example.ui.viewmodel.TuitionViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MainActivity : FragmentActivity() {

    private val viewModel: TuitionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFirebase()
        enableEdgeToEdge()
        setContent {
            EduManagerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EduManagerApp(
                        activity = this@MainActivity,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshBiometricSecurityStatus()
    }

    private fun initFirebase() {
        try {
            if (FirebaseApp.getApps(applicationContext).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyByYaH4xOhvzpPpvNyo70IjkuRC9SmeiLc")
                    .setApplicationId("1:350743183843:web:61ea8bf3e5b818679d60d6")
                    .setProjectId("siyathra-52db5")
                    .setGcmSenderId("350743183843")
                    .setStorageBucket("siyathra-52db5.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(applicationContext, options)
                Log.d("MainActivity", "FirebaseApp successfully initialized with siyathra-52db5 config.")
            }
        } catch (e: Throwable) {
            Log.w("MainActivity", "FirebaseApp init error: ${e.message}")
        }
    }
}

@Composable
fun EduManagerApp(
    activity: FragmentActivity,
    viewModel: TuitionViewModel
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()
    val loginError by viewModel.loginError.collectAsStateWithLifecycle()

    val isBiometricSupported by viewModel.isBiometricSupported.collectAsStateWithLifecycle()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val isPasswordLoginRequired by viewModel.isPasswordLoginRequired.collectAsStateWithLifecycle()
    val remainingPasswordTimeDesc by viewModel.remainingPasswordTimeDesc.collectAsStateWithLifecycle()
    val biometricError by viewModel.biometricError.collectAsStateWithLifecycle()

    Crossfade(targetState = currentUser != null, label = "auth_screen_fade") { isAuthenticated ->
        if (isAuthenticated) {
            TuitionDashboardScreen(viewModel = viewModel)
        } else {
            LoginScreen(
                isLoading = isLoading,
                errorMessage = loginError,
                isBiometricSupported = isBiometricSupported,
                isBiometricEnabled = isBiometricEnabled,
                isPasswordLoginRequired = isPasswordLoginRequired,
                remainingPasswordTimeDesc = remainingPasswordTimeDesc,
                biometricError = biometricError,
                onBiometricClick = {
                    viewModel.promptBiometricUnlock(activity)
                },
                onResetBiometric = {
                    viewModel.resetBiometric()
                },
                onLoginClick = { email, pass -> viewModel.login(email, pass) },
                onResetPassword = { email, onResult ->
                    viewModel.resetPassword(email, onResult)
                },
                onDemoLoginClick = { viewModel.loginDemo() }
            )
        }
    }
}
