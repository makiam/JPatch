/*
 * Created on Feb 26, 2005
 */
package inyo;

import java.util.ArrayList;
import javax.vecmath.*;

/**
 * @author david
 *
 * Implements subsurface scattering sampling. 
 */
public class RtScattering {

	ArrayList samples = new ArrayList();
	
	RtScattering() {}
	
	/**
	 * Add a triangle to the samples list
	 * @param triangles ArrayList holding the triangles
	 */
	final void addTriangle( RtTriangle triangle ) {
		// sort through the arraylist, and collect the materials
		// that have no specularity.
		
		// no specularity?
		if (triangle.material.specular == 0) {
			// create a sample
			RtScatteringSample sample = new RtScatteringSample( triangle );
			
			// add to the sample list
			this.samples.add( (Object)sample );
		}
	}
	
	/**
	 * Return the illumination for the point sampled by the view ray
	 * @param view
	 * @return
	 */
	final Vector3d multipleScattering( RtWorld world, RtPathNode view, RtMaterial material ) {
		
		// determine maximum distance
		double maxDistance = material.scatteringMaxRedDistance;
		maxDistance = Math.max( maxDistance, material.scatteringMaxGreenDistance);
		maxDistance = Math.max( maxDistance, material.scatteringMaxBlueDistance);
		
		// are the color distances the same?
		boolean equalColorDistances = false;
		if (material.scatteringMaxRedDistance == material.scatteringMaxGreenDistance &&
			material.scatteringMaxRedDistance == material.scatteringMaxBlueDistance) {
			equalColorDistances = true;
		}
				
		// amount to scale each contribution
		double scale = material.scatteringMultipleScale;

		// use the distance squared to speed up calculations
		double maxDistanceSquared = maxDistance*maxDistance;
		
		// get the hit point
		Point3d hitPoint = view.hitPoint;
		
		// this accumulates the results of the samples
		Vector3d accum = new Vector3d();
		
		// iterate through all the samples
		for (int i = 0; i < this.samples.size(); i++) {
			// get a sample point
			RtScatteringSample sample = (RtScatteringSample)samples.get(i);
						
			// get the squared distance to the sample point
			double distance = hitPoint.distanceSquared( sample.position );

			// close enough?
			if (distance < maxDistanceSquared && distance > 0.1) {

				// get the illumination of this sample
				Vector3d illumination = sample.getIllumination(world); 
				
				// get the real distance
				double realDistance = Math.sqrt(distance);
				
				// FIXME! add attenuation of normals
				double scaleDist = scale / realDistance;
				
				
				// no color scattering?
				if (equalColorDistances) {
					// all colors in range
					accum.x += illumination.x * scaleDist;
					accum.y += illumination.y * scaleDist;
					accum.z += illumination.z * scaleDist;
					
				} else {
					// check red component
					if (distance < material.scatteringMaxRedDistance ) {
						accum.x += illumination.x * scaleDist;
					}
	
					// check green component
					if (distance < material.scatteringMaxGreenDistance ) {
						accum.y += illumination.y * scaleDist;
					}
	
					// check blue component
					if (distance < material.scatteringMaxBlueDistance ) {
						accum.z += illumination.z * scaleDist;
					}
				}
				
			}
					
		}
								
		// return total contributed illumination
		return accum;
	}
	
	/** 
	 * Return the single scattering contribution for a point. This simulates
	 * the effect of light behind a diffuse object (such as wax). If this were
	 * done properly, I'd be doing ray marching. Instead, I opt for a much cheaper
	 * estimate. If the light is behind the hit triangle, a lightray is sent out
	 * in the direction of the light. This <i>should</i> collide with the other side
	 * of the object. The light estimate for the illumination at that point is then
	 * taken, and scaled by the distance between the original point and the outside
	 * point.
	 * @param world
	 * @param view
	 * @return
	 */
	final Vector3d singleScattering( RtWorld world, RtPathNode view, RtMaterial material ) {
		
		// get the scaling factor
		double scale = material.scatteringSingleScale;
		
		// get the maximum distance
		double maxDistance = material.scatteringSingleMaxDistance;
		
		// get the bias offset
		double bias = material.scatteringSingleBias;
		
		// holds accumulated light
		Vector3d accum = new Vector3d();

		// get a ray pointing in the same direction as the view
		RtPathNode insideRay = world.getPathNode();
		insideRay.origin = view.hitPoint;			
		insideRay.ignoreTriangle = view.hitTriangle;				
		insideRay.direction = new Vector3d( view.direction );
						
		// test to see what it hits
		insideRay.hitTest(world);
		if (insideRay.hit) {

			// get the distance through the volume
			double distance = view.hitPoint.distance( insideRay.hitPoint );
			if (distance < maxDistance) {

				// calculate new hit position, adding bias. This moves the hit point
				// slightly outside the object, in case there is something near the surface
				// that is occluding it (like clothes).
				if (bias != 0) {
					insideRay.calcHitFromOrigin(insideRay.distance + bias);
				}

				
				// iterate through the lights
				for (int i = 0; i < world.lightList.size(); i++) {
					// get a light
					RtLight light = (RtLight)world.lightList.get(i);
			
					// get a ray pointing to the light
					RtPathNode lightRay = light.getShadowRay( world, insideRay.hitPoint, insideRay.hitTriangle, false );
				
					// see if it reaches the light
					lightRay.hitTest(world);
					if (!lightRay.hit) {
						
						// use smoothstep for a nice falloff over the distance
						double falloff = RtShader.smoothstep( 0, maxDistance, maxDistance-distance );
						
						// get the light intensity
						double intensity = light.calcIntensity( world, lightRay.hitPoint );
						
						// calculate falloff over distance
						double distScale = scale * intensity * falloff;
													
						// add light's contribution
						accum.x += distScale * light.color.x;
						accum.y += distScale * light.color.y;
						accum.z += distScale * light.color.z;
					}
					// release the light ray
					lightRay.inUse = false;
				}
			}	
		}

		// release the interior ray
		insideRay.inUse = false;			
	
		// return total contributed illumination
		return accum;
	}

	/**
	 * This is similar to the single scattering algorithm, but is independant of any actual
	 * lights. Instead, the <b>scale</b> value is used to calculate the lighting effect.
	 * @param world
	 * @param view
	 * @return
	 */
	final double fakeSingleScattering( RtWorld world, RtPathNode view, RtMaterial material ) {
		
		// get the scaling factor
		double scale = material.scatteringSingleScale;
		
		// get the maximum distance
		double maxDistance = material.scatteringSingleMaxDistance;
		
		// holds accumulated light
		double result = 0;

		// get a ray pointing in the same direction as the view
		RtPathNode insideRay = world.getPathNode();
		insideRay.origin = view.hitPoint;			
		insideRay.ignoreTriangle = view.hitTriangle;				
		insideRay.direction = new Vector3d( view.direction );
						
		// test to see what it hits
		insideRay.hitTest(world);
		if (insideRay.hit) {

			// get the distance through the volume
			double distance = view.hitPoint.distance( insideRay.hitPoint );
			if (distance < maxDistance) {
				
				// use smoothstep for a nice falloff over the distance
				double falloff = RtShader.smoothstep( 0, maxDistance, maxDistance-distance );
												
				// calculate falloff over distance
				result = scale * falloff;
			}	
		}

		// release the interior ray
		insideRay.inUse = false;			
	
		// return total contributed illumination
		return result;
	}
	
	
}
