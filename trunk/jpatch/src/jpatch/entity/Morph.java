package jpatch.entity;

import java.util.*;
import javax.swing.tree.*;
import javax.vecmath.*;

import jpatch.boundary.*;

public class Morph implements MutableTreeNode {
	private boolean bInserted = false;
	private List listTargets = new ArrayList();
	private String strName;
	private float fMin = 0;
	private float fMax = 100;
	private float fValue = 0;
	private Map mapMorph = new HashMap();
	
	public Morph(String name) {
		strName = name;
	}
	
	public String getName() {
		return strName;
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
	}
	
	public int getSliderValue() {
		return (int) ((fValue - fMin) / (fMax - fMin) * 100f);
	}
	
	public void setSliderValue(int sliderValue) {
		setValue(fMin + (fMax - fMin) / 100f * (float) sliderValue);
	}
	
	private void setMorphMap() {
		MorphTarget mt0 = null, mt1 = null;
		float f0 = 0, f1 = 0;
		Vector3f v0 = null, v1 = null;
		int index = binarySearch(fValue) - 1;
		if (index >= 0)
			mt0 = ((MorphTarget) listTargets.get(index));
		if (index <= listTargets.size() - 1)
			mt1 = ((MorphTarget) listTargets.get(index + 1));
		if (mt0 != null) {
			if (mt1 != null) {
				f1 = (fValue - mt0.getPosition());
				if (f1 > 0)
					f1 /= (mt1.getPosition() - mt0.getPosition());
				f0 = 1f - f1;
			} else {
				f0 = 1;
			}
		} else if (mt1 != null) {
			f1 = 1;
		} else {
			throw new IllegalStateException();
		}
		for (Iterator it = mapMorph.keySet().iterator(); it.hasNext(); ) {
			ControlPoint cp = (ControlPoint) it.next();
			Vector3f vector = (Vector3f) mapMorph.get(cp);
			if (mt0 != null)
				v0 = (Vector3f) mt0.getMorphMap().get(cp);
			if (mt1 != null)
				v1 = (Vector3f) mt1.getMorphMap().get(cp);
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
	
	public void addTarget(MorphTarget target) {
		mapMorph.putAll(target.getMorphMap());
		MainFrame.getInstance().getTreeModel().insertNodeInto(target, this, binarySearch(target.getPosition()));
		setMorphMap();
	}
	
	public void removeTarget(MorphTarget target) {
		MainFrame.getInstance().getTreeModel().removeNodeFromParent(target);
		mapMorph.clear();
		for (Iterator it = listTargets.iterator(); it.hasNext(); ) {
			mapMorph.putAll(((MorphTarget) it.next()).getMorphMap());
		}
		setMorphMap();
	}
	
	public void changeTargetPosition(MorphTarget target, float newPosition) {
		MainFrame.getInstance().getTreeModel().removeNodeFromParent(target);
		target.setPosition(newPosition);
		MainFrame.getInstance().getTreeModel().insertNodeInto(target, this, binarySearch(target.getPosition()));
		setMorphMap();
	}
	
	int binarySearch(float position) {
		if (listTargets.size() == 0) return 0;
		if (position < ((MorphTarget) listTargets.get(0)).getPosition()) return 0;
		if (position > ((MorphTarget) listTargets.get(listTargets.size() - 1)).getPosition()) return listTargets.size();
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
	
	/*
	 * start of TreeNode interface implementation
	 */
	public TreeNode getChildAt(int childIndex) {
		return (MorphTarget) listTargets.get(childIndex);
	}

	public int getChildCount() {
		return listTargets.size();
	}

	public TreeNode getParent() {
		return bInserted ? MainFrame.getInstance().getModel().getTreenodeExpressions() : null;
	}

	public int getIndex(TreeNode node) {
		return MainFrame.getInstance().getModel().getMorphList().indexOf(this);
	}

	public boolean getAllowsChildren() {
		return true;
	}

	public boolean isLeaf() {
		return listTargets.size() == 0;
	}

	public Enumeration children() {
		return Collections.enumeration(listTargets);
	}
	/*
	 * end of TreeNode interface implementation
	 */

	/*
	 * start of MutableTreeNode interface implementation
	 */
	public void insert(MutableTreeNode child, int index) {
		listTargets.add(index, child);
	}

	public void remove(int index) {
		listTargets.remove(index);
	}

	public void remove(MutableTreeNode node) {
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
}
