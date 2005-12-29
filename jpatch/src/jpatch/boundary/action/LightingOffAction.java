package jpatch.boundary.action;

import java.awt.event.*;

import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.settings.RealtimeRendererSettings;
import jpatch.boundary.settings.Settings;

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
		screen.setLightingMode(RealtimeRendererSettings.LightingMode.OFF);
		Settings.getInstance().realtimeRenderer.lightingMode = RealtimeRendererSettings.LightingMode.OFF;
	}
}

