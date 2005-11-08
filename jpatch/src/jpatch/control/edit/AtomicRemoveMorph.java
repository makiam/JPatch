package jpatch.control.edit;

import javax.swing.tree.*;
import jpatch.entity.*;
import jpatch.boundary.sidebar.*;
import jpatch.boundary.*;

public class AtomicRemoveMorph extends JPatchAtomicEdit implements JPatchRootEdit {
	
	private Morph morph;
	
	public AtomicRemoveMorph(Morph morph) {
		this.morph = morph;
		redo();
	}
	
	public String getName() {
		return "delete morph";
	}
	
	public void redo() {
//		morph.unapply();
//		int[] aiIndex = new int[] { morph.getParent().getIndex(morph) };
//		Object[] aObject = new Object[] { morph };
		MainFrame.getInstance().getModel().removeExpression(morph);
//		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodesWereRemoved(morph.getParent(),aiIndex,aObject);
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
//		morph.apply();
		//MainFrame.getInstance().getModel().applyMorphs();
		
		//MainFrame.getInstance().getTree().setSelectionPath(path);
		//MainFrame.getInstance().getTree().makeVisible(path);
	}
	
	public int sizeOf() {
		return 8 + 4;
	}
}
