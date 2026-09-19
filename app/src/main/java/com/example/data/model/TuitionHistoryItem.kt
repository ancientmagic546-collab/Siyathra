package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class HistoryEventType {
    ENROLLMENT,     // Student joined / enrolled in grade & subjects
    PAYMENT,        // Monthly fee payment transaction
    CLASS_LEAVING   // Student left a class / subject or withdrew from tuition
}

data class TuitionHistoryItem(
    val id: String = "",
    val type: HistoryEventType = HistoryEventType.ENROLLMENT,
    val studentId: String = "",
    val studentName: String = "",
    val studentGrade: Int = 10,
    val studentMedium: String = Student.MEDIUM_SINHALA,
    val studentPhone: String = "",
    val title: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val amount: Double? = null,
    val receiptId: String? = null,
    val monthKey: String? = null,
    val subjects: List<String> = emptyList(),
    val subjectLeft: String? = null,
    val departureReason: String? = null,
    val note: String = ""
) {
    val formattedDate: String
        get() = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))

    val formattedShortDate: String
        get() = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))

    val formattedTime: String
        get() = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))

    val formattedAmount: String?
        get() = amount?.let { Student.formatCurrency(it) }

    val gradeDisplayName: String
        get() = "Grade $studentGrade"
}
