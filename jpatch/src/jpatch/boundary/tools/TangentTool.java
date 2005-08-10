package jpatch.boundary.tools;

import javax.vecmath.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;
import jpatch.entity.*;

public class TangentTool extends JPatchTool {
	
	private TangentHandle handleIn = new TangentHandle(JPatchSettings.getInstance().cTangent, null, TangentHandle.IN);
	private TangentHandle handleOut = new TangentHandle(JPatchSettings.getInstance().cTangent, null, TangentHandle.OUT);
	
	private static final int GHOST_FACTOR = JPatchSettings.getInstance().iGhost;
	
	//private PointSelection ps;

	public TangentHandle isHit(Viewport viewport, int x, int y) {
		Point3f p3 = new Point3f();
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		if (ps != null && ps.isSingle()) {
			ControlPoint cp = ps.getControlPoint();
			if (ps.isCurve() || cp.getPrevAttached() == null) {
				handleIn.setControlPoint(cp);
				handleOut.setControlPoint(cp);
				if (cp.getNext() != null && handleOut.isHit(viewport, x, y, p3)) return handleOut;
				if (cp.getPrev() != null && handleIn.isHit(viewport, x, y, p3)) return handleIn;
			}
		}
		return null;
	}
	
	public void paint(Viewport viewport, JPatchDrawable drawable) {
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		Matrix4f m4View = viewport.getViewDefinition().getMatrix();
		if (ps != null && ps.isSingle()) {
			ControlPoint cp = ps.getControlPoint();
			if (ps.isCurve() || cp.getPrevAttached() == null) {
				handleIn.setControlPoint(cp);
				handleOut.setControlPoint(cp);
				Point3f p3Pos = new Point3f(cp.getPosition());
				Point3f p3Tangent = new Point3f();
				m4View.transform(p3Pos);
				drawable.setColor(JPatchSettings.getInstance().cTangent);
				if (cp.getNext() != null) {
					p3Tangent.set(cp.getOutTangent());
					m4View.transform(p3Tangent);
					drawable.drawGhostLine3D(p3Pos, p3Tangent, GHOST_FACTOR);
					handleOut.paint(viewport, drawable);
				}
				if (cp.getPrev() != null) {
					p3Tangent.set(cp.getInTangent());
					m4View.transform(p3Tangent);
					drawable.drawGhostLine3D(p3Pos, p3Tangent, GHOST_FACTOR);
					handleIn.paint(viewport, drawable);
				}
			}
		}
	}
}

