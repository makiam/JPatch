package jpatch.boundary.mouse;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;

import javax.vecmath.*;
import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;
import jpatch.control.edit.*;

public class AddControlPointMouseAdapter extends JPatchMouseAdapter {
	private static int IDLE = 0;
	private static int ACTIVE = 1;
	
	private int iMouseX;
	private int iMouseY;
	
	private JPatchActionEdit edit;
	private ControlPoint cpHot;
	private int iState = IDLE;
	private boolean bMulti = false;
	
	private Component compSource;
	
	public AddControlPointMouseAdapter(boolean multi) {
		this();
		bMulti = multi;
	}
	
	public AddControlPointMouseAdapter() {
		MainFrame.getInstance().setHelpText("Press left mousebutton to add curve segment. Press left mousebutton near existing point to weld new curve segment to it or hold CTRL to attach new curve segment.");
	}
	
	public void mousePressed(MouseEvent mouseEvent) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
		if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
			/**
			* left mouse button pressed
			**/
			compSource = (Component) mouseEvent.getSource();
			viewDef.setZ(0);
			iMouseX = mouseEvent.getX();
			iMouseY = mouseEvent.getY();
			edit = new JPatchActionEdit("add curve segment");
			ControlPoint cp = viewDef.getClosestControlPoint(new Point2D.Float(iMouseX,iMouseY),null,null,false,true);
			if (cp == null || cp.getLooseEnd() != null) {
				/**
				* start a entirely new curve
				**/
				//viewDef.setZ(MainFrame.getInstance().getJPatchScreen().get3DCursor().getPosition());
				if (cp != null) {
					viewDef.setZ(cp.getPosition());
				}
				Point3f pos = viewDef.get3DPosition((float)iMouseX,(float)iMouseY);
//				viewport.getGrid().correctVector(pos);
				ControlPoint cpA = new ControlPoint(pos);
				ControlPoint cpB = new ControlPoint(pos);
				cpA.setNext(cpB);
				cpB.setPrev(cpA);
				edit.addEdit(new AtomicAddCurve(cpA));
				if (cp != null && cp.getLooseEnd() != null) {
					if (!mouseEvent.isControlDown())
						edit.addEdit(new CompoundWeldControlPoints(cpA,cp));
					else
						edit.addEdit(new AtomicAttachControlPoints(cpA.getHead(),cp.getTail()));
				}
				NewSelection selection = new NewSelection(cpB);
//				MainFrame.getInstance().setSelection(selection);
				edit.addEdit(new AtomicChangeSelection(selection));
				MainFrame.getInstance().getJPatchScreen().single_update(compSource);
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
			} */else {
				/**
				* start a new curve, attached to the selected controlpoint
				**/
				viewDef.setZ(cp.getPosition());
				ControlPoint cpA = new ControlPoint(cp);
				ControlPoint cpB = new ControlPoint(cp);
				cpA.setNext(cpB);
				cpB.setPrev(cpA);
				edit.addEdit(new AtomicAddCurve(cpA));
				//compoundEdit.addEdit(new WeldControlPointsEdit(cpA,cp));
				edit.addEdit(new CompoundWeldControlPoints(cpA,cp));
				
				NewSelection selection = new NewSelection(cpB);
				MainFrame.getInstance().setSelection(selection);
				
				MainFrame.getInstance().getJPatchScreen().single_update(compSource);
				//MainFrame.getInstance().getModel().dump();
				cpHot = cpB;
			}
			 
