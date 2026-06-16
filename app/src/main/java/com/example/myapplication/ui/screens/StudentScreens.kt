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
import androidx.compose.material.icons.filled.Search
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
import kotlinx.coroutines.launch
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    var showAnnouncementForm by remember { mutableStateOf(false) }
    var announcementTitle by remember { mutableStateOf("") }
    var announcementContent by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredStudents = remember(students, searchQuery) {
        if (searchQuery.isBlank()) students
        else students.filter { it.studentName.contains(searchQuery, ignoreCase = true) || it.email.contains(searchQuery, ignoreCase = true) }
    }

    GradilyBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
        ) {
            // ── Top bar ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onHome,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(GradilyTheme.colors.glassBg)
                    ) {
                        Icon(Icons.Default.Home, "Home", tint = GradilyTheme.colors.textPrimary)
                    }
                    Text(
                        "Class Management",
                        color = GradilyTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.size(48.dp)) // balance
                }
            }

            // ── Class header card ──
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Course name & enrollment badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    subject?.courseName ?: "Class",
                                    color = GradilyTheme.colors.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "ID: ${subject?.subjectId?.take(8) ?: "—"}",
                                    color = GradilyTheme.colors.textMuted,
                                    fontSize = 12.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (subject?.enrollmentOpen == true) GradilyTheme.colors.accentGreen.copy(alpha = 0.2f)
                                        else GradilyTheme.colors.accentRed.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    if (subject?.enrollmentOpen == true) "Open" else "Closed",
                                    color = if (subject?.enrollmentOpen == true) GradilyTheme.colors.accentGreen else GradilyTheme.colors.accentRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Stats row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${students.size}",
                                    color = GradilyTheme.colors.accentBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                                Text("Students", color = GradilyTheme.colors.textMuted, fontSize = 12.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(36.dp)
                                    .background(GradilyTheme.colors.glassBorder)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${subject?.creditHours ?: 0}",
                                    color = GradilyTheme.colors.accentPurple,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                                Text("Credits", color = GradilyTheme.colors.textMuted, fontSize = 12.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(36.dp)
                                    .background(GradilyTheme.colors.glassBorder)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val avgAttendance = if (students.isNotEmpty()) {
                                    val total = students.sumOf { it.totalClasses }
                                    val attended = students.sumOf { it.classesAttended }
                                    if (total > 0) (attended * 100 / total) else 0
                                } else 0
                                Text(
                                    "$avgAttendance%",
                                    color = if (avgAttendance >= 80) GradilyTheme.colors.accentGreen
                                        else if (avgAttendance >= 50) GradilyTheme.colors.accentAmber
                                        else GradilyTheme.colors.accentRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                                Text("Avg Attend.", color = GradilyTheme.colors.textMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // ── Quick actions row ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Add Student
                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onAddStudent() }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GradilyTheme.colors.lightGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, "Add", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Add Student", color = GradilyTheme.colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    // Post Announcement
                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showAnnouncementForm = !showAnnouncementForm }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GradilyTheme.colors.accentBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📢", fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Announce", color = GradilyTheme.colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    // Export PDF
                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                if (!isExporting && subject != null && students.isNotEmpty()) {
                                    isExporting = true
                                    scope.launch {
                                        val assessments = viewModel.getAssessmentsForStudents(students.map { it.studentId })
                                        com.example.myapplication.PdfExportHelper.exportGradesToPdf(
                                            context = context,
                                            subject = subject!!,
                                            students = students,
                                            assessments = assessments,
                                            calculateGPA = { viewModel.calculateGPA(it) }
                                        )
                                        isExporting = false
                                    }
                                } else if (students.isEmpty()) {
                                    android.widget.Toast.makeText(context, "No students to export", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GradilyTheme.colors.accentAmber.copy(alpha = 0.8f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isExporting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("📄", fontSize = 16.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Export", color = GradilyTheme.colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // ── Announcement form (collapsible) ──
            if (showAnnouncementForm) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Post Announcement", color = GradilyTheme.colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = announcementTitle,
                                onValueChange = { announcementTitle = it },
                                label = { Text("Title", color = GradilyTheme.colors.textMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GradilyTheme.colors.accentBlue,
                                    unfocusedBorderColor = GradilyTheme.colors.glassBorder,
                                    focusedTextColor = GradilyTheme.colors.textPrimary,
                                    unfocusedTextColor = GradilyTheme.colors.textPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = announcementContent,
                                onValueChange = { announcementContent = it },
                                label = { Text("Message", color = GradilyTheme.colors.textMuted) },
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GradilyTheme.colors.accentBlue,
                                    unfocusedBorderColor = GradilyTheme.colors.glassBorder,
                                    focusedTextColor = GradilyTheme.colors.textPrimary,
                                    unfocusedTextColor = GradilyTheme.colors.textPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showAnnouncementForm = false },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GradilyTheme.colors.textMuted)
                                ) {
                                    Text("Cancel")
                                }
                                GradilyButton(
                                    text = "Post",
                                    onClick = {
                                        if (subject != null && announcementTitle.isNotBlank() && announcementContent.isNotBlank()) {
                                            viewModel.postAnnouncement(subject.subjectId, announcementTitle, announcementContent)
                                            announcementTitle = ""
                                            announcementContent = ""
                                            showAnnouncementForm = false
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // ── Search bar ──
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search students...", color = GradilyTheme.colors.textMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, "Search", tint = GradilyTheme.colors.textMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GradilyTheme.colors.accentBlue,
                        unfocusedBorderColor = GradilyTheme.colors.glassBorder,
                        focusedTextColor = GradilyTheme.colors.textPrimary,
                        unfocusedTextColor = GradilyTheme.colors.textPrimary
                    )
                )
            }

            // ── Student list header ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Enrolled Students",
                        color = GradilyTheme.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        "${filteredStudents.size} of ${students.size}",
                        color = GradilyTheme.colors.textMuted,
                        fontSize = 13.sp
                    )
                }
            }

            // ── Student list ──
            if (filteredStudents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("👥", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No students found", color = GradilyTheme.colors.textSecondary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text(
                                if (students.isEmpty()) "Use the Add Student button above" else "Try a different search",
                                color = GradilyTheme.colors.textMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredStudents, key = { it.studentId }) { student ->
                    StudentListItem(student, viewModel, onAssessStudent)
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
    val assessmentFlow = remember(student.studentId) { viewModel.getAssessmentByStudentId(student.studentId) }
    val assessment by assessmentFlow.collectAsState(initial = null)
    val gpa = viewModel.calculateGPA(assessment)

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Avatar + Name + Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GradilyTheme.colors.surfaceGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            student.studentName.firstOrNull()?.uppercase() ?: "?",
                            color = GradilyTheme.colors.accentGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(student.studentName, color = GradilyTheme.colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(student.email.ifBlank { "No email" }, color = GradilyTheme.colors.textMuted, fontSize = 12.sp)
                        
                        if (student.totalClasses > 0) {
                            val attendancePercent = (student.classesAttended.toFloat() / student.totalClasses.toFloat() * 100).toInt()
                            Text(
                                "Attendance: $attendancePercent% (${student.classesAttended}/${student.totalClasses})",
                                color = if (attendancePercent >= 80) GradilyTheme.colors.accentGreen else if (attendancePercent >= 50) GradilyTheme.colors.accentAmber else GradilyTheme.colors.accentRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text("No attendance", color = GradilyTheme.colors.textMuted, fontSize = 11.sp)
                        }
                    }
                }

                // Top right: GPA & Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (gpa >= 3.0) GradilyTheme.colors.accentGreen.copy(alpha = 0.2f)
                                else if (gpa >= 2.0) GradilyTheme.colors.accentAmber.copy(alpha = 0.2f)
                                else GradilyTheme.colors.accentRed.copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            String.format("%.2f", gpa),
                            color = if (gpa >= 3.0) GradilyTheme.colors.accentGreen
                            else if (gpa >= 2.0) GradilyTheme.colors.accentAmber
                            else GradilyTheme.colors.accentRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GradilyTheme.colors.accentRed.copy(alpha = 0.1f))
                            .clickable { viewModel.deleteStudent(student) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Delete, "Delete", tint = GradilyTheme.colors.accentRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attendance P/A
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Attendance:", color = GradilyTheme.colors.textMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(GradilyTheme.colors.surfaceGreen)
                                .clickable { viewModel.markAttendance(student, true) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("P", color = GradilyTheme.colors.accentGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(GradilyTheme.colors.glassBgDark)
                                .clickable { viewModel.markAttendance(student, false) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("A", color = GradilyTheme.colors.accentRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Assess Button
                Button(
                    onClick = {
                        viewModel.setCurrentStudent(student)
                        onAssessStudent(student)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GradilyTheme.colors.accentBlue.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Assess", color = GradilyTheme.colors.accentBlue, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
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
                Text("Student Name", color = GradilyTheme.colors.textSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                GradilyTextField(
                    value = studentName,
                    onValueChange = { studentName = it },
                    label = "Full name"
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Student Email", color = GradilyTheme.colors.textSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                GradilyTextField(
                    value = studentEmail,
                    onValueChange = { studentEmail = it },
                    label = "student@email.com"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "The student can use this email to create an account and view their grades.",
                    color = GradilyTheme.colors.textMuted,
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
                Text("← Return", color = GradilyTheme.colors.textMuted, fontSize = 14.sp)
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
    val assessmentFlow = remember(student?.studentId) { viewModel.getAssessmentByStudentId(student?.studentId ?: "") }
    val assessment by assessmentFlow.collectAsState(initial = null)

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
                        .background(GradilyTheme.colors.glassBg)
                ) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = GradilyTheme.colors.textPrimary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(student?.studentName ?: "Student", color = GradilyTheme.colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("${subject?.courseName ?: "Class"} Assessment", color = GradilyTheme.colors.textMuted, fontSize = 13.sp)
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
                        Text("Current GPA", color = GradilyTheme.colors.textMuted, fontSize = 13.sp)
                        Text(
                            String.format("%.2f", currentGpa),
                            color = if (currentGpa >= 3.0) GradilyTheme.colors.accentGreen
                            else if (currentGpa >= 2.0) GradilyTheme.colors.accentAmber
                            else GradilyTheme.colors.accentRed,
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
                        color = GradilyTheme.colors.textSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            // Grade entries
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text("Assessment Scores", color = GradilyTheme.colors.textSecondary, fontWeight = FontWeight.Medium, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
                }
                item { GradeInputCard("Quiz 1", quiz1, "Max: 10") { quiz1 = it } }
                item { GradeInputCard("Assignment 1", assign1, "Max: 25") { assign1 = it } }
                item { GradeInputCard("Midterm", midterm, "Max: 10") { midterm = it } }
                item { GradeInputCard("Quiz 2", quiz2, "Max: 10") { quiz2 = it } }
                item { GradeInputCard("Assignment 2", assign2, "Max: 25") { assign2 = it } }
                item { GradeInputCard("Final Exam", finalExam, "Max: 40") { finalExam = it } }

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
                            ) ?: Assessment(
                                gradeId = "",
                                studentId = student?.studentId ?: "",
                                subjectId = subject?.subjectId ?: "",
                                quiz1 = quiz1.toDoubleOrNull() ?: 0.0,
                                assign1 = assign1.toDoubleOrNull() ?: 0.0,
                                midterm = midterm.toDoubleOrNull() ?: 0.0,
                                quiz2 = quiz2.toDoubleOrNull() ?: 0.0,
                                assign2 = assign2.toDoubleOrNull() ?: 0.0,
                                finalExam = finalExam.toDoubleOrNull() ?: 0.0
                            )
                            viewModel.updateAssessment(updatedAssessment)
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
                Text(label, color = GradilyTheme.colors.textPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Text(hint, color = GradilyTheme.colors.textMuted, fontSize = 11.sp)
            }
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = GradilyTheme.colors.glassBgDark,
                    unfocusedContainerColor = GradilyTheme.colors.glassBgDark,
                    focusedBorderColor = GradilyTheme.colors.lightGreen,
                    unfocusedBorderColor = GradilyTheme.colors.glassBorder,
                    focusedTextColor = GradilyTheme.colors.textPrimary,
                    unfocusedTextColor = GradilyTheme.colors.textPrimary,
                    cursorColor = GradilyTheme.colors.lightGreen
                ),
                textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.width(100.dp).height(52.dp),
                singleLine = true
            )
        }
    }
}
