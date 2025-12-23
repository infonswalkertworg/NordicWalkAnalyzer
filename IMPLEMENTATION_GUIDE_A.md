# 學員資料管理 UI 實作指南 (Part A)

## 已完成的內容

### ✅ 資料層 (Data Layer)
- [x] `StudentEntity`, `TrainingRecordEntity`, `AnalysisSessionEntity` 數據表定義
- [x] `StudentDao`, `TrainingRecordDao`, `AnalysisSessionDao` 資料存取介面
- [x] `DateTimeConverters` 日期時間轉換器
- [x] `NordicWalkDatabase` Room 資料庫主類

### ✅ Domain 層
- [x] `Student`, `TrainingRecord`, `AnalysisSession` 領域模型
- [x] `PoleLengthCalculator` 健走杖長度計算邏輯
- [x] `CaptureSource`, `ViewDirection` 列舉類

### ✅ Repository 層
- [x] `StudentRepository` 介面與 `StudentRepositoryImpl` 實現
- [x] `TrainingRecordRepository` 介面與 `TrainingRecordRepositoryImpl` 實現
- [x] Hilt `DataModule` 依賴注入配置

### ✅ ViewModel 層
- [x] `StudentListViewModel` - 學員清單管理
- [x] `StudentFormViewModel` - 學員新增/編輯（含自動計算杖長度）

---

## 尚需實作的內容

### 1️⃣ Compose UI 畫面

#### 1.1 學員清單畫面
**檔案**: `feature/student-management/src/main/kotlin/com/nordicwalk/feature/student/presentation/ui/StudentListScreen.kt`

```kotlin
@Composable
fun StudentListScreen(
    viewModel: StudentListViewModel = hiltViewModel(),
    onNavigateToForm: (Long?) -> Unit,
    onNavigateToDetail: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // 需要實作:
    // 1. 搜尋欄位 (TextField)
    // 2. 學員卡片列表 (LazyColumn)
    //    - 顯示: 大頭照、姓名、聯絡方式、建議杖長度
    //    - 點擊進入詳情頁
    // 3. 新增按鈕 (FAB)
    // 4. 長按卡片顯示刪除選項
    // 5. Loading / Error 狀態顯示
}
```

**UI 需求**:
- RecyclerView 風格的卡片列表
- 每張卡片包含:
  - 大頭照 (圓形頭像)
  - 學員姓名 (粗體)
  - 聯絡方式 (副文本灰色)
  - 建議杖長度 (標籤: "推薦: XXcm")
- 頂部搜尋欄
- 右下角新增按鈕 (FAB)
- 長按卡片出現刪除確認

#### 1.2 學員編輯/新增畫面
**檔案**: `feature/student-management/src/main/kotlin/com/nordicwalk/feature/student/presentation/ui/StudentFormScreen.kt`

```kotlin
@Composable
fun StudentFormScreen(
    viewModel: StudentFormViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // 需要實作:
    // 1. 大頭照選擇 (點擊打開相機/相簿)
    // 2. 姓名輸入欄 (TextField) + 驗證提示
    // 3. 聯絡方式輸入欄 (TextField)
    // 4. 身高輸入欄 (TextField，100-250cm) + 驗證
    // 5. 自動計算顯示:
    //    - 推薦杖長: XXcm
    //    - 初階杖長: XX-XXcm
    //    - 進階杖長: XX-XXcm
    // 6. 保存按鈕
    // 7. 錯誤提示和成功回饋
}
```

**UI 需求**:
- 大頭照: 圓形圖片 + 「點擊上傳」覆蓋層
- 姓名欄: 必填，有紅色驗證錯誤提示
- 聯絡方式欄: 選填 (電話或信箱)
- 身高欄: 必填，範圍驗證，滑塊或數字輸入
- **杖長度自動計算區塊** (視覺醒目):
  - 建議: 粗大數字顯示
  - 初階 / 進階: 灰色副文本
- 保存按鈕: 按下時轉圈 Loading，成功後 Toast 提示並返回

#### 1.3 學員詳細頁面
**檔案**: `feature/student-management/src/main/kotlin/com/nordicwalk/feature/student/presentation/ui/StudentDetailScreen.kt`

