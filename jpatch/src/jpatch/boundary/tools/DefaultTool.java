package jpatch.boundary.tools;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.vecmath.*;
import jpatch.boundary.*;
import jpatch.boundary.mouse.*;
import jpatch.boundary.selection.*;
import jpatch.entity.*;
import jpatch.control.edit.*;

public class DefaultTool extends JPatchTool {

	//private static final int IDLE = 0;
	//private static final int SINGLE_POINT = 1;
	//private static final int SELECTION = 2;
	//private static final int CURSOR = 3;
	
	private static final int GHOST_FACTOR = JPatchSettings.getInstance().iGhost;
	
	private static final int IDLE = 0;
	private static final int DRAW_SELECTION = 1;
	private static final int MOVE_SINGLE_POINT = 2;
	private static final int MOVE_GROUP = 3;
	private static final int SCALE_GROUP = 4;
	private static final int PIVOT = 5;
	private static final int ADD_MODIFY_SELECTION = 6;
	private static final int XOR_MODIFY_SELECTION = 7;
	private static final int TANGENT = 8;
	
	private Handle[] aHandle;
	//private Point3f p3Pivot;
	//private Matrix3f m3Rot;
	//private Point3f p3CornerA = new Point3f();
	//private Point3f p3CornerB = new Point3f();
	private int iMouseX;
	private int iMouseY;
	private HandleZOrderComparator comparator = new HandleZOrderComparator();
	private Handle activeHandle;
	//private PointSelection ps;
	private Point3f[] ap3;
	private PivotHandle2 pivotHandle;
	private ControlPoint cpHot;
	//private int iMode;
	private JPatchCompoundEdit compoundEdit;
	private Point3f p3Pivot = new Point3f();
	private Vector3f v3Move = new Vector3f();
	private int iState;
	//private Point3f p3Pivot = new Point3f();
	//private boolean bChange;
	private SelectMouseMotionListener selectMouseMotionListener;
	//private Viewport viewport;
	private boolean bMoveZ;
	private TangentTool tangentTool = MainFrame.getInstance().getJPatchScreen().getTangentTool();
	private TangentHandle tangentHandle;
	private float fMagnitude;
	
	private Matrix4f m4Transform = new Matrix4f();
	
	public DefaultTool() {
		//m3Rot.setIdentity();
		//m3RotA.setIdentity();
		
		//fRadius = 2;
		//PointSelection ps = MainFrame.getInstance().getPointSelection();
		//p3Pivot = ps.getPivot();
		//m3Rot = ps.getRotation();
		//p3CornerA = ps.getCornerA();
		//p3CornerB = ps.getCornerB();
		Color3f color = new Color3f(settings.cSelection);
		pivotHandle = new PivotHandle2(color);
		aHandle = new Handle[] {
			new DefaultHandle(this, new Point3f(), color),
			new DefaultHandle(this, new Point3f(), color),
			new DefaultHandle(this, new Point3f(), color),
			new DefaultHandle(this, new Point3f(), color),
			new DefaultHandle(this, new Point3f(), color),
			new DefaultHandle(this, new Point3f(), color),
			new DefaultHandle(this, new Point3f(), color),
			new DefaultHandle(this, new Point3f(), color),
			pivotHandle
		};
		if (MainFrame.getInstance().getPointSelection() == null) {
			MainFrame.getInstance().setHelpText("Drag to move or select points. Use ATL to move perpendicular to screen plane.");
		} else if (MainFrame.getInstance().getPointSelection().isSingle()) {
			MainFrame.getInstance().setHelpText("Use SHIFT or CTRL to modify selection. Use ATL to move perpendicular to screen plane. Press TAB to cycle through curve segments.");
		} else {
			MainFrame.getInstance().setHelpText("Drag inside box to move selection. Drag handles to scale. Use SHIFT or CTRL to modify selection, ATL to move perpendiclar. Doubleclick pivot to reset.");
		}
	}
	/*
	public Matrix3f getRot() {
		return m3Rot;
	}
*/

	//public void reInit(PointSelection ps) {
	//	m3Rot = ps.getRotation();
	//	p3Pivot = ps.getPivot();
	//	m3RotA.set(m3Rot);
	//	//MainFrame.getInstance().getJPatchScreen().update_all();
	//	//System.out.println("PointSelection.reInit(" + ps + ")");
	//	//fBeta = 0;
	//}
	
	public int getButton() {
		return MeshToolBar.DEFAULT;
	}
	
	private void setActiveHelpText() {
		switch (iState) {
			case MOVE_SINGLE_POINT:
				MainFrame.getInstance().setHelpText("Drag to move point. Press right mousebutton to weld to closest point. Hold CTRL and press right mousebutton to attach to closest point.");
				break;
			case MOVE_GROUP:
				if (MainFrame.getInstance().getPointSelection().getHotCp() != null) {
					MainFrame.getInstance().setHelpText("Drag to move selection. Press right mousebutton to weld to closest point");
				} else {
					MainFrame.getInstance().setHelpText("Drag to move selection.");
				}
				break;
			case DRAW_SELECTION:
				MainFrame.getInstance().setHelpText("Drag to select points");
				break;
			case ADD_MODIFY_SELECTION:
				MainFrame.getInstance().setHelpText("Drag to select points to be added to current selection");
				break;
			case XOR_MODIFY_SELECTION:
				MainFrame.getInstance().setHelpText("Drag to select points to be removed from current selection");
				break;
			case PIVOT:
				MainFrame.getInstance().setHelpText("Drag to move pivot");
				break;
			case SCALE_GROUP:
				MainFrame.getInstance().setHelpText("Drag to scale selected points");
				break;
		}
	}
	
