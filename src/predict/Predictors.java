package predict;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import guiFunction.CreateFile;
import guiFunction.Step;
import guiFunction.TransIfNeeded;
import preprocess.Split;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.Utils;
import weka.core.converters.CSVLoader;
import weka.gui.GenericObjectEditor;
import weka.gui.PropertySheetPanel;

public class Predictors {
	private static String[] classifierName = {	// 依照分類器名稱排序
			"weka.classifiers.meta.AdditiveRegression",
			"weka.classifiers.meta.AttributeSelectedClassifier",
			"weka.classifiers.meta.Bagging",
			"predict.BPNN",
			"weka.classifiers.meta.CVParameterSelection",
			"weka.classifiers.trees.DecisionStump",
			"weka.classifiers.rules.DecisionTable",
			"weka.classifiers.meta.FilteredClassifier",
			"weka.classifiers.functions.GaussianProcesses",
			"weka.classifiers.lazy.IBk",
			"weka.classifiers.meta.IterativeClassifierOptimizer",
			"weka.classifiers.lazy.KStar",
			"weka.classifiers.functions.LinearRegression",
			"weka.classifiers.lazy.LWL",
			"weka.classifiers.trees.M5P",
			"weka.classifiers.rules.M5Rules",
			"weka.classifiers.functions.MultilayerPerceptron",
			"weka.classifiers.meta.MultiScheme",
			"weka.classifiers.meta.RandomCommittee",
			"weka.classifiers.trees.RandomForest",
			"weka.classifiers.meta.RandomizableFilteredClassifier",
			"weka.classifiers.meta.RandomSubSpace",			
			"weka.classifiers.trees.RandomTree",
			"weka.classifiers.meta.RegressionByDiscretization",			
			"weka.classifiers.trees.REPTree",
			"weka.classifiers.functions.SMOreg",
			"weka.classifiers.meta.Stacking",
			"weka.classifiers.meta.Vote",
			"weka.classifiers.meta.WeightedInstancesHandlerWrapper",
			"weka.classifiers.rules.ZeroR"};
	private static String[] classifierSimpleName;
	/**取得可供使用的分類器陣列*/
	public static String[] getClassifierSimpleName() {
		return classifierSimpleName;
	}
	static {
		int length = classifierName.length;
		classifierSimpleName = new String[length];
		for(int i = 0; i < length; i++) {
			classifierSimpleName[i] = classifierName[i].substring(classifierName[i].lastIndexOf(".") + 1);
		}
		GenericObjectEditor.determineClasses();
	}
	
	private static PropertySheetPanel propertyPanel = new PropertySheetPanel(false);
	/**得到更改參數的版面*/
	public static JPanel getClassifierPanel() {
		return propertyPanel;
	}
	/**
	 * 更改使用的分類器
	 * @param index 分類器陣列索引值
	 * @throws Exception
	 */
	public static void changeClassifier(int index) throws Exception {
		clsr = AbstractClassifier.forName(classifierName[index], null);
		propertyPanel.setTarget(clsr);
	}
	
	private static Classifier clsr;
	private Instances trainInsts, testInsts;
	private String origTrain, step, tempStep;
	/**
	 * 讀取已建立並訓練完成的模型
	 * @param loadModel 輸入模型路徑
	 * @throws Exception
	 */
	public Predictors(String loadModel) throws Exception {
		Object[] load = SerializationHelper.readAll(loadModel);
		clsr = (Classifier) load[0];
		trainInsts = (Instances) load[1];
	}
	
	/**
	 * 跳過資料檢查階段，直接建立
	 * @param origTrain 原始未轉換訓練集 (必未經過三寶)
	 * @param transTrain 經過轉換訓練集 (因為不符合BPNN要求，自動轉換的檔案)
	 * @param transTest 經過轉換測試集 (因為不符合BPNN要求，自動轉換的檔案)
	 * @param step 步驟檔
	 * @throws IOException
	 */
	public Predictors(String origTrain, String transTrain, String transTest, String step) throws IOException {
		this.origTrain = origTrain;
		this.step = step;
		trainInsts = readFile(transTrain);
		testInsts = readFile(transTest);
	}
	
