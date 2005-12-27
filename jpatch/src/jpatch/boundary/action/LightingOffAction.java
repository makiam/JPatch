package jpatch.boundary.action;

import java.awt.event.*;

import javax.swing.*;
import jpatch.boundary.*;

public final class LightingOffAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPatchScreen screen;
	
	public LightingOffAction(JPatchScreen screen) {
		super("Off");
		this.screen = screen;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		screen.setLightingMode(JPatchUserSettings.RealtimeRendererSettings.LightingMode.OFF);
		JPatchUserSettings.getInstance().realtimeRenderer.lightingMode = JPatchUserSettings.RealtimeRendererSettings.LightingMode.OFF;
	}
}

