package jpatch.boundary.selection;

import jpatch.entity.*;

public class BoneSelection extends Selection {
	
	public static final int NOTHING = 0;
	public static final int START = 1;
	public static final int END = 2;
	public static final int LINE = 3;
	
	private static BoneSelection bs = new BoneSelection(null);
	
	private int iMode;
	private Bone bone;
	
	public BoneSelection(Bone bone) {
		this.bone = bone;
		iMode = LINE;
	}
	
	public BoneSelection(Bone bone, int mode) {
		this.bone = bone;
		iMode = mode;
	}
	
	public Bone getBone() {
		return bone;
	}
	
	public int getMode() {
		return iMode;
	}
	
	public static Class getBoneSelectionClass() {
		return bs.getClass();
	}
}
