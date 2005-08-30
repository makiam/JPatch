package jpatch.boundary.tools;

import java.awt.event.*;
import javax.vecmath.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;
import jpatch.entity.*;

public class TangentTool extends JPatchTool {
	
	private TangentHandle handleIn = new TangentHandle(new Color3f(JPatchSettings.getInstance().cTangent), null, TangentHandle.IN);
	private TangentHandle handleOut = new TangentHandle(new Color3f(JPatchSettings.getInstance().cTangent), null, TangentHandle.OUT);
	
	private static final int GHOST_FACTOR = JPatchSettings.getInstance().iGhost;
	
	//private PointSelection ps;

	public TangentHandle isHit(ViewDefinition viewDef, int x, int y) {
		Point3f p3 = new Point3f();
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		if (ps != null && ps.isSingle()) {
			ControlPoint cp = ps.getControlPoint();
			if (ps.isCurve() || cp.getPrevAttached() == null) {
				handleIn.setControlPoint(cp);
				handleOut.setControlPoint(cp);
				if (cp.getNext() != null && handleOut.isHit(viewDef, x, y, p3)) return handleOut;
				if (cp.getPrev() != null && handleIn.isHit(viewDef, x, y, p3)) return handleIn;
			}
		}
		return null;
	}
	
	public void paint(ViewDefinition viewDef) {
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		Matrix4f m4View = viewDef.getMatrix();
		JPatchDrawable2 drawable = viewDef.getDrawable();
		if (ps != null && ps.isSingle()) {
			ControlPoint cp = ps.getControlPoint();
			if (ps.isCurve() || cp.getPrevAttached() == null) {
				handleIn.setControlPoint(cp);
				handleOut.setControlPoint(cp);
				Point3f p3Pos = new Point3f(cp.getPosition());
				Point3f p3Tangent = new Point3f();
				m4View.transform(p3Pos);
				drawable.setColor(new Color3f(JPatchSettings.getInstance().cTangent)); // FIXME
				if (cp.getNext() != null) {
					p3Tangent.set(cp.getOutTangent());
					m4View.transform(p3Tangent);
					drawable.drawLine(p3Pos, p3Tangent);
					handleOut.paint(viewDef);
				}
				if (cp.getPrev() != null) {
					p3Tangent.set(cp.getInTangent());
					m4View.transform(p3Tangent);
					drawable.drawLine(p3Pos, p3Tangent);
					handleIn.paint(viewDef);
				}
			}
		}
	}
}

