# MediaPipe 模型下載說明

## 需要的模型文件

為了使用姿勢檢測功能，你需要下載 MediaPipe Pose Landmarker 模型。

### 下載步驟

1. **訪問 MediaPipe 官方資源**
   - 前往：https://developers.google.com/mediapipe/solutions/vision/pose_landmarker
   
2. **下載模型文件**
   - 點擊 "Download model" 或直接訪問：
   - https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/float16/latest/pose_landmarker_lite.task
   
3. **放置模型文件**
   - 將下載的 `pose_landmarker_lite.task` 文件放到此資料夾中
   - 完整路徑應該是：`app/src/main/assets/pose_landmarker_lite.task`

### 替代方案（使用命令列下載）

在專案根目錄執行：

```bash
# Windows PowerShell
Invoke-WebRequest -Uri "https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/float16/latest/pose_landmarker_lite.task" -OutFile "app/src/main/assets/pose_landmarker_lite.task"

# Linux/Mac
curl -o app/src/main/assets/pose_landmarker_lite.task https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/float16/latest/pose_landmarker_lite.task
```

### 檔案大小

- `pose_landmarker_lite.task` 大約 5-6 MB

### 驗證

確認檔案已正確放置：
```bash
# 應該看到檔案
ls app/src/main/assets/pose_landmarker_lite.task
```

## 注意事項

- ⚠️ **此模型文件不包含在 Git 版本控制中**（因為文件較大）
- ⚠️ 每次重新克隆專案後都需要重新下載
- ✅ 下載完成後即可編譯並執行應用

## 其他模型選項

MediaPipe 提供三種精度的模型：

1. **Lite** (推薦) - 速度快，準確度中等，檔案小
2. **Full** - 準確度高，速度中等
3. **Heavy** - 準確度最高，但速度較慢

目前應用使用的是 **Lite** 版本，適合實時分析。
