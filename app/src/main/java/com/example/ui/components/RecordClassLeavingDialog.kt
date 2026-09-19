package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Student
import com.example.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordClassLeavingDialog(
    initialStudent: Student? = null,
    allStudents: List<Student>,
    onDismiss: () -> Unit,
    onSubmit: (studentId: String, subjectLeft: String, reason: String, note: String) -> Unit
) {
    var selectedStudent by remember { mutableStateOf(initialStudent ?: allStudents.firstOrNull()) }
    var selectedSubject by remember {
        mutableStateOf(
            if (selectedStudent != null && selectedStudent!!.subjects.isNotEmpty()) {
                selectedStudent!!.subjects.keys.first()
            } else {
                "All Classes"
            }
        )
    }

    val commonReasons = listOf(
        "Switched Stream / Subject",
        "Time Schedule Conflict",
        "Family Relocation",
        "Course Syllabus Completed",
        "School Timetable Change",
        "Personal / Other"
    )

    var selectedReason by remember { mutableStateOf(commonReasons[0]) }
    var customReason by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showStudentDropdown by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .testTag("record_class_leaving_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF3E0),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.PersonRemove,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Record Class Leaving",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Log student departure or class withdrawal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // 1. Select Student
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Select Student *",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showStudentDropdown = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedStudent != null) {
                                Column {
                                    Text(
                                        text = selectedStudent!!.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Grade ${selectedStudent!!.grade} (${selectedStudent!!.medium}) • ${selectedStudent!!.phone}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Text(
                                    text = "Choose a student...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue)
                        }

                        DropdownMenu(
                            expanded = showStudentDropdown,
                            onDismissRequest = { showStudentDropdown = false }
                        ) {
                            allStudents.forEach { s ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("${s.name} (Grade ${s.grade})", fontWeight = FontWeight.Bold)
                                            Text(
                                                "Classes: ${s.subjects.keys.joinToString(", ")}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedStudent = s
                                        selectedSubject = s.subjects.keys.firstOrNull() ?: "All Classes"
                                        showStudentDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 2. Select Class / Subject Left
                if (selectedStudent != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Leaving Which Class / Subject? *",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val subjectOptions = mutableListOf<String>()
                        selectedStudent!!.subjects.keys.forEach { subjectOptions.add(it) }
                        subjectOptions.add("All Classes (Withdrawal)")

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(subjectOptions) { option ->
                                val isSelected = selectedSubject == option
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedSubject = option },
                                    label = { Text(option) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFFE0B2),
                                        selectedLabelColor = Color(0xFFE65100)
                                    )
                                )
                            }
                        }
                    }
                }

                // 3. Reason for Leaving
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Reason for Leaving *",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(commonReasons) { reason ->
                            val isSelected = selectedReason == reason
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedReason = reason },
                                label = { Text(reason) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                                    selectedLabelColor = PrimaryBlue
                                )
                            )
                        }
                    }

                    if (selectedReason == "Personal / Other") {
                        OutlinedTextField(
                            value = customReason,
                            onValueChange = { customReason = it },
                            label = { Text("Specify Custom Reason") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // 4. Notes & Comments
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Coordinator Notes / Follow-up (Optional)") },
                    placeholder = { Text("e.g. Issued clearance certificate; may return for revision seminars.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val student = selectedStudent ?: return@Button
                            val finalReason = if (selectedReason == "Personal / Other" && customReason.isNotBlank()) {
                                customReason.trim()
                            } else {
                                selectedReason
                            }
                            onSubmit(student.id, selectedSubject, finalReason, notes.trim())
                        },
                        enabled = selectedStudent != null,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("confirm_class_leaving_button")
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log Departure", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
