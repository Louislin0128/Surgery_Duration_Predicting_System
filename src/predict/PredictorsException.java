package predict;

/**
 * predict.Predictors的例外類別<br>
 * 若使用者給的資料集不符合規定就拋出例外
 */
public class PredictorsException extends Exception {
	private static final long serialVersionUID = 6380200091867268138L;
	/**
	 * predict.Predictors的例外類別<br>
	 * 若使用者給的資料集不符合規定就拋出例外
	 * @param message 錯誤訊息
	 */
	public PredictorsException(String message) {
        super(message);
    }
}