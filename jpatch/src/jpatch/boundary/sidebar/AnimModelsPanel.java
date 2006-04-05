package jpatch.boundary.sidebar;

import jpatch.boundary.*;
import jpatch.boundary.action.*;
import jpatch.boundary.ui.*;

public class AnimModelsPanel extends SidePanel {

	private static final long serialVersionUID = 1L;

	public AnimModelsPanel() {
		add(new JPatchButton(new NewAnimModelAction()));
		MainFrame.getInstance().getSideBar().clearDetailPanel();
	}
}