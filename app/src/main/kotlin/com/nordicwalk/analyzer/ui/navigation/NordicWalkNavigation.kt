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
import com.nordicwalk.feature.student.presentation.studentGraph
import com.nordicwalk.feature.video.presentation.videoAnalysisGraph

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
        // Integrate student management feature
        studentGraph(
            navController = navController,
            onNavigateToAnalysis = { studentId ->
                // Navigate to video recording
                navController.navigate("video_recording")
            }
        )

        // Video Analysis Feature
        videoAnalysisGraph(
            navController = navController,
            onBackClick = {
                navController.navigateUp()
            }
        )

        // Legacy Analysis Routes (placeholders for now)
        composable(AnalysisRoute.HOME) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("分析主頁 (Coming Soon)")
                    Button(onClick = { navController.navigateUp() }) {
                        Text("返回")
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
                    Text("攝像頭分析 (Coming Soon)")
                    Button(onClick = { navController.navigateUp() }) {
                        Text("返回")
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
                    Text("分析結果 (Coming Soon)")
                    Button(onClick = { navController.navigateUp() }) {
                        Text("返回")
                    }
                }
            }
        }
    }
}
