package com.nordicwalk.feature.video.presentation

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import com.nordicwalk.feature.video.domain.model.Landmark
import com.nordicwalk.feature.video.domain.model.PoseAnalysisResult
import com.nordicwalk.feature.video.domain.model.PostureIssue
import kotlin.math.abs

/**
 * 姿勢分析器
 * 使用 MediaPipe 進行姿勢檢測
 */
class PoseAnalyzer(private val context: Context) {
    private var poseLandmarker: PoseLandmarker? = null
    
    companion object {
        private const val TAG = "PoseAnalyzer"
        private const val MODEL_NAME = "pose_landmarker_lite.task"
    }

    init {
        initializePoseLandmarker()
    }

    /**
     * 初始化 PoseLandmarker
     */
    private fun initializePoseLandmarker() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_NAME)
                .build()
            
            val options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(com.google.mediapipe.tasks.vision.core.RunningMode.IMAGE)
                .setNumPoses(1)
                .setMinPoseDetectionConfidence(0.5f)
                .setMinPosePresenceConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .build()
            
            poseLandmarker = PoseLandmarker.createFromOptions(context, options)
            Log.d(TAG, "PoseLandmarker 初始化成功")
        } catch (e: Exception) {
            Log.e(TAG, "PoseLandmarker 初始化失敗", e)
        }
    }

    /**
     * 檢測姿勢
     */
    fun analyzePose(
        bitmap: Bitmap,
        frameIndex: Int = 0,
        trainingRecordId: Long = 0
    ): PoseAnalysisResult {
        return try {
            if (poseLandmarker == null) {
                Log.w(TAG, "PoseLandmarker 未初始化")
                return PoseAnalysisResult.empty(trainingRecordId)
            }

            // 轉換 Bitmap 為 MediaPipe Image
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = poseLandmarker?.detect(mpImage) ?: return PoseAnalysisResult.empty(trainingRecordId)

            // 提取關節點
            val landmarks = extractLandmarks(result, bitmap.width, bitmap.height)
            
            // 計算姿勢評估分數
            val postureScore = calculatePostureScore(landmarks)
            val balanceScore = calculateBalanceScore(landmarks)
            val armSwingScore = calculateArmSwingScore(landmarks)
            
            // 偵測問題
            val issues = detectPostureIssues(landmarks, postureScore, balanceScore, armSwingScore)
            
            // 產生建議
            val recommendations = generateRecommendations(issues)

            PoseAnalysisResult(
                trainingRecordId = trainingRecordId,
                landmarks = landmarks,
                postureScore = postureScore,
                balanceScore = balanceScore,
                armSwingScore = armSwingScore,
                issues = issues,
                recommendations = recommendations,
                frameIndex = frameIndex
            )
        } catch (e: Exception) {
            Log.e(TAG, "姿勢分析失敗", e)
            PoseAnalysisResult.empty(trainingRecordId)
        }
    }

    /**
     * 提取關節點
     */
    private fun extractLandmarks(
        result: PoseLandmarkerResult,
        width: Int,
        height: Int
    ): List<Landmark> {
        if (result.landmarks().isEmpty()) return emptyList()

        val poseLandmarks = result.landmarks()[0]
        return poseLandmarks.mapIndexed { index, landmark ->
            Landmark(
                name = getLandmarkName(index),
                x = landmark.x(),
                y = landmark.y(),
                z = landmark.z(),
                visibility = 1f  // 預設可見度為 1.0
            )
        }
    }

    /**
     * 取得關節名稱
     */
    private fun getLandmarkName(index: Int): String = when (index) {
        0 -> "nose"
        1 -> "left_eye_inner"
        2 -> "left_eye"
        3 -> "left_eye_outer"
        4 -> "right_eye_inner"
        5 -> "right_eye"
        6 -> "right_eye_outer"
        7 -> "left_ear"
        8 -> "right_ear"
        9 -> "mouth_left"
        10 -> "mouth_right"
        11 -> "left_shoulder"
        12 -> "right_shoulder"
        13 -> "left_elbow"
        14 -> "right_elbow"
        15 -> "left_wrist"
        16 -> "right_wrist"
        17 -> "left_pinky"
        18 -> "right_pinky"
        19 -> "left_index"
        20 -> "right_index"
        21 -> "left_thumb"
        22 -> "right_thumb"
        23 -> "left_hip"
        24 -> "right_hip"
        25 -> "left_knee"
        26 -> "right_knee"
        27 -> "left_ankle"
        28 -> "right_ankle"
        29 -> "left_heel"
        30 -> "right_heel"
        31 -> "left_foot_index"
        32 -> "right_foot_index"
        else -> "unknown_$index"
    }

    /**
     * 計算姿勢分數 (0-100)
     */
    private fun calculatePostureScore(landmarks: List<Landmark>): Float {
        if (landmarks.isEmpty()) return 0f
        
        val shoulderLandmarks = landmarks.filter { it.name in listOf("left_shoulder", "right_shoulder") }
        val hipLandmarks = landmarks.filter { it.name in listOf("left_hip", "right_hip") }
        
        var score = 85f
        
        if (shoulderLandmarks.size == 2) {
            val heightDiff = abs(shoulderLandmarks[0].y - shoulderLandmarks[1].y)
            if (heightDiff > 0.1f) score -= 10f
        }
        
        if (hipLandmarks.size == 2) {
            val heightDiff = abs(hipLandmarks[0].y - hipLandmarks[1].y)
            if (heightDiff > 0.1f) score -= 10f
        }
        
        return score.coerceIn(0f, 100f)
    }

    /**
     * 計算平衡分數
     */
    private fun calculateBalanceScore(landmarks: List<Landmark>): Float {
        if (landmarks.isEmpty()) return 0f
        
        val leftHip = landmarks.find { it.name == "left_hip" }
        val rightHip = landmarks.find { it.name == "right_hip" }
        val leftAnkle = landmarks.find { it.name == "left_ankle" }
        val rightAnkle = landmarks.find { it.name == "right_ankle" }
        
        if (leftHip == null || rightHip == null || leftAnkle == null || rightAnkle == null) {
            return 85f
        }
        
        val hipDist = abs(leftHip.x - rightHip.x)
        val ankleDist = abs(leftAnkle.x - rightAnkle.x)
        val balance = if (hipDist > 0) (ankleDist / hipDist) else 1f
        
        var score = 90f
        if (balance < 0.8f || balance > 1.2f) {
            score -= 20f
        }
        
        return score.coerceIn(0f, 100f)
    }

    /**
     * 計算手臂擺動分數
     */
    private fun calculateArmSwingScore(landmarks: List<Landmark>): Float {
        if (landmarks.isEmpty()) return 0f
        
        val leftShoulder = landmarks.find { it.name == "left_shoulder" }
        val rightShoulder = landmarks.find { it.name == "right_shoulder" }
        val leftElbow = landmarks.find { it.name == "left_elbow" }
        val rightElbow = landmarks.find { it.name == "right_elbow" }
        
        if (leftShoulder == null || rightShoulder == null || leftElbow == null || rightElbow == null) {
            return 85f
        }
        
        val leftSwing = abs(leftElbow.y - leftShoulder.y)
        val rightSwing = abs(rightElbow.y - rightShoulder.y)
        
        var score = 85f
        
        if (leftSwing < 0.1f || rightSwing < 0.1f) {
            score -= 20f
        }
        
        if (abs(leftSwing - rightSwing) > 0.15f) {
            score -= 15f
        }
        
        return score.coerceIn(0f, 100f)
    }

    /**
     * 偵測姿勢問題
     */
    private fun detectPostureIssues(
        landmarks: List<Landmark>,
        postureScore: Float,
        balanceScore: Float,
        armSwingScore: Float
    ): List<PostureIssue> {
        val issues = mutableListOf<PostureIssue>()
        
        if (postureScore < 70f) {
            issues.add(PostureIssue.UNEVEN_SHOULDERS)
        }
        
        if (balanceScore < 70f) {
            issues.add(PostureIssue.UNEVEN_HIPS)
        }
        
        if (armSwingScore < 70f) {
            issues.add(PostureIssue.INSUFFICIENT_ARM_SWING)
        }
        
        return issues
    }

    /**
     * 產生建議
     */
    private fun generateRecommendations(issues: List<PostureIssue>): List<String> {
        return issues.map { issue ->
            when (issue) {
                PostureIssue.UNEVEN_SHOULDERS -> "請保持肩膀水平，收緊核心放鬆肩膀"
                PostureIssue.INSUFFICIENT_ARM_SWING -> "增大手臂擺動幅度，從肩膀帶動手臂"
                PostureIssue.UNEVEN_HIPS -> "保持骸部水平，不要左右搖擺過大"
                else -> "改善${issue.description}"
            }
        }
    }

    /**
     * 關閉 PoseLandmarker
     */
    fun close() {
        poseLandmarker?.close()
        poseLandmarker = null
    }
}
