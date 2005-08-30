package jpatch.boundary.selection;

import java.util.*;
import javax.vecmath.*;

import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.control.edit.ChangeSelectionPivotEdit;
import jpatch.control.edit.JPatchAbstractUndoableEdit;
import jpatch.control.edit.JPatchUndoableEdit;
import jpatch.auxilary.*;
import jpatch.entity.*;

public class PointSelection extends Selection {
	private static PointSelection ps = new PointSelection(false);
	private static ControlPoint[] acp = new ControlPoint[0];

	protected Collection colCp;
//	protected Point3f p3Pivot;
	protected Matrix3f m3Rotation = new Matrix3f();;
	protected Point3f p3CornerA = new Point3f();
	protected Point3f p3CornerB = new Point3f();
	protected ControlPoint cpHot;
	protected boolean bDirection = false;
	protected boolean bCurve = false;
	protected Point3f p3TempPivot = new Point3f();
	protected Point3f p3PermPivot = new Point3f();
	protected Transformable pivot = new Transformable() {
		public void prepareForTemporaryTransformation() {
			p3TempPivot.set(p3PermPivot);
		}
		public JPatchAbstractUndoableEdit transformPermanently(Matrix4f m) {
			p3TempPivot.set(p3PermPivot);
			m.transform(p3TempPivot);
			return new ChangeSelectionPivotEdit(PointSelection.this, p3TempPivot, null);
		}

		public void transformTemporarily(Matrix4f m) {
			p3TempPivot.set(p3PermPivot);
			m.transform(p3TempPivot);
		}
	};
	
	private PointSelection(boolean b) {
	}
	
	public PointSelection() {
		colCp = new HashSet();
		m3Rotation.setIdentity();
	}
	
	public PointSelection(PointSelection ps) {
		this();
		addPointSelection(ps);
	}
	
	public PointSelection(ControlPoint controlPoint) {
		this();
		setControlPoint(controlPoint);
	}
	
	public static Class getPointSelectionClass() {
		return ps.getClass();
	}
	
	public void addPointSelection(PointSelection ps) {
		colCp.addAll(ps.colCp);
	}
	
	public Collection getSelectedControlPoints() {
		return colCp;
	}
	
	public ArrayList getTransformables() {
		ArrayList list = new ArrayList(colCp);
		list.add(pivot);
		if (MainFrame.getInstance().getMode() != MainFrame.MORPH) {
			for (Iterator it = MainFrame.getInstance().getModel().getMorphIterator(); it.hasNext(); ) {
				Transformable t = ((Morph) it.next()).getTransformable((Set) colCp);
				if (t != null)
					list.add(t);
			}
		}
		return list;
	}
	
	public void xorPointSelection(PointSelection ps) {
		for (Iterator i = ps.colCp.iterator(); i.hasNext(); ) {
			ControlPoint cp = (ControlPoint)i.next();
			if (colCp.contains(cp)) {
				colCp.remove(cp);
			} else {
				colCp.add(cp);
			}
		}
		if (colCp.size() == 0) {
			MainFrame.getInstance().setSelection(null);
		}
	}
	
	public void setControlPoint(ControlPoint controlPoint) {
		colCp.clear();
		colCp.add(controlPoint);
		cpHot = controlPoint;
	}
	
	public boolean isSingle() {
		return (colCp.size() == 1);
	}
	
	public ControlPoint[] getControlPointArray() {
		return (ControlPoint[])colCp.toArray(acp);
	}
	
	public void setHotCp(ControlPoint cp) {
		cpHot = cp;
	}
	
	public ControlPoint getHotCp() {
		return cpHot;
	}
	
	public Point3f[] getPointArray() {
		Point3f[] ap3 = new Point3f[colCp.size()];
		int p = 0;
		for (Iterator it = colCp.iterator(); it.hasNext();) {
			//ControlPoint cp = (ControlPoint)it.next();
			//Point3f p3 = cp.getPosition();
			//ap3[p++] = new Point3f(p3);
			ap3[p++] = new Point3f(((ControlPoint)it.next()).getPosition());
		}
		return ap3;
	}
	
	public ControlPoint getControlPoint() {
		if (isSingle()) {
			return (ControlPoint)colCp.iterator().next();
		} else {
			throw new IllegalStateException("attempted to get singe ControlPoint but size != 1");
		}
	}
	/*
	public ControlPoint getControlPoint(int index) {
		return (ControlPoint)alCp.get(index);
	}
	*/
	public int getSize() {
		return colCp.size();
	}
	
	public void addControlPoint(ControlPoint controlPoint) {
		colCp.add(controlPoint);
	}
	
