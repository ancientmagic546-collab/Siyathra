package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentStatus
import com.example.data.model.Student
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.StatusPaidContainer
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.StatusPaidText
import com.example.ui.theme.StatusPartialAmber
import com.example.ui.theme.StatusPartialContainer
import com.example.ui.theme.StatusPartialText
import com.example.ui.theme.StatusUnpaidContainer
import com.example.ui.theme.StatusUnpaidRed
import com.example.ui.theme.StatusUnpaidText
import com.example.ui.theme.SubjectCommerceColor
import com.example.ui.theme.SubjectEnglishColor
import com.example.ui.theme.SubjectMathsColor
import com.example.ui.theme.SubjectScienceColor
import com.example.ui.viewmodel.StudentPaymentUiItem

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentCard(
    item: StudentPaymentUiItem,
    onRecordPaymentClick: () -> Unit,
    onStudentClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val (statusLabel, statusBg, statusText) = when (item.payment.status) {
        PaymentStatus.PAID -> Triple("PAID", StatusPaidContainer, StatusPaidText)
        PaymentStatus.PARTIAL -> Triple("PARTIAL", StatusPartialContainer, StatusPartialText)
        PaymentStatus.UNPAID -> Triple("UNPAID", StatusUnpaidContainer, StatusUnpaidText)
    }

    val gradeGradient = when (item.student.grade) {
        6 -> listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
        7 -> listOf(Color(0xFF0D9488), Color(0xFF0F766E))
        8 -> listOf(Color(0xFF7C3AED), Color(0xFF6D28D9))
        9 -> listOf(Color(0xFFEA580C), Color(0xFFC2410C))
        10 -> listOf(Color(0xFF0284C7), Color(0xFF0369A1))
        11 -> listOf(Color(0xFF16A34A), Color(0xFF15803D))
        else -> listOf(PrimaryBlue, PrimaryBlue)
    }

    val progressColor = when (item.payment.status) {
        PaymentStatus.PAID -> StatusPaidGreen
        PaymentStatus.PARTIAL -> StatusPartialAmber
        PaymentStatus.UNPAID -> StatusUnpaidRed
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onStudentClick() }
            .testTag("student_card_${item.student.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Student Avatar, Name, Grade Badge & Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Initial Avatar Circle with subtle grade gradient
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(gradeGradient)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.student.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.student.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Grade Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        ) {
                            Text(
                                text = item.student.gradeDisplayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (item.student.phone.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Phone",
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.student.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusBg,
                    modifier = Modifier.padding(end = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusText)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = statusText
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("student_menu_button_${item.student.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Student Details") },
                            leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onStudentClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Student") },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Student", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Enrolled Subject & Per-Subject Medium Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (item.student.hasSubject(Student.SUBJECT_MATHS)) {
                    val med = item.student.getMediumForSubject(Student.SUBJECT_MATHS)
                    SubjectChip(
                        name = "📐 Maths ($med)",
                        bgColor = SubjectMathsColor.copy(alpha = 0.08f),
                        textColor = SubjectMathsColor
                    )
                }
                if (item.student.hasSubject(Student.SUBJECT_SCIENCE)) {
                    val med = item.student.getMediumForSubject(Student.SUBJECT_SCIENCE)
                    SubjectChip(
                        name = "🔬 Science ($med)",
                        bgColor = SubjectScienceColor.copy(alpha = 0.09f),
                        textColor = SubjectScienceColor
                    )
                }
                if (item.student.hasSubject(Student.SUBJECT_COMMERCE)) {
                    val med = item.student.getMediumForSubject(Student.SUBJECT_COMMERCE)
                    SubjectChip(
                        name = "📈 Commerce ($med)",
                        bgColor = SubjectCommerceColor.copy(alpha = 0.09f),
                        textColor = SubjectCommerceColor
                    )
                }
                if (item.student.hasSubject(Student.SUBJECT_ENGLISH)) {
                    SubjectChip(
                        name = "📖 English",
                        bgColor = SubjectEnglishColor.copy(alpha = 0.09f),
                        textColor = SubjectEnglishColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Clean Structured Fee Stats breakdown in a modern rounded container
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Monthly Total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.payment.formattedDue,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Paid So Far",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.payment.formattedPaid,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = StatusPaidGreen
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Balance Due",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.payment.formattedRemaining,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.payment.remainingBalance > 0) StatusUnpaidRed else StatusPaidGreen
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sleek progress bar
                    LinearProgressIndicator(
                        progress = { item.payment.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Add Payment Button
            Button(
                onClick = onRecordPaymentClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_payment_button_${item.student.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (item.payment.status == PaymentStatus.PAID)
                        StatusPaidContainer
                    else
                        PrimaryBlue,
                    contentColor = if (item.payment.status == PaymentStatus.PAID)
                        StatusPaidText
                    else
                        Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = if (item.payment.status == PaymentStatus.PAID) 0.dp else 1.dp)
            ) {
                Icon(
                    imageVector = if (item.payment.status == PaymentStatus.PAID) Icons.Default.CheckCircle else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (item.payment.status == PaymentStatus.PAID) "Fees Settled (Record More)" else "Record Fee Payment",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun SubjectChip(name: String, bgColor: Color, textColor: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
