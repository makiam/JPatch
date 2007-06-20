package trashcan;

import com.jpatch.afw.attributes.DoubleAttr;

public class TransformedVector3 extends TransformedTuple3 {

	public TransformedVector3(DoubleAttr x, DoubleAttr y, DoubleAttr z) {
		super(x, y, z);
	}
	
	public TransformedVector3(double x, double y, double z) {
		super(x, y, z);
	}
	
	@Override
	protected void transform() {
		double xIn = referenceTuple.getXAttr().getDouble();
		double yIn = referenceTuple.getYAttr().getDouble();
		double zIn = referenceTuple.getZAttr().getDouble(); 
		xAttr.setDouble(matrix.m00 * xIn + matrix.m01 * yIn + matrix.m02 * zIn);
		yAttr.setDouble(matrix.m10 * xIn + matrix.m11 * yIn + matrix.m12 * zIn);
		zAttr.setDouble(matrix.m20 * xIn + matrix.m21 * yIn + matrix.m22 * zIn);
	}

	@Override
	protected void invTransform() {
		super.invTransform();
		double xIn = xAttr.getDouble();
		double yIn = yAttr.getDouble();
		double zIn = zAttr.getDouble(); 
		referenceTuple.getXAttr().setDouble(inverseMatrix.m00 * xIn + inverseMatrix.m01 * yIn + inverseMatrix.m02 * zIn);
		referenceTuple.getYAttr().setDouble(inverseMatrix.m10 * xIn + inverseMatrix.m11 * yIn + inverseMatrix.m12 * zIn);
		referenceTuple.getZAttr().setDouble(inverseMatrix.m20 * xIn + inverseMatrix.m21 * yIn + inverseMatrix.m22 * zIn);
	}
}
