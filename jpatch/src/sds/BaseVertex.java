package sds;

public abstract class BaseVertex extends AbstractVertex {
	int corner, crease;
	HalfEdge creaseEdge0, creaseEdge1;
	int creaseEdgeIndex0, creaseEdgeIndex1;
}
