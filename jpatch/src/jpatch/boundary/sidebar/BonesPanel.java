package jpatch.boundary.sidebar;

import jpatch.boundary.*;
import jpatch.boundary.action.*;
import jpatch.entity.*;

public class BonesPanel extends SidePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1969415873406407853L;

	public BonesPanel(Model model) {
		add(new JPatchButton(new ResetPoseAction(model)));
		//add(new JPatchButton(new ApplyMaterialAction()));
		MainFrame.getInstance().getSideBar().clearDetailPanel();
		//add(new JButton("test2"));
		//add(new JButton("test3"));
		//add(new JButton("test4"));
	}
}

