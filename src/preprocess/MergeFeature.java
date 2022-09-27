package preprocess;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MergeFeature {
	/**執行{@link MergeFeature#MergeFeature MergeFeature}*/
	public static void exec(String inFile, String monthPath, String doctorPath, String outFile) throws Exception {
		new MergeFeature(inFile, monthPath, doctorPath, outFile);
	}
	
	/**
	 * 合併年月報表
	 * @param inFile 輸入檔名
	 * @param monthPath 月報表目錄
	 * @param doctorPath 醫師目錄
	 * @param outFile 輸出檔名
	 * @throws Exception
	 */
	private MergeFeature(String inFile, String monthPath, String doctorPath, String outFile) throws Exception {
		try(BufferedReader readYearFile = new BufferedReader(new FileReader(inFile))){	//年報表檔名 | 讀取年報表
			Year Year = new Year(monthPath, doctorPath, Split.withQuotes(readYearFile.readLine()));//年報表路徑 | 醫師年資路徑 | 月報表路徑
		try(BufferedWriter writeFile = new BufferedWriter(new FileWriter(outFile))){//寫入年報表
			writeFile.write(Year.wholeTitle);
			String yearInput;		//年報表 未切割的整行資料
			while((yearInput = readYearFile.readLine()) != null) {		//讀年報表 判斷是否已到檔案結尾
				writeFile.newLine();
				writeFile.write(Year.createWholeLine(yearInput));			
			}
		}
		}
	}
	
	private class Year{
		final String wholeTitle;	//合併報表 標題列
		final int divisionIndex;	//年報表的科別名稱 索引值
		final int dateIndex;		//年報表的手術日期 索引值
		final int numberIndex;		//年報表的病歷號碼 索引值
		final int operationIndex;	//年報表的手術名稱 索引值
		final int doctorIndex;		//年報表的醫師姓名 索引值
		String division;			//年報表 科別名稱
		String date;				//年報表 手術日期
		String number;				//年報表 病歷號碼
		String operation;			//年報表 手術名稱
		String doctorName;			//年報表 醫師姓名
		StringBuilder line = new StringBuilder();
		ArrayList<String> inputSplit;
		
		final Doctor doctor;
		final Month month;
		
		public Year(String monthPath, String doctorPath, ArrayList<String> yearTitle) throws Exception {
			doctor = new Doctor(doctorPath);
			month = new Month(monthPath);
			
			divisionIndex = yearTitle.indexOf("科別");		//年報表的科別名稱 索引值
			dateIndex = yearTitle.indexOf("手術日");			//年報表的手術日期 索引值
			numberIndex = yearTitle.indexOf("病歷號");		//年報表的病歷號碼 索引值
			operationIndex = yearTitle.indexOf("手術名稱");	//年報表的手術名稱 索引值
			doctorIndex = yearTitle.indexOf("主治醫師");		//年報表的醫師姓名 索引值
			for(int i = 0; i < yearTitle.size(); i++) {
				line.append(yearTitle.get(i));
				if(i == doctorIndex) {		//在醫師姓名欄位旁加上「醫師年資(月)」欄
					line.append(",醫師年資（月）");
				}
				line.append(",");
			}
			line.append(month.title);		//輸出月報表標題
			wholeTitle = line.toString();
		}
		
		private String createWholeLine(String yearLine) throws Exception {
			inputSplit = Split.withQuotes(yearLine);			//年報表 每筆資料分欄切割後的結果 存入yearInputSplit
			division = inputSplit.get(divisionIndex);	//年報表 科別
			division = (division.equals("OBS")) ? "GYN" : division;	//有OBS的資料就去GYN資料夾找
			date = inputSplit.get(dateIndex);			//年報表 手術日期
			number = inputSplit.get(numberIndex);		//年報表 病歷號碼
			operation = inputSplit.get(operationIndex);	//年報表 手術名稱
			doctorName = inputSplit.get(doctorIndex);		//年報表 醫師姓名
			
			line.setLength(0);	//清空字串
			for(int i = 0; i < inputSplit.size(); i++) {
				line.append(inputSplit.get(i));	//輸出年報表
				if(i == doctorIndex) {	//當輸出主治醫師
					line.append(",").append(doctor.countSeniority(doctorName, date));	//連帶輸出醫師的年資(單位：月)
				}
				line.append(",");
			}
			line.append(month.findLine(division, date, number, operation));
			
			return line.toString();
		}
	}
	
	private class Doctor{
		final File[] files;		//醫師年資
		int nameIndex = 0;				//醫師年資的醫師姓名 索引值
		int onBoardDateIndex = 0;		//醫師年資的到院日期 索引值
		String input;					//醫師年資 未切割的整行資料
		String seniority;				//醫師的年資
		String onBoardDate;				//醫師年資的到院日期
		LocalDate oldDate;				//醫師年資的到院日期
		LocalDate newDate;				//年報表的手術日期
		Period period;					//年資區間
		ArrayList<String> inputSplit;	//醫師年資 每筆資料分欄切割後的結果
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		
		public Doctor(String doctorPath) throws Exception {
			files = new File(doctorPath).listFiles();	//建立醫師年資的所有資料清單 方便遍歷			
		}
		
		private String countSeniority(String yearDoctor, String yearDate) throws Exception {
			if(yearDoctor.isEmpty() || yearDate.isEmpty()) {//醫師姓名和手術日期都要有值才計算醫師年資
				return "";
			}
			
			for(File eachFile: files) {				//遍歷醫師年資檔案
				try(BufferedReader readDoctorFile = new BufferedReader(new FileReader(eachFile))){ //讀取醫師年資
					inputSplit = Split.withQuotes(readDoctorFile.readLine());	//分割醫師年資標題列
					nameIndex = inputSplit.indexOf("姓名");					//醫師年資的醫師姓名 索引值
					onBoardDateIndex = inputSplit.indexOf("到院日");			//醫師年資的醫師到院日期 索引值
					
					while((input = readDoctorFile.readLine()) != null) {		//讀醫師年資 判斷是否已到檔案結尾
						inputSplit = Split.withQuotes(input);					//分割doctorInput 存入doctorInputSplit
						if(inputSplit.get(nameIndex).equals(yearDoctor)) {		//若醫師年資的醫師姓名與年報表的匹配
							onBoardDate = inputSplit.get(onBoardDateIndex);		//醫師年資 到院日期(年-月-日)			
							oldDate = LocalDate.parse(onBoardDate, formatter);
					        newDate = LocalDate.parse(yearDate, formatter);
					        period = Period.between(oldDate, newDate);			//計算開始日期~結束日期(不包括結束日期)。
				        	seniority = String.valueOf(period.toTotalMonths());
				        	readDoctorFile.close(); 	//將檔案資料流關閉
							return seniority;	//若成功配對 跳出醫師年資搜尋迴圈 不用找了
						}
					}	
				}
			}
			return "";	//找不到醫師對應年資則輸出空值
		}
	}
	
	private class Month {
		final String title;	//月報表標題		
		final String path;
		final String titleDelimiter;	//月報表標題分隔符
		final ArrayList<File> csvFiles;
		int dateIndex;		//年報表的手術日期 索引值
		int numberIndex;	//年報表的病歷號碼 索引值
		int operationIndex;	//年報表的手術名稱 索引值
		String date;		//年報表的手術日期
		String number;		//年報表的病歷號碼
		String operation;	//年報表的手術名稱
		int[] doctorIndexs = new int[6];			//月報表醫師索引
		int[] operationIndexs = new int[4];	//月報表手術代碼索引
				
		ArrayList<String> inputSplit;		//每筆資料分欄切割後的結果
		String input;						//月報表 未切割的整行資料
		String[] yDateSplit;
		StringBuilder mInputFile = new StringBuilder();
		
		public Month(String monthPath) throws Exception {
			this.path = monthPath;
			FileFilter monthFilter = pathname -> {
				String name = pathname.getName();
				return pathname.isDirectory() &&
					   !name.equals("Seniority") &&
					   !name.equals("Yearly Report") &&
					   !name.equals("Drg");
			};
			File[] monthFolder = new File(monthPath).listFiles(monthFilter);	//只蒐集包含月報表檔案的資料夾
			csvFiles = listAllFiles(monthFolder);
			
			String[] titles;
			try(BufferedReader readMonthFile = new BufferedReader(new FileReader(csvFiles.get(0)))){//讀取第一個月報表
				//月報表標題尚未建立，先讀取月報表標題
				titles = readMonthFile.readLine().split(",");
			}
			
			int length = titles.length;
			for(int i = 0, d = 0, o = 0; i < length; i++) {
				if(titles[i].equals("手術日")) {
					dateIndex = i;
				}
				if(titles[i].equals("病歷號")) {
					numberIndex = i;
				}
				if(titles[i].equals("手術名稱")) {
					operationIndex = i;
				}
				if(titles[i].contains("醫師")) {
					doctorIndexs[d++] = i;
				}
				if(titles[i].startsWith("手術代碼")) {
					operationIndexs[o++] = i;
				}
			}
			title = String.join(",", titles) + ",醫師人數,手術數量";
			titleDelimiter = ",".repeat(length + 1);	//月報表標題
		}
		
		private String findLine(String yearDivision, String yearDate, String yearRecordNumber, String yearOperation) throws Exception {
			if(yearDate.isEmpty() || yearRecordNumber.isEmpty() || yearOperation.isEmpty()) {	
				//只要任一年報表手術日期、病歷號碼或手術名稱為空值則視為找不到
				return titleDelimiter;
			}
			if(yearDivision.isEmpty()) {	//若年報表科別是空值 把所有月報表掃過一遍
				for(File eachCSVfile: csvFiles) {
					try(BufferedReader readMonthFile = new BufferedReader(new FileReader(eachCSVfile))){ //讀取月報表
						input = readMonthFile.readLine();	//跳過月報表標題
						while((input = readMonthFile.readLine()) != null) {	//讀月報表 判斷是否已到檔案結尾
							inputSplit = Split.withQuotes(input);
							date = inputSplit.get(dateIndex);
							number = inputSplit.get(numberIndex);
							operation = inputSplit.get(operationIndex);
							
							if(date.equals(yearDate) &&					//若月報表該行包含年報表手術日期
							   number.equals(yearRecordNumber) &&		//若月報表該行包含年報表病歷號碼
							   operation.equals(yearOperation)) {		//若月報表該行包含年報表手術名稱
								return count(inputSplit);	//若成功配對 跳出月報表搜尋迴圈 不用找了
							}
						}	
					}
				}
				//如果找不到對應的月報表 返回「，」
				return titleDelimiter;
			}
			
			//如果年報表科別、手術日期、病歷號碼和手術名稱都有值，則進行一般的搜尋
			yDateSplit = yearDate.split("/");	//年報表 日期分割
			mInputFile.setLength(0);
			mInputFile.append(path)
						  .append("\\")
						  .append(yearDivision)
						  .append("\\WFServlet_")
						  .append(yDateSplit[0])
						  .append(yDateSplit[1])
						  .append(".csv");
			//建立月報表路徑以讀取月報表
			try(BufferedReader readMonthFile = new BufferedReader(new FileReader(mInputFile.toString()))){ 	//讀取月報表
				input = readMonthFile.readLine();		//跳過月報表標題列
				while((input = readMonthFile.readLine()) != null) {	//讀月報表 判斷是否已到檔案結尾
					inputSplit = Split.withQuotes(input);
					date = inputSplit.get(dateIndex);
					number = inputSplit.get(numberIndex);
					operation = inputSplit.get(operationIndex);
					
					if(date.equals(yearDate) &&					//若月報表該行包含年報表手術日期
					   number.equals(yearRecordNumber) &&			//若月報表該行包含年報表病歷號碼
					   operation.equals(yearOperation)) {			//若月報表該行包含年報表手術名稱
						readMonthFile.close();		//將檔案資料流關閉
						return count(inputSplit);	//若成功配對 跳出月報表搜尋迴圈 不用找了
					}
				}	
			}
			//如果找不到對應的月報表 返回「，」
			return titleDelimiter;
		}
		
		/**計算醫師人數及手術數量*/
		private String count(ArrayList<String> inputSplit) {
			int doctorCount = 0, operationCount = 0;
			String str;
			for(int i: doctorIndexs) {
				str = inputSplit.get(i);
				if(!str.isEmpty()) {
					doctorCount++;
				}
			}
			
			for(int i: operationIndexs) {
				str = inputSplit.get(i);
				if(!str.isEmpty()) {
					operationCount++;
				}
			}
			return String.join(",", inputSplit) + "," + doctorCount + "," + operationCount;
		}
		
		/**列舉檔案*/
		private ArrayList<File> listAllFiles(File[] folder) {
			ArrayList<File> files = new ArrayList<>();
			for(File eachfile: folder) {
				if(eachfile.isDirectory()) {
					files.addAll(listAllFiles(eachfile.listFiles()));
				}else {
					files.add(eachfile);
				}
			}
			return files;
		}
	}
}