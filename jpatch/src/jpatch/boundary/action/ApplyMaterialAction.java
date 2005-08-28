package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;
import jpatch.entity.*;
import jpatch.control.edit.*;


public final class ApplyMaterialAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -869947425548126658L;
	private JPatchMaterial material;
	public ApplyMaterialAction(JPatchMaterial material) {
		super("Apply");
		this.material = material;
		//putValue(Action.SHORT_DESCRIPTION,"Add Controlpoint [A]");
		//MainFrame.getInstance().getKeyEventDispatcher().setKeyActionListener(this,KeyEvent.VK_A);
	}
	public ApplyMaterialAction() {
		super("Clear patch material");
		this.material = null;
		//putValue(Action.SHORT_DESCRIPTION,"Add Controlpoint [A]");
		//MainFrame.getInstance().getKeyEventDispatcher().setKeyActionListener(this,KeyEvent.VK_A);
	}
	public void actionPerformed(ActionEvent actionEvent) {
		//JDialog dialog = new JDialog(MainFrame.getInstance(),material.getName(),true);
		//dialog.getContentPane().add(new MaterialEditor(material));
		//dialog.setSize(430,340);
		//dialog.pack();
		//dialog.show();
		
		//MaterialEditor materialEditor = new MaterialEditor(material);
		//materialEditor.show();
		
		//MaterialEditor.createMaterialEditor(material.getMaterialProperties());
		//MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
		//MainFrame.getInstance().getJPatchScreen().addMouseListeners(new AddControlPointMouseAdapter());
		//MainFrame.getInstance().clearDialog();
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		if (ps != null) {
			//ps.applyMaterial(material);
			MainFrame.getInstance().getUndoManager().addEdit(new ChangeSelectionMaterialEdit(ps,material));
			/*
			for (Patch patch = MainFrame.getInstance().getModel().getFirstPatch(); patch != null; patch = patch.getNext()) {
				if (patch.isSelected(ps)) {
					patch.setMaterial(material);
				}
			}
			*/
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	}
}

