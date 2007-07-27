package com.jpatch.boundary;

import com.jpatch.afw.attributes.GenericAttr;
import com.jpatch.afw.ui.AttributeManager;
import com.jpatch.entity.Camera;
import com.jpatch.entity.Perspective;

public class PerspectiveViewDirection implements ViewDirection {
	private final Perspective perspective;
	
	protected PerspectiveViewDirection(Perspective perspective) {
		this.perspective = perspective;
	}
	
	public Perspective getPerspective() {
		return perspective;
	}

	public String getName() {
		return perspective.getNameAttribute().getValue();
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public void bindTo(Viewport viewport) {
//		AttributeManager.getInstance().lock(viewport.getViewRotationAttribute().getXAttr());
//		AttributeManager.getInstance().lock(viewport.getViewRotationAttribute().getYAttr());
//		AttributeManager.getInstance().lock(viewport.getViewTranslationAttribute().getXAttr());
//		AttributeManager.getInstance().lock(viewport.getViewTranslationAttribute().getYAttr());
//		AttributeManager.getInstance().lock(viewport.getViewScaleAttribute());
	}

	public void unbind(Viewport viewport) {
//		AttributeManager.getInstance().unlock(viewport.getViewRotationAttribute().getXAttr());
//		AttributeManager.getInstance().unlock(viewport.getViewRotationAttribute().getYAttr());
//		AttributeManager.getInstance().unlock(viewport.getViewTranslationAttribute().getXAttr());
//		AttributeManager.getInstance().unlock(viewport.getViewTranslationAttribute().getYAttr());
//		AttributeManager.getInstance().unlock(viewport.getViewScaleAttribute());
	}

	public int compareTo(Object o) {
		if (o == this) {
			return 0;
		}
		if (o instanceof OrthoViewDirection) {
			return 1;
		}
		if (o instanceof PerspectiveViewDirection) {
			PerspectiveViewDirection otherDir = (PerspectiveViewDirection) o;
			if (perspective instanceof Camera && !(otherDir.perspective instanceof Camera)) {
				return -1;
			}
			if (!(perspective instanceof Camera) && otherDir.perspective instanceof Camera) {
				return 1;
			}
			return getName().compareTo(((PerspectiveViewDirection) o).getName());
		}
		return -1;
	}

}
