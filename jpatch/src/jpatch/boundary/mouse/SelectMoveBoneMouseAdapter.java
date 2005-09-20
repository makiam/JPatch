package jpatch.boundary.mouse;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.vecmath.*;
import jpatch.entity.*;
import jpatch.boundary.*;


public class SelectMoveBoneMouseAdapter extends JPatchMouseAdapter {
	//private static int IDLE = 0;
	//private static int START = 1;
	//private static int END = 2;
	//private static int SELECT = 3;
	//private static int CURSOR = 3;
	
	private static final int NOTHING = 0;
	private static final int START = 1;
	private static final int END = 2;
	private static final int LINE = 3;
	
	private Bone boneSelected;
	private int iType = NOTHING;
	
	private int iMouseX;
	private int iMouseY;
	private int iDeltaX;
	private int iDeltaY;
	//private boolean bNewRectangle = true;
	
	//private CompoundEdit compoundEdit;
	//private ControlPoint cpHot;
	//private int iState = IDLE;
	
	private Component compSource;
	
	public SelectMoveBoneMouseAdapter() {
		//MainFrame.getInstance().setHelpText("click or drag to select points; hold SHIFT, CTRL or ALT to 'OR', 'XOR' or 'AND' to current selection; click and drag selected point to move selection");
	}
	
