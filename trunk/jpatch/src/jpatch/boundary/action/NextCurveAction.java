package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;
import jpatch.entity.ControlPoint;

public final class NextCurveAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public NextCurveAction() {
		super(KeyMapping.getDescription("next curve"));
		//putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("add point"));
		//MainFrame.getInstance().getKeyEventDispatcher().setKeyActionListener(this,KeyEvent.VK_A);
	}
	public void actionPerformed(ActionEvent actionEvent) {
//		PointSelection ps = MainFrame.getInstance().getPointSelection();
//		if (ps != null && ps.isSingle()) {
//			ps.nextPoint();
//			MainFrame.getInstance().getJPatchScreen().update_all();
//		}
		
		NewSelection selection = MainFrame.getInstance().getSelection();
		if (!selection.isSingle())
			return;
		Object object = selection.getHotObject();
		if (!(object instanceof ControlPoint))
			return;
		ControlPoint cp = (ControlPoint) object;
		switch (selection.getDirection()) {
			case 0: {
				selection.setDirection(1);
			} break;
			case 1: {
				selection.setDirection(-1);
			} break;
			case -1: {
				if (cp.getNextAttached() != null) {
					selection.setDirection(1);
					selection.getMap().remove(cp);
					selection.getMap().put(cp.getNextAttached(), new Float(1.0f));
				} else {
					selection.setDirection(0);
					selection.getMap().remove(cp);
					selection.getMap().put(cp.getHead(), new Float(1.0f));
				}
			}
		}
		MainFrame.getInstance().getJPatchScreen().update_all();
	}
}