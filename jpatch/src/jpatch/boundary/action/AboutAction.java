package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.boundary.*;

public final class AboutAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8880247828005239494L;
	public AboutAction() {
		super("About...");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		new About(MainFrame.getInstance());
	}
}

