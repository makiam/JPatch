package jpatch.boundary.tools;

import java.awt.*;
import java.awt.event.*;
import javax.vecmath.*;
import jpatch.boundary.*;

public class DefaultHandle extends Handle {
	private DefaultTool defaultTool;
	private Point3f p3OldPosition = new Point3f();
	
	
	public DefaultHandle(DefaultTool defaultTool, Point3f position, Color color) {
		super(position, color);
		this.defaultTool = defaultTool;
		iHitSize = 4;
	}
	
	public void setOldPosition() {
		p3OldPosition.set(getPosition());
	}
	
	public void mouseDragged(MouseEvent mouseEvent) {
		
		ViewDefinition viewDefinition = ((Viewport)mouseEvent.getSource()).getViewDefinition();
		Matrix4f m4View = viewDefinition.getMatrix();
		
		Point3f p3Pos = new Point3f(p3OldPosition);
	
		//p3Pos = p3OldPosition;
		m4View.transform(p3Pos);
		p3Pos.z = 0;
		
		Point3f p3Pivot = new Point3f(MainFrame.getInstance().getPointSelection().getPivot());
		m4View.transform(p3Pivot);
		p3Pivot.z = 0;
		
		Point3f p3Mouse = new Point3f((float)mouseEvent.getX(),(float)mouseEvent.getY(),0);
		
		Vector3f v3Handle = new Vector3f(p3Pos);
		v3Handle.sub(p3Pivot);
		Vector3f v3Mouse = new Vector3f(p3Mouse);
		v3Mouse.sub(p3Pivot);
		Vector3f v3Scale = new Vector3f(8.0f,8.0f,0.0f);
		//m4View.transform(v3Scale);
		
		float sign = Float.compare(v3Handle.dot(v3Mouse),0.0f);
		
		//float scale = 5f / m4View.getScale();
		
		v3Handle.x = Math.abs(v3Handle.x);
		v3Handle.y = Math.abs(v3Handle.y);
		v3Handle.z = Math.abs(v3Handle.z);
		
		v3Handle.sub(v3Scale);
		
		
		
		v3Mouse.x = Math.abs(v3Mouse.x);
		v3Mouse.y = Math.abs(v3Mouse.y);
		v3Mouse.z = Math.abs(v3Mouse.z);
		
		v3Mouse.sub(v3Scale);
		
		v3Mouse.x = Math.max(v3Mouse.x, 5.0f / m4View.getScale());
		v3Mouse.y = Math.max(v3Mouse.y, 5.0f / m4View.getScale());
		v3Mouse.z = Math.max(v3Mouse.z, 5.0f / m4View.getScale());
		
		float lh = v3Handle.length();
		float lm = v3Mouse.length();
		
		v3Handle.normalize();
		v3Mouse.normalize();
		
		float sc = v3Handle.dot(v3Mouse) / lh * lm * sign;
		
		defaultTool.scale(sc, (Viewport)mouseEvent.getSource());
	}
	
	public void paint(Viewport viewport, JPatchDrawable drawable) {
		//Point3f p3 = getTransformedPosition(viewport.getViewDefinition().getMatrix());
		float r, g, b;
		//if (bActive) {
			r = (float)cPassive.getRed() / 255;
			g = (float)cPassive.getGreen() / 255;
			b = (float)cPassive.getBlue() / 255;
			//drawable.setColor(cActive);
		/*} else {
			r = (float)cPassive.getRed() / 255;
			g = (float)cPassive.getGreen() / 255;
			b = (float)cPassive.getBlue() / 255;
			//drawable.setColor(cPassive);
		}*/
		//int x = (int)p3.x;
		//int y = (int)p3.y;
		/*
		drawable.drawPoint3D(p3, iSize);
		drawable.drawLine(x - iSize, y - iSize, x + iSize, y - iSize);
		drawable.drawLine(x - iSize, y + iSize, x + iSize, y + iSize);
		drawable.drawLine(x - iSize, y - iSize, x - iSize, y + iSize);
		drawable.drawLine(x + iSize, y - iSize, x + iSize, y + iSize);
		*/
		Matrix4f m4View = viewport.getViewDefinition().getMatrix();
		
		SimpleShape shape;
		
		if (!bActive) {
			shape = SimpleShape.createCube((float)iSize / m4View.getScale(), r, g, b);
		} else {
			//System.out.println("active Handle pos = " + p3Position);
			shape = SimpleShape.createCube((float)iSize / m4View.getScale(), 1.0f, 1.0f, 1.0f);
		}
		shape.transform(MainFrame.getInstance().getPointSelection().getRotation());
		//shape.transform(m3Cube);
		/*
		if (bActive) {
			shape.getMaterialProperties().ambient = 1.0f;
			//shape.getMaterialProperties().diffuse = 0.5f;
			//shape.getMaterialProperties().roughness = 0.5f;
			shape.getMaterialProperties().red = 1.0f;
			shape.getMaterialProperties().green = 1.0f;
			shape.getMaterialProperties().blue = 1.0f;
		}
		*/
		//drawable.drawSimpleShape(shape, m4View);
		//shape.setColor(1.0f,0.0f,0.0f);
		Matrix4f m4 = new Matrix4f();
		Vector3f v3 = new Vector3f(p3Position);
		//v3.scale(rotateTool.getRadius());
		m4.set(v3);
		/*
		m4.set(rotateTool.getRotA());
		m4.m03 = p3.x;
		m4.m13 = p3.y;
		m4.m23 = p3.z;
		shape.transform(m4);
		m4.setIdentity();
		*/
		shape.transform(m4);
		/*
		m4.setIdentity();
		m4.set(rotateTool.getRotA());
		shape.transform(m4);
		m4.set(new Vector3f(rotateTool.getPivot()));
		shape.transform(m4);
		*/
		drawable.drawSimpleShape(shape, m4View);
	}
}

