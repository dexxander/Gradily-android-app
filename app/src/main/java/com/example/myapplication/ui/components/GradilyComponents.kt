package com.example.myapplication.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.draw.blur

/**
 * Full-screen animated background.
 * Uses fillMaxSize to cover the entire window including system bars.
 */
@Composable
fun GradilyBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
    val haptic = LocalHapticFeedback.current
    val colors = GradilyTheme.colors
    
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )
    
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )
    
    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase3"
    )

    val rippleRadius = remember { Animatable(0f) }
    val rippleAlpha = remember { Animatable(0f) }
    var rippleCenter by remember { mutableStateOf(Offset.Zero) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GradilyTheme.colors.darkBackground)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    rippleCenter = offset
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    coroutineScope.launch {
                        rippleAlpha.snapTo(1f)
                        rippleRadius.snapTo(0f)
                        launch {
                            rippleRadius.animateTo(
                                targetValue = 1500f,
                                animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
                            )
                        }
                        launch {
                            rippleAlpha.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
                            )
                        }
                    }
                }
            }
    ) {
        // Animated gradient blobs with a heavy blur to create a "Mesh Gradient / Aurora" effect
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp) // The magic for Aurora effect
        ) {
            val w = size.width
            val h = size.height
            
            val cx = w / 2f
            val cy = h / 2f
            
            val r1 = w * 0.85f
            val r2 = w * 0.95f
            val r3 = w * 0.75f
            
            // Blob 1: Green
            val x1 = cx + Math.cos(phase1.toDouble()).toFloat() * (w * 0.4f)
            val y1 = cy + Math.sin(phase1.toDouble()).toFloat() * (h * 0.3f)
            drawCircle(color = colors.lightGreen.copy(alpha = 0.4f), radius = r1, center = Offset(x1, y1))
            
            // Blob 2: Blue
            val x2 = cx + Math.sin(phase2.toDouble()).toFloat() * (w * 0.3f)
            val y2 = cy + Math.cos(phase2.toDouble()).toFloat() * (h * 0.4f)
            drawCircle(color = colors.accentBlue.copy(alpha = 0.35f), radius = r2, center = Offset(x2, y2))
            
            // Blob 3: Purple (new)
            val x3 = cx + Math.cos(phase3.toDouble() + Math.PI).toFloat() * (w * 0.35f)
            val y3 = cy + Math.sin(phase3.toDouble() + Math.PI).toFloat() * (h * 0.25f)
            drawCircle(color = colors.accentPurple.copy(alpha = 0.3f), radius = r3, center = Offset(x3, y3))
        }

        // Ripple layer (unblurred)
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            if (rippleAlpha.value > 0f) {
                drawCircle(
                    color = colors.accentPurple.copy(alpha = rippleAlpha.value * 0.4f),
                    radius = rippleRadius.value,
                    center = rippleCenter
                )
                drawCircle(
                    color = colors.textPrimary.copy(alpha = rippleAlpha.value * 0.6f),
                    radius = rippleRadius.value * 0.8f,
                    center = rippleCenter,
                    style = Stroke(width = 8f)
                )
            }
        }
        
        // Subtle overlay to keep text readable without hiding the animation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x33000000), // 20% black
                            Color(0x66000000)  // 40% black
                        )
                    )
                )
        )
        content()
    }
}

/**
 * Frosted glass card effect with subtle moving gradient animation.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glass_shimmer")
    val translateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glass_translate"
    )

    val animatedBrush = Brush.linearGradient(
        colors = listOf(GradilyTheme.colors.glassBg, GradilyTheme.colors.glassBgDark, GradilyTheme.colors.glassBg),
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 400f, translateAnim + 400f)
    )

    Card(
        modifier = modifier
            .animateContentSize()
            .border(1.dp, GradilyTheme.colors.glassBorder, RoundedCornerShape(cornerRadius)),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(animatedBrush)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                content = content
            )
        }
    }
}

/**
 * Styled text field matching the green glass theme.
 */
@Composable
fun GradilyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = GradilyTheme.colors.textSecondary) },
        visualTransformation = if (isPassword) {
            androidx.compose.ui.text.input.PasswordVisualTransformation()
        } else {
            androidx.compose.ui.text.input.VisualTransformation.None
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GradilyTheme.colors.lightGreen,
            unfocusedBorderColor = GradilyTheme.colors.glassBorder,
            focusedTextColor = GradilyTheme.colors.textPrimary,
            unfocusedTextColor = GradilyTheme.colors.textPrimary,
            cursorColor = GradilyTheme.colors.lightGreen,
            focusedContainerColor = GradilyTheme.colors.glassBgDark,
            unfocusedContainerColor = GradilyTheme.colors.glassBgDark,
            focusedLabelColor = GradilyTheme.colors.lightGreen,
            unfocusedLabelColor = GradilyTheme.colors.textSecondary
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        singleLine = true
    )
}

