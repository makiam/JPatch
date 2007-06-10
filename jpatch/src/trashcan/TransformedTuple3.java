package trashcan;

import com.jpatch.afw.attributes.Attribute;
import com.jpatch.afw.attributes.AttributePostChangeListener;
import com.jpatch.afw.attributes.DoubleAttr;
import com.jpatch.afw.attributes.Tuple3Attr;

import javax.vecmath.*;

import jpatch.entity.Constants;

public abstract class TransformedTuple3 extends Tuple3Attr {
	protected final Tuple3Attr referenceTuple;
	protected final Matrix4d matrix = new Matrix4d(Constants.IDENTITY_MATRIX);
	protected final Matrix4d inverseMatrix = new Matrix4d(Constants.IDENTITY_MATRIX);
	protected boolean inverseInvalid = false;
	protected boolean autoTransform = true;
	
	private final AttributePostChangeListener transformAttributeListener = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
			if (autoTransform) {
				transform();
			}
		}
	};
	
	private final AttributePostChangeListener invTransformAttributeListener = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
			if (autoTransform) {
				invTransform();
			}
		}
	};
	
	protected TransformedTuple3(double x, double y, double z) {
		this(new DoubleAttr(x), new DoubleAttr(y), new DoubleAttr(z));
	}
	
	protected TransformedTuple3(DoubleAttr x, DoubleAttr y, DoubleAttr z) {
		super(x, y, z);
		referenceTuple = new Tuple3Attr() {
			@Override
			public final void setTuple(double x, double y, double z) {
				autoTransform = false;
				super.setTuple(x, y, z);
				transform();
				autoTransform = true;
			}
		};
		xAttr.addAttributePostChangeListener(invTransformAttributeListener);
		yAttr.addAttributePostChangeListener(invTransformAttributeListener);
		zAttr.addAttributePostChangeListener(invTransformAttributeListener);
		referenceTuple.xAttr.addAttributePostChangeListener(transformAttributeListener);
		referenceTuple.yAttr.addAttributePostChangeListener(transformAttributeListener);
		referenceTuple.zAttr.addAttributePostChangeListener(transformAttributeListener);
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
	
	public Tuple3Attr getReferenceTuple3() {
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
