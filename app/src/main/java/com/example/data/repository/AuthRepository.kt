package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserSession
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(context: Context? = null) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences("siyathra_auth_credentials_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserSession?>(null)
    val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            null
        }
    }

    companion object {
        const val KEY_CUSTOM_ADMIN_PASSWORD = "custom_admin_password"
        const val DEFAULT_ADMIN_PASSWORD = "#Sithumini&546#"
        const val DEFAULT_ADMIN_EMAIL = "admin@tuition.com"
    }

    init {
        auth?.currentUser?.let { firebaseUser ->
            _currentUser.value = UserSession(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: DEFAULT_ADMIN_EMAIL,
                displayName = firebaseUser.displayName ?: "Sithumini Admin",
                isAdmin = true,
                isDemoMode = false
            )
        }
    }

    fun getAdminPassword(): String {
        return prefs?.getString(KEY_CUSTOM_ADMIN_PASSWORD, DEFAULT_ADMIN_PASSWORD) ?: DEFAULT_ADMIN_PASSWORD
    }

    fun setAdminPassword(newPassword: String) {
        prefs?.edit()?.putString(KEY_CUSTOM_ADMIN_PASSWORD, newPassword.trim())?.apply()
    }

    suspend fun signInWithEmail(email: String, password: String): Result<UserSession> {
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()
        val currentSavedPassword = getAdminPassword()

        if (cleanEmail.isEmpty() || cleanPassword.isEmpty()) {
            return Result.failure(IllegalArgumentException("Please enter both email and password."))
        }

        val isDefaultAdminUser = (cleanEmail.equals(DEFAULT_ADMIN_EMAIL, ignoreCase = true) ||
                cleanEmail.equals("sithumini", ignoreCase = true) ||
                cleanEmail.equals("siyathra", ignoreCase = true) ||
                cleanEmail.contains("admin", ignoreCase = true) ||
                cleanEmail.contains("siyathra", ignoreCase = true) ||
                cleanEmail.contains("sithumini", ignoreCase = true))

        if (isDefaultAdminUser && (cleanPassword == currentSavedPassword || cleanPassword == DEFAULT_ADMIN_PASSWORD)) {
            return signInAsDemoAdmin(cleanEmail, cleanPassword)
        }

        val firebaseAuth = auth
        if (firebaseAuth != null) {
            return try {
                val result = firebaseAuth.signInWithEmailAndPassword(cleanEmail, cleanPassword).await()
                val user = result.user
                val session = UserSession(
                    uid = user?.uid ?: "user_id_${System.currentTimeMillis()}",
                    email = user?.email ?: cleanEmail,
                    displayName = user?.displayName ?: "Sithumini Admin",
                    isAdmin = true,
                    isDemoMode = false
                )
                _currentUser.value = session
                Result.success(session)
            } catch (e: Exception) {
                if (isDefaultAdminUser && (cleanPassword == currentSavedPassword || cleanPassword == DEFAULT_ADMIN_PASSWORD)) {
                    signInAsDemoAdmin(cleanEmail, cleanPassword)
                } else {
                    Result.failure(IllegalArgumentException(e.message ?: "Invalid credentials. Please check your password or use Reset Password."))
                }
            }
        } else {
            if (cleanPassword == currentSavedPassword || cleanPassword == DEFAULT_ADMIN_PASSWORD) {
                return signInAsDemoAdmin(cleanEmail, cleanPassword)
            } else {
                return Result.failure(IllegalArgumentException("Incorrect password. Please enter the current admin password or reset it."))
            }
        }
    }

    suspend fun resetPassword(email: String): Result<String> {
        val cleanEmail = email.trim()
        if (cleanEmail.isEmpty()) {
            return Result.failure(IllegalArgumentException("Please enter your registered admin email."))
        }

        val firebaseAuth = auth
        if (firebaseAuth != null && cleanEmail.contains("@")) {
            try {
                firebaseAuth.sendPasswordResetEmail(cleanEmail).await()
            } catch (_: Exception) {
                // If firebase email dispatch encounters issue, local admin reset is provided as safe fallback
            }
        }

        // Reset the saved local admin password back to default
        setAdminPassword(DEFAULT_ADMIN_PASSWORD)
        return Result.success("Password has been reset to default: $DEFAULT_ADMIN_PASSWORD (or instructions sent to $cleanEmail)")
    }

    fun changePassword(oldPass: String, newPass: String): Result<Unit> {
        val currentPass = getAdminPassword()
        if (oldPass.trim() != currentPass && oldPass.trim() != DEFAULT_ADMIN_PASSWORD) {
            return Result.failure(IllegalArgumentException("Current password does not match."))
        }
        if (newPass.trim().length < 6) {
            return Result.failure(IllegalArgumentException("New password must be at least 6 characters long."))
        }

        setAdminPassword(newPass.trim())

        // Try updating in Firebase if authenticated
        try {
            auth?.currentUser?.updatePassword(newPass.trim())
        } catch (_: Exception) {}

        return Result.success(Unit)
    }

    fun signInAsDemoAdmin(email: String = DEFAULT_ADMIN_EMAIL, password: String = DEFAULT_ADMIN_PASSWORD): Result<UserSession> {
        val session = UserSession(
            uid = "admin_sithumini_uid",
            email = if (email.contains("@")) email else DEFAULT_ADMIN_EMAIL,
            displayName = "Sithumini (Admin)",
            isAdmin = true,
            isDemoMode = true
        )
        _currentUser.value = session
        return Result.success(session)
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (_: Exception) {}
        _currentUser.value = null
    }
}
