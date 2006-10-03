package jpatch.entity;

import java.io.IOException;
import java.io.PrintStream;

import jpatch.auxilary.*;
import javax.vecmath.*;

/**
 * A ControlPoint is a point on a curve.
 * @author sascha
 */
public class ControlPoint extends AbstractJPatchXObject {
	public static enum TangentMode { DEFAULT, SPATCH };
	
	public Attribute.Tuple3 referencePosition = new Attribute.Tuple3(null, 0, 0, 0, false);
	public Attribute.Tuple3 position = new Attribute.Tuple3(null, 0, 0, 0, false);
	public Attribute.Double magnitude = new Attribute.Double(1);
	public Attribute.Enum<TangentMode> tangentMode = new Attribute.Enum<TangentMode>(TangentMode.DEFAULT);
	public Attribute.Double hookPos = new Attribute.Double();
	
	private Matrix4d transform = new Matrix4d(Constants.IDENTITY_MATRIX);
	private Matrix4d invTransform = new Matrix4d(Constants.IDENTITY_MATRIX);
	private boolean inverseInvalid = false;
	
	private Point3d pos = new Point3d();
	private Point3d inTangent = new Point3d();
	private Point3d outTangent = new Point3d();
	private Point3d refPos = new Point3d();
	private Point3d refInTangent = new Point3d();
	private Point3d refOutTangent = new Point3d();
	
	private ControlPoint nextCp;
	private ControlPoint prevCp;
	private ControlPoint nextAttachedCp;
	private ControlPoint prevAttachedCp;
	private boolean loop;
	private final Model model;
	private int id;
	
	/* * * * * * * * 
	 * Constructors
	 * * * * * * * */
	
