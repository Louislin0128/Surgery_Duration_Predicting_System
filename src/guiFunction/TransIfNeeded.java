package guiFunction;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class TransIfNeeded {	
	/**
	 * 若手術時間或麻醉時間有做正規化或標準化<br>
	 * 將預測數值給反轉。<br>
	 * <br>
	 * 限制：若對同時做正規化及標準化或多次做<br>
	 * 轉換後的數值將會不準確<br>
	 * 使用者應避免之<br>
	 * 
	 * @param stepFile 步驟檔
	 * @param trainFile 原始訓練集資料
	 * @throws Exception
	 */
	public static TransIfNeeded transValue(String stepFile, String trainFile) throws Exception {
		try(BufferedReader br = new BufferedReader(new FileReader(stepFile))){ // 步驟檔，讀檔
			String input; 	// 未切割的整行資料
			String[] step; 	// 步驟列切割
			while ((input = br.readLine()) != null) { // 判斷是否已到檔案結尾
				step = input.split(",");
				if ((step[0].startsWith("手術時間") || step[0].startsWith("麻醉時間"))) {
					switch(step[2]) {
					case "NormalizeFeature":
						System.err.println("有經過正規化，需轉換");
						return new TransNormalize(trainFile);
					case "StandardizeFeature":
						System.err.println("有經過標準化，需轉換");
						return new TransStandardize(trainFile);
					}
					// 就算後面對手術|麻醉時間做正規化或標準化，也無視之
					// 因為也無法正常轉換數值
				}
			}
		}
		System.out.println("未經過正規化或標準化，不需轉換");
		return new TransIfNeeded();
	}
	
	/**原始方法 若不需要轉換則輸出原始值*/
	public double get(double value) {
		return value;
	}
	
	/**轉換型態*/
	public String getType() {
		return "不需經過轉換";
	}
}

class TransNormalize extends TransIfNeeded {
	private double max = Double.MIN_VALUE, min = Double.MAX_VALUE;
	/**
	 * 若手術時間或麻醉時間有正規化<br>
	 * 將預測數值給反正規化。
	 * 
	 * @param trainFile
	 * @throws Exception
	 */
	protected TransNormalize(String trainFile) throws Exception {
		try(BufferedReader br = new BufferedReader(new FileReader(trainFile))){ //原資料檔，讀檔
			String str, input = br.readLine();	//跳過標題列
			double value;
			while((input = br.readLine()) != null) {	//判斷是否已到檔案結尾
				str = input.substring(input.lastIndexOf(",") + 1);	//最後一個逗號的位置 | 取得數值
				if(!str.isEmpty()) {
					value = Double.parseDouble(str);
					max = (max < value) ? value : max;	//最大值
					min = (min > value) ? value : min;	//最小值
				}
			}
		}
	}
	
	@Override
	public double get(double inValue) {
		return inValue * (max - min) + min;	//視需要將數值反正規化
	}
	
	@Override
	public String getType() {
		return "反正規化";
	}
}

class TransStandardize extends TransIfNeeded {
	private double average, sd;
	protected TransStandardize(String trainFile) throws Exception {
		ArrayList<Double> numAL = new ArrayList<>(); // 目標欄位數值 陣列
		
		try(BufferedReader br = new BufferedReader(new FileReader(trainFile))){ // 原資料檔，讀檔
			String str, input = br.readLine();	//跳過標題列
			while ((input = br.readLine()) != null) { // 判斷是否已到檔案結尾
				str = input.substring(input.lastIndexOf(",") + 1); // 最後一個逗號的位置 | 取得數值
				if (!str.isEmpty()) {
					numAL.add(Double.valueOf(str));
				}
			}	
		}

		average = average(numAL); 	// 取平均值
		sd = sd(numAL, average); 	// 取標準差
	}
	
	@Override
	public double get(double value) {
		return value * average + sd;	// 視需要將數值反標準化
	}
	
	@Override
	public String getType() {
		return "反標準化";
	}
	
	/**
	 * 計算標準差
	 * @param numberList 數值陣列
	 * @param average 平均值
	 * @return 標準差
	 */
	private double sd(ArrayList<Double> numberList, double average) {
		//(每個值-平均值)的平方和
		double sum = numberList.stream().mapToDouble(eachNumber -> Math.pow((eachNumber - average), 2)).sum();
		return Math.sqrt(sum / numberList.size());	//標準差
	}
	
	/**計算平均值*/
	private double average(ArrayList<Double> numberList) {
		return numberList.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
	}
}

