package guiFunction;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import guiComponent.AbstractTools;
import guiComponent.AbstractTools.Tool;
import preprocess.NormalizeFeature;
import preprocess.RemoveFeature;
import preprocess.RemoveRecord;
import preprocess.StandardizeFeature;
import preprocess.TargetEncoding;

/**
 * 定義步驟檔相關的操作
 */
public class Step {
	/**
	 * 執行步驟檔中的三寶
	 * @param preTrain 原始訓練集檔案
	 * @param preTest 原始測試集檔案
	 * @param doStepPath 執行過程產生檔案放置目錄
	 * @param stepFile 步驟檔
	 * @param trainOut 訓練集輸出檔案
	 * @param testOut 測試集輸出檔案
	 * @throws Exception
	 */
	public static void executeBitches(String preTrain, String preTest, String doStepPath, String stepFile, String trainOut, String testOut) throws Exception {
		String[] split;
		try(BufferedReader br = new BufferedReader(new FileReader(preTrain))){
			split = br.readLine().split(",");	// 目標欄位 | 指定內容 | 程式名稱
		}
		String ref = split[split.length - 1];	// 參照欄位 | 製作對照流水號檔案時，已移除流水號
		
		int index = 1;
		String train = doStepPath + "\\1_train.csv", test = doStepPath + "\\1_test.csv";
		try(BufferedReader br = new BufferedReader(new FileReader(stepFile))){// 讀入步驟檔
			String input;
			while ((input = br.readLine()) != null) {
				if(input.isEmpty()) {//若步驟檔為空，跳出迴圈
					break;
				}
				split = input.split(",");
				switch (split[2]) {
					case "TargetEncoding":
						TargetEncoding.exec(preTrain, split[0], ref, train, preTest, test);
						break;
					case "NormalizeFeature":
						NormalizeFeature.exec(preTrain, split[0], train, preTest, test);
						break;
					case "StandardizeFeature":
						StandardizeFeature.exec(preTrain, split[0], train, preTest, test);
						break;
				}
				switch (split[2]) {
					case "TargetEncoding":
					case "NormalizeFeature":
					case "StandardizeFeature":
						preTrain = train;
						preTest = test;
						train = doStepPath + "\\" + ++index + "_train.csv";
						test = doStepPath + "\\" + ++index + "_test.csv";		
						break;
				}
			}	
		}
		CreateFile.copy(preTrain, trainOut);
		CreateFile.copy(preTest, testOut);
	}
	
	/**
	 * 執行步驟檔中的三寶
	 * @param preTrain 原始訓練集檔案
	 * @param preTest 原始測試集檔案
	 * @param doStepPath 執行過程產生檔案放置目錄
	 * @param stepFile 步驟檔
	 * @param trainOut 訓練集輸出檔案
	 * @param testOut 測試集輸出檔案
	 * @throws Exception
	 */
	public static void executePredictorsStep(String preTrain, String preTest, String doStepPath, String stepFile, String trainOut, String testOut) throws Exception {
		String[] split;
		try(BufferedReader br = new BufferedReader(new FileReader(preTrain))){
			split = br.readLine().split(",");	// 目標欄位 | 指定內容 | 程式名稱
		}
		String ref = split[split.length - 1];	// 參照欄位 | 製作對照流水號檔案時，已移除流水號
		
		int index = 1;
		String train = doStepPath + "\\1_train.csv", test = doStepPath + "\\1_test.csv";
		try(BufferedReader br = new BufferedReader(new FileReader(stepFile))){// 讀入步驟檔
			String input;
			while ((input = br.readLine()) != null) {
				if(input.isEmpty()) {//若步驟檔為空，跳出迴圈
					break;
				}
				split = input.split(",");
				switch (split[2]) {
					case "TargetEncoding":
						TargetEncoding.exec(preTrain, split[0], ref, train, preTest, test);
						break;
					case "NormalizeFeature":
						NormalizeFeature.exec(preTrain, split[0], train, preTest, test);
						break;
					case "RemoveRecord"://只移除測試集的空值
						RemoveRecord.exec(preTest, split[0], split[1], test);
						break;
				}
				switch (split[2]) {
					case "TargetEncoding":	//訓練測試路徑都要更新
					case "NormalizeFeature":
						preTrain = train;
						train = doStepPath + "\\" + ++index + "_train.csv";
					case "RemoveRecord":	//更新測試路徑就好
						preTest = test;
						test = doStepPath + "\\" + ++index + "_test.csv";		
						break;
				}
			}	
		}
		CreateFile.copy(preTrain, trainOut);
		CreateFile.copy(preTest, testOut);
	}
	
	/**
	 * 執行移除特徵欄位
	 * @param inFile 輸入檔案
	 * @param stepFile 步驟檔案
	 * @param outFile 輸出檔案
	 * @throws Exception
	 */
	public static void executeRemoveFeature(String inFile, String stepFile, String outFile) throws Exception {
		String input;
		String[] split;
		ArrayList<String> remove = new ArrayList<>();
		try(BufferedReader br = new BufferedReader(new FileReader(stepFile))) {// 讀入步驟檔
			while((input = br.readLine()) != null) {
				if(input.isEmpty()) {//若步驟檔為空，跳出迴圈
					break;
				}
				split = input.split(",");
				remove.add(split[0]);
			}
		}
		RemoveFeature.exec(inFile, outFile, remove);
	}
	
