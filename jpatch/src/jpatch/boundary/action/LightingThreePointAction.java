package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class LightingThreePointAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPatchScreen screen;
	
	public LightingThreePointAction(JPatchScreen screen) {
		super("Three point");
		this.screen = screen;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		screen.setLightingMode(JPatchScreen.LIGHT_THREE_POINT);
		JPatchSettings.getInstance().iLightingMode = JPatchScreen.LIGHT_THREE_POINT;
	}
}

