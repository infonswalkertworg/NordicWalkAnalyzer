# Nordic Walk Analyzer - 建置與部署指南

## 環境需求

### 1. 開發環境
- **Android Studio**: 2023.2.0 或更新版本
- **JDK**: OpenJDK 17 或更新版本
- **Android SDK**:
  - compileSdk: 34
  - minSdk: 26 (Android 8.0+)
  - targetSdk: 34

### 2. Android 設備
- Android 8.0+ (API 26+)
- 建議: Android 10+ (API 29+)
- 至少 4GB RAM
- 相機功能 (用於即時分析)

---

## 第一步：初始化項目

### 1. Clone 專案

```bash
cd C:\AndroidProjects  # 或你的專案資料夾
git clone https://github.com/infonswalkertworg/NordicWalkAnalyzer.git
cd NordicWalkAnalyzer
```

### 2. 在 Android Studio 中打開專案

1. 啟動 **Android Studio**
2. 選擇 **Open** (不是 New Project)
3. 瀏覽到 `NordicWalkAnalyzer` 資料夾
4. 點擊 **OK**
5. 等待 Gradle 同步完成 (第一次可能需要 5-10 分鐘)

### 3. 驗證環境

在 Android Studio 的 **Terminal** 中執行：

```bash
# 驗證 Gradle
./gradlew --version

# 驗證 Android SDK
echo %ANDROID_HOME%  # Windows
echo $ANDROID_HOME   # Mac/Linux
```

---

## 第二步：開發與測試

### 編譯 Debug 版本

在 Android Studio 中：

1. 選擇 **Build** > **Make Project** (或按 Ctrl+F9)
2. 確認沒有編譯錯誤
3. 檢查底部 **Build** 面板的輸出

或在 Terminal 中：

```bash
./gradlew assembleDebug
```

成功時輸出位置：
```
app/build/outputs/apk/debug/app-debug.apk
```

### 在模擬器上運行

**方式 1：使用 Android Studio**

1. 點擊 **Run** 按鈕 (播放圖標)
2. 選擇運行設備 (模擬器或連接的真機)
3. 等待應用安裝並啟動

**方式 2：使用命令行**

```bash
# 列出可用設備
./gradlew -i tasks | grep installDebug

# 安裝並運行
./gradlew installDebug
./gradlew runDebug
```

### 首次運行測試

1. ✅ 應用啟動 → 看到空的學員清單
2. ✅ 點擊「新增學員」 → 進入表單
3. ✅ 填入測試數據：
   - 姓名："李小明"
   - 身高：170 cm
4. ✅ 觀察杖長度自動計算：
   - 建議：115 cm
   - 初階：110-120 cm
   - 進階：110-120 cm
5. ✅ 保存 → 返回清單
6. ✅ 點擊學員 → 進入詳細頁

---

## 第三步：生成 Release APK

### 1. 配置簽名

#### 方式 A：在 Android Studio 中生成簽名

1. 選擇 **Build** > **Generate Signed Bundle / APK**
2. 選擇 **APK**
3. 點擊 **Create new**
4. 填入簽名信息：
   ```
   Key store path: C:\AndroidProjects\nordicwalk.jks
   Key store password: [設定一個強密碼]
   Key alias: nordicwalk
   Key password: [同上或不同密碼]
   Certificate CN (名字): Nordic Walking Analyzer
   ```
5. 點擊 **OK**

#### 方式 B：使用命令行

```bash
# 生成金鑰庫 (第一次)
keytool -genkey -v -keystore C:\nordicwalk.jks -keyalg RSA -keysize 2048 -validity 10000 -alias nordicwalk

# 或用 gradlew
./gradlew signDebugBuild
```

### 2. 生成 Release APK

#### 方式 1：使用 Android Studio GUI

1. **Build** > **Generate Signed Bundle / APK**
2. 選擇 **APK**
3. 選擇剛才建立的金鑰庫
4. 選擇 **release** Build Type
5. 點擊 **Create**
6. 等待打包完成

#### 方式 2：使用命令行

```bash
# 編譯 Release 版本
./gradlew bundleRelease

# 或直接生成 APK
./gradlew assembleRelease -Pandroid.injected.signing.store.file=C:\nordicwalk.jks -Pandroid.injected.signing.store.password=[密碼] -Pandroid.injected.signing.key.alias=nordicwalk -Pandroid.injected.signing.key.password=[密碼]
```

成功輸出位置：
```
app/build/outputs/apk/release/app-release.apk
```

### 3. 驗證 APK

