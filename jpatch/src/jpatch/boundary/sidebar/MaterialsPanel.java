package jpatch.boundary.sidebar;

import jpatch.boundary.*;
import jpatch.boundary.action.*;
import jpatch.entity.*;
import javax.swing.*;

public class MaterialsPanel extends SidePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1969415873406407853L;

	public MaterialsPanel(OLDModel model) {
		add(new JButton(new NewMaterialAction(model)));
		add(new JButton(new ApplyMaterialAction()));
		MainFrame.getInstance().getSideBar().clearDetailPanel();
		//add(new JButton("test2"));
		//add(new JButton("test3"));
		//add(new JButton("test4"));
	}
}

