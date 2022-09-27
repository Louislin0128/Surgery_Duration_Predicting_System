package guiComponent;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**此為抽象類，需要實作按下╳按鈕後的動作*/
public abstract class AbstractTools implements ActionListener {
	public enum Mode {ExtractRecord, RemoveRecord, RemoveOutlier, RemoveOutlierBySD, RemoveFeature, TargetEncoding, NormalizeFeature, StandardizeFeature}
	private Font font18 = new Font("微軟正黑體", Font.PLAIN, 18);
	private String tempIn, tempOut, adopt, outPath;// 暫時輸入 | 暫時輸出 | 採用檔案 | 輸出路徑
	private String ref;	// 紀錄手術時間或麻醉時間欄位的標題
	private String[] title; // 檔案的標題
	private String[][] content;
	private int fileIndex = 1;
	
	/**
	 * 建立資料處理頁面各功能視覺
	 * @param inFile 輸入檔案
	 * @param outPath 輸出路徑
	 */
	protected AbstractTools(String inFile, String outPath) {
		this.outPath = outPath;
		this.tempIn = inFile;
		this.adopt = tempIn;
		update(new String[0], new String[0][]);
	}
	
	/**
	 * 建立資料處理頁面各功能視覺
	 * @param inFile 輸入檔案
	 * @param outPath 輸出路徑
	 * @param title 欄位標題陣列
	 * @param content 內容陣列
	 */
	protected AbstractTools(String inFile, String outPath, String[] title, String[][] content) {
		this.outPath = outPath;
		this.tempIn = inFile;
		this.adopt = tempIn;
		update(title, content);
	}
	
	/**
	 * 更新選項顯示內容
	 * @param title 欄位標題陣列
	 * @param content 內容陣列
	 */
	public void update(String[] title, String[][] content) {
		this.ref = title[title.length - 1];	// 最後一欄的手術時間 | 麻醉時間
		this.title = title;
		this.content = content;
	}
	
	/**
	 * 重設資料輸入來源
	 * @param inFile
	 */
	public void setInFile(String inFile) {
		this.tempIn = inFile;
	}
	
	/**
	 * 根據指定的模式，回傳功能參數版面
	 * @param className 功能名稱列舉
	 * @return 功能參數版面
	 */
	public Tool newInstance(Mode className) {
		switch(className) {
		case ExtractRecord:
			return new ExtractRecord();
		case RemoveRecord:
			return new RemoveRecord();
		case RemoveOutlier:
			return new RemoveOutlier();
		case RemoveOutlierBySD:
			return new RemoveOutlierBySD();
		case RemoveFeature:
			return new RemoveFeature();
		case TargetEncoding:
			return new TargetEncoding();
		case NormalizeFeature:
			return new NormalizeFeature();
		case StandardizeFeature:
			return new StandardizeFeature();
		default:
			throw new IllegalArgumentException("未定義" + className);
		}
	}
	
	/**
	 * 根據指定的模式，回傳功能參數版面
	 * @param className 功能名稱
	 * @param title 目標欄位
	 * @param content 內容
	 * @return 功能參數版面
	 */
	public Tool newInstance(String className, String title, String content) {
		switch(Mode.valueOf(className)) {
		case ExtractRecord:
			return new ExtractRecord(title, content);
		case RemoveRecord:
			return new RemoveRecord(title, content);
		case RemoveOutlier:
			return new RemoveOutlier(title);
		case RemoveOutlierBySD:
			return new RemoveOutlierBySD(title, content);
		case RemoveFeature:
			return new RemoveFeature(title);
		case TargetEncoding:
			return new TargetEncoding(title);
		case NormalizeFeature:
			return new NormalizeFeature(title);
		case StandardizeFeature:
			return new StandardizeFeature(title);
		default:
			throw new IllegalArgumentException("未定義" + className);
		}
	}

	private class ExtractRecord extends JPanel implements ItemListener, Tool {
		private static final long serialVersionUID = 1063036644756893424L;
		private JLabel erL1;
		private JComboBox<String> erTitle;
		private JLabel erL2;
		private JAutoCompleteComboBox erContent;
		private JButton erButton;

