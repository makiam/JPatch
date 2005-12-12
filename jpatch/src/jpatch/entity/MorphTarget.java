package jpatch.entity;

import java.util.*;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.vecmath.*;

import jpatch.auxilary.XMLutils;
import jpatch.boundary.*;
import jpatch.control.edit.*;

public class MorphTarget implements MutableTreeNode {
//	private ArrayList listPoints = new ArrayList();
//	private ArrayList listVectors = new ArrayList();
//	private float fValue = 0;
//	private float fMin = 0;
//	private float fMax = 1;
	private Morph morph;
	private float fPosition;
	private Map mapMorph = new HashMap();
	private Map mapPositions = new HashMap();
	
	public MorphTarget(float position) {
		fPosition = position;
	}
	
	public String toString() {
		return "Target " + fPosition;
	}
	
	public Morph getMorph() {
		return morph;
	}
	
	public float getPosition() {
		return fPosition;
	}
	
	public void setPosition(float position) {
		fPosition = position;
	}
	
	public Transformable getTransformable(final Map selectedPoints, final boolean editMorphTarget) {
		if (!editMorphTarget) {
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
		}
		
		if (editMorphTarget) {
			return new Transformable() {
				private Map initMap = new HashMap();
				public void beginTransform() {
					initMap.clear();
					Map newPointsMap = new HashMap();
					for (Iterator it = selectedPoints.keySet().iterator(); it.hasNext(); ) {
						Object o = it.next();
						if (!(o instanceof ControlPoint))
							continue;
						ControlPoint cp = (ControlPoint) o;
						Vector3f morphVector = (Vector3f) mapMorph.get(cp);
						if (morphVector == null) {
							morphVector = new Vector3f();
							newPointsMap.put(cp, morphVector);
						}
						initMap.put(cp, new Vector3f(morphVector));
					}
					MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeMorph.AddPoints(MorphTarget.this, newPointsMap), true);
					morph.setupMorphMap();
				}
				
				public void translate(Vector3f v) {
					Vector3f vv = new Vector3f();
					for (Iterator it = initMap.keySet().iterator(); it.hasNext(); ) {
						ControlPoint cp = (ControlPoint) it.next();
						Vector3f vector = (Vector3f) mapMorph.get(cp);
						vector.set((Vector3f) initMap.get(cp));
						float weight = ((Float) selectedPoints.get(cp)).floatValue();
						vv.set(v);
						vv.scale(weight);
						MainFrame.getInstance().getConstraints().constrainVector(vv);
						cp.getInvTransform().transform(vv);
						vector.add(vv);
					}
					morph.setMorphMap();
					MainFrame.getInstance().getModel().applyMorphs();
					MainFrame.getInstance().getModel().setMorphPose();
				}
				
				public void rotate(AxisAngle4f a, Point3f pivot) {
					Point3f p0 = new Point3f();
					Point3f p1 = new Point3f();
					Vector3f vv = new Vector3f();
					Matrix3f rot = new Matrix3f();
					AxisAngle4f aa = new AxisAngle4f();
					for (Iterator it = initMap.keySet().iterator(); it.hasNext(); ) {
						ControlPoint cp = (ControlPoint) it.next();
						Vector3f vector = (Vector3f) mapMorph.get(cp);
						vector.set((Vector3f) initMap.get(cp));
					}
					morph.setMorphMap();
					MainFrame.getInstance().getModel().applyMorphs();
					MainFrame.getInstance().getModel().setMorphPose();
					for (Iterator it = initMap.keySet().iterator(); it.hasNext(); ) {
						ControlPoint cp = (ControlPoint) it.next();
						Vector3f vector = (Vector3f) mapMorph.get(cp);
						p0.set(cp.getPosition());
						p1.set(p0);
						p1.sub(pivot);
						float weight = ((Float) selectedPoints.get(cp)).floatValue();
						aa.set(a);
						aa.angle *= weight;
						rot.set(aa);
						rot.transform(p1);
						p1.add(pivot);
						vv.sub(p1, p0);
						MainFrame.getInstance().getConstraints().constrainVector(vv);
						cp.getInvTransform().transform(vv);
						vector.add(vv);
					}
					morph.setMorphMap();
					MainFrame.getInstance().getModel().applyMorphs();
					MainFrame.getInstance().getModel().setMorphPose();
				}
				
				public void transform(Matrix3f m, Point3f pivot) {
					Point3f p0 = new Point3f();
					Point3f p1 = new Point3f();
					Vector3f vv = new Vector3f();
					Matrix3f mm = new Matrix3f();
					AxisAngle4f aa = new AxisAngle4f();
					for (Iterator it = initMap.keySet().iterator(); it.hasNext(); ) {
						ControlPoint cp = (ControlPoint) it.next();
						Vector3f vector = (Vector3f) mapMorph.get(cp);
						vector.set((Vector3f) initMap.get(cp));
					}
					morph.setMorphMap();
					MainFrame.getInstance().getModel().applyMorphs();
					MainFrame.getInstance().getModel().setMorphPose();
					for (Iterator it = initMap.keySet().iterator(); it.hasNext(); ) {
						ControlPoint cp = (ControlPoint) it.next();
						Vector3f vector = (Vector3f) mapMorph.get(cp);
						p0.set(cp.getPosition());
						p1.set(p0);
						p1.sub(pivot);
						float weight = ((Float) selectedPoints.get(cp)).floatValue();
						weightMatrix(m, weight, mm);
						mm.transform(p1);
						p1.add(pivot);
						vv.sub(p1, p0);
						MainFrame.getInstance().getConstraints().constrainVector(vv);
						cp.getInvTransform().transform(vv);
						vector.add(vv);
					}
					morph.setMorphMap();
					MainFrame.getInstance().getModel().applyMorphs();
					MainFrame.getInstance().getModel().setMorphPose();
				}
				
				public JPatchUndoableEdit endTransform() {
					return new AtomicChangeMorphVectors(MorphTarget.this, initMap, false);
				}
				
				public Point3f getPosition() {
					throw new UnsupportedOperationException();
				}
			};
		} else {
			return new Transformable() {
				private Map changeMap = new HashMap();
				public void beginTransform() {
					changeMap.clear();
					for (Iterator it = mapMorph.keySet().iterator(); it.hasNext(); ) {
						Object key = it.next();
						if (selectedPoints.containsKey(key))
							changeMap.put(key, new Vector3f((Vector3f) mapMorph.get(key)));
					}		
				}
				
				public void translate(Vector3f v) {
					;
				}
				
				public void rotate(AxisAngle4f a, Point3f pivot) {
//					System.out.println("rotate " + this + " " + a);
					AxisAngle4f aa = new AxisAngle4f(a);
					Matrix3f m1 = new Matrix3f();
					Matrix3f m2 = new Matrix3f();
					Matrix3f m3 = new Matrix3f();
					for (Iterator it = changeMap.keySet().iterator(); it.hasNext(); ) {
						ControlPoint cp = (ControlPoint) it.next();
						Vector3f v = (Vector3f) changeMap.get(cp);
						v.set((Vector3f) mapMorph.get(cp));
						float weight = ((Float) selectedPoints.get(cp)).floatValue();
						aa.angle = a.angle * weight;
						m3.set(aa);
						cp.getTransform().getRotationScale(m1);
						cp.getInvTransform().getRotationScale(m2);
						MainFrame.getInstance().getConstraints().constrainMatrix(m3);
						m2.mul(m3);
						m2.mul(m1);
						m2.transform(v);
					}
				}
				
				public void transform(Matrix3f m, Point3f pivot) {
					Matrix3f identity = new Matrix3f();
					Matrix3f m1 = new Matrix3f();
					Matrix3f m2 = new Matrix3f();
					Matrix3f m3 = new Matrix3f();
					for (Iterator it = changeMap.keySet().iterator(); it.hasNext(); ) {
						ControlPoint cp = (ControlPoint) it.next();
						Vector3f v = (Vector3f) changeMap.get(cp);
						v.set((Vector3f) mapMorph.get(cp));
						float weight = ((Float) selectedPoints.get(cp)).floatValue();
						m3.set(m);
						m3.mul(weight);
						identity.setIdentity();
						identity.mul(1 - weight);
						m3.add(identity);
						cp.getTransform().getRotationScale(m1);
						cp.getInvTransform().getRotationScale(m2);
						MainFrame.getInstance().getConstraints().constrainMatrix(m3);
						m2.mul(m3);
						m2.mul(m1);
						m2.transform(v);
					}
				}
				
				public JPatchUndoableEdit endTransform() {
//					System.out.println(changeMap);
					return new AtomicChangeMorphVectors(MorphTarget.this, changeMap, true);
				}
				
				public Point3f getPosition() {
					throw new UnsupportedOperationException();
				}
			};
		}
	}
	
	static private void weightMatrix(Matrix3f matrix, float weight, Matrix3f weightedMatrix) {
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
	
	public boolean contains(ControlPoint cp) {
		return mapMorph.keySet().contains(cp);
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
	
//	public void apply() {
////		Vector3f v3 = new Vector3f();
////		for (int i = 0, n = listPoints.size(); i < n; i++) {
////			ControlPoint cp = (ControlPoint) listPoints.get(i);
////			v3.set((Vector3f) listVectors.get(i));
////			v3.scale(fValue);
////			cp.getPosition().add(v3);
////			cp.invalidateTangents();
////		}
//		Vector3f v3 = new Vector3f();
//		for (Iterator it = mapMorph.keySet().iterator(); it.hasNext(); ) {
//			ControlPoint cp = (ControlPoint) it.next();
//			v3.set((Vector3f) mapMorph.get(cp));
//			v3.scale(fValue);
//			cp.getRefPosition().add(v3);
//			cp.invalidateTangents();
//		}
//	}
//	
//	public void unapply() {
//		fValue = -fValue;
//		apply();
//		fValue = -fValue;
//	}
	
	public Vector3f getVectorFor(ControlPoint cp) {
		return (Vector3f) mapMorph.get(cp);
	}
	
	public void addPoint(ControlPoint cp, Vector3f vector) {
//		listPoints.add(cp);
//		listVectors.add(vector);
		mapMorph.put(cp, vector);
	}
	
	public int getNumberOfPoints() {
		return mapMorph.size();
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
	
//	public void prepare() {
//		for (Iterator it = MainFrame.getInstance().getModel().allHeads().iterator(); it.hasNext(); ) {
//			ControlPoint cp = (ControlPoint) it.next();
//			mapPositions.put(cp, new Point3f(cp.getRefPosition()));
//		}
//		bPrepared = true;
//	}
	
//	public void set() {
//		if (!bPrepared) throw new IllegalStateException("attempted to set unprepared morph");
//		//MainFrame.getInstance().getJPatchScreen().update_all();
//		//HashMap pointMap = new HashMap();
//		//for (Iterator it = MainFrame.getInstance().getModel().allHeads().iterator(); it.hasNext(); ) {
//		//	ControlPoint cp = (ControlPoint) it.next();
//		//	pointMap.put(cp, new Point3f(cp.getPosition()));
//		//}
//		//fValue = 0;
//		//MainFrame.getInstance().getModel().applyMorphs();
//		//MainFrame.getInstance().getJPatchScreen().update_all();
//		Vector3f v3 = new Vector3f();
////		listPoints.clear();
////		listVectors.clear();
//		for (Iterator it = MainFrame.getInstance().getModel().allHeads().iterator(); it.hasNext(); ) {
//			ControlPoint cp = (ControlPoint) it.next();
//			v3.set(cp.getRefPosition());
//			v3.sub((Tuple3f) mapPositions.get(cp));
//			if (v3.x != 0f || v3.y != 0f || v3.z != 0f) {
////				listPoints.add(cp);
////				listVectors.add(new Vector3f(v3));
//				mapMorph.put(cp, new Vector3f(v3));
//			}
//		}
////		fValue = 1;
//		//apply();
//		mapPositions.clear();
//		bPrepared = false;
//	}
	
	public void dump() {
		System.out.println(toString());
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
	
//	public StringBuffer xml(String prefix) {
//		return xml(prefix, null, null);
//	}
			
	public StringBuffer xml(String prefix) {
		StringBuffer sb = new StringBuffer();
		if (fPosition == 0)
			return sb;
//		if (dof== null) {
//			sb.append(prefix).append("<morph name=\"").append(strName).append("\" ");
//		} else {
//			Bone bone = dof.getBone();
//			int index = bone.getDofIndex(dof);
//			sb.append(prefix).append("<morph bone=\"").append(bone.getXmlNumber()).append("\" dof=\"").append(index).append("\" type=\"").append(type).append("\" ");
//		}
//		sb.append("min=\"").append(fMin).append("\" ");
//		sb.append("max=\"").append(fMax).append("\" ");
//		sb.append("value=\"").append(fValue).append("\">");
//		sb.append("\n");
//		sb.append(prefix).append("\t<target value=\"1.0\">").append("\n");
//		for (Iterator it = mapMorph.keySet().iterator(); it.hasNext(); ) {
//			ControlPoint cp = (ControlPoint) it.next();
//			Vector3f v3 = (Vector3f) mapMorph.get(cp);
//			sb.append(prefix);
//			sb.append("\t\t<point nr=\"").append(cp.getXmlNumber()).append("\" ");
//			sb.append("x=\"").append(v3.x).append("\" " );
//			sb.append("y=\"").append(v3.y).append("\" " );
//			sb.append("z=\"").append(v3.z).append("\"/>");
//			sb.append("\n");
//		}
//		sb.append(prefix).append("\t</target>").append("\n");
//		sb.append(prefix).append("</morph>").append("\n");
		sb.append(prefix).append("<target value=\"" + fPosition + "\">\n");
		for (Iterator it = mapMorph.keySet().iterator(); it.hasNext(); ) {
			ControlPoint cp = (ControlPoint) it.next();
			Vector3f v3 = (Vector3f) mapMorph.get(cp);
			sb.append(prefix);
			sb.append("\t<point nr=\"").append(cp.getXmlNumber()).append("\" ");
			sb.append("x=\"").append(v3.x).append("\" " );
			sb.append("y=\"").append(v3.y).append("\" " );
			sb.append("z=\"").append(v3.z).append("\"/>");
			sb.append("\n");
		}
		sb.append(prefix).append("</target>").append("\n");
		return sb;
	}

	/*
	 * Mutable treenode interface implementation
	 */
	public void insert(MutableTreeNode child, int index) {
		throw new UnsupportedOperationException();
	}

	public void remove(int index) {
		throw new UnsupportedOperationException();
	}

	public void remove(MutableTreeNode node) {
		throw new UnsupportedOperationException();
	}

	public void setUserObject(Object object) {
		throw new UnsupportedOperationException();
	}

	public void removeFromParent() {
		if (morph != null)
			morph.remove(this);
		else
			throw new IllegalStateException(this + " node does not have a parent");
	}

	public void setParent(MutableTreeNode newParent) {
		morph = (Morph) newParent;
	}

	public TreeNode getChildAt(int childIndex) {
		return null;
	}

	public int getChildCount() {
		return 0;
	}

	public TreeNode getParent() {
		return morph;
	}

	public int getIndex(TreeNode node) {
		return morph.getIndex(this);
	}

	public boolean getAllowsChildren() {
		return false;
	}

	public boolean isLeaf() {
		return true;
	}

	public Enumeration children() {
		return null;
	}
}