	/**
	 * 本函式供FeatureSelect及MethodSelect使用。<br>
	 * 傳入參數須只包含欲移除特徵的欄位名稱。<br>
	 * 建立一個暫時的步驟檔供DataHandle讀入，以便移除無法使用的特徵
	 *
	 * @param remove 欲移除欄位陣列
	 * @param outFile 輸出步驟檔案
	 * @throws IOException
	 */
	public static void buildAutoRemove(ArrayList<String> remove, String outFile) throws IOException {
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))){
			for(String s: remove) {
				bw.write(s + ",,RemoveFeature");
				bw.newLine();
			}
		}
	}
	
	/**
	 * 轉換非數值欄位
	 * @param step 欲轉換欄位陣列
	 * @param outFile 輸出步驟檔案
	 * @throws Exception
	 */
	public static void buildAutoStep(ArrayList<ArrayList<String>> step, String outFile) throws Exception { // 是否要做3寶
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))) {
			for(String s: step.get(0)) {
				bw.write(s + ",null,TargetEncoding");
				bw.newLine();
			}
			for(String s: step.get(1)) {
				bw.write(s + ",null,NormalizeFeature");
				bw.newLine();
			}
			for(String s: step.get(2)) {
				bw.write(s + ",null,RemoveRecord");
				bw.newLine();
			}
		}
	}
	
	/**
	 * 本函式供FeatureSelect使用。<br>
	 * 傳入參數須只包含欲移除特徵的欄位名稱。<br>
	 * 因特徵選取可能會移除欄位，所以需要重新調整步驟。<br>
	 * 把已經移除的特徵的相關步驟移除。
	 * @param inFile 輸入檔案步驟
	 * @param remove 欲移除欄位陣列
	 * @param outFile 輸出步驟檔案
	 * @throws IOException
	 */
	public static void rearrange(String inFile, ArrayList<String> remove, String outFile) throws IOException {
		List<String> steps = Files.readAllLines(Paths.get(inFile), Charset.defaultCharset());
		if(steps.isEmpty()) {
			return;//若輸入步驟檔沒有任何步驟，不修改步驟檔
		}
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))){
			String[] split;
			for(String step: steps) {
				split = step.split(",");
				if(!remove.contains(split[0])) {// 如果移除特徵陣列不包含該名稱
					bw.write(step);				// 則輸出該步驟
					bw.newLine();
				}
			}	
		}
	}
	
	private JFileChooser chooser;
	/**
	 * 
	 * @param desktopPath 桌面路徑
	 */
	public Step(String desktopPath) {
		chooser = new JFileChooser(desktopPath);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(new FileNameExtensionFilter("Step Files", "step"));
	}
	
	/**
	 * 開啟對話框以儲存步驟至目的地
	 * @param steps
	 * @throws IOException
	 */
	public void saveWithDialog(Vector<Tool> steps) throws IOException {
		int choose = chooser.showSaveDialog(null);
		if (choose == JFileChooser.APPROVE_OPTION) {
			String saveFile = chooser.getSelectedFile().getAbsolutePath();
			if(!saveFile.endsWith(".step")) {
				saveFile += ".step";
			}
			saveAll(steps, saveFile);
		}
	}
	
	/**
	 * 儲存所有步驟至步驟檔
	 * @param steps
	 * @param saveFile
	 * @throws IOException
	 */
	public void saveAll(Vector<Tool> steps, String saveFile) throws IOException {
		String title, content, funcName;
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile))){
			for(Tool tool: steps) {
				title = tool.getTitle();
				content = tool.getContent();
				funcName = tool.getClass().getSimpleName();
				bw.write(String.join(",", title, content, funcName));
				bw.newLine();
			}
		}
	}
	
	/**
	 * 開啟對話框以載入步驟
	 * @param tool
	 * @return 載入的步驟
	 * @throws IOException
	 */
	public Optional<Vector<Tool>> loadWithDialog(AbstractTools tool) throws IOException {
		int choose = chooser.showOpenDialog(null);
		if (choose == JFileChooser.APPROVE_OPTION) {
			return Optional.ofNullable(loadAll(tool, chooser.getSelectedFile().getAbsolutePath()));
		}
		return Optional.empty();
	}
	
	/**
	 * 載入所有步驟
	 * @param tools
	 * @param loadFile
	 * @return 載入的步驟
	 * @throws IOException
	 */
	public Vector<Tool> loadAll(AbstractTools tools, String loadFile) throws IOException {
		Vector<Tool> allStep = new Vector<>();
		String step;
		String[] split;
		try(BufferedReader br = new BufferedReader(new FileReader(loadFile))){
			while((step = br.readLine()) != null) {
		    	split = step.split(",");
				switch(split[2]) {	// funcName
					case "ExtractRecord":
					case "RemoveRecord":
					case "NormalizeFeature":
					case "RemoveFeature":
					case "RemoveOutlier":
					case "RemoveOutlierBySD":
					case "StandardizeFeature":
					case "TargetEncoding":
						allStep.add(tools.newInstance(split[2], split[0], split[1]));
						break;
					default:
						System.err.println("沒有該步驟!");
				}
		    }	
		}
		return allStep;
	}
}
