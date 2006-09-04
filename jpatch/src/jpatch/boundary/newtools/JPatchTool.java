package jpatch.boundary.newtools;

import jpatch.boundary.*;

public interface JPatchTool {
	void registerListeners(Viewport[] viewports);
	void unregisterListeners(Viewport[] viewports);
	void draw(Viewport viewport);
}
