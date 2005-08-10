package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class LightingHeadAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPatchScreen screen;
	
	public LightingHeadAction(JPatchScreen screen) {
		super("Headlight");
		this.screen = screen;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		screen.setLightingMode(JPatchScreen.LIGHT_HEAD);
		JPatchSettings.getInstance().iLightingMode = JPatchScreen.LIGHT_HEAD;
	}
}

