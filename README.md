# mini七巧板

`mini七巧板` 是一款以 Kotlin 與 Jetpack Compose 製作的 Android 七巧板遊戲。玩家可拖曳、旋轉及翻轉七塊標準拼片，挑戰內建圖案或自行設計關卡。應用程式可離線遊玩，並保存關卡、進度、最佳時間與偏好設定。

## 主要功能

- 20 個內建七巧板關卡
- 自訂關卡編輯器，支援邊對邊、雙邊及頂點吸附、兩指整體移動、自動置中、關卡預覽、命名、編輯與刪除
- 自訂關卡 JSON 批次匯入與匯出
- 標準七巧板面積比例與等比例畫布繪製
- 點擊選取、拖曳、每次 45 度旋轉及平行四邊形翻面
- 相容拼片交換、目標占用管理及自動吸附
- 提示、重置、計時、關卡解鎖、最佳時間與完成圖案分享
- 系統、淺色、深色三種顯示模式
- 初級拼片外框與高級整體外圍輪廓
- 經典、高彩、柔和、海洋、夕照、單色六種拼板配色
- 可隱藏內建關卡，只顯示自訂關卡
- 可關閉的吸附提示音
- 本機 DataStore 持久化，無須網路或帳號
- Edge-to-edge 介面與系統導覽列安全區支援

## 環境需求

- Android Studio（建議使用最新穩定版）
- JDK 17
- Android SDK 35
- Android 8.0（API 26）以上裝置或模擬器

## 快速開始

1. 複製專案：

   ```bash
   git clone https://github.com/mark216tw/android-tangram-01.git
   cd android-tangram-01
   ```

2. 使用 Android Studio 開啟根目錄並等待 Gradle 同步完成。
3. 選擇 Android 裝置或模擬器後執行 `app`。

也可使用命令列：

```bash
# Windows
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug

# macOS / Linux
./gradlew assembleDebug
./gradlew installDebug
```

Debug APK 會產生於：

```text
app/build/outputs/apk/debug/app-debug.apk
```

測試發行版使用 `prerelease` Build Type，版本為 `1.0.0-prerelease`，啟用 R8 壓縮並使用 Debug 金鑰簽署：

```bash
# Windows
.\gradlew.bat assemblePrerelease

# macOS / Linux
./gradlew assemblePrerelease
```

APK 會產生於：

```text
app/build/outputs/apk/prerelease/app-prerelease.apk
```

## 品質檢查

```bash
# Windows
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug

# macOS / Linux
./gradlew testDebugUnitTest lintDebug assembleDebug
```

## 文件

- [文件首頁](docs/README.md)
- [使用指南](docs/USER_GUIDE.md)
- [系統架構與技術文件](docs/ARCHITECTURE.md)
- [系統設計文件](docs/SYSTEM_DESIGN.md)
- [開發與建置指南](docs/DEVELOPMENT.md)
- [貢獻指南](CONTRIBUTING.md)
- [安全政策](SECURITY.md)
- [版本紀錄](CHANGELOG.md)

## 專案結構

```text
android-tangram-01/
├─ app/
│  ├─ src/main/java/com/example/minitangram/
│  │  ├─ data/                 # DataStore 設定與進度
│  │  ├─ game/                 # 幾何、關卡與遊戲狀態
│  │  ├─ ui/theme/             # 淺色與深色主題
│  │  ├─ MainActivity.kt
│  │  └─ MiniTangramApp.kt     # Compose 畫面與導覽
│  ├─ src/main/res/            # 圖示、字串與樣式
│  └─ src/test/                # 單元測試
├─ docs/                       # 詳細文件
├─ LICENSE
└─ README.md
```

## 授權

本專案採用 [MIT License](LICENSE) 授權。

Copyright (c) 2026 mark216tw
