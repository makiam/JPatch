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

package inyo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.vecmath.*;

/**
 * 
 * @author David Cuny
 * 
 * The core rendering class.
 *  
 */
class RtRayTracer {

	BufferedImage image;

	/**
	 * Create an instance of the ray tracer.
	 * 
	 * @param world
	 *            Contains the scene to be renderered, as well as parameters
	 * @param width
	 *            Width of the graphic to output
	 * @param height
	 *            Height of the frame to output
	 * @param scale
	 *            Amount to scale the <b>width </b> and <b>height </b>.
	 * @param filename
	 *            Name of file to output to
	 */

	public void renderImageToFile(RtWorld world, String filename) {

		// create the image
		Image image = this.renderImage(world);

		// FIXME: quick hack for faster rendering
		world.jitter = 1;
		world.scale = .25;

		// write out the result
		File file = new File(filename);
		try {
			ImageIO.write((RenderedImage) image, world.outputFormat, file);
		} catch (IOException ioe) {
			System.out.println("Error creating file:\n " + ioe);
		}
		// release the file
		file = null;
	}

	public Image renderImage(RtWorld world) {

		// scale the values
		world.height *= world.scale;
		world.width *= world.scale;
		world.fov *= world.scale;

		// check jitter
		if (world.jitter == 0) {
			world.jitter = 1;
		}

		// create the camera
		RtPathNode pathNode = world.getPathNode();

		// start timer
		double startTime = System.currentTimeMillis();

		// subdivide model
		if (world.subdivisionMethod == RtWorld.OCTREE) {
			System.out.println("Creating octree...");
			// iterate through the models
			for (int i = 0; i < world.modelList.size(); i++) {
				RtModel model = (RtModel) world.modelList.get(i);
				model.buildOctree(world.maxOctreeDepth, world.maxOctreeItems);
			}
		} else {
			System.out.println("Creating Kd tree...");
			// create a kd tree for all the geometry
			world.kdTree = new RtKdTree(world, 0, world.triangleCount - 1);
		}

		// create a canvas to store the image
		RtCanvas canvas = new RtCanvas(world.width, world.height);

		// pre-sample the scene so samples are evenly distributed
		if (world.useIrradianceCache) {
			// create the cache
			world.irradianceCache = new RtIrradianceCache(world);
			// do an initial sampling in it
			// this.coarseIrradiancePass( world, pathNode, canvas, 8 );
		}

		// raytrace or zbuffer?
		if (world.useZBuffer) {
			System.out.println("Rendering zBuffer...");
			// FIXME: no zbuffer
			// traceBuffer(world, zBuffer, canvas);

		} else {
			System.out.println("Raytracing...");
			trace(world, pathNode, canvas);
		}

		// add lens flare?
		drawLensFlare(world, canvas);

		// end of rendering
		double endTime = System.currentTimeMillis();

		// write result
		System.out.println("Writing the file");

		// store the image
		Image image = canvas.getImage();
		// Image image = canvas.scaledImage(world.scale);

		// show stats?
		if (world.showStats) {
			Graphics g = image.getGraphics();
			g.setColor(Color.white);
			g.drawString((endTime - startTime) / 1000 + " seconds", 4,
					world.height - 28);
			if (world.useZBuffer) {
				g.drawString(world.triangleCount
						+ " triangles, zbuffer buckets =" + world.jitter
						+ " FOV=" + world.fov, 4, world.height - 16);
			} else {
				g.drawString(world.triangleCount + " triangles, jitter="
						+ world.jitter, 4, world.height - 16);
			}
			if (world.pathTracing == RtWorld.PATHTRACE_HEMISPHERE) {
				g.drawString(world.pathTracingThetaSamples + "x"
						+ world.pathTracingPhiSamples
						+ " hemisphere samples, max depth "
						+ world.pathTracingMaxBounces, 4, world.height - 4);
			} else if (world.useAmbientOcclusion) {
				g.drawString(world.ambientOcclusionSamples + " AO samples", 4,
						world.height - 4);
			}
		}

		// write end time
		System.out.println((endTime - startTime) / 1000 + " seconds");

		return image;
	}

