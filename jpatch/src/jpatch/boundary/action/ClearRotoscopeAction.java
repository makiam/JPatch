package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;

import jpatch.boundary.*;

public final class ClearRotoscopeAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ViewDefinition viewDefinition;
	
	public ClearRotoscopeAction(ViewDefinition viewDefinition) {
		super("clear image");
		this.viewDefinition = viewDefinition;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		MainFrame.getInstance().getModel().setRotoscope(viewDefinition.getView(),null);
		MainFrame.getInstance().getJPatchScreen().update_all();
	}
}

