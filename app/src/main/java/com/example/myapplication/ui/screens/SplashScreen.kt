package com.example.myapplication.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.GradilyViewModel
import com.example.myapplication.ui.components.GradilyBackground
import com.example.myapplication.ui.theme.GradilyTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: GradilyViewModel,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onNavigateToLecturer: () -> Unit,
    onNavigateToStudent: () -> Unit
) {
    val scale = remember { Animatable(0.5f) }
    val user by viewModel.currentUser.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 800)
        )
        delay(500) // Brief pause to show the logo

        val sharedPref = context.getSharedPreferences("gradily_prefs", android.content.Context.MODE_PRIVATE)
        val isFirstLaunch = sharedPref.getBoolean("is_first_launch", true)

        if (isFirstLaunch) {
            onNavigateToOnboarding()
        } else {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser == null) {
                onNavigateToAuth()
            } else {
                // Wait for user document to load if it hasn't already
            var waitCount = 0
            while (viewModel.currentUser.value == null && waitCount < 10) {
                delay(200)
                waitCount++
            }
            
            val loadedUser = viewModel.currentUser.value
            if (loadedUser != null) {
                if (loadedUser.role == "STUDENT") {
                    onNavigateToStudent()
                } else {
                    onNavigateToLecturer()
                }
            } else {
                // Fallback if document load fails
                FirebaseAuth.getInstance().signOut()
                onNavigateToAuth()
            }
            }
        }
    }

    GradilyBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.scale(scale.value)
            ) {
                Text("🎓", fontSize = 80.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Gradily",
                    color = GradilyTheme.colors.textPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Grade Management",
                    color = GradilyTheme.colors.textSecondary,
                    fontSize = 14.sp
                )
            }
        }
    }
}
