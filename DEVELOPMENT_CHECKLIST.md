# Development Checklist - Nordic Walk Analyzer

## 📋 Part A: 學員資料管理 UI

### ViewModel 層 (Programming)

#### StudentDetailViewModel
```
[ ] 初始化
  [ ] 注入 StudentRepository
  [ ] 注入 TrainingRecordRepository
  [ ] 定義 state flow
    [ ] student: StateFlow<StudentDomain>
    [ ] trainingRecords: StateFlow<List<TrainingRecordDomain>>
    [ ] isLoading: StateFlow<Boolean>
    [ ] errorMessage: StateFlow<String?>

[ ] 加載學員詳情
  [ ] fun loadStudent(studentId: Long)
  [ ] 處理加載狀態
  [ ] 錯誤處理

[ ] 加載訓練記錄清單
  [ ] fun loadTrainingRecords(studentId: Long)
  [ ] 按日期排序 (newer first)
  [ ] 分頁邏輯 (optional)

[ ] 刪除訓練記錄
  [ ] fun deleteTrainingRecord(recordId: Long)
  [ ] 確認對話框
  [ ] Toast 通知

[ ] 編輯學員
  [ ] fun navigateToEdit(studentId: Long)
  [ ] 返回前一個路由

[ ] 新增訓練記錄
  [ ] fun navigateToTrainingForm(studentId: Long)
```

#### TrainingRecordFormViewModel
```
[ ] 初始化
  [ ] 注入 TrainingRecordRepository
  [ ] 定義 state flow
    [ ] date: StateFlow<LocalDate>
    [ ] startTime: StateFlow<LocalTime>
    [ ] endTime: StateFlow<LocalTime>
    [ ] distance: StateFlow<Double>
    [ ] avgHeartRate: StateFlow<Int>
    [ ] maxHeartRate: StateFlow<Int>
    [ ] vo2Max: StateFlow<Double>
    [ ] description: StateFlow<String>
    [ ] improveNotes: StateFlow<String>
    [ ] screenshotUris: StateFlow<List<Uri>>
    [ ] isLoading: StateFlow<Boolean>
    [ ] saveSuccess: StateFlow<Boolean>
    [ ] errorMessage: StateFlow<String?>

[ ] 新增模式
  [ ] fun initForCreate(studentId: Long)
  [ ] 初始化預設值 (今日日期，當前時間)

[ ] 編輯模式
  [ ] fun loadRecord(recordId: Long)
  [ ] 填入既有數據
  [ ] 顯示已上傳截圖

[ ] 日期選擇
  [ ] fun updateDate(newDate: LocalDate)
  [ ] 驗證日期 (不能未來)

[ ] 時間選擇
  [ ] fun updateStartTime(newTime: LocalTime)
  [ ] fun updateEndTime(newTime: LocalTime)
  [ ] 驗證時間範圍 (end > start)
  [ ] 計算訓練時間

[ ] 數值輸入
  [ ] fun updateDistance(km: Double)
  [ ] fun updateAvgHeartRate(bpm: Int)
  [ ] fun updateMaxHeartRate(bpm: Int)
  [ ] fun updateVO2Max(value: Double)
  [ ] 範圍驗證

[ ] 文本輸入
  [ ] fun updateDescription(text: String)
  [ ] fun updateImproveNotes(text: String)

[ ] 截圖上傳
  [ ] fun addScreenshot(uri: Uri)
  [ ] fun removeScreenshot(index: Int)
  [ ] 驗證圖片格式 (JPG, PNG)
  [ ] 壓縮大型影像

[ ] 保存記錄
  [ ] fun saveRecord(studentId: Long)
  [ ] 輸入驗證
    [ ] 日期不能空
    [ ] 至少一個時間必填
    [ ] distance >= 0.1 km
    [ ] HR 範圍合理
  [ ] 調用 repository
  [ ] 成功反饋
  [ ] 錯誤處理

[ ] 導航
  [ ] fun navigateBack()
```

