package jpatch.entity;

import java.util.*;

import javax.swing.event.*;
import javax.swing.tree.*;
import javax.vecmath.*;

import jpatch.boundary.*;
import jpatch.control.edit.*;

public class Morph implements MutableTreeNode {
	boolean bInserted = false;
	List listTargets = new ArrayList();
	String strName;
	float fMin = 0;
	float fMax = 100;
	float fValue = 0;
	Map mapMorph = new HashMap();
	Model model;
	Morph() { }
	EventListenerList eventListeners = new EventListenerList();
	
	public Morph(String name, Model model) {
		strName = name;
		this.model = model;
//		System.out.println("new Modph(" + name + ", " + model + ")");
	}
	
	public String getName() {
		return strName;
	}
	
	public String getId() {
		return strName;
	}
	
	public Model getModel() {
		return model;
	}
	
	public String toString() {
		return strName;
	}
	
	public void setName(String name) {
		strName = name;
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
		setMorphMap();
//		System.out.println("value = " + value + " " + mapMorph);
		model.applyMorphs();
		model.setMorphPose();
//		if (listener != null)
//			listener.valueChanged(getSliderValue());
		for (MorphListener listener:eventListeners.getListeners(MorphListener.class)) {
			listener.valueChanged(this);
		}
	}
	
	public void presetValue(float value) {
		fValue = value;
		setMorphMap(); // FIXME is this necessary?
	}
	
	public void updateCurve() {
		Animation animation = MainFrame.getInstance().getAnimation();
//		for (AnimModel animModel:animation.getModels()) {
//			if (animModel.getModel() == model) {
				MotionCurveSet.Model mcs = (MotionCurveSet.Model) animation.getCurvesetFor(model.getAnimModel());
				ModifyAnimObject edit = new ModifyAnimObject(model.getAnimModel());
				edit.addEdit(new AtomicModifyMotionCurve.Float(mcs.morph(this), animation.getPosition(), fValue));
				MainFrame.getInstance().getUndoManager().addEdit(edit);
				return;
//			}
//		}
	}
	
	public boolean isTarget() {
		int index = binarySearch(fValue) - 1;
		return (index >= 0 && index <= listTargets.size() - 1 && fValue == ((MorphTarget) listTargets.get(index)).getPosition());
	
	}
	public int getSliderValue() {
		int i = (int) ((fValue - fMin) / (fMax - fMin) * 100f);
		if (i < 0)
			i = 0;
		if (i > 100)
			i = 100;
		return i;
	}
	
	public void setSliderValue(int sliderValue) {
		setValue(fMin + (fMax - fMin) / 100f * (float) sliderValue);
	}
	
	public Map getMorphMap() {
		return mapMorph;
	}
	
	public void setMorphMap() {
		if (listTargets.size() < 2)
			return;
		MorphTarget mt0 = null, mt1 = null;
		float f0 = 0, f1 = 0;
		Vector3f v0 = null, v1 = null;
		int index = binarySearch(fValue) - 1;
//		System.out.println("binarysearch for " + fValue + " index " + index);
		if (index == -1) {
			mt0 = ((MorphTarget) listTargets.get(0));
			mt1 = ((MorphTarget) listTargets.get(1));
		} else if (index == listTargets.size() - 1) {
			mt0 = ((MorphTarget) listTargets.get(listTargets.size() - 2));
			mt1 = ((MorphTarget) listTargets.get(listTargets.size() - 1));
		} else {
			mt0 = ((MorphTarget) listTargets.get(index));
			mt1 = ((MorphTarget) listTargets.get(index + 1));
		}
		f1 = (fValue - mt0.getPosition());
		if (f1 != 0)
			f1 /= (mt1.getPosition() - mt0.getPosition());
		f0 = 1f - f1;
//		System.out.println("mapmorph=" + mapMorph);
//		System.out.println("mt0 = " + mt0);
//		if (mt0 != null)
//			System.out.println(mt0.getMorphMap());
//		System.out.println("f0 = " + f0);
//		System.out.println("mt1 = " + mt1);
//		if (mt1 != null)
//			System.out.println(mt1.getMorphMap());
//		System.out.println("f1 = " + f1);
		
		for (Iterator it = mapMorph.keySet().iterator(); it.hasNext(); ) {
			ControlPoint cp = (ControlPoint) it.next();
			Vector3f vector = (Vector3f) mapMorph.get(cp);
			if (mt0 != null)
				v0 = (Vector3f) mt0.getMorphMap().get(cp);
			if (mt1 != null)
				v1 = (Vector3f) mt1.getMorphMap().get(cp);
//			System.out.println("cp = " + cp + " v0=" + v0 + " v1=" + v1);
			if (v0 != null) {
				if (v1 != null) {
					vector.set(v0.x * f0 + v1.x * f1, v0.y * f0 + v1.y * f1, v0.z * f0 + v1.z * f1);
				} else {
					vector.set(v0.x * f0, v0.y * f0, v0.z * f0);
				}
			} else if (v1 != null) {
				vector.set(v1.x * f1, v1.y * f1, v1.z * f1);
			} else {
				vector.set(0f, 0f, 0f);
			}
		}
	}
	
