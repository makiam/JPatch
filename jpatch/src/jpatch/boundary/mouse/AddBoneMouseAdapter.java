package jpatch.boundary.mouse;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.vecmath.*;
import jpatch.entity.*;
import jpatch.boundary.*;


public class AddBoneMouseAdapter extends JPatchMouseAdapter {
	//private static int IDLE = 0;
	//private static int ACTIVE = 1;
	
	private int iMouseX;
	private int iMouseY;
	private Bone bone;
	private Point3f p3Start = new Point3f();
	//private Point3f p3End = new Point3f();
	
	//private CompoundEdit compoundEdit;
	//private ControlPoint cpHot;
	//private int iState = IDLE;
	//private boolean bMulti = false;
	
	private Component compSource;
	
	public AddBoneMouseAdapter(boolean multi) {
		this();
		//bMulti = multi;
	}
	
	public AddBoneMouseAdapter() {
		MainFrame.getInstance().setHelpText("click to add bone; click near existing bone end to attach new bone to it");
	}
	
	public void mousePressed(MouseEvent mouseEvent) {
		if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
			/**
			* left mouse button pressed
			**/
			compSource = (Component)mouseEvent.getSource();
			Viewport viewport = (Viewport)mouseEvent.getSource();
			iMouseX = mouseEvent.getX();
			iMouseY = mouseEvent.getY();
			p3Start.set(viewport.getViewDefinition().get3DPosition(iMouseX,iMouseY));
			
			float fDistance = 64;
			Point3f p3End = new Point3f();
			Point2D.Float p2End = new Point2D.Float();
			Point2D.Float p2Mouse = new Point2D.Float(iMouseX,iMouseY);
			Bone boneParent = null;
			Model model = MainFrame.getInstance().getModel();
			for (Bone bone = model.getFirstBone(); bone != null; bone = bone.getNext()) {
				p3End.set(bone.getEnd());
				viewport.getViewDefinition().getScreenMatrix().transform(p3End);
				p2End.setLocation(p3End.x,-p3End.y);
				float fEndDist = (float)p2End.distanceSq(p2Mouse);
				if (fEndDist < fDistance) {
					fDistance = fEndDist;
					boneParent = bone;
				}
			}
			if (boneParent != null) {
				bone = new Bone(model,boneParent);
			} else {
				bone = new Bone(model,p3Start,p3Start);
			}
			MainFrame.getInstance().setSelection(new BoneSelection(bone));
			MainFrame.getInstance().getModel().addBone(bone);
			MainFrame.getInstance().getJPatchScreen().single_update(compSource);
			compSource.addMouseMotionListener(this);
			/*
			compoundEdit = new CompoundEdit();
			ControlPoint cp = viewport.getViewDefinition().getClosestControlPoint(new Point2D.Float(iMouseX,iMouseY),null);
			if (cp == null || cp.getLooseEnd() != null) {
				/**
				* start a entirely new curve
				
				viewport.getViewDefinition().setZ(MainFrame.getInstance().getJPatchScreen().get3DCursor().getPosition()); 
				ControlPoint cpA = new ControlPoint(viewport.getViewDefinition().get3DPosition(iMouseX,iMouseY));
				ControlPoint cpB = new ControlPoint(viewport.getViewDefinition().get3DPosition(iMouseX,iMouseY));
				cpA.setNext(cpB);
				cpB.setPrev(cpA);
				Curve curve = new Curve(cpA,MainFrame.getInstance().getModel());
				compoundEdit.addEdit(new CreateCurveEdit(curve));
				if (cp != null && cp.getLooseEnd() != null) {
					compoundEdit.addEdit(new ComplexAppendControlPointsEdit(cpA,cp.getLooseEnd()));
				}
				Selection selection = new PointSelection(cpB);
				MainFrame.getInstance().setSelection(selection);
				
				MainFrame.getInstance().getJPatchScreen().repaint();
				cpHot = cpB;
			} /*else if (cp.getNext() == null || cp.getPrev() == null) {	// CHANGE **************************************!!!!!!!!!!!!!!!!!!
			else if (cp.getLooseEnd() != null) {
				/**
				* append to an existing curve
				**
				ControlPoint cpLooseEnd = cp.getLooseEnd();
				ControlPoint cpB = new ControlPoint(cp);
				compoundEdit.addEdit(new AppendControlPointEdit(cpB,cpA,true));
				Selection selection = new SinglePointSelection(cpB);
				MainFrame.getInstance().setSelection(selection);
				
				MainFrame.getInstance().getJPatchScreen().repaint();
				cpHot = cpB;
			} else {
				/**
				* start a new curve, attached to the selected controlpoint
				
				ControlPoint cpA = new ControlPoint(cp);
				ControlPoint cpB = new ControlPoint(cp);
				cpA.setNext(cpB);
				cpB.setPrev(cpA);
				Curve curve = new Curve(cpA,MainFrame.getInstance().getModel());
				compoundEdit.addEdit(new CreateCurveEdit(curve));
				compoundEdit.addEdit(new WeldControlPointsEdit(cpA,cp));
				Selection selection = new PointSelection(cpB);
				MainFrame.getInstance().setSelection(selection);
				
				MainFrame.getInstance().getJPatchScreen().repaint();
				MainFrame.getInstance().getModel().dump();
				cpHot = cpB;
			}
			 
			setActiveState();
			
		} else if (iState == ACTIVE && mouseEvent.getButton() == MouseEvent.BUTTON3) {
			/**
			* right mouse button pressed (in active state)
			
			compSource = (Component)mouseEvent.getSource();
			Viewport viewport = (Viewport)mouseEvent.getSource();
			int iMouseX = mouseEvent.getX();
			int iMouseY = mouseEvent.getY();
			ControlPoint cp = viewport.getViewDefinition().getClosestControlPoint(new Point2D.Float(iMouseX,iMouseY),cpHot);
			if (cp != null) {
				compoundEdit.addEdit(new WeldControlPointsEdit(cpHot,cp));
				//AttachHelper.attach(cpHot,cp,compoundEdit);
				((PointSelection)MainFrame.getInstance().getSelection()).removeControlPoint(cpHot);
				MainFrame.getInstance().getJPatchScreen().repaint();
				setIdleState();
			}
		*/
		}
		
	}
	
	public void mouseReleased(MouseEvent mouseEvent) {
		compSource.removeMouseMotionListener(this);
		MainFrame.getInstance().getJPatchScreen().full_update();
		/*
		if (iState == ACTIVE && mouseEvent.getButton() == MouseEvent.BUTTON1) {
			compSource = (Component)mouseEvent.getSource();
			Selection selection = MainFrame.getInstance().getSelection();
			Class classPointSelection = PointSelection.getPointSelectionClass();
			if (classPointSelection.isAssignableFrom(selection.getClass())) {
			ControlPoint[] acp = new ControlPoint[1];
				acp[0] = cpHot;
				compoundEdit.addEdit(new MoveControlPointsEdit(MoveControlPointsEdit.TRANSLATE,acp));
			}
			setIdleState();
		}
		*/
	}
	
	public void mouseDragged(MouseEvent mouseEvent) {
		//int deltaX = mouseEvent.getX() - iMouseX;
		//int deltaY = mouseEvent.getY() - iMouseY;
		iMouseX = mouseEvent.getX();
		iMouseY = mouseEvent.getY();
		ViewDefinition viewDefinition = ((Viewport)(mouseEvent.getSource())).getViewDefinition();
		viewDefinition.setZ(p3Start);
		//cpHot.setPosition(viewDefinition.get3DPosition((float)iMouseX,(float)iMouseY));
		bone.setEnd(viewDefinition.get3DPosition((float)iMouseX,(float)iMouseY));
		MainFrame.getInstance().getJPatchScreen().single_update(compSource);
		//MainFrame.getInstance().getJPatchScreen().repaint();
	}
	/*
	private void setActiveState() {
		if (iState == IDLE) {
			compSource.addMouseMotionListener(this);
			MainFrame.getInstance().getJPatchScreen().enablePopupMenu(false);
			iState = ACTIVE;
		} else {
			throw new JPatchException("setActiveState() called in active state");
		}
		MainFrame.getInstance().setHelpText("drag to move curve-end; press RMB to weld to next closest point");
	}
	
	private void setIdleState() {
		if (iState == ACTIVE) {
			compSource.removeMouseMotionListener(this);
			compoundEdit.end();
			MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
			MainFrame.getInstance().getJPatchScreen().enablePopupMenu(true);
			iState = IDLE;
		} else {
			throw new JPatchException("setActiveState() called in active state");
		}
		MainFrame.getInstance().setHelpText("click to add curve segment; click near existing point to weld new curve segment to it");
		if (!bMulti) {
			MainFrame.getInstance().getMeshToolBar().reset();
		}
	}
	*/
}

