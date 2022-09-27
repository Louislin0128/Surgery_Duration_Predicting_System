package gui;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Logger;

public class Info {
	private static Logger logger = Logger.getLogger("Info");
	
	private static PropertyChangeSupport p = new PropertyChangeSupport(ProcessFrame.Mode.class);
	/**加入監聽器*/
	static void addListener(PropertyChangeListener listener) {
		p.addPropertyChangeListener(listener);
	}
	/**顯示頁面*/
	static void showPage(Page page) {
		p.firePropertyChange(new PropertyChangeEvent(ProcessFrame.Mode.SHOW_PAGE, "SHOWPAGE", null, page));
	}
	/**
	 * 清除所有過程及暫存檔案<br>
	 * 不包含Model目錄及Predict目錄<br>
	 * 通常用於進入預測主頁面的時候<br>
	 * {@link #deleteTemp()}
	 */
	static void clearProcessAndTemp() {
		p.firePropertyChange(new PropertyChangeEvent(ProcessFrame.Mode.CLEAR_PROCESS_AND_TEMP, "CLEAR_PROCESS_AND_TEMP", null, null));
		deleteTemp();
	}
	/**
	 * 清除所有過程及產生的檔案<br>
	 * {@link #deleteAll()}
	 */
	static void clearAll() {
		p.firePropertyChange(new PropertyChangeEvent(ProcessFrame.Mode.CLEAR_ALL, "CLEAR_ALL", null, null));
		deleteAll();
	}
	/**設定系統訊息*/
	static void showMessage(String message) {
		p.firePropertyChange(new PropertyChangeEvent(ProcessFrame.Mode.SHOWMESSAGE, "SHOWMESSAGE", null, message));
	}
	/**設定系統錯誤訊息*/
	static void showError(String message) {
		p.firePropertyChange(new PropertyChangeEvent(ProcessFrame.Mode.SHOWERROR, "SHOWERROR", null, message));
	}
	
	/**使用說明手冊路徑*/
	private static String manualPath ="src\\Manual";
	static String getManualPath() {
		return manualPath;
	}
	/**icon路徑*/
	private static String iconPath = "src\\icon";
	public static String getIconPath() {
		return iconPath;
	}
	
	// ============================================
	private static String desktop = "D:\\AA\\Surgery Time Prediction System";	//桌面路徑
	/**工作目錄*/
	private static Path workingData;
	
	/** 原始路徑*/
	private static Path rawPath;
	
	/**轉換後路徑*/
	private static Path dataPath;
	
	/**舊年報表路徑*/
	private static String oldYearPath;
	
	/**新年報表路徑*/
	private static String newYearPath;
	
	/**醫生路徑*/
	private static String doctorPath;
	
	/**DRG檔案路徑 */
	private static String drgPath;
	
	/**合併完路徑*/
	private static String appendRecord;
	
	/**merge年報表月報表路徑*/
	private static String mergeFeature;
	
	/**mergeDRG路徑*/
	private static String mergeDRG;
	
	/**手術時間 | 麻醉時間路徑*/
	private static String createTime;
	
	/**將標題重新編排*/
	private static String rearrangeTitle;
	
	/**資料轉檔頁面產生的檔案路徑*/
	private static String dataTransform;
	
	/**特徵選取頁面產生的檔案路徑*/
	private static String featureSelect;
	
	/**資料轉檔資料夾路徑*/
	private static String dataTransformPath;
	
	/**彙整表格資料夾路徑*/
	private static String summarizeDataPath;
	
	/**特徵選取資料夾路徑*/
	private static String featureSelectPath;
	
	/**將資料放進weka之前需要先行處理*/
	private static String transFeature;
	
	/**
	 * 特徵選取的資訊<br>
	 * 選取的特徵及排名
	 */
	private static String featureInfo;

	/**資料處理資料夾路徑*/
	private static String dataHandlePath;
	
	/**資料處理路徑*/
	private static String dataHandle;
	
	/**資料拆分資料夾路徑*/
	private static String dataSplitPath;
	
	/**流水號路徑*/
	private static String lsh;
	
	/**打亂資料集路徑*/
	private static String disOrganize;
	
	/**
	 * TrainTestSplit呼叫後存放的訓練集<br>
	 * 同時也是預測主頁面顯示選項的來源檔案<br>
	 * (一定不包含三寶)
	 */
	private static String originalTrain;
	
	/**
	 * TrainTestSplit呼叫後存放的測試集<br>
	 * (一定不包含三寶)
	 */
	private static String originalTest;
	
	// 模型相關
	/**模型路徑*/
	private static String modelPath;
	
	/**訓練模型用的訓練集(可能包含三寶)*/
	private static String modelTrain;
	
	/**訓練模型用的測試集(可能包含三寶)*/
	private static String modelTest;
	
	/**分類器模型*/
	private static String classifier;
	
	/**DoStepFile程式 存放的資料夾路徑*/
	private static String doStepPath;
	
	/**各個頁面進行轉換時，產生過渡檔案的存放路徑*/
	private static String tempPath;
	
