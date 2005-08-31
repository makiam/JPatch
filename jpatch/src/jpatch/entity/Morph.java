package jpatch.entity;

import java.util.*;
import javax.vecmath.*;

import jpatch.auxilary.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.PointSelection;
import jpatch.control.edit.ChangeMorphVectorsEdit;
import jpatch.control.edit.ChangeSelectionPivotEdit;
import jpatch.control.edit.JPatchAbstractUndoableEdit;
import jpatch.control.edit.JPatchUndoableEdit;

public class Morph extends JPatchTreeLeaf {
//	private ArrayList listPoints = new ArrayList();
//	private ArrayList listVectors = new ArrayList();
	private float fValue = 0;
	private float fMin = 0;
	private float fMax = 1;
	private Map mapMorph = new HashMap();
	private Map mapPositions = new HashMap();
	private boolean bPrepared = false;
	
	public Morph(int type, String name) {
		iNodeType = type;
		strName = name;
	}
	
	public Transformable getTransformable(final Map selectedPoints) {
		final HashMap changeMap = new HashMap();
		boolean ok = false;
		loop:
		for (Iterator it = mapMorph.keySet().iterator(); it.hasNext(); ) {
			Object key = it.next();
			if (selectedPoints.keySet().contains(key)) {
				
				ok = true;
				break loop;
			}
		}
		if (!ok)
			return null;
		return new Transformable() {
			public void beginTransform() {
				changeMap.clear();
				for (Iterator it = mapMorph.keySet().iterator(); it.hasNext(); ) {
					Object key = it.next();
					if (selectedPoints.keySet().contains(key))
						changeMap.put(key, new Vector3f((Vector3f) mapMorph.get(key)));
				}
			}
			public void translate(Vector3f v) { }
			public void rotate(Quat4f q, Point3f pivot) {
				Quat4f identity = new Quat4f(1, 0, 0, 0);
				Quat4f quat = new Quat4f();
				Matrix3f rot = new Matrix3f();
				for (Iterator it = changeMap.keySet().iterator(); it.hasNext(); ) {
					Object key = it.next();
					Vector3f v = (Vector3f) changeMap.get(key);
					v.set((Vector3f) mapMorph.get(key));
					float weight = ((Float) selectedPoints.get(key)).floatValue();
					quat.set(q);
					quat.interpolate(identity, 1.0 - weight);
					rot.set(quat);
					rot.transform(v);
				}
			}
			public void transform(Matrix3f m, Point3f pivot) {
				Matrix3f matrix = new Matrix3f();
				for (Iterator it = changeMap.keySet().iterator(); it.hasNext(); ) {
					Object key = it.next();
					Vector3f v = (Vector3f) changeMap.get(key);
					v.set((Vector3f) mapMorph.get(key));
					float weight = ((Float) selectedPoints.get(key)).floatValue();
					matrix.set(m);
					matrix.mul(weight);
					matrix.transform(v);
				}
			}
			public JPatchUndoableEdit endTransform() {
				return new ChangeMorphVectorsEdit(Morph.this, changeMap);
			}
		};
	}
	
	public void setMin(float min) {
		fMin = min;
	}
	
	public void setMax(float max) {
		fMax = max;
	}
	
	public float getMin() {
		return fMin;
	}
	
	public float getMax() {
		return fMax;
	}
	
	public float getValue() {
		return fValue;
	}
	
	public void setValue(float value) {
		fValue = value;
	}
	
	public int getSliderValue() {
		return (int) ((fValue - fMin) / (fMax - fMin) * 100f);
	}
	
	public void setSliderValue(int sliderValue) {
		fValue = fMin + (fMax - fMin) / 100f * (float) sliderValue;
	}
	
//	public List getPointList() {
////		return new ArrayList(listPoints);
//		return listPoints;
//	}
//	
//	public List getVectorList() {
////		return new ArrayList(listVectors);
//		return listVectors;
//	}
//	
//	public void setPointList(List points) {
//		listPoints.clear();
//		listPoints.addAll(points);
//	}
//	
//	public void setVectorList(List vectors) {
//		listVectors.clear();
//		listVectors.addAll(vectors);
//	}
	
	public Map getMorphMap() {
		return mapMorph;
	}
	
	public void setMorphMap(Map map) {
		mapMorph.clear();
		mapMorph.putAll(map);
	}
	
