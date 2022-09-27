package gui;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import guiComponent.AbstractPreview;
import guiComponent.ShowProgress;
import guiFunction.CreateFile;
import guiFunction.Step;
import predict.Predictors;
import predict.PredictorsException;

class MethodSelectPanel extends JPanel implements ActionListener, ItemListener, Panel {
	private static final long serialVersionUID = -8033801386610992572L;
	private Logger logger = Logger.getLogger("MethodSelectPanel");
	private String wekaTip = "Weka是由紐西蘭懷卡托大學使用Java開發的資料探勘軟體，被廣為接受。";
	private String bpnnTip = "自主研發的倒傳遞神經網路。請注意！所有欄位內容均須為數值。";	
	private JFileChooser chooser = new JFileChooser(Info.getDesktop());
	private JComboBox<String> combo = new JComboBox<>(Predictors.getClassifierSimpleName());
	private JTextArea result = new JTextArea();
	private JTextField info = new JTextField(wekaTip);
	private JButton confirm = new JButton("確定");
	private TrainPreview trainPreview = new TrainPreview();
	private TestPreview testPreview = new TestPreview();
	private ShowProgress showProgress = new ShowProgress();
	private Predictors predictors;
	
	public MethodSelectPanel() {
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(new FileNameExtensionFilter("CSV File", "csv"));
		setLayout(new BorderLayout(10, 10));
		
		JLabel label = new JLabel("請選擇欲使用的預測系統：");
		combo.addItemListener(this);
		combo.setSelectedIndex(0);
		combo.setEnabled(false);
		Box chooseBox = Box.createHorizontalBox();
		chooseBox.add(label);
		chooseBox.add(combo);
		
		result.setEditable(false);
		result.setLineWrap(true);
		JScrollPane resultScroll = new JScrollPane(result);
		resultScroll.setViewportBorder(new TitledBorder(null, "訓練模型時的詳細資訊", TitledBorder.CENTER, TitledBorder.BELOW_TOP));
		
		//==========頁面左上半==========
		JPanel propertyPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(0, 0, 10, 0);
		propertyPanel.add(chooseBox, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.RELATIVE;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		JSplitPane classifierSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, Predictors.getClassifierPanel(), resultScroll);
		propertyPanel.add(classifierSplit, gbc);
		classifierSplit.setDividerLocation(classifierSplit.getPreferredSize().width / 2);
		
		//==========頁面右上半==========
		trainPreview.setTableTitle("訓練集");
		testPreview.setTableTitle("測試集");
		JSplitPane previewSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, trainPreview, testPreview);
		previewSplit.setBorder(LineBorder.createBlackLineBorder());
		previewSplit.setDividerLocation(previewSplit.getPreferredSize().height / 2);
		
		//==========主頁面==========
		JPanel mainPanel = new JPanel(new GridLayout(1, 2, 5, 5));
		mainPanel.add(propertyPanel);
		mainPanel.add(previewSplit);
		add(mainPanel, BorderLayout.CENTER);
		
		//==========頁面最下面==========
		info.setEditable(false);
		confirm.setEnabled(false);
		confirm.addActionListener(this);
		
