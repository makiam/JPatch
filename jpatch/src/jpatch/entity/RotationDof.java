package jpatch.entity;

import java.util.Iterator;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.vecmath.*;

import jpatch.auxilary.Utils3D;
import jpatch.auxilary.XMLutils;
import jpatch.boundary.JPatchTreeLeaf;
import jpatch.boundary.MainFrame;
/**
 * Rotation Degree of freedom (Dof) for joints
 * @author sascha
 */
public class RotationDof extends Morph {
	public static final int ORTHO_1 = 1;
	public static final int ORTHO_2 = 2;
	public static final int RADIAL = 4;
	
	private Bone bone;
	private int iAxis;
//	private Vector3f v3ReferenceAxis;
//	private Vector3f v3Axis;
//	private float fMinAngle;
//	private float fMaxAngle;
//	private float fDefaultAngle;
//	private float fCurrentAngle;
	private Matrix4f m4Transform = new Matrix4f();
	private Matrix4f m4InvTransform = new Matrix4f();
	private boolean bValid = false;
//	private Morph morph;
	
	public RotationDof(Bone bone, int axis) {
		this.bone = bone;
//		v3ReferenceAxis = new Vector3f(0,0,1);
//		v3Axis = new Vector3f();
		iAxis = axis;
		fMin = -90;
		fMax = 90;
		strName = getAxisName();
	}
	
	public Bone getBone() {
		return bone;
	}
	
	public RotationDof getParentDof() {
		int index = bone.getDofIndex(this);
		if (index > 0)
			return bone.getDof(index - 1);
		Bone b = bone.getParentBone();
		while (b != null) {
			RotationDof d = b.getDof(-1);
			if (d != null)
				return d;
			b = b.getParentBone();
		}
		return null;
	}
	
	public int getType() {
		return iAxis;
	}
	
	public String getAxisName() {
		if (iAxis == 1)
			return "Pitch (bend)";
		else if (iAxis == 2)
			return "Yaw (bend)";
		else if (iAxis == 4)
			return "Roll (twist)";
		else
			return "";
	}
	
	public Vector3f getAxis() {
		Vector3f axis = null;
		Vector3f v = new Vector3f(bone.getReferenceEnd());
		v.sub(bone.getReferenceStart());
		switch (iAxis) {
			case ORTHO_1: {
				axis = Utils3D.perpendicularVector(v);
			}
			break;
			case ORTHO_2: {
				Vector3f vv = Utils3D.perpendicularVector(v);
				axis = new Vector3f();
				axis.cross(v, vv);
			}
			break;
			case RADIAL: {
				axis = v;
			}
			break;
		}
		axis.normalize();
		return axis;
	}
//	
//	public void setAxis(Vector3f axis) {
//		v3ReferenceAxis.set(axis);
//	}
	
	public boolean isTransformValid() {
		return bValid;
//		System.out.println(bone + "." + this + " isTransformValid()");
//		if (!bValid) {
//			return false;
//		} else {
//			RotationDof parentDof = getParentDof();
//			if (parentDof != null) {
//				return parentDof.isTransformValid();
//			} else
//				return true;
//		}
	}
	
	private void invalidate() {
		for (int i = bone.getDofIndex(this), n = bone.getDofs().size(); i < n; i++)
			((RotationDof) bone.getDof(i)).bValid = false;
		invalidate(bone);
//		bValid = false;
	}
	
	private void invalidate(Bone bone) {
		for (Iterator it = bone.getChildBones().iterator(); it.hasNext(); ) {
			Bone b = (Bone) it.next();
			for (Iterator jt = b.getDofs().iterator(); jt.hasNext(); )
				((RotationDof) jt.next()).bValid = false;
			invalidate(b);
		}
	}
	
