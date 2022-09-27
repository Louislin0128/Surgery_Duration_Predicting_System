package guiFunction;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

/**切分訓練集及測試集*/
public class SplitData {
	/**
	 * 以給定的分割百分比，計算切分筆數
	 * @param inFile 輸入檔案
	 * @param rangeStart 輸入起點
	 * @param rangeFinal 輸出終點
	 * @param dataSize 資料總筆數(不包含標題)
	 * @param trainOutFile 訓練集輸出路徑
	 * @param testOutFile 測試集輸出路徑
	 * @throws Exception
	 */
	public static void byPercent(String inFile, double rangeStart, double rangeFinal, double dataSize, String trainOutFile, String testOutFile) throws Exception {
		int dataStart = (int) Math.round(dataSize * (rangeStart / 100)); // 資料分割起始位址
		int dataFinal = (int) Math.round(dataSize * (rangeFinal / 100)); // 資料分割最後位址
		split(inFile, dataStart, dataFinal, trainOutFile, testOutFile);
	}
	
	/**
	 * 直接指定切分筆數
	 * @param inFile 輸入檔案
	 * @param rangeStart 輸入起點
	 * @param rangeFinal 輸出終點
	 * @param trainOutFile 訓練集輸出路徑
	 * @param testOutFile 測試集輸出路徑
	 * @throws Exception
	 */
	public static void byQuantity(String inFile, int rangeStart, int rangeFinal, String trainOutFile, String testOutFile) throws Exception {
		split(inFile, rangeStart, rangeFinal, trainOutFile, testOutFile);
	}
	
	/**
	 * 根據給定的比數切割資料
	 * @param inFile 輸入檔案
	 * @param dataStart 開始筆數
	 * @param dataFinal 結束筆數
	 * @param trainOutFile 訓練集輸出路徑
	 * @param testOutFile 測試集輸出路徑
	 * @throws Exception
	 */
	private static void split(String inFile, int dataStart, int dataFinal, String trainOutFile, String testOutFile) throws Exception {
		try(BufferedReader br = new BufferedReader(new FileReader(inFile))){ 			// 原資料
		try(BufferedWriter train = new BufferedWriter(new FileWriter(trainOutFile))){ 	// train檔案 覆蓋檔案
		try(BufferedWriter test = new BufferedWriter(new FileWriter(testOutFile))){ 	// test檔案 覆蓋檔案
			String input = br.readLine(); // 標題 | 未切割的整行資料
			train.write(input);
			test.write(input);
			
			int num = 1;
			while (((input = br.readLine()) != null)) {
				if ((num >= dataStart) && (num <= dataFinal)) { // 根據起始與最終位置將資料寫入
					test.newLine();
					test.write(input);
				} else {
					train.newLine();
					train.write(input);
				}
				num++;
			}	
		}
		}
		}
	}
}
