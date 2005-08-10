/*
 * Created on Feb 24, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package inyo;
import javax.vecmath.*;
	
/**
 * A vertex is a point in space with an associated normal.
 */
public class RtVertex {
	Point3d point;
	Vector3d normal;
	Point3d reference;

	RtVertex( Point3d point, Point3d reference, Vector3d normal ) {
		this.point = point;
		this.reference = reference;
		this.normal = normal;
	}
	
	RtVertex( double x, double y, double z, double rx, double ry, double rz, double nx, double ny, double nz ) {
		this.point = new Point3d( x, y, z );
		this.reference = new Point3d( rx, ry, rz );
		this.normal = new Vector3d( nx, ny, nz );
	}

}
