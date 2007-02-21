package jpatch.entity.attributes2;

import javax.vecmath.*;
import jpatch.entity.*;

public class TransformedPoint3dAttr {
	private Point3d pRef = new Point3d();
	private Point3d p = new Point3d();
	private Matrix4d matrix = new Matrix4d(Constants.IDENTITY_MATRIX);
	private Matrix4d inverseMatrix = new Matrix4d(Constants.IDENTITY_MATRIX);
	private boolean inverseInvalid = false;
	
	public void setMatrix(Matrix4d matrix) {
		this.matrix.set(matrix);
		inverseInvalid = true;
	}
	
	public Point3d getPoint(Point3d p) {
		p.set(this.p);
		return p;
	}
	
	public Point3d getRefPoint(Point3d p) {
		p.set(this.pRef);
		return p;
	}
	
}
