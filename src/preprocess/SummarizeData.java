package preprocess;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
//import java.util.TreeSet;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TreeMap;

public class SummarizeData {
	/**執行{@link SummarizeData#SummarizeData SummarizeData}*/
	public static void exec(String inFile, String seniorityPath, String outPath) throws Exception {
		new SummarizeData(inFile, seniorityPath, outPath);
	}
	
	private Collator collator = Collator.getInstance(Locale.TRADITIONAL_CHINESE);
	private Comparator<String> chComparator = (String s1, String s2) -> {
		return collator.compare(s1, s2);	//按中文筆畫排序
	};
	private Table4_1 table4_1 = new Table4_1();
	private Table4_2 table4_2 = new Table4_2();
	private Table4_3 table4_3 = new Table4_3();
	private Table4_4 table4_4 = new Table4_4();
	private Table4_5 table4_5 = new Table4_5();
	private Table4_6 table4_6 = new Table4_6();
//	private Table4_7 table4_7 = new Table4_7();
	private TreeMap<String, TreeMap<String, ArrayList<Integer>>> deptTM = new TreeMap<String, TreeMap<String, ArrayList<Integer>>>(String.CASE_INSENSITIVE_ORDER);
	//門診名稱 | 醫師姓名 | 手術時間 | 4-2 5 6 共用資料 不可消除
	private TreeMap<String, ArrayList<Integer>> drTM = new TreeMap<String, ArrayList<Integer>>(chComparator);
	//醫師姓名 | 各醫師花費手術時間
	private double total = 0.0; //資料總筆數 不可消除
	
	/**
	 * 計算統計資訊並輸出
	 * @param inFile 輸入檔案
	 * @param seniorityPath 年資路徑
	 * @param outPath 輸出路徑
	 * @throws Exception
	 */
	private SummarizeData(String inFile, String seniorityPath, String outPath) throws Exception {
		//surg = surgery 手術 | dr = doctor 醫師 | en = English 英文 | ch = Chinese 中文
		//dept = department 科別(部門) | anaes = anaesthetization 麻醉 | seni = seniority 年資
		long startTime = System.currentTimeMillis();
		ArrayList<String> inputSplit;
		String input;			//未切割的整行資料
		int[] spendTimeSplit;	//手術時間 時與分
		Integer spendTime;		//將手術時間轉換為分
		
		long processingTime = System.currentTimeMillis();	//程式執行中途 紀錄當下時間
		try(BufferedReader br = new BufferedReader(new FileReader(inFile))){//原資料檔
			inputSplit = Split.withQuotes(br.readLine());		//每筆資料分欄切割後的結果 | 第一列標題列
			int surgIndex = inputSplit.indexOf("手術名稱");		//手術名稱索引
			int deptIndex = inputSplit.indexOf("科別");			//門診名稱索引
			int drIndex = inputSplit.indexOf("主治醫師");			//醫師姓名索引
	//		int anaesIndex = inputSplit.indexOf("麻醉");			//麻醉種類索引
			int spendTimeIndex = inputSplit.indexOf("手術時間（時:分）");	//手術時間索引
			
			String spendTimeStr, surgStr, deptStr, drStr;
			while((input = br.readLine()) != null) {	//判斷是否已到檔案結尾
				inputSplit = Split.withQuotes(input);
				spendTimeStr = inputSplit.get(spendTimeIndex);
				if(spendTimeStr.contains(":")) {
					spendTimeSplit = Arrays.stream(spendTimeStr.split(":")).mapToInt(Integer::parseInt).toArray();	//以:為分隔符號 將手術時間的時與分 分開
					spendTime = spendTimeSplit[0] * 60 + spendTimeSplit[1];
					
					surgStr = inputSplit.get(surgIndex);
					if(!surgStr.isEmpty()) {
						table4_1.buildData(surgStr, spendTime); 		//建立基本資料 手術名稱 手術時間轉換
					}
					
					deptStr = inputSplit.get(deptIndex);
					drStr = inputSplit.get(drIndex);
					if(!deptStr.isEmpty() && !drStr.isEmpty()) {
						table4_2.buildData(deptStr, drStr, spendTime);	//建立基本資料 門診名稱 醫師姓名 手術時間轉換 | 4-2 5 6 共用資料
					}
					
					if(!drStr.isEmpty()) {
						table4_3.buildData(drStr, spendTime); 	//建立基本資料 醫師姓名 手術時間轉換 | 4-3 4 共用資料
					}	
				}
	//			if(!inputSplit.get(deptIndex).isEmpty() && !inputSplit.get(anaesIndex).isEmpty()) {
	//				table4_7.buildData(inputSplit.get(deptIndex), inputSplit.get(anaesIndex));  		//建立基本資料 門診名稱 麻醉種類
	//			}
				
				inputSplit.clear(); //每行分隔出的字串陣列 清除
				total++; 		//資料總筆數計算			
			}	
		}
		
		
		Files.walkFileTree(Paths.get(seniorityPath), new SimpleFileVisitor<Path>() {	//建立醫師年資的所有資料清單 方便遍歷
			private ArrayList<String> doctorTitle, inputSplit;	//醫師年資標題列 | 分割陣列
			private String input;
			private int drIndex, yearSeniIndex;
			
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				try(BufferedReader br = Files.newBufferedReader(file, Charset.defaultCharset())){//原資料檔
					doctorTitle = Split.withQuotes(br.readLine());		//分割醫師年資標題列
					drIndex = doctorTitle.indexOf("姓名");			//醫師年資的醫師姓名 索引值 (變數沿用)
					yearSeniIndex = doctorTitle.indexOf("年資_年");	//醫師年資的醫師到院日期 索引值
					
					while((input = br.readLine()) != null) {			//判斷是否已到檔案結尾
						inputSplit = Split.withQuotes(input);
						table4_4.buildData(inputSplit.get(drIndex), Integer.valueOf(inputSplit.get(yearSeniIndex)));	//建立基本資料 醫師姓名 醫師年資(年)
					}	
				}
				return FileVisitResult.CONTINUE;
			}
		});
		System.out.printf("處理與儲存資料 花費時間：%d (ms)\n\n", System.currentTimeMillis() - processingTime);
		
