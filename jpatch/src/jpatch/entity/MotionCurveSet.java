package jpatch.entity;

import jpatch.boundary.*;

import java.util.*;

public class MotionCurveSet {
	public List motionCurveList = new ArrayList();
	public MotionCurve2.Point3d position;
	public MotionCurve2.Quat4f orientation;
	AnimObject animObject;
	
	public static MotionCurveSet createMotionCurveSetFor(AnimObject animObject) {
		if (animObject.getClass().equals(AnimModel.class)) return new MotionCurveSet.Model((AnimModel) animObject);
		else if (animObject.getClass().equals(AnimLight.class)) return new MotionCurveSet.Light((AnimLight) animObject);
		else if (animObject.getClass().equals(Camera.class)) return new MotionCurveSet.Camera((jpatch.entity.Camera) animObject);
		else throw new IllegalArgumentException();
	}
	
	private MotionCurveSet() { }
	
	private MotionCurveSet(AnimObject animObject) {
		this.animObject = animObject;
		float pos = Animator.getInstance().getStart();
		position = MotionCurve2.createPositionCurve(new MotionKey2.Point3d(pos, animObject.getPosition()));
		orientation = MotionCurve2.createOrientationCurve(new MotionKey2.Quat4f(pos, animObject.getOrientation()));
		//MotionCurveSet.this.populateList();
	}
	
	public void setPosition(float pos) {
		animObject.setPosition(position.getPoint3dAt(pos));
		animObject.setOrientation(orientation.getQuat4fAt(pos));
	}
	
	public void updateCurves(float pos) {
		if (!position.getPoint3dAt(pos).equals(animObject.getPosition())) position.setPoint3dAt(pos, animObject.getPosition());
		if (!orientation.getQuat4fAt(pos).equals(animObject.getOrientation())) orientation.setQuat4fAt(pos, animObject.getOrientation());
	}
	
	public void xml(StringBuffer sb, String prefix) {
		position.xml(sb, prefix, "type=\"position\"");
		orientation.xml(sb, prefix, "type=\"orientation\" subtype=\"quaternion\"");
	}
	
	public void populateList() {
		motionCurveList.clear();
		motionCurveList.add(position);
		motionCurveList.add(orientation);
	}
	
	public static class Camera extends MotionCurveSet {
		public MotionCurve2.Float focalLength;
		
		public Camera(jpatch.entity.Camera camera) {
			super(camera);
			float pos = Animator.getInstance().getStart();
			focalLength = MotionCurve2.createFocalLengthCurve(new MotionKey2.Float(pos, camera.getFocalLength()));
			populateList();
		}
		
		public void setPosition(float pos) {
			super.setPosition(pos);
			((jpatch.entity.Camera) animObject).setFocalLength(focalLength.getFloatAt(pos));
		}
		
		public void updateCurves(float pos) {
			super.updateCurves(pos);
			if (focalLength.getFloatAt(pos) != ((jpatch.entity.Camera) animObject).getFocalLength()) focalLength.setFloatAt(pos, ((jpatch.entity.Camera) animObject).getFocalLength());
		}
		
		public void xml(StringBuffer sb, String prefix) {
			super.xml(sb, prefix);
			focalLength.xml(sb, prefix, "type=\"focallength\"");
		}
		
		public void populateList() {
			super.populateList();
			motionCurveList.add(focalLength);
		}
	}
	
	public static class Light extends MotionCurveSet {
		public MotionCurve2.Float size;
		public MotionCurve2.Float intensity;
		public MotionCurve2.Color3f color;
		
		public Light(AnimLight light) {
			super(light);
			float pos = Animator.getInstance().getStart();
			size = MotionCurve2.createSizeCurve(new MotionKey2.Float(pos, light.getSize()));
			intensity = MotionCurve2.createIntensityCurve(new MotionKey2.Float(pos, light.getIntensity()));
			color = MotionCurve2.createColorCurve(new MotionKey2.Color3f(pos, light.getColor()));
			populateList();
		}
		