		private ExtractRecord() {
			setLayout(new BorderLayout());
			setBorder(new EmptyBorder(5, 5, 5, 5));
			setToolTipText("提取指定欄位之特定內容值");
			
			Box box = Box.createHorizontalBox();
			add(box, BorderLayout.CENTER);
			erL1 = new JLabel("提取");
			erL1.setFont(font18);
			box.add(erL1);
			box.add(Box.createHorizontalStrut(10));

			erTitle = new JComboBox<>(title);
			erTitle.addItemListener(this);
			erTitle.setFont(font18);
			box.add(erTitle);
			box.add(Box.createHorizontalStrut(10));

			erL2 = new JLabel("的");
			erL2.setFont(font18);
			box.add(erL2);
			box.add(Box.createHorizontalStrut(10));
			
			erContent = new JAutoCompleteComboBox(content[0]);
			erContent.setFont(font18);
			box.add(erContent);
			box.add(Box.createHorizontalStrut(10));

			erButton = new JButton("╳");
			erButton.addActionListener(AbstractTools.this);
			add(erButton, BorderLayout.EAST);
		}
		
		private ExtractRecord(String title, String content) {
			this();
			erTitle.setSelectedItem(title);
			erContent.setSelectedItem(content);
		}
		
		@Override
		public void execute() throws Exception {
			String title = getTitle();
			String content = getContent();
			tempOut = outPath + "\\" + fileIndex++ + "_ER_" + title + ".csv";
			preprocess.ExtractRecord.exec(tempIn, title, content, tempOut);
			tempIn = tempOut;
		}
		
		@Override
		public String getTitle() {
			return erTitle.getSelectedItem().toString();
		}
		
		@Override
		public String getContent() {
			return erContent.getSelectedItem().toString();
		}
		
