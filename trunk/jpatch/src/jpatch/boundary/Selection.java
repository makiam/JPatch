package jpatch.boundary;

import java.util.*;

import javax.vecmath.*;

import jpatch.auxilary.Utils3D;
import jpatch.control.edit.*;
import jpatch.entity.*;
import jpatch.entity.Bone.BoneTransformable;

public class Selection extends JPatchTreeLeaf {
	public static final int CONTROLPOINTS = 1;
	public static final int MORPHS = 2;
	public static final int BONES = 4;
	public static final int MORPHTARGET = 8;
	public static int NUM = 0;
	
	private final Map mapObjects = new HashMap();
	private Map mapTransformables = new HashMap();
	private Object hotObject;
	private int iDirection;
	private Matrix3f m3Orientation;
	private Point3f p3Pivot = new Point3f();
	private Transformable pivotTransformable = new PivotTransformable();
	private float fPivotWeight = 1;
//	private int iNum = NUM++;
	private boolean bActive = false;
	
	public static Selection createRectangularPointSelection(int ax, int ay, int bx, int by, Matrix4f transformationMatrix, Model model, int mask) {
		Selection selection = new Selection();
		Point3f p3 = new Point3f();
		if ((mask & CONTROLPOINTS) != 0 && MainFrame.getInstance().getJPatchScreen().isSelectPoints()) {
			for (Iterator it = model.getCurveSet().iterator(); it.hasNext(); ) {
				for (ControlPoint cp = (ControlPoint) it.next(); cp != null; cp = cp.getNextCheckNextLoop()) {
					if (cp.isHead() && !cp.isHidden() && !cp.isStartHook() && !cp.isEndHook()) {
					//if (cp.isHead() && !cp.isHidden()) {
						p3.set(cp.getPosition());
						transformationMatrix.transform(p3);
						if (p3.x >= ax && p3.x <= bx && p3.y >= ay && p3.y <= by) {
							selection.mapObjects.put(cp, new Float(1.0f));
						}
					}
				}
			}
		}
		if ((mask & BONES) != 0 && MainFrame.getInstance().getJPatchScreen().isSelectBones()) {
			for (Iterator it = model.getBoneSet().iterator(); it.hasNext(); ) {
				Bone bone = (Bone) it.next();
				if (bone.getParentBone() == null) {
					bone.getStart(p3);
					transformationMatrix.transform(p3);
					if (p3.x >= ax && p3.x <= bx && p3.y >= ay && p3.y <= by)
						selection.mapObjects.put(bone.getBoneStart(), new Float(1.0f));
				}
				bone.getEnd(p3);
				transformationMatrix.transform(p3);
				if (p3.x >= ax && p3.x <= bx && p3.y >= ay && p3.y <= by)
					selection.mapObjects.put(bone.getBoneEnd(), new Float(1.0f));
			}
		}
		selection.p3Pivot.set(selection.getCenter());
		return (selection.mapObjects.size() > 0) ? selection : null;
	}
	
	private Selection() {
		this("NEW SELECTION");
		m3Orientation = new Matrix3f();
		m3Orientation.setIdentity();
	}
	
	public Selection(String name) {
		super(name);
	}
	
	public Selection(Object object) {
		this();
		mapObjects.put(object, new Float(1.0f));
		hotObject = object;
	}
	
	public Selection(Map objectWeightMap) {
		this();
		mapObjects.putAll(objectWeightMap);
		p3Pivot.set(getCenter());
	}

	public Selection(Collection objects) {
		this();
		for (Iterator it = objects.iterator(); it.hasNext(); )
			mapObjects.put(it.next(), new Float(1.0f));
		p3Pivot.set(getCenter());
	}

	public Set getObjects() {
		return mapObjects.keySet();
	}

	public Map getMap() {
		return mapObjects;
	}
	
	public void applyMask(int mask) {
		boolean cps = (mask & CONTROLPOINTS) != 0;
		boolean bones = (mask & BONES) != 0;
		for (Iterator it = new HashSet(mapObjects.keySet()).iterator(); it.hasNext(); ) {
			Object key = it.next();
			if (!cps && key instanceof ControlPoint)
				mapObjects.remove(key);
			if (!bones && key instanceof Bone.BoneTransformable)
				mapObjects.remove(key);
		}
	}
	
