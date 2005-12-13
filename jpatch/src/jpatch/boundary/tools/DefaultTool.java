package jpatch.boundary.tools;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.vecmath.*;
import jpatch.boundary.*;
import jpatch.boundary.mouse.*;
import jpatch.entity.*;
import jpatch.entity.Bone.BoneTransformable;
import jpatch.control.edit.*;

public class DefaultTool extends JPatchTool {

	//private static final int IDLE = 0;
	//private static final int SINGLE_POINT = 1;
	//private static final int SELECTION = 2;
	//private static final int CURSOR = 3;
	
	private static final int IDLE = 0;
	private static final int DRAW_SELECTION = 1;
	private static final int MOVE_SINGLE_POINT = 2;
	private static final int MOVE_GROUP = 3;
	private static final int SCALE_GROUP = 4;
//	private static final int PIVOT = 5;
	private static final int ADD_MODIFY_SELECTION = 6;
	private static final int XOR_MODIFY_SELECTION = 7;
	private static final int TANGENT = 8;
	private static final int MOVE_SINGLE_BONENED = 9;
	
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
//	private Point3f[] ap3;
//	private PivotHandle2 pivotHandle;
	private ControlPoint cpHot;
	private Bone.BoneTransformable btHot;
	//private int iMode;
	private JPatchActionEdit edit;
//	private Point3f p3Pivot = new Point3f();
//	private Vector3f v3Move = new Vector3f();
	private int iState;
	//private Point3f p3Pivot = new Point3f();
	//private boolean bChange;
	private SelectMouseMotionListener selectMouseMotionListener;
	//private Viewport viewport;
	private boolean bMoveZ;
	private TangentTool tangentTool = MainFrame.getInstance().getJPatchScreen().getTangentTool();
	private TangentHandle tangentHandle;
	private float fMagnitude;
	private Vector3f v3Translate = new Vector3f();
	private Point3f p3Hot;
	
	private Matrix4f m4Transform = new Matrix4f();
//	private Matrix4f m4ConstrainedTransform = new Matrix4f();
	
//	private Point3f p3Pivot = new Point3f();
	
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
//		pivotHandle = new PivotHandle2(color);
		aHandle = new Handle[] {
			new DefaultHandle(this, new Point3f(), color),
			new DefaultHandle(this, new Point3f(), color),
			new DefaultHandle(this, new Point3f(), color),
			new DefaultHandle(this, new Point3f(), color),
			new DefaultHandle(this, new Point3f(), color),
			new DefaultHandle(this, new Point3f(), color),
			new DefaultHandle(this, new Point3f(), color),
			new DefaultHandle(this, new Point3f(), color),
//			pivotHandle
		};
		if (MainFrame.getInstance().getSelection() == null) {
			MainFrame.getInstance().setHelpText("Drag to move or select points. Use ATL to move perpendicular to screen plane.");
		} else if (MainFrame.getInstance().getSelection().isSingle()) {
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
				if (MainFrame.getInstance().getSelection().getHotObject() instanceof ControlPoint) {
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
//			case PIVOT:
//				MainFrame.getInstance().setHelpText("Drag to move pivot");
//				break;
			case SCALE_GROUP:
				MainFrame.getInstance().setHelpText("Drag to scale selected points");
				break;
		}
	}
	
	protected boolean isHit(int x, int y, Matrix4f m4View) {
		Selection selection = MainFrame.getInstance().getSelection();
		Point3f p3A = new Point3f();
		Point3f p3B = new Point3f();
		selection.getBounds(p3A, p3B);
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
			selection.getOrientation().transform(ap3[p]);
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
		Selection selection = MainFrame.getInstance().getSelection();
		
//		if (MainFrame.getInstance().getJPatchScreen().showTangents() && tangentTool != null) tangentTool.paint(viewport, drawable); // FIXME
		if (selection != null && !selection.isSingle()) {
			Matrix4f m4View = viewDef.getMatrix();
			Point3f p3A = new Point3f();
			Point3f p3B = new Point3f();
			selection.getBounds(p3A, p3B);
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
				selection.getOrientation().transform(ap3[p]);
				aHandle[p].getPosition(viewDef).set(ap3[p]);
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
			
			comparator.setViewDefinition(viewDef);
			
			Handle[] aHandleCopy = (Handle[])aHandle.clone();
			Arrays.sort(aHandleCopy,comparator);
			
			for (int h = aHandleCopy.length - 1; h >= 0; h--) {
				aHandleCopy[h].paint(viewDef);
//				aHandleCopy[h].paint(viewport, drawable); // FIXME
			}
			p3A.set(selection.getPivot());
			m4View.transform(p3A);
			drawable.setPointSize(3);
			drawable.drawPoint(p3A);
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
			Selection selection = MainFrame.getInstance().getSelection();
			
			
			//System.out.println ("ps = " + ps);
			//viewport = (Viewport)mouseEvent.getSource();
			
			if (MainFrame.getInstance().getJPatchScreen().showTangents()) {
				tangentHandle = tangentTool.isHit(viewDef, x, y);
				if (tangentHandle != null) {
					if (mouseEvent.getClickCount() == 2) {
						fMagnitude = tangentHandle.getMagnitude();
						tangentHandle.getCp().setMagnitude(1);
//						tangentHandle.getCp().invalidateTangents();
						MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeControlPoint.Magnitude(tangentHandle.getCp(),fMagnitude));
						MainFrame.getInstance().getJPatchScreen().update_all();
						return;
					}
					
					fMagnitude = tangentHandle.getMagnitude();
					tangentHandle.setFactor(mouseEvent);
					((Component)mouseEvent.getSource()).addMouseMotionListener(tangentHandle);
					iState = TANGENT;
					return;
				}
			}
			
//			paint(viewDef);
			edit = new JPatchActionEdit("default tool");
			
			boolean repaint = false;
			
			activeHandle = null;
			setPassive();
			iState = IDLE;
			
//			if (ps != null) {
//				p3Pivot.set(ps.getPivot());
//			}
			
			/* check if a handle was clicked */
			if (selection != null && !selection.isSingle()) {
				
				float z = Float.MAX_VALUE;
				Point3f p3Hit = new Point3f();
				
				
				Matrix4f m4View = viewDef.getMatrix();
				Point3f p3A = new Point3f();
				Point3f p3B = new Point3f();
				selection.getBounds(p3A, p3B);
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
					selection.getOrientation().transform(ap3[p]);
					aHandle[p].getPosition(viewDef).set(ap3[p]);
				}
				
				for (int h = 0; h < aHandle.length; h++) {
					if (aHandle[h].isHit(viewDef, x, y, p3Hit) && (activeHandle == null || p3Hit.z < z)) {
						//System.out.println("hit");
						z = p3Hit.z;
						activeHandle = aHandle[h];
						aHandle[h].setActive(true);
					} else {
						aHandle[h].setActive(false);
					}
				}
				
//				/* check for double-click on pivot handle */
//				if (mouseEvent.getClickCount() == 2) {
//					if (activeHandle == pivotHandle) {
//						
//						/* reset pivot to selection center */
//						//p3Pivot.set(ps.getPivot());
//						//selection.resetPivot();
//						MainFrame.getInstance().getUndoManager().addEdit(new AtomicModifySelection.Pivot(selection, selection.getCenter()));
//						
//						/* clear active handle */
//						((Component)mouseEvent.getSource()).removeMouseMotionListener(activeHandle);
//						activeHandle = null;
//						return;
//					}
//				}
			}
			
