package jpatch.boundary.sidebar;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.boundary.action.*;

public class AnimModelPanel extends SidePanel
implements ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6771633567920818871L;
	JPatchInput inputName;
	AnimModel model;
	public AnimModelPanel(AnimModel model) {
		this.model = model;
		add(new JPatchButton(new EditAnimObjectAction(model)));
		add(new JPatchButton(new DeleteModelAction(model)));
		JPatchInput.setDimensions(50,150,20);
		inputName = new JPatchInput("Name:",model.getName());
		JPanel detailPanel = MainFrame.getInstance().getSideBar().getDetailPanel();
		detailPanel.removeAll();
		detailPanel.add(inputName);
		inputName.addChangeListener(this);
		detailPanel.repaint();
	}
	
	public void stateChanged(ChangeEvent changeEvent) {
		model.setName(inputName.getStringValue());
		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodeChanged(model);
		MainFrame.getInstance().requestFocus();
	}
}