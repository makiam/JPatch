/*
 * $Id$
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
 
package jpatch.entity;

import java.util.*;
import javax.vecmath.*;

import jpatch.auxilary.*;
import jpatch.control.edit.*;
import jpatch.boundary.*;

/**
 *  A ControlPoint object stores all information relevant to the model's geometry and
 *  provides methods to get the position and tangents (which have to be computed in
 *  case of attached ControlPoints or hooks).
 *  An overview about this Class can be found
 *  <a href="http://jpatch.sourceforge.net/developer/new_model/controlPoint/">here</a>
 *
 * @author     Sascha Ledinsky
 * @version    $Revision$
 */

public class OLDControlPoint implements Comparable, Transformable {
	/** Tangent mode "peak" */
	public static final int PEAK = 0;
	/** Tangent mode compatible to sPatch's "round" setting */
	public static final int SPATCH_ROUND = 1;
	/** Tangent mode compatible to Animation:Masters "smooth" setting */
	public static final int AM_SMOOTH = 2;
	/** JPatch tangent mode - C0 curve continuity */
	public static final int JPATCH_C0 = 3;
	/** JPatch tangent mode - C1 curve continuity */
	public static final int JPATCH_C1 = 5;
	/** JPatch tangent mode - G1 curve continuity */
	public static final int JPATCH_G1 = 4;
	
	public static final float[] HOOKPOS = new float[] { 0.25f, 0.5f, 0.75f };
	
	private static final float[] HOOK_B0 = new float[] { 0.421875f, 0.125f, 0.015625f };
	private static final float[] HOOK_B1 = new float[] { 0.421875f, 0.375f, 0.140625f };
	private static final float[] HOOK_B2 = new float[] { 0.140625f, 0.375f, 0.421875f };
	private static final float[] HOOK_B3 = new float[] { 0.015625f, 0.125f, 0.421875f };
	
	/** default tangent mode */
	private static int iDefaultMode = JPATCH_G1;
	/** default curvature */
	private static float fDefaultMagnitude = 1f;
	private static int nextId = 0;
	
	private int id;
	
	/** position of this ConrolPoint */
	private Point3f p3ReferencePosition = new Point3f();
	/** reference position of this ControlPoint */
	//private Point3f p3ReferencePosition = new Point3f();
	/** loop flag */
	private boolean bLoop = false;
	/** tangents valid flag */
//	private boolean bTangentsValid = false;
	/** tangent mode */
	private int iMode = iDefaultMode;
	/** next ControlPoint on curve */
	private OLDControlPoint cpNext;
	/** previous ControlPoint on Curve */
	private OLDControlPoint cpPrev;
	/** next attached ControlPoint */
	private OLDControlPoint cpNextAttached;
	/** previous attached ControlPoint */
	private OLDControlPoint cpPrevAttached;
	/** child hook */
	private OLDControlPoint cpChildHook;
	/** parent hook */
	private OLDControlPoint cpParentHook;
	/** position of hook (may be 0.25, 0.5, 0.75 or -1) */
	private float fHookPos = -1f;
	/** in curvature */
	private float fInMagnitude = fDefaultMagnitude;
	/** out curvature */
	private float fOutMagnitude = fDefaultMagnitude;
	///** in alpha */
	//private float fInAlpha = 0;
	///** out alpha */
	//private float fOutAlpha = 0;
	///** in gamma */
	//private float fInGamma = 0;
	///** out gamma */
	//private float fOutGamma = 0;
	/** cached reference in tangent */
	private Point3f p3ReferenceInTangent = new Point3f();
	/** cached reference out tangent */
	private Point3f p3ReferenceOutTangent = new Point3f();
	/** cached in tangent */
	private Point3f p3InTangent = new Point3f();
	/** cached out tangent */
	private Point3f p3OutTangent = new Point3f();
	
//	private Point3f p3RefPosition = new Point3f();
//	private Point3f p3RefInTangent = new Point3f();
//	private Point3f p3RefOutTangent = new Point3f();
	
	private Point3f p3BackupPosition = new Point3f();
	private Vector3f v3ReferenceMorph = new Vector3f();
	private Vector3f v3Morph = new Vector3f();
	
	private boolean bHidden = false;
	private boolean bDeleted = false;
	
	private OLDBone bone;
	private float fBonePosition;
	private float fDistanceToLine;
	private boolean bParentBone;
	
	private Matrix4f m4Transform = new Matrix4f();
	private Matrix4f m4InvTransform = new Matrix4f();
	private Matrix4f m4BoneTransform = new Matrix4f();
	private Matrix4f m4InvBoneTransform = new Matrix4f();
	
	/**
	 * Constructor
	 */
	public OLDControlPoint() {
		if (nextId < 0)
			throw new IllegalStateException();
		id = nextId++;
		setBonePose();
		setMorphPose();
	}

	/**
	 * Constructor
	 * creates a controlpoint with the same position as cp
	 * @param cp	The controlpoint which position is copied
	 */
	public OLDControlPoint(OLDControlPoint cp) {
		this(cp.getHead().p3ReferencePosition);
		iMode = cp.iMode;
		fInMagnitude = cp.fInMagnitude;
		fOutMagnitude = cp.fOutMagnitude;
	}
	
	/**
	 * Constructor
	 * @param  position  The 3D position
	 */
	public OLDControlPoint(Point3f position) {
		this();
		p3ReferencePosition.set(position);
		//p3ReferencePosition.set(position);
	}

	/**
	 * Constructor
	 *
	 * @param  x  The x-coordinate
	 * @param  y  The y-coordinate
	 * @param  z  The z-coordinate
	 */
	public OLDControlPoint(float x, float y, float z) {
		this();
		p3ReferencePosition.set(x, y, z);
		//p3ReferencePosition.set(x, y, z);
	}

	/*
	* Cpmparable implementation
	*/
	public int compareTo(Object o) {
		return this.hashCode() - o.hashCode();
	}
	
	/*
	* Transformable implementation
	*/
	
	public void beginTransform() {
		p3BackupPosition.set(p3ReferencePosition);
	}
	
	private static final Vector3f vv = new Vector3f();
	public void translate(Vector3f v) {
		if (MainFrame.getInstance().getJPatchScreen().isLockPoints())
			return;
		p3ReferencePosition.set(p3BackupPosition);
		vv.set(v);
		MainFrame.getInstance().getConstraints().constrainVector(vv);
		m4InvTransform.transform(vv);
		p3ReferencePosition.add(vv);
	}
	
	public void rotate(AxisAngle4f a, Point3f pivot) {
		if (MainFrame.getInstance().getJPatchScreen().isLockPoints())
			return;
		Matrix3f m = new Matrix3f();
		m.set(a);
		transform(m, pivot);
	}
	
	private static final Matrix3f m1 = new Matrix3f();
	private static final Matrix3f m2 = new Matrix3f();
	private static final Matrix3f m3 = new Matrix3f();
	public void transform(Matrix3f m, Point3f pivot) {
		if (MainFrame.getInstance().getJPatchScreen().isLockPoints())
			return;
		p3ReferencePosition.set(p3BackupPosition);
		Point3f p = new Point3f(pivot);
		m4InvTransform.transform(p);
		p3ReferencePosition.sub(p);
		m4Transform.getRotationScale(m1);
		m4InvTransform.getRotationScale(m2);
		m3.set(m);
		MainFrame.getInstance().getConstraints().constrainMatrix(m3);
		m2.mul(m3);
		m2.mul(m1);
		m2.transform(p3ReferencePosition);
		p3ReferencePosition.add(p);
//		invalidateTangents();
	}
	
	public JPatchUndoableEdit endTransform() {
		if (MainFrame.getInstance().getJPatchScreen().isLockPoints())
			return null;
		return new AtomicChangeControlPoint.Position(this, p3BackupPosition);
	}
	
	/**
	 * Sets the defalut mode for all ControlPoints created afterwards
	 *
	 * @param  defaultMode  The new defalutMode value
	 */
	public static void setDefaultMode(int defaultMode) {
		iDefaultMode = defaultMode;
	}
	
	/**
	 * Sets the default curvature for all ControlPoints created afterwards
	 *
	 * @param  defaultMagnitude  The new defaultMagnitude value
	 */
	public static void setDefaultMagnitude(float defaultMagnitude) {
		fDefaultMagnitude = defaultMagnitude;
	}