	void drawLensFlare(RtWorld world, RtCanvas canvas) {

		// set the origin of the eye ray
		Point3d origin = new Point3d(0, 0, 1);

		// iterate through the lights
		for (int i = 0; i < world.lightList.size(); i++) {
			// get a light from the list
			RtLight light = (RtLight) world.lightList.get(i);

			// lens flare effect set for this light?
			if (light.lensFlare) {
			
				// get a shadow ray
				RtPathNode shadowRay = light.getShadowRay(world, origin, null, true);
	
				// transparent object block flare
				shadowRay.stopAtFirstHit = true;
	
				// nothing occluding the light?
				shadowRay.hitTest(world);
				if (!shadowRay.hit) {
					// calculate screen position of the light
					int x = (int) ((light.position.x / light.position.z) * world.fov);
					int y = (int) ((light.position.y / light.position.z) * world.fov);
	
					// flare is 1/4 size of frame
					int radius = canvas.high / 4;
	
					RtLensFlare.drawFlare(canvas, x, y, radius);
				}
	
				// free the light ray
				shadowRay.inUse = false;
			}
		}

	}

	/**
	 * Render the zbuffer into the canvas. This is primarily used for debugging.
	 * 
	 * @param world
	 *            Contains the geometry and settings
	 * @param zbuffer
	 *            The depth buffer
	 * @param canvas
	 *            The canvas to draw to
	 */
	void traceBuffer(RtWorld world, RtZBuffer zbuffer, RtCanvas canvas) {

		// get a pathNode from the pool
		RtPathNode eyeRay = world.getPathNode();

		// set the origin of the eye ray
		eyeRay.origin.set(0, 0, 1);

		// scan from top to bottom
		int b = 0;
		for (int y = zbuffer.top; y >= zbuffer.bottom; y--) {
			// from left to right
			int a = 0;
			for (int x = zbuffer.left; x <= zbuffer.right; x++) {
				// what's in the zbuffer?
				eyeRay.hitTriangle = zbuffer.getTriangle(x, y);
				eyeRay.hit = (eyeRay.hitTriangle != null);

				// was a triangle hit?
				if (eyeRay.hit) {
					// set up the direction vector
					eyeRay.lookAt(x, y, world.fov);

					// test against the predicted triangle
					eyeRay.hit = false;
					eyeRay.hitTriangle.hitTest(eyeRay);

					// missed the predicted triangle?
					if (!eyeRay.hit) {
						// hit test against the world
						eyeRay.hit = false;
						eyeRay.ignoreTriangle = null;
						world.irradianceSampled = false;
						eyeRay.hitTest(world);
					}
				}

				// calculate the color at this point
				eyeRay.calcColor(world, 0, 0);
				canvas.setPixelScaled(RtCanvas.ABSOLUTE, a, b, eyeRay.red,
						eyeRay.green, eyeRay.blue);

				// move x
				a++;
			}
			// move y
			b++;
		}
		// release the eye node
		eyeRay.inUse = false;
	}

	/**
	 * Send out a single ray to sample a pixel
	 * 
	 * @param world
	 * @param pathNode
	 * @param zbuffer
	 * @param canvas
	 * @param x
	 * @param y
	 */
	void singleSamplePixel(RtWorld world, RtPathNode pathNode, RtCanvas canvas,
			int x, int y) {

		// echo output to the console
		// System.out.println("tracePixel("+x+","+y+")");

		// set the eye origin
		pathNode.origin.set(0, 0, 0); // SL: changed from (0,0,1) to (0,0,0)

		// set direction vector
		pathNode.lookAt(x, y, world.fov);

		// clear pathNode
		pathNode.hit = false;
		pathNode.ignoreTriangle = null;
		pathNode.rayId = world.nextRayId++;
		pathNode.stopAtFirstHit = false;
		world.irradianceSampled = false;

		// raytrace full world
		pathNode.hitTest(world);

		// calculate the color for the hit triangle
		pathNode.calcColor(world, 0, 0);

		// set the color
		canvas.setPixelScaled(RtCanvas.CARTESIAN, x, y, pathNode.red,
				pathNode.green, pathNode.blue);
	}

