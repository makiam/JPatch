package jpatch.entity;

import javax.swing.tree.TreeNode;
import javax.vecmath.*;

import jpatch.boundary.JPatchTreeLeaf;
/**
 * Rotation Degree of freedom (Dof) for joints
 * @author sascha
 */
public class RotationDof extends JPatchTreeLeaf {
	private Bone bone;
	private Vector3f v3ReferenceAxis;
	private Vector3f v3Axis;
	private float fMinAngle;
	private float fMaxAngle;
	private float fDefaultAngle;
	private float fCurrentAngle;
	private Matrix4f m4Transform = new Matrix4f();
	private Matrix4f m4InvTransform = new Matrix4f();
	private boolean bValid = false;
	
	public RotationDof(Bone bone) {
		this.bone = bone;
		v3ReferenceAxis = new Vector3f(1,0,0);
		v3Axis = new Vector3f();
		fMinAngle = (float) - Math.PI / 2;
		fMaxAngle = (float) Math.PI / 2;
		iNodeType = RDOF;
		strName = "RDOF";
	}
	
	public Bone getBone() {
		return bone;
	}
	
	public TreeNode getParent() {
		return bone;
	}
	
	public RotationDof getParentDof() {
		int index = bone.getDofIndex(this);
		if (index > 0)
			return bone.getDof(index - 1);
		else if (bone.getParentBone() != null)
			return bone.getParentBone().getDof(-1);
		else
			return null;
	}
	
	public Vector3f getAxis() {
		return new Vector3f(v3ReferenceAxis);
	}
	
	public void setAxis(Vector3f axis) {
		v3ReferenceAxis.set(axis);
	}
	
	public boolean isTransformValid() {
		if (!bValid) {
			return false;
		} else {
			RotationDof parentDof = getParentDof();
			if (parentDof != null) {
				return parentDof.isTransformValid();
			} else
				return true;
		}
	}
	
	private void computeTransform() {
		RotationDof parentDof = getParentDof();
		if (parentDof != null)
			m4Transform.set(getParentDof().getTransform());
		else
			m4Transform.setIdentity();
		v3Axis.set(v3ReferenceAxis);
		m4Transform.transform(v3Axis);
		Matrix4f m4 = new Matrix4f();
		m4.set(new AxisAngle4f(v3Axis, fCurrentAngle));
		Point3f pivot = bone.getStart(null);
		Vector3f v = new Vector3f(pivot);
		Vector3f v2 = new Vector3f(v);
		m4.transform(v2);
		v.sub(v2);
		m4.setTranslation(v);
		bValid = true;
	}
	
	public Matrix4f getTransform() {
		if (!isTransformValid())
			computeTransform();
		return m4Transform;
	}

	public Matrix4f getInvTransform() {
		if (!bValid) {
			computeTransform();
			m4InvTransform.invert(m4Transform);
		}
		return m4InvTransform;
	}
	
	public float getCurrentAngle() {
		return fCurrentAngle;
	}

	public void setCurrentAngle(float currentAngle) {
		fCurrentAngle = currentAngle;
		bValid = false;
	}

	public float getDefaultAngle() {
		return fDefaultAngle;
	}

	public void setDefaultAngle(float defaultAngle) {
		fDefaultAngle = defaultAngle;
	}

	public float getMaxAngle() {
		return fMaxAngle;
	}

	public void setMaxAngle(float maxAngle) {
		fMaxAngle = maxAngle;
	}

	public float getMinAngle() {
		return fMinAngle;
	}

	public void setMinAngle(float minAngle) {
		fMinAngle = minAngle;
	}
	
	public int getSliderValue() {
		return (int) ((fCurrentAngle - fMinAngle) / (fMaxAngle - fMinAngle) * 100f);
	}
	
	public void setSliderValue(int sliderValue) {
		fCurrentAngle = fMinAngle + (fMaxAngle - fMinAngle) / 100f * (float) sliderValue;
	}
}
