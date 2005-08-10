package jpatch.control.importer;

import javax.vecmath.*;

public final class HashCp {
	private long lFlags;
	private int iWeld;
	private int iNum;
	private float fHookPos;
	private Point3f p3Position;
	private int iWeldedTo;
	private float fInAlpha;
	private float fInGamma;
	private float fInMagnitude;
	private float fOutAlpha;
	private float fOutGamma;
	private float fOutMagnitude;

	// Constructors
	
	public HashCp(long flags, int weld, int num, float hookPos, Point3f position, int weldedTo, float inAlpha, float inGamma, float inMagnitude, float outAlpha, float outGamma, float outMagnitude) {
		lFlags = flags;
		iWeld = weld;
		iNum = num;
		fHookPos = hookPos;
		p3Position = position;
		iWeldedTo = weldedTo;
		fInAlpha = inAlpha;
		fInGamma = inGamma;
		fInMagnitude = inMagnitude;
		fOutAlpha = outAlpha;
		fOutGamma = outGamma;
		fOutMagnitude = outMagnitude;
	}
	
	// static methods
	
	public static boolean isWeld(int weld) {
		return (weld == 1);
	}
	
	public static boolean isSmooth(long flags) {
		return !((flags & 1l) == 0);
	}
	
	public static boolean isLoop(long flags) {
		return !((flags & 4l) == 0);
	}
	
	public static boolean isHook(long flags) {
		return !((flags & 16l) == 0);
	}
	
	// Accessor methods
	
	public int getNum() {
		return iNum;
	}
	
	public float getHookPos() {
		return fHookPos;
	}
	
	public Point3f getPosition() {
		return p3Position;
	}
	
	public int getWeldedTo() {
		return iWeldedTo;
	}
	
	public float getInAlpha() {
		return fInAlpha;
	}
	
	public float getInGamme() {
		return fInGamma;
	}
	
	public float getInMagnitude() {
		return fInMagnitude;
	}
	
	public float getOutAlpha() {
		return fOutAlpha;
	}
	
	public float getOutGamme() {
		return fOutGamma;
	}
	
	public float getOutMagnitude() {
		return fOutMagnitude;
	}
	
	// public methods
	
	public boolean isWeld() {
		return isWeld(iWeld);
	}
	
	public boolean isSmooth() {
		return isSmooth(lFlags);
	}
	
	public boolean isLoop() {
		return isLoop(lFlags);
	}
	
	public boolean isHook() {
		return isHook(lFlags);
	}
}