	/**
	 * Send out multiple jittered rays to oversample the pixel
	 * 
	 * @param world
	 * @param pathNode
	 * @param canvas
	 * @param x
	 * @param y
	 */
	void multiSamplePixel(RtWorld world, RtPathNode pathNode, RtCanvas canvas,
			int x, int y) {

		//		 echo output to the console
		//		 System.out.println("tracePixel("+x+","+y+")");

		double jitterWidth = 1.0 / (double) world.jitter;
		double halfJitter = jitterWidth / 2;
		double jitterArea = world.jitter * world.jitter;

		// set the eye origin
		pathNode.origin.set(0, 0, 0); // SL: changed from (0,0,1) to (0,0,0)

		// get prior values
		int rgb = canvas.getRGB(RtCanvas.CARTESIAN, x, y);

		// convert back to doubles
		double red = canvas.redByte(rgb) / 255f;
		double green = canvas.greenByte(rgb) / 255f;
		double blue = canvas.blueByte(rgb) / 255f;

		// sub-pixel sampling
		for (double x0 = 0.0; x0 < 1; x0 += jitterWidth) {
			for (double y0 = 0.0; y0 < 1; y0 += jitterWidth) {

				// pixel
				double jitterX = (double) x;
				double jitterY = (double) y;

				// jitter?
				if (world.jitter > 1) {
					// add jitter, but center on pixel
					jitterX += x0 + (Math.random() * jitterWidth) - halfJitter;
					jitterY += y0 + (Math.random() * jitterWidth) - halfJitter;
				}

				// set direction vector
				pathNode.lookAt(jitterX, jitterY, world.fov);

				// clear pathNode
				pathNode.hit = false;
				pathNode.ignoreTriangle = null;
				pathNode.stopAtFirstHit = false;
				pathNode.rayId = world.nextRayId++;
				world.irradianceSampled = false;

				// raytrace full world
				pathNode.hitTest(world);

				// calculate the color for the hit triangle
				pathNode.calcColor(world, 0, 0);

				// accumulate the color
				red += pathNode.red;
				green += pathNode.green;
				blue += pathNode.blue;

			}

		}

		// calculate average color. the +1 accounts for the initial sample
		red /= jitterArea + 1;
		green /= jitterArea + 1;
		blue /= jitterArea + 1;
		canvas.setPixelScaled(RtCanvas.CARTESIAN, x, y, red, green, blue);
	}

	/***************************************************************************
	 * Simulate sampling a pixel using focal blur
	 * 
	 * @param world
	 * @param pathNode
	 * @param canvas
	 * @param x
	 * @param y
	 */
	void focalBlurSamplePixel(RtWorld world, RtPathNode pathNode,
			RtCanvas canvas, int x, int y) {

		// prevent oversampling
		world.jitter = 1;

		// set the eye origin
		pathNode.origin.set(0, 0, 0);

		// set direction vector
		// pathNode.lookAt(x, y, world.fov);

		// calculate the focal point from eye to the focal point
		double focalX = ((double) x / (double) world.fov)
				* (world.fov + world.focalDepth);
		double focalY = ((double) y / (double) world.fov)
				* (world.fov + world.focalDepth);
		double focalZ = (world.fov + world.focalDepth);

		// these hold the accumulated color from the samples
		double red = 0.0;
		double green = 0.0;
		double blue = 0.0;

		// radius of aperature
		double halfAperature = world.aperture / 2;

		for (int i = 0; i < world.blurSamples; i++) {

			// FIXME! The aperature calculated here is square. It should
			// test to make sure the point falls within a circular area

			// jitter the eye position
			pathNode.origin.x = (Math.random() * world.aperture)
					- halfAperature;
			pathNode.origin.y = (Math.random() * world.aperture)
					- halfAperature;
			pathNode.origin.z = 0;

			// set direction vector to look at hit point
			pathNode.lookAt(focalX, focalY, focalZ);
			// pathNode.lookAt( x, y, world.fov );

			// clear pathNode
			pathNode.hit = false;
			pathNode.ignoreTriangle = null;
			pathNode.stopAtFirstHit = false;
			world.irradianceSampled = false;
			pathNode.rayId = world.nextRayId++;

			// raytrace
			pathNode.hitTest(world);

			// calculate the color for the hit triangle
			pathNode.calcColor(world, 0, 0);

			// accumulate color
			red += pathNode.red;
			green += pathNode.green;
			blue += pathNode.blue;

		}

		// set the result
		canvas.setPixelScaled(RtCanvas.CARTESIAN, x, y,
				red / world.blurSamples, green / world.blurSamples, blue
						/ world.blurSamples);
	}

