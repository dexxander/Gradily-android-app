package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.GradilyViewModel
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: GradilyViewModel,
    onNavigateHome: () -> Unit,
    onNavigateSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current

    GradilyDrawer(
        drawerState = drawerState,
        user = user,
        onNavigateHome = onNavigateHome,
        onNavigateProfile = { /* Already here */ },
        onNavigateSettings = onNavigateSettings,
        onLogout = onLogout
    ) {
        GradilyBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier.clip(CircleShape).background(GradilyTheme.colors.glassBg)
                    ) {
                        Icon(Icons.Default.Menu, "Menu", tint = GradilyTheme.colors.textPrimary)
                    }
                    Text("Profile", color = GradilyTheme.colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.size(48.dp)) // To balance the menu icon
                }

                // ID Card
                GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                    ) { uri ->
                        if (uri != null) {
                            viewModel.updateProfilePicture(uri.toString())
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(GradilyTheme.colors.lightGreen, GradilyTheme.colors.accentBlue)))
                                .border(4.dp, GradilyTheme.colors.glassBorder, CircleShape)
                                .clickable { launcher.launch("image/*") },
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
                                Text(if (user?.role == "LECTURER") "👨‍🏫" else "🎒", fontSize = 48.sp)
                            }
                            
                            // Edit badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(GradilyTheme.colors.accentGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        var isEditingName by remember { mutableStateOf(false) }
                        var tempName by remember { mutableStateOf(user?.name ?: "") }
                        
                        if (isEditingName) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = tempName,
                                    onValueChange = { tempName = it },
                                    singleLine = true,
                                    modifier = Modifier.width(180.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GradilyTheme.colors.accentBlue,
                                        focusedTextColor = GradilyTheme.colors.textPrimary,
                                        unfocusedTextColor = GradilyTheme.colors.textPrimary
                                    )
                                )
                                IconButton(onClick = { 
                                    if (tempName.isNotBlank()) {
                                        viewModel.updateName(tempName)
                                    }
                                    isEditingName = false 
                                }) {
                                    Icon(Icons.Default.Check, contentDescription = "Save", tint = GradilyTheme.colors.accentGreen)
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val displayName = if (!user?.name.isNullOrBlank()) user!!.name else (user?.email?.substringBefore("@")?.capitalize() ?: "User")
                                Text(
                                    displayName,
                                    color = GradilyTheme.colors.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                                IconButton(onClick = { 
                                    tempName = if (!user?.name.isNullOrBlank()) user!!.name else (user?.email?.substringBefore("@")?.capitalize() ?: "User")
                                    isEditingName = true 
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Name", tint = GradilyTheme.colors.textMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        
                        Text(
                            user?.email ?: "user@gradily.com",
                            color = GradilyTheme.colors.textMuted,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(GradilyTheme.colors.accentPurple.copy(alpha = 0.2f))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                user?.role ?: "GUEST",
                                color = GradilyTheme.colors.accentPurple,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Role-specific content
                if (user?.role == "STUDENT") {
                    val enrolledStudents by viewModel.getEnrolledStudents().collectAsState(initial = emptyList())
                    var totalGpa = 0.0
                    var classesGraded = 0
                    
                    enrolledStudents.forEach { student ->
                        val assessment by viewModel.getAssessmentByStudentId(student.studentId).collectAsState(initial = null)
                        val gpa = viewModel.calculateGPA(assessment)
                        if (gpa > 0.0) {
                            totalGpa += gpa
                            classesGraded++
                        }
                    }
                    
                    val cgpa = if (classesGraded > 0) totalGpa / classesGraded else 0.0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GlassCard(modifier = Modifier.weight(1f)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text("CGPA", color = GradilyTheme.colors.textMuted, fontSize = 12.sp)
                                Text(
                                    String.format("%.2f", cgpa),
                                    color = if (cgpa >= 3.0) GradilyTheme.colors.accentGreen else if (cgpa >= 2.0) GradilyTheme.colors.accentAmber else GradilyTheme.colors.accentRed,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        GlassCard(modifier = Modifier.weight(1f)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text("Enrolled", color = GradilyTheme.colors.textMuted, fontSize = 12.sp)
                                Text(
                                    "${enrolledStudents.size}",
                                    color = GradilyTheme.colors.accentBlue,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionHeader("Achievements")
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            AchievementItem("🌟", "Dean's List", "Achieved a CGPA above 3.5")
                        }
                        item {
                            AchievementItem("🎯", "Perfect Attendance", "Never missed a class")
                        }
                    }
                } else {
                    val subjects by viewModel.getSubjects().collectAsState(initial = emptyList())
                    val allStudents by viewModel.getStudents().collectAsState(initial = emptyList())
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GlassCard(modifier = Modifier.weight(1f)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text("Classes", color = GradilyTheme.colors.textMuted, fontSize = 12.sp)
                                Text(
                                    "${subjects.size}",
                                    color = GradilyTheme.colors.accentGreen,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        GlassCard(modifier = Modifier.weight(1f)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text("Total Students", color = GradilyTheme.colors.textMuted, fontSize = 12.sp)
                                Text(
                                    "${allStudents.size}",
                                    color = GradilyTheme.colors.accentBlue,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementItem(emoji: String, title: String, desc: String) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(GradilyTheme.colors.glassBgDark),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, color = GradilyTheme.colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(desc, color = GradilyTheme.colors.textMuted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SettingsScreen(
    user: com.example.myapplication.data.User?,
    onNavigateHome: () -> Unit,
    onNavigateProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var pushEnabled by remember { mutableStateOf(true) }
    var emailEnabled by remember { mutableStateOf(false) }

    GradilyDrawer(
        drawerState = drawerState,
        user = user,
        onNavigateHome = onNavigateHome,
        onNavigateProfile = onNavigateProfile,
        onNavigateSettings = { /* Already here */ },
        onLogout = onLogout
    ) {
        GradilyBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(24.dp)
            ) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier.clip(CircleShape).background(GradilyTheme.colors.glassBg)
                    ) {
                        Icon(Icons.Default.Menu, "Menu", tint = GradilyTheme.colors.textPrimary)
                    }
                    Text("Settings", color = GradilyTheme.colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.size(48.dp))
                }

                SectionHeader("Preferences")
                GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Push Notifications", color = GradilyTheme.colors.textPrimary, fontWeight = FontWeight.Medium)
                            Text("Receive alerts on your device", color = GradilyTheme.colors.textMuted, fontSize = 12.sp)
                        }
                        Switch(
                            checked = pushEnabled,
                            onCheckedChange = { pushEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = GradilyTheme.colors.lightGreen, checkedTrackColor = GradilyTheme.colors.surfaceGreen)
                        )
                    }
                    Divider(color = GradilyTheme.colors.glassBorder, modifier = Modifier.padding(vertical = 16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Email Updates", color = GradilyTheme.colors.textPrimary, fontWeight = FontWeight.Medium)
                            Text("Receive summary reports", color = GradilyTheme.colors.textMuted, fontSize = 12.sp)
                        }
                        Switch(
                            checked = emailEnabled,
                            onCheckedChange = { emailEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = GradilyTheme.colors.lightGreen, checkedTrackColor = GradilyTheme.colors.surfaceGreen)
                        )
                    }
                }

                SectionHeader("Account")
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { /* Placeholder */ }, modifier = Modifier.fillMaxWidth()) {
                        Text("Change Password", color = GradilyTheme.colors.accentBlue)
                    }
                    Divider(color = GradilyTheme.colors.glassBorder, modifier = Modifier.padding(vertical = 8.dp))
                    TextButton(onClick = { /* Placeholder */ }, modifier = Modifier.fillMaxWidth()) {
                        Text("Privacy Policy", color = GradilyTheme.colors.textSecondary)
                    }
                    Divider(color = GradilyTheme.colors.glassBorder, modifier = Modifier.padding(vertical = 8.dp))
                    TextButton(onClick = { /* Placeholder */ }, modifier = Modifier.fillMaxWidth()) {
                        Text("Delete Account", color = GradilyTheme.colors.accentRed)
                    }
                }
            }
        }
    }
}
