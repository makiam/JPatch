package jpatch.boundary.sidebar;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import jpatch.boundary.*;
import jpatch.boundary.action.*;
import jpatch.boundary.ui.*;

public class SelectionPanel extends SidePanel
implements ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -820847916109947399L;
	JPatchInput inputName;
	Selection selection;
	public SelectionPanel(Selection selection) {
		this.selection = selection;
		//add(new JPatchButton(new EditMaterialAction(material)));
		//add(new JPatchButton(new CloneMaterialAction(material)));
		add(new JButton(new DeleteSelectionAction(selection)));
		//add(new JPatchButton(new ApplyMaterialAction(material)));
		JPatchInput.setDimensions(50,150,20);
		inputName = new JPatchInput("Name:",selection.getName());
		JPanel detailPanel = MainFrame.getInstance().getSideBar().getDetailPanel();
		detailPanel.removeAll();
		detailPanel.add(inputName);
		detailPanel.repaint();
		inputName.addChangeListener(this);
		
		
	}
	
	public void stateChanged(ChangeEvent changeEvent) {
		selection.setName(inputName.getStringValue());
		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodeChanged(selection);
		MainFrame.getInstance().requestFocus();
	}

}
