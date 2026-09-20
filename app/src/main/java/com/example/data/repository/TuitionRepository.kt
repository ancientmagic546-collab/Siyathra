package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.HistoryEventType
import com.example.data.model.MonthlyPayment
import com.example.data.model.PaymentRecord
import com.example.data.model.Student
import com.example.data.model.TuitionHistoryItem
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class TuitionRepository(
    private val context: Context
) {
    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _payments = MutableStateFlow<List<MonthlyPayment>>(emptyList())
    val payments: StateFlow<List<MonthlyPayment>> = _payments.asStateFlow()

    private val _historyEvents = MutableStateFlow<List<TuitionHistoryItem>>(emptyList())
    val historyEvents: StateFlow<List<TuitionHistoryItem>> = _historyEvents.asStateFlow()

    private val _isCloudConnected = MutableStateFlow<Boolean>(false)
    val isCloudConnected: StateFlow<Boolean> = _isCloudConnected.asStateFlow()

    private var firestore: FirebaseFirestore? = null
    private var studentsListener: ListenerRegistration? = null
    private var paymentsListener: ListenerRegistration? = null
    private var historyListener: ListenerRegistration? = null

    init {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firestore = FirebaseFirestore.getInstance()
                _isCloudConnected.value = true
                checkAndInitializeFirestoreStructure()
            }
        } catch (e: Exception) {
            Log.e("TuitionRepository", "FirebaseFirestore init error", e)
            _isCloudConnected.value = false
        }

        startCloudListeners()
    }

    /**
     * Checks if the required system configuration documents and collection schemas exist in Firestore.
     * If missing (e.g. after wiping Firestore), creates them automatically.
     * If they already exist, preserves and uses the existing configurations.
     */
    fun checkAndInitializeFirestoreStructure(onComplete: ((Boolean, String) -> Unit)? = null) {
        val db = firestore
        if (db == null) {
            Log.w("TuitionRepository", "Firestore instance not available for structure check.")
            onComplete?.invoke(false, "Firestore instance not available")
            return
        }

        Log.d("TuitionRepository", "Checking required Firestore documents and collections structure...")

        // 1. Check and initialize system_config / metadata document
        db.collection("system_config").document("metadata").get()
            .addOnSuccessListener { doc ->
                if (doc == null || !doc.exists()) {
                    Log.i("TuitionRepository", "system_config/metadata missing. Auto-creating required metadata...")
                    val metadata = mapOf(
                        "instituteName" to "Siyathra Institute",
                        "status" to "active",
                        "grades" to listOf(6, 7, 8, 9, 10, 11),
                        "subjects" to listOf("Maths", "Science", "Commerce", "English"),
                        "mediums" to listOf(Student.MEDIUM_SINHALA, Student.MEDIUM_ENGLISH),
                        "currency" to "LKR",
                        "schemaVersion" to "1.0",
                        "createdAt" to System.currentTimeMillis(),
                        "lastVerifiedAt" to System.currentTimeMillis()
                    )
                    db.collection("system_config").document("metadata")
                        .set(metadata, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.i("TuitionRepository", "system_config/metadata created successfully.")
                        }
                } else {
                    Log.i("TuitionRepository", "Existing system_config/metadata found in Firestore. Using existing configuration.")
                }
            }
            .addOnFailureListener { e ->
                Log.w("TuitionRepository", "Failed checking system_config/metadata: ${e.message}")
            }

        // 2. Check and initialize system_config / schema document
        db.collection("system_config").document("schema").get()
            .addOnSuccessListener { doc ->
                if (doc == null || !doc.exists()) {
                    Log.i("TuitionRepository", "system_config/schema missing. Creating schema specification...")
                    val schema = mapOf(
                        "collections" to mapOf(
                            "students" to "Stores student profiles, enrolled grades, subjects, and contact info",
                            "monthly_payments" to "Tracks monthly fee amounts due, amount paid, and receipts",
                            "history_events" to "Real-time chronological activity audit for enrollments, fee collections, and class leaving",
                            "system_config" to "System settings, defaults, and institution metadata"
                        ),
                        "initializedAt" to System.currentTimeMillis(),
                        "lastVerifiedAt" to System.currentTimeMillis()
                    )
                    db.collection("system_config").document("schema")
                        .set(schema, SetOptions.merge())
                } else {
                    Log.i("TuitionRepository", "Existing system_config/schema found in Firestore. Using existing schema.")
                }
            }

        // 3. Check and initialize system_config / tuition_defaults document
        db.collection("system_config").document("tuition_defaults").get()
            .addOnSuccessListener { doc ->
                if (doc == null || !doc.exists()) {
                    Log.i("TuitionRepository", "system_config/tuition_defaults missing. Creating defaults...")
                    val defaults = mapOf(
                        "defaultSubjectFee" to 2500.0,
                        "englishSubjectFee" to 1200.0,
                        "currency" to "LKR",
                        "createdAt" to System.currentTimeMillis()
                    )
                    db.collection("system_config").document("tuition_defaults")
                        .set(defaults, SetOptions.merge())
                        .addOnSuccessListener {
                            onComplete?.invoke(true, "Firestore structure verified and initialized successfully.")
                        }
                } else {
                    Log.i("TuitionRepository", "Existing system_config/tuition_defaults found in Firestore.")
                    onComplete?.invoke(true, "Existing Firestore structure verified and ready.")
                }
            }
            .addOnFailureListener { e ->
                onComplete?.invoke(false, "Firestore verification error: ${e.message}")
            }
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

                    if (snapshot != null) {
                        _isCloudConnected.value = true
                        if (snapshot.isEmpty) {
                            // Firestore collection is empty - keep list clean, do NOT add test students
                            _students.value = emptyList()
                        } else {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    val name = doc.getString("name") ?: ""
                                    val phone = doc.getString("phone") ?: ""
                                    val grade = doc.getLong("grade")?.toInt() ?: 10
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

                                    val rawMediums = doc.get("subjectMediums") as? Map<*, *>
                                    val subjectMediumsMap = mutableMapOf<String, String>()
                                    rawMediums?.forEach { (key, value) ->
                                        if (key is String && value is String) {
                                            subjectMediumsMap[key] = value
                                        }
                                    }

                                    Student(
                                        id = doc.id,
                                        name = name,
                                        phone = phone,
                                        grade = grade,
                                        medium = medium,
                                        joinedDate = joinedDate,
                                        subjects = if (subjectsMap.isEmpty()) mapOf("Maths" to 2500.0) else subjectsMap,
                                        subjectMediums = subjectMediumsMap,
                                        notes = notes
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            _students.value = list
                        }
                    }
                }

            paymentsListener = db.collection("monthly_payments")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("TuitionRepository", "Payments listen failed.", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        _isCloudConnected.value = true
                        if (snapshot.isEmpty) {
                            _payments.value = emptyList()
                        } else {
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
                            _payments.value = list
                        }
                    }
                }

            historyListener = db.collection("history_events")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("TuitionRepository", "History listen failed.", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        _isCloudConnected.value = true
                        if (snapshot.isEmpty) {
                            _historyEvents.value = emptyList()
                        } else {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    val typeStr = doc.getString("type") ?: HistoryEventType.ENROLLMENT.name
                                    val type = try {
                                        HistoryEventType.valueOf(typeStr)
                                    } catch (e: Exception) {
                                        HistoryEventType.ENROLLMENT
                                    }
                                    val subjectsRaw = doc.get("subjects") as? List<*>
                                    val subjectsList = subjectsRaw?.mapNotNull { it as? String } ?: emptyList()

                                    TuitionHistoryItem(
                                        id = doc.id,
                                        type = type,
                                        studentId = doc.getString("studentId") ?: "",
                                        studentName = doc.getString("studentName") ?: "",
                                        studentGrade = doc.getLong("studentGrade")?.toInt() ?: 10,
                                        studentMedium = doc.getString("studentMedium") ?: Student.MEDIUM_SINHALA,
                                        studentPhone = doc.getString("studentPhone") ?: "",
                                        title = doc.getString("title") ?: "",
                                        description = doc.getString("description") ?: "",
                                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                        amount = doc.getDouble("amount"),
                                        receiptId = doc.getString("receiptId"),
                                        monthKey = doc.getString("monthKey"),
                                        subjects = subjectsList,
                                        subjectLeft = doc.getString("subjectLeft"),
                                        departureReason = doc.getString("departureReason"),
                                        note = doc.getString("note") ?: ""
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            _historyEvents.value = list.sortedByDescending { it.timestamp }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("TuitionRepository", "Failed to start listeners", e)
            _isCloudConnected.value = false
        }
    }

    fun addStudent(
        name: String,
        phone: String,
        grade: Int,
        medium: String,
        subjects: Map<String, Double>,
        subjectMediums: Map<String, String>,
        notes: String,
        monthKey: String
    ) {
        val newId = "std_${UUID.randomUUID().toString().take(8)}"
        val newStudent = Student(
            id = newId,
            name = name,
            phone = phone,
            grade = grade,
            medium = medium,
            joinedDate = System.currentTimeMillis(),
            subjects = subjects,
            subjectMediums = subjectMediums,
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

        // Log Enrollment History Event
        val enrEvent = TuitionHistoryItem(
            id = "evt_enr_${newId}",
            type = HistoryEventType.ENROLLMENT,
            studentId = newId,
            studentName = name,
            studentGrade = grade,
            studentMedium = medium,
            studentPhone = phone,
            title = "Student Enrolled: $name",
            description = "Enrolled in Grade $grade ($medium) • Subjects: ${subjects.keys.joinToString(", ")} • Total Fee: ${Student.formatCurrency(newStudent.totalMonthlyFee)}",
            timestamp = System.currentTimeMillis(),
            subjects = subjects.keys.toList(),
            note = notes.ifBlank { "New enrollment registered" }
        )
        addHistoryEvent(enrEvent)

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

    fun deleteStudent(studentId: String, reason: String = "Withdrawn / Student Left Tuition", notes: String = "") {
        val student = _students.value.find { it.id == studentId }
        if (student != null) {
            val leaveEvent = TuitionHistoryItem(
                id = "evt_leave_${UUID.randomUUID().toString().take(8)}",
                type = HistoryEventType.CLASS_LEAVING,
                studentId = studentId,
                studentName = student.name,
                studentGrade = student.grade,
                studentMedium = student.medium,
                studentPhone = student.phone,
                title = "Student Left Tuition: ${student.name}",
                description = "Withdrawn from Grade ${student.grade} (${student.medium}) • Departed from: ${student.subjects.keys.joinToString(", ")} • Reason: $reason",
                timestamp = System.currentTimeMillis(),
                subjects = student.subjects.keys.toList(),
                subjectLeft = "All Classes",
                departureReason = reason,
                note = notes.ifBlank { student.notes }
            )
            addHistoryEvent(leaveEvent)
        }

        _students.value = _students.value.filter { it.id != studentId }
        _payments.value = _payments.value.filter { it.studentId != studentId }

        firestore?.collection("students")?.document(studentId)?.delete()
    }

    fun recordClassLeaving(
        studentId: String,
        subjectLeft: String,
        departureReason: String,
        notes: String
    ) {
        val student = _students.value.find { it.id == studentId } ?: return
        val isLeavingAll = subjectLeft.equals("All Classes", ignoreCase = true) ||
                subjectLeft.equals("Withdrawn", ignoreCase = true) ||
                (student.subjects.size <= 1 && student.subjects.containsKey(subjectLeft))

        val leaveEvent = TuitionHistoryItem(
            id = "evt_leave_${UUID.randomUUID().toString().take(8)}",
            type = HistoryEventType.CLASS_LEAVING,
            studentId = studentId,
            studentName = student.name,
            studentGrade = student.grade,
            studentMedium = student.medium,
            studentPhone = student.phone,
            title = if (isLeavingAll) "Student Left Tuition: ${student.name}" else "Left Class: $subjectLeft (${student.name})",
            description = if (isLeavingAll)
                "Withdrawn from Grade ${student.grade} (${student.medium}) • Reason: $departureReason"
            else
                "Left Grade ${student.grade} $subjectLeft (${student.getMediumForSubject(subjectLeft)}) • Reason: $departureReason",
            timestamp = System.currentTimeMillis(),
            subjects = listOf(subjectLeft),
            subjectLeft = subjectLeft,
            departureReason = departureReason,
            note = notes
        )
        addHistoryEvent(leaveEvent)

        if (isLeavingAll) {
            _students.value = _students.value.filter { it.id != studentId }
            _payments.value = _payments.value.filter { it.studentId != studentId }
            firestore?.collection("students")?.document(studentId)?.delete()
        } else {
            val updatedSubjects = student.subjects.filterKeys { it != subjectLeft }
            val updatedMediums = student.subjectMediums.filterKeys { it != subjectLeft }
            updateStudent(
                student.copy(
                    subjects = updatedSubjects,
                    subjectMediums = updatedMediums
                ),
                monthKey = MonthlyPayment.getCurrentMonthKey()
            )
        }
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

        // Log Payment History Event
        val receiptId = "REC-${monthKey.replace("-", "")}-${(1000..9999).random()}"
        val payEvent = TuitionHistoryItem(
            id = "evt_pay_${UUID.randomUUID().toString().take(8)}",
            type = HistoryEventType.PAYMENT,
            studentId = studentId,
            studentName = student.name,
            studentGrade = student.grade,
            studentMedium = student.medium,
            studentPhone = student.phone,
            title = "Fee Payment: ${Student.formatCurrency(amount)}",
            description = "Tuition payment of ${Student.formatCurrency(amount)} for ${MonthlyPayment.getMonthDisplayName(monthKey)} • Receipt: #$receiptId",
            timestamp = System.currentTimeMillis(),
            amount = amount,
            receiptId = receiptId,
            monthKey = monthKey,
            note = note.ifBlank { "Fee payment processed" }
        )
        addHistoryEvent(payEvent)
    }

    fun addHistoryEvent(event: TuitionHistoryItem) {
        _historyEvents.value = (_historyEvents.value + event).sortedByDescending { it.timestamp }
        saveHistoryEventToCloud(event)
    }

    fun deleteHistoryEvent(eventId: String) {
        _historyEvents.value = _historyEvents.value.filterNot { it.id == eventId }
        firestore?.collection("history_events")?.document(eventId)?.delete()
            ?.addOnFailureListener { e ->
                Log.e("TuitionRepository", "Failed to delete history event: $eventId", e)
            }
    }

    fun deleteHistoryEvents(eventIds: Set<String>) {
        if (eventIds.isEmpty()) return
        _historyEvents.value = _historyEvents.value.filterNot { it.id in eventIds }
        val db = firestore ?: return
        try {
            eventIds.chunked(450).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { id ->
                    val ref = db.collection("history_events").document(id)
                    batch.delete(ref)
                }
                batch.commit().addOnFailureListener { e ->
                    Log.e("TuitionRepository", "Failed to batch delete history events", e)
                }
            }
        } catch (e: Exception) {
            Log.e("TuitionRepository", "Error batch deleting history events", e)
        }
    }

    fun deleteAllHistoryEvents() {
        val allIds = _historyEvents.value.map { it.id }
        _historyEvents.value = emptyList()
        val db = firestore ?: return
        try {
            allIds.chunked(450).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { id ->
                    val ref = db.collection("history_events").document(id)
                    batch.delete(ref)
                }
                batch.commit().addOnFailureListener { e ->
                    Log.e("TuitionRepository", "Failed to clear all history events", e)
                }
            }
        } catch (e: Exception) {
            Log.e("TuitionRepository", "Error clearing all history events", e)
        }
    }

    private fun saveHistoryEventToCloud(event: TuitionHistoryItem) {
        firestore?.collection("history_events")?.document(event.id)?.set(
            mapOf(
                "type" to event.type.name,
                "studentId" to event.studentId,
                "studentName" to event.studentName,
                "studentGrade" to event.studentGrade,
                "studentMedium" to event.studentMedium,
                "studentPhone" to event.studentPhone,
                "title" to event.title,
                "description" to event.description,
                "timestamp" to event.timestamp,
                "amount" to (event.amount ?: 0.0),
                "receiptId" to (event.receiptId ?: ""),
                "monthKey" to (event.monthKey ?: ""),
                "subjects" to event.subjects,
                "subjectLeft" to (event.subjectLeft ?: ""),
                "departureReason" to (event.departureReason ?: ""),
                "note" to event.note
            ),
            SetOptions.merge()
        )
    }

    private fun addStudentToCloud(student: Student) {
        firestore?.collection("students")?.document(student.id)?.set(
            mapOf(
                "name" to student.name,
                "phone" to student.phone,
                "grade" to student.grade,
                "medium" to student.medium,
                "joinedDate" to student.joinedDate,
                "subjects" to student.subjects,
                "subjectMediums" to student.subjectMediums,
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
        historyListener?.remove()
    }
}
