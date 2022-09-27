package gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import guiComponent.AbstractPreview;
import guiComponent.JAutoCompleteComboBox;
import guiComponent.PanelTable;
import guiComponent.PanelTableModel;
import guiComponent.ShowProgress;
import guiFunction.CreateFile;
import guiFunction.Detect;
import guiFunction.Enum;
import guiFunction.LoadFile;
import guiFunction.Step;
import guiFunction.TransIfNeeded;
import guiFunction.ZipOrUnzip;
import predict.Predictors;

class MainPagePanel extends JPanel implements ActionListener, Panel {
	private static final long serialVersionUID = 1180611674900194495L;
	private Logger logger = Logger.getLogger("MainPagePanel");
	private String[] imageName = {"\\single.png", "\\multiple.png", "\\folder.png"};
	private ImageIcon[] icon = LoadFile.fromIcons(Info.getIconPath(), imageName, 30, 30);
	private JFileChooser chooser = new JFileChooser(Info.getDesktop());
	private JTextArea info = new JTextArea();
	private JButton startPredict = new JButton("開始預測"),
			loadModel = new JButton("載入模型");
	private JTabbedPane optionPane = new JTabbedPane();	//特徵選項切換頁籤
	private SingleModel singleModel = new SingleModel();
	private ShowProgress showProgress = new ShowProgress();
	private SingleResult singleResult = new SingleResult();
	private MultiplePreview multiplePreview = new MultiplePreview();
	private String[] title;
	private TransIfNeeded trans;
	private Predictors predictors;
	private String predictType;		//手術時間或麻醉時間
	
