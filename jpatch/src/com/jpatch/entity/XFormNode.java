package com.jpatch.entity;

import static com.jpatch.afw.vecmath.TransformUtil.LOCAL;

import java.util.List;

import javax.vecmath.*;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.control.AttributeEdit;
import com.jpatch.afw.control.JPatchUndoableEdit;
import com.jpatch.afw.vecmath.*;

public class XFormNode extends SceneGraphNode implements Transformable {
	/** enumeration of transform operations */
	private static enum XFormOp { TRANSLATION, SCALE, AXIS_ROTATION, ROTATION }
	
	/** possible orders of transform operations */
	private static final TransformOrder[] ORDERS = new TransformOrder[] {
		new TransformOrder(XFormOp.TRANSLATION, XFormOp.SCALE, XFormOp.ROTATION),
		new TransformOrder(XFormOp.TRANSLATION, XFormOp.ROTATION, XFormOp.SCALE),
		new TransformOrder(XFormOp.SCALE, XFormOp.TRANSLATION, XFormOp.ROTATION),
		new TransformOrder(XFormOp.SCALE, XFormOp.ROTATION, XFormOp.TRANSLATION),
		new TransformOrder(XFormOp.ROTATION, XFormOp.SCALE, XFormOp.TRANSLATION),
		new TransformOrder(XFormOp.ROTATION, XFormOp.TRANSLATION, XFormOp.SCALE),
	};
	
	/** transform order attribute */
	protected final StateMachine<TransformOrder> transformOrderAttr = new StateMachine<TransformOrder>(ORDERS, ORDERS[3]);
	
	/** translation vector */
	protected final Vector3d translation = new Vector3d();
	
	/** Translation attribute */
	protected final Tuple3Attr translationAttr = new Tuple3Attr(translation);
	
	/** scale vector */
	protected final Scale3d scale = new Scale3d();
	
	/** scale attribute */
	protected final Tuple3Attr scaleAttr = new Tuple3Attr(scale);
	
	/** axisRotation */
	protected final Rotation3d axisRotation = new Rotation3d();
	
	/** AxisRotation attribute */
	protected final Tuple3Attr axisRotationAttr = new Tuple3Attr(axisRotation);
	
	/** rotation */
	protected final Rotation3d rotation = new Rotation3d();
	
	/** rotation attribute */
	protected final Tuple3Attr rotationAttr = new Tuple3Attr(rotation);
	
	/** AxisRotation-order attribute */
	protected final StateMachine<Rotation3d.Order> axisRotationOrderAttr = new RotationOrderAttribute(axisRotation, axisRotationAttr);
	
	/** rotation-order attribute */
	protected final StateMachine<Rotation3d.Order> rotationOrderAttr = new RotationOrderAttribute(rotation, rotationAttr);
	
	/** visibility attribute */
	protected final BooleanAttr visibilityAttr = new BooleanAttr(true);
	
	/** local axisRotation transform matrix */
	protected final Matrix4d axisRotationMatrix = new Matrix4d();
	
	/** local transform matrix */
	protected final Matrix4d localMatrix = new Matrix4d();

	/** world axisRotation transform matrix */
	protected final Matrix4d axisRotation2WolrdMatrix = new Matrix4d();
	
	/** world transform matrix */
	protected final Matrix4d local2WorldMatrix = new Matrix4d();
	
	/** lazy evaluation flag */
	private boolean localInvalid = false;
	
	/** lazy evaluation flag */
	private boolean worldInvalid = false;
	
