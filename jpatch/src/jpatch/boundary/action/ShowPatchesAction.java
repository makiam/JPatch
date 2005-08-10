package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class ShowPatchesAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ViewDefinition viewDefinition;
	
	public ShowPatchesAction(ViewDefinition viewDefinition) {
		super("patches");
		this.viewDefinition = viewDefinition;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		viewDefinition.renderPatches(!viewDefinition.renderPatches());
		//((JPatchCanvas)viewDefinition.getViewport()).updateImage();
		viewDefinition.reset();
	}
}