	/**
	 * 預測分類器的建構子
	 * @param origTrain 原始未轉換訓練集 (必未經過三寶)
	 * @param modelTrain 訓練集 (可能經過三寶)
	 * @param modelTest 測試集 (可能經過三寶)
	 * @param transTrain 供內部轉換的訓練集
	 * @param transTest 供內部轉換的測試集
	 * @param step 步驟檔
	 * @param tempStep 暫時步驟檔
	 * 
	 * @throws PredictorsException 資料集不符合規定
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Predictors(String origTrain,
					  String modelTrain, String modelTest,
					  String transTrain, String transTest,
					  String step, String tempStep)
					  throws PredictorsException, FileNotFoundException, IOException {
		try {
			changeClassifier(0);
		} catch (Exception e) {
			// 若發生例外，請重新啟動系統
			JOptionPane.showMessageDialog(null, "更改分類器時發生問題，請重新啟動系統。");
			System.exit(1);
		}
		
		this.origTrain = origTrain;
		this.step = step;
		this.tempStep = tempStep;
		
		// 產生給CSVLoader的檔案以辨識String屬性的欄位
		CreateFile.forWekaLoader(modelTrain, transTrain);
		CreateFile.forWekaLoader(modelTest, transTest);
		// 訓練集開始檢查 | 不能出現String屬性的欄位
		trainInsts = readFile(transTrain);
		// 測試集開始檢查 | 不能出現String屬性的欄位
		testInsts = readFile(transTest);
		
		//資料集開始檢查 | 不能出現String屬性的欄位
		if(trainInsts.checkForStringAttributes()) {
			System.err.println("訓練集不合乎規定");
			ArrayList<String> stringAttr = listStringAttr(trainInsts);
			Step.buildAutoRemove(stringAttr, tempStep);
			throw new PredictorsException(getErrorMessage(stringAttr));
		}
	}
	
	/**
	 * 讀取檔案並判斷有無不能使用的特徵
	 * @param inFile 輸入檔案
	 * @return Instances
	 * @throws PredictorsException 資料集不符合規定
	 * @throws IOException
	 */
	private Instances readFile(String inFile) throws IOException {
		CSVLoader csv = new CSVLoader();
		csv.setSource(new File(inFile));
		Instances insts = csv.getDataSet();
		
		if(insts.attribute(0).name().equals("流水號")) {
			insts.deleteAttributeAt(0);	// 若第一欄為流水號，就刪除最後一欄
		}
		insts.setClassIndex(insts.numAttributes() - 1);
		//若無報錯，代表資料可被weka接受
		return insts;
	}
	
	/**列出String型態的weka特徵*/
	private ArrayList<String> listStringAttr(Instances insts) {
		Enumeration<Attribute> attrs = insts.enumerateAttributes();
		ArrayList<String> stringAttr = new ArrayList<String>(insts.numAttributes());
		Attribute attr;
		while(attrs.hasMoreElements()) {
			attr = attrs.nextElement();
			if (attr.isString()) {	//若該欄位為String Type
				stringAttr.add(attr.name());//加入陣列
			}
		}
		return stringAttr;
	}
	
	/**取得關於String的錯誤訊息*/
	private String getErrorMessage(ArrayList<String> stringAttr) {
		StringBuilder tempString = new StringBuilder("因為這些欄位空值太多，無法進行特徵選取，將自動刪除：\n");
		stringAttr.forEach(s -> {
			tempString.append("「")
					  .append(s)
					  .append("」 ");	
		});
		tempString.append("\n是：自動刪除上列特徵；")
				  .append("\n否：無法使用本頁面功能。");
		return tempString.toString();
	}
	
	/**
	 * 儲存訓練模型
	 * @param fileName 輸出路徑
	 * @throws Exception
	 */
	public void saveClassifier(String fileName) throws Exception {
		SerializationHelper.writeAll(fileName, new Object[] {clsr, trainInsts});
	}
	
