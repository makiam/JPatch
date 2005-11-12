package jpatch.entity;

import java.util.*;

import javax.swing.tree.*;
import javax.vecmath.*;

import jpatch.auxilary.XMLutils;
import jpatch.boundary.*;
import jpatch.control.edit.*;
//import jpatch.auxilary.*;
//
public class Bone implements MutableTreeNode {
//	public static final BoneTransformableType START = new BoneTransformableType();
//	public static final BoneTransformableType END = new BoneTransformableType();
	private static final float DEFAULT_INFLUENCE = 0.33f; 
	private static int NUM = 0;
	private static Map mapBones;
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
	private final Point3f p3TempEnd = new Point3f();
//	private Vector3f v3Extent;
//	private final Point3f p3ReferenceStart = new Point3f();
//	private final Point3f p3ReferenceEnd = new Point3f();
//	private Vector3f v3TempExtent = new Vector3f();
//	private final Point3f p3End = new Point3f();
	private float fStartRadius = DEFAULT_INFLUENCE;
	private float fEndRadius = DEFAULT_INFLUENCE;
	private MutableTreeNode parent;
//	private Bone boneNext;
//	private Bone bonePrev;
	private ArrayList listChildBones = new ArrayList();
	private ArrayList listDofs = new ArrayList();
	
	private boolean bSelected = false;
	private BoneTransformable boneStart = new BoneTransformable(p3Start);
	private BoneTransformable boneEnd = new BoneTransformable(p3End);
	
	private String strName;
	private int iDofAxis = 0;
//	private int iNum = NUM++;
//	
	public Bone(Model model, Point3f start, Vector3f extent) {
		this.model = model;
		p3Start.set(start);
		p3End.set(start);
		p3End.add(extent);
//		v3Extent = extent;
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
		return new Color3f(color);
	}

	public static void setMap(Map map) {
		mapBones = map;
	}
	
	public void addDofAxis(int axis) {
		iDofAxis |= axis;
	}
	
	public void removeDofAxis(int axis) {
		iDofAxis &= (~axis);
	}
	
	public int getDofAxis() {
		if ((iDofAxis & 1) == 0) {
			iDofAxis |= 1;
			return RotationDof.ORTHO_1;
		} else if ((iDofAxis & 2) == 0) {
			iDofAxis |= 2;
			return RotationDof.ORTHO_2;
		} else if ((iDofAxis & 4) == 0) {
			iDofAxis |= 4;
			return RotationDof.RADIAL;
		}
		return -1;
	}
	
	public int getXmlNumber() {
		return ((Integer) mapBones.get(this)).intValue();
	}
	
	public List getChildBones() {
		return listChildBones;
	}
	
	public List getDofs() {
		return listDofs;
	}
	
	/*
	* TreeNode interface implementation
	*/

	public Enumeration children() {
		return new Enumeration() {
			private int i = 0;
			public boolean hasMoreElements() {
				return i < listDofs.size() + listChildBones.size();
			}

			public Object nextElement() {
				return getChildAt(i++);
			}
		};
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
		if (index < listDofs.size())
			return (TreeNode) listDofs.get(index);
		else
			return (TreeNode) listChildBones.get(index - listDofs.size());
	}

	public int getChildCount() {
		return listDofs.size() + listChildBones.size();
	}

	public int getIndex(TreeNode node) {
		int i = listDofs.indexOf(node);
		if (i > -1)
			return i;
		i = listChildBones.indexOf(node);
		if (i > -1)
			return listDofs.size() + i;
		return -1;
	}

	public TreeNode getParent() {
		return parent;
	}

	public boolean isLeaf() {
		return (listDofs.size() <= 0 && listChildBones.size() <= 0);
	}

	public void insert(MutableTreeNode child, int index) {
		System.out.println("insert at " + index + "/" + listChildBones.size() + "/" + listDofs.size() + "/" + getChildCount());
		if (child instanceof RotationDof)
			listDofs.add(index, child);
		else if (child instanceof Bone)
			listChildBones.add(index - listDofs.size(), child);
		child.setParent(this);
	}

	public void remove(int index) {
		if (index < listDofs.size())
			listDofs.remove(index);
		else
			listChildBones.remove(index - listDofs.size());
	}

	public void remove(MutableTreeNode node) {
		if (node instanceof Bone)
			listChildBones.remove(node);
		else if (node instanceof RotationDof)
			listDofs.remove(node);
	}