		public void setPosition(float pos) {
			super.setPosition(pos);
			((AnimLight) animObject).setSize(size.getFloatAt(pos));
			((AnimLight) animObject).setIntensity(intensity.getFloatAt(pos));
			((AnimLight) animObject).setColor(color.getColor3fAt(pos));
		}
		
		public void updateCurves(float pos) {
			super.updateCurves(pos);
			AnimLight light = (AnimLight) animObject;
			if (size.getFloatAt(pos) != light.getSize()) size.setFloatAt(pos, light.getSize());
			if (intensity.getFloatAt(pos) != light.getIntensity()) intensity.setFloatAt(pos, light.getIntensity());
			System.out.println(color.getColor3fAt(pos));
			System.out.println(light.getColor());
			System.out.println();
			if (!color.getColor3fAt(pos).equals(light.getColor())) color.setColor3fAt(pos, light.getColor());
		}
		
		public void xml(StringBuffer sb, String prefix) {
			super.xml(sb, prefix);
			size.xml(sb, prefix, "type=\"size\"");
			intensity.xml(sb, prefix, "type=\"intensity\"");
			color.xml(sb, prefix, "type=\"color\"");
		}
		
		public void populateList() {
			super.populateList();
			motionCurveList.add(size);
			motionCurveList.add(intensity);
			motionCurveList.add(color);
		}
	}
	
	public static class Model extends MotionCurveSet {
		public MotionCurve2.Float scale;
		private HashMap map = new HashMap();
		
		public Model(AnimModel animModel) {
			super(animModel);
			float pos = Animator.getInstance().getStart();
			scale = MotionCurve2.createScaleCurve(new MotionKey2.Float(pos, animModel.getScale()));
			for (Iterator it = animModel.getModel().getMorphList().iterator(); it.hasNext(); ) {
				MorphTarget morph = (MorphTarget) it.next();
				MotionCurve2.Float morphCurve = MotionCurve2.createMorphCurve(morph, new MotionKey2.Float(pos, morph.getValue()));
				map.put(morph, morphCurve);
			}
			populateList();
		}
		
		public MotionCurve2.Float morph(MorphTarget morph) {
			return (MotionCurve2.Float) map.get(morph);
		}
		
		public void setMorphCurve(MorphTarget morph, MotionCurve2.Float curve) {
			map.put(morph, curve);
		}
		
		public void setPosition(float pos) {
			super.setPosition(pos);
			((AnimModel) animObject).setScale(scale.getFloatAt(pos));
			for (Iterator it = ((AnimModel) animObject).getModel().getMorphList().iterator(); it.hasNext(); ) {
				MorphTarget morph = (MorphTarget) it.next();
				morph.unapply();
				morph.setValue(morph(morph).getFloatAt(pos));
				morph.apply();
			}
		}
		
		public void updateCurves(float pos) {
			super.updateCurves(pos);
			if (scale.getFloatAt(pos) != ((AnimModel) animObject).getScale()) scale.setFloatAt(pos, ((AnimModel) animObject).getScale());
		}
		
		public void xml(StringBuffer sb, String prefix) {
			super.xml(sb, prefix);
			scale.xml(sb, prefix, "type=\"scale\" subtype=\"uniform\"");
			int m = 0;
			for (Iterator it = ((AnimModel) animObject).getModel().getMorphList().iterator(); it.hasNext(); ) {
				((MotionCurve2.Float) map.get(it.next())).xml(sb, prefix, "type=\"morph\" morph=\"" + m++ + "\"");
			}
		}
		
		public void populateList() {
			super.populateList();
			motionCurveList.add(scale);
			for (Iterator it = ((AnimModel) animObject).getModel().getMorphList().iterator(); it.hasNext(); ) {
				MorphTarget morph = (MorphTarget) it.next();
				motionCurveList.add(map.get(morph));
			}
		}
	}
}

