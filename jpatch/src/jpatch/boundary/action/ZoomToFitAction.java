package jpatch.boundary.action;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.vecmath.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;
import jpatch.entity.*;

public final class ZoomToFitAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1261428944588643536L;

	public ZoomToFitAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/zoomtofit.png")));
		putValue(Action.SHORT_DESCRIPTION,"Zoom to fit");
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		zoomToFit(MainFrame.getInstance().getJPatchScreen().getActiveViewport());
	}
	
	public static void zoomToFit(Viewport2 viewport) {
		ViewDefinition viewdef = viewport.getViewDefinition();
		//if (MainFrame.getInstance().getMeshToolBar().getMode() != MeshToolBar.VIEW_ZOOM) {
		//	MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
		//	MainFrame.getInstance().getJPatchScreen().addMouseListeners(new ChangeViewMouseListener(MouseEvent.BUTTON1,ChangeViewMouseListener.ZOOM));
		//	MainFrame.getInstance().getMeshToolBar().setMode(MeshToolBar.VIEW_ZOOM);
		//} else {
		//	MainFrame.getInstance().getMeshToolBar().reset();
		//}
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		float left = Float.MAX_VALUE;
		float right = -Float.MAX_VALUE;
		float bottom = Float.MAX_VALUE;
		float top = -Float.MAX_VALUE;
		Point3f p3 = new Point3f();
		Matrix4f m4View = viewdef.getScreenMatrix();
		//Matrix3f m3RotScale = new Matrix3f();
		//m4View.getRotationScale(m3RotScale);
		boolean doit = true;
		if (ps != null && !ps.isSingle()) {
			ControlPoint[] acp = ps.getControlPointArray();
			for (int c = 0; c < acp.length; c++) {
				if (acp[c].isHead()) {
					p3.set(acp[c].getPosition());
					m4View.transform(p3);
					if (p3.x < left) left = p3.x;
					if (p3.x > right) right = p3.x;
					if (p3.y < bottom) bottom = p3.y;
					if (p3.y > top) top = p3.y;
				}
			}
		} else {
			ArrayList heads = MainFrame.getInstance().getModel().allHeads();
			int p = 0;
			for (Iterator it = heads.iterator(); it.hasNext(); ) {
				ControlPoint cp = (ControlPoint) it.next();
				if (!cp.isHidden()) {
					p3.set(cp.getPosition());
					m4View.transform(p3);
					if (p3.x < left) left = p3.x;
					if (p3.x > right) right = p3.x;
					if (p3.y < bottom) bottom = p3.y;
					if (p3.y > top) top = p3.y;
					p++;
				}
			}
			doit = (p >= 2);
		}
		if (doit) {
			//System.out.println(left + " " + right + " " + top + " " + bottom + " " + viewdef.getScale());
			//System.out.println(viewdef.getTranslateX() + " " + viewdef.getTranslateY());
			float centerX = (left + right) / 2f;
			float centerY = (top + bottom) / 2f;
			float dimX = viewdef.getWidth() / 2f;
			float dimY = viewdef.getHeight() /2f;
			float sizeX = right - centerX;
			float sizeY = top - centerY;
			if (sizeX > 0 || sizeY > 0) {
				//System.out.println(centerX + ":" + centerY);
				
				float scaleX = dimX / sizeX;
				float scaleY = dimY / sizeY;
				float scale = Math.min(scaleX, scaleY) * 0.9f;
				//viewdef.setScale(viewdef.getScale() * scale);
				viewdef.setLock(null);
				viewdef.moveView(-centerX / dimX + 1, (dimY - centerY) / dimX, false);
				viewdef.scaleView(scale);
				//viewdef.setTranslation(centerX, centerY);
				//viewdef.computeMatrix();
				//viewport.render();
			}
		}
	}
}

