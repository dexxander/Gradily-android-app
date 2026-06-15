package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.GradilyViewModel
import com.example.myapplication.data.Assessment
import com.example.myapplication.data.Student
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*

@Composable
fun ClassStudentContentScreen(
    viewModel: GradilyViewModel,
    onAddStudent: () -> Unit,
    onAssessStudent: (Student) -> Unit,
    onHome: () -> Unit
) {
    val subject = viewModel.currentSubject.collectAsState().value
    val students by viewModel.getStudents().collectAsState(initial = emptyList())

    GradilyBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onHome,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(GlassBg)
                ) {
                    Icon(Icons.Default.Home, "Home", tint = TextPrimary)
                }
                Text(
                    subject?.courseName ?: "Class",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Box(modifier = Modifier.size(48.dp)) // Spacer for alignment
            }

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Text("📊", fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${students.size}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text("Students", color = TextMuted, fontSize = 12.sp)
                }
                GlassCard(modifier = Modifier.weight(1f)) {
                    Text("📖", fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${subject?.creditHours ?: 0}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text("Credit Hours", color = TextMuted, fontSize = 12.sp)
                }
            }

            // Add student button
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onAddStudent() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LightGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, "Add", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Add Student", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Enroll a new student", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }

            // Student list header
            Text(
                "Enrolled Students",
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (students.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👥", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No students enrolled", color = TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text("Tap + to add the first student", color = TextMuted, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(students, key = { it.studentId }) { student ->
                        StudentListItem(student, viewModel, onAssessStudent)
                    }
                }
            }
        }
    }
}