	public static void setNextId(int nextId) {
		OLDControlPoint.nextId = nextId;
		System.out.println("ControlPoint.setNextId(" + nextId + ")");
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
//	public void setReference() {
//		p3RefPosition.set(getPosition());
//		if (cpPrev != null) p3RefInTangent.set(getInTangent());
//		if (cpNext != null) p3RefOutTangent.set(getOutTangent());
//	}
	
	public void setBone(OLDBone bone) {
		this.bone = bone;
	}
	
	public void setBone(OLDBone bone, float position, float distanceToLine, boolean parent) {
		this.bone = bone;
		this.fBonePosition = position;
		this.fDistanceToLine = distanceToLine;
		this.bParentBone = parent;
	}
	
	public OLDBone getBone() {
		return bone;
	}
	
	/**
	 * Creates a clone
	 */
	public OLDControlPoint createClone() {
		OLDControlPoint clone = new OLDControlPoint();
		clone.cpNext = cpNext;
		clone.cpPrev = cpPrev;
		clone.cpNextAttached = cpNextAttached;
		clone.cpPrevAttached = cpPrevAttached;
		clone.cpParentHook = cpParentHook;
		clone.cpChildHook = cpChildHook;
		return clone;
	}
	
	/**
	 * Clones a ControlPoint
	 */
	public void cloneFrom(OLDControlPoint cp) {
		cpNext = cp.cpNext;
		cpPrev = cp.cpPrev;
		cpNextAttached = cp.cpNextAttached;
		cpPrevAttached = cp.cpPrevAttached;
		cpParentHook = cp.cpParentHook;
		cpChildHook = cp.cpChildHook;
	}
	
//	/**
//	 * sets all references to null
//	 */
//	public void free() {
//		 cpNext = null;
//		 cpPrev = null;
//		 cpNextAttached = null;
//		 cpPrevAttached = null;
//		 cpParentHook = null;
//		 cpChildHook = null;
//	}

	public Point3f getReferenceHookPosition(int hook) {
		if (cpNext != null) {
			Point3f p0 = getReferencePosition();
			Point3f p1 = getReferenceOutTangent();
			Point3f p2 = cpNext.getReferenceInTangent();
			Point3f p3 = cpNext.getReferencePosition();
			float x = p0.x * HOOK_B0[hook] + p1.x * HOOK_B1[hook] + p2.x * HOOK_B2[hook] + p3.x * HOOK_B3[hook];
			float y = p0.y * HOOK_B0[hook] + p1.y * HOOK_B1[hook] + p2.y * HOOK_B2[hook] + p3.y * HOOK_B3[hook];
			float z = p0.z * HOOK_B0[hook] + p1.z * HOOK_B1[hook] + p2.z * HOOK_B2[hook] + p3.z * HOOK_B3[hook];
			return new Point3f(x,y,z);
		} else {
			return null;
		}
	}
	
	public Point3f getHookPosition(int hook) {
		Point3f p = getReferenceHookPosition(hook);
		if (p != null)
			m4Transform.transform(p);
		return p;
	}
	
	public float getBonePosition() {
		return fBonePosition;
	}
	
	public float getBoneDistance() {
		return fDistanceToLine;
	}
	
	public boolean isParentBone() {
		return bParentBone;
	}
	
	/**
	 * returns the length of a curve. must be called on curve start, throws exception otherwise
	 * @return the length of the curve
	 */
	public int getLength() {
		if (!bLoop && cpPrev != null)
			throw new IllegalArgumentException(getClass().getName()+ ".getLength() must be called on curve-start");
		int n = 0;
		for (OLDControlPoint cp = this; cp != null; cp = cp.getNextCheckNextLoop())
			n++;
		return n;
	}
	
	/**
	 * returns an array of all attached controlPoints
	 */
	public OLDControlPoint[] getStack() {
		/*
		 * count attached points
		 */
		int n = 0;
		for (OLDControlPoint cp = getHead(); cp != null; cp = cp.getPrevAttached()) {
			n++;
		}
		/*
		 * fill array
		 */
		OLDControlPoint[] acp = new OLDControlPoint[n];
		n = 0;
		for (OLDControlPoint cp = getHead(); cp != null; cp = cp.getPrevAttached()) {
			acp[n++] = cp;
		}
		return acp;
	}

	/**
	 *  Adds a hook to a ControlPoint
	 *
	 * @param  hookPos  The hook position (between 0..1)
	 * @return          The hook. Attach your "target hook" to this ControlPoint.
	 */
	public OLDControlPoint addHook(float hookPos, OLDModel model) {
		
		OLDControlPoint cp;
		OLDControlPoint cpHook;
		if (cpChildHook == null) {
			cpChildHook = createEmptyHookCurve();
			model.addCurve(cpChildHook);
		}
		cp = cpChildHook;
		while (cp.cpNext != null && cp.cpNext.fHookPos < hookPos) {
			cp = cp.cpNext;
		}
		if (cp.isEndHook()) {
			throw new IllegalStateException("can't add hook to " + this + " at position " + hookPos);
		}
		cpHook = new OLDControlPoint();
		//cpHook.cpParentHook = this;
		cpHook.fHookPos = hookPos;
		cpHook.appendTo(cp);
		return cpHook;
		/*
		if (cp.cpNext == null) {
			throw new JPatchException("can't add hook to end of curve!");
		};
		ControlPoint cp = cpNext();
		while (cp.isHook) {
			cp = cp.cpNext;
		}
		ControlPoint hook = new ControlPoint();
		hook.fHookPos = hookPos;
		hook.cpPrev = cp;
		hook.cpNext = cp.cpNext;
		if (cp.cpNext != null) {
			cp.cpNext.cpPrev = hook;
		}
		cp.cpNext = hook;
		return hook;
		*/
	}

	/**
	 * Hooks this ControlPoint ControlPoint cpHookTo at hookposition hookPos
	 * Creates a new hook (for cpHookTo) and attaches this ControlPoint to the resulting hook
	 */
	public void hookTo(OLDControlPoint cpHookTo, float hookPos, OLDModel model) {
		attachTo(cpHookTo.addHook(hookPos, model));
		/*
		ControlPoint cp;
		if (cpHookTo.cpChildHook == null) {
			cpHookTo.createEmptyHookCurve();
		}
		cp = cpHookTo.cpChildHook;
		while (cp.cpNext != null && cp.cpNext.fHookPos < hookPos) {
			cp = cp.cpNext;
		}
		if (cp.isEndHook()) {
			throw new JPatchException("can't add hook to " + this + " at position " + hookPos);
		}
		//cpHook = new ControlPoint();
		//cpHook.cpParentHook = this;
		fHookPos = hookPos;
		attachTo(cp);
		*/
	}

	public OLDControlPoint getHookAt(float hookPos) {
		for (OLDControlPoint cp = cpChildHook; cp != null; cp = cp.getNext()) {
			if (cp.fHookPos == hookPos) return cp;
		}
		return null;
	}
				
	/**
	 *  Appends this ControlPoint to ControlPoint cp
	 *
	 * @param  cp  The ControlPoint this ControlPoint will be appended to (on the same Curve)
	 */
	public void appendTo(OLDControlPoint cp) {
		OLDControlPoint next = cp.getNext();
		if (next != null) {
			next.setPrev(this);
		}
		cp.setNext(this);
		setPrev(cp);
		setNext(next);
	}

	/**
	 *  Attaches this ControlPoint to ControlPoint cp
	 *
	 * @param  cp  The ControlPoint this ControlPoint will be attached to
	 */
	public void attachTo(OLDControlPoint cp) {
		OLDControlPoint cpTail = cp.getTail();
		cpTail.cpPrevAttached = this;
		cpNextAttached = cpTail;
	}

	///**
	// *  Computes and sets the reference-position of this ControlPoint by reversing all morphs and skeleton
	// *  transformations starting from the current position.
	// */
	//public void fixPosition() {
	//	/*	!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	//	 *
	//	 * needs to be changed to support morphs and bones!!!
	//	 *
	//	 *	!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	//	 */
	//	System.out.println("ControlPoint.fixPosition()");
	//	p3ReferencePosition.set(p3Position);
	//}


	/**
	 *  Returns a list with information about all neighbors
	 *  Must only be called on head-points!!!
	 *
	 * @return    An ArrayList containing one array per neighbor
	 *	      The array contains: The neighbors head,
	 *				  The attached ControlPoint
	 *				  The neighbor
	 */
	public ArrayList allNeighbors() {
		ArrayList neighborList = new ArrayList();
		//ControlPoint cp = getHead();
		OLDControlPoint cp = this;
		while (cp != null) {
			addNeighborsToList(neighborList,cp);
			
			if (cp.cpChildHook != null) {
				addNeighborsToList(neighborList,cp.cpChildHook);
			}
			
			if (cp.cpPrev != null && cp.cpPrev.cpChildHook != null) {
				addNeighborsToList(neighborList,cp.cpPrev.cpChildHook.getEnd());
			}
			
			cp = cp.cpPrevAttached;
		}
		
		if (cpParentHook != null) {
			addNeighborsToList(neighborList,cpParentHook.getHead());
		}
		
		return neighborList;
	}
	
	/**
	 * ???
	 */
	private void addNeighborsToList(ArrayList list, OLDControlPoint cp) {
		OLDControlPoint[] neighborDetail;
		if (cp.cpNext != null) {
			neighborDetail = new OLDControlPoint[]{
				cp.cpNext.getHookHead(),
				cp,
				cp.cpNext
				};
			list.add(neighborDetail);
		}
		if (cp.cpPrev != null) {
			neighborDetail = new OLDControlPoint[]{
				cp.cpPrev.getHookHead(),
				cp,
				cp.cpPrev
				};
			list.add(neighborDetail);
		}
		//return list;
	}

	/**
	 * ???
	 */
	private OLDControlPoint getHookHead() {
		if (cpNextAttached != null) {
			return cpNextAttached.getHookHead();
		}
		if (cpParentHook != null) {
			return cpParentHook.getHookHead();
		}
		return this;
	}

	public OLDControlPoint getChildHook() {
		return cpChildHook;
	}
	
	public OLDControlPoint getParentHook() {
		return cpParentHook;
	}
	
	public void setChildHook(OLDControlPoint childHook) {
//		System.out.println("***");
//		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
//		for (int i = 0; i < stacktrace.length; System.out.println(stacktrace[i++]));
		cpChildHook = childHook;
	}
	
	public void setParentHook(OLDControlPoint parentHook) {
		cpParentHook = parentHook;
	}
	
	public float getHookPos() {
		return fHookPos;
	}
	
	public void setHookPos(float hookPos) {
		fHookPos = hookPos;
	}
	
	public boolean isHidden() {
		return bHidden;
	}
	
	public void setHidden(boolean enable) {
		bHidden = enable;
	}
	
	
	/**
	 * ????? no idea..... FIXME!!!!
	 */
	public OLDControlPoint getHookCurve() {
		return ((getStart().cpParentHook != null) ? getStart().cpParentHook : getStart());
	}

	/**
	 *
	 */
	public OLDControlPoint getNextAttached() {
		return cpNextAttached;
	}

	/**
	 *
	 */
	public OLDControlPoint getPrevAttached() {
		return cpPrevAttached;
	}
	
	/**
	 *  Counts the number of ControlPoints on this curve
	 *
	 * @return    the number of ControlPoints on this curve
	 */
	public int getCurveLength() {
		int number = 0;
		for (OLDControlPoint cp = getEnd(); cp != null; cp = cp.getPrevCheckLoop()) {
			number++;
		}
		return number;
	}


	/**
	 *  Returns the end of the curve
	 *
	 * @return    The last ControlPoint on the curve
	 */
	public OLDControlPoint getEnd() {
		return ((getNextCheckLoop() != null) ? cpNext.getEnd() : this);
	}


	/**
	 *  Recursively searches the head in the list of attached ControlPoints
	 *
	 * @return    returns the head of a list of attached ControlPoints
	 */
	public OLDControlPoint getHead() {
		return ((cpNextAttached != null) ? cpNextAttached.getHead() : this);
	}


	/**
	 *  Computes or returns the cached inTangent of this ControlPoint
	 *
	 * @return    The inTangent (javax.vecmath.Point3f)
	 */
	public Point3f getReferenceInTangent() {
		if (cpPrev == null) {
			//throw new JPatchException("attempted to get inTangent on hookStart " + this);
			return null;
		}
		if (isTargetHook()) {
			Vector3f v3Direction = new Vector3f(cpPrev.getReferencePosition());
			v3Direction.sub(getReferencePosition());
			Vector3f v3InTangent = getReferenceTargetHookTangent(v3Direction);
			v3InTangent.normalize();
			v3InTangent.scale(v3Direction.length() * fInMagnitude / 3f);
			Point3f p3InTangent = new Point3f(getReferencePosition());
			p3InTangent.add(v3InTangent);
			return p3InTangent;
		}
		if (isHook()) {
		//if (cpParentHook != null) {
			Point3f[] ap3Bezier1 = Bezier.deCasteljau(getStart().cpParentHook.getReferencePosition(),
				getStart().cpParentHook.getReferenceOutTangent(),
				getEnd().cpParentHook.getReferenceInTangent(),
				getEnd().cpParentHook.getReferencePosition(),
				fHookPos);
			//float alpha = (fHookPos - cpPrev.fHookPos) / (1 - cpPrev.fHookPos);
			Point3f[] ap3Bezier2 = Bezier.deCasteljau(ap3Bezier1[0], ap3Bezier1[1], ap3Bezier1[2], ap3Bezier1[3], cpPrev.fHookPos);
			return ap3Bezier2[5];
		}
		//if (!bTangentsValid) {			// if the tangents are invalid
			computeReferenceTangents();		// compute them
		//}
		return p3ReferenceInTangent;			// return cached in tangent
	}

	public Point3f getInTangent() {
		if (cpPrev == null) {
			throw new IllegalArgumentException("attempted to get inTangent on hookStart " + this);
//			return null;
		}
		if (isTargetHook()) {
			Vector3f v3Direction = new Vector3f(cpPrev.getReferencePosition());
			v3Direction.sub(getReferencePosition());
			Vector3f v3InTangent = getTargetHookTangent(v3Direction);
			v3InTangent.normalize();
			v3InTangent.scale(v3Direction.length() * fInMagnitude / 3f);
			Point3f p3InTangent = new Point3f(getPosition());
			p3InTangent.add(v3InTangent);
			return p3InTangent;
		}
		if (isHook()) {
		//if (cpParentHook != null) {
			Point3f[] ap3Bezier1 = Bezier.deCasteljau(
					getStart().cpParentHook.getPosition(),
					getStart().cpParentHook.getOutTangent(),
					getEnd().cpParentHook.getInTangent(),
					getEnd().cpParentHook.getPosition(),
					fHookPos
			);
			//float alpha = (fHookPos - cpPrev.fHookPos) / (1 - cpPrev.fHookPos);
			Point3f[] ap3Bezier2 = Bezier.deCasteljau(ap3Bezier1[0], ap3Bezier1[1], ap3Bezier1[2], ap3Bezier1[3], cpPrev.fHookPos);
			return ap3Bezier2[5];
		}
		//if (!bTangentsValid) {			// if the tangents are invalid
		computeTangents();		// compute them
		//}
		return p3InTangent;
	}
	
	/**
	 *  Computes or retunes the cached inTangent of this ControlPoint
	 *
	 * @return    The inTangent (javax.vecmath.Point3f)
	 */
	public Point3f getReferenceOutTangent() {
		if (cpNext == null) {
			throw new IllegalArgumentException("attempted to get outTangent on hookEnd " + this);
//			return null;
		}
		if (isTargetHook()) {
			Vector3f v3Direction = new Vector3f(cpNext.getReferencePosition());
			v3Direction.sub(getReferencePosition());
			Vector3f v3OutTangent = getReferenceTargetHookTangent(v3Direction);
			v3OutTangent.normalize();
			v3OutTangent.scale(v3Direction.length() * fOutMagnitude / 3f);
			Point3f p3OutTangent = new Point3f(getReferencePosition());
			p3OutTangent.add(v3OutTangent);
			return p3OutTangent;
		}
		if (isHook()) {
		//if (cpParentHook != null) {
			Point3f[] ap3Bezier1 = Bezier.deCasteljau(
				getStart().cpParentHook.getReferencePosition(),
				getStart().cpParentHook.getReferenceOutTangent(),
				getEnd().cpParentHook.getReferenceInTangent(),
				getEnd().cpParentHook.getReferencePosition(),
				fHookPos
				);
			Point3f[] ap3Bezier2 = Bezier.deCasteljau(ap3Bezier1[3], ap3Bezier1[4], ap3Bezier1[5], ap3Bezier1[6], cpNext.fHookPos);
			return ap3Bezier2[1];
		}
		//if (!bTangentsValid) {			// if the tangents are invalid
			computeReferenceTangents();		// compute them
		//}
		return p3ReferenceOutTangent;			// return cached out tangent
	}
	
	public Point3f getOutTangent() {
		if (cpNext == null) {
			//throw new JPatchException("attempted to get outTangent on hookEnd " + this);
			return null;
		}
		if (isTargetHook()) {
			Vector3f v3Direction = new Vector3f(cpNext.getReferencePosition());
			v3Direction.sub(getReferencePosition());
			Vector3f v3OutTangent = getTargetHookTangent(v3Direction);
			v3OutTangent.normalize();
			v3OutTangent.scale(v3Direction.length() * fOutMagnitude / 3f);
			Point3f p3OutTangent = new Point3f(getPosition());
			p3OutTangent.add(v3OutTangent);
			return p3OutTangent;
		}
		if (isHook()) {
		//if (cpParentHook != null) {
			Point3f[] ap3Bezier1 = Bezier.deCasteljau(
				getStart().cpParentHook.getPosition(),
				getStart().cpParentHook.getOutTangent(),
				getEnd().cpParentHook.getInTangent(),
				getEnd().cpParentHook.getPosition(),
				fHookPos
				);
			Point3f[] ap3Bezier2 = Bezier.deCasteljau(ap3Bezier1[3], ap3Bezier1[4], ap3Bezier1[5], ap3Bezier1[6], cpNext.fHookPos);
			return ap3Bezier2[1];
		}
		//if (!bTangentsValid) {			// if the tangents are invalid
		computeTangents();		// compute them
		//}
		return p3OutTangent;
	}

	/**
	 *  Goes through all ControlPoints this one is attached to and
	 *  returns the first ControlPoint that is a start or end of a curve, or
	 *  - if none is found - the tail. (or null???)
	 *
	 * @return    A start or end ControlPoint (if found), otherwise the tail
	 */
	public OLDControlPoint getLooseEnd() {
		OLDControlPoint cp;
		for (cp = getHead(); cp != null && !cp.isStart() && !cp.isEnd(); cp = cp.cpPrevAttached);
		return cp;
	}

	/**
	 * Gets the loop flag
	 *
	 * @return	loop
	 */
	public boolean getLoop() {
		return bLoop;
	}
	
	/**
	 * Gets the next ControlPoint on the curve
	 *
	 * @return    The next ControlPoint
	 */
	public OLDControlPoint getNext() {
		return cpNext;
	}

	/**
	 * Returns the next ControlPoint but checks the loop flag of this
	 * ControlPoint before it
	 *
	 * @return    The next ControlPoint if there is no loop, null otherwise
	 */
	public OLDControlPoint getNextCheckLoop() {
		return (bLoop ? null : cpNext);
	}

	/**
	 * Returns the next ControlPoint on the curve but checks the loop-flag of the
	 * next point before it.
	 *
	 * @return	The next ControlPoint if there is no loop, null otherwise
	 */
	public OLDControlPoint getNextCheckNextLoop() {
		return ((cpNext != null && cpNext.bLoop) ? null : cpNext);
	}

	/**
	 * Gets the position of this ControlPoint. If this ControlPoint is
	 * not a head, it returns the position of the head or - in case of a hook -
	 * computes the hook's position.
	 *
	 * @return    The position
	 */
	public Point3f getReferencePosition() {
		if (isTargetHook()) {
			return cpNextAttached.getReferencePosition();
		} else if (isHook()) {
			return Bezier.evaluate(
				getStart().cpParentHook.getReferencePosition(),
				getStart().cpParentHook.getReferenceOutTangent(),
				getEnd().cpParentHook.getReferenceInTangent(),
				getEnd().cpParentHook.getReferencePosition(),
				fHookPos
				);
		} else if (cpParentHook != null) {
			return cpParentHook.getReferencePosition();
		} else if (isHead()) {
			return p3ReferencePosition;
//			Point3f p = new Point3f(p3ReferencePosition);
//			if (bone != null) {
//				RotationDof dof = bone.getLastDof();
//				if (dof != null) {
//					//if (!dof.isTransformValid()) {
//						m4Transform.set(dof.getTransform());
//						m4InvTransform.set(dof.getInvTransform());
////						bTangentsValid = false;
//					//}
//				}
//			}
//			m4Transform.transform(p);
//			return p;
		} else {
			return getHead().getReferencePosition();
		}
	}

	public Point3f getPosition() {
		if (isTargetHook()) {
			return cpNextAttached.getPosition();
		} else if (isHook()) {
			return Bezier.evaluate(
				getStart().cpParentHook.getPosition(),
				getStart().cpParentHook.getOutTangent(),
				getEnd().cpParentHook.getInTangent(),
				getEnd().cpParentHook.getPosition(),
				fHookPos
				);
		} else if (cpParentHook != null) {
			return cpParentHook.getPosition();
		} else if (isHead()) {
			Point3f p = new Point3f(p3ReferencePosition);
			m4Transform.transform(p);
			return p;
		} else {
			return getHead().getPosition();
		}
	}

	public void setBonePose() {
		if (bone != null) {
			RotationDof dof = bone.getLastDof();
			if (dof != null) {
				dof.getTransform(m4BoneTransform, fBonePosition, fDistanceToLine, bParentBone);			
			}
		} else {
			m4BoneTransform.setIdentity();
		}
		m4InvBoneTransform.invert(m4BoneTransform);
	}
	
	public void setMorphPose() {
		v3Morph.set(v3ReferenceMorph);
		m4BoneTransform.transform(v3Morph);
		m4Transform.set(m4BoneTransform);
		m4Transform.m03 += v3Morph.x;
		m4Transform.m13 += v3Morph.y;
		m4Transform.m23 += v3Morph.z;
		m4InvTransform.invert(m4Transform);
	}
	
	public Vector3f getMorphVector() {
		return v3ReferenceMorph;
	}
	
	/**
	 *  Gets the previous ControlPoint on the curve
	 *
	 * @return    The previous ControlPoint
	 */
	public OLDControlPoint getPrev() {
		return cpPrev;
	}


	/**
	 *  Returns the prev ControlPoint but checks for a loop before
	 *
	 * @return    The prev ControlPoint if loop == false, else null
	 */
	public OLDControlPoint getPrevCheckLoop() {
		return (bLoop ? null : cpPrev);
	}


	/**
	 *  Gets the referencePosition of the ControlPoint object
	 *
	 * @return    The referencePosition
	 */
	//public Point3f getReferencePosition() {
	//	return p3ReferencePosition;
	//}


	/*
	 *  public ControlPoint getNext(boolean checkLoop) {
	 *  if (checkLoop) {
	 *  return getNextCheckLoop();
	 *  } else {
	 *  return getNext();
	 *  }
	 *  }
	 *  public ControlPoint getPrev(boolean checkLoop) {
	 *  if (checkLoop) {
	 *  return getPrevCheckLoop();
	 *  } else {
	 *  return getPrev();
	 *  }
	 *  }
	 */
	/**
	 *  Returns the start of the curve
	 *
	 * @return    The start ControlPoint
	 */
	public OLDControlPoint getStart() {
		return ((getPrevCheckLoop() != null) ? cpPrev.getStart() : this);
	}

	/**
	 *  Recursively searches the tail of a list of attached ControlPoints
	 *
	 * @return    returns the tail of a list of attached ControlPoints
	 */
	public OLDControlPoint getTail() {
		return ((cpPrevAttached != null) ? cpPrevAttached.getTail() : this);
	}

	/**
	 *  Checks if another ControlPoint is attached to this one.
	 *
	 * @return    returns true if another ControlPoint is attached to this one.
	 */
	public boolean hasAttached() {
		return (cpPrevAttached != null);
	}


	/**
	 *  Returns some textual info about this ControlPoint, for debugging reasons
	 *
	 * @return    a string with some information about this ControlPoint
	 */
	public String info() {
		return p3ReferencePosition + "\tthis:" + this + "\tnext:" + cpNext + "\tprev:" + cpPrev + "\tup  :" + cpNextAttached + "\tdown:" + cpPrevAttached
			 + "\tpar :" + cpParentHook + "\tchld:" + cpChildHook + "\thp:" + fHookPos;
	}

	/**
	 *  Checks if this ControlPoint is the end (of the curve)
	 *
	 * @return    returns true if this ControlPoint the end (of the curve)
	 */
	public boolean isEnd() {
		return (getNext() == null);
	}

	public boolean isCenter() {
		return (cpNext != null && cpPrev != null);
	}
	
	/**
	 *  Checks if this ControlPoint is an end-hook
	 *
	 * @return    returns true if this ControlPoint is an end-hook
	 */
	
	public boolean isEndHook() {
		return (isHook() && fHookPos == 1f);
	}

	public boolean isChildHook() {
		return (cpParentHook != null);
	}

	/**
	 *  Checks if this ControlPoint is a head
	 *
	 * @return    returns true if this ControlPoint is a head
	 */
	public boolean isHead() {
		return (cpNextAttached == null);
	}


	/**
	 *  Checks if this ControlPoint is a hook
	 *
	 * @return    returns true if this ControlPoint is a hook
	 */
	public boolean isHook() {
		return (fHookPos > -1);
	}

	/**
	 *  Checks if this ControlPoint is the start (of the curve)
	 *
	 * @return    returns true if this ControlPoint is the start (of the curve)
	 */
	public boolean isStart() {
		return (getPrev() == null);
	}


	public boolean isSingle() {
		return (cpNextAttached == null && cpPrevAttached == null);
	}
	
	public boolean isMulti() {
		return (cpPrevAttached != null && cpPrevAttached.cpPrevAttached != null);
	}
	
	/**
	 *  Checks if this ControlPoint is a start-hook
	 *
	 * @return    returns true if this ControlPoint is a start-hook
	 */
	 
	public boolean isStartHook() {
		return (fHookPos == 0f);
	}
	

	/**
	 *  Checks if this ControlPoint is a target-hook
	 *
	 * @return    returns true if this ControlPoint is a target-hook
	 */
	public boolean isTargetHook() {
		return (cpNextAttached != null && cpNextAttached.isHook());
	}


	/**
	 *  Computes and sets the position by applying all morphs and skeleton transformations starting from the
	 *  reference position.
	 */
	//public void recomputePosition() {
	//	/*	!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	//	 *
	//	 * needs to be changed to support morphs and bones!!!
	//	 *
	//	 *	!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	//	 */
	//	setPosition(p3ReferencePosition);
	//}

	//public void resetPosition() {
	//	setPosition(p3ReferencePosition);
	//}

	/**
	 *  Sets both, in- and outMagnitude.
	 *
	 * @param  curvature  The new curvature value
	 */
	public void setMagnitude(float curvature) {
		fInMagnitude = curvature;
		fOutMagnitude = curvature;
	}

	/**
	 *  Sets the loop flag
	 *
	 * @param  loop  Must be true on the FIRST ControlPoint of a CLOSED curve!
	 */
	public void setLoop(boolean loop) {
		bLoop = loop;
	}

	public void setDeleted(boolean deleted) {
		bDeleted = deleted;
	}
	
	public boolean isDeleted() {
		return bDeleted;
	}

	/**
	 *  Sets the tangent mode of the ControlPoint object
	 *
	 * @param  mode  The new tangent mode value (e.g. PEAK, SPATCH_ROUND, AM_SMOOTH, etc.)
	 */
	public void setMode(int mode) {
		iMode = mode;
	}

	/**
	 * Gets the tangent mode
	 *
	 * @return 	The tangent mode
	 */
	public int getMode() {
		return iMode;
	}

	/**
	 *  Sets the next ControlPoint and invalidates the tangents
	 *
	 * @param  next  The next ControlPoint on the curve
	 */
	public void setNext(OLDControlPoint next) {
		cpNext = next;
//		invalidateTangents();
	}

	/**
	 *  Sets the nextAttached ControlPoint and invalidates the tangents
	 *
	 * @param  nextAttached  The next attached ControlPoint
	 */
	public void setNextAttached(OLDControlPoint nextAttached) {
		cpNextAttached = nextAttached;
//		invalidateTangents();
	}

	/**
	 * Sets the position of the ControlPoint object, or if it is attached,
	 * the position of the head - and invalidates the tangents
	 *
	 * @param  position  The new position (javax.vecmath.Point3f)
	 */
	public void setPosition(Point3f position) {
		if (!isHead()) throw new IllegalStateException("attempted to set potsition on attached point");
		p3ReferencePosition.set(position);
		m4InvTransform.transform(p3ReferencePosition);
//		invalidateTangents();
	}


	/**
	 * Sets the position of the ControlPoint object, or if it is attached,
	 * the position of the head - and invalidates the tangents
	 *
	 * @param  x  The new x-coordinate
	 * @param  y  The new y-coordinate
	 * @param  z  The new z-coordinate
	 */
	public void setPosition(float x, float y, float z) {
		if (!isHead()) throw new IllegalStateException("attempted to set potsition on attached point");
		p3ReferencePosition.set(x, y,z);
		m4InvTransform.transform(p3ReferencePosition);
//		invalidateTangents();
	}

	public void setReferencePosition(float x, float y, float z) {
		if (!isHead()) throw new IllegalStateException("attempted to set reference potsition on attached point");
		p3ReferencePosition.set(x, y,z);
//		invalidateTangents();
	}
	
	/**
	 *  Sets the prev ControlPoint and invalidates the tangents
	 *
	 * @param  prev  The previous ControlPoint on the curve
	 */
	public void setPrev(OLDControlPoint prev) {
		cpPrev = prev;
//		invalidateTangents();
	}

	/**
	 *  Sets the prevAttached ControlPoint
	 *
	 * @param  prevAttached  The previous attached ControlPoint
	 */
	public void setPrevAttached(OLDControlPoint prevAttached) {
		cpPrevAttached = prevAttached;
	}

	/**
	 *  Sets the referencePosition of the ControlPoint object
	 *
	 * @param  referencePosition  The referencePosition
	 */
	//public void setReferencePosition(Point3f referencePosition) {
	//	p3ReferencePosition.set(referencePosition);
	//}


//	/**
//	 *  Sets the tangentsValid attribute of the ControlPoint object
//	 *
//	 * @param  valid  Sets the tangents valid or invalid.
//	 *		Invalid tangents will become re-computed the next time the're needed.
//	 */
//	public void setTangentsValid(boolean valid) {
//		bTangentsValid = valid;
//	}

	/**
	 *  Just returns the hashCode as a String
	 *
	 * @return    The objects hashCode as a String
	 */
	public String toString() {
		return "cp_" + id;
	}

	/**
	 *  Computes reference tangents for "PEAK" mode
	 */
	private void computeReferencePeakTangents() {
		if (cpPrev != null) {
			p3ReferenceInTangent.interpolate(getReferencePosition(), cpPrev.getReferencePosition(), fInMagnitude / 3f);
		}
		if (cpNext != null) {
			p3ReferenceOutTangent.interpolate(getReferencePosition(), cpNext.getReferencePosition(), fOutMagnitude /3f);
		}
	}
	
	/**
	 *  Computes tangents for "PEAK" mode
	 */
	private void computePeakTangents() {
		if (cpPrev != null) {
			p3InTangent.interpolate(getPosition(), cpPrev.getPosition(), fInMagnitude / 3f);
		}
		if (cpNext != null) {
			p3OutTangent.interpolate(getPosition(), cpNext.getPosition(), fOutMagnitude /3f);
		}
	}
	
       
	/**
	 *  Computes reference tangents for "SPATCH_ROUND" mode
	 */
	private void computeReferenceSPatchRoundTangents() {
		Vector3f v3InTangent;
		Vector3f v3OutTangent;
		float fInLength = 0f;
		float fOutLength = 0f;
		if (cpPrev != null) {
			fInLength = cpPrev.getReferencePosition().distance(getReferencePosition());
		}
		if (cpNext != null) {
			fOutLength = cpNext.getReferencePosition().distance(getReferencePosition());
		}
		v3InTangent = getReferenceNormalizedTangent();
		v3OutTangent = new Vector3f(v3InTangent);
		v3InTangent.scale(fInMagnitude * fInLength / 3f);
		v3OutTangent.scale(fOutMagnitude * fOutLength /3f);
		p3ReferenceInTangent.set(getReferencePosition());
		p3ReferenceOutTangent.set(getReferencePosition());
		p3ReferenceInTangent.sub(v3InTangent);
		p3ReferenceOutTangent.add(v3OutTangent);
	}

	/**
	 *  Computes tangents for "SPATCH_ROUND" mode
	 */
	private void computeSPatchRoundTangents() {
		Vector3f v3InTangent;
		Vector3f v3OutTangent;
		float fInLength = 0f;
		float fOutLength = 0f;
		if (cpPrev != null) {
			fInLength = cpPrev.getPosition().distance(getPosition());
		}
		if (cpNext != null) {
			fOutLength = cpNext.getPosition().distance(getPosition());
		}
		v3InTangent = getNormalizedTangent();
		v3OutTangent = new Vector3f(v3InTangent);
		v3InTangent.scale(fInMagnitude * fInLength / 3f);
		v3OutTangent.scale(fOutMagnitude * fOutLength /3f);
		p3InTangent.set(getPosition());
		p3OutTangent.set(getPosition());
		p3InTangent.sub(v3InTangent);
		p3OutTangent.add(v3OutTangent);
	}
	
	/**
	 * Compute reference G1 tangents
	 */
	private void computeReferenceG1Tangents() {
		float s;
		Vector3f v3 = new Vector3f();
		if (cpPrev != null && cpNext != null) {
			Point3f A = new Point3f(cpPrev.getReferencePosition());
			Point3f B = new Point3f(cpNext.getReferencePosition());
			Point3f C = new Point3f(getReferencePosition());
			
			float ca = C.distance(A);
			float cb = C.distance(B);
			
			Point3f AC = new Point3f(C);
			AC.sub(A);
			s = (ca == 0) ? 0 : cb / ca / 3f;
			AC.scaleAdd(s, C);
			
			Point3f BC = new Point3f(C);
			BC.sub(B);
			s = (cb == 0) ? 0 : ca / cb / 3f;
			BC.scaleAdd(s, C);
			
			float a = (float) Math.sqrt(ca);
			float b = (float) Math.sqrt(cb);
			float t = (a != 0) ? a / (a + b) : 0;
			float t1 = 1 - t;
			float b0 = t1 * t1;
			float b1 = 2 * t1 * t;
			float b2 = t * t;

			float x = A.x * b0 + BC.x * b1 + AC.x * b2;
			float y = A.y * b0 + BC.y * b1 + AC.y * b2;
			float z = A.z * b0 + BC.z * b1 + AC.z * b2;
			
			v3.set(x,y,z);
			v3.sub(C);
			v3.scale(fInMagnitude);
			p3ReferenceInTangent.add(C,v3);
			
			x = BC.x * b0 + AC.x * b1 + B.x * b2; 
			y = BC.y * b0 + AC.y * b1 + B.y * b2; 
			z = BC.z * b0 + AC.z * b1 + B.z * b2; 
			
			v3.set(x,y,z);
			v3.sub(C);
			v3.scale(fOutMagnitude);
			p3ReferenceOutTangent.add(C,v3);
			
		} else if (cpPrev == null) {
			if (cpNext != null && cpNext.cpNext != null) {
				Point3f A = new Point3f(getReferencePosition());
				Point3f B = new Point3f(cpNext.cpNext.getReferencePosition());
				Point3f C = new Point3f(cpNext.getReferencePosition());
		        	
				float ca = C.distance(A);
				float cb = C.distance(B);
				
				Point3f AC = new Point3f(C);
				AC.sub(A);
				s = (ca == 0) ? 0 : cb / ca / 3f;
				AC.scaleAdd(s, C);
				
				Point3f BC = new Point3f(C);
				BC.sub(B);
				s = (cb == 0) ? 0 : ca / cb / 3f;
				BC.scaleAdd(s, C);
				
				float a = (float) Math.sqrt(ca);
				float b = (float) Math.sqrt(cb);
				float t = a / (a + b);
				p3ReferenceOutTangent.interpolate(A,BC,t * fOutMagnitude);
			} else {
				p3ReferenceOutTangent.interpolate(getReferencePosition(),cpNext.getReferencePosition(),fOutMagnitude / 3f);
			}
		} else {	//cpNext == null
			if (cpPrev != null && cpPrev.cpPrev != null) {
				Point3f A = new Point3f(getReferencePosition());
				Point3f B = new Point3f(cpPrev.cpPrev.getReferencePosition());
				Point3f C = new Point3f(cpPrev.getReferencePosition());
		        	
				float ca = C.distance(A);
				float cb = C.distance(B);
				
				Point3f AC = new Point3f(C);
				AC.sub(A);
				s = (ca == 0) ? 0 : cb / ca / 3f;
				AC.scaleAdd(s, C);
				
				Point3f BC = new Point3f(C);
				BC.sub(B);
				s = (cb == 0) ? 0 : ca / cb / 3f;
				BC.scaleAdd(s, C);
				
				float a = (float) Math.sqrt(ca);
				float b = (float) Math.sqrt(cb);
				float t = a / (a + b);
				p3ReferenceInTangent.interpolate(A,BC,t * fInMagnitude);
			} else {
				p3ReferenceInTangent.interpolate(getReferencePosition(),cpPrev.getReferencePosition(),fInMagnitude / 3f);
		
			}
		}		
	}
	/**
	 * Compute G1 tangents
	 */
	private void computeG1Tangents() {
		float s;
		Vector3f v3 = new Vector3f();
		if (cpPrev != null && cpNext != null) {
			Point3f A = new Point3f(cpPrev.getPosition());
			Point3f B = new Point3f(cpNext.getPosition());
			Point3f C = new Point3f(getPosition());
			
			float ca = C.distance(A);
			float cb = C.distance(B);
			
			Point3f AC = new Point3f(C);
			AC.sub(A);
			s = (ca == 0) ? 0 : cb / ca / 3f;
			AC.scaleAdd(s, C);
			
			Point3f BC = new Point3f(C);
			BC.sub(B);
			s = (cb == 0) ? 0 : ca / cb / 3f;
			BC.scaleAdd(s, C);
			
			float a = (float) Math.sqrt(ca);
			float b = (float) Math.sqrt(cb);
			float t = (a != 0) ? a / (a + b) : 0;
			float t1 = 1 - t;
			float b0 = t1 * t1;
			float b1 = 2 * t1 * t;
			float b2 = t * t;
			
			float x = A.x * b0 + BC.x * b1 + AC.x * b2;
			float y = A.y * b0 + BC.y * b1 + AC.y * b2;
			float z = A.z * b0 + BC.z * b1 + AC.z * b2;
			
			v3.set(x,y,z);
			v3.sub(C);
			v3.scale(fInMagnitude);
			p3InTangent.add(C,v3);
			
			x = BC.x * b0 + AC.x * b1 + B.x * b2; 
			y = BC.y * b0 + AC.y * b1 + B.y * b2; 
			z = BC.z * b0 + AC.z * b1 + B.z * b2; 
			
			v3.set(x,y,z);
			v3.sub(C);
			v3.scale(fOutMagnitude);
			p3OutTangent.add(C,v3);
			
		} else if (cpPrev == null) {
			if (cpNext != null && cpNext.cpNext != null) {
				Point3f A = new Point3f(getPosition());
				Point3f B = new Point3f(cpNext.cpNext.getPosition());
				Point3f C = new Point3f(cpNext.getPosition());
				
				float ca = C.distance(A);
				float cb = C.distance(B);
				
				Point3f AC = new Point3f(C);
				AC.sub(A);
				s = (ca == 0) ? 0 : cb / ca / 3f;
				AC.scaleAdd(s, C);
				
				Point3f BC = new Point3f(C);
				BC.sub(B);
				s = (cb == 0) ? 0 : ca / cb / 3f;
				BC.scaleAdd(s, C);
				
				float a = (float) Math.sqrt(ca);
				float b = (float) Math.sqrt(cb);
				float t = a / (a + b);
				p3OutTangent.interpolate(A,BC,t * fOutMagnitude);
			} else {
				p3OutTangent.interpolate(getPosition(),cpNext.getPosition(),fOutMagnitude / 3f);
			}
		} else {	//cpNext == null
			if (cpPrev != null && cpPrev.cpPrev != null) {
				Point3f A = new Point3f(getPosition());
				Point3f B = new Point3f(cpPrev.cpPrev.getPosition());
				Point3f C = new Point3f(cpPrev.getPosition());
				
				float ca = C.distance(A);
				float cb = C.distance(B);
				
				Point3f AC = new Point3f(C);
				AC.sub(A);
				s = (ca == 0) ? 0 : cb / ca / 3f;
				AC.scaleAdd(s, C);
				
				Point3f BC = new Point3f(C);
				BC.sub(B);
				s = (cb == 0) ? 0 : ca / cb / 3f;
				BC.scaleAdd(s, C);
				
				float a = (float) Math.sqrt(ca);
				float b = (float) Math.sqrt(cb);
				float t = a / (a + b);
				p3InTangent.interpolate(A,BC,t * fInMagnitude);
			} else {
				p3InTangent.interpolate(getPosition(),cpPrev.getPosition(),fInMagnitude / 3f);
				
			}
		}		
		//Vector3f v3InTangent = new Vector3f();
		//Vector3f v3OutTangent = new Vector3f();
		//float fInLength = 0f;
		//float fOutLength = 0f;
		//
		//if (cpPrev != null) {
		//	fInLength = cpPrev.getPosition().distance(getPosition());
		//}
		//if (cpNext != null) {
		//	fOutLength = cpNext.getPosition().distance(getPosition());
		//}
		//
		//if (cpPrev != null && cpNext != null) {
		//	v3InTangent.addScaled(cpPrev.getPosition(),1.0f/6.0f);
		//	v3InTangent.addScaled(cpNext.getPosition(),-1.0f/6.0f);
		//} else if (cpPrev != null && cpPrev.cpPrev != null) {
		//	v3InTangent.scale(-0.5f,getPosition());
		//	v3InTangent.addScaled(cpPrev.getPosition(),4.0f/6.0f);
		//	v3InTangent.addScaled(cpPrev.cpPrev.getPosition(),-1.0f/6.0f);
		//	fOutLength = cpPrev.getPosition().distance(cpPrev.cpPrev.getPosition());
		//} else if (cpPrev != null) {
		//	v3InTangent.sub(cpPrev.getPosition(), getPosition());
		//	v3InTangent.scale(1.0f/3.0f);
		//	v3InTangent.scale(0.5f);
		//}
		//if (cpNext != null && cpPrev != null) {
		//	v3OutTangent.addScaled(cpNext.getPosition(),1.0f/6.0f);
		//	v3OutTangent.addScaled(cpPrev.getPosition(),-1.0f/6.0f);
		//} else if (cpNext != null && cpNext.cpNext != null) {
		//	v3OutTangent.scale(-0.5f,getPosition());
		//	v3OutTangent.addScaled(cpNext.getPosition(),4.0f/6.0f);
		//	v3OutTangent.addScaled(cpNext.cpNext.getPosition(),-1.0f/6.0f);
		//	fInLength = cpNext.getPosition().distance(cpNext.cpNext.getPosition());
		//} else if (cpNext != null) {
		//	v3OutTangent.sub(cpNext.getPosition(), getPosition());
		//	v3OutTangent.scale(1.0f/3.0f);
		//	v3OutTangent.scale(0.5f);
		//}
		//
		//float fTotalLength = fInLength + fOutLength;
		//if (fTotalLength != 0) {
		//	fInLength /= fTotalLength;
		//	fOutLength /= fTotalLength;
		//}
		//
		//v3InTangent.scale(fInLength * 6.0f * fInMagnitude);
		//v3OutTangent.scale(fOutLength * 6.0f * fOutMagnitude);
		//p3InTangent.add(getPosition(),v3InTangent);
		//p3OutTangent.add(getPosition(),v3OutTangent);
	}
	
	///**
	// * Compute C1 tangents
	// */
	//private void computeC1Tangents() {
	//	if (cpPrev != null && cpNext != null) {
	//		p3InTangent.set(getPosition());
	//		p3InTangent.addScaled(cpPrev.getPosition(),1.0f/6.0f);
	//		p3InTangent.addScaled(cpNext.getPosition(),-1.0f/6.0f);
	//	} else if (cpPrev != null && cpPrev.cpPrev != null) {
	//		p3InTangent.scale(0.5f,getPosition());
	//		p3InTangent.addScaled(cpPrev.getPosition(),4.0f/6.0f);
	//		p3InTangent.addScaled(cpPrev.cpPrev.getPosition(),-1.0f/6.0f);
	//	} else if (cpPrev != null) {
	//		p3InTangent.interpolate(getPosition(),cpPrev.getPosition(), 2.0f/3.0f);
	//	}
	//	if (cpNext != null && cpPrev != null) {
	//		p3OutTangent.set(getPosition());
	//		p3OutTangent.addScaled(cpNext.getPosition(),1.0f/6.0f);
	//		p3OutTangent.addScaled(cpPrev.getPosition(),-1.0f/6.0f);
	//	} else if (cpNext != null && cpNext.cpNext != null) {
	//		p3OutTangent.scale(0.5f,getPosition());
	//		p3OutTangent.addScaled(cpNext.getPosition(),4.0f/6.0f);
	//		p3OutTangent.addScaled(cpNext.cpNext.getPosition(),-1.0f/6.0f);
	//	} else if (cpNext != null) {
	//		p3OutTangent.interpolate(getPosition(),cpNext.getPosition(), 2.0f/3.0f);
	//	}
	//}
	//
	///**
	// * Compute Animation:Master tangents
	// */
	//private void computeAMSmoothTangents() {
	//	//System.out.println("computeAMSmoothTangents()");
	//	Vector3f v3InTangent;
	//	Vector3f v3OutTangent;
	//	float fInLength = 0f;
	//	float fOutLength = 0f;
	//	if (cpPrev != null) {
	//		fInLength = cpPrev.getPosition().distance(getPosition());
	//	}
	//	if (cpNext != null) {
	//		fOutLength = cpNext.getPosition().distance(getPosition());
	//	}
	//	v3InTangent = getNormalizedTangent();
	//	v3OutTangent = new Vector3f(v3InTangent);
	//	v3InTangent.scale(fInMagnitude * fInLength);
	//	v3OutTangent.scale(fInMagnitude * fOutLength);
	//	
	//	Point3f p3 = getPosition();
	//	Point3f p3n = null;
	//	Point3f p3p = null;
	//	Vector3f v3n = new Vector3f();
	//	Vector3f v3p = new Vector3f();
	//	Vector3f v3Normal = new Vector3f();
	//	if (cpNext != null) {
	//		p3n = cpNext.getPosition();
	//		if (cpPrev == null && cpNext.cpNext != null) {
	//			p3p = cpNext.cpNext.getPosition();
	//		}
	//	}
	//	if (cpPrev != null) {
	//		p3p = cpPrev.getPosition();
	//		if (cpNext == null && cpPrev.cpPrev != null) {
	//			p3n = cpPrev.cpPrev.getPosition();
	//		}
	//	}
	//	if (p3n != null && p3p != null) {
	//		//System.out.println("fInAlpha = " + fInAlpha);
	//		v3n.sub(p3n,p3);
	//		v3p.sub(p3p,p3);
	//		//v3n.normalize();
	//		//v3p.normalize();
	//		v3Normal.cross(v3n,v3p);
	//		//System.out.println(v3n.dot(v3p));
	//		//System.out.println(v3Normal);
	//		AxisAngle4f axisAngle;
	//		/*
	//		if (v3Normal.x < 0) {
	//			v3Normal.scale(-1);
	//		}
	//		if (v3Normal.y < 0) {
	//			v3Normal.scale(-1);
	//		}
	//		if (v3Normal.z < 0) {
	//			v3Normal.scale(-1);
	//		}
	//		*/
	//		Matrix3f matrix3;
	//		if (fInAlpha != 0) {
	//			axisAngle = new AxisAngle4f(v3Normal,fInAlpha);
	//			matrix3 = new Matrix3f();
	//			matrix3.set(axisAngle);
	//			matrix3.transform(v3InTangent);
	//			matrix3.transform(v3OutTangent);
	//		}
	//		if (fInGamma != 0) {
	//			Vector3f v3Gamma = new Vector3f();
	//			v3Gamma.cross(v3Normal,v3InTangent);
	//			//v3Gamma.normalize();
	//			axisAngle = new AxisAngle4f(v3Gamma,fInGamma);
	//			matrix3 = new Matrix3f();
	//			matrix3.set(axisAngle);
	//			matrix3.transform(v3InTangent);
	//			matrix3.transform(v3OutTangent);
	//		}
	//	}
	//	p3InTangent.sub(getPosition(),v3InTangent);
	//	p3OutTangent.add(getPosition(),v3OutTangent);
	//}
	
	/*
	public void setInBias(Vector3f v3InBiasTangent) {
		v3InTangent = getNormalizedTangent();
		Point3f p3 = getPosition();
		Point3f p3n = null;
		Point3f p3p = null;
		Vector3f v3n = new Vector3f();
		Vector3f v3p = new Vector3f();
		Vector3f v3Normal = new Vector3f();
		if (cpNext != null) {
			p3n = cpNext.getPosition();
			if (cpPrev == null && cpNext.cpNext != null) {
				p3p = cpNext.cpNext.getPosition();
			}
		}
		if (cpPrev != null) {
			p3p = cpPrev.getPosition();
			if (cpNext == null && cpPrev.cpPrev != null) {
				p3n = cpPrev.cpPrev.getPosition();
			}
		}
		if (p3n != null && p3p != null) {
			//System.out.println("fInAlpha = " + fInAlpha);
			v3n.sub(p3n,p3);
			v3p.sub(p3p,p3);
			//v3n.normalize();
			//v3p.normalize();
			v3Normal.cross(v3n,v3p);
	*/
	
	
	///**
	// *
	// */
	//public void setAlpha(float alpha) {
	//	fInAlpha = alpha;
	//	fOutAlpha = alpha;
	//	//computeTangents();
	//}
	//
	///**
	// *
	// */
	//public void setGamma(float gamma) {
	//	fInGamma = gamma;
	//	fOutGamma = gamma;
	//	//computeTangents();
	//}
	
	/**
	 *
	 */
	public void setInMagnitude(float inMagnitude) {
		fInMagnitude = inMagnitude;
		//computeTangents();
	}
	
	/**
	 *
	 */
	public float getInMagnitude() {
		return fInMagnitude;
	}
	
	/**
	 *
	 */
	public void setOutMagnitude(float outMagnitude) {
		fOutMagnitude = outMagnitude;
		//computeTangents();
	}
	
	/**
	 *
	 */
	public float getOutMagnitude() {
		return fOutMagnitude;
	}
	
	///**
	// *
	// */
	//public void setInAlpha(float inAlpha) {
	//	fInAlpha = inAlpha * DEG2RAD;
	//	//computeTangents();
	//}
	//
	///**
	// *
	// */
	//public float getInAlpha() {
	//	return fInAlpha / DEG2RAD;
	//}
	//
	///**
	// *
	// */
	//public void setOutAlpha(float outAlpha) {
	//	fOutAlpha = outAlpha * DEG2RAD;
	//	//computeTangents();
	//}
	//
	///**
	// *
	// */
	//public float getOutAlpha() {
	//	return fOutAlpha / DEG2RAD;
	//}
	//
	///**
	// *
	// */
	//public void setInGamma(float inGamma) {
	//	fInGamma = inGamma * DEG2RAD;
	//	//computeTangents();
	//}
	//
	///**
	// *
	// */
	//public float getInGamma() {
	//	return fInGamma / DEG2RAD;
	//}
	//
	///**
	// *
	// */
	//public void setOutGamma(float outGamma) {
	//	fOutGamma = outGamma * DEG2RAD;
	//	//computeTangents();
	//}
	//
	///**
	// *
	// */
	//public float getOutGamma() {
	//	return fOutGamma / DEG2RAD;
	//}
	
	/**
	 *  Compute the tangents and sets tangents valid
	 */
	private void computeReferenceTangents() {
		switch (iMode) {
			case PEAK:
				computeReferencePeakTangents();
				break;
			case SPATCH_ROUND:
				computeReferenceSPatchRoundTangents();
				break;
		//	case AM_SMOOTH:
		//		//computeAMSmoothTangents();
		//		computeG1Tangents();
		//		break;
			default:
				computeReferenceG1Tangents();
				
		}
//		bTangentsValid = true;
	}

	private void computeTangents() {
		switch (iMode) {
			case PEAK:
				computePeakTangents();
				break;
			case SPATCH_ROUND:
				computeSPatchRoundTangents();
				break;
		//	case AM_SMOOTH:
		//		//computeAMSmoothTangents();
		//		computeG1Tangents();
		//		break;
			default:
				computeG1Tangents();
				
		}
//		bTangentsValid = true;
	}
	
	/**
	 *  Creates an empty hook Curve (a starthook on this position, and an
	 *  end hook on "next"'s position)
	 */
	public OLDControlPoint createEmptyHookCurve() {
		if (cpNext == null) {
			throw new IllegalStateException("Can't add hook to end of curve " + " on " + this);
		}
		OLDControlPoint cpStart = new OLDControlPoint();
		OLDControlPoint cpEnd = new OLDControlPoint();
		cpStart.cpParentHook = this;
		//cpStart.hookTo(this);
	//cpStart.attachTo(this);
		cpEnd.cpParentHook = cpNext;
		//cpEnd.hookTo(cpNext);
	//cpEnd.attachTo(cpNext);
		cpStart.fHookPos = 0f;
		cpEnd.fHookPos = 1f;
//		cpChildHook = cpStart;
	//cpNext.cpChildHook = cpEnd;
		cpEnd.appendTo(cpStart);
		return cpStart;
	}


	/**
	 *  Compute the normalized tangent (which is parallel to the line between the previous
	 *  and the next ControlPoint on this curve).
	 *
	 * @return    The normalizedTangent value
	 */
	private Vector3f getReferenceNormalizedTangent() {
		Vector3f v3Tangent = getReferenceTangent();
		if (v3Tangent.lengthSquared() > 0) {
			v3Tangent.normalize();
		}
		return v3Tangent;
	}

	private Vector3f getNormalizedTangent() {
		Vector3f v3Tangent = getTangent();
		if (v3Tangent.lengthSquared() > 0) {
			v3Tangent.normalize();
		}
		return v3Tangent;
	}
	
	/**
	 *  Compute the tangent (which is parallel to the line between the previous
	 *  and the next ControlPoint on this curve).
	 *
	 * @return    The tangent value
	 */
	private Vector3f getReferenceTangent() {
		Point3f p3NextPos;
		Point3f p3PrevPos;
		Vector3f v3Tangent;

		if (cpNext != null) {
			p3NextPos = cpNext.getReferencePosition();
		} else {
			p3NextPos = getReferencePosition();
		}
		if (cpPrev != null) {
			p3PrevPos = cpPrev.getReferencePosition();
		} else {
			p3PrevPos = getReferencePosition();
		}

		v3Tangent = new Vector3f(p3NextPos);
		v3Tangent.sub(p3PrevPos);
		return v3Tangent;
	}

	private Vector3f getTangent() {
		Point3f p3NextPos;
		Point3f p3PrevPos;
		Vector3f v3Tangent;

		if (cpNext != null) {
			p3NextPos = cpNext.getPosition();
		} else {
			p3NextPos = getPosition();
		}
		if (cpPrev != null) {
			p3PrevPos = cpPrev.getPosition();
		} else {
			p3PrevPos = getPosition();
		}

		v3Tangent = new Vector3f(p3NextPos);
		v3Tangent.sub(p3PrevPos);
		return v3Tangent;
	}
	
	/**
	 *  Assuming that this is a target-hook, compute the tangent by interpolating
	 *  between the tangents of the next and previous controlPoints that points
	 *  approximately in the direction specified.
	 *
	 * @param  v3Direction  The direction
	 * @return              The targetHookTangent value
	 */
	private Vector3f getReferenceTargetHookTangent(Vector3f v3Direction) {
		Vector3f v3Start = new Vector3f();
		Vector3f v3End = new Vector3f();
		computeReferenceTargetHookBorderTangents(v3Direction, v3Start, v3End);
		v3Start.interpolate(v3End, getHead().fHookPos);
		return v3Start;
	}

	private Vector3f getTargetHookTangent(Vector3f v3Direction) {
		Vector3f v3Start = new Vector3f();
		Vector3f v3End = new Vector3f();
		computeTargetHookBorderTangents(v3Direction, v3Start, v3End);
		v3Start.interpolate(v3End, getHead().fHookPos);
		return v3Start;
	}
	
	///**
	// *  Assuming that this is a target-hook, compute the tangent by interpolating
	// *  between the tangents of the next and previous controlPoints that points
	// *  approximately in the direction specified.
	// *
	// * @param  v3Direction  The direction
	// * @return              The targetHookTangent value
	// */
	public void computeTargetHookBorderTangents(Vector3f v3Direction, Vector3f v3Start, Vector3f v3End) {
		Vector3f v3Test = new Vector3f(v3Direction);
		//Vector3f v3Start = new Vector3f(v3Direction);
		//Vector3f v3End = new Vector3f();
		Point3f p3ReferenceStartPosition;
		Point3f p3ReferenceEndPosition;
		Point3f p3StartPosition;
		Point3f p3EndPosition;
		OLDControlPoint cpStart;
		OLDControlPoint cpEnd;
		float fMinAngle;
		float fAngle = 0;
		cpStart = getHead().getStart().cpParentHook.getHead();
		cpEnd = getHead().getEnd().cpParentHook.getHead();
		p3ReferenceStartPosition = cpStart.getReferencePosition();
		p3ReferenceEndPosition = cpEnd.getReferencePosition();
		p3StartPosition = cpStart.getPosition();
		p3EndPosition = cpEnd.getPosition();
		fMinAngle = (float) Math.PI;
		OLDControlPoint parentHook = getHead().getStart().cpParentHook;
		while (cpStart != null) {
			if (cpStart != parentHook) {
				if (cpStart.cpNext != null) {
					v3Test.sub(cpStart.getReferenceOutTangent(), p3ReferenceStartPosition);
					fAngle = v3Test.angle(v3Direction);
					if (fAngle < fMinAngle) {
						fMinAngle = fAngle;
						v3Start.sub(cpStart.getOutTangent(), p3StartPosition);
					}
				}
				if (cpStart.cpPrev != null) {
					v3Test.sub(cpStart.getReferenceInTangent(), p3ReferenceStartPosition);
					fAngle = v3Test.angle(v3Direction);
					if (fAngle < fMinAngle) {
						fMinAngle = fAngle;
						v3Start.sub(cpStart.getInTangent(), p3StartPosition);
					}
				}
			}
			cpStart = cpStart.cpPrevAttached;
		}
		parentHook = getHead().getEnd().cpParentHook;
		fMinAngle = (float) Math.PI;
		while (cpEnd != null) {
			if (cpEnd != parentHook) {
				if (cpEnd.cpNext != null) {
					v3Test.sub(cpEnd.getReferenceOutTangent(), p3ReferenceEndPosition);
					fAngle = v3Test.angle(v3Direction);
					if (fAngle < fMinAngle) {
						fMinAngle = fAngle;
						v3End.sub(cpEnd.getOutTangent(), p3EndPosition);
					}
				}
				if (cpEnd.cpPrev != null) {
					v3Test.sub(cpEnd.getReferenceInTangent(), p3ReferenceEndPosition);
					fAngle = v3Test.angle(v3Direction);
					if (fAngle < fMinAngle) {
						fMinAngle = fAngle;
						v3End.sub(cpEnd.getInTangent(), p3EndPosition);
					}
				}
			}
			cpEnd = cpEnd.cpPrevAttached;
		}
		/*
		 *  v3Start.normalize();
		 *  v3Start.mul(v3Direction.length() *
		 *  p3TargetHookTangent = new Point3f(getPosition());
		 *  p3Targgetloos
		 *  getLoetHookTangent.add(v3Start);
		 *  return p3TargetHookTangent;
		 */
	}
	
	public void computeReferenceTargetHookBorderTangents(Vector3f v3Direction, Vector3f v3Start, Vector3f v3End) {
		Vector3f v3Test = new Vector3f(v3Direction);
		//Vector3f v3Start = new Vector3f(v3Direction);
		//Vector3f v3End = new Vector3f();
		Point3f p3StartPosition;
		Point3f p3EndPosition;
		OLDControlPoint cpStart;
		OLDControlPoint cpEnd;
		float fMinAngle;
		float fAngle = 0;
		cpStart = getHead().getStart().cpParentHook.getHead();
		cpEnd = getHead().getEnd().cpParentHook.getHead();
		p3StartPosition = cpStart.getReferencePosition();
		p3EndPosition = cpEnd.getReferencePosition();
		fMinAngle = (float) Math.PI;
		OLDControlPoint parentHook = getHead().getStart().cpParentHook;
		while (cpStart != null) {
			if (cpStart != parentHook) {
				if (cpStart.cpNext != null) {
					v3Test.sub(cpStart.getReferenceOutTangent(), p3StartPosition);
					fAngle = v3Test.angle(v3Direction);
					if (fAngle < fMinAngle) {
						fMinAngle = fAngle;
						v3Start.set(v3Test);
					}
				}
				if (cpStart.cpPrev != null) {
					v3Test.sub(cpStart.getReferenceInTangent(), p3StartPosition);
					fAngle = v3Test.angle(v3Direction);
					if (fAngle < fMinAngle) {
						fMinAngle = fAngle;
						v3Start.set(v3Test);
					}
				}
			}
			cpStart = cpStart.cpPrevAttached;
		}
		fMinAngle = (float) Math.PI;
		while (cpEnd != null) {
			if (cpEnd != parentHook) {
				if (cpEnd.cpNext != null) {
					v3Test.sub(cpEnd.getReferenceOutTangent(), p3EndPosition);
					fAngle = v3Test.angle(v3Direction);
					if (fAngle < fMinAngle) {
						fMinAngle = fAngle;
						v3End.set(v3Test);
					}
				}
				if (cpEnd.cpPrev != null) {
					v3Test.sub(cpEnd.getReferenceInTangent(), p3EndPosition);
					fAngle = v3Test.angle(v3Direction);
					if (fAngle < fMinAngle) {
						fMinAngle = fAngle;
						v3End.set(v3Test);
					}
				}
			}
			cpEnd = cpEnd.cpPrevAttached;
		}
		/*
		 *  v3Start.normalize();
		 *  v3Start.mul(v3Direction.length() *
		 *  p3TargetHookTangent = new Point3f(getPosition());
		 *  p3Targgetloos
		 *  getLoetHookTangent.add(v3Start);
		 *  return p3TargetHookTangent;
		 */
	}
	
//	/**
//	 *  Invalidates the tangents of this and ALL neighbor ControlPoints
//	 */
//	public void invalidateTangents() {
//		bTangentsValid = false;
//		if (cpNext != null) {
//			cpNext.bTangentsValid = false;
//			if (cpNext.cpNext != null) {
//				cpNext.cpNext.bTangentsValid = false;
//			}
//		}
//		if (cpPrev != null) {
//			cpPrev.bTangentsValid = false;
//			if (cpPrev.cpPrev != null) {
//				cpPrev.cpPrev.bTangentsValid = false;
//			}
//		}
//		if (cpPrevAttached != null) {
//			cpPrevAttached.invalidateTangents();
//		}
//		//reTriangulizePatches();
//		//bCurveSegmentValid = false;
//	}
	
	/**
	 *
	 */
	public StringBuffer xml(String prefix) {
		StringBuffer sb = new StringBuffer();
		sb.append(prefix).append("<cp id=\"").append(id).append("\"");
		//if (cpNextAttached == null) {
		//	//
		//	//
		//	// remove !!!!!!!!!!!!!!!!!!
		//	//
		//	//
		//	fixPosition();
		//	//
		//	//
		//	//
		//	sb.append(" x=").append(XMLutils.quote(p3ReferencePosition.x));
		//	sb.append(" y=").append(XMLutils.quote(p3ReferencePosition.y));
		//	sb.append(" z=").append(XMLutils.quote(p3ReferencePosition.z));
		//} else if (cpNextAttached != null) {
		//	if (cpNextAttached.fHookPos < 0) {
		//		int attach = ((Integer)mapCp.get(cpNextAttached)).intValue();
		//		sb.append(" attach=").append(XMLutils.quote(attach));
		//	} else {
		//		int hook = ((Integer)mapCp.get(cpNextAttached.getStart().cpParentHook)).intValue();
		//		sb.append(" hook=").append(XMLutils.quote(hook)).append(" hookpos=").append(XMLutils.quote(cpNextAttached.fHookPos));
		//	}
		//}
		if (cpNextAttached != null) {
			sb.append(" attach=\"").append(cpNextAttached.id).append("\"");
		} else if (cpParentHook != null) {
			sb.append(" hook=\"").append(cpParentHook.id).append("\"");
			sb.append(" hookpos=\"").append(fHookPos).append("\"");
		} else if (fHookPos > 0 && fHookPos < 1) {
			sb.append(" hookpos=\"").append(fHookPos).append("\"");
		} else {
			//
			// remove !!!!!!!!!!!!!!!!!!
			//
			//fixPosition();
			//
			sb.append(" x=\"").append(p3ReferencePosition.x).append("\"");
			sb.append(" y=\"").append(p3ReferencePosition.y).append("\"");
			sb.append(" z=\"").append(p3ReferencePosition.z).append("\"");
			//if (cpChildHook != null) {
			//	int chook = ((Integer)mapCp.get(cpChildHook)).intValue();
			//	sb.append(" chook=").append(XMLutils.quote(chook));
			//}
		}
		if (iMode == PEAK) {
			sb.append(" mode=\"peak\"");
		} else if (iMode == SPATCH_ROUND) {
			sb.append(" mode=\"spatch\"");
		}
		if (fInMagnitude != fDefaultMagnitude) {
			sb.append(" magnitude=\"").append(fInMagnitude).append("\"");
		}
		if (bone != null) {
			sb.append(" bone=\"" + bone.getName() + "\" parent=\"" + bParentBone + "\"");// pos=\"" + fBonePosition + "\" dist=\"" + fDistanceToLine + "\"");
		}
		sb.append("/>").append("\n");
		return sb;
	}
	
	public OLDControlPoint trueHead() {
		return (getParentHook() == null) ? getHead() : getParentHook().getHead();
	}
	
	public OLDControlPoint trueCp() {
		return (cpParentHook != null) ? cpParentHook : this;
	}

	public Matrix4f getInvTransform() {
		return m4InvTransform;
	}

	public Matrix4f getTransform() {
		return m4Transform;
	}
	
//	public Matrix4f getInvBoneTransform() {
//		return m4InvBoneTransform;
//	}

//	public Matrix4f getBoneTransform() {
//		return m4BoneTransform;
//	}
	
	//public Point3f() getTwist() {
	//	if (cpPrevAttached == null || cpPrevAttached.cpPrevAttached != null) {
	//		
	//		/* return zero twist if whe haven't got exactly two splines crossing in this point */
	//		return new Point3f();
	//	} else {
	//		ControlPoint A = cpPrev;
	//		ControlPoint B = cpNext;
	//		ControlPoint C = cpPrevAttached.cpPrev;
	//		ControlPoint 
}
