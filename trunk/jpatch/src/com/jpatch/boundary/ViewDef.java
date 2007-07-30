package com.jpatch.boundary;

import javax.vecmath.*;
import com.jpatch.afw.attributes.*;

public interface ViewDef {
	public BooleanAttr getShowControlMeshAttribute();
	public BooleanAttr getShowLimitSurfaceAttribute();
	public BooleanAttr getShowProjectedMeshAttribute();
	public StateMachine<ViewDef> getViewTypeAttribute();
	public Matrix4d getMatrix(Matrix4d matrix);
	public Matrix4d getInverseMatrix(Matrix4d matrix);
}
