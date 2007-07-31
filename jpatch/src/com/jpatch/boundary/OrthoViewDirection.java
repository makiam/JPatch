package com.jpatch.boundary;

import com.jpatch.afw.attributes.Attribute;
import com.jpatch.afw.attributes.AttributePostChangeListener;
import com.jpatch.afw.attributes.Tuple2Attr;

import java.util.*;

public abstract class OrthoViewDirection implements ViewDirection {
	public static final OrthoViewDirection FRONT = new FixedViewDirection("Front", 0, 0);
	public static final OrthoViewDirection REAR = new FixedViewDirection("Rear", 0, 180);
	public static final OrthoViewDirection LEFT = new FixedViewDirection("Left", 0, 90);
	public static final OrthoViewDirection RIGHT = new FixedViewDirection("Right", 0, -90);
	public static final OrthoViewDirection TOP = new FixedViewDirection("Top", -90, 0);
	public static final OrthoViewDirection BOTTOM = new FixedViewDirection("Bottom", 90, 0);
	public static final OrthoViewDirection BIRDS_EYE = new BirdsEyeViewDirection("Bird's Eye", -45, -45);
	
	public static final OrthoViewDirection[] DIRECTIONS = new OrthoViewDirection[] { FRONT, REAR, LEFT, RIGHT, TOP, BOTTOM, BIRDS_EYE };
	
	final String name;
	final OrthoView initialView = new OrthoView();
	final Map <Viewport, OrthoView> viewMap = new HashMap<Viewport, OrthoView>();
	
	private OrthoViewDirection(String name, double rotX, double rotY) {
		this.name = name;
		this.initialView.rotation.set(rotX, rotY);
	}
	
	public void bindViewport(Viewport viewport) {
		OrthoView storedView = viewMap.get(viewport);
		if (storedView == null) {
			storedView = new OrthoView(initialView);
			viewMap.put(viewport, storedView);
		}
		viewport.setViewDef(new OrthoViewDef(viewport, storedView));
	}

	public void unbindViewport(Viewport viewport) {
		OrthoView storedView = viewMap.get(viewport);
		((OrthoViewDef) viewport.getViewDef()).getTranslationAttribute().getTuple(storedView.translation);
		storedView.scale = ((OrthoViewDef) viewport.getViewDef()).getScaleAttribute().getDouble();
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public int compareTo(Object o) {
		/* check for equality */
		if (this == o) {
			return 0;
		}
		
		if (o instanceof OrthoViewDirection) {
			/* 
			 * o is an OrthoViewDirection
			 * loop through the list of DIRECTIONS, return -1 if <i>this</i> comes
			 * first and 1 if <i>o</i> comes first.
			 */
			for (int i = 0; i < DIRECTIONS.length; i++) {
				if (this == DIRECTIONS[i]) {
					return -1;
				} else if (o == DIRECTIONS[i]) {
					return 1;
				}
			}
		}
		/* o is not an OrthoViewDirection, return -1 (OrthoViewDirections are ordered first) */
		return -1;
	}
	
	private static class FixedViewDirection extends OrthoViewDirection {
		private FixedViewDirection(String name, double rotationX, double rotationY) {
			super(name, rotationX, rotationY);
		}
		
		@Override
		public void bindViewport(final Viewport viewport) {
			super.bindViewport(viewport);
			((OrthoViewDef) viewport.getViewDef()).getRotationAttribute().addAttributePostChangeListener(new AttributePostChangeListener() {
				public void attributeHasChanged(Attribute source) {
					Tuple2Attr rot = (Tuple2Attr) source;
					if (rot.getX() != initialView.rotation.x || rot.getY() != initialView.rotation.y) {
						OrthoView birdsEyeView = BIRDS_EYE.viewMap.get(viewport);
						if (birdsEyeView == null) {
							birdsEyeView = new OrthoView();
							BIRDS_EYE.viewMap.put(viewport, birdsEyeView);
						}
						unbindViewport(viewport);
						OrthoView storedView = viewMap.get(viewport);
						storedView.updateViewdef(((OrthoViewDef) viewport.getViewDef()));
						birdsEyeView.set(storedView);
						rot.getTuple(birdsEyeView.rotation);		
						viewport.getViewDirectionAttribute().setValue(BIRDS_EYE);
					}
				}
			});
		}
	}
	
	private static class BirdsEyeViewDirection extends OrthoViewDirection {
		
		private BirdsEyeViewDirection(String name, double rotationX, double rotationY) {
			super(name, rotationX, rotationY);
		}
		
		@Override
		public void unbindViewport(Viewport viewport) {
			super.unbindViewport(viewport);
			OrthoView storedView = viewMap.get(viewport);
			((OrthoViewDef) viewport.getViewDef()).getRotationAttribute().getTuple(storedView.rotation);
		}
	}
}
