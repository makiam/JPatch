package com.jpatch.boundary;

import com.jpatch.afw.attributes.*;

public abstract class AbstractViewDef implements ViewDef {
	protected final Viewport viewport;
	
	public AbstractViewDef(Viewport viewport) {
		this.viewport = viewport;
	}
	
	public Viewport getViewport() {
		return viewport;
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

	public BooleanAttr getShowNodeNamesAttribute() {
		return viewport.getShowNodeNamesAttribute();
	}
	
	public StateMachine<ViewDirection> getViewDirectionAttribute() {
		return viewport.getViewDirectionAttribute();
	}
}