	private void computeTransform() {
//		System.out.println(" computeTransform " + bone + " " + bone.getDofs().indexOf(this));
		RotationDof parentDof = getParentDof();
		if (parentDof != null)
			m4Transform.set(getParentDof().getTransform());
		else
			m4Transform.setIdentity();
		Vector3f v3Axis = getAxis();
		m4Transform.transform(v3Axis);
//		System.out.println("  raxis=" + v3ReferenceAxis + " axis=" + v3Axis + " angle=" + fCurrentAngle / Math.PI * 180);
		Matrix4f m4 = new Matrix4f();
		m4.setIdentity();
		m4.set(new AxisAngle4f(v3Axis, fValue / 180 * (float) Math.PI));
		Point3f pivot = bone.getStart(null);
//		System.out.println("  Pivot = " + pivot);
		Vector3f v = new Vector3f(pivot);
		Vector3f v2 = new Vector3f(v);
		m4.transform(v2);
		v.sub(v2);
		m4.setTranslation(v);
		
		//m4Transform.mul(m4);
		m4.mul(m4Transform);
		m4Transform.set(m4);
		
//		m4Transform.setIdentity();
		m4InvTransform.invert(m4Transform);
		bValid = true;
	}
	
	public Matrix4f getTransform() {
		if (!isTransformValid())
			computeTransform();
		return m4Transform;
	}

	public Matrix4f getInvTransform() {
		if (!bValid)
			computeTransform();
		return m4InvTransform;
	}
	
	public void setPointTransform(Matrix4f m) {
		
	}

	public void setValue(float value) {
		fValue = value;
		invalidate();
		setMorphMap();
		MainFrame.getInstance().getModel().applyMorphs();
		MainFrame.getInstance().getModel().setPose();
//		setMorphValues();
	}

//	public float getDefaultAngle() {
//		return fDefaultAngle;
//	}

//	public void setDefaultAngle(float defaultAngle) {
//		fDefaultAngle = defaultAngle;
//	}

//	public float getMax() {
//		return fMax;
//	}
//
//	public float getMin() {
//		return fMin;
//	}
//
//	public void setMin(float min) {
//		fMin = min;
//	}
	
//	public int getSliderValue() {
//		return (int) ((fCurrentAngle - fMinAngle) / (fMaxAngle - fMinAngle) * 100f);
//	}
	
	public void setSliderValue(int sliderValue) {
//		fCurrentAngle = fMinAngle + (fMaxAngle - fMinAngle) / 100f * (float) sliderValue;
		setValue(fMin + (fMax - fMin) / 100f * (float) sliderValue);
//		invalidate();
//		setMorphValues();
	}
	
//	private void setMorphValues() {
//		float min = 0;
//		float max = 0;
//		if (fCurrentAngle > 0)
//			max = fCurrentAngle / fMaxAngle;
//		else if (fCurrentAngle < 0)
//			min = fCurrentAngle / fMinAngle;
////		if (minMorph != null) {
////			if (minMorph.getValue() != min) {
////				minMorph.unapply();
////				minMorph.setValue(min);
////				minMorph.apply();
////			}
////		if (maxMorph != null)
////			if (maxMorph.getValue() != max) {
////				maxMorph.unapply();
////				maxMorph.setValue(max);
////				maxMorph.apply();
////			}
////		}
//	}
	
	public StringBuffer xml(String prefix) {
		StringBuffer sb = new StringBuffer();
		sb.append(prefix).append("<dof type=\"rotation\" name=").append(XMLutils.quote(strName)).append(">\n");
//		sb.append(prefix).append("\t<axis x=\"" + v3ReferenceAxis.x + "\" y=\"" + v3ReferenceAxis.y + "\" z=\"" + v3ReferenceAxis.z + "\"/>\n");
		sb.append(prefix).append("\t<angle min=\"" + fMin + "\" max=\"" + fMax + "\" current=\"" + fValue + "\"/>\n");
		sb.append(prefix).append("</dof>\n");
		return sb;
	}

//	public Morph getMorph() {
//		return morph;
//	}
//
//	public void setMorph(Morph morph) {
//		this.morph = morph;
//	}
	
	public void dump() {
		System.out.println("RDOF " + strName);
		for (Iterator it = listTargets.iterator(); it.hasNext(); ) {
			((MorphTarget) it.next()).dump();
		}
	}
	
	/*
	 * mutable treenode
	 */
	public TreeNode getParent() {
		return bInserted ? bone : null;
	}
	
	public void removeFromParent() {
		bone.remove(this);
	}

	public void setParent(MutableTreeNode newParent) {
		bInserted = true;
	}
}
