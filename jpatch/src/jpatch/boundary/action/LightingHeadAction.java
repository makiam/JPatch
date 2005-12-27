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
		screen.setLightingMode(JPatchUserSettings.RealtimeRendererSettings.LightingMode.HEADLIGHT);
		JPatchUserSettings.getInstance().realtimeRenderer.lightingMode = JPatchUserSettings.RealtimeRendererSettings.LightingMode.HEADLIGHT;
	}
}

