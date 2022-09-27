package guiFunction;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * 建立各種不同的檔案<br><br>
 * toText：輸出字串<br>
 * forWekaLoader：產生給CSVLoader的檔案<br>
 * buildOptions：按下主頁面預測按鈕時，需要將所有選項寫成一個檔案供後續轉換<br>
 * forLSH：建立流水號檔案<br>
 * disOrganize：打亂資料集
 */
public class CreateFile {
	/**
	 * 複製並取代
	 * @param inFile 輸入檔案
	 * @param outFile 輸出檔案
	 * @throws IOException
	 */
	public static void copy(String inFile, String outFile) throws IOException {
		Files.copy(Paths.get(inFile), Paths.get(outFile), StandardCopyOption.REPLACE_EXISTING);
	}
	
	/**
	 * 複製並取代
	 * @param inFile 輸入檔案
	 * @param outFile 輸出檔案
	 * @throws IOException
	 */
	public static void copy(Path inFile, String outFile) throws IOException {
		Files.copy(inFile, Paths.get(outFile), StandardCopyOption.REPLACE_EXISTING);
	}
	
	/**
	 * 複製並取代
	 * @param inFile 輸入檔案
	 * @param outFile 輸出檔案
	 * @throws IOException
	 */
	public static void copy(File inFile, String outFile) throws IOException {
		Files.copy(inFile.toPath(), Paths.get(outFile), StandardCopyOption.REPLACE_EXISTING);
	}
	
	/**
	 * 輸出字串
	 * @param outFile 輸出檔案
	 * @param text 文字
	 * @throws IOException
	 */
	public static void toCSV(String outFile, String text) throws IOException {
		Files.writeString(Paths.get(outFile), text, Charset.defaultCharset(), StandardOpenOption.CREATE);
	}
	
	/**
	 * 產生給CSVLoader的檔案
	 * @param inFile 輸入檔案
	 * @param outFile 輸出檔案
	 * @throws Exception
	 */
	public static void forWekaLoader(String inFile, String outFile) throws FileNotFoundException, IOException {
		try(BufferedReader br = new BufferedReader(new FileReader(inFile))){
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))){
			bw.write(br.readLine()); // 印出標題
			String input;
			while ((input = br.readLine()) != null) {
				bw.write(addDoubleQuotes(input));
			}
		}
		}
	}
	/**在單個字串前後加上"*/
	private static String addDoubleQuotes(String input) { 	// 在每行所有值前後加上" | 未切割的整行資料
		boolean inQuotes = false; 					// 判斷某逗號是否在雙引號中
		StringBuilder tempString = new StringBuilder("\n\""); // 存放逐字元判斷所串接的字串 | 首欄補回換行符及"
		for (char c : input.toCharArray()) { 		// 將input轉換為字元陣列並依序讀出
			if (c == ',' && !inQuotes)
				tempString.append("\",\"");
			else if (c == '\"')						// 雙引號
				inQuotes = !inQuotes;
			else
				tempString.append(c);				// 其他字元加到tempString中
		}
		tempString.append('\"'); 					// 補回"
		return tempString.toString();
	}
	
	/**
	 * 按下主頁面預測按鈕時，需要將所有選項寫成一個檔案供後續轉換
	 * @param title
	 * @param options
	 * @param outFile
	 * @throws IOException
	 */
	public static void buildOptions(String[] title, String[] options, String outFile) throws IOException {
		StringBuilder tempStr = new StringBuilder(String.join(",", title));
		tempStr.append(",未知時間\n");
		for(String s: options) {
			if(s.contains(",")) {
				tempStr.append("\"").append(s).append("\""); // 寫入內容 | 以"包覆含有,的項目
			}else {
				tempStr.append(s);
			}
			tempStr.append(",");
		}
		tempStr.append("0.0");
		toCSV(outFile, tempStr.toString());
	}
	
	private static SecureRandom random = new SecureRandom();
	/**
	 * 打亂資料集
	 * @param inFile 原資料集
	 * @param number 種子碼
	 * @param outfile 打亂資料集的檔案路徑
	 * @throws Exception
	 */
	public static void disOrganize(String inFile, String number, String outfile) throws Exception {
		if (number != null && !number.isEmpty()) {
			random.setSeed(Long.parseLong(number)); // 設定種子碼
		}
		List<String> content;
		try(Stream<String> stream = Files.lines(Paths.get(inFile), Charset.defaultCharset())){
			content = stream.skip(1).toList();
		}
		String title;
		try(BufferedReader br = new BufferedReader(new FileReader(inFile))) {
			title = br.readLine();
		}
		Collections.shuffle(content, random);
		
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outfile))) { // 寫入檔案
			bw.write(title); // 輸出標題
			for (String s: content) {
				bw.newLine();
				bw.write(s); // 輸出打亂後的內容
			}
		}
	}
}