	public void setPivotWeight(float weight) {
		fPivotWeight = weight;
	}
	
	public void setHotObject(Object object) {
		if (mapObjects.keySet().contains(object) || object == null)
			hotObject = object;
		else
			throw new IllegalArgumentException("Object " + object.toString() + " is not contained in this selection");
	}

	public Object getHotObject() {
		if (isSingle()) {
			Iterator it = mapObjects.keySet().iterator();
			return it.next();
		}
		return hotObject;
	}

	public ControlPoint[] getControlPointArray() {
		ArrayList points = new ArrayList();
		for (Iterator it = mapObjects.keySet().iterator(); it.hasNext(); ) {
			Object object = it.next();
			if (object instanceof ControlPoint)
				points.add(object);
		}
		ControlPoint[] array = new ControlPoint[points.size()];
		return (ControlPoint[]) points.toArray(array);
	}
	
	public int getDirection() {
		return iDirection;
	}

	public void setDirection(int direction) {
		iDirection = direction;
	}

	public Matrix3f getOrientation() {
		return m3Orientation;
	}
	
	public boolean contains(Object object) {
		return mapObjects.containsKey(object);
	}
	
	public boolean containsBone(Bone bone) {
		Bone parent = bone.getParentBone();
		if (parent != null)
			return (mapObjects.containsKey(parent.getBoneEnd()) && mapObjects.containsKey(bone.getBoneEnd()));
		else
			return (mapObjects.containsKey(bone.getBoneStart()) && mapObjects.containsKey(bone.getBoneEnd()));
	}
	
	public boolean isSingle() {
		return mapObjects.size() == 1;
	}
	
	public boolean isActive() {
		return bActive;
	}
	
	public void setActive(boolean active) {
		bActive = active;
	}
	
	public void getBounds(Point3f p0, Point3f p1) {
		float xMax = -Float.MAX_VALUE;
		float xMin = Float.MAX_VALUE;
		float yMax = -Float.MAX_VALUE;
		float yMin = Float.MAX_VALUE;
		float zMax = -Float.MAX_VALUE;
		float zMin = Float.MAX_VALUE;
		Point3f p3 = new Point3f();
		Matrix3f m3 = new Matrix3f(m3Orientation);
		m3.invert();
		for (Iterator it = mapObjects.keySet().iterator(); it.hasNext(); ) {
			p3.set(((Transformable) it.next()).getPosition());
			m3.transform(p3);
			if (p3.x > xMax) xMax = p3.x;
			if (p3.x < xMin) xMin = p3.x;
			if (p3.y > yMax) yMax = p3.y;
			if (p3.y < yMin) yMin = p3.y;
			if (p3.z > zMax) zMax = p3.z;
			if (p3.z < zMin) zMin = p3.z;
		}
		p0.set(xMin,yMin,zMin);
		p1.set(xMax,yMax,zMax);
	}
	
	public Point3f getCenter() {
		Bone bone = null;
		for (Iterator it = mapObjects.keySet().iterator(); it.hasNext(); ) {
			Object object = it.next();
			if (object instanceof BoneTransformable) {
				bone = ((BoneTransformable) object).getBone();
				break;
			}
		}
		if (bone == null) {
			Point3f p0 = new Point3f();
			Point3f p1 = new Point3f();
			getBounds(p0, p1);
			p0.interpolate(p1, 0.5f);
//			return recurseGetCenter(p0, 10);
			return p0;
		} else {
			while (bone.getParentBone() != null && (mapObjects.containsKey(bone.getParentBone().getBoneEnd())))
				bone = bone.getParentBone();
			//System.out.println(bone);
			//return bone.getStart(null);
			return mapObjects.containsKey(bone.getBoneStart()) ? bone.getStart(null) :  bone.getEnd(null);
		}
	}
	
//	private Point3f recurseGetCenter(final Point3f center, int level) {
//		List list = new ArrayList(mapObjects.keySet());
//		Collections.sort(list, new Comparator() {
//			public int compare(Object o0, Object o1) {
//				float d0 = center.distanceSquared(((Transformable) o0).getPosition());
//				float d1 = center.distanceSquared(((Transformable) o1).getPosition());
//				return d0 < d1 ? 1 : d0 > d1 ? -1 : 0;
//			}
//		});
////		Point3f p = new Point3f(((Transformable) list.get(0)).getPosition());
////		p.add(((Transformable) list.get(1)).getPosition());
////		p.add(((Transformable) list.get(2)).getPosition());
////		p.scale(1f / 3f);
//		Point3f p = Utils3D.circumCenter(
//				((Transformable) list.get(0)).getPosition(),
//				((Transformable) list.get(1)).getPosition(),
//				((Transformable) list.get(2)).getPosition()
//		);
//		System.out.println(p);
//		if (!p.equals(center)) {
//			p.interpolate(center, 0.5f);
//			return recurseGetCenter(p, level--);
//		} else {
//			return p;
//		}
//	}
	
