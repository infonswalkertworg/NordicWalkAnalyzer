package com.nordicwalk.feature.video.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType

/**
 * 視频分析功能的導航路緑
 */
object VideoAnalysisDestinations {
    const val RECORDING_ROUTE = "video_recording"
    const val ANALYSIS_ROUTE = "video_analysis"
    const val ANALYSIS_ROUTE_WITH_VIDEO = "video_analysis/{videoPath}"
}

/**
 * 導航最頂层路緑
 */
fun NavGraphBuilder.videoAnalysisGraph(
    navController: NavController,
    onBackClick: () -> Unit
) {
    // 影片錄製屏幕
    composable(VideoAnalysisDestinations.RECORDING_ROUTE) {
        VideoRecordingScreen(
            onVideoRecorded = { videoPath ->
                // 錄製完成後，導航到分析屏幕
                navController.navigate("video_analysis/$videoPath") {
                    popUpTo(VideoAnalysisDestinations.RECORDING_ROUTE)
                }
            }
        )
    }

    // 動作分析屏幕
    composable(
        route = VideoAnalysisDestinations.ANALYSIS_ROUTE_WITH_VIDEO,
        arguments = listOf(
            navArgument("videoPath") {
                type = NavType.StringType
                nullable = false
            }
        )
    ) { backStackEntry ->
        val videoPath = backStackEntry.arguments?.getString("videoPath")
        VideoAnalysisScreen(
            videoPath = videoPath,
            onBack = {
                navController.popBackStack()
            }
        )
    }
}

/**
 * 從主記憶體導航到影片錄製屏幕
 */
fun NavController.navigateToVideoRecording() {
    this.navigate(VideoAnalysisDestinations.RECORDING_ROUTE) {
        launchSingleTop = true
    }
}

/**
 * 從主記憶體導航到影片分析屏幕
 */
fun NavController.navigateToVideoAnalysis(videoPath: String) {
    this.navigate("video_analysis/$videoPath") {
        launchSingleTop = true
    }
}
