package guiComponent;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Stack;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

/**檔案樹的抽象類，須實作點選任一項目執行的動作*/
public abstract class AbstractFileTree extends JScrollPane implements TreeSelectionListener, FileVisitor<Path> {
	private static final long serialVersionUID = 8137115178579328106L;
	private Font font16 = new Font("微軟正黑體", Font.PLAIN, 16);
	private int defaultRowHeight = font16.getSize() + 10;
	private JTree tree;

	protected AbstractFileTree() {
		tree = new JTree();
		clear();
		tree.setFont(font16);
		tree.setRowHeight(defaultRowHeight);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(this);
		setViewportView(tree);
	}

	/**
	 * 更新檔案樹
	 * @param folder 字串路徑
	 * @throws IOException
	 */
	public void update(String folder) throws IOException {
		update(Paths.get(folder));
	}
	
	/**
	 * 更新檔案樹
	 * @param folder Path路徑
	 * @throws IOException
	 */
	public void update(Path folder) throws IOException {
		Files.walkFileTree(folder, this);
		((DefaultTreeModel) tree.getModel()).setRoot(folderNode);
	}
	
	/**清除檔案樹*/
	public void clear() {
		((DefaultTreeModel) tree.getModel()).setRoot(null);
	}
	
	private Stack<DefaultMutableTreeNode> nodeStack = new Stack<>();// 目錄
	private Stack<ArrayList<String>> fileStack = new Stack<>();		// 檔案
	private DefaultMutableTreeNode folderNode;
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		folderNode = new DefaultMutableTreeNode(dir.getFileName());
		if(!nodeStack.isEmpty()) {
			nodeStack.peek().add(folderNode);
		}
		nodeStack.push(folderNode);
		fileStack.push(new ArrayList<>());
		return FileVisitResult.CONTINUE;
	}
	
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		fileStack.peek().add(file.getFileName().toString());
		return FileVisitResult.CONTINUE;
	}
	
	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		folderNode = nodeStack.pop();
		ArrayList<String> fileList = fileStack.pop();
		fileList.forEach(s -> folderNode.add(new DefaultMutableTreeNode(s, false)));
		fileList.clear();
		return FileVisitResult.CONTINUE;
	}
	
	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		System.err.println(exc);
		return FileVisitResult.CONTINUE;
	}
}
