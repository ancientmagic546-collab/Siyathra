package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MonthlyPayment
import com.example.data.model.PaymentStatus
import com.example.data.model.Student
import com.example.ui.components.AddStudentDialog
import com.example.ui.components.MetricCard
import com.example.ui.components.PaymentReceiptDialog
import com.example.ui.components.QuickRecordPaymentDialog
import com.example.ui.components.RecordClassLeavingDialog
import com.example.ui.components.RecordPaymentDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.StudentCard
import com.example.ui.components.StudentDetailBottomSheet
import com.example.ui.screens.PaymentHistorySection
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.StatusPaidContainer
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.StatusUnpaidRed
import com.example.ui.viewmodel.HistorySubTab
import com.example.ui.viewmodel.MenuStep
import com.example.ui.viewmodel.TuitionViewModel
import com.example.ui.viewmodel.ViewMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TuitionDashboardScreen(
    viewModel: TuitionViewModel
) {
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val menuStep by viewModel.menuStep.collectAsStateWithLifecycle()
    val selectedGrade by viewModel.selectedGrade.collectAsStateWithLifecycle()
    val selectedSubject by viewModel.selectedSubject.collectAsStateWithLifecycle()
    val selectedMedium by viewModel.selectedMedium.collectAsStateWithLifecycle()

    val classUiItems by viewModel.classUiItems.collectAsStateWithLifecycle()
    val allStudentsUiItems by viewModel.allStudentsUiItems.collectAsStateWithLifecycle()
    val classMetrics by viewModel.classMetrics.collectAsStateWithLifecycle()
    val overallMetrics by viewModel.overallMetrics.collectAsStateWithLifecycle()

    val gradeCounts by viewModel.gradeCounts.collectAsStateWithLifecycle()
    val subjectCounts by viewModel.subjectCountsInGrade.collectAsStateWithLifecycle()
    val mediumCounts by viewModel.mediumCountsInGradeSubject.collectAsStateWithLifecycle()

    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()

    val userSession by viewModel.currentUser.collectAsStateWithLifecycle()
    val isCloudConnected by viewModel.isCloudConnected.collectAsStateWithLifecycle()
    val isBiometricSupported by viewModel.isBiometricSupported.collectAsStateWithLifecycle()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val remainingPasswordTimeDesc by viewModel.remainingPasswordTimeDesc.collectAsStateWithLifecycle()

    val isAddStudentOpen by viewModel.isAddStudentOpen.collectAsStateWithLifecycle()
    val editingStudent by viewModel.editingStudent.collectAsStateWithLifecycle()
    val paymentTarget by viewModel.paymentTargetStudent.collectAsStateWithLifecycle()
    val detailTarget by viewModel.detailTargetStudent.collectAsStateWithLifecycle()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsStateWithLifecycle()
    val allPayments by viewModel.allPayments.collectAsStateWithLifecycle()

    // History states
    val historyEventsFeed by viewModel.historyEventsFeed.collectAsStateWithLifecycle()
    val historyOverviewMetrics by viewModel.historyOverviewMetrics.collectAsStateWithLifecycle()
    val historyEventTypeFilter by viewModel.historyEventTypeFilter.collectAsStateWithLifecycle()
    val isRecordClassLeavingOpen by viewModel.isRecordClassLeavingOpen.collectAsStateWithLifecycle()
    val classLeavingTargetStudent by viewModel.classLeavingTargetStudent.collectAsStateWithLifecycle()

    val historySubTab by viewModel.historySubTab.collectAsStateWithLifecycle()
    val historyMonthFilter by viewModel.historyMonthFilter.collectAsStateWithLifecycle()
    val historyGradeFilter by viewModel.historyGradeFilter.collectAsStateWithLifecycle()
    val historySearchQuery by viewModel.historySearchQuery.collectAsStateWithLifecycle()
    val historyStatusFilter by viewModel.historyStatusFilter.collectAsStateWithLifecycle()
    val allPaymentTransactions by viewModel.allPaymentTransactions.collectAsStateWithLifecycle()
    val historyStatements by viewModel.historyStatements.collectAsStateWithLifecycle()
    val historyMetrics by viewModel.historyMetrics.collectAsStateWithLifecycle()
    val receiptTarget by viewModel.receiptTarget.collectAsStateWithLifecycle()
    val isQuickPaymentPickerOpen by viewModel.isQuickPaymentPickerOpen.collectAsStateWithLifecycle()

    var showMonthDropdown by remember { mutableStateOf(false) }

    val monthOptions = listOf(
        "2026-08",
        "2026-07",
        "2026-06",
        "2026-05",
        "2026-04"
    )

    // Handle System Back Button for hierarchical menu navigation
    val canGoBackInMenu = viewMode == ViewMode.CLASS_MENU && menuStep != MenuStep.SELECT_GRADE
    BackHandler(enabled = canGoBackInMenu) {
        viewModel.navigateBackFromStep()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (viewMode == ViewMode.CLASS_MENU && menuStep != MenuStep.SELECT_GRADE) {
                        IconButton(
                            onClick = { viewModel.navigateBackFromStep() },
                            modifier = Modifier.testTag("button_nav_back")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(start = 12.dp, end = 4.dp)
                                .size(38.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(Brush.linearGradient(listOf(PrimaryBlue, SecondaryTeal))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "S",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            )
                        }
                    }
                },
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val titleText = when (viewMode) {
                                ViewMode.CLASS_MENU -> {
                                    when (menuStep) {
                                        MenuStep.SELECT_GRADE -> "Siyathra Institute"
                                        MenuStep.SELECT_SUBJECT -> "Grade $selectedGrade"
                                        MenuStep.SELECT_MEDIUM -> "Grade $selectedGrade • $selectedSubject"
                                        MenuStep.STUDENT_ROSTER -> if (selectedSubject == Student.SUBJECT_ENGLISH)
                                            "Grade $selectedGrade • English"
                                        else
                                            "Grade $selectedGrade • $selectedSubject ($selectedMedium)"
                                    }
                                }
                                ViewMode.ALL_STUDENTS -> "Student Directory"
                                ViewMode.PAYMENT_HISTORY -> "Institute History"
                            }

                            Text(
                                text = titleText,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Cloud Sync Status Badge
                            Surface(
                                shape = CircleShape,
                                color = if (isCloudConnected) StatusPaidContainer else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isCloudConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                        contentDescription = "Cloud Sync",
                                        modifier = Modifier.size(12.dp),
                                        tint = if (isCloudConnected) StatusPaidGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isCloudConnected) "Live" else "Offline",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = if (isCloudConnected) StatusPaidGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Admin: ${userSession?.displayName ?: "Sithumini"} • Academic Portal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Month Picker Pill
                    Box {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryBlue.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clickable { showMonthDropdown = true }
                                .testTag("month_selector_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Select Month",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = MonthlyPayment.getMonthDisplayName(selectedMonth),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue
                                    )
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showMonthDropdown,
                            onDismissRequest = { showMonthDropdown = false }
                        ) {
                            monthOptions.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(MonthlyPayment.getMonthDisplayName(m)) },
                                    onClick = {
                                        viewModel.selectMonth(m)
                                        showMonthDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Settings Button
                    IconButton(
                        onClick = { viewModel.openSettings() },
                        modifier = Modifier.testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (viewMode == ViewMode.PAYMENT_HISTORY) {
                Row(
                    modifier = Modifier.padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.openRecordClassLeaving() },
                        icon = { Icon(Icons.Default.PersonRemove, contentDescription = null) },
                        text = { Text(text = "Log Leaving", fontWeight = FontWeight.Bold) },
                        containerColor = Color(0xFFE65100),
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_record_class_leaving")
                    )
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.openQuickRecordPaymentPicker() },
                        icon = { Icon(Icons.Default.Payments, contentDescription = null) },
                        text = { Text(text = "Record Fee", fontWeight = FontWeight.Bold) },
                        containerColor = StatusPaidGreen,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_record_payment")
                    )
                }
            } else {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openAddStudent() },
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                    text = {
                        val label = if (viewMode == ViewMode.CLASS_MENU && menuStep == MenuStep.STUDENT_ROSTER) {
                            "Enroll to this Class"
                        } else if (viewMode == ViewMode.CLASS_MENU && menuStep != MenuStep.SELECT_GRADE) {
                            "Enroll in Grade $selectedGrade"
                        } else {
                            "Enroll Student"
                        }
                        Text(text = label, fontWeight = FontWeight.Bold)
                    },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    modifier = Modifier
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                        .testTag("fab_add_student")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top View Mode Switcher (Classes Menu | All Students | Payment History)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Tab 1: Classes Menu
                    val isMenuSelected = viewMode == ViewMode.CLASS_MENU
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.setViewMode(ViewMode.CLASS_MENU) }
                            .testTag("tab_class_menu"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isMenuSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (isMenuSelected) 2.dp else 0.dp,
                        border = if (isMenuSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Class,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isMenuSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Classes",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isMenuSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isMenuSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Tab 2: All Students
                    val isAllSelected = viewMode == ViewMode.ALL_STUDENTS
                    Surface(
                        modifier = Modifier
                            .weight(1.15f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.setViewMode(ViewMode.ALL_STUDENTS) }
                            .testTag("tab_all_students"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isAllSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (isAllSelected) 2.dp else 0.dp,
                        border = if (isAllSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isAllSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Students (${overallMetrics.totalStudents})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isAllSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Tab 3: History (Enrollments, Payments, Class Leaving)
                    val isHistorySelected = viewMode == ViewMode.PAYMENT_HISTORY
                    Surface(
                        modifier = Modifier
                            .weight(1.1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.setViewMode(ViewMode.PAYMENT_HISTORY) }
                            .testTag("tab_payment_history"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isHistorySelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (isHistorySelected) 2.dp else 0.dp,
                        border = if (isHistorySelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isHistorySelected) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "History (${historyOverviewMetrics.totalEvents})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isHistorySelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isHistorySelected) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            when (viewMode) {
                ViewMode.CLASS_MENU -> {
                    // Multi-page step transition: Grade -> Subject -> Medium -> Class Students
                    AnimatedContent(
                        targetState = menuStep,
                        transitionSpec = {
                            if (targetState.ordinal > initialState.ordinal) {
                                slideInHorizontally { width -> width } + fadeIn() togetherWith
                                        slideOutHorizontally { width -> -width } + fadeOut()
                            } else {
                                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                        slideOutHorizontally { width -> width } + fadeOut()
                            }
                        },
                        label = "menu_step_transition"
                    ) { step ->
                        when (step) {
                            MenuStep.SELECT_GRADE -> {
                                // PAGE 1: Big Buttons for Each Grade (6 to 11)
                                GradeSelectionPage(
                                    gradeCounts = gradeCounts,
                                    totalStudents = overallMetrics.totalStudents,
                                    onGradeClick = { grade ->
                                        viewModel.onGradeSelected(grade)
                                    }
                                )
                            }
                            MenuStep.SELECT_SUBJECT -> {
                                // PAGE 2: Big Buttons for Subjects (Maths, Science, Commerce, English)
                                SubjectSelectionPage(
                                    grade = selectedGrade,
                                    subjectCounts = subjectCounts,
                                    onSubjectClick = { subject ->
                                        viewModel.onSubjectSelected(subject)
                                    },
                                    onBackClick = { viewModel.navigateBackFromStep() }
                                )
                            }
                            MenuStep.SELECT_MEDIUM -> {
                                // PAGE 3: Big Buttons for Medium (Sinhala, English)
                                MediumSelectionPage(
                                    grade = selectedGrade,
                                    subject = selectedSubject,
                                    mediumCounts = mediumCounts,
                                    onMediumClick = { medium ->
                                        viewModel.onMediumSelected(medium)
                                    },
                                    onBackClick = { viewModel.navigateBackFromStep() }
                                )
                            }
                            MenuStep.STUDENT_ROSTER -> {
                                // PAGE 4: Class Students List Screen
                                ClassStudentRosterPage(
                                    grade = selectedGrade,
                                    subject = selectedSubject,
                                    medium = selectedMedium,
                                    monthKey = selectedMonth,
                                    metrics = classMetrics,
                                    studentItems = classUiItems,
                                    searchQuery = searchQuery,
                                    statusFilter = statusFilter,
                                    onSearchChange = { viewModel.setSearchQuery(it) },
                                    onStatusFilterChange = { viewModel.setStatusFilter(it) },
                                    onRecordPayment = { viewModel.openRecordPayment(it) },
                                    onStudentClick = { viewModel.openStudentDetail(it.student) },
                                    onEditStudent = { viewModel.openEditStudent(it.student) },
                                    onDeleteStudent = { viewModel.deleteStudent(it.student.id) },
                                    onAddStudent = { viewModel.openAddStudent() },
                                    onBackClick = { viewModel.navigateBackFromStep() }
                                )
                            }
                        }
                    }
                }
                ViewMode.ALL_STUDENTS -> {
                    // ALL STUDENTS DIRECTORY VIEW
                    AllStudentsDirectoryPage(
                        overallMetrics = overallMetrics,
                        allStudentsUiItems = allStudentsUiItems,
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onRecordPayment = { viewModel.openRecordPayment(it) },
                        onStudentClick = { viewModel.openStudentDetail(it.student) },
                        onEditStudent = { viewModel.openEditStudent(it.student) },
                        onDeleteStudent = { viewModel.deleteStudent(it.student.id) }
                    )
                }
                ViewMode.PAYMENT_HISTORY -> {
                    // DEDICATED HISTORY SECTION (Enrollments, Payments, Class Leaving)
                    PaymentHistorySection(
                        historyEvents = historyEventsFeed,
                        historyOverviewMetrics = historyOverviewMetrics,
                        selectedEventTypeFilter = historyEventTypeFilter,
                        transactions = allPaymentTransactions,
                        statements = historyStatements,
                        metrics = historyMetrics,
                        subTab = historySubTab,
                        selectedMonthFilter = historyMonthFilter,
                        selectedGradeFilter = historyGradeFilter,
                        selectedStatusFilter = historyStatusFilter,
                        searchQuery = historySearchQuery,
                        monthOptions = monthOptions,
                        onSelectEventTypeFilter = { viewModel.setHistoryEventTypeFilter(it) },
                        onSelectSubTab = { viewModel.setHistorySubTab(it) },
                        onSelectMonthFilter = { viewModel.setHistoryMonthFilter(it) },
                        onSelectGradeFilter = { viewModel.setHistoryGradeFilter(it) },
                        onSelectStatusFilter = { viewModel.setHistoryStatusFilter(it) },
                        onSearchQueryChange = { viewModel.setHistorySearchQuery(it) },
                        onOpenReceipt = { viewModel.openReceipt(it) },
                        onOpenReceiptFromHistory = { viewModel.openReceiptFromHistory(it) },
                        onOpenReceiptForStudent = { viewModel.openReceiptForStudent(it) },
                        onRecordPaymentForStudent = { viewModel.openRecordPayment(it) },
                        onOpenQuickRecordPayment = { viewModel.openQuickPaymentPicker() },
                        onOpenRecordClassLeaving = { viewModel.openRecordClassLeaving() },
                        onOpenAddStudent = { viewModel.openAddStudent() },
                        onDeleteHistoryEvent = { viewModel.deleteHistoryEvent(it) },
                        onDeleteHistoryEvents = { viewModel.deleteHistoryEvents(it) },
                        onDeleteAllHistoryEvents = { viewModel.deleteAllHistoryEvents() }
                    )
                }
            }
        }
    }

    // ==========================================
    // DIALOGS & BOTTOM SHEETS
    // ==========================================

    // 1. Add / Edit Student Dialog
    if (isAddStudentOpen) {
        AddStudentDialog(
            initialStudent = editingStudent,
            defaultGrade = selectedGrade,
            defaultSubject = selectedSubject,
            defaultMedium = selectedMedium,
            onDismiss = { viewModel.closeAddStudent() },
            onSave = { name, phone, grade, medium, subjects, subjectMediums, notes ->
                viewModel.saveStudent(
                    name = name,
                    phone = phone,
                    grade = grade,
                    medium = medium,
                    subjects = subjects,
                    subjectMediums = subjectMediums,
                    notes = notes
                )
            }
        )
    }

    // 2. Record Payment Dialog
    if (paymentTarget != null) {
        RecordPaymentDialog(
            item = paymentTarget!!,
            monthKey = selectedMonth,
            onDismiss = { viewModel.closeRecordPayment() },
            onSubmitPayment = { amount, note ->
                viewModel.submitPayment(amount, note)
            }
        )
    }

    // 3. Student Details BottomSheet
    if (detailTarget != null) {
        StudentDetailBottomSheet(
            student = detailTarget!!,
            paymentsList = allPayments,
            onDismiss = { viewModel.closeStudentDetail() },
            onEditStudent = {
                val studentToEdit = detailTarget!!
                viewModel.closeStudentDetail()
                viewModel.openEditStudent(studentToEdit)
            },
            onDeleteStudent = {
                val idToDelete = detailTarget!!.id
                viewModel.closeStudentDetail()
                viewModel.deleteStudent(idToDelete)
            },
            onRecordClassLeaving = {
                val student = detailTarget!!
                viewModel.closeStudentDetail()
                viewModel.openRecordClassLeaving(student)
            },
            onViewReceipt = { p ->
                val targetStudent = detailTarget!!
                val lastRecord = p.history.lastOrNull()
                val tx = com.example.ui.viewmodel.PaymentTransaction(
                    receiptId = "REC-${p.monthKey.replace("-", "")}-${targetStudent.id.takeLast(4).uppercase()}",
                    paymentId = p.id,
                    studentId = targetStudent.id,
                    studentName = targetStudent.name,
                    studentGrade = targetStudent.grade,
                    studentMedium = targetStudent.medium,
                    studentPhone = targetStudent.phone,
                    monthKey = p.monthKey,
                    amount = lastRecord?.amount ?: p.amountPaid,
                    timestamp = lastRecord?.timestamp ?: p.lastUpdated,
                    note = lastRecord?.note ?: "Tuition payment receipt",
                    totalFeeDue = p.totalFeeDue,
                    totalPaidSoFar = p.amountPaid,
                    remainingBalance = p.remainingBalance,
                    status = p.status,
                    enrolledSubjects = targetStudent.subjects
                )
                viewModel.openReceipt(tx)
            }
        )
    }

    // 4. Settings Dialog
    if (isSettingsOpen) {
        SettingsDialog(
            userSession = userSession,
            isCloudConnected = isCloudConnected,
            isBiometricSupported = isBiometricSupported,
            isBiometricEnabled = isBiometricEnabled,
            remainingPasswordTimeDesc = remainingPasswordTimeDesc,
            onToggleBiometric = { viewModel.setBiometricEnabled(it) },
            onRequirePasswordNow = { viewModel.forceRequirePassword() },
            onResetBiometric = { viewModel.resetBiometric() },
            onChangePassword = { oldPass, newPass, callback ->
                viewModel.changePassword(oldPass, newPass, callback)
            },
            onResetPassword = { email, callback ->
                viewModel.resetPassword(email, callback)
            },
            onVerifyFirestoreStructure = { callback ->
                viewModel.verifyFirestoreStructure(callback)
            },
            onDismiss = { viewModel.closeSettings() },
            onLogout = {
                viewModel.closeSettings()
                viewModel.logout()
            }
        )
    }

    // 5. Digital Payment Receipt Dialog
    if (receiptTarget != null) {
        PaymentReceiptDialog(
            transaction = receiptTarget!!,
            adminName = userSession?.displayName ?: "Sithumini (Admin)",
            onDismiss = { viewModel.closeReceipt() }
        )
    }

    // 6. Quick Record Payment Student Picker
    if (isQuickPaymentPickerOpen) {
        QuickRecordPaymentDialog(
            students = allStudentsUiItems,
            onSelectStudent = { item ->
                viewModel.closeQuickPaymentPicker()
                viewModel.openRecordPayment(item)
            },
            onDismiss = { viewModel.closeQuickPaymentPicker() }
        )
    }

    // 7. Record Class Leaving Dialog
    if (isRecordClassLeavingOpen) {
        RecordClassLeavingDialog(
            initialStudent = classLeavingTargetStudent,
            allStudents = allStudentsUiItems.map { it.student },
            onDismiss = { viewModel.closeRecordClassLeaving() },
            onSubmit = { studentId, subjectLeft, reason, note ->
                viewModel.submitClassLeaving(studentId, subjectLeft, reason, note)
            }
        )
    }
}

// =========================================================================
// PAGE 1: GRADE SELECTION SCREEN (Big prominent buttons for each grade 6-11)
// =========================================================================

@Composable
private fun GradeSelectionPage(
    gradeCounts: Map<Int, Int>,
    totalStudents: Int,
    onGradeClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Institute Welcome & Overview Hero Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E3A8A),
                                    Color(0xFF2563EB)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "ACADEMIC PORTAL 2026",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = StatusPaidGreen.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(StatusPaidGreen)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "System Ready",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFFA7F3D0)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Siyathra Education",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Select a grade level below to manage attendance, batches & monthly fee ledgers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.82f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Grades",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "6 to 11",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Enrolled",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "$totalStudents Students",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Batches",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "Sinhala/Eng",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)) {
                Text(
                    text = "Select Grade Level",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Choose from Grade 6 to 11 to view its subjects & class roster",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(Student.ALL_GRADES) { gradeNum ->
            val count = gradeCounts[gradeNum] ?: 0
            BigGradeButton(
                gradeNum = gradeNum,
                studentCount = count,
                onClick = { onGradeClick(gradeNum) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun BigGradeButton(
    gradeNum: Int,
    studentCount: Int,
    onClick: () -> Unit
) {
    val gradeGradient = when (gradeNum) {
        6 -> listOf(Color(0xFF1E88E5), Color(0xFF1565C0))
        7 -> listOf(Color(0xFF00897B), Color(0xFF00695C))
        8 -> listOf(Color(0xFF7B1FA2), Color(0xFF6A1B9A))
        9 -> listOf(Color(0xFFE65100), Color(0xFFBF360C))
        10 -> listOf(Color(0xFF0288D1), Color(0xFF01579B))
        11 -> listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))
        else -> listOf(PrimaryBlue, PrimaryBlue)
    }

    val subjectsDesc = if (Student.isCommerceAvailableForGrade(gradeNum)) {
        "Maths • Science • Commerce • English"
    } else {
        "Maths • Science • English"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("btn_grade_$gradeNum"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Grade Number Gradient Badge
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(gradeGradient)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "G$gradeNum",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Grade $gradeNum",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subjectsDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryBlue.copy(alpha = 0.08f)
                        ) {
                            Text(
                                text = "$studentCount Students Enrolled",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = PrimaryBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Arrow forward button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open Grade $gradeNum",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// =========================================================================
// PAGE 2: SUBJECT SELECTION SCREEN (Big prominent buttons for Subjects)
// =========================================================================

@Composable
private fun SubjectSelectionPage(
    grade: Int,
    subjectCounts: Map<String, Int>,
    onSubjectClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 6.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryBlue.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "GRADE $grade CURRICULUM",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryBlue,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Select a Subject",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Choose the subject class you want to view for Grade $grade",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Subject 1: Mathematics
        item {
            BigSubjectButton(
                title = "Mathematics",
                subtitle = "Rs. 2,500 / mo • Sinhala & English Medium batches",
                icon = Icons.Default.Functions,
                accentColor = PrimaryBlue,
                studentCount = subjectCounts[Student.SUBJECT_MATHS] ?: 0,
                testTag = "btn_subject_maths",
                onClick = { onSubjectClick(Student.SUBJECT_MATHS) }
            )
        }

        // Subject 2: Science
        item {
            BigSubjectButton(
                title = "Science",
                subtitle = "Rs. 2,500 / mo • Sinhala & English Medium batches",
                icon = Icons.Default.Science,
                accentColor = SecondaryTeal,
                studentCount = subjectCounts[Student.SUBJECT_SCIENCE] ?: 0,
                testTag = "btn_subject_science",
                onClick = { onSubjectClick(Student.SUBJECT_SCIENCE) }
            )
        }

        // Subject 3: Commerce (Only for Grade 10 & 11)
        if (Student.isCommerceAvailableForGrade(grade)) {
            item {
                BigSubjectButton(
                    title = "Commerce (වාණිජ්‍යය)",
                    subtitle = "Rs. 2,500 / mo • Sinhala & English Medium batches",
                    icon = Icons.Default.TrendingUp,
                    accentColor = Color(0xFFE65100),
                    studentCount = subjectCounts[Student.SUBJECT_COMMERCE] ?: 0,
                    testTag = "btn_subject_commerce",
                    onClick = { onSubjectClick(Student.SUBJECT_COMMERCE) }
                )
            }
        }

        // Subject 4: English (Direct roster, fee Rs. 1200)
        item {
            BigSubjectButton(
                title = "English Language",
                subtitle = "Rs. 1,200 / mo • Standard Class • Direct class roster",
                icon = Icons.Default.MenuBook,
                accentColor = Color(0xFF673AB7),
                studentCount = subjectCounts[Student.SUBJECT_ENGLISH] ?: 0,
                testTag = "btn_subject_english",
                onClick = { onSubjectClick(Student.SUBJECT_ENGLISH) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun BigSubjectButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    studentCount: Int,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "$studentCount Students Enrolled",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open $title",
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// =========================================================================
// PAGE 3: MEDIUM SELECTION SCREEN (Big prominent buttons for Mediums)
// =========================================================================

@Composable
private fun MediumSelectionPage(
    grade: Int,
    subject: String,
    mediumCounts: Map<String, Int>,
    onMediumClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val subjectColor = if (subject == Student.SUBJECT_MATHS) PrimaryBlue else SecondaryTeal

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 6.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = subjectColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "GRADE $grade • ${subject.uppercase()}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = subjectColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Select Medium",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Choose language medium for Grade $grade $subject",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Medium 1: Sinhala Medium
        item {
            BigMediumButton(
                title = "Sinhala Medium",
                nativeTitle = "සිංහල මාධ්‍යය",
                flagEmoji = "🇱🇰",
                studentCount = mediumCounts[Student.MEDIUM_SINHALA] ?: 0,
                testTag = "btn_medium_sinhala",
                onClick = { onMediumClick(Student.MEDIUM_SINHALA) }
            )
        }

        // Medium 2: English Medium
        item {
            BigMediumButton(
                title = "English Medium",
                nativeTitle = "English Language Instruction",
                flagEmoji = "🌐",
                studentCount = mediumCounts[Student.MEDIUM_ENGLISH] ?: 0,
                testTag = "btn_medium_english",
                onClick = { onMediumClick(Student.MEDIUM_ENGLISH) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun BigMediumButton(
    title: String,
    nativeTitle: String,
    flagEmoji: String,
    studentCount: Int,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PrimaryBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = flagEmoji, fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = nativeTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryBlue.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = "$studentCount Students Participating",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open $title",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// =========================================================================
// PAGE 4: CLASS STUDENT ROSTER SCREEN (Categorized student list)
// =========================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ClassStudentRosterPage(
    grade: Int,
    subject: String,
    medium: String,
    monthKey: String,
    metrics: com.example.ui.viewmodel.DashboardMetrics,
    studentItems: List<com.example.ui.viewmodel.StudentPaymentUiItem>,
    searchQuery: String,
    statusFilter: PaymentStatus?,
    onSearchChange: (String) -> Unit,
    onStatusFilterChange: (PaymentStatus?) -> Unit,
    onRecordPayment: (com.example.ui.viewmodel.StudentPaymentUiItem) -> Unit,
    onStudentClick: (com.example.ui.viewmodel.StudentPaymentUiItem) -> Unit,
    onEditStudent: (com.example.ui.viewmodel.StudentPaymentUiItem) -> Unit,
    onDeleteStudent: (com.example.ui.viewmodel.StudentPaymentUiItem) -> Unit,
    onAddStudent: () -> Unit,
    onBackClick: () -> Unit
) {
    val mediumLabel = if (subject == Student.SUBJECT_ENGLISH) "" else "($medium Medium)"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // 1. Class Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Grade $grade • $subject $mediumLabel",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Month: ${MonthlyPayment.getMonthDisplayName(monthKey)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PrimaryBlue.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${metrics.totalStudents} Students",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ClassStatItem(
                            label = "Paid Full",
                            value = "${metrics.paidCount}",
                            color = StatusPaidGreen,
                            modifier = Modifier.weight(1f)
                        )
                        ClassStatItem(
                            label = "Unpaid / Due",
                            value = "${metrics.unpaidCount}",
                            color = if (metrics.unpaidCount > 0) StatusUnpaidRed else StatusPaidGreen,
                            modifier = Modifier.weight(1f)
                        )
                        ClassStatItem(
                            label = "Collected",
                            value = Student.formatCurrency(metrics.totalCollected),
                            color = PrimaryBlue,
                            modifier = Modifier.weight(1.3f)
                        )
                    }
                }
            }
        }

        // 2. Search & Filter Bar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search student in this class...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_student_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = statusFilter == null,
                        onClick = { onStatusFilterChange(null) },
                        label = { Text("All Students (${metrics.totalStudents})") },
                        modifier = Modifier.testTag("filter_all")
                    )
                    FilterChip(
                        selected = statusFilter == PaymentStatus.UNPAID,
                        onClick = {
                            onStatusFilterChange(
                                if (statusFilter == PaymentStatus.UNPAID) null else PaymentStatus.UNPAID
                            )
                        },
                        label = { Text("Unpaid (${metrics.unpaidCount})") },
                        modifier = Modifier.testTag("filter_unpaid")
                    )
                    FilterChip(
                        selected = statusFilter == PaymentStatus.PAID,
                        onClick = {
                            onStatusFilterChange(
                                if (statusFilter == PaymentStatus.PAID) null else PaymentStatus.PAID
                            )
                        },
                        label = { Text("Paid (${metrics.paidCount})") },
                        modifier = Modifier.testTag("filter_paid")
                    )
                }
            }
        }

        // 3. Students List
        if (studentItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Class,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = PrimaryBlue.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No students in Grade $grade $subject $mediumLabel yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap below to enroll students directly into this class.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onAddStudent,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Enroll Student to this Class")
                        }
                    }
                }
            }
        } else {
            items(studentItems, key = { it.student.id }) { item ->
                StudentCard(
                    item = item,
                    onRecordPaymentClick = { onRecordPayment(item) },
                    onStudentClick = { onStudentClick(item) },
                    onEditClick = { onEditStudent(item) },
                    onDeleteClick = { onDeleteStudent(item) },
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// =========================================================================
// ALL STUDENTS DIRECTORY PAGE (Global list & stats)
// =========================================================================

@Composable
private fun AllStudentsDirectoryPage(
    overallMetrics: com.example.ui.viewmodel.DashboardMetrics,
    allStudentsUiItems: List<com.example.ui.viewmodel.StudentPaymentUiItem>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onRecordPayment: (com.example.ui.viewmodel.StudentPaymentUiItem) -> Unit,
    onStudentClick: (com.example.ui.viewmodel.StudentPaymentUiItem) -> Unit,
    onEditStudent: (com.example.ui.viewmodel.StudentPaymentUiItem) -> Unit,
    onDeleteStudent: (com.example.ui.viewmodel.StudentPaymentUiItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Total Students",
                        value = "${overallMetrics.totalStudents}",
                        icon = Icons.Default.Group,
                        iconBgColor = PrimaryBlue.copy(alpha = 0.12f),
                        iconTint = PrimaryBlue,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_total_students"
                    )
                    MetricCard(
                        title = "Collected",
                        value = Student.formatCurrency(overallMetrics.totalCollected),
                        icon = Icons.Default.Payments,
                        iconBgColor = StatusPaidGreen.copy(alpha = 0.15f),
                        iconTint = StatusPaidGreen,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_collected_fees"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Total Fees",
                        value = Student.formatCurrency(overallMetrics.totalFeesDue),
                        icon = Icons.Default.MonetizationOn,
                        iconBgColor = SecondaryTeal.copy(alpha = 0.15f),
                        iconTint = SecondaryTeal,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_total_fees"
                    )
                    MetricCard(
                        title = "Pending Due",
                        value = Student.formatCurrency(overallMetrics.totalPending),
                        icon = Icons.Default.PendingActions,
                        iconBgColor = StatusUnpaidRed.copy(alpha = 0.15f),
                        iconTint = StatusUnpaidRed,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_pending_fees"
                    )
                }
            }
        }

        // Global search
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search by student name, grade or phone...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_all_input"),
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Registered Students (${allStudentsUiItems.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (allStudentsUiItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No students found.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(allStudentsUiItems, key = { it.student.id }) { item ->
                StudentCard(
                    item = item,
                    onRecordPaymentClick = { onRecordPayment(item) },
                    onStudentClick = { onStudentClick(item) },
                    onEditClick = { onEditStudent(item) },
                    onDeleteClick = { onDeleteStudent(item) },
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ClassStatItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}