	/**確認資料是否符合要求*/
	public String checkBPNNdata() throws Exception {
		String clsrName = clsr.getClass().getSimpleName();
		if(clsrName.equals("BPNN")) {	// 如果使用BPNN，檢查資料集各欄位的型態
			System.out.println("檢查資料集中");
			if(trainInsts.checkForAttributeType(Attribute.NOMINAL)) {
				System.err.println("資料集不符合需求，需要轉換");
				ArrayList<ArrayList<String>> transAttrs = listTransAttrs(trainInsts);
				Step.buildAutoStep(transAttrs, tempStep);
				return getTransMessage(transAttrs);
			}
		}
		return null;
	}
	
	/**
	 * 訓練分類器
	 * @param outFile 輸出訓練值與對照值檔案
	 * @return 訓練結果字串
	 * @throws PredictorsException
	 * @throws Exception
	 */
	public String trainClassifier(String outFile) throws Exception {
		String clsrName = clsr.getClass().getSimpleName();
		System.out.println("建立模型中");
		StringBuilder results = new StringBuilder();
		results.append("使用的分類器：").append(clsrName).append('\n');
		//以訓練集訓練模型
		long start = System.currentTimeMillis();
		clsr.buildClassifier(trainInsts);
		//以訓練集測試模型
		Evaluation eval = new Evaluation(trainInsts);
		eval.evaluateModel(clsr, trainInsts);
		long end = System.currentTimeMillis();
		
		//輸出模型的各項評估數值
		String cc = Utils.doubleToString(eval.correlationCoefficient(), 4);
		TransIfNeeded trans = TransIfNeeded.transValue(step, origTrain);//若有做三寶，將評估值還原
		String transMAE = Utils.doubleToString(trans.get(eval.meanAbsoluteError()), 4);
		
//		System.out.println(origTrain);
//		System.out.println("MAE "+eval.meanAbsoluteError());
//		System.out.println("transMAE "+trans.get(eval.meanAbsoluteError()));
		
		String transRMSE = Utils.doubleToString(trans.get(eval.rootMeanSquaredError()), 4);
		String transType = trans.getType();
		results.append("使用訓練集評估模型的各項指標數值\n");
		results.append("Correlation coefficient(相關係數)：").append(cc).append('\n');
		results.append("Mean absolute error(平均絕對誤差)：").append(transMAE).append(" (").append(transType).append(")\n");
		results.append("Root mean squared error(均方根誤差)：").append(transRMSE).append(" (").append(transType).append(")\n");
		results.append("訓練模型時間：").append(end - start).append("(ms)\n\n");
		
		//以測試集測試模型
		start = System.currentTimeMillis();
		eval = new Evaluation(trainInsts);
		eval.evaluateModel(clsr, testInsts);
		end = System.currentTimeMillis();
		//輸出模型的各項評估數值
		cc = Utils.doubleToString(eval.correlationCoefficient(), 4);
		transMAE = Utils.doubleToString(trans.get(eval.meanAbsoluteError()), 4);
		transRMSE = Utils.doubleToString(trans.get(eval.rootMeanSquaredError()), 4);
		results.append("使用測試集評估模型的各項指標數值\n");
		results.append("Correlation coefficient(相關係數)：").append(cc).append('\n');
		results.append("Mean absolute error(平均絕對誤差)：").append(transMAE).append(" (").append(transType).append(")\n");
		results.append("Root mean squared error(均方根誤差)：").append(transRMSE).append(" (").append(transType).append(")\n");
		results.append("測試模型時間：").append(end - start).append("(ms)");
		
		//輸出測試集的實際值與預測值的對照檔案
		StringBuilder pAndAstr = new StringBuilder("實際值,預測值,差值(實際值-預測值)\n");
		eval.predictions().forEach(p -> {
			double actual = trans.get(p.actual()), predict = trans.get(p.predicted()), deviation = actual - predict;
			pAndAstr.append(actual == Double.NaN ? '?' : actual)	//實際
					.append(',')
					.append(predict == Double.NaN ? '?' : predict)	//預測
					.append(',')
					.append(deviation == Double.NaN ? '?' : deviation)//差值
					.append('\n');
		});
		CreateFile.toCSV(outFile, pAndAstr.toString());
		
		return results.toString();
	}
	
