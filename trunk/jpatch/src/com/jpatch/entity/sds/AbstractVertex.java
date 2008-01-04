package com.jpatch.entity.sds;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.ui.*;

import javax.vecmath.*;

import trashcan.HardBoundedDoubleAttr;
import trashcan.TransformedPoint3;

public abstract class AbstractVertex {
	final TransformedPoint3 position = new TransformedPoint3(0, 0, 0);
	final DoubleAttr sharpness = AttributeManager.getInstance().createBoundedDoubleAttr(new DoubleAttr(0), new DoubleAttr(10));
	
	final Point3f projectedPos = new Point3f();
	
	public TransformedPoint3 getPosition() {
		return position;
	}
	
	public Tuple3Attr getReferencePosition() {
		return position.getReferenceTuple3();
	}
	
	public DoubleAttr getSharpness() {
		return sharpness;
	}
	
	public double sharpness() {
		return sharpness.getDouble();
	}
	
	public void project(Matrix4f matrix) {
		getPos(projectedPos);
		matrix.transform(projectedPos);
	}
	
	public void getReferencePos(Tuple3d p) {
		position.getReferenceTuple3().getTuple(p);
	}
	
	public void getReferencePos(Tuple3f p) {
		position.getReferenceTuple3().getTuple(p);
	}
	
	public Tuple3d getPos(Tuple3d p) {
		position.getTuple(p);
		return p;
	}
	
	public Tuple3f getPos(Tuple3f p) {
		position.getTuple(p);
		return p;
	}
	
	public void getProjectedPos(Tuple3f p) {
		p.set(projectedPos);
	}
}
