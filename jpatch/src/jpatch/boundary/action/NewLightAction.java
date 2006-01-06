package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.entity.*;
import jpatch.boundary.*;

public final class NewLightAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NewLightAction() {
		super("New Lightsource");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		MainFrame.getInstance().getAnimation().addLight(new AnimLight(), null);
	}
}