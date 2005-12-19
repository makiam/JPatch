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
 * This describes a ray from a given viewpoint pointing a particular direction.
 */

package inyo;
import javax.vecmath.*;

class RtPathNode {

	// the eyepoint
	public Point3d origin = new Point3d(0, 0, 1);
	public Vector3d direction = new Vector3d();

	// if true, pathNode is in use
	public boolean inUse = false;

	// the hit object attributes
	public boolean hit = false;

	// triangle that was hit, if any
	public RtTriangle hitTriangle;
		
	// point on triangle that was hit
	public Point3d hitPoint = new Point3d();

	public Point3d hitPointWorld = new Point3d();	// SL: hit point in world coordinates
	
	// normal of the hit triangle
	public Vector3d normal = new Vector3d();

	// distance to the hit triangle
	public double distance = 0.0;

	// color of the hit triangle
	public double red, green, blue;
	
	// stops raytracer at first hit, for shadow test
	public boolean stopAtFirstHit = false;
		
	// maximum distance for allowable hit, for ambiant occlusion
	public double maxDistance = 0.0;

	// triangle to ignore in hit tests, prevents triangle from hitting itself
	RtTriangle ignoreTriangle = null;
	
	// count the number of shadows
	public int lightCount = 0;
	
	// material if reflects/refracts
	RtMaterial reflectedMaterial = null;
	
	// if true, the raytracer will oversample this pixel
	boolean needsOversampling = false;
	
	// the material with the visible properties
	public RtMaterial visibleMaterial = null;
	
	// identifier to prevent multiple visits to same triangles
	public int rayId = 0;
	
	// flag if inside of a refractive object
	public boolean inside = false;

	/**
	 * Sets the direction the ray is aiming.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	final void lookAt(double x, double y, double z) {
		// point to the requested object
		this.direction.sub(origin);
		this.direction.x = x - this.origin.x;
		this.direction.y = y - this.origin.y;
		this.direction.z = z - this.origin.z;

		// normalize to create a unit vector
		this.direction.normalize();
	}

	/**
	 * Sets the direction the ray is aiming.
	 * 
	 * @param p
	 */
	final void lookAt(Point3d p) {
		this.direction.x = p.x - this.origin.x;
		this.direction.y = p.y - this.origin.y;
		this.direction.z = p.z - this.origin.z;

		// normalize to create a unit vector
		this.direction.normalize();
	}

	/**
	 * Calculate hit point, given <i>distance</i> from the origin.
	 * @param distance
	 */
	final void calcHitFromOrigin( double distance ) {
		// calculate hit position			
		this.hitPoint.x = this.origin.x + (this.direction.x * distance);
		this.hitPoint.y = this.origin.y + (this.direction.y * distance);
		this.hitPoint.z = this.origin.z + (this.direction.z * distance);
	}
	
	/**
	 * Tests to see what objects in the <b>world </b> the ray intersects. <b>hit
	 * </b> is set to <b>true </b> if an intersecting object is found, and
	 * <b>hitPoint </b>, <b>hitTriangle </b> and <b>distance</b> contain
	 * information about the object that was hit.
	 * 
	 * If the triangle has a texture, that hit point is transformed into word space so
	 * the texture on the triangle can be calculated.
	 * 
	 * @param world
	 */
	final void hitTest(RtWorld world) {

		// clear hit flag
		this.hit = false;

		// which data structure do we look in?
		if (world.subdivisionMethod == RtWorld.OCTREE) {
			// visit each object in the model list
			for ( int i = 0; i < world.modelList.size(); i++ ) {		        
	            // get the next model
	            RtModel model = (RtModel)world.modelList.get(i);            
		            
	            // test
				model.hitTest( this );

				// stop?
				if (this.hit && this.stopAtFirstHit) {
					break;				
				}
			}
			
		} else {
			// use kd tree
			world.kdTree.hitTest( world, this );			
		}
			
		// calculate the position of the hit point?
		if (this.hit) {
			// calculate position in world space
			hitPointWorld.set(hitPoint.x, hitPoint.y, hitPoint.z);
			world.m4Cam2World.transform(hitPointWorld);
			
			// calculate hit position
			this.calcHitFromOrigin(this.distance);
		}
	}


