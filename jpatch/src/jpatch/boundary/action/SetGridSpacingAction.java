package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class SetGridSpacingAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SetGridSpacingAction() {
		super("Grid spacing...");
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		new GridDialog(MainFrame.getInstance());
	}
}
