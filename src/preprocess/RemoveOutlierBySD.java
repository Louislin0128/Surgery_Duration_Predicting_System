package preprocess;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class RemoveOutlierBySD {
	/**執行{@link RemoveOutlierBySD#RemoveOutlierBySD RemoveOutlierBySD}*/
	public static void exec(String inFile, String remove, String multiple, String outFile) throws Exception {
		new RemoveOutlierBySD(inFile, remove, multiple, outFile);
	}
	
	/**
	 * @param inFile 資料輸入檔名
	 * @param target 移除欄位索引
	 * @param multiple 標準差倍數
	 * @param outFile 資料輸出檔名
	 * @throws Exception
	 */
	private RemoveOutlierBySD(String inFile, String target, String multiple, String outFile) throws Exception {
		ArrayList<Double> numAL = new ArrayList<Double>(); // 目標欄位數值 陣列
		ArrayList<String> inputSplit;
		String input, str; // 未切割的整行資料 | 單個欄位值
		int index;
		try(BufferedReader br = new BufferedReader(new FileReader(inFile))){ // 讀檔
			inputSplit = Split.withQuotes(br.readLine());	// 分割標題列 | 每筆資料分欄切割後的結果
			index = inputSplit.indexOf(target);	//參照欄位索引
			if(index == -1) {
				throw new Exception("找不到您輸入的欲轉換欄位編號「" + target + "」，請重新確認");
			}
			
			while ((input = br.readLine()) != null) { // 判斷是否已到檔案結尾
				inputSplit = Split.withoutQuotes(input);
				str = inputSplit.get(index);
				if (!str.isEmpty()) {
					numAL.add(Double.valueOf(str));
				}
			}	
		}
		double[] outlier = countOutlier(numAL, Double.parseDouble(multiple));
		System.out.printf("| 計算結果 | 最小離群值：%f | 最大離群值：%f |\n", outlier[0], outlier[1]);
		
		double number;
		try(BufferedReader br = new BufferedReader(new FileReader(inFile))){	// 原資料檔
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))){	// 檔案 覆蓋檔案
			bw.write(br.readLine());// 第一列標題列
			while ((input = br.readLine()) != null) { // 判斷是否已到檔案結尾
				inputSplit = Split.withoutQuotes(input);
				str = inputSplit.get(index);
				if(!str.isEmpty()) {
					number = Double.parseDouble(str);
					if(number < outlier[0] || number > outlier[1]) { //outlier[0](minOutlier) <= number <= outlier[1](maxOutlier) 要輸出
						continue;	//若不在範圍內不輸出
					}
				}
				bw.newLine();
				bw.write(input);
			}	
		}
		}
	}
	
	/**
	 * 計算離群值
	 * @param numAL 數值陣列
	 * @param multiple 幾倍的標準差
	 * @return 下界離群值 | 上界離群值
	 */
	private double[] countOutlier(ArrayList<Double> numAL, double multiple) {
		double average = numAL.stream().mapToDouble(Double::doubleValue).average().getAsDouble();	//平均值
		double sum = numAL.stream().mapToDouble(eachNumber -> Math.pow((eachNumber - average), 2)).sum();
		double sd = Math.sqrt(sum / numAL.size());	//標準差
		double adjustedSD = multiple * sd;
		return new double[] {average - adjustedSD, average + adjustedSD};
	}
}