	/**步驟檔名稱 | 必須存在*/
	private static String step;
	
	/**
	 * 特徵選取或方法選擇<br>
	 * weka不接受資料集時，要自動產出的步驟檔
	 */
	private static String tempStep;
	
	/**資料處理頁面的步驟檔*/
	private static String dataHandleStep;
	
	/**PreviewSheet進行移除流水號的暫存路徑*/
	private static String impAndExpTemp;
	
	/**訓練模型產生的訊息*/
	private static String trainResult;
	
	/**原始預測與實際值的對照 供PreviewSheet讀取*/
	private static String pAnda;
	
	/**給Predictors類別讀的訓練集，是一個過渡檔案*/
	private static String transTrain;
	
	/**給Predictors類別讀的測試集，是一個過渡檔案*/
	private static String transTest;
	
	/**
	 * 主頁面的預測按鈕會產生的檔案<br>
	 * 都存放於此<br>
	 */
	private static String predictPath;
	
	/**
	 * 經主頁面步驟檔轉換後的訓練集<br>
	 * 若手術或麻醉時間有正規化，可轉換還原
	 */
	private static String predictTrain;

	/**丟進模型預測的單筆測試集*/
	private static String predictTest;
	
	/**預測選項*/
	private static String predictOptions;
	
	/**多筆資料預測 檔案輸出*/
	private static String multiplePredict;
	
	/**取得使用者的桌面路徑*/
	static String getDesktop() {
		return desktop;
	}
	//位於資料轉檔頁面，不需檢查
	static String getAppendRecord() {
		return appendRecord;
	}
	static String getMergeDRG() {
		return mergeDRG;
	}
	static String getMergeFeature() {
		return mergeFeature;
	}
	static String getCreateTime() {
		return createTime;
	}
	static String getSummarizeData() {	
		return summarizeDataPath;
	}
	//====================
	static String getRearrangeTitle() {
		setDefaultRawPath();
		return rearrangeTitle;
	}
	static String getFeatureSelect() {
		setDefaultRawPath();
		return featureSelect;
	}
	static String getTransFeature() {
		setDefaultRawPath();
		return transFeature;
	}
	static String getFeatureInfo() {
		return featureInfo;
	}
	static String getDataHandlePath() {
		return dataHandlePath;
	}
	static String getDataHandle() {
		setDefaultRawPath();
		return dataHandle;
	}
	static String getOriginalTrain() {
		return originalTrain;
	}
	static String getOriginalTest() {
		return originalTest;
	}
	static String getModelPath() {
		return modelPath;
	}
	static String getSummarizeDataPath() {
		setDefaultRawPath();
		return summarizeDataPath;
	}
	static String getDataPath() {
		setDefaultRawPath();
		return dataPath.toString();
	}
	static String getNewYearPath() {
		setDefaultRawPath();
		return newYearPath;
	}
	static String getOldYearPath() {
		setDefaultRawPath();
		return oldYearPath;
	}
	static String getDoctorPath() {
		setDefaultRawPath();
		return doctorPath;
	}
	static String getDRGPath() {
		return drgPath;
	}
	static String getDataTransform() {
		setDefaultRawPath();
		return dataTransform;
	}
	static String getDisOrganize() {
		setDefaultRawPath();
		return disOrganize;
	}
	static String getLSH() {
		setDefaultRawPath();
		return lsh;
	}
	static String getDoStepPath() {
		setDefaultRawPath();
		return doStepPath;
	}
	public static String getTempPath() {
		return tempPath;
	}
	public static String getImpAndExpTemp() {	//PreviewSheet
		return impAndExpTemp;
	}
	static String getModelTrain() {
		setDefaultRawPath();
		return modelTrain;
	}
	static String getModelTest() {
		setDefaultRawPath();
		return modelTest;
	}
	static String getTransTrain() {
		return transTrain;
	}
	static String getTransTest() {
		return transTest;
	}
	static String getTrainResult() {
		return trainResult;
	}
	static String getPandA() {
		return pAnda;
	}
	static String getClassifier() {
		return classifier;
	}
	static String getDataHandleStep() {
		return dataHandleStep;
	}
	public static String getStep() {
		return step;
	}
	public static String getTempStep() {
		setDefaultRawPath();
		return tempStep;
	}
	static String getPredictPath() {
		return predictPath;
	}
	static String getPredictTrain() {
		return predictTrain;
	}
	static String getPredictTest() {
		return predictTest;
	}
	static String getPredictOptions() {
		return predictOptions;
	}
	static String getMultiplePredict() {
		return multiplePredict;
	}
	
