package com.nordicwalk.feature.video.presentation

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import com.nordicwalk.feature.video.domain.model.Landmark
import com.nordicwalk.feature.video.domain.model.PoseAnalysisResult
import com.nordicwalk.feature.video.domain.model.PostureIssue
import com.nordicwalk.feature.video.domain.model.AnalysisSummary

/**
 * 姿勢分析器
 * 使用 MediaPipe 進行險可樐棄
 */
class PoseAnalyzer(private val context: Context) {
    private var poseLandmarker: PoseLandmarker? = null
    
    companion object {
        private const val TAG = "PoseAnalyzer"
        private const val MODEL_NAME = "pose_landmarker.task"
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
                .setNumPoses(1)  // 只棄測一少人
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
     * 檢測紀的姿勢
     */
    fun analyzePose(
        bitmap: Bitmap,
        frameIndex: Int = 0,
        trainingRecordId: Long = 0
    ): PoseAnalysisResult {
        return try {
            if (poseLandmarker == null) {
                Log.w(TAG, "PoseLandmarker 未嬉後")
                return PoseAnalysisResult.empty(trainingRecordId)
            }

            // 轉換輸出紀影元
            val mpImage = MPImage.create(bitmap)
            val result = poseLandmarker?.detect(mpImage) ?: return PoseAnalysisResult.empty(trainingRecordId)

            // 推抽關節點
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
     * 推抽關節點
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
                visibility = landmark.presence().orElse(1f)
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
     * 根據池琇靜正推估
     */
    private fun calculatePostureScore(landmarks: List<Landmark>): Float {
        if (landmarks.isEmpty()) return 0f
        
        val shoulderLandmarks = landmarks.filter { it.name in listOf("left_shoulder", "right_shoulder") }
        val hipLandmarks = landmarks.filter { it.name in listOf("left_hip", "right_hip") }
        
        // 棄程上下寶緑弸形清正池琇靜正推估
        var score = 85f  // 基础分數
        
        // 檢查肩膼高度
        if (shoulderLandmarks.size == 2) {
            val heightDiff = Math.abs(shoulderLandmarks[0].y - shoulderLandmarks[1].y)
            if (heightDiff > 0.1f) score -= 10f  // 肩膀不平衡
        }
        
        // 檢查臀部高度
        if (hipLandmarks.size == 2) {
            val heightDiff = Math.abs(hipLandmarks[0].y - hipLandmarks[1].y)
            if (heightDiff > 0.1f) score -= 10f  // 臀部不平衡
        }
        
        return score.coerceIn(0f, 100f)
    }

    /**
     * 計算平衡分數
     */
    private fun calculateBalanceScore(landmarks: List<Landmark>): Float {
        if (landmarks.isEmpty()) return 0f
        
        // 上了彼此相關的關節點
        val leftHip = landmarks.find { it.name == "left_hip" }
        val rightHip = landmarks.find { it.name == "right_hip" }
        val leftAnkle = landmarks.find { it.name == "left_ankle" }
        val rightAnkle = landmarks.find { it.name == "right_ankle" }
        
        if (leftHip == null || rightHip == null || leftAnkle == null || rightAnkle == null) {
            return 85f
        }
        
        // 計算後壢實位置不對稱程度
        val hipDist = Math.abs(leftHip.x - rightHip.x)
        val ankleDist = Math.abs(leftAnkle.x - rightAnkle.x)
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
        
        // 手臂上下撰動範圍
        val leftSwing = Math.abs(leftElbow.y - leftShoulder.y)
        val rightSwing = Math.abs(rightElbow.y - rightShoulder.y)
        
        var score = 85f
        
        // 擺動涵蓋憨兗
        if (leftSwing < 0.1f || rightSwing < 0.1f) {
            score -= 20f  // 手臂擺動不足
        }
        
        // 擺動床称潔量不對稱
        if (Math.abs(leftSwing - rightSwing) > 0.15f) {
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
                PostureIssue.UNEVEN_SHOULDERS -> "請會肩膀保持水平。收緦核心而簡肩膼"
                PostureIssue.INSUFFICIENT_ARM_SWING -> "懶臂擺動行措。估益幪天擺高筋胡世"
                PostureIssue.UNEVEN_HIPS -> "紱部保涁水平。不礦左右搖晃過大"
                else -> "改善${issue.description}。請不礦紃親俠"
            }
        }
    }

    /**
     * 積院 PoseLandmarker
     */
    fun close() {
        poseLandmarker?.close()
        poseLandmarker = null
    }
}
