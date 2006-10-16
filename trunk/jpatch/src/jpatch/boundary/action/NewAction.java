package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;


public class NewAction extends AbstractAction {
	public void actionPerformed(ActionEvent e) {
		if (MainFrame.getInstance().getAnimation() != null)
			OldActions.getInstance().getAction("new animation").actionPerformed(e);
		else
			OldActions.getInstance().getAction("new model").actionPerformed(e);
	}
}
