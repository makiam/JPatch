package jpatch.control.edit;

import javax.swing.tree.*;
import jpatch.boundary.*;

public class AtomicAddSelection extends JPatchAtomicEdit implements JPatchRootEdit {
	
	private Selection selection;
	private int index = -1;
	
	public AtomicAddSelection(Selection selection) {
		this.selection = selection;
		redo();
	}
	
	public AtomicAddSelection(int index, Selection selection) {
		this.index = index;
		this.selection = selection;
		redo();
	}
	
	public String getName() {
		return "add selection";
	}
	
	public void undo() {
//		int[] aiIndex = new int[] { selection.getParent().getIndex(selection) };
//		Object[] aObject = new Object[] { selection };
		MainFrame.getInstance().getModel().removeSelection(selection);
		//System.out.println("<");
		//TreePath path = selection.getParent().getTreePath();
		//MainFrame.getInstance().getTree().setSelectionPath(path);
//		MainFrame.getInstance().getUndoManager().setEnabled(false);
//		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodesWereRemoved(selection.getParent(),aiIndex,aObject);
//		MainFrame.getInstance().getUndoManager().setEnabled(true);
		//System.out.println(">");
	}
	
	public void redo() {
		if (index < 0)
			MainFrame.getInstance().getModel().addSelection(selection);
		else
			MainFrame.getInstance().getModel().addSelection(index, selection);
//		int[] aiIndex = new int[] { selection.getParent().getIndex(selection) };
//		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodesWereInserted(selection.getParent(),aiIndex);
//		TreePath path = selection.getTreePath();
//		//MainFrame.getInstance().getTree().setSelectionPath(path);
//		MainFrame.getInstance().getTree().makeVisible(path);
	}
	
	public int sizeOf() {
		return 8 + 4 + 4;
	}
}
