package com.nordicwalk.analyzer.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Student List (Coming Soon)")
                    Button(onClick = { navController.navigate(StudentRoute.FORM) }) {
                        Text("Add Student")
                    }
                }
            }
        }

        composable(StudentRoute.FORM) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Student Form (Coming Soon)")
                    Button(onClick = { navController.navigateUp() }) {
                        Text("Back")
                    }
                }
            }
        }

        composable(
            StudentRoute.FORM_WITH_ID,
            arguments = listOf(navArgument("studentId") { type = NavType.LongType })
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Edit Student Form (Coming Soon)")
                    Button(onClick = { navController.navigateUp() }) {
                        Text("Back")
                    }
                }
            }
        }

        composable(
            StudentRoute.DETAIL,
            arguments = listOf(navArgument("studentId") { type = NavType.LongType })
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Student Detail (Coming Soon)")
                    Button(onClick = { navController.navigateUp() }) {
                        Text("Back")
                    }
                }
            }
        }

        // Analysis Routes
        composable(AnalysisRoute.HOME) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Analysis Home (Coming Soon)")
                    Button(onClick = { navController.navigateUp() }) {
                        Text("Back")
                    }
                }
            }
        }

        composable(
            AnalysisRoute.CAMERA,
            arguments = listOf(navArgument("studentId") { type = NavType.LongType })
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Camera Analysis (Coming Soon)")
                    Button(onClick = { navController.navigateUp() }) {
                        Text("Back")
                    }
                }
            }
        }

        composable(
            AnalysisRoute.RESULT,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Analysis Result (Coming Soon)")
                    Button(onClick = { navController.navigateUp() }) {
                        Text("Back")
                    }
                }
            }
        }
    }
}
