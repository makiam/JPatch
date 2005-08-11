package jpatch.boundary.tools;

import java.awt.*;
import java.awt.event.*;
import javax.vecmath.*;
import jpatch.boundary.*;

public class RotateHandleS extends RotateHandle {	
	public RotateHandleS(Point3f position, RotateTool rotateTool, Color3f color) {
		super(position, rotateTool, color, null, null);
		v3Axis = new Vector3f(0,0,1);
		//iHitSize = 6;
	}
	
	public void paint(Viewport viewport, JPatchDrawable drawable) {
		//ViewDefinition viewDefinition = viewport.getViewDefinition();
		float r, g, b;
		r = (float)cPassive.getRed() / 255;
		g = (float)cPassive.getGreen() / 255;
		b = (float)cPassive.getBlue() / 255;
		
		/*
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
		*/
		Matrix4f m4View = viewport.getViewDefinition().getMatrix();
		float scale = m4View.getScale();
		
		SimpleShape shape;
		if (!bActive) {
			shape = SimpleShape.createCube((float)iSize / m4View.getScale(), r, g, b);
		} else {
			shape = SimpleShape.createCube((float)iSize / m4View.getScale(), 1.0f, 1.0f, 1.0f);
		}
		//shape.transform(m3Cube);
		/*if (bActive) {
			shape.getMaterialProperties().ambient = 1.0f;
			shape.getMaterialProperties().diffuse = 0.5f;
			shape.getMaterialProperties().roughness = 0.5f;
		}*/
		//drawable.drawSimpleShape(shape, m4View);
		//shape.setColor(1.0f,0.0f,0.0f);
		
		/*
		Matrix4f m4View = viewDefinition.getMatrix();
		p3.set(p3Position);
		v3Axis.set(0,0,1);
		axisAngle.set(v3Axis,rotateTool.getAlpha());
		m3.set(axisAngle);
		m3.transform(p3);
		p3.scale(rotateTool.getRadius());
		Vector3f v3 = new Vector3f();
		Matrix3f m3 = new Matrix3f();
		m4View.get(m3);
		m3.invert();
		m3.transform(p3);
		p3.add(rotateTool.getPivot());
		m4View.transform(p3);
		*/
		
		Matrix4f mm4 = new Matrix4f();
		Vector3f vv3 = new Vector3f(p3Position);
		vv3.scale(rotateTool.getRadius(scale));
		mm4.set(vv3);
		Matrix3f mm3 = new Matrix3f();
		mm3.setIdentity();
		/*
		m4.set(rotateTool.getRotA());
		m4.m03 = p3.x;
		m4.m13 = p3.y;
		m4.m23 = p3.z;
		shape.transform(m4);
		m4.setIdentity();
		*/
		shape.transform(mm4);
		
		AxisAngle4f aa = new AxisAngle4f();
		v3Axis.set(0,0,1);
		aa.set(v3Axis,rotateTool.getAlpha());
		mm3.set(aa);
		
		shape.transform(mm3);
		
		mm3 = new Matrix3f();
		mm4.setIdentity();
		//m4.set(rotateTool.getRotA());
		//shape.transform(m4);
		mm3.setIdentity();
		m4View.get(mm3);
		mm3.invert();
		
		shape.transform(mm3);
		
		mm4.set(new Vector3f(rotateTool.getPivot()));
		shape.transform(mm4);
		drawable.drawSimpleShape(shape, m4View);
	}
	
	public boolean isHit(Viewport viewport, int x, int y, Point3f hit) {
		ViewDefinition viewDefinition = viewport.getViewDefinition();
		Matrix4f m4View = viewDefinition.getMatrix();
		float scale = m4View.getScale();
		p3.set(p3Position);
		axisAngle.set(v3Axis,rotateTool.getAlpha());
		m3.set(axisAngle);
		m3.transform(p3);
		p3.scale(rotateTool.getRadius(scale));
		Matrix3f m3 = new Matrix3f();
		m4View.get(m3);
		m3.invert();
		m3.transform(p3);
		p3.add(rotateTool.getPivot());
		m4View.transform(p3);
		if (x >= p3.x - iSize && x <= p3.x + iSize && y >= p3.y - iSize && y <= p3.y + iSize) {
			hit.set(p3);
			return true;
		}
		return false;
	}
	
	public void mouseDragged(MouseEvent mouseEvent) {
		Matrix4f m4 = ((Viewport)mouseEvent.getSource()).getViewDefinition().getMatrix();
		float scale = m4.getScale();
		v3Axis.set(0,0,-1);
		m4.get(m3);
		m3.invert();
		m3.transform(v3Axis);
		v3Axis.normalize();
		int dx = mouseEvent.getX() - iMouseX;
		int dy = mouseEvent.getY() - iMouseY;
		//float angle = (float)dx / 3f;
		
		Vector3f V = new Vector3f();
		Vector3f P = new Vector3f(p3Position);
		Vector3f A = new Vector3f(0,0,1);
		//rotateTool.getRotA().transform(P);
		//rotateTool.getRot().transform(A);
		
		V.cross(A, P);
		
		//m4.transform(V);
		
		V.z = 0;
		V.normalize();
		
		Vector3f M = new Vector3f(dx, dy, 0);
		Matrix3f m3Helper = new Matrix3f();
		Vector3f v3Helper = new Vector3f(0,0,-1);
		m3Helper.set(new AxisAngle4f(v3Helper,rotateTool.getBeta()));
		m3Helper.transform(M);
		
		float l = M.length() / m4.getScale() / rotateTool.getRadius(scale) * 180 / (float)Math.PI;
		M.normalize();
		//System.out.println("r = " + rotateTool.getRadius() + "s = " + m4View.getScale());
		//System.out.println("V = " + V + " M = " + M);
		
		float newAngle = l * M.dot(V);
		
		if (!mouseEvent.isShiftDown()) {
			newAngle = Math.round(newAngle / 5f) * 5f;
		}
		
		if (angle != newAngle) {
			angle = newAngle;
			rotateTool.setAlpha(rotateTool.getBeta() + angle / 180f * (float)Math.PI);
			v3AxisA.set(v3Axis);
			m3.set(rotateTool.getRot());
			m3.invert();
			m3.transform(v3AxisA);
			v3AxisA.normalize();
			axisAngle.set(v3AxisA, angle / 180f * (float)Math.PI);
			//System.out.println(v3AxisA);
			m3.set(axisAngle);
			rotateTool.getRotA().set(rotateTool.getRot());
			rotateTool.getRotA().mul(m3);
			if (!mouseEvent.isControlDown()) rotateTool.rotate();
			MainFrame.getInstance().getJPatchScreen().single_update((Component)mouseEvent.getSource());
		}
	}
}

