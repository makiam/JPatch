package sds;

import jpatch.entity.*;

public abstract class Level2Vertex extends AbstractVertex {
	public final Attribute.Boolean overridePosition = new Attribute.Boolean(false);
	public final Attribute.Boolean overrideSharpness = new Attribute.Boolean(false);
	public abstract void computeDerivedPosition();
}
