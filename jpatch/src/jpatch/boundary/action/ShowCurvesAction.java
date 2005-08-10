package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class ShowCurvesAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ViewDefinition viewDefinition;
	
	public ShowCurvesAction(ViewDefinition viewDefinition) {
		super("curves");
		this.viewDefinition = viewDefinition;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		viewDefinition.renderCurves(!viewDefinition.renderCurves());
		viewDefinition.repaint();
	}
}

