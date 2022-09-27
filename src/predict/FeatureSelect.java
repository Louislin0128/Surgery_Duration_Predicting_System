package predict;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;

import guiFunction.CreateFile;
import guiFunction.Step;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.CorrelationAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.CSVLoader;

public class FeatureSelect {
	/**
	 * 特徵選取建構子
	 * @param inFile 輸入檔案
	 * @param readFile 供weka讀取的檔案
	 * @param tempStep 暫時步驟檔
	 * @return 特徵名稱 | 特徵重要度
	 * @throws FeatureSelectException 資料集不符合要求
	 * @throws Exception 其他例外
	 */
	public static LinkedHashMap<String, String> startRank(String inFile, String readFile, String tempStep)
												throws FeatureSelectException, Exception {
		return new FeatureSelect(inFile, readFile, tempStep).getAttrsRank();
	}
	
	private Instances insts;
	private FeatureSelect(String inFile, String readFile, String tempStep)
			throws FeatureSelectException, FileNotFoundException, IOException {
		// 產生給Weka的檔案以辨識String屬性的欄位
		CreateFile.forWekaLoader(inFile, readFile);
		//==============================
		CSVLoader csv = new CSVLoader();
		csv.setSource(new File(readFile));
		insts = csv.getDataSet();
		if(insts.attribute(0).name().equals("流水號")) {
			insts.deleteAttributeAt(0);	//移除流水號
		}
		insts.setClassIndex(insts.numAttributes() - 1);		//設定標籤(手術時間或麻醉時間)
		
		//開始檢查 | 不能出現String屬性的欄位
		if(insts.checkForStringAttributes()) {	//true代表有欄位是String屬性
			System.err.println("資料集不合乎規定");
			ArrayList<String> stringAttr = listStringAttr(insts);
			Step.buildAutoRemove(stringAttr, tempStep);
			throw new FeatureSelectException(getErrorMessage(stringAttr));
		}
		//若無報錯，代表資料可被weka接受
	}
	
	/**取得關於string attribute錯誤訊息*/
	private String getErrorMessage(ArrayList<String> remove) {
		StringBuilder tempString = new StringBuilder("因為這些欄位空值太多，無法進行特徵選取，須於資料處理頁面刪除：\n");
		remove.forEach(s -> tempString.append("「").append(s).append("」\n"));
		tempString.append("是：自動移除無法使用的特徵；\n").append("否：列出所有特徵供您自行選取。");
		return tempString.toString();
	}
	
	/**列舉string attribute*/
	private ArrayList<String> listStringAttr(Instances insts) {	//確保沒有String type的屬性
		Enumeration<Attribute> attrs = insts.enumerateAttributes();
		ArrayList<String> stringAttr = new ArrayList<String>(insts.numAttributes());
		Attribute attr;
		while(attrs.hasMoreElements()) {
			attr = attrs.nextElement();
			if (attr.isString()) {	//若該欄位為String Type
				stringAttr.add(attr.name());	//加入陣列
			}
		}
		return stringAttr;
	}
	
	/**取得特徵排序結果*/
	private LinkedHashMap<String, String> getAttrsRank() throws Exception {
		AttributeSelection attrSelection = new AttributeSelection();
		CorrelationAttributeEval evaluator = new CorrelationAttributeEval();
		Ranker ranker = new Ranker();
		attrSelection.setEvaluator(evaluator);
		attrSelection.setSearch(ranker);
		attrSelection.SelectAttributes(insts);
		System.out.println(attrSelection.toResultsString());
		
		LinkedHashMap<String, String> rankAttr = new LinkedHashMap<String, String>(
		attrSelection.numberAttributesSelected());
		for (double[] eachAttrRank : attrSelection.rankedAttributes()) {
			rankAttr.put(insts.attribute((int) eachAttrRank[0]).name(), Utils.doubleToString(eachAttrRank[1], 4));
			
		}
		return rankAttr;
	}
}
