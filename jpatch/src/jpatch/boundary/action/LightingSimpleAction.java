package jpatch.boundary.action;

import java.awt.event.*;

import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.settings.RealtimeRendererSettings;
import jpatch.boundary.settings.Settings;

public final class LightingSimpleAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPatchScreen screen;
	
	public LightingSimpleAction(JPatchScreen screen) {
		super("Simple");
		this.screen = screen;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		screen.setLightingMode(RealtimeRendererSettings.LightingMode.SIMPLE);
		Settings.getInstance().realtimeRenderer.lightingMode = RealtimeRendererSettings.LightingMode.SIMPLE;
	}
}

