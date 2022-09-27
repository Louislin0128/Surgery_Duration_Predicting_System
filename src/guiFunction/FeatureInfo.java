package guiFunction;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**建立特徵重要度檔案*/
public class FeatureInfo {
	/**
	 * 供特徵選取頁面輸出資料
	 * @param keep 保留特徵陣列
	 * @param attrsRank 特徵重要度
	 * @param outFile 輸出檔案
	 * @throws IOException
	 */
	public static void buildWithScore(ArrayList<String> keep, LinkedHashMap<String, String> attrsRank, String outFile) throws IOException {
		try(BufferedWriter br = new BufferedWriter(new FileWriter(outFile))){
			br.write("選取的特徵,重要度");
			for(Entry<String, String> e: attrsRank.entrySet()) {
				if(keep.contains(e.getKey())) {
					br.newLine();
					br.write(e.getKey() + "," + e.getValue());
				}
			}	
		}
	}
	
	/**
	 * 若沒有特徵選取資料，建立一個不提供排名的檔案
	 * @param feature 特徵陣列
	 * @param outFile 輸出檔案
	 * @throws IOException
	 */
	public static void buildWithoutScore(String[] feature, String outFile) throws IOException {
		try(BufferedWriter br = new BufferedWriter(new FileWriter(outFile))){
			br.write("選取的特徵,重要度");
			for(String s: feature) {
				br.newLine();
				br.write(s + ",無法提供");
			}
		}
	}
	
	/**
	 * 若沒有特徵選取資料，建立一個不提供排名的檔案
	 * @param feature
	 * @param outFile
	 * @throws IOException
	 */
	public static void buildWithoutScore(ArrayList<String> feature, String outFile) throws IOException {
		buildWithoutScore(feature.toArray(new String[feature.size()]), outFile);
	}
}
