package jpatch.boundary.tools;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.vecmath.*;

import jpatch.boundary.*;

import jpatch.control.edit.*;
import jpatch.entity.*;
import jpatch.entity.Bone.BoneTransformable;

public class RotateTool extends JPatchTool {
//	private static final int GHOST_FACTOR = JPatchUserSettings.getInstance().iGhost;
	
	private static final int SUBDIV = 64;
	private static final float S = 1f / (float) Math.sqrt(2);
	private static final float[] COS = new float[SUBDIV];
	private static final float[] SIN = new float[SUBDIV];
	
	private static final int IDLE = 0;
	private static final int ROTATE = 1;
	private static final int PIVOT = 2;
	private static final int ROTATE_FREE = 3;
	private static final int ROTATE_FREE_X = 4;
	private static final int ROTATE_FREE_Y = 5;
	
	//private boolean bAutoReset = false;
	private float fAlpha;
	private float fBeta = 0;
	private float fRadius = 0;
	private Point3f p3Pivot;
	private Matrix3f m3InitialRotation;// = new Matrix3f();
	//private Matrix3f m3Rotation = new Matrix3f();
	private AxisAngle4f aaRotation = new AxisAngle4f();
	private Vector3f v3AxisX = new Vector3f(1,0,0);
	private Vector3f v3AxisY = new Vector3f(0,1,0);
	private Vector3f v3AxisZ = new Vector3f(0,0,1);
	private Handle[] aHandle;
	private HandleZOrderComparator comparator = new HandleZOrderComparator();
	private Handle activeHandle;
	private Handle pivotHandle;
//	private Point3f[] ap3;
//	private ControlPoint[] acp;
	private Point3f p3OldPivot = new Point3f();
	
	private int iState = IDLE;
	
	private int iMouseX;
	private int iMouseY;
	
	private float fRotX = 0;
	private float fRotY = 0;
	private boolean bChange;
	
	private Selection selection;
	
	private JPatchActionEdit edit;
	
	static {
		for (int s = 0; s < SUBDIV; s++) {
			COS[s] = (float)Math.cos((float)s / SUBDIV * 2 * Math.PI);
			SIN[s] = (float)Math.sin((float)s / SUBDIV * 2 * Math.PI);
		}
	}
	
