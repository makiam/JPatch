package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.entity.*;

public final class EditMaterialAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPatchMaterial material;
	public EditMaterialAction(JPatchMaterial material) {
		super("Edit");
		this.material = material;
		//putValue(Action.SHORT_DESCRIPTION,"Add Controlpoint [A]");
		//MainFrame.getInstance().getKeyEventDispatcher().setKeyActionListener(this,KeyEvent.VK_A);
	}
	public void actionPerformed(ActionEvent actionEvent) {
		//JDialog dialog = new JDialog(MainFrame.getInstance(),material.getName(),true);
		//dialog.getContentPane().add(new MaterialEditor(material));
		//dialog.setSize(430,340);
		//dialog.pack();
		//dialog.show();
		MaterialEditor materialEditor = new MaterialEditor(MainFrame.getInstance(), material);
		materialEditor.setVisible(true);
		
		//MaterialEditor.createMaterialEditor(material.getMaterialProperties());
		//MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
		//MainFrame.getInstance().getJPatchScreen().addMouseListeners(new AddControlPointMouseAdapter());
		//MainFrame.getInstance().clearDialog();
	}
}

