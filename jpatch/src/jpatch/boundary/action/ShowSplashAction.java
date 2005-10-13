package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.boundary.*;

public final class ShowSplashAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ShowSplashAction() {
		super("Show splash-screen...");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		SplashScreen splash = new SplashScreen();
		splash.showSplash(false);
	}
}