	public void removeControlPoint(ControlPoint controlPoint) {
		colCp.remove(controlPoint);
		if (colCp.size() == 0) {
			MainFrame.getInstance().setSelection(null);
		}
	}
	
	public boolean contains(ControlPoint controlPoint) {
		return colCp.contains(controlPoint);
	}
	
	public void applyMaterial(JPatchMaterial material) {
		for (Patch patch = MainFrame.getInstance().getModel().getFirstPatch(); patch != null; patch = patch.getNext()) {
			if (patch.isSelected(this)) {
				patch.setMaterial(material);
			}
		}
	}
	/*
	public void setPivot(Point3f pivot) {
		p3Pivot.set(pivot);
	}
	*/
	public Point3f getPivot() {
//		if (p3TempPivot == null)
//			resetPivotToCenter();
		return p3TempPivot;
	}
	
	public Point3f getOldPivot() {
//		if (p3TempPivot == null)
//			resetPivotToCenter();
		return p3PermPivot;
	}
	
	public Point3f getCornerA() {
		computeBounds();
		return p3CornerA;
	}
	
	public Point3f getCornerB() {
		computeBounds();
		return p3CornerB;
	}
	
	public Matrix3f getRotation() {
		if (m3Rotation == null) {
			m3Rotation = new Matrix3f();
			m3Rotation.setIdentity();
		}
		return m3Rotation;
	}
	
	public void resetPivotToCenter() {
		setPivot(getCenter());
	}
	
	public void setPivot(Point3f pivot) {
		p3TempPivot.set(pivot);
		p3PermPivot.set(pivot);
	}
	
	protected void computeBounds() {
		float xMax = -Float.MAX_VALUE;
		float xMin = Float.MAX_VALUE;
		float yMax = -Float.MAX_VALUE;
		float yMin = Float.MAX_VALUE;
		float zMax = -Float.MAX_VALUE;
		float zMin = Float.MAX_VALUE;
		Point3f[] ap3 = getPointArray();
		Point3f p3 = new Point3f();
		Matrix3f m3 = new Matrix3f(m3Rotation);
		m3.invert();
		for (int p = 0; p < ap3.length; p++) {
			p3.set(ap3[p]);
			m3.transform(p3);
			if (p3.x > xMax) xMax = p3.x;
			if (p3.x < xMin) xMin = p3.x;
			if (p3.y > yMax) yMax = p3.y;
			if (p3.y < yMin) yMin = p3.y;
			if (p3.z > zMax) zMax = p3.z;
			if (p3.z < zMin) zMin = p3.z;
		}
		p3CornerA.set(xMin,yMin,zMin);
		p3CornerB.set(xMax,yMax,zMax);
	}
	
	public Point3f getCenter() {
		Point3f p3 = new Point3f();
		computeBounds();
		p3.interpolate(p3CornerA,p3CornerB,0.5f);
		m3Rotation.transform(p3);
		return p3;
	}
	
	/**
	 * returns StringBuffer containing an XML representation of the curve, used to save
	 * models in XML format
	 *
	 * @return A StringBuffer containing an XML representation of this curve
	 */
	public StringBuffer xml(int tabs) {
		StringBuffer sbIndent = XMLutils.indent(tabs);
		StringBuffer sbLineBreak = XMLutils.lineBreak();
		StringBuffer sb = new StringBuffer();
		sb.append(sbIndent).append("<selection name=\"" + getName() + "\">");
		//int size = getType();
		//int p = 0;
		for (Iterator it = colCp.iterator(); it.hasNext();) {
			int i = ((ControlPoint) it.next()).getXmlNumber();
			if (i != -1) {
				sb.append(i);
				if (it.hasNext()) {
					sb.append(",");
				}
			}
		}
		sb.append("</selection>").append(sbLineBreak);
		return sb;
	}
	
	public boolean getDirection() {
		return bDirection;
	}
	
	public boolean isCurve() {
		return bCurve && isSingle();
	}
	
	public void nextPoint() {
		if (!bCurve) {
			bCurve = true;
		} else {
			if (isSingle()) {
				if (bDirection) {
					ControlPoint cp = getControlPoint();
					if (cp.getPrevAttached() != null) {
						setControlPoint(cp.getPrevAttached());
					} else {
						setControlPoint(cp.getHead());
						bCurve = false;
					}
				}
				bDirection = !bDirection;
			}
		}
		//System.out.println(getControlPoint() + " " + getControlPoint().getPrevAttached() + " " + bDirection);
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof PointSelection))
			return false;
		PointSelection ps = (PointSelection) o;
		if (ps != null && colCp.size() == ps.colCp.size())
			return colCp.containsAll(ps.colCp);
		else
			return false;
	}
}
