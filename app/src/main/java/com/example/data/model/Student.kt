package com.example.data.model

import java.text.NumberFormat
import java.util.Locale

data class SubjectInfo(
    val name: String = "",
    val fee: Double = 2500.0,
    val medium: String = Student.MEDIUM_SINHALA,
    val active: Boolean = true
)

data class Student(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val grade: Int = 10, // Grade 6 to 11
    val medium: String = MEDIUM_SINHALA, // Default/fallback medium
    val joinedDate: Long = System.currentTimeMillis(),
    val subjects: Map<String, Double> = mapOf(
        SUBJECT_MATHS to DEFAULT_FEE_MATHS
    ), // Subject Name -> Fee
    val subjectMediums: Map<String, String> = mapOf(
        SUBJECT_MATHS to MEDIUM_SINHALA
    ), // Subject Name -> Medium ("Sinhala", "English", "General")
    val notes: String = ""
) {
    val totalMonthlyFee: Double
        get() = subjects.values.sum()

    val formattedTotalMonthlyFee: String
        get() = formatCurrency(totalMonthlyFee)

    val gradeDisplayName: String
        get() = "Grade $grade"

    fun hasSubject(subjectName: String): Boolean = subjects.containsKey(subjectName)

    fun getMediumForSubject(subjectName: String): String {
        if (subjectName == SUBJECT_ENGLISH) {
            return MEDIUM_GENERAL
        }
        return subjectMediums[subjectName] ?: medium
    }

    fun isEnrolledInClass(targetGrade: Int, targetSubject: String, targetMedium: String?): Boolean {
        if (grade != targetGrade) return false
        if (!hasSubject(targetSubject)) return false
        if (targetSubject == SUBJECT_ENGLISH) return true
        if (targetMedium == null) return true
        val med = getMediumForSubject(targetSubject)
        return med.equals(targetMedium, ignoreCase = true)
    }

    companion object {
        const val MEDIUM_SINHALA = "Sinhala"
        const val MEDIUM_ENGLISH = "English"
        const val MEDIUM_GENERAL = "General"

        const val SUBJECT_MATHS = "Maths"
        const val SUBJECT_SCIENCE = "Science"
        const val SUBJECT_COMMERCE = "Commerce"
        const val SUBJECT_ENGLISH = "English"

        val ALL_GRADES = listOf(6, 7, 8, 9, 10, 11)
        val ALL_SUBJECTS = listOf(SUBJECT_MATHS, SUBJECT_SCIENCE, SUBJECT_COMMERCE, SUBJECT_ENGLISH)
        val MEDIUM_OPTIONS = listOf(MEDIUM_SINHALA, MEDIUM_ENGLISH)

        const val DEFAULT_FEE_MATHS = 2500.0
        const val DEFAULT_FEE_SCIENCE = 2500.0
        const val DEFAULT_FEE_COMMERCE = 2500.0
        const val DEFAULT_FEE_ENGLISH = 1200.0 // English subject fee is Rs. 1,200

        fun isCommerceAvailableForGrade(grade: Int): Boolean {
            return grade == 10 || grade == 11
        }

        fun getAvailableSubjectsForGrade(grade: Int): List<String> {
            return if (isCommerceAvailableForGrade(grade)) {
                listOf(SUBJECT_MATHS, SUBJECT_SCIENCE, SUBJECT_COMMERCE, SUBJECT_ENGLISH)
            } else {
                listOf(SUBJECT_MATHS, SUBJECT_SCIENCE, SUBJECT_ENGLISH)
            }
        }

        fun getDefaultFeeForSubject(subjectName: String): Double {
            return when (subjectName) {
                SUBJECT_MATHS -> DEFAULT_FEE_MATHS
                SUBJECT_SCIENCE -> DEFAULT_FEE_SCIENCE
                SUBJECT_COMMERCE -> DEFAULT_FEE_COMMERCE
                SUBJECT_ENGLISH -> DEFAULT_FEE_ENGLISH
                else -> 2500.0
            }
        }

        fun formatCurrency(amount: Double): String {
            return "Rs. ${String.format(Locale.US, "%,.0f", amount)}"
        }
    }
}
