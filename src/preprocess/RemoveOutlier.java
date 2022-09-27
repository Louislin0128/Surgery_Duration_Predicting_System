package preprocess;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;

public class RemoveOutlier {//麻醉 anes
	/**執行{@link RemoveOutlier#RemoveOutlier RemoveOutlier}*/
	public static void exec(String inFile, String remove, String outFile) throws Exception {
		new RemoveOutlier(inFile, remove, outFile);
	}
	
	/**
	 * 移除離群值
	 * @param inFile 輸入檔案
	 * @param target 移除目標欄位
	 * @param outFile 輸出檔案
	 * @throws Exception
	 */
	private RemoveOutlier(String inFile, String target, String outFile) throws Exception {
		ArrayList<String> inputSplit;
		int index;
		double number = 0.0;	//數值
		String input, str;			//未切割的整行資料
		ArrayList<Double> numberAL = new ArrayList<Double>();	//數值陣列 計算四分位數
		
		try(BufferedReader br = new BufferedReader(new FileReader(inFile))){	//讀檔 原資料檔
			inputSplit = Split.withQuotes(br.readLine());	//分割標題列 | 每筆資料分欄切割後的結果
			index = inputSplit.indexOf(target);		//參照欄位索引
			if(index == -1) {
				throw new Exception("找不到您輸入的欲轉換欄位編號「" + target + "」，請重新確認");
			}
			
			while((input = br.readLine()) != null) {	//判斷是否已到檔案結尾
				inputSplit = Split.withQuotes(input);
				str = inputSplit.get(index);
				if(!str.isEmpty()) {
					number = Double.parseDouble(str);	//數值
					numberAL.add(number);
				}
			}	
		}
		
		double[] outlier = countOutlier(numberAL);	//存放小&大離群值
		System.out.printf("| 計算結果 | 最小離群值：%f | 最大離群值：%f |\n", outlier[0], outlier[1]);
		
		try(BufferedReader br = new BufferedReader(new FileReader(inFile))){ //原資料檔
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))){//寫檔 覆蓋檔案
			bw.write(br.readLine());					//第一列標題列		
			while((input = br.readLine()) != null) {	//判斷是否已到檔案結尾
				inputSplit = Split.withQuotes(input);
				str = inputSplit.get(index);
				if(!str.isEmpty()) {	//若目標欄位非空才進行判斷
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
	
	private double[] countOutlier(ArrayList<Double> numberAL){
		double size = numberAL.size();
		double Q1Index = size * 0.25;	//firstQuartileIndex
		double Q3Index = size * 0.75;	//thirdQuartileIndex
		double Q1 = 0.0;				//firstQuartile
		double Q3 = 0.0;				//thirdQuartile
		double IQR = 0.0;
		double maxOutlier = 0.0;
		double minOutlier = 0.0;
		
		Collections.sort(numberAL);	//小到大排列
		if(Double.toString(Q1Index).contains(".0")) {	//若firstQuartileIndex為整數
			Q1 = (numberAL.get((int) Q1Index - 1) + numberAL.get((int) Q1Index)) / 2;
		}else {	//若firstQuartileIndex不為整數
			Q1 = numberAL.get((int) (Math.ceil(Q1Index)) - 1);	//無條件進位
		}
		if(Double.toString(Q3Index).contains(".0")) {	//若thirdQuartileIndex為整數
			Q3 = (numberAL.get((int) Q3Index - 1) + numberAL.get((int) Q3Index)) / 2;
		}else {	//若thirdQuartileIndex不為整數
			Q3 = numberAL.get((int) (Math.ceil(Q3Index)) - 1);	//無條件進位
		}
		IQR = Q3 - Q1;
		minOutlier = Q1 - (IQR * 1.5);
		maxOutlier = Q3 + (IQR * 1.5);
		
		return new double[] {minOutlier, maxOutlier};
	}
}