package com.jpatch.boundary.tools;

import com.jpatch.boundary.*;
import com.jpatch.entity.*;

public interface JPatchTool {
//	void setTransformable(Transformable transformable);
	void registerListeners(Viewport[] viewports);
	void unregisterListeners(Viewport[] viewports);
	void draw(Viewport viewport);
}
