package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

import jpatch.control.edit.*;

public final class ConvertHookToCpAction extends AbstractAction {
	
	private static final long serialVersionUID = 1L;
	
	public ConvertHookToCpAction() {
		super("convert to cp");
		}
	public void actionPerformed(ActionEvent actionEvent) {
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		if (ps != null && ps.isSingle() && ps.getControlPoint().isHook()) {
			MainFrame.getInstance().getUndoManager().addEdit(new ConvertHookToCpEdit(ps.getControlPoint()));
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	}
}

