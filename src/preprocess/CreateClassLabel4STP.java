package preprocess;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class CreateClassLabel4STP {
	/**執行{@link CreateClassLabel4STP#CreateClassLabel4STP CreateClassLabel4STP}*/
	public static void exec(String inFile, String outFile) throws Exception {
		new CreateClassLabel4STP(inFile, outFile);
	}
	
	/**
	 * 計算手術時間
	 * @param inFile 輸入檔案
	 * @param outFile 輸出檔案
	 * @throws Exception
	 */
	private CreateClassLabel4STP(String inFile, String outFile) throws Exception {
		try(BufferedReader br = new BufferedReader(new FileReader(inFile))){//讀檔
			ArrayList<String> inputSplit;	//每筆資料分欄切割後的結果
			StringBuilder outStr = new StringBuilder();
			int[] spendTimeSplit;	//手術時間 時與分
			int spendTime = 0;		//手術時間(分)
			
			String input = br.readLine();	//未切割的整行資料
			int spendTimeIndex = Split.withQuotes(input).indexOf("手術時間（時:分）");	//找手術時間索引
			outStr.append(input).append(",手術時間（分）");	//補上標題
			
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))){	//寫檔
			String spendTimeStr;
			while((input = br.readLine()) != null) {	//判斷是否已到檔案結尾
				inputSplit = Split.withQuotes(input);	//補回換行符並分割
				
				spendTimeStr = inputSplit.get(spendTimeIndex);
				if(spendTimeStr.contains(":")) {	//若手術時間欄包含「:」則轉換
					spendTimeSplit = Arrays.stream(spendTimeStr.split(":")).mapToInt(Integer::parseInt).toArray();
					//以:為分隔符號 將手術時間的時與分 分開
					spendTime = spendTimeSplit[0] * 60 + spendTimeSplit[1];
				}else spendTime = 0;	//否則直接輸出0
				
				outStr.append('\n').append(input).append(",").append(spendTime);
				bw.write(outStr.toString());
				outStr.setLength(0);
			}	
		}
		}
	}
}