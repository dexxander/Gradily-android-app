package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.myapplication.ui.theme.DarkBackground

class MainActivity : ComponentActivity() {
    private val viewModel: GradilyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
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

@Composable
fun GradilyApp(viewModel: GradilyViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main_auth",
        enterTransition = { slideInFromRight() },
        exitTransition = { slideOutToLeft() },
        popEnterTransition = { slideInFromLeft() },
        popExitTransition = { slideOutToRight() }
    ) {
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
                    navController.navigate("login/$role") {
                        popUpTo("main_auth")
                    }
                }
            )
        }

        // ====== LECTURER FLOW ======
        composable(
            "class_list",
            enterTransition = { fadeInSmooth() },
            exitTransition = { slideOutToLeft() }
        ) {
            ClassListScreen(
                viewModel = viewModel,
                onCreateClass = { navController.navigate("class_creation") },
                onManageClass = { navController.navigate("class_student_content") },
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
            enterTransition = { fadeInSmooth() },
            exitTransition = { slideOutToLeft() }
        ) {
            StudentDashboardScreen(
                viewModel = viewModel,
                onViewSubject = { student ->
                    viewModel.setCurrentStudent(student)
                    navController.navigate("student_subject_detail/${student.studentId}")
                },
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