package jpatch.boundary.tools;

import java.awt.event.*;
import javax.vecmath.*;
import jpatch.boundary.*;
import jpatch.boundary.settings.Settings;
import jpatch.entity.*;

public class TangentTool extends JPatchTool {
	
	private TangentHandle handleIn = new TangentHandle(Settings.getInstance().colors.tangents, null, TangentHandle.IN);
	private TangentHandle handleOut = new TangentHandle(Settings.getInstance().colors.tangents, null, TangentHandle.OUT);
	
//	private static final int GHOST_FACTOR = JPatchUserSettings.getInstance().iGhost;
	
	//private PointSelection ps;

	TangentTool() { }
	
	public TangentHandle isHit(ViewDefinition viewDef, int x, int y) {
		Point3f p3 = new Point3f();
		Selection selection = MainFrame.getInstance().getSelection();
		if (selection != null && selection.getMap().size() == 1 && selection.getHotObject() instanceof ControlPoint) {
			ControlPoint cp = (ControlPoint) selection.getHotObject();
			if (selection.getDirection() != 0 || cp.getPrevAttached() == null) {
				handleIn.setControlPoint(cp);
				handleOut.setControlPoint(cp);
				if (cp.getNext() != null && handleOut.isHit(viewDef, x, y, p3)) return handleOut;
				if (cp.getPrev() != null && handleIn.isHit(viewDef, x, y, p3)) return handleIn;
			}
		}
		return null;
	}
	
	public void paint(ViewDefinition viewDef) {
		Matrix4f m4View = viewDef.getMatrix();
		JPatchDrawable2 drawable = viewDef.getDrawable();
		Selection selection = MainFrame.getInstance().getSelection();
		if (selection != null && selection.getMap().size() == 1 && selection.getHotObject() instanceof ControlPoint) {
			ControlPoint cp = (ControlPoint) selection.getHotObject();
			if (selection.getDirection() != 0 || cp.getPrevAttached() == null) {
				handleIn.setControlPoint(cp);
				handleOut.setControlPoint(cp);
				Point3f p3Pos = new Point3f(cp.getPosition());
				Point3f p3Tangent = new Point3f();
				m4View.transform(p3Pos);
				drawable.setColor(Settings.getInstance().colors.tangents); // FIXME
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

