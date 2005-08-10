package jpatch.boundary.selection;

import javax.vecmath.*;
import jpatch.entity.*;

public class SelectionFactory {
	static float fRadius = 5f;
	
	public static void setRadius(float radius) {
		fRadius = radius;
	}
	
	public static PointSelection createRectangularPointSelection(int ax, int ay, int bx, int by, Matrix4f transformationMatrix, Model model) {
		PointSelection selection = new PointSelection();
		Point3f p3 = new Point3f();
		for (Curve curve = model.getFirstCurve(); curve != null; curve = curve.getNext()) {
			for (ControlPoint cp = curve.getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
				//if (cp.isHead() && !cp.isHidden() && !cp.isStartHook() && !cp.isEndHook()) {
				if (cp.isHead() && !cp.isHidden()) {
					p3.set(cp.getPosition());
					transformationMatrix.transform(p3);
					if (p3.x >= ax && p3.x <= bx && p3.y >= ay && p3.y <= by) {
						selection.addControlPoint(cp);
					}
				}
			}
		}
		return (selection.getSize() != 0) ? selection : null;
	}
	
	public static PointWeightSelection createMagnetSelection(ControlPoint target, Model model) {
		PointWeightSelection selection = new PointWeightSelection();
		Point3f p3Target = target.getPosition();
		float fDistance;
		float fFactor;
		for (Curve curve = model.getFirstCurve(); curve != null; curve = curve.getNext()) {
			for (ControlPoint cp = curve.getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
				if (cp.isHead() && ! cp.isHidden()) {
					fDistance = p3Target.distance(cp.getPosition());
					if (fDistance < fRadius) {
						fFactor = 0.5f + (float)Math.cos(Math.PI * fDistance / fRadius)/2;
						selection.addControlPoint(cp,fFactor);
					}
				}
			}
		}
		return (selection.getSize() != 0) ? selection : null;
	}
}
