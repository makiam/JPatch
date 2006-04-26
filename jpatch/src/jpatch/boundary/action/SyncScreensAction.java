package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.settings.Settings;

public final class SyncScreensAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SyncScreensAction() {
		super("Synchronize viewports");
		//this.viewDefinition = viewDefinition;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		Settings.getInstance().viewports.synchronizeViewports = !Settings.getInstance().viewports.synchronizeViewports;
	}
}