#### TrainingRecordDetailViewModel
```
[ ] 初始化
  [ ] 注入 TrainingRecordRepository
  [ ] 定義 state flow
    [ ] record: StateFlow<TrainingRecordDomain>
    [ ] isLoading: StateFlow<Boolean>
    [ ] errorMessage: StateFlow<String?>

[ ] 加載記錄
  [ ] fun loadRecord(recordId: Long)
  [ ] 處理加載狀態
  [ ] 錯誤處理

[ ] 刪除記錄
  [ ] fun deleteRecord(recordId: Long)
  [ ] 確認對話框
  [ ] 成功回調

[ ] 編輯導航
  [ ] fun navigateToEdit(recordId: Long)

[ ] 返回導航
  [ ] fun navigateBack()
```

### Compose UI 層 (4 個屏幕)

#### StudentListScreen
```
[ ] 結構
  [ ] TopAppBar
    [ ] 標題「學員列表」
    [ ] 搜尋圖標
  [ ] SearchBar (可收縮)
    [ ] 輸入框
    [ ] 清除按鈕
  [ ] LazyColumn
    [ ] StudentCard 清單
    [ ] 點擊導航到詳情
    [ ] 長按刪除菜單
  [ ] FAB
    [ ] 新增學員

[ ] 狀態管理
  [ ] 觀察 viewModel.students StateFlow
  [ ] 觀察 viewModel.searchQuery StateFlow
  [ ] 觀察 viewModel.isLoading

[ ] 用戶交互
  [ ] 搜尋
    [ ] 即時篩選
    [ ] 突出符合項
  [ ] 點擊卡片
    [ ] 導航到 StudentDetailScreen
  [ ] 長按卡片
    [ ] 顯示刪除確認對話框
    [ ] 調用 viewModel.deleteStudent()
  [ ] FAB 點擊
    [ ] 導航到 StudentFormScreen (create mode)

[ ] 視覺效果
  [ ] 加載動畫 (Shimmer placeholder)
  [ ] 空清單提示
  [ ] 無搜尋結果提示
  [ ] 滾動動畫

[ ] Accessibility
  [ ] 為按鈕添加 contentDescription
  [ ] 適當的焦點順序
  [ ] 文本對比度 (4.5:1)
```

#### StudentFormScreen (Create & Edit)
```
[ ] 結構
  [ ] TopAppBar
    [ ] 標題（新增/編輯）
    [ ] 返回按鈕
  [ ] ScrollableColumn
    [ ] AvatarPicker (optional)
    [ ] 姓名輸入框
    [ ] 聯絡方式輸入框
    [ ] 身高輸入框
    [ ] 杖長度建議顯示
      [ ] 基礎計算: (height cm × 0.68)
      [ ] 初級:建議值 ± 5cm
      [ ] 進階:建議值 ± 5cm
    [ ] 保存按鈕
    [ ] 刪除按鈕 (編輯模式)

[ ] 狀態管理
  [ ] 觀察 viewModel 中的各個 StateFlow
  [ ] 輸入驗證實時反饋

[ ] 用戶交互
  [ ] 名字輸入
    [ ] 即時驗證 (非空)
    [ ] 錯誤提示
  [ ] 身高輸入
    [ ] 驗證數值範圍 (100-250 cm)
    [ ] 實時計算杖長度
  [ ] 聯絡方式
    [ ] 驗證格式 (電話/郵箱)
  [ ] 保存
    [ ] 調用 viewModel.saveStudent()
    [ ] 成功 Toast
    [ ] 返回上一層
  [ ] 刪除 (編輯模式)
    [ ] 確認對話框
    [ ] 調用 viewModel.deleteStudent()
    [ ] 返回清單

[ ] 自動計算杖長度
  [ ] 公式: height × 0.68
  [ ] 初級: calculated ± 5 cm
  [ ] 進階: calculated ± 5 cm
  [ ] 實時更新顯示

[ ] 視覺效果
  [ ] 聚焦動畫
  [ ] 錯誤信息紅色高亮
  [ ] 成功狀態綠色指示

[ ] Accessibility
  [ ] 標籤和輸入框配對
  [ ] 錯誤公告
  [ ] 按鈕大小 (48dp minimum)
```

