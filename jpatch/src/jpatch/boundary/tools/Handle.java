package jpatch.boundary.tools;

import java.awt.*;
import java.awt.event.*;
import javax.vecmath.*;
import jpatch.boundary.*;

public class Handle 
implements MouseMotionListener {
	protected Point3f p3Position;
	protected int iSize = 3;
	protected int iHitSize = 4;
	protected Color cPassive;
	protected Color cActive = new Color(0xFF,0xFF,0xFF);
	protected int iPassive;
	protected int iActive = cActive.getRGB();
	protected boolean bActive = false;
	protected int iMouseX;
	protected int iMouseY;
	
	protected JPatchSettings settings = JPatchSettings.getInstance();

		
	public Handle(Point3f position, Color color) {
		p3Position = position;
		cPassive = color;
		iPassive = color.getRGB();
	}
	
	public Point3f getPosition() {
		return p3Position;
	}
	
	public Point3f getTransformedPosition(Matrix4f matrix) {
		Point3f p = new Point3f(getPosition());
		matrix.transform(p);
		return p;
	}
	
	public void setMouse(int x, int y) {
		iMouseX = x;
		iMouseY = y;
	}
	
	public void setActive(boolean active) {
		//if (active) System.out.println("active " + this + " pos = " + p3Position);
		bActive = active;
	}
	
	public void paint(Viewport viewport, JPatchDrawable drawable) {
		Point3f p3 = getTransformedPosition(viewport.getViewDefinition().getMatrix());
		if (bActive) {
			drawable.setColor(cActive);
		} else {
			drawable.setColor(cPassive);
		}
		int x = (int)p3.x;
		int y = (int)p3.y;
		drawable.drawPoint3D(p3, iSize);
		drawable.drawLine(x - iSize, y - iSize, x + iSize, y - iSize);
		drawable.drawLine(x - iSize, y + iSize, x + iSize, y + iSize);
		drawable.drawLine(x - iSize, y - iSize, x - iSize, y + iSize);
		drawable.drawLine(x + iSize, y - iSize, x + iSize, y + iSize);
		/*
		SimpleShape shape = SimpleShape.createCube(5f, 0.0f, 1.0f, 1.0f);
		//drawable.drawSimpleShape(shape, m4View);
		//shape.setColor(1.0f,0.0f,0.0f);
		Matrix4f m4 = new Matrix4f();
		m4.set(new Vector3f(p3));
		shape.transform(m4);
		m4.setIdentity();
		drawable.drawSimpleShape(shape, m4);
		*/
	}
	
	public boolean isHit(Viewport viewport, int x, int y, Point3f hit) {
		Point3f p3 = getTransformedPosition(viewport.getViewDefinition().getMatrix());
		//System.out.println("handle " + this + " " + p3 + " " + x + " " + y);
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

