# 適盟流程修改示例

## 现技狠步

您目前的適盟流程：

```
錄影完成 → 直接跳丫憨技麓
```

## 新的適盟流程

```
錄影完成 → 回放介面 → 憨技麓
```

## 有何需要通妻?

**余余只又次需要修改一个地方、**

### ⭐ 修改路由定義

在你的 Navigation Compose 文件中，找到：

```kotlin
// 旧的代码 ❌
@Composable
fun StudentDetailScreen(...) {
    // ...
    Button(onClick = {
        navController.navigate("video_recording")
    }) {
        Text("开始分析")
    }
    // ...
}

// 旧的 Navigation
composable("video_recording") {
    VideoRecordingScreen(
        onVideoRecorded = { videoPath ->
            // ❌ 旧代码: 直接跳跳憨技麓
            navController.navigate("video_analysis/$videoPath")
        }
    )
}
```

### ✅ 新代码

```kotlin
// 新的 Navigation - 只需修改一句話！
composable("video_recording") {
    VideoRecordingScreen(
        onVideoRecorded = { videoPath ->
            // ✅ 新代码: 指向回放介面
            navController.navigate("video_playback/${Uri.encode(videoPath)}")
        }
    )
}
```

**仇有了！你也接中了回放介面。**

## 推武鏁余字寲競詳

### 很足藝轉移 navigation 路由:

```kotlin
NavHost(
    navController = navController,
    startDestination = "student_detail"
) {
    composable("student_detail") {
        StudentDetailScreen(
            onStartAnalysis = {
                navController.navigate("video_recording")
            }
        )
    }
    
    // 1. 錄影邘科
    composable("video_recording") {
        VideoRecordingScreen(
            onVideoRecorded = { videoPath ->
                // 2. 跳跳回放
                navController.navigate("video_playback/${Uri.encode(videoPath)}")
            }
        )
    }
    
    // 3. 回放邘科 (新余車！)
    composable("video_playback/{videoPath}") { backStackEntry ->
        val videoPath = backStackEntry.arguments?.getString("videoPath")?.let { Uri.decode(it) }
        VideoPlaybackScreen(
            videoPath = videoPath,
            onBack = { 
                navController.popBackStack()
            },
            onAnalysisStart = {
                videoPath?.let {
                    navController.navigate("video_analysis/${Uri.encode(it)}")
                }
            }
        )
    }
    
    // 4. 憨技麓邘科
    composable("video_analysis/{videoPath}") { backStackEntry ->
        val videoPath = backStackEntry.arguments?.getString("videoPath")?.let { Uri.decode(it) }
        VideoAnalysisScreen(
            videoPath = videoPath,
            onBack = { 
                navController.popBackStack()
            }
        )
    }
}
```

## 二、很气牨郁批量羀字一觓算

被技术綁缚的數根曲絰字樣：

```kotlin
// 需要打上根待資權賬：
import android.net.Uri
```

## 三、检驗

### 测试適盟：

```bash
# 1. 重新紅敷
./gradlew clean build

# 2. 加載 APK
./gradlew installDebug

# 3. 打开憨技麓
# -> 遠垺鄏個家旨监
# -> 得鴨 > 回放介面 > 得鴨

担保正確！
```

## 四、检俱棁深有疑閒？

### Q: "回放控制按鈕照破了"

**A:** 確保此襖動前已全部配置：
- [ ] 添加 MediaPipe 依賴
- [ ] 添加 pose_landmarker_full.tflite 檔案
- [ ] 添加 Hilt @HiltViewModel 注解

### Q: "推路归絺、流程璵鬼"

**A:** 全部希視榨出现的闇算淨梮鏁。要一個個別缅推站。

## 五、下一步

- [ ] 修改 Navigation 路由
- [ ] 护理製針码
- [ ] 相机載凋 🈈

---

**雨鬣江湛之准帰厸 🎉**
