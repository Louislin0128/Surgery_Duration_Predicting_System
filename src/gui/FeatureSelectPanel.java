package gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import guiComponent.AbstractPreview;
import guiComponent.ShowProgress;
import guiFunction.CreateFile;
import guiFunction.Enum;
import guiFunction.FeatureInfo;
import guiFunction.LSH;
import guiFunction.Step;
import guiFunction.TitleIndex;
import predict.FeatureSelect;
import predict.FeatureSelectException;
import preprocess.RemoveFeature;

class FeatureSelectPanel extends JPanel implements ActionListener, Panel {
	private static final long serialVersionUID = 2449969085578476145L;
	private Logger logger = Logger.getLogger("FeatureSelectPanel");
	private Font font20 = new Font("微軟正黑體", Font.PLAIN, 20);
	private JFileChooser chooser = new JFileChooser(Info.getDesktop());
	private JButton confirmButton = new JButton("確認並選取");
	private RankFeature rankFeature = new RankFeature();
	private BeforeSheet beforeSheet = new BeforeSheet();
	private AbstractPreview afterSheet = AbstractPreview.newSheetWithoutImport();
	private ShowProgress showProgress = new ShowProgress();
	private Border normalBorder = LineBorder.createBlackLineBorder();
	private Border errorBorder = new LineBorder(Color.RED, 3, true);
	private LinkedHashMap<String, String> attrsRank;

	public FeatureSelectPanel() {
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(new FileNameExtensionFilter("CSV File", "csv"));
		setLayout(new BorderLayout(5, 5));
		
		beforeSheet.setTableTitle("資料內容(選取前)");
		afterSheet.setTableTitle("資料內容(選取後)");
		JSplitPane previewPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, beforeSheet, afterSheet);
		previewPanel.setDividerLocation(previewPanel.getPreferredSize().height / 2);
		add(previewPanel, BorderLayout.CENTER);
		
		JLabel tipLabel = new JLabel("請選取要用於預測的特徵：");
		confirmButton.addActionListener(this);
		rankFeature.setViewportBorder(normalBorder);
		
		JPanel eastPanel = new JPanel(new GridBagLayout());
		add(eastPanel, BorderLayout.EAST);
		Insets inset = new Insets(5, 0, 5, 0);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 0.5;
		gbc.insets = inset;
		gbc.anchor = GridBagConstraints.LINE_START;
		eastPanel.add(tipLabel, gbc);

		gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 0.5;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = inset;
		eastPanel.add(rankFeature, gbc);

