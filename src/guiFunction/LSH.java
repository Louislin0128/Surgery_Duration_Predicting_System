package guiFunction;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedList;

import preprocess.Split;

/**與流水號相關的操作*/
public class LSH {
	/**
	 * 在列首加上流水號
	 * @param inFile 輸入檔名
	 * @param outFile 輸出檔名
	 * @throws Exception
	 */
	public static void append(String inFile, String outFile) throws Exception {
		append(new File(inFile), outFile);
	}
	
	/**
	 * 在列首加上流水號
	 * @param inFile 輸入檔名
	 * @param outFile 輸出檔名
	 * @throws Exception
	 */
	public static void append(File inFile, String outFile) throws Exception {
		try(LineNumberReader lnr = new LineNumberReader(new FileReader(inFile))){
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))){
			String input = lnr.readLine();	// 讀取標題列
			if(input.startsWith("流水號")) {	// 原有流水號重新加流水號
				bw.write(input);	// 輸出原標題列(包含流水號)
				while((input = lnr.readLine()) != null) {
					bw.newLine();	// 換行
					bw.write((lnr.getLineNumber() - 1) + "," + removeFirstCol(input));// 輸出加流水號的原資料
				}
			}else {
				bw.write("流水號," + input);	//輸出標題列
				while((input = lnr.readLine()) != null) {
					bw.newLine();	// 換行
					bw.write((lnr.getLineNumber() - 1) + "," + input);	// 輸出原資料+流水號
				}
			}	
		}
		}
	}
	/**
	 * 在列首移除流水號
	 * @param inFile 輸入檔名
	 * @param outFile 輸出檔名
	 * @throws Exception
	 */
	public static void remove(String inFile, String outFile) throws Exception {
		remove(new File(inFile), outFile);
	}
	
	/**
	 * 在列首移除流水號
	 * @param inFile 輸入檔名
	 * @param outFile 輸出檔名
	 * @throws Exception
	 */
	public static void remove(File inFile, String outFile) throws Exception {
		try(LineNumberReader lnr = new LineNumberReader(new FileReader(inFile))){
			String input = lnr.readLine();	// 讀取標題列
			if(input.startsWith("流水號")) {
				try(BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))){
					bw.write(removeFirstCol(input));	// 輸出原標題列(不包含流水號)
					while((input = lnr.readLine()) != null) {
						bw.newLine();	// 換行
						bw.write(removeFirstCol(input));// 輸出不加流水號的資料
					}	
				}
			}else {	//沒有流水號又不要加流水號，將輸入檔直接複製到目的地
				Files.copy(inFile.toPath(), Paths.get(outFile), StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}
	
	/**
	 * 移除首欄的字串
	 * @param input 欲處理的字串
	 * @return 回傳移除首欄的字串
	 */
	private static String removeFirstCol(String input) {
		return input.substring(input.indexOf(",") + 1);
	}
	
	/**
	 * 建立流水號檔案
	 * @param featureSelect 特徵選取
	 * @param dataTransform 資料轉檔
	 * @param outFile 輸出檔案
	 * @throws Exception
	 */
	public static void build(String featureSelect, String dataTransform, String outFile) throws Exception {	
		String fsLSH, dtLSH;				// 特徵選取 流水號 | 資料轉檔 流水號
		String fsLine, dtLine;				// 特徵選取 整列 | 資料轉檔 整列
		ArrayList<String> fsSplit, dtSplit;	// 特徵選取 分割 | 資料轉檔 分割
		LinkedList<String> outLine = new LinkedList<>();
		ArrayList<Integer> keep = new ArrayList<>();
		
		try(BufferedReader fsReader = new BufferedReader(new FileReader(featureSelect))){// 特徵選取
			fsLine = fsReader.readLine(); 		// 讀取特徵選取標題列
			fsSplit = Split.withQuotes(fsLine);	// 分割特徵選取標題列	
		try(BufferedReader dtReader = new BufferedReader(new FileReader(dataTransform))){// 資料轉檔
			dtLine = dtReader.readLine();		// 讀取資料轉檔標題列
			dtSplit = Split.withQuotes(dtLine);	// 分割資料轉檔標題列
			
			// 遍歷資料轉檔標題列，搜尋特徵選取有無該欄位。有則輸出該標題欄，並加入keep(ArrayList)
			for(int i = 1, size = dtSplit.size(); i < size; i++) {	// 遍歷不包含流水號
				if(fsSplit.contains(dtSplit.get(i))) {
					outLine.add(dtSplit.get(i));
					keep.add(i);
				}
			}
			fsLine = fsReader.readLine();		// 讀取特徵選取第一列
			fsSplit = Split.withQuotes(fsLine);	// 分割特徵選取第一列
			fsLSH = fsSplit.get(0);				// 取得特徵選取 首欄流水號
			
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))){ // 輸出檔案 覆蓋檔案
			bw.write(String.join(",", outLine));// 輸出資料轉檔標題列
			outLine.clear();	// 清空輸出暫存區
			
			while ((dtLine = dtReader.readLine()) != null) {	// 讀資料轉檔
				dtSplit = Split.withQuotes(dtLine);				// 分割
				dtLSH = dtSplit.get(0);	// 取得資料轉檔 首欄流水號
				
				if (dtLSH.equals(fsLSH)) {
					bw.newLine();
					for(int i = 1, size = dtSplit.size(); i < size; i++) {	// 遍歷不包含流水號
						if(keep.contains(i)) {			// 若該欄位在保留名單則輸出
							outLine.add(dtSplit.get(i));
						}
					}
					bw.write(String.join(",", outLine));// 輸出資料轉檔
					outLine.clear();	// 清空輸出暫存區
					
					if((fsLine = fsReader.readLine()) == null) break;	//若特徵選取讀取完畢，不搜尋了
					fsSplit = Split.withQuotes(fsLine);
					fsLSH = fsSplit.get(0);	// 取得特徵選取 首欄流水號	
				}
			}	
		}
		}
		}
	}
}
