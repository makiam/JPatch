package jpatch.boundary.sidebar;

import jpatch.boundary.action.*;
import jpatch.boundary.*;

public class MorphsPanel extends SidePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3199368294142833195L;

	public MorphsPanel(JPatchTreeNode node) {
		add(new JPatchButton(new NewMorphAction(node)));
		//add(new JPatchButton(new NewMorphGroupAction(node)));
		MainFrame.getInstance().getSideBar().clearDetailPanel();
	}
}

