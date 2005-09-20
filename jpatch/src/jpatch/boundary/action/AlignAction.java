package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;


public final class AlignAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3959013803958277647L;
	public AlignAction() {
		super("align controlpoints");
		//putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("lathe"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		if (ps != null) {
			//PointSelection newPs = MainFrame.getInstance().getModel().clone(ps.getControlPointArray());
			//MainFrame.getInstance().setSelection(newPs);
			//float epsilon = 3f / MainFrame.getInstance().getJPatchScreen().getActiveViewport().getViewDefinition().getMatrix().getScale();
			//System.out.println(epsilon);
			new AlignOptions(MainFrame.getInstance());
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	}
}