	protected boolean isHit(int x, int y, Matrix4f m4View) {
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		Point3f p3A = new Point3f(ps.getCornerA());
		Point3f p3B = new Point3f(ps.getCornerB());
		float scale = 12f / m4View.getScale();
		Vector3f v3Margin = new Vector3f(scale,scale,scale);
		p3A.sub(v3Margin);
		p3B.add(v3Margin);
		Point3f[] ap3 = new Point3f[] {
			new Point3f(p3A.x, p3A.y, p3A.z),
			new Point3f(p3A.x, p3A.y, p3B.z),
			new Point3f(p3B.x, p3A.y, p3B.z),
			new Point3f(p3B.x, p3A.y, p3A.z),
			new Point3f(p3A.x, p3B.y, p3A.z),
			new Point3f(p3A.x, p3B.y, p3B.z),
			new Point3f(p3B.x, p3B.y, p3B.z),
			new Point3f(p3B.x, p3B.y, p3A.z)
		};
		
		for (int p = 0; p < 8; p++) {
			ps.getRotation().transform(ap3[p]);
			m4View.transform(ap3[p]);
		}
		
		Polygon polygon = new Polygon();
		
		polygon.reset();
		polygon.addPoint((int)ap3[0].x,(int)ap3[0].y);
		polygon.addPoint((int)ap3[1].x,(int)ap3[1].y);
		polygon.addPoint((int)ap3[2].x,(int)ap3[2].y);
		polygon.addPoint((int)ap3[3].x,(int)ap3[3].y);
		if (polygon.contains(x,y)) return true;
		
		polygon.reset();
		polygon.addPoint((int)ap3[4].x,(int)ap3[4].y);
		polygon.addPoint((int)ap3[5].x,(int)ap3[5].y);
		polygon.addPoint((int)ap3[6].x,(int)ap3[6].y);
		polygon.addPoint((int)ap3[7].x,(int)ap3[7].y);
		if (polygon.contains(x,y)) return true;
		
		polygon.reset();
		polygon.addPoint((int)ap3[0].x,(int)ap3[0].y);
		polygon.addPoint((int)ap3[1].x,(int)ap3[1].y);
		polygon.addPoint((int)ap3[5].x,(int)ap3[5].y);
		polygon.addPoint((int)ap3[4].x,(int)ap3[4].y);
		if (polygon.contains(x,y)) return true;
		
		polygon.reset();
		polygon.addPoint((int)ap3[3].x,(int)ap3[3].y);
		polygon.addPoint((int)ap3[2].x,(int)ap3[2].y);
		polygon.addPoint((int)ap3[6].x,(int)ap3[6].y);
		polygon.addPoint((int)ap3[7].x,(int)ap3[7].y);
		if (polygon.contains(x,y)) return true;
		
		polygon.reset();
		polygon.addPoint((int)ap3[1].x,(int)ap3[1].y);
		polygon.addPoint((int)ap3[2].x,(int)ap3[2].y);
		polygon.addPoint((int)ap3[6].x,(int)ap3[6].y);
		polygon.addPoint((int)ap3[5].x,(int)ap3[5].y);
		if (polygon.contains(x,y)) return true;
		
		polygon.reset();
		polygon.addPoint((int)ap3[0].x,(int)ap3[0].y);
		polygon.addPoint((int)ap3[3].x,(int)ap3[3].y);
		polygon.addPoint((int)ap3[7].x,(int)ap3[7].y);
		polygon.addPoint((int)ap3[4].x,(int)ap3[4].y);
		if (polygon.contains(x,y)) return true;
		
		return false;
	}
	
