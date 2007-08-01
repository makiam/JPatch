package com.jpatch.boundary;

import com.jpatch.entity.Perspective;

public class PerspectiveViewDirection implements ViewDirection {
	final Perspective perspective;
	
	public PerspectiveViewDirection(Perspective perspective) {
		this.perspective = perspective;
	}
	
	public void bindViewport(Viewport viewport) {
		viewport.setViewDef(new PerspectiveViewDef(viewport, perspective));
	}

	public void unbindViewport(Viewport viewport) {
		;	// do nothing
	}

	public int compareTo(Object o) {
		if (o == this) {
			return 0;
		}
		if (o instanceof OrthoViewDirection) {
			return 1;
		}
		if (o instanceof PerspectiveViewDirection) {
			return hashCode() - o.hashCode(); 	// FIXME
		}
		return -1;
	}

	@Override
	public String toString() {
		return perspective.getNameAttribute().getValue();
	}
}
