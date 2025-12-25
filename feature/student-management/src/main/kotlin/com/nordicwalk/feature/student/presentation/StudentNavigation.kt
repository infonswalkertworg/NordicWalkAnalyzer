package com.nordicwalk.feature.student.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

object StudentRoutes {
    const val LIST = "student_list"
    const val FORM = "student_form"
    const val FORM_WITH_ID = "student_form/{studentId}"
    const val DETAIL = "student_detail/{studentId}"
}

fun NavGraphBuilder.studentGraph(
    navController: NavController,
    onNavigateToAnalysis: (Long) -> Unit
) {
    composable(StudentRoutes.LIST) {
        StudentListScreen(
            onNavigateToForm = { navController.navigate(StudentRoutes.FORM) },
            onNavigateToDetail = { studentId ->
                navController.navigate("student_detail/$studentId")
            }
        )
    }

    composable(StudentRoutes.FORM) {
        StudentFormScreen(
            onNavigateBack = { navController.navigateUp() }
        )
    }

    composable(
        StudentRoutes.FORM_WITH_ID,
        arguments = listOf(navArgument("studentId") { type = NavType.LongType })
    ) {
        StudentFormScreen(
            onNavigateBack = { navController.navigateUp() }
        )
    }

    composable(
        StudentRoutes.DETAIL,
        arguments = listOf(navArgument("studentId") { type = NavType.LongType })
    ) {
        StudentDetailScreen(
            onNavigateBack = { navController.navigateUp() },
            onNavigateToEdit = { studentId ->
                navController.navigate("student_form/$studentId")
            },
            onNavigateToAnalysis = onNavigateToAnalysis
        )
    }
}
