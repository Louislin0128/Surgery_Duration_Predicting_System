package gui;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * 定義頁面名稱及其是否開啟
 */
enum Page {
	/**設定資料目錄*/
	SELECT_FOLDER("設定資料目錄"),
	/**資料轉檔*/
	DATA_TRANSFORM("資料轉檔"),
	/**資料處理*/
	DATA_HANDLE("資料處理"),
	/**特徵選取*/
	FEATURE_SELECT("特徵選取"),
	/**資料拆分*/
	DATA_SPLIT("資料拆分"),
	/**方法設定*/
	METHOD_SELECT("方法設定"),
	/**訓練結果*/
	TRAIN_RESULT("訓練結果"),
	/**預測主頁面*/
	MAIN_PAGE("預測主頁面"),
	/**流程選擇*/
	CHOOSE("流程選擇");
	
	/**該頁面名稱*/
	private String name;
	/**是否啟用該頁面*/
	private boolean enabled;
	/**本次流程首次進入的旗標*/
	private boolean firstEnter;
	/**監聽器*/
	private PropertyChangeSupport p;
	private Page(String name) {
		this.name = name;
		this.enabled = false;
		this.firstEnter = true;
		this.p = new PropertyChangeSupport(this);
	}
	
	/**取得頁面名稱*/
	public String getName() {
		return name;
	}
	
	/**取得頁面是否啟用*/
	public boolean isEnabled() {
		return enabled;
	}
	
	/**設定監聽器*/
	public void addListener(PropertyChangeListener listener) {
		p.addPropertyChangeListener(listener);
	}
	
	/**使用者已進入該頁面，設定旗標*/
	public void enteredPage() {
		this.firstEnter = false;
	}
	
	/**設定頁面是否啟用*/
	public void setEnabled(boolean b) {
		//如果該頁面為首次進入，啟動監聽器
		System.out.println("將" + name + "頁面設為：" + b);
		if(firstEnter || !b) {	//若首次進入或設為禁用，啟動監聽器
			p.firePropertyChange(name, enabled, b);
		}
		this.enabled = b;
	}
	
	/**重設頁面旗標*/
	public void reset() {
		setEnabled(false);
		this.firstEnter = true;
	}
}
