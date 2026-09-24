# 貢獻指南

感謝你對 `mini七巧板` 的關注。提交修改前，請先確認變更符合本專案的離線、簡潔與易操作原則。

## 開發流程

1. Fork 本倉庫並建立功能分支。
2. 使用 JDK 17 與 Android SDK 35 完成開發。
3. 保持 Kotlin 程式碼簡潔，沿用既有 Compose、ViewModel 與 StateFlow 模式。
4. 涉及幾何或吸附規則時，新增或更新單元測試。
5. 提交前執行：

   ```bash
   ./gradlew testDebugUnitTest lintDebug assembleDebug
   ```

6. 建立 Pull Request，描述問題、解法、測試結果及畫面變更。

## 程式碼原則

- 新增拼片幾何時使用標準化座標，避免固定像素。
- UI 必須處理狀態列及導覽列 Insets。
- 淺色與深色模式皆須保持足夠對比。
- 不應加入不必要的網路、追蹤或個人資料蒐集功能。
- 使用者可見文字以繁體中文為主。

## 問題回報

請在 GitHub Issues 提供 Android 版本、裝置型號、重現步驟、預期結果及實際結果。若是介面問題，建議附上截圖或螢幕錄影。
