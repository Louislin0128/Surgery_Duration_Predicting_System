package preprocess;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class NormalizeFeature {
	/**執行{@link NormalizeFeature#NormalizeFeature NormalizeFeature}*/
	public static void exec(String inTrain, String target, String outTrain, String... test) throws Exception {
		new NormalizeFeature(inTrain, target, outTrain, test);
	}
	
	/**
	 * 正規化
	 * @param inTrain 輸入訓練集
	 * @param target 目標欄位
	 * @param outTrain 輸出訓練集 
	 * @param test 測試集陣列(可選)
	 * @throws Exception
	 */
	private NormalizeFeature(String inTrain, String target, String outTrain, String... test) throws Exception {
		ArrayList<String> inputSplit;	//每筆資料分欄切割後的結果
		String input, str;	//未切割的整行資料
		double max = Double.MIN_VALUE, min = Double.MAX_VALUE, value;
		int index;	//欲轉換欄位轉換為索引值
		try(BufferedReader br = new BufferedReader(new FileReader(inTrain))){	//讀資料檔
			input = br.readLine();	//讀取標題列
			inputSplit = Split.withQuotes(input);	//分割標題列 | 每筆資料分欄切割後的結果
			index = inputSplit.indexOf(target);	
			if(index == -1) {	// 若找不到欲轉換欄位
				throw new Exception("在輸入檔找不到您輸入的欲轉換欄位編號「" + target + "」，請重新確認");
			}
			
			while((input = br.readLine()) != null) {//判斷是否已到檔案結尾
				inputSplit = Split.withQuotes(input);
				str = inputSplit.get(index);
				if(!str.isEmpty()) {
					value = Double.parseDouble(str);
					max = (max < value) ? value : max;
					min = (min > value) ? value : min;	
				}
			}	
		}
		double deviation = max - min;	//最大最小值的差值
		if(deviation == 0.0) {
			throw new Exception(target + "欄位的差值為0，無法產生結果。");
		}
		
		// 訓練集 | 測試集 輸出入檔名
		Queue<String> file = new LinkedList<>();
		file.offer(inTrain);
		file.offer(outTrain);
		for(String s: test) {	//加入輸入測試集
			file.offer(s);
		}
		
		StringBuilder outStr = new StringBuilder();
		BigDecimal bd;
		Double normNumber;
		while(!file.isEmpty()) {
			try(BufferedReader br = new BufferedReader(new FileReader(file.poll()))){ 	//原資料檔
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(file.poll()))){	//檔案 覆蓋檔案
				bw.write(br.readLine());					//第一列標題列
				while((input = br.readLine()) != null) {	//判斷是否已到檔案結尾
					inputSplit = Split.withQuotes(input);
					for(int j = 0, size = inputSplit.size(); j < size; j++) {
						str = inputSplit.get(j);
						if(j == index && !str.isEmpty()) {	//是欲轉換欄位且非空植才輸出數值
							value = Double.parseDouble(str);
							normNumber = (value - min) / deviation;
							bd = new BigDecimal(normNumber.toString());
							outStr.append(bd.toPlainString());	//輸出化數值
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
}