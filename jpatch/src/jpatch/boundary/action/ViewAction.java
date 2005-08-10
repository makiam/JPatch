package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class ViewAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ViewDefinition viewDefinition;
	private int iDirection;
	
	public ViewAction(ViewDefinition viewDefinition, int direction) {
		super(KeyMapping.getDescription(ViewDefinition.aViewName[direction]));
		this.viewDefinition = viewDefinition;
		iDirection = direction;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		viewDefinition.setView(iDirection);
		viewDefinition.repaint();
	}
}

