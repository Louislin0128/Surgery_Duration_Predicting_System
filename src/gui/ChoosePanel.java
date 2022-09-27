package gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import guiComponent.PaintImage;
import guiFunction.LoadFile;

/**
 * 供使用者選擇要從哪個頁面開始
 */
class ChoosePanel extends JPanel implements ItemListener, ActionListener, Panel {
	private static final long serialVersionUID = -5069756136400336131L;
	private String[] imageName = { "\\3. 選擇資料夾路徑\\選擇資料來源.png", "\\5. 資料處理\\資料處理.png", "\\6. 特徵選取\\特徵選取.png",
			"\\7. 資料拆分\\資料拆分.png", "\\8. 方法設定\\方法設定.png", "\\2. 主頁面\\預測主頁面.png" };	
	private String[] tipName = { "\\3. 選擇資料夾路徑\\如何操作頁面.txt", "\\5. 資料處理\\如何操作頁面.txt", "\\6. 特徵選取\\如何操作頁面.txt",
			"\\7. 資料拆分\\如何操作頁面.txt", "\\8. 方法設定\\如何操作頁面.txt", "\\2. 主頁面\\如何操作頁面.txt" };
	private String[] tipText = LoadFile.fromTexts(Info.getManualPath(), tipName);
	private BufferedImage[] img = LoadFile.fromImages(Info.getManualPath(), imageName);
	private Page startPage = Page.SELECT_FOLDER; // 預設從選擇資料夾頁面開始
	private JTextArea tip = new JTextArea();
	private PaintImage paintImage = new PaintImage();
	private String[] name = { "完整模式", "資料處理", "特徵選取", "資料拆分", "方法設定", "載入模型" };
	private int nameLength = name.length;
	private JRadioButton[] choose = new JRadioButton[nameLength];

	public ChoosePanel() {
		setLayout(new BorderLayout(10, 10));
		setPreferredSize(new Dimension(1000, 700));
		
		Box chooseBox = Box.createHorizontalBox();
		add(chooseBox, BorderLayout.NORTH);

		JLabel label = new JLabel("請選擇要從哪個流程開始：");
		chooseBox.add(label);
		ButtonGroup group = new ButtonGroup();
		for (int i = 0; i < nameLength; i++) {
			choose[i] = new JRadioButton(name[i]);
			choose[i].addItemListener(this);
			group.add(choose[i]);
			chooseBox.add(choose[i]);
		}
		
		JButton confirm = new JButton("確定");
		confirm.addActionListener(this);
		chooseBox.add(Box.createHorizontalGlue());
		chooseBox.add(confirm);
		
		tip.setEditable(false);
		tip.setLineWrap(true);
		choose[0].setSelected(true);
		
		JScrollPane tipScroll = new JScrollPane(tip);
		tipScroll.setViewportBorder(new TitledBorder(null, "此流程的詳細資訊", TitledBorder.CENTER, TitledBorder.BELOW_TOP));
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(paintImage), tipScroll);
		splitPane.setDividerLocation(splitPane.getPreferredSize().width / 2);
		add(splitPane, BorderLayout.CENTER);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (ItemEvent.SELECTED == e.getStateChange()) {
			if (choose[0] == e.getSource()) { 	// 完整模式
				tip.setText(tipText[0]);
				paintImage.update(img[0]);
				startPage = Page.SELECT_FOLDER;
			} else if (choose[1] == e.getSource()) { // 資料處理
				tip.setText(tipText[1]);
				paintImage.update(img[1]);
				startPage = Page.DATA_HANDLE;
			} else if (choose[2] == e.getSource()) { // 特徵選取
				tip.setText(tipText[2]);
				paintImage.update(img[2]);
				startPage = Page.FEATURE_SELECT;
			} else if (choose[3] == e.getSource()) { // 資料拆分
				tip.setText(tipText[3]);
				paintImage.update(img[3]);
				startPage = Page.DATA_SPLIT;
			} else if (choose[4] == e.getSource()) { // 方法設定
				tip.setText(tipText[4]);
				paintImage.update(img[4]);
				startPage = Page.METHOD_SELECT;
			} else {
				tip.setText(tipText[5]);
				paintImage.update(img[5]);
				startPage = Page.MAIN_PAGE;
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (startPage) {
		case SELECT_FOLDER:
			Page.SELECT_FOLDER.enteredPage();
			Info.showPage(Page.SELECT_FOLDER);
			break;
		case DATA_HANDLE:
			Page.DATA_HANDLE.enteredPage();
			Info.showPage(Page.DATA_HANDLE);
			break;
		case FEATURE_SELECT:
			Page.FEATURE_SELECT.enteredPage();
			Page.DATA_HANDLE.setEnabled(false);
			Info.showPage(Page.FEATURE_SELECT);
			break;
		case DATA_SPLIT:
			Page.DATA_SPLIT.enteredPage();
			Page.DATA_HANDLE.setEnabled(false);
			Page.FEATURE_SELECT.setEnabled(false);
			Info.showPage(Page.DATA_SPLIT);
			break;
		case METHOD_SELECT:
			Page.METHOD_SELECT.enteredPage();
			Page.DATA_HANDLE.setEnabled(false);
			Page.FEATURE_SELECT.setEnabled(false);
			Page.DATA_SPLIT.setEnabled(false);
			Info.showPage(Page.METHOD_SELECT);
			break;
		case MAIN_PAGE:
			Page.MAIN_PAGE.enteredPage();
			Info.showPage(Page.MAIN_PAGE);
			break;
		default:
			break;
		}
	}

	@Override
	public void setFile() {	// 空實作
	}

	@Override
	public void reset() {
		choose[0].setSelected(true);
	}
}