		processingTime = System.currentTimeMillis();	//程式執行中途 紀錄當下時間
		table4_1.create(outPath + "\\各疾病採行的手術式之手術平均時間.csv");
		System.out.printf("花費時間：%d (ms)\n\n", System.currentTimeMillis() - processingTime);
		
		processingTime = System.currentTimeMillis();	//程式執行中途 紀錄當下時間
		table4_2.create(outPath + "\\各科之手術平均時間.csv");
		System.out.printf("花費時間：%d (ms)\n\n", System.currentTimeMillis() - processingTime);
		
		processingTime = System.currentTimeMillis();	//程式執行中途 紀錄當下時間
		table4_3.create(outPath + "\\各醫師之手術平均時間.csv");
		System.out.printf("花費時間：%d (ms)\n\n", System.currentTimeMillis() - processingTime);
		
		processingTime = System.currentTimeMillis();	//程式執行中途 紀錄當下時間
		table4_4.create(outPath + "\\與醫師年資有關之手術平均時間.csv");
		System.out.printf("花費時間：%d (ms)\n\n", System.currentTimeMillis() - processingTime);
		
		processingTime = System.currentTimeMillis();	//程式執行中途 紀錄當下時間
		table4_5.create(outPath + "\\各科醫師別之手術平均時間.csv");
		System.out.printf("花費時間：%d (ms)\n\n", System.currentTimeMillis() - processingTime);
		
		processingTime = System.currentTimeMillis();	//程式執行中途 紀錄當下時間
		table4_6.create(outPath + "\\各科別手術人次數.csv");
		System.out.printf("花費時間：%d (ms)\n\n", System.currentTimeMillis() - processingTime);
		
//		processingTime = System.currentTimeMillis();	//程式執行中途 紀錄當下時間
//		table4_7.create(outPath + "\\各科別手術採行麻醉方法及比率.csv");
//		System.out.printf("花費時間：%d (ms)\n\n", System.currentTimeMillis() - processingTime);
		
