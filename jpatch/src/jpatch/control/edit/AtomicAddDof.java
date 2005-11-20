package jpatch.control.edit;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import jpatch.boundary.*;
import jpatch.entity.*;

public class AtomicAddDof extends JPatchAtomicEdit implements JPatchRootEdit {

	private RotationDof dof;
	private Bone bone;

	public AtomicAddDof(RotationDof dof, Bone bone) {
		this.dof = dof;
		this.bone = bone;
		redo();
		//dof.setName(dof.getBone().getName() + " DOF #" + dof.getBone().getDofIndex(dof));
	}
	
	public String getName() {
		return "add degree-of-freedom";
	}

	public void undo() {
//		MainFrame.getInstance().getUndoManager().setEnabled(false);
		bone.removeDofAxis(dof.getType());
		MainFrame.getInstance().getTreeModel().removeNodeFromParent(dof);
		
//		MainFrame.getInstance().getUndoManager().setEnabled(true);
		//morph.setValue(0);
		//MainFrame.getInstance().getModel().applyMorphs();
		MainFrame.getInstance().getSideBar().clearDetailPanel();
//		morph.unapply();
	}
	
	public void redo() {
//		MainFrame.getInstance().getModel().addDof(dof);
//		int[] aiIndex = new int[] { dof.getParent().getIndex(dof) };
//		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodesWereInserted(dof.getParent(),aiIndex);
//		TreePath path = dof.getTreePath();
//		dof.getBone().insert(dof, 0);
		bone.addDofAxis(dof.getType());
		MainFrame.getInstance().getTreeModel().insertNodeInto(dof, bone, bone.getDofs().size());
	
//		MainFrame.getInstance().getTree().makeVisible(dof.get);
		//morph.setValue(value);
//		morph.apply();
		//MainFrame.getInstance().getModel().applyMorphs();
	}

	public int sizeOf() {
		return 8 + 4 + 4;
	}

}
