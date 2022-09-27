package gui;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import guiFunction.LoadFile;

class ProcessFrame extends JFrame implements PropertyChangeListener {
	private static final long serialVersionUID = -8412388088390642503L;
	private static Logger logger = Logger.getLogger("ProcessFrame");
	enum Mode{SHOW_PAGE, CLEAR_PROCESS_AND_TEMP, CLEAR_ALL, SHOWMESSAGE, SHOWERROR}
	private Border emptyBorder = new EmptyBorder(10, 10, 10, 10);
	private Font font38 = new Font("微軟正黑體", Font.PLAIN, 38);
	private Font font32 = new Font("微軟正黑體", Font.PLAIN, 32);
	private Font font30 = new Font("微軟正黑體", Font.PLAIN, 30);
	private Font font26 = new Font("微軟正黑體", Font.PLAIN, 26);
	private Font font24 = new Font("微軟正黑體", Font.PLAIN, 24);
	private Font font22 = new Font("微軟正黑體", Font.PLAIN, 22);
	private Font font20 = new Font("微軟正黑體", Font.PLAIN, 20);
	private Color sloganColor = Color.decode("#25119e");
	private Page nowPage, prePage; 	// 目前頁 | 先前頁
	private Page[] pages = Page.values();
	private int procNum = pages.length - 2;	// 頁面數量，不包含選擇頁面及主頁面
	private JTextField[] rectangle = new JTextField[procNum]; 	// 用陣列的方式來取代(因為有七個動作加一個主頁面)
	private JLabel tip = new JLabel("歡迎使用本系統"),
			slogan = new JLabel("「醫」刻千金 · 料「術」如神"),
			systemName = new JLabel("手術時間預測系統");
	private JLabel[] pageText = new JLabel[procNum]; 	// 所以每一個都用索引值來引導動作
	private String[] iconName = {"\\surgery.png", "\\manual.png", "\\chart.png"};
	private String[] imageName = {"\\Background.png", "\\TitleBackground.png"};
	private ImageIcon[] icon = LoadFile.fromIcons(Info.getIconPath(), iconName, 80, 80);
	private BufferedImage[] image = LoadFile.fromImages(Info.getIconPath(), imageName);
	// 版面宣告
	private HashMap<Page, Panel> panelMap = new HashMap<>();
	private OperatingManual operatingManual = new OperatingManual();
	private View view = new View();
	private Title title = new Title();
	private Process process = new Process();
	
	/**程式執行點*/
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				UIManager.put("OptionPane.messageDialogTitle", "似乎出了點問題");
				Font mainFont = new Font("微軟正黑體", Font.PLAIN, 20);
				UIManager.put("Label.font", mainFont);
				UIManager.put("Button.font", mainFont);
				UIManager.put("RadioButton.font", mainFont);
				UIManager.put("ComboBox.font", mainFont);
				UIManager.put("CheckBox.font", mainFont);
				UIManager.put("TextField.font", mainFont);
				UIManager.put("TextArea.font", mainFont);
				UIManager.put("ProgressBar.font", mainFont);
				UIManager.put("TitledBorder.font", mainFont);
				UIManager.put("TabbedPane.font", mainFont);
				Color mainColor = Color.decode("#e9ebfe");
				UIManager.put("info", mainColor);
				UIManager.put("control", mainColor);
				UIManager.put("Panel.background", mainColor);
				UIManager.put("RadioButton.background", mainColor);
				UIManager.put("CheckBox.background", mainColor);
				UIManager.put("TextField.background", mainColor);
				UIManager.put("TextArea.background", mainColor);
				UIManager.put("OptionPane.background", mainColor);
				UIManager.put("ComboBox.background", mainColor);
				UIManager.put("TabbedPane.background", mainColor);
				UIManager.put("Button.background", Color.decode("#e8f4ff"));
				UIManager.put("ProgressBar.background", Color.WHITE);
				UIManager.put("ProgressBar.foreground", Color.decode("#5649a5"));
//				UIManager.put("FileChooserUI", "com.sun.java.swing.plaf.windows.WindowsFileChooserUI");
				UIManager.put("SplitPane.dividerSize", 10);
				UIManager.put("FileChooser.readOnly", true);	// 將檔案選擇器設為唯讀
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
				
