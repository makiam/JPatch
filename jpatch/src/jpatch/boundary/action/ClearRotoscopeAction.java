package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;

import jpatch.boundary.*;

public final class ClearRotoscopeAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ClearRotoscopeAction() {
		super("clear image");
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getActiveViewport().getViewDefinition();
		MainFrame.getInstance().getModel().setRotoscope(viewDef.getView(),null);
		MainFrame.getInstance().getJPatchScreen().update_all();
	}
}

