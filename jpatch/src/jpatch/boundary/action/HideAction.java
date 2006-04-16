package jpatch.boundary.action;

import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

import jpatch.entity.*;

public final class HideAction extends AbstractAction {
	private boolean bActive = false;
	public void actionPerformed(ActionEvent actionEvent) {
		if (!bActive) {
			Selection selection = MainFrame.getInstance().getSelection();
			if (selection != null) {
//				ControlPoint[] acp = selection.getControlPointArray();
				ArrayList selectedPoints = new ArrayList();
//				for (int i = 0; i < acp.length; i++) {
				for (Iterator it = selection.getObjects().iterator(); it.hasNext(); ) {
					Object object = it.next();
					if (object instanceof ControlPoint) {
						ControlPoint cp = (ControlPoint) object;
						selectedPoints.add(cp.getHead());
						if (cp.isHook()) {
							selectedPoints.add(cp.trueHead());
						}
					}
				}
//				Curve curve = MainFrame.getInstance().getModel().getFirstCurve();
//				while (curve != null) {
//					ControlPoint cp = curve.getStart();
//					while (cp != null) {
//						cp.setHidden(!selectedPoints.contains(cp.trueHead()));
//						cp = cp.getNextCheckNextLoop();
//					}
//					curve = curve.getNext();
//				}
				for (Iterator it = MainFrame.getInstance().getModel().getCurveSet().iterator(); it.hasNext(); ) {
					for (ControlPoint cp = (ControlPoint) it.next(); cp != null; cp = cp.getNextCheckNextLoop()) {
						cp.setHidden(!selectedPoints.contains(cp.trueHead()));
					}
				}
			} else {
//				Curve curve = MainFrame.getInstance().getModel().getFirstCurve();
//				while (curve != null) {
//					ControlPoint cp = curve.getStart();
//					while (cp != null) {
//						cp.setHidden(true);
//						cp = cp.getNextCheckNextLoop();
//					}
//					curve = curve.getNext();
//				}
				for (Iterator it = MainFrame.getInstance().getModel().getCurveSet().iterator(); it.hasNext(); ) {
					for (ControlPoint cp = (ControlPoint) it.next(); cp != null; cp = cp.getNextCheckNextLoop()) {
						cp.setHidden(true);
					}
				}
			}
		} else {
//			Curve curve = MainFrame.getInstance().getModel().getFirstCurve();
//			while (curve != null) {
//				ControlPoint cp = curve.getStart();
//				while (cp != null) {
//					cp.setHidden(false);
//					cp = cp.getNextCheckNextLoop();
//				}
//			curve = curve.getNext();
//			}
			for (Iterator it = MainFrame.getInstance().getModel().getCurveSet().iterator(); it.hasNext(); ) {
				for (ControlPoint cp = (ControlPoint) it.next(); cp != null; cp = cp.getNextCheckNextLoop()) {
					cp.setHidden(false);
				}
			}
		}
		MainFrame.getInstance().getJPatchScreen().update_all();
		bActive = !bActive;
	}
}
