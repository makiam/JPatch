package jpatch.boundary.sidebar;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import jpatch.boundary.action.*;
import jpatch.boundary.*;

public class MorphGroupPanel extends SidePanel
implements ChangeListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4498533614509705351L;
	JPatchInput inputName;
	JPatchTreeNode node;
	
	public MorphGroupPanel(JPatchTreeNode node) {
		this.node = node;
		add(new JPatchButton(new NewMorphAction(node)));
		add(new JPatchButton(new NewMorphGroupAction(node)));
		JPatchInput.setDimensions(50,150,20);
		inputName = new JPatchInput("Name:",node.getName());
		JPanel detailPanel = MainFrame.getInstance().getSideBar().getDetailPanel();
		detailPanel.removeAll();
		detailPanel.add(inputName);
		detailPanel.repaint();
		inputName.addChangeListener(this);
	}
	
	public void stateChanged(ChangeEvent changeEvent) {
		node.setName(inputName.getStringValue());
		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodeChanged(node);
	}
}

