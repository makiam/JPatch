package com.jpatch.entity.sds;

import com.jpatch.afw.attributes.*;

import javax.vecmath.*;

public abstract class AbstractVertex {
	final TransformedPoint3 position = new TransformedPoint3(0, 0, 0);
	final DoubleAttr sharpness = new HardBoundedDoubleAttr(0, 10, 0);
	
	public final Point3f projectedPos = new Point3f();
	
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
	
	public void getPos(Tuple3d p) {
		position.getTuple(p);
	}
	
	public void getPos(Tuple3f p) {
		position.getTuple(p);
	}
	
	public void getProjectedPos(Tuple3f p) {
		p.set(projectedPos);
	}
}