	public Point3f getPivot() {
		return p3Pivot;
	}
	
	public void setPivot(Point3f pivot) {
		p3Pivot = pivot;
	}
	
	public void arm(int mask) {
//		System.out.println("Selection.arm(" + mask + ")");
		mapTransformables.clear();
		if (((mask & CONTROLPOINTS) != 0) || ((mask & BONES) != 0)) {
			boolean cps = (mask & CONTROLPOINTS) != 0;
			boolean bones = (mask & BONES) != 0;
			for (Iterator it = mapObjects.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				if (cps && key instanceof ControlPoint)
					mapTransformables.put(key, mapObjects.get(key));
				if (bones && key instanceof Bone.BoneTransformable)
					mapTransformables.put(key, mapObjects.get(key));
			}
		}
		if ((mask & MORPHS) != 0) {
			for (Iterator itMorphs = MainFrame.getInstance().getModel().getMorphIterator(); itMorphs.hasNext(); ) {
				Morph moprh = (Morph) itMorphs.next();
				for (Iterator itTargets = moprh.getTargets().iterator(); itTargets.hasNext(); ) {
					Transformable transformable = ((MorphTarget) itTargets.next()).getTransformable(mapObjects, false);
					if (transformable != null)
						mapTransformables.put(transformable, new Float(1.0f));
				}
			}
		}
		if ((mask & MORPHTARGET) != 0) {
			mapTransformables.put(MainFrame.getInstance().getEditedMorph().getTransformable(mapObjects, true), new Float(1.0f));
		}
	}

	public void beginTransform() {
//		System.out.println("beginTransform: " + mapTransformables);
		for (Iterator it = mapTransformables.keySet().iterator(); it.hasNext(); )
			((Transformable) it.next()).beginTransform();
		pivotTransformable.beginTransform();
	}

	public void translate(Vector3f v) {
		Vector3f vector = new Vector3f();
		for (Iterator it = mapTransformables.keySet().iterator(); it.hasNext(); ) {
			Transformable transformable = (Transformable) it.next();
			float weight = ((Float) mapTransformables.get(transformable)).floatValue();
			vector.scale(weight, v);
			transformable.translate(vector);
		}
		pivotTransformable.translate(vector);
	}

	public void rotate(AxisAngle4f a, Point3f pivot) {
		AxisAngle4f aa = new AxisAngle4f(a);
		for (Iterator it = mapTransformables.keySet().iterator(); it.hasNext(); ) {
			Transformable transformable = (Transformable) it.next();
			float weight = ((Float) mapTransformables.get(transformable)).floatValue();
			aa.angle = a.angle * weight;
			transformable.rotate(aa, pivot);
		}
//		pivotTransformable.rotate(a, pivot);
	}
	
	public void transform(Matrix3f m, Point3f pivot) {
		Matrix3f matrix = new Matrix3f();
		for (Iterator it = mapTransformables.keySet().iterator(); it.hasNext(); ) {
			Transformable transformable = (Transformable) it.next();
			float weight = ((Float) mapTransformables.get(transformable)).floatValue();
			weightMatrix(m, weight, matrix);
			transformable.transform(matrix, pivot);
		}
//		pivotTransformable.transform(m, pivot);
	}

	private void weightMatrix(Matrix3f matrix, float weight, Matrix3f weightedMatrix) {
		weightedMatrix.m00 = matrix.m00 * weight + 1 - weight;
		weightedMatrix.m01 = matrix.m01 * weight;
		weightedMatrix.m02 = matrix.m02 * weight;
		weightedMatrix.m10 = matrix.m10 * weight;
		weightedMatrix.m11 = matrix.m11 * weight + 1 - weight;
		weightedMatrix.m12 = matrix.m12 * weight;
		weightedMatrix.m20 = matrix.m20 * weight;
		weightedMatrix.m21 = matrix.m21 * weight;
		weightedMatrix.m22 = matrix.m22 * weight + 1 - weight;	
	}
	
