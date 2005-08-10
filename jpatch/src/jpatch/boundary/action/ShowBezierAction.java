package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class ShowBezierAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ViewDefinition viewDefinition;
	
	public ShowBezierAction(ViewDefinition viewDefinition) {
		super("Bezier");
		this.viewDefinition = viewDefinition;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		viewDefinition.renderBezierCPs(!viewDefinition.renderBezierCPs());
		viewDefinition.repaint();
	}
}

