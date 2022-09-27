package gui;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import guiComponent.AbstractPreview;
import guiComponent.AbstractTools;
import guiComponent.AbstractTools.Tool;
import guiComponent.PanelTable;
import guiComponent.PanelTableModel;
import guiComponent.ShowProgress;
import guiFunction.CreateFile;
import guiFunction.Enum;
import guiFunction.LSH;
import guiFunction.LoadFile;
import guiFunction.Step;
import guiFunction.TitleIndex;

class DataHandlePanel extends JPanel implements Panel {
	private static final long serialVersionUID = 7437323078040768459L;
	private Logger logger = Logger.getLogger("DataHandlePanel");
	// GUI元件
	private BeforeSheet beforeSheet = new BeforeSheet();// 原始資料(資料轉檔資料)
	private AbstractPreview afterSheet = AbstractPreview.newSheetWithoutImport(); 	// 預覽資料(彈窗後要顯示的資料集)
	private AbstractPreview confirmSheet = AbstractPreview.newNothingSheet();	// 與使用者確認步驟執行結果
	private ShowProgress showProgress = new ShowProgress();
	private JFileChooser chooser = new JFileChooser(Info.getDesktop());
	private TableModel model = new TableModel(); 			// 表的模型
	private PanelTable stepTable = new PanelTable(model); 	// 步驟欄
	private HandlePanel handlePanel = new HandlePanel();
	private ToolPanel toolPanel = new ToolPanel();
	// Class
	private Step step = new Step(Info.getDesktop());// 儲存、載入步驟
	private Tools tools; 			// 工具列，包含Panel與指令(呼叫->同名的方法)
	// 變數
	private int removeIndex; 		// 刪除某步驟時用的
	private boolean loadStepFlag = false;	// 紀錄步驟欄目前顯示的是否是載入的
	private boolean bitches = false;		// 紀錄使用者是否點選三寶
	private Vector<Tool> allStep = new Vector<>();
	
	public DataHandlePanel() {
		stepTable.addMouseListener(clickListener);
		stepTable.addMouseMotionListener(clickListener);
		
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(new FileNameExtensionFilter("CSV File", "csv"));
		setLayout(new BorderLayout());
		
		add(toolPanel, BorderLayout.WEST); 		// 製作工具欄
		JPanel centerPanel = new JPanel(new GridLayout(1, 2));
		centerPanel.add(handlePanel);	// 製作步驟欄
		centerPanel.add(new PreViewPanel());// 製作預覽欄
		add(centerPanel, BorderLayout.CENTER);
		//
		confirmSheet.setTableTitle("步驟執行後的結果");
	}
	
