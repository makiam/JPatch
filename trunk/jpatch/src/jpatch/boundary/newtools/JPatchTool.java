package jpatch.boundary.newtools;

import java.awt.Component;
import jpatch.boundary.ViewDefinition;

public interface JPatchTool {
	void registerListeners(Component[] components);
	void unregisterListeners(Component[] components);
	void draw(ViewDefinition viewDef);
}
