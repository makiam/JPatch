package jpatch.entity;

import java.util.*;
import javax.swing.tree.*;
import javax.vecmath.*;
import jpatch.boundary.*;
import jpatch.auxilary.*;

public class Bone implements TreeNode {
	private static int NUM = 0;
	
	private static final Bone[] emptyBoneArray = new Bone[0];
	private Model model;
	private final Point3f p3Start = new Point3f();
	private final Point3f p3End = new Point3f();
//	private float fStartRadius = 0.33f;
//	private float fEndRadius = 0.33f;
	private Bone boneParent;
	private Bone boneNext;
	private Bone bonePrev;
	private int iChildren = 0;
	private boolean bSelected = false;
	private int iNum = NUM++;
	
	public Bone(Model model, Point3f start, Point3f end) {
		this.model = model;
		p3Start.set(start);
		p3End.set(end);
	}

	public Bone(Model model,Bone parent) {
		this(model, parent.getStart(),parent.getStart());
		boneParent = parent;
		boneParent.iChildren++;
	}

	/*
	* TreeNode interface implementation
	*/

	public Enumeration children() {
		return new ArrayEnumeration(getChildren());
	}

	public boolean getAllowsChildren() {
		return true;
	}

	public void setSelected(boolean selected) {
		bSelected = selected;
	}

	public boolean isSelected() {
		return bSelected;
	}

	public TreeNode getChildAt(int childIndex) {
		Bone[] aboneChildren = getChildren();
		return aboneChildren[childIndex];
	}

	public int getChildCount() {
		return iChildren;
	}

	public int getIndex(TreeNode node) {
		Bone bone = (Bone) node;
		Bone[] aboneChildren = getChildren();
		for (int b = 0; b < aboneChildren.length; b++) {
			if (aboneChildren[b] == bone) {
				return b;
			}
		}
		return -1;
	}

	public TreeNode getParent() {
		return (boneParent != null) ? (TreeNode) boneParent : (TreeNode) model.getRootBone();
	}

	public boolean isLeaf() {
		return false;
	}

	/*
	* TreeNode interface implementation end
	*/

	public Bone[] getChildren() {
		if (iChildren > 0) {
			Bone[] aboneChildren = new Bone[iChildren];
			int c = 0;
			for (Bone bone = model.getFirstBone(); bone != null; bone = bone.getNext()) {
				if (bone.boneParent == this) {
					aboneChildren[c++] = bone;
				}
			}
			return aboneChildren;
		}
		return emptyBoneArray;
	}

	public Bone getParentBone() {
		return boneParent;
	}
	
	/**
	* return root bone
	**/
	public Bone getRoot() {
		/* recursively search root bone */
		return (boneParent == null) ? this : boneParent.getRoot();
	}

	public Point3f getStart() {
		return (boneParent == null) ? p3Start : boneParent.p3End;
	}

	public Point3f getEnd() {
		return p3End;
	}

	public void setStart(Point3f start) {
		if (boneParent == null) p3Start.set(start);
		else boneParent.p3End.set(start);
	}

	public void setEnd(Point3f end) {
		p3End.set(end);
	}

	public void setParent(Bone parent) {
		boolean loop = false;
		for (Bone bone = parent; bone != null; bone = bone.boneParent) {
			loop |= (bone == this);
		}
		if (!loop) {	
			if (boneParent != null) {
				boneParent.iChildren--;
			}
			if (parent != null) {
				parent.iChildren++;
			} else {
				p3Start.set(getStart());
			}
			boneParent = parent;
		}
	}

	public Bone getNext() {
		return boneNext;
	}

	public void insertBefore(Bone firstBone) {
		boneNext = firstBone;
		if (firstBone != null) {
			firstBone.bonePrev = this;
		}
		model.getBoneShapeList().add(new BoneShape(this));
	}

	public void delete() {
		removeFromList(false);
	}

	public void remove() {
		removeFromList(true);
	}

	public int getNumber() {
		return iNum;
	}
	
	private void removeFromList(boolean reconnect) {
		loop:
		for (Iterator it = model.getBoneShapeList().iterator(); it.hasNext(); ) {
			BoneShape bs = (BoneShape) it.next();
			if (bs.getBone() == this) {
				model.getBoneShapeList().remove(bs);
				break loop;
			}
		}
		if (model.getFirstBone() == this) {
			model.setFirstBone(boneNext);
		}
		if (iChildren > 0) {
			Bone[] aboneChild = getChildren();
			for (int b = 0; b < aboneChild.length; b++) {
				if (reconnect) {
					aboneChild[b].boneParent = boneParent;
					boneParent.iChildren++;
					//aboneChild[b].v3Extent.add(v3Extent);
				} else {
					aboneChild[b].boneParent = null;
					aboneChild[b].p3Start.set(p3End);
				}
			}
		}
		if (boneParent != null) {
			boneParent.iChildren--;
		}
		if (bonePrev != null) {
			bonePrev.boneNext = boneNext;
		}
		if (boneNext != null) {
			boneNext.bonePrev = bonePrev;
		}
	}
}
