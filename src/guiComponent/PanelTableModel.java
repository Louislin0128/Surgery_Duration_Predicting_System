package guiComponent;
import java.util.Collection;
import java.util.Iterator;
import java.util.RandomAccess;
import java.util.Vector;
import javax.swing.JPanel;
import javax.swing.table.AbstractTableModel;

/**於JTable顯示JPanel的列表*/
public class PanelTableModel<E> extends AbstractTableModel implements Collection<E>, RandomAccess {
	private static final long serialVersionUID = 8065272017293296892L;
	private Vector<E> panelVector = new Vector<>();
	
	@Override
	public E getValueAt(int rowIndex, int columnIndex) {
		return panelVector.get(rowIndex);
	}

	@Override
	public boolean isCellEditable(int columnIndex, int rowIndex) {
		return true;
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public int getRowCount() {
		return panelVector.size();
	}

	@Override
	public String getColumnName(int columnIndex) {
		return null;
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return JPanel.class;
	}
	
	@Override
	public Iterator<E> iterator() {
		return panelVector.iterator();
	}
	
	@Override
	public boolean add(E panel) {
		boolean b = panelVector.add(panel);
		fireTableDataChanged();
		return b;
	}
	
	@Override
	public boolean remove(Object panel) {
		boolean b = panelVector.remove(panel);
		fireTableDataChanged();
		return b;
	}
	
	public void remove(int index) {
		panelVector.remove(index);
		fireTableDataChanged();
	}
	
	@Override
	public void clear() {
		panelVector.clear();
		fireTableDataChanged();
	}
	
	/**
	 * 插入版面<br>
	 * {@link Vector#insertElementAt(Object, int)}
	 */
	public void insertAt(E panel, int index) {
		panelVector.insertElementAt(panel, index);
		fireTableDataChanged();
	}
	
	/**
	 * 得到版面<br>
	 * {@link Vector#get(int)}
	 */
	public E get(int index) {
		return panelVector.get(index);
	}
	
	@Override
	public boolean isEmpty() {
		return panelVector.isEmpty();
	}

	@Override
	public int size() {
		return panelVector.size();
	}

	@Override
	public boolean contains(Object o) {
		return panelVector.contains(o);
	}

	@Override
	public Object[] toArray() {
		return panelVector.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return panelVector.toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return panelVector.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return panelVector.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return panelVector.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return panelVector.retainAll(c);
	}
}
