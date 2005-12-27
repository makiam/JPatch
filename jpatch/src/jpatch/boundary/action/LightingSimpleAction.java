package jpatch.boundary.action;

import java.awt.event.*;

import javax.swing.*;
import jpatch.boundary.*;

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
		screen.setLightingMode(JPatchUserSettings.RealtimeRendererSettings.LightingMode.SIMPLE);
		JPatchUserSettings.getInstance().realtimeRenderer.lightingMode = JPatchUserSettings.RealtimeRendererSettings.LightingMode.SIMPLE;
	}
}