	public void mousePressed(MouseEvent mouseEvent) {
		compSource = (Component)mouseEvent.getSource();
		Viewport viewport = (Viewport)mouseEvent.getSource();
		iMouseX = mouseEvent.getX();
		iMouseY = mouseEvent.getY();
		Model model = MainFrame.getInstance().getModel();
		if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
			float fDistance = 64;
			Point3f p3Start = new Point3f();
			Point3f p3End = new Point3f();
			Point2D.Float p2Start = new Point2D.Float();
			Point2D.Float p2End = new Point2D.Float();
			Line2D.Float l2Line = new Line2D.Float();
			Point2D.Float p2Mouse = new Point2D.Float(iMouseX,iMouseY);
			for (Bone bone = model.getFirstBone(); bone != null; bone = bone.getNext()) {
				p3Start.set(bone.getStart());
				p3End.set(bone.getEnd());
				viewport.getViewDefinition().getScreenMatrix().transform(p3Start);
				viewport.getViewDefinition().getScreenMatrix().transform(p3End);
				p2Start.setLocation(p3Start.x,p3Start.y);
				p2End.setLocation(p3End.x,p3End.y);
				l2Line.setLine(p2Start,p2End);
				//System.out.println(p3Start + " " + p3End);
				float fStartDist = (float)p2Start.distanceSq(p2Mouse);
				float fEndDist = (float)p2End.distanceSq(p2Mouse);
				float fLineDist = (float)l2Line.ptSegDistSq(p2Mouse);
				//System.out.println(fStartDist + " " + fEndDist + " " + fLineDist);
				if (fStartDist < fDistance) {
					fDistance = fStartDist;
					boneSelected = bone;
					iType = START;
				}
				if (fEndDist <= fDistance) {
					fDistance = fEndDist;
					boneSelected = bone;
					iType = END;
				}
				if (fLineDist < fDistance && fLineDist < fStartDist - 64 && fLineDist < fEndDist - 64) {
					fDistance = fLineDist;
					boneSelected = bone;
					iType = LINE;
				}
			}
			//System.out.println("select move bone" + iType);
			if (iType != NOTHING) {
				compSource.addMouseMotionListener(this);
				MainFrame.getInstance().getJPatchScreen().enablePopupMenu(false);
				MainFrame.getInstance().setSelection(new BoneSelection(boneSelected,iType));
				MainFrame.getInstance().getJPatchScreen().single_update(compSource);
			} else {
				MainFrame.getInstance().setSelection(null);
				MainFrame.getInstance().getJPatchScreen().single_update(compSource);
			}
		} else if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
			if (iType == START || iType == LINE) {
				float fDistance = 64;
				Point3f p3End = new Point3f();
				Point2D.Float p2End = new Point2D.Float();
				Point3f p3Start = new Point3f(boneSelected.getStart());
				viewport.getViewDefinition().getScreenMatrix().transform(p3Start);
				Point2D.Float p2Start = new Point2D.Float(p3Start.x,-p3Start.y);
				Bone boneParent = null;
				for (Bone bone = model.getFirstBone(); bone != null; bone = bone.getNext()) {
					p3End.set(bone.getEnd());
					viewport.getViewDefinition().getScreenMatrix().transform(p3End);
					p2End.setLocation(p3End.x,-p3End.y);
					float fEndDist = (float)p2End.distanceSq(p2Start);
					if (fEndDist < fDistance) {
						fDistance = fEndDist;
						boneParent = bone;
					}
				}
				if (boneParent != null && boneSelected.getParentBone() == null) {
					boneSelected.setParent(boneParent);
					MainFrame.getInstance().getJPatchScreen().single_update(compSource);
				}
			}
		}
		/*
			ControlPoint cp = viewport.getViewDefinition().getClosestControlPoint(new Point2D.Float(iMouseX,iMouseY),null,true);
			Selection selection = MainFrame.getInstance().getSelection();
			Class classPointSelection = PointSelection.getPointSelectionClass();
			PointSelection pointSelection = null;
			if (selection != null && classPointSelection.isAssignableFrom(selection.getClass())) {
				pointSelection = (PointSelection)selection;
			}
			if (cp != null) {
				if (cp == MainFrame.getInstance().getJPatchScreen().get3DCursor()) {
					selection = new PointSelection(cp);
					MainFrame.getInstance().setSelection(selection);
					MainFrame.getInstance().getJPatchScreen().repaint();
					cpHot = cp;
					setCursorState();
				} else if (pointSelection != null && pointSelection.contains(cp)) {
					cpHot = cp;
					setActiveState();
				} else {
					cp = cp.getHead();
					selection = new PointSelection(cp);
					MainFrame.getInstance().setSelection(selection);
					MainFrame.getInstance().getJPatchScreen().repaint();
					cpHot =cp;
					setActiveState();
				}
			} else {
				MainFrame.getInstance().setSelection(null);
				MainFrame.getInstance().getJPatchScreen().repaint();
				setSelectState();
			}
		} else if (iState == ACTIVE && mouseEvent.getButton() == MouseEvent.BUTTON3) {
			/**
			* right mouse button pressed (in active state)
			
			compSource = (Component)mouseEvent.getSource();
			Viewport viewport = (Viewport)mouseEvent.getSource();
			ControlPoint cp = viewport.getViewDefinition().getClosestControlPoint(new Point2D.Float(mouseEvent.getX(),mouseEvent.getY()),cpHot);
			if (cp != null) {
				
				Selection selection = MainFrame.getInstance().getSelection();
				Class classPointSelection = PointSelection.getPointSelectionClass();
				if (classPointSelection.isAssignableFrom(selection.getClass())) {
					ControlPoint[] acp = ((PointSelection)selection).getControlPointArray();
					compoundEdit.addEdit(new MoveControlPointsEdit(MoveControlPointsEdit.TRANSLATE,acp));
					((PointSelection)MainFrame.getInstance().getSelection()).removeControlPoint(cpHot);
				}
				compoundEdit.addEdit(new WeldControlPointsEdit(cpHot,cp));
				//AttachHelper.attach(cpHot,cp,compoundEdit);
				MainFrame.getInstance().getJPatchScreen().repaint();
				setIdleState();
			}
		}
		*/
	}
	public void mouseReleased(MouseEvent mouseEvent) {
		if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
			if (iType != NOTHING) {
				iType = NOTHING;
				boneSelected = null;
				compSource.removeMouseMotionListener(this);
				MainFrame.getInstance().getJPatchScreen().enablePopupMenu(true);
				MainFrame.getInstance().getJPatchScreen().full_update();
			}
		}
		/*
		if (iState == ACTIVE && mouseEvent.getButton() == MouseEvent.BUTTON1) {			
			compSource = (Component)mouseEvent.getSource();
			Selection selection = MainFrame.getInstance().getSelection();
			Class classPointSelection = PointSelection.getPointSelectionClass();
			if (classPointSelection.isAssignableFrom(selection.getClass())) {
				ControlPoint[] acp = ((PointSelection)selection).getControlPointArray();
				compoundEdit.addEdit(new MoveControlPointsEdit(MoveControlPointsEdit.TRANSLATE,acp));
			}
			setIdleState();
		} else if (iState == SELECT && mouseEvent.getButton() == MouseEvent.BUTTON1) {
			ViewDefinition viewDefinition = ((Viewport)(mouseEvent.getSource())).getViewDefinition();
			Selection selection = SelectionFactory.createRectangularPointSelection(iCornerX,iCornerY,iCornerX + iDeltaX, iCornerY + iDeltaY, viewDefinition.getScreenMatrix(), MainFrame.getInstance().getModel());
			MainFrame.getInstance().setSelection(selection);
			MainFrame.getInstance().getJPatchScreen().repaint();
			setIdleState();
		} else if (iState == CURSOR && mouseEvent.getButton() == MouseEvent.BUTTON1) {
			setIdleState();
		}
		*/
	}
	
	public void mouseDragged(MouseEvent mouseEvent) {
		iDeltaX = mouseEvent.getX() - iMouseX;
		iDeltaY = mouseEvent.getY() - iMouseY;
		iMouseX = mouseEvent.getX();
		iMouseY = mouseEvent.getY();
		ViewDefinition viewDefinition = ((Viewport)(mouseEvent.getSource())).getViewDefinition();
		Vector3f v3Move = new Vector3f(iDeltaX,iDeltaY,0);
		Matrix4f m4InvScreenMatrix = new Matrix4f(viewDefinition.getScreenMatrix());
		m4InvScreenMatrix.invert();
		m4InvScreenMatrix.transform(v3Move);
		System.out.println(iType + " " + boneSelected);
		switch(iType) {
			case START:
				//if (boneSelected.getParentBone() == null) {
				//	boneSelected.getStart().add(v3Move);
				//	boneSelected.getExtent().sub(v3Move);
				//} else {
				//	System.out.println("error!!!");
				//	//boneSelected.getParentBone().getExtent().add(v3Move);
				//	//boneSelected.getExtent().sub(v3Move);
				//}
				boneSelected.getStart().add(v3Move);
				break;
			case END:
				//boneSelected.getExtent().add(v3Move);
				//if (boneSelected.getNumberOfChilds() > 0) {
				//	Bone[] aboneChild = boneSelected.getChilds();
				//	for (int b = 0; b < aboneChild.length; b++) {
				//		aboneChild[b].getExtent().sub(v3Move);
				//	}
				//}
				boneSelected.getEnd().add(v3Move);
				break;
			case LINE:
				//if (boneSelected.getParentBone() == null) {
				//	boneSelected.getStart().add(v3Move);
				//} else {
				//	boneSelected.getParentBone().getExtent().add(v3Move);
				//}
				boneSelected.getStart().add(v3Move);
				boneSelected.getEnd().add(v3Move);
				break;
		}
		MainFrame.getInstance().getJPatchScreen().single_update(compSource);
	
			
		/*
		if (iState == ACTIVE || iState == CURSOR) {
			iDeltaX = mouseEvent.getX() - iMouseX;
			iDeltaY = mouseEvent.getY() - iMouseY;
			iMouseX = mouseEvent.getX();
			iMouseY = mouseEvent.getY();
			ViewDefinition viewDefinition = ((Viewport)(mouseEvent.getSource())).getViewDefinition();
			Vector3f v3Move = new Vector3f(iDeltaX,-iDeltaY,0);
			Matrix4f m4InvScreenMatrix = new Matrix4f(viewDefinition.getScreenMatrix());
			m4InvScreenMatrix.invert();
			m4InvScreenMatrix.transform(v3Move);
			Selection selection = MainFrame.getInstance().getSelection();
			Class classPointSelection = PointSelection.getPointSelectionClass();
			if (classPointSelection.isAssignableFrom(selection.getClass())) {
				Point3f p3 = new Point3f();
				ControlPoint[] acp = ((PointSelection)selection).getControlPointArray();
				for (int i = 0; i < acp.length; i++) {
					ControlPoint cp = acp[i];
					//viewDefinition.setZ(cp.getPosition());
					//cp.setPosition(viewDefinition.get3DPosition((float)iMouseX,(float)iMouseY));
					p3.add(cp.getPosition(),v3Move);
					MainFrame.getInstance().getConstraints().setControlPointPosition(cp,p3);
					//cp.getPosition().add(v3Move);
					//cp.invalidateTangents();
					//viewDefinition.rerender();
				}
				//MainFrame.getInstance().getJPatchScreen().repaint();
				MainFrame.getInstance().getJPatchScreen().rerender();
			}
		} else if (iState == SELECT) {
			Graphics g = ((Component)mouseEvent.getSource()).getGraphics();
			g.setXORMode(Preferences.selectionRectangleColor);
			//g.drawImage(image,0,0,null);
			int eventX = mouseEvent.getX();
			int eventY = mouseEvent.getY();
			if (!bNewRectangle) {
				drawSelectionRectangle(g,iCornerX, iCornerY, iDeltaX, iDeltaY);
			}
			bNewRectangle = false;
			iDeltaX = Math.abs(iMouseX - eventX);
			iDeltaY = Math.abs(iMouseY - eventY);
			iCornerX = (iMouseX < eventX) ? iMouseX : eventX;
			iCornerY = (iMouseY < eventY) ? iMouseY : eventY;
			drawSelectionRectangle(g,iCornerX, iCornerY, iDeltaX, iDeltaY);
		}
		*/
	}
	/*
	private void drawSelectionRectangle(Graphics g, int iCornerX, int iCornerY, int iDeltaX, int iDeltaY) {
		g.drawLine(iCornerX,iCornerY,iCornerX + iDeltaX,iCornerY);
		g.drawLine(iCornerX,iCornerY,iCornerX,iCornerY + iDeltaY);
		g.drawLine(iCornerX,iCornerY + iDeltaY,iCornerX + iDeltaX,iCornerY + iDeltaY);
		g.drawLine(iCornerX + iDeltaX,iCornerY,iCornerX + iDeltaX,iCornerY + iDeltaY);
	}
	
	private void setActiveState() {
		if (iState == IDLE) {
			compSource.addMouseMotionListener(this);
			compoundEdit = new CompoundEdit();
			MainFrame.getInstance().getJPatchScreen().enablePopupMenu(false);
			iState = ACTIVE;
		} else {
			throw new JPatchException("setActiveState() called in non-idle state");
		}
		MainFrame.getInstance().setHelpText("drag to move selected point; hold SHIFT to snap-to-grid, CTRL/ALT to snap only to X/Y-Grid; press RMB to weld to next closest point");
	}
	
	private void setCursorState() {
		if (iState == IDLE) {
			compSource.addMouseMotionListener(this);
			//compoundEdit = new CompoundEdit();
			MainFrame.getInstance().getJPatchScreen().enablePopupMenu(false);
			iState = CURSOR;
		} else {
			throw new JPatchException("setActiveState() called in non-idle state");
		}
	}
	
	private void setIdleState() {
		if (iState == ACTIVE) {
			compSource.removeMouseMotionListener(this);
			compoundEdit.end();
			MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
			MainFrame.getInstance().getJPatchScreen().enablePopupMenu(true);
			iState = IDLE;
		} else if (iState == SELECT || iState == CURSOR) {
			compSource.removeMouseMotionListener(this);
			iState = IDLE;
		} else {
			throw new JPatchException("setIdleState() called in idle state");
		}
		MainFrame.getInstance().setHelpText("click or drag to select points; hold SHIFT, CTRL or ALT to 'OR', 'XOR' or 'AND' to current selection; click and drag selected point to move selection");
	}
	
	private void setSelectState() {
		if (iState == IDLE) {
			compSource.addMouseMotionListener(this);
			//image = compSource.createImage(compSource.getWidth(),compSource.getHeight());
			//image = new BufferedImage(compSource.getWidth(),compSource.getHeight(),BufferedImage.TYPE_INT_RGB);
			//((Viewport)compSource).renderToImage(image);
			bNewRectangle = true;
			iState = SELECT;
		} else {
			throw new JPatchException("setSelectState() called in non-idle state");
		}
		MainFrame.getInstance().setHelpText("drag to draw selection rectangle; hold SHIFT to add to current selection, CTRL to 'XOR' with current selection or ALT to 'AND' with current selection");
	}
	*/
}

