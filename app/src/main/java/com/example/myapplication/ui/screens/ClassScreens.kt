package com.example.myapplication.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.GradilyViewModel
import com.example.myapplication.data.Subject
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ClassListScreen(
    viewModel: GradilyViewModel,
    onCreateClass: () -> Unit,
    onManageClass: (Subject) -> Unit,
    onNavigateProfile: () -> Unit,
    onNavigateSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val subjects by viewModel.getSubjects().collectAsState(initial = emptyList())
    val user by viewModel.currentUser.collectAsState()
    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var searchQuery by remember { mutableStateOf("") }

    GradilyDrawer(
        drawerState = drawerState,
        user = user,
        onNavigateHome = { /* Already on home */ },
        onNavigateProfile = onNavigateProfile,
        onNavigateSettings = onNavigateSettings,
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
                                .background(GradilyTheme.colors.glassBg)
                        ) {
                            Icon(Icons.Default.Menu, "Menu", tint = GradilyTheme.colors.textPrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        // Profile picture
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(GradilyTheme.colors.lightGreen, GradilyTheme.colors.accentGreen)),
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
                                Text("👤", fontSize = 22.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Welcome back,",
                                color = GradilyTheme.colors.textMuted,
                                fontSize = 12.sp
                            )
                            val displayName = if (!user?.name.isNullOrBlank()) user!!.name else (user?.email?.substringBefore("@") ?: "Lecturer")
                            Text(
                                displayName,
                                color = GradilyTheme.colors.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(GradilyTheme.colors.glassBg)
                    ) {
                        Icon(Icons.Default.ExitToApp, "Logout", tint = GradilyTheme.colors.accentRed)
                    }
                }

                // Title
                SectionHeader("Your Classes")
                SectionSubtitle("${subjects.size} ${if (subjects.size == 1) "class" else "classes"} registered")

                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search classes...", color = GradilyTheme.colors.textMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, "Search", tint = GradilyTheme.colors.textMuted) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GradilyTheme.colors.accentBlue,
                        unfocusedBorderColor = GradilyTheme.colors.glassBorder,
                        focusedTextColor = GradilyTheme.colors.textPrimary,
                        unfocusedTextColor = GradilyTheme.colors.textPrimary
                    )
                )

                val filteredSubjects = remember(subjects, searchQuery) {
                    if (searchQuery.isBlank()) subjects
                    else subjects.filter { it.courseName.contains(searchQuery, ignoreCase = true) || it.subjectId.contains(searchQuery, ignoreCase = true) }
                }

                // Create class FAB button
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onCreateClass() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GradilyTheme.colors.lightGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, "Add", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Create New Class", color = GradilyTheme.colors.textPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Add a course to manage", color = GradilyTheme.colors.textMuted, fontSize = 12.sp)
                        }
                    }
                }

                // Class list
                if (filteredSubjects.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📚", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No classes yet", color = GradilyTheme.colors.textSecondary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            Text("Tap + to create your first class", color = GradilyTheme.colors.textMuted, fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredSubjects, key = { it.subjectId }) { subject ->
                            GlassCard(
                                modifier = Modifier.fillMaxWidth().animateItem()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                        Text(
                                            subject.courseName,
                                            color = GradilyTheme.colors.textPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                        Text(
                                            "${subject.creditHours} Credit Hours",
                                            color = GradilyTheme.colors.textMuted,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.setCurrentSubject(subject)
                                                onManageClass(subject)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = GradilyTheme.colors.accentAmber),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Text("Manage", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteSubject(subject) },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(GradilyTheme.colors.accentRed.copy(alpha = 0.15f))
                                        ) {
                                            Icon(Icons.Default.Delete, "Delete", tint = GradilyTheme.colors.accentRed, modifier = Modifier.size(20.dp))
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
fun ClassCreationScreen(
    viewModel: GradilyViewModel,
    onNavigateBack: () -> Unit,
    onHome: () -> Unit
) {
    var className by remember { mutableStateOf("") }
    var creditHours by remember { mutableStateOf("3") }
    var enrollmentOpen by remember { mutableStateOf(false) }

    GradilyBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.15f))

            Text("📝", fontSize = 48.sp, modifier = Modifier.padding(bottom = 16.dp))
            SectionHeader("Create Class")
            SectionSubtitle("Set up a new course")

            Spacer(modifier = Modifier.height(16.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Course Name", color = GradilyTheme.colors.textSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                GradilyTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = "e.g. Mathematics 101"
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Credit Hours", color = GradilyTheme.colors.textSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                GradilyTextField(
                    value = creditHours,
                    onValueChange = { creditHours = it },
                    label = "e.g. 3"
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Open to everyone", color = GradilyTheme.colors.textPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("Students can self-enroll", color = GradilyTheme.colors.textMuted, fontSize = 12.sp)
                    }
                    androidx.compose.material3.Switch(
                        checked = enrollmentOpen,
                        onCheckedChange = { enrollmentOpen = it },
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GradilyTheme.colors.accentBlue
                        )
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                GradilyButton(
                    text = "Create Class",
                    onClick = {
                        if (className.isNotBlank()) {
                            viewModel.createSubject(className, creditHours.toIntOrNull() ?: 3, enrollmentOpen)
                            onNavigateBack()
                        }
                    },
                    enabled = className.isNotBlank()
                )
            }

            Spacer(modifier = Modifier.weight(0.3f))

            TextButton(onClick = onNavigateBack) {
                Text("← Return", color = GradilyTheme.colors.textMuted, fontSize = 14.sp)
            }
        }
    }
}
