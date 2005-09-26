package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import javax.vecmath.*;
import jpatch.boundary.*;


public final class SetViewLockAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ViewDefinition viewDefinition;
	
	public SetViewLockAction(ViewDefinition viewDefinition) {
		super("lock view to selection");
		this.viewDefinition = viewDefinition;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		Selection selection = MainFrame.getInstance().getSelection();
		if (selection != null) {
			viewDefinition.setLock(new Point3f(selection.getPivot()));
		}
		//((JPatchCanvas)viewDefinition.getViewport()).updateImage();
		//viewDefinition.reset();
	}
}

