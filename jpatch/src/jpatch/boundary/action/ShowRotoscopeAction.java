package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class ShowRotoscopeAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ViewDefinition viewDefinition;
	
	public ShowRotoscopeAction(ViewDefinition viewDefinition) {
		super("rotoscope");
		this.viewDefinition = viewDefinition;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		viewDefinition.showRotoscope(!viewDefinition.showRotoscope());
		viewDefinition.repaint();
	}
}

