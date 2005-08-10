package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class ViewKeyAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int iDirection;
	
	public ViewKeyAction(int direction) {
		super(ViewDefinition.aViewName[direction]);
		iDirection = direction;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		ViewDefinition viewDefinition = MainFrame.getInstance().getJPatchScreen().getActiveViewport().getViewDefinition();
		viewDefinition.setView(iDirection);
		viewDefinition.repaint();
	}
}

