package guiFunction;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

/**標題編號相關*/
public class TitleIndex {
	/**
	 * 為標題編號<br>
	 * 若原來已經有標題編號，則會重新編號
	 * @param inFile 輸入檔案
	 * @param outFile 輸出檔案
	 * @throws Exception
	 */
	public static void append(String inFile, String outFile) throws Exception {
		rearrage(new File(inFile), outFile, true);
	}
	
	/**
	 * 為標題編號<br>
	 * 若原來已經有標題編號，則會重新編號
	 * @param inFile 輸入檔案
	 * @param outFile 輸出檔案
	 * @throws Exception
	 */
	public static void append(File inFile, String outFile) throws Exception {
		rearrage(inFile, outFile, true);
	}
	
	/**
	 * 移除標題編號
	 * @param inFile 輸入檔案
	 * @param outFile 輸出檔案
	 * @throws Exception
	 */
	public static void remove(String inFile, String outFile) throws Exception {
		rearrage(new File(inFile), outFile, false);
	}
	
	/**
	 * 為標題編號或移除標題編號
	 * @param inFile 輸入檔案
	 * @param outFile 輸出檔案
	 * @param add true：為標題編號；false：移除標題編號
	 * @throws Exception
	 */
	private static void rearrage(File inFile, String outFile, boolean add) throws Exception {
		try(BufferedReader br = new BufferedReader(new FileReader(inFile))) {
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))) {
			if(add) {	//在每個標題後加上欄號
				bw.write(rearrangeTitle(splitInput(br.readLine()))); 		// 輸出改變後的標題
			}else {		//只輸出標題(不包含欄號)
				bw.write(String.join(",", splitInput(br.readLine())));
			}
			String input;
			while ((input = br.readLine()) != null) { 	// 輸出剩餘內容
				bw.newLine();
				bw.write(input);
			}	
		}
		}
	}
	
	/**該類別專用的切割字串函式*/
	private static ArrayList<String> splitInput(String input) {	//未切割的整行資料
		boolean inQuotes = false;								//判斷某逗號是否在雙引號中
		boolean inBrackets = false;								//判斷某字串是否在括號中
		StringBuilder tempString = new StringBuilder();			//存放逐字元判斷所串接的字串
		ArrayList<String> inputSplit = new ArrayList<String>();	//每筆資料分欄切割後的結果
		for (char c: input.toCharArray()) {	//將input轉換為字元陣列並依序讀出
			if(c == ',' && !inQuotes) {
				inputSplit.add(tempString.toString());
			    tempString.setLength(0);	//將tempString清空
			}else if(c == '\"') {			//雙引號
				tempString.append(c);		//補回"
				inQuotes = !inQuotes;
			}else if(c == '(' || c == ')') {
				inBrackets = !inBrackets;
			}else if(!inBrackets) {
				tempString.append(c);		//其他字元加到tempString中
			}
		}
		inputSplit.add(tempString.toString());
		return inputSplit;
	}
	
	/**將索引值(欄號)轉為欄名*/
	private static String rearrangeTitle(ArrayList<String> title) {
		String indexName;
		StringBuilder newTitle = new StringBuilder();
		for (int i = 0, size = title.size(); i < size; i++) {
			indexName = "";
			for (int index = i; index >= 0; index = index / 26 - 1) {
				indexName = (char)((char)(index % 26) + 'A') + indexName;
	        }
			
			newTitle.append(title.get(i))
					.append('(')
					.append(indexName)
					.append(')');
			if(i != size - 1) {
				newTitle.append(',');
			}		
		}
		return newTitle.toString();
	}
}
