package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.entity.*;
import jpatch.boundary.*;

public final class DeleteLightAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private AnimLight light;
	
	public DeleteLightAction(AnimLight light) {
		super("delete");
		this.light = light;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		MainFrame.getInstance().getAnimation().removeLight(light);
	}
}