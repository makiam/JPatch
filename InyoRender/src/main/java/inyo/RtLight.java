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
 * 
 * @author David Cuny
 *
 * Light in a scene. Point lights (lights with no size, radiating in all directions)
 * and "soft" lights (lights with a given size, radiating in all directions) are 
 * handled by this class.
 */

package inyo;
import javax.vecmath.*;


class RtLight {

	double radius = 0.0; // how soft the shadows from the light is
	double power = 1.0; // strength of the light
	Color3f color = new Color3f( 1, 1, 1 ); // color of the light
	Point3d position = null; // location of the light
	boolean castsShadow = true; // false if doesn't cast shadow
	boolean hasSpecular = true; // false if doesn't light specuar component
	boolean hasDiffuse = true; // false if doesn't light diffuse component
	int falloffType = FALLOFF_NONE;	// type of falloff
	double falloffScale = 1;		// amount to scale falloff
	Vector3d direction = null; // direction the light is pointing
	double falloffAngle = 0; // angle past which no light escapes
	RtTriangle lastOccludedBy = null; // for optimizing the raytracer test
	boolean lensFlare = false;	// if true, will generate a lens flare

	public static final int FALLOFF_NONE  		= 0;	// no falloff over distance
	public static final int FALLOFF_LINEAR    	= 1;	// falloffScale/distance
	public static final int FALLOFF_QUADRATIC  	= 2;	// falloffScale/distance squared
	
	/**
	 * Create a new light at the given position. The larger the radius, the 
	 * "softer" (more blurred) the shadows will be.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param power
	 * @param radius
	 */
	public RtLight( double x, double y, double z, double power, double radius ) {
		this.position = new Point3d( x, y, z );
		this.power = power;
		this.radius = radius;
	}
    
	/**
	 * Create a point light with default settings at the given position.
	 * @param p
	*/
	public RtLight( Vector3d p ) {
		// point light with default settings
		this.position = new Point3d( p );        
	}

	
	/**
	 * Returns a random point on a sphere for the raytracer to attempt to hit.
	 * This is what gives soft shadows their soft edges. 
	 * @return
	 */
	final public Point3d sampleOnSurface() {
		// return random point on sphere of around light of given radius
                
		// two random angles
		double theta = Math.random() * Math.PI * 2;
		double phi = Math.random() * Math.PI * 2;
    
		// calculate point on unit sphere
		double x = Math.sin( theta ) * this.radius;
		double y = Math.sin( theta ) * this.radius;
		double z = Math.cos( phi ) * x * this.radius;
    
		// translate by light's position
		return new Point3d( this.position.x + x, this.position.y + y, this.position.z + z );
	}
        
	/**
	 * Test if the surface is illuminated by the light. If the angle between the
	 * surface normal and vector from the hit point to the light is greater than
	 * 90 degrees, the light lies behind the surface normal, and does not illuminate
	 * the surface
	 * @param surfaceNormal normal of the surface being tested
	 * @param hitPoint hit point on the surface
	 * @return true if the surface faces the light
	 */
	final boolean facesLight( Vector3d surfaceNormal, Point3d hitPoint ) {    	
				
		// create a vector from the hit point to the light
		Vector3d toLight = new Vector3d(hitPoint.x - this.position.x, 
		hitPoint.y - this.position.y, 
		hitPoint.z - this.position.z);
    	
		// calculate the dot product		
		double dotProduct = surfaceNormal.dot( toLight );
    	
		// angle is less than 90 degrees if dot product is positive
		return (dotProduct < 0.0);
	}
    
