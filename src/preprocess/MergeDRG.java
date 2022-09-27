package preprocess;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MergeDRG {
	/**執行{@link MergeDRG#MergeDRG MergeDRG}*/
	public static void exec(String yearFile, String drgPath, String outFile) throws Exception {
		new MergeDRG(yearFile, drgPath, outFile);
	}
	
	/**
	 * 合併DRG檔案
	 * @param yearFile 年(月)報表檔名
	 * @param drgPath DRG檔名的路徑
	 * @param outFile 輸出檔名
	 * @throws Exception
	 */
	private MergeDRG(String yearFile, String drgPath, String outFile) throws Exception {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		ArrayList<Object[]> drgList = new ArrayList<>();
		ArrayList<String> drgSplit;
		String drgInput;
		Object[] drg;
		int inDateIndex, outDateIndex, drgIndex, drgNumberIndex, divisionIndex;	// 入院日期 | 出院日期 | DRG編號 | 病歷號 | 科別
		for(File file: new File(drgPath).listFiles()) {
			try(BufferedReader readDRG = new BufferedReader(new FileReader(file))) {
				drgInput = readDRG.readLine();
				drgSplit = Split.withQuotes(drgInput);
				inDateIndex = drgSplit.indexOf("入院日期");	//DRG檔案的入院日期 索引值
				outDateIndex = drgSplit.indexOf("出院日期");	//DRG檔案的出院日期 索引值
				drgIndex = drgSplit.indexOf("DRG編號");		//DRG檔案的號碼 索引值
				drgNumberIndex = drgSplit.indexOf("病歷號");	//DRG檔案的病歷號碼 索引值
				divisionIndex = drgSplit.indexOf("科別");	//DRG檔案的科別 索引值
				
				while((drgInput = readDRG.readLine()) != null) {
					drgSplit = Split.withQuotes(drgInput);
					drg = new Object[5];
					drg[0] = LocalDate.parse(drgSplit.get(inDateIndex), formatter);
					drg[1] = LocalDate.parse(drgSplit.get(outDateIndex), formatter);		
					drg[2] = drgSplit.get(drgNumberIndex);
					drg[3] = drgSplit.get(divisionIndex);
					drg[4] = drgSplit.get(drgIndex);
					drgList.add(drg);
				}	
			}
		}
		
		try(BufferedReader readYear = new BufferedReader(new FileReader(yearFile))){
			String yearInput = readYear.readLine();
			ArrayList<String> yearSplit = Split.withQuotes(yearInput);
			int dateIndex = yearSplit.indexOf("手術日");
			int yearNumberIndex = yearSplit.indexOf("病歷號");	//年報表的病歷號碼 索引值
			int yeardivisionIndex = yearSplit.indexOf("科別");	//年報表的科別 索引值
			
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))){
			bw.write(yearInput + ",DRG編號");
			LocalDate inDate, outDate, date;	// 入院日期(drg) | 出院日期(drg) | 手術日(year)
			String drgStr = "";
			while((yearInput = readYear.readLine()) != null) {
				yearSplit = Split.withQuotes(yearInput);
				date = LocalDate.parse(yearSplit.get(dateIndex), formatter);
				
				for(Object[] o: drgList) {
					inDate = (LocalDate) o[0];
					outDate = (LocalDate) o[1];
					if(((String) o[2]).equals(yearSplit.get(yearNumberIndex)) &&
					   ((String) o[3]).equals(yearSplit.get(yeardivisionIndex)) &&
					  ((date.isAfter(inDate) && date.isBefore(outDate)) || date.isEqual(inDate) || date.isEqual(outDate))) {				
					// DRG的病歷號碼等於年報表的病歷號碼 &&
					// DRG的科別等於年報表的科別 &&
					// ((手術日於入院日後 && 手術日於出院日前) || 手術日等於入院日 || 手術日等於出院日)
						drgStr = (String) o[4];
						break;
					}
				}
				bw.newLine();
				bw.write(yearInput + "," + drgStr);
				drgStr = "";
			}	
		}
		}
	}
}
