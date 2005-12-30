package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.boundary.*;
import jpatch.boundary.settings.*;

public final class EditSettingsAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public EditSettingsAction() {
		super("Settings...");
	}
	public void actionPerformed(ActionEvent actionEvent) {
//		new ColorPreferences(MainFrame.getInstance());
		Settings.getInstance().showDialog(MainFrame.getInstance());
	}
}

