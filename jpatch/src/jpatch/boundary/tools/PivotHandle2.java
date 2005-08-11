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
	
	public Point3f getPosition() {
		return MainFrame.getInstance().getPointSelection().getPivot();
	}
	
	public void paint(ViewDefinition viewDef) {
		p3Position.set(MainFrame.getInstance().getPointSelection().getPivot());
		paint(viewDef, MainFrame.getInstance().getPointSelection().getRotation());
	}
	
	public void mouseDragged(MouseEvent mouseEvent) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
		viewDef.setZ(getPosition());
		MainFrame.getInstance().getConstraints().setPointPosition(getPosition(),viewDef.get3DPosition(mouseEvent.getX(), mouseEvent.getY()));
		//getPosition().set(viewDefinition.get3DPosition(mouseEvent.getX(), mouseEvent.getY()));
		//rotateTool.setRadius();
		MainFrame.getInstance().getJPatchScreen().single_update((Component)mouseEvent.getSource());
	}
}

