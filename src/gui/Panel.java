package gui;

/**各Panel共有的功能*/
interface Panel {
	/**設定該介面必要的檔案，通常為初始化的函式*/
	public void setFile();
	/**重設該版面*/
	public void reset();
}
