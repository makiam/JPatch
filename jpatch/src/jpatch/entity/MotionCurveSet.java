package jpatch.entity;

import jpatch.boundary.*;

import java.io.PrintStream;
import java.util.*;

public class MotionCurveSet {
	public List<MotionCurve> motionCurveList = new ArrayList<MotionCurve>();
	public MotionCurve.Point3d position;
	public MotionCurve.Quat4f orientation;
	AnimObject animObject;
	
	public static MotionCurveSet createMotionCurveSetFor(AnimObject animObject) {
		if (animObject instanceof AnimModel)
			return new MotionCurveSet.Model((AnimModel) animObject);
		else if (animObject instanceof AnimLight)
			return new MotionCurveSet.Light((AnimLight) animObject);
		else if (animObject instanceof jpatch.entity.OLDCamera)
			return new MotionCurveSet.Camera((jpatch.entity.OLDCamera) animObject);
		else
			throw new IllegalArgumentException();
	}
	
	private MotionCurveSet() { }
	
	private MotionCurveSet(AnimObject animObject) {
		this.animObject = animObject;
		float pos = 0; // FIXME;
		position = MotionCurve.createPositionCurve(new MotionKey.Point3d(pos, animObject.getPositionDouble()));
		orientation = MotionCurve.createOrientationCurve(new MotionKey.Quat4f(pos, animObject.getOrientation()));
		//MotionCurveSet.this.populateList();
	}
	
	public void setPosition(float pos) {
		animObject.setPosition(position.getPoint3dAt(pos));
		animObject.setOrientation(orientation.getQuat4fAt(pos));
	}
	
	public void updateCurves(float pos) {
		if (!position.getPoint3dAt(pos).equals(animObject.getPositionDouble())) position.setPoint3dAt(pos, animObject.getPositionDouble());
		if (!orientation.getQuat4fAt(pos).equals(animObject.getOrientation())) orientation.setQuat4fAt(pos, animObject.getOrientation());
	}
	
	public void xml(PrintStream out, String prefix) {
		position.xml(out, prefix, "type=\"position\"");
		orientation.xml(out, prefix, "type=\"orientation\" subtype=\"quaternion\"");
	}
	
	public void populateList() {
		motionCurveList.clear();
		motionCurveList.add(position);
		motionCurveList.add(orientation);
	}
	
	public static class Camera extends MotionCurveSet {
		public MotionCurve.Float focalLength;
		
		public Camera(jpatch.entity.OLDCamera camera) {
			super(camera);
			float pos = 0; // FIXME;
			focalLength = MotionCurve.createFocalLengthCurve(new MotionKey.Float(pos, camera.getFocalLength()));
			populateList();
		}
		
		public void setPosition(float pos) {
			super.setPosition(pos);
			((jpatch.entity.OLDCamera) animObject).setFocalLength(focalLength.getFloatAt(pos));
		}
		
		public void updateCurves(float pos) {
			super.updateCurves(pos);
			if (focalLength.getFloatAt(pos) != ((jpatch.entity.OLDCamera) animObject).getFocalLength()) focalLength.setFloatAt(pos, ((jpatch.entity.OLDCamera) animObject).getFocalLength());
		}
		
		public void xml(PrintStream out, String prefix) {
			super.xml(out, prefix);
			focalLength.xml(out, prefix, "type=\"focallength\"");
		}
		
		public void populateList() {
			super.populateList();
			motionCurveList.add(focalLength);
		}
	}
	
	public static class Light extends MotionCurveSet {
		public MotionCurve.Float size;
		public MotionCurve.Float intensity;
		public MotionCurve.Color3f color;
		
		public Light(AnimLight light) {
			super(light);
			float pos = 0; // FIXME;
			size = MotionCurve.createSizeCurve(new MotionKey.Float(pos, light.getSize()));
			intensity = MotionCurve.createIntensityCurve(new MotionKey.Float(pos, light.getIntensity()));
			color = MotionCurve.createColorCurve(new MotionKey.Color3f(pos, light.getColor()));
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
		
		public void xml(PrintStream out, String prefix) {
			super.xml(out, prefix);
			size.xml(out, prefix, "type=\"size\"");
			intensity.xml(out, prefix, "type=\"intensity\"");
			color.xml(out, prefix, "type=\"color\"");
		}
		
		public void populateList() {
			super.populateList();
			motionCurveList.add(size);
			motionCurveList.add(intensity);
			motionCurveList.add(color);
		}
	}
	
	public static class Model extends MotionCurveSet {
		public MotionCurve.Float scale;
		public MotionCurve.Object anchor;
		private Map<OLDMorph, MotionCurve.Float> map = new HashMap<OLDMorph, MotionCurve.Float>();
		private Map<String, OLDMorph> idMap = new HashMap<String, OLDMorph>();
		