		System.out.printf("總花費時間：%d (ms)\n\n", System.currentTimeMillis() - startTime);
	}
	
	/**
	 * 某資料筆數占資料總筆數的幾%
	 * @param number 某資料筆數
	 * @return %數
	 */
	private double percent(double number) {
		return number / total * 100;
	}
	
	/**
	 * 計算平均
	 * @param numberList 數值陣列
	 * @return 平均值
	 */
	private double average(ArrayList<Integer> numberList) {
		return numberList.stream().mapToInt(Integer::intValue).average().getAsDouble();
	}
	
	/**
	 * 計算標準差
	 * @param numberList 數值陣列
	 * @param average 此數值陣列的平均值
	 * @return 標準差
	 */
	private double sd(ArrayList<Integer> numberList, double average) {
		double sum = numberList.stream().mapToDouble(eachNumber -> Math.pow((eachNumber - average), 2)).sum();	
		return Math.sqrt(sum / numberList.size());
	}
	
	private class Table4_1 {
		private TreeMap<String, ArrayList<Integer>> surgTM = new TreeMap<String, ArrayList<Integer>>(String.CASE_INSENSITIVE_ORDER);
		//手術名稱 | 手術時間 | 不考慮英文大小寫排序
		
		private void buildData(String surg, Integer time) {
			surgTM.putIfAbsent(surg, new ArrayList<Integer>());		//比對手術名稱 若沒有該名稱 加入新的陣列
			surgTM.get(surg).add(time);
		}
		
		private void create(String fileName) throws Exception {
			StringBuilder outStr = new StringBuilder("手術名稱,最小值,最大值,平均時間,標準差,次數");	
			ArrayList<Integer> surgSpendTime;
			double average;
			
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))){
				for(Entry<String, ArrayList<Integer>> surgTMEntry: surgTM.entrySet()) {
			    	surgSpendTime = surgTMEntry.getValue();
			    	//拿到Entry<String, ArrayList<Integer>>	再拿到其中的ArrayList<Integer> 手術時間
			    	average = average(surgSpendTime);
			    	
			    	outStr.append("\n")//新增一行資料
			    				.append(surgTMEntry.getKey())
			    				.append(",")
			    				.append(Collections.min(surgSpendTime))
			    				.append(",")
			    				.append(Collections.max(surgSpendTime))
			    				.append(",")
			    				.append(Math.round(average * 100.0) / 100.0)
			    				.append(",")
			    				.append(Math.round(sd(surgSpendTime, average) * 100.0) / 100.0)
			    				.append(",")
			    				.append(surgSpendTime.size());
			    	bw.write(outStr.toString());
			    	outStr.setLength(0);
			    }
			}
			System.out.println("成功輸出4-1表格。");
		}
	}
	
	private class Table4_2 {
		private void buildData(String dept, String dr, Integer time) {	//建立主要資料 | 門診名稱 | 醫師姓名 | 手術花費時間		
			deptTM.putIfAbsent(dept, new TreeMap<String, ArrayList<Integer>>(chComparator));	//比對門診名稱 無該門診 建立醫師TreeMap
			deptTM.get(dept).putIfAbsent(dr, new ArrayList<Integer>());	//比對醫生姓名 無該醫師 建立手術花費時間陣列		
			deptTM.get(dept).get(dr).add(time); 						//將手術時間加入
		}
		
		private void create(String fileName) throws Exception {
			StringBuilder outStr = new StringBuilder("門診名稱,最小值,最大值,平均時間,標準差,次數");
			ArrayList<Integer> deptSpendTime = new ArrayList<Integer>(); 		//每個部門所花的手術時間
			double average;
			
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))){	//檔案 覆蓋檔案
				for(Entry<String, TreeMap<String, ArrayList<Integer>>> deptTMEntry: deptTM.entrySet()) {	//departmentTreeMap的迭代器
					deptTMEntry.getValue().values().forEach(deptSpendTime::addAll);
					//將各科別下每個醫師的手術時間加入陣列
					average = average(deptSpendTime);
					
					outStr.append("\n")	//新增一行資料
								.append(deptTMEntry.getKey())			//門診名稱
								.append(",")
								.append(Collections.min(deptSpendTime))	//最小值
								.append(",")
								.append(Collections.max(deptSpendTime))	//最大值
								.append(",")
								.append(Math.round(average * 100.0) / 100.0)	//平均值
								.append(",")
								.append(Math.round(sd(deptSpendTime, average) * 100.0) / 100.0)		//標準差
								.append(",")
								.append(deptSpendTime.size());			//次數
					deptSpendTime.clear(); //將門診所花的手術時間陣列清除
					bw.write(outStr.toString());
			    	outStr.setLength(0);
				}
			}
			System.out.println("成功輸出4-2表格。");
		}
	}
	
	private class Table4_3 {
		private void buildData(String dr, Integer time) {
			drTM.putIfAbsent(dr, new ArrayList<Integer>());	//比對醫師姓名 無該姓名 建立醫師TreeMap
			drTM.get(dr).add(time);
		}
		
		private void create(String fileName) throws Exception {
			StringBuilder outStr = new StringBuilder("醫師姓名,最小值,最大值,平均時間,標準差,次數");
			ArrayList<Integer> drSpendTime;
			double average;
			
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))){	//檔案 覆蓋檔案
				for(Entry<String, ArrayList<Integer>> drTMEntry: drTM.entrySet()) {
					drSpendTime = drTMEntry.getValue();
					average = average(drSpendTime);
					
					outStr.append("\n")	//新增一行資料
								.append(drTMEntry.getKey())				//醫師名稱
								.append(",")
								.append(Collections.min(drSpendTime))	//最小值
								.append(",")
								.append(Collections.max(drSpendTime))	//最大值
								.append(",")
								.append(Math.round(average * 100.0) / 100.0)	//平均值
								.append(",")
								.append(Math.round(sd(drSpendTime, average) * 100.0) / 100.0)		//標準差
								.append(",")
								.append(drSpendTime.size());			//次數
					bw.write(outStr.toString());
					outStr.setLength(0);
				}
			}
			System.out.println("成功輸出4-3表格。");
		}
	}
	
	private class Table4_4 {	
		private HashMap<String, Integer> drSeniHM = new HashMap<String, Integer>();	//醫師姓名 | 醫師年資(年)
		
		private void buildData(String dr, Integer yearSeni) {
			drSeniHM.putIfAbsent(dr, yearSeni);
		}
		
		private void create(String fileName) throws Exception {
			LinkedHashMap<String, ArrayList<Integer>> seniLHM = new LinkedHashMap<String, ArrayList<Integer>>(5);
			for(String eachKey: new String[] {"1-5年", "6-10年", "11-15年", "16-20年", "20年以上"}) {
				seniLHM.put(eachKey, new ArrayList<Integer>());
			}
			Integer yearSeni;
			ArrayList<Integer> drSpendTime, seniSpendTime;
			for(Entry<String, ArrayList<Integer>> drTMEntry: drTM.entrySet()) {
				yearSeni = drSeniHM.get(drTMEntry.getKey());	//某醫師的年資(年)
				if(yearSeni != null) {							//若醫師年資HashMap有對應資料再繼續做
					drSpendTime = drTMEntry.getValue();			//某醫師的所有手術時間
					if(yearSeni <= 5) 		seniLHM.get("1-5年").addAll(drSpendTime);
					else if(yearSeni <= 10) seniLHM.get("6-10年").addAll(drSpendTime);
					else if(yearSeni <= 15) seniLHM.get("11-15年").addAll(drSpendTime);
					else if(yearSeni <= 20) seniLHM.get("16-20年").addAll(drSpendTime);
					else 					seniLHM.get("20年以上").addAll(drSpendTime);
				}
			}
			
			double average;
			StringBuilder outStr = new StringBuilder("醫師年資,最小值,最大值,平均時間,標準差,手術次數");
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))){	//檔案 覆蓋檔案
				for(Entry<String, ArrayList<Integer>> seniLHMEntry: seniLHM.entrySet()) {
					seniSpendTime = seniLHMEntry.getValue();
					average = average(seniSpendTime);
					
					outStr.append("\n")	//新增一行資料
								.append(seniLHMEntry.getKey())	//醫師年資分類
								.append(",")
								.append(Collections.min(seniSpendTime))	//最小值
								.append(",")
								.append(Collections.max(seniSpendTime))	//最大值
								.append(",")
								.append(Math.round(average * 100.0) / 100.0)	//平均值
								.append(",")
								.append(Math.round(sd(seniSpendTime, average) * 100.0) / 100.0)		//標準差
								.append(",")
								.append(seniSpendTime.size());			//次數				
					bw.write(outStr.toString());
					outStr.setLength(0);
				}	
			}
			System.out.println("成功輸出4-4表格。");
		}
	}
	
	private class Table4_5 {
		private void create(String fileName) throws Exception {
			StringBuilder outStr = new StringBuilder("科別,醫師,平均時間,標準差,次數\n");
			ArrayList<Integer> drSpendTime;	//手術花費時間陣列
			double average;
			
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))){	//檔案 覆蓋檔案
				for(Entry<String, TreeMap<String, ArrayList<Integer>>> deptTMEntry: deptTM.entrySet()) { //departmentTreeMap的迭代器
					outStr.append(deptTMEntry.getKey()); 		//門診名稱
					
					for(Entry<String, ArrayList<Integer>> drTMEntry: deptTMEntry.getValue().entrySet()) {
						//先拿到doctorTreeMap 再拿到它的entry 再拿到迭代器	| doctorTreeMap的迭代器
						drSpendTime = drTMEntry.getValue();
						average = average(drSpendTime);
						
						outStr.append(",")
									.append(drTMEntry.getKey())		//醫師名稱
									.append(",")
									.append(Math.round(average * 100.0) / 100.0)	//平均值
									.append(",")
									.append(Math.round(sd(drSpendTime, average) * 100.0) / 100.0)		//標準差
									.append(",")
									.append(drSpendTime.size())		//次數
									.append("\n");	//新增一行資料
						bw.write(outStr.toString());
						outStr.setLength(0);
					}
				}
			}
			System.out.println("成功輸出4-5表格。");
		}
	}
	
	private class Table4_6 {
		private void create(String fileName) throws Exception {
			double surgCount = 0.0;			//手術次數
			StringBuilder outStr = new StringBuilder("科別,人次數,百分比率(%)");
			
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))){	//檔案 覆蓋檔案
				for(Entry<String, TreeMap<String, ArrayList<Integer>>> deptTMEntry: deptTM.entrySet()) {	//departmentTreeMap的迭代器
					outStr.append("\n")	//新增一行資料
								.append(deptTMEntry.getKey()); //門診名稱
					surgCount = deptTMEntry.getValue().values().stream().mapToDouble(e -> e.size()).sum();
					outStr.append(",")
								.append(surgCount)	//醫師名稱
								.append(",")
								.append(Math.round(percent(surgCount) * 100.0) / 100.0);			//平均值
					bw.write(outStr.toString());
					outStr.setLength(0);
				}
			}
			System.out.println("成功輸出4-6表格。");
		}
	}
	
