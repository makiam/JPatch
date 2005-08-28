package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.control.edit.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;

public final class FlipAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;
	
	private static final String[] astrAxis = { "X","Y","Z" };
	
	private int iAxis;
	
	public FlipAction(int axis) {
		super(astrAxis[axis]);
		iAxis = axis;
		//putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("lathe"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		if (ps != null) {
			//PointSelection newPs = MainFrame.getInstance().getModel().clone(ps.getControlPointArray());
			//MainFrame.getInstance().setSelection(newPs);
			//float epsilon = 3f / MainFrame.getInstance().getJPatchScreen().getActiveViewport().getViewDefinition().getMatrix().getScale();
			//System.out.println(epsilon);
			MainFrame.getInstance().getUndoManager().addEdit(new FlipEdit(ps, iAxis, true, false));
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	}
}