	private final  AttributePostChangeListener invalidationListener = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
			invalidate();
		}
	};
	
	private final TransformableHelper transformable = new TransformableHelper();
	
	public XFormNode() {
		/* add invalidation listener to attributes */
		translationAttr.addAttributePostChangeListener(invalidationListener);
		scaleAttr.addAttributePostChangeListener(invalidationListener);
		axisRotationAttr.addAttributePostChangeListener(invalidationListener);
		axisRotationOrderAttr.addAttributePostChangeListener(invalidationListener);
		rotationAttr.addAttributePostChangeListener(invalidationListener);
		rotationOrderAttr.addAttributePostChangeListener(invalidationListener);
		transformOrderAttr.addAttributePostChangeListener(invalidationListener);
	}
	
	/*
	 * attribute getter methods
	 */
	
	/**
	 * Gets the axis rotation attribute
	 * @return the axis rotation attribute
	 */
	public Tuple3Attr getAxisRotationAttribute() {
		return axisRotationAttr;
	}

	/**
	 * Gets the axis rotation order attribute
	 * @return the axis rotation order attribute
	 */
	public StateMachine<Rotation3d.Order> getAxisRotationOrderAttribute() {
		return axisRotationOrderAttr;
	}

	/**
	 * Gets the rotation attribute
	 * @return the rotation attribute
	 */
	public Tuple3Attr getRotationAttribute() {
		return rotationAttr;
	}

	/**
	 * Gets the rotation order attribute
	 * @return the rotation order attribute
	 */
	public StateMachine<Rotation3d.Order> getRotationOrderAttribute() {
		return rotationOrderAttr;
	}

	/**
	 * Gets the scale attribute
	 * @return the scale attribute
	 */
	public Tuple3Attr getScaleAttribute() {
		return scaleAttr;
	}

	/**
	 * Gets the transform order attribute
	 * @return the transform order attribute
	 */
	public StateMachine<TransformOrder> getTransformOrderAttribute() {
		return transformOrderAttr;
	}

	/**
	 * Gets the translation attribute
	 * @return the translation attribute
	 */
	public Tuple3Attr getTranslationAttribute() {
		return translationAttr;
	}

	/**
	 * Gets the visibility attribute
	 * @return the visibility attribute
	 */
	public BooleanAttr getVisibilityAttribute() {
		return visibilityAttr;
	}

	/*
	 * Transformable implementation
	 */
	
	public void begin() {
		transformable.begin();
	}

	public void end(List<JPatchUndoableEdit> editList) {
		transformable.end(editList);
	}

	public void rotate(Point3d pivot, AxisAngle4d axisAngle) {
		transformable.rotate(pivot, axisAngle);
	}

	public void scale(Scale3d scale) {
		// TODO Auto-generated method stub
		
	}

	public void sync() {
		// TODO Auto-generated method stub
		
	}

	public void translate(Vector3d vector) {
		transformable.translate(vector);
	}
	
	public void getPivot(Point3d pivot) {
		pivot.set(0, 0, 0);
	}
	
	
	public void getBaseTransform(TransformUtil transformUtil, int space) {
		transformable.getBaseTransform(transformUtil, space);
	}
	
	/*
	 * public methods
	 */
	
	/**
	 * Sets the specified matrix to the axisRotation->world transformation matrix
	 * of this node.
	 * @param matrix specified matrix
	 * @return specified matrix
	 */
	public Matrix4d getAxisRotation2WorldTransform(Matrix4d matrix) {
		computeWorldMatrices();
		matrix.set(axisRotation2WolrdMatrix);
		return matrix;
	}
	
	public void getAxisRotation2WorldTransform(TransformUtil transformUtil, int space) {
		computeWorldMatrices();
		transformUtil.setSpace2World(space, axisRotation2WolrdMatrix);
	}
	
	/**
	 * Sets the specified matrix to the local->world transformation matrix
	 * of this node.
	 * @param matrix specified matrix
	 * @return specified matrix
	 */
	public Matrix4d getLocal2WorldTransform(Matrix4d matrix) {
		computeWorldMatrices();
		matrix.set(local2WorldMatrix);
		return matrix;
	}
	
	public void getLocal2WorldTransform(TransformUtil transformUtil, int space) {
		computeWorldMatrices();
		transformUtil.setSpace2World(space, local2WorldMatrix);
	}
	
	/**
	 * Invalidates this branch (this node and all its descendants). This method must
	 * be called when the scene-graph's topology has been changed.
	 */
	public void resetBranch() {
		invalidateBranch(true);
	}
	
	/**
	 * sets localInvalid flag on this node and
	 * worldInvalid flag on this node and all of its descendants.
	 * Note that sub-branches are only traversed if their worldInvalid
	 * flag has not been set yet (this method assumes that, if worldInvalid
	 * is set on a node, it is also set on all its descendants).
	 */
	private void invalidate() {
		localInvalid = true;
		invalidateBranch(false);
	}
	
	/**
	 * sets worldInvalid flag on this node and all of its descendants.
	 * If force is false, sub-branches are only traversed if their worldInvalid
	 * flag has not been set yet (it is assumed that, if worldInvalid
	 * is set on a node, it is also set on all its descendants).
	 * If force is true, the entire branch is traversed and all its worldInvalid
	 * flags are set. Force must be set when invalidating the branch after the
	 * scene-graph's topology has been changed.
	 * @param force 
	 */
	private void invalidateBranch(boolean force) {
		if (force || !worldInvalid) {
			worldInvalid = true;
			for (SceneGraphNode child : getChildrenAttribute().getElements()) {
				XFormNode childNode = (XFormNode) child;
				if (force || !childNode.worldInvalid) {
					childNode.invalidateBranch(force);
				}
			}
		}
	}
	
	/**
	 * Lazily evaluates the local transformation matrices on this node
	 */
	private void computeLocalMatrices() {
		if (localInvalid) {
			transformOrderAttr.getValue().computeMatrices(this);
			localInvalid = false;
		}
	}
	
	/**
	 * Lazily evaluates the to-world transformation matrices of this node.
	 * Note that, if the to-world matrices of the parent node are invalide, this method
	 * is called (recursively) on the parent node.
	 */
	private void computeWorldMatrices() {
		if (localInvalid || worldInvalid) {
			computeLocalMatrices();
			
			/* multiply matrices with parent's local2world-transform */
			SceneGraphNode parent = getParentAttribute().getValue();
			if (parent != null && parent instanceof XFormNode) {
				XFormNode parentXForm = ((XFormNode) parent);
				parentXForm.computeWorldMatrices();
				axisRotation2WolrdMatrix.mul(parentXForm.local2WorldMatrix, axisRotationMatrix);
				local2WorldMatrix.mul(parentXForm.local2WorldMatrix, localMatrix);
			} else {
				local2WorldMatrix.set(localMatrix);
			}
			worldInvalid = false;
		}
	}
	
	/**
	 * Attribute for the rotation order of a specified Rotation3d/Tuple3Attr combination
	 * @author sascha
	 */
	private static class RotationOrderAttribute extends StateMachine<Rotation3d.Order> {
		private RotationOrderAttribute(final Rotation3d rotation, final Tuple3Attr rotationAttr) {
			super(Rotation3d.Order.values(), rotation.getOrder());
			addAttributePostChangeListener(new AttributePostChangeListener() {
				public void attributeHasChanged(Attribute source) {
					Matrix3d matrix = rotation.getRotationMatrix(new Matrix3d());	// get current rotation matrix
					rotation.setOrder(getValue());									// set new rotation order
					/* set rotation to matrix (triggers recomputation of x/y/z with new rotation order) */
					rotation.setRotation(matrix);									
					rotationAttr.setTuple(rotation);								// set rotationAttr to reflect new values
				}
			});
		}
	}
	
	/**
	 * strategy pattern
	 * used to apply transformation operations in the order specified at construction time
	 */
	private static class TransformOrder {
		private final XFormOp[] order;
		private final int rotationIndex;
		private final String name;
		
		private TransformOrder(XFormOp... order) {
			this.order = order;
			int axisRotationIndex = -1;
			for (int i = 0; i < order.length; i++) {
				if (order[i] == XFormOp.ROTATION) {
					axisRotationIndex = i;
				}
			}
			if (axisRotationIndex == -1) {
				throw new IllegalArgumentException("rotation missing");
			}
			this.rotationIndex = axisRotationIndex;
			
			/* create string representation */
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < order.length - 1; i++) {
				sb.append(asString(order[i]));
				sb.append(", ");
			}
			sb.append(asString(order[order.length - 1]));
			name = sb.toString();
		}
		
		private void computeMatrices(XFormNode node) {
			/* compute axisRotation matrix */
			node.axisRotationMatrix.setIdentity();
			for (int i = 0; i < rotationIndex; i++) {
				transformMatrix(node, node.axisRotationMatrix, order[i]);
			}
			transformMatrix(node, node.axisRotationMatrix, XFormOp.AXIS_ROTATION);
			
			/* compute local matrix */
			node.localMatrix.set(node.axisRotationMatrix);
			for (int i = rotationIndex; i < order.length; i++) {
				transformMatrix(node, node.localMatrix, order[i]);
			}
		}
		
		private void transformMatrix(XFormNode node, Matrix4d matrix, XFormOp xformOp) {
			switch (xformOp) {
			case SCALE:
				node.scale.scaleMatrix(matrix);
				break;
			case TRANSLATION:
				Utils3d.translateMatrix(matrix, node.translation);
				break;
			case AXIS_ROTATION:
				node.axisRotation.rotateMatrix(matrix);
				break;
			case ROTATION:
				node.rotation.rotateMatrix(matrix);
				break;
			default:
				throw new RuntimeException();
			}
		}
		
		private String asString(XFormOp xformOp) {
			switch (xformOp) {
			case SCALE:
				return "scale";
			case TRANSLATION:
				return "transl";
			case ROTATION:
				return "rot";
			default:
				throw new RuntimeException();
			}
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	private class TransformableHelper {
		private Rotation3d rStart = new Rotation3d();
		private Vector3d startTranslation = new Vector3d();
		private Matrix3d startRot = new Matrix3d();
		private Matrix3d newRot = new Matrix3d();
		private Matrix4d localTransform = new Matrix4d();
		private boolean active = false;
		
		public void begin() {
			startTranslation.set(translation);
			rStart.set(rotation);
			rotation.getRotationMatrix(startRot);
			getLocal2WorldTransform(localTransform);
			active = true;
		}
		
		public void rotate(Point3d pivot, AxisAngle4d axisAngle) {
			newRot.set(axisAngle);
			newRot.mul(startRot, newRot);
			rotation.setRotation(newRot);
			rotationAttr.setTuple(rotation);
		}
		
		public void translate(Vector3d vector) {
			
			Matrix3d m = rotation.getRotationMatrix(new Matrix3d());
//			m.invert();
			Vector3d v = new Vector3d(vector);
			m.transform(v);
			
			translation.add(startTranslation, v);
			
			translationAttr.setTuple(translation);
		}
		
		public void end(List<JPatchUndoableEdit> editList) {
			active = false;
			editList.add(AttributeEdit.changeAttribute(rotationAttr, rStart, false));
		}
		
		public void getBaseTransform(TransformUtil transformUtil, int space) {
			if (active) {
				transformUtil.setSpace2World(space, localTransform);
			} else {
				getLocal2WorldTransform(transformUtil, space);
			}
//			System.out.println(active + " " + transformUtil.getMatrix(space, TransformUtil.WORLD, new Matrix4d()));
		}
		
	}
}
