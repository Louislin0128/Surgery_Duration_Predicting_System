# Surgery-Time-Prediction-System

因Java 9加入模組功能，但我們導入的第三方函式庫尚未導入此特性。若於此版本後的Java上執行，需要加上參數「--add-opens java.base/java.lang=ALL-UNNAMED」。

Weka執行時需要將整個資料集載入，較耗用記憶體空間。
若特徵選取頁面無法正常執行，可調整Java執行參數「-Xmx」，將其加大。

若更新了程式碼，請先將「classes」資料夾中的資料清除，重新編譯。