	public RotateTool() {
		//m3Rot.setIdentity();
		//m3RotA.setIdentity();
		
		//fRadius = 2;
//		NewSelection selection = MainFrame.getInstance().getSelection();
//		p3Pivot = selection.getPivot();
//		m3Rot = selection.getOrientation();
//		m3RotA.set(m3Rot);
//		
//		setRadius();
		reInit(MainFrame.getInstance().getSelection());
		
		Matrix3f m3X = new Matrix3f();
		Matrix3f m3Y = new Matrix3f();
		Matrix3f m3Z = new Matrix3f();
		m3X.rotX((float)Math.PI/4);
		m3Y.rotY((float)Math.PI/4);
		m3Z.rotZ((float)Math.PI/4);
		Color3f cSelect = settings.colors.selection;
		Color3f colorX = settings.colors.xAxis;
		Color3f colorY = settings.colors.yAxis;
		Color3f colorZ = settings.colors.zAxis;
		pivotHandle = new PivotHandle(this, cSelect);
		if (MainFrame.getInstance().getSelection().getHotObject() instanceof AnimObject) {
			aHandle = new Handle[] {
					new RotateHandle(new Point3f( 0, S, S), this, colorX, v3AxisX, m3X),
					new RotateHandle(new Point3f( 0,-S, S), this, colorX, v3AxisX, m3X),
					new RotateHandle(new Point3f( 0,-S,-S), this, colorX, v3AxisX, m3X),
					new RotateHandle(new Point3f( 0, S,-S), this, colorX, v3AxisX, m3X),
					new RotateHandle(new Point3f( S, 0, S), this, colorY, v3AxisY, m3Y),
					new RotateHandle(new Point3f(-S, 0, S), this, colorY, v3AxisY, m3Y),
					new RotateHandle(new Point3f(-S, 0,-S), this, colorY, v3AxisY, m3Y),
					new RotateHandle(new Point3f( S, 0,-S), this, colorY, v3AxisY, m3Y),
					new RotateHandle(new Point3f( S, S, 0), this, colorZ, v3AxisZ, m3Z),
					new RotateHandle(new Point3f(-S, S, 0), this, colorZ, v3AxisZ, m3Z),
					new RotateHandle(new Point3f(-S,-S, 0), this, colorZ, v3AxisZ, m3Z),
					new RotateHandle(new Point3f( S,-S, 0), this, colorZ, v3AxisZ, m3Z),
					new RotateHandleS(new Point3f( 0, 1, 0), this, cSelect),
					new RotateHandleS(new Point3f( 1, 0, 0), this, cSelect),
					new RotateHandleS(new Point3f( 0,-1, 0), this, cSelect),
					new RotateHandleS(new Point3f(-1, 0, 0), this, cSelect),
			};
		} else {
			aHandle = new Handle[] {
					new RotateHandle(new Point3f( 0, S, S), this, colorX, v3AxisX, m3X),
					new RotateHandle(new Point3f( 0,-S, S), this, colorX, v3AxisX, m3X),
					new RotateHandle(new Point3f( 0,-S,-S), this, colorX, v3AxisX, m3X),
					new RotateHandle(new Point3f( 0, S,-S), this, colorX, v3AxisX, m3X),
					new RotateHandle(new Point3f( S, 0, S), this, colorY, v3AxisY, m3Y),
					new RotateHandle(new Point3f(-S, 0, S), this, colorY, v3AxisY, m3Y),
					new RotateHandle(new Point3f(-S, 0,-S), this, colorY, v3AxisY, m3Y),
					new RotateHandle(new Point3f( S, 0,-S), this, colorY, v3AxisY, m3Y),
					new RotateHandle(new Point3f( S, S, 0), this, colorZ, v3AxisZ, m3Z),
					new RotateHandle(new Point3f(-S, S, 0), this, colorZ, v3AxisZ, m3Z),
					new RotateHandle(new Point3f(-S,-S, 0), this, colorZ, v3AxisZ, m3Z),
					new RotateHandle(new Point3f( S,-S, 0), this, colorZ, v3AxisZ, m3Z),
					new RotateHandleS(new Point3f( 0, 1, 0), this, cSelect),
					new RotateHandleS(new Point3f( 1, 0, 0), this, cSelect),
					new RotateHandleS(new Point3f( 0,-1, 0), this, cSelect),
					new RotateHandleS(new Point3f(-1, 0, 0), this, cSelect),
					pivotHandle
			};
		}
		MainFrame.getInstance().setHelpText("Click and drag handles to rotate or move pivot. Click and drag inside sphere to rotate freely. Doubleclick to reset coordinate system or pivot.");
	}
	
	public int getButton() {
		return MeshToolBar.ROTATE;
	}
	
	public void setRadius() {
		if (selection.getHotObject() instanceof AnimObject) {
			fRadius = ((AnimObject) selection.getHotObject()).getRadius();
			return;
		}
//		PointSelection ps = MainFrame.getInstance().getPointSelection();
		float r = 0;
		float ds = 0;
//		ap3 = ps.getPointArray();
//		acp = selection.getControlPointArray();
//		ControlPoint[] acp = selection.getControlPointArray();
		for (Iterator it = selection.getObjects().iterator(); it.hasNext(); ) {
			ds = p3Pivot.distanceSquared(((Transformable) it.next()).getPosition());
			if (ds > r) r = ds;
		}
//		for (int i = 0; i < ap3.length; i++) {
//			ds = p3Pivot.distanceSquared(ap3[i]);
//			if (ds > r) r = ds;
//		}
		fRadius = (float)Math.sqrt(r);
	}

	public float getRadius(float scale) {
		return (1.1f * fRadius * scale < 24f) ? 24f / scale : 1.1f * fRadius;
	}
	
	public float getAlpha() {
		return fAlpha;
	}
	
	public float getBeta() {
		return fBeta;
	}
	
	public void setAlpha(float alpha) {
		fAlpha = alpha;
	}
	
	public Matrix3f getInitialRotation() {
		return m3InitialRotation;
	}
	
