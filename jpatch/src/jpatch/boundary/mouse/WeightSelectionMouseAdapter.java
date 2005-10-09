package jpatch.boundary.mouse;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.vecmath.*;

import jpatch.boundary.*;
import jpatch.entity.*;

//import jpatch.control.edit.*;

public class WeightSelectionMouseAdapter extends JPatchMouseAdapter {

	private int iStartX;
	private int iStartY;
	
	private int iEndX, iEndX1, iEndX2;
	private int iEndY, iEndY1, iEndY2;
	
	private JPatchSettings settings = JPatchSettings.getInstance();

	public void mousePressed(MouseEvent mouseEvent) {
		iStartX = iEndX = mouseEvent.getX();
		iStartY = iEndY = mouseEvent.getY();
		((Component) mouseEvent.getSource()).addMouseMotionListener(this);
	}
	
	public void mouseDragged(MouseEvent mouseEvent) {
		Graphics2D g2 = (Graphics2D)((Component)mouseEvent.getSource()).getGraphics();
		int eventX = mouseEvent.getX();
		int eventY = mouseEvent.getY();
		drawLine(g2);
		iEndX = eventX;
		iEndY = eventY;
		int dx = iEndX - iStartX;
		int dy = iEndY - iStartY;
		iEndX1 = iEndX + dy / 24;
		iEndY1 = iEndY - dx / 24;
		iEndX2 = iEndX - dy / 24;
		iEndY2 = iEndY + dx / 24;
		
		drawLine(g2);
	}
	
	public void mouseReleased(MouseEvent mouseEvent) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
		Graphics2D g2 = (Graphics2D)((Component)mouseEvent.getSource()).getGraphics();
		((Component) mouseEvent.getSource()).removeMouseMotionListener(this);
		drawLine(g2);
		MainFrame.getInstance().getMeshToolBar().reset();
		Selection selection = MainFrame.getInstance().getSelection();
		Point3f p = new Point3f();
		float dx = iEndX - iStartX;
		float dy = iEndY - iStartY;
		boolean x = (Math.abs(dx) > Math.abs(dy));
		float w;
		//System.out.println(iStartX + "," + iStartY + " " + iEndX + "," + iEndY);
		//boolean x = Math.abs(dx) > Math.abs(dy);
		//System.out.println(x + " " + dx + " " + dy);
		for (Iterator it = selection.getObjects().iterator(); it.hasNext(); ) {
			Transformable transformable = (Transformable) it.next();
			p.set(((Transformable) transformable).getPosition());
			viewDef.getScreenMatrix().transform(p);
			p.x -= iStartX;
			p.y -= iStartY;
			//System.out.println(p);
			if (x) {
				w = (p.x + p.y * (dy/dx)) / dx;
			} else
				w = (p.y + p.x * (dx/dy)) / dy;
			w = (w < 0) ? 0 : (w > 1) ? 1 : w;
			selection.getMap().put(transformable, new Float(w));
//			System.out.println(w);
		}
		MainFrame.getInstance().getJPatchScreen().update_all();
	}
	
	private void drawLine(Graphics2D g2) {
		final int N = 1;
		g2.setXORMode(new Color(settings.cBackground.getRGB() ^ settings.cSelected.getRGB()));
		int[] x = new int[4];
		int[] y = new int[4];
		for (int i = 0; i < N; i++) {
			x[0] = iStartX + (iEndX1 - iStartX) * i / N;
			y[0] = iStartY + (iEndY1 - iStartY) * i / N;
			x[1] = iStartX + (iEndX1 - iStartX) * (i + 1) / N;
			y[1] = iStartY + (iEndY1 - iStartY) * (i + 1) / N;
			x[3] = iStartX + (iEndX2 - iStartX) * i / N;
			y[3] = iStartY + (iEndY2 - iStartY) * i / N;
			x[2] = iStartX + (iEndX2 - iStartX) * (i + 1) / N;
			y[2] = iStartY + (iEndY2 - iStartY) * (i + 1) / N;
			g2.fillPolygon(x, y , 4);
		}
	}
//	public Selection getSelection(ViewDefinition viewDefinition) {
//		switch(iMode) {
//			case RECTANGLE:
//				return Selection.createRectangularPointSelection(iCornerX,iCornerY,iCornerX + iDeltaX, iCornerY + iDeltaY, viewDefinition.getScreenMatrix(), MainFrame.getInstance().getModel());
//		}
//		return null;
//	}
	
				
				//public void mouseReleased(MouseEvent mouseEvent) {
	//	if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
	//		Component compSource = (Component)mouseEvent.getSource();
	//		switch(iMode) {
	//			case RECTANGLE:
	//				ViewDefinition viewDefinition = ((Viewport)(mouseEvent.getSource())).getViewDefinition();
	//				PointSelection selection = SelectionFactory.createRectangularPointSelection(iCornerX,iCornerY,iCornerX + iDeltaX, iCornerY + iDeltaY, viewDefinition.getScreenMatrix(), MainFrame.getInstance().getModel());
	//				PointSelection ps = MainFrame.getInstance().getPointSelection();
	//				if (ps != null) {
	//					if (selection != null) {
	//						if (mouseEvent.isShiftDown()) {
	//							ps.addPointSelection(selection);
	//						} else if (mouseEvent.isControlDown()) {
	//							ps.xorPointSelection(selection);
	//						} else {
	//							MainFrame.getInstance().setSelection(selection);
	//						}
	//					} else {
	//						if (!mouseEvent.isShiftDown() && !mouseEvent.isControlDown()) {
	//							MainFrame.getInstance().setSelection(null);
	//						}
	//					}
	//				} else {
	//					MainFrame.getInstance().setSelection(selection);
	//				}
	//				ps = MainFrame.getInstance().getPointSelection();
	//				if (ps != null) { 
	//					ps.resetPivotToCenter();
	//				}
	//				//MainFrame.getInstance().getJPatchScreen().prepareBackground(compSource);
	//				break;
	//		}
	//		//MouseListener[] aml = compSource.getMouseListeners();
	//		//MouseMotionListener[] amml = compSource.getMouseMotionListeners();
	//		//for (int i = 0; i < aml.length; i++) {
	//		//	System.out.println("\t" + aml[i]);
	//		//}
	//		//for (int i = 0; i < amml.length; i++) {
	//		//	System.out.println("\t" + amml[i]);
	//		//}
	//		compSource.removeMouseListener(this);
	//		compSource.removeMouseMotionListener(this);
	//		MainFrame.getInstance().getTree().setSelectionPath(new TreePath(MainFrame.getInstance().getRootTreenode()));
	//		MainFrame.getInstance().getJPatchScreen().update_all();
	//		
	//	}
	//}
	
//	private void drawSelectionRectangle(Graphics g, int iCornerX, int iCornerY, int iDeltaX, int iDeltaY) {
//		g.drawLine(iCornerX,iCornerY,iCornerX + iDeltaX,iCornerY);
//		g.drawLine(iCornerX + iDeltaX,iCornerY,iCornerX + iDeltaX,iCornerY + iDeltaY);
//		g.drawLine(iCornerX,iCornerY + iDeltaY,iCornerX + iDeltaX,iCornerY + iDeltaY);
//		g.drawLine(iCornerX,iCornerY,iCornerX,iCornerY + iDeltaY);
//	}
}
