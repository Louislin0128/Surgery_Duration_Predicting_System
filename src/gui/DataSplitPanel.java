package gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import guiComponent.AbstractPreview;
import guiFunction.CreateFile;
import guiFunction.LSH;
import guiFunction.SplitData;
import guiFunction.Step;

class DataSplitPanel extends JPanel implements Panel {
	private static final long serialVersionUID = -835851681402783283L;
	private Logger logger = Logger.getLogger("DataSplitPanel");
	private JFileChooser chooser = new JFileChooser(Info.getDesktop());
	private OptionsPanel optionsPanel = new OptionsPanel();
	private AbstractPreview trainPreview = AbstractPreview.newSheetWithoutImport(),
			testPreview = AbstractPreview.newSheetWithoutImport(),
			popTable = AbstractPreview.newNothingSheet();// 彈窗看資料集
	private PopUp popUp = new PopUp(popTable);
	private Border normalBorder = UIManager.getBorder("TextField.border");
	private Border errorBorder = new LineBorder(Color.RED, 3);

	public DataSplitPanel() {
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(new FileNameExtensionFilter("CSV File", "csv"));
		popTable.setTableTitle("檢視資料集");
		
		setLayout(new BorderLayout(10, 10));
		add(optionsPanel, BorderLayout.NORTH);
		
		trainPreview.setTableTitle("拆分結果(訓練集)");
		testPreview.setTableTitle("拆分結果(測試集)");
		JSplitPane previewPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, trainPreview, testPreview);
		previewPanel.setBorder(null);
		previewPanel.setDividerLocation(previewPanel.getPreferredSize().width / 2);
		add(previewPanel, BorderLayout.CENTER);
	}
	
	@Override
	public void setFile() {
		if(Files.notExists(Paths.get(Info.getFeatureSelect()))) {
			System.err.println("沒有特徵選取的檔案");
			optionsPanel.importData.setEnabled(true);
			return;
		}
		optionsPanel.importData.setEnabled(false);
		
		try {
			popTable.update(Info.getFeatureSelect());
			optionsPanel.dataCount.setText(popTable.getRowsCount() + "");
			optionsPanel.setButtonEnabled(true);
			Info.showMessage("資料匯入成功");
		} catch (Exception e) {
			optionsPanel.dataCount.setText("尚無資料");
			optionsPanel.setButtonEnabled(false);
			optionsPanel.clearTable();
			logger.info(e.getMessage());
			Info.showError("資料匯入失敗");
		}
	}
	
	@Override
	public void reset() {
		trainPreview.clear();
		testPreview.clear();
		popTable.clear();
		optionsPanel.importData.setEnabled(true);
		optionsPanel.randomCheck.setSelected(false);
		optionsPanel.splitText[0].setText("");
		optionsPanel.splitText[1].setText("");
		optionsPanel.dataCount.setText("尚無資料");
	}
	
	private class OptionsPanel extends JPanel implements ActionListener, ItemListener {
		private static final long serialVersionUID = -1626768545221257752L;
		private JCheckBox randomCheck = new JCheckBox("打亂資料集");
		private JRadioButton percentRadio = new JRadioButton("百分比", true),
				numberRadio = new JRadioButton("筆數", false);
		private JTextField[] splitText = new JTextField[2];
		private JTextField randomText = new JTextField();
		private JLabel dataCount = new JLabel("尚無資料");
		private JLabel[] splitLabel = new JLabel[5];
		private JButton importData = new JButton("匯入資料集"),
				lookup = new JButton("檢視資料集"),
				confirm = new JButton("確定產生");
		
		public OptionsPanel() {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			
			importData.addActionListener(this);
			lookup.setEnabled(false);
			lookup.addActionListener(this);
			
			Box buttonUpper = Box.createHorizontalBox();
			buttonUpper.add(importData);
			buttonUpper.add(Box.createHorizontalStrut(5));
			buttonUpper.add(lookup);
			
			Box buttonLower = Box.createHorizontalBox();
			buttonLower.add(new JLabel("資料總筆數："));
			buttonLower.add(dataCount);
			buttonLower.add(Box.createHorizontalGlue());
			
			Box buttonBox = Box.createVerticalBox();
			buttonBox.add(buttonUpper);
			buttonBox.add(buttonLower);
			add(buttonBox);
			add(Box.createHorizontalStrut(30));
			//
			randomCheck.addItemListener(this);
			randomText.setEnabled(false);

			Box randomUpper = Box.createHorizontalBox();
			randomUpper.add(randomCheck);
			randomUpper.add(Box.createHorizontalGlue());

			Box randomLower = Box.createHorizontalBox();
			randomLower.add(new JLabel("種子碼：", JLabel.CENTER));
			randomLower.add(randomText);
			Box randomBox = Box.createVerticalBox();
			randomBox.add(randomUpper);
			randomBox.add(randomLower);
			add(randomBox);
			add(Box.createHorizontalStrut(30));
			//
			splitLabel[0] = new JLabel("使用單位：", JLabel.CENTER);
			splitLabel[1] = new JLabel("設定測試集為：", JLabel.CENTER);
			splitLabel[2] = new JLabel("%", JLabel.CENTER);
			splitLabel[3] = new JLabel("~", JLabel.CENTER);
			splitLabel[4] = new JLabel("%的資料", JLabel.CENTER);
			
			percentRadio.addItemListener(this);

			ButtonGroup radioGroup = new ButtonGroup();
			radioGroup.add(percentRadio);
			radioGroup.add(numberRadio);

			Box splitUpper = Box.createHorizontalBox();
			splitUpper.add(splitLabel[0]); // JLabel 使用單位:
			splitUpper.add(percentRadio);
			splitUpper.add(numberRadio);
			//
			splitText[0] = new JTextField(); // 於資料拆分選項輸入數字
			splitText[0].setColumns(15);
			splitText[1] = new JTextField(); // 於資料拆分選項輸入數字
			splitText[1].setColumns(15);

			Box splitLower = Box.createHorizontalBox();
			splitLower.add(splitLabel[1]); 	// JLabel 設定測試集為：
			splitLower.add(splitText[0]); 	// 輸入數字 左
			splitLower.add(splitLabel[2]); 	// JLabel % | 筆
			splitLower.add(splitLabel[3]);	// JLabel ~
			splitLower.add(splitText[1]); 	// 輸入數字 右
			splitLower.add(splitLabel[4]); 	// JLabel %的資料 | 筆的資料
			
			JPanel splitPanel = new JPanel(new GridLayout(2, 1));
			splitPanel.add(splitUpper);
			splitPanel.add(splitLower);
			add(splitPanel);
			add(Box.createHorizontalStrut(30));
			//
			confirm.setEnabled(false);
			confirm.addActionListener(this);
			add(confirm);
		}
		
		@Override
		public void itemStateChanged(ItemEvent e) {
			if(e.getSource() == randomCheck) {	//打亂資料集，輸入種子碼
				if(ItemEvent.SELECTED == e.getStateChange()) {
					randomText.setEnabled(true);
				}else {
					randomText.setEnabled(false);
					randomText.setText("");
				}
			}else if(e.getSource() == percentRadio) {	// percentRadio	| 針對百分比RadioButton
				if (ItemEvent.SELECTED == e.getStateChange()) {
					splitLabel[2].setText("%");
					splitLabel[4].setText("%的資料");
				} else { 	// 筆數
					splitLabel[2].setText("筆");
					splitLabel[4].setText("筆的資料");
				}
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (importData == e.getSource()) {//匯入資料集按鈕
				int choose = chooser.showOpenDialog(this);
				if (choose == JFileChooser.APPROVE_OPTION) {
					try {
						CreateFile.copy(chooser.getSelectedFile(), Info.getFeatureSelect());
						popTable.update(Info.getFeatureSelect());
						setFile();
						
						choose = JOptionPane.showConfirmDialog(this, "請問您要查看匯入的資料集嗎?", "提示視窗", JOptionPane.YES_NO_OPTION);
						if (choose == JOptionPane.YES_OPTION) {
							popUp.setVisible(true);
						}
						confirm.setEnabled(true); 		// 確定產生按鈕
						randomCheck.setSelected(false);	// 取消勾選打亂checkBox
						randomText.setText("");
						splitText[0].setText("");
						splitText[1].setText("");
						
					} catch (IOException ee) {
						logger.info(ee.getMessage());
						Info.showError("資料匯入失敗"); 
					}
					trainPreview.clear();
					testPreview.clear();
				}

			} else if (lookup == e.getSource()) {//查看資料集按鈕
				popUp.setVisible(true);
				
			} else if(confirm == e.getSource()) {//確定按鈕
				confirm();
			}
		}
		
		/**確認使用者輸入的數值是否合理*/
		private void confirm() {
			String randSeed = randomText.getText();
			if (!randSeed.isEmpty()) {
				try { // 檢查輸入數字是否合法
					Long.parseLong(randSeed);
				} catch (NumberFormatException error) {
					remindError(randomText, "請輸入正整數。");
					return;
				}
			}

			String num1 = splitText[0].getText();
			String num2 = splitText[1].getText();
			int num1Int = 0;
			int num2Int = 0;
			try { // 檢查輸入數字是否合法
				num1Int = Integer.parseInt(num1);
			} catch (NumberFormatException error) {
				remindError(splitText[0], "請輸入正整數。");
				return;
			}
			try { // 檢查輸入數字是否合法
				num2Int = Integer.parseInt(num2);
			} catch (NumberFormatException error) {
				remindError(splitText[1], "請輸入正整數。");
				return;
			}
			
			if (num1Int < 0) {
				remindError(splitText[0], "請輸入大於等於0的值。");
				return;
			}else if (num2Int <= 0) {
				remindError(splitText[1], "請輸入大於0的值。");
				return;
			}else if (num1Int > num2Int) {
				remindError(splitText[0], "左邊輸入格須小於等於右邊輸入格的數值。");
				return;
			}
			if (percentRadio.isSelected()) {
				if (num1Int > 100) {
					remindError(splitText[0], "請輸入0~100的值。");
					return;
				}else if (num2Int > 100) {
					remindError(splitText[1], "請輸入0~100的值。");
					return;
				}
			}
			
			setButtonEnabled(false);
			try {
				execute(num1Int, num2Int);
			}catch (Exception ee) {
				logger.info(ee.getMessage());
				Info.showError("資料拆分失敗");
				Page.METHOD_SELECT.setEnabled(false);
				clearTable();
			}
			setButtonEnabled(true);
		}
		
		/**執行流水號檔案 | 步驟檔程序*/
		private void execute(int num1, int num2) throws Exception {
			boolean stepExist = Files.exists(Paths.get(Info.getStep()));//是否有步驟檔
			if (stepExist) { // 有步驟，製作原型資料集
				System.out.println("有步驟檔 移除流水號及特徵選取移除的欄位");
				// 製作對照流水號檔案時，一併移除流水號及特徵選取移除的欄位
				LSH.build(Info.getFeatureSelect(), Info.getDataTransform(), Info.getLSH());
			} else {
				// 沒有步驟，是程咬金 | 不論程咬金是從特徵選取或是資料拆分進來，都應該執行這裡
				System.out.println("無步驟檔 移除流水號");
				LSH.remove(Info.getFeatureSelect(), Info.getLSH());// 移除流水號
			}
			
			String splitInFile;
			if (randomCheck.isSelected()) { // 打亂資料集
				System.out.println("打亂資料集");
				CreateFile.disOrganize(Info.getLSH(), randomText.getText(), Info.getDisOrganize());
				splitInFile = Info.getDisOrganize();
			}else {
				System.out.println("不打亂資料集");
				splitInFile = Info.getLSH();
			}
			if(splitLabel[2].getText().equals("%")) {	// 使用者選擇百分比
				System.out.println("以百分比切割");
				SplitData.byPercent(splitInFile, num1, num2, popTable.getRowsCount(), Info.getOriginalTrain(), Info.getOriginalTest());
			}else {
				System.out.println("以筆數切割");
				SplitData.byQuantity(splitInFile, num1, num2, Info.getOriginalTrain(), Info.getOriginalTest());
			}
			
			if(stepExist) {// 執行步驟檔
				System.out.println("執行步驟檔");
				Step.executeBitches(Info.getOriginalTrain(), Info.getOriginalTest(), Info.getDoStepPath(),
									Info.getStep(), Info.getModelTrain(), Info.getModelTest());
			}else {//原始訓練測試檔視為模型訓練測試檔
				System.out.println("原始訓練測試檔視為模型訓練測試檔");
				CreateFile.copy(Info.getOriginalTrain(), Info.getModelTrain());
				CreateFile.copy(Info.getOriginalTest(), Info.getModelTest());
			}
			
			trainPreview.update(Info.getModelTrain());
			testPreview.update(Info.getModelTest());
			Page.METHOD_SELECT.setEnabled(true);
			Info.showMessage("資料拆分成功");
		}
		
		/**設定按鈕是否啟用*/
		private void setButtonEnabled(boolean b) {
			lookup.setEnabled(b);
			confirm.setEnabled(b);
		}
		
		/**清除訓練及測試表格*/
		private void clearTable() {
			trainPreview.clear();
			testPreview.clear();
		}
		
		/**
		 * 提示錯誤訊息
		 * @param comp 元件
		 * @param errorMessage 錯誤訊息
		 */
		private void remindError(JComponent comp, String errorMessage) {
			comp.setBorder(errorBorder);
			JOptionPane.showMessageDialog(DataSplitPanel.this, errorMessage, "提示訊息", JOptionPane.WARNING_MESSAGE);
			comp.setBorder(normalBorder);
		}
	}
	
	private class PopUp extends JFrame {
		private static final long serialVersionUID = -4929202953380274007L;
		public PopUp(AbstractPreview sheet) {
			setTitle("資料預覽");
			setSize(sheet.getPreferredSize().width, sheet.getPreferredSize().height);
			setContentPane(sheet);
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setLocationRelativeTo(DataSplitPanel.this);
		}
	}
}
