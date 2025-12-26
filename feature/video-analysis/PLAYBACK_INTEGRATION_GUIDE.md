# 影片回放特性整合指南

## 1. 添加 MediaPipe 依賴

在 `feature/video-analysis/build.gradle.kts` 中添加:

```gradle
dependencies {
    // MediaPipe 姿態检步
    implementation("com.google.mediapipe:tasks-vision:0.10.9")
    implementation("com.google.mediapipe:framework-vision:0.10.9")
    
    // 其他具 ML 依賴
    implementation("org.tensorflow:tensorflow-lite:2.13.0")
    implementation("org.tensorflow:tensorflow-lite-gpu-api:2.13.0")
}
```

## 2. 添加 Assets 檔案

從 [MediaPipe](https://developers.google.com/mediapipe/solutions/vision/pose_landmarker) 下載 `pose_landmarker_full.tflite` 並放置到:

```
feature/video-analysis/src/main/assets/pose_landmarker_full.tflite
```

## 3. 修改 Navigation
在你的 Navigation 墨 Compose 路患定義中添加:

```kotlin
// 在錯誤或錄影完成後，削移到回放介面
navigateToPlayback(videoPath: String) {
    navController.navigate("playback/$videoPath")
}

// 回放介面的路由：
composable("playback/{videoPath}") { backStackEntry ->
    val videoPath = backStackEntry.arguments?.getString("videoPath")
    VideoPlaybackScreen(
        videoPath = videoPath,
        onBack = { navController.popBackStack() },
        onAnalysisStart = {
            navController.navigate("analysis/$videoPath")
        }
    )
}
```

## 4. 整理流程

```
錄影完成 → 回放介面 (新) → 分析介面
     ☑️預覽
     ☑️播放控制
     ☑️速度控制
     ☑️姿態可視化
     ☑️截圖
```

## 5. 流程圖

```
「開始分析」按鈕
     → 閱讀整個視頻
     → 在每一幀提取姿態
     → 繪制骨架 + 關節
     → 進入可詳化分析介面
```

## 6. 記憑需求

- 約 **50MB** 檔案大小 (姿態模型)
- **1-2秒** 幀提取時間 (上上一个根據你的設備)
- 最伐 **Android 5.0+**

## 7. 故障排除

### 妻態检步針不顯示或誤判

✅ 確保 30fps 幀率正確
✅ 確保檔案像素正常
✅ 检查信心度门槛 (0.3f 為接受梵值)

### 截圖失敗

✅ 確保已棒購 WRITE_EXTERNAL_STORAGE 權限
✅ 確保 SD 卡有足夠空間

## 8. 下一步

- [ ] 伺服器整合
- [ ] 綀断肨何解抄
- [ ] 化盘執冶有北歐套东方越描推枧
- [ ] 控制台检驗詳化壊詰
