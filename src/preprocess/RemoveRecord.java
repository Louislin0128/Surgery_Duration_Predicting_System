package preprocess;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class RemoveRecord {
	/**執行{@link RemoveRecord#RemoveRecord RemoveRecord}*/
	public static void exec(String inFile, String title, String content, String outFile) throws Exception {
		new RemoveRecord(inFile, title, content, outFile);
	}
	
	/**
	 * 移除紀錄
	 * @param inFile 輸入檔案
	 * @param target 目標欄位
	 * @param content 目標值
	 * @param outFile 輸出檔案
	 * @throws Exception
	 */
	private RemoveRecord(String inFile, String target, String content, String outFile) throws Exception {
		String specify = content; 	// 欲比較數字
		int mode = -1; 			// 比對模式
		if (content.equalsIgnoreCase("NOTNULL")) { // 若關鍵字為非空值
			mode = 0;
		} else if (content.equalsIgnoreCase("NULL") || content.isEmpty()) { // 若關鍵字為空值
			mode = 1;
		} else if (content.startsWith(">=")) {
			specify = content.substring(2);
			mode = 2;
		} else if (content.startsWith("<=")) {
			specify = content.substring(2);
			mode = 3;
		} else if (content.charAt(0) == '=') {
			specify = content.substring(1);
			mode = 4;
		} else if (content.charAt(0) == '>') {
			specify = content.substring(1);
			mode = 5;
		} else if (content.charAt(0) == '<') {
			specify = content.substring(1);
			mode = 6;
		}
		
		try(BufferedReader br = new BufferedReader(new FileReader(inFile))){ 	//原資料檔
			String input = br.readLine();	//標題 | 未切割的整行資料
			ArrayList<String> inputSplit = Split.withQuotes(input);	//標題分割 | 每筆資料分欄切割後的結果
			int index = inputSplit.indexOf(target);	//目標欄位索引(每行索引值從0開始)
			
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))){	//檔案 覆蓋檔案
			bw.write(input);	//寫入第一列標題列
			while((input = br.readLine()) != null) {	//判斷是否已到檔案結尾
				inputSplit = Split.withQuotes(input);
				if(compare(mode, inputSplit.get(index), specify)) {
					bw.newLine();
					bw.write(input);
				}
			}
		}
		}
	}
	
	private boolean compare(int mode, String content, String specify) {	//搜尋模式 | 檔案指定欄位內容 | 指定字串
		switch (mode) {
		case 0: // NOTNULL
			if (content.isEmpty()) {
				return true;
			}
			break;
		case 1: // NULL
			if (!content.isEmpty()) {
				return true;
			}
			break;
		case 2: // >=
			if (content.isEmpty() || !(Double.parseDouble(content) >= Double.parseDouble(specify))) {
				return true;
			}
			break;
		case 3: // <=
			if (content.isEmpty() || !(Double.parseDouble(content) <= Double.parseDouble(specify))) {
				return true;
			}
			break;
		case 4: // =
			if (content.isEmpty() || !(Double.parseDouble(content) == Double.parseDouble(specify))) {
				return true;
			}
			break;
		case 5: // >
			if (content.isEmpty() || !(Double.parseDouble(content) > Double.parseDouble(specify))) {
				return true;
			}
			break;
		case 6: // <
			if (content.isEmpty() || !(Double.parseDouble(content) < Double.parseDouble(specify))) {
				return true;
			}
			break;
		default:
			if (content.isEmpty() || !content.equals(specify)) {
				return true;
			}
			break;
		}
		return false;
	}
}