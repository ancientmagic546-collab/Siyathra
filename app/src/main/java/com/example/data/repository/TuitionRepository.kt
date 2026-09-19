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
            }
        } catch (e: Exception) {
            Log.e("TuitionRepository", "FirebaseFirestore init error", e)
            _isCloudConnected.value = false
        }

        initInitialData()
        startCloudListeners()
    }

    private fun initInitialData() {
        // Sample realistic students across grades 6 to 11 with Maths, Science, Commerce (G10-11), and English
        val sampleStudents = listOf(
            Student(
                id = "std_101",
                name = "Kasun Perera",
                phone = "+94 77 123 4567",
                grade = 11,
                medium = Student.MEDIUM_SINHALA,
                joinedDate = System.currentTimeMillis() - 86400000L * 30,
                subjects = mapOf("Maths" to 2500.0, "Commerce" to 2500.0, "English" to 1200.0),
                subjectMediums = mapOf("Maths" to Student.MEDIUM_SINHALA, "Commerce" to Student.MEDIUM_SINHALA, "English" to Student.MEDIUM_GENERAL),
                notes = "O/L Exam Candidate"
            ),
            Student(
                id = "std_102",
                name = "Nimali Fernando",
                phone = "+94 71 987 6543",
                grade = 11,
                medium = Student.MEDIUM_SINHALA,
                joinedDate = System.currentTimeMillis() - 86400000L * 25,
                subjects = mapOf("Maths" to 2500.0, "Science" to 2500.0),
                subjectMediums = mapOf("Maths" to Student.MEDIUM_SINHALA, "Science" to Student.MEDIUM_SINHALA),
                notes = "O/L Maths & Science Sinhala"
            ),
            Student(
                id = "std_103",
                name = "Dilshan Silva",
                phone = "+94 75 555 4321",
                grade = 10,
                medium = Student.MEDIUM_ENGLISH,
                joinedDate = System.currentTimeMillis() - 86400000L * 20,
                subjects = mapOf("Commerce" to 2500.0, "English" to 1200.0),
                subjectMediums = mapOf("Commerce" to Student.MEDIUM_ENGLISH, "English" to Student.MEDIUM_GENERAL),
                notes = "English Medium Commerce Stream"
            ),
            Student(
                id = "std_104",
                name = "Samanthi Jayasinghe",
                phone = "+94 78 888 1122",
                grade = 10,
                medium = Student.MEDIUM_ENGLISH,
                joinedDate = System.currentTimeMillis() - 86400000L * 15,
                subjects = mapOf("Science" to 2500.0, "English" to 1200.0),
                subjectMediums = mapOf("Science" to Student.MEDIUM_ENGLISH, "English" to Student.MEDIUM_GENERAL),
                notes = "Grade 10 Science & English"
            ),
            Student(
                id = "std_105",
                name = "Ravindu Senanayake",
                phone = "+94 76 333 9988",
                grade = 9,
                medium = Student.MEDIUM_SINHALA,
                joinedDate = System.currentTimeMillis() - 86400000L * 12,
                subjects = mapOf("Maths" to 2500.0, "Science" to 2500.0),
                subjectMediums = mapOf("Maths" to Student.MEDIUM_SINHALA, "Science" to Student.MEDIUM_SINHALA),
                notes = "Grade 9 Active Learner"
            ),
            Student(
                id = "std_106",
                name = "Anuki Wickramasinghe",
                phone = "+94 70 222 4455",
                grade = 8,
                medium = Student.MEDIUM_ENGLISH,
                joinedDate = System.currentTimeMillis() - 86400000L * 8,
                subjects = mapOf("Maths" to 2500.0, "Science" to 2500.0, "English" to 1200.0),
                subjectMediums = mapOf("Maths" to Student.MEDIUM_ENGLISH, "Science" to Student.MEDIUM_ENGLISH, "English" to Student.MEDIUM_GENERAL),
                notes = "Grade 8 All 3 Subjects"
            ),
            Student(
                id = "std_107",
                name = "Kavishka Bandara",
                phone = "+94 72 444 8877",
                grade = 7,
                medium = Student.MEDIUM_SINHALA,
                joinedDate = System.currentTimeMillis() - 86400000L * 6,
                subjects = mapOf("Maths" to 2500.0),
                subjectMediums = mapOf("Maths" to Student.MEDIUM_SINHALA),
                notes = "Grade 7 Maths"
            ),
            Student(
                id = "std_108",
                name = "Methmi Alwis",
                phone = "+94 77 999 1100",
                grade = 6,
                medium = Student.MEDIUM_SINHALA,
                joinedDate = System.currentTimeMillis() - 86400000L * 3,
                subjects = mapOf("Science" to 2500.0, "English" to 1200.0),
                subjectMediums = mapOf("Science" to Student.MEDIUM_SINHALA, "English" to Student.MEDIUM_GENERAL),
                notes = "Grade 6 Junior Batch"
            )
        )

        _students.value = sampleStudents

        val currentMonth = MonthlyPayment.getCurrentMonthKey()
        val prevMonth = "2026-07"
        val samplePayments = mutableListOf<MonthlyPayment>()

        // Generate payments for current month
        sampleStudents.forEachIndexed { index, student ->
            val totalDue = student.totalMonthlyFee
            val (paidAmount, note) = when (index % 4) {
                0 -> Pair(totalDue, "Full monthly tuition payment")
                1 -> Pair(2500.0, "1st installment cash receipt")
                2 -> Pair(totalDue, "Bank transfer ref #SYT${5100 + index}")
                else -> Pair(0.0, "")
            }
            val timeOffset = index * 43200000L // staggered hours
            val timestamp = System.currentTimeMillis() - timeOffset
            samplePayments.add(
                MonthlyPayment(
                    id = "${student.id}_$currentMonth",
                    studentId = student.id,
                    studentName = student.name,
                    monthKey = currentMonth,
                    totalFeeDue = totalDue,
                    amountPaid = paidAmount,
                    lastUpdated = timestamp,
                    history = if (paidAmount > 0) listOf(
                        PaymentRecord(
                            amount = paidAmount,
                            timestamp = timestamp,
                            note = note
                        )
                    ) else emptyList()
                )
            )
        }

        // Generate historical payments for previous month (July 2026)
        sampleStudents.forEachIndexed { index, student ->
            val totalDue = student.totalMonthlyFee
            val paidAmount = if (index % 5 == 4) 0.0 else totalDue
            val timeOffset = 30L * 86400000L + (index * 86400000L / 2)
            val timestamp = System.currentTimeMillis() - timeOffset
            val note = if (index % 2 == 0) "Cash at counter" else "Online bank transfer"
            samplePayments.add(
                MonthlyPayment(
                    id = "${student.id}_$prevMonth",
                    studentId = student.id,
                    studentName = student.name,
                    monthKey = prevMonth,
                    totalFeeDue = totalDue,
                    amountPaid = paidAmount,
                    lastUpdated = timestamp,
                    history = if (paidAmount > 0) listOf(
                        PaymentRecord(
                            amount = paidAmount,
                            timestamp = timestamp,
                            note = note
                        )
                    ) else emptyList()
                )
            )
        }

        _payments.value = samplePayments

        // Generate unified chronological history (Enrollments, Payments, Class Leaving)
        val sampleHistoryEvents = mutableListOf<TuitionHistoryItem>()

        // 1. Initial Enrollments
        sampleStudents.forEachIndexed { index, student ->
            sampleHistoryEvents.add(
                TuitionHistoryItem(
                    id = "evt_enr_${student.id}",
                    type = HistoryEventType.ENROLLMENT,
                    studentId = student.id,
                    studentName = student.name,
                    studentGrade = student.grade,
                    studentMedium = student.medium,
                    studentPhone = student.phone,
                    title = "Student Enrolled: ${student.name}",
                    description = "Enrolled in Grade ${student.grade} (${student.medium}) • Subjects: ${student.subjects.keys.joinToString(", ")} • Fee: ${Student.formatCurrency(student.totalMonthlyFee)}",
                    timestamp = student.joinedDate,
                    subjects = student.subjects.keys.toList(),
                    note = student.notes.ifBlank { "Registered for academic year" }
                )
            )
        }

        // 2. Initial Payments
        samplePayments.filter { it.amountPaid > 0 }.forEachIndexed { index, payment ->
            val receiptId = "REC-${payment.monthKey.replace("-", "")}-${1001 + index}"
            val student = sampleStudents.find { it.id == payment.studentId }
            sampleHistoryEvents.add(
                TuitionHistoryItem(
                    id = "evt_pay_${payment.id}",
                    type = HistoryEventType.PAYMENT,
                    studentId = payment.studentId,
                    studentName = payment.studentName,
                    studentGrade = student?.grade ?: 10,
                    studentMedium = student?.medium ?: Student.MEDIUM_SINHALA,
                    studentPhone = student?.phone ?: "",
                    title = "Fee Payment: ${Student.formatCurrency(payment.amountPaid)}",
                    description = "Tuition payment received for ${MonthlyPayment.getMonthDisplayName(payment.monthKey)} • Receipt: #$receiptId",
                    timestamp = payment.lastUpdated,
                    amount = payment.amountPaid,
                    receiptId = receiptId,
                    monthKey = payment.monthKey,
                    note = payment.history.firstOrNull()?.note ?: "Tuition payment confirmed"
                )
            )
        }

        // 3. Realistic Class Leaving / Departures
        val now = System.currentTimeMillis()
        sampleHistoryEvents.add(
            TuitionHistoryItem(
                id = "evt_leave_01",
                type = HistoryEventType.CLASS_LEAVING,
                studentId = "std_arch_01",
                studentName = "Hirun Wickramasinghe",
                studentGrade = 10,
                studentMedium = Student.MEDIUM_SINHALA,
                studentPhone = "+94 77 345 6789",
                title = "Left Class: Science",
                description = "Left Grade 10 Science (Sinhala) • Reason: Switched focus to Commerce & Business stream",
                timestamp = now - (86400000L * 2 + 3600000L * 4), // 2 days ago
                subjects = listOf("Science"),
                subjectLeft = "Science",
                departureReason = "Switched to Commerce Stream",
                note = "Academic transfer approved by coordinator"
            )
        )
        sampleHistoryEvents.add(
            TuitionHistoryItem(
                id = "evt_leave_02",
                type = HistoryEventType.CLASS_LEAVING,
                studentId = "std_arch_02",
                studentName = "Thisara Mendis",
                studentGrade = 11,
                studentMedium = Student.MEDIUM_SINHALA,
                studentPhone = "+94 71 888 4321",
                title = "Student Left Tuition: Thisara Mendis",
                description = "Withdrawn from Grade 11 (Sinhala) • Reason: Relocated with family to Kurunegala district",
                timestamp = now - (86400000L * 5 + 3600000L * 6), // 5 days ago
                subjects = listOf("Maths", "Science"),
                subjectLeft = "All Classes",
                departureReason = "Family Relocation",
                note = "Issued clearance statement & completed materials"
            )
        )
        sampleHistoryEvents.add(
            TuitionHistoryItem(
                id = "evt_leave_03",
                type = HistoryEventType.CLASS_LEAVING,
                studentId = "std_arch_03",
                studentName = "Dinuka Silva",
                studentGrade = 9,
                studentMedium = Student.MEDIUM_SINHALA,
                studentPhone = "+94 76 111 2233",
                title = "Left Class: Maths",
                description = "Left Grade 9 Maths (Sinhala) • Reason: Time slot clash with school athletics practice",
                timestamp = now - (86400000L * 10 + 3600000L * 2), // 10 days ago
                subjects = listOf("Maths"),
                subjectLeft = "Maths",
                departureReason = "Time Schedule Conflict",
                note = "Recommended for weekend revision sessions"
            )
        )
        sampleHistoryEvents.add(
            TuitionHistoryItem(
                id = "evt_leave_04",
                type = HistoryEventType.CLASS_LEAVING,
                studentId = "std_arch_04",
                studentName = "Sanduni Rajapaksa",
                studentGrade = 10,
                studentMedium = Student.MEDIUM_ENGLISH,
                studentPhone = "+94 72 999 8877",
                title = "Left Class: English",
                description = "Left Grade 10 English class • Reason: Completed O/L English preparation syllabus early",
                timestamp = now - (86400000L * 18), // 18 days ago
                subjects = listOf("English"),
                subjectLeft = "English",
                departureReason = "Course Syllabus Completed Early",
                note = "Outstanding marks in final institute assessment"
            )
        )

        // Strict order: LATEST TO OLDEST
        _historyEvents.value = sampleHistoryEvents.sortedByDescending { it.timestamp }
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
                        if (list.isNotEmpty()) {
                            _students.value = list
                        }
                    } else {
                        // Push starter data if cloud collection is blank
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

            historyListener = db.collection("history_events")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("TuitionRepository", "History listen failed.", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
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
                        if (list.isNotEmpty()) {
                            val existingMap = _historyEvents.value.associateBy { it.id }.toMutableMap()
                            list.forEach { existingMap[it.id] = it }
                            _historyEvents.value = existingMap.values.sortedByDescending { it.timestamp }
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
