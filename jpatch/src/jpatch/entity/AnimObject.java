package jpatch.entity;

import java.awt.Polygon;
import java.util.Enumeration;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.vecmath.*;

import jpatch.boundary.MainFrame;
import jpatch.boundary.Selection;
import jpatch.control.edit.AtomicChangeAnimObjectTransform;
import jpatch.control.edit.JPatchUndoableEdit;

public abstract class AnimObject implements MutableTreeNode, Transformable {
	static final double MIN_ROLL = 0.0000001;
	
	protected Matrix4d m4Transform = new Matrix4d();
	protected Matrix4d m4BackupTransform = new Matrix4d();
	
	protected String strName = "(new object)";
	
	public AnimObject() {
		m4Transform.setIdentity();
	}
	
	public void setTransform(Matrix4d transform) {
		m4Transform.set(transform);
	}
	
	public void setPosition(Point3d position) {
		m4Transform.setTranslation(new Vector3d(position));
	}
	
	public void setOrientation(Quat4f orient) {
		Quat4d q = new Quat4d(orient);
		q.normalize();
		setOrientation(q);
	}
	
	public void setOrientation(Quat4d orient) {
		m4Transform.setRotation(orient);
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
	}
		
	public void setScale(float scale) {
		m4Transform.setScale(scale);
	}
	
	public void setName(String name) {
		strName = name;
	}
	
	public Matrix4d getTransform() {
		return m4Transform;
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
		return (float) m4Transform.getScale();
	}
	
	public String getName() {
		return strName;
	}
	
	public String toString() {
		return strName;
	}
	
	public abstract void getBounds(Point3f p3A, Point3f p3B);
	
	
	
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
	}

	public void translate(Vector3f v) {
		m4Transform.set(m4BackupTransform);
		m4Transform.m03 += v.x;
		m4Transform.m13 += v.y;
		m4Transform.m23 += v.z;
	}

	public void rotate(AxisAngle4f a, Point3f pivot) {
		// TODO Auto-generated method stub
		
	}

	public void transform(Matrix3f m, Point3f pivot) {
		// TODO Auto-generated method stub
		
	}

	public JPatchUndoableEdit endTransform() {
		MainFrame.getInstance().getAnimation().getCurvesetFor(this).updateCurves(MainFrame.getInstance().getAnimation().getPosition());
		return new AtomicChangeAnimObjectTransform(this, m4BackupTransform);
	}
}