	/**
	 * 列舉欲轉換特徵
	 * @param train 訓練集
	 * @return 欲轉換特徵陣列
	 */
	public ArrayList<ArrayList<String>> listTransAttrs(Instances train) {
		ArrayList<ArrayList<String>> transAttrs = new ArrayList<>(3);
		transAttrs.add(new ArrayList<>());//TargetEncoding
		transAttrs.add(new ArrayList<>());//NormalizeFeature
		transAttrs.add(new ArrayList<>());//RemoveRecord
		Enumeration<Attribute> trainAttrs = train.enumerateAttributes();
		Attribute trainAttr;
		String attrName;
		while(trainAttrs.hasMoreElements()) {
			trainAttr = trainAttrs.nextElement();
			attrName = trainAttr.name();
			if(trainAttr.isNominal()) {
				transAttrs.get(0).add(attrName);
			}
			transAttrs.get(1).add(attrName);
			transAttrs.get(2).add(attrName);
		}
		transAttrs.get(1).add(train.classAttribute().name());
		return transAttrs;
	}
	
	/**取得關於將自動轉換欄位的訊息*/
	private String getTransMessage(ArrayList<ArrayList<String>> trans) {
		StringBuilder tempStr = new StringBuilder("以下所列欄位不符合演算法(BPNN)，無法執行演算法，將自動轉換：");
		tempStr.append("\n未數值化：");
		trans.get(0).forEach(s -> tempStr.append(s).append(' '));
		
		tempStr.append("\n未正規化：");
		trans.get(1).forEach(s -> tempStr.append(s).append(' '));
		
		tempStr.append("\n將移除空值：");
		trans.get(2).forEach(s -> tempStr.append(s).append(' '));
		return tempStr.toString();
	}
	
	/**
	 * 得到單筆預測值
	 * @param fileName 待預測檔案
	 * @param trans 傳入TransIfNeeded以轉換數值
	 * @return 經轉換後的預測值
	 * @throws Exception
	 */
	public double getValue(String fileName, TransIfNeeded trans) throws Exception {	//一筆待預測資料
		System.out.println("單筆預測中");
		ArrayList<String> predict;
		String input;
		try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			input = br.readLine();//跳過標題列
			input = br.readLine();
			predict = Split.withoutQuotes(input);
		}
		return trans.get(getPredict(predict));
	}
	
	/**
	 * 產出多筆預測值檔案
	 * @param inFile 測試輸入檔案
	 * @param outFile 測試輸出檔案
	 * @param trans 傳入TransIfNeeded以轉換數值
	 * @throws Exception
	 */
	public void buildPredictFile(String inFile, String outFile, TransIfNeeded trans) throws Exception {	//一筆待預測資料
		System.out.println("多筆預測中");
		ArrayList<String> predict;
		String input;
		try(BufferedReader br = new BufferedReader(new FileReader(inFile))) {
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))) {
			bw.write(br.readLine());//寫入標題列
			while((input = br.readLine()) != null) {
				predict = Split.withoutQuotes(input);
				bw.newLine();
				input = input.substring(0, input.lastIndexOf(","));
				bw.write(input + "," + trans.get(getPredict(predict)));
			}
		}
		}
	}
	
	/**
	 * 得到單筆預測值
	 * @param predict 測試資料陣列
	 * @return 預測值
	 * @throws Exception
	 */
	private double getPredict(ArrayList<String> predict) throws Exception {
		double[] values = new double[predict.size()];
		Enumeration<Attribute> attrs = trainInsts.enumerateAttributes();
		Attribute attr;
		int i = 0;
		while(attrs.hasMoreElements()) {
			attr = attrs.nextElement();
			if(attr.isNominal()) {
				values[i] = trainInsts.attribute(i).indexOfValue(predict.get(i));
			}else if(attr.isNumeric()) {
				values[i] = Double.parseDouble(predict.get(i));
			}else {
				System.err.println(attr.name() + "無法預測");
			}
			i++;
		}
		DenseInstance predictInst = new DenseInstance(1.0, values);
		predictInst.setDataset(trainInsts);
		predictInst.setClassMissing();
		return clsr.classifyInstance(predictInst);
	}
}