	/**
	 * Calculates the light contribution from the sky, using ambiant occlusion.
	 * Ambiant occlusion is similar to path tracing, but makes a number of
	 * simplifying assumptions. Rays are sent out in random directions, and
	 * those intersecting nearby objects are considered to be shadow rays.
	 * Otherwise, the rays are considered to be light rays. Note that these rays
	 * (unlike path tracing) do not bounce.
	 * 
	 * @param world
	 * @return
	 */
	final Color3f ambientOcclusion(RtWorld world) {

		// return the light contribution from the sky

		// accumulates the color
		float red = 0;
		float green = 0;
		float blue = 0;
		
		// color bleeding?
		double bleed = world.colorBleed;
		
		// create a pathNode for casting ambient occlusion rays
		RtPathNode shadowRay = world.getPathNode();

		// set up the ray
		shadowRay.origin = this.hitPoint;
		shadowRay.ignoreTriangle = this.hitTriangle;
		shadowRay.stopAtFirstHit = true;
		shadowRay.maxDistance = world.ambientOcclusionDistance;

		// get the triangle that's being looked at
		RtTriangle hitTriangle = this.hitTriangle;

		// get the normal for the triangle
		this.normal = hitTriangle.calcAverageNormal(world, this.hitPoint, this.direction, this.inside );
		
		// calculate the amount of contribution from the ambient occlusion
		int skyCount = 0;
		int sampleCount = 0;
		double contrib = 0;		

		// collect samples
		for (int i = 0; i < world.ambientOcclusionSamples; i++) {

			// set the shadow ray to a random point on the normal's hemisphere
			// FIXME: should this use cosine sampling?
			shadowRay.direction = sampleRandomly(normal);
			// shadowRay.direction = sampleUsingCosine(normal);

			// set it as the target of the shadow ray
			shadowRay.hit = false;
			shadowRay.blue = world.nextRayId++;

			// test the ray against the world
			shadowRay.hitTest(world);			

			// missed any objects?
			if (shadowRay.hit) {
				// color bleeding?
				if (bleed > 0) {
					// get the hit material
					RtMaterial m = shadowRay.hitTriangle.material;
				
					// get the color of the hit material
					red += (float)m.red * bleed;
					green += (float)m.green * bleed;
					blue += (float)m.blue * bleed;
				}
				
			} else {
				// count as reaching the sky
				skyCount++;
				
				// add sky color 
				red += world.skyLightColor.x;
				green += world.skyLightColor.y;
				blue += world.skyLightColor.z;

			}

			// calculate contribution
			sampleCount++;
		}

		// calculate contribution
		contrib = (double) skyCount / (double) sampleCount;
		
		// return the shadow ray to the pool
		shadowRay.inUse = false;

		// scale the colors by the sample count
		red /= sampleCount;
		green /= sampleCount;
		blue /= sampleCount;
				
		// return the color
		return new Color3f( red, green, blue );
	}

