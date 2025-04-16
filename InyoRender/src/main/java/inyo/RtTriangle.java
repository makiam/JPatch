// Copyright (c) 2004 David Cuny
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and 
// associated documentation files (the "Software"), to deal in the Software without restriction, including 
// without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or 
// sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
// subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in all copies or substantial 
// portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT 
// NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
// IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
// SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

/** 
 * @author David Cuny
 *
 * The atomic chunk of geometry.
 */

package inyo;
import javax.vecmath.*;

class RtTriangle extends RtBoundingBox {
    
	RtVertex v1, v2, v3;			// the vertices defining the triangle
	Vector3d normal;				// normal of the triangle
	RtMaterial material;			// material attached to the triangle
	int lastVisitedBy = -1;			// last raytracer id visited by
	double v1_v2, v2_v3, v1_v3;		// lengths of edges
	private Point3d p3Ref = new Point3d();		// reference position
	
	/**
	 * Creates a triangle, given three vertexes.
	 * 
	 * @param world
	 * @param v1
	 * @param v2
	 * @param v3
	 */
	public RtTriangle( RtVertex v1, RtVertex v2, RtVertex v3 ) {
	
		// save the vertices
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
        
		// add to bounding box
		this.add( v1.point );
		this.add( v2.point );
		this.add( v3.point );
                
		// rays lying in a plain        
		Vector3d edge1 = new Vector3d( v2.point );
		edge1.sub( v1.point );
        
		Vector3d edge2 = new Vector3d( v3.point );
		edge2.sub( v1.point );
        
		// get normalized cross-product
		this.normal = new Vector3d();
		this.normal.cross( edge1, edge2 );
		this.normal.normalize();
				
		// need to switch direction?
		Vector3d v = new Vector3d(v1.point.x, v1.point.y, v1.point.z);
		if (this.normal.dot(v) >= 0) {
			this.normal.negate();
		}
				
		// calculate lengths between vertices, so calcAverageNormal() can reuse them		
		v1_v2 = v1.point.distance( v2.point );
		v2_v3 = v2.point.distance( v3.point );
		v1_v3 = v3.point.distance( v1.point );
    }
        
    
	/**
	* Interpolates the "average" normal for a point. This is done by
	* interpolating between the normals at the vertexes, allowing the
	* triangle to simulate a curved surface.
	* 
	* @param world
	* @param p
	* @return
	*/
	final Vector3d calcAverageNormal( RtWorld world, Point3d p, Vector3d direction, boolean inside ) {

		// calculate lengths
		double p_v1 = p.distance( this.v1.point );
		double p_v2 = p.distance( this.v2.point );
		double p_v3 = p.distance( this.v3.point );

		// calculate area using heron's formula
		double s = (p_v2 + v2_v3 + p_v3) / 2;
		double area1 = Math.sqrt( s * (s-p_v2) * (s-v2_v3) * (s-p_v3) );

		s = (p_v3 + v1_v3 + p_v1) / 2;
		double area2 = Math.sqrt( s * (s-p_v3) * (s-v1_v3) * (s-p_v1) );
        
		s = (p_v1 + v1_v2 + p_v2) / 2;
		double area3 = Math.sqrt( s * (s-p_v1) * (s-v1_v2) * (s-p_v2) );

		// ensure barycentric coordinates (must sum up to 1)
		double area = area1 + area2 + area3;
		area1 /= area;
		area2 /= area;
		area3 /= area;
		
		// get the vertex normals
		Vector3d n1 = this.v1.normal;
		Vector3d n2 = this.v2.normal;
		Vector3d n3 = this.v3.normal;
        
		// create a point to hold the results
		Vector3d averageNormal = new Vector3d();
        
		// add the contributions to make the normal
		averageNormal.x = (area1 * n1.x ) + (area2 * n2.x ) + (area3 * n3.x );
		averageNormal.y = (area1 * n1.y ) + (area2 * n2.y ) + (area3 * n3.y );
		averageNormal.z = (area1 * n1.z ) + (area2 * n2.z ) + (area3 * n3.z );
	
		// SL: get the reference points
		Point3d r1 = v1.reference;
		Point3d r2 = v2.reference;
		Point3d r3 = v3.reference;
		
		p3Ref.set(
			r1.x * area1 + r2.x * area2 + r3.x * area3,
			r1.y * area1 + r2.y * area2 + r3.y * area3,
			r1.z * area1 + r2.z * area2 + r3.z * area3
		);
		
		// normalize the normal
		averageNormal.normalize();

		// ensure the normal goes in the opposite direction of ray
		if (direction != null) {
			double dotProduct = averageNormal.dot( direction );
			if ((dotProduct >= 0.0 && !inside) ||							
			   (dotProduct < 0.0 && inside)) {
				// reverse the direction of the calculated normal
				averageNormal.negate();
			}
		}

		return averageNormal;
	}    

	/**
	* SL: return the referenceposition (which has been computed
	* by the calcAverageNormal() method)
	**/
	public Point3d getReferencePosition() {
		return p3Ref;
	}
	
	/**
	 * Test to see if a path node intersects the triangle.
	 * 
	 * @param world
	 * @param pathNode
	 */
	final void hitTest( RtPathNode pathNode ) {
		
		// ignore this?
		if (this == pathNode.ignoreTriangle) {
			return;
		}
		
		// already visited this triangle?
		if (this.lastVisitedBy == pathNode.rayId ) {
			// return;
		}
		
		// missed bounding box?
		if (!(this.hitsBox( pathNode ))) {
			return;
		}
        
		// get the points
		Point3d p1 = this.v1.point;
		Point3d p2 = this.v2.point;
		Point3d p3 = this.v3.point;

		// get the viewpoint
		Point3d origin = pathNode.origin;
		Vector3d direction = pathNode.direction;
        
		boolean intersected = false;
        
		double a = p1.x - p2.x;
		double b = p1.x - p3.x;
		double c = direction.x;
		double d = p1.x - origin.x;
        
		double e = p1.y - p2.y;
		double f = p1.y - p3.y;
		double g = direction.y;
		double h = p1.y - origin.y;
        
		double i = p1.z - p2.z;
		double j = p1.z - p3.z;
		double k = direction.z;
		double l = p1.z - origin.z;
        
		double m = f * k - g * j;
		double n = h * k - g * l;
		double p = f * l - h * j;
        
		double q = g * i - e * k;
		double r = e * l - h * i;
		double s = e * j - f * i;
        
		double e1 = d * m - b * n - c * p;
		double e2 = a * n + d * q + c * r;
		double D = a * m + b * q + c * s;
        
		double beta = e1 / D;
		double gamma = e2 / D;
		double t = 0.0;
        
		if ( beta + gamma <= 1.0 && beta >= 0.0 && gamma >= 0.0 ) {
			double e3 = a * p - b * r + d * s;
			t = e3 / D;
            
			// true if larger than error amount
			intersected = (t >= Double.MIN_VALUE);
            
		} else {
			intersected = false;
		}
        
		// hit?
		if (intersected) {
			// maximum distance test?
			if (pathNode.maxDistance == 0 || t < pathNode.maxDistance) {
				// first hit or closest so far?
				if ( !pathNode.hit || t < pathNode.distance ) {
					// mark as a hit
					pathNode.hit = true;
					
					// save triangle and distance
					pathNode.hitTriangle = this;                
					pathNode.distance = t;
				}
			}
		}			
	}
}