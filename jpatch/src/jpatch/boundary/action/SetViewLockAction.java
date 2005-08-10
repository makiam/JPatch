package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import javax.vecmath.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;

public final class SetViewLockAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ViewDefinition viewDefinition;
	
	public SetViewLockAction(ViewDefinition viewDefinition) {
		super("lock to selection");
		this.viewDefinition = viewDefinition;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		if (ps != null) {
			viewDefinition.setLock(new Point3f(ps.getPivot()));
		}
		//((JPatchCanvas)viewDefinition.getViewport()).updateImage();
		//viewDefinition.reset();
	}
}