	/**
	 * Calculate the direct illumination of lights on the hit point.
	 * 
	 * @param world
	 */
	final void calcLights(RtWorld world) {
		
		// no lights?
		if (world.lightList.size() == 0) {
			return;
		}

		// material of hit triangle
		RtMaterial material = this.hitTriangle.material;
		
		// holds material rgb values 
		double materialRed, materialGreen, materialBlue;
		
		// set up float values to store the reference position
		//hitTriangle.calcAverageNormal(world, this.hitPoint, this.direction);
		Point3d pRef = hitTriangle.getReferencePosition();
		float refX = (float) pRef.x;
		float refY = (float) pRef.y;
		float refZ = (float) pRef.z;

		// is there a texture, and does it have a pigment?
		if (material.texture != null && material.texture.getPigment() != null) {
			// get the color in world space
			Color3f c = material.texture.getPigment().colorAt(refX, refY, refZ);
				
			// set the color
			materialRed = c.x;
			materialGreen = c.y;
			materialBlue = c.z;
			
		} else {
			// use the default material colors
			materialRed = material.red;
			materialGreen = material.green;
			materialBlue = material.blue;
		}
		
		
		double Ks = material.specular; // specular component (default = 0.1)
		double Kd = material.diffuse; // diffuse component (default = 0.5)
		double n = material.brilliance; // specular highlight (default = 2)

		// sums up color
		float sumRed;
		float sumGreen;
		float sumBlue;
				
		
		// iterate through the lights
		for (int i = 0; i < world.lightList.size(); i++) {

			// get a light
			RtLight light = (RtLight)world.lightList.get(i);

			// how many samples? 
			// FIXME: solve the softShadowSamples vs. jitter
			int samples = 1;
			if (world.useSoftShadows && light.radius > 0 && world.jitter == 1) {
				samples = world.softShadowSamples;
			}
						
			// specular and diffuse are computed independantly			
			double specular = 0;
			double diffuse = 0;
			
			// clear accumulated color
			sumRed = 0;
			sumGreen = 0;
			sumBlue = 0;
					
			// does the surface face the light?
			double intensity = 0;
			// FIXME: this will have issues with wrapped lights
			if (light.facesLight( this.normal, this.hitPoint)) {
				// calculate illumination from this light
				intensity = light.calcIntensity( world, this.hitPoint );				
			}
			
			// clear the light hit counter
			int lightHitCount = 0;
			
			// any light to illuminate?
			if (intensity > 0 && light.castsShadow) {				
				
				
				
				// run the sampling loop
				for (int sample = 0; sample < samples; sample++ ) {
					// calculate light color
					RtPathNode colorRay = light.calcLightColor( world, this.hitPoint, this.hitTriangle );
										
					// hit something?
					if (colorRay.hit) {
						// transparent and rendering caustics? 
						if ( !world.useBlackShadows &&
							world.useFakeCaustics &&
							(colorRay.hitTriangle.material.filter > 0 || colorRay.hitTriangle.material.transmit > 0) &&
							this.hitTriangle.material.filter == 0 ) {
							
							// treat as reaching light
							lightHitCount++;

							// need to oversample caustic?
							this.needsOversampling = world.useOversampledCaustics;

							// fake caustics
							Vector3d hitNormal = this.hitTriangle.calcAverageNormal( world, colorRay.hitPoint, null, this.inside );
							double scaleCaustic = this.normal.dot( colorRay.hitTriangle.normal );
							colorRay.red *= scaleCaustic;
							colorRay.green *= scaleCaustic;
							colorRay.blue *= scaleCaustic;
						}
						
					} else {
						// reached the light
						lightHitCount++;						
					}
					
					// add the color
					sumRed += colorRay.red;
					sumGreen += colorRay.green;
					sumBlue += colorRay.blue;

					// free the ray
					colorRay.inUse = false;
											
				}
							
				// count as being in shadow?
				// FIXME: should this test for > 0 instead?
				if (lightHitCount == samples) {
					// not shadowed
					this.lightCount++;
				}											
			}

			// non-shadow casting light is a special case
			if (!light.castsShadow) {
				// don't divide by number of samples
				samples = 1;
				
				// get light color
				sumRed = light.color.x;
				sumGreen = light.color.y;
				sumBlue = light.color.z;
			}
			
			
			// average samples?
			if (samples > 1) {
				sumRed /= samples;
				sumGreen /= samples;
				sumBlue /= samples;
			}
			
			// any illumination?
			if (intensity > 0) {

				// get a light ray for the light
				RtPathNode shadowRay = light.getShadowRay( world, this.hitPoint, this.hitTriangle, false );
						
				// calculate diffuse contribution
				diffuse = material.getDiffuse(this.normal, shadowRay.direction, this.direction );				
				
//				 does the light have a specular component?
				if (light.hasSpecular && lightHitCount > 0) {
				   // calculate specular contribution based on the material's specular model
				   specular = material.getSpecular(this.normal, shadowRay.direction, this.direction );
				   // soft shadows?
				   if (lightHitCount != samples) {      
				      // scale by the ratio of samples that illuminate the surface
				      specular *= (float)lightHitCount / (float)samples;
				   }
				} else {
				   // no specular highlight
				   specular = 0;
				}
				
				// release the shadow ray
				shadowRay.inUse = false;
			}

			double specularRed = 0;
			double specularGreen = 0;
			double specularBlue = 0;

			// specular element?
			if (specular > 0) {
				// for interpolating metallic
				double alpha = (float)material.metallic;
				double beta = 1 - alpha;
			
				// interpolate the specular value
				specularRed = specular * (beta + alpha * materialRed);
				specularGreen = specular * (beta + alpha * materialGreen);
				specularBlue = specular * (beta + alpha * materialBlue);
			}
						
			// the contribution from this light 
			this.red += (materialRed * sumRed * diffuse + specularRed) * intensity;
			this.green += (materialGreen * sumGreen * diffuse + specularGreen) * intensity;
			this.blue += (materialBlue * sumBlue * diffuse + specularBlue) * intensity;						
		}

	}

	/**
	 * Set the sky color for a ray that reached the sky.
	 * @param world
	 * @param diffuseHits
	 */
	void calcSkyColor( RtWorld world, int diffuseHits ) {
		// if first diffuse hit, return the direct color of the sky
		if (diffuseHits == 0) {
			// do we have a skyTexture defined?
			if (world.skyTexture != null) {
				// where did we hit the (normalized) sky-sphere?
				Vector3d skyHit = new Vector3d(direction);
				// FIXME: this should be precomputed and stored
				world.m4Cam2World.transform(skyHit);
				
				// texture matrix...
				skyHit.normalize();
				Color3f skyColor = world.skyTexture.getPigment().colorAt((float) skyHit.x, (float) skyHit.y, (float) skyHit.z);
				
				// set the color
				this.red = skyColor.x;
				this.green = skyColor.y;
				this.blue = skyColor.z;
				
			} else {
				// use default sky color
				this.red = world.skyColor.x;
				this.green = world.skyColor.y;
				this.blue = world.skyColor.z;
			}
		
		} else {
			// hit sky after diffuse bounce, so return indirect sky light color
			
			// do we have a skyTexture defined?
			if (world.skyTexture != null) {
				// where did we hit the (normalized) sky-sphere?
				Vector3d skyHit = new Vector3d(direction);
				
				// FIXME: this should be precomputed and stored
				world.m4Cam2World.transform(skyHit);
				
				// texture matrix...
				skyHit.normalize();
				Color3f skyColor = world.skyTexture.getPigment().colorAt((float) skyHit.x, (float) skyHit.y, (float) skyHit.z);
									
				// set the color of the sky, scaled by the sky's power
				this.red = skyColor.x * world.skyPower;
				this.green = skyColor.y * world.skyPower;
				this.blue = skyColor.z * world.skyPower;
									
			} else {
				// use sky light color
				this.red = world.skyLightColor.x * world.skyPower;
				this.green = world.skyLightColor.y * world.skyPower;
				this.blue = world.skyLightColor.z * world.skyPower;
			}
		}
		return;
	}

