package jpatch.control.edit;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import jpatch.boundary.*;
import jpatch.entity.*;

public class AtomicAddDof extends JPatchAtomicEdit implements JPatchRootEdit {

	private RotationDof dof;
	
	public AtomicAddDof(RotationDof dof) {
		this.dof = dof;
		redo();
		//dof.setName(dof.getBone().getName() + " DOF #" + dof.getBone().getDofIndex(dof));
	}
	
	public String getName() {
		return "add degree-of-freedom";
	}

	public void undo() {
		int[] aiIndex = new int[] { dof.getParent().getIndex(dof) };
		Object[] aObject = new Object[] { dof };
		MainFrame.getInstance().getModel().removeDof(dof);
		MainFrame.getInstance().getUndoManager().setEnabled(false);
		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodesWereRemoved(dof.getParent(),aiIndex,aObject);
		MainFrame.getInstance().getUndoManager().setEnabled(true);
		//morph.setValue(0);
		//MainFrame.getInstance().getModel().applyMorphs();
		MainFrame.getInstance().getSideBar().clearDetailPanel();
//		morph.unapply();
	}
	
	public void redo() {
		MainFrame.getInstance().getModel().addDof(dof);
		int[] aiIndex = new int[] { dof.getParent().getIndex(dof) };
		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodesWereInserted(dof.getParent(),aiIndex);
		TreePath path = dof.getTreePath();
		MainFrame.getInstance().getTree().makeVisible(path);
		//morph.setValue(value);
//		morph.apply();
		//MainFrame.getInstance().getModel().applyMorphs();
	}

	public int sizeOf() {
		return 8 + 4 + 4;
	}

}
