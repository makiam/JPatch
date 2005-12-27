package jpatch.boundary.tools;

import jpatch.boundary.*;
import jpatch.boundary.mouse.*;

public abstract class JPatchTool extends JPatchMouseAdapter {
	
	protected JPatchUserSettings settings = JPatchUserSettings.getInstance();

	public abstract void paint(ViewDefinition viewDef);
	//public abstract void reInit(PointSelection ps);
	public int getButton() {
		return -1;
	}
}