	static String getRawPath() {
		return rawPath.toString();
	}
	static Path getWorkingData() {
		setDefaultRawPath();
		return workingData;
	}
	static void setRawPath(String rawPath) {
		setRawPath(Paths.get(rawPath));
	}
	public static void setRawPath(Path rawPath) {
		Info.rawPath = rawPath;
		System.out.println("原始資料目錄：" + rawPath);
		try {
			workingData = rawPath.resolveSibling("WorkingData");
			deleteDirectory(workingData);
			Files.createDirectories(workingData);	//建立新的WorkingData
			dataPath = workingData.resolve("Data");
			doctorPath = workingData + "\\Seniority";
			drgPath = workingData + "\\Drg";
			oldYearPath = workingData + "\\YearlyReport";
			newYearPath = Files.createDirectories(dataPath.resolve("FilledYearlyReport")).toString();
			//
			dataTransformPath = Files.createDirectories(dataPath.resolve("DataTransform")).toString();
			appendRecord = dataTransformPath + "\\AppendRecord.csv";
			mergeDRG = dataTransformPath + "\\MergeDRG.csv";
			mergeFeature = dataTransformPath + "\\MergeFeature.csv";
			createTime = dataTransformPath + "\\CreateTime.csv";
			rearrangeTitle = dataTransformPath + "\\RearrangeTitle.csv";
			dataTransform = dataTransformPath + "\\DataTransform.csv";
			summarizeDataPath = Files.createDirectories(dataPath.resolve("SummarizeData")).toString();
			//
			dataHandlePath = Files.createDirectories(dataPath.resolve("DataHandle")).toString();
			dataHandle = dataHandlePath + "\\DataHandle.csv";
			//
			featureSelectPath = Files.createDirectories(dataPath.resolve("FeatureSelect")).toString();
			transFeature = featureSelectPath + "\\TransFeature.csv";
			featureSelect = featureSelectPath + "\\FeatureSelect.csv";
			featureInfo = featureSelectPath + "\\FeatureInfo.csv";
			//
			dataSplitPath = Files.createDirectories(dataPath.resolve("DataSplit")).toString();
			lsh = dataSplitPath + "\\LSH.csv";
			disOrganize = dataSplitPath + "\\DisOrganize.csv";
			//
			doStepPath = Files.createDirectories(dataPath.resolve("DoStepFile")).toString();
			dataHandleStep = doStepPath + "\\DataHandleStep.csv";
			tempStep = doStepPath + "\\TempStepFile.csv";
			//
			tempPath = Files.createDirectories(dataPath.resolve("Temp")).toString();
			pAnda = tempPath + "\\PredictAndActual.csv";
			transTrain = tempPath + "\\TransTrain.csv";
			transTest = tempPath + "\\TransTest.csv";
			modelTrain = tempPath + "\\ModelTrain.csv";
			modelTest = tempPath + "\\ModelTest.csv";
			impAndExpTemp = modelPath + "\\ImpAndExpTemp.csv";
			//
			modelPath = Files.createDirectories(dataPath.resolve("Model")).toString();
			step = modelPath + "\\AllStep.step";
			trainResult = modelPath + "\\TrainResult.csv";
			classifier = modelPath + "\\Predictors.model";
			
			originalTrain = modelPath + "\\OriginalTrain.csv";
			originalTest = modelPath + "\\OriginalTest.csv";
			//
			predictPath = Files.createDirectories(dataPath.resolve("Predict")).toString();
			predictTrain = predictPath + "\\PredictTrain.csv";
			predictTest = predictPath + "\\PredictTest.csv";
			predictOptions = predictPath + "\\PredictOptions.csv";
			multiplePredict = predictPath + "\\MultiplePredict.csv";
			
			System.out.println("工作目錄建立完成");
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
	}
	
	/**通常是程咬金會觸發此函式*/
	private static void setDefaultRawPath() {
		if(rawPath == null) {
			setRawPath(desktop);
		}
	}
	
	/**刪除執行過程中產生的檔案(不包含Model、Predict及Temp目錄)*/
	private static void deleteTemp() {
		deleteDirectory(newYearPath);
		deleteDirectory(dataTransformPath);
		deleteDirectory(summarizeDataPath);
		deleteDirectory(dataHandlePath);
		deleteDirectory(featureSelectPath);
		deleteDirectory(dataSplitPath);
		deleteDirectory(doStepPath);
//		deleteDirectory(tempPath);
	}
	
	/**刪除所有已建立的檔案及目錄*/
	private static void deleteAll() {
		rawPath = null;
		deleteTemp();
		deleteDirectory(tempPath);
		deleteDirectory(modelPath);
		deleteDirectory(predictPath);
	}
	
	/**刪除目錄*/
	private static void deleteDirectory(String delete) {
		if(delete != null) {
			deleteDirectory(Paths.get(delete));
		}
	}
	
	/**刪除目錄*/
	private static void deleteDirectory(Path delete) {
		if(delete == null) {
			System.err.println("欲刪除目錄尚未設置");
			return;
		}else if(Files.notExists(delete)) {
			System.err.println(delete.getFileName() + "目錄不存在");
			return;
		}
		try {
			Files.walkFileTree(delete, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE; 
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
					if (e == null) {
						Files.delete(dir);
					}else{
						System.err.println(e);
					}
					return FileVisitResult.CONTINUE;
				}
				
				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					System.err.println(exc);
					return FileVisitResult.CONTINUE;
				}
			});
			System.out.println(delete.getFileName() + "刪除成功");
		} catch (Exception e) {
			System.err.println(e);
		}
	}
}