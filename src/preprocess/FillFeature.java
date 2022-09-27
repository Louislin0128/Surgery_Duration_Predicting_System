package preprocess;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FillFeature {
	/**執行{@link FillFeature#FillFeature FillFeature}*/
	public static void exec(String in, String out) throws Exception {
		new FillFeature(in, out);
	}
	
	/**
	 * 填補缺失值
	 * @param in 輸入路徑
	 * @param out 輸出路徑
	 * @throws Exception
	 */
	private FillFeature(String in, String out) throws Exception {
		Path outPath = Paths.get(out);
		Files.walkFileTree(Paths.get(in), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				try(BufferedReader br = Files.newBufferedReader(file, Charset.defaultCharset())){
					String[] inputSplit;//每筆資料分欄切割後的結果
					String input, date = "", room = "";		//未切割的整行資料 | 日期 | 室別
					StringBuilder outStr = new StringBuilder();
					
				try(BufferedWriter bw = Files.newBufferedWriter(outPath.resolve(file.getFileName()), Charset.defaultCharset())){
					bw.write(br.readLine());	//第一列標題列
					while((input = br.readLine()) != null) {	//判斷是否已到檔案結尾
						inputSplit = input.split(",", 3);		//將每列切成三份
						if(!inputSplit[0].isEmpty()) {	//如果該欄有日期
							date = inputSplit[0];		//將該欄日期放入date
						}
						if(!inputSplit[1].isEmpty()) {	//如果該欄有室別
							room = inputSplit[1];		//將該欄室別放入room
						}
						
						outStr.append(date).append(",").append(room).append(",").append(inputSplit[2]);
						bw.newLine();
						bw.write(outStr.toString());			
						outStr.setLength(0);
					}		
				}
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}
}