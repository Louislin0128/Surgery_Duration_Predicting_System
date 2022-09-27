package gui;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import guiComponent.AbstractPreview;
import guiFunction.Enum;
import guiFunction.FeatureInfo;
import guiFunction.LoadFile;
import guiFunction.ZipOrUnzip;

class TrainResultPanel extends JPanel implements ActionListener, Panel {
	private static final long serialVersionUID = 7225862284829363231L;
	private Logger logger = Logger.getLogger("TrainResultPanel");
	private JFileChooser chooser = new JFileChooser(Info.getDesktop());
	private JButton save = new JButton("儲存模型"),
			lookUp = new JButton("檢視測試集"),
			done = new JButton("完成建立");
	private JTextArea resultText = new JTextArea();
	private AbstractPreview featureSheet = AbstractPreview.newNothingSheet(),
			showPandA = AbstractPreview.newNothingSheet(),
			popTable = AbstractPreview.newNothingSheet();
	private PopUp popUpFrame = new PopUp(popTable);
	
	public TrainResultPanel() {
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(new FileNameExtensionFilter("Model File", "model"));
		
		setLayout(new GridLayout(1, 2, 10, 10));
		resultText.setEditable(false);
		
		featureSheet.setTableTitle("特徵及其重要度");
		showPandA.setTableTitle("實際值與預測值的對照");
		popTable.setTableTitle("檢視測試集");
		
		JScrollPane resultScroll = new JScrollPane(resultText);
		resultScroll.setViewportBorder(new TitledBorder(null, "訓練模型的細節", TitledBorder.CENTER, TitledBorder.BELOW_TOP));
		
		JSplitPane infoSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, resultScroll, featureSheet);
		infoSplit.setBorder(null);
		infoSplit.setDividerLocation(infoSplit.getPreferredSize().height / 2);
		add(infoSplit);
		
		lookUp.addActionListener(this);
		save.addActionListener(this);
		done.addActionListener(this);
		
		Box buttonBox = Box.createHorizontalBox();
		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(lookUp);
		buttonBox.add(Box.createHorizontalStrut(5));
		buttonBox.add(save);
		buttonBox.add(Box.createHorizontalStrut(5));
		buttonBox.add(done);
		
		JPanel viewPanel = new JPanel(new BorderLayout(10, 10));
		viewPanel.add(showPandA, BorderLayout.CENTER);
		viewPanel.add(buttonBox, BorderLayout.SOUTH);
		add(viewPanel);
	}
	
	private class PopUp extends JFrame {
		private static final long serialVersionUID = -4929202953380274007L;
		public PopUp(AbstractPreview sheet) {
			setTitle("資料預覽");
			setSize(sheet.getPreferredSize().width, sheet.getPreferredSize().height);
			setContentPane(sheet);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setLocationRelativeTo(TrainResultPanel.this);
		}
	}
	
	@Override
	public void setFile() {
		try {
			// 在顯示特徵排名前先檢查，若無則建立之
			if(Files.notExists(Paths.get(Info.getFeatureInfo()))) {
				String[] title = Enum.title(Info.getModelTrain());
				// 不顯示手術時間 | 麻醉時間
				FeatureInfo.buildWithoutScore(Arrays.copyOf(title, title.length - 1), Info.getFeatureInfo());
			}
			featureSheet.update(Info.getFeatureInfo()); // 更新特徵排名
			
			resultText.setText(LoadFile.fromText(Info.getTrainResult()));
			showPandA.update(Info.getPandA());	//顯示預測值及對照值
			popTable.update(Info.getModelTest());
			
		} catch (Exception e) {
			logger.info(e.getMessage());
			Info.showError("無法訓練結果");
			JOptionPane.showMessageDialog(this, "無法訓練結果");
		}
	}
	
	@Override
	public void reset() {
		resultText.setText("");
		featureSheet.clear();
		showPandA.clear();
		popTable.clear();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == lookUp) {
			popUpFrame.setVisible(true);	//檢視測試集
			
		}else if(e.getSource() == save) {	//儲存模型
			int choose = chooser.showSaveDialog(this);
			if (choose == JFileChooser.APPROVE_OPTION) {
				try {
					//壓縮
					//XXX Paths.get(Info.getModelPath())
					Path source = Info.getWorkingData();
					String name = chooser.getSelectedFile().getAbsolutePath();
					if(!name.endsWith(".model")) {
						name += ".model";
					}
					Path target = Paths.get(name);
					ZipOrUnzip.zip(source, target);
					System.out.println("壓縮成功");
				    Info.showMessage("儲存模型檔成功");
				} catch (IOException ee) {
					System.err.println("壓縮失敗");
				    Info.showError("儲存模型檔失敗");
				}
			}
		}else if(e.getSource() == done) {	//完成建立
			int choose = JOptionPane.showConfirmDialog(this, "確定要使用此模型嗎？\n(若要續用此模型，請先儲存)", "按下確定後無法返回", JOptionPane.YES_NO_OPTION);
			if(choose == JOptionPane.YES_OPTION) {	// 若否則跳出
				//若暫時步驟檔存在，將暫時步驟檔替代為步驟檔
//				if(Files.exists(Paths.get(Info.getTempStep()))) {
//					try {
//						//若暫時步驟檔存在，通常是方法選擇頁面時，使用BPNN演算法，系統自動轉換欄位所記錄的暫時步驟檔
//						CreateFile.copy(Info.getTempStep(), Info.getStep());
//					} catch (IOException ee) {
//						JOptionPane.showMessageDialog(this, "將暫時步驟檔替代為步驟檔時失敗");
//					}
//				}
				Info.showPage(Page.MAIN_PAGE);
				Info.showMessage("完成建立");
			}
		}
	}
}
