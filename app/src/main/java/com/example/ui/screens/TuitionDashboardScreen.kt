package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
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
import com.example.ui.components.RecordPaymentDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.StudentCard
import com.example.ui.components.StudentDetailBottomSheet
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.StatusPaidContainer
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.StatusPartialAmber
import com.example.ui.theme.StatusPartialContainer
import com.example.ui.theme.StatusUnpaidContainer
import com.example.ui.theme.StatusUnpaidRed
import com.example.ui.viewmodel.TuitionViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TuitionDashboardScreen(
    viewModel: TuitionViewModel
) {
    val uiItems by viewModel.uiItems.collectAsStateWithLifecycle()
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val subjectFilter by viewModel.subjectFilter.collectAsStateWithLifecycle()
    val mediumFilter by viewModel.mediumFilter.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val userSession by viewModel.currentUser.collectAsStateWithLifecycle()
    val isCloudConnected by viewModel.isCloudConnected.collectAsStateWithLifecycle()

    val isAddStudentOpen by viewModel.isAddStudentOpen.collectAsStateWithLifecycle()
    val editingStudent by viewModel.editingStudent.collectAsStateWithLifecycle()
    val paymentTarget by viewModel.paymentTargetStudent.collectAsStateWithLifecycle()
    val detailTarget by viewModel.detailTargetStudent.collectAsStateWithLifecycle()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsStateWithLifecycle()

    var showMonthDropdown by remember { mutableStateOf(false) }

    val monthOptions = listOf(
        "2026-08",
        "2026-07",
        "2026-06",
        "2026-05",
        "2026-04"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Siyathra",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Cloud status badge
                            Surface(
                                shape = CircleShape,
                                color = if (isCloudConnected) StatusPaidContainer else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
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
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (isCloudConnected) StatusPaidGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Admin: ${userSession?.displayName ?: "Sithumini"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Month Picker Chip Action
                    Box {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryBlue.copy(alpha = 0.1f),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("month_selector_button")
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { showMonthDropdown = true },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Select Month",
                                        tint = PrimaryBlue
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = MonthlyPayment.getMonthDisplayName(selectedMonth),
                                    style = MaterialTheme.typography.labelLarge.copy(
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
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddStudent() },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Add Student", fontWeight = FontWeight.Bold) },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                    .testTag("fab_add_student")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Institutional Financial Overview Cards
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricCard(
                                title = "Students",
                                value = "${metrics.totalStudents}",
                                icon = Icons.Default.Group,
                                iconBgColor = PrimaryBlue.copy(alpha = 0.12f),
                                iconTint = PrimaryBlue,
                                modifier = Modifier.weight(1f),
                                testTag = "metric_total_students"
                            )
                            MetricCard(
                                title = "Collected",
                                value = Student.formatCurrency(metrics.totalCollected),
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
                                value = Student.formatCurrency(metrics.totalFeesDue),
                                icon = Icons.Default.MonetizationOn,
                                iconBgColor = SecondaryTeal.copy(alpha = 0.15f),
                                iconTint = SecondaryTeal,
                                modifier = Modifier.weight(1f),
                                testTag = "metric_total_fees"
                            )
                            MetricCard(
                                title = "Pending",
                                value = Student.formatCurrency(metrics.totalPending),
                                icon = Icons.Default.PendingActions,
                                iconBgColor = StatusUnpaidRed.copy(alpha = 0.15f),
                                iconTint = StatusUnpaidRed,
                                modifier = Modifier.weight(1f),
                                testTag = "metric_pending_fees"
                            )
                        }
                    }
                }

                // Search & Filter Controls
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        // Search TextField
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search by student name or phone...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_student_input"),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Filter Chips Row
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FilterChip(
                                selected = mediumFilter == null,
                                onClick = { viewModel.setMediumFilter(null) },
                                label = { Text("All Mediums") },
                                modifier = Modifier.testTag("filter_medium_all")
                            )
                            FilterChip(
                                selected = mediumFilter == Student.MEDIUM_SINHALA,
                                onClick = {
                                    viewModel.setMediumFilter(
                                        if (mediumFilter == Student.MEDIUM_SINHALA) null else Student.MEDIUM_SINHALA
                                    )
                                },
                                label = { Text("Sinhala Medium") },
                                modifier = Modifier.testTag("filter_medium_sinhala")
                            )
                            FilterChip(
                                selected = mediumFilter == Student.MEDIUM_ENGLISH,
                                onClick = {
                                    viewModel.setMediumFilter(
                                        if (mediumFilter == Student.MEDIUM_ENGLISH) null else Student.MEDIUM_ENGLISH
                                    )
                                },
                                label = { Text("English Medium") },
                                modifier = Modifier.testTag("filter_medium_english")
                            )

                            FilterChip(
                                selected = subjectFilter == null,
                                onClick = { viewModel.setSubjectFilter(null) },
                                label = { Text("All Subjects") },
                                modifier = Modifier.testTag("filter_subject_all")
                            )
                            FilterChip(
                                selected = subjectFilter == "Maths",
                                onClick = { viewModel.setSubjectFilter("Maths") },
                                label = { Text("Maths") },
                                modifier = Modifier.testTag("filter_subject_maths")
                            )
                            FilterChip(
                                selected = subjectFilter == "Science",
                                onClick = { viewModel.setSubjectFilter("Science") },
                                label = { Text("Science") },
                                modifier = Modifier.testTag("filter_subject_science")
                            )

                            FilterChip(
                                selected = statusFilter == PaymentStatus.UNPAID,
                                onClick = {
                                    viewModel.setStatusFilter(
                                        if (statusFilter == PaymentStatus.UNPAID) null else PaymentStatus.UNPAID
                                    )
                                },
                                label = { Text("Unpaid Only") },
                                modifier = Modifier.testTag("filter_status_unpaid")
                            )
                        }
                    }
                }

                // Section Title & Counter
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Student List (${uiItems.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = MonthlyPayment.getMonthDisplayName(selectedMonth),
                            style = MaterialTheme.typography.labelMedium,
                            color = PrimaryBlue
                        )
                    }
                }

                // Student Cards List
                if (uiItems.isEmpty()) {
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
                                    modifier = Modifier.size(60.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No students match your filter.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Tap 'Add Student' to register a new student.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                } else {
                    items(uiItems, key = { it.student.id }) { item ->
                        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                            StudentCard(
                                item = item,
                                onRecordPaymentClick = { viewModel.openRecordPayment(item) },
                                onStudentClick = { viewModel.openStudentDetail(item.student) },
                                onEditClick = { viewModel.openEditStudent(item.student) },
                                onDeleteClick = { viewModel.deleteStudent(item.student.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs & Sheets
    if (isAddStudentOpen) {
        AddStudentDialog(
            initialStudent = editingStudent,
            onDismiss = { viewModel.closeAddStudent() },
            onSave = { name, phone, medium, subjects, notes ->
                viewModel.saveStudent(name, phone, medium, subjects, notes)
            }
        )
    }

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

    if (detailTarget != null) {
        val allPayments by viewModel.allPayments.collectAsStateWithLifecycle()
        StudentDetailBottomSheet(
            student = detailTarget!!,
            paymentsList = allPayments,
            onDismiss = { viewModel.closeStudentDetail() },
            onEditStudent = {
                val std = detailTarget!!
                viewModel.closeStudentDetail()
                viewModel.openEditStudent(std)
            },
            onDeleteStudent = {
                val stdId = detailTarget!!.id
                viewModel.closeStudentDetail()
                viewModel.deleteStudent(stdId)
            }
        )
    }

    if (isSettingsOpen) {
        SettingsDialog(
            userSession = userSession,
            isCloudConnected = isCloudConnected,
            onDismiss = { viewModel.closeSettings() },
            onLogout = {
                viewModel.closeSettings()
                viewModel.logout()
            }
        )
    }
}
