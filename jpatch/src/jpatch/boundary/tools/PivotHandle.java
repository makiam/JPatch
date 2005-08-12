package jpatch.boundary.tools;

import java.awt.*;
import java.awt.event.*;
import javax.vecmath.*;
import jpatch.boundary.*;

public class PivotHandle extends Handle {
	private RotateTool rotateTool;
	
	public PivotHandle(RotateTool rotateTool, Color3f color) {
		super(null, color);
		this.rotateTool = rotateTool;
		//iHitSize = 6;
	}
	
	public Point3f getPosition(ViewDefinition viewDef) {
		return rotateTool.getPivot();
	}
	
	public void mouseDragged(MouseEvent mouseEvent) {
//		ViewDefinition viewDefinition = ((Viewport)mouseEvent.getSource()).getViewDefinition();
//		viewDefinition.setZ(getPosition());
//		MainFrame.getInstance().getConstraints().setPointPosition(getPosition(),viewDefinition.get3DPosition(mouseEvent.getX(), mouseEvent.getY()));
//		//getPosition().set(viewDefinition.get3DPosition(mouseEvent.getX(), mouseEvent.getY()));
//		rotateTool.setRadius();
//		MainFrame.getInstance().getJPatchScreen().single_update((Component)mouseEvent.getSource());
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
		viewDef.setZ(getPosition(viewDef));
		MainFrame.getInstance().getConstraints().setPointPosition(getPosition(viewDef),viewDef.get3DPosition(mouseEvent.getX(), mouseEvent.getY()));
		//getPosition().set(viewDefinition.get3DPosition(mouseEvent.getX(), mouseEvent.getY()));
		rotateTool.setRadius();
		MainFrame.getInstance().getJPatchScreen().single_update((Component)mouseEvent.getSource());
	}
	
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
//		
//		SimpleShape shape;
//		
//		if (!bActive) {
//			shape = SimpleShape.createCube((float)iSize / m4View.getScale(), r, g, b);
//		} else {
//			//System.out.println("active Handle pos = " + p3Position);
//			shape = SimpleShape.createCube((float)iSize / m4View.getScale(), 1.0f, 1.0f, 1.0f);
//		}
//		//shape.transform(MainFrame.getInstance().getPointSelection().getRotation());
//		//shape.transform(m3Cube);
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
//		
//		//m4.setIdentity();
//		m4.set(rotateTool.getRotA());
//		shape.transform(m4);
//		
//		Vector3f v3 = new Vector3f(getPosition());
//		//v3.scale(rotateTool.getRadius());
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
//		
//		
//		//m4.set(new Vector3f(rotateTool.getPivot()));
//		//shape.transform(m4);
//		
//		drawable.drawSimpleShape(shape, m4View);
//	}
}

