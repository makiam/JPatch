/*
 * Created on Feb 26, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package inyo;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * This class represents a single sample of illumination on a
 * triangle. It assumes the triangle is sufficiently small. If
 * needed, it will calculate the illumination of the sample, and
 * cache the result. 
 * */
public class RtScatteringSample {
	private Vector3d illumination = null;
	Point3d position = null;
	RtTriangle triangle = null;
	
	RtScatteringSample() {}
	
	/**
	 * Create a sample, given a triangle. It calculates the center
	 * point of the triangle (the average of the vertices, and stores
	 * the triangle as the sample's parent.
	 * @param world
	 * @param triangle
	 */
	RtScatteringSample( RtTriangle triangle ) {
		// set the parent
		this.triangle = triangle;

		// center of triangle is average of points
		this.position = new Point3d(triangle.v1.point);
		this.position.add(triangle.v2.point);
		this.position.add(triangle.v3.point);
		this.position.scale(1f/3f);
	}
	
	/**
	 * Return the illumination for this sample. Sampling is deferred until
	 * the illumination is actually needed, so backfacing samples that never
	 * contribute don't calculate lighting.
	 * @return
	 */
	public final Vector3d getIllumination( RtWorld world )
	{
		// has illumination been calculated yet?
		if (this.illumination == null) {
			// get a ray to calculate the lighting
			RtPathNode lightRay = world.getPathNode();
			
			// hit triangle 
			lightRay.ignoreTriangle = triangle;
			lightRay.hitTriangle = triangle;
			
			// origin and hit point are the same
			lightRay.origin = this.position;
			lightRay.hitPoint = this.position;
			
			// direction is reverse of normal
			Vector3d view = this.triangle.normal;
			view.negate();
			lightRay.direction = view;
			
			// surface normal
			lightRay.normal = this.triangle.normal;			
			
			// run lighting test for this point
			lightRay.calcLights(world);
						
			// cache result
			this.illumination = new Vector3d( lightRay.red, lightRay.green, lightRay.blue );
			
			// calculate the area of the triangle
			double area = (triangle.v1_v2 * triangle.v1_v2) / 2;
			
			// divide the illumination by the area of the triangle
			this.illumination.scale(area);
			
			// free the ray
			lightRay.inUse = false;
		}
		return this.illumination;	
	}
	

}
