package com.jpatch.entity.sds2;

import javax.vecmath.*;

public class BaseVertex extends AbstractVertex {
	
	public BaseVertex() {
		positionAttr.bindTuple(position);
	}
	
	public BaseVertex(double x, double y, double z) {
		this();
		positionAttr.setTuple(x, y, z);
	}
	
	@Override
	public void getLimit(Tuple3f limit) {
		vertexPoint.getLimit(limit);
	}
	
	@Override
	public void getLimit(Tuple3d limit) {
		vertexPoint.getLimit(limit);
	}
	
	@Override
	public void getNormal(Tuple3f normal) {
		vertexPoint.getNormal(normal);
	}
	
	@Override
	public void getNormal(Tuple3d normal) {
		vertexPoint.getNormal(normal);
	}
	
	@Override
	public void validateAlteredLimit() {
		vertexPoint.validateAlteredLimit();
	}
}
