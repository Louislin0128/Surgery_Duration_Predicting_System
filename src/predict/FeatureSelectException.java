package predict;

/**
 * predict.FeatureSelect的例外類別<br>
 * 若使用者給的資料集不符合規定就拋出例外
 */
public class FeatureSelectException extends Exception {
	private static final long serialVersionUID = -8938154424602669607L;
	/**
	 * predict.FeatureSelect的例外類別<br>
	 * 若使用者給的資料集不符合規定就拋出例外
	 * @param message 錯誤訊息
	 */
	public FeatureSelectException(String message) {
        super(message);
    }
}
