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
	
	public GenericAttr<SdsModel> getSelectedSdsModelAttribute() {
		return selectedSdsModelAttr;
	}
	
	public CollectionAttr<AbstractVertex> getSelectedVerticesAttribute() {
		return selectedVerticesAttr;
	}
	
	public void getBounds(Tuple3d p0, Tuple3d p1) {
		p0.set(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		p1.set(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		SdsModel sdsModel = selectedSdsModelAttr.getValue();
		Transform transform = sdsModel.getTransform();
		Point3d p = new Point3d();
		for (AbstractVertex vertex : selectedVerticesAttr.getElements()) {
			vertex.getPos(p);
			transform.transform(p);
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
	
	public void getCenter(Point3d center) {
		Point3d p = new Point3d();
		getBounds(p, center);
		center.interpolate(p, 0.5);
	}
	
//	public Sphere getBounds(Sphere sphere) {
//		SdsModel sdsModel = selectedSdsModelAttr.getValue();
//		Transform transform = sdsModel.getTransform();
//		Point3d p = new Point3d();
//		Point3d center = new Point3d();
//		getBounds(p, center);
//		center.interpolate(p, 0.5);
////		for (AbstractVertex vertex : selectedVerticesAttr.getElements()) {
////			vertex.getPos(p);
////			center.add(p);
////		}
////		center.scale(1.0 / selectedVerticesAttr.size());
//		double radiusSq = 0;
//		for (AbstractVertex vertex : selectedVerticesAttr.getElements()) {
//			vertex.getPos(p);
//			transform.transform(p);
//			double distanceSq = p.distanceSquared(center);
//			if (distanceSq > radiusSq) {
//				radiusSq = distanceSq;
//			}
//		}
//		transform.transform(center);
//		Matrix4d m = transform.getMatrix(new Matrix4d());
//		
//		sphere.setCenter(center);
//		sphere.setRadius(Math.sqrt(radiusSq) * m.getScale());
//		
//		return sphere;
//	}

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

	public void rotateTo(Point3d pivot, AxisAngle4d axisAngle) {
		Matrix3d rotation = new Matrix3d();
		rotation.set(axisAngle);
		Point3d xPivot = new Point3d(pivot);
		rotation.transform(xPivot);
		Matrix4d matrix = new Matrix4d(
				rotation.m00, rotation.m01, rotation.m02, pivot.x - xPivot.x,
				rotation.m10, rotation.m11, rotation.m12, pivot.y - xPivot.y,
				rotation.m20, rotation.m21, rotation.m22, pivot.z - xPivot.z,
				0, 0, 0, 1
		);
		int i = 0;
		Point3d p = new Point3d();
		for (AbstractVertex vertex : selectedVerticesAttr.getElements()) {
			p.set(startPositions[i]);
			matrix.transform(p);
			vertex.getPosition().setTuple(p);
			i++;
		}
	}

	public void transform(Matrix4d matrix) {
		int i = 0;
		Point3d p = new Point3d();
		for (AbstractVertex vertex : selectedVerticesAttr.getElements()) {
			p.set(startPositions[i]);
			matrix.transform(p);
			vertex.getPosition().setTuple(p);
			i++;
		}
	}
	
	
}
