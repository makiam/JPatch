package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.control.edit.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;
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
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		if (ps != null && ps.isCurve()) {
			MainFrame.getInstance().getUndoManager().addEdit(new ChangeControlPointTangentModeEdit(ps.getControlPoint(),iMode));
			MainFrame.getInstance().getJPatchScreen().update_all();
		} else if (ps != null) {
			JPatchCompoundEdit compoundEdit = new JPatchCompoundEdit();
			ControlPoint[] acp = ps.getControlPointArray();
			for (int i = 0; i < acp.length; i++) {
				ControlPoint[] stack = acp[i].getStack();
				for (int j = 0; j < stack.length; j++) {
					compoundEdit.addEdit(new ChangeControlPointTangentModeEdit(stack[j],iMode));
				}
			}
			MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	}
}