	public void paint(ViewDefinition viewDef) {
		//drawable.clearZBuffer();
		JPatchDrawable2 drawable = viewDef.getDrawable();
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		
//		if (MainFrame.getInstance().getJPatchScreen().showTangents() && tangentTool != null) tangentTool.paint(viewport, drawable); // FIXME
				
		if (ps != null && ps.getSize() > 1) {
			Matrix4f m4View = viewDef.getMatrix();
			Point3f p3A = new Point3f(ps.getCornerA());
			Point3f p3B = new Point3f(ps.getCornerB());
			float scale = 12f / m4View.getScale();
			Vector3f v3Margin = new Vector3f(scale,scale,scale);
			p3A.sub(v3Margin);
			p3B.add(v3Margin);
			Point3f[] ap3 = new Point3f[] {
				new Point3f(p3A.x, p3A.y, p3A.z),
				new Point3f(p3A.x, p3A.y, p3B.z),
				new Point3f(p3B.x, p3A.y, p3B.z),
				new Point3f(p3B.x, p3A.y, p3A.z),
				new Point3f(p3A.x, p3B.y, p3A.z),
				new Point3f(p3A.x, p3B.y, p3B.z),
				new Point3f(p3B.x, p3B.y, p3B.z),
				new Point3f(p3B.x, p3B.y, p3A.z)
			};
			
			for (int p = 0; p < 8; p++) {
				ps.getRotation().transform(ap3[p]);
				aHandle[p].getPosition().set(ap3[p]);
				m4View.transform(ap3[p]);
			}
			
			drawable.setColor(new Color3f(settings.cSelection)); // FIXME
//			drawable.drawGhostLine3D(ap3[0],ap3[1],GHOST_FACTOR);
//			drawable.drawGhostLine3D(ap3[1],ap3[2],GHOST_FACTOR);
//			drawable.drawGhostLine3D(ap3[2],ap3[3],GHOST_FACTOR);
//			drawable.drawGhostLine3D(ap3[3],ap3[0],GHOST_FACTOR);
//			drawable.drawGhostLine3D(ap3[4],ap3[5],GHOST_FACTOR);
//			drawable.drawGhostLine3D(ap3[5],ap3[6],GHOST_FACTOR);
//			drawable.drawGhostLine3D(ap3[6],ap3[7],GHOST_FACTOR);
//			drawable.drawGhostLine3D(ap3[7],ap3[4],GHOST_FACTOR);
//			drawable.drawGhostLine3D(ap3[0],ap3[4],GHOST_FACTOR);
//			drawable.drawGhostLine3D(ap3[1],ap3[5],GHOST_FACTOR);
//			drawable.drawGhostLine3D(ap3[2],ap3[6],GHOST_FACTOR);
//			drawable.drawGhostLine3D(ap3[3],ap3[7],GHOST_FACTOR);
			drawable.drawLine(ap3[0],ap3[1]);
			drawable.drawLine(ap3[1],ap3[2]);
			drawable.drawLine(ap3[2],ap3[3]);
			drawable.drawLine(ap3[3],ap3[0]);
			drawable.drawLine(ap3[4],ap3[5]);
			drawable.drawLine(ap3[5],ap3[6]);
			drawable.drawLine(ap3[6],ap3[7]);
			drawable.drawLine(ap3[7],ap3[4]);
			drawable.drawLine(ap3[0],ap3[4]);
			drawable.drawLine(ap3[1],ap3[5]);
			drawable.drawLine(ap3[2],ap3[6]);
			drawable.drawLine(ap3[3],ap3[7]);
			
			comparator.setMatrix(m4View);
			
			Handle[] aHandleCopy = (Handle[])aHandle.clone();
			Arrays.sort(aHandleCopy,comparator);
			
			for (int h = aHandleCopy.length - 1; h >= 0; h--) {
				aHandleCopy[h].paint(viewDef);
//				aHandleCopy[h].paint(viewport, drawable); // FIXME
			}
		}
	}
	
	private void setPassive() {
		for (int h = 0; h < aHandle.length; h++) {
			aHandle[h].setActive(false);
		}
	}
	
	public void mousePressed(MouseEvent mouseEvent) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
		m4Transform.setIdentity();
		
		/* LEFT MOUSE BUTTON */
		if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
			
			
			/* disable popup menu */
			//System.out.println("disable popup");
			MainFrame.getInstance().getJPatchScreen().enablePopupMenu(false);
			int x = mouseEvent.getX();
			int y = mouseEvent.getY();
			PointSelection ps = MainFrame.getInstance().getPointSelection();
			
			
			//System.out.println ("ps = " + ps);
			//viewport = (Viewport)mouseEvent.getSource();
			
			// FIXME
//			if (MainFrame.getInstance().getJPatchScreen().showTangents()) {
//				tangentHandle = tangentTool.isHit(viewport, x, y); // FIXME
//				if (tangentHandle != null) {
//					if (mouseEvent.getClickCount() == 2) {
//						fMagnitude = tangentHandle.getMagnitude();
//						tangentHandle.getCp().setMagnitude(1);
//						tangentHandle.getCp().invalidateTangents();
//						MainFrame.getInstance().getUndoManager().addEdit(new ChangeControlPointMagnitudeEdit(tangentHandle.getCp(),fMagnitude));
//						MainFrame.getInstance().getJPatchScreen().update_all();
//						return;
//					}
//					
//					fMagnitude = tangentHandle.getMagnitude();
//					tangentHandle.setFactor(mouseEvent);
//					((Component)mouseEvent.getSource()).addMouseMotionListener(tangentHandle);
//					iState = TANGENT;
//					return;
//				}
//			}
			
			paint(viewDef);
			compoundEdit = new JPatchCompoundEdit();
			
			boolean repaint = false;
			
			activeHandle = null;
			setPassive();
			iState = IDLE;
			
			if (ps != null) {
				p3Pivot.set(ps.getPivot());
			}
			
			/* check if a handle was clicked */
			if (ps != null && ps.getSize() > 1) {
				ap3 = ps.getPointArray();
				
				float z = Float.MAX_VALUE;
				Point3f p3Hit = new Point3f();
				
				for (int h = 0; h < aHandle.length; h++) {
					if (aHandle[h].isHit(viewDef, x, y, p3Hit) && (activeHandle == null || p3Hit.z < z)) {
						//System.out.println("hit");
						z = p3Hit.z;
						activeHandle = aHandle[h];
					}
				}
				
				/* check for double-click on pivot handle */
				if (mouseEvent.getClickCount() == 2) {
					if (activeHandle == pivotHandle) {
						
						/* reset pivot to selection center */
						//p3Pivot.set(ps.getPivot());
						ps.resetPivotToCenter();
						MainFrame.getInstance().getUndoManager().addEdit(new ChangeSelectionPivotEdit(ps,p3Pivot,null));
						
						/* clear active handle */
						((Component)mouseEvent.getSource()).removeMouseMotionListener(activeHandle);
						activeHandle = null;
						return;
					}
				}
			}
			
