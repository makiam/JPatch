package jpatch.control.edit;

import javax.swing.tree.*;
import jpatch.entity.*;
import jpatch.boundary.sidebar.*;
import jpatch.boundary.*;

public class RemoveMorphEdit extends JPatchAbstractUndoableEdit {
	
	private Morph morph;
	
	public RemoveMorphEdit(Morph morph) {
		this.morph = morph;
		redo();
	}
	
	public String getPresentationName() {
		return "remove morph";
	}
	
	public void redo() {
		morph.unapply();
		int[] aiIndex = new int[] { morph.getParent().getIndex(morph) };
		Object[] aObject = new Object[] { morph };
		MainFrame.getInstance().getModel().removeExpression(morph);
		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodesWereRemoved(morph.getParent(),aiIndex,aObject);
		MainFrame.getInstance().getSideBar().replacePanel(new SidePanel());
		MainFrame.getInstance().getSideBar().clearDetailPanel();
		MainFrame.getInstance().getSideBar().validate();
		//MainFrame.getInstance().getModel().applyMorphs();
	}
	
	public void undo() {
		
		MainFrame.getInstance().getModel().addExpression(morph);
		int[] aiIndex = new int[] { morph.getParent().getIndex(morph) };
		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodesWereInserted(morph.getParent(),aiIndex);
		//TreePath path = morph.getTreePath();
		
		MainFrame.getInstance().getSideBar().replacePanel(new SidePanel());
		MainFrame.getInstance().getSideBar().clearDetailPanel();
		MainFrame.getInstance().getSideBar().validate();
		morph.apply();
		//MainFrame.getInstance().getModel().applyMorphs();
		
		//MainFrame.getInstance().getTree().setSelectionPath(path);
		//MainFrame.getInstance().getTree().makeVisible(path);
	}
}
