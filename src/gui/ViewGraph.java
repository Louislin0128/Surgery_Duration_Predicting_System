package gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import guiComponent.AbstractPreview;
import guiComponent.Graph;

class ViewGraph extends JFrame implements ActionListener, ItemListener {
	private static final long serialVersionUID = 890470383572123962L;
	private Logger logger = Logger.getLogger("ViewGraph");
	private JTextField[] rangeText = new JTextField[2];
	private JButton checkButton = new JButton("確定");
	private AbstractPreview sheet = AbstractPreview.newNothingSheet();
	private Graph graph = new Graph();
	private File[] listFiles;
	private File chooseFile;
	private Border normalBorder = UIManager.getBorder("TextField.border");
	private Border errorBorder = new LineBorder(Color.RED, 3);
	private String[] items = {"長條圖", "圓餅圖", "折線圖"};
	private String name = items[0];
	private JComboBox<String> fileCombo = new JComboBox<>(),
			graphCombo = new JComboBox<>(items);

	public ViewGraph() {
		setLayout(new BorderLayout(10, 10));
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setTitle("顯示統計圖表");
		setSize(1024, 768);
		setLocationRelativeTo(null);

		JLabel[] text = new JLabel[2];
		text[0] = new JLabel("選擇的檔案為：");
		text[1] = new JLabel("欲產生的圖表為：");
		
		fileCombo.addItemListener(this);
		graphCombo.addItemListener(this);

		JLabel[] rangeLabel = new JLabel[4];
		rangeLabel[0] = new JLabel("設定範圍：第", JLabel.CENTER);
		rangeLabel[1] = new JLabel("筆", JLabel.CENTER);
		rangeLabel[2] = new JLabel(" ~ 第", JLabel.CENTER);
		rangeLabel[3] = new JLabel("筆", JLabel.CENTER);
		
		rangeText[0] = new JTextField(); // 選項輸入數字
		rangeText[1] = new JTextField();
		
		checkButton.addActionListener(this);
		sheet.setBorder(LineBorder.createBlackLineBorder());
		graph.setPreferredSize(getMaximumSize());
		
		Box chooseBox = Box.createHorizontalBox();
		chooseBox.add(text[0]);
		chooseBox.add(fileCombo);
		chooseBox.add(Box.createHorizontalStrut(15));
		chooseBox.add(text[1]);
		chooseBox.add(graphCombo);
		chooseBox.add(Box.createHorizontalStrut(15));
		
		Box setBox = Box.createHorizontalBox();
		setBox.add(rangeLabel[0]); // JLabel 設定測試集為：
		setBox.add(rangeText[0]); // 輸入數字 左
		setBox.add(rangeLabel[1]); // JLabel % | 筆
		setBox.add(rangeLabel[2]); // JLabel ~
		setBox.add(rangeText[1]); // 輸入數字 右
		setBox.add(rangeLabel[3]); // JLabel %的資料 | 筆的資料
		setBox.add(checkButton);
		
		Box northBox = Box.createVerticalBox();
		northBox.add(chooseBox);
		northBox.add(Box.createVerticalStrut(10));
		northBox.add(setBox);
		add(northBox, BorderLayout.NORTH);

		JSplitPane previewPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sheet, graph);
		previewPanel.setBorder(null);
		previewPanel.setDividerLocation(previewPanel.getPreferredSize().width / 2);
		add(previewPanel, BorderLayout.CENTER);
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			if(e.getSource() == fileCombo) {
				chooseFile = listFiles[fileCombo.getSelectedIndex()];
				try {
					sheet.update(chooseFile);
					sheet.setTableTitle(fileCombo.getSelectedItem().toString());
					rangeText[0].setText("");
					rangeText[1].setText("");
					graph.update(chooseFile, name, 0, 0);
				} catch (Exception ee) {
					logger.info(ee.getMessage());
				}	
			}else if(e.getSource() == graphCombo) {
				name = graphCombo.getSelectedItem().toString(); // graph 名稱
				rangeText[0].setText("");
				rangeText[1].setText("");
				graph.update(chooseFile, name, 0, 0);
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String num1 = rangeText[0].getText();
		String num2 = rangeText[1].getText();
		int num1Int = 0;
		int num2Int = 0;
		int dataSize = sheet.getRowsCount();	// 資料總筆數(不包含標題)

		try { // 檢查輸入數字是否合法
			num1Int = Integer.parseInt(num1);
		} catch (NumberFormatException error) {
			setErrorBorder(rangeText[0], "請輸入正整數。");
			return;
		}
		try { // 檢查輸入數字是否合法
			num2Int = Integer.parseInt(num2);
		} catch (NumberFormatException error) {
			setErrorBorder(rangeText[1], "請輸入正整數。");
			return;
		}
		if (num1Int <= 0 || num1Int > dataSize) {
			setErrorBorder(rangeText[0], "請輸入正常的數值。");
			return;
		} else if (num2Int <= 0 || num2Int > dataSize) {
			setErrorBorder(rangeText[1], "請輸入正常的數值。");
			return;
		} else if (num1Int > num2Int) {
			setErrorBorder(rangeText[0], "左邊輸入格須小於等於右邊輸入格的數值。");
			return;
		} else if ((num2Int - num1Int) > 40) {
			setErrorBorder(rangeText[0], "最多只呈現40筆資料喔~~。");
			return;
		}
		graph.update(chooseFile, name, Integer.valueOf(rangeText[0].getText()),
				Integer.valueOf(rangeText[1].getText()));
	}

	private void setErrorBorder(JComponent comp, String errorMessage) {
		comp.setBorder(errorBorder);
		JOptionPane.showMessageDialog(null, errorMessage, "提示訊息", JOptionPane.WARNING_MESSAGE);
		comp.setBorder(normalBorder);
	}

	public void update(String inPath) {
		fileCombo.removeAllItems();
		listFiles = new File(inPath).listFiles();
		String fileName;
		for (int i = 0; i < listFiles.length; i++) {
			fileName = listFiles[i].getName();
			fileCombo.addItem(fileName.substring(0, fileName.length() - 4));
		}
	}
}