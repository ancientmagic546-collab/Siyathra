package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricSecurityManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("siyathra_biometric_security_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_LAST_PASSWORD_AUTH_TIME = "last_password_auth_timestamp"
        const val KEY_BIOMETRIC_ENABLED = "biometric_unlock_enabled"
        const val SEVENTY_TWO_HOURS_MS = 72 * 60 * 60 * 1000L // 72 hours in milliseconds
    }

    /**
     * Checks if the device has biometric hardware and an enrolled fingerprint / face.
     */
    fun canAuthenticateBiometrics(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Whether biometric unlock is toggled ON by user (default true if device supports it).
     */
    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    /**
     * Timestamp of the last successful email/password authentication.
     */
    fun getLastPasswordAuthTimestamp(): Long {
        return prefs.getLong(KEY_LAST_PASSWORD_AUTH_TIME, 0L)
    }

    /**
     * Call this whenever the user successfully signs in with Email and Password.
     * Resets the 72-hour security timer.
     */
    fun recordPasswordLoginSuccess() {
        prefs.edit()
            .putLong(KEY_LAST_PASSWORD_AUTH_TIME, System.currentTimeMillis())
            .apply()
    }

    /**
     * Force-expires the 72-hour timer (e.g. on manual logout or security reset).
     */
    fun invalidatePasswordAuthSession() {
        prefs.edit()
            .putLong(KEY_LAST_PASSWORD_AUTH_TIME, 0L)
            .apply()
    }

    /**
     * Resets biometric configuration and clears stored credentials/timers.
     */
    fun resetBiometricSettings() {
        prefs.edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED, true)
            .putLong(KEY_LAST_PASSWORD_AUTH_TIME, 0L)
            .apply()
    }

    /**
     * Determines whether the 72-hour window has expired and full password login is required.
     */
    fun isPasswordLoginRequired(): Boolean {
        val lastAuth = getLastPasswordAuthTimestamp()
        if (lastAuth <= 0L) return true
        val timeSinceAuth = System.currentTimeMillis() - lastAuth
        return timeSinceAuth >= SEVENTY_TWO_HOURS_MS || timeSinceAuth < 0L
    }

    /**
     * Returns true if fingerprint unlock is currently eligible to be used:
     * - Device has biometric support
     * - User has biometric unlock enabled
     * - Less than 72 hours have elapsed since last email/password sign-in
     */
    fun isBiometricUnlockEligible(): Boolean {
        return canAuthenticateBiometrics() && isBiometricEnabled() && !isPasswordLoginRequired()
    }

    /**
     * Human-readable remaining time until password authentication is required.
     */
    fun getRemainingTimeDescription(): String {
        val lastAuth = getLastPasswordAuthTimestamp()
        if (lastAuth <= 0L) {
            return "Email & Password required"
        }
        val elapsed = System.currentTimeMillis() - lastAuth
        val remainingMs = SEVENTY_TWO_HOURS_MS - elapsed
        if (remainingMs <= 0) {
            return "72-hour limit reached • Password required"
        }
        val hours = remainingMs / (1000 * 60 * 60)
        val minutes = (remainingMs % (1000 * 60 * 60)) / (1000 * 60)
        return if (hours > 0) {
            "${hours}h ${minutes}m remaining"
        } else {
            "${minutes}m remaining"
        }
    }

    /**
     * Launches the native Android BiometricPrompt for fingerprint verification.
     */
    fun promptBiometricAuth(
        activity: FragmentActivity,
        title: String = "Fingerprint Unlock",
        subtitle: String = "Verify your fingerprint to access Siyathra",
        negativeButtonText: String = "Use Password",
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit,
        onFailed: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errorCode, errString)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