	/**
	 * Calculate the color for the the intersected point. The variable
	 * <b>diffuseHits </b> counts the number of times the ray has intersected
	 * with diffuse surfaces. This is used by to determine if a full sampling
	 * should be done when a diffuse surface is hit. (For the sake of speed,
	 * it's only done for the first diffuse surface).
	 * 
	 * This handles diffuse, specular, reflection, refraction...
	 * 
	 * @param world
	 * @param depth
	 * @param diffuseHits
	 */
	final void calcColor(RtWorld world, int depth, int diffuseHits ) {

		// past depth?
		if (depth > world.maxDepth) {
			// fixme: should this set the color?
			return;
		}
		
		// clear the shadow count flag
		this.lightCount = 0;
		
		// clear the indirect material
		this.visibleMaterial = null;
		this.reflectedMaterial = null;
		
		// FIXME: this should be moved into a seperate routine
		// nothing hit?
		if (!this.hit) {
			calcSkyColor( world, diffuseHits );
			return;
		}

		// save as visible material
		this.visibleMaterial = this.hitTriangle.material;
		
		// diffuse and ambient terms
		double Kd = this.hitTriangle.material.diffuse;
		double Ka = this.hitTriangle.material.ambient;

		// calculate the average normal
		this.normal = this.hitTriangle.calcAverageNormal(world, this.hitPoint, this.direction, this.inside );
		// FIXME: for debugging, use the normal
		// this.normal = new Vector3d( this.hitTriangle.normal );
		
		// clear color
		this.red = 0;
		this.green = 0;
		this.blue = 0;

		// get the material of the hit triangle
		RtMaterial material = this.hitTriangle.material;
						
		// get the reference position in model space
		Point3d reference = hitTriangle.getReferencePosition();
		
		// set the material color to the pigment color
		Color3f c = material.getColor( reference );

		double materialRed = c.x;
		double materialGreen = c.y;
		double materialBlue = c.z;
		
		// bump mapping the normal vector?
		material.peturb( this.normal, reference );
		
		// flag if item is transparent
		boolean isTransparent = (material.reflectionMax > 0 || material.reflectionMin > 0);

		// add ambient lighting
		if (material.diffuse > 0.0) {
			// path trace illumination?
			if (world.pathTracing != RtWorld.PATHTRACE_OFF) {
				// prevent transparent objects from being path traced
				if (!isTransparent) {
					// use path tracing to determine illumination
					this.pathTrace(world, depth+1, diffuseHits + 1 );
				}
			} else {
				// add ambient term
				this.red += materialRed * material.ambient;
				this.green += materialGreen * material.ambient;
				this.blue += materialBlue * material.ambient;
			}
		}

		// add the lighting
		if (!this.inside) {
			this.calcLights(world);
		}
		
		// multiple scattering?
		if (material.multipleScattering) {
			// get contribution from this material
			Vector3d scattering = material.samples.multipleScattering( world, this, material );
			
			// add contribution, attenuated by material color
			this.red += materialRed * scattering.x;
			this.green += materialGreen * scattering.y;
			this.blue += materialBlue * scattering.z;						
		}

		// single scattering?
		if (material.singleScattering) {
			// get contribution from this material
			Vector3d singleScattering = material.samples.singleScattering( world, this, material );
			
			// add contribution, attenuated by material color
			this.red += materialRed * singleScattering.x;
			this.green += materialGreen * singleScattering.y;
			this.blue += materialBlue * singleScattering.z;						
		}

		// fake single scattering?
		if (material.fakeSingleScattering) {
			// get contribution from this material
			double scattering = material.samples.fakeSingleScattering( world, this, material );
			
			// add contribution, attenuated by material color
			this.red += materialRed * scattering;
			this.green += materialGreen * scattering;
			this.blue += materialBlue * scattering;						
		}

		
		// run ambiant occlusion
		if (world.useAmbientOcclusion && material.diffuse > 0.0) {
			// calculate ambiant occlusion
			Color3f ao = this.ambientOcclusion(world);			
			
			// scale by the sky power
			ao.scale( (float)world.skyPower );
			
			// scale diffuse color
			this.red += ao.x * Kd * materialRed;
			this.green += ao.y * Kd * materialGreen;
			this.blue += ao.z * Kd * materialBlue;
		}

		// is it reflective?
		double reflectionAmount = 0;
		
		// calculate amount to filter and transmit
		double transmit, filter;
		if (material.conserveEnergy) {
			transmit = material.transmit * (1 - reflectionAmount);
			filter = material.filter * (1 - reflectionAmount);
		} else {
			transmit = material.transmit;
			filter = material.filter;
		}
		
		// refraction
		if ((material.transmit > 0.0 || material.filter > 0.0)
				&& depth < world.maxDepth) {
									
			// get a refraction ray from the pool
			RtPathNode refractionRay = world.getPathNode();

			// set up the values
			refractionRay.origin = this.hitPoint;
			refractionRay.ignoreTriangle = this.hitTriangle;
			refractionRay.normal = new Vector3d( this.normal );

			// swap flag, so entering rays exit, and exiting rays enter
			refractionRay.inside = !this.inside;
	
			// refraction
	        double cos = -this.direction.dot(this.normal);
	        double ior = 0;
	        // if (cos > 0.0)
	        if (refractionRay.inside) {
	            // out going in
	            ior = 1.0 / material.ior;
	        } else {
	            // in going out	            
	            ior = material.ior;
	            // flip direction
	            cos = -cos;
	            refractionRay.normal.negate();     	            
	        }
	        
	        // refracted ray	 
	        double nK = (ior * cos) - Math.sqrt(1.0 - (ior * ior * (1.0 - (cos * cos))));
	        refractionRay.direction = new Vector3d();
	        refractionRay.direction.x = (ior * this.direction.x) + (nK * refractionRay.normal.x);
	        refractionRay.direction.y = (ior * this.direction.y) + (nK * refractionRay.normal.y);
	        refractionRay.direction.z = (ior * this.direction.z) + (nK * refractionRay.normal.z);
	        
			// set hit test flags
			refractionRay.stopAtFirstHit = false;
			
			// shoot the ray			
			refractionRay.hitTest(world);
			// inside, but didn't hit same material?
			if (refractionRay.hit && 
				refractionRay.inside && 
				refractionRay.hitTriangle.material != this.hitTriangle.material) {
				// no longer inside
				refractionRay.inside = false;
			}
			// calculate color contribution
			refractionRay.calcColor(world, depth + 1, diffuseHits);				

			// inherit needsOversampling flag, if set
			this.needsOversampling |= refractionRay.needsOversampling;
			
			// track the refracted material
			if (refractionRay.reflectedMaterial != null) {
				// track the material visible
				this.reflectedMaterial = refractionRay.reflectedMaterial;
			} else {
				// track the refracting material
				this.reflectedMaterial = refractionRay.visibleMaterial;				
			}
			this.lightCount = refractionRay.lightCount;
			
			// add the color
			this.red += (material.red * refractionRay.red * filter)
					+ (refractionRay.red * transmit);
			this.green += (material.green * refractionRay.green * filter)
					+ (refractionRay.green * transmit);
			this.blue += (material.blue * refractionRay.blue * filter)
					+ (refractionRay.blue * transmit);

			// return the ray to the pool
			refractionRay.inUse = false;
		}

		
		// don't bother reflecting if inside an object
		if (isTransparent && depth < world.maxDepth && !this.inside) {
			double variableReflection = 1 + this.direction.dot(this.normal);
			reflectionAmount = material.reflectionMin
					+ Math.pow(variableReflection, material.reflectionFalloff)
					* (material.reflectionMax - material.reflectionMin);
			
			// get a reflection ray from the pool
			RtPathNode reflectionRay = world.getPathNode();

			// set up the values
			reflectionRay.origin = this.hitPoint;
			reflectionRay.ignoreTriangle = this.hitTriangle;
			reflectionRay.hit = false;

			// calculate the reflection angle: 2*(N.D)*N - D
			Vector3d D = new Vector3d(this.direction);			
			D.negate();
			reflectionRay.direction.set(this.normal);
			reflectionRay.direction.scale(2 * this.normal.dot(D));
			reflectionRay.direction.sub(D);

			// set hit test flags
			reflectionRay.stopAtFirstHit = false;			
			
			// shoot out the reflection ray
			reflectionRay.hitTest(world);			
			reflectionRay.calcColor(world, depth + 1, diffuseHits);

			// track the material
			if (reflectionRay.reflectedMaterial != null) {
				// track the reflected material
				this.reflectedMaterial = reflectionRay.reflectedMaterial;
			} else {
				// track the reflecting material
				this.reflectedMaterial = reflectionRay.visibleMaterial;
			}
			this.lightCount = reflectionRay.lightCount;

			// inherit needsOversampling flag, if set
			this.needsOversampling |= reflectionRay.needsOversampling;

			// scale by the reflection amount
			reflectionRay.red *= reflectionAmount;
			reflectionRay.green *= reflectionAmount;
			reflectionRay.blue *= reflectionAmount;
			
			// interpolate metallic
			double alpha = material.metallic;
			double beta = 1 - alpha;
			reflectionRay.red *= beta + alpha * materialRed; 
			reflectionRay.green *= beta + alpha * materialGreen;
			reflectionRay.blue *= beta + alpha * materialBlue;
			
			// add to output color
			this.red += reflectionRay.red;
			this.green += reflectionRay.green;
			this.blue += reflectionRay.blue;

			// return the ray to the pool
			reflectionRay.inUse = false;
		}		
		
	}

