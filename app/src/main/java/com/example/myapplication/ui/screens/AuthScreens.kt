package com.example.myapplication.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.GradilyViewModel
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*

const val WEB_CLIENT_ID = "667003870196-5r1qao1v97f20urlrptluuko8e78s7h1.apps.googleusercontent.com"

@Composable
fun MainAuthScreen(
    onNavigateToLogin: (String) -> Unit,
    onNavigateToSignUp: (String) -> Unit
) {
    var selectedRole by remember { mutableStateOf<String?>(null) }

    GradilyBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.3f))

            // App title
            Text(
                "🎓",
                fontSize = 64.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "Gradily",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                "Grade Management System",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Role selection cards
            Text(
                "I am a...",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Lecturer card
                val isLecturer = selectedRole == "LECTURER"
                val lecturerScale by animateFloatAsState(if (isLecturer) 1.05f else 1f)
                
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .scale(lecturerScale)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { selectedRole = "LECTURER" }
                        .then(
                            if (isLecturer) {
                                Modifier.background(
                                    Brush.linearGradient(listOf(SurfaceGreen, MediumGreen)),
                                    RoundedCornerShape(20.dp)
                                )
                            } else Modifier
                        )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("👨‍🏫", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Lecturer", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Manage grades", color = TextMuted, fontSize = 11.sp)
                    }
                }

                // Student card
                val isStudent = selectedRole == "STUDENT"
                val studentScale by animateFloatAsState(if (isStudent) 1.05f else 1f)
                
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .scale(studentScale)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { selectedRole = "STUDENT" }
                        .then(
                            if (isStudent) {
                                Modifier.background(
                                    Brush.linearGradient(listOf(SurfaceGreen, MediumGreen)),
                                    RoundedCornerShape(20.dp)
                                )
                            } else Modifier
                        )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🎒", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Student", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("View grades", color = TextMuted, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = selectedRole != null,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut()
            ) {
                Column {
                    GradilyButton(
                        text = "Login",
                        onClick = { selectedRole?.let { onNavigateToLogin(it) } }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GradilyOutlineButton(
                        text = "Create Account",
                        onClick = { selectedRole?.let { onNavigateToSignUp(it) } }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.3f))
        }
    }
}

@Composable
fun AnimatedButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        content()
    }
}

@Composable
fun GoogleSignInButton(onClick: () -> Unit, isLoading: Boolean) {
    AnimatedButton(onClick = onClick) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = DarkGreen, modifier = Modifier.size(24.dp))
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("G", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Continue with Google",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun AuthDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f).height(1.dp).background(GlassBorder))
        Text("  OR  ", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Box(modifier = Modifier.weight(1f).height(1.dp).background(GlassBorder))
    }
}

@Composable
fun LoginScreen(
    viewModel: GradilyViewModel,
    role: String,
    onNavigateBack: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val roleLabel = if (role == "LECTURER") "Lecturer" else "Student"

    GradilyBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.15f))

            Text(
                if (role == "LECTURER") "👨‍🏫" else "🎒",
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            SectionHeader("Welcome Back")
            SectionSubtitle("Sign in as $roleLabel")

            Spacer(modifier = Modifier.height(16.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GradilyTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email"
                )
                GradilyTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    isPassword = true
                )
                Spacer(modifier = Modifier.height(24.dp))
                GradilyButton(
                    text = if (isLoading) "Signing in..." else "Login",
                    onClick = {
                        isLoading = true
                        viewModel.login(email, password, role) { success, msg ->
                            isLoading = false
                            if (success) {
                                onLoginSuccess()
                            } else {
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isLoading && !isGoogleLoading && email.isNotBlank() && password.isNotBlank()
                )

                AuthDivider()

                GoogleSignInButton(
                    isLoading = isGoogleLoading,
                    onClick = {
                        if (!isLoading && !isGoogleLoading) {
                            isGoogleLoading = true
                            viewModel.signInWithGoogle(context, WEB_CLIENT_ID, role) { success, msg ->
                                isGoogleLoading = false
                                if (success) {
                                    onLoginSuccess()
                                } else {
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.weight(0.3f))

            TextButton(onClick = onNavigateBack) {
                Text("← Back to role selection", color = TextMuted, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun SignUpScreen(
    viewModel: GradilyViewModel,
    role: String,
    onNavigateBack: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val roleLabel = if (role == "LECTURER") "Lecturer" else "Student"

    GradilyBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.1f))

            Text(
                if (role == "LECTURER") "👨‍🏫" else "🎒",
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            SectionHeader("Create Account")
            SectionSubtitle("Register as $roleLabel")

            Spacer(modifier = Modifier.height(16.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GradilyTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email"
                )
                GradilyTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    isPassword = true
                )
                GradilyTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                    isPassword = true
                )
                Spacer(modifier = Modifier.height(24.dp))
                GradilyButton(
                    text = if (isLoading) "Creating..." else "Sign Up",
                    onClick = {
                        if (password != confirmPassword) {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        } else {
                            isLoading = true
                            viewModel.signUp(email, password, role) { success, msg ->
                                isLoading = false
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success) onSignUpSuccess()
                            }
                        }
                    },
                    enabled = !isLoading && !isGoogleLoading && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
                )

                AuthDivider()

                GoogleSignInButton(
                    isLoading = isGoogleLoading,
                    onClick = {
                        if (!isLoading && !isGoogleLoading) {
                            isGoogleLoading = true
                            viewModel.signInWithGoogle(context, WEB_CLIENT_ID, role) { success, msg ->
                                isGoogleLoading = false
                                if (success) {
                                    // Signed up/logged in successfully
                                    onSignUpSuccess()
                                } else {
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.weight(0.2f))

            TextButton(onClick = onNavigateBack) {
                Text("← Back to role selection", color = TextMuted, fontSize = 14.sp)
            }
        }
    }
}
