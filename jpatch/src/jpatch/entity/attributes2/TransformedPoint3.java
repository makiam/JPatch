package jpatch.entity.attributes2;

public class TransformedPoint3 extends TransformedTuple3 {

	public TransformedPoint3(DoubleAttr x, DoubleAttr y, DoubleAttr z, final boolean checkLimit) {
		super(x, y, z, checkLimit);
	}
	
	@Override
	protected void transform() {
		super.invTransform();
		double xIn = referenceTuple.xAttr.getDouble();
		double yIn = referenceTuple.yAttr.getDouble();
		double zIn = referenceTuple.zAttr.getDouble(); 
		xAttr.setDouble(inverseMatrix.m00 * xIn + inverseMatrix.m01 * yIn + inverseMatrix.m02 * zIn + inverseMatrix.m03);
		yAttr.setDouble(inverseMatrix.m10 * xIn + inverseMatrix.m11 * yIn + inverseMatrix.m12 * zIn + inverseMatrix.m13);
		zAttr.setDouble(inverseMatrix.m20 * xIn + inverseMatrix.m21 * yIn + inverseMatrix.m22 * zIn + inverseMatrix.m23);
	}

	@Override
	protected void invTransform() {
		double xIn = xAttr.getDouble();
		double yIn = yAttr.getDouble();
		double zIn = zAttr.getDouble(); 
		referenceTuple.xAttr.setDouble(matrix.m00 * xIn + matrix.m01 * yIn + matrix.m02 * zIn + matrix.m03);
		referenceTuple.yAttr.setDouble(matrix.m10 * xIn + matrix.m11 * yIn + matrix.m12 * zIn + matrix.m13);
		referenceTuple.zAttr.setDouble(matrix.m20 * xIn + matrix.m21 * yIn + matrix.m22 * zIn + matrix.m23);
	}
}
