package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.GradilyBackground
import com.example.myapplication.ui.components.GlassCard
import com.example.myapplication.ui.theme.GradilyTheme

data class OnboardingSlide(
    val title: String,
    val description: String,
    val emoji: String
)

val onboardingSlides = listOf(
    OnboardingSlide(
        title = "Welcome to Gradily",
        description = "Your modern, elegant, and interactive academic companion.",
        emoji = "✨"
    ),
    OnboardingSlide(
        title = "Manage with Ease",
        description = "Lecturers can easily manage classes, grade students, and track attendance all in one place.",
        emoji = "📚"
    ),
    OnboardingSlide(
        title = "Track Your Success",
        description = "Students get deep insights into their performance, grade analytics, and leaderboard standings.",
        emoji = "📈"
    )
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    var currentSlide by remember { mutableStateOf(0) }

    GradilyBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Skip button
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onFinishOnboarding) {
                    Text("Skip", color = GradilyTheme.colors.textSecondary, fontSize = 16.sp)
                }
            }

            // Content
            AnimatedContent(
                targetState = currentSlide,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(animationSpec = tween(500)) { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally(animationSpec = tween(500)) { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally(animationSpec = tween(500)) { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally(animationSpec = tween(500)) { width -> width } + fadeOut()
                    }
                },
                label = "onboarding_animation"
            ) { slideIndex ->
                val slide = onboardingSlides[slideIndex]
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GlassCard(
                        modifier = Modifier
                            .size(200.dp)
                            .padding(16.dp),
                        cornerRadius = 100.dp
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(slide.emoji, fontSize = 80.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    Text(
                        text = slide.title,
                        color = GradilyTheme.colors.textPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = slide.description,
                        color = GradilyTheme.colors.textSecondary,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            // Bottom Navigation
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicators
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    repeat(onboardingSlides.size) { index ->
                        val isSelected = currentSlide == index
                        val width = if (isSelected) 24.dp else 8.dp
                        val color = if (isSelected) GradilyTheme.colors.accentBlue else GradilyTheme.colors.glassBorder
                        
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                // Next / Finish Button
                Button(
                    onClick = {
                        if (currentSlide < onboardingSlides.size - 1) {
                            currentSlide++
                        } else {
                            onFinishOnboarding()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GradilyTheme.colors.accentBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        if (currentSlide < onboardingSlides.size - 1) "Next" else "Get Started",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
