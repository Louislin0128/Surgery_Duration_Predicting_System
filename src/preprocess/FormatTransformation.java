package preprocess;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class FormatTransformation extends SimpleFileVisitor<Path> {
	/**執行{@link FormatTransformation#FormatTransformation FormatTransformation}*/
	public static void exec(String inPath, String outPath) throws Exception {
		new FormatTransformation(inPath, outPath);
	}
	
	private Path src, dst;
	/**
	 * 轉換檔案<br>
	 * CSV：直接複製到目的地<br>
	 * XLS, XLSX：移除不要的資訊並轉換至目的地
	 * @param in 輸入路徑
	 * @param out 輸出路徑
	 * @throws IOException
	 */
	private FormatTransformation(String in, String out) throws Exception {
		src = Paths.get(in);
		dst = Paths.get(out);
		Files.walkFileTree(src, this);
	}
	
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		Files.createDirectories(dst.resolve(src.relativize(dir)));
		System.out.println("Folder Name:" + dir.getFileName());
		return FileVisitResult.CONTINUE;
	}
	
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		Path outPath = dst.resolve(src.relativize(file));
		String outPathStr = outPath.toString();
		
		if(outPathStr.endsWith(".xls") || outPathStr.endsWith(".xlsx")) {
			Workbook workbook = WorkbookFactory.create(Files.newInputStream(file));
			Sheet sheet = workbook.getSheetAt(0);
			Row row = sheet.getRow(0);
			Cell cell = row.getCell(0);
			String title = cell.getStringCellValue();

			System.out.println("File Name:" + file.getFileName());
			String outFile = outPathStr.substring(0, outPathStr.lastIndexOf(".")) + ".csv";
			if (title.contains("手術室病人流程明細表")) {
				createYearSheet(sheet, outFile);
			} else if (title.contains("月報表")) {
				createMonthSheet(sheet, outFile);
			} else {
				createOtherSheet(sheet, outPathStr.contains("年資"), outFile);
			}
		
		}else if(outPathStr.endsWith(".csv")) {
			Files.copy(file, outPath, StandardCopyOption.REPLACE_EXISTING);
		}
		return FileVisitResult.CONTINUE;
	}
	
	private String messyCodeFilter(String str) {
		StringBuilder tempStr = new StringBuilder();
		for(char c: str.toCharArray()) {
//			String str = "" + c;
//			str.matches("[\\u4e00-\\u9fa5\\u0020-\\u007e]+")
			if (!Character.isLetterOrDigit(c) && Character.getType(c) == Character.CONTROL) {
				tempStr.append(' ');// 用空格替換不可用字元
				continue;
			}
			tempStr.append(c);
		}
		return tempStr.toString();
	}
	
	/**若某欄含有「,」則加入「"」*/
	private String quote(String str) {
		if (!str.isEmpty()) {
			str = str.strip().replace("\"", "");
			if (str.contains(",")) {
				str = "\"" + str + "\"";
			}
		}
		return str;
	}

	private DataFormatter formatter = new DataFormatter();
	/**建立年報表*/
	private void createYearSheet(Sheet sheet, String outFile) throws IOException {
		//取得資料來源的row
		Row row = sheet.getRow(3);
		//取得每一row的col
		Cell cell;
		//列數(不包含最後7列) | 欄數
		int rowNum = sheet.getPhysicalNumberOfRows() - 7, cellNum = row.getPhysicalNumberOfCells();
		StringBuilder tempStr = new StringBuilder();
		String title, str;
		//遍歷標題
		for(int cellIndex = 0; cellIndex < cellNum; cellIndex++) {
			cell = row.getCell(cellIndex);
			title = cell.getStringCellValue().replace("\n", "");
			if(title.equals("手術時間")) {
				title += "（時:分）";
			}
			tempStr.append(title);
			if(cellIndex != cellNum - 1) {
				tempStr.append(",");
			}
		}
		
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))) {
			bw.write(tempStr.toString());//輸出標題
			tempStr.setLength(0);
			
			//從內容第一列開始遍歷(不包含標題)
			for (int rowIndex = 4; rowIndex < rowNum; rowIndex++) {
				row = sheet.getRow(rowIndex);	// 取得資料來源的row
				
				for (int cellIndex = 0; cellIndex < cellNum; cellIndex++) {
					cell = row.getCell(cellIndex); // 取得每一row的col
					
					str = formatter.formatCellValue(cell).strip();
					if (cellIndex == 6) {		//年齡
						if(str.length() > 1) {	//若字串長度為0或1則不作更動
							int index = -1;
							for(char c: str.toCharArray()) {
								index++;
								if(c != '0') {
									break;
								}
							}
							
							int mLocation = str.lastIndexOf('M');
							if(mLocation == -1) {
								str = str.substring(index);
							}else {//mLocation != -1
								str = Double.parseDouble(str.substring(index, mLocation)) / 12 + "";
							}
						}
					}else if(cellIndex == 12) {	//手術名稱
						str = messyCodeFilter(str);
					}
					
					tempStr.append(quote(str));
					if(cellIndex != cellNum - 1) {
						tempStr.append(",");
					}
				}
				
				bw.newLine();
				bw.write(tempStr.toString());
				tempStr.setLength(0);
			}
		}
	}
	
	/**建立月報表*/
	private void createMonthSheet(Sheet sheet, String outFile) throws IOException {
		//取得資料來源的row
		Row row = sheet.getRow(3);
		//取得每一row的col
		Cell cell;
		//列數(不包含最後7列) | 欄數(不包含最後2欄)
		int rowNum = sheet.getPhysicalNumberOfRows() - 7, cellNum = row.getPhysicalNumberOfCells() - 2;
		StringBuilder tempStr = new StringBuilder();
		//遍歷標題
		for(int cellIndex = 2; cellIndex < cellNum; cellIndex++) {
			cell = row.getCell(cellIndex);
			tempStr.append(cell.getStringCellValue().strip());
			if(cellIndex != cellNum - 1) {
				tempStr.append(",");
			}
		}
		
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))) {
			bw.write(tempStr.toString());//輸出標題
			tempStr.setLength(0);
			
			//從內容第一列開始遍歷(不包含標題)
			for (int rowIndex = 4; rowIndex < rowNum; rowIndex++) {
				row = sheet.getRow(rowIndex);
				
				for(int cellIndex = 2; cellIndex < cellNum; cellIndex++) {
					cell = row.getCell(cellIndex);
					
					String str = formatter.formatCellValue(cell).strip();
					if (cellIndex == 11) {		//年齡
						if(str.length() > 1) {	//若字串長度為0或1則不作更動
							int index = -1;
							for(char c: str.toCharArray()) {
								index++;
								if(c != '0') {
									break;
								}
							}
							str = str.substring(index);
						}
					}
					tempStr.append(quote(str));
					if(cellIndex != cellNum - 1) {
						tempStr.append(",");
					}
				}
				
				bw.newLine();
				bw.write(tempStr.toString());
				tempStr.setLength(0);
			}	
		}
	}
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
	/**
	 * 建立其他報表
	 * @param isSeniority 是否是醫師年資報表
	 */
	private void createOtherSheet(Sheet sheet, boolean isSeniority, String outFile) throws IOException {
		int rowNum = sheet.getPhysicalNumberOfRows();
		int cellNum = sheet.getRow(3).getLastCellNum();
		StringBuilder tempStr = new StringBuilder();
		
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))) {
			for (int rowIndex = 0; rowIndex < rowNum; rowIndex++) {
				Row row = sheet.getRow(rowIndex);
				
				for (int cellIndex = (isSeniority) ? 5 : 0; cellIndex < cellNum; cellIndex++) {
					Cell cell = row.getCell(cellIndex);
					
					String str;
					switch(cell.getCellType()) {
					case NUMERIC:
						if (DateUtil.isCellDateFormatted(cell) || cellIndex == 9) {//到院日
							str = sdf.format(cell.getDateCellValue());
							break;
						}
					default:
						str = formatter.formatCellValue(cell).strip();
						break;
					}
					
					tempStr.append(quote(str));
					if(cellIndex != cellNum - 1) {
						tempStr.append(",");
					}
				}
				
				bw.write(tempStr.toString());
				if(rowIndex != rowNum - 1) {
					bw.newLine();
				}
				tempStr.setLength(0);
			}	
		}
	}
}