package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class AlwaysUseZBufferAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3129672807496778437L;
	private ViewDefinition viewDefinition;
	
	public AlwaysUseZBufferAction(ViewDefinition viewDefinition) {
		super("always use z-buffer");
		this.viewDefinition = viewDefinition;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
//		viewDefinition.alwaysUseZBuffer(!viewDefinition.alwaysUseZBuffer());
		//((JPatchCanvas)viewDefinition.getViewport()).updateImage();
//		viewDefinition.reset();
	}
}

