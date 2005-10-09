package jpatch.boundary.tools;

import java.awt.*;
import java.awt.event.*;
import javax.vecmath.*;
import jpatch.boundary.*;

public class RotateHandle extends Handle {
	protected Matrix3f m3 = new Matrix3f();
	protected Matrix3f m3Cube;
	protected RotateTool rotateTool;
	protected Point3f p3 = new Point3f();
	protected Vector3f v3Axis;
	protected Vector3f v3AxisA = new Vector3f();
	protected AxisAngle4f axisAngle = new AxisAngle4f();
	protected float angle = 0;
//	protected float fScale = 1;
	
	public RotateHandle(Point3f position, RotateTool rotateTool, Color3f color, Vector3f axis, Matrix3f orientation) {
		super(position, color);
		this.rotateTool = rotateTool;
//		m3Cube = m;
		if (orientation != null)
			shape.transform(orientation);
		v3Axis = axis;
		//iHitSize = 6;
	}
	
	public Point3f getPosition(ViewDefinition viewDef) {
		p3.set(p3Position);
		p3.scale(rotateTool.getRadius(viewDef.getScreenMatrix().getScale()));
		//p3.scale(radius);
		rotateTool.getInitialRotation().transform(p3);
		Matrix3f m = new Matrix3f();
		m.set(rotateTool.getRotation());
		m.transform(p3);
		
		p3.add(rotateTool.getPivot());
		return p3;
	}
	/*
	public void mousePressed(MouseEvent mouseEvent) {
		angle = 0;
		super.mousePressed(mouseEvent);
	}
	*/
	
//	public void paint(Viewport viewport, JPatchDrawable drawable) {
//		//Point3f p3 = getTransformedPosition(viewport.getViewDefinition().getMatrix());
//		float r, g, b;
//		//if (bActive) {
//			r = (float)cPassive.getRed() / 255;
//			g = (float)cPassive.getGreen() / 255;
//			b = (float)cPassive.getBlue() / 255;
//			//drawable.setColor(cActive);
//		/*} else {
//			r = (float)cPassive.getRed() / 255;
//			g = (float)cPassive.getGreen() / 255;
//			b = (float)cPassive.getBlue() / 255;
//			//drawable.setColor(cPassive);
//		}*/
//		//int x = (int)p3.x;
//		//int y = (int)p3.y;
//		/*
//		drawable.drawPoint3D(p3, iSize);
//		drawable.drawLine(x - iSize, y - iSize, x + iSize, y - iSize);
//		drawable.drawLine(x - iSize, y + iSize, x + iSize, y + iSize);
//		drawable.drawLine(x - iSize, y - iSize, x - iSize, y + iSize);
//		drawable.drawLine(x + iSize, y - iSize, x + iSize, y + iSize);
//		*/
//		Matrix4f m4View = viewport.getViewDefinition().getMatrix();
//		float scale = m4View.getScale();
//		fScale = scale;
//		
//		SimpleShape shape;
//		
//		if (!bActive) {
//			shape = SimpleShape.createCube((float)iSize / m4View.getScale(), r, g, b);
//		} else {
//			shape = SimpleShape.createCube((float)iSize / m4View.getScale(), 1.0f, 1.0f, 1.0f);
//		}
//		shape.transform(m3Cube);
//		/*
//		if (bActive) {
//			shape.getMaterialProperties().ambient = 1.0f;
//			//shape.getMaterialProperties().diffuse = 0.5f;
//			//shape.getMaterialProperties().roughness = 0.5f;
//			shape.getMaterialProperties().red = 1.0f;
//			shape.getMaterialProperties().green = 1.0f;
//			shape.getMaterialProperties().blue = 1.0f;
//		}
//		*/
//		//drawable.drawSimpleShape(shape, m4View);
//		//shape.setColor(1.0f,0.0f,0.0f);
//		Matrix4f m4 = new Matrix4f();
//		Vector3f v3 = new Vector3f(p3Position);
//		v3.scale(rotateTool.getRadius(scale));
//		m4.set(v3);
//		/*
//		m4.set(rotateTool.getRotA());
//		m4.m03 = p3.x;
//		m4.m13 = p3.y;
//		m4.m23 = p3.z;
//		shape.transform(m4);
//		m4.setIdentity();
//		*/
//		shape.transform(m4);
//		m4.setIdentity();
//		m4.set(rotateTool.getRotA());
//		shape.transform(m4);
//		m4.set(new Vector3f(rotateTool.getPivot()));
//		shape.transform(m4);
//		drawable.drawSimpleShape(shape, m4View);
//	}
	
	public void mouseDragged(MouseEvent mouseEvent) {	
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
		Matrix4f m4View = viewDef.getScreenMatrix();
		
		int dx = mouseEvent.getX() - iMouseX;
		int dy = mouseEvent.getY() - iMouseY;
//		iMouseX = mouseEvent.getX();
//		iMouseY = mouseEvent.getY();
		//iMouseX = mouseEvent.getX();
		//iMouseY = mouseEvent.getY();
		//float angle = (float)dx / 3f;
		
		
		
		float scale = m4View.getScale();
		
		Vector3f V = new Vector3f();
		Vector3f P = new Vector3f(p3Position);
		Vector3f A = new Vector3f(v3Axis);
//		rotateTool.getRotation().transform(P);
//		rotateTool.getRotation().transform(A);
		rotateTool.getInitialRotation().transform(P);
		rotateTool.getInitialRotation().transform(A);
		
		V.cross(A, P);
//		System.out.println("A=" + A + " P=" + P + " V=" + V);
//		rotateTool.getInitialRotation().transform(V);
//		rotateTool.getRotation().transform(V);
		m4View.transform(V);
		
		V.z = 0;
		V.normalize();
		
		Vector3f M = new Vector3f(dx, dy, 0);
		float l = M.length() / m4View.getScale() / rotateTool.getRadius(scale) * 180 / (float)Math.PI;
		M.normalize();
		
		System.out.println("V=" + V + " M=" + M);
		//System.out.println("r = " + rotateTool.getRadius() + "s = " + m4View.getScale());
		//System.out.println("V = " + V + " M = " + M);
		
		float newAngle = l * M.dot(V);
		if (!mouseEvent.isShiftDown()) {
			newAngle = Math.round(newAngle / 5f) * 5f;
		}
		
		if (newAngle != angle) {
			angle = newAngle;
			
			//v3AxisA.set(v3Axis);
			//v3AxisA.normalize();
			rotateTool.getRotation().set(A, angle / 360f * 2f * (float)Math.PI);
			//m3.set(axisAngle);
			//rotateTool.getRotation().set(rotateTool.getInitialRotation());
			//rotateTool.getRotation().mul(m3);
			//rotateTool.getRotation().set(m3);
			if (!mouseEvent.isControlDown()) rotateTool.rotate();
			MainFrame.getInstance().getJPatchScreen().single_update(viewDef.getDrawable().getComponent());
		}
	}
}