	/**
	 * Does a path trace to determine the illumination on a point. First, it
	 * attempts to get an estimate from the irradiace cache. If this fails, it
	 * checks if this is the first diffuse surface encountered. If it is, it
	 * performs a full sampling. Otherwise, it ony sends out a single ray to get
	 * an estimate.
	 * 
	 * @param world
	 * @param predicted
	 * @param depth
	 * @param diffuseHits
	 */
	final void pathTrace(RtWorld world, int depth, int diffuseHits) {

		// color estimate
		Color3f colorEstimate = null;

		// past maximum depth?
		if (diffuseHits > world.pathTracingMaxBounces) {
			// treat as shadow
			this.red = 0.0;
			this.green = 0.0;
			this.blue = 0.0;
			return;
		}

		// get the material of the hit triangle
		RtMaterial material = this.hitTriangle.material;
		
		// holds color contribution of material or pigment
		double materialRed, materialGreen, materialBlue;

		// material or pigment
		if (material.texture != null && material.texture.getPigment() != null) {
			// SL: get the color from reference space
			hitTriangle.calcAverageNormal(world, this.hitPoint, this.direction, this.inside);
			Point3d pRef = hitTriangle.getReferencePosition();
			Color3f c = material.texture.getPigment().colorAt(
				(float) pRef.x, 
				(float) pRef.y, 
				(float) pRef.z);
				
			// set it as the material color
			materialRed = c.x;
			materialGreen = c.y;
			materialBlue = c.z;
		} else {
			// use the material colors
			materialRed = material.red;
			materialGreen = material.green;
			materialBlue = material.blue;
		}


		// intensity of light based on the normal and the direction vector
				
		// first diffuse surface, and not during irradiance pass?
		if (world.useIrradianceCache && diffuseHits == 1) {
			double intensity = Math.abs(this.direction.dot(this.normal));
			
			colorEstimate = world.irradianceCache.estimateIrradiance(this.hitPoint, this.normal);
			if (colorEstimate != null) {
				// calcuate the color based on the interpolated irradiance
				this.red = material.diffuse * materialRed * colorEstimate.x * intensity;
				this.green = material.diffuse * materialGreen * colorEstimate.y * intensity;
				this.blue = material.diffuse * materialBlue * colorEstimate.z * intensity;

				return;
			}
		}

		// get a ray from the pool
		RtPathNode sampleRay = world.getPathNode();

		// set the values
		sampleRay.origin = this.hitPoint;
		sampleRay.ignoreTriangle = this.hitTriangle;
		sampleRay.normal = this.normal;
		sampleRay.hit = false;

		// only do hemisphere sampling on first hit
		if (diffuseHits == 1 && world.pathTracing == RtWorld.PATHTRACE_HEMISPHERE) {				
			// sample the irradiance at this hit point
			sampleRay.sampleIrradiance(world, depth, diffuseHits);

		} else {
			// sample randomly
			sampleRay.direction = sampleRandomly(normal);
		}
			
		// hit test
		sampleRay.hitTest(world);
		sampleRay.calcColor(world, depth+1, diffuseHits+1);			


		// calculate intensity
		double intensity = Math.abs(this.direction.dot(sampleRay.direction));			
		
		// add the contribution of the sample for the final color
		this.red += material.diffuse * materialRed * sampleRay.red * intensity;
		this.green += material.diffuse * materialGreen * sampleRay.green * intensity;
		this.blue += material.diffuse * materialBlue * sampleRay.blue * intensity;

		sampleRay.inUse = false;
	}

