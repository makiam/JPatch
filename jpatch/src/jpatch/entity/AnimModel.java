package jpatch.entity;

import java.util.Enumeration;
import java.util.Iterator;

import jpatch.boundary.*;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.vecmath.*;

public class AnimModel extends AnimObject {
	private boolean bParent = false;
	
	protected RenderExtension re = new RenderExtension(new String[] {
		"povray", "",
		"renderman", "Attribute \"visibility\" \"string transmission\" [\"shader\"]\n"
	});
	protected Model model;
	protected int iSubdivisionOffset = 0;
	
	public AnimModel() { }
	
	public AnimModel(Model model) {
		strName = model.getName();
		this.model = model;
	}
	
	public AnimModel(String name, Model model) {
		strName = name;
		this.model = model;
	}
	
	public Model getModel() {
		return model;
	}
	
	public String toString() {
		return strName;
	}
	
	public void setModel(Model model) {
		this.model = model;
	}
	
	public void setName(String name) {
		strName = name;
	}
	
	public void setSubdivisionOffset(int offset) {
		iSubdivisionOffset = offset;
	}
	
	public int getSubdivisionOffset() {
		return iSubdivisionOffset;
	}
	
	public void setRenderString(String format, String version, String renderString) {
		re.setRenderString(format, version, renderString);
	}
	
	public String getRenderString(String format, String version) {
		return re.getRenderString(format, version);
	}
	
	public StringBuffer renderStrings(String prefix) {
		return re.xml(prefix);
	}

	public void getBounds(Point3f p0, Point3f p1) {
		float xMax = -Float.MAX_VALUE;
		float xMin = Float.MAX_VALUE;
		float yMax = -Float.MAX_VALUE;
		float yMin = Float.MAX_VALUE;
		float zMax = -Float.MAX_VALUE;
		float zMin = Float.MAX_VALUE;
		for (Iterator it = model.getCurveSet().iterator(); it.hasNext(); ) {
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
		m4Transform.getRotationScale(m);
		float s = m.getScale();
		p0.scale(s);
		p1.scale(s);
		m.invert();
		m.transform(v);
		v.scale(s);
		p0.add(v);
		p1.add(v);
		
//		m4Transform.transform(p0);
//		m4Transform.transform(p1);
		System.out.println("AnimObject.getBounds() = " + p0 + " " + p1);
	}
	
	public float getRadius() {
		float ds = 0;
		for (Iterator it = model.getCurveSet().iterator(); it.hasNext(); ) {
			for (ControlPoint cp = (ControlPoint) it.next(); cp != null; cp = cp.getNextCheckNextLoop()) {
				if (!cp.isHead())
					continue;
				Point3f p3 = cp.getPosition();
				float f = p3.x * p3.x + p3.y * p3.y + p3.z * p3.z;
				if (f > ds)
					ds = f;
			}
		}
		return (float) Math.sqrt(ds);
	}
	
	public boolean isHit(int x, int y, Matrix4f m4View) {
		Matrix4f mt = new Matrix4f(m4Transform);
		Matrix4f m = new Matrix4f(m4View);
		m.mul(mt);
//		m.invert();
		Point3f p3Hit = new Point3f(x, y, 0);
		Point3f p3 = new Point3f();
		for (Iterator it = model.getCurveSet().iterator(); it.hasNext(); ) {
			for (ControlPoint cp = (ControlPoint) it.next(); cp != null; cp = cp.getNextCheckNextLoop()) {
				if (!cp.isHead())
					continue;
				p3.set(cp.getPosition());
				m.transform(p3);
				p3.z = 0;
				if (p3.distanceSquared(p3Hit) < 64)
					return true;
			}
		}
		return false;		
	}
	
	public void removeFromParent() {
		MainFrame.getInstance().getAnimation().removeModel(this);
	}

	public void setParent(MutableTreeNode newParent) {
		bParent = true;
	}

	public TreeNode getParent() {
		return bParent ? MainFrame.getInstance().getAnimation().getTreenodeModels() : null;
	}
	
	public TreeNode getChildAt(int childIndex) {
		switch(childIndex) {
		case 0:
			return model.getTreenodeExpressions();
		case 1:
			return model.getTreenodeBones();
		}
		throw new ArrayIndexOutOfBoundsException();
	}

	public int getChildCount() {
		return 2;
	}

	public int getIndex(TreeNode node) {
		return -1;
	}

	public boolean getAllowsChildren() {
		return true;
	}

	public boolean isLeaf() {
		return false;
	}

	public Enumeration children() {
		return new Enumeration() {
			private int i = 0;
			public boolean hasMoreElements() {
				return i < 2;
			}

			public Object nextElement() {
				return getChildAt(i++);
			}
		};
	}
}
