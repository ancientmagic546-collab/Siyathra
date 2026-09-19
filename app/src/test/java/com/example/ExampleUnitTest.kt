package com.example

import com.example.data.model.Student
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testEnglishFeeIsRs1200() {
        assertEquals(1200.0, Student.DEFAULT_FEE_ENGLISH, 0.01)
    }

    @Test
    fun testCommerceFeeIsRs2500() {
        assertEquals(2500.0, Student.DEFAULT_FEE_COMMERCE, 0.01)
    }

    @Test
    fun testCommerceAvailableOnlyForGrades10And11() {
        assertFalse(Student.isCommerceAvailableForGrade(6))
        assertFalse(Student.isCommerceAvailableForGrade(7))
        assertFalse(Student.isCommerceAvailableForGrade(8))
        assertFalse(Student.isCommerceAvailableForGrade(9))
        assertTrue(Student.isCommerceAvailableForGrade(10))
        assertTrue(Student.isCommerceAvailableForGrade(11))
    }

    @Test
    fun testStudentTotalMonthlyFeeCalculation() {
        val studentGrade10 = Student(
            id = "test_1",
            name = "Kasun Perera",
            phone = "0771234567",
            grade = 10,
            medium = Student.MEDIUM_SINHALA,
            subjects = mapOf(
                Student.SUBJECT_MATHS to 2000.0,
                Student.SUBJECT_ENGLISH to 1200.0,
                Student.SUBJECT_COMMERCE to 2500.0
            )
        )
        // 2000 + 1200 + 2500 = 5700
        assertEquals(5700.0, studentGrade10.totalMonthlyFee, 0.01)
    }
}
