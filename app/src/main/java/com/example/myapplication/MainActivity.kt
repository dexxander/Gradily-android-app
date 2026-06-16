package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.Student
import com.example.myapplication.ui.screens.*
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GradilyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    GradilyApp(viewModel)
                }
            }
        }
    }
}

// Smooth transition helpers
private const val TRANSITION_DURATION = 400

private fun slideInFromRight(): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(TRANSITION_DURATION)
    ) + fadeIn(animationSpec = tween(TRANSITION_DURATION))

private fun slideOutToLeft(): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth / 3 },
        animationSpec = tween(TRANSITION_DURATION)
    ) + fadeOut(animationSpec = tween(TRANSITION_DURATION / 2))

private fun slideInFromLeft(): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(TRANSITION_DURATION)
    ) + fadeIn(animationSpec = tween(TRANSITION_DURATION))

private fun slideOutToRight(): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(TRANSITION_DURATION)
    ) + fadeOut(animationSpec = tween(TRANSITION_DURATION / 2))

private fun fadeInSmooth(): EnterTransition =
    fadeIn(animationSpec = tween(TRANSITION_DURATION)) +
    scaleIn(initialScale = 0.92f, animationSpec = tween(TRANSITION_DURATION))

private fun fadeOutSmooth(): ExitTransition =
    fadeOut(animationSpec = tween(TRANSITION_DURATION / 2)) +
    scaleOut(targetScale = 1.05f, animationSpec = tween(TRANSITION_DURATION / 2))

private val bottomBarRoutes = listOf("profile", "settings", "class_list", "student_dashboard")

