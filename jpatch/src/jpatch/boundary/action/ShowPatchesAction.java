package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class ShowPatchesAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ShowPatchesAction() {
		super("patches");
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getActiveViewport().getViewDefinition();
		viewDef.renderPatches(!viewDef.renderPatches());
		viewDef.repaint();
	}
}

