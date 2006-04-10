package jpatch.boundary.sidebar;

import javax.swing.tree.MutableTreeNode;

import jpatch.boundary.action.*;
import jpatch.boundary.*;
import javax.swing.*;

public class MorphsPanel extends SidePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3199368294142833195L;

	public MorphsPanel() {
		add(new JButton(new NewMorphAction()));
		//add(new JPatchButton(new NewMorphGroupAction(node)));
		MainFrame.getInstance().getSideBar().clearDetailPanel();
	}
}

