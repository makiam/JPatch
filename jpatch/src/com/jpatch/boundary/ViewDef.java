package com.jpatch.boundary;

import javax.vecmath.*;
import com.jpatch.afw.attributes.*;
import com.jpatch.afw.vecmath.TransformUtil;

public interface ViewDef {
	public BooleanAttr getShowControlMeshAttribute();
	public BooleanAttr getShowLimitSurfaceAttribute();
	public BooleanAttr getShowProjectedMeshAttribute();
	public StateMachine<ViewDirection> getViewDirectionAttribute();
	public Viewport getViewport();
	public void configureTransformUtil(TransformUtil transformUtil);
}
