package com.jpatch.boundary;

import java.util.*;

import javax.vecmath.*;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.vecmath.*;
import com.jpatch.entity.*;
import com.jpatch.entity.sds.*;

public class Selection {
	private final GenericAttr<SdsModel> selectedSdsModelAttr = new GenericAttr<SdsModel>();
	private final CollectionAttr<AbstractVertex> selectedVerticesAttr = new CollectionAttr<AbstractVertex>(HashSet.class);
	
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
	
	public void getBounds(Sphere sphere) {
		SdsModel sdsModel = selectedSdsModelAttr.getValue();
		Transform transform = sdsModel.getTransform();
		Point3d p = new Point3d();
		Point3d center = new Point3d();
		for (AbstractVertex vertex : selectedVerticesAttr.getElements()) {
			vertex.getPos(p);
			center.add(p);
		}
		center.scale(1.0 / selectedVerticesAttr.size());
		double radiusSq = 0;
		for (AbstractVertex vertex : selectedVerticesAttr.getElements()) {
			vertex.getPos(p);
			double distanceSq = p.distanceSquared(center);
			if (distanceSq > radiusSq) {
				radiusSq = distanceSq;
			}
		}
		transform.transform(center);
		Matrix4d m = transform.getMatrix(new Matrix4d());
		
		sphere.setCenter(center);
		sphere.setRadius(Math.sqrt(radiusSq) * m.getScale());
	}
}
