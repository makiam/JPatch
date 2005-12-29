package jpatch.boundary.action;

import java.awt.event.*;

import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.settings.RealtimeRendererSettings;
import jpatch.boundary.settings.Settings;

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
		screen.setLightingMode(RealtimeRendererSettings.LightingMode.THREE_POINT);
		Settings.getInstance().realtimeRenderer.lightingMode = RealtimeRendererSettings.LightingMode.THREE_POINT;
	}
}

