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
 * Holds the general settings, and all the geometry of the scene.
 */

package inyo;

import javax.vecmath.*;
import patterns.*;

import java.util.ArrayList;

public class RtWorld {

	static int OCTREE = 0;

	static int KDTREE = 1;

	// subdivision method
	int subdivisionMethod = OCTREE;

	boolean traceFlag = false;

	// SL: camera settings
	RtCamera camera = new RtCamera(); // initialize with default camera

	Matrix4d m4World2Cam = new Matrix4d(camera.getMatrix()); // to convert from
															 // world to camera
															 // space

	Matrix4d m4Cam2World = new Matrix4d(); // to convert from camera to world
										   // space

	// size of image
	int height = 640; // image height
	int width = 480; // image width
	double scale = 1; // amount to scale image by

	// progress counter
	double progress = 0;

	// for cancelling rendering
	boolean stopRendering = false; // if true, stops rendering

	// general settings
	boolean showStats = true; // display stats on output file?
	boolean debugOversampling = false; // if true, marks candidates instead of resampling

	boolean displayIrradianceSamples = true; // display irradiance sample points
	String outputFormat = new String("png");
	int maxOctreeDepth = 6; // maximum depth of the octree
	int maxOctreeItems = 14; // maximum number of items per leaf
	boolean multisample = false; // if true, oversample every pixel
	int jitter = 1; // oversample per pixel, minimum value must be 1. This is n
					// SQUARED!
	int maxDepth = 6; // max recursion depth
	boolean useOversampling = false; // do adaptive oversampling?
	boolean useFakeOversampling = false; // blur instead of oversampling
	double colorTolerance = 30; // 0..255. amount color must differ to trigger resampling colors
	boolean useZBuffer = false; // buggy! use zbuffer to find first hit.
	boolean useSoftShadows = false; // use soft shadows?
	int softShadowSamples = 8; // number of samples per light
	boolean useBlackShadows = false; // if false, use transparent shadows
	boolean useFakeCaustics = true; // if true, render fake caustics
	boolean useOversampledCaustics = false; // if true, caustics are oversampled
	boolean useAmbientOcclusion = false; // ambient occlusion?
	
	double skyPower = 1.0; // strength of sky color
	Color3f skyColor = new Color3f(0, 1, 1); // color of sky
	Color3f skyLightColor = new Color3f(1, 1, 1); // color of sky's light
	Texture skyTexture; // texture of the sky sphere

	// ambient occlusion
	boolean ambientOcclusionAdditive = false; // add light instead of shadow
	int ambientOcclusionSamples = 3; // number of samples/pixel (4 is good)
	double ambientOcclusionDistance = 1000; // distance to ignore hits (100 is good)
	double colorBleed = 0.25; // amount of color bleeding from AO materials

	// path tracing
	public static final int PATHTRACE_OFF = 0; // no pathtracing
	public static final int PATHTRACE_COSINE = 1; // use cosine sampling
	public static final int PATHTRACE_HEMISPHERE = 2; // sample over stratified
													  // hemisphere
	int pathTracing = PATHTRACE_OFF; // use path tracer?
	
	int pathTracingThetaSamples = 2; // number of hemisphere samples around angle theta (0..2PI)
	int pathTracingPhiSamples = 2; // number of hemisphere samples around angle phi (0..PI)
	int pathTracingMaxBounces = 3; // number of bounces, should use maxDepth instead?

	// focal blur
	boolean useFocalBlur = false; // true if using focal blur
	double aperture = 20.0; // size of aperture, if using focal blur
	double focalDepth = -250; // focal depth of camera
	int blurSamples = 12; // samples per pixel for focal blur

	// irradiance cache
	boolean useIrradianceCache = false; // use irradiance cache?
	int irradianceMaxTreeDepth = 6; // maximum depth of octree
	boolean irradianceSampled = false; // set when irradiance is sampled at a point
	double irradianceCacheTolerance = 10.0; // allowable error to accept estimated sample
	double irradianceCacheSpacing = 100.0; // spacing between samples
	double irradianceScale = 0.1; // amount to scale world for irradiance distances
	double irradianceCacheMinDistance = 0.50;
	double irradianceCacheMaxDistance = 10000.0;
	RtIrradianceCache irradianceCache; // holds the irradiance cache

