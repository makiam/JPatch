package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.entity.*;
import jpatch.boundary.*;

public final class DeleteCameraAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Camera camera;
	
	public DeleteCameraAction(Camera camera) {
		super("delete");
		this.camera = camera;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		MainFrame.getInstance().getAnimation().removeCamera(camera);
	}
}


