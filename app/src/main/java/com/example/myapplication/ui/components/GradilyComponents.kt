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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.*

/**
 * Full-screen background with the gradient image overlay.
 * Uses fillMaxSize to cover the entire window including system bars.
 */
@Composable
fun GradilyBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Background image - fills entire screen
        Image(
            painter = painterResource(id = R.drawable.app_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Dark overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC0A0F0D),
                            Color(0xE60A0F0D)
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
        colors = listOf(GlassBg, GlassBgDark, GlassBg),
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 400f, translateAnim + 400f)
    )

    Card(
        modifier = modifier
            .animateContentSize()
            .border(1.dp, GlassBorder, RoundedCornerShape(cornerRadius)),
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
        label = { Text(label, color = TextSecondary) },
        visualTransformation = if (isPassword) {
            androidx.compose.ui.text.input.PasswordVisualTransformation()
        } else {
            androidx.compose.ui.text.input.VisualTransformation.None
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LightGreen,
            unfocusedBorderColor = GlassBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = LightGreen,
            focusedContainerColor = GlassBgDark,
            unfocusedContainerColor = GlassBgDark,
            focusedLabelColor = LightGreen,
            unfocusedLabelColor = TextSecondary
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
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = LightGreen,
            disabledContainerColor = SurfaceGreen
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Text(
            text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
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
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
            brush = Brush.linearGradient(listOf(LightGreen, AccentGreen))
        )
    ) {
        Text(
            text,
            color = LightGreen,
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
                Brush.linearGradient(listOf(LightGreen, AccentGreen))
            )
            .border(2.dp, GlassBorder, CircleShape),
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
        color = TextPrimary,
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
        color = TextSecondary,
        fontSize = 14.sp,
        modifier = modifier.padding(bottom = 16.dp)
    )
}
