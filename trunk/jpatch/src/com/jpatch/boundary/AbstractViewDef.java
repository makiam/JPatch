package com.jpatch.boundary;

import com.jpatch.afw.attributes.BooleanAttr;
import com.jpatch.afw.attributes.StateMachine;

public abstract class AbstractViewDef implements ViewDef {
	protected Viewport viewport;
	
	public void setViewport(Viewport viewport) {
		this.viewport = viewport;
	}
	
	public BooleanAttr getShowControlMeshAttribute() {
		return viewport.getShowControlMeshAttribute();
	}

	public BooleanAttr getShowLimitSurfaceAttribute() {
		return viewport.getShowLimitSurfaceAttribute();
	}

	public BooleanAttr getShowProjectedMeshAttribute() {
		return viewport.getShowProjectedMeshAttribute();
	}

	public StateMachine<ViewDef> getViewTypeAttribute() {
		return viewport.getViewTypeAttribute();
	}

}
