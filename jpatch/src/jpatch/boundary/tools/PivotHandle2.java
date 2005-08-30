package jpatch.boundary.tools;

import java.awt.*;
import java.awt.event.*;
import javax.vecmath.*;

import jpatch.boundary.*;

public class PivotHandle2 extends Handle {
	
	public PivotHandle2(Color3f color) {
		super(new Point3f(), color);
		//iHitSize = 6;
	}
	
	public Point3f getPosition(ViewDefinition viewDef) {
		return MainFrame.getInstance().getSelection().getPivot();
	}
	
	public void paint(ViewDefinition viewDef) {
//		p3Position.set(MainFrame.getInstance().getPointSelection().getPivot());
		paint(viewDef, MainFrame.getInstance().getSelection().getOrientation());
	}
	
	public void mouseDragged(MouseEvent mouseEvent) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
		viewDef.setZ(getPosition(viewDef));
		Point3f p = getPosition(viewDef);
		MainFrame.getInstance().getConstraints().setPointPosition(p, viewDef.get3DPosition(mouseEvent.getX(), mouseEvent.getY()));
		MainFrame.getInstance().getSelection().getPivot().set(p);
		//getPosition().set(viewDefinition.get3DPosition(mouseEvent.getX(), mouseEvent.getY()));
		//rotateTool.setRadius();
		MainFrame.getInstance().getJPatchScreen().single_update((Component)mouseEvent.getSource());
	}
}

