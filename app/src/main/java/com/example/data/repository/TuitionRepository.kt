package com.example.data.repository

import android.util.Log
import com.example.data.model.MonthlyPayment
import com.example.data.model.PaymentRecord
import com.example.data.model.Student
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class TuitionRepository {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            Log.w("TuitionRepository", "Firestore not available: ${e.message}")
            null
        }
    }

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _payments = MutableStateFlow<List<MonthlyPayment>>(emptyList())
    val payments: StateFlow<List<MonthlyPayment>> = _payments.asStateFlow()

    private val _isCloudConnected = MutableStateFlow(false)
    val isCloudConnected: StateFlow<Boolean> = _isCloudConnected.asStateFlow()

    private var studentsListener: ListenerRegistration? = null
    private var paymentsListener: ListenerRegistration? = null

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        initInitialData()
        startCloudListeners()
    }

    private fun initInitialData() {
        // Sample starter data for educational institution
        val sampleStudents = listOf(
            Student(
                id = "std_101",
                name = "Kasun Perera",
                phone = "+94 77 123 4567",
                medium = Student.MEDIUM_SINHALA,
                joinedDate = System.currentTimeMillis() - 86400000L * 30,
                subjects = mapOf("Maths" to 2500.0, "Science" to 2500.0),
                notes = "Grade 11 Student"
            ),
            Student(
                id = "std_102",
                name = "Nimali Fernando",
                phone = "+94 71 987 6543",
                medium = Student.MEDIUM_SINHALA,
                joinedDate = System.currentTimeMillis() - 86400000L * 20,
                subjects = mapOf("Maths" to 2500.0),
                notes = "Maths Stream"
            ),
            Student(
                id = "std_103",
                name = "Dilshan Silva",
                phone = "+94 75 555 4321",
                medium = Student.MEDIUM_ENGLISH,
                joinedDate = System.currentTimeMillis() - 86400000L * 10,
                subjects = mapOf("Science" to 2500.0),
                notes = "Science Stream"
            ),
            Student(
                id = "std_104",
                name = "Samanthi Jayasinghe",
                phone = "+94 78 888 1122",
                medium = Student.MEDIUM_ENGLISH,
                joinedDate = System.currentTimeMillis() - 86400000L * 5,
                subjects = mapOf("Maths" to 2500.0, "Science" to 2500.0),
                notes = "Both Subjects"
            )
        )

        _students.value = sampleStudents

        val currentMonth = MonthlyPayment.getCurrentMonthKey()
        val samplePayments = sampleStudents.mapIndexed { index, student ->
            val totalDue = student.totalMonthlyFee
            val paidAmount = when (index) {
                0 -> totalDue // Paid in full
                1 -> 1500.0  // Partial
                2 -> 2500.0  // Paid in full
                else -> 0.0  // Unpaid
            }
            MonthlyPayment(
                id = "${student.id}_$currentMonth",
                studentId = student.id,
                studentName = student.name,
                monthKey = currentMonth,
                totalFeeDue = totalDue,
                amountPaid = paidAmount,
                lastUpdated = System.currentTimeMillis(),
                history = if (paidAmount > 0) listOf(
                    PaymentRecord(amount = paidAmount, note = "Initial Payment")
                ) else emptyList()
            )
        }

        _payments.value = samplePayments
    }

    private fun startCloudListeners() {
        val db = firestore ?: return

        try {
            studentsListener = db.collection("students")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("TuitionRepository", "Students listen failed.", error)
                        _isCloudConnected.value = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        _isCloudConnected.value = true
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                val name = doc.getString("name") ?: ""
                                val phone = doc.getString("phone") ?: ""
                                val medium = doc.getString("medium") ?: Student.MEDIUM_SINHALA
                                val joinedDate = doc.getLong("joinedDate") ?: System.currentTimeMillis()
                                val notes = doc.getString("notes") ?: ""
                                val rawSubjects = doc.get("subjects") as? Map<*, *>
                                val subjectsMap = mutableMapOf<String, Double>()
                                rawSubjects?.forEach { (key, value) ->
                                    if (key is String) {
                                        val fee = when (value) {
                                            is Number -> value.toDouble()
                                            else -> 2500.0
                                        }
                                        subjectsMap[key] = fee
                                    }
                                }
                                Student(
                                    id = doc.id,
                                    name = name,
                                    phone = phone,
                                    medium = medium,
                                    joinedDate = joinedDate,
                                    subjects = if (subjectsMap.isEmpty()) mapOf("Maths" to 2500.0) else subjectsMap,
                                    notes = notes
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (list.isNotEmpty()) {
                            _students.value = list
                        }
                    } else {
                        // If collection is empty in cloud, push initial sample data to cloud
                        if (_students.value.isNotEmpty()) {
                            _students.value.forEach { addStudentToCloud(it) }
                        }
                    }
                }

            paymentsListener = db.collection("monthly_payments")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("TuitionRepository", "Payments listen failed.", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        _isCloudConnected.value = true
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                val studentId = doc.getString("studentId") ?: ""
                                val studentName = doc.getString("studentName") ?: ""
                                val monthKey = doc.getString("monthKey") ?: ""
                                val totalFeeDue = doc.getDouble("totalFeeDue") ?: 0.0
                                val amountPaid = doc.getDouble("amountPaid") ?: 0.0
                                val lastUpdated = doc.getLong("lastUpdated") ?: System.currentTimeMillis()
                                
                                val rawHistory = doc.get("history") as? List<Map<String, Any>>
                                val historyList = rawHistory?.map { recordMap ->
                                    PaymentRecord(
                                        amount = (recordMap["amount"] as? Number)?.toDouble() ?: 0.0,
                                        timestamp = (recordMap["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                                        note = recordMap["note"] as? String ?: ""
                                    )
                                } ?: emptyList()

                                MonthlyPayment(
                                    id = doc.id,
                                    studentId = studentId,
                                    studentName = studentName,
                                    monthKey = monthKey,
                                    totalFeeDue = totalFeeDue,
                                    amountPaid = amountPaid,
                                    lastUpdated = lastUpdated,
                                    history = historyList
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (list.isNotEmpty()) {
                            _payments.value = list
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("TuitionRepository", "Failed to start listeners", e)
            _isCloudConnected.value = false
        }
    }

    fun addStudent(name: String, phone: String, medium: String, subjects: Map<String, Double>, notes: String, monthKey: String) {
        val newId = "std_${UUID.randomUUID().toString().take(8)}"
        val newStudent = Student(
            id = newId,
            name = name,
            phone = phone,
            medium = medium,
            joinedDate = System.currentTimeMillis(),
            subjects = subjects,
            notes = notes
        )

        // Update local state immediately
        _students.value = _students.value + newStudent

        // Ensure payment document exists for current month
        val paymentId = "${newId}_$monthKey"
        val newPayment = MonthlyPayment(
            id = paymentId,
            studentId = newId,
            studentName = name,
            monthKey = monthKey,
            totalFeeDue = newStudent.totalMonthlyFee,
            amountPaid = 0.0,
            lastUpdated = System.currentTimeMillis(),
            history = emptyList()
        )
        _payments.value = _payments.value + newPayment

        // Sync with Cloud Firestore
        addStudentToCloud(newStudent)
        savePaymentToCloud(newPayment)
    }

    fun updateStudent(student: Student, monthKey: String) {
        _students.value = _students.value.map { if (it.id == student.id) student else it }
        
        // Update payment total due if subject fees changed
        val paymentId = "${student.id}_$monthKey"
        val existingPayment = _payments.value.find { it.id == paymentId }
        val updatedPayment = if (existingPayment != null) {
            existingPayment.copy(
                studentName = student.name,
                totalFeeDue = student.totalMonthlyFee,
                lastUpdated = System.currentTimeMillis()
            )
        } else {
            MonthlyPayment(
                id = paymentId,
                studentId = student.id,
                studentName = student.name,
                monthKey = monthKey,
                totalFeeDue = student.totalMonthlyFee,
                amountPaid = 0.0,
                lastUpdated = System.currentTimeMillis(),
                history = emptyList()
            )
        }

        _payments.value = _payments.value.filter { it.id != paymentId } + updatedPayment

        addStudentToCloud(student)
        savePaymentToCloud(updatedPayment)
    }

    fun deleteStudent(studentId: String) {
        _students.value = _students.value.filter { it.id != studentId }
        _payments.value = _payments.value.filter { it.studentId != studentId }

        firestore?.collection("students")?.document(studentId)?.delete()
    }

    fun recordPayment(studentId: String, monthKey: String, amount: Double, note: String) {
        val student = _students.value.find { it.id == studentId } ?: return
        val paymentId = "${studentId}_$monthKey"
        val existingPayment = _payments.value.find { it.id == paymentId }

        val newRecord = PaymentRecord(
            amount = amount,
            timestamp = System.currentTimeMillis(),
            note = note.ifBlank { "Payment received" }
        )

        val updatedPayment = if (existingPayment != null) {
            existingPayment.copy(
                amountPaid = existingPayment.amountPaid + amount,
                lastUpdated = System.currentTimeMillis(),
                history = existingPayment.history + newRecord
            )
        } else {
            MonthlyPayment(
                id = paymentId,
                studentId = studentId,
                studentName = student.name,
                monthKey = monthKey,
                totalFeeDue = student.totalMonthlyFee,
                amountPaid = amount,
                lastUpdated = System.currentTimeMillis(),
                history = listOf(newRecord)
            )
        }

        _payments.value = _payments.value.filter { it.id != paymentId } + updatedPayment
        savePaymentToCloud(updatedPayment)
    }

    private fun addStudentToCloud(student: Student) {
        firestore?.collection("students")?.document(student.id)?.set(
            mapOf(
                "name" to student.name,
                "phone" to student.phone,
                "medium" to student.medium,
                "joinedDate" to student.joinedDate,
                "subjects" to student.subjects,
                "notes" to student.notes
            ),
            SetOptions.merge()
        )
    }

    private fun savePaymentToCloud(payment: MonthlyPayment) {
        firestore?.collection("monthly_payments")?.document(payment.id)?.set(
            mapOf(
                "studentId" to payment.studentId,
                "studentName" to payment.studentName,
                "monthKey" to payment.monthKey,
                "totalFeeDue" to payment.totalFeeDue,
                "amountPaid" to payment.amountPaid,
                "lastUpdated" to payment.lastUpdated,
                "history" to payment.history.map {
                    mapOf(
                        "amount" to it.amount,
                        "timestamp" to it.timestamp,
                        "note" to it.note
                    )
                }
            ),
            SetOptions.merge()
        )
    }

    fun cleanup() {
        studentsListener?.remove()
        paymentsListener?.remove()
    }
}
