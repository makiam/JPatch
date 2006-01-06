package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.entity.*;
import jpatch.boundary.*;

public final class NewCameraAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NewCameraAction() {
		super("New Camera");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		MainFrame.getInstance().getAnimation().addCamera(new Camera("New Camera"), null);
	}
}


