package com.example.myapplication.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.GradilyViewModel
import com.example.myapplication.data.Assessment
import com.example.myapplication.data.Student
import com.example.myapplication.data.Subject
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun StudentDashboardScreen(
    viewModel: GradilyViewModel,
    onViewSubject: (Student) -> Unit,
    onNavigateProfile: () -> Unit,
    onNavigateSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val enrolledStudents by viewModel.getEnrolledStudents().collectAsState(initial = emptyList())
    val user by viewModel.currentUser.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Resolve subject names for each enrollment
    val subjectNames = remember { mutableStateMapOf<String, String>() }
    LaunchedEffect(enrolledStudents) {
        enrolledStudents.forEach { student ->
            if (!subjectNames.containsKey(student.subjectId)) {
                val subject = viewModel.getSubjectById(student.subjectId)
                subjectNames[student.subjectId] = subject?.courseName ?: "Unknown"
            }
        }
    }

    val allAnnouncements by viewModel.getAnnouncements().collectAsState(initial = emptyList())

    GradilyBottomBar(
        currentItem = BottomNavItem.HOME,
        onNavigateHome = { /* Already on home */ },
        onNavigateProfile = onNavigateProfile,
        onNavigateSettings = onNavigateSettings,
        onLogout = onLogout
    ) { innerPadding ->
        GradilyBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .systemBarsPadding()
                    .padding(horizontal = 24.dp)
            ) {
                // Top bar: profile
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(GradilyTheme.colors.accentBlue, GradilyTheme.colors.accentPurple)),
                                CircleShape
                            )
                            .border(2.dp, GradilyTheme.colors.glassBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val picUri = user?.profilePicUri
                        if (picUri != null) {
                            if (picUri.startsWith("data:image")) {
                                val base64 = picUri.substringAfter(",")
                                val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                                val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                if (bitmap != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Profile",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(picUri).build(),
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            Text("🎒", fontSize = 22.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Hello,",
                            color = GradilyTheme.colors.textMuted,
                            fontSize = 12.sp
                        )
                        val displayName = if (!user?.name.isNullOrBlank()) user!!.name else (user?.email?.substringBefore("@") ?: "Student")
                        Text(
                            displayName,
                            color = GradilyTheme.colors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }

                // Calculate announcements relevant to enrolled subjects
                val relevantAnnouncements = remember(enrolledStudents, allAnnouncements) {
                    val subjectIds = enrolledStudents.map { it.subjectId }.toSet()
                    allAnnouncements.filter { it.subjectId in subjectIds }
                }

                if (relevantAnnouncements.isNotEmpty()) {
                    SectionHeader("Recent Announcements")
                    val latest = relevantAnnouncements.first()
                    GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                        Column {
                            Text(subjectNames[latest.subjectId] ?: "Course", color = GradilyTheme.colors.accentBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(latest.title, color = GradilyTheme.colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(latest.content, color = GradilyTheme.colors.textSecondary, fontSize = 14.sp, maxLines = 2)
                        }
                    }
                }

                val openSubjects by viewModel.getOpenSubjects().collectAsState(initial = emptyList())
                val enrolledSubjectIds = enrolledStudents.map { it.subjectId }.toSet()
                val availableOpenSubjects = openSubjects.filter { it.subjectId !in enrolledSubjectIds }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        SectionHeader("My Courses")
                        SectionSubtitle("${enrolledStudents.size} ${if (enrolledStudents.size == 1) "course" else "courses"} enrolled")
                    }

                    if (enrolledStudents.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📭", fontSize = 48.sp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("No courses yet", color = GradilyTheme.colors.textSecondary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                                    Text("Your lecturer will add you to a class", color = GradilyTheme.colors.textMuted, fontSize = 14.sp, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    } else {
                        items(enrolledStudents, key = { it.studentId }) { student ->
                            val assessmentFlow = remember(student.studentId) { viewModel.getAssessmentByStudentId(student.studentId) }
                            val assessment by assessmentFlow.collectAsState(initial = null)
                            val gpa = viewModel.calculateGPA(assessment)

                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { onViewSubject(student) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            subjectNames[student.subjectId] ?: "Loading...",
                                            color = GradilyTheme.colors.textPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                        Text(
                                            "Tap to view grades",
                                            color = GradilyTheme.colors.textMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                    // GPA badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (gpa >= 3.0) GradilyTheme.colors.accentGreen.copy(alpha = 0.2f)
                                                else if (gpa >= 2.0) GradilyTheme.colors.accentAmber.copy(alpha = 0.2f)
                                                else GradilyTheme.colors.accentRed.copy(alpha = 0.2f)
                                            )
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                String.format("%.2f", gpa),
                                                color = if (gpa >= 3.0) GradilyTheme.colors.accentGreen
                                                else if (gpa >= 2.0) GradilyTheme.colors.accentAmber
                                                else GradilyTheme.colors.accentRed,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                            Text("GPA", color = GradilyTheme.colors.textMuted, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (availableOpenSubjects.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            SectionHeader("Discover Open Classes")
                            SectionSubtitle("Self-enroll into available courses")
                        }

                        items(availableOpenSubjects, key = { it.subjectId }) { subject ->
                            GlassCard(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            subject.courseName,
                                            color = GradilyTheme.colors.textPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            "${subject.creditHours} Credits",
                                            color = GradilyTheme.colors.textMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                    androidx.compose.material3.Button(
                                        onClick = { viewModel.selfEnroll(subject) },
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = GradilyTheme.colors.accentGreen
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Enroll", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentSubjectDetailScreen(
    viewModel: GradilyViewModel,
    student: Student,
    onNavigateBack: () -> Unit
) {
    val assessmentFlow = remember(student.studentId) { viewModel.getAssessmentByStudentId(student.studentId) }
    val assessment by assessmentFlow.collectAsState(initial = null)
    val gpa = viewModel.calculateGPA(assessment)
    val scope = rememberCoroutineScope()
    var subjectName by remember { mutableStateOf("Loading...") }
    var showUnenrollConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(student.subjectId) {
        val subject = viewModel.getSubjectById(student.subjectId)
        subjectName = subject?.courseName ?: "Unknown"
    }

    GradilyBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Top bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                            Text(subjectName, color = GradilyTheme.colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("Grade Report", color = GradilyTheme.colors.textMuted, fontSize = 13.sp)
                        }
                    }
                    
                    IconButton(
                        onClick = { showUnenrollConfirmation = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(GradilyTheme.colors.glassBg)
                    ) {
                        Icon(Icons.Default.Delete, "Unenroll", tint = GradilyTheme.colors.accentRed)
                    }
                }
            }

            // GPA card
            item {
                GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            when {
                                gpa >= 3.5 -> "🌟"
                                gpa >= 3.0 -> "👍"
                                gpa >= 2.0 -> "📝"
                                gpa > 0 -> "⚠️"
                                else -> "📋"
                            },
                            fontSize = 40.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            String.format("%.2f", gpa),
                            color = if (gpa >= 3.0) GradilyTheme.colors.accentGreen
                            else if (gpa >= 2.0) GradilyTheme.colors.accentAmber
                            else GradilyTheme.colors.accentRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 48.sp
                        )
                        Text("GPA", color = GradilyTheme.colors.textMuted, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            when {
                                gpa >= 3.5 -> "Excellent Performance"
                                gpa >= 3.0 -> "Good Performance"
                                gpa >= 2.0 -> "Average Performance"
                                gpa > 0 -> "Needs Improvement"
                                else -> "Not Graded Yet"
                            },
                            color = GradilyTheme.colors.textSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Grade Analytics Chart
            item {
                GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Column {
                        Text("Score Breakdown", color = GradilyTheme.colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        GradeBarChart(
                            labels = listOf("Quiz 1", "Assign 1", "Midterm", "Quiz 2", "Assign 2", "Final"),
                            values = listOf(
                                (assessment?.quiz1 ?: 0.0).toFloat(),
                                (assessment?.assign1 ?: 0.0).toFloat(),
                                (assessment?.midterm ?: 0.0).toFloat(),
                                (assessment?.quiz2 ?: 0.0).toFloat(),
                                (assessment?.assign2 ?: 0.0).toFloat(),
                                (assessment?.finalExam ?: 0.0).toFloat()
                            ),
                            maxValues = listOf(10f, 25f, 10f, 10f, 25f, 100f)
                        )
                    }
                }
            }

            // Leaderboard
            item {
                val classmates by viewModel.getStudentsBySubject(student.subjectId).collectAsState(initial = emptyList())
                if (classmates.size > 1) {
                    GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Column {
                            Text("Class Ranking", color = GradilyTheme.colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Build ranked list
                            data class RankedStudent(val name: String, val gpa: Double, val isMe: Boolean)
                            
                            val ranked = classmates.map { s ->
                                RankedStudent(
                                    name = s.studentName.take(1) + "***" + s.studentName.takeLast(1),
                                    gpa = 0.0,
                                    isMe = s.studentId == student.studentId
                                )
                            }
                            
                            Text(
                                "Your rank will appear once all grades are submitted",
                                color = GradilyTheme.colors.textMuted,
                                fontSize = 12.sp
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            LeaderboardEntry(
                                rank = 0,
                                name = "You — ${student.studentName}",
                                gpa = gpa,
                                isCurrentUser = true
                            )
                        }
                    }
                }
            }

            // Assessment Breakdown header
            item {
                Text(
                    "Assessment Breakdown",
                    color = GradilyTheme.colors.textSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Grade items (read-only for students)
            item { ReadOnlyGradeCard("Quiz 1", assessment?.quiz1 ?: 0.0, 10.0) }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { ReadOnlyGradeCard("Assignment 1", assessment?.assign1 ?: 0.0, 25.0) }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { ReadOnlyGradeCard("Midterm", assessment?.midterm ?: 0.0, 10.0) }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { ReadOnlyGradeCard("Quiz 2", assessment?.quiz2 ?: 0.0, 10.0) }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { ReadOnlyGradeCard("Assignment 2", assessment?.assign2 ?: 0.0, 25.0) }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { ReadOnlyGradeCard("Final Exam", assessment?.finalExam ?: 0.0, 40.0) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        if (showUnenrollConfirmation) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showUnenrollConfirmation = false },
                title = { Text("Unenroll from Class", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to unenroll from '$subjectName'? You will lose access to all class materials and your grade data will be permanently deleted.") },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            viewModel.unenroll(student)
                            showUnenrollConfirmation = false
                            onNavigateBack()
                        }
                    ) {
                        Text("Unenroll", color = GradilyTheme.colors.accentRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showUnenrollConfirmation = false }) {
                        Text("Cancel", color = GradilyTheme.colors.textPrimary)
                    }
                },
                containerColor = GradilyTheme.colors.surface,
                titleContentColor = GradilyTheme.colors.textPrimary,
                textContentColor = GradilyTheme.colors.textSecondary,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun ReadOnlyGradeCard(label: String, score: Double, maxScore: Double) {
    val percentage = if (maxScore > 0) (score / maxScore).toFloat() else 0f

    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 14.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = GradilyTheme.colors.textPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(6.dp))
                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(GradilyTheme.colors.glassBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = percentage.coerceIn(0f, 1f))
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (percentage >= 0.7) GradilyTheme.colors.accentGreen
                                else if (percentage >= 0.4) GradilyTheme.colors.accentAmber
                                else GradilyTheme.colors.accentRed
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    String.format("%.1f", score),
                    color = GradilyTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    "/ ${maxScore.toInt()}",
                    color = GradilyTheme.colors.textMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}