	public void setUserObject(Object object) {
		throw new UnsupportedOperationException();
	}

	public void removeFromParent() {
		((MutableTreeNode) getParent()).remove(this);
	}

	public void setParent(MutableTreeNode newParent) {
		parent = newParent;
	}
	
	/*
	* TreeNode interface implementation end
	*/

	public Color3f getColor() {
		return color;
	}
	
//	public List getChildren() {
//		return listChildren;
//	}

//	public void attachTo(Bone parent) {
//		if (boneParent != null)
//			throw new IllegalStateException("bone is already attached");
//		for (Bone bone = parent; bone != null; bone = bone.boneParent) {
//			if (bone == this)
//				return;
//		}
//		boneParent = parent;
//		boneParent.listChildBones.add(this);
//		boneParent.listChildren.add(this);
//	}
//	
//	public void detach() {
//		if (boneParent == null)
//			throw new IllegalStateException("bone isn't attached");
//		boneParent.listChildBones.remove(this);
//		boneParent.listChildren.remove(this);
//		boneParent = null;
//	}
	
	public Bone getParentBone() {
		return parent instanceof Bone ? (Bone) parent : null;
	}
	
//	public void setParentBone(Bone parent) {
//		boneParent = parent;
//	}
	
//	public void addDof(RotationDof dof) {
//		listDofs.add(dof);
//		listChildren.add(dof);
//	}
//	
//	public void removeDof(RotationDof dof) {
//		listDofs.remove(dof);
//		listChildren.add(dof);
//	}
	
	/**
	* return root bone
	**/
	public Bone getRoot() {
		/* recursively search root bone */
		Bone parentBone = getParentBone();
		return parentBone == null ? this : parentBone.getRoot();
	}

	public Point3f getStart(Point3f start) {
		if (start == null)
			start = new Point3f();
		if (getParentBone() == null)
			start.set(p3Start);
		else
			getParentBone().getEnd(start);
		return start;
	}

	public Point3f getReferenceStart() {
		if (getParentBone() == null)
			return p3Start;
		else
			return getParentBone().p3End;
	}
	
	public Point3f getReferenceEnd() {
		return p3End;
	}
	
//	public Vector3f getExtent() {
//		return v3Extent;
//	}

	public Point3f getEnd(Point3f end) {
//		System.out.println("getEnd() Bone = " + this);
		
//		if (end == null)
//			end = getStart(null);
//		else
//			getStart(end);
//		Vector3f v = new Vector3f(p3End);
//		v.sub(getReferenceStart());
//		lastDofTransform(v);
//		end.add(v);
//		return end;
		
		if (end == null)
			end = new Point3f(p3End);
		else
			end.set(p3End);
		lastDofTransform(end);
		return end;
	}
	
	public void setEnd(Point3f end) {
		p3End.set(end);
		lastDofInvTransform(p3End);
	}
	
	public void setStart(Point3f start) {
		if (getParentBone() != null)
			throw new IllegalStateException("can set start of attached bone");
		p3Start.set(start);
	}
//	
//	public void setStart(Point3f start) {
//		if (boneParent == null) {
//			p3Start.set(start);
//		} else {
//			Vector3f parentExtent = boneParent.getExtent();
//			parentExtent.set(start);
//			parentExtent.sub(boneParent.getStart(null));
//		}
//	}
//
//	public void setExtent(Vector3f extent) {
//		v3Extent.set(extent);
//		setEnd();
//	}

//	public void setParent(Bone parent) {
//		
//	}
	
//	public void setExtent() {
//		v3Extent.set(p3End);
//		v3Extent.sub(getStart(null));
//	}
//	
//	public void setEnd() {
//		getStart(p3End);
//		p3End.add(v3Extent);
//	}
	
	public void setColor(Color3f color) {
		this.color.set(color);
	}
	
	public void setStartInfluence(float influence) {
		fStartRadius = influence;
	}
	
	public void setEndInfluence(float influence) {
		fEndRadius = influence;
	}
	
	public BoneTransformable getBoneStart() {
		return (getParentBone() == null) ? boneStart : null;
	}
	
	public BoneTransformable getBoneEnd() {
		return boneEnd;
	}
	
	public String toString() {
		return strName;
	}
	
	public int getDofIndex(RotationDof dof) {
		return listDofs.indexOf(dof);
	}
	
