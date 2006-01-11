package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.boundary.*;

public final class DumpAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public DumpAction() {
		super("Dump");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		if (MainFrame.getInstance().getAnimation() != null) {
			MainFrame.getInstance().getAnimation().dump();
			return;
		}
		MainFrame.getInstance().getModel().dump();
	}
}

