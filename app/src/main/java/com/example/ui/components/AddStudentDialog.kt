package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Student
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryTeal

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddStudentDialog(
    initialStudent: Student? = null,
    defaultGrade: Int = 10,
    defaultSubject: String? = null,
    defaultMedium: String? = null,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        phone: String,
        grade: Int,
        medium: String,
        subjects: Map<String, Double>,
        subjectMediums: Map<String, String>,
        notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialStudent?.name ?: "") }
    var phone by remember { mutableStateOf(initialStudent?.phone ?: "") }
    var selectedGrade by remember { mutableIntStateOf(initialStudent?.grade ?: defaultGrade) }
    
    // Subjects enrollment state
    var takesMaths by remember {
        mutableStateOf(
            initialStudent?.hasSubject(Student.SUBJECT_MATHS)
                ?: (defaultSubject == null || defaultSubject == Student.SUBJECT_MATHS)
        )
    }
    var mathsMedium by remember {
        mutableStateOf(
            initialStudent?.getMediumForSubject(Student.SUBJECT_MATHS)
                ?: (if (defaultSubject == Student.SUBJECT_MATHS && defaultMedium != null) defaultMedium else Student.MEDIUM_SINHALA)
        )
    }

    var takesScience by remember {
        mutableStateOf(
            initialStudent?.hasSubject(Student.SUBJECT_SCIENCE)
                ?: (defaultSubject == Student.SUBJECT_SCIENCE)
        )
    }
    var scienceMedium by remember {
        mutableStateOf(
            initialStudent?.getMediumForSubject(Student.SUBJECT_SCIENCE)
                ?: (if (defaultSubject == Student.SUBJECT_SCIENCE && defaultMedium != null) defaultMedium else Student.MEDIUM_SINHALA)
        )
    }

    var takesCommerce by remember {
        mutableStateOf(
            initialStudent?.hasSubject(Student.SUBJECT_COMMERCE)
                ?: (defaultSubject == Student.SUBJECT_COMMERCE)
        )
    }
    var commerceMedium by remember {
        mutableStateOf(
            initialStudent?.getMediumForSubject(Student.SUBJECT_COMMERCE)
                ?: (if (defaultSubject == Student.SUBJECT_COMMERCE && defaultMedium != null) defaultMedium else Student.MEDIUM_SINHALA)
        )
    }

    var takesEnglish by remember {
        mutableStateOf(
            initialStudent?.hasSubject(Student.SUBJECT_ENGLISH)
                ?: (defaultSubject == Student.SUBJECT_ENGLISH)
        )
    }

    var notes by remember { mutableStateOf(initialStudent?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isCommerceEligible = Student.isCommerceAvailableForGrade(selectedGrade)

    val mathsFee = Student.DEFAULT_FEE_MATHS
    val scienceFee = Student.DEFAULT_FEE_SCIENCE
    val commerceFee = Student.DEFAULT_FEE_COMMERCE
    val englishFee = Student.DEFAULT_FEE_ENGLISH

    val effectiveTakesCommerce = isCommerceEligible && takesCommerce

    val calculatedTotalFee = (if (takesMaths) mathsFee else 0.0) +
            (if (takesScience) scienceFee else 0.0) +
            (if (effectiveTakesCommerce) commerceFee else 0.0) +
            (if (takesEnglish) englishFee else 0.0)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_student_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (initialStudent == null) "Enroll New Student" else "Edit Student Profile",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Configure student details, grade, and subject mediums",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Student Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Student Full Name *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_student_name"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Phone Number
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone / WhatsApp") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_student_phone"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Grade Selection Section (Grade 6 to 11)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Grade,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Select Grade *",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Student.ALL_GRADES.forEach { gradeNum ->
                        val isSelected = selectedGrade == gradeNum
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedGrade = gradeNum
                            },
                            label = {
                                Text(
                                    text = "Grade $gradeNum",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.testTag("chip_grade_$gradeNum")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 2. Enrolled Subjects & Per-Subject Medium Selection Section
                Text(
                    text = "Select Subjects & Medium *",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isCommerceEligible)
                        "Commerce is available for Grade $selectedGrade (Rs. 2,500/mo). English fee is Rs. 1,200/mo."
                    else
                        "Select medium for Maths & Science. English fee is Rs. 1,200/mo (General medium).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Subject 1: Mathematics Card
                SubjectSelectionCard(
                    title = "Mathematics",
                    subtitle = "Rs. 2,500 / month",
                    icon = Icons.Default.Functions,
                    iconTint = PrimaryBlue,
                    isChecked = takesMaths,
                    onCheckedChange = {
                        takesMaths = it
                        errorMessage = null
                    },
                    checkboxTag = "checkbox_maths",
                    content = {
                        Column(modifier = Modifier.padding(start = 36.dp, top = 4.dp, bottom = 4.dp)) {
                            Text(
                                text = "Mathematics Medium:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = mathsMedium == Student.MEDIUM_SINHALA,
                                    onClick = { mathsMedium = Student.MEDIUM_SINHALA },
                                    label = { Text("🇱🇰 Sinhala Medium") },
                                    modifier = Modifier.testTag("maths_medium_sinhala")
                                )
                                FilterChip(
                                    selected = mathsMedium == Student.MEDIUM_ENGLISH,
                                    onClick = { mathsMedium = Student.MEDIUM_ENGLISH },
                                    label = { Text("🌐 English Medium") },
                                    modifier = Modifier.testTag("maths_medium_english")
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subject 2: Science Card
                SubjectSelectionCard(
                    title = "Science",
                    subtitle = "Rs. 2,500 / month",
                    icon = Icons.Default.Science,
                    iconTint = SecondaryTeal,
                    isChecked = takesScience,
                    onCheckedChange = {
                        takesScience = it
                        errorMessage = null
                    },
                    checkboxTag = "checkbox_science",
                    content = {
                        Column(modifier = Modifier.padding(start = 36.dp, top = 4.dp, bottom = 4.dp)) {
                            Text(
                                text = "Science Medium:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = scienceMedium == Student.MEDIUM_SINHALA,
                                    onClick = { scienceMedium = Student.MEDIUM_SINHALA },
                                    label = { Text("🇱🇰 Sinhala Medium") },
                                    modifier = Modifier.testTag("science_medium_sinhala")
                                )
                                FilterChip(
                                    selected = scienceMedium == Student.MEDIUM_ENGLISH,
                                    onClick = { scienceMedium = Student.MEDIUM_ENGLISH },
                                    label = { Text("🌐 English Medium") },
                                    modifier = Modifier.testTag("science_medium_english")
                                )
                            }
                        }
                    }
                )

                // Subject 3: Commerce Card (Only for Grade 10 and Grade 11)
                AnimatedVisibility(
                    visible = isCommerceEligible,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))
                        SubjectSelectionCard(
                            title = "Commerce (වාණිජ්‍යය)",
                            subtitle = "Rs. 2,500 / month • Grade 10 & 11 only",
                            icon = Icons.Default.TrendingUp,
                            iconTint = Color(0xFFE65100),
                            isChecked = takesCommerce,
                            onCheckedChange = {
                                takesCommerce = it
                                errorMessage = null
                            },
                            checkboxTag = "checkbox_commerce",
                            content = {
                                Column(modifier = Modifier.padding(start = 36.dp, top = 4.dp, bottom = 4.dp)) {
                                    Text(
                                        text = "Commerce Medium:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = commerceMedium == Student.MEDIUM_SINHALA,
                                            onClick = { commerceMedium = Student.MEDIUM_SINHALA },
                                            label = { Text("🇱🇰 Sinhala Medium") },
                                            modifier = Modifier.testTag("commerce_medium_sinhala")
                                        )
                                        FilterChip(
                                            selected = commerceMedium == Student.MEDIUM_ENGLISH,
                                            onClick = { commerceMedium = Student.MEDIUM_ENGLISH },
                                            label = { Text("🌐 English Medium") },
                                            modifier = Modifier.testTag("commerce_medium_english")
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Subject 4: English Subject Card (No separate medium, fee is Rs. 1,200)
                SubjectSelectionCard(
                    title = "English Language",
                    subtitle = "Rs. 1,200 / month (Updated fee)",
                    icon = Icons.Default.MenuBook,
                    iconTint = Color(0xFF673AB7),
                    isChecked = takesEnglish,
                    onCheckedChange = {
                        takesEnglish = it
                        errorMessage = null
                    },
                    checkboxTag = "checkbox_english",
                    content = {
                        Row(
                            modifier = Modifier
                                .padding(start = 36.dp, top = 4.dp, bottom = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF673AB7).copy(alpha = 0.1f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📖 Standard Class • Fee: Rs. 1,200 / month",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF673AB7)
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Live Total Monthly Fee Summary Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryBlue.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Monthly Fee Due",
                                style = MaterialTheme.typography.labelMedium,
                                color = PrimaryBlue
                            )
                            val count = (if (takesMaths) 1 else 0) +
                                    (if (takesScience) 1 else 0) +
                                    (if (effectiveTakesCommerce) 1 else 0) +
                                    (if (takesEnglish) 1 else 0)
                            Text(
                                text = "$count subject${if (count != 1) "s" else ""} selected",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = Student.formatCurrency(calculatedTotalFee),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Student Remarks / School / Batch") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_student_notes"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Please enter student full name."
                                return@Button
                            }
                            if (!takesMaths && !takesScience && !effectiveTakesCommerce && !takesEnglish) {
                                errorMessage = "Please select at least one subject."
                                return@Button
                            }

                            val subjectsMap = mutableMapOf<String, Double>()
                            val mediumsMap = mutableMapOf<String, String>()

                            if (takesMaths) {
                                subjectsMap[Student.SUBJECT_MATHS] = mathsFee
                                mediumsMap[Student.SUBJECT_MATHS] = mathsMedium
                            }
                            if (takesScience) {
                                subjectsMap[Student.SUBJECT_SCIENCE] = scienceFee
                                mediumsMap[Student.SUBJECT_SCIENCE] = scienceMedium
                            }
                            if (effectiveTakesCommerce) {
                                subjectsMap[Student.SUBJECT_COMMERCE] = commerceFee
                                mediumsMap[Student.SUBJECT_COMMERCE] = commerceMedium
                            }
                            if (takesEnglish) {
                                subjectsMap[Student.SUBJECT_ENGLISH] = englishFee
                                mediumsMap[Student.SUBJECT_ENGLISH] = Student.MEDIUM_GENERAL
                            }

                            // Primary medium fallback:
                            val primaryMedium = when {
                                takesMaths -> mathsMedium
                                takesScience -> scienceMedium
                                effectiveTakesCommerce -> commerceMedium
                                else -> Student.MEDIUM_ENGLISH
                            }

                            onSave(
                                name,
                                phone,
                                selectedGrade,
                                primaryMedium,
                                subjectsMap,
                                mediumsMap,
                                notes
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_student_button")
                    ) {
                        Text(if (initialStudent == null) "Enroll Student" else "Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectSelectionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    checkboxTag: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) iconTint.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = if (isChecked) androidx.compose.foundation.BorderStroke(1.5.dp, iconTint.copy(alpha = 0.4f)) else null
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange,
                    modifier = Modifier.testTag(checkboxTag)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = isChecked,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                content()
            }
        }
    }
}
