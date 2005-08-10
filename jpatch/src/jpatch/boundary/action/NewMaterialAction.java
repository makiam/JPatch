package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import jpatch.entity.*;
import jpatch.boundary.*;

public final class NewMaterialAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Model model;
	
	public NewMaterialAction(Model model) {
		super("New Material");
		this.model = model;
		//putValue(Action.SHORT_DESCRIPTION,"Add Controlpoint [A]");
		//MainFrame.getInstance().getKeyEventDispatcher().setKeyActionListener(this,KeyEvent.VK_A);
	}
	public void actionPerformed(ActionEvent actionEvent) {
		JPatchMaterial material = new JPatchMaterial();
		if (model.addMaterial(material)) {
			int[] aiIndex = new int[] { material.getParent().getIndex(material) };
			((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodesWereInserted(material.getParent(),aiIndex);
			TreePath path = material.getTreePath();
			MainFrame.getInstance().getTree().makeVisible(path);
		}
		//MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
		//MainFrame.getInstance().getJPatchScreen().addMouseListeners(new AddControlPointMouseAdapter());
		//MainFrame.getInstance().clearDialog();
	}
}

