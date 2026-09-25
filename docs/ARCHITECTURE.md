# 系統架構與技術文件

## 技術棧

| 項目 | 技術 |
| --- | --- |
| 語言 | Kotlin 2.1.20 |
| UI | Jetpack Compose、Material 3 |
| 導覽 | Navigation Compose |
| 狀態管理 | ViewModel、StateFlow |
| 持久化 | Preferences DataStore |
| 繪圖 | Compose Canvas、Path |
| 音效 | Android `ToneGenerator` |
| 建置 | Gradle 8.11.1、Android Gradle Plugin 8.9.2 |
| Java | JDK 17 |
| Android | minSdk 26、targetSdk 35、compileSdk 35 |
| 測試 | JUnit 4、Android Lint |

## 架構概觀

應用程式採單一 `app` 模組，以 UI、遊戲領域及資料持久化三個責任區域組成。

```text
┌──────────────────────────────────────────────┐
│ Compose UI / Navigation                     │
│ MiniTangramApp、首頁、關卡、遊戲、設定      │
└──────────────────────┬───────────────────────┘
                       │ 狀態 / 使用者操作
┌──────────────────────▼───────────────────────┐
│ 遊戲領域                                     │
│ GameViewModel、TangramModels、Levels         │
└──────────────────────┬───────────────────────┘
                       │ 完成紀錄 / 偏好設定
┌──────────────────────▼───────────────────────┐
│ PreferencesRepository / DataStore           │
└──────────────────────────────────────────────┘
```

## 套件責任

### `com.example.minitangram`

- `MainActivity.kt`：Activity 入口、edge-to-edge 啟用及 Compose 掛載。
- `MiniTangramApp.kt`：Navigation、畫面組合、Canvas、手勢、系統音效與 UI 元件。

### `com.example.minitangram.game`

- `TangramModels.kt`：向量、拼片類型、標準幾何、座標轉換及方向判定。
- `Levels.kt`：由 `mini-tangram-levels.json` 轉入的 20 個內建關卡，以及依清單順序進行解鎖與下一關的查詢函式。
- `EditorSnapping.kt`：自訂關卡編輯器的邊對邊幾何吸附。
- `CustomLevelOrder.kt`：自訂關卡原位置更新規則。
- `GameViewModel.kt`：遊戲狀態、計時、選取、拖曳、旋轉、翻面、提示及吸附。

### `com.example.minitangram.data`

- `PreferencesRepository.kt`：偏好、進度、自訂關卡及匯入匯出的 DataStore 存取。

### `com.example.minitangram.ui.theme`

- `Theme.kt`：淺色／深色色票、系統模式判定及系統列圖示明暗。

## 狀態與資料流

```text
使用者手勢
  → GameViewModel 方法
  → MutableStateFlow<GameUiState>
  → collectAsStateWithLifecycle
  → Compose 重組與 Canvas 重繪

設定操作
  → PreferencesRepository.edit
  → DataStore Flow<UserProgress>
  → 全域主題、設定畫面或遊戲音效更新
```

`GameUiState` 包含：

- 拼片清單及其姿態
- 當前選取拼片
- 經過秒數
- 完成狀態
- 提示拼片及提示目標
- 提示使用次數
- 吸附事件計數

## 七巧板幾何

七塊拼片以 `Vec2` 頂點表示，使用共同基準 `SMALL_LEG` 推導尺寸：

| 拼片 | 數量 | 單塊占總面積 |
| --- | ---: | ---: |
| 大直角等腰三角形 | 2 | 1/4 |
| 中直角等腰三角形 | 1 | 1/8 |
| 小直角等腰三角形 | 2 | 1/16 |
| 正方形 | 1 | 1/8 |
| 平行四邊形 | 1 | 1/8 |

三角形頂點以幾何重心為原點，正方形與平行四邊形亦以中心為原點。因此旋轉不會造成中心漂移。

### 座標系統

- 關卡和拼片中心採 `0..1` 標準化座標。
- X 軸依畫布寬度換算。
- Y 軸使用 `width / height` 校正，確保長螢幕不會拉伸幾何形狀。
- 點擊測試與 Canvas 繪製使用相同轉換，避免視覺與觸控範圍不一致。

高級難度的整體剪影會先在固定的等比例座標中執行 `PathOperation.Union`，再由遊戲畫布與編輯器預覽分別縮放及平移。布林合併不受最終畫布像素尺寸影響，因此兩處會保留相同的外圍線條。

## 選取與拖曳

1. Pointer Input 接收點擊或拖曳起點。
2. 依繪製順序反向執行多邊形 point-in-polygon 測試。
3. 找到未吸附的最上層拼片後設為選取。
4. 被選取拼片移至清單尾端，Canvas 會最後繪製它。
5. 拖曳位移轉為標準化座標並限制在畫布範圍內。

## 吸附演算法

`trySnap()` 依序執行：

1. 取得當前拼片與已占用目標。
2. 篩選尺寸相容且未占用的目標。
3. 檢查旋轉對稱與平行四邊形翻面。
4. 以等比例畫布距離計算拼片中心和目標中心。
5. 距離小於容差時，選擇最近目標並鎖定。

相容規則：

- 兩個大三角形可互換。
- 兩個小三角形可互換。
- 其他拼片只可使用同類目標。
- 正方形旋轉週期為 90 度。
- 平行四邊形旋轉週期為 180 度，且翻面必須相同。
- 三角形旋轉週期為 360 度。

## 持久化

DataStore 檔名為 `mini_tangram_preferences_v2`，使用以下鍵值：

- `display_mode`：`SYSTEM`、`LIGHT` 或 `DARK`
- `sound_enabled`：布林值，預設 `true`
- `built_in_unlocked_v2`：目前最高已解鎖的內建關卡 ID，預設為清單第一關
- `built_in_v2_best_time_<level>`：新版內建關卡的最佳秒數
- `best_time_<level>`：自訂關卡的最佳秒數
- `difficulty`：`BEGINNER` 或 `ADVANCED`
- `piece_color_theme`：目前拼板配色
- `custom_levels`：版本化 JSON 格式的自訂關卡清單

應用程式不使用網路權限，也不將資料傳送到裝置外。

## 導覽

| Route | 畫面 |
| --- | --- |
| `home` | 首頁 |
| `levels` | 關卡選擇 |
| `editor/{levelId}` | 建立或編輯自訂關卡 |
| `instructions` | 遊戲說明 |
| `settings` | 設定 |
| `game/{levelId}` | 指定關卡遊戲畫面 |

## 測試範圍

單元測試目前涵蓋：

- 角度跨越 0/360 度的距離
- 多邊形內外點判定
- 20 關資料完整性
- 內建關卡清單順序、解鎖及下一關查詢
- 多種畫布比例下的內建關卡幾何轉換
- 翻面及旋轉後頂點
- 標準七巧板面積比例
- 直角等腰與幾何中心
- 長畫布等比例顯示
- 同尺寸三角形目標交換
- 正方形、平行四邊形旋轉對稱
- 平行四邊形翻面判定
- 音效預設值
