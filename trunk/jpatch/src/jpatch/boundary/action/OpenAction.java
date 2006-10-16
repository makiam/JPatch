package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;


public class OpenAction extends AbstractAction {
	public void actionPerformed(ActionEvent e) {
		if (MainFrame.getInstance().getAnimation() != null)
			OldActions.getInstance().getAction("open animation").actionPerformed(e);
		else
			OldActions.getInstance().getAction("open model").actionPerformed(e);
	}
}
