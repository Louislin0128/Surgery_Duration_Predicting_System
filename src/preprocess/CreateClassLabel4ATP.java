package preprocess;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class CreateClassLabel4ATP {
	/**執行{@link CreateClassLabel4ATP#CreateClassLabel4ATP CreateClassLabel4ATP}*/
	public static void exec(String inFile, String outFile) throws Exception {
		new CreateClassLabel4ATP(inFile, outFile);
	}
	
	/**
	 * 計算麻醉時間
	 * @param inFile 輸入檔案
	 * @param outFile 輸出檔案
	 * @throws Exception
	 */
	private CreateClassLabel4ATP(String inFile, String outFile) throws Exception {
		try(BufferedReader br = new BufferedReader(new FileReader(inFile))){
			ArrayList<String> inputSplit;//每筆資料分欄切割後的結果
			StringBuilder outStr = new StringBuilder();
			int[] anaesStartSplit;		//麻醉開始 時與分
			int[] anaesEndSplit;		//麻醉結束 時與分
			int[] spendTimeSplit;		//手術時間 時與分
			int anaesSpendTime = 0;		//麻醉時間
			
			String input = br.readLine();	//未切割的整行資料
			inputSplit = Split.withQuotes(input);
			int anaesStartIndex = inputSplit.indexOf("麻醉開始");	//找麻醉開始索引
			int anaesEndIndex = inputSplit.indexOf("麻醉結束");	//找麻醉結束索引
			int spendTimeIndex = inputSplit.indexOf("手術時間（時:分）");	//找手術時間索引
			outStr.append(input).append(",麻醉時間（分）");		//補上標題
			
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))){
			String anaesStartStr, anaesEndStr, spendTimeStr;
			while((input = br.readLine()) != null) {	//判斷是否已到檔案結尾
				inputSplit = Split.withQuotes(input);	//補回換行符並分割
				
				anaesStartStr = inputSplit.get(anaesStartIndex);
				anaesEndStr = inputSplit.get(anaesEndIndex);
				spendTimeStr = inputSplit.get(spendTimeIndex);
				if(!anaesStartStr.isEmpty() &&	// 若手術開始時間不為空
				   !anaesEndStr.isEmpty() &&	// 若手術結束時間不為空
				   !spendTimeStr.isEmpty()) {	// 若手術時間不為空
					anaesStartSplit = Arrays.stream(anaesStartStr.split(":")).mapToInt(Integer::parseInt).toArray();	//將麻醉開始分割並轉為整數陣列 時與分
					anaesEndSplit = Arrays.stream(anaesEndStr.split(":")).mapToInt(Integer::parseInt).toArray();		//將麻醉結束分割並轉為整數陣列 時與分
					spendTimeSplit = Arrays.stream(spendTimeStr.split(":")).mapToInt(Integer::parseInt).toArray();		//將手術時間分割並轉為整數陣列 時與分
					
					if((anaesStartSplit[0] > anaesEndSplit[0]) || ((anaesStartSplit[0] == anaesEndSplit[0]) && (anaesStartSplit[1] > anaesEndSplit[1]))) {
						//如果開始時間(時)>結束時間(時) 或 (開始時間(時)等於結束時間(時) 及 開始時間(分)>結束時間(分))
				    	anaesEndSplit[0] += 24;	//結束時間(時)+24
					}
	
					anaesSpendTime = (anaesEndSplit[0] * 60 + anaesEndSplit[1]) - (anaesStartSplit[0] * 60 + anaesStartSplit[1]);
					if((spendTimeSplit[0] * 60 + spendTimeSplit[1]) > anaesSpendTime){
						anaesSpendTime += 1440;		//若手術時間大於麻醉時間，加1440分
					}
				}else anaesSpendTime = 0;			
				
				outStr.append('\n').append(input).append(",").append(anaesSpendTime);
				bw.write(outStr.toString());
				outStr.setLength(0);
			}	
		}
		}
	}
}