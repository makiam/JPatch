package jpatch.entity;

import java.awt.Polygon;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.vecmath.*;

import jpatch.boundary.MainFrame;
import jpatch.boundary.Selection;
import jpatch.control.edit.AtomicChangeAnimObjectScale;
import jpatch.control.edit.AtomicChangeAnimObjectTransform;
import jpatch.control.edit.AtomicModifyMotionCurve;
import jpatch.control.edit.JPatchUndoableEdit;
import jpatch.control.edit.ModifyAnimObject;

public abstract class AnimObject implements MutableTreeNode, Transformable {
	static final double MIN_ROLL = 0.0000001;
	
	protected Matrix4d m4Transform = new Matrix4d();
	protected Matrix4d m4ScaledTransform = new Matrix4d();
	protected Matrix4d m4BackupTransform = new Matrix4d();
	
	protected float fBackupScale;
	protected float fScale = 1.0f;
	
	protected String strName = "(new object)";
	
	protected RenderExtension re = new RenderExtension(new String[] {
			"povray", "",
			"renderman", ""
		});
	
	public AnimObject() {
		m4Transform.setIdentity();
		m4ScaledTransform.set(m4Transform);
	}
	
	public void setRenderString(String format, String version, String renderString) {
		re.setRenderString(format, version, renderString);
	}
	
	public String getRenderString(String format, String version) {
		return re.getRenderString(format, version);
	}
	
	public void setTransform(Matrix4d transform) {
		m4Transform.set(transform);
		m4ScaledTransform.set(m4Transform);
	}
	
	public void setPosition(Point3d position) {
		m4Transform.setTranslation(new Vector3d(position));
		m4ScaledTransform.set(m4Transform);
		m4ScaledTransform.setScale(fScale);
	}
	
	public void setOrientation(Quat4f orient) {
		Quat4d q = new Quat4d(orient);
		q.normalize();
		setOrientation(q);
	}
	
	public void setOrientation(Quat4d orient) {
		m4Transform.setRotation(orient);
		m4ScaledTransform.set(m4Transform);
		m4ScaledTransform.setScale(fScale);
	}
	
	public void setOrientation(double roll, double pitch, double yaw) {
		if (roll != getRoll() || pitch != getPitch() || yaw != getYaw()) {
			if (roll >= 0 && roll <= MIN_ROLL) roll = MIN_ROLL;
			else if (roll <= -0 && roll >= -MIN_ROLL) roll = -MIN_ROLL;
			m4Transform.m00 = Math.sin(-roll) * Math.sin(-pitch) * Math.sin(yaw) + Math.cos(-roll) * Math.cos(yaw);
			m4Transform.m01 = Math.cos(-roll) * Math.sin(-pitch) * Math.sin(yaw) - Math.sin(-roll) * Math.cos(yaw);
			m4Transform.m02 = Math.cos(-pitch) * Math.sin(yaw);
			m4Transform.m10 = Math.sin(-roll) * Math.cos(-pitch);
			m4Transform.m11 = Math.cos(-roll) * Math.cos(-pitch);
			m4Transform.m12 = -Math.sin(-pitch);
			m4Transform.m20 = Math.sin(-roll) * Math.sin(-pitch) * Math.cos(yaw) - Math.cos(-roll) * Math.sin(yaw);
			m4Transform.m21 = Math.cos(-roll) * Math.sin(-pitch) * Math.cos(yaw) + Math.sin(-roll) * Math.sin(yaw);
			m4Transform.m22 = Math.cos(-pitch) * Math.cos(yaw);
		}
		m4ScaledTransform.set(m4Transform);
		m4ScaledTransform.setScale(fScale);
	}
		
	public void setScale(float scale) {
		fScale = scale;
		m4ScaledTransform.set(m4Transform);
		m4ScaledTransform.setScale(fScale);
	}
	
	public void setName(String name) {
		strName = name;
	}
	
	public Matrix4d getTransform() {
		return m4ScaledTransform;
	}
	
	public Point3d getPositionDouble() {
		Vector3d translation = new Vector3d();
		m4Transform.get(translation);
		return new Point3d(translation);
	}
	
