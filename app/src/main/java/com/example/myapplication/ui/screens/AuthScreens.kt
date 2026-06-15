package com.example.myapplication.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.GradilyViewModel
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*

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
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { selectedRole = "LECTURER" }
                        .then(
                            if (selectedRole == "LECTURER") {
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
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { selectedRole = "STUDENT" }
                        .then(
                            if (selectedRole == "STUDENT") {
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
fun LoginScreen(
    viewModel: GradilyViewModel,
    role: String,
    onNavigateBack: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val roleLabel = if (role == "LECTURER") "Lecturer" else "Student"

    GradilyBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.2f))

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
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
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
    val context = LocalContext.current

    val roleLabel = if (role == "LECTURER") "Lecturer" else "Student"

    GradilyBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.15f))

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
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
                )
            }

            Spacer(modifier = Modifier.weight(0.3f))

            TextButton(onClick = onNavigateBack) {
                Text("← Back to role selection", color = TextMuted, fontSize = 14.sp)
            }
        }
    }
}
