package jpatch.boundary.sidebar;

import jpatch.boundary.*;
import jpatch.boundary.action.*;

public class CamerasPanel extends SidePanel {

	private static final long serialVersionUID = 1L;

	public CamerasPanel() {
		add(new JPatchButton(new NewCameraAction()));
		MainFrame.getInstance().getSideBar().clearDetailPanel();
	}
}
