package jpatch.boundary.action;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import jpatch.control.edit.*;
import jpatch.boundary.*;
import jpatch.entity.*;

public final class PeakAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public PeakAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/peak.png")));
		putValue(Action.SHORT_DESCRIPTION,"Peak");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		Selection selection = MainFrame.getInstance().getSelection();
		if (selection != null && selection.getDirection() != 0) {
			MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeControlPoint.TangentMode((ControlPoint) selection.getHotObject(),ControlPoint.PEAK));
			MainFrame.getInstance().getJPatchScreen().update_all();
		} else if (selection != null) {
			JPatchActionEdit edit = new JPatchActionEdit("peak tangents");
			for (Iterator it = selection.getObjects().iterator(); it.hasNext(); ) {
				Object object = it.next();
				if (object instanceof ControlPoint) {
					ControlPoint[] stack = ((ControlPoint) object).getStack();
					for (int j = 0; j < stack.length; j++) {
						edit.addEdit(new AtomicChangeControlPoint.TangentMode(stack[j],ControlPoint.PEAK));
					}
				}
			}
			MainFrame.getInstance().getUndoManager().addEdit(edit);
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
		ControlPoint.setDefaultMode(ControlPoint.PEAK);
	}
}
