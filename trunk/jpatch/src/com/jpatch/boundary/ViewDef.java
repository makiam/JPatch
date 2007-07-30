package com.jpatch.boundary;

import javax.vecmath.*;
import com.jpatch.afw.attributes.*;

public interface ViewDef {
	public BooleanAttr getShowControlMeshAttribute();
	public BooleanAttr getShowLimitSurfaceAttribute();
	public BooleanAttr getShowProjectedMeshAttribute();
	public StateMachine<ViewDirection> getViewDirectionAttribute();
	public Matrix4d getMatrix();
	public Matrix4d getInverseMatrix();
	public void computeMatrix();
}
