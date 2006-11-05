package sds;

import javax.vecmath.*;

public class Slate {
	final Point3d[][] fans = new Point3d[4][];
	
	Slate(Point3d[][] fans) {
		this.fans[0] = fans[0].clone();
		this.fans[1] = fans[1].clone();
		this.fans[2] = fans[2].clone();
		this.fans[3] = fans[3].clone();
	}
}
