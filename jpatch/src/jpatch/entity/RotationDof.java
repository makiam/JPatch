package jpatch.entity;

import java.util.Iterator;

import javax.vecmath.*;

import jpatch.auxilary.XMLutils;
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
	private Morph morph;
	
	public RotationDof(Bone bone) {
		this.bone = bone;
		v3ReferenceAxis = new Vector3f(0,0,1);
		v3Axis = new Vector3f();
		fMinAngle = (float) - Math.PI / 2;
		fMaxAngle = (float) Math.PI / 2;
		strName = "RDOF";
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
	
	public Vector3f getAxis() {
		return new Vector3f(v3ReferenceAxis);
	}
	
	public void setAxis(Vector3f axis) {
		v3ReferenceAxis.set(axis);
	}
	
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
		v3Axis.set(v3ReferenceAxis);
		m4Transform.transform(v3Axis);
//		System.out.println("  raxis=" + v3ReferenceAxis + " axis=" + v3Axis + " angle=" + fCurrentAngle / Math.PI * 180);
		Matrix4f m4 = new Matrix4f();
		m4.setIdentity();
		m4.set(new AxisAngle4f(v3Axis, fCurrentAngle));
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
	
	public float getCurrentAngle() {
		return fCurrentAngle;
	}

	public void setCurrentAngle(float currentAngle) {
		fCurrentAngle = currentAngle;
		invalidate();
		setMorphValues();
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
		invalidate();
		setMorphValues();
	}
	
	private void setMorphValues() {
		float min = 0;
		float max = 0;
		if (fCurrentAngle > 0)
			max = fCurrentAngle / fMaxAngle;
		else if (fCurrentAngle < 0)
			min = fCurrentAngle / fMinAngle;
//		if (minMorph != null) {
//			if (minMorph.getValue() != min) {
//				minMorph.unapply();
//				minMorph.setValue(min);
//				minMorph.apply();
//			}
//		if (maxMorph != null)
//			if (maxMorph.getValue() != max) {
//				maxMorph.unapply();
//				maxMorph.setValue(max);
//				maxMorph.apply();
//			}
//		}
	}
	public StringBuffer xml(String prefix) {
		StringBuffer sb = new StringBuffer();
		sb.append(prefix).append("<dof type=\"rotation\" name=").append(XMLutils.quote(strName)).append(">\n");
		sb.append(prefix).append("\t<axis x=\"" + v3ReferenceAxis.x + "\" y=\"" + v3ReferenceAxis.y + "\" z=\"" + v3ReferenceAxis.z + "\"/>\n");
		sb.append(prefix).append("\t<angle min=\"" + fMinAngle * 180 / (float) Math.PI + "\" max=\"" + fMaxAngle * 180 / (float) Math.PI + "\" current=\"" + fCurrentAngle * 180 / (float) Math.PI + "\"/>\n");
		sb.append(prefix).append("</dof>\n");
		return sb;
	}

	public Morph getMorph() {
		return morph;
	}

	public void setMorph(Morph morph) {
		this.morph = morph;
	}
}
