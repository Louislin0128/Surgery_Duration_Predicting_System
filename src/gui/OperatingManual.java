package gui;
import java.awt.BorderLayout;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;

import guiComponent.AbstractFileTree;
import guiComponent.PaintImage;
import guiFunction.LoadFile;

/**操作手冊*/
class OperatingManual extends JFrame {
	private static final long serialVersionUID = -819745036588847596L;
	private Logger logger = Logger.getLogger("OperatingManual");
	private JTextArea textArea = new JTextArea();
	private PaintImage paintImage = new PaintImage();
	
	public OperatingManual() {
		setIconImage(LoadFile.fromImage(Info.getIconPath(), "\\surgery.png"));
		setTitle("操作手冊");
		setLayout(new BorderLayout(10, 10));
		setSize(1024, 768);
		getRootPane().setBorder(new EmptyBorder(5, 5, 5, 5));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		
		FileTree fileTree = new FileTree();
		try {
			fileTree.update(Info.getManualPath());
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		
		add(fileTree, BorderLayout.WEST);
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(textArea), new JScrollPane(paintImage));
		splitPane.setDividerLocation(getPreferredSize().height / 2);
		add(splitPane, BorderLayout.CENTER);
	}
	
	/**繼承{@link AbstractFileTree}*/
	private class FileTree extends AbstractFileTree {
		private static final long serialVersionUID = -4638780198419775864L;
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			StringBuilder buildPath = new StringBuilder(Info.getManualPath());
			Object[] path = e.getPath().getPath();
			for (int i = 1, length = path.length; i < length; i++) {
				buildPath.append('\\').append(path[i]);
			}
			String fileName = buildPath.toString();
			if (fileName.endsWith(".txt")) {
				// 將顯示區域設定為使用者選擇的檔案讀入的文字
				textArea.setText(LoadFile.fromText(fileName));
			}else if (fileName.endsWith(".png") || fileName.endsWith(".jpg")) {
				try {
					paintImage.update(fileName);
				} catch (IOException ee) {
					Info.showError("無法讀取該圖片");
					JOptionPane.showMessageDialog(this, ee.getMessage(), "讀入檔案時，發生不可預期的錯誤!!!", JOptionPane.ERROR_MESSAGE);
				}
			}else {	//目錄
				textArea.setText(fileName);
			}
		}
	}
}
