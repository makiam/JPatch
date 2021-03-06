package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

import jpatch.control.edit.*;
import jpatch.entity.OLDSelection;

public final class ExtendSelectionAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ExtendSelectionAction() {
		super("extend selection");
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		OLDSelection selection = MainFrame.getInstance().getSelection();
		if (selection != null) {
//			boolean selectCurveOnly = (ps.isSingle() && (ps.isCurve() || ps.getControlPoint().getPrevAttached() == null));
			MainFrame.getInstance().getUndoManager().addEdit(new CompoundExpandSelection(selection));
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	}
}
