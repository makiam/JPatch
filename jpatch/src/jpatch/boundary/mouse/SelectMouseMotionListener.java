package jpatch.boundary.mouse;

import java.awt.*;
import java.awt.event.*;
import jpatch.boundary.*;

//import jpatch.control.edit.*;

public class SelectMouseMotionListener implements MouseMotionListener {

	public static final int RECTANGLE = 1;
	public static final int POLYGON = 2;
	public static final int LASSO = 3;
	
	private int iMode = RECTANGLE;
	private int iCornerX;
	private int iCornerY;
	private int iDeltaX;
	private int iDeltaY;
	private int iMouseX;
	private int iMouseY;
	
	private JPatchSettings settings = JPatchSettings.getInstance();

	public SelectMouseMotionListener(int mouseX, int mouseY) {
		iMouseX = mouseX;
		iMouseY = mouseY;
	}
	
	public void mouseMoved(MouseEvent mouseEvent) {
		;
	}
	
	public void mouseDragged(MouseEvent mouseEvent) {
		switch(iMode) {
			case RECTANGLE:
				Graphics2D g2 = (Graphics2D)((Component)mouseEvent.getSource()).getGraphics();
				g2.setXORMode(new Color(settings.cBackground.getRGB() ^ settings.cSelection.getRGB()));
				g2.setStroke(new BasicStroke(1.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,0.0f,new float[] { 5.0f, 5.0f }, 0.0f));
				int eventX = mouseEvent.getX();
				int eventY = mouseEvent.getY();
				drawSelectionRectangle(g2,iCornerX, iCornerY, iDeltaX, iDeltaY);
				iDeltaX = Math.abs(iMouseX - eventX);
				iDeltaY = Math.abs(iMouseY - eventY);
				iCornerX = (iMouseX < eventX) ? iMouseX : eventX;
				iCornerY = (iMouseY < eventY) ? iMouseY : eventY;
				drawSelectionRectangle(g2,iCornerX, iCornerY, iDeltaX, iDeltaY);
				break;
		}
	}
	
	public Selection getSelection(ViewDefinition viewDefinition) {
		switch(iMode) {
			case RECTANGLE:
				int mask = Selection.CONTROLPOINTS;
				if (MainFrame.getInstance().getEditedMorph() == null)
					mask |= Selection.BONES;
				return Selection.createRectangularPointSelection(iCornerX,iCornerY,iCornerX + iDeltaX, iCornerY + iDeltaY, viewDefinition.getScreenMatrix(), MainFrame.getInstance().getModel(), mask);
		}
		return null;
	}
	
				
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
	
	private void drawSelectionRectangle(Graphics g, int iCornerX, int iCornerY, int iDeltaX, int iDeltaY) {
		g.drawLine(iCornerX,iCornerY,iCornerX + iDeltaX,iCornerY);
		g.drawLine(iCornerX + iDeltaX,iCornerY,iCornerX + iDeltaX,iCornerY + iDeltaY);
		g.drawLine(iCornerX,iCornerY + iDeltaY,iCornerX + iDeltaX,iCornerY + iDeltaY);
		g.drawLine(iCornerX,iCornerY,iCornerX,iCornerY + iDeltaY);
	}
}
