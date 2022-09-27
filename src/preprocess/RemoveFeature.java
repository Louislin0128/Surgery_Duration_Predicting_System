package preprocess;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class RemoveFeature {
	/**
	 * 執行{@link RemoveFeature#RemoveFeature RemoveFeature}
	 * @param remove 移除欄標題可變長度陣列
	 */
	public static void exec(String inFile, String outFile, ArrayList<String> remove) throws Exception {
		new RemoveFeature(inFile, remove.toArray(new String[remove.size()]), outFile);
	}
	
	/**
	 * 執行{@link RemoveFeature#RemoveFeature RemoveFeature}
	 * @param remove 移除欄標題可變長度陣列
	 */
	public static void exec(String inFile, String outFile, String... remove) throws Exception {
		new RemoveFeature(inFile, remove, outFile);
	}
	
	/**
	 * 移除欄位
	 * @param inFile 輸入檔案
	 * @param remove 移除欄標題陣列
	 * @param outFile 輸出檔案
	 * @throws Exception
	 */
	private RemoveFeature(String inFile, String[] remove, String outFile) throws Exception {
		try(BufferedReader br = new BufferedReader(new FileReader(inFile))){ 	//原資料檔
			String input = br.readLine();	//第一列標題列 | 未切割的整行資料
			ArrayList<String> inputSplit = Split.withQuotes(input);	//分割標題列 | 每筆資料分欄切割後的結果
			int length = remove.length;
			int[] index = new int[length];
			for(int i = 0; i < length; i++) {
				index[i] = inputSplit.indexOf(remove[i]);//將欲移除欄位依序轉換為索引值
			}
			Arrays.sort(index);	// 升排序
			
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))){	//輸出檔案 覆蓋檔案
			bw.write(outputLine(inputSplit, index));	//輸出標題
			while((input = br.readLine()) != null) {	//判斷是否已到檔案結尾
				inputSplit = Split.withQuotes(input);
				bw.newLine();
				bw.write(outputLine(inputSplit, index));
			}
		}	
		}
	}
	
	private String outputLine(ArrayList<String> inputSplit, int[] index) {
		StringBuilder tempString = new StringBuilder();
		int inputSize = inputSplit.size();
		int dynamic = inputSize - 1;	//預設為沒有移除最後的欄位 | 每行倒數第一個不加","
		int indexLength = index.length;
		if(index[indexLength -1] == inputSize - 1) {	//若使用者欲移除最後的欄位
			dynamic -= 1;	//更改調整值 | 每行倒數第二個不加","
		}
		int n = 0;
		for(int i = 0; i < inputSize; i++) {
			if(indexLength > n && i == index[n]) {
				n += 1;
			}else {		//如果為非指定移除欄位 輸出
				tempString.append(inputSplit.get(i));
				if(i != dynamic) {
					tempString.append(",");
				}
			}
		}
		return tempString.toString();
	}
}