	/**
	 * Performs stratified sampling at hit point to get an estimate of the
	 * irradiance at that point.
	 * 
	 * @param world
	 * @param depth
	 * @param diffuseHits
	 */
	public void sampleIrradiance(RtWorld world, int depth, int diffuseHits) {
		// sample the irradiance at a hit point

		// holds the resulting color
		Color3f color = new Color3f();

		// clear mean harmonic distance
		double sumOfInverses = 0.0;
		int hitCount = 0;

		// multiple samples - phi has twice the range to cover as theta
		float phiSamples = world.pathTracingPhiSamples;
		float thetaSamples = world.pathTracingThetaSamples;

		// size of step
		double phiStep = 1.0 / phiSamples;
		double thetaStep = 1.0 / thetaSamples;

		// stratified sample over range of phi
		for (double phi0 = 0.0; phi0 < 1.0; phi0 += phiStep) {

			// stratified sample over range of theta
			for (double theta0 = 0.0; theta0 < 1.0; theta0 += thetaStep) {

				// jitter theta and phi to get a sample
				double phi = (phi0 + (Math.random() * phiStep)) * Math.PI * 2.0;
				double theta = (theta0 + (Math.random() * thetaStep)) * Math.PI;

				// what type of sampling?
				if (world.pathTracing == RtWorld.PATHTRACE_HEMISPHERE){
					// sample over the hemisphere
					this.direction = sampleHemisphere(this.normal, theta, phi);
				} else {
					// sample randomly
					this.direction = sampleRandomly(this.normal);
				}				

				// shoot out the sample ray
				this.hit = false;
				this.rayId = world.nextRayId++;
				
				this.hitTest(world);
				this.calcColor(world, depth + 1, diffuseHits+1);				

				// add the contribution of the sample for the final color
				color.x += this.red;
				color.y += this.green;
				color.y += this.blue;

				// accumulate distance?
				if (this.hit) {
					// accumulate inverted distance
					sumOfInverses += 1.0 / this.distance;
					hitCount++;
				}
			}
		}

		// average the colors
		float samples = (float)(phiSamples * thetaSamples);
		color.scale(1 / samples);
		
		// store the sample into the irradiance cache?
		if (world.useIrradianceCache) {
			// calcuate inverse of mean harmonic distance
			double invR0 = 0.0;
			if (hitCount > 0) {
				invR0 = (double) hitCount / sumOfInverses;
			}
			world.irradianceCache.add(this.origin, this.normal, invR0, color);

			// set debugging flag
			world.irradianceSampled = true;
		}
		
		// set as the result
		this.red = color.x;
		this.green = color.y;
		this.blue = color.z;

	}
	
	
	final void rotateToNormal( Vector3d n, Vector3d s ) {

		// create a basis matrix that can be used to transform 
		// vector (0,0,1) into normal n, and then apply it to sample s.  
		// create orthonormal vectors, and load into matrix
		// Thanks to David Rigel for the code
		
		double ux, uy, uz;
		
		// precalculate squared values for u
		double x2 = n.x * n.x;
		double y2 = n.y * n.y;
		double z2 = n.z * n.z;
				
		// use the maximum for the best precision
		if (Math.abs(n.x) > Math.abs(n.y)
		&& Math.abs(n.x) > Math.abs(n.z) ) {
			// x axis is largest value
			uz = Math.sqrt(1.0/(1.0+z2/x2) );
			ux = -n.z * uz/n.x;
			uy = 0.0;
		} else if (Math.abs(n.y) > Math.abs(n.x)
				&& Math.abs(n.y) > Math.abs(n.z) ) {
			// y axis is largest
			ux = Math.sqrt(1.0/(1.0+x2/y2) );
			uy = -n.x * ux/n.y;
			uz = 0.0;
		} else {
			// z axis is largest
			uy = Math.sqrt(1.0/(1.0+y2/z2) );
			uz = -n.y * uy/n.z;
			ux = 0;
		}
		
		// create vector u
		Vector3d u = new Vector3d(ux, uy, uz );
						
		// create normalized orthagonal vector
		Vector3d v = new Vector3d();
		v.cross(n, u);
		v.normalize();
				
		// transform to canonical base
		Matrix4d m = new Matrix4d();
		m.setColumn(0, u.x, u.y, u.z, 0);
		m.setColumn(1, v.x, v.y, v.z, 0);
		m.setColumn(2, n.x, n.y, n.z, 0);
		m.setColumn(3, 0, 0, 0, 1);
		
		// apply the matrix to s
		m.transform( s );
	
	}
	