	public RotationDof getDof(int index) {
		if (listDofs.size() == 0)
			return null;
		if (index >= 0)
			return (RotationDof) listDofs.get(index);
		else
			// return last dof
			return (RotationDof) listDofs.get(listDofs.size() - 1);
	}
	
	public RotationDof getLastDof() {
		if (listDofs.size() > 0)
			return getDof(-1);
		if (getParentBone() != null)
			return getParentBone().getLastDof();
		return null;
	}
	
	private void lastDofTransform(Point3f p) {
		RotationDof dof = getLastDof();
		if (dof != null)
			dof.getTransform().transform(p);
	}
	
	private void lastDofTransform(Vector3f v) {
		RotationDof dof = getLastDof();
		if (dof != null)
			dof.getTransform().transform(v);
	}
	
	private void lastDofInvTransform(Point3f p) {
		RotationDof dof = getLastDof();
		if (dof != null)
			dof.getInvTransform().transform(p);
	}
	
	private void lastDofInvTransform(Vector3f v) {
		RotationDof dof = getLastDof();
		if (dof != null)
			dof.getInvTransform().transform(v);
	}
	
	
	public StringBuffer xml(String prefix) {
		StringBuffer sb = new StringBuffer();
		sb.append(prefix).append("<bone name=").append(XMLutils.quote(strName)).append(">\n");
		if (getParentBone() != null) {
			int parent = ((Integer) mapBones.get(getParentBone())).intValue();
			sb.append(prefix).append("\t<parent id=").append(XMLutils.quote(parent)).append("/>\n");
		} else {
			sb.append(prefix).append("\t<start x=").append(XMLutils.quote(p3Start.x));
			sb.append(" y=").append(XMLutils.quote(p3Start.y));
			sb.append(" z=").append(XMLutils.quote(p3Start.z)).append("/>\n");
		}
		sb.append(prefix).append("\t<end x=").append(XMLutils.quote(p3End.x));
		sb.append(" y=").append(XMLutils.quote(p3End.y));
		sb.append(" z=").append(XMLutils.quote(p3End.z)).append("/>\n");
		sb.append(prefix).append("\t<color r=").append(XMLutils.quote(color.x));
		sb.append(" g=").append(XMLutils.quote(color.y));
		sb.append(" b=").append(XMLutils.quote(color.z)).append("/>\n");
		if (fStartRadius != DEFAULT_INFLUENCE || fEndRadius != DEFAULT_INFLUENCE) {
			sb.append(prefix).append("\t<influence start=").append(XMLutils.quote(fStartRadius));
			sb.append(" end=").append(XMLutils.quote(fEndRadius)).append("/>\n");
		}
		for (Iterator it = listDofs.iterator(); it.hasNext(); )
			sb.append(((RotationDof) it.next()).xml(prefix + "\t"));
		sb.append(prefix).append("</bone>\n");
		return sb;
	}
	
//	public void setReferencePose() {
//		System.out.println("setReferencePose");
//		if (boneParent != null) {
//			RotationDof dof = boneParent.getLastDof();
//			if (dof != null) {
//				p3ReferenceStart.set(p3Start);
//				dof.getInvTransform().transform(p3ReferenceStart);
//			}
//		}
//		RotationDof dof = getLastDof();
//		if (dof != null) {
//			p3ReferenceEnd.set(p3End);
//			dof.getInvTransform().transform(p3ReferenceEnd);
//		}
//	}
//	
//	public void setPose() {
//		if (boneParent != null) {
//			RotationDof dof = boneParent.getLastDof();
//			if (dof != null) {
//				p3Start.set(p3ReferenceStart);
//				dof.getTransform().transform(p3Start);
//			}
//		}
//		RotationDof dof = getLastDof();
//		if (dof != null) {
//			p3End.set(p3ReferenceEnd);
//			dof.getTransform().transform(p3End);
//		}
//		setExtent();
//	}
	
	private static void applyCorrection(Bone bone, boolean inverse) {
		for (Iterator it = bone.listChildBones.iterator(); it.hasNext(); ) {
			Bone child = (Bone) it.next();
			if (inverse) {
				child.lastDofInvTransform(child.p3TempEnd);
				child.p3End.set(child.p3TempEnd);
			} else {
				child.p3TempEnd.set(child.p3End);
				child.lastDofTransform(child.p3TempEnd);
			}
			applyCorrection(child, inverse);
		}
	}
	
//	private void buildDownstreamBoneList(List list) {
//		for (Iterator it = listChildBones.iterator(); it.hasNext(); ) {
//			Bone child = (Bone) it.next();
//			child.buildDownstreamBoneList(list);
//		}
//		list.add(this);
//	}
	
