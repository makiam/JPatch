package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.settings.RealtimeRendererSettings;
import jpatch.boundary.settings.Settings;

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
		screen.setLightingMode(RealtimeRendererSettings.LightingMode.HEADLIGHT);
		Settings.getInstance().realtimeRenderer.lightingMode = RealtimeRendererSettings.LightingMode.HEADLIGHT;
	}
}

