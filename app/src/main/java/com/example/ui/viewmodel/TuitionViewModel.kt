package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.MonthlyPayment
import com.example.data.model.PaymentStatus
import com.example.data.model.Student
import com.example.data.model.UserSession
import com.example.data.repository.AuthRepository
import com.example.data.repository.TuitionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StudentPaymentUiItem(
    val student: Student,
    val payment: MonthlyPayment
)

data class DashboardMetrics(
    val totalStudents: Int = 0,
    val totalFeesDue: Double = 0.0,
    val totalCollected: Double = 0.0,
    val totalPending: Double = 0.0
)

class TuitionViewModel(
    private val tuitionRepository: TuitionRepository = TuitionRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    val currentUser: StateFlow<UserSession?> = authRepository.currentUser
    val isCloudConnected: StateFlow<Boolean> = tuitionRepository.isCloudConnected

    private val _selectedMonth = MutableStateFlow(MonthlyPayment.getCurrentMonthKey())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _subjectFilter = MutableStateFlow<String?>(null) // null = All, "Maths", "Science", "Both"
    val subjectFilter: StateFlow<String?> = _subjectFilter.asStateFlow()

    private val _mediumFilter = MutableStateFlow<String?>(null) // null = All, "Sinhala", "English"
    val mediumFilter: StateFlow<String?> = _mediumFilter.asStateFlow()

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

    private val monthStudentPaymentItems = combine(
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
    }

    // Combined UI items for current month with active filters
    val uiItems: StateFlow<List<StudentPaymentUiItem>> = combine(
        monthStudentPaymentItems,
        _searchQuery,
        _subjectFilter,
        _mediumFilter,
        _statusFilter
    ) { items, query, subject, medium, status ->
        items.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.student.name.contains(query, ignoreCase = true) ||
                    item.student.phone.contains(query, ignoreCase = true)

            val matchesSubject = when (subject) {
                "Maths" -> item.student.hasSubject("Maths") && !item.student.hasSubject("Science")
                "Science" -> item.student.hasSubject("Science") && !item.student.hasSubject("Maths")
                "Both" -> item.student.hasSubject("Maths") && item.student.hasSubject("Science")
                else -> true
            }

            val matchesMedium = medium == null || item.student.medium.equals(medium, ignoreCase = true)

            val matchesStatus = status == null || item.payment.status == status

            matchesQuery && matchesSubject && matchesMedium && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Overall metrics for current selected month
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        tuitionRepository.students,
        tuitionRepository.payments,
        _selectedMonth
    ) { studentsList, paymentsList, month ->
        val monthPayments = studentsList.map { student ->
            paymentsList.find { it.studentId == student.id && it.monthKey == month }
                ?: MonthlyPayment(
                    studentId = student.id,
                    monthKey = month,
                    totalFeeDue = student.totalMonthlyFee,
                    amountPaid = 0.0
                )
        }

        val totalStudents = studentsList.size
        val totalDue = monthPayments.sumOf { it.totalFeeDue }
        val totalCollected = monthPayments.sumOf { it.amountPaid }
        val totalPending = (totalDue - totalCollected).coerceAtLeast(0.0)

        DashboardMetrics(
            totalStudents = totalStudents,
            totalFeesDue = totalDue,
            totalCollected = totalCollected,
            totalPending = totalPending
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    // All available payment history for detail sheet
    val allPayments: StateFlow<List<MonthlyPayment>> = tuitionRepository.payments

    fun selectMonth(monthKey: String) {
        _selectedMonth.value = monthKey
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSubjectFilter(subject: String?) {
        _subjectFilter.value = subject
    }

    fun setMediumFilter(medium: String?) {
        _mediumFilter.value = medium
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

    fun saveStudent(name: String, phone: String, medium: String, subjects: Map<String, Double>, notes: String) {
        val editing = _editingStudent.value
        val currentM = _selectedMonth.value
        if (editing != null) {
            tuitionRepository.updateStudent(
                editing.copy(
                    name = name.trim(),
                    phone = phone.trim(),
                    medium = medium.trim(),
                    subjects = subjects,
                    notes = notes.trim()
                ),
                monthKey = currentM
            )
        } else {
            tuitionRepository.addStudent(
                name = name.trim(),
                phone = phone.trim(),
                medium = medium.trim(),
                subjects = subjects,
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
            if (result.isFailure) {
                _loginError.value = result.exceptionOrNull()?.message ?: "Login failed. Check credentials."
            }
        }
    }

    fun loginDemo() {
        authRepository.signInAsDemoAdmin("admin@tuition.com", "#Sithumini&546#")
    }

    fun logout() {
        authRepository.signOut()
    }

    override fun onCleared() {
        super.onCleared()
        tuitionRepository.cleanup()
    }
}