@Composable
fun StudentListItem(
    student: Student,
    viewModel: GradilyViewModel,
    onAssessStudent: (Student) -> Unit
) {
    val assessment by viewModel.getAssessmentByStudentId(student.studentId).collectAsState(initial = null)
    val gpa = viewModel.calculateGPA(assessment)

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar + Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        student.studentName.first().uppercase(),
                        color = AccentGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(student.studentName, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(student.email.ifBlank { "No email" }, color = TextMuted, fontSize = 11.sp)
                }
            }

            // GPA badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (gpa >= 3.0) AccentGreen.copy(alpha = 0.2f)
                        else if (gpa >= 2.0) AccentAmber.copy(alpha = 0.2f)
                        else AccentRed.copy(alpha = 0.2f)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    String.format("%.2f", gpa),
                    color = if (gpa >= 3.0) AccentGreen
                    else if (gpa >= 2.0) AccentAmber
                    else AccentRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Assess button
            Button(
                onClick = {
                    viewModel.setCurrentStudent(student)
                    onAssessStudent(student)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text("Assess", color = AccentBlue, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Delete
            IconButton(
                onClick = { viewModel.deleteStudent(student) },
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AccentRed.copy(alpha = 0.1f))
            ) {
                Icon(Icons.Default.Delete, "Delete", tint = AccentRed, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun StudentEnrollmentScreen(
    viewModel: GradilyViewModel,
    onNavigateBack: () -> Unit,
    onHome: () -> Unit
) {
    var studentName by remember { mutableStateOf("") }
    var studentEmail by remember { mutableStateOf("") }
    val subject = viewModel.currentSubject.collectAsState().value

    GradilyBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.1f))

            Text("🎓", fontSize = 48.sp, modifier = Modifier.padding(bottom = 16.dp))
            SectionHeader("Enroll Student")
            SectionSubtitle("Add to ${subject?.courseName ?: "class"}")

            Spacer(modifier = Modifier.height(16.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Student Name", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                GradilyTextField(
                    value = studentName,
                    onValueChange = { studentName = it },
                    label = "Full name"
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Student Email", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                GradilyTextField(
                    value = studentEmail,
                    onValueChange = { studentEmail = it },
                    label = "student@email.com"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "The student can use this email to create an account and view their grades.",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                GradilyButton(
                    text = "Enroll Student",
                    onClick = {
                        if (studentName.isNotBlank()) {
                            viewModel.createStudent(studentName, studentEmail)
                            onNavigateBack()
                        }
                    },
                    enabled = studentName.isNotBlank()
                )
            }

            Spacer(modifier = Modifier.weight(0.3f))

            TextButton(onClick = onNavigateBack) {
                Text("← Return", color = TextMuted, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun StudentGradeScreen(
    viewModel: GradilyViewModel,
    onNavigateBack: () -> Unit,
    onHome: () -> Unit
) {
    val student = viewModel.currentStudent.collectAsState().value
    val subject = viewModel.currentSubject.collectAsState().value
    val assessment by viewModel.getAssessment().collectAsState(initial = null)

    var quiz1 by remember(assessment) { mutableStateOf(assessment?.quiz1?.toString() ?: "0.0") }
    var assign1 by remember(assessment) { mutableStateOf(assessment?.assign1?.toString() ?: "0.0") }
    var midterm by remember(assessment) { mutableStateOf(assessment?.midterm?.toString() ?: "0.0") }
    var quiz2 by remember(assessment) { mutableStateOf(assessment?.quiz2?.toString() ?: "0.0") }
    var assign2 by remember(assessment) { mutableStateOf(assessment?.assign2?.toString() ?: "0.0") }
    var finalExam by remember(assessment) { mutableStateOf(assessment?.finalExam?.toString() ?: "0.0") }

    GradilyBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(GlassBg)
                ) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(student?.studentName ?: "Student", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("${subject?.courseName ?: "Class"} Assessment", color = TextMuted, fontSize = 13.sp)
                }
            }

            // GPA overview card
            val currentGpa = viewModel.calculateGPA(assessment)
            GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Current GPA", color = TextMuted, fontSize = 13.sp)
                        Text(
                            String.format("%.2f", currentGpa),
                            color = if (currentGpa >= 3.0) AccentGreen
                            else if (currentGpa >= 2.0) AccentAmber
                            else AccentRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                    }
                    Text(
                        when {
                            currentGpa >= 3.5 -> "🌟 Excellent"
                            currentGpa >= 3.0 -> "👍 Good"
                            currentGpa >= 2.0 -> "📝 Average"
                            currentGpa > 0 -> "⚠️ Needs work"
                            else -> "📋 Not graded"
                        },
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            // Grade entries
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text("Assessment Scores", color = TextSecondary, fontWeight = FontWeight.Medium, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
                }
                item { GradeInputCard("Quiz 1", quiz1, "Max: 10") { quiz1 = it } }
                item { GradeInputCard("Assignment 1", assign1, "Max: 25") { assign1 = it } }
                item { GradeInputCard("Midterm", midterm, "Max: 10") { midterm = it } }
                item { GradeInputCard("Quiz 2", quiz2, "Max: 10") { quiz2 = it } }
                item { GradeInputCard("Assignment 2", assign2, "Max: 25") { assign2 = it } }
                item { GradeInputCard("Final Exam", finalExam, "Max: 100") { finalExam = it } }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    GradilyButton(
                        text = "Save Grades",
                        onClick = {
                            val updatedAssessment = assessment?.copy(
                                quiz1 = quiz1.toDoubleOrNull() ?: 0.0,
                                assign1 = assign1.toDoubleOrNull() ?: 0.0,
                                midterm = midterm.toDoubleOrNull() ?: 0.0,
                                quiz2 = quiz2.toDoubleOrNull() ?: 0.0,
                                assign2 = assign2.toDoubleOrNull() ?: 0.0,
                                finalExam = finalExam.toDoubleOrNull() ?: 0.0
                            )
                            if (updatedAssessment != null) {
                                viewModel.updateAssessment(updatedAssessment)
                            }
                            onNavigateBack()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GradeInputCard(label: String, value: String, hint: String, onValueChange: (String) -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 14.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Text(hint, color = TextMuted, fontSize = 11.sp)
            }
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = GlassBgDark,
                    unfocusedContainerColor = GlassBgDark,
                    focusedBorderColor = LightGreen,
                    unfocusedBorderColor = GlassBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = LightGreen
                ),
                textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.width(100.dp).height(52.dp),
                singleLine = true
            )
        }
    }
}