		Box confirmBox = Box.createHorizontalBox();
		confirmBox.add(info);
		confirmBox.add(Box.createHorizontalStrut(5));
		confirmBox.add(confirm);
		add(confirmBox, BorderLayout.SOUTH);
	}
	
	/**點選confirmButton*/
	@Override
	public void actionPerformed(ActionEvent e) {
		Page.METHOD_SELECT.setEnabled(false);
		Page.TRAIN_RESULT.setEnabled(false);
		unLockOthers(false);
		
		String checkError;
		try {
			checkError = predictors.checkBPNNdata();//檢查資料集
		} catch (Exception ee) {
			Page.METHOD_SELECT.setEnabled(true);
			unLockOthers(true);
			logger.info(ee.getMessage());
			Info.showError("內部流程發生錯誤");
			JOptionPane.showMessageDialog(MethodSelectPanel.this, "內部流程發生錯誤，請再試一次");
			return;
		}
		
		if(checkError == null) {
			//資料即可被接受，進行訓練模型
			showProgress.update(new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() {
					if(trainModel()) {
						Page.TRAIN_RESULT.setEnabled(true);
					}
					Page.METHOD_SELECT.setEnabled(true);
					unLockOthers(true);
					return null;
				}
			});
		}else {//result != null
			Info.showError("資料集不符合要求");
			
			int choose = JOptionPane.showConfirmDialog(this,
			checkError + "\n您要自動轉換嗎？\n(請注意！轉換後訓練集及測試集將變更為轉換後結果。若要使用您原有的資料集，請重新匯入。)", 
			"資料集不符合要求", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
			if(choose == JOptionPane.YES_OPTION) {
				showProgress.update(new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() {
						try {
							showProgress.setText("正在轉換特徵");
							//訓練集和測試集變更為轉換後結果，要用原來的資料集，只能重新匯入
							//通常跳出PredictorsException例外時，為資料集不符合要求，且暫時步驟檔已經存在
							Step.executePredictorsStep(Info.getModelTrain(), Info.getModelTest(),
													   Info.getTempPath(), Info.getTempStep(),
													   Info.getTransTrain(), Info.getTransTest());
							//XXX 待測試。若原有步驟檔被覆蓋，是否影響到預測主頁面的預測結果
							CreateFile.copy(Info.getTempStep(), Info.getStep());
						} catch (Exception eee) {
							Page.METHOD_SELECT.setEnabled(true);
							unLockOthers(true);
							logger.info(eee.getMessage());
							Info.showError("轉換特徵時發生錯誤");
							JOptionPane.showMessageDialog(MethodSelectPanel.this, "轉換特徵時發生錯誤");
							return null;
						}
						
						//重新建立預測器的物件 | 重新讀檔
						try {
							predictors = new Predictors(Info.getModelTrain(), Info.getTransTrain(), Info.getTransTest(), Info.getStep());
						} catch (IOException eee) {
							Page.METHOD_SELECT.setEnabled(true);
							unLockOthers(true);
							logger.info(eee.getMessage());
							Info.showError("初始化分類預測器時發生錯誤");
							JOptionPane.showMessageDialog(MethodSelectPanel.this, "初始化分類預測器時發生錯誤");
							return null;
						}
						
						//訓練模型
						boolean success = trainModel();
						
						//更新訓練集和測試集
						try {
							showProgress.setText("正在更新訓練集和測試集");
							updateSheet(Info.getTransTrain(), Info.getTransTest());
							CreateFile.copy(Info.getTransTrain(), Info.getModelTrain());
							CreateFile.copy(Info.getTransTrain(), Info.getModelTest());
							Info.showMessage("轉換資料集成功");
						} catch (IOException eee) {
							Page.METHOD_SELECT.setEnabled(true);
							unLockOthers(true);
							logger.info(eee.getMessage());
							Info.showError("更新訓練集/測試集發生錯誤");
							JOptionPane.showMessageDialog(MethodSelectPanel.this, "更新訓練集/測試集時發生錯誤");
							success = false;//視為訓練模型不成功
						}
						
						if(success) {//成功訓練模型
							Page.TRAIN_RESULT.setEnabled(true);
						}
						Page.METHOD_SELECT.setEnabled(true);
						unLockOthers(true);
						return null;
					}
				});
				
			}else {//JOptionPane.NO_OPTION
				Info.showError("請使用其他資料集或演算法");
				JOptionPane.showMessageDialog(this, "請使用其他演算法或匯入其他資料集");
				Page.METHOD_SELECT.setEnabled(true);
				unLockOthers(true);
			}
		}
	}
	
	/**
	 * 訓練模型
	 * @return 訓練模型是否成功
	 */
	private boolean trainModel() {
		String results;
		try {
			showProgress.setText("正在建立模型");
			//得到訓練結果並輸出預測值及實際值
			results = predictors.trainClassifier(Info.getPandA());
			result.setText(results);
		} catch (Exception ee) {
			logger.info(ee.getMessage());
			Info.showError("訓練模型失敗");
			JOptionPane.showMessageDialog(MethodSelectPanel.this, ee.getMessage());
			return false;
		}
		
		try {
			CreateFile.toCSV(Info.getTrainResult(), results);
			predictors.saveClassifier(Info.getClassifier());
			Info.showMessage("訓練模型成功");
		} catch (Exception ee) {
			logger.info(ee.getMessage());
			Info.showError("儲存訓練模型結果失敗");
			JOptionPane.showMessageDialog(MethodSelectPanel.this, ee.getMessage());
			return false;
		}
		return true;
	}
	
	/**點選演算法列表*/
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (ItemEvent.SELECTED == e.getStateChange()) {
			int index = combo.getSelectedIndex();
			if(index == 3) {	// 若選擇BPNN
				info.setText(bpnnTip);
			}else {
				info.setText(wekaTip);
			}
			try {
				Predictors.changeClassifier(index);
			} catch (Exception ee) {
				logger.info(ee.getMessage());
				Info.showError("請重新啟動系統");
				JOptionPane.showMessageDialog(this, "更改分類器時發生問題，請再試一次。");
			}
		}
	}
	
	/**解鎖匯入按鈕(true -> 解鎖)*/
	private void unLockImport(boolean b) {
		trainPreview.setImportEnabled(b);
		testPreview.setImportEnabled(b);
	}
	
	/**解鎖其他按鈕(true -> 解鎖)*/
	private void unLockOthers(boolean b) {
		confirm.setEnabled(b);
		combo.setEnabled(b);
	}
	
	@Override
	public void setFile() {
		if(Files.notExists(Paths.get(Info.getModelTrain())) ||
		   Files.notExists(Paths.get(Info.getModelTest()))) {
			unLockImport(true);
			return;
		}
		unLockImport(false);
		Page.METHOD_SELECT.setEnabled(false);
		updateSheet(Info.getModelTrain(), Info.getModelTest());
		
		//若步驟檔不存在，建立
		Path step = Paths.get(Info.getStep());
		if(Files.notExists(step)) {
			try {
				Files.createFile(step);
			} catch (IOException e) {
				logger.info(e.getMessage());
				Info.showError("內部流程出錯");
				JOptionPane.showMessageDialog(this, "內部流程出錯，請再試一次");
			}
		}
		
		try {// 建立預測分類器
			predictors = new Predictors(Info.getOriginalTrain(),
										Info.getModelTrain(), Info.getModelTest(),
									 	Info.getTransTrain(), Info.getTransTest(),
									 	Info.getStep(), Info.getTempStep());
			unLockOthers(true);
		} catch (PredictorsException e) {//資料集不符合要求
			Info.showError("資料集不符合要求");
			int choose = JOptionPane.showConfirmDialog(this, e.getMessage(), "資料集不符合要求", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
			if(choose == JOptionPane.YES_OPTION) {
				try {
					//通常跳出PredictorsException例外時，代表資料集不符合要求，且暫時步驟檔已經存在
					Step.executeRemoveFeature(Info.getModelTrain(), Info.getTempStep(), Info.getTransTrain());
					Step.executeRemoveFeature(Info.getModelTest(), Info.getTempStep(), Info.getTransTest());
					CreateFile.copy(Info.getTransTrain(), Info.getModelTrain());
					CreateFile.copy(Info.getTransTest(), Info.getModelTest());
					Files.delete(Paths.get(Info.getTempStep()));
					setFile();//再執行讀檔
					
				} catch (Exception ee) {
					logger.info(ee.getMessage());
					Info.showError("移除無用特徵時發生錯誤");
					JOptionPane.showMessageDialog(this, "移除無用特徵時發生錯誤");
				}
				
			}else {
				Info.showError("無法使用本頁面功能");
				JOptionPane.showMessageDialog(this, "無法使用本頁面功能，將清除本頁資料");
				reset();
			}
			
		} catch (Exception e) {
			logger.info(e.getMessage());
			Info.showError("初始化分類預測器時發生錯誤");
			JOptionPane.showMessageDialog(this, "初始化分類預測器時發生錯誤");
			return;
		}
		Page.METHOD_SELECT.setEnabled(true);
	}
	
	/**
	 * 更新預覽表格
	 * @param train 訓練集資料來源
	 * @param test 測試集資料來源
	 */
	public void updateSheet(String train, String test) {
		try {
			trainPreview.update(train);
			testPreview.update(test);
		} catch (Exception e) {
			logger.info(e.getMessage());
			Info.showError("無法更新預覽表格");
			JOptionPane.showMessageDialog(this, "更新預覽表格時發生錯誤");
		}
	}
	
	@Override
	public void reset() {
		predictors = null;
		result.setText("");
		trainPreview.clear();
		testPreview.clear();
		unLockImport(true);
		unLockOthers(false);
	}
	
	/**顯示訓練集*/
	private class TrainPreview extends AbstractPreview {
		private static final long serialVersionUID = -3161485467044773055L;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			int flag = chooser.showOpenDialog(this);
			if (flag == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				try {
					CreateFile.copy(file, Info.getModelTrain());
					CreateFile.copy(file, Info.getOriginalTrain());
					update(file);
					setFile();
					Info.showMessage("訓練集匯入成功");
				} catch (Exception ee) {
					logger.info(ee.getMessage());
					Info.showError("訓練集匯入失敗");
				}
			}
		}
	}
	
	/**顯示測試集*/
	private class TestPreview extends AbstractPreview {
		private static final long serialVersionUID = -8733890928778564081L;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			int flag = chooser.showOpenDialog(this);
			if (flag == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				try {
					CreateFile.copy(file, Info.getModelTest());
					CreateFile.copy(file, Info.getOriginalTest());
					update(file);
					setFile();
					Info.showMessage("測試集匯入成功");
				} catch (Exception ee) {
					logger.info(ee.getMessage());
					Info.showError("測試集匯入失敗");
				}
			}
		}
	}
}