```kotlin
@Composable
fun StudentDetailScreen(
    studentId: Long,
    viewModel: StudentDetailViewModel = hiltViewModel(),
    onNavigateToTrainingForm: (Long, Long?) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToAnalysis: (Long) -> Unit
) {
    val student by viewModel.student.collectAsStateWithLifecycle(initial = null)
    val trainingRecords by viewModel.trainingRecords.collectAsStateWithLifecycle(initial = emptyList())
    
    // 需要實作:
    // 1. 上方: 學員基本資訊卡片 (大頭照、姓名、聯絡方式、身高、杖長度建議)
    // 2. 編輯按鈕
    // 3. 中間: 杖長度推薦區塊 (三層展示)
    // 4. 下方: 訓練紀錄列表
    //    - 每筆紀錄: 日期、時間、距離、心率、VO2、截圖縮圖
    //    - 新增紀錄按鈕
    // 5. 點擊訓練紀錄進入詳情或編輯
}
```

**UI 需求**:
- 上方卡片:
  - 大頭照 (較大) + 姓名、聯絡方式
  - 身高: "175 cm"
  - 編輯按鈕 (右上角)
- 杖長度推薦區塊:
  - **推薦**: 118cm (大號粗體綠色)
  - 初階: 113-123cm (灰色)
  - 進階: 113-123cm (灰色)
- 訓練紀錄區塊:
  - 標題: "訓練紀錄" + "新增按鈕"
  - LazyColumn 列表，每筆記錄顯示:
    - 日期 + 時間 (粗體)
    - 距離 (如有)
    - 心率 (Max/Avg)
    - VO2 (如有)
    - 縮圖 (左側)
  - 點擊進入該筆紀錄詳情

#### 1.4 訓練紀錄編輯畫面
**檔案**: `feature/student-management/src/main/kotlin/com/nordicwalk/feature/student/presentation/ui/TrainingRecordFormScreen.kt`

```kotlin
@Composable
fun TrainingRecordFormScreen(
    studentId: Long,
    recordId: Long?,
    viewModel: TrainingRecordFormViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // 需要實作:
    // 1. 日期選擇 (DatePicker)
    // 2. 時間選擇 (TimePicker)
    // 3. 距離輸入 (km)
    // 4. 心率輸入 (最大 / 平均)
    // 5. VO2MAX 輸入
    // 6. 訓練內容描述 (多行 TextField)
    // 7. 截圖上傳區域 (多張)
    // 8. 改進建議輸入區
    // 9. 保存按鈕
}
```

**UI 需求**:
- 卡片式輸入框
- 日期/時間: 可點擊的選擇器
- 數值欄位: 帶單位標籤
- 截圖上傳: 網格顯示，可移除單張
- 改進建議: 多行文本框

#### 1.5 訓練紀錄詳情頁面
**檔案**: `feature/student-management/src/main/kotlin/com/nordicwalk/feature/student/presentation/ui/TrainingRecordDetailScreen.kt`

```kotlin
@Composable
fun TrainingRecordDetailScreen(
    recordId: Long,
    viewModel: TrainingRecordDetailViewModel = hiltViewModel(),
    onNavigateToEdit: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val record by viewModel.record.collectAsStateWithLifecycle(initial = null)
    
    // 需要實作:
    // 1. 顯示所有紀錄資訊 (唯讀)
    // 2. 編輯按鈕
    // 3. 刪除按鈕 (含確認)
    // 4. 截圖大圖查看 (可滑動)
    // 5. 改進建議展示
}
```

---

### 2️⃣ ViewModel 實作

#### 2.1 StudentDetailViewModel

```kotlin
@HiltViewModel
class StudentDetailViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val trainingRecordRepository: TrainingRecordRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val studentId: Long = savedStateHandle["studentId"] ?: 0L
    
    val student: StateFlow<Student?> // 觀察單個學員
    val trainingRecords: StateFlow<List<TrainingRecord>> // 該學員的訓練紀錄流
    
    fun deleteTrainingRecord(record: TrainingRecord)
    fun reloadData()
}
```

#### 2.2 TrainingRecordFormViewModel

```kotlin
@HiltViewModel
class TrainingRecordFormViewModel @Inject constructor(
    private val trainingRecordRepository: TrainingRecordRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val studentId: Long = savedStateHandle["studentId"] ?: 0L
    private val recordId: Long? = savedStateHandle["recordId"]
    
    val uiState: StateFlow<TrainingRecordFormUiState>
    
    fun updateDate(date: LocalDate)
    fun updateTime(time: LocalTime)
    fun updateDistance(km: Double)
    fun updateHeartRate(max: Int, avg: Int)
    fun updateVO2Max(vo2: Double)
    fun updateDescription(text: String)
    fun addScreenshot(uri: String)
    fun removeScreenshot(uri: String)
    fun updateImprovementNotes(text: String)
    fun saveRecord()
}
```

