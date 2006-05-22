package jpatch.boundary.mouse;

import java.awt.Component;
import java.awt.event.*;
import java.util.Iterator;

import javax.vecmath.*;

import jpatch.boundary.*;
import jpatch.boundary.action.*;
import jpatch.boundary.ui.*;
import jpatch.control.edit.*;
import jpatch.entity.*;

public class AnchorMouseListener extends MouseAdapter {
	
	@Override
	public void mousePressed(MouseEvent e) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) e.getSource());
		Selection selection = MainFrame.getInstance().getSelection();
		if (selection == null) {
			((LockingButtonGroup) Actions.getInstance().getButtonGroup("mode")).actionDone(false);
			return;
		}
		Object object = selection.getHotObject();
		if (object == null || !(object instanceof AnimModel)){
			((LockingButtonGroup) Actions.getInstance().getButtonGroup("mode")).actionDone(false);
			return;
		}
		AnimModel animModel = (AnimModel) object;
		
		Matrix4f m = new Matrix4f(viewDef.getScreenMatrix());
		m.mul(new Matrix4f(animModel.getTransform()));
//		m.invert();
		Point3f p3Hit = new Point3f(e.getX(), e.getY(), 0);
		Point3f p3 = new Point3f();
		float minDist = 64;
		Transformable anchor = null;
		for (Iterator it = animModel.getModel().getCurveSet().iterator(); it.hasNext(); ) {
			for (ControlPoint cp = (ControlPoint) it.next(); cp != null; cp = cp.getNextCheckNextLoop()) {
				if (!cp.isHead())
					continue;
				p3.set(cp.getPosition());
				m.transform(p3);
				p3.z = 0;
				float dist = p3.distanceSquared(p3Hit);
				if (dist < minDist) {
					anchor = cp;
					minDist = dist;
				}
			}
		}
		for (Bone bone : animModel.getModel().getBoneSet()) {
			p3.set(bone.getBoneEnd().getPosition());
			m.transform(p3);
			p3.z = 0;
			float dist = p3.distanceSquared(p3Hit);
			if (dist < minDist) {
				anchor = bone.getBoneEnd();
				minDist = dist;
			}
		}
		
		if (anchor != animModel.getAnchor()) {
			Transformable oldAnchor = animModel.getAnchor();
			Point3f p0 = oldAnchor == null ? new Point3f() : oldAnchor.getPosition();
			Point3f p1 = (anchor == null) ? new Point3f() : anchor.getPosition();
			Vector3d v = new Vector3d(p1.x - p0.x, p1.y - p0.y, p1.z - p0.z);
			animModel.getTransform().transform(v);
			Point3d p = animModel.getPositionDouble();
			p.add(v);
			MotionCurveSet.Model mcs = (MotionCurveSet.Model) MainFrame.getInstance().getAnimation().getCurvesetFor(animModel);
			JPatchActionEdit edit = new JPatchActionEdit("change anchor");
			edit.addEdit(new AtomicModifyMotionCurve.Object(mcs.anchor, MainFrame.getInstance().getAnimation().getPosition(), anchor));
			edit.addEdit(new AtomicModifyMotionCurve.Point3d(mcs.position, MainFrame.getInstance().getAnimation().getPosition(), p));
			MainFrame.getInstance().getUndoManager().addEdit(edit);
			animModel.setAnchor(anchor);
			animModel.setPosition(p);
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
		((LockingButtonGroup) Actions.getInstance().getButtonGroup("mode")).actionDone(false);
	}
}
