package guiComponent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField; 

class AutoCompleter extends KeyAdapter implements ItemListener {
	private Logger logger = Logger.getLogger("AutoCompleter");
	private JComboBox<String> owner;
	private JTextField editor;
	private ComboBoxModel<String> model;
	AutoCompleter(JComboBox<String> comboBox) {
		editor = (JTextField) comboBox.getEditor().getEditorComponent();
		editor.addKeyListener(this);
		model = comboBox.getModel();
		owner = comboBox;
		owner.addItemListener(this);
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		char ch = e.getKeyChar();
		if (ch == KeyEvent.CHAR_UNDEFINED ||
			Character.isISOControl(ch) ||
			ch == KeyEvent.VK_DELETE)
			return;
		int caretPosition = editor.getCaretPosition();
		String str = editor.getText();
		if (!str.isEmpty()) {
			autoComplete(str, caretPosition);
		}
	}
	
	protected void autoComplete(String strf, int caretPosition) {
		String[] opts;
		opts = getMatchingOptions(strf.substring(0, caretPosition));
		if (owner != null) {
			model = new DefaultComboBoxModel<String>(opts);
			owner.setModel(model);
		}
		if (opts.length > 0) {
			editor.setCaretPosition(caretPosition);
			if (owner != null) {
				try {
					owner.showPopup();
				} catch (Exception e) {
					logger.info(e.getMessage());
				}
			}
		}
	}
	
	protected String[] getMatchingOptions(String str) {
		List<String> v = new Vector<>();
		List<String> v1 = new Vector<>();
		for (int k = 0; k < model.getSize(); k++) {
			Object itemObj = model.getElementAt(k);
			if (itemObj != null) {
				String item = itemObj.toString().toLowerCase();
				if (item.startsWith(str.toLowerCase()))
					v.add(model.getElementAt(k));
				else
					v1.add(model.getElementAt(k));
			} else
				v1.add(model.getElementAt(k));
		}
		for (int i = 0; i < v1.size(); i++) {
			v.add(v1.get(i));
		}
		if (v.isEmpty())
			v.add(str);
		return v.toArray(new String[v.size()]);
	}
	
	@Override
	public void itemStateChanged(ItemEvent event) {
		if (event.getStateChange() == ItemEvent.SELECTED) {
			int caretPosition = editor.getCaretPosition();
			if (caretPosition != -1) {
				try {
					editor.moveCaretPosition(caretPosition);
				} catch (IllegalArgumentException e) {
					logger.info(e.getMessage());
				}
			}
		}
	}
}
