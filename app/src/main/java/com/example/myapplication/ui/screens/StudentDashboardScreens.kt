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
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

    // Image picker for profile picture
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) { }
            viewModel.updateProfilePicture(it.toString())
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    GradilyDrawer(
        drawerState = drawerState,
        user = user,
        onNavigateHome = { /* Already on home */ },
        onLogout = onLogout
    ) {
        GradilyBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(24.dp)
            ) {
                // Top bar: hamburger + profile + logout
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(GlassBg)
                        ) {
                            Icon(Icons.Default.Menu, "Menu", tint = TextPrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(AccentBlue, AccentPurple)),
                                    CircleShape
                                )
                                .border(2.dp, GlassBorder, CircleShape)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (user?.profilePicUri != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(user!!.profilePicUri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text("🎒", fontSize = 22.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Hello,",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            Text(
                                user?.email?.substringBefore("@") ?: "Student",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(GlassBg)
                    ) {
                        Icon(Icons.Default.ExitToApp, "Logout", tint = AccentRed)
                    }
                }

                SectionHeader("My Courses")
                SectionSubtitle("${enrolledStudents.size} ${if (enrolledStudents.size == 1) "course" else "courses"} enrolled")

                if (enrolledStudents.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📭", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No courses yet", color = TextSecondary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            Text("Your lecturer will add you to a class", color = TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(enrolledStudents, key = { it.studentId }) { student ->
                            val assessment by viewModel.getAssessmentByStudentId(student.studentId).collectAsState(initial = null)
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
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                        Text(
                                            "Tap to view grades",
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                    // GPA badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (gpa >= 3.0) AccentGreen.copy(alpha = 0.2f)
                                                else if (gpa >= 2.0) AccentAmber.copy(alpha = 0.2f)
                                                else AccentRed.copy(alpha = 0.2f)
                                            )
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                String.format("%.2f", gpa),
                                                color = if (gpa >= 3.0) AccentGreen
                                                else if (gpa >= 2.0) AccentAmber
                                                else AccentRed,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                            Text("GPA", color = TextMuted, fontSize = 10.sp)
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
}

@Composable
fun StudentSubjectDetailScreen(
    viewModel: GradilyViewModel,
    student: Student,
    onNavigateBack: () -> Unit
) {
    val assessment by viewModel.getAssessmentByStudentId(student.studentId).collectAsState(initial = null)
    val gpa = viewModel.calculateGPA(assessment)
    val scope = rememberCoroutineScope()
    var subjectName by remember { mutableStateOf("Loading...") }

    LaunchedEffect(student.subjectId) {
        val subject = viewModel.getSubjectById(student.subjectId)
        subjectName = subject?.courseName ?: "Unknown"
    }

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
                    Text(subjectName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Grade Report", color = TextMuted, fontSize = 13.sp)
                }
            }

            // GPA card
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
                        color = if (gpa >= 3.0) AccentGreen
                        else if (gpa >= 2.0) AccentAmber
                        else AccentRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp
                    )
                    Text("GPA", color = TextMuted, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        when {
                            gpa >= 3.5 -> "Excellent Performance"
                            gpa >= 3.0 -> "Good Performance"
                            gpa >= 2.0 -> "Average Performance"
                            gpa > 0 -> "Needs Improvement"
                            else -> "Not Graded Yet"
                        },
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                "Assessment Breakdown",
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Grade items (read-only for students)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { ReadOnlyGradeCard("Quiz 1", assessment?.quiz1 ?: 0.0, 10.0) }
                item { ReadOnlyGradeCard("Assignment 1", assessment?.assign1 ?: 0.0, 25.0) }
                item { ReadOnlyGradeCard("Midterm", assessment?.midterm ?: 0.0, 10.0) }
                item { ReadOnlyGradeCard("Quiz 2", assessment?.quiz2 ?: 0.0, 10.0) }
                item { ReadOnlyGradeCard("Assignment 2", assessment?.assign2 ?: 0.0, 25.0) }
                item { ReadOnlyGradeCard("Final Exam", assessment?.finalExam ?: 0.0, 100.0) }
            }
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
                Text(label, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(6.dp))
                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(GlassBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = percentage.coerceIn(0f, 1f))
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (percentage >= 0.7) AccentGreen
                                else if (percentage >= 0.4) AccentAmber
                                else AccentRed
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    String.format("%.1f", score),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    "/ ${maxScore.toInt()}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}
