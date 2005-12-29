package jpatch.boundary.tools;

import jpatch.boundary.*;
import jpatch.boundary.mouse.*;
import jpatch.boundary.settings.Settings;

public abstract class JPatchTool extends JPatchMouseAdapter {
	
	protected Settings settings = Settings.getInstance();

	public abstract void paint(ViewDefinition viewDef);
	//public abstract void reInit(PointSelection ps);
	public int getButton() {
		return -1;
	}
}