	final void rotateToNormal2( Vector3d n, Vector3d s ) {
		
		Vector3d t = new Vector3d( n.x, 0.1923, 0.8376 );
		t.normalize();
		
		Vector3d u = new Vector3d();
		u.cross(t,n);
		u.normalize();
		
		Vector3d v = new Vector3d();
		v.cross(n, u);
		v.normalize();
		
		// transform to canonical base
		Matrix3d m = new Matrix3d();
		m.setRow(0, u );
		m.setRow(1, v );
		m.setRow(2, n );		
		
		// apply the matrix to s
		m.transform( s );
				
	}

	
    /**
     * Return a vector that points somewhere in the unit hemisphere of the source vector
     * @return
     */
    final Vector3d sampleRandomly( Vector3d v) {
        
		Vector3d sample;

		// calcluate
		double theta = Math.PI * 2 * Math.random();
		double phi = Math.random();
		double sqrtPhi = Math.sqrt( phi );		
		double x = Math.cos( theta ) * sqrtPhi;
		double y = Math.sin( theta ) * sqrtPhi;
		double z = Math.sqrt( 1-phi );
	
		// create a new ray
		sample = new Vector3d( x, y, z );

		// rotate to normal's space
		rotateToNormal( v, sample );
    			
        // return sample;
		return sample ;		
    }

	
    /**
     * Return a vector that points somewhere in the unit hemisphere of the source vector
     * @return
     */
    final Vector3d sampleRandomly_( Vector3d v) {
        
		Vector3d sample;

		// two random angles
		double theta = Math.random() * (Math.PI * 2);
		double phi = Math.random() * Math.PI;

		// calculate direction vector    		
		double x = Math.sin(phi) * Math.cos(theta);
		double y = Math.sin(phi) * Math.sin(theta);
		double z = Math.cos(phi);		
	
		// create a new ray
		sample = new Vector3d( x, y, z );

		// rotate to normal's space
		rotateToNormal( v, sample );
    			
        // return sample;
		return sample ;
    }

	
    /**
     * Return a vector that points somewhere in the unit hemisphere of the source vector
     * @return
     */
    final Vector3d sampleRandomly__( Vector3d v) {
        
		Vector3d sample;

		// two random angles
		double theta = Math.random() * Math.PI * 2;
		double phi = Math.random() * Math.PI;

		// calculate direction vector    		
		double x = Math.cos(phi) * Math.sin(theta);
		double y = Math.sin(phi) * Math.sin(theta);
		double z = Math.cos(theta);
	
		// create a new ray
		sample = new Vector3d( x, y, z );

		// add the vector, and normalize
		sample.add( v );
		sample.normalize();
    
		// dot product negative?
		if (v.dot( sample ) <= 0.0) {
			// reverse the direction of the vector
			sample.negate();			
		}
			
        return sample;
    }

    

