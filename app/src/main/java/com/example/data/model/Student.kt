package com.example.data.model

import java.text.NumberFormat
import java.util.Locale

data class SubjectInfo(
    val name: String = "",
    val fee: Double = 2500.0,
    val active: Boolean = true
)

data class Student(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val medium: String = MEDIUM_SINHALA, // "Sinhala" or "English"
    val joinedDate: Long = System.currentTimeMillis(),
    val subjects: Map<String, Double> = mapOf(
        "Maths" to 2500.0
    ), // Subject Name -> Fee
    val notes: String = ""
) {
    val totalMonthlyFee: Double
        get() = subjects.values.sum()

    val formattedTotalMonthlyFee: String
        get() = formatCurrency(totalMonthlyFee)

    fun hasSubject(subjectName: String): Boolean = subjects.containsKey(subjectName)

    companion object {
        const val MEDIUM_SINHALA = "Sinhala"
        const val MEDIUM_ENGLISH = "English"

        const val SUBJECT_MATHS = "Maths"
        const val SUBJECT_SCIENCE = "Science"
        const val DEFAULT_FEE_MATHS = 2500.0
        const val DEFAULT_FEE_SCIENCE = 2500.0

        fun formatCurrency(amount: Double): String {
            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "LK"))
            // Fallback for clean display formatted as Rs.
            return "Rs. ${String.format(Locale.US, "%,.0f", amount)}"
        }
    }
}