		@Override
		public boolean canMove() {
			return true;
		}
		
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				erContent.removeAllItems();
				for (String str : content[erTitle.getSelectedIndex()]) {
					erContent.addItem(str);
				}
				erContent.validate();
			}
		}
	}

	private class RemoveRecord extends JPanel implements ItemListener, Tool {
		private static final long serialVersionUID = -557456313031145042L;
		private JLabel rrL1;
		private JComboBox<String> rrTitle;
		private JLabel rrL2;
		private JAutoCompleteComboBox rrContent;
		private JButton rrButton;

		private RemoveRecord() {
			setLayout(new BorderLayout());
			setBorder(new EmptyBorder(5, 5, 5, 5));
			setToolTipText("移除指定欄位之特定內容值");

			Box box = Box.createHorizontalBox();
			add(box, BorderLayout.CENTER);
			rrL1 = new JLabel("刪除");
			rrL1.setFont(font18);
			box.add(rrL1);
			box.add(Box.createHorizontalStrut(10));

			rrTitle = new JComboBox<String>(title);
			rrTitle.addItemListener(this);
			rrTitle.setFont(font18);
			box.add(rrTitle);
			box.add(Box.createHorizontalStrut(10));

			rrL2 = new JLabel("的");
			rrL2.setFont(font18);
			box.add(rrL2);
			box.add(Box.createHorizontalStrut(10));
			
			rrContent = new JAutoCompleteComboBox(content[0]);
			rrContent.setFont(font18);
			box.add(rrContent);
			box.add(Box.createHorizontalStrut(10));

			rrButton = new JButton("╳");
			rrButton.addActionListener(AbstractTools.this);
			add(rrButton, BorderLayout.EAST);
		}
		
		private RemoveRecord(String title, String content) {
			this();
			rrTitle.setSelectedItem(title);
			rrContent.setSelectedItem(content);
		}
		
		@Override
		public void execute() throws Exception {
			String title = getTitle();
			String content = getContent();
			tempOut = outPath + "\\" + fileIndex++ + "_RR_" + title + ".csv";	
			preprocess.RemoveRecord.exec(tempIn, title, content, tempOut);
			tempIn = tempOut;
		}
		
		@Override
		public String getTitle() {
			return rrTitle.getSelectedItem().toString();
		}
		
		@Override
		public String getContent() {
			return rrContent.getSelectedItem().toString();
		}
		
		@Override
		public boolean canMove() {
			return true;
		}
		
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				rrContent.removeAllItems();
				for (String str : content[rrTitle.getSelectedIndex()])
					rrContent.addItem(str);
				rrContent.validate();	
			}
		}
	}

	private class RemoveOutlier extends JPanel implements Tool {
		private static final long serialVersionUID = -3099948733994545837L;
		private JLabel roL1;
		private JComboBox<String> roTitle;
		private JLabel roL2;
		private JButton roButton;

		private RemoveOutlier() {
			setLayout(new BorderLayout());
			setBorder(new EmptyBorder(5, 5, 5, 5));
			setToolTipText("移除指定欄位離群值(僅限數值欄位)");

			Box box = Box.createHorizontalBox();
			add(box, BorderLayout.CENTER);
			roL1 = new JLabel("移除");
			roL1.setFont(font18);
			box.add(roL1);
			box.add(Box.createHorizontalStrut(10));

			roTitle = new JComboBox<String>(title);
			roTitle.setFont(font18);
			box.add(roTitle);
			box.add(Box.createHorizontalStrut(10));

			roL2 = new JLabel("的離群值");
			roL2.setFont(font18);
			box.add(roL2);
			box.add(Box.createHorizontalStrut(10));

			roButton = new JButton("╳");
			roButton.addActionListener(AbstractTools.this);
			add(roButton, BorderLayout.EAST);
		}
		
		private RemoveOutlier(String title) {
			this();
			roTitle.setSelectedItem(title);
		}
		
		@Override
		public void execute() throws Exception {
			String title = getTitle();
			tempOut = outPath + "\\" + fileIndex++ + "_RO_" + title + ".csv";
			preprocess.RemoveOutlier.exec(tempIn, title, tempOut);
			tempIn = tempOut;
		}
		
		@Override
		public String getTitle() {
			return roTitle.getSelectedItem().toString();
		}
		
		@Override
		public String getContent() {
			return null;
		}
		
		@Override
		public boolean canMove() {
			return true;
		}
	}
	
	private class RemoveOutlierBySD extends JPanel implements Tool {
		private static final long serialVersionUID = 1230009594917382902L;
		private JLabel rosdL1;
		private JComboBox<String> rosdTitle;
		private JTextField rosdContent;
		private JLabel rosdL2;
		private JButton rosdButton;

		private RemoveOutlierBySD() {
			setLayout(new BorderLayout());
			setBorder(new EmptyBorder(5, 5, 5, 5));
			setToolTipText("移除指定欄位離群值(僅限數值欄位)。計算某平均值，並移除以該值為基準的某標準差以外的值。");

			Box box = Box.createHorizontalBox();
			add(box, BorderLayout.CENTER);
			rosdL1 = new JLabel("移除");
			rosdL1.setFont(font18);
			box.add(rosdL1);
			box.add(Box.createHorizontalStrut(10));

			rosdTitle = new JComboBox<String>(title);
			rosdTitle.setFont(font18);
			box.add(rosdTitle);
			box.add(Box.createHorizontalStrut(10));
			
			rosdContent = new JTextField();
			rosdContent.setFont(font18);
			box.add(rosdContent);
			box.add(Box.createHorizontalStrut(10));

			rosdL2 = new JLabel("標準差之外的離群值");
			rosdL2.setFont(font18);
			box.add(rosdL2);
			box.add(Box.createHorizontalStrut(10));

			rosdButton = new JButton("╳");
			rosdButton.addActionListener(AbstractTools.this);
			add(rosdButton, BorderLayout.EAST);
		}
		
		private RemoveOutlierBySD(String title, String content) {
			this();
			rosdTitle.setSelectedItem(title);
			rosdContent.setText(content);
		}

		@Override
		public void execute() throws Exception {
			String title = getTitle();
			String content = getContent();
			tempOut = outPath + "\\" + fileIndex++ + "_ROSD_" + title + ".csv";
			preprocess.RemoveOutlierBySD.exec(tempIn, title, content, tempOut);
			tempIn = tempOut;
		}

		@Override
		public String getTitle() {
			return rosdTitle.getSelectedItem().toString();
		}

		@Override
		public String getContent() {
			return rosdContent.getText();
		}

		@Override
		public boolean canMove() {
			return true;
		}
	}

	private class RemoveFeature extends JPanel implements Tool {
		private static final long serialVersionUID = 6058836880761843238L;
		private JLabel rfL1;
		private JComboBox<String> rfTitle;
		private JLabel rfL2;
		private JButton rfButton;

		private RemoveFeature() {
			setLayout(new BorderLayout());
			setBorder(new EmptyBorder(5, 5, 5, 5));
			setToolTipText("移除指定欄位");

			Box box = Box.createHorizontalBox();
			add(box, BorderLayout.CENTER);
			rfL1 = new JLabel("刪除");
			rfL1.setFont(font18);
			box.add(rfL1);
			box.add(Box.createHorizontalStrut(10));

			rfTitle = new JComboBox<String>(title);
			rfTitle.setFont(font18);
			box.add(rfTitle);
			box.add(Box.createHorizontalStrut(10));

			rfL2 = new JLabel("的所有內容");
			rfL2.setFont(font18);
			box.add(rfL2);
			box.add(Box.createHorizontalStrut(10));

			rfButton = new JButton("╳");
			rfButton.addActionListener(AbstractTools.this);
			add(rfButton, BorderLayout.EAST);
		}
		
		private RemoveFeature(String title) {
			this();
			rfTitle.setSelectedItem(title);
		}
		
		@Override
		public void execute() throws Exception {
			String title = getTitle();
			tempOut = outPath + "\\" + fileIndex++ + "_RF_" + title + ".csv";
			preprocess.RemoveFeature.exec(tempIn, tempOut, title);
			tempIn = tempOut;
		}
		
		@Override
		public String getTitle() {
			return rfTitle.getSelectedItem().toString();
		}
		
		@Override
		public String getContent() {
			return null;
		}
		
		@Override
		public boolean canMove() {
			return true;
		}
	}

	private class TargetEncoding extends JPanel implements Tool {
		private static final long serialVersionUID = 8270140149339606230L;
		private JLabel teL1;
		private JLabel teL2;
		private JComboBox<String> teTitle;
		private JButton teButton;

		private TargetEncoding() {
			setLayout(new BorderLayout());
			setBorder(new EmptyBorder(5, 5, 5, 5));
			setToolTipText("指定欄位數值化(適用非數值欄位)");

			Box box = Box.createHorizontalBox();
			add(box, BorderLayout.CENTER);
			teL1 = new JLabel("　將");
			teL1.setFont(font18);
			box.add(teL1);
			box.add(Box.createHorizontalStrut(10));

			teTitle = new JComboBox<String>(title);
			teTitle.setFont(font18);
			box.add(teTitle);
			box.add(Box.createHorizontalStrut(10));

			teL2 = new JLabel("數值化");
			teL2.setFont(font18);
			box.add(teL2);
			box.add(Box.createHorizontalStrut(10));

			teButton = new JButton("╳");
			teButton.addActionListener(AbstractTools.this);
			add(teButton, BorderLayout.EAST);
		}
		
		private TargetEncoding(String title) {
			this();
			teTitle.setSelectedItem(title);
		}
		
		@Override
		public void execute() throws Exception {
			String title = getTitle();
			tempOut = outPath + "\\" + fileIndex++ + "_TE_" + title + ".csv";
			preprocess.TargetEncoding.exec(tempIn, title, ref, tempOut);
			tempIn = tempOut;
		}
		
		@Override
		public String getTitle() {
			return teTitle.getSelectedItem().toString();
		}
		
		@Override
		public String getContent() {
			return null;
		}
		
		@Override
		public boolean canMove() {
			return false;
		}
	}

	private class NormalizeFeature extends JPanel implements Tool {
		private static final long serialVersionUID = -6814924414408074809L;
		private JLabel nfL1;
		private JComboBox<String> nfTitle;
		private JLabel nfL2;
		private JButton nfButton;

		private NormalizeFeature() {
			setLayout(new BorderLayout());
			setBorder(new EmptyBorder(5, 5, 5, 5));
			setToolTipText("指定欄位標準化(適用數值欄位)");

			Box box = Box.createHorizontalBox();
			add(box, BorderLayout.CENTER);
			nfL1 = new JLabel("　將");
			nfL1.setFont(font18);
			box.add(nfL1);
			box.add(Box.createHorizontalStrut(10));

			nfTitle = new JComboBox<String>(title);
			nfTitle.setFont(font18);
			box.add(nfTitle);
			box.add(Box.createHorizontalStrut(10));

			nfL2 = new JLabel("正規化");
			nfL2.setFont(font18);
			box.add(nfL2);
			box.add(Box.createHorizontalStrut(10));

			nfButton = new JButton("╳");
			nfButton.addActionListener(AbstractTools.this);
			add(nfButton, BorderLayout.EAST);
		}
		
		private NormalizeFeature(String title) {
			this();
			nfTitle.setSelectedItem(title);
		}
		
		@Override
		public void execute() throws Exception {
			String title = getTitle();
			tempOut = outPath + "\\" + fileIndex++ + "_NF_" + title + ".csv";
			preprocess.NormalizeFeature.exec(tempIn, title, tempOut);
			tempIn = tempOut;
		}
		
		@Override
		public String getTitle() {
			return nfTitle.getSelectedItem().toString();
		}
		
		@Override
		public String getContent() {
			return null;
		}
		
		@Override
		public boolean canMove() {
			return false;
		}
	}

	private class StandardizeFeature extends JPanel implements Tool {
		private static final long serialVersionUID = 6033249602435649386L;
		private JLabel sfL1;
		private JComboBox<String> sfTitle;
		private JLabel sfL2;
		private JButton sfButton;

		private StandardizeFeature() {
			setLayout(new BorderLayout());
			setBorder(new EmptyBorder(5, 5, 5, 5));
			setToolTipText("指定欄位標準化(僅限數值欄位)");

			Box box = Box.createHorizontalBox();
			add(box, BorderLayout.CENTER);
			sfL1 = new JLabel("　將");
			sfL1.setFont(font18);
			box.add(sfL1);
			box.add(Box.createHorizontalStrut(10));

			sfTitle = new JComboBox<String>(title);
			sfTitle.setFont(font18);
			box.add(sfTitle);
			box.add(Box.createHorizontalStrut(10));

			sfL2 = new JLabel("標準化");
			sfL2.setFont(font18);
			box.add(sfL2);
			box.add(Box.createHorizontalStrut(10));

			sfButton = new JButton("╳");
			sfButton.addActionListener(AbstractTools.this);
			add(sfButton, BorderLayout.EAST);
		}
		
		private StandardizeFeature(String title) {
			this();
			sfTitle.setSelectedItem(title);
		}
		
		@Override
		public void execute() throws Exception {
			String title = getTitle();
			tempOut = outPath + "\\" + fileIndex++ + "_SF_" + title + ".csv"; // 輸出檔案的檔名
			preprocess.StandardizeFeature.exec(tempIn, title, tempOut); // 給予參數
			tempIn = tempOut;
		}
		
		@Override
		public String getTitle() {
			return sfTitle.getSelectedItem().toString();
		}
		
		@Override
		public String getContent() {
			return null;
		}
		
		@Override
		public boolean canMove() {
			return false;
		}
	}
	
	/**
	 * 取得輸出檔案
	 */
	public File getTempOutFile() {
		return new File(tempOut);
	}
	/**
	 * 使用者確定要這個輸出結果時，更新輸入檔
	 */
	public void adoptResult() {
		adopt = tempIn;
	}
	/**
	 * 捨棄產出的結果
	 */
	public void abandoneResult() {
		tempIn = adopt;
	}
	
	/**
	 * 定義資料處理頁面，各功能資訊
	 */
	public interface Tool {
		/**
		 * 該功能執行片段
		 * @throws Exception
		 */
		public void execute() throws Exception;
		/**
		 * 回傳使用者選擇的目標欄位
		 * @return 目標欄位
		 */
		public String getTitle();
		/**
		 * 回傳使用者選擇的參數
		 * @return 參數
		 */
		public String getContent();
		/**
		 * 是否可移動(拖動)
		 * @return boolean
		 */
		public boolean canMove();
	}
}