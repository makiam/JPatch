package jpatch.boundary.sidebar;

import jpatch.boundary.*;
import jpatch.boundary.action.*;
import javax.swing.*;

public class AnimModelsPanel extends SidePanel {

	private static final long serialVersionUID = 1L;

	public AnimModelsPanel() {
		add(new JButton(new NewAnimModelAction()));
		MainFrame.getInstance().getSideBar().clearDetailPanel();
	}
}