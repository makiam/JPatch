package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.sidebar.*;
import jpatch.entity.*;
import jpatch.control.edit.*;

public final class NewMorphAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static int iNum = 1;
	
	//private JPatchTreeNode treeNode;
	
	public NewMorphAction(JPatchTreeNode node) {
		super("New Morph");
		//treeNode = node;
		//putValue(Action.SHORT_DESCRIPTION,"Add Controlpoint [A]");
		//MainFrame.getInstance().getKeyEventDispatcher().setKeyActionListener(this,KeyEvent.VK_A);
	}
	public void actionPerformed(ActionEvent actionEvent) {
		Morph morph = new Morph(Morph.MORPH,"new morph #" + iNum++);
		//MainFrame.getInstance().getModel().addExpression(morph);
		////treeNode.add(morph);
		//int[] aiIndex = new int[] { treeNode.getIndex(morph) };
		//((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodesWereInserted(treeNode,aiIndex);
		//TreePath path = morph.getTreePath();
		//MainFrame.getInstance().getTree().makeVisible(path);
		//MainFrame.getInstance().getTree().validate();
		//MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
		//MainFrame.getInstance().getJPatchScreen().addMouseListeners(new AddControlPointMouseAdapter());
		//MainFrame.getInstance().clearDialog();
		MainFrame.getInstance().getUndoManager().addEdit(new AddMorphEdit(morph));
		MainFrame.getInstance().getTree().setSelectionPath(morph.getTreePath());
		((MorphPanel) MainFrame.getInstance().getSideBar().getSidePanel()).edit();
	}
}

