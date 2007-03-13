package sds;

import javax.vecmath.*;

public abstract class BaseVertex extends AbstractVertex {
	int corner, crease;
	HalfEdge creaseEdge0, creaseEdge1;
}
