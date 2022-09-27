package guiComponent;
import java.awt.Color;
import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**在表格中顯示JPanel*/
public class PanelTable extends JTable {
	private static final long serialVersionUID = 6406210106332271542L;
	public PanelTable(AbstractTableModel model) {
		super(model);
		setRowHeight(50);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setTableHeader(null);
		TableEditorAndRenderer editAndRender = new TableEditorAndRenderer();
		setDefaultRenderer(JPanel.class, editAndRender);
		setDefaultEditor(JPanel.class, editAndRender);
	}
	
	@Override
	public void tableChanged(TableModelEvent e) {
		if(isEditing()) {
			getCellEditor().stopCellEditing();
		}
		super.tableChanged(e);
	}
	
	/**
	 * 繪製指令的JPanel(TableCellRenderer)<br>
	 * 讓JPanel可編輯(AbstractCellEditor、TableCellEditor)
	 */
	private class TableEditorAndRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
		private static final long serialVersionUID = -6788942281921154624L;
		private Color chooseColor = Color.decode("#9fa3ed");
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			// 繪製Panel
			JPanel component = (JPanel) value;
			component.setBackground(isSelected || hasFocus ? chooseColor : Color.WHITE);
			return component;
		}
		
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			// 建立Panel內物件使其可互動
			JPanel component = (JPanel) value;
			component.setBackground(isSelected ? chooseColor : Color.WHITE);
			return component;
		}

		@Override
		public Object getCellEditorValue() {
			return null;
		}
	}
}
