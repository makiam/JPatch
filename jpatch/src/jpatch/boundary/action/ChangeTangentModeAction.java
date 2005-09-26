package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.control.edit.*;
import jpatch.boundary.*;

import jpatch.entity.*;

public final class ChangeTangentModeAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4514692916254071819L;
	public static final int PEAK = 0;
	public static final int JPATCH = 1;
	public static final int SPATCH = 2;
	
	private static final String[] astrNames = new String[] { "peak", "round (JPatch)", "round (sPatch)" };
	private static final int[] aiMapping = new int[] {
		ControlPoint.PEAK,
		ControlPoint.JPATCH_G1,
		ControlPoint.SPATCH_ROUND
	};
	
	private int iMode;
	
	public ChangeTangentModeAction(int mode) {
		super(astrNames[mode]);
		iMode = aiMapping[mode];
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		Selection selection = MainFrame.getInstance().getSelection();
		if (selection == null)
			return;
		if (selection.getDirection() != 0) {
			MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeControlPoint.TangentMode((ControlPoint) selection.getHotObject(),iMode));
			MainFrame.getInstance().getJPatchScreen().update_all();
		} else {
			String name;
			switch (iMode) {
				case ControlPoint.PEAK: name = "peak tangents";
				default: name = "round tangents";
			}
			JPatchActionEdit edit = new JPatchActionEdit(name);
			ControlPoint[] acp = selection.getControlPointArray();
			for (int i = 0; i < acp.length; i++) {
				ControlPoint[] stack = acp[i].getStack();
				for (int j = 0; j < stack.length; j++) {
					edit.addEdit(new AtomicChangeControlPoint.TangentMode(stack[j],iMode));
				}
			}
			MainFrame.getInstance().getUndoManager().addEdit(edit);
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	}
}
