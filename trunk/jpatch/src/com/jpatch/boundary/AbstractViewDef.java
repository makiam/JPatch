package com.jpatch.boundary;

import com.jpatch.afw.attributes.BooleanAttr;
import com.jpatch.afw.attributes.StateMachine;

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

	public BooleanAttr getAntialiasAttribute() {
		return viewport.getAntialiasAttribute();
	}
	
	public BooleanAttr getShowNodeNamesAttribute() {
		return viewport.getShowNodeNamesAttribute();
	}
	
	public StateMachine<ViewDirection> getViewDirectionAttribute() {
		return viewport.getViewDirectionAttribute();
	}

}
