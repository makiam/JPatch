package jpatch.boundary.tools;

import java.awt.event.*;
import javax.vecmath.*;

import jpatch.boundary.*;

public class Handle 
implements MouseMotionListener {
	protected Point3f p3Position;
	protected Matrix4f m4Transform = new Matrix4f();
	protected SimpleShape shape = SimpleShape.createCube(1);
	protected float fSize = 3;
	protected int iHitSize = 8;
	protected Color3f c3Passive;
	protected Color3f c3Active = new Color3f(1, 1, 1);
	protected int iPassive;
	protected boolean bActive = false;
	protected int iMouseX;
	protected int iMouseY;
	
	protected JPatchUserSettings settings = JPatchUserSettings.getInstance();
		
	public Handle(Point3f position, Color3f color) {
		p3Position = position;
		c3Passive = color;
	}
	
	public Point3f getPosition(ViewDefinition viewDef) {
		return p3Position;
	}
	
//	public Point3f getTransformedPosition(ViewDefinition viewDef, Matrix4f matrix) {
//		Point3f p = new Point3f(getPosition(viewDef));
//		matrix.transform(matrix);
//		return p;
//	}
	
	public void setMouse(int x, int y) {
		iMouseX = x;
		iMouseY = y;
	}
	
	public void setActive(boolean active) {
		bActive = active;
	}
	
	public void paint(ViewDefinition viewDef) {
		paint(viewDef, null);
	}
	
	protected void paint(ViewDefinition viewDef, Matrix3f orientation) {
		if (bActive)
			shape.setColor(c3Active);
		else
			shape.setColor(c3Passive);
		m4Transform.setIdentity();
		if (orientation != null)
			m4Transform.setRotationScale(orientation);
		m4Transform.setScale(fSize / viewDef.getMatrix().getScale());
		m4Transform.m03 += getPosition(viewDef).x;
		m4Transform.m13 += getPosition(viewDef).y;
		m4Transform.m23 += getPosition(viewDef).z;
		shape.paint(viewDef, m4Transform, viewDef.getMatrix());
//		Point3f pp = getPosition(viewDef);
//		
//		viewDef.getDrawable().drawRect
	}
	
	public boolean isHit(ViewDefinition viewDef, int x, int y, Point3f hit) {
//		Point3f p3 = getTransformedPosition(viewDef.getScreenMatrix());
		Point3f p3 = new Point3f(getPosition(viewDef));
//		System.out.println(p3);
		viewDef.getScreenMatrix().transform(p3);
//		System.out.println("handle hit? " + x + " " + y + " " + p3);
//		System.out.println("handle isHit? x=" + x + " y=" + y + " hx=" + p3.x + " hy=" + p3.y);
		if (x >= p3.x - iHitSize && x <= p3.x + iHitSize && y >= p3.y - iHitSize && y <= p3.y + iHitSize) {
			hit.set(p3);
			return true;
		}
		return false;
	}
	
	public void mouseDragged(MouseEvent mouseEvent) {
		;
	}
	
	public void mouseMoved(MouseEvent mouseEvent) {
		;
	}
}

