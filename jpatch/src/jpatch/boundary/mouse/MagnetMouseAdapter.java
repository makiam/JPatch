package jpatch.boundary.mouse;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.vecmath.*;
import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;
import jpatch.control.edit.*;

public class MagnetMouseAdapter extends JPatchMouseAdapter {
	private static int IDLE = 0;
	private static int ACTIVE = 1;
	private static int SETRADIUS = 2;
	
	
	private int iMouseX;
	private int iMouseY;
	private int iDeltaX;
	private int iDeltaY;
	
	private int iRadius;
	private float fRadius;
	
	private JPatchCompoundEdit compoundEdit;
	private int iState = IDLE;
	private boolean bNewCircle = true;
	
	private Component compSource;
	
	private JPatchSettings settings = JPatchSettings.getInstance();

	public MagnetMouseAdapter() {
		MainFrame.getInstance().setHelpText("Click and drag empty space to set magnet influence. Click and drag on point to move.");
	}
	public void mousePressed(MouseEvent mouseEvent) {
		//System.out.println("mousePressed button=" + mouseEvent.getButton());
		if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
			compSource = (Component)mouseEvent.getSource();
			Viewport viewport = (Viewport)mouseEvent.getSource();
			iMouseX = mouseEvent.getX();
			iMouseY = mouseEvent.getY();
			ControlPoint cp = viewport.getViewDefinition().getClosestControlPoint(new Point2D.Float(iMouseX,iMouseY),null);
			if (cp != null) {
				cp = cp.getHead();
				PointSelection ps = (PointSelection) SelectionFactory.createMagnetSelection(cp,MainFrame.getInstance().getModel());
				MainFrame.getInstance().setSelection(ps);
				//MainFrame.getInstance().getJPatchScreen().prepareBackground(compSource);
				//MainFrame.getInstance().getJPatchScreen().single_update(compSource);
				Graphics g = ((Component)mouseEvent.getSource()).getGraphics();
				g.setXORMode(new Color(settings.cBackground.getRGB() ^ settings.cSelection.getRGB()));
				drawSelectionCircle(g,iMouseX,iMouseY,iRadius);
				setActiveState();
				compoundEdit.addEdit(new NewMoveControlPointsEdit(ps.getControlPointArray()));
			} else {
				MainFrame.getInstance().setSelection(null);
				//MainFrame.getInstance().getJPatchScreen().clearBackground();
				//MainFrame.getInstance().getJPatchScreen().repaint();
				compSource = (Component)mouseEvent.getSource();
				iMouseX = mouseEvent.getX();
				iMouseY = mouseEvent.getY();
				setRadiusState();
			}
		} /*else if (iState == IDLE && mouseEvent.getButton() == MouseEvent.BUTTON1) {
			compSource = (Component)mouseEvent.getSource();
			iMouseX = mouseEvent.getX();
			iMouseY = mouseEvent.getY();
			setRadiusState();
		}*/
	}
	public void mouseReleased(MouseEvent mouseEvent) {
		if (iState == ACTIVE && mouseEvent.getButton() == MouseEvent.BUTTON1) {			
			compSource = (Component)mouseEvent.getSource();
			//if (classPointSelection.isAssignableFrom(selection.getClass())) {
			//	ControlPoint[] acp = ((PointSelection)selection).getControlPointArray();
			//	compoundEdit.addEdit(new MoveControlPointsEdit(MoveControlPointsEdit.TRANSLATE,acp));
			//}
			setIdleState();
		} else if (iState == SETRADIUS && mouseEvent.getButton() == MouseEvent.BUTTON1) {
			Graphics g = ((Component)mouseEvent.getSource()).getGraphics();
			g.setXORMode(new Color(settings.cBackground.getRGB() ^ settings.cSelection.getRGB()));
			drawSelectionCircle(g,iMouseX,iMouseY,iRadius);
			setIdleState();
		}
	}
	
	public void mouseDragged(MouseEvent mouseEvent) {
		if (iState == ACTIVE) {
			iDeltaX = mouseEvent.getX() - iMouseX;
			iDeltaY = mouseEvent.getY() - iMouseY;
			iMouseX = mouseEvent.getX();
			iMouseY = mouseEvent.getY();
			ViewDefinition viewDefinition = ((Viewport)(mouseEvent.getSource())).getViewDefinition();
			Vector3f v3Move = new Vector3f(iDeltaX,iDeltaY,0);
			Matrix4f m4InvScreenMatrix = new Matrix4f(viewDefinition.getScreenMatrix());
			Vector3f v3Scaled = new Vector3f();
			m4InvScreenMatrix.invert();
			m4InvScreenMatrix.transform(v3Move);
			Selection selection = MainFrame.getInstance().getSelection();
			Class classPointSelection = PointSelection.getPointSelectionClass();
			if (classPointSelection.isAssignableFrom(selection.getClass())) {
				ControlPoint[] acp = ((PointWeightSelection)selection).getControlPointArray();
				//float[] afWeight = ((PointWeightSelection)selection).getWeightArray();
				for (int i = 0; i < acp.length; i++) {
					ControlPoint cp = acp[i];
					//viewDefinition.setZ(cp.getPosition());
					//cp.setPosition(viewDefinition.get3DPosition((float)iMouseX,(float)iMouseY));
					v3Scaled.set(v3Move);
					float fWeight = ((PointWeightSelection)selection).getWeight(cp);
					//System.out.println(fWeight);
					v3Scaled.scale(fWeight);
					cp.getPosition().add(v3Scaled);
					cp.invalidateTangents();
				}
				MainFrame.getInstance().getJPatchScreen().single_update(compSource);
				Graphics g = ((Component)mouseEvent.getSource()).getGraphics();
				g.setXORMode(new Color(settings.cBackground.getRGB() ^ settings.cSelection.getRGB()));
				drawSelectionCircle(g,iMouseX,iMouseY,iRadius);
			}
		} else if (iState == SETRADIUS) {
			Graphics g = ((Component)mouseEvent.getSource()).getGraphics();
			ViewDefinition viewDefinition = ((Viewport)(mouseEvent.getSource())).getViewDefinition();
			g.setXORMode(new Color(settings.cBackground.getRGB() ^ settings.cSelection.getRGB()));
			//g.setXORMode(settings.cSelection);
			//g.drawImage(image,0,0,null);
			if (!bNewCircle) {
				drawSelectionCircle(g,iMouseX,iMouseY,iRadius);
			}
			bNewCircle = false;
			iRadius = (int)Point2D.distance(iMouseX,iMouseY, mouseEvent.getX(), mouseEvent.getY());
			fRadius = iRadius / viewDefinition.getScreenMatrix().getScale();
			drawSelectionCircle(g,iMouseX,iMouseY,iRadius);
		}
	}
	
	private void drawSelectionCircle(Graphics g, int iCenterX, int iCenterY, int iRadius) {
		for (int i = 0; i < 256; i += 4) {
			int X1 = iCenterX + (int)(iRadius * Math.cos((float)i * Math.PI / 128f));
			int Y1 = iCenterY + (int)(iRadius * Math.sin((float)i * Math.PI / 128f));
			int X2 = iCenterX + (int)(iRadius * Math.cos((float)(i + 1) * Math.PI / 128f));
			int Y2 = iCenterY + (int)(iRadius * Math.sin((float)(i + 1)* Math.PI / 128f));
			g.drawLine(X1,Y1,X2,Y2);
		}
	}
	
	private void setActiveState() {
		if (iState == IDLE) {
			compSource.addMouseMotionListener(this);
			compoundEdit = new JPatchCompoundEdit();
			MainFrame.getInstance().getJPatchScreen().enablePopupMenu(false);
			iState = ACTIVE;
		} else {
			throw new IllegalStateException("setActiveState() called in non-idle state");
		}
	}
	
	private void setIdleState() {
		if (iState == ACTIVE) {
			compSource.removeMouseMotionListener(this);
			MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
			MainFrame.getInstance().getJPatchScreen().update_all();
			MainFrame.getInstance().getJPatchScreen().enablePopupMenu(true);
			iState = IDLE;
		} else if (iState == SETRADIUS) {
			compSource.removeMouseMotionListener(this);
			SelectionFactory.setRadius(fRadius);
			MainFrame.getInstance().getJPatchScreen().update_all();
			MainFrame.getInstance().getJPatchScreen().enablePopupMenu(true);
			iState = IDLE;
		} else {
			throw new IllegalStateException("setIdleState() called in idle state");
		}
	}
	
	private void setRadiusState() {
		if (iState == IDLE) {
			compSource.addMouseMotionListener(this);
			//image = compSource.createImage(compSource.getWidth(),compSource.getHeight());
			//image = new BufferedImage(compSource.getWidth(),compSource.getHeight(),BufferedImage.TYPE_INT_RGB);
			//((Viewport)compSource).renderToImage(image);
			MainFrame.getInstance().getJPatchScreen().enablePopupMenu(false);
			bNewCircle = true;
			iState = SETRADIUS;
		} else {
			throw new IllegalStateException("setRadiusState() called in non-idle state");
		}
	}
}