#### StudentDetailScreen
```
[ ] 結構
  [ ] TopAppBar
    [ ] 標題「學員詳情」
    [ ] 返回按鈕
    [ ] 編輯菜單
  [ ] ScrollableColumn
    [ ] 學員卡片
      [ ] 頭像
      [ ] 名字
      [ ] 聯絡方式
      [ ] 身高
      [ ] 杖長度建議 (3 級)
    [ ] 訓練記錄清單
      [ ] 日期/時間
      [ ] 距離
      [ ] 平均心率
      [ ] 點擊導航到詳情
      [ ] 長按刪除菜單
    [ ] FAB
      [ ] 新增訓練記錄

[ ] 狀態管理
  [ ] 觀察 viewModel.student
  [ ] 觀察 viewModel.trainingRecords
  [ ] 觀察 viewModel.isLoading

[ ] 用戶交互
  [ ] 編輯按鈕
    [ ] 導航到 StudentFormScreen (edit mode)
  [ ] 訓練記錄卡片
    [ ] 點擊 → StudentDetailScreen
  [ ] 長按訓練記錄
    [ ] 刪除確認
    [ ] 調用 viewModel.deleteTrainingRecord()
  [ ] FAB
    [ ] 新增訓練記錄
    [ ] 導航到 TrainingRecordFormScreen (create mode)
  [ ] 返回按鈕
    [ ] 返回清單

[ ] 視覺效果
  [ ] 加載動畫
  [ ] 空訓練記錄提示
  [ ] 杖長度三級並排顯示
  [ ] 訓練記錄卡片懸停效果

[ ] Accessibility
  [ ] 語義標題
  [ ] 適當焦點順序
  [ ] 對比度達標
```

#### TrainingRecordFormScreen (Create & Edit)
```
[ ] 結構
  [ ] TopAppBar
    [ ] 標題（新增/編輯訓練記錄）
    [ ] 返回按鈕
  [ ] ScrollableColumn
    [ ] DatePicker
    [ ] 開始時間選擇器
    [ ] 結束時間選擇器
    [ ] 距離輸入框 (km)
    [ ] 平均心率輸入框 (bpm)
    [ ] 最高心率輸入框 (bpm)
    [ ] VO2Max 輸入框
    [ ] 訓練描述 (multiline)
    [ ] 改進建議 (multiline)
    [ ] 截圖上傳
      [ ] 已上傳截圖清單 (可刪除)
      [ ] 添加更多按鈕
    [ ] 保存按鈕
    [ ] 刪除按鈕 (編輯模式)

[ ] 狀態管理
  [ ] 觀察 viewModel 的所有 StateFlow
  [ ] 實時驗證反饋

[ ] 用戶交互
  [ ] 日期選擇
    [ ] 彈出日期選擇器
    [ ] 驗證不能未來日期
  [ ] 時間選擇
    [ ] 彈出時間選擇器
    [ ] 驗證結束時間 > 開始時間
  [ ] 數值輸入
    [ ] 驗證範圍
    [ ] 錯誤提示
  [ ] 截圖上傳
    [ ] 點擊「添加」
    [ ] 選擇圖片
    [ ] 顯示縮圖
    [ ] 長按刪除
  [ ] 保存
    [ ] 驗證所有必填項
    [ ] 調用 viewModel.saveRecord()
    [ ] 成功返回
  [ ] 刪除 (編輯模式)
    [ ] 確認對話框
    [ ] 調用 viewModel.deleteRecord()
    [ ] 返回詳情頁

[ ] 視覺效果
  [ ] 時間輸入反饋
  [ ] 數值驗證指示
  [ ] 截圖縮圖網格
  [ ] 加載動畫

[ ] Accessibility
  [ ] 標籤配對
  [ ] 錯誤公告
  [ ] 按鈕大小
  [ ] 鍵盤支持
```

#### TrainingRecordDetailScreen
```
[ ] 結構
  [ ] TopAppBar
    [ ] 標題「訓練詳情」
    [ ] 返回按鈕
    [ ] 編輯菜單
  [ ] ScrollableColumn
    [ ] 日期顯示
    [ ] 時間範圍顯示
    [ ] 距離顯示
    [ ] 心率數據 (avg/max)
    [ ] VO2Max
    [ ] 描述 (readonly)
    [ ] 改進建議 (readonly)
    [ ] 截圖庫
      [ ] 可點擊全屏預覽

[ ] 狀態管理
  [ ] 觀察 viewModel.record StateFlow
  [ ] 觀察 viewModel.isLoading

[ ] 用戶交互
  [ ] 編輯按鈕
    [ ] 導航到 TrainingRecordFormScreen (edit mode, recordId)
  [ ] 刪除菜單
    [ ] 確認對話框
    [ ] 調用 viewModel.deleteRecord()
    [ ] 返回詳情頁
  [ ] 截圖點擊
    [ ] 全屏預覽 (放大/縮小)
    [ ] 滑動瀏覽
  [ ] 返回按鈕
    [ ] 返回學員詳情

[ ] 視覺效果
  [ ] 加載動畫
  [ ] 截圖網格
  [ ] 數據卡片排版

[ ] Accessibility
  [ ] 語義標題
  [ ] 圖片 alt 文本
  [ ] 焦點管理
```