	public MainPagePanel() {// 開始建立模型按鈕的監聽器
		// XXX chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(new FileNameExtensionFilter("Model File", "model"));
		setLayout(new BorderLayout(10, 10));
		
		JScrollPane singleScroll = new JScrollPane(new PanelTable(singleModel));
		singleScroll.setViewportBorder(new TitledBorder(null, "選擇欲預測的選項", TitledBorder.CENTER, TitledBorder.BELOW_TOP));
		optionPane.addTab("單筆資料預測", icon[0], singleScroll, "選擇各特徵的選項以預測單筆手術資料");
		optionPane.addTab("多筆資料預測", icon[1], multiplePreview, "匯入檔案以預測多筆手術資料");
		
		info.setLineWrap(true);
		info.setEditable(false);
		JScrollPane infoScroll = new JScrollPane(info);
		infoScroll.setViewportBorder(new TitledBorder(null, "使用模型的詳細資訊", TitledBorder.CENTER, TitledBorder.BELOW_TOP));
		JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
		centerPanel.add(optionPane);
		centerPanel.add(infoScroll);
		add(centerPanel, BorderLayout.CENTER);
		
		startPredict.setEnabled(false);
		startPredict.addActionListener(this);
		loadModel.addActionListener(this);
		
		Box buttonBox = Box.createHorizontalBox();
		buttonBox.add(startPredict);
		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(loadModel);
		add(buttonBox, BorderLayout.SOUTH);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == startPredict) { // 開始預測			
			switch(optionPane.getSelectedIndex()) {
			case 0://單筆資料預測
				try {
					CreateFile.buildOptions(title, singleModel.getOptions(), Info.getPredictOptions());
					Step.executeBitches(Info.getOriginalTrain(), Info.getPredictOptions(), Info.getPredictPath(),
								Info.getStep(), Info.getPredictTrain(), Info.getPredictTest());
					singleResult.update(singleModel.getOptionsText(), predictType,
							predictors.getValue(Info.getPredictTest(), trans));	// 檢查有無需要反正規化或反標準化
					JOptionPane.showMessageDialog(this, singleResult, "預測結果", JOptionPane.INFORMATION_MESSAGE);
					
				} catch (Exception ee) {
					logger.info(ee.getMessage());
					Info.showError("無法預測");
					JOptionPane.showMessageDialog(this, "發生問題，無法預測！");
				}
				break;
				
			case 1://多筆資料預測
				try {
					Step.executeBitches(Info.getOriginalTrain(), Info.getMultiplePredict(), Info.getPredictPath(),
								Info.getStep(), Info.getPredictTrain(), Info.getPredictTest());
					predictors.buildPredictFile(Info.getPredictTest(), Info.getMultiplePredict(), trans);
					multiplePreview.update(Info.getMultiplePredict());
					JOptionPane.showMessageDialog(this, "已成功轉換多筆資料預測檔");
					
				} catch (Exception ee) {
					logger.info(ee.getMessage());
					Info.showError("無法預測");
					JOptionPane.showMessageDialog(this, "發生問題，無法預測！");
					return;
				}
				break;				
			}

		} else if (e.getSource() == loadModel) { // 載入模型
			int choose = chooser.showOpenDialog(this);
			if (choose == JFileChooser.APPROVE_OPTION) {
				//解壓縮
				Path source = chooser.getSelectedFile().toPath();
				Info.setRawPath(source);
				Path target = Paths.get(Info.getModelPath());
				try {
					ZipOrUnzip.unzip(source, target);
					Info.showMessage("載入模型檔成功");
				} catch (IOException ee) {
					logger.info(ee.getMessage());
					Info.showError("載入模型檔失敗");
				}
				
				showProgress.update(new SwingWorker<Void, Void>(){
					@Override
					protected Void doInBackground() {
						showProgress.setText("正在建立選擇列表");
						try {
							buildOptions(source.toString());
							Info.showMessage("建立選擇列表成功");
						} catch (Exception e) {
							logger.info(e.getMessage());
							Info.showError("建立選擇列表失敗");
						}
						return null;
					}
				});
			}
		}
	}

	@Override
	public void setFile() {
		if(Info.getStep() == null || Files.notExists(Paths.get(Info.getStep()))) {
			System.err.println("尚未設定");
			reset();
			return;
		}
		
		showProgress.update(new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() {
				// 不讓使用者在程式執行完成前按下按鈕
				Page.MAIN_PAGE.setEnabled(false);
				showProgress.setText("正在清除先前工作階段");
				Info.clearProcessAndTemp();
				showProgress.setText("正在建立選擇清單");
				try {
					buildOptions("根據流程建立");
				} catch (Exception e) {
					startPredict.setEnabled(false);
					logger.info(e.getMessage());
					Info.showError("無法建立選擇列表");
					JOptionPane.showMessageDialog(MainPagePanel.this, "無法建立選擇列表");
				}
				Page.MAIN_PAGE.setEnabled(true);
				return null;
			}
		});
	}
	
	@Override
	public void reset() {
		predictors = null;
		info.setText("");
		singleModel.clear();
		multiplePreview.clear();
	}
	
	/**
	 * 單筆資料預測 | 建立頁面的選擇選項
	 * @param modelPath 載入模型路徑
	 * @throws Exception
	 */
	private void buildOptions(String modelPath) throws Exception {
		info.setText("模型路徑：" + modelPath + "\n" + LoadFile.fromText(Info.getTrainResult()));
		
		trans = TransIfNeeded.transValue(Info.getStep(), Info.getOriginalTrain());
		predictors = new Predictors(Info.getClassifier());
		String[] rawTitle = Enum.title(Info.getOriginalTrain());
		int last = rawTitle.length - 1;
		predictType = rawTitle[last].startsWith("手術時間") ? "手術時間" : "麻醉時間";		// 手術時間 | 麻醉時間
		title = Arrays.copyOf(rawTitle, last);
		singleModel.showFeature(title, Detect.digits(Info.getOriginalTrain()), Enum.content(Info.getOriginalTrain()));
		startPredict.setEnabled(true);
	}
	
	/**單筆預測 | 選項版面*/
	private class OptionPanel extends JPanel {
		private static final long serialVersionUID = -4294554042900750386L;
		private JLabel label;
		private JTextField text;
		private JAutoCompleteComboBox combo;
		
		/**建立數值選項的版面*/
		public OptionPanel(String title) {
			setLabel(title);
			text = new JTextField();
			text.setPreferredSize(new Dimension(150, 10));
			add(text);
		}
		
		/**建立可供選擇選項的版面*/
		public OptionPanel(String title, String[] items) {
			setLabel(title);
			combo = new JAutoCompleteComboBox(items);
			combo.setPreferredSize(new Dimension(150, 10));
			add(combo);
		}
		
		private void setLabel(String title) {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setBorder(new EmptyBorder(5, 5, 5, 5));
			label = new JLabel(title, JLabel.RIGHT);
			label.setPreferredSize(new Dimension(120, 10));
			add(label);
			add(Box.createHorizontalStrut(10));
		}
		
		public String getDigit() {
			return label.getText();
		}
		
		public String getOption() {
			if(text != null) {
				return text.getText();
			}else {
				return combo.getSelectedItem().toString();
			}
		}
	}
	
	/**單筆預測中 | 顯示供使用者選擇預測選項的表格Model*/
	private class SingleModel extends PanelTableModel<OptionPanel> {
		private static final long serialVersionUID = -4299382561130529685L;
		/**
		 * 回傳使用者選擇的選項(與標題合併)<br>
		 * 供顯示於預測數值結果版面用
		 * @return 使用者選擇的選項
		 */
		private String getOptionsText() {
			StringBuilder str = new StringBuilder();
			forEach((OptionPanel o) -> {
				String digit = o.getDigit();
				str.append(digit.isEmpty() ? "0.0" : digit).append("：").append(o.getOption()).append("\n");
			});
			return str.toString();
		}
		
		/**
		 * 回傳使用者選擇的選項(不包含標題)<br>
		 * 供預測模型前，輸出成檔案
		 * @return 使用者選擇的選項
		 */
		private String[] getOptions() {
			int size = getRowCount();
			String[] value = new String[size];
			String option;
			for(int i = 0; i < size; i++) {
				option = get(i).getOption();
				option = option.isEmpty() ? "0.0" : option;
				value[i] = option;
			}
			return value;
		}
		
		/**
		 * 傳入內容應不包含手術時間或麻醉時間
		 * @param title
		 * @param isDigit
		 * @param content
		 */
		private void showFeature(String[] title, boolean[] isDigit, String[][] content) {
			clear();
			for (int i = 0, length = title.length; i < length; i++) {
				if (isDigit[i]) {	// 數值：JTextField
					add(new OptionPanel(title[i]));
				} else { 			// 文字：JComboBox
					add(new OptionPanel(title[i], content[i]));
				}
			}
		}
	}
	
	/**單筆預測 | 提供預測資訊給使用者*/
	private class SingleResult extends JPanel {
		private static final long serialVersionUID = 6779114897192596182L;
		private JTextArea featureInfo, predictInfo;
		
		public SingleResult() {
			setLayout(new BorderLayout(10, 10));
			setPreferredSize(new Dimension(500, 500));
			
			featureInfo = new JTextArea();
			featureInfo.setEditable(false);
			JScrollPane featureScroll = new JScrollPane(featureInfo);
			featureScroll.setViewportBorder(new TitledBorder(null, "您選擇的預測選項", TitledBorder.CENTER, TitledBorder.BELOW_TOP));
			add(featureScroll, BorderLayout.CENTER);
			
			predictInfo = new JTextArea();
			predictInfo.setBorder(new TitledBorder(null, "預測結果：", TitledBorder.LEFT, TitledBorder.BELOW_TOP));
			predictInfo.setEditable(false);
			add(predictInfo, BorderLayout.SOUTH);
		}
		
		/**
		 * 更新預測結果
		 * @param optionsText 使用者選擇的特徵資訊
		 * @param predictType 手術時間 | 麻醉時間
		 * @param predictValue 預測時間
		 */
		private void update(String optionsText, String predictType, double predictValue) {
			featureInfo.setText(optionsText);
			predictInfo.setText(predictType + "的預測值為「" + predictValue + "」分鐘");
		}
	}
	
	/**多筆資料預測 匯入資料預覽*/
	private class MultiplePreview extends AbstractPreview {
		private static final long serialVersionUID = 1705315329772510531L;
		private JFileChooser chooser = new JFileChooser(Info.getDesktop());
		private MultiplePreview() {
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setFileFilter(new FileNameExtensionFilter("CSV File", "csv"));
			setTableTitle("多筆資料預測內容");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			int choose = chooser.showOpenDialog(this);
			if(choose == JFileChooser.APPROVE_OPTION) {
				try {
					File file = chooser.getSelectedFile();
					CreateFile.copy(file, Info.getMultiplePredict());
					update(file);
				} catch (Exception ee) {
					logger.info(ee.getMessage());
					JOptionPane.showMessageDialog(this, "匯入資料時發生錯誤！");
				}
			}
		}
	}
}