	public AxisAngle4f getRotation() {
		return aaRotation;
	}
	
	public Point3f getPivot() {
		return p3Pivot;
	}
	
	public void paint(ViewDefinition viewDef) {
		JPatchDrawable2 drawable = viewDef.getDrawable();
		//drawable.clearZBuffer();
		//System.out.println("fAlpha = " + fAlpha);
		Point3f p3a = new Point3f();
		Point3f p3b = new Point3f();
		Color3f c3a = new Color3f();
		Color3f c3b = settings.colors.background;
		Color3f c3 = new Color3f();
		Color3f c3la = new Color3f();
		Color3f c3lb = new Color3f();
		Point3f p3la = new Point3f();
		Point3f p3lb = new Point3f();
		//int s2;
		Matrix4f m4View = viewDef.getMatrix();
		
		
		float scale = m4View.getScale();
		//setRadius();
		//if (fRadius * scale < 24f) {
		//	fRadius = 24f / scale;
		//}
		
		float fRadius = getRadius(scale);
		
		Matrix3f m = new Matrix3f();
		m.set(aaRotation);
		m.mul(m3InitialRotation);
		Point3f p3TransformedPivot = new Point3f(p3Pivot);
		m4View.transform(p3TransformedPivot);
		float fInvRadius = 1 / (fRadius * viewDef.getScale() * viewDef.getWidth());// * 0.5f;
		//System.out.println("fradius = " + fTransformedRadius);
		p3a.set(0,fRadius * COS[SUBDIV - 1],fRadius * SIN[SUBDIV - 1]);
		m.transform(p3a);
		p3a.add(p3Pivot);
		m4View.transform(p3a);
		c3a.set(settings.colors.xAxis);
		float f;
		for (int s = 0; s < SUBDIV; s++) {
			p3b.set(0, fRadius * COS[s], fRadius * SIN[s]);
			m.transform(p3b);
			p3b.add(p3Pivot);
			m4View.transform(p3b);
			f = (p3a.z - p3TransformedPivot.z) * fInvRadius + 0.3f;
			c3.interpolate(c3a,c3b,f);
			c3.clamp(0.1f,1);
			drawable.setColor(c3);
			//if (p3a.z < p3TransformedPivot.z) {
			//	drawable.setColor(settings.cX);
			//} else {
			//	drawable.setColor(settings.cGrey);
			//}
			drawable.drawLine(p3a, p3b);
			//if (g2 != null) g2.drawLine((int)p3a.x, (int)p3a.y, (int)p3b.x, (int)p3b.y);
			//else zBufferRenderer.draw3DLine(p3a, p3b, settings.iX);
			p3a.set(p3b);
		}
		p3a.set(-fRadius, 0, 0);
		m.transform(p3a);
		p3a.add(p3Pivot);
		p3b.set(fRadius, 0, 0);
		m.transform(p3b);
		p3b.add(p3Pivot);
		m4View.transform(p3a);
		m4View.transform(p3b);
		
		c3a.set(settings.colors.xAxis);
		f = (p3a.z - p3TransformedPivot.z) * fInvRadius + 0.3f;
		c3la.interpolate(c3a,c3b,f);
		c3la.clamp(0.1f,1);
		f = (p3b.z - p3TransformedPivot.z) * fInvRadius + 0.3f;
		c3lb.interpolate(c3a,c3b,f);
		c3lb.clamp(0.1f,1);
		p3la = new Point3f(p3a);
		p3lb = new Point3f();
		for (int i = 1; i < 11; i++) {
			float s = ((float) i) / 10;
			c3.interpolate(c3la,c3lb,s);
			drawable.setColor(c3);
			p3lb.interpolate(p3a,p3b,s);
			drawable.drawLine(p3la,p3lb);
			p3la.set(p3lb);
		}
			
		//drawable.drawLine3D(p3a, p3b);
		
		//drawable.setColor(settings.cY);
		p3a.set(fRadius * COS[SUBDIV - 1], 0, fRadius * SIN[SUBDIV - 1]);
		m.transform(p3a);
		p3a.add(p3Pivot);
		m4View.transform(p3a);
		c3a.set(settings.colors.yAxis);
		for (int s = 0; s < SUBDIV; s++) {
			p3b.set(fRadius * COS[s], 0, fRadius * SIN[s]);
			m.transform(p3b);
			p3b.add(p3Pivot);
			m4View.transform(p3b);
			//if (p3a.z < p3TransformedPivot.z) {
			//	drawable.setColor(settings.cY);
			//} else {
			//	drawable.setColor(settings.cGrey);
			//}
			f = (p3a.z - p3TransformedPivot.z) * fInvRadius + 0.3f;
			c3.interpolate(c3a,c3b,f);
			c3.clamp(0.1f,1);
			drawable.setColor(c3);
			drawable.drawLine(p3a, p3b);
			//else zBufferRenderer.draw3DLine(p3a, p3b, settings.iY);
			p3a.set(p3b);
		}
		p3a.set(0, -fRadius, 0);
		m.transform(p3a);
		p3a.add(p3Pivot);
		p3b.set(0, fRadius, 0);
		m.transform(p3b);
		p3b.add(p3Pivot);
		m4View.transform(p3a);
		m4View.transform(p3b);
		
		//drawable.setColor(settings.cY);
		//drawable.drawLine3D(p3a, p3b);
		
		c3a.set(settings.colors.yAxis);
		f = (p3a.z - p3TransformedPivot.z) * fInvRadius + 0.3f;
		c3la.interpolate(c3a,c3b,f);
		c3la.clamp(0.1f,1);
		f = (p3b.z - p3TransformedPivot.z) * fInvRadius + 0.3f;
		c3lb.interpolate(c3a,c3b,f);
		c3lb.clamp(0.1f,1);
		p3la = new Point3f(p3a);
		p3lb = new Point3f();
		for (int i = 1; i < 11; i++) {
			float s = ((float) i) / 10;
			c3.interpolate(c3la,c3lb,s);
			drawable.setColor(c3);
			p3lb.interpolate(p3a,p3b,s);
			drawable.drawLine(p3la,p3lb);
			p3la.set(p3lb);
		}
		
		//drawable.setColor(settings.cZ);
		p3a.set(fRadius * COS[SUBDIV - 1],fRadius * SIN[SUBDIV - 1], 0);
		m.transform(p3a);
		p3a.add(p3Pivot);
		m4View.transform(p3a);
		c3a.set(settings.colors.zAxis);
		for (int s = 0; s < SUBDIV; s++) {
			p3b.set(fRadius * COS[s], fRadius * SIN[s], 0);
			m.transform(p3b);
			p3b.add(p3Pivot);
			m4View.transform(p3b);
			//if (p3a.z < p3TransformedPivot.z) {
			//	drawable.setColor(settings.cZ);
			//} else {
			//	drawable.setColor(settings.cGrey);
			//}
			f = (p3a.z - p3TransformedPivot.z) * fInvRadius + 0.3f;
			c3.interpolate(c3a,c3b,f);
			c3.clamp(0.1f,1);
			drawable.setColor(c3);
			drawable.drawLine(p3a, p3b);
			//else zBufferRenderer.draw3DLine(p3a, p3b, settings.iZ);
			p3a.set(p3b);
		}
		p3a.set(0, 0, -fRadius);
		m.transform(p3a);
		p3a.add(p3Pivot);
		p3b.set(0, 0, fRadius);
		m.transform(p3b);
		p3b.add(p3Pivot);
		m4View.transform(p3a);
		m4View.transform(p3b);
		
		//drawable.setColor(settings.cZ);
		//drawable.drawLine3D(p3a, p3b);
		
		c3a.set(settings.colors.zAxis);
		f = (p3a.z - p3TransformedPivot.z) * fInvRadius + 0.3f;
		c3la.interpolate(c3a,c3b,f);
		c3la.clamp(0.1f,1);
		f = (p3b.z - p3TransformedPivot.z) * fInvRadius + 0.3f;
		c3lb.interpolate(c3a,c3b,f);
		c3lb.clamp(0.1f,1);
		p3la = new Point3f(p3a);
		p3lb = new Point3f();
		for (int i = 1; i < 11; i++) {
			float s = ((float) i) / 10;
			c3.interpolate(c3la,c3lb,s);
			drawable.setColor(c3);
			p3lb.interpolate(p3a,p3b,s);
			drawable.drawLine(p3la,p3lb);
			p3la.set(p3lb);
		}
		
		drawable.setColor(settings.colors.selection);
		p3a.set(fRadius * COS[SUBDIV - 1] * 1.0f, fRadius * SIN[SUBDIV - 1] * 1.0f, 0);
		//m3RotA.transform(p3a);
		Matrix3f m3 = new Matrix3f();
		m4View.get(m3);
		m3.invert();
		m3.transform(p3a);
		p3a.add(p3Pivot);
		m4View.transform(p3a);
		for (int s = 0; s < SUBDIV; s++) {
			p3b.set(fRadius * COS[s] * 1.0f, fRadius * SIN[s] * 1.0f, 0);
			//m3RotA.transform(p3b);
			m3.transform(p3b);
			p3b.add(p3Pivot);
			m4View.transform(p3b);
			drawable.drawLine(p3a, p3b);
			//else zBufferRenderer.draw3DLine(p3a, p3b, settings.iSelection);
			p3a.set(p3b);
		}
		
		
		comparator.setViewDefinition(viewDef);
		Arrays.sort(aHandle,comparator);
		for (int h = aHandle.length - 1; h >= 0; h--) {
			if (aHandle[h] instanceof RotateHandleS)
				aHandle[h].paint(viewDef);
			else
				aHandle[h].paint(viewDef, m);
		}
	}
	
