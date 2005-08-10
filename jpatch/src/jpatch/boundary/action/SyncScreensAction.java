package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

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
		JPatchScreen screen = MainFrame.getInstance().getJPatchScreen();
		screen.synchronize(!screen.isSynchronized());
		JPatchSettings.getInstance().bSyncWindows = screen.isSynchronized();
	}
}

