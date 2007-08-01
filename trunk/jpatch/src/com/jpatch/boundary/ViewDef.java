package com.jpatch.boundary;

import javax.vecmath.*;
import com.jpatch.afw.attributes.*;

public interface ViewDef {
	public BooleanAttr getShowControlMeshAttribute();
	public BooleanAttr getShowLimitSurfaceAttribute();
	public BooleanAttr getShowProjectedMeshAttribute();
	public StateMachine<ViewDirection> getViewDirectionAttribute();
	public Matrix4d getMatrix(Matrix4d matrix);
	public Matrix4d getInverseMatrix(Matrix4d matrix);
	public void computeMatrix();
	public Viewport getViewport();
	public Point3d transform(Point3d p);
	public Point3d invTransform(Point3d p);
}
