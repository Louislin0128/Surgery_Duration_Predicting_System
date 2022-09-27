package preprocess;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class AppendRecord {
	/**執行{@link AppendRecord#AppendRecord AppendRecord}*/
	public static void exec(String[] inFile, String outFile) throws Exception {
		new AppendRecord(inFile, outFile);
	}
	
	/**
	 * 執行AppendRecord
	 * @param inFile 輸入檔案
	 * @param outFile 輸出檔案
	 * @throws Exception
	 */
	private AppendRecord(String[] inFile, String outFile) throws Exception {
		Files.copy(Paths.get(inFile[0]), Paths.get(outFile), StandardCopyOption.REPLACE_EXISTING);
		int length = inFile.length;
		if(length == 1) {	//若只有一個輸入檔案
			return;			//結束執行
		}
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outFile, true))){
			String input;
			for(int i = 1; i < length; i++) {
				try(BufferedReader br = new BufferedReader(new FileReader(inFile[i]))) {
					input = br.readLine();//跳過標題列
					while((input = br.readLine()) != null) {
						bw.newLine();
						bw.write(input);
					}
				}
			}
		}
	}
}