	@Override
	public void setFile() {	// 設定頁面，將所需資料準備好
		if (Files.notExists(Paths.get(Info.getDataTransform())) ||
			Files.exists(Paths.get(Info.getDataHandle()))) {
			beforeSheet.setImportEnabled(true);
			return;
		}
		beforeSheet.setImportEnabled(false);
		
		showProgress.update(new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				Page.DATA_HANDLE.setEnabled(false);	// 不讓使用者在程式執行完成前按下按鈕
				showProgress.setText("正在載入資料");
				unLockOthers(false);
				unLockFuncs(false);
				model.removeAll();
				allStep.removeAllElements();
				try {
					File file = new File(Info.getDataTransform());
					beforeSheet.update(file);
					tools = new Tools(Info.getDataTransform(), Info.getDataHandlePath(), Enum.title(file), Enum.content(file));
					unLockFuncs(true);
					Info.showMessage("資料載入成功");
				} catch (Exception e) {
					logger.info(e.getMessage());
					Info.showError("資料載入失敗");
				}
				
				unLockOthers(true);
				Page.DATA_HANDLE.setEnabled(true);
				return null;
			}
		});
	}
	
	@Override
	public void reset() {
		beforeSheet.clear();	// 原始資料(資料轉檔資料)
		afterSheet.clear(); 	// 預覽資料(彈窗後要顯示的資料集)
		confirmSheet.clear();	// 與使用者確認步驟執行結果
		model.removeAll(); 		// 表的模型
		allStep.removeAllElements();
		loadStepFlag = false;	// 紀錄步驟欄目前顯示的是否是載入的
		bitches = false;		// 紀錄使用者是否點選三寶
		unLockFuncs(false);		// 將按鈕都鎖住
		unLockOthers(false);
		beforeSheet.setImportEnabled(true);
	}

	// 鎖定工具欄按鈕(true -> 解鎖)
	private void unLockFuncs(boolean b) {
		toolPanel.rosdButton.setEnabled(b);
		toolPanel.roButton.setEnabled(b);
		toolPanel.rrButton.setEnabled(b);
		toolPanel.rfButton.setEnabled(b);
		toolPanel.erButton.setEnabled(b);
		toolPanel.teButton.setEnabled(b);
		toolPanel.nfButton.setEnabled(b);
		toolPanel.sfButton.setEnabled(b);
	}
	
	// 鎖定步驟欄下的三個按鈕(true -> 解鎖)
	private void unLockOthers(boolean b) {
		handlePanel.confirm.setEnabled(b);
		handlePanel.loadStep.setEnabled(b);
		handlePanel.saveStep.setEnabled(b);
	}
	
	// 跟Table有關的動作(拖曳交換)	
	private MouseAdapter clickListener = new MouseAdapter() {
		private int first, second; 	// 紀錄交換的是哪兩個步驟元件
		
		@Override
		public void mousePressed(MouseEvent e) {
			first = removeIndex = stepTable.rowAtPoint(e.getPoint()); // 取得按下時的元件
			Info.showMessage("已點選步驟" + (first + 1));
		}
		
		private Tool firstFunc;
		@Override
		public void mouseReleased(MouseEvent e) {
			second = stepTable.rowAtPoint(e.getPoint()); // 取得鬆開時的元件
			
			if (second != -1 &&
				first != second &&
				(firstFunc = model.get(first)).canMove() &&
				model.get(second).canMove()) {
				
				model.remove(firstFunc); 				// 先移除
				model.insertAt(firstFunc, second);
				Info.showMessage("插入至步驟" + (second + 1));
			}
		}
		
		private int now;
		@Override
		public void mouseDragged(MouseEvent e) {
			now = stepTable.rowAtPoint(e.getPoint());
			stepTable.addRowSelectionInterval(now, now);
		}
	};
	
	private class ToolPanel extends JScrollPane implements ActionListener {
		private static final long serialVersionUID = 4936944520744858424L;
		private String[] imageName = {"\\RemoveOutlier.png", "\\RemoveOutlierBySD.png", "\\RemoveRecord.png", "\\RemoveFeature.png",
				"\\ExtractRecord.png", "\\TargetEncoding.png", "\\NormalizeFeature.png", "\\StandardizeFeature.png"};
		private ImageIcon[] icon = LoadFile.fromIcons(Info.getIconPath(), imageName, 80, 80); 	// Button的圖案
		private JButton erButton, nfButton, sfButton, roButton, rosdButton, rrButton, rfButton, teButton;// 工具欄的按鈕

		public ToolPanel() {
			setViewportBorder(new TitledBorder(null, "工具欄", TitledBorder.CENTER, TitledBorder.BELOW_TOP));
			setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			
			JPanel content = new JPanel(new GridLayout(8, 1, 5, 5));
			setViewportView(content);
			
			roButton = new JButton(icon[0]);
			roButton.setToolTipText("移除指定欄位離群值(僅限數值欄位)");
			roButton.addActionListener(this);
			content.add(roButton);
			
			rosdButton = new JButton(icon[1]);
			rosdButton.setToolTipText("以標準差為基準，移除指定欄位離群值(僅限數值欄位)");
			rosdButton.addActionListener(this);
			content.add(rosdButton);

			rrButton = new JButton(icon[2]);
			rrButton.setToolTipText("移除指定欄位之特定內容值");
			rrButton.addActionListener(this);
			content.add(rrButton);

			rfButton = new JButton(icon[3]);
			rfButton.setToolTipText("移除指定欄位");
			rfButton.addActionListener(this);
			content.add(rfButton);

			erButton = new JButton(icon[4]);
			erButton.setToolTipText("提取指定欄位之特定內容值");
			erButton.addActionListener(this);
			content.add(erButton);

			teButton = new JButton(icon[5]);
			teButton.setToolTipText("指定欄位數值化(適用非數值欄位)");
			teButton.setEnabled(true);
			teButton.addActionListener(this);
			content.add(teButton);

			nfButton = new JButton(icon[6]);
			nfButton.setToolTipText("指定欄位標準化(適用數值欄位)");
			nfButton.addActionListener(this);
			content.add(nfButton);

			sfButton = new JButton(icon[7]);
			sfButton.setToolTipText("指定欄位標準化(僅限數值欄位)");
			sfButton.addActionListener(this);
			content.add(sfButton);
		}

		/**跟Tool有關的動作 | 點擊工具欄的動作*/
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == erButton)
				model.add(tools.newInstance(Tools.Mode.ExtractRecord));
			else if (e.getSource() == rrButton)
				model.add(tools.newInstance(Tools.Mode.RemoveRecord));
			else if (e.getSource() == rfButton)
				model.add(tools.newInstance(Tools.Mode.RemoveFeature));
			else if (e.getSource() == roButton)
				model.add(tools.newInstance(Tools.Mode.RemoveOutlier));
			else if (e.getSource() == rosdButton)
				model.add(tools.newInstance(Tools.Mode.RemoveOutlierBySD));
			else if (e.getSource() == sfButton) {
				model.add(tools.newInstance(Tools.Mode.StandardizeFeature));
				unLockFuncs(false);
				bitches = true;
			}
			else if (e.getSource() == nfButton) {
				model.add(tools.newInstance(Tools.Mode.NormalizeFeature));
				unLockFuncs(false);
				bitches = true;
			}
			else if (e.getSource() == teButton) {
				model.add(tools.newInstance(Tools.Mode.TargetEncoding));
				unLockFuncs(false);
				bitches = true;
			}
			Info.showMessage("已新增步驟");
		}
	}

	private class HandlePanel extends JPanel implements ActionListener {
		private static final long serialVersionUID = 6451689933054716323L;
		private JButton loadStep, saveStep, confirm; // 步驟欄的按鈕與載入資料按鈕
		
		public HandlePanel() {
			setLayout(new BorderLayout());
			setBorder(new TitledBorder(null, "資料處理流程", TitledBorder.CENTER, TitledBorder.BELOW_TOP));
			add(new JScrollPane(stepTable), BorderLayout.CENTER);

			saveStep = new JButton("儲存步驟");
			saveStep.setToolTipText("儲存先前執行並採用的步驟");
			saveStep.addActionListener(this);

			loadStep = new JButton("載入步驟");
			loadStep.setToolTipText("載入先前儲存的步驟");
			loadStep.addActionListener(this);

			confirm = new JButton("預覽結果");
			confirm.addActionListener(this);
			
			Box buttonBox = Box.createHorizontalBox();
			buttonBox.add(Box.createHorizontalGlue());
			buttonBox.add(saveStep);
			buttonBox.add(Box.createHorizontalStrut(5));
			buttonBox.add(loadStep);
			buttonBox.add(Box.createHorizontalStrut(5));
			buttonBox.add(confirm);
			add(buttonBox, BorderLayout.SOUTH);			
		}
		
		/**載入步驟、儲存步驟和預覽結果按鈕*/
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == saveStep) {
				try {
					step.saveWithDialog(allStep);
					Info.showMessage("儲存步驟成功");
				} catch (IOException ee) {
					logger.info(ee.getMessage());
					Info.showError("儲存步驟失敗");
				}

			} else if (e.getSource() == loadStep) {
				try {
					step.loadWithDialog(tools).ifPresent(steps -> {	// 若有內容 才繼續做
						model.clear();
						model.addAll(steps);
						if(!steps.isEmpty()) {		// 的確有載入步驟
							unLockFuncs(false);		// 將工具列鎖定
							bitches = false;		// 因為載入步驟會取代原有所有步驟，所以此時三寶狀態設為false
							loadStepFlag = true;	// 將載入步驟旗標設為true
							Info.showMessage("載入步驟成功");
						}
					});
				} catch (IOException ee) {
					logger.info(ee.getMessage());
					Info.showError("載入步驟失敗");
				}

			} else if (e.getSource() == confirm) {
				if(model.isEmpty()) {
					int choose = JOptionPane.showConfirmDialog(DataHandlePanel.this, "是否要將資料處理後檔案設為原資料？", "進行重設", JOptionPane.YES_NO_OPTION);
					if (choose == JOptionPane.YES_OPTION) {
						try {
							tools.setInFile(Info.getDataTransform());
							CreateFile.copy(Info.getDataTransform(), Info.getDataHandle());
							afterSheet.update(Info.getDataHandle());
							
							//將步驟檔清空
							Path step = Paths.get(Info.getDataHandleStep());
							Files.deleteIfExists(step);
							Files.createFile(step);
							
							Page.FEATURE_SELECT.setEnabled(true);
							Info.showMessage("資料處理成功");
						} catch (Exception ee) {
							Page.FEATURE_SELECT.setEnabled(false);
							logger.info(ee.getMessage());
							Info.showError("資料處理失敗");
						}
						return;
					}
				}
				
				unLockOthers(false);
				unLockFuncs(false);
				try {
					popResult(model);
					Page.FEATURE_SELECT.setEnabled(true);
					Info.showMessage("資料處理成功");
				} catch (Exception ee) {
					logger.info(ee.getMessage());
					Info.showError("資料處理失敗");
					Page.FEATURE_SELECT.setEnabled(false);
					JOptionPane.showMessageDialog(DataHandlePanel.this, "請檢查設定步驟", "處理步驟錯誤", JOptionPane.ERROR_MESSAGE);
				}
				if(!bitches && !loadStepFlag) {	// 若非三寶狀態也非載入步驟狀態
					unLockFuncs(true);	// 解鎖工具列
				}
				unLockOthers(true);
			}
		}
		
		/**
		 * 彈窗後做的事情<br>
		 * 1.載入步驟執行後所得到的檔案<br>
		 * 2.如果按取消 -> 不做任何事(彈窗消失)<br>
		 * 如果按確定 -> 更新PreviewSheet<br>
		 * 移除所有步驟<br>
		 * 更新Tool(即時化)<br>
		 * 
		 * @param stepsModel 步驟欄的步驟
		 * @throws FileNotFoundException
		 * @throws IOException
		 */
		private void popResult(TableModel stepsModel) throws Exception {
			String funcName;
			for (Tool tool: stepsModel) {	// 做非移除欄位的動作
				funcName = tool.getClass().getSimpleName();
				if(!funcName.equals("RemoveFeature")) {
					tool.execute();
				}
			}
			for (Tool tool: stepsModel) {	// 移除欄位的動作放在最後面才做
				funcName = tool.getClass().getSimpleName();
				if (funcName.equals("RemoveFeature")) {
					tool.execute();
				}
			}
			
			//========================================
			Info.showMessage("資料更新中...");
			File file = tools.getTempOutFile();
			
			confirmSheet.update(file);	// 更新確認表格
			int choose = JOptionPane.showConfirmDialog(DataHandlePanel.this, confirmSheet, "是否採用此資料集？", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
			if (choose == JOptionPane.YES_OPTION) {
				bitches = false;		// 將三寶旗標設為false
				loadStepFlag = false;	// 將載入步驟旗標設為false
				showProgress.update(new SwingWorker<Void, Void>(){
					@Override
					protected Void doInBackground() {
						try {
							// 將執行結果複製到目的地
							CreateFile.copy(file, Info.getDataHandle());
							afterSheet.update(file);	// 更新執行後表格
							allStep.addAll(stepsModel);	// 將所有步驟加入
							step.saveAll(allStep, Info.getDataHandleStep());	// 將這些步驟儲存起來
							// 更新
							tools.update(Enum.title(file), Enum.content(file));	// tool的內容來源
							model.removeAll();	// 移除步驟欄所有步驟
							tools.adoptResult();// 設定採用此資料集
							Info.showMessage("資料更新成功！");
							
						} catch (Exception e) {
							logger.info(e.getMessage());
							Info.showError("資料更新失敗！");
						}
						return null;
					}
				});
			} else {
				tools.abandoneResult();
				Info.showMessage("已取消更新！");
			}
			confirmSheet.clear();	// 清除確認表格
		}
	}

	/**預覽版面*/
	private class PreViewPanel extends JPanel {
		private static final long serialVersionUID = -8552488333943233617L;
		public PreViewPanel() {
			setLayout(new BorderLayout());
			setBorder(new TitledBorder(null, "資料預覽", TitledBorder.CENTER, TitledBorder.BELOW_TOP));
			
			beforeSheet.setTableTitle("資料結果(處理前)");
			afterSheet.setTableTitle("資料結果(處理後)");
			JSplitPane previewSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, beforeSheet, afterSheet);
			previewSplit.setBorder(null);
			previewSplit.setDividerLocation(previewSplit.getPreferredSize().height / 2);
			add(previewSplit, BorderLayout.CENTER);
			
			unLockFuncs(false);
			unLockOthers(false);
		}
	}
	
	/**
	 * 資料結果(處理前) 預覽表格<br>
	 * 繼承{@link AbstractPreview}
	 */
	private class BeforeSheet extends AbstractPreview {
		private static final long serialVersionUID = -7287660204910605281L;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Info.showMessage("載入新資料集...");
			int choose = chooser.showOpenDialog(this);
			
			// 若選擇了檔案，則輸入選擇了什麼檔案(檔名)
			if (choose == JFileChooser.APPROVE_OPTION) {
				try {
					TitleIndex.append(chooser.getSelectedFile(), Info.getImpAndExpTemp());
					LSH.append(Info.getImpAndExpTemp(), Info.getDataTransform());
					setFile();
				} catch (Exception ee) {
					logger.info(ee.getMessage());
				}
			} else {
				Info.showMessage("取消載入資料集");
			}
		}
	}
	
	/**繼承{@link AbstractTools}*/
	private class Tools extends AbstractTools {
		/**
		 * Tool建構子
		 * @param inFile 輸入檔案
		 * @param outPath 輸出路徑
		 * @param title 欄位標題陣列
		 * @param content 內容陣列
		 */
		public Tools(String inFile, String outPath, String[] title, String[][] content) {
			super(inFile, outPath, title, content);
		}
		
		/**
		 * Tool建構子
		 * @param inFile 輸入檔案
		 * @param outPath 輸出路徑
		 */
		public Tools(String inFile, String outPath) {
			super(inFile, outPath);
		}
		
		/**
		 * 按下移除按鈕要做的動作
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			model.removeItem(removeIndex);
		}
	}
	
	/**繼承{@link PanelTableModel}*/
	private class TableModel extends PanelTableModel<Tool> {
		private static final long serialVersionUID = -7797314770182057947L;

		public void removeItem(int index) {
			if(loadStepFlag) {	// 若步驟欄是載入的，一按下移除按鈕則全部刪除
				int choose = JOptionPane.showConfirmDialog(DataHandlePanel.this, "將刪除所有步驟。是否刪除？", "確認刪除", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(choose == JOptionPane.YES_OPTION) {
					loadStepFlag = false;
					unLockFuncs(true);	// 解除工具列
					removeAll();		// 移除所有步驟
				}
				return;
			}
			
			String funcName = get(index).getClass().getSimpleName();
			switch(funcName) {
				case "NormalizeFeature":
				case "StandardizeFeature":
				case "TargetEncoding":
					bitches = false;	 // 重設三寶狀態
					unLockFuncs(true);
					break;
			}
			
			remove(index);
			Info.showMessage("已移除指定步驟");
		}
		
		public void removeAll() {
			bitches = false;	 // 重設三寶狀態
			super.clear();
			Info.showMessage("已移除所有步驟");
		}
	}
}