private fun AnimatedContentTransitionScope<NavBackStackEntry>.bottomBarEnterTransition(): EnterTransition {
    return if (initialState.destination.route in bottomBarRoutes) {
        fadeIn(animationSpec = tween(300))
    } else {
        fadeInSmooth()
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.bottomBarExitTransition(): ExitTransition {
    return if (targetState.destination.route in bottomBarRoutes) {
        fadeOut(animationSpec = tween(300))
    } else {
        fadeOutSmooth()
    }
}

@Composable
fun GradilyApp(viewModel: GradilyViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash",
        enterTransition = { slideInFromRight() },
        exitTransition = { slideOutToLeft() },
        popEnterTransition = { slideInFromLeft() },
        popExitTransition = { slideOutToRight() }
    ) {
        composable(
            "splash",
            enterTransition = { fadeInSmooth() },
            exitTransition = { fadeOutSmooth() }
        ) {
            SplashScreen(
                viewModel = viewModel,
                onNavigateToOnboarding = {
                    navController.navigate("onboarding") { popUpTo("splash") { inclusive = true } }
                },
                onNavigateToAuth = {
                    navController.navigate("main_auth") { popUpTo("splash") { inclusive = true } }
                },
                onNavigateToLecturer = {
                    navController.navigate("class_list") { popUpTo("splash") { inclusive = true } }
                },
                onNavigateToStudent = {
                    navController.navigate("student_dashboard") { popUpTo("splash") { inclusive = true } }
                }
            )
        }

        composable(
            "onboarding",
            enterTransition = { slideInFromRight() },
            exitTransition = { fadeOutSmooth() }
        ) {
            val context = androidx.compose.ui.platform.LocalContext.current
            OnboardingScreen(
                onFinishOnboarding = {
                    val sharedPref = context.getSharedPreferences("gradily_prefs", android.content.Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putBoolean("is_first_launch", false)
                        apply()
                    }
                    navController.navigate("main_auth") { popUpTo("onboarding") { inclusive = true } }
                }
            )
        }

        composable(
            "main_auth",
            enterTransition = { fadeInSmooth() },
            exitTransition = { fadeOutSmooth() }
        ) {
            MainAuthScreen(
                onNavigateToLogin = { role -> navController.navigate("login/$role") },
                onNavigateToSignUp = { role -> navController.navigate("signup/$role") }
            )
        }

        composable("login/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "LECTURER"
            LoginScreen(
                viewModel = viewModel,
                role = role,
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = {
                    val destination = if (role == "STUDENT") "student_dashboard" else "class_list"
                    navController.navigate(destination) {
                        popUpTo("main_auth") { inclusive = true }
                    }
                }
            )
        }

        composable("signup/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "LECTURER"
            SignUpScreen(
                viewModel = viewModel,
                role = role,
                onNavigateBack = { navController.popBackStack() },
                onSignUpSuccess = {
                    val destination = if (role == "STUDENT") "student_dashboard" else "class_list"
                    navController.navigate(destination) {
                        popUpTo("main_auth") { inclusive = true }
                    }
                }
            )
        }

        // ====== MAIN DRAWER SCREENS ======
        composable(
            "profile",
            enterTransition = { bottomBarEnterTransition() },
            exitTransition = { bottomBarExitTransition() }
        ) {
            ProfileScreen(
                viewModel = viewModel,
                onNavigateHome = {
                    val role = viewModel.currentUser.value?.role
                    val destination = if (role == "STUDENT") "student_dashboard" else "class_list"
                    navController.navigate(destination) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateSettings = {
                    navController.navigate("settings") {
                        popUpTo("settings") { inclusive = true }
                    }
                },
                onLogout = {
                    viewModel.logout()
                    navController.navigate("main_auth") { popUpTo(0) }
                }
            )
        }

        composable(
            "settings",
            enterTransition = { bottomBarEnterTransition() },
            exitTransition = { bottomBarExitTransition() }
        ) {
            SettingsScreen(
                user = viewModel.currentUser.collectAsState().value,
                onNavigateHome = {
                    val role = viewModel.currentUser.value?.role
                    val destination = if (role == "STUDENT") "student_dashboard" else "class_list"
                    navController.navigate(destination) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateProfile = {
                    navController.navigate("profile") {
                        popUpTo("profile") { inclusive = true }
                    }
                },
                onLogout = {
                    viewModel.logout()
                    navController.navigate("main_auth") { popUpTo(0) }
                }
            )
        }

        // ====== LECTURER FLOW ======
        composable(
            "class_list",
            enterTransition = { bottomBarEnterTransition() },
            exitTransition = { bottomBarExitTransition() }
        ) {
            ClassListScreen(
                viewModel = viewModel,
                onCreateClass = { navController.navigate("class_creation") },
                onManageClass = { navController.navigate("class_student_content") },
                onNavigateProfile = { navController.navigate("profile") },
                onNavigateSettings = { navController.navigate("settings") },
                onLogout = {
                    viewModel.logout()
                    navController.navigate("main_auth") { popUpTo(0) }
                }
            )
        }

        composable("class_creation") {
            ClassCreationScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onHome = {
                    navController.navigate("class_list") {
                        popUpTo("class_list") { inclusive = true }
                    }
                }
            )
        }

        composable("class_student_content") {
            ClassStudentContentScreen(
                viewModel = viewModel,
                onAddStudent = { navController.navigate("student_enrollment") },
                onAssessStudent = { navController.navigate("student_grade") },
                onHome = {
                    navController.navigate("class_list") {
                        popUpTo("class_list") { inclusive = true }
                    }
                }
            )
        }

        composable("student_enrollment") {
            StudentEnrollmentScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onHome = {
                    navController.navigate("class_list") {
                        popUpTo("class_list") { inclusive = true }
                    }
                }
            )
        }

        composable("student_grade") {
            StudentGradeScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onHome = {
                    navController.navigate("class_list") {
                        popUpTo("class_list") { inclusive = true }
                    }
                }
            )
        }

        // ====== STUDENT FLOW ======
        composable(
            "student_dashboard",
            enterTransition = { bottomBarEnterTransition() },
            exitTransition = { bottomBarExitTransition() }
        ) {
            StudentDashboardScreen(
                viewModel = viewModel,
                onViewSubject = { student ->
                    viewModel.setCurrentStudent(student)
                    navController.navigate("student_subject_detail/${student.studentId}")
                },
                onNavigateProfile = { navController.navigate("profile") },
                onNavigateSettings = { navController.navigate("settings") },
                onLogout = {
                    viewModel.logout()
                    navController.navigate("main_auth") { popUpTo(0) }
                }
            )
        }

        composable("student_subject_detail/{studentId}") { backStackEntry ->
            val currentStudent = viewModel.currentStudent.collectAsState().value
            if (currentStudent != null) {
                StudentSubjectDetailScreen(
                    viewModel = viewModel,
                    student = currentStudent,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}