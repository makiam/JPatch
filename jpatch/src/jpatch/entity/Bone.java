package jpatch.entity;

import java.util.*;
import javax.swing.tree.*;
import javax.vecmath.*;

import jpatch.boundary.*;
import jpatch.control.edit.*;
//import jpatch.auxilary.*;
//
public class Bone extends JPatchTreeNode {
//	public static final BoneTransformableType START = new BoneTransformableType();
//	public static final BoneTransformableType END = new BoneTransformableType();
	
	private static int NUM = 0;
//	
//	private static final Bone[] emptyBoneArray = new Bone[0];
	private static int col = 0;
	private static final Color3f[] COLORS = new Color3f[] {
		new Color3f(1, 0, 0),
		new Color3f(1, 1, 0),
		new Color3f(0, 1, 0),
		new Color3f(0, 1, 1),
		new Color3f(0, 0, 1),
		new Color3f(1, 0, 1),
	};
	private Color3f color = nextColor();
	private Model model;
	private final Point3f p3Start = new Point3f();
	private final Point3f p3End = new Point3f();
	private Vector3f v3Extent;
//	private Vector3f v3TempExtent = new Vector3f();
//	private final Point3f p3End = new Point3f();
////	private float fStartRadius = 0.33f;
////	private float fEndRadius = 0.33f;
	private Bone boneParent;
//	private Bone boneNext;
//	private Bone bonePrev;
	private int iChildren = 0;
	private boolean bSelected = false;
	private BoneTransformable boneStart = new BoneTransformable(p3Start);
	private BoneTransformable boneEnd = new BoneTransformable(p3End);

//	private int iNum = NUM++;
//	
	public Bone(Model model, Point3f start, Vector3f extent) {
		this.model = model;
		p3Start.set(start);
		v3Extent = extent;
		setEnd();
//		boneStart = new BoneTransformable(START);
//		boneEnd = new BoneTransformable(END);
		strName = "new bone #" + NUM++;
	}

//	public Bone(Model model, Bone parent) {
//		this.model = model;
//		parent.getStart(p3Start);
//		p3Start.add(parent.getExtent());
//		v3Extent = new Vector3f();
//		boneParent = parent;
//		boneParent.iChildren++;
//		setEnd();
//		boneStart = new BoneTransformable(START);
//		boneEnd = new BoneTransformable(END);
//		strName = "new bone #" + NUM++;
//	}
//	public Bone(Model model,Bone parent) {
//		this(model, parent.getStart(null), new Vector3f());
//		boneParent = parent;
//		boneParent.iChildren++;
//	}
	
	private static Color3f nextColor() {
		Color3f color = COLORS[col];
		col++;
		if (col >= COLORS.length)
			col = 0;
		return color;
	}

	/*
	* TreeNode interface implementation
	*/

