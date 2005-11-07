package jpatch.control.edit;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import jpatch.boundary.MainFrame;
import jpatch.entity.MorphTarget;

public class AtomicAddMorph extends JPatchAtomicEdit implements JPatchRootEdit {
	
	private MorphTarget morph;
	//private float value;
	
	public AtomicAddMorph(MorphTarget morph) {
		this.morph = morph;
		//value = morph.getValue();
		redo();
	}
	
	public String getName() {
		return "add morph";
	}
	
	public void undo() {
//		int[] aiIndex = new int[] { morph.getParent().getIndex(morph) };
//		Object[] aObject = new Object[] { morph };
		MainFrame.getInstance().getModel().removeExpression(morph);
//		MainFrame.getInstance().getUndoManager().setEnabled(false);
//		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodesWereRemoved(morph.getParent(),aiIndex,aObject);
//		MainFrame.getInstance().getUndoManager().setEnabled(true);
//		//morph.setValue(0);
//		//MainFrame.getInstance().getModel().applyMorphs();
//		MainFrame.getInstance().getSideBar().clearDetailPanel();
		morph.unapply();
	}
	
	public void redo() {
		MainFrame.getInstance().getModel().addExpression(morph);
//		int[] aiIndex = new int[] { morph.getParent().getIndex(morph) };
//		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodesWereInserted(morph.getParent(),aiIndex);
//		TreePath path = morph.getTreePath();
//		MainFrame.getInstance().getTree().makeVisible(path);
		//morph.setValue(value);
		morph.apply();
		//MainFrame.getInstance().getModel().applyMorphs();
	}
	
	public int sizeOf() {
		return 8 + 4;
	}
}
