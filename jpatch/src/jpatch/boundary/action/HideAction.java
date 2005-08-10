package jpatch.boundary.action;

import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;
import jpatch.entity.*;

public final class HideAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean bActive = false;
	
	public HideAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/hide.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("hide"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		if (!bActive) {
			PointSelection ps = MainFrame.getInstance().getPointSelection();
			if (ps != null) {
				ControlPoint[] acp = ps.getControlPointArray();
				ArrayList selectedPoints = new ArrayList();
				for (int i = 0; i < acp.length; i++) {
					selectedPoints.add(acp[i].getHead());
					if (acp[i].isHook()) {
						selectedPoints.add(acp[i].trueHead());
					}
				}
				Curve curve = MainFrame.getInstance().getModel().getFirstCurve();
				while (curve != null) {
					ControlPoint cp = curve.getStart();
					while (cp != null) {
						cp.setHidden(!selectedPoints.contains(cp.trueHead()));
						cp = cp.getNextCheckNextLoop();
					}
					curve = curve.getNext();
				}
			} else {
				Curve curve = MainFrame.getInstance().getModel().getFirstCurve();
				while (curve != null) {
					ControlPoint cp = curve.getStart();
					while (cp != null) {
						cp.setHidden(true);
						cp = cp.getNextCheckNextLoop();
					}
					curve = curve.getNext();
				}
			}
		} else {
			Curve curve = MainFrame.getInstance().getModel().getFirstCurve();
			while (curve != null) {
				ControlPoint cp = curve.getStart();
				while (cp != null) {
					cp.setHidden(false);
					cp = cp.getNextCheckNextLoop();
				}
			curve = curve.getNext();
			}
		}
		MainFrame.getInstance().getJPatchScreen().update_all();
		bActive = !bActive;
	}
}