			/* if a handle was clicked */
			if (activeHandle != null) {
				
				System.out.println("handle hit");
				/* activate it */
				activeHandle.setActive(true);
				activeHandle.setMouse(x, y);
				((Component)mouseEvent.getSource()).addMouseMotionListener(activeHandle);
				
				/* we need to repaint */
				repaint = true;
				
				/* check if a scale or the pivot handle is active and set state */
//				iState = PIVOT;
//				p3Pivot.set(selection.getPivot());
//				if (activeHandle != pivotHandle) {
					((DefaultHandle)activeHandle).setOldPosition(viewDef);
					iState = SCALE_GROUP;
					//compoundEdit.addEdit(new NewMoveControlPointsEdit(ps.getControlPointArray()));
					beginTransform();
//				}	
			} else {
				/* no handle was clicked... */
//				Matrix4f m4View = viewDef.getMatrix();
				
				/* check if a controlpoint was clicked... */
				Point2D.Float p2 = new Point2D.Float(x,y);
				cpHot = viewDef.getClosestControlPoint(p2, null, null, true, false, cpHot);
				btHot = viewDef.getClosestBoneEnd(p2, null, true, true);
				Bone hitBone = viewDef.getClosestBone(p2);
				if (!MainFrame.getInstance().getJPatchScreen().isSelectBones()) {
					btHot = null;
					hitBone = null;
				}
				if (!MainFrame.getInstance().getJPatchScreen().isSelectPoints()) {
					cpHot = null;
				}
				/* if a point was hit... */
				if (cpHot != null) {
					
					/* if neither shift nor control is down */
					if ((!mouseEvent.isControlDown() && !mouseEvent.isShiftDown()) || selection == null) {
						
						/* is the point inside the selection box? */
						if (selection != null && !selection.isSingle() && isHit(x,y,viewDef.getScreenMatrix())) {
							
							/* change the hot cp */
							if (selection.contains(cpHot)) {
								edit.addEdit(new AtomicModifySelection.HotObject(selection, cpHot));
								repaint = true;
							}
							/* set state */
							iState = MOVE_GROUP;
							beginTransform();
							iMouseX = x;
							iMouseY = y;
							/* add motionlistener */
							((Component)mouseEvent.getSource()).addMouseMotionListener(this);
							bMoveZ = mouseEvent.isAltDown();
//							p3Pivot.set(selection.getPivot());
						} else {
							/*
							 * selection box was not hit
							 * create a new selection containing only cphot
							 */
							edit.addEdit(new AtomicChangeSelection(new Selection(cpHot)));
							
							/* set state */
							iState = MOVE_SINGLE_POINT;
							//compoundEdit.addEdit(new NewMoveControlPointsEdit(new ControlPoint[] { cpHot } ));
							beginTransform();
							repaint = true;
							iMouseX = x;
							iMouseY = y;
							/* add motionlistener */
							((Component)mouseEvent.getSource()).addMouseMotionListener(this);
							bMoveZ = mouseEvent.isAltDown();
						}
					} else {
						
						/* control or shift was down */
						if (mouseEvent.isControlDown() && selection.contains(cpHot)) {
							
							/* remove the cp from the selection */
							Map map = new HashMap();
							map.put(cpHot, new Float(1));
							edit.addEdit(new AtomicModifySelection.RemoveObjects(selection, map));
							edit.addEdit(new AtomicModifySelection.HotObject(selection, null));
							edit.setName("remove point from selection");
							MainFrame.getInstance().getUndoManager().addEdit(edit);
							selectionChanged(selection);
							/* set state */
							
							iState = IDLE;
							
							repaint = true;
						} else if ((mouseEvent.isShiftDown() || (mouseEvent.isControlDown()) && !selection.contains(cpHot))) {
							
							/* add the cp to the selection */
							Map map = new HashMap();
							map.put(cpHot, new Float(1));
							edit.addEdit(new AtomicModifySelection.AddObjects(selection, map));
							edit.addEdit(new AtomicModifySelection.HotObject(selection, cpHot));
							edit.setName("add point to selection");
							MainFrame.getInstance().getUndoManager().addEdit(edit);
							selectionChanged(selection);
							/* set state */
							iState = IDLE;
							repaint = true;
						}
					}
				} else {
					
					if (btHot != null) {
						/* bone end was hit */
						
						/* if neither shift nor control is down */
						if ((!mouseEvent.isControlDown() && !mouseEvent.isShiftDown()) || selection == null) {
							
							/* is the point inside the selection box? */
							if (selection != null && !selection.isSingle() && isHit(x,y,viewDef.getScreenMatrix())) {
								
								/* change the hot cp */
								if (selection.contains(btHot)) {
									edit.addEdit(new AtomicModifySelection.HotObject(selection, btHot));
									repaint = true;
								}
								/* set state */
								iState = MOVE_GROUP;
								beginTransform();
								iMouseX = x;
								iMouseY = y;
								/* add motionlistener */
								((Component)mouseEvent.getSource()).addMouseMotionListener(this);
								bMoveZ = mouseEvent.isAltDown();
//								p3Pivot.set(selection.getPivot());
							} else {
								/*
								 * selection box was not hit
								 * create a new selection containing only cphot
								 */
								edit.addEdit(new AtomicChangeSelection(new Selection(btHot)));
								
								/* set state */
								iState = MOVE_SINGLE_BONENED;
								//compoundEdit.addEdit(new NewMoveControlPointsEdit(new ControlPoint[] { cpHot } ));
								beginTransform();
								repaint = true;
								iMouseX = x;
								iMouseY = y;
								/* add motionlistener */
								((Component)mouseEvent.getSource()).addMouseMotionListener(this);
								bMoveZ = mouseEvent.isAltDown();
							}
						} else {
							
							/* control or shift was down */
							if (mouseEvent.isControlDown() && selection.contains(btHot)) {
								
								/* remove the cp from the selection */
								Map map = new HashMap();
								map.put(btHot, new Float(1));
								edit.addEdit(new AtomicModifySelection.RemoveObjects(selection, map));
								edit.addEdit(new AtomicModifySelection.HotObject(selection, null));
								edit.setName("remove bone-end from selection");
								MainFrame.getInstance().getUndoManager().addEdit(edit);
								
								/* set state */
								
								iState = IDLE;
								
								repaint = true;
							} else if ((mouseEvent.isShiftDown() || (mouseEvent.isControlDown()) && !selection.contains(btHot))) {
								
								/* add the cp to the selection */
								Map map = new HashMap();
								map.put(btHot, new Float(1));
								edit.addEdit(new AtomicModifySelection.AddObjects(selection, map));
								edit.addEdit(new AtomicModifySelection.HotObject(selection, btHot));
								edit.setName("add bone-end to selection");
								MainFrame.getInstance().getUndoManager().addEdit(edit);
								
								/* set state */
								iState = IDLE;
								repaint = true;
							}
						}
					} else if (hitBone != null) {
						/* bone was hit */
						
						/* if neither shift nor control is down */
						if ((!mouseEvent.isControlDown() && !mouseEvent.isShiftDown()) || selection == null) {
							
							
							/* is the point inside the selection box? */
							if (selection != null && isHit(x,y,viewDef.getScreenMatrix())) {
								/* change the hot cp */
								//if (selection.contains(btHot)) {
									edit.addEdit(new AtomicModifySelection.HotObject(selection, null));
									repaint = true;
								//}
								/* set state */
								iState = MOVE_GROUP;
								beginTransform();
								iMouseX = x;
								iMouseY = y;
								/* add motionlistener */
								((Component)mouseEvent.getSource()).addMouseMotionListener(this);
								bMoveZ = mouseEvent.isAltDown();
//								p3Pivot.set(selection.getPivot());
							} else {
								/*
								 * selection box was not hit
								 * create a new selection containing only cphot
								 */
								DefaultTreeModel treeModel = (DefaultTreeModel) MainFrame.getInstance().getTree().getModel();
								MainFrame.getInstance().getTree().setSelectionPath(new TreePath(treeModel.getPathToRoot(hitBone)));
								MutableTreeNode lastNode = hitBone.getDofs().size() > 0 ? (MutableTreeNode) hitBone.getDof(hitBone.getDofs().size() - 1) : hitBone;
//								MainFrame.getInstance().getTree().expandPath(new TreePath(treeModel.getPathToRoot(hitBone)));
								MainFrame.getInstance().getTree().makeVisible(new TreePath(treeModel.getPathToRoot(lastNode)));
								MainFrame.getInstance().getTree().scrollPathToVisible(new TreePath(treeModel.getPathToRoot(hitBone)));
								MainFrame.getInstance().getTree().scrollPathToVisible(new TreePath(treeModel.getPathToRoot(lastNode)));
								Map map = new HashMap();
								map.put(hitBone.getBoneEnd(), new Float(1));
								map.put(hitBone.getParentBone() == null ? hitBone.getBoneStart() : hitBone.getParentBone().getBoneEnd(), new Float(1));
								edit.addEdit(new AtomicChangeSelection(new Selection(map)));
								
								/* set state */
								iState = MOVE_GROUP;
								//compoundEdit.addEdit(new NewMoveControlPointsEdit(new ControlPoint[] { cpHot } ));
								beginTransform();
								repaint = true;
								iMouseX = x;
								iMouseY = y;
								/* add motionlistener */
								((Component)mouseEvent.getSource()).addMouseMotionListener(this);
								bMoveZ = mouseEvent.isAltDown();
							}
						} else {
							
							/* control or shift was down */
//							if (mouseEvent.isControlDown()) {
//								
//								/* remove the cp from the selection */
//								Map map = new HashMap();
//								map.put(hitBone.getBoneEnd(), new Float(1));
//								map.put(hitBone.getParentBone() == null ? hitBone.getBoneStart() : hitBone.getParentBone().getBoneEnd(), new Float(1));
//								edit.addEdit(new AtomicModifySelection.RemoveObjects(selection, map));
//								edit.addEdit(new AtomicModifySelection.HotObject(selection, null));
//								edit.setName("remove bone from selection");
//								MainFrame.getInstance().getUndoManager().addEdit(edit);
//								
//								/* set state */
//								
//								iState = IDLE;
//								
//								repaint = true;
//							} else 
							if (mouseEvent.isShiftDown()) {
								
								/* add the cp to the selection */
								Map map = new HashMap();
								map.put(hitBone.getBoneEnd(), new Float(1));
								map.put(hitBone.getParentBone() == null ? hitBone.getBoneStart() : hitBone.getParentBone().getBoneEnd(), new Float(1));
								edit.addEdit(new AtomicModifySelection.AddObjects(selection, map));
								edit.addEdit(new AtomicModifySelection.HotObject(selection, null));
								edit.setName("add bone to selection");
								MainFrame.getInstance().getUndoManager().addEdit(edit);
								
								/* set state */
								iState = IDLE;
								repaint = true;
							}
						}
					} else { 
					
						/* no point was hit, clear cpHot */
						
						if (selection != null && cpHot != selection.getHotObject()) {
							edit.addEdit(new AtomicModifySelection.HotObject(selection, null));
							repaint = true;
						}
						//System.out.println("* " + ps);
						/* is shift or control down? set state */
						if (mouseEvent.isShiftDown() && selection != null) {
							iState = ADD_MODIFY_SELECTION;
							iMouseX = x;
							iMouseY = y;
						} else if (mouseEvent.isControlDown() && selection != null) {
							iState = XOR_MODIFY_SELECTION;
							iMouseX = x;
							iMouseY = y;
						} else {
							//System.out.println("**");
							/* neither shift nor control are down */
							
							/* check if selection box was hit and set state*/
							if (selection != null && !selection.isSingle() && isHit(x,y,viewDef.getScreenMatrix())) {
								
								/* selection box was hit, set state*/
								iState = MOVE_GROUP;
	//							compoundEdit.addEdit(new NewMoveControlPointsEdit(ps.getControlPointArray()));
	//							
	//							/* add motionlistener */
	//							((Component)mouseEvent.getSource()).addMouseMotionListener(this);
	//							iMouseX = x;
	//							iMouseY = y;
	//							bMoveZ = mouseEvent.isAltDown();
	//							compoundEdit.addEdit(new NewMoveControlPointsEdit(new ControlPoint[] { cpHot } ));
								beginTransform();
	//							repaint = true;
								iMouseX = x;
								iMouseY = y;
								/* add motionlistener */
								((Component)mouseEvent.getSource()).addMouseMotionListener(this);
								bMoveZ = mouseEvent.isAltDown();
//								p3Pivot.set(selection.getPivot());
							} else {
								
								/* selection box was not it, set state */
								iState = DRAW_SELECTION;
								iMouseX = x;
								iMouseY = y;
								
								
							}
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
			if (iState == MOVE_SINGLE_BONENED || (btHot != null && iState == MOVE_GROUP)) {
				boolean weld = false;
				if (btHot.isStart()) {
					Bone.BoneTransformable bt = viewDef.getClosestBoneEnd(new Point2D.Float(mouseEvent.getX(),mouseEvent.getY()), btHot, false, true);
					if (bt != null) {
						edit.addEdit(new AtomicAttachBone(btHot.getBone(), bt.getBone()));
						weld = true;
					}
				} else if (btHot.isEnd()) {
					Bone.BoneTransformable bt = viewDef.getClosestBoneEnd(new Point2D.Float(mouseEvent.getX(),mouseEvent.getY()), btHot, true, false);
					if (bt != null) {
						btHot.getBone().setEnd(bt.getBone().getStart(null));
						edit.addEdit(new AtomicAttachBone(bt.getBone(), btHot.getBone()));
						weld = true;
					}
				}
				if (weld) {
					Selection selection = MainFrame.getInstance().getSelection();
					if (iState == MOVE_SINGLE_BONENED)
						edit.addEdit(new AtomicChangeSelection(null));
					else
						MainFrame.getInstance().getSelection().setHotObject(null);
					((Component)mouseEvent.getSource()).removeMouseMotionListener(this);
					edit.addEdit(selection.endTransform());
					MainFrame.getInstance().getUndoManager().addEdit(edit);
					MainFrame.getInstance().getJPatchScreen().update_all();
					iState = IDLE;
				}
			} else if (iState == MOVE_SINGLE_POINT || (cpHot != null && iState == MOVE_GROUP)) {
//				Viewport viewport = (Viewport)mouseEvent.getSource();
				float[] hookPos = new float[1];
				boolean singlePoint = (iState == MOVE_SINGLE_POINT);
				ControlPoint cp = viewDef.getClosestControlPoint(new Point2D.Float(mouseEvent.getX(),mouseEvent.getY()),cpHot,hookPos,false,true);
//				NewSelection selection = MainFrame.getInstance().getSelection();
				if (cp != null && cp != cpHot.getPrev() && cp != cpHot.getNext()) {
					if (hookPos[0] == -1) {
						//System.out.println("*");
						if (cp.isSingle() || cpHot.isSingle()) {
							// - compoundEdit.addEdit(new MoveControlPointsEdit(MoveControlPointsEdit.TRANSLATE,ps.getControlPointArray()));
							if (!mouseEvent.isControlDown())
								edit.addEdit(new CompoundWeldControlPoints(cpHot,cp));
							else
//								compoundEdit.addEdit(CorrectSelectionsEdit.attachPoints(cpHot.getHead(),cp.getTail()));
								edit.addEdit(new CompoundAttachControlPoints(cpHot.getHead(),cp.getTail()));
//							Float weight = (Float) selection.getMap().get(cpHot);
//							Map map = new HashMap();
//							map.put(cpHot, weight);
//							compoundEdit.addEdit(new RemoveControlPointsFromSelectionEdit(selection ,map));
//							map = new HashMap();
//							map.put(cp, weight);
//							compoundEdit.addEdit(new AddControlPointsToSelectionEdit(selection, map));
//							compoundEdit.addEdit(new ChangeSelectionHotEdit(selection, cp));
							//((PointSelection)MainFrame.getInstance().getSelection()).removeControlPoint(cpHot);
							if (singlePoint)
								edit.addEdit(new AtomicChangeSelection(null));
							else
								edit.addEdit(new AtomicModifySelection.HotObject(MainFrame.getInstance().getSelection(), null));
							MainFrame.getInstance().getJPatchScreen().full_update();
							((Component)mouseEvent.getSource()).removeMouseMotionListener(this);
							MainFrame.getInstance().getUndoManager().addEdit(edit);
							MainFrame.getInstance().getJPatchScreen().update_all();
							cpHot = null;
							iState = IDLE;
						}
					} else if (iState == MOVE_SINGLE_POINT && cpHot.isSingle()) {
						if (cpHot.getPrev() == null || cpHot.getNext() == null) {
							if ((cpHot.getPrev() != null && (cpHot.getPrev().getNextAttached() == null || !cpHot.getPrev().getNextAttached().isHook())) || (cpHot.getNext().getNextAttached() == null || !cpHot.getNext().getNextAttached().isHook())) {
								if (cpHot.getChildHook() == null && (cpHot.getPrev() == null || cpHot.getPrev().getChildHook() == null)) {
									if (!cp.getNext().getHead().isHook() && !cp.getHead().isHook()) {
										if (cp.getHookAt(hookPos[0]) == null) {
											// - compoundEdit.addEdit(new MoveControlPointsEdit(MoveControlPointsEdit.TRANSLATE,ps.getControlPointArray()));
											edit.addEdit(new CompoundHook(cpHot,cp,hookPos[0]));
											//Collection collection = new ArrayList();
											//collection.add(cpHot);
											//compoundEdit.addEdit(new RemoveControlPointsFromSelectionEdit(ps,collection));
											//collection.clear();
											//collection.add(cpHot.getHead());
											//compoundEdit.addEdit(new AddControlPointsToSelectionEdit(ps,collection));
											//((PointSelection)MainFrame.getInstance().getSelection()).removeControlPoint(cpHot);
											if (singlePoint)
												edit.addEdit(new AtomicChangeSelection(null));
											else
												edit.addEdit(new AtomicModifySelection.HotObject(MainFrame.getInstance().getSelection(), null));
											MainFrame.getInstance().getJPatchScreen().full_update();
											((Component)mouseEvent.getSource()).removeMouseMotionListener(this);
											MainFrame.getInstance().getUndoManager().addEdit(edit);
											MainFrame.getInstance().getJPatchScreen().update_all();
										} else {
//											ControlPoint hook = cp.getHookAt(hookPos[0]);
//											edit.addEdit(new ConvertHookToCpEdit(hook));
//											edit.addEdit(new CompoundWeldControlPoints(cpHot,hook));
//											Float weight = (Float) selection.getMap().get(cpHot);
//											Map map = new HashMap();
//											map.put(cpHot, weight);
//											compoundEdit.addEdit(new RemoveControlPointsFromSelectionEdit(selection ,map));
//											map = new HashMap();
//											map.put(cp, weight);
//											compoundEdit.addEdit(new AddControlPointsToSelectionEdit(selection, map));
//											compoundEdit.addEdit(new ChangeSelectionHotEdit(selection, hook));
//											//((PointSelection)MainFrame.getInstance().getSelection()).removeControlPoint(cpHot);
//											MainFrame.getInstance().getJPatchScreen().full_update();
//											((Component)mouseEvent.getSource()).removeMouseMotionListener(this);
//											MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
//											MainFrame.getInstance().getJPatchScreen().update_all();
//											iState = IDLE;
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
			Selection selection = MainFrame.getInstance().getSelection();
			
			/* check if Control or Shift was down (on mouse release) and modify state if necessary */
			if ((iState == DRAW_SELECTION || iState == ADD_MODIFY_SELECTION || iState == XOR_MODIFY_SELECTION) && selection != null) {
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
					edit.setName("move point");
					endTransform();
//					edit.addEdit(new AtomicChangeSelection(null));
					break;
				case MOVE_SINGLE_BONENED:
					edit.setName("move bone");
					endTransform();
//					edit.addEdit(new AtomicChangeSelection(null));
					break;
				case MOVE_GROUP:
					// - compoundEdit.addEdit(new MoveControlPointsEdit(MoveControlPointsEdit.TRANSLATE,ps.getControlPointArray()));
//					compoundEdit.addEdit(new ChangeSelectionPivotEdit(ps,p3Pivot,null));
//					MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
//					edit.addEdit(new AtomicModifySelection.Pivot(selection, p3Pivot));
					edit.setName("move objects");
					endTransform();
//					if (selection.getHotObject() != null)
//						selection.setHotObject(null);
					break;
				case SCALE_GROUP:
					// edit = new MoveControlPointsEdit(MoveControlPointsEdit.SCALE,ps.getControlPointArray());
					edit.setName("scale");
					endTransform();
//					MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
					((Component)mouseEvent.getSource()).removeMouseMotionListener(activeHandle);
					break;
//FIXME					
				case DRAW_SELECTION:
					Selection sel = selectMouseMotionListener.getSelection(viewDef);
					if ((selection != null ^ sel != null) || sel != null && !sel.equals(selection)) {
						MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeSelection(sel));
						selectionChanged(sel);
					}
					((Component)mouseEvent.getSource()).removeMouseMotionListener(selectMouseMotionListener);
					break;
				case ADD_MODIFY_SELECTION: {
					Selection newSelection = selectMouseMotionListener.getSelection(viewDef);
					if (newSelection != null) {
						edit.addEdit(new AtomicModifySelection.AddObjects(selection, newSelection.getMap()));
						edit.setName("add objects to selection");
						edit.addEdit(new AtomicModifySelection.Pivot(selection, selection.getCenter(), true));
						MainFrame.getInstance().getUndoManager().addEdit(edit);
						selectionChanged(selection);
					}
//					PointSelection psNew = (PointSelection)selectMouseMotionListener.getSelection(viewDef);
//					if (psNew != null) {
//						Collection colPointsToAdd = psNew.getSelectedControlPoints();
//						if (ps != null) colPointsToAdd.removeAll(ps.getSelectedControlPoints());
//						compoundEdit.addEdit(new AddControlPointsToSelectionEdit(ps,colPointsToAdd));
//						//compoundEdit.addEdit(new ChangeSelectionPivotEdit(ps,ps.getCenter(),null));
//						MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
//					}
					((Component)mouseEvent.getSource()).removeMouseMotionListener(selectMouseMotionListener);
//					selectionChanged(ps);
				} break;
				case XOR_MODIFY_SELECTION: {
//					psNew = (PointSelection)selectMouseMotionListener.getSelection(viewDef);
//					if (psNew != null) {
//						Collection colNewSelection = psNew.getSelectedControlPoints();
//						Collection colPointsToAdd = new ArrayList();
//						Collection colPointsToRemove = new ArrayList();
//						//Collection colSelection = ps.getSelectedControlPoints();
//						for (Iterator it = colNewSelection.iterator(); it.hasNext(); ) {
//							ControlPoint cp = (ControlPoint)it.next();
//							// commented out - the following lines would xor the selection
//							// chaged to just remove points from selection
//							//if (colSelection.contains(cp)) {
//							//	colPointsToRemove.add(cp);
//							//} else {
//							//	colPointsToAdd.add(cp);
//							//}
//							colPointsToRemove.add(cp);
//						}
//						compoundEdit.addEdit(new AddControlPointsToSelectionEdit(ps,colPointsToAdd));
//						compoundEdit.addEdit(new RemoveControlPointsFromSelectionEdit(ps,colPointsToRemove));
//						//compoundEdit.addEdit(new ChangeSelectionPivotEdit(ps,ps.getCenter(),null));
//						MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
//					}
					Selection newSelection = selectMouseMotionListener.getSelection(viewDef);
					if (newSelection != null) {
						edit.addEdit(new AtomicModifySelection.RemoveObjects(selection, newSelection.getMap()));
						if (newSelection.contains(selection.getHotObject()))
							edit.addEdit(new AtomicModifySelection.HotObject(selection, null));
						edit.addEdit(new AtomicModifySelection.Pivot(selection, selection.getCenter(), true));
						edit.setName("remove objects from selection");
						MainFrame.getInstance().getUndoManager().addEdit(edit);
						selectionChanged(selection);
					}
					((Component)mouseEvent.getSource()).removeMouseMotionListener(selectMouseMotionListener);
//					selectionChanged(ps);
				} break;
//				case PIVOT:
//					//if (!selection.getPivot().equals(pivotHandle.getPosition(viewDef))) {
//						MainFrame.getInstance().getUndoManager().addEdit(new AtomicModifySelection.Pivot(selection, p3Pivot));
//					//}
//					((Component)mouseEvent.getSource()).removeMouseMotionListener(activeHandle);
//					break;
				case TANGENT:
					if (tangentHandle.getCp().getInMagnitude() != fMagnitude) {
						MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeControlPoint.Magnitude(tangentHandle.getCp(),fMagnitude));
					}
					((Component)mouseEvent.getSource()).removeMouseMotionListener(tangentHandle);
					break;
			}
			((Component)mouseEvent.getSource()).removeMouseMotionListener(this);
			for (int i = 0; i < aHandle.length; aHandle[i++].setActive(false));
			if (iState != IDLE) {
				MainFrame.getInstance().getJPatchScreen().update_all();
				iState = IDLE;
			}
			if (MainFrame.getInstance().getSelection() == null) {
				MainFrame.getInstance().setHelpText("Drag to move or select points. Use ATL to move perpendicular to screen plane.");
			} else if (MainFrame.getInstance().getSelection().isSingle()) {
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
//		Selection selection = MainFrame.getInstance().getSelection();
		int iDeltaX = mouseEvent.getX() - iMouseX;
		int iDeltaY = mouseEvent.getY() - iMouseY;
		Matrix4f m4InvScreenMatrix = new Matrix4f(viewDef.getScreenMatrix());
		m4InvScreenMatrix.invert();
		switch(iState) {
			case MOVE_GROUP: {
				Vector3f v3Move = bMoveZ ? new Vector3f(0,0,iDeltaY - iDeltaX) : new Vector3f(iDeltaX, iDeltaY, 0);
				m4InvScreenMatrix.transform(v3Move);
//				m4Transform.set(v3Move);
//				transformTemporarily();
//				selection.getPivot().set(p3Pivot);
//				selection.getPivot().add(v3Move);
				translate(v3Move, viewDef);
				
//				if (cpHot == null) {
//				//iMouseX = mouseEvent.getX();
//				//iMouseY = mouseEvent.getY();
//				Vector3f v3Move;
//				if (bMoveZ) {
//					v3Move = new Vector3f(0,0,iDeltaY - iDeltaX);
//				} else {
//					v3Move = new Vector3f(iDeltaX,iDeltaY,0);
//				}
//				
//				//Vector3f v3Ortho = new Vector3f(0,0,1);
//				m4InvScreenMatrix.transform(v3Move);
//				//m4InvScreenMatrix.transform(v3Ortho);
//				//if (Math.abs(v3Ortho.y) > 0.01f);
//				//	float f = v3Move.y / v3Ortho.y;
//				//	v3Move.addScaled(v3Ortho, -f);
//					translate(v3Move, viewDef);
//				//}
			}
			break;
		//} else {
			case MOVE_SINGLE_BONENED: // intentionally falls through to MOVE_SINGLE_POINT!
			case MOVE_SINGLE_POINT: {
			
				Vector3f v3Move = bMoveZ ? new Vector3f(0,0,iDeltaY - iDeltaX) : new Vector3f(iDeltaX, iDeltaY, 0);
				m4InvScreenMatrix.transform(v3Move);
				translate(v3Move, viewDef);
			} break;
		}
	}
	
	protected void beginTransform() {
//		PointSelection ps = MainFrame.getInstance().getPointSelection();
//		ArrayList list = ps.getTransformables();
//		for (int i = 0, n = list.size(); i < n; i++) {
//			((Transformable) list.get(i)).beginTransform();
//		}
		Selection selection = MainFrame.getInstance().getSelection();
		int mask;
		if (MainFrame.getInstance().getEditedMorph() != null)
			mask = Selection.MORPHTARGET;
		else
			mask = Selection.CONTROLPOINTS | Selection.MORPHS | Selection.BONES;
//		System.out.println(MainFrame.getInstance().getEditedMorph() + " " + mask);
		selection.arm(mask);
		selection.beginTransform();
		if (selection.getHotObject() != null)
			p3Hot = new Point3f(((Transformable) selection.getHotObject()).getPosition());
		else
			p3Hot = null;
	}
	
	protected void endTransform() {
////		m4ConstrainedTransform.set(m4Transform);
////		MainFrame.getInstance().getConstraints().constrainMatrix(m4ConstrainedTransform);
//		PointSelection ps = MainFrame.getInstance().getPointSelection();
//		ArrayList list = ps.getTransformables();
////		System.out.println(list);
//		for (int i = 0, n = list.size(); i < n; i++) {
//			compoundEdit.addEdit(((Transformable) list.get(i)).endTransform();
//		}
		Selection selection = MainFrame.getInstance().getSelection();
		edit.addEdit(selection.endTransform());
		MainFrame.getInstance().getUndoManager().addEdit(edit);
	}
	
//	protected void transformTemporarily() {
//		m4ConstrainedTransform.set(m4Transform);
//		MainFrame.getInstance().getConstraints().constrainMatrix(m4ConstrainedTransform);
//		PointSelection ps = MainFrame.getInstance().getPointSelection();
//		ArrayList list = ps.getTransformables();
//		for (int i = 0, n = list.size(); i < n; i++) {
//			((Transformable) list.get(i)).transformTemporarily(m4ConstrainedTransform);
//		}
//	}
	protected void translate(Vector3f v, ViewDefinition viewDef) {
		Selection selection = MainFrame.getInstance().getSelection();
		Vector3f vv = new Vector3f(v);
		if (selection.getHotObject() != null) {
			float scale = ((Float) selection.getMap().get(selection.getHotObject())).floatValue();
			if (scale != 0)
				vv.scale(1f / scale);
			else
				return;
		}
		if (MainFrame.getInstance().getJPatchScreen().snapToGrid()) {
			if (p3Hot != null)
				vv.add(p3Hot);
			MainFrame.getInstance().getJPatchScreen().getGrid().correctVector(vv, viewDef.getGridPlane());
			if (p3Hot != null)
				vv.sub(p3Hot);
			//vv.sub(cor);
		}
		if (!vv.equals(v3Translate)) {
//			MainFrame.getInstance().getConstraints().constrainVector(vv);
			v3Translate.set(vv);
			selection.translate(vv);
			if (bMoveZ)
				MainFrame.getInstance().getJPatchScreen().update_all();
			else
				MainFrame.getInstance().getJPatchScreen().single_update(viewDef.getDrawable().getComponent());
		}
	}
	
	protected void scale(float scale, ViewDefinition viewDef) {
		Selection selection = MainFrame.getInstance().getSelection();
		Matrix3f matrix = new Matrix3f(
				scale, 0, 0,
				0, scale, 0,
				0, 0, scale
		);
//		MainFrame.getInstance().getConstraints().constrainMatrix(matrix);
		selection.transform(matrix, selection.getPivot());
		if (bMoveZ) {
			MainFrame.getInstance().getJPatchScreen().update_all();
		} else {
			MainFrame.getInstance().getJPatchScreen().single_update(viewDef.getDrawable().getComponent());
		}
	}
	
//	protected void translate(Vector3f v, ViewDefinition viewDef, Grid grid) {
//		NewSelection selection = MainFrame.getInstance().getSelection();
//		Point3f point = new Point3f();
//		//ControlPoint[] acp = ps.getControlPointArray();
//		//if (bMoveZ) {
//		//	Vector3f cv = viewport.getGrid().getZCorrectionVector(v);
//		//} else {
//		//	Vector3f cv = viewport.getGrid().getCorrectionVector(v);
//		//}
//		
//		
//		Vector3f vector = new Vector3f(v);
////		if (bMoveZ) {
////			grid.correctZVector(vector);
////		} else {
////			grid.correctVector(vector);
////		}
//		if (cpHot != null) {
//			//System.out.println("*  " + vector);
//			if (bMoveZ) {
//				vector.add(grid.getZCorrectionVector(cpHot.getPosition()));
//			} else {
//				vector.add(grid.getCorrectionVector(cpHot.getPosition()));
//			}
//			//cv.set(-1,0,0);
//			//System.out.println("** " + vector);
//		}
//		
////		if (!vector.equals(v3Move)) {
////			v3Move.set(vector);
////			for (int p = 0; p < acp.length; p++) {
////				point.add(ap3[p],vector);
////				MainFrame.getInstance().getConstraints().setControlPointPosition(acp[p],point);
////			}
////			Point3f newPivot = new Point3f(p3Pivot);
////			newPivot.add(vector);
////			MainFrame.getInstance().getConstraints().setPointPosition(ps.getPivot(),newPivot);
////			
//			selection.translate(v3Move);
//			if (bMoveZ) {
//				MainFrame.getInstance().getJPatchScreen().update_all();
//			} else {
//				MainFrame.getInstance().getJPatchScreen().single_update(viewDef.getDrawable().getComponent());
//			}
//		//}
//	}
	
//	protected void scale(float scale, ViewDefinition viewDef) {
//		PointSelection ps = MainFrame.getInstance().getPointSelection();
//		Point3f point = new Point3f();
//		ControlPoint[] acp = ps.getControlPointArray();
//		for (int p = 0; p < acp.length; p++) {
//			point.sub(ap3[p],ps.getPivot());
//			point.scale(scale);
//			point.add(ps.getPivot());
//			MainFrame.getInstance().getConstraints().setControlPointPosition(acp[p],point);
//		}
//		MainFrame.getInstance().getJPatchScreen().single_update(viewDef.getDrawable().getComponent());
//	}
	
//	void scale(float s, ViewDefinition viewDef) {
////		System.out.println("scale " + s);
//		Point3f pivot = MainFrame.getInstance().getPointSelection().getPivot();
//		m4Transform = new Matrix4f(
//				 s, 0, 0, pivot.x - pivot.x * s,
//				 0, s, 0, pivot.y - pivot.y * s,
//				 0, 0, s, pivot.z - pivot.z * s,
//				 0, 0, 0, 1
//		);
//		m4ConstrainedTransform.set(m4Transform);
//		MainFrame.getInstance().getConstraints().constrainMatrix(m4ConstrainedTransform);
//		PointSelection ps = MainFrame.getInstance().getPointSelection();
//		ArrayList list = ps.getTransformables();
//		for (int i = 0, n = list.size(); i < n; i++) {
//			((Transformable) list.get(i)).transformTemporarily(m4ConstrainedTransform);
//		}
//		MainFrame.getInstance().getJPatchScreen().single_update(viewDef.getDrawable().getComponent());
//	}
	
	private void selectionChanged(Selection selection) {
//		System.out.println("Selection changed: " + selection.getObjects());
		MutableTreeNode leaf = null;
		if (MainFrame.getInstance().getTree().getSelectionPath() != null)
			leaf = (MutableTreeNode) MainFrame.getInstance().getTree().getSelectionPath().getLastPathComponent();
		if (leaf == null || leaf == MainFrame.getInstance().getModel().getTreenodeSelections() || leaf == MainFrame.getInstance().getModel() || leaf instanceof Selection) {
			//MainFrame.getInstance().getSideBar().enableTreeSelectionListener(false);
			if (selection != null && !MainFrame.getInstance().getModel().checkSelection(selection)) {
				MainFrame.getInstance().selectTreeNode(MainFrame.getInstance().getModel().getSelection(selection));
			//	System.out.println("*");
			//	MainFrame.getInstance().getSideBar().enableTreeSelectionListener(false);
			//	System.out.println(selection + " " + MainFrame.getInstance().getModel().getSelection(selection));
			//	DefaultTreeModel treeModel = (DefaultTreeModel) MainFrame.getInstance().getTree().getModel();
			//	MainFrame.getInstance().getTree().setSelectionPath(new TreePath(treeModel.getPathToRoot(selection)));
			//	MainFrame.getInstance().getSideBar().enableTreeSelectionListener(true);
			} else {
				MainFrame.getInstance().selectTreeNode(MainFrame.getInstance().getModel().getTreenodeSelections());
			//	DefaultTreeModel treeModel = (DefaultTreeModel) MainFrame.getInstance().getTree().getModel();
			//	MainFrame.getInstance().getTree().setSelectionPath(new TreePath(treeModel.getPathToRoot(MainFrame.getInstance().getModel().getTreenodeSelections())));
			}
			//MainFrame.getInstance().getSideBar().enableTreeSelectionListener(true);
		}
//		MainFrame.getInstance().setSelection(selection);
//		if (leaf == null || leaf == MainFrame.getInstance().getModel().getTreenodeBones() || leaf == MainFrame.getInstance().getModel() || leaf instanceof Bone || leaf instanceof RotationDof) {
//			System.out.println("1");
//			if (selection != null) {
//				System.out.println("2");
//				if (selection.getMap().size() == 2) {
//					System.out.println("3");
//					ArrayList list = new ArrayList(selection.getMap().keySet());
//					BoneTransformable[] bt = new BoneTransformable[2];
//					for (int i = 0; i < 2; i++) {
//						if (list.get(i) instanceof BoneTransformable)
//							bt[i] = (BoneTransformable) list.get(i);
//						else
//							return;
//					}
//					System.out.println("4");
//					Bone bone = null;
//					if (bt[0].getBone() == bt[1].getBone())
//						bone = bt[0].getBone();
//					else if (bt[0].getBone() == bt[1].getBone().getParentBone())
//						bone = bt[0].getBone();
//					else if (bt[1].getBone() == bt[0].getBone().getParentBone())
//						bone = bt[1].getBone();
//					System.out.println("bone=" + bone);
//					if (bone != null) {
//						DefaultTreeModel treeModel = (DefaultTreeModel) MainFrame.getInstance().getTree().getModel();
//						MainFrame.getInstance().getTree().setSelectionPath(new TreePath(treeModel.getPathToRoot(bone)));
//					}
//				}
//			}
//		}
	}
}
