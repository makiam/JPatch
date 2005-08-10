package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class LightingStickyAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPatchScreen screen;
	
	public LightingStickyAction(JPatchScreen screen) {
		super("Stick to camera");
		this.screen = screen;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		screen.setStickyLight(!screen.isStickyLight());
		JPatchSettings.getInstance().bStickyLight = screen.isStickyLight();
	}
}

