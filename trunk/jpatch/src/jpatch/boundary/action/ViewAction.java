package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.entity.*;

public final class ViewAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int iDirection;
	private Camera camera;
	
	public ViewAction(int direction) {
		super(KeyMapping.getDescription(ViewDefinition.aViewName[direction]));
		iDirection = direction;
		camera = null;
	}
	
	public ViewAction(Camera camera) {
		this.camera = camera;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getActiveViewport().getViewDefinition();
		if (camera != null)
			viewDef.setCamera(camera);
		else
			viewDef.setView(iDirection);
		viewDef.repaint();
	}
}