/**
 * Primary green gradient button.
 */
@Composable
fun GradilyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (enabled) Modifier.background(
                        Brush.horizontalGradient(listOf(GradilyTheme.colors.mediumGreen, GradilyTheme.colors.lightGreen))
                    ) else Modifier.background(GradilyTheme.colors.surfaceGreen.copy(alpha = 0.5f))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (enabled) GradilyTheme.colors.textPrimary else GradilyTheme.colors.textMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Secondary outline button.
 */
@Composable
fun GradilyOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val colors = GradilyTheme.colors
    OutlinedButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = GradilyTheme.colors.lightGreen
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.horizontalGradient(listOf(GradilyTheme.colors.mediumGreen, GradilyTheme.colors.lightGreen))
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Profile avatar circle with optional image.
 */
@Composable
fun ProfileAvatar(
    profilePicUri: String?,
    size: Dp = 80.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(listOf(GradilyTheme.colors.lightGreen, GradilyTheme.colors.accentGreen))
            )
            .border(2.dp, GradilyTheme.colors.glassBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (profilePicUri != null) {
            // Will use Coil for image loading - handled by the caller
            // This is the fallback
            Text("👤", fontSize = (size.value * 0.4).sp)
        } else {
            Text("👤", fontSize = (size.value * 0.4).sp)
        }
    }
}

/**
 * Section header text.
 */
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        color = GradilyTheme.colors.textPrimary,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(bottom = 8.dp)
    )
}

/**
 * Subtitle text.
 */
@Composable
fun SectionSubtitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = GradilyTheme.colors.textSecondary,
        fontSize = 14.sp,
        modifier = modifier.padding(bottom = 16.dp)
    )
}

/**
 * Grade Analytics Bar Chart — Canvas-based horizontal bar chart
 * showing individual assessment scores with animated fills.
 */
@Composable
fun GradeBarChart(
    labels: List<String>,
    values: List<Float>,
    maxValues: List<Float>,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(values) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(800, easing = LinearOutSlowInEasing))
    }

    val barColors = listOf(
        GradilyTheme.colors.accentBlue,
        GradilyTheme.colors.accentGreen,
        GradilyTheme.colors.accentPurple,
        GradilyTheme.colors.accentAmber,
        GradilyTheme.colors.lightGreen,
        GradilyTheme.colors.accentRed
    )

    Column(modifier = modifier.fillMaxWidth()) {
        labels.forEachIndexed { i, label ->
            val maxVal = if (i < maxValues.size) maxValues[i] else 100f
            val value = if (i < values.size) values[i] else 0f
            val ratio = if (maxVal > 0) (value / maxVal).coerceIn(0f, 1f) else 0f
            val color = barColors[i % barColors.size]

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    label,
                    color = GradilyTheme.colors.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.width(80.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GradilyTheme.colors.glassBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(ratio * animProgress.value)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(color.copy(alpha = 0.7f), color)
                                )
                            )
                    )
                }
                Text(
                    "${value.toInt()}/${maxVal.toInt()}",
                    color = GradilyTheme.colors.textMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.width(50.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }
    }
}

/**
 * Leaderboard entry row.
 */
@Composable
fun LeaderboardEntry(
    rank: Int,
    name: String,
    gpa: Double,
    isCurrentUser: Boolean = false,
    modifier: Modifier = Modifier
) {
    val medalEmoji = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "#$rank"
    }

    val bgColor = if (isCurrentUser) GradilyTheme.colors.accentBlue.copy(alpha = 0.12f)
                  else Color.Transparent

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                medalEmoji,
                fontSize = if (rank <= 3) 20.sp else 14.sp,
                modifier = Modifier.width(36.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    name,
                    color = if (isCurrentUser) GradilyTheme.colors.accentBlue else GradilyTheme.colors.textPrimary,
                    fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    when {
                        gpa >= 3.5 -> GradilyTheme.colors.accentGreen.copy(alpha = 0.15f)
                        gpa >= 2.5 -> GradilyTheme.colors.accentAmber.copy(alpha = 0.15f)
                        else -> GradilyTheme.colors.accentRed.copy(alpha = 0.15f)
                    }
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                String.format("%.2f", gpa),
                color = when {
                    gpa >= 3.5 -> GradilyTheme.colors.accentGreen
                    gpa >= 2.5 -> GradilyTheme.colors.accentAmber
                    else -> GradilyTheme.colors.accentRed
                },
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
