package trashcan;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.ui.*;

public class Globals {
	private static final Globals INSTANCE = new Globals();
	private final IntAttr editLevelAttr = AttributeManager.getInstance().createBoundedIntAttr(0, 0, 4);
	private final IntAttr renderLevelAttr = AttributeManager.getInstance().createBoundedIntAttr(1, 1, 4);
	
	public static Globals getInstance() {
		return INSTANCE;
	}
	
	private Globals() {

	}
	
	public IntAttr getEditLevelAttribute() {
		return editLevelAttr;
	}
	
	public IntAttr getRenderLevelAttribute() {
		return renderLevelAttr;
	}
}