    /**
     * Assuming that this vector is a surface normal, return a new vector that
     * is pointing into the hemisphere around this normal. 
     * 
     * @param randomTheta
     * @param randomPhi
     * @return
     */
    final Vector3d sampleHemisphere_( Vector3d normal, double randomTheta, double randomPhi ) { 
    	
		Vector3d u, v, n;

		
		// initialize u to make Java happy
		u = new Vector3d();
		v = new Vector3d();
		
		// create unit vector perpendicular to normal
		// ensure normal is not colinear with (1, 0, 0)
		if (Math.abs(normal.x) < 0.5) {
			u.set( 1.0, 0.0, 0.0 );
		} else {			
			u.cross( normal, new Vector3d(0.0, 1.0, 0.0) );
		}
		u.normalize();
		
		// v should already be unit length
		v.cross( u, normal );

		// create sample by rejection sampling
		double a, b, c;
		while (true) {
			// create random values between -1 and 1		
			a = (Math.random() * 2.0) - 1.0;
			b = (Math.random() * 2.0) - 1.0;
			if (a*a + b*b < 1) break;
		}
		c = Math.sqrt(1 - (a*a + b*b));
		
		// return the vector a*u + b*v + c*n
		return new Vector3d( 
				a*u.x + b*v.x + c*normal.x,
				a*u.y + b*v.y + c*normal.y, 
				a*u.z + b*v.z + c*normal.z);
		
    }
    
    /**
     * Return a jittered normal that points somewhere in the
     * hemisphere around this surface normal.
     * 
     * @param randomTheta
     * @param randomPhi
     * @return
     */
    final Vector3d sampleHemisphere( Vector3d normal, double theta, double phi ) {
        
		Vector3d sample;

		// calculate direction vector    		
		double x = Math.cos(phi) * Math.sin(theta);
		double y = Math.sin(phi) * Math.sin(theta);
		double z = Math.cos(theta);
	
		// create a new ray
		sample = new Vector3d( x, y, z );

		// add the average normal, and normalize
		sample.add( sample, normal );
		sample.normalize();
    
		// dot product negative?
		if (normal.dot( sample ) < 0.0) {
			// reverse the direction of the vector
			sample.negate();			
		}
			
        return sample;
    }

}