	public Quat4f getOrientation() {
		Quat4f orient = new Quat4f();
		m4Transform.get(orient);
		return orient;
	}
	
	public double getRoll() {
		double roll =  Math.atan(m4Transform.m11 / m4Transform.m10) - Math.PI / 2;
		if (m4Transform.m10 < 0) roll -= Math.PI;
		if (roll < -Math.PI) roll += 2 * Math.PI;
		return roll;
	}
	
	public double getPitch() {
		return -Math.asin(-m4Transform.m12);
	}
	
	public double getYaw() {
		double yaw = Math.PI / 2 - Math.atan(m4Transform.m22 / m4Transform.m02);
		if (m4Transform.m02 < 0) yaw += Math.PI;
		return yaw;
	}
	
	public float getScale() {
		return fScale;
	}
	
	public String getName() {
		return strName;
	}
	
	public String toString() {
		return strName;
	}
	
	public void getBounds(Point3f p0, Point3f p1) {
		float xMax = -Float.MAX_VALUE;
		float xMin = Float.MAX_VALUE;
		float yMax = -Float.MAX_VALUE;
		float yMin = Float.MAX_VALUE;
		float zMax = -Float.MAX_VALUE;
		float zMin = Float.MAX_VALUE;
		for (Iterator it = getModel().getCurveSet().iterator(); it.hasNext(); ) {
			for (ControlPoint cp = (ControlPoint) it.next(); cp != null; cp = cp.getNextCheckNextLoop()) {
				if (!cp.isHead())
					continue;
				Point3f p3 = cp.getPosition();
				if (p3.x > xMax) xMax = p3.x;
				if (p3.x < xMin) xMin = p3.x;
				if (p3.y > yMax) yMax = p3.y;
				if (p3.y < yMin) yMin = p3.y;
				if (p3.z > zMax) zMax = p3.z;
				if (p3.z < zMin) zMin = p3.z;
			}
		}
		p0.set(xMin,yMin,zMin);
		p1.set(xMax,yMax,zMax);
		Vector3f v = new Vector3f((float) m4Transform.m03, (float) m4Transform.m13, (float) m4Transform.m23);
		
		Matrix3f m = new Matrix3f();
		m4ScaledTransform.getRotationScale(m);
		float s = m.getScale();
		p0.scale(s);
		p1.scale(s);
		m.invert();
		m.transform(v);
		if (this instanceof AnimModel && ((AnimModel) this).getAnchor() != null)
			v.sub(((AnimModel) this).getAnchor().getPosition());
		v.scale(s);
//		p0.scale(fScale);
//		p1.scale(fScale);
		p0.add(v);
		p1.add(v);
	}
	
	public float getRadius() {
		float ds = 0;
		Point3f pivot = new Point3f();
		if (this instanceof AnimModel) {
			Transformable anchor = ((AnimModel) this).getAnchor();
			if (anchor != null)
				pivot.set(anchor.getPosition());
		}
		for (Iterator it = getModel().getCurveSet().iterator(); it.hasNext(); ) {
			for (ControlPoint cp = (ControlPoint) it.next(); cp != null; cp = cp.getNextCheckNextLoop()) {
				if (!cp.isHead())
					continue;
				Point3f p3 = cp.getPosition();
				float f = pivot.distanceSquared(p3);
				if (f > ds)
					ds = f;
			}
		}
		return (float) Math.sqrt(ds) * fScale;
	}
	
	public abstract Model getModel();
	
	public boolean isHit(int x, int y, Matrix4f m4View) {
		Matrix4f mt = new Matrix4f(m4ScaledTransform);
		Matrix4f m = new Matrix4f(m4View);
		m.mul(mt);
//		m.invert();
		Point3f p3Hit = new Point3f(x, y, 0);
		Point3f p3 = new Point3f();
		for (Iterator it = getModel().getCurveSet().iterator(); it.hasNext(); ) {
			for (ControlPoint cp = (ControlPoint) it.next(); cp != null; cp = cp.getNextCheckNextLoop()) {
				if (!cp.isHead())
					continue;
				p3.set(cp.getPosition());
				m.transform(p3);
				p3.z = 0;
				if (p3.distanceSquared(p3Hit) < 64) {
//					if (this instanceof AnimModel) {
//						((AnimModel) this).setAnchor(cp);
//					}
					return true;
				}
			}
		}
		return false;		
	}
	