	/**
	 * Render the full scene. Loops through the pixel, calling <b>tracePixel
	 * </b>/.
	 * 
	 * @param world
	 * @param pathNode
	 * @param zbuffer
	 * @param canvas
	 * @param gap
	 */
	void trace(RtWorld world, RtPathNode pathNode, RtCanvas canvas) {

		// canvas for fake oversampling
		RtCanvas original = null;
		
		// used to calculate progress
		int estimateCount = world.height;
		if (world.jitter > 1 && !world.multisample) {
			// double the number of lines
			estimateCount *= 2;
		}
		int estimateRemaining = estimateCount;

		// create an array to hold the hit materials
		RtMaterial materialBuffer[][] = new RtMaterial[canvas.wide + 1][canvas.high + 1];
		RtMaterial reflectedBuffer[][] = new RtMaterial[canvas.wide + 1][canvas.high + 1];
		boolean needsOversampling[][] = new boolean[canvas.wide + 1][canvas.high + 1];
		int lightCountBuffer[][] = new int[canvas.wide + 1][canvas.high + 1];
		int a, b;

		// skip first pass if every pixel gets multisampled
		if (!world.multisample) {
			// scan from top to bottom
			a = 0;
			for (int y = canvas.halfHigh; y >= -canvas.halfHigh; y--) {

				// halt rendering?
				if (world.stopRendering) {
					return;
				}

				System.out.println("y=" + y);
				// scan from left to right
				b = 0;
				for (int x = -canvas.halfWide; x <= canvas.halfWide; x++) {
					//System.out.println("x="+x);
					// clear sample flag
					world.irradianceSampled = false;

					// clear needs oversampling flag
					pathNode.needsOversampling = false;

					// render a single pixel
					if (world.useFocalBlur) {
						focalBlurSamplePixel(world, pathNode, canvas, x, y);
					} else {
						// FIXME: add option of multisample as well
						singleSamplePixel(world, pathNode, canvas, x, y);
					}

					// store indirect flag
					reflectedBuffer[b][a] = pathNode.reflectedMaterial;

					// set the needs oversampling flag
					needsOversampling[b][a] = pathNode.needsOversampling;

					// store visible material
					materialBuffer[b][a] = pathNode.visibleMaterial;

					//  don't track if soft shadows is on
					if (world.useSoftShadows) {
						// save the count of lights at this point
						lightCountBuffer[b][a] = pathNode.lightCount;
					}

					// increment b
					b++;

				}
				// increment a
				a++;

				// update progress counter
				estimateRemaining--;
				world.progress = 1 - (estimateRemaining / estimateCount);
			}

			// no oversampling?
			if (world.jitter < 2) {
				return;
			}

		}
		
		// check surrounding pixels to flag oversampling
		for (a = 1; a < canvas.high; a++ ) {
			for (b = 1; b < canvas.wide; b++ ) {
				// get the material and the reflected material
				RtMaterial material = materialBuffer[b][a];
				RtMaterial reflected = reflectedBuffer[b][a];

				// different materials?
				if ((materialBuffer[b][a] != materialBuffer[b - 1][a - 1])
				|| (materialBuffer[b][a] != materialBuffer[b][a - 1])
				|| (materialBuffer[b][a] != materialBuffer[b + 1][a - 1])
				|| (materialBuffer[b][a] != materialBuffer[b - 1][a])
				|| (materialBuffer[b][a] != materialBuffer[b + 1][a])
				|| (materialBuffer[b][a] != materialBuffer[b - 1][a + 1])
				|| (materialBuffer[b][a] != materialBuffer[b][a + 1])
				|| (materialBuffer[b][a] != materialBuffer[b + 1][a + 1])

				// direct vs. reflection or refraction
				|| (reflectedBuffer[b][a] != reflectedBuffer[b - 1][a - 1])
				|| (reflectedBuffer[b][a] != reflectedBuffer[b][a - 1])
				|| (reflectedBuffer[b][a] != reflectedBuffer[b + 1][a - 1])
				|| (reflectedBuffer[b][a] != reflectedBuffer[b - 1][a])
				|| (reflectedBuffer[b][a] != reflectedBuffer[b + 1][a])
				|| (reflectedBuffer[b][a] != reflectedBuffer[b - 1][a + 1])
				|| (reflectedBuffer[b][a] != reflectedBuffer[b][a + 1])
				|| (reflectedBuffer[b][a] != reflectedBuffer[b + 1][a + 1])

				// different number of lights at this point?
				|| (lightCountBuffer[b][a] != lightCountBuffer[b - 1][a - 1])
				|| (lightCountBuffer[b][a] != lightCountBuffer[b][a - 1])
				|| (lightCountBuffer[b][a] != lightCountBuffer[b + 1][a - 1])
				|| (lightCountBuffer[b][a] != lightCountBuffer[b - 1][a])
				|| (lightCountBuffer[b][a] != lightCountBuffer[b + 1][a])
				|| (lightCountBuffer[b][a] != lightCountBuffer[b - 1][a + 1])
				|| (lightCountBuffer[b][a] != lightCountBuffer[b][a + 1]) 
				|| (lightCountBuffer[b][a] != lightCountBuffer[b + 1][a + 1])
				
				// base or reflected material has a pigment?
				|| (material != null && material.hasPigment() ) 
				|| (reflected != null && reflected.hasPigment() )) {

					// flag as candidate for resampling
					needsOversampling[b][a] = true;						
				}
			}
		}

		// save the ao sample rate
		int aoSamples = world.ambientOcclusionSamples;
		world.ambientOcclusionSamples = 1;

		// need copy of original for fake oversampling?
		if (world.useFakeOversampling) {
			// make a copy of the original canvas
			original = new RtCanvas( canvas );
		}

		
		// oversampling pass
		a = 1;
		for (int y = canvas.halfHigh - 1; y >= -canvas.halfHigh + 1; y--) {
			System.out.println("y=" + y);

			// scan from left to right
			b = 1;
			for (int x = -canvas.halfWide + 1; x <= canvas.halfWide - 1; x++) {

				// halt rendering?
				if (world.stopRendering) {
					return;
				}
				
				// flagged for oversampling?
				if (needsOversampling[b][a]) {
					// check color difference
					needsOversampling[b][a] = canvas.needsResampling(RtCanvas.ABSOLUTE, b, a, world.colorTolerance);
					if (needsOversampling[b][a]) {
						// debug mode?
						if (!world.debugOversampling) {
							if (world.useFakeOversampling) {
								// fake oversampling with blur
								canvas.averagePixel(RtCanvas.ABSOLUTE, original, b, a );
							} else {
								// multisample the pixel
								multiSamplePixel(world, pathNode, canvas, x, y);
							}
						}
					}
				}

				// increment along x axis
				b++;
			}
			// increment along y axis
			a++;

			// update progress counter
			estimateRemaining--;
			world.progress = 1 - (estimateRemaining / estimateCount);
		}

		// restore the ambient occlusion sample rate
		world.ambientOcclusionSamples = aoSamples;
		
		// need to debug oversampling?
		if (world.debugOversampling) {
			for (a = 1; a < canvas.high; a++ ) {
				for (b = 1; b < canvas.wide; b++ ) {
					// marked for oversampling?
					if (needsOversampling[b][a]) {
						// color the pixel red
						canvas.setPixel(RtCanvas.ABSOLUTE, b, a, 255, 0, 0);
					}
				}
			}
		}

	}
}