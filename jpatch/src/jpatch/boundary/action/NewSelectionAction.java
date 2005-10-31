package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.control.edit.*;

public final class NewSelectionAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static int iNum = 1;
	//private Model model;

	public NewSelectionAction(Model model) {
		super("Add Current Selection");
		//this.model = model;
		//putValue(Action.SHORT_DESCRIPTION,"Add Controlpoint [A]");
		//MainFrame.getInstance().getKeyEventDispatcher().setKeyActionListener(this,KeyEvent.VK_A);
	}
	public void actionPerformed(ActionEvent actionEvent) {
		//System.out.println("add selection");
		//Selection selection = new PointSelection((PointSelection) MainFrame.getInstance().getSelection());
		//System.out.println("1");
		//if (selection != null && model.addSelection(selection)) {
		//	selection.setName("new selection #" + iNum++);
		//	int[] aiIndex = new int[] { selection.getParent().getIndex(selection) };
		//	((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodesWereInserted(selection.getParent(),aiIndex);
		//	TreePath path = selection.getTreePath();
		//	MainFrame.getInstance().getTree().setSelectionPath(path);
		//	MainFrame.getInstance().getTree().makeVisible(path);
		//}
		//MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
		//MainFrame.getInstance().getJPatchScreen().addMouseListeners(new AddControlPointMouseAdapter());
		//MainFrame.getInstance().clearDialog();
		
//		if (selection != null) {
//			if (MainFrame.getInstance().getModel().checkSelection(selection)) {
//				selection.setName("new selection #" + iNum++);
//				JPatchUndoableEdit edit = new AddSelectionEdit(selection);
//				MainFrame.getInstance().getUndoManager().addEdit(edit);
//			} else {
//				MainFrame.getInstance().getUndoManager().setEnabled(false);
//				MainFrame.getInstance().getTree().setSelectionPath(MainFrame.getInstance().getModel().getSelection(selection).getTreePath());
//				MainFrame.getInstance().getUndoManager().setEnabled(true);
//			}
//		}
		
		Selection selection = MainFrame.getInstance().getSelection();
		if (selection != null) {
			selection = selection.cloneSelection();
			if (MainFrame.getInstance().getModel().checkSelection(selection)) {
				selection.setName("new selection #" + iNum++);
				MainFrame.getInstance().getUndoManager().addEdit(new AtomicAddSelection(selection));
			}
			MainFrame.getInstance().getSideBar().enableTreeSelectionListener(false);
			MainFrame.getInstance().selectTreeNode(selection);
//			MainFrame.getInstance().getTree().setSelectionPath(MainFrame.getInstance().getModel().getSelection(selection).getTreePath());
			MainFrame.getInstance().getSideBar().enableTreeSelectionListener(true);
		}
	}
}

