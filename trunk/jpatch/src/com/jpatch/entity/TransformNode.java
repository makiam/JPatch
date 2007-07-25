package com.jpatch.entity;

import javax.vecmath.*;

import trashcan.SoftBoundedDoubleAttr;
import com.jpatch.afw.vecmath.*;
import com.jpatch.afw.attributes.*;

public class TransformNode extends SceneGraphNode {
	
	/**
	 * Temporary storage for the translation (relative to the parent node)
	 */
	private Point3d translationTuple = new Point3d();
	
	/**
	 * Temporary storage for the axis rotation
	 */
	private Rotation3d axisRotationTuple = new Rotation3d();
	
	/**
	 * Temporary storage for the rotation
	 */
	private Rotation3d rotationTuple = new Rotation3d();
	
	/**
	 * Temporary storage for the scale
	 */
	private Scale3d scaleTuple = new Scale3d();
	
	/**
	 * Temporary storage for the position (relative to world space).
	 * This is derived from translation
	 */
	private Point3d positionTuple = new Point3d();
	
	/**
	 * Translation attribute (Tuple3 consisting of 3 SoftBoundedDoubleAttr)
	 */
	protected Tuple3Attr translationAttr = new Tuple3Attr();
	
	/**
	 * Position attribute (Tuple3 consisting of 3 DoubleAttr)
	 */
	protected Tuple3Attr positionAttr = new Tuple3Attr();
	
	/**
	 * Axis-rotation attribute (Tuple3 consisting of 3 DoubleAttr)
	 */
	protected Tuple3Attr axisRotationAttr = new Tuple3Attr();
	
	/**
	 * Rotation attribute (Tuple3 consisting of 3 SoftBoundedDoubleAttr)
	 */
	protected Tuple3Attr rotationAttr = new Tuple3Attr();
	
	/**
	 * Rotation-order attribute
	 */
	protected StateMachine<Rotation3d.Order> rotationOrderAttr = new StateMachine<Rotation3d.Order>(Rotation3d.Order.values(), getRotationOrder());
	
	/**
	 * Scale attribute (Tuple3 consisting of 3 SoftBoundedDoubleAttr)
	 */
	protected Tuple3Attr scaleAttr = new Tuple3Attr(1, 1, 1);
	
	protected BooleanAttr visibilityAttr = new BooleanAttr(true);
	
	protected GenericAttr<String> nameAttr = new GenericAttr<String>();
	
	/**
	 * Auto-transform flag, used to prevent loops when a position change updates the translation
	 * or vice versa.
	 */
	protected boolean autoTransform = true;
	
	/**
	 * Listens for translation changes and updates position
	 */
	protected AttributePostChangeListener translationListener = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
			if (autoTransform) {
				autoTransform = false;
				computePosition();
				autoTransform = true;
			}
		}
	};
	
	/**
	 * Listens for position changes and updates translation
	 */
	protected AttributePostChangeListener positionListener = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
			if (autoTransform) {
				autoTransform = false;
				positionAttr.getTuple(translationTuple);
				transform.invTransform(translationTuple);
				translationAttr.setTuple(translationTuple);
				if (translationTuple.x != translationAttr.getX() || translationTuple.y != translationAttr.getY() || translationTuple.z != translationAttr.getZ()) {
					positionTuple.set(translationTuple);
					transform.transform(positionTuple);
					positionAttr.setTuple(positionTuple);
				}
				autoTransform = true;
			}
		}
	};
	
	/**
	 * Listens for rotation order changes and updates all Rotation3d objects accordingly
	 */
	protected AttributePostChangeListener rotationOrderListener = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
			setRotationOrder(rotationOrderAttr.getValue());
		}
	};
	
	void computePosition() {
		translationAttr.getTuple(positionTuple);
		transform.transform(positionTuple);
		positionAttr.setTuple(positionTuple);
	}
	
	private final Transform transform = new Transform() {
		@Override
		public void computeMatrix() {
			SceneGraphNode parent = parentAttr.getValue();
			Transform parentTransform = parent.getTransform();
			if (parentTransform != null) {
				parentTransform.getMatrix(matrix);
			} else {
				matrix.setIdentity();
			}
			translationAttr.getTuple(translationTuple);
			axisRotationAttr.getTuple(axisRotationTuple);
			rotationAttr.getTuple(rotationTuple);
			scaleAttr.getTuple(scaleTuple);
			scaleTuple.scaleMatrix(matrix);
			axisRotationTuple.rotateMatrix(matrix);
			rotationTuple.rotateMatrix(matrix);
			Utils3d.translateMatrix(matrix, translationTuple);
			invInvalid = true;
//			System.out.println(nameAttr.getValue() + " computeMatrix called, matrix is " + matrix);
		}
		
		@Override
		public void computeTransformedValues() {
			if (autoTransform) {
				autoTransform = false;
				translationAttr.getTuple(positionTuple);
				transform.transform(positionTuple);
				positionAttr.setTuple(positionTuple);
				autoTransform = true;
			}
		}
	};
	
	public Transform getTransform() {
		return transform;
	}
	
	/**
	 * Constructor
	 */
	public TransformNode() {
		translationAttr.addAttributePostChangeListener(translationListener);
		positionAttr.addAttributePostChangeListener(positionListener);
//		rotationOrderAttr.addAttributePostChangeListener(rotationOrderListener);
	}
	
	/**
	 * Returns the translation attribute
	 * @return the translation attribute
	 */
	public Tuple3Attr getTranslationAttribute() {
		return translationAttr;
	}
	
	/**
	 * Returns the axis rotation attribute
	 * @return the axis rotation attribute
	 */
	public Tuple3Attr getAxisRotationAttribute() {
		return axisRotationAttr;
	}
	
	/**
	 * Returns the rotation attribute
	 * @return the rotation attribute
	 */
	public Tuple3Attr getRotationAttribute() {
		return rotationAttr;
	}
	
	/**
	 * Returns the scale attribute
	 * @return the scale attribute
	 */
	public Tuple3Attr getScaleAttribute() {
		return scaleAttr;
	}
	
	/**
	 * Returns the position attribute
	 * @return the position attribute
	 */
	public Tuple3Attr getPositionAttribute() {
		return positionAttr;
	}
	
	/**
	 * Returns the rotation order attribute
	 * @return the rotation order attribute
	 */
	public StateMachine<Rotation3d.Order> getRotationOrderAttribute() {
		return rotationOrderAttr;
	}
	
	/**
	 * Returns the rotation order
	 * @return the rotation order
	 */
	public Rotation3d.Order getRotationOrder() {
		return rotationTuple.getOrder();
	}
	
	/**
	 * Returns the visibility attribute
	 * @return the visibility attribute
	 */
	public BooleanAttr getVisibilityAttribute() {
		return visibilityAttr;
	}
	
	/**
	 * Returns the visibility of this node
	 * @return true if the node is visibile, false otherwise
	 */
	public boolean isVisible() {
		return visibilityAttr.getBoolean();
	}
	
	/**
	 * Sets the rotation order
	 * @param order the new rotation order
	 */
	public void setRotationOrder(Rotation3d.Order order) {
		if (order != getRotationOrder()) {
			rotationOrderAttr.setValue(order);
			transform.computeBranch();
		}
	}
	
	/**
	 * Returns the name attribute
	 * @return the name attribute
	 */
	public GenericAttr<String> getNameAttribute() {
		return nameAttr;
	}
}