package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class ViewAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int iDirection;
	
	public ViewAction(int direction) {
		super(KeyMapping.getDescription(ViewDefinition.aViewName[direction]));
		iDirection = direction;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getActiveViewport().getViewDefinition();
		viewDef.setView(iDirection);
		viewDef.repaint();
	}
}

