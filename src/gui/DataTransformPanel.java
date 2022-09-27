package gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import guiComponent.AbstractPreview;
import guiComponent.ShowProgress;
import guiFunction.LSH;
import guiFunction.LoadFile;
import guiFunction.TitleIndex;
import preprocess.AppendRecord;
import preprocess.CreateClassLabel4ATP;
import preprocess.CreateClassLabel4STP;
import preprocess.MergeDRG;
import preprocess.MergeFeature;
import preprocess.SummarizeData;

class DataTransformPanel extends JPanel implements ActionListener, Panel {
	private static final long serialVersionUID = -4528788971375791834L;
	private Logger logger = Logger.getLogger("DataTransformPanel");
	private String[] imageName = {"\\chart.png", "\\Off.png", "\\On.png"};
	private ImageIcon[] icon = LoadFile.fromIcons(Info.getIconPath(), imageName, 50, 50);
	private ChooserPanel chooser = new ChooserPanel();
	private MergeStpAtpPanel mergeStpAtp = new MergeStpAtpPanel();
	private AbstractPreview sheet = AbstractPreview.newSheetWithoutImport();
	private ShowProgress showProgress = new ShowProgress();
	private ViewGraph viewGraph = new ViewGraph();
	private CompoundBorder normalBorder = BorderFactory.createCompoundBorder(LineBorder.createBlackLineBorder(),
			BorderFactory.createEmptyBorder(5, 5, 5, 5));
	private CompoundBorder errorBorder = BorderFactory.createCompoundBorder(new LineBorder(Color.RED, 3, true),
			BorderFactory.createEmptyBorder(5, 5, 5, 5));
	private JButton chartButton = new JButton(icon[0]),
			confirmButton = new JButton("確認");

	public DataTransformPanel() {
		setLayout(new BorderLayout());
		
		JPanel westPanel = new JPanel(new GridBagLayout());
		Insets inset = new Insets(0, 5, 0, 5);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 1.0;
		gbc.weighty = 0.5;
		gbc.ipady = 200;
		gbc.insets = inset;
		gbc.fill = GridBagConstraints.BOTH;
		westPanel.add(chooser, gbc);
		
		mergeStpAtp.setBorder(normalBorder);
		gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 1.0;
		gbc.weighty = 0.5;
		gbc.insets = inset;
		gbc.fill = GridBagConstraints.BOTH;
		westPanel.add(mergeStpAtp, gbc);
		
		chartButton.setEnabled(false);
		chartButton.setToolTipText("顯示資料圖表");
		chartButton.addActionListener(this);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.insets = inset;
		westPanel.add(chartButton, gbc);
		
		confirmButton.addActionListener(this);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.insets = inset;
		westPanel.add(confirmButton, gbc);
		add(westPanel, BorderLayout.WEST);
		
		sheet.setTableTitle("資料轉檔完的結果");
		add(sheet, BorderLayout.CENTER);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(confirmButton == e.getSource()) {
			if (chooser.chooserHasNoOption()) {
				chooser.setBorder(errorBorder);
				sheet.clear();				
				JOptionPane.showMessageDialog(this, "請至少選擇一個年報表。", "似乎出了點問題...", JOptionPane.ERROR_MESSAGE);				
				chooser.setBorder(normalBorder);
				// 向使用者提示錯誤
				return;
			}
			
			showProgress.update(new SwingWorker<Void, Void>(){
				@Override
				public Void doInBackground() {
					transform();
					return null;
				}
			});
			
		}else {	//chartButton
			viewGraph.setVisible(true);
		}
	}
	