	public void setupMorphMap() {
		mapMorph.clear();
		for (Iterator itTargets = listTargets.iterator(); itTargets.hasNext(); )
			for (Iterator itCps = ((MorphTarget) itTargets.next()).getMorphMap().keySet().iterator(); itCps.hasNext(); )
				mapMorph.put(itCps.next(), new Vector3f());
	}
	
	public void addTarget(MorphTarget target) {
		for (Iterator it = target.getMorphMap().keySet().iterator(); it.hasNext(); )
			mapMorph.put(it.next(), new Vector3f());
		MainFrame.getInstance().getTreeModel().insertNodeInto(target, this, binarySearch(target.getPosition()));
		setMorphMap();
	}
	
	public void removeTarget(MorphTarget target) {
		MainFrame.getInstance().getTreeModel().removeNodeFromParent(target);
		mapMorph.clear();
		for (Iterator itTargets = listTargets.iterator(); itTargets.hasNext(); )
			for (Iterator itCps = ((MorphTarget) itTargets.next()).getMorphMap().keySet().iterator(); itCps.hasNext(); )
				mapMorph.put(itCps.next(), new Vector3f());
		setMorphMap();
	}
	
	public void changeTargetPosition(MorphTarget target, float newPosition) {
		System.out.println("changeTargetPosition() " + target + ", " + newPosition);
		System.out.println(listTargets);
		MainFrame.getInstance().getTreeModel().removeNodeFromParent(target);
		System.out.println(listTargets);
		target.setPosition(newPosition);
		MainFrame.getInstance().getTreeModel().insertNodeInto(target, this, binarySearch(target.getPosition()));
		System.out.println(listTargets);
		setMorphMap();
	}
	
	int binarySearch(float position) {
		if (listTargets.size() == 0) return 0;
		if (position < ((MorphTarget) listTargets.get(0)).getPosition()) return 0;
		if (position >= ((MorphTarget) listTargets.get(listTargets.size() - 1)).getPosition()) return listTargets.size();
		int min = 0;
		int max = listTargets.size() - 1;
		int i = max >> 1;
		while (max > min + 1) {
			if (((MorphTarget) listTargets.get(i)).getPosition() > position) {
				max = i;
				i -= ((i - min) >> 1);
			} else {
				min = i;
				i += ((max - i) >> 1);
			}
		}
		return max;
	}
	
	public List getTargets() {
		return listTargets;
	}
	
	public void dump() {
		System.out.println("Morph " + strName);
		for (Iterator it = listTargets.iterator(); it.hasNext(); ) {
			((MorphTarget) it.next()).dump();
		}
	}
	/*
	 * start of TreeNode interface implementation
	 */
	public TreeNode getChildAt(int childIndex) {
		if (MainFrame.getInstance().getAnimation() != null)
			return null;
		return (MorphTarget) listTargets.get(childIndex);
	}

	public int getChildCount() {
		if (MainFrame.getInstance().getAnimation() != null)
			return 0;
		return listTargets.size();
	}

	public TreeNode getParent() {
		return bInserted ? model.getTreenodeExpressions() : null;
	}

	public int getIndex(TreeNode node) {
		return listTargets.indexOf(node);
	}

	public boolean getAllowsChildren() {
		return MainFrame.getInstance().getAnimation() == null;
	}

	public boolean isLeaf() {
		return listTargets.size() == 0 || MainFrame.getInstance().getAnimation() != null;
	}

	public Enumeration children() {
		if (MainFrame.getInstance().getAnimation() != null)
			return null;
		return Collections.enumeration(listTargets);
	}
	
	public StringBuffer xml(String prefix) {
		StringBuffer sb = new StringBuffer();
		sb.append(prefix).append("<morph name=\"").append(strName).append("\" ");
		sb.append("min=\"").append(fMin).append("\" ");
		sb.append("max=\"").append(fMax).append("\" ");
		sb.append("value=\"").append(fValue).append("\">\n");
		String prefix2 = prefix + "\t";
		for (Iterator it = listTargets.iterator(); it.hasNext(); ) {
			sb.append(((MorphTarget) it.next()).xml(prefix2));
		}
		sb.append(prefix).append("</morph>").append("\n");
		return sb;
	}
	
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
		
	/*
	 * end of TreeNode interface implementation
	 */

//	public void setMorphListener(MorphListener listener) {
//		this.listener = listener;
//		listener.valueChanged(getSliderValue());
//	}
	
	public void addMorphListener(MorphListener morphListener) {
		eventListeners.add(MorphListener.class, morphListener);
	}
	
	public void removeMorphListener(MorphListener morphListener) {
		eventListeners.remove(MorphListener.class, morphListener);
	}
	
	/*
	 * start of MutableTreeNode interface implementation
	 */
	public void insert(MutableTreeNode child, int index) {
		listTargets.add(index, child);
		child.setParent(this);
	}

	public void remove(int index) {
		System.out.println("remove " + index);
		listTargets.remove(index);
	}

	public void remove(MutableTreeNode node) {
		System.out.println("remove " + node);
		listTargets.remove(node);
	}

	public void setUserObject(Object object) {
		throw new UnsupportedOperationException();
	}

	public void removeFromParent() {
		throw new UnsupportedOperationException();
	}

	public void setParent(MutableTreeNode newParent) {
		bInserted = true;
	}
	/*
	 * end of MutableTreeNode interface implementation
	 */
	
	public interface MorphListener extends EventListener {
		public void valueChanged(Morph morph);
	}
}