		gbc = new GridBagConstraints();
		gbc.weightx = 0.5;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = inset;
		eastPanel.add(confirmButton, gbc);
	}
	
	private class BeforeSheet extends AbstractPreview {
		private static final long serialVersionUID = -7287660204910605281L;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			int choose = chooser.showOpenDialog(getParent());
			if (choose == JFileChooser.APPROVE_OPTION) {
	        	try {
	        		//計算特徵重要度的資料來源
	        		TitleIndex.append(chooser.getSelectedFile(), Info.getImpAndExpTemp());
	        		LSH.append(Info.getImpAndExpTemp(), Info.getDataHandle());
					//使用者選擇的檔案我們視為前個步驟-資料處理的檔案
					setFile();	// 設定檔案 開始特徵選取
					Info.showMessage("資料載入成功");
					
				} catch (Exception ee) {
					logger.info(ee.getMessage());
					Info.showError("資料處理失敗");
					JOptionPane.showMessageDialog(FeatureSelectPanel.this, ee.getMessage(), "似乎出了點問題...", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	@Override
	public void setFile() {
		if(Files.notExists(Paths.get(Info.getDataHandle()))) {
			beforeSheet.setImportEnabled(true);
			return;
		}
		beforeSheet.setImportEnabled(false);
		
		Page.FEATURE_SELECT.setEnabled(false);	// 不讓使用者在程式執行完成前按下按鈕
		showProgress.update(new SwingWorker<Void, Void>(){
			@Override
			protected Void doInBackground() {
				confirmButton.setEnabled(false);
				rankFeature.clear();
				beforeSheet.clear();
				afterSheet.clear();
				
				try {
					showProgress.setText("正在讀取資料");
					beforeSheet.update(Info.getDataHandle());	//載入資料處理的檔案
					Info.showMessage("資料載入成功");
				} catch (Exception e) {
					logger.info(e.getMessage());
					Info.showError("資料載入失敗");
					JOptionPane.showMessageDialog(FeatureSelectPanel.this, e.getMessage(), "似乎出了點問題...", JOptionPane.ERROR_MESSAGE);
					return null;
				}
				
				try {
					showProgress.setText("正在進行特徵重要度評比");			
					attrsRank = FeatureSelect.startRank(Info.getDataHandle(), Info.getTransFeature(), Info.getTempStep());
					rankFeature.update(attrsRank);		//更新特徵排序列表
					Info.showMessage("特徵重要度評比成功");
					
				} catch (FeatureSelectException e) {	//資料集不符合要求
					Info.showError("資料集不符合要求");
					
					int choose = JOptionPane.showConfirmDialog(FeatureSelectPanel.this, e.getMessage(), "似乎出了點問題...", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
					if(choose == JOptionPane.YES_OPTION) {
						try {
							//通常跳出PredictorsException例外時，代表資料集不符合要求，且暫時步驟檔已經存在
							Step.executeRemoveFeature(Info.getDataHandle(), Info.getTempStep(), Info.getTransFeature());
							CreateFile.copy(Info.getTransFeature(), Info.getDataHandle());
							Files.delete(Paths.get(Info.getTempStep()));
							setFile();//再執行讀檔
							
						} catch (Exception ee) {
							logger.info(ee.getMessage());
							Info.showError("移除無用特徵時發生錯誤");
							JOptionPane.showMessageDialog(FeatureSelectPanel.this, "移除無用特徵時發生錯誤");
						}
						
					}else {	// JOptionPane.NO_OPTION
						//若Weka特徵選取無法使用，則直接提供所有特徵供使用者選取
						setNoRankOptions();
					}
					
				} catch (Exception e) {
					setNoRankOptions();
				}
				
				confirmButton.setEnabled(true);
				Page.FEATURE_SELECT.setEnabled(true);
				return null;
			}
		});
	}
	
	@Override
	public void reset() {
		rankFeature.clear();
		beforeSheet.clear();
		afterSheet.clear();
		beforeSheet.setImportEnabled(true);
	}
	
	/**顯示未提供排名的特徵選項*/
	private void setNoRankOptions() {
		Info.showError("特徵重要度評比失敗");
		try {
			String[] title = Enum.title(Info.getDataHandle());
			rankFeature.update(Arrays.copyOf(title, title.length - 1));	// 不顯示最後一欄的手術時間或麻醉時間
			JOptionPane.showMessageDialog(this, "無法計算特徵重要度");
		} catch(Exception e) {
			logger.info(e.getMessage());
			JOptionPane.showMessageDialog(this, "無法提供任何特徵供您選擇，請使用其他檔案");
		}
	}

	/**按下「確認並提取」按鈕時要做的動作*/
	@Override
	public void actionPerformed(ActionEvent e) {
		// 檢查使用者是否想刪掉全部的屬性，若是則提示
		ArrayList<String> keep;
		if((keep = rankFeature.listRemove(false)).isEmpty()) {
			// 若保留特徵陣列為空，代表使用者想刪掉全部的屬性。這不被允許！
			afterSheet.clear();		//清除特徵選取後預覽表格
			remindError(rankFeature, "請至少選擇一個屬性。");
			return;
		}
		
		// 建立特徵選取的資訊檔案
		try {
			if(attrsRank == null) {	//若無法進行特徵排序，輸出「無法提供」的特徵檔案
				FeatureInfo.buildWithoutScore(keep, Info.getFeatureInfo());
			}else{
				FeatureInfo.buildWithScore(keep, attrsRank, Info.getFeatureInfo());
			}
		} catch (Exception ee) {
			logger.info(ee.getMessage());
			Page.DATA_SPLIT.setEnabled(false);
			Info.showError("建立特徵重要度檔案失敗");
			return;
		}
		
		// 如果使用者不移除特徵，將原始檔案直接輸出
		ArrayList<String> remove = rankFeature.listRemove(true);
		if(remove.isEmpty()) {
			try {
				CreateFile.copy(Info.getDataHandle(), Info.getFeatureSelect());
				afterSheet.update(Info.getFeatureSelect());
				Page.DATA_SPLIT.setEnabled(true);
				Info.showMessage("不移除特徵，特徵選取成功");
			} catch (Exception ee) {
				logger.info(ee.getMessage());
				Page.DATA_SPLIT.setEnabled(false);
				Info.showError("建立特徵選取檔案失敗");
				JOptionPane.showMessageDialog(this, "建立特徵選取檔案失敗");
			}
			return;
		}
		
		//檢查資料處理頁面的步驟檔是否存在
		//存在：需要重新調整步驟檔的資料，把已經移除的特徵移除
		//不存在：跳過步驟調整，把步驟檔刪除。
		if(Files.exists(Paths.get(Info.getDataHandleStep()))) {
			try {
				//重新調整步驟檔，移除不須用到的特徵步驟
				Step.rearrange(Info.getDataHandleStep(), remove, Info.getStep());
			} catch (IOException ee) {
				logger.info(ee.getMessage());
				Page.DATA_SPLIT.setEnabled(false);
				Info.showError("建立調整後的步驟檔失敗");
				JOptionPane.showMessageDialog(this, "建立調整後的步驟檔失敗");
				return;
			}
		}else {
			try {
				Files.deleteIfExists(Paths.get(Info.getStep()));
			} catch (IOException ee) {
				logger.info(ee.getMessage());
				Info.showError("內部流程執行失敗");
				JOptionPane.showMessageDialog(this, "內部流程執行失敗，請再試一次");
			}
		}
		
		// 取得使用者欲移除的欄位名稱及建構RemoveFeature的傳入參數
		try {
			RemoveFeature.exec(Info.getDataHandle(), Info.getFeatureSelect(), remove);
			afterSheet.update(Info.getFeatureSelect());
			Page.DATA_SPLIT.setEnabled(true);
			Info.showMessage("移除特徵成功");
		} catch (Exception ee) {
			logger.info(ee.getMessage());
			Page.DATA_SPLIT.setEnabled(false);
			Info.showError("移除特徵失敗");
			JOptionPane.showMessageDialog(this, "移除特徵失敗");
		}
	}
	
	private void remindError(JComponent comp, String errorMessage) {
		comp.setBorder(errorBorder);
		JOptionPane.showMessageDialog(this, errorMessage, "提示訊息", JOptionPane.WARNING_MESSAGE);
		comp.setBorder(normalBorder);
	}

	public class RankFeature extends JScrollPane {
		private static final long serialVersionUID = 3838537531891727458L;
		private int defaultRowHeight = 30;
		private JTable table = new JTable() {
			private static final long serialVersionUID = -1340347628284121524L;
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == 0) {
					return Boolean.class;
				}
				return String.class;
			}
		};
		private String[] all = {"全選", "重要度", "欄位名稱"};
		private String[] none = {"全不選", "重要度", "欄位名稱"};

		public RankFeature() {
			table.setRowHeight(defaultRowHeight);
			table.setDefaultEditor(Object.class, null);
			table.setFillsViewportHeight(true);
			table.setFont(font20);
			table.setDragEnabled(false);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					int index = table.rowAtPoint(e.getPoint());
					if (index != -1) {
						boolean check = (boolean) table.getValueAt(index, 0);
						table.setValueAt(!check, index, 0);
					}
				}
			});
			setViewportView(table);
			
			DefaultTableCellRenderer r = (DefaultTableCellRenderer) table.getDefaultRenderer(Object.class);
			r.setHorizontalAlignment(SwingConstants.CENTER);
			table.setDefaultRenderer(Object.class, r);
			
			JTableHeader columnHeader = table.getTableHeader();
			columnHeader.setReorderingAllowed(false);
			columnHeader.setFont(font20);
			columnHeader.setToolTipText("點擊首欄可全選/取消全選");
			((DefaultTableCellRenderer) columnHeader.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
			columnHeader.addMouseListener(firstColumnSelect);
		}
		
		private MouseAdapter firstColumnSelect = new MouseAdapter() {
			private boolean flag = true;	//true 全選 | false 取消全選
			public void mousePressed(MouseEvent e) {
				int index = table.columnAtPoint(e.getPoint());
				if (index == 0) {	//選擇首欄的標題
					DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
					flag = !flag;
					if(flag) {
						tableModel.setColumnIdentifiers(all);
					}else {
						tableModel.setColumnIdentifiers(none);
					}
					for(int i = 0, count = table.getRowCount(); i < count; i++) {
						table.setValueAt(flag, i, 0);
					}
				}
			}			
		};
		
		/**建立有重要度排序的表格*/
		private void update(LinkedHashMap<String, String> rank) {
			Object[][] content = new Object[rank.size()][3];
			int index = 0;
			double score;
			for(Entry<String, String> rankEntry: rank.entrySet()) {
				score = Double.parseDouble(rankEntry.getValue());
				content[index++] = new Object[] {score >= 0, rankEntry.getValue(), rankEntry.getKey()};
				// 若值為正，預先打勾；若為負，預先取消打勾
			}
			((DefaultTableModel) table.getModel()).setDataVector(content, all);
		}
		
		/**建立沒有重要度排序的表格*/
		private void update(String[] title) {
			Object[][] content = new Object[title.length][3];
			int index = 0;
			for(String s: title) {
				content[index++] = new Object[] {true, "無法提供", s};
			}
			((DefaultTableModel) table.getModel()).setDataVector(content, all);
		}
		
		/**清除表格*/
		private void clear() {
			((DefaultTableModel) table.getModel()).setColumnCount(0);
		}
		
		/**
		 * 列舉欲保留或移除的特徵
		 * @Param remove 是否列舉欲移除的特徵。若為false，列舉欲保留的特徵
		 * @return 特徵陣列
		 * @throws IllegalStateException 若使用者欲移除所有欄位時，會拋出例外
		 */
		private ArrayList<String> listRemove(boolean listRemove) {
			int count = table.getRowCount();	// 特徵個數
			ArrayList<String> list = new ArrayList<>(count);
			for (int i = 0; i < count; i++) {
				if ((boolean) table.getValueAt(i, 0) ^ listRemove)	{// 互斥或
					// 欄位為true，特徵保留；欄位為false，特徵移除
					list.add(table.getValueAt(i, 2).toString());
				}
			}
			return list;
		}
	}
}
