package guiFunction;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import preprocess.Split;

/**辨識欄位屬性為數值或一般文字*/
public class Detect {
	/**
	 * 不是數值，true ; 是數值，false
	 * @param inFile 輸入檔名
	 * @throws Exception
	 */
	public static boolean[] digits(String inFile) throws Exception {
		ArrayList<String> firstLine;
		String input;
		try(BufferedReader br = new BufferedReader(new FileReader(inFile))){
			input = br.readLine();	//跳過標題列
			input = br.readLine();
			firstLine = Split.withoutQuotes(input);	//第一列存取
		}
		int size = firstLine.size();
		boolean[] isDigit = new boolean[size];
		for(int i = 0; i < size; i++) {
			isDigit[i] = isDigit(firstLine.get(i));
		}
		return isDigit;
	}
	
	/**判讀欄位值是否為數值*/
	private static boolean isDigit(String str) {
		if(str.isEmpty()) {
			return false;
		}
		for (char c: str.toCharArray()) {
			//如果偵測到不是數值("遇到不是0~9的字元")，回傳false-->抓到了齁
           	if (!Character.isDigit(c))
               	return false;
        }
        return true;
	}
}