	private void transform() {
		setFuncEnabled(false);
		try {
			showProgress.setText("正在合併年報表");
			AppendRecord.exec(chooser.buildAppendRecord(), Info.getAppendRecord());
			
			String inFile = Info.getAppendRecord();	// 輸入檔案
			if (mergeStpAtp.mergeMonthToggle.isSelected()) {
				showProgress.setText("正在合併年與月報表");
				MergeFeature.exec(inFile, Info.getWorkingData().toString(), Info.getDoctorPath(), Info.getMergeFeature());
				inFile = Info.getMergeFeature();
			}
			
			if(mergeStpAtp.mergeDRGToggle.isSelected()) {
				showProgress.setText("正在合併DRG檔案");
				MergeDRG.exec(inFile, Info.getDRGPath(), Info.getMergeDRG());
				inFile = Info.getMergeDRG();
			}
			
			if (mergeStpAtp.stp.isSelected()) {
				showProgress.setText("正在計算手術時間");
				CreateClassLabel4STP.exec(inFile, Info.getCreateTime());
			} else {
				showProgress.setText("正在計算麻醉時間");
				CreateClassLabel4ATP.exec(inFile, Info.getCreateTime());
			}
			
			//
			showProgress.setText("正在產生統計資訊");
			SummarizeData.exec(Info.getCreateTime(), Info.getDoctorPath(), Info.getSummarizeDataPath());
			//
			showProgress.setText("正在為標題編號");
			TitleIndex.append(Info.getCreateTime(), Info.getRearrangeTitle());
			//
			showProgress.setText("正在加上流水號");
			LSH.append(Info.getRearrangeTitle(), Info.getDataTransform());
			//
			viewGraph.update(Info.getSummarizeDataPath());
			chartButton.setEnabled(true);
			sheet.update(Info.getDataTransform());
			
			Page.DATA_HANDLE.setEnabled(true);
			Info.showMessage("資料轉檔成功");
		} catch (Exception ee) {
			Page.DATA_HANDLE.setEnabled(false);
			logger.info(ee.getMessage());
			Info.showError("資料轉檔失敗");
			JOptionPane.showMessageDialog(DataTransformPanel.this, ee.getMessage(), "似乎出了點問題...", JOptionPane.ERROR_MESSAGE);
		}
		setFuncEnabled(true);
	}

	@Override
	public void setFile() {
		if(Files.notExists(Paths.get(Info.getDataTransform()))) {
			chooser.update(Info.getNewYearPath());
		}
	}
	
	@Override
	public void reset() {
		sheet.clear();
		chooser.clear();
	}
	
	private void setFuncEnabled(boolean b) {
		chooser.setEnabled(b);
		mergeStpAtp.setEnabled(b);
		chartButton.setEnabled(b);
		confirmButton.setEnabled(b);
	}

	private class ChooserPanel extends JPanel implements ListCellRenderer<JCheckBox>, PropertyChangeListener {
		private static final long serialVersionUID = -470229501144606243L;
		private DefaultListModel<JCheckBox> listModel = new DefaultListModel<>();
		private JList<JCheckBox> list = new JList<JCheckBox>(listModel);
		private ArrayList<JCheckBox> checkList;
		private Color mainColor = Color.decode("#e9ebfe"), white = Color.WHITE;
		
		public ChooserPanel() {
			addPropertyChangeListener(this);
			setLayout(new BorderLayout());
			setBorder(new TitledBorder(null, "年報表清單", TitledBorder.CENTER, TitledBorder.BELOW_TOP));
			setToolTipText("請選擇一個以上的年報表以合併");
			
			list.setCellRenderer(this);
			list.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if(isEnabled()) {	//如果該版面為啟用狀態，才執行動作
						int[] select = list.getSelectedIndices();
						int length = select.length;
						if(length == 1) {
							JCheckBox check = listModel.get(select[0]);
							setCheckBox(check, !check.isSelected());
							list.clearSelection();
							return;
						}
						
						int n = 0;
						for(int i = 0, size = checkList.size(); i < size; i++) {
							if(length > n && i == select[n]) {
								setCheckBox(listModel.get(i), true);
								n += 1;
							}else {
								setCheckBox(listModel.get(i), false);
								
							}
						}
						list.repaint();
					}
				}
				
