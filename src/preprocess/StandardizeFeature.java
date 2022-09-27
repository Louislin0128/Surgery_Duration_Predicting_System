package preprocess;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class StandardizeFeature {
	/**執行{@link StandardizeFeature#StandardizeFeature StandardizeFeature}*/
	public static void exec(String inTrain, String target, String outTrain, String... test) throws Exception {
		new StandardizeFeature(inTrain, target, outTrain, test);
	}
	
	/**
	 * 標準化
	 * @param inTrain 輸入訓練集
	 * @param target 目標欄位
	 * @param outTrain 輸出訓練集 
	 * @param test 測試集陣列(可選)
	 * @throws Exception
	 */
	private StandardizeFeature(String inTrain, String target, String outTrain, String... test) throws Exception{
		ArrayList<Double> numAL = new ArrayList<Double>();	//目標欄位數值 陣列
		ArrayList<String> inputSplit;
		String input, str;	//未切割的整行資料
		int index;
		try(BufferedReader br = new BufferedReader(new FileReader(inTrain))){//讀檔
			inputSplit = Split.withQuotes(br.readLine());	//分割標題列 | 每筆資料分欄切割後的結果
			index = inputSplit.indexOf(target);	//欲轉換欄位轉換為索引值
			if(index == -1) {	// 若找不到欲轉換欄位
				throw new Exception("找不到您輸入的欲轉換欄位編號「" + target + "」，請重新確認");
			}
			
			while((input = br.readLine()) != null) {	//判斷是否已到檔案結尾
				inputSplit = Split.withQuotes(input);
				str = inputSplit.get(index);
				if(!str.isEmpty()){
					numAL.add(Double.valueOf(str));
				}
			}
		}
		
		HashMap<Double, String> zScoreHM = standardize(numAL);	//將originalHashMap標準化後的HashMap
		// 訓練集 | 測試集 輸出入檔名
		Queue<String> file = new LinkedList<>();
		file.offer(inTrain);
		file.offer(outTrain);
		for(String s: test) {	//加入輸入測試集
			file.offer(s);
		}
		
		StringBuilder outStr = new StringBuilder();
		while(!file.isEmpty()) {
			try(BufferedReader br = new BufferedReader(new FileReader(file.poll()))){ 	//原資料檔
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(file.poll()))){	//檔案 覆蓋檔案
				bw.write(br.readLine());//第一列標題列
				while((input = br.readLine()) != null) {	//判斷是否已到檔案結尾
					inputSplit = Split.withQuotes(input);
					for(int j = 0, size = inputSplit.size(); j < size; j++) {
						str = inputSplit.get(j);
						if(j == index && !str.isEmpty()) {	//是欲轉換欄位且非空植才輸出數值
							outStr.append(zScoreHM.getOrDefault(Double.valueOf(str), "0.0"));	//輸出化數值
						}else {		//否則直接輸出
							outStr.append(str);
						}
						if(j != size - 1) {
							outStr.append(",");
						}
					}
					bw.newLine();
					bw.write(outStr.toString());
					outStr.setLength(0);
				}	
			}
			}
		}
	}
	
	/**計算標準差*/
	private HashMap<Double, String> standardize(ArrayList<Double> numAL) {
		double average = numAL.stream().mapToDouble(Double::doubleValue).average().getAsDouble();	//平均值
		double sum = numAL.stream().mapToDouble(eachNumber -> Math.pow((eachNumber - average), 2)).sum();	//(每個值-平均值)的平方和
		double sd = Math.sqrt(sum / numAL.size());	//標準差
		
		HashMap<Double, String> zScoreHM = new HashMap<Double, String>();
		Double zScore;
		BigDecimal bd;
		for(Double eachNumber: numAL) {
			zScore = Math.abs((eachNumber - average) / sd);
			bd = new BigDecimal(zScore.toString());
			zScoreHM.putIfAbsent(eachNumber, bd.toPlainString());
		}
		return zScoreHM;
	}
}