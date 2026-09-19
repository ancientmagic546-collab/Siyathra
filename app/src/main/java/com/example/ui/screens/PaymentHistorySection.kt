package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HistoryEventType
import com.example.data.model.MonthlyPayment
import com.example.data.model.PaymentStatus
import com.example.data.model.Student
import com.example.data.model.TuitionHistoryItem
import com.example.ui.components.MetricCard
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.StatusPaidContainer
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.StatusUnpaidRed
import com.example.ui.viewmodel.DashboardMetrics
import com.example.ui.viewmodel.HistoryOverviewMetrics
import com.example.ui.viewmodel.HistorySubTab
import com.example.ui.viewmodel.PaymentTransaction
import com.example.ui.viewmodel.StudentPaymentUiItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistorySection(
    historyEvents: List<TuitionHistoryItem>,
    historyOverviewMetrics: HistoryOverviewMetrics,
    selectedEventTypeFilter: HistoryEventType?,
    transactions: List<PaymentTransaction>,
    statements: List<StudentPaymentUiItem>,
    metrics: DashboardMetrics,
    subTab: HistorySubTab,
    selectedMonthFilter: String?,
    selectedGradeFilter: Int?,
    selectedStatusFilter: PaymentStatus?,
    searchQuery: String,
    monthOptions: List<String>,
    onSelectEventTypeFilter: (HistoryEventType?) -> Unit,
    onSelectSubTab: (HistorySubTab) -> Unit,
    onSelectMonthFilter: (String?) -> Unit,
    onSelectGradeFilter: (Int?) -> Unit,
    onSelectStatusFilter: (PaymentStatus?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onOpenReceipt: (PaymentTransaction) -> Unit,
    onOpenReceiptFromHistory: (TuitionHistoryItem) -> Unit,
    onOpenReceiptForStudent: (StudentPaymentUiItem) -> Unit,
    onRecordPaymentForStudent: (StudentPaymentUiItem) -> Unit,
    onOpenQuickRecordPayment: () -> Unit,
    onOpenRecordClassLeaving: () -> Unit,
    onOpenAddStudent: () -> Unit
) {
    var showMonthMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("payment_history_section"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Section Title & Quick Actions
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PrimaryBlue.copy(alpha = 0.12f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Institute History",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enrollments, fee payments & class departures from latest to oldest",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Record Class Leaving Button
                    OutlinedButton(
                        onClick = onOpenRecordClassLeaving,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE65100)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_history_record_leaving")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonRemove,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Log Leaving", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Record Payment Button
                    Button(
                        onClick = onOpenQuickRecordPayment,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusPaidGreen),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_history_quick_record_payment")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Record Fee", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Enroll Student Button
                    Button(
                        onClick = onOpenAddStudent,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_history_enroll_student")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Enroll", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // 2. 4 KPI Summary Cards for History
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Total Events",
                        value = "${historyOverviewMetrics.totalEvents}",
                        subtitle = "Lifecycle activities logged",
                        icon = Icons.Default.History,
                        iconBgColor = PrimaryBlue.copy(alpha = 0.12f),
                        iconTint = PrimaryBlue,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_history_total_events"
                    )
                    MetricCard(
                        title = "Enrollments",
                        value = "${historyOverviewMetrics.totalEnrollments}",
                        subtitle = "Students registered",
                        icon = Icons.Default.PersonAdd,
                        iconBgColor = StatusPaidGreen.copy(alpha = 0.15f),
                        iconTint = StatusPaidGreen,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_history_enrollments"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Fee Payments",
                        value = "${historyOverviewMetrics.totalPayments}",
                        subtitle = Student.formatCurrency(historyOverviewMetrics.totalFeesCollected),
                        icon = Icons.Default.ReceiptLong,
                        iconBgColor = SecondaryTeal.copy(alpha = 0.15f),
                        iconTint = SecondaryTeal,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_history_payments"
                    )
                    MetricCard(
                        title = "Class Leaving",
                        value = "${historyOverviewMetrics.totalClassLeaving}",
                        subtitle = "Departures & withdrawals",
                        icon = Icons.Default.PersonRemove,
                        iconBgColor = Color(0xFFFFF3E0),
                        iconTint = Color(0xFFE65100),
                        modifier = Modifier.weight(1f),
                        testTag = "metric_history_leaving"
                    )
                }
            }
        }

        // 3. Sub-Tab Switcher: Unified History Timeline vs Monthly Fee Statements
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Timeline Feed Tab
                    val isTimeline = subTab == HistorySubTab.TRANSACTIONS
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectSubTab(HistorySubTab.TRANSACTIONS) }
                            .testTag("subtab_timeline"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isTimeline) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (isTimeline) 2.dp else 0.dp,
                        border = if (isTimeline) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)) else null
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
                                tint = if (isTimeline) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Timeline Feed (${historyEvents.size})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isTimeline) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isTimeline) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Monthly Fee Statements Tab
                    val isStatements = subTab == HistorySubTab.STATEMENTS
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectSubTab(HistorySubTab.STATEMENTS) }
                            .testTag("subtab_statements"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isStatements) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (isStatements) 2.dp else 0.dp,
                        border = if (isStatements) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isStatements) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Fee Statements",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isStatements) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isStatements) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 4. Search & Multi-Criteria Filters
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_history_search"),
                        placeholder = {
                            Text(
                                if (subTab == HistorySubTab.TRANSACTIONS)
                                    "Search by student, receipt, reason, class..."
                                else
                                    "Search by student name or phone..."
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryBlue)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // If Timeline Feed: Event Type Filter Chips (All, Enrollments, Payments, Class Leaving)
                    if (subTab == HistorySubTab.TRANSACTIONS) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Filter Event Type:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    FilterChip(
                                        selected = selectedEventTypeFilter == null,
                                        onClick = { onSelectEventTypeFilter(null) },
                                        label = { Text("All Activities (${historyOverviewMetrics.totalEvents})") },
                                        leadingIcon = {
                                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.12f),
                                            selectedLabelColor = PrimaryBlue
                                        )
                                    )
                                }

                                item {
                                    FilterChip(
                                        selected = selectedEventTypeFilter == HistoryEventType.ENROLLMENT,
                                        onClick = { onSelectEventTypeFilter(HistoryEventType.ENROLLMENT) },
                                        label = { Text("Enrollments (${historyOverviewMetrics.totalEnrollments})") },
                                        leadingIcon = {
                                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = StatusPaidGreen.copy(alpha = 0.15f),
                                            selectedLabelColor = StatusPaidGreen
                                        )
                                    )
                                }

                                item {
                                    FilterChip(
                                        selected = selectedEventTypeFilter == HistoryEventType.PAYMENT,
                                        onClick = { onSelectEventTypeFilter(HistoryEventType.PAYMENT) },
                                        label = { Text("Payments (${historyOverviewMetrics.totalPayments})") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SecondaryTeal.copy(alpha = 0.15f),
                                            selectedLabelColor = SecondaryTeal
                                        )
                                    )
                                }

                                item {
                                    FilterChip(
                                        selected = selectedEventTypeFilter == HistoryEventType.CLASS_LEAVING,
                                        onClick = { onSelectEventTypeFilter(HistoryEventType.CLASS_LEAVING) },
                                        label = { Text("Class Leaving (${historyOverviewMetrics.totalClassLeaving})") },
                                        leadingIcon = {
                                            Icon(Icons.Default.PersonRemove, contentDescription = null, modifier = Modifier.size(16.dp))
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFFFE0B2),
                                            selectedLabelColor = Color(0xFFE65100)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Grade Filter Chips (All, 6, 7, 8, 9, 10, 11)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Filter by Grade:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedGradeFilter == null,
                                    onClick = { onSelectGradeFilter(null) },
                                    label = { Text("All Grades") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryBlue.copy(alpha = 0.12f),
                                        selectedLabelColor = PrimaryBlue
                                    )
                                )
                            }
                            items(Student.ALL_GRADES) { grade ->
                                FilterChip(
                                    selected = selectedGradeFilter == grade,
                                    onClick = { onSelectGradeFilter(grade) },
                                    label = { Text("Grade $grade") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryBlue.copy(alpha = 0.12f),
                                        selectedLabelColor = PrimaryBlue
                                    )
                                )
                            }
                        }
                    }

                    // Month Filter Pill (if Statements or filtered view)
                    if (subTab == HistorySubTab.STATEMENTS) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Statement Month:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Box {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = PrimaryBlue.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f)),
                                    modifier = Modifier.clickable { showMonthMenu = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = selectedMonthFilter?.let { MonthlyPayment.getMonthDisplayName(it) } ?: "Current Month",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryBlue
                                            )
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showMonthMenu,
                                    onDismissRequest = { showMonthMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Current Month (Default)") },
                                        onClick = {
                                            onSelectMonthFilter(null)
                                            showMonthMenu = false
                                        }
                                    )
                                    monthOptions.forEach { m ->
                                        DropdownMenuItem(
                                            text = { Text(MonthlyPayment.getMonthDisplayName(m)) },
                                            onClick = {
                                                onSelectMonthFilter(m)
                                                showMonthMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // SUB-TAB 1: UNIFIED CHRONOLOGICAL HISTORY FEED (Latest to Oldest)
        // ==========================================
        if (subTab == HistorySubTab.TRANSACTIONS) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "History Feed (Latest to Oldest)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${historyEvents.size} entries",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (historyEvents.isEmpty()) {
                item {
                    HistoryEmptyState(
                        onReset = {
                            onSearchQueryChange("")
                            onSelectEventTypeFilter(null)
                            onSelectGradeFilter(null)
                        }
                    )
                }
            } else {
                items(historyEvents, key = { it.id }) { eventItem ->
                    UnifiedHistoryEventCard(
                        item = eventItem,
                        onOpenReceipt = { onOpenReceiptFromHistory(eventItem) }
                    )
                }
            }
        }

        // ==========================================
        // SUB-TAB 2: MONTHLY FEE STATEMENTS (Per Student)
        // ==========================================
        if (subTab == HistorySubTab.STATEMENTS) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Student Monthly Statements",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${statements.size} students",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (statements.isEmpty()) {
                item {
                    HistoryEmptyState(
                        message = "No student fee statements found matching criteria.",
                        onReset = {
                            onSearchQueryChange("")
                            onSelectGradeFilter(null)
                            onSelectStatusFilter(null)
                        }
                    )
                }
            } else {
                items(statements, key = { it.payment.id }) { item ->
                    StatementStudentCard(
                        item = item,
                        onOpenReceipt = { onOpenReceiptForStudent(item) },
                        onRecordPayment = { onRecordPaymentForStudent(item) }
                    )
                }
            }
        }

        // Bottom space padding
        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

// ==========================================
// UNIFIED HISTORY EVENT CARD
// Supports ENROLLMENT, PAYMENT, CLASS_LEAVING
// ==========================================
@Composable
fun UnifiedHistoryEventCard(
    item: TuitionHistoryItem,
    onOpenReceipt: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_event_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Avatar, Event Badge, Title, Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Event Icon Avatar
                val (bgColor, iconColor, icon) = when (item.type) {
                    HistoryEventType.ENROLLMENT -> Triple(StatusPaidGreen.copy(alpha = 0.15f), StatusPaidGreen, Icons.Default.PersonAdd)
                    HistoryEventType.PAYMENT -> Triple(PrimaryBlue.copy(alpha = 0.12f), PrimaryBlue, Icons.Default.Payments)
                    HistoryEventType.CLASS_LEAVING -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), Icons.Default.PersonRemove)
                }

                Surface(
                    shape = CircleShape,
                    color = bgColor,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Event Type Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = bgColor
                        ) {
                            Text(
                                text = when (item.type) {
                                    HistoryEventType.ENROLLMENT -> "NEW ENROLLMENT"
                                    HistoryEventType.PAYMENT -> "FEE PAYMENT"
                                    HistoryEventType.CLASS_LEAVING -> "CLASS DEPARTURE"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp
                                ),
                                color = iconColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        // Timestamp (Formatted)
                        Text(
                            text = item.formattedDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Title
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Subtitle / Student info
                    Text(
                        text = "${item.studentName} • Grade ${item.studentGrade} (${item.studentMedium})" +
                                if (item.studentPhone.isNotBlank()) " • ${item.studentPhone}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            // Body Details based on event type
            when (item.type) {
                HistoryEventType.ENROLLMENT -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (item.subjects.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Subjects:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(item.subjects) { subj ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = StatusPaidGreen.copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                text = subj,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = StatusPaidGreen,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (item.note.isNotBlank()) {
                            Text(
                                text = "Note: ${item.note}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HistoryEventType.PAYMENT -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = item.amount?.let { Student.formatCurrency(it) } ?: "Paid",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = StatusPaidGreen
                                    )
                                )
                                Text(
                                    text = "Receipt: #${item.receiptId ?: "REC"}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // View Receipt Button
                            OutlinedButton(
                                onClick = onOpenReceipt,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_view_receipt_${item.id}")
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Receipt", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (item.note.isNotBlank()) {
                            Text(
                                text = "Note: ${item.note}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HistoryEventType.CLASS_LEAVING -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFE0B2)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ExitToApp,
                                        contentDescription = null,
                                        tint = Color(0xFFE65100),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Left: ${item.subjectLeft ?: "Class"}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE65100)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            if (!item.departureReason.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "Reason: ${item.departureReason}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (item.note.isNotBlank()) {
                            Text(
                                text = "Coordinator Remarks: ${item.note}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// STATEMENT STUDENT CARD
// ==========================================
@Composable
fun StatementStudentCard(
    item: StudentPaymentUiItem,
    onOpenReceipt: () -> Unit,
    onRecordPayment: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("statement_card_${item.student.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.student.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Grade ${item.student.grade} (${item.student.medium}) • ${item.student.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Badge
                val isPaid = item.payment.status == PaymentStatus.PAID
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPaid) StatusPaidContainer else StatusUnpaidRed.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = item.payment.status.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isPaid) StatusPaidGreen else StatusUnpaidRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Financial Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Student.formatCurrency(item.payment.totalFeeDue), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
                Column {
                    Text("Paid So Far", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Student.formatCurrency(item.payment.amountPaid), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = StatusPaidGreen))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Remaining", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Student.formatCurrency(item.payment.remainingBalance), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = if (item.payment.remainingBalance > 0) StatusUnpaidRed else StatusPaidGreen))
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.payment.amountPaid > 0) {
                    OutlinedButton(
                        onClick = onOpenReceipt,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Receipt", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (item.payment.remainingBalance > 0) {
                    Button(
                        onClick = onRecordPayment,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusPaidGreen),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Record Fee", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// EMPTY STATE COMPONENT
// ==========================================
@Composable
fun HistoryEmptyState(
    message: String = "No history records found for the selected filters.",
    onReset: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = PrimaryBlue.copy(alpha = 0.1f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No History Records Found",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onReset,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Reset Filters")
            }
        }
    }
}
