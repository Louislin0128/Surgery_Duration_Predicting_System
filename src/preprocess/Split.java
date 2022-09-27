package preprocess;
import java.util.ArrayList;
import java.util.Vector;

public final class Split {	
	public static ArrayList<String> withQuotes(String input) {		//未切割的整行資料
		return split(input, true);
	}
	
	public static ArrayList<String> withoutQuotes(String input) {	//未切割的整行資料
		return split(input, false);
	}
	
	public static String[] withoutQuotesOfArray(String input) {
		ArrayList<String> inputSplit = split(input, false);
		return inputSplit.toArray(new String[inputSplit.size()]);
	}
	
	public static Vector<String> withoutQuotesOfVector(String input) {
		return new Vector<String>(split(input, false));
	}
	
	private static ArrayList<String> split(String input, boolean withQuotes) {
		boolean inQuotes = false;						//判斷某逗號是否在雙引號中
		StringBuilder tempString = new StringBuilder();	//存放逐字元判斷所串接的字串
		ArrayList<String> inputSplit = new ArrayList<String>();	//每筆資料分欄切割後的結果
		
		for (char c: input.toCharArray()) {	//將input轉換為字元陣列並依序讀出
			if(c == ',' && !inQuotes) {
				inputSplit.add(tempString.toString());
			    tempString.setLength(0);	//將tempString清空
			}else if(c == '\"') {			//雙引號
				inQuotes = !inQuotes;
				if(withQuotes) {
					tempString.append('\"');	//補回"
				}
			}else{
				tempString.append(c);		//其他字元加到tempString中
			}
		}
		inputSplit.add(tempString.toString());
		return inputSplit;
	}
}