	public Map modifyMorphMap(Map map) {
//		System.out.println("modifying morph...");
//		System.out.println("before:");
//		dump();
		Map changes = new HashMap();
		for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
			Object key = it.next();
			changes.put(key, mapMorph.get(key));
		}
		mapMorph.putAll(map);
//		System.out.println("after:");
//		dump();
		return changes;
	}
	
	public void apply() {
//		Vector3f v3 = new Vector3f();
//		for (int i = 0, n = listPoints.size(); i < n; i++) {
//			ControlPoint cp = (ControlPoint) listPoints.get(i);
//			v3.set((Vector3f) listVectors.get(i));
//			v3.scale(fValue);
//			cp.getPosition().add(v3);
//			cp.invalidateTangents();
//		}
		Vector3f v3 = new Vector3f();
		for (Iterator it = mapMorph.keySet().iterator(); it.hasNext(); ) {
			ControlPoint cp = (ControlPoint) it.next();
			v3.set((Vector3f) mapMorph.get(cp));
			v3.scale(fValue);
			cp.getPosition().add(v3);
			cp.invalidateTangents();
		}
	}
	
	public void unapply() {
		fValue = -fValue;
		apply();
		fValue = -fValue;
	}
	
	public void add(ControlPoint cp, Vector3f vector) {
//		listPoints.add(cp);
//		listVectors.add(vector);
		mapMorph.put(cp, vector);
	}
	
	public Vector3f removePoint(ControlPoint cp) {
		Vector3f v = (Vector3f) mapPositions.get(cp);
		mapMorph.remove(cp);
		return v;
//		
//		int index = listPoints.indexOf(cp);
//		if (index != -1) {
//			Vector3f vector = (Vector3f) listVectors.get(index);
//			listPoints.remove(index);
//			listVectors.remove(index);
//			return vector;
//		}
//		return null;
	}
	
//	public boolean replacePoint(ControlPoint cpToReplace, ControlPoint cpToReplaceWith) {
//		int index = listPoints.indexOf(cpToReplace);
//		if (index != -1) {
//			listPoints.set(index, cpToReplaceWith);
//			return true;
//		}
//		return false;
//	}
	
	public void prepare() {
		for (Iterator it = MainFrame.getInstance().getModel().allHeads().iterator(); it.hasNext(); ) {
			ControlPoint cp = (ControlPoint) it.next();
			mapPositions.put(cp, new Point3f(cp.getPosition()));
		}
		bPrepared = true;
	}
	
	public void set() {
		if (!bPrepared) throw new IllegalStateException("attempted to set unprepared morph");
		//MainFrame.getInstance().getJPatchScreen().update_all();
		//HashMap pointMap = new HashMap();
		//for (Iterator it = MainFrame.getInstance().getModel().allHeads().iterator(); it.hasNext(); ) {
		//	ControlPoint cp = (ControlPoint) it.next();
		//	pointMap.put(cp, new Point3f(cp.getPosition()));
		//}
		//fValue = 0;
		//MainFrame.getInstance().getModel().applyMorphs();
		//MainFrame.getInstance().getJPatchScreen().update_all();
		Vector3f v3 = new Vector3f();
//		listPoints.clear();
//		listVectors.clear();
		for (Iterator it = MainFrame.getInstance().getModel().allHeads().iterator(); it.hasNext(); ) {
			ControlPoint cp = (ControlPoint) it.next();
			v3.set(cp.getPosition());
			v3.sub((Tuple3f) mapPositions.get(cp));
			if (v3.x != 0f || v3.y != 0f || v3.z != 0f) {
//				listPoints.add(cp);
//				listVectors.add(new Vector3f(v3));
				mapMorph.put(cp, new Vector3f(v3));
			}
		}
		fValue = 1;
		//apply();
		mapPositions.clear();
		bPrepared = false;
	}
	
	public void dump() {
		System.out.println(strName);
//		for (int i = 0, n = listPoints.size(); i < n; i++) {
//			ControlPoint cp = (ControlPoint) listPoints.get(i);
//			Vector3f v3 = (Vector3f) listVectors.get(i);
//			System.out.println("\tcp " + cp + "\t" + v3);
//		}
		for (Iterator it = mapMorph.keySet().iterator(); it.hasNext(); ) {
			ControlPoint cp = (ControlPoint) it.next();
			System.out.println("\tcp " + cp + "\t" + (Vector3f) mapMorph.get(cp));
		}
	}
	
	public StringBuffer xml(int tabs) {
		StringBuffer sbIndent = XMLutils.indent(tabs);
		StringBuffer sbIndent2 = XMLutils.indent(tabs + 1);
		StringBuffer sbIndent3 = XMLutils.indent(tabs + 2);
		StringBuffer sbLineBreak = XMLutils.lineBreak();
		StringBuffer sb = new StringBuffer();
		sb.append(sbIndent).append("<morph name=\"").append(strName).append("\" ");
		sb.append("min=\"").append(fMin).append("\" ");
		sb.append("max=\"").append(fMax).append("\" ");
		sb.append("value=\"").append(fValue).append("\">");
		sb.append(sbLineBreak);
		sb.append(sbIndent2).append("<target value=\"1.0\">");
		sb.append(sbLineBreak);
		for (Iterator it = mapMorph.keySet().iterator(); it.hasNext(); ) {
			ControlPoint cp = (ControlPoint) it.next();
			Vector3f v3 = (Vector3f) mapMorph.get(cp);
			sb.append(sbIndent3);
			sb.append("<point nr=\"").append(cp.getXmlNumber()).append("\" ");
			sb.append("x=\"").append(v3.x).append("\" " );
			sb.append("y=\"").append(v3.y).append("\" " );
			sb.append("z=\"").append(v3.z).append("\"/>");
			sb.append(sbLineBreak);
		}
		sb.append(sbIndent2).append("</target>").append(sbLineBreak);
		sb.append(sbIndent).append("</morph>").append(sbLineBreak);
		return sb;
	}
}
