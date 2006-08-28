package jpatch.entity;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.*;

public abstract class AbstractTransform extends AbstractNamedObject {
	protected AbstractTransform parent;
	protected Matrix4d matrix = new Matrix4d(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1); // identity matrix
	protected Matrix4d inverseMatrix = new Matrix4d(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1); // identity matrix
	protected boolean inverseInvalid = false;
	private List<AbstractTransform> childTransforms = new ArrayList<AbstractTransform>(1);
	
	public void setParent(JPatchObject newParent) {
		if (parent != null) {
			parent.removeChild(this);
		}
		parent = (AbstractTransform) newParent;
		if (parent != null) {
			parent.addChild(this);
		}
	}

	public void transform(Point3d point3d) {
		matrix.transform(point3d);
	}
	
	public void transform(Vector3d vector3d) {
		matrix.transform(vector3d);
	}
	
	public void multiply(Matrix4d matrix) {
		matrix.mul(this.matrix);
	}
	
	public void invTransform(Point3d point3d) {
		validateInverse();
		inverseMatrix.transform(point3d);
	}
	
	public void invTransform(Vector3d vector3d) {
		validateInverse();
		inverseMatrix.transform(vector3d);
	}
	
	public void computeBranch() {
		computeMatrix();
		computeDerivedAttributes();
		evaluateConstraints();
		for (AbstractTransform child : childTransforms)
			child.computeBranch();
	}
	
	protected void addChild(AbstractTransform child) {
		childTransforms.add(child);
	}
	
	protected void removeChild(AbstractTransform child) {
		childTransforms.remove(child);
	}
	
	protected abstract void computeMatrix();
	
	protected void computeDerivedAttributes() { }
	
	protected void evaluateConstraints() { }
	
	protected void positionChanged(Attribute.Tuple3 position, Attribute.Tuple3 translation) {
		Point3d tmp = new Point3d();
		position.get(tmp);
		if (parent != null)
			parent.invTransform(tmp);
//		position.setValueAdjusting(true);
		translation.set(tmp);
//		position.setValueAdjusting(false);
	}
	
	protected void translationChanged(Attribute.Tuple3 translation, Attribute.Tuple3 position) {
		Point3d tmp = new Point3d();
		translation.get(tmp);
		if (parent != null)
			parent.transform(tmp);
//		translation.setValueAdjusting(true);
		position.set(tmp);
//		translation.setValueAdjusting(false);
	}
	
	private void validateInverse() {
		if (inverseInvalid) {
			inverseMatrix.invert(matrix);
			inverseInvalid = false;
		}
	}
	
}
