package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class BackfaceNormalFlipAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7102605713188071375L;
	
	public BackfaceNormalFlipAction() {
		super("Flip backfacing normals");
		//this.viewDefinition = viewDefinition;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		JPatchScreen screen = MainFrame.getInstance().getJPatchScreen();
		screen.flipBackfacingNormals(!screen.flipBackfacingNormals());
		screen.update_all();
		//JPatchUserSettings.getInstance().bSyncWindows = screen.isSynchronized();
	}
}