			/* if a handle was clicked */
			if (activeHandle != null) {
				
				/* activate it */
				activeHandle.setActive(true);
				activeHandle.setMouse(x, y);
				((Component)mouseEvent.getSource()).addMouseMotionListener(activeHandle);
				
				/* we need to repaint */
				repaint = true;
				
				/* check if a scale or the pivot handle is active and set state */
				iState = PIVOT;
				if (activeHandle != pivotHandle) {
					((DefaultHandle)activeHandle).setOldPosition();
					iState = SCALE_GROUP;
					compoundEdit.addEdit(new NewMoveControlPointsEdit(ps.getControlPointArray()));
				}	
			} else {
				/* no handle was clicked... */
				Matrix4f m4View = viewDef.getMatrix();
				
				/* check if a controlpoint was clicked... */
				cpHot = viewDef.getClosestControlPoint(new Point2D.Float(x,y),null,null,true,false,cpHot);
				
				/* if a point was hit... */
				if (cpHot != null) {
					
					/* if neither shift nor control is down */
					if ((!mouseEvent.isControlDown() && !mouseEvent.isShiftDown()) || ps == null) {
						
						/* is the point inside the selection box? */
						if (ps != null && ps.getSize() > 1 && isHit(x,y,viewDef.getScreenMatrix())) {
							
							/* change the hot cp */
							if (ps.contains(cpHot)) {
								compoundEdit.addEdit(new ChangeSelectionCPHotEdit(ps,cpHot));
								repaint = true;
							}
							
							///* check if the selection contains the hotCp */
							//if (!ps.contains(cpHot)) {
							//	
							//	/* add it to the selection */
							//	Collection collection = new ArrayList();
							//	collection.add(cpHot);
							//	compoundEdit.addEdit(new AddControlPointsToSelectionEdit(ps, collection));
							//	repaint = true;
							//}
							
							/* set state */
							iState = MOVE_GROUP;
							compoundEdit.addEdit(new NewMoveControlPointsEdit(ps.getControlPointArray()));
							
							/* add motionlistener */
							((Component)mouseEvent.getSource()).addMouseMotionListener(this);
							iMouseX = x;
							iMouseY = y;
							bMoveZ = mouseEvent.isAltDown();
						} else {
							
							/*
							 * selection box was not hit
							 * create a new selection containing only cphot
							 */
							compoundEdit.addEdit(new ChangeSelectionEdit(new PointSelection(cpHot)));
							
							/* set state */
							iState = MOVE_SINGLE_POINT;
							//compoundEdit.addEdit(new NewMoveControlPointsEdit(new ControlPoint[] { cpHot } ));
							cpHot.prepareForTemporaryTransform();
							repaint = true;
							iMouseX = x;
							iMouseY = y;
							/* add motionlistener */
							((Component)mouseEvent.getSource()).addMouseMotionListener(this);
							bMoveZ = mouseEvent.isAltDown();
						}
					} else {
						
						/* control or shift was down */
						if (mouseEvent.isControlDown() && ps.contains(cpHot)) {
							
							/* remove the cp from the selection */
							Collection collection = new ArrayList();
							collection.add(cpHot);
							compoundEdit.addEdit(new RemoveControlPointsFromSelectionEdit(ps,collection));
							compoundEdit.addEdit(new ChangeSelectionCPHotEdit(ps,null));
							MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
							
							/* set state */
							
							iState = IDLE;
							
							repaint = true;
						} else if ((mouseEvent.isShiftDown() || (mouseEvent.isControlDown()) && !ps.contains(cpHot))) {
							
							/* add the cp to the selection */
							Collection collection = new ArrayList();
							collection.add(cpHot);
							compoundEdit.addEdit(new AddControlPointsToSelectionEdit(ps,collection));
							compoundEdit.addEdit(new ChangeSelectionCPHotEdit(ps,cpHot));
							MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
							//ps.addControlPoint(cpHot);
							//ps.setHotCp(cpHot);
							
							/* set state */
							
							iState = IDLE;
							//} else {
							//	iState = (mouseEvent.isShiftDown()) ? ADD_MODIFY_SELECTION : XOR_MODIFY_SELECTION;
							//	iMouseX = x;
							//	iMouseY = y;
							//}
							repaint = true;
						}
					}
				} else {
					
					/* no point was hit, clear cpHot */
					
					if (ps != null) {
						compoundEdit.addEdit(new ChangeSelectionCPHotEdit(ps,null));
						repaint = true;
					}
					//System.out.println("* " + ps);
					/* is shift or control down? set state */
					if (mouseEvent.isShiftDown() && ps != null) {
						iState = ADD_MODIFY_SELECTION;
						iMouseX = x;
						iMouseY = y;
					} else if (mouseEvent.isControlDown() && ps != null) {
						iState = XOR_MODIFY_SELECTION;
						iMouseX = x;
						iMouseY = y;
					} else {
						//System.out.println("**");
						/* neither shift nor control are down */
						
						/* check if selection box was hit and set state*/
						if (ps != null && ps.getSize() > 1 && isHit(x,y,viewDef.getScreenMatrix())) {
							
							/* selection box was hit, set state*/
							iState = MOVE_GROUP;
							compoundEdit.addEdit(new NewMoveControlPointsEdit(ps.getControlPointArray()));
							
							/* add motionlistener */
							((Component)mouseEvent.getSource()).addMouseMotionListener(this);
							iMouseX = x;
							iMouseY = y;
							bMoveZ = mouseEvent.isAltDown();
						} else {
							
							/* selection box was not it, set state */
							iState = DRAW_SELECTION;
							iMouseX = x;
							iMouseY = y;
							
							
						}
					}
				}
				
				/* if a new selection rectangle is to be drawn */
				if (iState == DRAW_SELECTION || iState == ADD_MODIFY_SELECTION || iState == XOR_MODIFY_SELECTION) {
					
					/* add the mouseMotionListener */
					selectMouseMotionListener = new SelectMouseMotionListener(x,y);
					((Component)mouseEvent.getSource()).addMouseMotionListener(selectMouseMotionListener);
				}
			}
			
