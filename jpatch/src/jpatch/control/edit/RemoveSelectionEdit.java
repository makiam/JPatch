package jpatch.control.edit;

import javax.swing.tree.*;
import jpatch.boundary.selection.*;
import jpatch.boundary.sidebar.*;
import jpatch.boundary.*;

public class RemoveSelectionEdit extends JPatchAbstractUndoableEdit {
	
	private Selection selection;
	private int index = -1;
	
	public RemoveSelectionEdit(Selection selection) {
		this.selection = selection;
		redo();
	}
	
	public String getPresentationName() {
		return "remove selection";
	}
	
	public void redo() {
		int[] aiIndex = new int[] { selection.getParent().getIndex(selection) };
		Object[] aObject = new Object[] { selection };
		MainFrame.getInstance().getModel().removeSelection(selection);
		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodesWereRemoved(selection.getParent(),aiIndex,aObject);
		MainFrame.getInstance().getSideBar().replacePanel(new SidePanel());
		MainFrame.getInstance().getSideBar().clearDetailPanel();
		MainFrame.getInstance().getSideBar().validate();
	}
	
	public void undo() {
		if (index < 0) 
			MainFrame.getInstance().getModel().addSelection(selection);
		else
			// undo at deleted position ( not yet implemented @lois )
			MainFrame.getInstance().getModel().addSelection(index, selection);
		int[] aiIndex = new int[] { selection.getParent().getIndex(selection) };
		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodesWereInserted(selection.getParent(),aiIndex);
		//TreePath path = selection.getTreePath();
		
		MainFrame.getInstance().getSideBar().replacePanel(new SidePanel());
		MainFrame.getInstance().getSideBar().clearDetailPanel();
		MainFrame.getInstance().getSideBar().validate();
		//MainFrame.getInstance().getTree().setSelectionPath(path);
		//MainFrame.getInstance().getTree().makeVisible(path);
	}
}
