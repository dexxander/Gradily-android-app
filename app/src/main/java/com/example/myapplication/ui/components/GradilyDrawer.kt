package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*

enum class BottomNavItem(
    val icon: ImageVector,
    val label: String
) {
    HOME(Icons.Default.Home, "Home"),
    PROFILE(Icons.Default.Person, "Profile"),
    SETTINGS(Icons.Default.Settings, "Settings"),
    LOGOUT(Icons.Default.ExitToApp, "Logout")
}

@Composable
fun GradilyBottomBar(
    currentItem: BottomNavItem,
    onNavigateHome: () -> Unit,
    onNavigateProfile: () -> Unit,
    onNavigateSettings: () -> Unit,
    onLogout: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                containerColor = GradilyTheme.colors.darkBackground.copy(alpha = 0.95f),
                tonalElevation = 0.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                GradilyTheme.colors.glassBgDark,
                                GradilyTheme.colors.darkBackground.copy(alpha = 0.95f)
                            )
                        )
                    )
            ) {
                BottomNavItem.entries.forEach { item ->
                    val selected = item == currentItem
                    val isLogout = item == BottomNavItem.LOGOUT
                    val tint = when {
                        isLogout -> GradilyTheme.colors.accentRed
                        selected -> GradilyTheme.colors.lightGreen
                        else -> GradilyTheme.colors.textMuted
                    }
                    NavigationBarItem(
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = item.label,
                                tint = tint
                            )
                        },
                        label = {
                            Text(
                                item.label,
                                color = tint,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        selected = selected,
                        onClick = {
                            when (item) {
                                BottomNavItem.HOME -> onNavigateHome()
                                BottomNavItem.PROFILE -> onNavigateProfile()
                                BottomNavItem.SETTINGS -> onNavigateSettings()
                                BottomNavItem.LOGOUT -> onLogout()
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = if (isLogout)
                                GradilyTheme.colors.accentRed.copy(alpha = 0.12f)
                            else
                                GradilyTheme.colors.lightGreen.copy(alpha = 0.15f),
                            selectedIconColor = Color.Unspecified,
                            unselectedIconColor = Color.Unspecified,
                            selectedTextColor = Color.Unspecified,
                            unselectedTextColor = Color.Unspecified
                        )
                    )
                }
            }
        },
        content = content
    )
}
