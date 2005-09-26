package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import java.util.*;

import jpatch.boundary.*;
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
		NewSelection selection = MainFrame.getInstance().getSelection();
		if (selection != null) {
			//ps.applyMaterial(material);
			JPatchActionEdit edit = new JPatchActionEdit("change material");
			for (Iterator it = MainFrame.getInstance().getModel().getPatchSet().iterator(); it.hasNext(); ) {
				Patch patch = (Patch) it.next();
				if (patch.isSelected(selection))
					edit.addEdit(new AtomicChangePatchMaterial(patch, material));
			}
			if (edit.isValid()) {
				MainFrame.getInstance().getUndoManager().addEdit(edit);
				MainFrame.getInstance().getJPatchScreen().update_all();
			}
		}
	}
}

