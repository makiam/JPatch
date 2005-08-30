package jpatch.entity;

import javax.vecmath.*;
import jpatch.control.edit.*;

public interface Transformable {
	public void beginTransform();
	public void translate(Vector3f v);
	public void rotate(Quat4f q, Point3f pivot);
	public void transform(Matrix3f m, Point3f pivot);
	public JPatchUndoableEdit endTransform();
}
