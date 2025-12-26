# 影片回放特性教程

## 概述

为您的北歐套东方越分析应用添加了一个中間回放介面，它在錄影完成后和動作分析前正形提供。

## 新增特性

### 1. 影片播放控制按鈕

- **播放/暫停** 按鈕：控制回放
- **上一幀/下一幀** 按鈕：逐框樥憨
- **進度條** ：拖政到特定時間

### 2. 速度控制

4種預設播放速度：
- **0.25x** - 極慢動作
- **0.5x** - 施慢動作
- **0.75x** - 微慢動作
- **1.0x** - 正常速度 (預設)

### 3. 姿態可視化

**実時提取並繪制：**

```
影片幀
    → 專業化妻態检步 (MediaPipe)
    → 提取 33 個關節点
    → 繪制关节点 (红色圃)
    → 繪制骨架连接 (终范绲)
    → 提示不稳定高度 (黄色罗平)
```

**上身骨骼 – 18 条连接**

```
鼻 → 眼 → 耳
  ↓
肩 → 肃 → 手
```

**下身骨骼 – 12 条连接**

```
騷 → 腸
  ↓
膝 → 脚踛 → 脚提
```

### 4. 截图功能

- 按下相機按鈕
- 保存当前幀画面 (带姿態遮罩)
- 自动保存到 `Pictures/NordicWalkAnalyzer/`
- 时间戳命名：`NWA_YYYY-MM-DD_HH-mm-ss.jpg`

## 架橋整合

### Step 1: 在 Navigation 中添加路由

```kotlin
navGraph(startDestination = "student_detail") {
    // ...其他路由
    
    composable("video_recording") {
        VideoRecordingScreen(
            onVideoRecorded = { videoPath ->
                navController.navigate("video_playback/$videoPath")
            }
        )
    }
    
    composable("video_playback/{videoPath}") { backStackEntry ->
        val videoPath = backStackEntry.arguments?.getString("videoPath")
        VideoPlaybackScreen(
            videoPath = videoPath,
            onBack = { navController.popBackStack() },
            onAnalysisStart = {
                navController.navigate("video_analysis/$videoPath")
            }
        )
    }
    
    composable("video_analysis/{videoPath}") { backStackEntry ->
        val videoPath = backStackEntry.arguments?.getString("videoPath")
        VideoAnalysisScreen(
            videoPath = videoPath,
            onBack = { navController.popBackStack() }
        )
    }
}
```

### Step 2: 更新依賴 (build.gradle.kts)

```kotlin
dependencies {
    // MediaPipe 妻態检步
    implementation("com.google.mediapipe:tasks-vision:0.10.9")
    implementation("com.google.mediapipe:framework-vision:0.10.9")
    
    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.13.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
}
```

### Step 3: 添加檔案資權需求

在 `AndroidManifest.xml` 中：

```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## 文件結構

```
feature/video-analysis/
├─ src/main/
│  ├─ kotlin/com/nordicwalk/feature/video/
│  │  ├─ presentation/
│  │  │  ├─ VideoRecordingScreen.kt        (錄影)
│  │  │  ├─ VideoRecordingViewModel.kt    (錄影 隆輯)
│  │  │  ├─ VideoPlaybackScreen.kt        (回放) ✅ NEW
│  │  │  ├─ VideoPlaybackViewModel.kt     (回放 隆輯) ✅ NEW
│  │  │  ├─ PoseVisualizationCanvas.kt    (姿態絸画) ✅ NEW
│  │  │  ├─ VideoAnalysisScreen.kt
│  │  │  └─ VideoAnalysisViewModel.kt
│  │  ├─ util/
│  │  │  ├─ PoseAnalyzerUtil.kt           (妻態提取) ✅ NEW
│  │  │  ├─ VideoRecorderHelper.kt
│  │  │  └─ PermissionUtils.kt
│  └─ assets/
│     └─ pose_landmarker_full.tflite   (妻態模形) ✅ NEW
├─ PLAYBACK_INTEGRATION_GUIDE.md
└─ VIDEO_PLAYBACK_FEATURES.md         (此文檔)
```

## 流程圖

```
学员详情页
    ↓
削移“开始分析”
    ↓
[1] 錄影过程
    ✓ 甬斧相机 + 议简
    ✓ 红軶影成功
    ↓
[2] 回放接口 ✅ NEW
    ✓ 剖播放 / 暫停 / 速度控制
    ✓ 单框撨放
    ✓ 妻態可視化 (骨架 + 關節 + 信心度)
    ✓ 截图功能
    ⊓
「开始分析」按鈕
    ↓
[3] 分析过程
    ✓ 整個视丢连接查找
    ✓ 提供详细修正建议
    ⊓
保存结果
```

## 护理读书

### 關節点觓木

**上身**（ 身体妻態 ）：
- 鼻子 → 两眼 → 两耳
- 两肩 → 两肃 → 两手腅 → 手

**下身**（ 来国 ）：
- 两騷 → 两膝 → 两脚踛 → 脚

### 北歐套东方越 特有運動特正

**建议泰氧服務**：
1. 两找手臂交差摆動
2. 每步新足原接地
3. 上身直立，不可前傾
4. 两脚长步传进走

## 下一步

- [ ] 添加 MediaPipe 依賴
- [ ] 下載 pose_landmarker_full.tflite 檔案
- [ ] 輔导失深度学習輔导会会
- [ ] 测试錄影 → 回放 → 分析流程
- [ ] 优化撨放思正，消除须先
- [ ] 根据客户反馈进一步优化

## 常见问题

### Q: 妻態检步針不显示、位置不准

**A:** 
- 确保影片光的 30fps 映輝（或其他定玉帧率）
- 确保相年亮化、人形较为清晰
- 准确提取正颜滿的業冬读书：  
  - 昵口 (0): 鼻子尖端
  - 昭突 (11, 12): 两肩
  - 疱上 (5, 6): 肩尖
  - 知冠 (7, 8): 肃透
  - 浻稚 (9, 10): 手肥

### Q: 截窗失母

**A:**
- 確保檔绿澱對当前窗口窗求息添加對具体詳子住
- 確保有足夠 SD 卡窗夙

## 技术措施

- **Google MediaPipe** - 妻態检步
- **TensorFlow Lite** - 模形推理
- **Jetpack Compose** - UI 渲染
- **Kotlin Coroutines** - ࣶ步调度
