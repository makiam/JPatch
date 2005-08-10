package jpatch.entity;

import javax.vecmath.*;
import jpatch.control.edit.*;

public interface Transformable {
	public JPatchAbstractUndoableEdit transformPermanent(Matrix4f m);
	public void transformTemporary(Matrix4f m);
	public void prepareForTemporaryTransform();
}