				private void setCheckBox(JCheckBox check, boolean setSelected) {
					if(setSelected) {
						check.setBackground(mainColor);
						check.setSelected(true);
					}else {
						check.setBackground(white);
						check.setSelected(false);
					}
				}
			});
			add(new JScrollPane(list), BorderLayout.CENTER);
		}

		public void update(String folder) {
			File[] files = new File(folder).listFiles();
			checkList = new ArrayList<>(files.length);
			JCheckBox check;
			for (File file: files) {
				check = new JCheckBox(file.getName());
				check.setBackground(white);
				checkList.add(check);
			}
			listModel.clear();
			listModel.addAll(checkList);
		}
		
		public void clear() {
			listModel.clear();
		}

		private boolean chooserHasNoOption() {
			for(JCheckBox check: checkList) {
				if(check.isSelected()) {
					return false;
				}
			}
			return true;
		}
		
		private String[] buildAppendRecord() {
			ArrayList<String> fileList = new ArrayList<String>();
			for(JCheckBox check: checkList) {
				if(check.isSelected()) {
					fileList.add(Info.getNewYearPath() + "\\" + check.getText());
				}
			}
			return fileList.toArray(new String[fileList.size()]);
		}
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if(evt.getPropertyName().equals("enabled")) {
				 list.setEnabled(!list.isEnabled());
			}
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends JCheckBox> list, JCheckBox value, int index,
				boolean isSelected, boolean cellHasFocus) {
			value.setEnabled(isEnabled());
			value.setFocusPainted(false);
			value.setBorderPainted(true);
			value.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : new EmptyBorder(1, 1, 1, 1));
			return value;
		}
	}

	private class MergeStpAtpPanel extends JPanel implements PropertyChangeListener{
		private static final long serialVersionUID = 248221410459070138L;
		private JToggleButton mergeMonthToggle = new JToggleButton(icon[1]),
				mergeDRGToggle = new JToggleButton(icon[1]);;
		private JRadioButton stp = new JRadioButton("手術時間"),
				atp = new JRadioButton("麻醉時間");

		public MergeStpAtpPanel() {
			addPropertyChangeListener(this);
			setLayout(new GridBagLayout());
			//======================
			mergeMonthToggle.setSelectedIcon(icon[2]);
			mergeMonthToggle.setBorderPainted(false);
			mergeMonthToggle.setContentAreaFilled(false);
			mergeMonthToggle.setFocusPainted(false);
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			gbc.anchor = GridBagConstraints.WEST;
			add(new JLabel("是否合併月報表資料："), gbc);

			gbc = new GridBagConstraints();
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			add(mergeMonthToggle, gbc);
			//======================
			mergeDRGToggle.setSelectedIcon(icon[2]);
			mergeDRGToggle.setBorderPainted(false);
			mergeDRGToggle.setContentAreaFilled(false);
			mergeDRGToggle.setFocusPainted(false);
			
			gbc = new GridBagConstraints();
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			gbc.anchor = GridBagConstraints.WEST;
			add(new JLabel("是否合併DRG編號："), gbc);

			gbc = new GridBagConstraints();
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			add(mergeDRGToggle, gbc);
			//======================
			stp.setSelected(true);
			ButtonGroup radioGroup = new ButtonGroup();
			radioGroup.add(stp);
			radioGroup.add(atp);
			
			gbc = new GridBagConstraints();
			gbc.weightx = 1.0;
			gbc.weighty = 0.5;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.anchor = GridBagConstraints.WEST;
			add(new JLabel("要預測何種資料："), gbc);

			gbc = new GridBagConstraints();
			gbc.weightx = 1.0;
			gbc.weighty = 0.5;
			add(stp, gbc);

			gbc = new GridBagConstraints();
			gbc.weightx = 1.0;
			gbc.weighty = 0.7;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			add(atp, gbc);
		}

		@Override
		public void propertyChange(PropertyChangeEvent pce) {
			if(pce.getPropertyName().equals("enabled")) {
				 mergeMonthToggle.setEnabled(!mergeMonthToggle.isEnabled());
				 mergeDRGToggle.setEnabled(!mergeDRGToggle.isEnabled());
				 stp.setEnabled(!stp.isEnabled());
				 atp.setEnabled(!atp.isEnabled());
			}
		}
	}
}
