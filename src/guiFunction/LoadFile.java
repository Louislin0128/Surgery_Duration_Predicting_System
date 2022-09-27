package guiFunction;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**載入圖片或文字檔*/
public class LoadFile {
	/**
	 * 載入icon們
	 * @param iconPath 圖片來源路徑
	 * @param iconName 名稱陣列
	 * @param width 寬度
	 * @param height 高度
	 * @return icon們
	 */
	public static ImageIcon[] fromIcons(String iconPath, String[] iconName, int width, int height) {
		ImageIcon[] icons = new ImageIcon[iconName.length];
		for(int i = 0, length = icons.length; i < length; i++) {
			icons[i] = fromIcon(iconPath, iconName[i], width, height);
		}
		return icons;
	}
	/**
	 * 載入icon
	 * @param iconPath 圖片來源路徑
	 * @param iconName 名稱陣列
	 * @param width 寬度
	 * @param height 高度
	 * @return icon
	 */
	public static ImageIcon fromIcon(String iconPath, String iconName, int width, int height) {
		return new ImageIcon(fromImage(iconPath, iconName).getScaledInstance(width, height, Image.SCALE_DEFAULT));
	}
	/**
	 * 載入圖片們
	 * @param imagePath 圖片來源路徑
	 * @param imageName 名稱陣列
	 * @return 圖片們
	 */
	public static BufferedImage[] fromImages(String imagePath, String[] imageName) {
		BufferedImage[] images = new BufferedImage[imageName.length];
		for(int i = 0, length = images.length; i < length; i++) {
			images[i] = fromImage(imagePath, imageName[i]);
		}
		return images;
	}
	/**
	 * 載入圖片
	 * @param imagePath 圖片來源路徑
	 * @param imageName 名稱陣列
	 * @return 圖片
	 */
	public static BufferedImage fromImage(String imagePath, String imageName) {
		try {
			return ImageIO.read(new File(imagePath + imageName));
		} catch (IOException e) {
			System.err.println(imageName + "載入失敗。請檢查路徑是否正確或檔案是否存在。");
		}
		return new BufferedImage(0, 0, 0);
	}
	
	/**
	 * 載入文字們
	 * @param textPath 文字來源路徑
	 * @param tipName 名稱陣列
	 * @return 文字們
	 * @throws IOException
	 */
	public static String[] fromTexts(String textPath, String[] tipName) {
		int length = tipName.length;
		String[] tips = new String[length];
		for (int i = 0; i < length; i++) {
			tips[i] = fromText(textPath + tipName[i]);
		}
		return tips;
	}
	
	/**
	 * 載入文字
	 * @param textPath 文字來源完整路徑
	 * @return 文字
	 * @throws IOException
	 */
	public static String fromText(String textPath) {
		try {
			return Files.readString(Paths.get(textPath), Charset.defaultCharset());
		} catch (IOException e) {
			System.err.println(textPath + "載入失敗。請檢查路徑是否正確或檔案是否存在。");
		}
		return null;
	}
}
