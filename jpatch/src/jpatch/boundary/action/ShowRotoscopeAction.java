package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class ShowRotoscopeAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ShowRotoscopeAction() {
		super("rotoscope");
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getActiveViewport().getViewDefinition();
		viewDef.showRotoscope(!viewDef.showRotoscope());
		viewDef.repaint();
	}
}

