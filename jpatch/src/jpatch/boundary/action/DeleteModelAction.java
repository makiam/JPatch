package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.entity.*;
import jpatch.boundary.*;

public final class DeleteModelAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private AnimModel model;
	
	public DeleteModelAction(AnimModel model) {
		super("delete");
		this.model = model;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		MainFrame.getInstance().getAnimation().removeModel(model);
	}
}