	public final class BoneTransformable implements Transformable {
		private final Point3f p3Temp = new Point3f();
		private final Point3f p3Dummy = new Point3f();
//		private final Point3f p3Move = new Point3f();
		private final Point3f p3;
//		private final BoneTransformableType type;
//		private boolean bStart;
		
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
			Point3f p = new Point3f(p3);
			if (isStart() && getParentBone() != null)
				getParentBone().lastDofTransform(p);
			else
				lastDofTransform(p);
			return p;
		}
		
		public void beginTransform() {
			p3Temp.set(p3);
		}

		private void setDummy() {
			p3Dummy.set(p3Temp);
			if (isStart() && getParentBone() != null)
				getParentBone().lastDofTransform(p3Dummy);
			else
				lastDofTransform(p3Dummy);
		}
		
		private void setPoint() {
			if (isStart() && getParentBone() != null) {
				getParentBone().lastDofInvTransform(p3Dummy);
				p3.set(p3Dummy);
			} else {
				
				Bone.applyCorrection(Bone.this, false);
				lastDofInvTransform(p3Dummy);
				p3.set(p3Dummy);
				Bone.applyCorrection(Bone.this, true);
//				System.out.println(p3Dummy);
//				int children = listChildBones.size();
//				//if (children > 0) {
//					Vector3f[] v = new Vector3f[children];
//					for (int i = 0; i < children; i++) {
//						Bone child = (Bone) listChildBones.get(i);
//						v[i] = new Vector3f(child.getEnd(null));
//						v[i].sub(p3Dummy);
////						child.lastDofInvTransform(v[i]);
////						child.p3End.set(child.getReferenceStart());
////						child.p3End.add(v[i]);
//	//					System.out.println(child + " " + child.p3End);
//	//					child.setEnd(child.p3End);
//	//					lastDofTransform(child.p3End);
//	//					System.out.println(child + " " + child.p3End);
//					}
//					lastDofInvTransform(p3Dummy);
//					p3.set(p3Dummy);
//					for (int i = 0; i < children; i++) {
//						Bone child = (Bone) listChildBones.get(i);
//						child.lastDofInvTransform(v[i]);
//						child.p3End.set(child.getReferenceStart());
//						child.p3End.add(v[i]);
//					}
////						child.lastDofInvTransform(v[i]);
////						child.p3End.set(child.getStart(null));
////						child.p3End.add(v[i]);
////	//					System.out.println(child + " " + child.p3End);
////	//					System.out.println();
////					}
//				//}
				
			}
		}
		
		
		public void translate(Vector3f v) {
			setDummy();
			p3Dummy.add(v);
			setPoint();
		}

		public void rotate(AxisAngle4f a, Point3f pivot) {
// FIXME: doesn't work if bone isn't in reference pose
			setDummy();
//			if (isStart() && getParentBone() != null)
//				getParentBone().lastDofInvTransform(p);
//			else
//				lastDofInvTransform(p);
			p3Dummy.sub(pivot);
			Matrix3f m = new Matrix3f();
			m.set(a);
			m.transform(p3Dummy);
			p3Dummy.add(pivot);
			setPoint();
		}

		public void transform(Matrix3f m, Point3f pivot) {
// FIXME: doesn't work if bone isn't in reference pose
			setDummy();
			p3Dummy.sub(pivot);
			m.transform(p3Dummy);
			p3Dummy.add(pivot);
			setPoint();
		}

		public JPatchUndoableEdit endTransform() {
			return new AtomicChangeBone.Point(Bone.this, p3Temp, p3);
		}
		
//		public String toString() {
//			return Bone.this.toString() + "-" + (isStart() ? "start " + Bone.this.getStart(null): "end" + Bone.this.getEnd(null)) + " " + p3;
//		}
		
		
//		public boolean equals(Object o) {
//			BoneTransformable bt = (BoneTransformable) o;
//			return Bone.this == bt.getBone() && type == bt.type;
//		}
//		
//		public int hashCode() {
//			return Bone.this.hashCode() + 3 * type.hashCode();
//		}
	}

	public String getName() {
		return strName;
	}

	public void setName(String name) {
		this.strName = name;
	}


	
//	public static final class BoneTransformableType { }
}