	// field of vision
	double fov; // actual

	// hold all the lights
	ArrayList lightList = new ArrayList();

	// holds all the models
	ArrayList modelList = new ArrayList();
	RtModel currentModel = null;
	int triangleCount = 0;

	// the Kd tree for the world space
	RtKdTree kdTree = null;

	// pool of available viewers
	RtPathNode pathNodeList[];

	int pathNodeCount = 0;

	int nextRayId = 1;

	/**
	 * Create an instance of the scene and all the settings.
	 * <p>
	 * This also creates a cache of path nodes, so they don't have to be
	 * constantly created and destroyed.
	 *  
	 */
	public RtWorld() {
		// SL: set up inverse cam2world matrix
		m4Cam2World.invert(m4World2Cam);

		// allocate a pool of pathNodes
		this.pathNodeCount = this.maxDepth * 3;
		pathNodeList = new RtPathNode[this.pathNodeCount];
		for (int i = 0; i < pathNodeCount; i++) {
			pathNodeList[i] = new RtPathNode();
		}
	}

	/**
	 * Returns a free path node from the cache.
	 * 
	 * @return
	 */
	RtPathNode getPathNode() {
		for (int i = 0; i < this.pathNodeCount; i++) {
			if (!this.pathNodeList[i].inUse) {
				RtPathNode selected = this.pathNodeList[i];
				// clear the default values
				selected.inUse = true;
				selected.red = 0.0;
				selected.green = 0.0;
				selected.blue = 0.0;
				selected.stopAtFirstHit = true;
				selected.hit = false;
				selected.needsOversampling = false;
				selected.rayId = nextRayId++;
				selected.maxDistance = 0;
				selected.inside = false;
				return selected;
			}
		}
		// FIXME: this shouldn't happen
		System.out.println("Out of path nodes!");
		return null;
	}

	/**
	 * SL: Sets the camera, (and computes the transformation matrices).
	 * 
	 * @param camera
	 */
	final void setCamera(RtCamera camera) {
		// set the camera
		this.camera = camera;

		// create a matrix
		m4World2Cam.set(camera.getMatrix());
		m4Cam2World.invert(m4World2Cam);

		// get the focal length based on the screen width
		fov = camera.getFocalLength(this.width);
	}

	/**
	 * Adds a light to the light list.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param power
	 * @param radius
	 */
	final RtLight addLight(double x, double y, double z, double power,
			double radius) {
		// SL: transform light positions into camera space
		Point3d p = new Point3d(x, y, z);
		m4World2Cam.transform(p);
		radius *= m4World2Cam.getScale();
		RtLight light = new RtLight(p.x, p.y, p.z, power, radius);
		lightList.add( (Object)light );
		return light;
	}

	final Point3d transformPoint(double x, double y, double z) {
		// transform point to camera space
		Point3d p = new Point3d(x, y, z);
		m4World2Cam.transform(p);
		return p;
	}

	final Point3d transformPoint(Point3d point) {
		// transform point to camera space
		Point3d p = new Point3d(point);
		m4World2Cam.transform(p);
		return p;
	}

	final Vector3d transformNormal(Vector3d normal) {
		// transform normal to camera space
		Vector3d n = new Vector3d(normal);
		m4World2Cam.transform(n);
		n.normalize();
		return n;
	}

	final Vector3d transformNormal(double x, double y, double z) {
		// transform normal to camera space
		Vector3d n = new Vector3d(x, y, z);
		m4World2Cam.transform(n);
		n.normalize();
		return n;
	}

	final void addModel() {
		// create a new model
		currentModel = new RtModel();

		// add it to the model list
		this.modelList.add((Object) currentModel);
	}

	void setSkyTexture(Texture texture) {
		skyTexture = texture;
	}
}