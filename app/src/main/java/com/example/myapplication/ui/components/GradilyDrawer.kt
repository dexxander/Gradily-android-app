package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.data.User
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun GradilyDrawer(
    drawerState: DrawerState,
    user: User?,
    onNavigateHome: () -> Unit,
    onNavigateProfile: () -> Unit,
    onNavigateSettings: () -> Unit,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = GradilyTheme.colors.darkGreen,
                drawerContentColor = GradilyTheme.colors.textPrimary,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Brush.verticalGradient(listOf(GradilyTheme.colors.darkBackground, GradilyTheme.colors.darkGreen)))
                            .padding(24.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(GradilyTheme.colors.surfaceGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                if (user?.profilePicUri != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(user.profilePicUri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Profile",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text("👤", fontSize = 32.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                user?.email ?: "Guest",
                                color = GradilyTheme.colors.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                user?.role?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "User",
                                color = GradilyTheme.colors.lightGreen,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    DrawerItem(
                        icon = Icons.Default.Home,
                        label = "Home",
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateHome()
                        }
                    )

                    DrawerItem(
                        icon = Icons.Default.Person,
                        label = "Profile",
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateProfile()
                        }
                    )

                    DrawerItem(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateSettings()
                        }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    HorizontalDivider(color = GradilyTheme.colors.glassBorder, modifier = Modifier.padding(horizontal = 24.dp))
                    
                    DrawerItem(
                        icon = Icons.Default.ExitToApp,
                        label = "Logout",
                        onClick = {
                            scope.launch { drawerState.close() }
                            onLogout()
                        },
                        tint = GradilyTheme.colors.accentRed
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        },
        content = content
    )
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = GradilyTheme.colors.textPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 32.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, color = tint, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}
