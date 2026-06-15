package com.example.myapplication.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.GradilyViewModel
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*

const val WEB_CLIENT_ID = "667003870196-5r1qao1v97f20urlrptluuko8e78s7h1.apps.googleusercontent.com"

@Composable
fun FloatingLogo(icon: ImageVector = Icons.Default.School) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_float")
    val yOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_float_offset"
    )
    
    Box(
        modifier = Modifier
            .padding(bottom = 24.dp)
            .offset(y = yOffset.dp)
            .size(80.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        GradilyTheme.colors.lightGreen.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Logo",
            tint = GradilyTheme.colors.lightGreen,
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
fun MainAuthScreen(
    onNavigateToLogin: (String) -> Unit,
    onNavigateToSignUp: (String) -> Unit
) {
    var selectedRole by remember { mutableStateOf<String?>(null) }
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

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

            FloatingLogo()
            Text(
                "Gradily",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = GradilyTheme.colors.textPrimary,
                letterSpacing = 1.sp
            )
            Text(
                "Grade Management System",
                fontSize = 14.sp,
                color = GradilyTheme.colors.textSecondary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { it / 3 }) + fadeIn(tween(800))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Select Your Role",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = GradilyTheme.colors.textSecondary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val isLecturer = selectedRole == "LECTURER"
                        val lecturerScale by animateFloatAsState(if (isLecturer) 1.05f else 1f, label = "scale")
                        
                        GlassCard(
                            modifier = Modifier
                                .weight(1f)
                                .scale(lecturerScale)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedRole = "LECTURER" }
                                .then(
                                    if (isLecturer) {
                                        Modifier.background(
                                            Brush.linearGradient(listOf(GradilyTheme.colors.surfaceGreen, GradilyTheme.colors.mediumGreen)),
                                            RoundedCornerShape(20.dp)
                                        )
                                    } else Modifier
                                )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                            ) {
                                Icon(
                                    Icons.Default.School, 
                                    contentDescription = "Lecturer",
                                    tint = if (isLecturer) Color.White else GradilyTheme.colors.textPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Lecturer",
                                    color = if (isLecturer) Color.White else GradilyTheme.colors.textPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        val isStudent = selectedRole == "STUDENT"
                        val studentScale by animateFloatAsState(if (isStudent) 1.05f else 1f, label = "scale")

                        GlassCard(
                            modifier = Modifier
                                .weight(1f)
                                .scale(studentScale)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedRole = "STUDENT" }
                                .then(
                                    if (isStudent) {
                                        Modifier.background(
                                            Brush.linearGradient(listOf(GradilyTheme.colors.accentBlue, GradilyTheme.colors.accentPurple)),
                                            RoundedCornerShape(20.dp)
                                        )
                                    } else Modifier
                                )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Person, 
                                    contentDescription = "Student",
                                    tint = if (isStudent) Color.White else GradilyTheme.colors.textPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Student",
                                    color = if (isStudent) Color.White else GradilyTheme.colors.textPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    AnimatedVisibility(
                        visible = selectedRole != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
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
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
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
                CircularProgressIndicator(color = GradilyTheme.colors.darkGreen, modifier = Modifier.size(24.dp))
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
        Box(modifier = Modifier.weight(1f).height(1.dp).background(GradilyTheme.colors.glassBorder))
        Text("  OR  ", color = GradilyTheme.colors.textMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Box(modifier = Modifier.weight(1f).height(1.dp).background(GradilyTheme.colors.glassBorder))
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
    val roleIcon = if (role == "LECTURER") Icons.Default.School else Icons.Default.Person
    
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    GradilyBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.15f))

            FloatingLogo(roleIcon)
            SectionHeader("Welcome Back")
            SectionSubtitle("Sign in as $roleLabel")

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(tween(600))
            ) {
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
            }

            Spacer(modifier = Modifier.weight(0.3f))

            TextButton(onClick = onNavigateBack) {
                Text("← Back to role selection", color = GradilyTheme.colors.textMuted, fontSize = 14.sp)
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
    val roleIcon = if (role == "LECTURER") Icons.Default.School else Icons.Default.Person

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    GradilyBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.1f))

            FloatingLogo(roleIcon)
            SectionHeader("Create Account")
            SectionSubtitle("Register as $roleLabel")

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(tween(600))
            ) {
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
                                return@GradilyButton
                            }
                            isLoading = true
                            viewModel.signUp(email, password, role) { success, msg ->
                                isLoading = false
                                if (success) {
                                    onSignUpSuccess()
                                } else {
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
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
                                        onSignUpSuccess()
                                    } else {
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.3f))

            TextButton(onClick = onNavigateBack) {
                Text("← Back to login", color = GradilyTheme.colors.textMuted, fontSize = 14.sp)
            }
        }
    }
}
