package gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;

import guiComponent.AbstractFileTree;
import guiComponent.AbstractPreview;
import guiComponent.ShowProgress;
import guiFunction.LoadFile;
import preprocess.FillFeature;
import preprocess.FormatTransformation;

class SelectFolderPanel extends JPanel implements ActionListener, Panel {
	private static final long serialVersionUID = -6332451822625149595L;
	private Logger logger = Logger.getLogger("SelectFolderPanel");
	private JFileChooser chooser = new JFileChooser(Info.getDesktop());
	private JButton importButton = new JButton(LoadFile.fromIcon(Info.getIconPath(), "\\folder.png", 35, 35));
	private JTextField showPath = new JTextField();
	private ShowProgress showProgress = new ShowProgress();
	private AbstractPreview sheet = AbstractPreview.newNothingSheet();
	private AbstractFileTree fileTree = new FileTree();

	public SelectFolderPanel() {
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		setLayout(new BorderLayout(10, 10));
		importButton.setToolTipText("選擇資料夾路徑");
		importButton.addActionListener(this);
		
		showPath.setEditable(false);
		Box northBox = Box.createHorizontalBox();
		northBox.add(importButton);
		northBox.add(Box.createHorizontalStrut(10));
		northBox.add(showPath);
		add(northBox, BorderLayout.NORTH);
		
		fileTree.setViewportBorder(new TitledBorder(LineBorder.createBlackLineBorder(), "目錄", TitledBorder.CENTER, TitledBorder.BELOW_TOP));
		fileTree.setPreferredSize(new Dimension(300, getHeight()));
		add(fileTree, BorderLayout.WEST);
		add(sheet, BorderLayout.CENTER);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		int choose = chooser.showOpenDialog(this);
		if (choose == JFileChooser.APPROVE_OPTION) {
			showProgress.update(new SwingWorker<Void, Void>() {
		        @Override
		        protected Void doInBackground() {
		        	try {
		        		showProgress.setText("正在建立工作目錄");
						fileTree.clear();
						//使用者選擇的資料夾 | 設定來源資料夾
						Info.setRawPath(chooser.getSelectedFile().getAbsolutePath());
						showPath.setText(Info.getRawPath());
						importButton.setEnabled(false);	// 先將資料夾按鈕關閉
						
						showProgress.setText("正在進行資料轉換");
						FormatTransformation.exec(Info.getRawPath(), Info.getWorkingData().toString());
						FillFeature.exec(Info.getOldYearPath(), Info.getNewYearPath());
						
						fileTree.update(Info.getWorkingData());
						importButton.setEnabled(true);				// 開啟資料夾按鈕
						Page.SELECT_FOLDER.setEnabled(true);
						Page.DATA_TRANSFORM.setEnabled(true);	// 將資料轉檔按鈕打開
						Info.showMessage("資料來源設定成功");
					} catch (Exception e) {
						importButton.setEnabled(true);	// 將資料夾按鈕開啟
						Page.DATA_TRANSFORM.setEnabled(false);
						logger.warning(e.getMessage());
						Info.showError("資料來源設定失敗");
						JOptionPane.showMessageDialog(SelectFolderPanel.this, "請檢查選擇資料來源", "所選資料夾未包含所需資料", JOptionPane.ERROR_MESSAGE);
					}
		            return null;
		        }
		    });
		}
	}
	
	@Override
	public void setFile() {	// 空實作
	}
	
	@Override
	public void reset() {
		showPath.setText("");
		fileTree.clear();
		sheet.clear();
	}
	
	private class FileTree extends AbstractFileTree {
		private static final long serialVersionUID = 5974961810914587782L;

		@Override
		public void valueChanged(TreeSelectionEvent e) {
			StringBuilder buildPath = new StringBuilder(Info.getWorkingData().toString());
			Object[] path = e.getPath().getPath();
			for (int i = 1; i < path.length; i++) {
				buildPath.append('\\').append(path[i]);
			}
			
			File f = new File(buildPath.toString());
			String name = f.getName();
			if (name.endsWith(".csv")) {
				sheet.setTableTitle("目前預覽檔名：" + name);
				try {
					sheet.update(f);
				} catch (Exception ee) {
					logger.info(ee.getMessage());
					Info.showError("無法讀取該檔案");
					JOptionPane.showMessageDialog(this, ee.getMessage(), "讀入檔案時，發生不可預期的錯誤!!!", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				sheet.setTableTitle("目錄：" + name);
			}
		}
	}
}