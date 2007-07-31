package com.jpatch.boundary;

public interface ViewDirection extends Comparable {
	public void bindViewport(Viewport viewport);
	public void unbindViewport(Viewport viewport);
}