	/***
	 * Helper function to get a pathnode for casting shadow rays. Sets the
	 * origin and triangle to ignore, and points to the light
	 * @param origin
	 * @param ignore
	 * @return
	 */
	final RtPathNode getShadowRay( RtWorld world, Point3d origin, RtTriangle ignore, boolean jitter ) {
		
		// get a shadow ray
		RtPathNode shadowRay = world.getPathNode();
		
		// ignore this triangle
		shadowRay.ignoreTriangle = ignore;

		// set the origin
		shadowRay.origin.set( origin );
		
		// jitter light position?
		if ( jitter && world.useSoftShadows && this.radius > 0 ) {
			// pick a jittered point on the surface and point to it
			Point3d jitteredPos = sampleOnSurface();
			shadowRay.lookAt( jitteredPos );
		
			// set maximum distance to light distance
			shadowRay.maxDistance = origin.distance( jitteredPos );

			
		} else {
			// set origin and point at the light
			shadowRay.origin.set( origin );
			shadowRay.lookAt( this.position );
		
			// set maximum distance to light distance
			shadowRay.maxDistance = origin.distance( this.position );
		}
		
		return shadowRay;
	}
	
	
	/**
	 * Returns the intensity of light on a given point. This needs to be changed
	 * at some point in the future so lights of different colors can be used.
	 * 
	 * @param world
	 * @param shadowRay
	 * @return
	 */
	final double calcIntensity( RtWorld world, Point3d origin ) {

		// default to light's current power
		double total = this.power;		

		// need to calcuate falloff for the light?
		double falloff = 1;
		switch (this.falloffType) {
		case RtLight.FALLOFF_NONE:
			// don't scale light intensity
			break;
		case RtLight.FALLOFF_LINEAR:
			// scale by falloffScale / distance
			falloff = this.falloffScale / origin.distance( this.position );
			break;
		case RtLight.FALLOFF_QUADRATIC:
			// scale by falloffScale / distance squared
			falloff *= this.falloffScale / origin.distanceSquared( this.position );
			break;
		}		
		
		// scale by the power of the light
		return total * falloff;		
	}	

	
	final RtPathNode calcLightColor( RtWorld world, Point3d origin, RtTriangle ignoreTriangle ) {
				
		boolean isGround = false;
		if (ignoreTriangle.material.transmit == 0 ) {
			isGround = true;
		}
		
		// get a shadow ray
		RtPathNode shadowRay = this.getShadowRay( world, origin, ignoreTriangle, true );
		
		// early out on if no transparent shadows		
		shadowRay.stopAtFirstHit = world.useBlackShadows; 		
		shadowRay.hitTest( world );
			
		if (shadowRay.hit) {
			
			if (world.useBlackShadows) {
				// return black as color
				shadowRay.red = 0;
				shadowRay.green = 0;
				shadowRay.blue = 0;

			} else {
				// get the transparency amount of the hit triangle
				RtMaterial material = shadowRay.hitTriangle.material;			

				// transparent surface?
				if (material.filter > 0 || material.transmit > 0 ) {

					// calculate the transparency color recursively 
					RtPathNode transRay = this.calcLightColor( world, shadowRay.hitPoint, shadowRay.hitTriangle );
										
					// calculate amount to filter and transmit
					double transmit = material.transmit;
					double filter = material.filter;
					
					// conserve energy? (hacked out, this is slow...)
					if (material.conserveEnergy) {
						// FIXME: should probably interpolate the normal
						double variableReflection = 1 + transRay.direction.dot(shadowRay.hitTriangle.normal);
						double reflectionAmount = material.reflectionMin
								+ Math.pow(variableReflection, material.reflectionFalloff)
								* (material.reflectionMax - material.reflectionMin);

						// adjust
						transmit *= 1 - reflectionAmount;
						filter *= 1 - reflectionAmount;
					}
																				
					// calculate the color
					shadowRay.red += (material.red * transRay.red * filter)	+ (transRay.red * transmit);
					shadowRay.green += (material.green * transRay.green * filter) + (transRay.green * transmit);
					shadowRay.blue += (material.blue * transRay.blue * filter) + (transRay.blue * transmit);
				
					// free the ray
					transRay.inUse = false;				
				}
			}

		} else {
			// use the light color
			shadowRay.red = this.color.x;
			shadowRay.green = this.color.y;
			shadowRay.blue = this.color.z;			
		}		
		
		return shadowRay;
	}

	
}