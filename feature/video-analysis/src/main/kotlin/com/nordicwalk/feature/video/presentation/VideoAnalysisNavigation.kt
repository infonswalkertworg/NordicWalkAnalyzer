package com.nordicwalk.feature.video.presentation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType

/**
 * 視頻分析功能的導航路線
 */
object VideoAnalysisDestinations {
    const val RECORDING_ROUTE = "video_recording"
    const val ANALYSIS_ROUTE = "video_analysis"
    const val ANALYSIS_ROUTE_WITH_VIDEO = "video_analysis/{encodedVideoPath}"
}

/**
 * 導航最頂層路線
 */
fun NavGraphBuilder.videoAnalysisGraph(
    navController: NavController,
    onBackClick: () -> Unit,
    onSaveTrainingRecord: ((videoPath: String, summary: com.nordicwalk.feature.video.domain.model.AnalysisSummary) -> Unit)? = null
) {
    // 影片錄製畫面
    composable(VideoAnalysisDestinations.RECORDING_ROUTE) {
        VideoRecordingScreen(
            onVideoRecorded = { videoPath ->
                // URL 編碼路徑以避免 / 字符問題
                val encodedPath = Uri.encode(videoPath)
                // 錄製完成後，導航到分析畫面
                navController.navigate("video_analysis/$encodedPath") {
                    // 不移除錄影畫面，允許返回再次錄影
                    launchSingleTop = true
                }
            }
        )
    }

    // 動作分析畫面
    composable(
        route = VideoAnalysisDestinations.ANALYSIS_ROUTE_WITH_VIDEO,
        arguments = listOf(
            navArgument("encodedVideoPath") {
                type = NavType.StringType
                nullable = false
            }
        )
    ) { backStackEntry ->
        val encodedPath = backStackEntry.arguments?.getString("encodedVideoPath")
        // URL 解碼路徑
        val videoPath = encodedPath?.let { Uri.decode(it) }
        
        VideoAnalysisScreen(
            videoPath = videoPath,
            onBack = {
                // 返回時，清除所有分析和錄影畫面，直接回到學員詳情
                navController.popBackStack(
                    route = VideoAnalysisDestinations.RECORDING_ROUTE,
                    inclusive = true  // 也移除錄影畫面
                )
            },
            onAnalysisComplete = { path, summary ->
                // 分析完成時保存訓練記錄
                onSaveTrainingRecord?.invoke(path, summary)
            }
        )
    }
}

/**
 * 從主畫面導航到影片錄製畫面
 */
fun NavController.navigateToVideoRecording() {
    this.navigate(VideoAnalysisDestinations.RECORDING_ROUTE) {
        launchSingleTop = true
    }
}

/**
 * 從主畫面導航到影片分析畫面
 */
fun NavController.navigateToVideoAnalysis(videoPath: String) {
    val encodedPath = Uri.encode(videoPath)
    this.navigate("video_analysis/$encodedPath") {
        launchSingleTop = true
    }
}
