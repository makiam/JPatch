package trashcan;

import com.jpatch.afw.attributes.DoubleAttr;

public class TransformedPoint3 extends TransformedTuple3 {

	public TransformedPoint3(DoubleAttr x, DoubleAttr y, DoubleAttr z) {
		super(x, y, z);
	}
	
	public TransformedPoint3(double x, double y, double z) {
		super(x, y, z);
	}
	
	@Override
	protected void transform() {
		double xIn = referenceTuple.getXAttr().getDouble();
		double yIn = referenceTuple.getYAttr().getDouble();
		double zIn = referenceTuple.getZAttr().getDouble(); 
		getXAttr().setDouble(matrix.m00 * xIn + matrix.m01 * yIn + matrix.m02 * zIn + matrix.m03);
		getYAttr().setDouble(matrix.m10 * xIn + matrix.m11 * yIn + matrix.m12 * zIn + matrix.m13);
		getZAttr().setDouble(matrix.m20 * xIn + matrix.m21 * yIn + matrix.m22 * zIn + matrix.m23);
	}

	@Override
	protected void invTransform() {
		super.invTransform();
		double xIn = getXAttr().getDouble();
		double yIn = getYAttr().getDouble();
		double zIn = getZAttr().getDouble(); 
		referenceTuple.getXAttr().setDouble(inverseMatrix.m00 * xIn + inverseMatrix.m01 * yIn + inverseMatrix.m02 * zIn + inverseMatrix.m03);
		referenceTuple.getYAttr().setDouble(inverseMatrix.m10 * xIn + inverseMatrix.m11 * yIn + inverseMatrix.m12 * zIn + inverseMatrix.m13);
		referenceTuple.getZAttr().setDouble(inverseMatrix.m20 * xIn + inverseMatrix.m21 * yIn + inverseMatrix.m22 * zIn + inverseMatrix.m23);
	}
}