	public void mousePressed(MouseEvent mouseEvent) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
		Matrix4f m4View = viewDef.getScreenMatrix();
		
		if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
			//System.out.println("mousePressed() state = " + iState);
			fBeta = fAlpha;
			int x = mouseEvent.getX();
			int y = mouseEvent.getY();
//			PointSelection ps = MainFrame.getInstance().getPointSelection();
//			ap3 = ps.getPointArray();
//			acp = ps.getControlPointArray();
			boolean repaint = false;
//			Viewport viewport = (Viewport)mouseEvent.getSource();
			paint(viewDef);
			float z = Float.MAX_VALUE;
			Point3f p3Hit = new Point3f();
			if (activeHandle != null) {
				((Component) mouseEvent.getSource()).removeMouseMotionListener(activeHandle);
				activeHandle = null;
				repaint = true;
			}
			for (int h = 0; h < aHandle.length; h++) {
				if (aHandle[h].isHit(viewDef, x, y, p3Hit) && (activeHandle == null || p3Hit.z < z)) {
					z = p3Hit.z;
					activeHandle = aHandle[h];
				}
			}
			if (activeHandle != null && mouseEvent.getClickCount() == 2 && !bChange) {
				if (activeHandle == pivotHandle) {
//					p3OldPivot.set(p3Pivot);
//					selection.setPivot(selection.getCenter());
//					p3Pivot.set(selection.getPivot());
					MainFrame.getInstance().getUndoManager().addEdit(new AtomicModifySelection.Pivot(selection, selection.getCenter(), true));
					p3Pivot.set(selection.getPivot());
					setRadius();
				} else {
					//Matrix3f m3Dummy = new Matrix3f(m3RotA);
					//float fDummy = fBeta;
					//reset();
					aaRotation.angle = 0;
					//m3Rotation.setIdentity();
					Matrix3f mi = new Matrix3f();
					mi.setIdentity();
					MainFrame.getInstance().getUndoManager().addEdit(new AtomicModifySelection.Orientation(selection,mi));
				}
			}
			
