# 如何添加應用程式圖標

## 📱 方法一：使用 Android Studio（推薦）

### 步驟：

1. **準備圖標圖片**
   - 準備一個高解析度的正方形圖片（建議 512x512 或 1024x1024 像素）
   - 格式：PNG、JPG、或 SVG
   - 背景：可以是透明或純色

2. **在 Android Studio 中生成圖標**
   ```
   右鍵點擊 app/src/main/res 資料夾
   → New → Image Asset
   ```

3. **配置圖標**
   - **Icon Type**: 選擇 "Launcher Icons (Adaptive and Legacy)"
   - **Foreground Layer**: 
     - Source Asset Type: 選擇 "Image"
     - Path: 點擊資料夾圖標，選擇你的圖片
     - Trim: 勾選（自動裁剪空白邊緣）
     - Resize: 調整大小以符合圓圈指引
   - **Background Layer**: 
     - Source Asset Type: 選擇 "Color"
     - Color: 選擇背景顏色（例如：#2180A8）
   - **Legacy**: 勾選 "Generate Legacy Icon"
   - **Name**: 保持預設 "ic_launcher"

4. **點擊 Finish**
   - Android Studio 會自動生成所有需要的圖標尺寸
   - 圖標會放在以下位置：
     ```
     app/src/main/res/mipmap-mdpi/ic_launcher.png
     app/src/main/res/mipmap-hdpi/ic_launcher.png
     app/src/main/res/mipmap-xhdpi/ic_launcher.png
     app/src/main/res/mipmap-xxhdpi/ic_launcher.png
     app/src/main/res/mipmap-xxxhdpi/ic_launcher.png
     app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml (Adaptive Icon)
     ```

5. **重新構建並安裝**
   ```powershell
   .\gradlew clean
   .\gradlew assembleDebug
   .\gradlew installDebug
   ```

---

## 🖼️ 方法二：線上工具生成

如果沒有 Android Studio，可以使用線上工具：

### 推薦工具：
1. **Icon Kitchen** - https://icon.kitchen/
   - 上傳圖片
   - 自動生成所有尺寸
   - 下載 ZIP 檔案

2. **App Icon Generator** - https://www.appicon.co/
   - 上傳圖片
   - 選擇 Android
   - 下載並解壓

### 使用步驟：

1. **上傳圖片到線上工具**

2. **下載生成的圖標檔案**

3. **手動放置圖標**
   - 將下載的檔案解壓
   - 複製各個 `mipmap-*` 資料夾到：
     ```
     C:\nswalker_app\nordic_walking_coach\NordicWalkAnalyzer\app\src\main\res\
     ```
   - 覆蓋現有的 `ic_launcher.png` 檔案

4. **提交到 Git**
   ```powershell
   cd C:\nswalker_app\nordic_walking_coach\NordicWalkAnalyzer
   git add app/src/main/res/mipmap-*
   git commit -m "Update app icon"
   git push origin main
   ```

5. **重新構建**
   ```powershell
   .\gradlew clean assembleDebug
   ```

---

## 🎨 圖標設計建議

### 北歐式健走應用程式圖標概念：

1. **簡潔設計**
   - 使用北歐式健走杖的輪廓
   - 或是行走的人物剪影（配合手杖）
   - 簡單的線條和形狀

2. **配色方案**
   - 主色：藍色 `#2180A8`（運動、活力）
   - 輔色：橘色 `#C98A2E`（能量、熱情）
   - 背景：白色或淺藍色

3. **圖標元素**
   - 兩支交叉的健走杖 ⚡⚡
   - 行走的人形 🚶
   - 北歐山脈輪廓 🏔️
   - 足跡圖案 👣

### 範例概念：
```
╔═══════════╗
║           ║
║    🚶     ║  ← 簡化的人形 + 手杖
║   /|\    ║
║  / | \   ║
║ /  |  \  ║
║           ║
╚═══════════╝
```

---

## ✅ 檢查圖標是否成功

1. **在 Android Studio 中檢查**
   - 開啟 `app/src/main/res/mipmap-*` 資料夾
   - 確認 `ic_launcher.png` 已更新

2. **在設備上檢查**
   - 安裝應用程式
   - 在主畫面查看圖標
   - 在應用程式列表查看圖標

3. **不同主題下檢查**
   - 淺色主題
   - 深色主題
   - 確保圖標在兩種模式下都清晰可見

---

## 🔧 目前的圖標配置

應用程式使用預設的 Android 啟動圖標。在 `AndroidManifest.xml` 中：

```xml
<application
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round"
    ...
>
```

只需替換 `ic_launcher` 相關的圖片檔案即可。

---

## 📞 需要協助？

如果你有圖標設計的圖片，可以：
1. 將圖片放在專案某處
2. 告訴我檔案位置
3. 我可以幫你生成並配置所有需要的圖標檔案