		public Model(AnimModel animModel) {
			super(animModel);
			System.out.println("Model:");
			animModel.getModel().dump();
			float pos = 0; // FIXME;
			scale = MotionCurve.createScaleCurve(new MotionKey.Float(pos, animModel.getScale()));
			anchor = MotionCurve.createAnchorCurve(new MotionKey.Object(pos, null));
			for (Iterator it = animModel.getModel().getMorphList().iterator(); it.hasNext(); ) {
				OLDMorph morph = (OLDMorph) it.next();
				MotionCurve.Float morphCurve = MotionCurve.createMorphCurve(morph, new MotionKey.Float(pos, morph.getValue()));
				map.put(morph, morphCurve);
				idMap.put(morph.getId(), morph);
//				motionCurveList.add(morphCurve);
			}
			Set rootBoneSet = new HashSet();
			for (Iterator itBone = animModel.getModel().getBoneSet().iterator(); itBone.hasNext(); ) {
				OLDBone bone = ((OLDBone) itBone.next()).getRoot();
				System.out.println("**:" + bone);
				if (!rootBoneSet.contains(bone)) {
					rootBoneSet.add(bone);
					recursiveAddBoneDofs(bone, map, pos);
				}
			}
			populateList();
			System.out.println(motionCurveList);
			//populateList();
			System.out.println("new motioncurveset for " + animModel + " created. map = " + map);
		}
		
		public MotionCurve.Float morph(OLDMorph morph) {
			return (MotionCurve.Float) map.get(morph);
		}
		
		public void setMorphCurve(OLDMorph morph, MotionCurve.Float curve) {
			map.put(morph, curve);
		}
		
		public void setPosition(float pos) {
			super.setPosition(pos);
			((AnimModel) animObject).setScale(scale.getFloatAt(pos));
			((AnimModel) animObject).setAnchor((Transformable) anchor.getObjectAt(pos));
			for (Iterator it = ((AnimModel) animObject).getModel().getMorphList().iterator(); it.hasNext(); ) {
				OLDMorph morph = (OLDMorph) it.next();
//				morph.unapply();
				morph.presetValue(morph(morph).getFloatAt(pos));
//				morph.apply();
			}
			for (Iterator itBone = ((AnimModel) animObject).getModel().getBoneSet().iterator(); itBone.hasNext(); ) {
				for (Iterator itDof = ((OLDBone) itBone.next()).getDofs().iterator(); itDof.hasNext(); ) {
					RotationDof dof = (RotationDof) itDof.next();
					dof.presetValue(morph(dof).getFloatAt(pos));
				}
			}
			((AnimModel) animObject).getModel().applyMorphs();
			((AnimModel) animObject).getModel().setPose();
		}
		
		public void updateCurves(float pos) {
			super.updateCurves(pos);
			if (scale.getFloatAt(pos) != ((AnimModel) animObject).getScale()) scale.setFloatAt(pos, ((AnimModel) animObject).getScale());
		}
		
		public OLDMorph getMorphById(String id) {
			return idMap.get(id);
		}
		
		public void xml(PrintStream out, String prefix) {
			super.xml(out, prefix);
			scale.xml(out, prefix, "type=\"scale\" subtype=\"uniform\"");
			anchor.xml(out, prefix, "type=\"anchor\"");
//			int m = 0;
//			for (Iterator it = ((AnimModel) animObject).getModel().getMorphList().iterator(); it.hasNext(); ) {
//				((MotionCurve2.Float) map.get(it.next())).xml(sb, prefix, "type=\"morph\" morph=\"" + m++ + "\"");
//			}
			for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
				Object key = it.next();
				MotionCurve.Float mc = (MotionCurve.Float) map.get(key);
				mc.xml(out, prefix, "type=\"avar\" name=\"" + mc.getName() + "\"");
			}
		}
		
		public void populateList() {
			super.populateList();
			motionCurveList.add(anchor);
			motionCurveList.add(scale);
//			for (Iterator it = ((AnimModel) animObject).getModel().getMorphList().iterator(); it.hasNext(); ) {
//				Morph morph = (Morph) it.next();
//				System.out.println("populateList morph=" + morph);
//				motionCurveList.add(map.get(morph));
//			}
//			for (Iterator itBone = ((AnimModel) animObject).getModel().getBoneSet().iterator(); itBone.hasNext(); ) {
//				for (Iterator itDof = ((Bone) itBone.next()).getDofs().iterator(); itDof.hasNext(); ) {
//					RotationDof dof = (RotationDof) itDof.next();
//					System.out.println("populateList dof=" + dof);
//					motionCurveList.add(map.get(dof));
//				}
//			}
		}
		
		public String toString() {
			return map.toString();
		}
		
		private void recursiveAddBoneDofs(OLDBone bone, Map map, float pos) {
			System.out.println("\trecursiveAddBoneDofs bone=" + bone);
			for (Iterator itDofs = bone.getDofs().iterator(); itDofs.hasNext(); ) {
				RotationDof dof = (RotationDof) itDofs.next();
				System.out.println("\t\tdof=" + dof);
				MotionCurve.Float morphCurve = MotionCurve.createMorphCurve(dof, new MotionKey.Float(pos, dof.getValue()));
				map.put(dof, morphCurve);
				idMap.put(dof.getId(), dof);
//				motionCurveList.add(morphCurve);
			}
			for (Iterator itBones = bone.getChildBones().iterator(); itBones.hasNext(); ) {
				recursiveAddBoneDofs((OLDBone) itBones.next(), map, pos);
			}
		}
	}
}

