package jpatch.entity;

public class Camera extends AnimObject {
	/* the focal lenghth of the lense */
	protected float fFocalLength = 50.0f;
	
	/* the aperture value (only relevant if bFocalBlur is set to true) */
	protected float fAperture = 16.0f;
	
	/* the focal distance (only relevant if bFocalBlur is set to true) */
	protected double dFocus = Double.POSITIVE_INFINITY;
	
	/* the frame rate in 1/seconds (-1 will use the global frameRate) */
	protected float fFrameRate = 25.0f;
	
	/* the shutter speed in 1/seconds (only relevant if bMotionBlur is set to true) */
	protected float fShutterSpeed = 25.0f;
	
	/* the exposure 1 = correct exposure, < 1 is underexposed, > 1 is overexposed */
	protected float fExposure = 1.0f;
	
	/* the film size (used together with dFocalLength to compute the field of view) */
	protected float fFilmSize = 35.0f;
	
	///* the aspect ratio (-1, -1 will use the global aspectRatio) */
	//protected double dAspectWidth = 16.0;
	//protected double dAspectHeight = 9.0;
	//
	///* the image resolution (-1, -1 will use the global resolution) */
	//protected int iHorizontalResolution = 640;
	//protected int iVerticalResolution = 480;
	
	/* enable focalblur flag */
	protected boolean bFocalBlur = false;
	
	/* enable motionblur flag */
	protected boolean bMotionBlur = false;
	
	//protected Rotation3f rotation = new Rotation3f();
	
	public Camera(String name) {
		strName = name;
	}
	
	public void setFocalLength(float focalLength) {
		fFocalLength = focalLength;
	}
	
	public void setAperture(float aperture) {
		fAperture = aperture;
	}
	
	public void setFocus(double focus) {
		dFocus = focus;
	}
	
	public void setFrameRate(float frameRate) {
		fFrameRate = frameRate;
	}
	
	public void setShutterSpeed(float shutterSpeed) {
		fShutterSpeed = shutterSpeed;
	}
	
	public void setExposure(float exposure) {
		fExposure = exposure;
	}
	
	public void setFilmSize(float filmSize) {
		fFilmSize = filmSize;
	}
	
	//public void setAspectWidth(double aspectWidth) {
	//	dAspectWidth = aspectWidth;
	//}
	//
	//public void setAspectHeight(double apsectHeight) {
	//	dAspectHeight = apsectHeight;
	//}
	//
	//public void setAspectRatio(double width, double height) {
	//	dAspectWidth = width;
	//	dAspectHeight = height;
	//}
	
	//public void setHorizontalResolution(int horizontalResolution) {
	//	iHorizontalResolution = horizontalResolution;
	//}
	//
	//public void setVerticalResolution(int verticallResolution) {
	//	iVerticalResolution = verticallResolution;
	//}
	//
	//public void setResolution(int x, int y) {
	//	iHorizontalResolution = x;
	//	iVerticalResolution = y;
	//}
	
	public void setFoculBlur(boolean enabled) {
		bFocalBlur = enabled;
	}
	
	public void setMotionBlur(boolean enabled) {
		bMotionBlur = enabled;
	}
	
	
	//public void setRotation(Rotation3f r) {
	//	//rotation.set(
	//	//	(r.x > 0) ? r.x % (2 * Math.PI) : 2 * Math.PI + (r.x % (2 * Math.PI)),
	//	//	(r.y > 0) ? r.y % (2 * Math.PI) : 2 * Math.PI + (r.y % (2 * Math.PI)),
	//	//	(r.z > 0) ? r.z % (2 * Math.PI) : 2 * Math.PI + (r.z % (2 * Math.PI)),
	//	//);
	//	rotation.set(r);
	//	m4Transform.setRotationScale(rotation.getRotationMatrix());
	//}	
	
	public float getFieldOfView() {
		return (float) Math.atan(fFilmSize / 2 / fFocalLength) / (float) Math.PI * 360;
	}
	
	public float getFocalLength() {
		return fFocalLength;
	}
	
	public float getAperture() {
		return fAperture;
	}
	
	public double getFocus() {
		return dFocus;
	}
	
	public float getFrameRate() {
		return fFrameRate;
	}
	
	public float getShutterSpeed() {
		return fShutterSpeed;
	}
	
	public float getExposure() {
		return fExposure;
	}
	
	public float getFilmSize() {
		return fFilmSize;
	}
	
	//public double getAspectWidth() {
	//	return dAspectWidth;
	//}
	//
	//public double getAspectHeight() {
	//	return dAspectHeight;
	//}
	//
	//public double getAspectRatio() {
	//	return dAspectWidth / dAspectHeight;
	//}
	
	//public String getAspectRatioString() {
	//	if (dAspectWidth / dAspectHeight == 4.0 / 3.0) return "TV";
	//	else if (dAspectWidth / dAspectHeight == 1.66) return "narrowscreen";
	//	else if (dAspectWidth / dAspectHeight == 16.0 / 9.0) return "HDTV";
	//	else if (dAspectWidth / dAspectHeight == 1.85) return "widescreen";
	//	else if (dAspectWidth / dAspectHeight == 2.35) return "cinemascope";
	//	String s = "";
	//	if (dAspectWidth % 1 == 0.0) s += (int) dAspectWidth;
	//	else s += dAspectWidth;
	//	s += ":";
	//	if (dAspectHeight % 1 == 0.0) s += (int) dAspectHeight;
	//	else s+= dAspectHeight;
	//	return s;
	//}
	
	//public int getHorizontalResolution() {
	//	return iHorizontalResolution;
	//}
	//
	//public int getVerticalResolution() {
	//	return iVerticalResolution;
	//}
	
	public boolean isFoculBlur() {
		return bFocalBlur;
	}
	
	public boolean isMotionBlur() {
		return bMotionBlur;
	}
}
