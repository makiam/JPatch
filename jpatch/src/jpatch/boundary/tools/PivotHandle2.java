package jpatch.boundary.tools;

import java.awt.*;
import java.awt.event.*;
import javax.vecmath.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;

public class PivotHandle2 extends Handle {
	
	private static final int GHOST_FACTOR = JPatchSettings.getInstance().iGhost;
	
	public PivotHandle2(Color color) {
		super(null, color);
		//iHitSize = 6;
	}
	
	public Point3f getPosition() {
		return MainFrame.getInstance().getPointSelection().getPivot();
	}
	
	public void paint(Viewport viewport, JPatchDrawable drawable) {
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		Matrix4f m4View = viewport.getViewDefinition().getMatrix();
		float len = 24f/m4View.getScale();;
		Point3f p1 = new Point3f();
		Point3f p2 = new Point3f();
		p1.set(0,0,0);
		p2.set(len,0,0);
		ps.getRotation().transform(p2);
		p1.add(ps.getPivot());
		p2.add(ps.getPivot());
		m4View.transform(p1);
		m4View.transform(p2);
		drawable.setColor(settings.cX);
		drawable.drawGhostLine3D(p1,p2,GHOST_FACTOR);
		//g2.drawLine((int)p1.x,(int)p1.y,(int)p2.x,(int)p2.y);
		p1.set(0,0,0);
		p2.set(0,len,0);
		ps.getRotation().transform(p2);
		p1.add(ps.getPivot());
		p2.add(ps.getPivot());
		m4View.transform(p1);
		m4View.transform(p2);
		drawable.setColor(settings.cY);
		drawable.drawGhostLine3D(p1,p2,GHOST_FACTOR);
		//g2.drawLine((int)p1.x,(int)p1.y,(int)p2.x,(int)p2.y);
		p1.set(0,0,0);
		p2.set(0,0,len);
		ps.getRotation().transform(p2);
		p1.add(ps.getPivot());
		p2.add(ps.getPivot());
		m4View.transform(p1);
		m4View.transform(p2);
		drawable.setColor(settings.cZ);
		drawable.drawGhostLine3D(p1,p2,GHOST_FACTOR);
		
		
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
		//Matrix4f m4View = viewport.getViewDefinition().getMatrix();
		
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
		Vector3f v3 = new Vector3f(getPosition());
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
	
		//g2.drawLine((int)p1.x,(int)p1.y,(int)p2.x,(int)p2.y);
	}
	
	public void mouseDragged(MouseEvent mouseEvent) {
		ViewDefinition viewDefinition = ((Viewport)mouseEvent.getSource()).getViewDefinition();
		viewDefinition.setZ(getPosition());
		MainFrame.getInstance().getConstraints().setPointPosition(getPosition(),viewDefinition.get3DPosition(mouseEvent.getX(), mouseEvent.getY()));
		//getPosition().set(viewDefinition.get3DPosition(mouseEvent.getX(), mouseEvent.getY()));
		//rotateTool.setRadius();
		MainFrame.getInstance().getJPatchScreen().single_update((Component)mouseEvent.getSource());
	}
}

