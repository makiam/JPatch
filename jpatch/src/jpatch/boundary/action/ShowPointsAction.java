package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class ShowPointsAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ViewDefinition viewDefinition;
	
	public ShowPointsAction(ViewDefinition viewDefinition) {
		super("points");
		this.viewDefinition = viewDefinition;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		viewDefinition.renderPoints(!viewDefinition.renderPoints());
		viewDefinition.repaint();
	}
}

