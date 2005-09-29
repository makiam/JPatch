package jpatch.boundary;

import java.util.*;

import javax.vecmath.*;

import jpatch.control.edit.*;
import jpatch.entity.*;

public class Selection extends JPatchTreeLeaf implements Transformable {
	public static final int CONTROLPOINTS = 1;
	public static final int MORPHS = 2;
	public static int NUM = 0;
	
	private final Map mapObjects = new HashMap();
	private Map mapTransformables;
	private Object hotObject;
	private int iDirection;
	private Matrix3f m3Orientation;
	private Point3f p3Pivot = new Point3f();
//	private int iNum = NUM++;
	private boolean bActive = false;
	
	public static Selection createRectangularPointSelection(int ax, int ay, int bx, int by, Matrix4f transformationMatrix, Model model) {
		Selection selection = new Selection();
		Point3f p3 = new Point3f();
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
		selection.p3Pivot.set(selection.getCenter());
		return (selection.mapObjects.size() > 0) ? selection : null;
	}
	
	private Selection() {
		super("NEW SELECTION");
		m3Orientation = new Matrix3f();
		m3Orientation.setIdentity();
	}
	
	public Selection(ControlPoint cp) {
		this();
		mapObjects.put(cp, new Float(1.0f));
		hotObject = cp;
	}
	
	public Selection(Map objectWeightMap) {
		this();
		mapObjects.putAll(objectWeightMap);
	}

	public Selection(Collection objects) {
		this();
		for (Iterator it = objects.iterator(); it.hasNext(); ) {
			mapObjects.put(it.next(), new Float(1.0f));
		}
	}

	public int getNodeType() {
		return SELECTION;
	}

	public Set getObjects() {
		return mapObjects.keySet();
	}

	public Map getMap() {
		return mapObjects;
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
			Object object = it.next();
			if (object instanceof ControlPoint) {
				p3.set(((ControlPoint) object).getPosition());
				m3.transform(p3);
				if (p3.x > xMax) xMax = p3.x;
				if (p3.x < xMin) xMin = p3.x;
				if (p3.y > yMax) yMax = p3.y;
				if (p3.y < yMin) yMin = p3.y;
				if (p3.z > zMax) zMax = p3.z;
				if (p3.z < zMin) zMin = p3.z;
			}
		}
		p0.set(xMin,yMin,zMin);
		p1.set(xMax,yMax,zMax);
	}
	
	public Point3f getCenter() {
		Point3f p0 = new Point3f();
		Point3f p1 = new Point3f();
		getBounds(p0, p1);
		p0.interpolate(p1, 0.5f);
		return p0;
	}
	
	public Point3f getPivot() {
		return p3Pivot;
	}
	
	public void setPivot(Point3f pivot) {
		p3Pivot = pivot;
	}
	
	public void arm(int mask) {
		mapTransformables = new HashMap();
		if ((mask & CONTROLPOINTS) != 0) {
			for (Iterator it = mapObjects.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				mapTransformables.put(key, mapObjects.get(key));
			}
		}
		if ((mask & MORPHS) != 0) {
			for (Iterator it = MainFrame.getInstance().getModel().getMorphIterator(); it.hasNext();) {
				Transformable transformable = ((Morph) it.next()).getTransformable(mapObjects);
				if (transformable != null)
					mapTransformables.put(transformable, new Float(1.0f));

			}
		}
	}

	public void beginTransform() {
		for (Iterator it = mapTransformables.keySet().iterator(); it.hasNext(); ((Transformable) it.next()).beginTransform());
	}

	public void translate(Vector3f v) {
		Vector3f vector = new Vector3f();
		for (Iterator it = mapTransformables.keySet().iterator(); it.hasNext(); ) {
			Transformable transformable = (Transformable) it.next();
			float weight = ((Float) mapTransformables.get(transformable)).floatValue();
			vector.set(v);
			vector.scale(weight);
			transformable.translate(vector);
		}
	}

	public void rotate(Quat4f q, Point3f pivot) {
		Quat4f identity = new Quat4f(1, 0, 0, 0);
		Quat4f quat = new Quat4f();
		for (Iterator it = mapTransformables.keySet().iterator(); it.hasNext(); ) {
			Transformable transformable = (Transformable) it.next();
			float weight = ((Float) mapTransformables.get(transformable)).floatValue();
			quat.set(q);
			quat.interpolate(identity, 1.0 - weight);
			transformable.rotate(quat, pivot);
		}
	}

	public void transform(Matrix3f m, Point3f pivot) {
		Matrix3f identity = new Matrix3f();
		Matrix3f matrix = new Matrix3f();
		for (Iterator it = mapTransformables.keySet().iterator(); it.hasNext(); ) {
			Transformable transformable = (Transformable) it.next();
			float weight = ((Float) mapTransformables.get(transformable)).floatValue();
			matrix.set(m);
			matrix.mul(weight);
			identity.setIdentity();
			identity.mul(1 - weight);
			matrix.add(identity);
			transformable.transform(matrix, pivot);
		}
	}

	public JPatchUndoableEdit endTransform() {
		JPatchActionEdit edit = new JPatchActionEdit("transform selection");
		for (Iterator it = mapTransformables.keySet().iterator(); it.hasNext(); ) {
			Transformable transformable = (Transformable) it.next();
			edit.addEdit(transformable.endTransform());
		}
		return edit;
	}
	
	public Selection cloneSelection() {
		Selection selection = new Selection(mapObjects);
		selection.p3Pivot.set(p3Pivot);
		selection.m3Orientation.set(m3Orientation);
		selection.hotObject = hotObject;
		selection.iDirection = iDirection;
		return selection;
	}
	
	public boolean equals(Object object) {
		if (object == null)
			return false;
		if (object == this)
			return true;
		Selection selection = (Selection) object;
		if (!mapObjects.equals(selection.mapObjects))
			return false;
		if (!(iDirection == selection.iDirection))
			return false;
		return true;
	}
	
	public int hashCode() {
		return mapObjects.hashCode();
	}
	
	public String toString() {
//		return "Selection " + getName() + " (" + iNum + ")";
		return getName();
	}
	
	public StringBuffer xml(String prefix) {
		StringBuffer sb = new StringBuffer();
		StringBuffer cpList = new StringBuffer();
		StringBuffer cpWeightList = new StringBuffer();
		sb.append(prefix).append("<selection name=\"" + getName() + "\">\n");
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
		cpList.setLength(cpList.length() - 1); // remove last ","
		cpWeightList.setLength(cpWeightList.length() - 1); // remove last ","
		sb.append(prefix).append("\t<points>").append(cpList).append("</points>\n");
		sb.append(prefix).append("\t<pointweights>").append(cpWeightList).append("</pointweights>\n");
		sb.append(prefix).append("</selection>").append("\n");
		return sb;
	}
}