			/* check if we need to repaint */
			if (repaint) {
				MainFrame.getInstance().getJPatchScreen().single_update((Component)mouseEvent.getSource());
			}
		                //
				//
				//
				//
				//
				///* check if we have a selection - and - if the click hit the selection box */
				//if (ps != null && ps.getSize() > 1 && isHit(x,y,m4View)) {
				//	((Component)mouseEvent.getSource()).addMouseMotionListener(this);
				//	iMouseX = x;
				//	iMouseY = y;
				//	//iMode = SELECTION;
				//	iState = MOVE_GROUP;
				//	cpHot = viewport.getViewDefinition().getClosestControlPoint(new Point2D.Float(x,y),null,null,true);
				//	if (cpHot != null) {
				//		if (mouseEvent.isControlDown() && ps.contains(cpHot)) {
				//			ps.removeControlPoint(cpHot);
				//			ps.setHotCp(null);
				//		} else if ((mouseEvent.isShiftDown() || mouseEvent.isControlDown()) && !ps.contains(cpHot)) {
				//			ps.addControlPoint(cpHot);
				//			ps.setHotCp(cpHot);
				//		}
				//		repaint = true;
				//		ps.resetPivotToCenter();
				//		ap3 = ps.getPointArray();
				//	}
				//		
				//} else {
				//	cpHot = viewport.getViewDefinition().getClosestControlPoint(new Point2D.Float(x,y),null,null,true);
				//	if (cpHot != null) {
				//		((Component)mouseEvent.getSource()).addMouseMotionListener(this);
				//		if ((mouseEvent.isShiftDown() || mouseEvent.isControlDown()) && ps != null) {
				//			ps.addControlPoint(cpHot);
				//			ps.resetPivotToCenter();
				//		} else {
				//			ps = new PointSelection(cpHot);
				//		}
				//		MainFrame.getInstance().setSelection(ps);
				//		repaint = true;
				//		//iMode = SINGLE_POINT;
				//	} else {
				//		SelectMouseAdapter selectMouseAdapter = new SelectMouseAdapter(x,y);
				//		((Component)mouseEvent.getSource()).addMouseListener(selectMouseAdapter);
				//		((Component)mouseEvent.getSource()).addMouseMotionListener(selectMouseAdapter);
				//	}
				//}
			//}
			//if (repaint) {
			//	MainFrame.getInstance().getJPatchScreen().single_update((Component)mouseEvent.getSource());
			//}
			//MainFrame.getInstance().setHelpText("Hold SHIFT to snap to 15� steps. Hold CTRL to rotate coordinate system. Hold ALT to keep rotation.");
		
