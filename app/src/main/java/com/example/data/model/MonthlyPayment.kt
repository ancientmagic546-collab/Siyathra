package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PaymentStatus {
    PAID,
    PARTIAL,
    UNPAID
}

data class PaymentRecord(
    val amount: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
) {
    val formattedDate: String
        get() = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

data class MonthlyPayment(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val monthKey: String = "", // e.g., "2026-08"
    val totalFeeDue: Double = 0.0,
    val amountPaid: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis(),
    val history: List<PaymentRecord> = emptyList()
) {
    val remainingBalance: Double
        get() = (totalFeeDue - amountPaid).coerceAtLeast(0.0)

    val status: PaymentStatus
        get() = when {
            amountPaid >= totalFeeDue && totalFeeDue > 0 -> PaymentStatus.PAID
            amountPaid > 0 -> PaymentStatus.PARTIAL
            else -> PaymentStatus.UNPAID
        }

    val progressFraction: Float
        get() = if (totalFeeDue > 0) (amountPaid / totalFeeDue).coerceIn(0.0, 1.0).toFloat() else 0f

    val formattedPaid: String
        get() = Student.formatCurrency(amountPaid)

    val formattedRemaining: String
        get() = Student.formatCurrency(remainingBalance)

    val formattedDue: String
        get() = Student.formatCurrency(totalFeeDue)

    companion object {
        fun getCurrentMonthKey(): String {
            return SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
        }

        fun getMonthDisplayName(monthKey: String): String {
            return try {
                val parser = SimpleDateFormat("yyyy-MM", Locale.US)
                val date = parser.parse(monthKey) ?: Date()
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date)
            } catch (e: Exception) {
                monthKey
            }
        }
    }
}
