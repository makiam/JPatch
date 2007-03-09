package sds;

import javax.vecmath.*;

public abstract class BaseVertex extends AbstractVertex {
	int corner, crease;
	HalfEdge creaseEdge0, creaseEdge1;
	public Point3d limit = new Point3d();
//	int creaseEdgeIndex0, creaseEdgeIndex1;
}