			setActiveState();
		} else if (iState == ACTIVE && mouseEvent.getButton() == MouseEvent.BUTTON3) {
			/**
			* right mouse button pressed (in active state)
			**/
			compSource = (Component)mouseEvent.getSource();
			int iMouseX = mouseEvent.getX();
			int iMouseY = mouseEvent.getY();
			float[] hookPos = new float[1];
			ControlPoint cp = viewDef.getClosestControlPoint(new Point2D.Float(iMouseX,iMouseY),cpHot,hookPos,false,true);
			if (cp != null && cp != cpHot.getPrev()) {
				if (hookPos[0] == -1) {
					if (!mouseEvent.isControlDown())
						edit.addEdit(new CompoundWeldControlPoints(cpHot,cp));
					else
						edit.addEdit(new AtomicAttachControlPoints(cpHot.getHead(),cp.getTail()));
//						((PointSelection)MainFrame.getInstance().getSelection()).removeControlPoint(cpHot);
//					edit.addEdit(new RemoveControlPointFromSelectionEdit(cpHot, MainFrame.getInstance().getSelection()));
					MainFrame.getInstance().getJPatchScreen().full_update();
					setIdleState();
				} else {
					if (!cp.getNext().getHead().isHook() && !cp.getHead().isHook()) {
						if (cp.getHookAt(hookPos[0]) == null) {
							edit.addEdit(new CompoundHook(cpHot,cp,hookPos[0]));
//FIXME							((PointSelection)MainFrame.getInstance().getSelection()).removeControlPoint(cpHot);
//							compoundEdit.addEdit(new RemoveControlPointFromSelectionEdit(cpHot, MainFrame.getInstance().getSelection()));
							MainFrame.getInstance().getJPatchScreen().full_update();
							setIdleState();
						} else {
//							ControlPoint hook = cp.getHookAt(hookPos[0]);
//							compoundEdit.addEdit(new ConvertHookToCpEdit(hook));
//							compoundEdit.addEdit(new CompoundWeldControlPoints(cpHot,hook));
////FIXME							((PointSelection)MainFrame.getInstance().getSelection()).removeControlPoint(cpHot);
//							compoundEdit.addEdit(new RemoveControlPointFromSelectionEdit(cpHot, MainFrame.getInstance().getSelection()));
//							MainFrame.getInstance().getJPatchScreen().full_update();
//							setIdleState();
						}
					}
				}				
			}
		}
	}
	
	public void mouseReleased(MouseEvent mouseEvent) {
		if (iState == ACTIVE && mouseEvent.getButton() == MouseEvent.BUTTON1) {
			//compSource = (Component)mouseEvent.getSource();
//FIXME
//			Selection selection = MainFrame.getInstance().getSelection();
//			Class classPointSelection = PointSelection.getPointSelectionClass();
//			if (selection != null && classPointSelection.isAssignableFrom(selection.getClass())) {
//				ControlPoint[] acp = new ControlPoint[1];
//				acp[0] = cpHot;
//				//compoundEdit.addEdit(new MoveControlPointsEdit(MoveControlPointsEdit.TRANSLATE,acp));
//			}
//			compoundEdit.addEdit(new MoveControlPointsEdit(MoveControlPointsEdit.TRANSLATE,new ControlPoint[] { cpHot }));
			MainFrame.getInstance().getJPatchScreen().full_update();
			setIdleState();
		}
	}
	
	public void mouseDragged(MouseEvent mouseEvent) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
		iMouseX = mouseEvent.getX();
		iMouseY = mouseEvent.getY();
//		ViewDefinition viewDefinition = viewDef;
		//viewDefinition.setZ(cpHot.getPosition());
		Point3f pos = viewDef.get3DPosition((float)iMouseX,(float)iMouseY);
//		viewport.getGrid().correctVector(pos);
		cpHot.setPosition(pos);
		MainFrame.getInstance().getJPatchScreen().single_update(compSource);
		//MainFrame.getInstance().getJPatchScreen().repaint();
	}
	
	private void setActiveState() {
		if (iState == IDLE) {
			compSource.addMouseMotionListener(this);
			MainFrame.getInstance().getJPatchScreen().enablePopupMenu(false);
			iState = ACTIVE;
		} else {
			throw new IllegalStateException("setActiveState() called in active state");
		}
		MainFrame.getInstance().setHelpText("Drag to move curve-end. Press right mouse button to weld to closest point. Hold CTRL and press right mouse button to attach to closest point.");
	}
	
	private void setIdleState() {
		if (iState == ACTIVE) {
			compSource.removeMouseMotionListener(this);
			MainFrame.getInstance().getUndoManager().addEdit(edit);
			MainFrame.getInstance().getJPatchScreen().enablePopupMenu(true);
			iState = IDLE;
		} else {
			throw new IllegalStateException("setIdleState() called in idle state");
		}
		MainFrame.getInstance().setHelpText("Press left mousebutton to add curve segment. Press left mousebutton near existing point to weld new curve segment to it or hold CTRL to attach new curve segment.");
		if (!bMulti) {
			MainFrame.getInstance().getMeshToolBar().reset();
		}
	}
}

