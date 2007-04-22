package com.jpatch.afw.attributes;

import javax.vecmath.*;

import jpatch.entity.Constants;

public abstract class TransformedTuple3 extends Tuple3 {
	protected final Tuple3 referenceTuple;
	protected final Matrix4d matrix = new Matrix4d(Constants.IDENTITY_MATRIX);
	protected final Matrix4d inverseMatrix = new Matrix4d(Constants.IDENTITY_MATRIX);
	protected boolean inverseInvalid = false;
	protected boolean autoTransform = true;
	
	private final AttributeListener transformAttributeListener = new AttributeListener() {
		public void attributeChanged(Attribute source) {
			if (autoTransform) {
				transform();
			}
		}
	};
	
	private final AttributeListener invTransformAttributeListener = new AttributeListener() {
		public void attributeChanged(Attribute source) {
			if (autoTransform) {
				invTransform();
			}
		}
	};
	
	protected TransformedTuple3(DoubleAttr x, DoubleAttr y, DoubleAttr z, final boolean checkLimit) {
		super(x, y, z);
		referenceTuple = new Tuple3() {
			@Override
			public final void setTuple(double x, double y, double z) {
				autoTransform = false;
				if (checkLimit) {
					double oldX = xAttr.value;
					double oldY = yAttr.value;
					double oldZ = zAttr.value;
					xAttr.value = x;
					yAttr.value = y;
					zAttr.value = z;
					invTransform();
					boolean fireX = xAttr.fireEvents;
					boolean fireY = yAttr.fireEvents;
					boolean fireZ = zAttr.fireEvents;
					xAttr.fireEvents = false;
					yAttr.fireEvents = false;
					zAttr.fireEvents = false;
					transform();
					xAttr.fireEvents = fireX;
					yAttr.fireEvents = fireY;
					zAttr.fireEvents = fireZ;
					if (oldX != xAttr.value) {
						xAttr.fireAttributeChanged();
					}
					if (oldY != xAttr.value) {
						xAttr.fireAttributeChanged();
					}
					if (oldZ != xAttr.value) {
						xAttr.fireAttributeChanged();
					}
				} else {
					super.setTuple(x, y, z);
					transform();
				}
				autoTransform = true;
			}
		};
		xAttr.addAttributeListener(invTransformAttributeListener);
		yAttr.addAttributeListener(invTransformAttributeListener);
		zAttr.addAttributeListener(invTransformAttributeListener);
		referenceTuple.xAttr.addAttributeListener(transformAttributeListener);
		referenceTuple.yAttr.addAttributeListener(transformAttributeListener);
		referenceTuple.zAttr.addAttributeListener(transformAttributeListener);
	}
	
	@Override
	public final void setTuple(double x, double y, double z) {
		autoTransform = false;
		super.setTuple(x, y, z);
		invTransform();
		autoTransform = true;
	}

	public void setTransform(Matrix4d matrix) {
		this.matrix.set(matrix);
		inverseInvalid = true;
		transform();
	}
	
	public Tuple3 getReferenceTuple3() {
		return referenceTuple;
	}
	
	protected abstract void transform();

	protected void invTransform() {
		if (inverseInvalid) {
			inverseMatrix.invert(matrix);
			inverseInvalid = false;
		}
	}
}
