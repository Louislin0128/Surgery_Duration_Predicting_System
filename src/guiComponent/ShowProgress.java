package guiComponent;
import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

public class ShowProgress extends JDialog implements PropertyChangeListener {
	private static final long serialVersionUID = 8583132355797718616L;
	private JProgressBar bar;
	private SwingWorker<Void, Void> swingWorker;

	public ShowProgress() {
		bar = new JProgressBar();
		bar.setStringPainted(true);
		bar.setIndeterminate(true);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		setAlwaysOnTop(true);
		setUndecorated(true);
		setContentPane(bar);
		setSize(300, 70);
		setLocationRelativeTo(null);
	}

	public void update(SwingWorker<Void, Void> swingWorker) {
		if(this.swingWorker != null) {		//若之前還有工作，就直接中斷
			this.swingWorker.cancel(true);	//由新的程序繼續執行
		}
		this.swingWorker = swingWorker;
		this.swingWorker.addPropertyChangeListener(this);
		setVisible(true);
		this.swingWorker.execute();
	}
		
	public void setText(String text) {
		bar.setString(text);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("state")) {
			if (swingWorker.isDone()) {
				dispose();
			}
		}
	}
}