	public JPatchUndoableEdit endTransform() {
		JPatchActionEdit edit = new JPatchActionEdit("transform selection");
		for (Iterator it = mapTransformables.keySet().iterator(); it.hasNext(); ) {
			Transformable transformable = (Transformable) it.next();
			JPatchUndoableEdit transformEdit = transformable.endTransform();
			if (transformEdit != null)
				edit.addEdit(transformEdit);
		}
		edit.addEdit(pivotTransformable.endTransform());
		return edit;
	}
	
	public Selection cloneSelection() {
		Selection selection = new Selection(mapObjects);
		selection.mapObjects.remove(pivotTransformable);
		selection.p3Pivot.set(p3Pivot);
		selection.m3Orientation.set(m3Orientation);
		selection.hotObject = hotObject;
		selection.iDirection = iDirection;
		return selection;
	}
	
//	public boolean equals(Object object) {
//		if (object == null)
//			return false;
//		return mapObjects.equals(((Selection) object).mapObjects);
////		if (object == this)
////			return true;
////		Selection selection = (Selection) object;
//////		System.out.println("comparting " + this + " with " + selection);
//////		System.out.println(mapObjects);
//////		System.out.println(selection.mapObjects);
//////		System.out.println(mapObjects.equals(selection.mapObjects));
////		if (mapObjects.size() != selection.mapObjects.size())
////			return false;
////		for (Iterator it = mapObjects.keySet().iterator(); it.hasNext(); ) {
////			Object o = it.next();
////			if (o != pivotTransformable && !selection.contains(o))
////				return false;
////		}
//////		if (!mapObjects.equals(selection.mapObjects))
//////			return false;
////		if (!(iDirection == selection.iDirection))
////			return false;
////		return true;
//	}
//	
//	public int hashCode() {
//		return mapObjects.hashCode();
//	}
	
	public String toString() {
//		return "Selection " + getName() + " (" + iNum + ")";
		return strName;
	}
	
	public String getName() {
		return strName;
	}
	
	public StringBuffer xml(String prefix) {
		StringBuffer sb = new StringBuffer();
		StringBuffer cpList = new StringBuffer();
		StringBuffer cpWeightList = new StringBuffer();
		sb.append(prefix).append("<selection name=\"" + strName + "\">\n");
		//int size = getType();
		//int p = 0;
		for (Iterator it = mapObjects.keySet().iterator(); it.hasNext();) {
			Object object = it.next();
			if (object instanceof ControlPoint) {
				ControlPoint cp = (ControlPoint) object;
				cpList.append(cp.getXmlNumber()).append(",");
				cpWeightList.append(mapObjects.get(cp).toString()).append(",");
			}			
		}
		if (cpList.length() > 0)
			cpList.setLength(cpList.length() - 1); // remove last ","
		if (cpWeightList.length() > 0)
			cpWeightList.setLength(cpWeightList.length() - 1); // remove last ","
		sb.append(prefix).append("\t<points>").append(cpList).append("</points>\n");
		sb.append(prefix).append("\t<pointweights>").append(cpWeightList).append("</pointweights>\n");
		sb.append(prefix).append("</selection>").append("\n");
		return sb;
	}
	
	private class PivotTransformable implements Transformable {
		private Point3f p3Temp = new Point3f();
		public Point3f getPosition() {
			return p3Pivot;
		}

		public void beginTransform() {
			p3Temp.set(p3Pivot);
		}

		public void translate(Vector3f v) {
			p3Pivot.set(p3Temp);
			p3Pivot.x += v.x * fPivotWeight;
			p3Pivot.y += v.y * fPivotWeight;
			p3Pivot.z += v.z * fPivotWeight;
		}

		public void rotate(AxisAngle4f a, Point3f pivot) {
		}

		public void transform(Matrix3f m, Point3f pivot) {
		}

		public JPatchUndoableEdit endTransform() {
			return new AtomicModifySelection.Pivot(Selection.this, p3Temp);
		}
	}
}