### 測試清單

```
[ ] Unit Tests
  [ ] StudentListViewModel
    [ ] loadStudents()
    [ ] searchStudents()
    [ ] deleteStudent()
  [ ] StudentFormViewModel
    [ ] calculatePoleLengths()
    [ ] validateInput()
    [ ] saveStudent()
  [ ] StudentDetailViewModel
    [ ] loadStudent()
    [ ] loadTrainingRecords()
    [ ] deleteTrainingRecord()
  [ ] TrainingRecordFormViewModel
    [ ] validateTimeRange()
    [ ] validateMetrics()
    [ ] saveRecord()
  [ ] TrainingRecordDetailViewModel
    [ ] loadRecord()
    [ ] deleteRecord()

[ ] UI Tests (Compose)
  [ ] StudentListScreen
    [ ] 渲染清單
    [ ] 搜尋功能
    [ ] 導航到詳情
    [ ] 刪除對話框
  [ ] StudentFormScreen
    [ ] 輸入驗證
    [ ] 杖長度計算
    [ ] 保存功能
  [ ] StudentDetailScreen
    [ ] 顯示學員信息
    [ ] 訓練記錄清單
    [ ] 導航功能
  [ ] TrainingRecordFormScreen
    [ ] 日期/時間選擇
    [ ] 數值輸入驗證
    [ ] 截圖上傳
    [ ] 保存功能
  [ ] TrainingRecordDetailScreen
    [ ] 顯示信息
    [ ] 編輯導航
    [ ] 刪除功能

[ ] Integration Tests
  [ ] Room DAO
    [ ] CRUD 操作
    [ ] 查詢功能
    [ ] 關聯刪除
  [ ] Repository
    [ ] Entity <-> Domain 轉換
    [ ] 業務邏輯

[ ] 真機測試
  [ ] 在 Android 10+ 實機運行
  [ ] 權限請求
  [ ] 螢幕旋轉適應
  [ ] 後退按鈕
  [ ] 系統返回手勢
  [ ] 長期使用 (数据持久化)
  [ ] 網絡離線 (如適用)
  [ ] 記憶體洩漏 (Android Profiler)
```

### 代碼質量檢查

```
[ ] Lint
  [ ] ./gradlew lintDebug
  [ ] 修復所有警告 (除了 Info)

[ ] Formatting
  [ ] ./gradlew ktlintFormat
  [ ] 遵循 Kotlin conventions

[ ] Architecture
  [ ] ViewModel 不持有 UI
  [ ] 單向數據流
  [ ] 沒有 Context 洩漏

[ ] Documentation
  [ ] KDoc for public APIs
  [ ] 中文註解 (if applicable)
  [ ] README 更新
```

---

## 📋 Part B: 即時相機 + 骨架疊圖 Demo

*(詳細檢查清單待補充)*

---

## 📋 Part C: 匯入影片 + 播放控制 + 單格截圖

*(詳細檢查清單待補充)*

---

## 📋 Part D: 動作報告 & 提業

*(詳細檢查清單待補充)*

---

## 🚀 整個項目的最終檢查

```
[ ] 所有部分編譯成功
[ ] Gradle build warnings < 5
[ ] No compilation errors
[ ] 所有測試通過
[ ] 真機測試通過
[ ] Logcat 無異常
[ ] Android Profiler 無記憶體洩漏
[ ] 無 ANR (Application Not Responding)
[ ] 合適的權限聲明
[ ] 正確的 minSdk/targetSdk
[ ] Release APK 簽名正確
[ ] README & 文檔完整
[ ] 代碼推送至 GitHub
[ ] 版本號遞增
```

---

**最後更新**: 2025-12-23  
**作者**: Nordic Walking Analyzer Team  
**進度**: Part A 開發中
