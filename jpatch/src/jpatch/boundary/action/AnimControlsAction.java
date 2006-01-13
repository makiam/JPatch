package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.boundary.*;

public final class AnimControlsAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8880247828005239494L;
	public AnimControlsAction() {
		super("Show animation controls");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		MainFrame.getInstance().showAnimControls();
	}
}

