package guiComponent;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
 
public class JAutoCompleteComboBox extends JComboBox<String> {
	private static final long serialVersionUID = 1345348800041737022L;
	private AutoCompleter completer;
 
	public JAutoCompleteComboBox() {
		super();
		addCompleter();
	}
	public JAutoCompleteComboBox(ComboBoxModel<String> cm) {	
		super(cm);
		addCompleter();
	}
	public JAutoCompleteComboBox(String[] items) {
		super(items);
		addCompleter();
	}
	private void addCompleter() {
		setEditable(true);
		setSize(50, 50);
		completer = new AutoCompleter(this);
	}
	public void autoComplete(String str) {
		completer.autoComplete(str, str.length());
	}
	public String getText() {
		return ((JTextField) getEditor().getEditorComponent()).getText();
	}
	public void setText(String text) {
		((JTextField) getEditor().getEditorComponent()).setText(text);
	}
	public boolean containsItem(String itemString) {
		ComboBoxModel<String> model = getModel();
		for (int i = 0, size = model.getSize(); i < size; i++) {
			if (model.getElementAt(i).equals(itemString))
				return true;
		}
		return false;
	}
}