//	private class Table4_7 {
//		private TreeMap<String, HashMap<String, Integer>> deptTM = new TreeMap<String, HashMap<String, Integer>>(String.CASE_INSENSITIVE_ORDER);
//		//門診名稱 | 麻醉名稱 | 麻醉次數
//		private TreeSet<String> anaesSet = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER); 	//所有麻醉種類
//
//		private void buildData(String dept, String anaes) {				//門診名稱 | 麻醉種類		
//			deptTM.putIfAbsent(dept, new HashMap<String, Integer>());	//比對門診名稱 無該門診 建立麻醉HashMap
//			deptTM.get(dept).putIfAbsent(anaes, 0);						//從門診名稱判定有無該麻醉種類 | 若無把手術次數設為0
//			deptTM.get(dept).put(anaes, deptTM.get(dept).get(anaes) + 1);
//			//從門診名稱拿到特定麻醉種類 / 依據麻醉種類更改其中的手術次數 / 再把原本麻醉種類其中的手術次數+1
//			anaesSet.add(anaes);	//將麻醉種類加入不重複Set
//		}
//		
//		private void create(String fileName) throws Exception {
//			StringBuilder outStr = new StringBuilder();
//			BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));	//檔案 覆蓋檔案
//			outStr.append("麻醉方法,");
//			String[] anaes = anaesSet.toArray(new String[anaesSet.size()]);
//			for(int i = 0; i < anaes.length; i++) {
//				outStr.append(anaes[i]);
//				if(i != anaes.length - 1) {
//					outStr.append(",");
//				}		
//			}
//			outStr.append("\n科別,");
//			for(int i = 0; i < anaes.length; i++) {
//				outStr.append("%");
//				if(i != anaes.length - 1) {
//					outStr.append(",");
//				}
//			}
//			
//			for(Entry<String, HashMap<String, Integer>> deptTMEntry: deptTM.entrySet()) {
//				outStr.append("\n")	//新增一行資料
//							.append(deptTMEntry.getKey())
//							.append(",");	//門診名稱
//				
//				for(int i = 0; i < anaes.length; i++) {
//					if(deptTMEntry.getValue().containsKey(anaes[i])) { //判斷該門診的麻醉種類hashMap是否包含該麻醉名稱
//						outStr.append(Math.round(percent(deptTMEntry.getValue().get(anaes[i])) * 100.0) / 100.0);
//					}else {
//						outStr.append(0);
//					}
//					if(i != anaes.length - 1) {
//						outStr.append(",");
//					}
//				}
//				bw.write(outStr.toString());				
//				outStr.setLength(0);
//			}
//			bw.close();
//			System.out.println("成功輸出4-7表格。");
//		}
//	}
}