				new ProcessFrame();
			} catch (Exception e) {
				logger.info(e.getMessage());
			}
		});
	}
	
	/**主框架*/
	public ProcessFrame() {
		Info.addListener(this);	//新增監聽器
		
		JRootPane rootPane = new JRootPane() {
			private static final long serialVersionUID = -4902174568799098300L;
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
		        g.drawImage(image[0], 0, 0, getWidth(), getHeight(), this);
			}
		};
		rootPane.setBorder(emptyBorder);
		setRootPane(rootPane);
		setIconImage(icon[0].getImage());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setExtendedState(MAXIMIZED_BOTH); //一開始就全畫面
		setTitle("手術時間預測系統");
		setSize(1024, 768); // 縮小時的解析度
		setLocationRelativeTo(null);
		
		add(view, BorderLayout.CENTER);
		add(title, BorderLayout.NORTH); // 製作標題
		add(process, BorderLayout.SOUTH);
		addWindowStateListener((WindowEvent e) -> {
			switch(e.getNewState()) {
			case 0:
				System.out.println("縮小");
				slogan.setFont(font32);
				systemName.setFont(font22);
				tip.setFont(font26);
				for (int i = 0; i < procNum; i++)
					pageText[i].setFont(font20);
				break;
			case 6:
				System.out.println("全螢幕");
				slogan.setFont(font38);
				systemName.setFont(font26);
				tip.setFont(font30);
				for (int i = 0; i < procNum; i++)
					pageText[i].setFont(font24);
				break;
			}
		});
		setVisible(true);
	}
	
	/**顯示指定頁面*/
	private void showPage(Page page) {
		view.showPage(page);
	}
	
	/**設定系統訊息*/
	private void showMessage(String message) {
		tip.setForeground(sloganColor);
		tip.setText(message);
	}
	
	/**設定系統錯誤訊息*/
	private void showError(String message) {
		tip.setForeground(Color.RED);
		tip.setText(message);
	}
	
	/**根據監聽器傳遞的訊息，執行指定函式*/
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch((Mode) evt.getSource()) {
		case CLEAR_PROCESS_AND_TEMP://清除流程及暫存檔案
		case CLEAR_ALL:		//清除流程方框及Page列舉旗標
			//呼叫每個頁面的reset()
			panelMap.values().forEach(Panel::reset);
			//將底部流程方框設為白色
			for(int i = 0; i < procNum; i++) {
				rectangle[i].setBackground(Color.WHITE);
			}
			//重設Page列舉旗標
			for(int i = 0, length = pages.length; i < length; i++) {
				pages[i].reset();
			}
			//若流程方框仍在閃爍則取消閃爍
			if(process.twinkleBorder != null) {
				process.twinkleBorder.cancel(true);//取消閃爍
			}
			break;
		case SHOWMESSAGE:	//顯示一般訊息
			showMessage(evt.getNewValue().toString());
			break;
		case SHOWERROR:		//顯示錯誤訊息
			showError(evt.getNewValue().toString());
			break;
		case SHOW_PAGE:		//顯示頁面
			showPage((Page) evt.getNewValue());
			break;
		}
	}

	/**框架頂部*/
	private class Title extends JPanel implements ActionListener {
		private static final long serialVersionUID = 1745813210715743430L;
		private Color systemNameColor = Color.decode("#5649a5");
		private JButton logo = new JButton(icon[0]),
				help = new JButton(icon[1]);
		
		public Title() {
			setLayout(new BorderLayout(10, 0));
			logo.setFocusPainted(false);
			logo.setBorderPainted(false);
			logo.setContentAreaFilled(false);
			logo.setToolTipText("返回主頁面");
			logo.addActionListener(this);
			add(logo, BorderLayout.WEST);
			
			slogan.setFont(font38);
			slogan.setForeground(sloganColor);
			systemName.setFont(font26);
			systemName.setForeground(systemNameColor);
			tip.setFont(font30);
			tip.setForeground(sloganColor);
			tip.setAlignmentX(RIGHT_ALIGNMENT);
			
			Box verticalBox = Box.createVerticalBox();
			verticalBox.add(slogan);
			verticalBox.add(systemName);
			verticalBox.add(Box.createVerticalGlue());
			Box centerBox = Box.createHorizontalBox();
			centerBox.add(verticalBox);
			centerBox.add(Box.createHorizontalGlue());
			centerBox.add(tip);
			add(centerBox, BorderLayout.CENTER);
			
			help.setFocusPainted(false);
			help.setBorderPainted(false);
			help.setContentAreaFilled(false);
			help.addActionListener(this);
			help.setAlignmentX(RIGHT_ALIGNMENT);
			help.setToolTipText("使用手冊");
			add(help, BorderLayout.EAST);
		}
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
	        g.drawImage(image[1], 0, 0, getWidth(), getHeight(), this);
		}

		/**按下logo按鈕*/
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == logo && nowPage != Page.CHOOSE) {
				int choose = JOptionPane.showConfirmDialog(ProcessFrame.this, "確定捨棄目前的工作進度嗎？", "是否回到主頁面", JOptionPane.YES_NO_OPTION);
				if(choose == JOptionPane.YES_OPTION) {	// 若否則跳出
					Info.clearAll();
					showPage(Page.CHOOSE);
				}
			}else if (e.getSource() == help) {
				operatingManual.setVisible(true);
			}
		}
	}
	
	/**主版面*/
	private class View extends JPanel {
		private static final long serialVersionUID = 2606502395233174342L;
		private Color hasProcessedColor = Color.decode("#a6a9e6");
		private Color isProcessingColor = Color.decode("#3c4074");
		private CardLayout card = new CardLayout(); // 用來切換頁面的
		
		public View() {
			setLayout(card);
			
			String pageName;	// 將建立Panel的過程包裝起來，避免繁雜
			Panel panel;		// 頁面建置順序與Page列舉順序有關
			for(Page page: pages) {
				panel = newPage(page);
				pageName = page.getName();
				panelMap.put(page, panel);
				add((Component) panel, pageName);
				System.out.println(pageName);
			}
			showPage(Page.CHOOSE);
		}
		
		private Panel newPage(Page page) {
			switch(page) {
			case CHOOSE:
				return new ChoosePanel();
			case DATA_HANDLE:
				return new DataHandlePanel();
			case DATA_SPLIT:
				return new DataSplitPanel();
			case DATA_TRANSFORM:
				return new DataTransformPanel();
			case FEATURE_SELECT:
				return new FeatureSelectPanel();
			case MAIN_PAGE:
				return new MainPagePanel();
			case METHOD_SELECT:
				return new MethodSelectPanel();
			case SELECT_FOLDER:
				return new SelectFolderPanel();
			case TRAIN_RESULT:
				return new TrainResultPanel();
			}
			throw new IllegalArgumentException("參數未定義");
		}
		
		private void showPage(Page page) {
			card.show(this, page.getName());
			if(page == Page.CHOOSE) {	// 如果要求顯示流程選擇頁面，將prePage及nowPage設為Page.CHOOSE
				prePage = nowPage = Page.CHOOSE;
			}else {
				prePage = nowPage;
				nowPage = page;
			}
			
			// 設定上個頁面按鈕顏色
			switch(prePage) {
			case CHOOSE:
			case MAIN_PAGE:
				break;
			default:
				rectangle[prePage.ordinal()].setBackground(hasProcessedColor);
				break;				
			}
			// 設定本頁面按鈕顏色
			switch(nowPage) {
			case CHOOSE:
			case MAIN_PAGE:
				break;
			default:
				rectangle[nowPage.ordinal()].setBackground(isProcessingColor);
				break;				
			}
			//呼叫該頁面的setFile方法
			panelMap.get(page).setFile();
		}
	}
	
	/**框架底部*/
	private class Process extends JPanel implements PropertyChangeListener {
		private static final long serialVersionUID = -1881550063813242073L;
		private SwingWorker<Void, Void> twinkleBorder;
		private LineBorder redBorder = new LineBorder(Color.RED, 3);
		private EmptyBorder boxBorder = new EmptyBorder(3, 3, 3, 3);
		private Box[] procBox = new Box[procNum];
		
		public Process() {
			setLayout(new GridLayout(1, procNum, 10, 0));
			setBackground(Color.decode("#b1b5fd"));
			setBorder(emptyBorder);
			
			for(int i = 0; i < procNum; i++) {
				pageText[i] = new JLabel(pages[i].getName());
				pageText[i].setFont(font24);
				pageText[i].setAlignmentX(CENTER_ALIGNMENT);
				rectangle[i] = new JTextField();
				rectangle[i].setEditable(false);
				rectangle[i].setBackground(Color.WHITE);
				
				procBox[i] = Box.createVerticalBox();
				procBox[i].setBorder(boxBorder);
				procBox[i].add(pageText[i]);
				procBox[i].add(rectangle[i]);
				procBox[i].addMouseListener(pageListener);
				add(procBox[i]);
				
				pages[i].addListener(this);//加入每個頁面的監聽器
			}
		}
		
		private boolean twinkle = true;
		/**設定頁面開啟時，會啟動監聽器，以顯示紅框，提醒使用者*/
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			Page page = (Page) evt.getSource();
//			if((boolean) evt.getNewValue() && nowPage != page) {	// 若將頁面設為啟用並且設定頁面與目前頁面不相同
			if((boolean) evt.getNewValue()) {	// 若將頁面設為啟用
				twinkleBorder = new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() throws Exception {
						twinkle = true;
						while(twinkle) {
							setBoxBorder(page, redBorder);
							Thread.sleep(500);
							setBoxBorder(page, boxBorder);
							Thread.sleep(500);
						}
						return null;
					}
					
					@Override
					protected void done() {
						setBoxBorder(page, boxBorder);	//程式結束執行時，將邊框還原
					}
				};
				twinkleBorder.execute();
			}else {	// 若將頁面設為禁用
				if(twinkleBorder != null) {
					twinkle = false;	//取消閃爍 將邊框設為原本
				}
			}
		}
		
		/**設定流程方框顏色*/
		private void setBoxBorder(Page page, Border border) {
			switch(page) {
			case DATA_TRANSFORM:
				procBox[1].setBorder(border);
				break;
			case DATA_HANDLE:
				procBox[2].setBorder(border);
				break;
			case FEATURE_SELECT:
				procBox[3].setBorder(border);
				break;
			case DATA_SPLIT:
				procBox[4].setBorder(border);
				break;
			case METHOD_SELECT:
				procBox[5].setBorder(border);
				break;
			case TRAIN_RESULT:
				procBox[6].setBorder(border);
				break;
			default:
				break;
			}
		}
		
		/**進度條的動作*/
		private MouseAdapter pageListener = new MouseAdapter() {
			private Component comp;
			private Page page;
			@Override
			public void mouseClicked(MouseEvent e) {
				comp = e.getComponent();
				for(int i = 0; i < procNum; i++) {
					if(comp == procBox[i]) {
						// 若點選的頁面未開放，則跳出
						page = pages[i];
						if(page.isEnabled()) {
							page.enteredPage();
							twinkleBorder.cancel(true);	//取消閃爍 將邊框設為原本
							showPage(page);
							showMessage("目前頁面為「" + page.getName() + "」");
							break;
						}
					}
				}
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				comp = e.getComponent();
				for(int i = 0; i < procNum; i++) {
					if(comp == procBox[i]) {
						page = pages[i];
						if(page.isEnabled()) {
							comp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
						}
						break;
					}
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				comp.setCursor(Cursor.getDefaultCursor());
			}
		};
	}
}