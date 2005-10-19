package jpatch.boundary;

import javax.vecmath.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import jpatch.entity.*;

public final class ViewDefinition
implements ComponentListener {
	public static final int FRONT = 1;
	public static final int REAR = 2;
	public static final int TOP = 3;
	public static final int BOTTOM = 4;
	public static final int LEFT = 5;
	public static final int RIGHT = 6;
	public static final int BIRDS_EYE = 7;
	
	public static final String[] aViewName = { "","front view","rear view","top view","bottom view","left view","right view","bird's eye view" };
	public static final int[] aiGridPlane = { 0, Grid.XY, Grid.XY, Grid.XZ, Grid.XZ, Grid.YZ, Grid.YZ, Grid.NONE };

	private JPatchDrawable2 drawable;
	private int iView;
	private float fRotateX = 0f;
	private float fRotateY = 0f;
	private float fTranslateX = 0f;
	private float fTranslateY = 0f;
	private float fScale = 0.03f;
	private float fZ = 0;
	private boolean bRenderPoints = true;
	private boolean bRenderCurves = true;
	private boolean bRenderPatches = false;
	private boolean bRenderBezierCPs = false;
	private boolean bShowRotoscope = true;
	private boolean bAlwaysUseZBuffer = false;
	private ControlPoint cpTangentHandles = null;
	//private Matrix4f m4Screen = new Matrix4f();
	private Vector4f v4Screen = new Vector4f();
//	private Vector4f v4ScreenOffset = new Vector4f();
	private Matrix4f m4View = new Matrix4f();
	private RealtimeLighting lighting;
	
//	private float fWidth;
//	private float fHeight;
//	private boolean bVisible;
	
	private Point3f p3Lock;
	
	public ViewDefinition(int view) {
		//setView(view);
		iView = view;
		//setGridPlane();
	}
	
//	private final void setGridPlane() {
//		//if (iView != BIRDS_EYE) {
//			//iGridPlane = aiGridPlane[iView];
//			viewport.getGrid().setPlane(aiGridPlane[iView]);
//		//}
//	}
	/*
	public final int getGridPlane() {
		return iGridPlane;
	}
	*/
	public final void componentHidden(ComponentEvent componentEvent) {
		;
	}
	
	public final void componentMoved(ComponentEvent componentEvent) {
		;
	}
	
	public final void componentResized(ComponentEvent componentEvent) {
		//fHeight = componentEvent.getComponent().getHeight();
		//fWidth = componentEvent.getComponent().getWidth();
		computeMatrix();
		//System.out.println("*");
	}
	
	public final void componentShown(ComponentEvent componentEvent) {
		;
	}
	
//	public void setViewport(Viewport viewport) {
//		this.viewport = viewport;
//		viewport.addComponentListener(this);
//		setView(iView);
//		setGridPlane();
//	}
	
	public void setDrawable(JPatchDrawable2 drawable) {
		this.drawable = drawable;
		drawable.getComponent().addComponentListener(this);
		setView(iView);
	}
	
	public JPatchDrawable2 getDrawable() {
		return drawable;
	}
	
	public void setLighting(RealtimeLighting lighting) {
		this.lighting = lighting;
	}
	
	public RealtimeLighting getLighting() {
		return lighting;
	}
	
	public final Matrix4f getMatrix() {
		return m4View;
	}
	
	public final Matrix4f getScreenMatrix() {
		
//		float fWidth = drawable.getComponent().getWidth();
//		float fHeight = drawable.getComponent().getHeight();
//		//fMin = (fWidth < fHeight) ? fWidth : fHeight;
//		float fMin = fWidth;
//		Matrix4f m4Screen = new Matrix4f(m4View);
////		Matrix4f m4 = new Matrix4f();
//		
//		m4Screen.mul(fMin/2);
//		m4Screen.setRow(3,0,0,0,1);
//		
//		m4Screen.getColumn(3,v4Screen);
//		v4ScreenOffset = new Vector4f(fWidth/2,fHeight/2,0,0);
//		v4Screen.add(v4ScreenOffset);
//		m4Screen.setColumn(3,v4Screen);
//		return m4Screen;
		
		Matrix4f m4Screen = new Matrix4f(m4View);
		m4Screen.m03 += drawable.getComponent().getWidth() / 2;
		m4Screen.m10 = -m4Screen.m10;
		m4Screen.m11 = -m4Screen.m11;
		m4Screen.m12 = -m4Screen.m12;
		m4Screen.m13 = -m4Screen.m13 + drawable.getComponent().getHeight() / 2;
		return m4Screen;
		//return m4View;
	}
		
	public final float getRotateX() {
		return fRotateX;
	}
	
	public final float getRotateY() {
		return fRotateY;
	}
	
	public final float getTranslateX() {
		return fTranslateX;
	}
	
	public final float getTranslateY() {
		return fTranslateY;
	}
	
	public final void setTranslation(float x, float y) {
		fTranslateX = x;
		fTranslateY = y;
	}
	
	public final float getScale() {
		return fScale;
	}
	
	//public Point3f getLock() {
	//	return p3Lock;
	//}
	
	public void setScale(float scale) {
		fScale = scale;
	}
	
	public void setLock(Point3f lock) {
		p3Lock = lock;
		if (p3Lock != null) {
			Point3f p3 = new Point3f(p3Lock);
			Matrix3f m = new Matrix3f();
			m4View.getRotationScale(m);
			m.setScale(1);
			m.transform(p3);
			fTranslateX = -p3.x;
			fTranslateY = -p3.y;
			computeMatrix();
		}
		drawable.display();
	}
	
	public float getWidth() {
		//return fWidth;
		return drawable.getComponent().getWidth();
	}
	
	public float getHeight() {
		//return fHeight;
		return drawable.getComponent().getHeight();
	}
	
	public final int getView() {
		return iView;
	}
	
	public final String getViewName() {
		return aViewName[iView];
	}
	
	public final boolean alwaysUseZBuffer() {
		return bAlwaysUseZBuffer;
	}
	
	public final void alwaysUseZBuffer(boolean enable) {
		bAlwaysUseZBuffer = enable;
	}
	
	public final boolean renderPoints() {
		return bRenderPoints;
	}
	
	public final boolean renderCurves() {
		return bRenderCurves;
	}
	
	public final boolean renderPatches() {
		return bRenderPatches;
	}
	
	public final boolean renderBezierCPs() {
		return bRenderBezierCPs;
	}
	
	public final boolean showRotoscope() {
		return bShowRotoscope;
	}
	
	public final boolean isLocked() {
		return p3Lock != null;
	}
	
	public final void renderPoints(boolean enable) {
		bRenderPoints = enable;
	}
	
	public final void renderCurves(boolean enable) {
		bRenderCurves = enable;
	}
	
	public final void renderPatches(boolean enable) {
		bRenderPatches = enable;
	}
	
	public final void renderBezierCPs(boolean enable) {
		bRenderBezierCPs = enable;
	}
	
	public final void showRotoscope(boolean enable) {
		bShowRotoscope = enable;
	}
	
	public final void setTangentHandles(ControlPoint cp) {
		cpTangentHandles = cp;
	}
	
	public final ControlPoint getTangentHandles() {
		return cpTangentHandles;
	}
	
	//public final void recomputeMatrix() {
	//	computeMatrix();
		
	public final void computeMatrix() {
		switch (iView) {
			case FRONT:
				m4View.set(new Matrix4f(
						1, 0, 0, 0,
						0, 1, 0, 0,
						0, 0, 1, 0,
						0, 0, 0, 1));
			break;
			case REAR:
				m4View.set(new Matrix4f(
						-1, 0, 0, 0,
						 0, 1, 0, 0,
						 0, 0,-1, 0,
						 0, 0, 0, 1));
			break;
			case RIGHT:
				m4View.set(new Matrix4f(
						0, 0, 1, 0,
						0, 1, 0, 0,
						-1, 0, 0, 0,
						0, 0, 0, 1));
			break;
			case LEFT:
				m4View.set(new Matrix4f(
						0, 0,-1, 0,
			 			0, 1, 0, 0,
						1, 0, 0, 0,
						0, 0, 0, 1));
			break;
			case BOTTOM:
				m4View.set(new Matrix4f(
						1, 0, 0, 0,
						0, 0,-1, 0,
						0, 1, 0, 0,
						0, 0, 0, 1));
			break;
			case TOP:
				m4View.set(new Matrix4f(
						1, 0, 0, 0,
						0, 0, 1, 0,
						0,-1, 0, 0,
						0, 0, 0, 1));
			break;
			case BIRDS_EYE:
				m4View.set(new Matrix4f(
						1, 0, 0, 0,
						0, 1, 0, 0,
						0, 0, 1, 0,
						0, 0, 0, 1));
				Matrix4f m4Transform = new Matrix4f();
				//m4Transform.setIdentity();
				//m4Transform.set(new Vector3f(-v3Pivot.x, -v3Pivot.y, -v3Pivot.z));
				//m4View.mul(m4Transform);
				m4Transform.rotX(fRotateX);
				m4View.mul(m4Transform);
				m4Transform.rotY(fRotateY);
				m4View.mul(m4Transform);
				//m4Transform.setIdentity();
				//m4Transform.set(v3Pivot);
				//m4View.mul(m4Transform);
				//System.out.println(m4View);
			break;
		}
		m4View.setScale(fScale);
		m4View.setTranslation(new Vector3f(fTranslateX*fScale,fTranslateY*fScale,0));
		//Matrix4f m = new Matrix4f();
		//m.setIdentity();
		//m.setTranslation(new Vector3f(fTranslateX*fScale,-fTranslateY*fScale,0));
		//m4View.mul(m);
		//m4View.setScale(fScale);
		float width = getWidth();
//		float height = getHeight();
		//if (viewport != null) {
			//float fWidth = viewport.getWidth();
			//float fHeight = viewport.getHeight();
			//fMin = (fWidth < fHeight) ? fWidth : fHeight;
//			float fMin = width;
			//m4Screen = new Matrix4f(m4View);
			//Matrix4f m4 = new Matrix4f();
			
			m4View.mul(width / 2);
			m4View.setRow(3,0,0,0,1);
			
			m4View.getColumn(3,v4Screen);
			//v4ScreenOffset = new Vector4f(width/2,height/2,0,0);
			//v4Screen.add(v4ScreenOffset);
			//m4View.setColumn(3,v4Screen);
			
		//}
		//return m4Screen;
		
	}

	public final void setView(int view) {
		iView = view;
		switch(iView) {
			case FRONT:
				fRotateX = 0;
				fRotateY = 0;
			break;
			case REAR:
				fRotateX = 0;
				fRotateY = (float)Math.PI;
			break;
			case LEFT:
				fRotateX = 0;
				fRotateY = -(float)Math.PI/2f;
			break;
			case RIGHT:
				fRotateX = 0;
				fRotateY = (float)Math.PI/2f;
			break;
			case TOP:
				fRotateX = -(float)Math.PI/2f;
				fRotateY = 0;
			break;
			case BOTTOM:
				fRotateX = (float)Math.PI/2f;
				fRotateY = 0;
			break;
			case BIRDS_EYE:
				fRotateX = -(float)Math.PI/4;
				fRotateY = (float)Math.PI/4;
			break;
		}
		p3Lock = null;
		computeMatrix();
//		setGridPlane();
	}
	
	public final void moveView(float x, float y) {
		moveView(x,y,true);
	}
		
	public final void moveView(float x, float y, boolean repaint) {
		fTranslateX += x/fScale;
		fTranslateY -= y/fScale;
		computeMatrix();
//		((JPatchCanvas)viewport).clearBackground();
		if (repaint) drawable.display();
	}
	
	public final void rotateView(float x, float y) {
		iView = BIRDS_EYE;
		fRotateX -= y;
		fRotateY -= x;
		if (fRotateX < -Math.PI/2) {
			fRotateX = -(float)Math.PI/2;
		}
		if (fRotateX > Math.PI/2) {
			fRotateX = (float)Math.PI/2;
		}
//		viewport.getGrid().setPlane(Grid.NONE);
		if (p3Lock != null) {
			computeMatrix();
			Point3f p3 = new Point3f(p3Lock);
			Matrix3f m = new Matrix3f();
			m4View.getRotationScale(m);
			m.setScale(1);
			m.transform(p3);
			fTranslateX = -p3.x;
			fTranslateY = -p3.y;
		}
		computeMatrix();
//		((JPatchCanvas)viewport).clearBackground();
		drawable.display();
	}
	
	public final void scaleView(float scale) {
		fScale *= scale;
		computeMatrix();
//		((JPatchCanvas)viewport).clearBackground();
		drawable.display();
	}
	
	public final void repaint() {
		drawable.display();
	}
	
//	public final void reset() {
//		((JPatchCanvas)viewport).updateImage();
//	}
	
	public final Point get2DPosition(Point3f point) {
		Point3f p3 = new Point3f(point);
		getScreenMatrix().transform(p3);
		return new Point((int)p3.x,(int)p3.y);
	}
	
	public final void setZ(Point3f point) {
		Point3f p3 = new Point3f(point);
		getScreenMatrix().transform(p3);
		fZ = p3.z;
	}
	
	public final void setZ(float z) {
		fZ = z;
	}
	
	public final Point3f get3DPosition(float x, float y) {
		Point3f p3 = new Point3f(x, y, fZ);
		Matrix4f m4Screen = getScreenMatrix();
		Matrix4f m4Inverse = new Matrix4f();
		m4Inverse.invert(m4Screen);
		m4Inverse.transform(p3);
		p3.y = p3.y;
		return p3;
	}
	
	public Bone getClosestBone(Point2D.Float target) {
		Matrix4f m4Screen = getScreenMatrix();
		Point3f p3 = new Point3f();
		Line2D.Float line = new Line2D.Float();
		float fMinDistance = 64;
		float fDistance;
		Bone closest = null;
		for (Iterator it = MainFrame.getInstance().getModel().getBoneSet().iterator(); it.hasNext(); ) {
			Bone bone = (Bone) it.next();
			bone.getStart(p3);
			m4Screen.transform(p3);
			line.x1 = p3.x;
			line.y1 = p3.y;
			bone.getEnd(p3);
			m4Screen.transform(p3);
			line.x2 = p3.x;
			line.y2 = p3.y;
			fDistance = (float) line.ptSegDistSq(target);
			if (fDistance <= fMinDistance) {
				fMinDistance = fDistance;
				closest = bone;
			}
		}
		return closest;
	}
	
	public Bone.BoneTransformable getClosestBoneEnd(Point2D.Float target, Bone.BoneTransformable except, boolean includeStarts, boolean includeEnds) {
		Matrix4f m4Screen = getScreenMatrix();
		Point3f p3 = new Point3f();
		Point2D.Float p2 = new Point2D.Float();
		float fMinDistance = 64;
		float fDistance;
		Model model = MainFrame.getInstance().getModel();
		Bone.BoneTransformable closest = null;
		for (Iterator it = model.getBoneSet().iterator(); it.hasNext(); ) {
			Bone bone = (Bone) it.next();
			Bone.BoneTransformable bt;
			if (includeEnds) {
				bt = bone.getBoneEnd();
				p3.set(bt.getPosition());
				m4Screen.transform(p3);
				p2.setLocation(p3.x,p3.y);
				fDistance = (float) p2.distanceSq(target);
				if (fDistance <= fMinDistance && bt != except) {
					fMinDistance = fDistance;
					closest = bt;
				}
			}
			if (includeStarts && bone.getParentBone() == null) {
				bt = bone.getBoneStart();
				p3.set(bt.getPosition());
				m4Screen.transform(p3);
				p2.setLocation(p3.x,p3.y);
				fDistance = (float)p2.distanceSq(target);
				if (fDistance <= fMinDistance && bt != except) {
					fMinDistance = fDistance;
					closest = bt;
				}
			}
		}
		return closest;
	}
	
	public final ControlPoint getClosestControlPoint(Point2D.Float target, ControlPoint except) {
		return getClosestControlPoint(target, except, null);
	}
	
	public final ControlPoint getClosestControlPoint(Point2D.Float target, ControlPoint except, float[] hookPos) {
		return getClosestControlPoint(target, except, hookPos, false);
	}
	
	public final ControlPoint getClosestControlPoint(Point2D.Float target, ControlPoint except, float[] hookPos, boolean selectHooks) {
		return getClosestControlPoint(target, except, hookPos, selectHooks, false);
	}
		
	public final ControlPoint getClosestControlPoint(Point2D.Float target, ControlPoint except, float[] hookPos, boolean selectHooks, boolean grid) {
		return getClosestControlPoint(target, except, hookPos, selectHooks, grid, null);
	}
		
	public final ControlPoint getClosestControlPoint(Point2D.Float t, ControlPoint except, float[] hookPos, boolean selectHooks, boolean grid, ControlPoint selected) {
		Point2D.Float target;
//		if (grid && viewport.getGrid().snap()) {
//			Point3f p3Target = new Point3f(t.x,t.y,0);
//			Matrix4f m4InvScreen = new Matrix4f(getScreenMatrix());
//			m4InvScreen.invert();
//			m4InvScreen.transform(p3Target);
//			viewport.getGrid().correctVector(p3Target);
//			getScreenMatrix().transform(p3Target);
//			target = new Point2D.Float(p3Target.x,p3Target.y);
//		} else {
			target = t;
//		}
		ControlPoint cp;
		float fHookPos = -1;
		ControlPoint cpExcept = (except != null) ? except.getHead() : null;
		Model model = MainFrame.getInstance().getModel();
		Matrix4f m4Screen = getScreenMatrix();
		Point3f p3 = new Point3f();
		Point2D.Float p2 = new Point2D.Float();
		float fMinDistance = 64;
		float fMinHookDistance = 256;
		float fMinHotDistance = 16;
		float fDistance;
		
		if (selected != null) {
			p3.set(selected.getPosition());
			m4Screen.transform(p3);
			p2.setLocation(p3.x,p3.y);
			fDistance = (float)p2.distanceSq(target);
			if (fDistance <= fMinHotDistance) {
				return selected;
			}
		}
		
		ControlPoint cpClosest = null;
		for (Iterator it = model.getCurveSet().iterator(); it.hasNext(); ) {
			cp = (ControlPoint) it.next();
			//if (cp.getHookPos() == -1) {
				if ((selectHooks && cp.getHookPos() != 0 && cp.getHookPos() != 1) || cp.getHookPos() == -1) {
				while (cp != null) {
					if (!cp.isHidden()) {
						if (cp.isHead()) {
							p3.set(cp.getPosition());
							m4Screen.transform(p3);
							p2.setLocation(p3.x,p3.y);
							fDistance = (float)p2.distanceSq(target);
							if (fDistance <= fMinDistance && cp != cpExcept) {
								fMinDistance = fDistance;
								cpClosest = cp;
								fHookPos = -1;
							}
						}
						/*
						 * check for hooks too...
						 */
						if (hookPos != null && fMinDistance == 64 && cp.getNext() != null) {
							for (int i = 0; i < 3; i++) {
								p3.set(cp.getHookPosition(i));
								m4Screen.transform(p3);
								p2.setLocation(p3.x,p3.y);
								fDistance = (float)p2.distanceSq(target);
								if (fDistance <= fMinHookDistance && cp != cpExcept) {
									fMinHookDistance = fDistance;
									cpClosest = cp;
									fHookPos = ControlPoint.HOOKPOS[i];
								}
							}
						}
					}
					cp = cp.getNextCheckNextLoop();
				}
			}
		}
		if (hookPos != null) {
			hookPos[0] = fHookPos;
		}
		return cpClosest;
	}
	
	
	/*
	public final void setZ(Point3f point) {
		Point3f c = new Point3f(point);
		m4View.transform(c);
		fZ = c.z;
	}
		
	public final Point3f get3DPosition (float x, float y)
	{
		Point3f n = new Point3f(x,y,fZ);
		Matrix4f m4InverseView = new Matrix4f(m4View);
		m4InverseView.invert();
		m4InverseView.transform(n);
		return n;
	}
	*/
}