//package guiFunction;
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.util.ArrayList;
//
//public class ReverseIfNeeded {
//	private Denormalize denormalize;
//	private Destandardize destandardize;
//	
//	/**
//	 * 轉換實際及預測檔案<br>
//	 * <br>
//	 * 若手術時間或麻醉時間有做正規化或標準化<br>
//	 * 將預測數值給反轉。<br>
//	 * 限制：若對同時做正規化及標準化或多次做<br>
//	 * 轉換後的數值將會不準確<br>
//	 * 使用者應避免之
//	 * 
//	 * @param stepFile 步驟檔
//	 * @param trainFile 原始訓練集資料
//	 * @param inFile 實際預測值輸入檔案
//	 * @param outFile 實際預測值轉換後輸出檔案
//	 * @throws Exception
//	 */
//	public ReverseIfNeeded(String stepFile, String trainFile, String inFile, String outFile) throws Exception {
//		this(stepFile, trainFile);	// 判斷是否需要進行反正規化或反標準化
//		BufferedReader br = new BufferedReader(new FileReader(inFile)); 	// 實際預測值輸入檔案
//		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));	// 實際預測值轉換後輸出檔案
//		String input; 	// 未切割的整行資料
//		String[] split;
//		double actual, predict, deviation;
//		bw.write("實際值,預測值,差值(實際值-預測值)");
//		while((input = br.readLine()) != null) {
//			split = input.split(",");
//			actual = (split[0].isEmpty()) ? Double.NaN : get(Double.parseDouble(split[0]));
//			predict = (split[1].isEmpty()) ? Double.NaN : get(Double.parseDouble(split[1]));
//			deviation = (actual == Double.NaN || predict == Double.NaN) ? Double.NaN : actual - predict;
//			
//			bw.newLine();
//			bw.write((actual == Double.NaN ? "?" : actual) + "," +
//					 (predict == Double.NaN ? "?" : predict) + "," + 
//					 (deviation == Double.NaN ? "?" : deviation));
//		}
//		br.close();
//		bw.close();
//	}
//	
//	/**
//	 * 若手術時間或麻醉時間有做正規化或標準化<br>
//	 * 將預測數值給反轉。<br>
//	 * <br>
//	 * 限制：若對同時做正規化及標準化或多次做<br>
//	 * 轉換後的數值將會不準確<br>
//	 * 使用者應避免之<br>
//	 * 
//	 * @param stepFile 步驟檔
//	 * @param trainFile 原始訓練集資料
//	 * @throws Exception
//	 */
//	public ReverseIfNeeded(String stepFile, String trainFile) throws Exception {
//		BufferedReader br = new BufferedReader(new FileReader(stepFile)); // 步驟檔，讀檔
//		String input; 	// 未切割的整行資料
//		String[] step; 	// 步驟列切割
//		while ((input = br.readLine()) != null) { // 判斷是否已到檔案結尾
//			step = input.split(",");
//			if ((step[0].startsWith("手術時間") || step[0].startsWith("麻醉時間"))) {
//				if(step[2].equals("NormalizeFeature")) {
//					System.out.println("需要反正規化");
//					denormalize = new Denormalize(trainFile);
//					break;
//				}else if(step[2].equals("StandardizeFeature")) {
//					System.out.println("需要反標準化");
//					destandardize = new Destandardize(trainFile);
//					break;
//				}
//				// 就算後面對手術|麻醉時間做正規化或標準化，也無視之
//				// 因為也無法正常轉換數值
//			}
//		}
//		br.close();
//	}
//	
//	public double get(double inValue) {
//		if(denormalize != null) {
//			return denormalize.get(inValue);
//		}else if(destandardize != null) {
//			return destandardize.get(inValue);
//		}
//		return inValue;
//	}
//	
//	public class Denormalize {
//		private double max, min;
//		/**
//		 * 若手術時間或麻醉時間有正規化<br>
//		 * 將預測數值給反正規化。
//		 * 
//		 * @param stepFile
//		 * @param inFile
//		 * @param inValue
//		 * @throws Exception
//		 */
//		public Denormalize(String inFile) throws Exception{	
//			String input;
//			int last;		//最後一個逗號的位置
//			double findMax = Double.MIN_VALUE, findMin = Double.MAX_VALUE, value;
//			String valueStr;
//			BufferedReader br = new BufferedReader(new FileReader(inFile));  //原資料檔，讀檔
//			br.readLine();	//跳過標題列
//			while((input = br.readLine()) != null) {	//判斷是否已到檔案結尾
//				last = input.lastIndexOf(",");
//				valueStr = input.substring(last + 1);	//取得數值
//				if(!valueStr.isEmpty()) {
//					value = Double.parseDouble(valueStr);
//					findMax = (findMax < value) ? value : findMax;
//					findMin = (findMin > value) ? value : findMin;
//				}
//			}
//			br.close(); 		//將檔案資料流關閉
//			max = findMax * 2;	//將最大值提高2倍
//			min = findMin;		//最小值不動
//		}
//		
//		private double get(double inValue) {
//			return inValue * (max - min) + min;	//視需要將數值反正規化
//		}
//	}
//	
//	private class Destandardize {
//		private double m, u;
//		public Destandardize(String inFile) throws Exception {
//			BufferedReader br = new BufferedReader(new FileReader(inFile)); // 原資料檔，讀檔
//			br.readLine();	//跳過標題列
//			String input;
//			int last; 		// 最後一個逗號的位置
//			ArrayList<Double> numAL = new ArrayList<>(); // 目標欄位數值 陣列
//			String valueStr;
//			while ((input = br.readLine()) != null) { // 判斷是否已到檔案結尾
//				last = input.lastIndexOf(",");
//				valueStr = input.substring(last + 1); // 取得數值
//				if (!valueStr.isEmpty()) {
//					numAL.add(Double.parseDouble(valueStr));
//				}
//			}
//			br.close(); // 將檔案資料流關閉
//
//			u = mean(numAL); 	// 取平均值
//			m = std(numAL, u); 	// 取標準差
//		}
//		
//		private double get(double inValue) {
//			return inValue * m + u;	// 視需要將數值反標準化
//		}
//		
//		// 計算標準差
//		private double std(ArrayList<Double> tot, double u) {
//			int n = tot.size();
//			double value = 0;
//			for (int i = 0; i < n; i++) {
//				value += Math.pow((tot.get(i) - u), 2); // (每個值-平均值)的平方的總和
//			}
//			return Math.sqrt(value / n);
//		}
//	
//		// 計算平均值
//		private double mean(ArrayList<Double> tot) {
//			return tot.stream().mapToDouble(d -> d.doubleValue()).average().getAsDouble();
//		}
//	}
//}