#### 2.3 TrainingRecordDetailViewModel

```kotlin
@HiltViewModel
class TrainingRecordDetailViewModel @Inject constructor(
    private val trainingRecordRepository: TrainingRecordRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val recordId: Long = savedStateHandle["recordId"] ?: 0L
    
    val record: StateFlow<TrainingRecord?>
    
    fun deleteRecord(onSuccess: () -> Unit)
}
```

---

### 3️⃣ 導航與路由

**檔案**: `app/src/main/kotlin/com/nordicwalk/analyzer/navigation/StudentNavigation.kt`

```kotlin
object StudentRoute {
    const val LIST = "student_list"
    const val FORM = "student_form"
    const val FORM_WITH_ID = "student_form/{studentId}"
    const val DETAIL = "student_detail/{studentId}"
    const val TRAINING_FORM = "training_form/{studentId}"
    const val TRAINING_FORM_WITH_ID = "training_form/{studentId}/{recordId}"
    const val TRAINING_DETAIL = "training_detail/{recordId}"
}

fun NavGraphBuilder.studentGraph(
    onNavigateToAnalysis: (Long) -> Unit,
    onNavigateUp: () -> Unit
) {
    composable(StudentRoute.LIST) {
        StudentListScreen(
            onNavigateToForm = { studentId ->
                // 導航到表單
            },
            onNavigateToDetail = { studentId ->
                // 導航到詳情
            }
        )
    }
    
    composable(
        StudentRoute.FORM_WITH_ID,
        arguments = listOf(navArgument("studentId") { type = NavType.LongType })
    ) {
        StudentFormScreen(onNavigateBack = onNavigateUp)
    }
    
    // ... 其他路由
}
```

---

### 4️⃣ 主程式進入點

**檔案**: `app/src/main/kotlin/com/nordicwalk/analyzer/NordicWalkApp.kt`

```kotlin
@HiltAndroidApp
class NordicWalkApp : Application()

// Activity
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NordicWalkAnalyzerTheme {
                NordicWalkNavigation()
            }
        }
    }
}

@Composable
fun NordicWalkNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "student_list"
    ) {
        studentGraph(
            onNavigateToAnalysis = { studentId ->
                // 進入影像分析模組
            },
            onNavigateUp = { navController.navigateUp() }
        )
    }
}
```

---

## 實作檢查清單

### UI 實作
- [ ] StudentListScreen (搜尋、卡片、FAB、刪除)
- [ ] StudentFormScreen (輸入、驗證、自動計算)
- [ ] StudentDetailScreen (基本資訊、編輯、訓練紀錄列表)
- [ ] TrainingRecordFormScreen (日期、時間、數值、截圖、描述)
- [ ] TrainingRecordDetailScreen (顯示、編輯、刪除、截圖查看)

### ViewModel 實作
- [ ] StudentDetailViewModel
- [ ] TrainingRecordFormViewModel
- [ ] TrainingRecordDetailViewModel

### 導航實作
- [ ] StudentRoute 定義
- [ ] studentGraph NavGraphBuilder
- [ ] MainActivity + NordicWalkNavigation

### Theme & 共用元件
- [ ] NordicWalkTheme (Material3 主題)
- [ ] StudentCard Composable
- [ ] PoleRecommendationCard Composable
- [ ] TrainingRecordCard Composable
- [ ] ErrorDialog Composable

---

## 下一步

完成 Part A 後，你可以在 Android Studio 中執行 app：

```bash
# 編譯並在模擬器/真機上執行
./gradlew installDebug
```

測試流程:
1. ✅ 打開應用 → 看到空的學員清單
2. ✅ 點擊新增 → 填入學員資訊
3. ✅ 身高自動計算杖長度
4. ✅ 保存 → 學員出現在清單
5. ✅ 點擊學員 → 詳情頁面
6. ✅ 新增訓練紀錄
7. ✅ 查看、編輯、刪除紀錄

完成此部分後，下一步可選擇 **Part B (即時相機)** 或 **Part C (匯入影片)**。