			setPassive();
			iState = IDLE;
			bChange = false;
			if (activeHandle != null) {
				activeHandle.setActive(true);
				activeHandle.setMouse(x, y);
				((Component)mouseEvent.getSource()).addMouseMotionListener(activeHandle);
				((Component)mouseEvent.getSource()).addMouseMotionListener(this);
				repaint = true;
				if (activeHandle == pivotHandle) {
					iState = PIVOT;
					p3OldPivot.set(p3Pivot);
				} else {
					iState = ROTATE;
					beginTransform();
					edit = new JPatchActionEdit("rotate");
					edit.addEdit(new AtomicMoveControlPoints(selection.getControlPointArray()));
				}
			} else {
				Point3f p3 = new Point3f(p3Pivot);
				m4View.transform(p3);
				float r = m4View.getScale() * getRadius(m4View.getScale());
				//System.out.println(p3 + " " + x + " " + y + " " + r);
				float rq = r * r;
				float xx = x - p3.x;
				float yy = y - p3.y;
				if (( xx * xx + yy * yy) < rq) {
					//System.out.println("inside");
					iMouseX = mouseEvent.getX();
					iMouseY = mouseEvent.getY();
					((Component)mouseEvent.getSource()).addMouseMotionListener(this);
					iState = ROTATE_FREE;
					beginTransform();
					edit = new JPatchActionEdit("rotate");
					edit.addEdit(new AtomicMoveControlPoints(selection.getControlPointArray()));
				}
			}	
			if (repaint) {
				MainFrame.getInstance().getJPatchScreen().single_update((Component)mouseEvent.getSource());
			}
			MainFrame.getInstance().setHelpText("Hold SHIFT to disable 5° steps.");
		}
	}
	
	public void mouseReleased(MouseEvent mouseEvent) {
		if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
			//System.out.println("mouseReleased() state = " + iState + " change = " + bChange);
			if (bChange && iState != IDLE) {
			//System.out.println("mouseReleased()" + mouseEvent.getClickCount());
			//if (mouseEvent.getClickCount() != 2) {
				//if (!mouseEvent.isAltDown() || mouseEvent.isControlDown()) {
					//m3Rot.set(m3RotA);
//					PointSelection ps = MainFrame.getInstance().getPointSelection();
					//System.out.println("fAlpha = " + fAlpha);
//					if (ps != null) {
						//JPatchCompoundEdit compoundEdit = null;
						if (iState == ROTATE || iState == ROTATE_FREE || iState == ROTATE_FREE_X || iState == ROTATE_FREE_Y) {
							//compoundEdit = new JPatchCompoundEdit("rotate");
							//compoundEdit.addEdit(new MoveControlPointsEdit(MoveControlPointsEdit.ROTATE,ps.getControlPointArray()));
							endTransform();
//							edit.addEdit(new AtomicModifySelection.Orientation(selection,m3RotA));
						} else if(iState == PIVOT) {
							edit = new JPatchActionEdit("move pivot");
							edit.addEdit(new AtomicModifySelection.Pivot(selection,p3OldPivot));
						} else {
							System.err.println("error in RotateTool");
						}
						MainFrame.getInstance().getUndoManager().addEdit(edit);
						MainFrame.getInstance().getJPatchScreen().full_update();
						if (MainFrame.getInstance().getTimelineEditor() != null)
							MainFrame.getInstance().getTimelineEditor().repaint();
					}
					//System.out.println("fAlpha = " + fAlpha);
				//} else {
				//	m3RotA.set(m3Rot);
				//	MainFrame.getInstance().getJPatchScreen().update_all();
				//}
				//fAlpha = 0;
				
				
				//fBeta = getAlpha();
				//fAlpha = 0;
				//m3RotA.setIdentity();
				//if (bAutoReset) {
				//	reset();
				//}
				iState = IDLE;
				bChange = false;
//			}
			((Component) mouseEvent.getSource()).removeMouseMotionListener(activeHandle);
			((Component) mouseEvent.getSource()).removeMouseMotionListener(this);
			MainFrame.getInstance().setHelpText("Click and drag handles to rotate or move pivot. Click and drag inside sphere to rotate freely. Doubleclick to reset coordinate system or pivot.");
		}
	}
	
	public void mouseDragged(MouseEvent mouseEvent) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
		Matrix4f m4 = viewDef.getScreenMatrix();
		
		bChange = true;
		int dx = mouseEvent.getX() - iMouseX;
		int dy = mouseEvent.getY() - iMouseY;
		if (iState == ROTATE_FREE || (dx * dx + dy * dy) < 64) {
			iState = (Math.abs(dy) > Math.abs(dx)) ? ROTATE_FREE_X : ROTATE_FREE_Y;
		}
		if (iState == ROTATE_FREE_X) {
			Matrix3f m3 = new Matrix3f();
			//AxisAngle4f axisAngle = new AxisAngle4f();
			Vector3f v3AxisX = new Vector3f(1,0,0);
			float scale = m4.getScale();
			m4.get(m3);
			m3.invert();
			m3.transform(v3AxisX);
			v3AxisX.normalize();
			float ax = -dy / m4.getScale() / getRadius(scale) * 180 / (float)Math.PI;
			if (!mouseEvent.isShiftDown())
				ax = Math.round(ax / 5f) * 5f;
			if (ax != fRotX) {
				fRotX = ax;
				aaRotation.set(v3AxisX, ax / 360f * 2f * (float)Math.PI);
				if (!mouseEvent.isControlDown()) rotate();
				MainFrame.getInstance().getJPatchScreen().single_update((Component)mouseEvent.getSource());
			}
		} else if (iState == ROTATE_FREE_Y) {
			Matrix3f m3 = new Matrix3f();
			//AxisAngle4f axisAngle = new AxisAngle4f();
			Vector3f v3AxisY = new Vector3f(0,1,0);
			float scale = m4.getScale();
			m4.get(m3);
			m3.invert();
			m3.transform(v3AxisY);
			v3AxisY.normalize();
			float ay = dx / m4.getScale() / getRadius(scale) * 180 / (float)Math.PI;
			if (!mouseEvent.isShiftDown())
				ay = Math.round(ay / 5f) * 5f;
			if (ay != fRotY) {
				fRotY = ay;
				aaRotation.set(v3AxisY, ay / 360f * 2f * (float)Math.PI);
				if (!mouseEvent.isControlDown()) rotate();
				MainFrame.getInstance().getJPatchScreen().single_update((Component)mouseEvent.getSource());
			}
		}
	}
			

	
	public void reset() {
		m3InitialRotation.setIdentity();
		aaRotation.angle = 0;
		setPassive();
		//MainFrame.getInstance().getJPatchScreen().update_all();
	}
	
	public void reInit(Selection selection) {
		this.selection = selection;
		m3InitialRotation = selection.getOrientation();
		p3Pivot = selection.getPivot();
		aaRotation.angle = 0;
//		acp = selection.getControlPointArray();
//		ap3 = new Point3f[acp.length];
//		for (int i = 0; i < acp.length; i++)
//			ap3[i] = new Point3f(acp[i].getPosition());
		setRadius();
		//MainFrame.getInstance().getJPatchScreen().update_all();
		//System.out.println("PointSelection.reInit(" + ps + ")");
		//fBeta = 0;
	}
	
	private void setPassive() {
		for (int h = 0; h < aHandle.length; h++) {
			aHandle[h].setActive(false);
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
		selection.arm(mask);
		selection.beginTransform();
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
		Matrix3f rotation = new Matrix3f();
		rotation.set(aaRotation);
		rotation.mul(m3InitialRotation);
		edit.addEdit(new AtomicModifySelection.Orientation(selection, rotation));
		m3InitialRotation.set(rotation);
		aaRotation.angle = 0;
//		MainFrame.getInstance().getUndoManager().addEdit(edit);
	}
	
	public void rotate() {
//		Point3f point = new Point3f();
//		Vector3f cv = new Vector3f(p3Pivot);
//		Quat4f q = new Quat4f();
//		q.set(m3Rotation);
		selection.rotate(aaRotation, p3Pivot);
		
//		for (int p = 0; p < acp.length; p++) {
//			ControlPoint cp = acp[p];
//			point.set(ap3[p]);
//			Matrix4f transform = new Matrix4f();
//			transform.setIdentity();
//			Matrix4f dummy = new Matrix4f();
//			transform.set(cv);
//			
//			dummy.setIdentity();
//			dummy.set(m3RotA);
//			transform.mul(dummy);
//			
//			dummy.setIdentity();
//			dummy.set(m3Rot);
//			dummy.invert();
//			transform.mul(dummy);
//			
//			cv.scale(-1);
//			dummy.set(cv);
//			cv.scale(-1);
//			transform.mul(dummy);
//			transform.transform(point);
//			MainFrame.getInstance().getConstraints().setControlPointPosition(cp,point);
//		}
	}
}
