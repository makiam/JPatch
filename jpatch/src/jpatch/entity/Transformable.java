package jpatch.entity;

import javax.vecmath.*;
import jpatch.control.edit.*;

public interface Transformable {
	public JPatchAbstractUndoableEdit transformPermanently(Matrix4f m);
	public void transformTemporarily(Matrix4f m);
	public void prepareForTemporaryTransformation();
}
