package com.example.ui.viewmodel

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.HistoryEventType
import com.example.data.model.MonthlyPayment
import com.example.data.model.PaymentStatus
import com.example.data.model.Student
import com.example.data.model.TuitionHistoryItem
import com.example.data.model.UserSession
import com.example.data.repository.AuthRepository
import com.example.data.repository.TuitionRepository
import com.example.data.security.BiometricSecurityManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MenuStep {
    SELECT_GRADE,     // Screen 1: Big Grade buttons (6 to 11)
    SELECT_SUBJECT,   // Screen 2: Big Subject buttons (Maths, Science, Commerce [G10-11], English)
    SELECT_MEDIUM,    // Screen 3: Big Medium buttons (Sinhala, English)
    STUDENT_ROSTER    // Screen 4: Class students list
}

enum class ViewMode {
    CLASS_MENU,
    ALL_STUDENTS,
    PAYMENT_HISTORY
}

enum class HistorySubTab {
    TRANSACTIONS,
    STATEMENTS
}

data class PaymentTransaction(
    val receiptId: String,
    val paymentId: String,
    val studentId: String,
    val studentName: String,
    val studentGrade: Int,
    val studentMedium: String,
    val studentPhone: String,
    val monthKey: String,
    val amount: Double,
    val timestamp: Long,
    val note: String,
    val totalFeeDue: Double,
    val totalPaidSoFar: Double,
    val remainingBalance: Double,
    val status: PaymentStatus,
    val enrolledSubjects: Map<String, Double> = emptyMap()
) {
    val formattedDate: String
        get() = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))

    val formattedAmount: String
        get() = Student.formatCurrency(amount)

    val formattedRemaining: String
        get() = Student.formatCurrency(remainingBalance)
}

data class StudentPaymentUiItem(
    val student: Student,
    val payment: MonthlyPayment
)

data class DashboardMetrics(
    val totalStudents: Int = 0,
    val totalFeesDue: Double = 0.0,
    val totalCollected: Double = 0.0,
    val totalPending: Double = 0.0,
    val paidCount: Int = 0,
    val unpaidCount: Int = 0
)

data class HistoryOverviewMetrics(
    val totalEvents: Int = 0,
    val totalEnrollments: Int = 0,
    val totalPayments: Int = 0,
    val totalClassLeaving: Int = 0,
    val totalFeesCollected: Double = 0.0
)

class TuitionViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val tuitionRepository = TuitionRepository(application.applicationContext)
    private val authRepository = AuthRepository(application.applicationContext)
    val biometricSecurityManager = BiometricSecurityManager(application.applicationContext)

    val currentUser: StateFlow<UserSession?> = authRepository.currentUser
    val isCloudConnected: StateFlow<Boolean> = tuitionRepository.isCloudConnected

    // Biometric security states
    private val _isBiometricSupported = MutableStateFlow(biometricSecurityManager.canAuthenticateBiometrics())
    val isBiometricSupported: StateFlow<Boolean> = _isBiometricSupported.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(biometricSecurityManager.isBiometricEnabled())
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _isPasswordLoginRequired = MutableStateFlow(biometricSecurityManager.isPasswordLoginRequired())
    val isPasswordLoginRequired: StateFlow<Boolean> = _isPasswordLoginRequired.asStateFlow()

    private val _remainingPasswordTimeDesc = MutableStateFlow(biometricSecurityManager.getRemainingTimeDescription())
    val remainingPasswordTimeDesc: StateFlow<String> = _remainingPasswordTimeDesc.asStateFlow()

    private val _biometricError = MutableStateFlow<String?>(null)
    val biometricError: StateFlow<String?> = _biometricError.asStateFlow()

    private val _selectedMonth = MutableStateFlow(MonthlyPayment.getCurrentMonthKey())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.CLASS_MENU)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    // Hierarchical navigation step
    private val _menuStep = MutableStateFlow(MenuStep.SELECT_GRADE)
    val menuStep: StateFlow<MenuStep> = _menuStep.asStateFlow()

    // 1. Grade selection (6 to 11)
    private val _selectedGrade = MutableStateFlow(10)
    val selectedGrade: StateFlow<Int> = _selectedGrade.asStateFlow()

    // 2. Subject selection ("Maths", "Science", "Commerce", "English")
    private val _selectedSubject = MutableStateFlow(Student.SUBJECT_MATHS)
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    // 3. Medium selection ("Sinhala", "English") - English subject has no medium division
    private val _selectedMedium = MutableStateFlow(Student.MEDIUM_SINHALA)
    val selectedMedium: StateFlow<String> = _selectedMedium.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<PaymentStatus?>(null) // null = All
    val statusFilter: StateFlow<PaymentStatus?> = _statusFilter.asStateFlow()

    private val _isAddStudentOpen = MutableStateFlow(false)
    val isAddStudentOpen: StateFlow<Boolean> = _isAddStudentOpen.asStateFlow()

    private val _editingStudent = MutableStateFlow<Student?>(null)
    val editingStudent: StateFlow<Student?> = _editingStudent.asStateFlow()

    private val _paymentTargetStudent = MutableStateFlow<StudentPaymentUiItem?>(null)
    val paymentTargetStudent: StateFlow<StudentPaymentUiItem?> = _paymentTargetStudent.asStateFlow()

    private val _detailTargetStudent = MutableStateFlow<Student?>(null)
    val detailTargetStudent: StateFlow<Student?> = _detailTargetStudent.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    init {
        refreshBiometricSecurityStatus()
    }

    fun refreshBiometricSecurityStatus() {
        _isBiometricSupported.value = biometricSecurityManager.canAuthenticateBiometrics()
        _isBiometricEnabled.value = biometricSecurityManager.isBiometricEnabled()
        _isPasswordLoginRequired.value = biometricSecurityManager.isPasswordLoginRequired()
        _remainingPasswordTimeDesc.value = biometricSecurityManager.getRemainingTimeDescription()
    }

    fun setBiometricEnabled(enabled: Boolean) {
        biometricSecurityManager.setBiometricEnabled(enabled)
        _isBiometricEnabled.value = enabled
        refreshBiometricSecurityStatus()
    }

    fun resetBiometric() {
        biometricSecurityManager.resetBiometricSettings()
        _biometricError.value = null
        refreshBiometricSecurityStatus()
    }

    fun promptBiometricUnlock(activity: FragmentActivity) {
        _biometricError.value = null
        if (biometricSecurityManager.isPasswordLoginRequired()) {
            _loginError.value = "72-Hour Security Check: Please sign in with email and password."
            refreshBiometricSecurityStatus()
            return
        }

        biometricSecurityManager.promptBiometricAuth(
            activity = activity,
            title = "Unlock Siyathra",
            subtitle = "Scan your fingerprint to access tuition records",
            negativeButtonText = "Use Password",
            onSuccess = {
                val savedPass = authRepository.getAdminPassword()
                authRepository.signInAsDemoAdmin("admin@tuition.com", savedPass)
                _biometricError.value = null
            },
            onError = { code, errString ->
                if (code != androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED &&
                    code != androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    _biometricError.value = errString.toString()
                }
            },
            onFailed = {
                _biometricError.value = "Fingerprint not recognized. Please try again or use password."
            }
        )
    }

    fun forceRequirePassword() {
        biometricSecurityManager.invalidatePasswordAuthSession()
        refreshBiometricSecurityStatus()
    }

    // All student-payment items for the selected month
    private val monthStudentPaymentItems: StateFlow<List<StudentPaymentUiItem>> = combine(
        tuitionRepository.students,
        tuitionRepository.payments,
        _selectedMonth
    ) { studentsList, paymentsList, month ->
        studentsList.map { student ->
            val payment = paymentsList.find { it.studentId == student.id && it.monthKey == month }
                ?: MonthlyPayment(
                    id = "${student.id}_$month",
                    studentId = student.id,
                    studentName = student.name,
                    monthKey = month,
                    totalFeeDue = student.totalMonthlyFee,
                    amountPaid = 0.0,
                    lastUpdated = System.currentTimeMillis()
                )
            StudentPaymentUiItem(student = student, payment = payment)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combine Class Selectors into a single state
    private data class ClassSelection(
        val grade: Int,
        val subject: String,
        val medium: String,
        val query: String,
        val status: PaymentStatus?
    )

    private val classSelectionFlow = combine(
        _selectedGrade,
        _selectedSubject,
        _selectedMedium,
        _searchQuery,
        _statusFilter
    ) { grade, subject, medium, query, status ->
        ClassSelection(grade, subject, medium, query, status)
    }

    // Active Class Student Roster (Filtered by Grade -> Subject -> Medium)
    val classUiItems: StateFlow<List<StudentPaymentUiItem>> = combine(
        monthStudentPaymentItems,
        classSelectionFlow
    ) { items, selection ->
        items.filter { item ->
            val inClass = item.student.isEnrolledInClass(
                targetGrade = selection.grade,
                targetSubject = selection.subject,
                targetMedium = if (selection.subject == Student.SUBJECT_ENGLISH) null else selection.medium
            )

            val matchesQuery = selection.query.isBlank() ||
                    item.student.name.contains(selection.query, ignoreCase = true) ||
                    item.student.phone.contains(selection.query, ignoreCase = true)

            val matchesStatus = selection.status == null || item.payment.status == selection.status

            inClass && matchesQuery && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Class Metrics (Calculated for current Grade + Subject + Medium)
    val classMetrics: StateFlow<DashboardMetrics> = combine(
        monthStudentPaymentItems,
        _selectedGrade,
        _selectedSubject,
        _selectedMedium
    ) { items, grade, subject, medium ->
        val classStudents = items.filter { item ->
            item.student.isEnrolledInClass(
                targetGrade = grade,
                targetSubject = subject,
                targetMedium = if (subject == Student.SUBJECT_ENGLISH) null else medium
            )
        }

        val totalStudents = classStudents.size
        val totalDue = classStudents.sumOf { it.student.subjects[subject] ?: 0.0 }
        val totalCollected = classStudents.sumOf { it.payment.amountPaid }
        val totalPending = (totalDue - totalCollected).coerceAtLeast(0.0)
        val paidCount = classStudents.count { it.payment.status == PaymentStatus.PAID }
        val unpaidCount = classStudents.count { it.payment.status != PaymentStatus.PAID }

        DashboardMetrics(
            totalStudents = totalStudents,
            totalFeesDue = totalDue,
            totalCollected = totalCollected,
            totalPending = totalPending,
            paidCount = paidCount,
            unpaidCount = unpaidCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    // All Students Directory (For global lookup and management)
    val allStudentsUiItems: StateFlow<List<StudentPaymentUiItem>> = combine(
        monthStudentPaymentItems,
        _searchQuery,
        _statusFilter
    ) { items, query, status ->
        items.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.student.name.contains(query, ignoreCase = true) ||
                    item.student.phone.contains(query, ignoreCase = true) ||
                    item.student.gradeDisplayName.contains(query, ignoreCase = true)

            val matchesStatus = status == null || item.payment.status == status

            matchesQuery && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Overall metrics for current selected month across the institute
    val overallMetrics: StateFlow<DashboardMetrics> = monthStudentPaymentItems.combine(
        MutableStateFlow(Unit)
    ) { items, _ ->
        val totalStudents = items.size
        val totalDue = items.sumOf { it.payment.totalFeeDue }
        val totalCollected = items.sumOf { it.payment.amountPaid }
        val totalPending = (totalDue - totalCollected).coerceAtLeast(0.0)
        val paidCount = items.count { it.payment.status == PaymentStatus.PAID }
        val unpaidCount = items.count { it.payment.status != PaymentStatus.PAID }

        DashboardMetrics(
            totalStudents = totalStudents,
            totalFeesDue = totalDue,
            totalCollected = totalCollected,
            totalPending = totalPending,
            paidCount = paidCount,
            unpaidCount = unpaidCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    // Counts per Grade
    val gradeCounts: StateFlow<Map<Int, Int>> = tuitionRepository.students.combine(_selectedMonth) { list, _ ->
        val counts = mutableMapOf<Int, Int>()
        Student.ALL_GRADES.forEach { g ->
            counts[g] = list.count { it.grade == g }
        }
        counts
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Counts per Subject in Selected Grade
    val subjectCountsInGrade: StateFlow<Map<String, Int>> = combine(
        tuitionRepository.students,
        _selectedGrade
    ) { list, grade ->
        val counts = mutableMapOf<String, Int>()
        Student.getAvailableSubjectsForGrade(grade).forEach { s ->
            counts[s] = list.count { it.grade == grade && it.hasSubject(s) }
        }
        counts
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Counts per Medium in Selected Grade & Subject
    val mediumCountsInGradeSubject: StateFlow<Map<String, Int>> = combine(
        tuitionRepository.students,
        _selectedGrade,
        _selectedSubject
    ) { list, grade, subject ->
        val counts = mutableMapOf<String, Int>()
        Student.MEDIUM_OPTIONS.forEach { m ->
            counts[m] = list.count {
                it.grade == grade && it.hasSubject(subject) && it.getMediumForSubject(subject).equals(m, ignoreCase = true)
            }
        }
        counts
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // All available payment history for detail sheet
    val allPayments: StateFlow<List<MonthlyPayment>> = tuitionRepository.payments

    // --- Payment History States & Flows ---
    private val _historySubTab = MutableStateFlow(HistorySubTab.TRANSACTIONS)
    val historySubTab: StateFlow<HistorySubTab> = _historySubTab.asStateFlow()

    private val _historyMonthFilter = MutableStateFlow<String?>(null) // null = All Months
    val historyMonthFilter: StateFlow<String?> = _historyMonthFilter.asStateFlow()

    private val _historyGradeFilter = MutableStateFlow<Int?>(null) // null = All Grades
    val historyGradeFilter: StateFlow<Int?> = _historyGradeFilter.asStateFlow()

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    private val _historyStatusFilter = MutableStateFlow<PaymentStatus?>(null)
    val historyStatusFilter: StateFlow<PaymentStatus?> = _historyStatusFilter.asStateFlow()

    private val _receiptTarget = MutableStateFlow<PaymentTransaction?>(null)
    val receiptTarget: StateFlow<PaymentTransaction?> = _receiptTarget.asStateFlow()

    private val _isQuickPaymentPickerOpen = MutableStateFlow(false)
    val isQuickPaymentPickerOpen: StateFlow<Boolean> = _isQuickPaymentPickerOpen.asStateFlow()

    // Chronological Feed of Payment Transactions
    val allPaymentTransactions: StateFlow<List<PaymentTransaction>> = combine(
        tuitionRepository.students,
        tuitionRepository.payments,
        _historyMonthFilter,
        _historyGradeFilter,
        _historySearchQuery
    ) { students, payments, monthFilter, gradeFilter, query ->
        val studentMap = students.associateBy { it.id }
        val transactions = mutableListOf<PaymentTransaction>()

        payments.forEach { payment ->
            val student = studentMap[payment.studentId]
            val grade = student?.grade ?: 10
            val medium = student?.medium ?: Student.MEDIUM_SINHALA
            val phone = student?.phone ?: ""
            val subjects = student?.subjects ?: emptyMap()

            // Check month filter
            if (monthFilter != null && payment.monthKey != monthFilter) {
                return@forEach
            }

            // Check grade filter
            if (gradeFilter != null && grade != gradeFilter) {
                return@forEach
            }

            if (payment.history.isNotEmpty()) {
                payment.history.forEachIndexed { idx, record ->
                    val recId = "REC-${payment.monthKey.replace("-", "")}-${payment.studentId.takeLast(4).uppercase()}-$idx"
                    transactions.add(
                        PaymentTransaction(
                            receiptId = recId,
                            paymentId = payment.id,
                            studentId = payment.studentId,
                            studentName = payment.studentName.ifBlank { student?.name ?: "Student" },
                            studentGrade = grade,
                            studentMedium = medium,
                            studentPhone = phone,
                            monthKey = payment.monthKey,
                            amount = record.amount,
                            timestamp = record.timestamp,
                            note = record.note.ifBlank { "Tuition fee payment" },
                            totalFeeDue = payment.totalFeeDue,
                            totalPaidSoFar = payment.amountPaid,
                            remainingBalance = payment.remainingBalance,
                            status = payment.status,
                            enrolledSubjects = subjects
                        )
                    )
                }
            } else if (payment.amountPaid > 0) {
                val recId = "REC-${payment.monthKey.replace("-", "")}-${payment.studentId.takeLast(4).uppercase()}-0"
                transactions.add(
                    PaymentTransaction(
                        receiptId = recId,
                        paymentId = payment.id,
                        studentId = payment.studentId,
                        studentName = payment.studentName.ifBlank { student?.name ?: "Student" },
                        studentGrade = grade,
                        studentMedium = medium,
                        studentPhone = phone,
                        monthKey = payment.monthKey,
                        amount = payment.amountPaid,
                        timestamp = payment.lastUpdated,
                        note = "Tuition fee payment",
                        totalFeeDue = payment.totalFeeDue,
                        totalPaidSoFar = payment.amountPaid,
                        remainingBalance = payment.remainingBalance,
                        status = payment.status,
                        enrolledSubjects = subjects
                    )
                )
            }
        }

        transactions.filter { tx ->
            query.isBlank() ||
                    tx.studentName.contains(query, ignoreCase = true) ||
                    tx.studentPhone.contains(query, ignoreCase = true) ||
                    tx.receiptId.contains(query, ignoreCase = true) ||
                    tx.note.contains(query, ignoreCase = true)
        }.sortedByDescending { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private data class HistoryStatementFilter(
        val monthFilter: String?,
        val defaultMonth: String,
        val gradeFilter: Int?,
        val query: String,
        val statusFilter: PaymentStatus?
    )

    private val historyStatementFilterFlow = combine(
        _historyMonthFilter,
        _selectedMonth,
        _historyGradeFilter,
        _historySearchQuery,
        _historyStatusFilter
    ) { monthFilter, defaultMonth, gradeFilter, query, statusFilter ->
        HistoryStatementFilter(monthFilter, defaultMonth, gradeFilter, query, statusFilter)
    }

    // Monthly Fee Statements per Student
    val historyStatements: StateFlow<List<StudentPaymentUiItem>> = combine(
        tuitionRepository.students,
        tuitionRepository.payments,
        historyStatementFilterFlow
    ) { students, payments, filter ->
        val activeMonth = filter.monthFilter ?: filter.defaultMonth

        students.filter { s ->
            (filter.gradeFilter == null || s.grade == filter.gradeFilter) &&
            (filter.query.isBlank() || s.name.contains(filter.query, ignoreCase = true) || s.phone.contains(filter.query, ignoreCase = true))
        }.map { student ->
            val payment = payments.find { it.studentId == student.id && it.monthKey == activeMonth }
                ?: MonthlyPayment(
                    id = "${student.id}_$activeMonth",
                    studentId = student.id,
                    studentName = student.name,
                    monthKey = activeMonth,
                    totalFeeDue = student.totalMonthlyFee,
                    amountPaid = 0.0,
                    lastUpdated = System.currentTimeMillis()
                )
            StudentPaymentUiItem(student = student, payment = payment)
        }.filter { item ->
            filter.statusFilter == null || item.payment.status == filter.statusFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Metrics for the Payment History View
    val historyMetrics: StateFlow<DashboardMetrics> = combine(
        allPaymentTransactions,
        historyStatements
    ) { transactions, statements ->
        val totalCollected = transactions.sumOf { it.amount }
        val totalDue = statements.sumOf { it.payment.totalFeeDue }
        val totalPending = statements.sumOf { it.payment.remainingBalance }
        val paidCount = statements.count { it.payment.status == PaymentStatus.PAID }
        val unpaidCount = statements.count { it.payment.status != PaymentStatus.PAID }

        DashboardMetrics(
            totalStudents = statements.size,
            totalFeesDue = totalDue,
            totalCollected = totalCollected,
            totalPending = totalPending,
            paidCount = paidCount,
            unpaidCount = unpaidCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    fun setHistorySubTab(tab: HistorySubTab) {
        _historySubTab.value = tab
    }

    fun setHistoryMonthFilter(monthKey: String?) {
        _historyMonthFilter.value = monthKey
    }

    fun setHistoryGradeFilter(grade: Int?) {
        _historyGradeFilter.value = grade
    }

    fun setHistorySearchQuery(query: String) {
        _historySearchQuery.value = query
    }

    fun setHistoryStatusFilter(status: PaymentStatus?) {
        _historyStatusFilter.value = status
    }

    fun openReceipt(transaction: PaymentTransaction) {
        _receiptTarget.value = transaction
    }

    fun openReceiptForStudent(item: StudentPaymentUiItem) {
        val lastRecord = item.payment.history.lastOrNull()
        val tx = PaymentTransaction(
            receiptId = "REC-${item.payment.monthKey.replace("-", "")}-${item.student.id.takeLast(4).uppercase()}",
            paymentId = item.payment.id,
            studentId = item.student.id,
            studentName = item.student.name,
            studentGrade = item.student.grade,
            studentMedium = item.student.medium,
            studentPhone = item.student.phone,
            monthKey = item.payment.monthKey,
            amount = lastRecord?.amount ?: item.payment.amountPaid,
            timestamp = lastRecord?.timestamp ?: item.payment.lastUpdated,
            note = lastRecord?.note ?: "Tuition payment receipt",
            totalFeeDue = item.payment.totalFeeDue,
            totalPaidSoFar = item.payment.amountPaid,
            remainingBalance = item.payment.remainingBalance,
            status = item.payment.status,
            enrolledSubjects = item.student.subjects
        )
        _receiptTarget.value = tx
    }

    fun closeReceipt() {
        _receiptTarget.value = null
    }

    fun openQuickRecordPaymentPicker() {
        _isQuickPaymentPickerOpen.value = true
    }

    fun openQuickPaymentPicker() {
        openQuickRecordPaymentPicker()
    }

    fun closeQuickPaymentPicker() {
        _isQuickPaymentPickerOpen.value = false
    }

    // --- Unified Chronological History Stream (Enrollments, Payments, Class Leaving) ---
    private val _historyEventTypeFilter = MutableStateFlow<HistoryEventType?>(null) // null = ALL
    val historyEventTypeFilter: StateFlow<HistoryEventType?> = _historyEventTypeFilter.asStateFlow()

    val historyEventsFeed: StateFlow<List<TuitionHistoryItem>> = combine(
        tuitionRepository.historyEvents,
        _historyEventTypeFilter,
        _historyGradeFilter,
        _historySearchQuery
    ) { events, typeFilter, gradeFilter, query ->
        events.filter { event ->
            val matchesType = typeFilter == null || event.type == typeFilter
            val matchesGrade = gradeFilter == null || event.studentGrade == gradeFilter
            val matchesQuery = query.isBlank() ||
                    event.studentName.contains(query, ignoreCase = true) ||
                    event.title.contains(query, ignoreCase = true) ||
                    event.description.contains(query, ignoreCase = true) ||
                    (event.receiptId?.contains(query, ignoreCase = true) == true) ||
                    (event.subjectLeft?.contains(query, ignoreCase = true) == true) ||
                    (event.departureReason?.contains(query, ignoreCase = true) == true) ||
                    event.subjects.any { it.contains(query, ignoreCase = true) }

            matchesType && matchesGrade && matchesQuery
        }.sortedByDescending { it.timestamp } // STRICTLY LATEST TO OLDEST
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyOverviewMetrics: StateFlow<HistoryOverviewMetrics> = tuitionRepository.historyEvents.combine(
        MutableStateFlow(Unit)
    ) { list, _ ->
        HistoryOverviewMetrics(
            totalEvents = list.size,
            totalEnrollments = list.count { it.type == HistoryEventType.ENROLLMENT },
            totalPayments = list.count { it.type == HistoryEventType.PAYMENT },
            totalClassLeaving = list.count { it.type == HistoryEventType.CLASS_LEAVING },
            totalFeesCollected = list.filter { it.type == HistoryEventType.PAYMENT }.sumOf { it.amount ?: 0.0 }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryOverviewMetrics())

    private val _isRecordClassLeavingOpen = MutableStateFlow(false)
    val isRecordClassLeavingOpen: StateFlow<Boolean> = _isRecordClassLeavingOpen.asStateFlow()

    private val _classLeavingTargetStudent = MutableStateFlow<Student?>(null)
    val classLeavingTargetStudent: StateFlow<Student?> = _classLeavingTargetStudent.asStateFlow()

    fun setHistoryEventTypeFilter(type: HistoryEventType?) {
        _historyEventTypeFilter.value = type
    }

    fun openRecordClassLeaving(student: Student? = null) {
        _classLeavingTargetStudent.value = student
        _isRecordClassLeavingOpen.value = true
    }

    fun closeRecordClassLeaving() {
        _isRecordClassLeavingOpen.value = false
        _classLeavingTargetStudent.value = null
    }

    fun submitClassLeaving(studentId: String, subjectLeft: String, reason: String, note: String) {
        tuitionRepository.recordClassLeaving(
            studentId = studentId,
            subjectLeft = subjectLeft,
            departureReason = reason,
            notes = note
        )
        closeRecordClassLeaving()
        if (_detailTargetStudent.value?.id == studentId) {
            val updated = tuitionRepository.students.value.find { it.id == studentId }
            _detailTargetStudent.value = updated
        }
    }

    fun openReceiptFromHistory(item: TuitionHistoryItem) {
        val student = tuitionRepository.students.value.find { it.id == item.studentId }
        val payment = tuitionRepository.payments.value.find { it.studentId == item.studentId && it.monthKey == (item.monthKey ?: MonthlyPayment.getCurrentMonthKey()) }
        val transaction = PaymentTransaction(
            receiptId = item.receiptId ?: "REC-${item.id.takeLast(6).uppercase()}",
            paymentId = payment?.id ?: "${item.studentId}_${item.monthKey ?: MonthlyPayment.getCurrentMonthKey()}",
            studentId = item.studentId,
            studentName = item.studentName,
            studentGrade = item.studentGrade,
            studentMedium = item.studentMedium,
            studentPhone = item.studentPhone,
            monthKey = item.monthKey ?: MonthlyPayment.getCurrentMonthKey(),
            amount = item.amount ?: 0.0,
            timestamp = item.timestamp,
            note = item.note.ifBlank { "Fee payment recorded" },
            totalFeeDue = payment?.totalFeeDue ?: student?.totalMonthlyFee ?: (item.amount ?: 0.0),
            totalPaidSoFar = payment?.amountPaid ?: (item.amount ?: 0.0),
            remainingBalance = payment?.remainingBalance ?: 0.0,
            status = payment?.status ?: PaymentStatus.PAID,
            enrolledSubjects = student?.subjects ?: emptyMap()
        )
        _receiptTarget.value = transaction
    }

    fun deleteHistoryEvent(eventId: String) {
        tuitionRepository.deleteHistoryEvent(eventId)
    }

    fun deleteHistoryEvents(eventIds: Set<String>) {
        tuitionRepository.deleteHistoryEvents(eventIds)
    }

    fun deleteAllHistoryEvents() {
        tuitionRepository.deleteAllHistoryEvents()
    }

    // --- Navigation Flow Methods ---

    fun onGradeSelected(grade: Int) {
        _selectedGrade.value = grade
        // If switching to a grade where Commerce is not available, reset to Maths if Commerce was selected
        if (!Student.isCommerceAvailableForGrade(grade) && _selectedSubject.value == Student.SUBJECT_COMMERCE) {
            _selectedSubject.value = Student.SUBJECT_MATHS
        }
        _searchQuery.value = ""
        _statusFilter.value = null
        _menuStep.value = MenuStep.SELECT_SUBJECT
    }

    fun onSubjectSelected(subject: String) {
        _selectedSubject.value = subject
        _searchQuery.value = ""
        _statusFilter.value = null
        if (subject == Student.SUBJECT_ENGLISH) {
            // English has no medium selection -> go directly to class students list
            _menuStep.value = MenuStep.STUDENT_ROSTER
        } else {
            // Maths / Science / Commerce -> select medium (Sinhala / English)
            _menuStep.value = MenuStep.SELECT_MEDIUM
        }
    }

    fun onMediumSelected(medium: String) {
        _selectedMedium.value = medium
        _searchQuery.value = ""
        _statusFilter.value = null
        _menuStep.value = MenuStep.STUDENT_ROSTER
    }

    fun navigateBackFromStep(): Boolean {
        return when (_menuStep.value) {
            MenuStep.STUDENT_ROSTER -> {
                if (_selectedSubject.value == Student.SUBJECT_ENGLISH) {
                    _menuStep.value = MenuStep.SELECT_SUBJECT
                } else {
                    _menuStep.value = MenuStep.SELECT_MEDIUM
                }
                true
            }
            MenuStep.SELECT_MEDIUM -> {
                _menuStep.value = MenuStep.SELECT_SUBJECT
                true
            }
            MenuStep.SELECT_SUBJECT -> {
                _menuStep.value = MenuStep.SELECT_GRADE
                true
            }
            MenuStep.SELECT_GRADE -> {
                false // already at root
            }
        }
    }

    fun resetMenuToGradeSelection() {
        _menuStep.value = MenuStep.SELECT_GRADE
        _searchQuery.value = ""
        _statusFilter.value = null
    }

    fun selectMonth(monthKey: String) {
        _selectedMonth.value = monthKey
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: PaymentStatus?) {
        _statusFilter.value = status
    }

    fun openAddStudent() {
        _editingStudent.value = null
        _isAddStudentOpen.value = true
    }

    fun openEditStudent(student: Student) {
        _editingStudent.value = student
        _isAddStudentOpen.value = true
    }

    fun closeAddStudent() {
        _isAddStudentOpen.value = false
        _editingStudent.value = null
    }

    fun saveStudent(
        name: String,
        phone: String,
        grade: Int,
        medium: String,
        subjects: Map<String, Double>,
        subjectMediums: Map<String, String>,
        notes: String
    ) {
        val editing = _editingStudent.value
        val currentM = _selectedMonth.value
        if (editing != null) {
            tuitionRepository.updateStudent(
                editing.copy(
                    name = name.trim(),
                    phone = phone.trim(),
                    grade = grade,
                    medium = medium.trim(),
                    subjects = subjects,
                    subjectMediums = subjectMediums,
                    notes = notes.trim()
                ),
                monthKey = currentM
            )
        } else {
            tuitionRepository.addStudent(
                name = name.trim(),
                phone = phone.trim(),
                grade = grade,
                medium = medium.trim(),
                subjects = subjects,
                subjectMediums = subjectMediums,
                notes = notes.trim(),
                monthKey = currentM
            )
        }
        closeAddStudent()
    }

    fun deleteStudent(studentId: String) {
        tuitionRepository.deleteStudent(studentId)
        if (_detailTargetStudent.value?.id == studentId) {
            _detailTargetStudent.value = null
        }
    }

    fun openRecordPayment(item: StudentPaymentUiItem) {
        _paymentTargetStudent.value = item
    }

    fun closeRecordPayment() {
        _paymentTargetStudent.value = null
    }

    fun submitPayment(amount: Double, note: String) {
        val target = _paymentTargetStudent.value ?: return
        tuitionRepository.recordPayment(
            studentId = target.student.id,
            monthKey = _selectedMonth.value,
            amount = amount,
            note = note
        )
        closeRecordPayment()
    }

    fun openStudentDetail(student: Student) {
        _detailTargetStudent.value = student
    }

    fun closeStudentDetail() {
        _detailTargetStudent.value = null
    }

    fun openSettings() {
        refreshBiometricSecurityStatus()
        _isSettingsOpen.value = true
    }

    fun closeSettings() {
        _isSettingsOpen.value = false
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _loginError.value = null
            val result = authRepository.signInWithEmail(email, password)
            _isAuthLoading.value = false
            if (result.isSuccess) {
                // Successfully authenticated with email/password -> reset 72h timer
                biometricSecurityManager.recordPasswordLoginSuccess()
                refreshBiometricSecurityStatus()
            } else {
                _loginError.value = result.exceptionOrNull()?.message ?: "Login failed. Check credentials."
            }
        }
    }

    fun resetPassword(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.resetPassword(email)
            if (result.isSuccess) {
                onResult(true, result.getOrNull() ?: "Password reset instructions sent.")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to reset password.")
            }
        }
    }

    fun changePassword(oldPass: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        val result = authRepository.changePassword(oldPass, newPass)
        if (result.isSuccess) {
            biometricSecurityManager.recordPasswordLoginSuccess()
            refreshBiometricSecurityStatus()
            onResult(true, "Password updated successfully!")
        } else {
            onResult(false, result.exceptionOrNull()?.message ?: "Failed to update password.")
        }
    }

    fun loginDemo() {
        val savedPass = authRepository.getAdminPassword()
        authRepository.signInAsDemoAdmin("admin@tuition.com", savedPass)
        biometricSecurityManager.recordPasswordLoginSuccess()
        refreshBiometricSecurityStatus()
    }

    fun logout() {
        authRepository.signOut()
        refreshBiometricSecurityStatus()
    }

    fun verifyFirestoreStructure(onResult: (Boolean, String) -> Unit) {
        tuitionRepository.checkAndInitializeFirestoreStructure(onResult)
    }

    override fun onCleared() {
        super.onCleared()
        tuitionRepository.cleanup()
    }
}