	public Enumeration children() {
		return Collections.enumeration(getChildren());
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

	public TreeNode getChildAt(int index) {
		return (TreeNode) getChildren().get(index);
	}

	public int getChildCount() {
		return iChildren;
	}

	public int getIndex(TreeNode node) {
		return getChildren().indexOf(node);
	}

	public TreeNode getParent() {
		return (boneParent != null) ? (TreeNode) boneParent : (TreeNode) model.getRootBone();
	}

	public boolean isLeaf() {
		return (iChildren == 0);
	}

	/*
	* TreeNode interface implementation end
	*/

	public Color3f getColor() {
		return color;
	}
	
	public List getChildren() {
		ArrayList list = new ArrayList();
		if (iChildren > 0) {
			for (Iterator it = model.getBoneSet().iterator(); it.hasNext(); ) {
			Bone bone = (Bone) it.next();
			if (bone.boneParent == this)
				list.add(bone);
			}
		}
		return list;
	}

	public void attachTo(Bone parent) {
		if (boneParent != null)
			throw new IllegalStateException("bone is already attached");
		boneParent = parent;
		parent.iChildren++;
	}
	
	public void detach() {
		if (boneParent == null)
			throw new IllegalStateException("bone isn't attached");
		boneParent.iChildren--;
		boneParent = null;
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

	public Point3f getStart(Point3f start) {
		if (start == null)
			start = new Point3f();
		if (boneParent == null)
			start.set(p3Start);
		else
			boneParent.getEnd(start);
		return start;
	}

	public Vector3f getExtent() {
		return v3Extent;
	}

	public Point3f getEnd(Point3f end) {
		if (end == null)
			end = new Point3f();
		end.set(p3End);
		return end;
	}
	
	public void setEnd(Point3f end) {
		p3End.set(end);
		setExtent();
	}
	
	public void setStart(Point3f start) {
		if (boneParent == null) {
			p3Start.set(start);
		} else {
			Vector3f parentExtent = boneParent.getExtent();
			parentExtent.set(start);
			parentExtent.sub(boneParent.getStart(null));
		}
	}

	public void setExtent(Vector3f extent) {
		v3Extent.set(extent);
		setEnd();
	}

	public void setParent(Bone parent) {
		boolean loop = false;
		for (Bone bone = parent; bone != null; bone = bone.boneParent) {
			loop |= (bone == this);
		}
		if (!loop) {	
			if (boneParent != null)
				boneParent.iChildren--;
			if (parent != null)
				parent.iChildren++;
			else
				getStart(p3Start);
			boneParent = parent;
		}
	}
	
	public void setExtent() {
		v3Extent.set(p3End);
		v3Extent.sub(getStart(null));
	}
	
	public void setEnd() {
		getStart(p3End);
		p3End.add(v3Extent);
	}
	
	public BoneTransformable getBoneStart() {
		return (boneParent == null) ? boneStart : null;
	}
	
	public BoneTransformable getBoneEnd() {
		return boneEnd;
	}
	
	public String toString() {
		return strName;
	}
	
	
	
	public final class BoneTransformable implements Transformable {
		private final Point3f p3Temp = new Point3f();
		private final Point3f p3;
//		private final BoneTransformableType type;
		
		public BoneTransformable(Point3f p) {
			p3 = p;
		}
		
//		public BoneTransformableType getType() {
//			return type;
//		}
		
		public boolean isStart() {
			return p3 == p3Start;
		}
		
		public boolean isEnd() {
			return p3 == p3End;
		}
		
		public Bone getBone() {
			return Bone.this;
		}
		
		public Point3f getPosition() {
			return p3;
		}
		
		public void beginTransform() {
			setEnd();
			p3Temp.set(p3);
		}

		public void translate(Vector3f v) {
			p3.set(p3Temp);
			p3.add(v);
			setExtent();
		}

		public void rotate(AxisAngle4f a, Point3f pivot) {
			p3.set(p3Temp);
			p3.sub(pivot);
			Matrix3f m = new Matrix3f();
			m.set(a);
			m.transform(p3);
			p3.add(pivot);
			setExtent();
		}

		public void transform(Matrix3f m, Point3f pivot) {
			p3.set(p3Temp);
			p3.sub(pivot);
			m.transform(p3);
			p3.add(pivot);
			setExtent();
		}

		public JPatchUndoableEdit endTransform() {
			return new AtomicChangeBone.Point(Bone.this, p3Temp, p3);
		}
		
		public String toString() {
			return Bone.this.toString() + "-" + (isStart() ? "start " + Bone.this.getStart(null): "end" + Bone.this.getEnd(null)) + " " + p3;
		}
		
//		public boolean equals(Object o) {
//			BoneTransformable bt = (BoneTransformable) o;
//			return Bone.this == bt.getBone() && type == bt.type;
//		}
//		
//		public int hashCode() {
//			return Bone.this.hashCode() + 3 * type.hashCode();
//		}
	}
	
//	public static final class BoneTransformableType { }
}