```bash
# 檢查 APK 簽名
jarsigner -verify -verbose app/build/outputs/apk/release/app-release.apk

# 檢查 APK 大小
ls -lh app/build/outputs/apk/release/app-release.apk
```

---

## 第四步：部署到實機

### 前置準備

**在 Android 設備上**：
1. 進入 **設定** > **系統** > **關於手機**
2. 連續點擊 **版本號** 7 次，啟用**開發者模式**
3. 回到設定，進入 **開發人員選項**
4. 啟用 **USB 偵錯**
5. 啟用 **安裝來自未知來源的應用**

**在開發電腦上**：
1. 安裝 USB 驅動程式 (通常 Android Studio 會自動處理)
2. 用 USB 線連接設備
3. 在設備上選擇 **允許 USB 偵錯**

### 安裝方式

#### 方式 1：使用 Android Studio

1. 確認設備已連接 (檢查 **Run** > 下拉菜單)
2. 點擊 **Run** 按鈕
3. 選擇你的設備
4. 等待安裝完成 (~30 秒)

#### 方式 2：使用 ADB 命令行

```bash
# 列出連接的設備
adb devices

# 安裝 APK
adb install app/build/outputs/apk/debug/app-debug.apk

# 或 release 版本
adb install app/build/outputs/apk/release/app-release.apk

# 啟動應用
adb shell am start -n com.nordicwalk.analyzer/.MainActivity
```

#### 方式 3：文件管理器直接安裝

1. 將 APK 文件複製到設備 (USB 傳輸模式)
2. 用文件管理器打開 APK
3. 點擊安裝
4. 允許權限提示

### 授予權限

App 首次啟動時會請求：
- ✅ **相機**: 用於即時視頻分析
- ✅ **存儲**: 用於儲存分析結果和影片
- ✅ **位置**: 供未來擴展功能使用

全部點擊「**允許**」。

---

## 第五步：驗證部署

### 檢查清單

```
✅ 應用成功安裝
✅ 應用正常啟動
✅ 看到空的學員清單
✅ 可以新增學員
✅ 杖長度自動計算
✅ 可以添加訓練紀錄
✅ 可以查看訓練詳情
✅ 導航流暢無崩潰
```

### 調試輸出

若有問題，在 Android Studio 的 **Logcat** 檢查:

```bash
# 篩選應用日誌
adb logcat | grep -i "NordicWalk\|com.nordicwalk"

# 或在 Android Studio
# View > Tool Windows > Logcat
```

---

## 常見問題

### 1. "Gradle sync failed"
**解決**:
```bash
./gradlew clean
./gradlew sync
# 或在 Android Studio: File > Sync Now
```

### 2. "Build failed - compilation errors"
**解決**:
```bash
# 清除快取
./gradlew clean build

# 更新依賴
./gradlew dependencyUpdates
```

### 3. "Cannot find symbol 'R'"
**解決**:
- Build > Clean Project
- Build > Rebuild Project
- 確保所有資源文件都在 `res/` 資料夾內

### 4. "APK 無法在設備上安裝"
**可能原因**:
- 設備上已安裝舊版本，需先卸載
- 架構不相容 (確認 minSdk 版本)
- 簽名不相符

**解決**:
```bash
# 卸載應用
adb uninstall com.nordicwalk.analyzer

# 重新安裝
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 發布檢查清單

在生成最終 Release APK 前：

```
☐ 所有功能在 Debug 上測試過
☐ 沒有 Lint 警告
☐ 沒有記錄 `Log.v()` 或 `Log.d()` 除錯輸出
☐ ProGuard/R8 規則配置正確
☐ 版本號更新 (versionCode, versionName)
☐ AndroidManifest.xml 正確
☐ 所有必要的權限聲明
☐ 測試在不同 API 級別的設備上
☐ 測試橫直屏切換
☐ 隱私政策更新
```

---

## 更新應用

### 本地測試新版本

1. 修改 `app/build.gradle.kts`:
   ```kotlin
   android {
       defaultConfig {
           versionCode = 2  // 遞增
           versionName = "1.1.0"  // 更新版本號
       }
   }
   ```

2. 生成新 APK
3. 卸載舊版本
4. 安裝新版本

---

## 下一步

完成 Part A (學員管理) 後，可選擇優先開發：
- **Part B**: 即時相機 + 骨架疊圖
- **Part C**: 匯入影片 + 播放控制

每個 Part 完成後，重複上述建置與部署流程。

---

有任何建置問題，請檢查：
1. 本地 `local.properties` 是否正確指向 Android SDK
2. Gradle JVM 設定 (File > Settings > Build > Gradle)
3. Android Studio 是否為最新版本
