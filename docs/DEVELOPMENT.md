# 開發與建置指南

## 開發環境

- JDK 17
- Android SDK Platform 35
- Android Build Tools 35.x
- Android Studio 最新穩定版
- Git

Gradle Wrapper 已包含於倉庫，不需要另外安裝 Gradle。

## 初次設定

1. 複製倉庫。
2. 使用 Android Studio 開啟根目錄。
3. 確認 Android SDK 路徑正確。
4. 等待 Gradle 依賴下載與同步完成。

若使用 `local.properties`，請勿提交該檔案；它已被 `.gitignore` 排除。

## 常用命令

### Windows

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat assemblePrerelease
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat installDebug
```

### macOS / Linux

```bash
./gradlew assembleDebug
./gradlew assemblePrerelease
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew installDebug
```

完整驗證：

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

## 建置輸出

| 類型 | 路徑 |
| --- | --- |
| Debug APK | `app/build/outputs/apk/debug/app-debug.apk` |
| Prerelease APK | `app/build/outputs/apk/prerelease/app-prerelease.apk` |
| 單元測試報告 | `app/build/reports/tests/testDebugUnitTest/index.html` |
| Lint 報告 | `app/build/reports/lint-results-debug.html` |

`prerelease` Build Type 的版本名稱為 `1.2.0-prerelease`，啟用 R8 程式碼與資源壓縮，並使用 Android Debug 金鑰簽署，適合測試發行，不適合正式上架。

## 新增關卡

1. 以編輯器建立關卡並匯出 `mini-tangram-levels.json`，再將資料轉入 `Levels.kt`。
2. 內建 ID 必須為正數且唯一，顯示及解鎖順序以 `levels` 清單為準。
3. 每個 `PieceKind` 必須剛好出現一次。
4. 旋轉角度應為 45 度倍數，目標中心與 `canvasYScale` 必須是有限值。
5. 依設計需求確認分離、接觸或局部重疊的拼片在初級與高級剪影中呈現正確。
6. 更新關卡資料、文件及測試，並在實機上確認每塊都能完成吸附。

## 修改拼片幾何

幾何修改會同時影響繪製、點擊測試、關卡布局與吸附體驗。修改後至少確認：

- 標準面積比例不變
- 三角形仍為直角等腰
- 所有拼片以幾何中心旋轉
- 長寬比不同的畫布不會造成變形
- 20 個內建關卡仍能完整完成

相關測試位於：

```text
app/src/test/java/com/example/minitangram/game/TangramModelsTest.kt
```

## 主題與系統列

- 色票定義在 `ui/theme/Theme.kt`。
- 新顏色必須在淺色與深色模式確認對比。
- 系統列圖示由 `WindowCompat.getInsetsController()` 控制。
- Edge-to-edge 畫面底部元件需使用 `navigationBarsPadding()` 或 Scaffold Insets。

## App 圖示

- Adaptive Icon 前景：`res/drawable/ic_launcher_foreground.xml`
- Adaptive Icon 背景：`res/drawable/ic_launcher_background.xml`
- API 26+ 定義：`res/mipmap-anydpi-v26/`
- 舊版及圓形備用：`res/mipmap-anydpi/`

圖示畫布為 108dp，設計時需考慮 72dp 裁切區與 66dp 安全區。

## 發布前檢查

- 更新 `versionCode` 與 `versionName`
- 更新 `CHANGELOG.md`
- 執行測試、Lint 與 Release 建置
- 使用正式 keystore 簽署
- 在 API 26、目前 target API 及至少一台實機測試
- 驗證淺色、深色、手勢導航與三鍵導航
- 確認隱私政策與商店資訊符合實際功能
