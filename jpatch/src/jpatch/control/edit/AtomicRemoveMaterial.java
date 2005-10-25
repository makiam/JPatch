package jpatch.control.edit;

import javax.swing.tree.*;
import jpatch.entity.*;
import jpatch.boundary.sidebar.*;
import jpatch.boundary.*;

public class AtomicRemoveMaterial extends JPatchAtomicEdit implements JPatchRootEdit {
	
	private JPatchMaterial material;
	
	public AtomicRemoveMaterial(JPatchMaterial material) {
		this.material = material;
		redo();
	}
	
	public String getPresentationName() {
		return "remove material";
	}
	
	public void redo() {
//		int[] aiIndex = new int[] { material.getParent().getIndex(material) };
//		Object[] aObject = new Object[] { material };
		MainFrame.getInstance().getModel().removeMaterial(material);
//		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodesWereRemoved(material.getParent(),aiIndex,aObject);
		MainFrame.getInstance().getSideBar().replacePanel(new SidePanel());
		MainFrame.getInstance().getSideBar().clearDetailPanel();
		MainFrame.getInstance().getSideBar().validate();
	}
	
	public void undo() {
		MainFrame.getInstance().getModel().addMaterial(material);
//		int[] aiIndex = new int[] { material.getParent().getIndex(material) };
//		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodesWereInserted(material.getParent(),aiIndex);
		MainFrame.getInstance().getSideBar().replacePanel(new SidePanel());
		MainFrame.getInstance().getSideBar().clearDetailPanel();
		MainFrame.getInstance().getSideBar().validate();
		//MainFrame.getInstance().getTree().setSelectionPath(path);
		//MainFrame.getInstance().getTree().makeVisible(path);
	}
	
	public int sizeOf() {
		return 8 + 4;
	}
	
	public String getName() {
		return "remove material";
	}
}