	/**
	 * @param model
	 */
	public ControlPoint(Model model) {
		this.model = model;
		id = model.getNextCpId();
		position.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
//				System.out.println(ControlPoint.this + " position changed");
				position.get(pos);
				refPos.set(pos);
				if (inverseInvalid) {
					computeInverseTransform();
				}
				invTransform.transform(refPos);
				referencePosition.setValueAdjusting(true);
				referencePosition.set(refPos);
				referencePosition.setValueAdjusting(false);
			}
		});
		referencePosition.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				referencePosition.get(refPos);
				pos.set(refPos);
				transform.transform(pos);
				position.setValueAdjusting(true);
				position.set(pos);
				position.setValueAdjusting(false);
			}
		});
	}
	
	/* * * * * * * * * * * * * * * * * * * * *
	 * JPatchObject interface implementation
	 * * * * * * * * * * * * * * * * * * * * */
	
	public String getName() {
		return "cp" + id;
	}

	public void setParent(JPatchObject parent) {
		// TODO Auto-generated method stub
	}

	public ObjectRegistry getObjectRegistry() {
		throw new UnsupportedOperationException();
	}
	
	/* * * * * * * * * * * *
	 * Getters and setters
	 * * * * * * * * * * * */
	
	/**
	 * Gets the id of this ControlPoint
	 * @return the id of this ControlPoint
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Sets the id of this ControlPoint
	 * param id the id of this ControlPoint
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Gets the next attached ControlPoint.
	 * @return the next attached ControlPoint
	 */
	public ControlPoint getNextAttached() {
		return nextAttachedCp;
	}
	
	/**
	 * Sets the next attached ControlPoint.
	 * @param nextAttached the next attached ControlPoint
	 */
	public void setNextAttached(ControlPoint nextAttached) {
		this.nextAttachedCp = nextAttached;
	}
	
	/**
	 * Gets the previous attached ControlPoint.
	 * @return the previous attached ControlPoint
	 */
	public ControlPoint getPrevAttached() {
		return prevAttachedCp;
	}
	
	/**
	 * Sets the previous attached ControlPoint.
	 * @param prevAttached the previous attached ControlPoint
	 */
	public void setPrevAttached(ControlPoint prevAttached) {
		this.prevAttachedCp = prevAttached;
	}
	
	/**
	 * Gets the next ControlPoint.
	 * @return the next  ControlPoint
	 */
	public ControlPoint getNext() {
		return nextCp;
	}
	
	/**
	 * Sets the next ControlPoint.
	 * @param next the next ControlPoint
	 */
	public void setNext(ControlPoint next) {
		this.nextCp = next;
	}
	
	/**
	 * Gets the previous ControlPoint.
	 * @return the previous ControlPoint
	 */
	public ControlPoint getPrev() {
		return prevCp;
	}
	
	/**
	 * Sets the previous  ControlPoint.
	 * @param prev the previous ControlPoint
	 */
	public void setPrev(ControlPoint prev) {
		this.prevCp = prev;
	}
	
	/**
	 * Gets the loop flag. The loop flag must be set on the first ControlPoint of a
	 * looped curve and must be cleared on all other ControlPoints.
	 * @return the loop flag
	 */
	public boolean isLoop() {
		return loop;
	}
	
	/**
	 * Sets the loop flag to the passed boolean value. The loop flag must be set on the
	 * first ControlPoint of a looped curve and must be cleared on all other ControlPoints.
	 * @param loop the loop flag
	 */
	public void setLoop(boolean loop) {
		this.loop = loop;
	}
	
	/**
	 * Gets the model this ControlPoint belongs to.
	 * @return the model this ControlPoint belongs to
	 */
	public Model getModel() {
		return model;
	}
	
	/* * * * * * * * *
	 * Other methods
	 * * * * * * * * */
	
	@Override
	public String toString() {
		return getName();
	}
	
	/**
	 * Transforms this controlpoint using the specified transformation matrix
	 * @param matrix The transformation matrix to use.
	 */
	public void transform(Matrix4d matrix) {
		matrix.transform(pos);
		position.set(pos);
		if (inverseInvalid) {
			computeInverseTransform();
		}
		invTransform.transform(refPos);
		referencePosition.set(refPos);
	}
	
	/**
	 * Returns the start of this curve. The start of a curve is either the ControlPoint which has
	 * no previous ControlPoint, or (in case of a looped curve) the ControlPoint with the loop
	 * flag set.
	 * @returns the first ControlPoint of this curve
	 */
	public ControlPoint getStart() {
		if (isStart()) {
			return this;
		} else {
			return prevCp.getStart();
		}
	}
	
	/**
	 * Returns the end of this curve. The end of a curve is either the ControlPoint which has
	 * no next ControlPoint, or (in case of a looped curve) the ControlPoint whichs next ControlPoint
	 * has the loop flag set.
	 * @returns the last ControlPoint of this curve
	 */
	public ControlPoint getEnd() {
		if (isEnd()) {
			return this;
		} else {
			return nextCp.getEnd();
		}
	}
	
	/**
	 * Returns the length of this curve. Must be called on a ControlPoint that is the start of a curve.
	 * @return the length of this curve.
	 */
	public int getCurveLength() {
		assert isStart() : this + " is not the start of a curve";
		int i = 1;
		for (ControlPoint cp = nextCp; cp != null && !cp.loop; cp = cp.nextCp) {
			i++;
		}
		return i;
	}
	
	/**
	 * Returns the next ControlPoint that is not a hook.
	 * @return the next ControlPoint that is not a hook
	 */
	public ControlPoint getNextNonHook() {
		if (nextCp == null) {
			return null;
		}
		ControlPoint cp = nextCp;
		while (cp.nextCp != null && cp.isHook()) {
			cp = cp.nextCp;
		}
		return cp;
	}
	
	/**
	 * Returns the previous ControlPoint that is not a hook.
	 * @return the previous ControlPoint that is not a hook
	 */
	public ControlPoint getPrevNonHook() {
		if (prevCp == null) {
			return null;
		}
		ControlPoint cp = prevCp;
		while (cp.prevCp != null && cp.isHook()) {
			cp = cp.prevCp;
		}
		return cp;
	}
	
	/**
	 * Returns the head for this ControlPoint. The head is the first ControlPoint
	 * on a chain of attached ControlPoints. It is found by recursively calling
	 * getHead() on the prevAttachedCp until prevAttachedCp is null.
	 * @return the head for this ControlPoint.
	 */
	public ControlPoint getHead() {
		if (isHead()) {
			return this;
		} else {
			return prevAttachedCp.getHead();
		}
	}
	
	/**
	 * Returns the tail for this ControlPoint. The tail is the last ControlPoint
	 * on a chain of attached ControlPoints. It is found by recursively calling
	 * getTail() on the nextAttachedCp until nextAttachedCp is null.
	 * @return the tail for this ControlPoint.
	 */
	public ControlPoint getTail() {
		if (isTail()) {
			return this;
		} else {
			return nextAttachedCp.getTail();
		}
	}
	
	/**
	 * Sets the passed Bezier control vertices to the values for this curve segment
	 * (the segment between this and the next controlpoint) in local-space.
	 * This method assumes that all tangents have already been computed
	 * (including the tangents of the next controlpoint).
	 * It must not be called on the end of an open curve (i.e. nextCp must not be null) and
	 * will throw a NullPointerException otherwise.
	 * To draw a curve, set the path to the position of the first ControlPoint, then
	 * add cubic curve segments until the end of the curve (or the start in case of a
	 * looped curve) is reached. Be careful to check for loops (cp != start) to prevent
	 * looping forever!
	 * Pseudocode to draw a curve:
	 * <code>
	 *    ControlPoint start (initialized to the first point of the curve)
	 *    Point3d p1, p2, p3 (initialized!)
	 *    getPathSegmentCV(p1, p2, p3);
	 *    path.moveTo(start's position);
	 *    path.curveTo(p1, p2, p3);        // draw the first segment
	 *    for (ControlPoint cp = start.getNextNonHook(); cp != null && !cp.isLoop(); cp = cp.getNextNonHook()) {
	 *        path.curveTo(p1, p2, p3);    // draw all subsequent segments
	 *    }
	 * </code> 
	 * 
	 * @param p1 the second Bezier control vertex
	 * @param p2 the third Bezier control vertex
	 * @param p3 the forth Bezier control vertex
	 */
	public boolean getPathSegmentCVs(Tuple3d p1, Tuple3d p2, Tuple3d p3) {
		ControlPoint nextNonHook = getNextNonHook();
		if (nextNonHook == null) {
			return false;
		}
		p1.set(outTangent);
		p2.set(nextNonHook.inTangent);
		p3.set(nextNonHook.getHead().pos);
		return true;
	}
	
	/**
	 * Sets the passed Bezier control vertices to the values for this curve segment
	 * (the segment between this and the next controlpoint) in local-space.
	 * This method assumes that all tangents have already been computed
	 * (including the tangents of the next controlpoint).
	 * It must not be called on the end of an open curve (i.e. nextCp must not be null) and
	 * will throw a NullPointerException otherwise.
	 * To draw a curve, set the path to the position of the first ControlPoint, then
	 * add cubic curve segments until the end of the curve (or the start in case of a
	 * looped curve) is reached. Be careful to check for loops (cp != start) to prevent
	 * looping forever!
	 * Pseudocode to draw a curve:
	 * <code>
	 *    ControlPoint start (initialized to the first point of the curve)
	 *    Point3d p1, p2, p3 (initialized!)
	 *    getPathSegmentCV(p1, p2, p3);
	 *    path.moveTo(start's position);
	 *    path.curveTo(p1, p2, p3);        // draw the first segment
	 *    for (ControlPoint cp = start.getNextNonHook(); cp != null && !cp.isLoop(); cp = getNextNonHook()) {
	 *        path.curveTo(p1, p2, p3);    // draw all subsequent segments
	 *    }
	 * </code> 
	 * 
	 * @param p1 the second Bezier control vertex
	 * @param p2 the third Bezier control vertex
	 * @param p3 the forth Bezier control vertex
	 */
	public void getPathSegmentCVs(Tuple3f p1, Tuple3f p2, Tuple3f p3) {
		ControlPoint nextNonHook = getNextNonHook();
		p1.set(outTangent);
		p2.set(nextNonHook.inTangent);
		p3.set(nextNonHook.getHead().pos);
	}
	
	/**
	 * Sets the passed point to the local-space position of this ControlPoint
	 * or it's head if it is an attached ControlPoint.
	 * If this ControlPoint is a hook, computeHookPosition(false) must be
	 * called first.
	 * @param p The point to set
	 */
	public void getPos(Tuple3d p) {
		p.set(getHead().pos);
	}
	
	/**
	 * Sets the passed point to the local-space position of this ControlPoint
	 * or it's head if it is an attached ControlPoint.
	 * If this ControlPoint is a hook, computeHookPosition(false) must be
	 * called first.
	 * @param p The point to set
	 */
	public void getPos(Tuple3f p) {
		p.set(getHead().pos);
	}
	
	/**
	 * Sets the passed point to the local-space position of this inTangent.
	 * computeTangents(false) must be called first.
	 * @param p The point to set
	 */
	public void getInTangent(Tuple3d p) {
		p.set(inTangent);
	}
	
	/**
	 * Sets the passed point to the local-space position of this inTangent.
	 * computeTangents(false) must be called first.
	 * @param p The point to set
	 */
	public void getInTangent(Tuple3f p) {
		p.set(inTangent);
	}
	
	/**
	 * Sets the passed point to the local-space position of this outTangent.
	 * computeTangents(false) must be called first.
	 * @param p The point to set
	 */
	public void getOutTangent(Tuple3d p) {
		p.set(outTangent);
	}
	
	/**
	 * Sets the passed point to the local-space position of this outTangent.
	 * computeTangents(false) must be called first.
	 * @param p The point to set
	 */
	public void getOutTangent(Tuple3f p) {
		p.set(outTangent);
	}
	
	/**
	 * Sets the passed point to the reference position of this ControlPoint
	 * or it's head if it is an attached ControlPoint.
	 * If this ControlPoint is a hook, computeHookPosition(true) must have been
	 * called first.
	 * @param p The point to set
	 */
	public void getRefPos(Tuple3d p) {
		p.set(getHead().refPos);
	}
	
	/**
	 * Sets the passed point to the reference position of this ControlPoint
	 * or it's head if it is an attached ControlPoint.
	 * If this ControlPoint is a hook, computeHookPosition(true) must have been
	 * called first.
	 * @param p The point to set
	 */
	public void getRefPos(Tuple3f p) {
		p.set(getHead().refPos);
	}
	
	/**
	 * Sets the passed point to the reference position of this inTangent.
	 * computeTangents(true) must be called first.
	 * @param p The point to set
	 */
	public void getRefInTangent(Tuple3d p) {
		p.set(refInTangent);
	}
	
	/**
	 * Sets the passed point to the reference position of this inTangent.
	 * computeTangents(true) must be called first.
	 * @param p The point to set
	 */
	public void getRefInTangent(Tuple3f p) {
		p.set(refInTangent);
	}
	
	/**
	 * Sets the passed point to the reference position of this outTangent.
	 * computeTangents(true) must be called first.
	 * @param p The point to set
	 */
	public void getRefOutTangent(Tuple3d p) {
		p.set(refOutTangent);
	}
	
	/**
	 * Sets the passed point to the reference position of this outTangent.
	 * computeTangents(true) must be called first.
	 * @param p The point to set
	 */
	public void getRefOutTangent(Tuple3f p) {
		p.set(refOutTangent);
	}
	
	/**
	 * Computes the (reference) tangents.
	 * This method must be called <b>after</b> the positions (and hook position) of all ControlPoints of the
	 * same model have been computed.
	 * @param reference true to compute the reference tangents, false to compute the local-space tangents.
	 */
	public void computeTangents(boolean reference) {
		if (prevAttachedCp != null && prevAttachedCp.isHook()) {
			computeHookTangent(reference);		// this ControlPoint is attached to a hook, compute hook tangents
		} else {
			if (reference) {
				computeTangents(				// compute standard (non hook) reference tangents
						prevCp == null ? null : prevCp.getHead().refPos,
						getHead().refPos,
						nextCp == null ? null : nextCp.getHead().refPos,
						refInTangent,
						refOutTangent
				);
			} else {
				computeTangents(				// compute standard (non hook) tangents
						prevCp == null ? null : prevCp.getHead().pos,
						getHead().pos,
						nextCp == null ? null : nextCp.getHead().pos,
						inTangent,
						outTangent
				);
			}
		}
	}
	
	/**
	 * Reverses this curve and returns the new start of the curve. It must be called on the first ControlPoint
	 * of the curve and throws an IllegalStateException otherwise.
	 * It reverses the order of the curve (the first point will be the last afterwards), corrects
	 * hookPos (if it is not -1, hookPos will be changed to 1 - hookPos).
	 * If the curve is looped, the start ControlPoint will not change! Otherwise
	 * this method will also remove the old curve from the model and add it (with the new start
	 * ControlPoint) again.
	 * Note that this is an atomic operation that can be undone by calling reverseCurve() again (on the
	 * new curve start!). Therefore it is implemented here instead of a JPatchUndoableEdit. This method is
	 * intended to be used by JPatchUndoableEdits.
	 * @return the new start of the curve (which is this ControlPoint if the curve is looped)
	 */
	public ControlPoint reverseCurve() {
		if (!isStart()) {
			throw new IllegalStateException("reverseCurve must be called on the first point of a curve");
		}
		ControlPoint cp = this;
		ControlPoint start;
		do {
			ControlPoint tmp = cp.nextCp;				// swap 
			cp.nextCp = cp.prevCp;						// nextCp and
			cp.prevCp = tmp;							// prevCp
			if (cp.isHook()) {
				cp.hookPos.set(1 - cp.hookPos.get());	// correct hookPos if neccessary
			}
			start = cp;		// at the end of this loop "start" will be the new start of this curve
			cp = cp.prevCp;
			/* 
			 * This loop actually runs forward (from the start of the curve over all "nextCp's" until
			 * the end of the curve). prevCp is used because nextCp and prevCp have been swapped inside
			 * the loop block.
			 */
		} while (cp != null && !cp.loop);
		if (!loop) {
			model.removeCurve(this);	// temporarily remove this curve from the model and
			model.addCurve(start);		// add this curve to the model again (with it's new start)
			return start;
		}
		return this;
	}
	
	/**
	 * Tests if this ControlPoint is the start of a curve
	 * @return true if this ControlPoint is the stat of a curve, false otherwise
	 */
	public boolean isStart() {
		return prevCp == null || loop;
	}
	
	/**
	 * Tests if this ControlPoint is the end of a curve
	 * @return true if this ControlPoint is the end of a curve, false otherwise
	 */
	public boolean isEnd() {
		return nextCp == null || nextCp.loop;
	}
	
	/**
	 * Tests if this ControlPoint is a head (the first in a series of attached ControlPoints)
	 * @return true if this ControlPoint is a head (the first in a series of attached ControlPoints), false otherwise
	 */
	public boolean isHead() {
		return prevAttachedCp == null;
	}
	
	/**
	 * Tests if this ControlPoint is a tail (the last in a series of attached ControlPoints)
	 * @return true if this ControlPoint is a tail (the last in a series of attached ControlPoints), false otherwise
	 */
	public boolean isTail() {
		return nextAttachedCp == null;
	}
	
	/**
	 * Tests if this ControlPoint is a unattached (has no attached ControlPoints and is not attached to a ControlPoint)
	 * @return true if this ControlPoint is unattached, false otherwise
	 */
	public boolean isUnattached() {
		return isHead() && isTail();
	}
	
	/**
	 * Tests if this ControlPoint is a hook
	 * @return true if this ControlPoint is a hook, false otherwise
	 */
	public boolean isHook() {
		return hookPos.get() > 0;
	}
	
	/**
	 * Resets the ControlPoints internal transformation matrix (i.e. sets it to the identity matrix)
	 */
	public void resetTransform() {
		transform.setIdentity();
		invTransform.setIdentity();
		inverseInvalid = false;
	}
	
	/**
	 * Adds a morph vector to this ControlPoint's transfromation matrix.
	 * @param vector the vector to add.
	 */
	public void addMorphVector(Tuple3d vector) {
		transform.m03 += vector.x;
		transform.m13 += vector.y;
		transform.m23 += vector.z;
		inverseInvalid = true;
	}
	
	/**
	 * Applies this ControlPoints internal transformation matrix its position.
	 */
	public void applyTransform() {
		pos.set(refPos);
		transform.transform(pos);
		position.set(pos);
	}
	
	/**
	 * Prints an xml representation of a curve to the provided PrintStream.
	 * It must be called on the first ControlPoint of the curve (it
	 * throws an IllegalStateException otherwise).
	 * @param out the PrintStream to print the xml representation to
	 * @param indent the indentation
	 * @throws IOException 
	 */
	public void writeXml(XmlWriter xmlWriter) throws IOException {
		assert isStart() : this + " is not the start of a curve.";
		
		xmlWriter.startElement("curve");
		if (loop) {
			xmlWriter.attribute("loop", true);
		}
		for (ControlPoint cp = nextCp; cp != null && !cp.loop; cp = cp.nextCp) {
			cp.xmlCp(xmlWriter);
		}
		xmlWriter.endElement();
	}
	
	/**
	 * Computes the (reference) position for this hook.
	 * This method must be called on all hooks (of the same model) after the positions of all other
	 * ControlPoints (of the same model) have been computed. 
	 * @param reference true to set the reference position, falso to set the local-space position.
	 */
	public void computeHookPosition(boolean reference) {
		assert isHook() : "cp " + this + " is not a hook.";
		ControlPoint start = getPrevNonHook();
		ControlPoint end = getNextNonHook();
		double t = hookPos.get();
		double t_2 = t * t;			
		double t1 = 1 - t;
		double t1_2 = t1 * t1;
		double b0 = t1 * t1_2;		// set up
		double b1 = 3 * t1_2 * t;	// bernstein
		double b2 = 3 * t1 + t_2;	// coefficients
		double b3 = t * t_2;		// for bezier curve
		if (reference) {
			Point3d startPos = start.getHead().refPos;
			Point3d endPos = end.getHead().refPos;
			referencePosition.set(
					startPos.x * b0 + start.refOutTangent.x * b1 + end.refInTangent.x * b2 + endPos.x * b3,
					startPos.y * b0 + start.refOutTangent.y * b1 + end.refInTangent.x * b2 + endPos.y * b3,
					startPos.z * b0 + start.refOutTangent.z * b1 + end.refInTangent.x * b2 + endPos.z * b3
			);
		} else {
			Point3d startPos = start.getHead().pos;
			Point3d endPos = end.getHead().pos;
			position.set(
					startPos.x * b0 + start.outTangent.x * b1 + end.inTangent.x * b2 + endPos.x * b3,
					startPos.y * b0 + start.outTangent.y * b1 + end.inTangent.x * b2 + endPos.y * b3,
					startPos.z * b0 + start.outTangent.z * b1 + end.inTangent.x * b2 + endPos.z * b3
			);
		}
	}
	
	/**
	 * Computes the inverse transformation matrix and clears
	 * the inverseInvalid flag.
	 */
	private void computeInverseTransform() {
		invTransform.invert(transform);
		inverseInvalid = false;
	}
	
	/**
	 * Computes the tangent (in or out) of a hook
	 * @param reference true to compute the reference tangent, falso to compute the local tangent
	 */
	private void computeHookTangent(boolean reference) {
		assert nextCp == null && prevCp == null : "hooked " + this + " must have exactly one neighbor, but has no neighbors";
		assert nextCp != null && prevCp != null : "hooked " + this + " must have exactly one neighbor, but has two neighbors";
		assert prevAttachedCp != null : "hooked " + this + " is not attached.";
		assert prevAttachedCp.isHook() : "hooked " + this + " is not attached to a hook.";
		assert prevAttachedCp.isHead() : "hooked " + this + " attached hook is not a head.";
		assert nextAttachedCp == null : "hooked " + this + " has attached points.";
		
		/*
		 * set matchX,matchY,matchZ to be the vector from this (reference)position to the next or prev (reference)position.
		 */
		double matchX, matchY, matchZ;
		if (reference) {
			if (nextCp != null) {
				matchX = nextCp.refPos.x - refPos.x;
				matchY = nextCp.refPos.y - refPos.y;
				matchZ = nextCp.refPos.z - refPos.z;
			} else {
				matchX = prevCp.refPos.x - refPos.x;
				matchY = prevCp.refPos.y - refPos.y;
				matchZ = prevCp.refPos.z - refPos.z;
			}
		} else {
			if (nextCp != null) {
				matchX = nextCp.pos.x - pos.x;
				matchY = nextCp.pos.y - pos.y;
				matchZ = nextCp.pos.z - pos.z;
			} else {
				matchX = prevCp.pos.x - pos.x;
				matchY = prevCp.pos.y - pos.y;
				matchZ = prevCp.pos.z - pos.z;
			}
		}
		double len = Math.sqrt(matchX * matchX + matchY * matchY + matchZ * matchZ);
		matchX /= len;
		matchY /= len;
		matchZ /= len;						// normalize matchX,matchY,matchZ
		
		ControlPoint startCp = prevAttachedCp.getPrevNonHook();
		ControlPoint endCp = prevAttachedCp.getNextNonHook();
		double startX = 0, startY = 0, startZ = 0;
		double endX = 0, endY = 0, endZ = 0;
		double testX, testY, testZ;
		double error, dot;
		
		/*
		 * Find the best start tangent
		 */
		error = Double.MAX_VALUE;
		for (ControlPoint start = startCp.getHead(); start != null; start = start.getNextAttached()) {
			if (start.nextCp != null && start.nextCp != endCp) {
				if (reference) {
					testX = start.nextCp.refPos.x - start.refPos.x;
					testY = start.nextCp.refPos.y - start.refPos.y;
					testZ = start.nextCp.refPos.z - start.refPos.z;
				} else {
					testX = start.nextCp.pos.x - start.pos.x;
					testY = start.nextCp.pos.y - start.pos.x;
					testZ = start.nextCp.pos.z - start.pos.x;
				}
				len = Math.sqrt(testX * testX + testY * testY + testZ * testZ);
				dot = matchX * testX / len + matchY * testY / len + matchZ * testZ / len;	// dot product with norm(testX,testY,testZ)
				if (dot < error) {
					error = dot;
					startX = testX;
					startY = testY;
					startZ = testZ;
				}
			}
			if (start.prevCp != null && start.prevCp != endCp) {				
				if (reference) {
					testX = start.prevCp.refPos.x - start.refPos.x;
					testY = start.prevCp.refPos.y - start.refPos.y;
					testZ = start.prevCp.refPos.z - start.refPos.z;
				} else {
					testX = start.prevCp.pos.x - start.pos.x;
					testY = start.prevCp.pos.y - start.pos.x;
					testZ = start.prevCp.pos.z - start.pos.x;
				}
				len = Math.sqrt(testX * testX + testY * testY + testZ * testZ);
				dot = matchX * testX / len + matchY * testY / len + matchZ * testZ / len;	// dot product with norm(testX,testY,testZ)
				if (dot < error) {
					error = dot;
					startX = testX;
					startY = testY;
					startZ = testZ;
				}
			}
		}
		
		/*
		 * Find the best end tangent
		 */
		for (ControlPoint end = endCp.getHead(); end != null; end = end.getNextAttached()) {
			if (end.nextCp != null && end.nextCp != startCp) {
				if (reference) {
					testX = end.nextCp.refPos.x - end.refPos.x;
					testY = end.nextCp.refPos.y - end.refPos.y;
					testZ = end.nextCp.refPos.z - end.refPos.z;
				} else {
					testX = end.nextCp.pos.x - end.pos.x;
					testY = end.nextCp.pos.y - end.pos.x;
					testZ = end.nextCp.pos.z - end.pos.x;
				}
				len = Math.sqrt(testX * testX + testY * testY + testZ * testZ);
				dot = matchX * testX / len + matchY * testY / len + matchZ * testZ / len;	// dot product with norm(testX,testY,testZ)
				if (dot < error) {
					error = dot;
					endX = testX;
					endY = testY;
					endZ = testZ;
				}
			}
			if (end.prevCp != null && end.prevCp != startCp) {				
				if (reference) {
					testX = end.prevCp.refPos.x - end.refPos.x;
					testY = end.prevCp.refPos.y - end.refPos.y;
					testZ = end.prevCp.refPos.z - end.refPos.z;
				} else {
					testX = end.prevCp.pos.x - end.pos.x;
					testY = end.prevCp.pos.y - end.pos.x;
					testZ = end.prevCp.pos.z - end.pos.x;
				}
				len = Math.sqrt(testX * testX + testY * testY + testZ * testZ);
				dot = matchX * testX / len + matchY * testY / len + matchZ * testZ / len;	// dot product with norm(testX,testY,testZ)
				if (dot < error) {
					error = dot;
					endX = testX;
					endY = testY;
					endZ = testZ;
				}
			}
		}
		
		/*
		 * interpolate the tangents
		 */
		double t = hookPos.get();
		double t1 = 1 - t;
		if (reference) {
			if (nextCp != null) {
				refOutTangent.set(t1 * startX + t * endX, t1 * startY + t * endY, t1 * startZ + t * endZ);
			} else {
				refInTangent.set(t1 * startX + t * endX, t1 * startY + t * endY, t1 * startZ + t * endZ);
			}
		} else {
			if (nextCp != null) {
				outTangent.set(t1 * startX + t * endX, t1 * startY + t * endY, t1 * startZ + t * endZ);
			} else {
				inTangent.set(t1 * startX + t * endX, t1 * startY + t * endY, t1 * startZ + t * endZ);
			}
		}
	}
	
	/**
	 * Compute tangents
	 * @param p0 the previous point
	 * @param p1 this point
	 * @param p2 the next point
	 * @param in point to store the input tangent
	 * @param out point to store the output tangent
	 */
	private void computeTangents(Point3d p0, Point3d p1, Point3d p2, Point3d in, Point3d out) {
		switch (tangentMode.get()) {
		case DEFAULT:
			// FIXME fallthrough to spatch algorithm
		case SPATCH:
			/*
			 * sPatch tangent algorithm:
			 * p0: previous point, p1: this point, p2: next point
			 * the direction of the tangent is p2 - p0
			 * the length of the input tangent is magnitude * distance(p1 - p0) / 3
			 * the length of the output tangent is magnitude * distance(p2 - p1) / 3
			 * if the previous or the next point doesn't exist, this point is used instead
			 */
			double inLength, outLength;
			// compute outLength, set out to next point (if it exists) or to this point (otherwise)
			if (p2 == null) {
				outLength = 0;
				out.set(p1);
			} else {
				outLength = p1.distance(p2);
				out.set(p2);
			}
			// compute inLength, subtract prev point (if it exists) or this point (otherwise) from out
			if (p0 == null) {
				inLength = 0;
				out.sub(p1);
			} else {
				inLength = p1.distance(p0);
				out.sub(p0);
			}
			// compute overall length (distance prev to next)
			double len = inLength + outLength;
			// normalize out
			out.x /= len;
			out.y /= len;
			out.z /= len;	// out is now the normalized vector (tangent direction)
			in.x = -out.x;
			in.y = -out.y;
			in.z = -out.z;	// set in to point in reverse direction
			// multiply in and out tangents with magnitude and lengths
			in.scale(magnitude.get() * inLength / 3);
			out.scale(magnitude.get() * outLength / 3);
			// since we're interested in the position (not the vector), add this position (p1) to the tangents
			in.add(p1);
			out.add(p1);
			break;
		}
	}
	
	/**
	 * Prints an xml representation of this ControlPoint to the specified PrintStream.
	 * This method is private because it's only called from within the (ControlPoint) xml
	 * method, which is used to print an entire curve (und thus must be called on the
	 * start of the curve).
	 * @param xmlWriter the XmlWriter to write to
	 * @see writeXml(XmlWriter)
	 */
	private void xmlCp(XmlWriter xmlWriter) throws IOException {
		xmlWriter.startElement("cp");
		xmlWriter.attribute("id", id);
		if (isHead() && !isHook()) {
			referencePosition.writeXml(xmlWriter);
		} else {
			if (!isHead()) {
				xmlWriter.attribute("attach", prevAttachedCp.id);
			}
			if (isHook()) {
				xmlWriter.attribute("hook", hookPos);
			}
		}
		if (magnitude.get() != 1) {
			xmlWriter.attribute("mag", magnitude);
		}
		if (tangentMode.get() != TangentMode.DEFAULT) {
			xmlWriter.attribute("tangent", tangentMode);
		}
		xmlWriter.endElement();
	}
}
