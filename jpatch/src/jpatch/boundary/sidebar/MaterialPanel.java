package jpatch.boundary.sidebar;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.boundary.action.*;
import jpatch.boundary.ui.*;

public class MaterialPanel extends SidePanel
implements ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6771633567920818871L;
	JPatchInput inputName;
	JPatchMaterial material;
	public MaterialPanel(JPatchMaterial material) {
		this.material = material;
		add(new JPatchButton(new EditMaterialAction(material)));
		//add(new JPatchButton(new CloneMaterialAction(material)));
		add(new JPatchButton(new DeleteMaterialAction(material)));
		add(new JPatchButton(new ApplyMaterialAction(material)));
		add(new JPatchButton(new SelectMaterialAction(material)));
		add(new JPatchButton(new ApplyMaterialAction()));
		JPatchInput.setDimensions(50,150,20);
		inputName = new JPatchInput("Name:",material.getName());
		JPanel detailPanel = MainFrame.getInstance().getSideBar().getDetailPanel();
		detailPanel.removeAll();
		if (material != MainFrame.getInstance().getModel().getMaterialList().get(0)) {
			detailPanel.add(inputName);
			inputName.addChangeListener(this);
		}
		detailPanel.repaint();
	}
	
	public void stateChanged(ChangeEvent changeEvent) {
		material.setName(inputName.getStringValue());
		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodeChanged(material);
		MainFrame.getInstance().requestFocus();
	}

}
