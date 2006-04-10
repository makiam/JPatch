package jpatch.boundary.sidebar;

import jpatch.boundary.*;
import jpatch.boundary.action.*;
import javax.swing.*;

public class LightsPanel extends SidePanel {

	private static final long serialVersionUID = 1L;

	public LightsPanel() {
		add(new JButton(new NewLightAction()));
		MainFrame.getInstance().getSideBar().clearDetailPanel();
	}
}