			//System.out.println("STATE = " + iState);
		} else if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
			
			/* RIGHT MOUSE BUTTON */
			
			/* check state */
			if (iState == MOVE_SINGLE_POINT || (cpHot != null && iState == MOVE_GROUP)) {
				Viewport viewport = (Viewport)mouseEvent.getSource();
				float[] hookPos = new float[1];
				ControlPoint cp = viewport.getViewDefinition().getClosestControlPoint(new Point2D.Float(mouseEvent.getX(),mouseEvent.getY()),cpHot,hookPos,false,true);
				PointSelection ps = MainFrame.getInstance().getPointSelection();
				if (cp != null && cp != cpHot.getPrev() && cp != cpHot.getNext()) {
					if (hookPos[0] == -1) {
						//System.out.println("*");
						if (cp.isSingle() || cpHot.isSingle()) {
							// - compoundEdit.addEdit(new MoveControlPointsEdit(MoveControlPointsEdit.TRANSLATE,ps.getControlPointArray()));
							if (!mouseEvent.isControlDown())
								compoundEdit.addEdit(new WeldControlPointsEdit(cpHot,cp));
							else
								compoundEdit.addEdit(CorrectSelectionsEdit.attachPoints(cpHot.getHead(),cp.getTail()));
							Collection collection = new ArrayList();
							collection.add(cpHot);
							compoundEdit.addEdit(new RemoveControlPointsFromSelectionEdit(ps,collection));
							collection.clear();
							collection.add(cp);
							compoundEdit.addEdit(new AddControlPointsToSelectionEdit(ps,collection));
							compoundEdit.addEdit(new ChangeSelectionCPHotEdit(ps,cp));
							//((PointSelection)MainFrame.getInstance().getSelection()).removeControlPoint(cpHot);
							MainFrame.getInstance().getJPatchScreen().full_update();
							((Component)mouseEvent.getSource()).removeMouseMotionListener(this);
							MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
							MainFrame.getInstance().getJPatchScreen().update_all();
							iState = IDLE;
						}
					} else if (iState == MOVE_SINGLE_POINT && cpHot.isSingle()) {
						if (cpHot.getPrev() == null || cpHot.getNext() == null) {
							if ((cpHot.getPrev() != null && (cpHot.getPrev().getNextAttached() == null || !cpHot.getPrev().getNextAttached().isHook())) || (cpHot.getNext().getNextAttached() == null || !cpHot.getNext().getNextAttached().isHook())) {
								if (cpHot.getChildHook() == null && (cpHot.getPrev() == null || cpHot.getPrev().getChildHook() == null)) {
									if (!cp.getNext().getHead().isHook() && !cp.getHead().isHook()) {
										if (cp.getHookAt(hookPos[0]) == null) {
											// - compoundEdit.addEdit(new MoveControlPointsEdit(MoveControlPointsEdit.TRANSLATE,ps.getControlPointArray()));
											compoundEdit.addEdit(new HookEdit(cpHot,cp,hookPos[0]));
											//Collection collection = new ArrayList();
											//collection.add(cpHot);
											//compoundEdit.addEdit(new RemoveControlPointsFromSelectionEdit(ps,collection));
											//collection.clear();
											//collection.add(cpHot.getHead());
											//compoundEdit.addEdit(new AddControlPointsToSelectionEdit(ps,collection));
											//((PointSelection)MainFrame.getInstance().getSelection()).removeControlPoint(cpHot);
											MainFrame.getInstance().getJPatchScreen().full_update();
											((Component)mouseEvent.getSource()).removeMouseMotionListener(this);
											MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
											MainFrame.getInstance().getJPatchScreen().update_all();
										} else {
											ControlPoint hook = cp.getHookAt(hookPos[0]);
											compoundEdit.addEdit(new ConvertHookToCpEdit(hook));
											compoundEdit.addEdit(new WeldControlPointsEdit(cpHot,hook));
											Collection collection = new ArrayList();
											collection.add(cpHot);
											compoundEdit.addEdit(new RemoveControlPointsFromSelectionEdit(ps,collection));
											collection.clear();
											collection.add(cp);
											compoundEdit.addEdit(new AddControlPointsToSelectionEdit(ps,collection));
											compoundEdit.addEdit(new ChangeSelectionCPHotEdit(ps,hook));
											//((PointSelection)MainFrame.getInstance().getSelection()).removeControlPoint(cpHot);
											MainFrame.getInstance().getJPatchScreen().full_update();
											((Component)mouseEvent.getSource()).removeMouseMotionListener(this);
											MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
											MainFrame.getInstance().getJPatchScreen().update_all();
											iState = IDLE;
										}
									}
								}
							}
						}
						
						iState = IDLE;
					}
				}
			}
		}
		if (iState == IDLE) {
			/* enable popup menu */
			//System.out.println("enable popup");
			MainFrame.getInstance().getJPatchScreen().enablePopupMenu(true);
		} else {
			setActiveHelpText();
		}
	}
	
	public void mouseReleased(MouseEvent mouseEvent) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
		//Viewport viewport = (Viewport)mouseEvent.getSource();
		if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
			PointSelection ps = MainFrame.getInstance().getPointSelection();
			JPatchUndoableEdit edit;
			
			/* check if Control or Shift was down (on mouse release) and modify state if necessary */
			if ((iState == DRAW_SELECTION || iState == ADD_MODIFY_SELECTION || iState == XOR_MODIFY_SELECTION) && ps != null) {
				if (mouseEvent.isControlDown()) {
					iState = XOR_MODIFY_SELECTION;
				}
				if (mouseEvent.isShiftDown()) {
					iState = ADD_MODIFY_SELECTION;
				}
			}
			
			//System.out.println(ps + " " + iState);
			switch (iState) {
				case MOVE_SINGLE_POINT:
					transformPermanent();
					break;
				case MOVE_GROUP:
					// - compoundEdit.addEdit(new MoveControlPointsEdit(MoveControlPointsEdit.TRANSLATE,ps.getControlPointArray()));
					compoundEdit.addEdit(new ChangeSelectionPivotEdit(ps,p3Pivot,null));
					MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
					break;
				case SCALE_GROUP:
					// edit = new MoveControlPointsEdit(MoveControlPointsEdit.SCALE,ps.getControlPointArray());
					MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
					((Component)mouseEvent.getSource()).removeMouseMotionListener(activeHandle);
					break;
				case DRAW_SELECTION:
					Selection selection = selectMouseMotionListener.getSelection(viewDef);
					if (selection != null || ps != null) {
						edit = new ChangeSelectionEdit(selection);
						MainFrame.getInstance().getUndoManager().addEdit(edit);
					}
					((Component)mouseEvent.getSource()).removeMouseMotionListener(selectMouseMotionListener);
					selectionChanged(selection);
					break;
				case ADD_MODIFY_SELECTION:
					PointSelection psNew = (PointSelection)selectMouseMotionListener.getSelection(viewDef);
					if (psNew != null) {
						Collection colPointsToAdd = psNew.getSelectedControlPoints();
						if (ps != null) colPointsToAdd.removeAll(ps.getSelectedControlPoints());
						compoundEdit.addEdit(new AddControlPointsToSelectionEdit(ps,colPointsToAdd));
						//compoundEdit.addEdit(new ChangeSelectionPivotEdit(ps,ps.getCenter(),null));
						MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
					}
					((Component)mouseEvent.getSource()).removeMouseMotionListener(selectMouseMotionListener);
					selectionChanged(ps);
					break;
				case XOR_MODIFY_SELECTION:
					psNew = (PointSelection)selectMouseMotionListener.getSelection(viewDef);
					if (psNew != null) {
						Collection colNewSelection = psNew.getSelectedControlPoints();
						Collection colPointsToAdd = new ArrayList();
						Collection colPointsToRemove = new ArrayList();
						//Collection colSelection = ps.getSelectedControlPoints();
						for (Iterator it = colNewSelection.iterator(); it.hasNext(); ) {
							ControlPoint cp = (ControlPoint)it.next();
							// commented out - the following lines would xor the selection
							// chaged to just remove points from selection
							//if (colSelection.contains(cp)) {
							//	colPointsToRemove.add(cp);
							//} else {
							//	colPointsToAdd.add(cp);
							//}
							colPointsToRemove.add(cp);
						}
						compoundEdit.addEdit(new AddControlPointsToSelectionEdit(ps,colPointsToAdd));
						compoundEdit.addEdit(new RemoveControlPointsFromSelectionEdit(ps,colPointsToRemove));
						//compoundEdit.addEdit(new ChangeSelectionPivotEdit(ps,ps.getCenter(),null));
						MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
					}
					((Component)mouseEvent.getSource()).removeMouseMotionListener(selectMouseMotionListener);
					selectionChanged(ps);
					break;
				case PIVOT:
					if (!ps.getPivot().equals(p3Pivot)) {
						MainFrame.getInstance().getUndoManager().addEdit(new ChangeSelectionPivotEdit(ps,p3Pivot,null));
						((Component)mouseEvent.getSource()).removeMouseMotionListener(activeHandle);
					}
					break;
				case TANGENT:
					if (tangentHandle.getCp().getInMagnitude() != fMagnitude) {
						MainFrame.getInstance().getUndoManager().addEdit(new ChangeControlPointMagnitudeEdit(tangentHandle.getCp(),fMagnitude));
					}
					((Component)mouseEvent.getSource()).removeMouseMotionListener(tangentHandle);
					break;
			}
			((Component)mouseEvent.getSource()).removeMouseMotionListener(this);
			MainFrame.getInstance().getJPatchScreen().update_all();
			iState = IDLE;
			if (MainFrame.getInstance().getPointSelection() == null) {
				MainFrame.getInstance().setHelpText("Drag to move or select points. Use ATL to move perpendicular to screen plane.");
			} else if (MainFrame.getInstance().getPointSelection().isSingle()) {
				MainFrame.getInstance().setHelpText("Use SHIFT or CTRL to modify selection. Use ATL to move perpendicular to screen plane. Press TAB to cycle through curve segments.");
			} else {
				MainFrame.getInstance().setHelpText("Drag inside box to move selection. Drag handles to scale. Use SHIFT or CTRL to modify selection, ATL to move perpendicular. Doubleclick pivot to reset.");
			}
		}
				
		//	if (ps != null) {
		//		JPatchUndoableEdit edit = new MoveControlPointsEdit(MoveControlPointsEdit.TRANSLATE,ps.getControlPointArray());
		//		MainFrame.getInstance().getUndoManager().addEdit(edit);
		//	}
		//	MainFrame.getInstance().getJPatchScreen().update_all();
		//	((Component)mouseEvent.getSource()).removeMouseMotionListener(activeHandle);
		//	((Component)mouseEvent.getSource()).removeMouseMotionListener(this);
		//	MainFrame.getInstance().getJPatchScreen().enablePopupMenu(true);
		//	MainFrame.getInstance().setHelpText("Click and drag handles to rotate or move pivot. Click and drag inside yellow circle to rotate freely. Doubleclick to reset coordinate system or pivot.");
		//}
		
		if (iState == IDLE) {
			/* enable popup menu */
			//System.out.println("enable popup");
			MainFrame.getInstance().getJPatchScreen().enablePopupMenu(true);
		}
	}
	
	public void mouseDragged(MouseEvent mouseEvent) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
		//System.out.println("mouseDragged");
		int iDeltaX = mouseEvent.getX() - iMouseX;
		int iDeltaY = mouseEvent.getY() - iMouseY;
		Matrix4f m4InvScreenMatrix = new Matrix4f(viewDef.getScreenMatrix());
		m4InvScreenMatrix.invert();
		switch(iState) {
			case MOVE_GROUP: {
		//if (cpHot == null) {
				//iMouseX = mouseEvent.getX();
				//iMouseY = mouseEvent.getY();
				Vector3f v3Move;
				if (bMoveZ) {
					v3Move = new Vector3f(0,0,iDeltaY - iDeltaX);
				} else {
					v3Move = new Vector3f(iDeltaX,iDeltaY,0);
				}
				
				//Vector3f v3Ortho = new Vector3f(0,0,1);
				m4InvScreenMatrix.transform(v3Move);
				//m4InvScreenMatrix.transform(v3Ortho);
				//if (Math.abs(v3Ortho.y) > 0.01f);
				//	float f = v3Move.y / v3Ortho.y;
				//	v3Move.addScaled(v3Ortho, -f);
					translate(v3Move, viewDef);
				//}
			}
			break;
		//} else {
			case MOVE_SINGLE_POINT: {
			
				Vector3f v3Move = bMoveZ ? new Vector3f(0,0,iDeltaY - iDeltaX) : new Vector3f(iDeltaX, iDeltaY, 0);
				m4InvScreenMatrix.transform(v3Move);
				m4Transform.set(v3Move);
				transformTemporary();
				if (bMoveZ) {
					MainFrame.getInstance().getJPatchScreen().update_all();
				} else {
					MainFrame.getInstance().getJPatchScreen().single_update(viewDef.getDrawable().getComponent());
				}
				//Point3f p3Old = cpHot.getPosition();
				//viewport.getViewDefinition().setZ(p3Old);
				//Point3f p3New;
				//if (bMoveZ) {
				//	p3New = new Point3f(cpHot.getPosition());
				//	//System.out.println(p3New);
				//	Vector3f v3Z = new Vector3f(0,0,iDeltaY - iDeltaX);
				//	m4InvScreenMatrix.transform(v3Z);
				//	//m4InvScreenMatrix.transform(p3New);
				//	p3New.add(v3Z);
				//	//viewport.getViewDefinition().getMatrix().transform(p3New);
				//} else {
				//	p3New = viewport.getViewDefinition().get3DPosition((float)mouseEvent.getX(),(float)mouseEvent.getY());
				//}
				////MainFrame.getInstance().getConstraints().setPointPosition(p3Old,p3New);
				//if ((bMoveZ && viewport.getGrid().correctZPosition(p3Old,p3New)) || viewport.getGrid().correctPosition(p3Old,p3New)) {
				//	//Point3f p3NewPos = viewport.getGrid().getCorrectedPosition(viewport.getViewDefinition().get3DPosition((float)mouseEvent.getX(),(float)mouseEvent.getY()));
				//	//if (cpH
				//	//MainFrame.getInstance().getConstraints().setControlPointPosition(cpHot,viewport.getGrid().getCorrectedPosition(viewport.getViewDefinition().get3DPosition((float)mouseEvent.getX(),(float)mouseEvent.getY())));
				//	//if (!p3New.equals(p3Old)) {
				//	//cpHot.setPosition(p3New);
				//	MainFrame.getInstance().getConstraints().setControlPointPosition(cpHot,p3New);
				//	if (bMoveZ) {
				//		MainFrame.getInstance().getJPatchScreen().update_all();
				//	} else {
				//		MainFrame.getInstance().getJPatchScreen().single_update((Component)viewport);
				//	}
				//}
				//break;
			}
			break;
		}
	}
	
	protected void prepare() {
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		ControlPoint[] acp = ps.getControlPointArray();
		for (int i = 0, n = acp.length; i < n; i++) {
			acp[i].prepareForTemporaryTransform();
		}
	}
	
	protected void transformPermanent() {
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		ControlPoint[] acp = ps.getControlPointArray();
		for (int i = 0, n = acp.length; i < n; i++) {
			compoundEdit.addEdit(acp[i].transformPermanent(m4Transform));
		}
		MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
	}
	
	protected void transformTemporary() {
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		ControlPoint[] acp = ps.getControlPointArray();
		for (int i = 0, n = acp.length; i < n; i++) {
			acp[i].transformTemporary(m4Transform);
		}
	}
	
	protected void translate(Vector3f v, ViewDefinition viewDef) {
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		Point3f point = new Point3f();
		ControlPoint[] acp = ps.getControlPointArray();
		//if (bMoveZ) {
		//	Vector3f cv = viewport.getGrid().getZCorrectionVector(v);
		//} else {
		//	Vector3f cv = viewport.getGrid().getCorrectionVector(v);
		//}
		
		
		Vector3f vector = new Vector3f(v);
		if (bMoveZ) {
//			viewport.getGrid().correctZVector(vector);
		} else {
//			viewport.getGrid().correctVector(vector);
		}
		if (cpHot != null) {
			//System.out.println("*  " + vector);
			if (bMoveZ) {
//				vector.add(viewport.getGrid().getZCorrectionVector(cpHot.getPosition()));
			} else {
//				vector.add(viewport.getGrid().getCorrectionVector(cpHot.getPosition()));
			}
			//cv.set(-1,0,0);
			//System.out.println("** " + vector);
		}
		
		if (!vector.equals(v3Move)) {
			v3Move.set(vector);
			for (int p = 0; p < acp.length; p++) {
				point.add(ap3[p],vector);
				MainFrame.getInstance().getConstraints().setControlPointPosition(acp[p],point);
			}
			Point3f newPivot = new Point3f(p3Pivot);
			newPivot.add(vector);
			MainFrame.getInstance().getConstraints().setPointPosition(ps.getPivot(),newPivot);
			
			if (bMoveZ) {
				MainFrame.getInstance().getJPatchScreen().update_all();
			} else {
				MainFrame.getInstance().getJPatchScreen().single_update(viewDef.getDrawable().getComponent());
			}
		}
	}
	
	protected void scale(float scale, ViewDefinition viewDef) {
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		Point3f point = new Point3f();
		ControlPoint[] acp = ps.getControlPointArray();
		for (int p = 0; p < acp.length; p++) {
			point.sub(ap3[p],ps.getPivot());
			point.scale(scale);
			point.add(ps.getPivot());
			MainFrame.getInstance().getConstraints().setControlPointPosition(acp[p],point);
		}
		MainFrame.getInstance().getJPatchScreen().single_update(viewDef.getDrawable().getComponent());
	}
	
	private void selectionChanged(Selection selection) {
		JPatchTreeLeaf leaf = null;
		if (MainFrame.getInstance().getTree().getSelectionPath() != null)
			leaf = (JPatchTreeLeaf) MainFrame.getInstance().getTree().getSelectionPath().getLastPathComponent();
		if (leaf == null || leaf.getNodeType() == JPatchTreeLeaf.SELECTIONS || leaf.getNodeType() == JPatchTreeLeaf.SELECTION || leaf.getNodeType() == JPatchTreeLeaf.MODEL) {
			//MainFrame.getInstance().getSideBar().enableTreeSelectionListener(false);
			if (selection != null && !MainFrame.getInstance().getModel().checkSelection(selection)) {
			//	MainFrame.getInstance().getSideBar().enableTreeSelectionListener(false);
				MainFrame.getInstance().getTree().setSelectionPath(MainFrame.getInstance().getModel().getSelection(selection).getTreePath());
			//	MainFrame.getInstance().getSideBar().enableTreeSelectionListener(true);
			} else {
				MainFrame.getInstance().getTree().setSelectionPath(MainFrame.getInstance().getModel().getTreenodeSelections().getTreePath());
			}
			//MainFrame.getInstance().getSideBar().enableTreeSelectionListener(true);
		}
	}
}
