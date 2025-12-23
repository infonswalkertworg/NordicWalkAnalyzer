# Nordic Walk Analyzer - 北歐式健走策略統計幫手 App

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![License](https://img.shields.io/badge/license-MIT-green)
![Android](https://img.shields.io/badge/Android-8.0+-brightgreen)

## 項目簡述

Nordic Walk Analyzer 是一个专业的 Android 应用，为北歐式健走教练提供完整的学员管理和实时动作分析功能。

### 🎯 主要功能

**第一层：学员资料清單**
- ✅ 学员基本资料管理
- ✅ 自动计算建议健走杖長度 (height × 0.68 漫添到 5cm 個)
- ✅ 訓練走数记录 (日期、距离、技肢率、VO2Max 等)
- ✅ 詳情展示和编辑

**第二层：视频有效测释（Part B & C 定旨）**
- ✅ 实时相机与骨架干掭
- ✅ 导入每日视频控制播放
- ✅ 动作分析并提供中文建议
- ✅ MP4 输出有效测释结果

---

## 🏗️ 项目结构

```
NordicWalkAnalyzer/
├── app/                              # Main application module
│   ├── src/main/kotlin/
│   │   ├── com/nordicwalk/analyzer/
│   │   │   ├── NordicWalkApp.kt     # Application entry point
│   │   │   ├── MainActivity.kt       # Main activity
│   │   │   └── ui/
│   │   │       ├── navigation/      # Navigation & routes
│   │   │       └── theme/           # Material3 theme
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
│
├── core/
│   ├── data/                         # Data layer (Room DB, Repository)
│   │   ├── src/main/kotlin/
│   │   │   └── com/nordicwalk/core/data/
│   │   │       ├── db/              # Room entities, DAOs, Database
│   │   │       ├── repository/      # Repository interfaces & impl
│   │   │       └── di/              # Hilt dependency injection
│   │   └── build.gradle.kts
│   │
│   ├── domain/                       # Domain layer (Business logic)
│   │   ├── src/main/kotlin/
│   │   │   └── com/nordicwalk/core/domain/
│   │   │       └── model/           # Domain models
│   │   └── build.gradle.kts
│   │
│   └── ui/                           # UI layer (Shared components)
│       ├── src/main/kotlin/
│       │   └── com/nordicwalk/core/ui/
│       │       └── components/      # Reusable Compose components
│       └── build.gradle.kts
│
├── feature/
│   ├── student-management/           # Student CRUD & training records
│   │   ├── src/main/kotlin/
│   │   │   └── com/nordicwalk/feature/student/
│   │   │       └── presentation/
│   │   │           ├── StudentListViewModel.kt
│   │   │           ├── StudentFormViewModel.kt
│   │   │           ├── StudentDetailViewModel.kt
│   │   │           └── ui/           # Compose screens (待实现)
│   │   └── build.gradle.kts
│   │
│   ├── video-analysis/               # Camera & video processing (Part B & C)
│   │   └── ...
│   │
│   ├── pose-engine/                  # MediaPipe Pose estimation
│   │   └── ...
│   │
│   └── reporting/                    # Analysis reports & feedback
│       └── ...
│
├── build.gradle.kts                  # Root Gradle configuration
├── settings.gradle.kts               # Module definitions
├── IMPLEMENTATION_GUIDE_A.md         # Part A (Student Mgmt) implementation guide
├── BUILD_AND_DEPLOY.md              # Build & APK deployment guide
└── README.md                         # This file
```

---

## 🚀 快速开始

### 需求
- Android Studio 2023.2.0+
- JDK 17+
- Android SDK 26+ (minSdk), API 34 (compileSdk)
- 4GB+ RAM, 2GB+ 空闲存储

### 步骤2：Clone & 打开

```powershell
# 1. Clone 仓库
git clone https://github.com/infonswalkertworg/NordicWalkAnalyzer.git
cd NordicWalkAnalyzer

# 2. 在 Android Studio 打开
open -a "Android Studio" .  # macOS
start android-studio .       # Windows
```

### 步骤3：编译 & 运行

**使用 Android Studio**:
1. 等待 Gradle 同步完成
2. 选择 **Run** > **Run 'app'** (或按 Shift+F10)
3. 选择你的模拟器/真機
4. 等待应用启动

或使用命令行：
```bash
./gradlew installDebug  # 编译并安裝
./gradlew runDebug      # 运行
```

---

## 📋 实现进展

### ✅ 已完成

**数据层 (Data Layer)**
- [x] Room 数据库架构
- [x] Student, TrainingRecord, AnalysisSession 实体类
- [x] DAO 接口 & 实现
- [x] Repository 模式
- [x] Hilt DI 配置

**业务逻辑层 (Domain Layer)**
- [x] Student, TrainingRecord, AnalysisSession 消息体
- [x] PoleLengthCalculator (健走杖長度计算)
- [x] CaptureSource, ViewDirection 枚举

**批准管理层 (ViewModel Layer)**
- [x] StudentListViewModel
- [x] StudentFormViewModel

**基础框架 (Infrastructure)**
- [x] NordicWalkApp (Hilt Application)
- [x] MainActivity & Navigation
- [x] Material3 Theme & Typography
- [x] AndroidManifest.xml
- [x] Build & Deploy configuration

### 🔄 正在实现

**Part A ：学员管理 UI**
- [ ] StudentListScreen
- [ ] StudentFormScreen  
- [ ] StudentDetailScreen
- [ ] TrainingRecordFormScreen
- [ ] TrainingRecordDetailScreen
- [ ] StudentDetailViewModel
- [ ] TrainingRecordFormViewModel
- [ ] TrainingRecordDetailViewModel

### 📅 此后实现

**Part B：实时相机 + 骨架干掭**
- [ ] CameraX 集成
- [ ] MediaPipe Pose 估计器
- [ ] 骨架干掭绘制
- [ ] 参考线標注
- [ ] 实时指标 HUD

**Part C：视频上传 + 播放控制**
- [ ] 视频上传 UI
- [ ] Media3 ExoPlayer 一体化
- [ ] 控制播放速度/匚速
- [ ] 动作指标计算
- [ ] 決抢回放
- [ ] MP4 输出与截图

**Part D：动作报告 & 提业**
- [ ] ONWF/INWA 标沖参数 事项审洙
- [ ] 中文实质分析模板
- [ ] 常见错误案例注覣
- [ ] 提业输出

---

## 📖 文档

- **[Part A 实现指南](./IMPLEMENTATION_GUIDE_A.md)** - 学员管理 UI 实现策略 & 散件规范
- **[构建与部署指南](./BUILD_AND_DEPLOY.md)** - APK 输出、测试 & 预载至真機
- **[API 文档](./docs/API.md)** (待前) - Repository & ViewModel 接口

---

## 🛠️ 技术栈

### 核心依赖
- **UI：** Jetpack Compose 1.6.4 + Material Design 3
- **数据库：** Room 2.6.1 + Kotlin Coroutines
- **依赖注入：** Hilt 2.48
- **相机：** CameraX 1.3.1 (Part B)
- **视频：** Media3 1.2.1 / ExoPlayer (Part C)
- **主体估计：** MediaPipe Pose 0.20230731 (Part B)

### 下位组件
```kotlin
core:data          // Room entities, DAOs, repositories
core:domain        // Business models & logic
core:ui            // Shared Compose components
feature:student    // Student management screens & logic
feature:video      // Camera & video processing
feature:pose       // Pose estimation engine
feature:reporting  // Analysis reports
```

---

## ⚙️ 配置 & 权限

### Android Manifest 舓报
```xml
<!-- 定位权限 -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />

<!-- 硬件事项 -->
<uses-feature android:name="android.hardware.camera" />
```

### 平彦矩形
- **方向：** Landscape (16:9 上)
- **最小 SDK：** Android 8.0 (API 26)
- **目标 SDK：** Android 14 (API 34)

---

## 🔐 簽名 & 发布

### Debug APK
```bash
./gradlew assembleDebug
# 输出： app/build/outputs/apk/debug/app-debug.apk
```

### Release APK
```bash
./gradlew assembleRelease
# 输出： app/build/outputs/apk/release/app-release.apk
```

详见 [BUILD_AND_DEPLOY.md](./BUILD_AND_DEPLOY.md) 有效中鼓步骤。

---

## 🐛 故障排究

### Gradle 同步失败
```bash
./gradlew clean
./gradlew sync
```

### 应用未启轮
```bash
# 检查 Logcat
adb logcat | grep -i "NordicWalk"
```

### 旧版本导致的沒有有效控作
```bash
adb uninstall com.nordicwalk.analyzer
adb install app/build/outputs/apk/debug/app-debug.apk
```

更多信息见 [BUILD_AND_DEPLOY.md#常见问题](./BUILD_AND_DEPLOY.md#常见问题)

---

## 📈 下一步

1. **完成 Part A** (学员管理 UI) → 在真機上测试
2. **开发 Part B** (相机 + 骨架) → 推自模式整合
3. **实现 Part C** (视频 + 指标) → 动作浄浜
4. **串联 Part D** (报告 + 提业) → 重汉实测

---

## ✅ 帅氣梣息

- 完全中文汇 & 操作业
- ONWF/INWA 标沖遵什
- 正式运动分析幻满庚
- 教练员訴遘有吹幻想的操作

---

## 📄 氅典

MIT License - 详见 [LICENSE](./LICENSE)

---

## Ὅd 贊款人

**開發与佔技术支援**
- GitHub: [infonswalkertworg](https://github.com/infonswalkertworg)
- 約汉北歐式健走協會

---

## 脚注

中文版 README 參照 IMPLEMENTATION_GUIDE_A.md 詳情潒化。
