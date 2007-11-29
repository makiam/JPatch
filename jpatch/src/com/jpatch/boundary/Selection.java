package com.jpatch.boundary;

import java.util.*;

import javax.vecmath.*;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.control.AttributeEdit;
import com.jpatch.afw.control.JPatchUndoableEdit;
import com.jpatch.afw.vecmath.*;
import com.jpatch.entity.*;
import com.jpatch.entity.sds.*;

public class Selection implements Transformable {
	private final GenericAttr<SdsModel> selectedSdsModelAttr = new GenericAttr<SdsModel>();
	private final CollectionAttr<AbstractVertex> selectedVerticesAttr = new CollectionAttr<AbstractVertex>(LinkedHashSet.class);
	private Point3d[] startPositions;
	private Matrix4d matrix = new Matrix4d();
	
	public GenericAttr<SdsModel> getSelectedSdsModelAttribute() {
		return selectedSdsModelAttr;
	}
	
	public CollectionAttr<AbstractVertex> getSelectedVerticesAttribute() {
		return selectedVerticesAttr;
	}
	
	public void getBounds(Tuple3d p0, Tuple3d p1, Matrix4d matrix) {
		p0.set(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		p1.set(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		Point3d p = new Point3d();
		for (AbstractVertex vertex : selectedVerticesAttr.getElements()) {
			vertex.getPos(p);
			if (matrix != null) {
				matrix.transform(p);
			}
			if (p.x < p0.x) {
				p0.x = p.x;
			}
			if (p.y < p0.y) {
				p0.y = p.y;
			}
			if (p.z < p0.z) {
				p0.z = p.z;
			}
			if (p.x > p1.x) {
				p1.x = p.x;
			}
			if (p.y > p1.y) {
				p1.y = p.y;
			}
			if (p.z > p1.z) {
				p1.z = p.z;
			}
		}
	}
	
	public int getVertexCount() {
		return selectedVerticesAttr.size();
	}
	
	public void getCenter(Point3d center, Matrix4d matrix) {
		if (selectedVerticesAttr.size() == 0) {
			return;
		}
		Miniball mb = new Miniball();
		ArrayList<Point3d> points = new ArrayList<Point3d>();
		for (AbstractVertex vertex : selectedVerticesAttr.getElements()) {
			Point3d p = new Point3d();
			vertex.getPos(p);
			if (matrix != null) {
				matrix.transform(p);
			}
			points.add(p);
		}
		mb.build(points);
		center.set(mb.center());
	}
	
	public void begin() {
		int count = selectedVerticesAttr.getElements().size();
		startPositions = new Point3d[count];
		int i = 0;
		for (AbstractVertex vertex : selectedVerticesAttr.getElements()) {
			startPositions[i] = new Point3d();
			vertex.getPos(startPositions[i]);
			i++;
		}
	}

	public void end(List<JPatchUndoableEdit> editList) {
		int i = 0;
		for (AbstractVertex vertex : selectedVerticesAttr.getElements()) {
			editList.add(AttributeEdit.changeAttribute(vertex.getReferencePosition(), startPositions[i], false));
			i++;
		}
	}

	public void rotate(Point3d pivot, AxisAngle4d axisAngle) {
		/* set matrix to the rotation matrix specified by axisAngle around specivied pivot */
		matrix.set(axisAngle);
		matrix.m03 = pivot.x;
		matrix.m13 = pivot.y;
		matrix.m23 = pivot.z;
		
		matrix.m03 = pivot.x - matrix.m00 * pivot.x - matrix.m01 * pivot.y - matrix.m02 * pivot.z;
		matrix.m13 = pivot.y - matrix.m10 * pivot.x - matrix.m11 * pivot.y - matrix.m12 * pivot.z;
		matrix.m23 = pivot.z - matrix.m20 * pivot.x - matrix.m21 * pivot.y - matrix.m22 * pivot.z;
		transformVertices();
	}

	private void transformVertices() {
		Point3d p = new Point3d();
		int i = 0;
		for (AbstractVertex vertex : selectedVerticesAttr.getElements()) {
			p.set(startPositions[i++]);
			matrix.transform(p);
			vertex.getPosition().setTuple(p);
		}
	}

	public void scale(Scale3d scale) {
		matrix.setIdentity();
		scale.getScaleMatrix(matrix);
		transformVertices();
	}

	public void translate(Vector3d vector) {
		matrix.set(vector);
		transformVertices();
	}
	
	public void getBaseTransform(TransformUtil transformUtil, int space) {
		selectedSdsModelAttr.getValue().getLocal2WorldTransform(transformUtil, space);
	}
	
	public void getPivot(Point3d pivot) {
		getCenter(pivot, null);
	}
}