	public abstract void xml(StringBuffer sb, String prefix);
	
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

	public TreeNode getChildAt(int childIndex) {
		return null;
	}

	public int getChildCount() {
		return 0;
	}

	public int getIndex(TreeNode node) {
		return -1;
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
	
	/*
	 * Transformable interface implementation
	 */
	
	public Point3f getPosition() {
		Vector3d translation = new Vector3d();
		m4Transform.get(translation);
		return new Point3f(translation);
	}
	
	public void beginTransform() {
		m4BackupTransform.set(m4Transform);
		fBackupScale = fScale;
	}

	public void translate(Vector3f v) {
		Vector3f vv = new Vector3f(v);
		MainFrame.getInstance().getConstraints().constrainVector(vv);
		m4Transform.set(m4BackupTransform);
		m4Transform.m03 += vv.x;
		m4Transform.m13 += vv.y;
		m4Transform.m23 += vv.z;
		m4ScaledTransform.set(m4Transform);
		m4ScaledTransform.setScale(fScale);
		MainFrame.getInstance().getSideBar().updatePanel();
	}

	private static final Matrix3f m1 = new Matrix3f();
	private static final Matrix3f m2 = new Matrix3f();
	public void rotate(AxisAngle4f a, Point3f pivot) {
		Matrix3f m = new Matrix3f();
		m.set(a);
		
		m4BackupTransform.getRotationScale(m1);
		m2.set(m);
		m2.mul(m1);
		m4Transform.setRotationScale(m2);
		m4ScaledTransform.set(m4Transform);
		m4ScaledTransform.setScale(fScale);
//		transform(m, pivot);
		MainFrame.getInstance().getSideBar().updatePanel();
	}

//	private static final Matrix3f m1 = new Matrix3f();
//	private static final Matrix3f m2 = new Matrix3f();
	public void transform(Matrix3f m, Point3f pivot) {
		setScale(fBackupScale * m.getScale());
//		m4BackupTransform.getRotationScale(m1);
//		m4BackupTransform.getRotationScale(m2);
//		m2.invert();
//		m1.mul(m);
//		m1.mul(m2);
//		m4Transform.setRotationScale(m1);
//		m4BackupTransform.getRotationScale(m1);
//		m2.set(m);
//		m2.mul(m1);
//		m4Transform.setRotationScale(m2);
//		m4ScaledTransform.set(m4Transform);
//		m4ScaledTransform.setScale(fScale);
		MainFrame.getInstance().getSideBar().updatePanel();
	}

	public JPatchUndoableEdit endTransform() {
		Animation animation = MainFrame.getInstance().getAnimation();
		MotionCurveSet mcs = animation.getCurvesetFor(this);
		float position = animation.getPosition();
		ModifyAnimObject edit = new ModifyAnimObject(this);
		Point3d newPosition = getPositionDouble();
		Quat4f newOrientation = getOrientation();
		m4Transform.set(m4BackupTransform);
		m4ScaledTransform.set(m4Transform);
		m4ScaledTransform.setScale(fScale);
		if (!newPosition.equals(getPositionDouble()))
			edit.addEdit(new AtomicModifyMotionCurve.Point3d(mcs.position, position, newPosition));
		if (!newOrientation.equals(getOrientation()))
			edit.addEdit(new AtomicModifyMotionCurve.Quat4f(mcs.orientation, position, newOrientation));
		if (fScale != fBackupScale) {
			if (this instanceof AnimModel) {
				edit.addEdit(new AtomicModifyMotionCurve.Float(((MotionCurveSet.Model) mcs).scale, position, fScale));
			} else {
				edit.addEdit(new AtomicChangeAnimObjectScale(this, fBackupScale));
			}
		}
		mcs.setPosition(position);
		return edit;	
	}
}
