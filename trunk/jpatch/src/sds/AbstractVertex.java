package sds;

import javax.vecmath.*;
import jpatch.entity.*;

public abstract class AbstractVertex {
	public final Attribute.Tuple3 referencePosition = new Attribute.Tuple3(null, 0, 0, 0, false);
	public final Attribute.Tuple3 position = new Attribute.Tuple3(null, 0, 0, 0, false);
	public final Attribute.Integer sharpness = new Attribute.Integer(0);
	
	private final Matrix4d transform = new Matrix4d(Constants.IDENTITY_MATRIX);
	private final Matrix4d invTransform = new Matrix4d(Constants.IDENTITY_MATRIX);
	private boolean inverseInvalid = false;
	
	final Point3d pos = new Point3d();
	final Point3d refPos = new Point3d();
	
	public AbstractVertex() {
		position.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
//				System.out.println(ControlPoint.this + " position changed");
				position.get(pos);
				refPos.set(pos);
				if (inverseInvalid) {
					computeInverseTransform();
				}
				invTransform.transform(refPos);
//				referencePosition.setValueAdjusting(true);
				referencePosition.set(refPos);
//				referencePosition.setValueAdjusting(false);
			}
		});
		referencePosition.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				referencePosition.get(refPos);
				pos.set(refPos);
				transform.transform(pos);
//				position.setValueAdjusting(true);
				position.set(pos);
//				position.setValueAdjusting(false);
			}
		});
	}
	
	/**
	 * Computes the inverse transformation matrix and clears
	 * the inverseInvalid flag.
	 */
	private void computeInverseTransform() {
		invTransform.invert(transform);
		inverseInvalid = false;
	}
}
