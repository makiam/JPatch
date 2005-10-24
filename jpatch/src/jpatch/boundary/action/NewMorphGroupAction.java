//package jpatch.boundary.action;
//
//import java.awt.event.*;
//import javax.swing.*;
//import javax.swing.tree.*;
//import jpatch.boundary.*;
//
//public final class NewMorphGroupAction extends AbstractAction {
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = 1L;
//	JPatchTreeNode treeNode;
//	public NewMorphGroupAction(JPatchTreeNode node) {
//		super("New Group");
//		treeNode = node;
//		//putValue(Action.SHORT_DESCRIPTION,"Add Controlpoint [A]");
//		//MainFrame.getInstance().getKeyEventDispatcher().setKeyActionListener(this,KeyEvent.VK_A);
//	}
//	public void actionPerformed(ActionEvent actionEvent) {
//		JPatchTreeNode group = new JPatchTreeNode(JPatchTreeNode.MORPHGROUP,treeNode,"New Group");
//		//treeNode.add(group);
//		int[] aiIndex = new int[] { treeNode.getIndex(group) };
//		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodesWereInserted(treeNode,aiIndex);
//		TreePath path = group.getTreePath();
//		MainFrame.getInstance().getTree().makeVisible(path);
//		//MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
//		//MainFrame.getInstance().getJPatchScreen().addMouseListeners(new AddControlPointMouseAdapter());
//		//MainFrame.getInstance().clearDialog();
//	}
//}
//
