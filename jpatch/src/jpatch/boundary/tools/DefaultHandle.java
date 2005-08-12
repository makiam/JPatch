package jpatch.boundary.tools;

import java.awt.*;
import java.awt.event.*;
import javax.vecmath.*;
import jpatch.boundary.*;
import jpatch.entity.MaterialProperties;

public class DefaultHandle extends Handle {
	private DefaultTool defaultTool;
	private Point3f p3OldPosition = new Point3f();
	
	public DefaultHandle(DefaultTool defaultTool, Point3f position, Color3f color) {
		super(position, color);
		this.defaultTool = defaultTool;
		iHitSize = 4;
	}
	
	public void setOldPosition(ViewDefinition viewDef) {
		p3OldPosition.set(getPosition(viewDef));
	}
	
	public void mouseDragged(MouseEvent mouseEvent) {
		
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
		Matrix4f m4View = viewDef.getScreenMatrix();
		
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
		defaultTool.scale(sc, viewDef);
	}
	
	public void paint(ViewDefinition viewDef) {
		paint(viewDef, MainFrame.getInstance().getPointSelection().getRotation());
	}
}

