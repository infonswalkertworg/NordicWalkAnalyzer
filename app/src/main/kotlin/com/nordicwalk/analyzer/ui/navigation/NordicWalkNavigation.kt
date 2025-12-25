package com.nordicwalk.analyzer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

object StudentRoute {
    const val LIST = "student_list"
    const val FORM = "student_form"
    const val FORM_WITH_ID = "student_form/{studentId}"
    const val DETAIL = "student_detail/{studentId}"
    const val TRAINING_FORM = "training_form/{studentId}"
    const val TRAINING_FORM_WITH_ID = "training_form/{studentId}/{recordId}"
    const val TRAINING_DETAIL = "training_detail/{recordId}"
}

object AnalysisRoute {
    const val HOME = "analysis_home"
    const val CAMERA = "analysis_camera/{studentId}"
    const val VIDEO_IMPORT = "analysis_video_import/{studentId}"
    const val PLAYBACK = "analysis_playback/{sessionId}"
    const val RESULT = "analysis_result/{sessionId}"
}

@Composable
fun NordicWalkNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = StudentRoute.LIST
    ) {
        // Student Management Routes
        composable(StudentRoute.LIST) {
            StudentListPlaceholder {
                if (it > 0) {
                    navController.navigate("${StudentRoute.DETAIL.replace("{studentId}", it.toString())}")
                } else {
                    navController.navigate(StudentRoute.FORM)
                }
            }
        }

        composable(StudentRoute.FORM) {
            StudentFormPlaceholder(
                onNavigateBack = { navController.navigateUp() },
                isEditing = false
            )
        }

        composable(
            StudentRoute.FORM_WITH_ID,
            arguments = listOf(navArgument("studentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L
            StudentFormPlaceholder(
                onNavigateBack = { navController.navigateUp() },
                isEditing = true,
                studentId = studentId
            )
        }

        composable(
            StudentRoute.DETAIL,
            arguments = listOf(navArgument("studentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L
            StudentDetailPlaceholder(
                studentId = studentId,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToForm = { navController.navigate("${StudentRoute.FORM_WITH_ID.replace("{studentId}", it.toString())}") },
                onNavigateToTrainingForm = { recId ->
                    if (recId > 0) {
                        navController.navigate("training_form/$studentId/$recId")
                    } else {
                        navController.navigate("training_form/$studentId")
                    }
                },
                onNavigateToAnalysis = { navController.navigate("${AnalysisRoute.CAMERA.replace("{studentId}", studentId.toString())}") }
            )
        }

        // Analysis Routes
        composable(AnalysisRoute.HOME) {
            AnalysisHomePlaceholder(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(
            AnalysisRoute.CAMERA,
            arguments = listOf(navArgument("studentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L
            CameraAnalysisPlaceholder(
                studentId = studentId,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToResult = { sessionId ->
                    navController.navigate("${AnalysisRoute.RESULT.replace("{sessionId}", sessionId.toString())}")
                }
            )
        }

        composable(
            AnalysisRoute.RESULT,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            AnalysisResultPlaceholder(
                sessionId = sessionId,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}
