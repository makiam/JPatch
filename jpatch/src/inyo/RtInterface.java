package inyo;
import java.awt.Image;
import java.util.ArrayList;

import javax.vecmath.*;

import patterns.*;
// SL: needed for sky-texture support

public class RtInterface implements JPatchInyoInterface {
	
	RtWorld world;
	RtMaterial currentMaterial;
	RtLight currentLight;
	ArrayList vertexList;
	ArrayList triangleList;

	
	public RtInterface() 
	{ 
       	// create a world to hold all the objects
       	world = new RtWorld();
	}
	
	
	// OBJECTS
	
	public void objectBegin() {
		// initialize the vertex list
		vertexList = new ArrayList();		
		
		// add a model to the world
		world.addModel();
	}

	public void objectEnd() {
		// free the vertexList
		vertexList = null;
	}
	
	
	// VERTICES
	
	public void addVertex( double x, double y, double z, double rx, double ry, double rz, double nx, double ny, double nz ) {
		// add a vertex, reference point and normal
		// world.addVertex( x, y, z, rx, ry, rz, nx, ny, nz );
		
		// transform to camera space
		Point3d p = world.transformPoint(x, y, z);
		Point3d r = new Point3d(rx, ry, rz);
		Vector3d n = world.transformNormal(nx, ny, nz);
		
		// create a vertex
		RtVertex vertex = new RtVertex(p, r, n);
		
		// add it to the vertex list
		vertexList.add( vertex );
		
	}

	// TRIANGLES
	
	public void addTriangle(int vertexIndex1, int vertexIndex2, int vertexIndex3) {
		// add a triangle to the world list
		// world.addTriangle( vertexIndex1, vertexIndex2, vertexIndex3 );
		
		// get the vertices from the vertex list
		RtVertex v1 = (RtVertex)vertexList.get(vertexIndex1);
		RtVertex v2 = (RtVertex)vertexList.get(vertexIndex2);
		RtVertex v3 = (RtVertex)vertexList.get(vertexIndex3);

		// create a triangle
		RtTriangle triangle = new RtTriangle( v1, v2, v3 );
		
		// set material
		triangle.material = this.currentMaterial;
		
		// add to the current model
		world.currentModel.addTriangle( triangle );
		
		// increment world count
		world.triangleCount++;
	}

	
	// MATERIAL ATTRIBUTES
	
	public void addMaterial( float red, float green, float blue ) {
		currentMaterial = new RtMaterial( red, green, blue );
	}
	
	public void setMaterialFilter( double filter ) {
		currentMaterial.filter = filter;
	}

	public void setMaterialTransmit( double transmit ) {
		currentMaterial.transmit = transmit;
	}
	
	public void setMaterialAmbient( double ambient ) {
		currentMaterial.ambient= ambient;
	}

	public void setMaterialDiffuse( double diffuse ) {
		currentMaterial.diffuse= diffuse;
	}

	public void setMaterialBrilliance( double brilliance ) {
		currentMaterial.brilliance= brilliance;
	}

	public void setMaterialSpecular( double specular) {
		currentMaterial.specular = specular;
	}
	
	public void setMaterialRoughness( double roughness ) {
		currentMaterial.roughness = roughness;
	}
		
	public void setMaterialMetallic( double metallic ) {
		currentMaterial.metallic = metallic;
	}

	public void setMaterialReflection( double min, double max, double falloff ) {
		currentMaterial.reflectionMin = min;
		currentMaterial.reflectionMax = max;
		currentMaterial.reflectionFalloff = falloff;
	}

	public void setMaterialRefraction( double ior ) {
		currentMaterial.ior = ior;
	}
	
	public void setMaterialConserveEnergy( boolean conserveEnergy ) {
		currentMaterial.conserveEnergy = conserveEnergy;
	}

	public void setMaterialTexture( String textureName ) {
		currentMaterial.texture = TextureParser.parseTexture(textureName);
	}

	// SKY ATTRIBUTES 
	
	public void setSkyColor( float red, float green, float blue ) {
		// color of the sky
		world.skyColor = new Color3f( red, green, blue );
	}

	public void setSkyLightColor( float red, float green, float blue ) {
		// color of the light from the sky
		world.skyLightColor = new Color3f( red, green, blue );
	}

	public void setSkyPower( double power ) {
		// strength of the light from the sky
		world.skyPower = power;
	}

	public void setSkyTexture( String textureName ) {
		// strength of the light from the sky
		world.skyTexture = TextureParser.parseTexture(textureName);
	}
			
	
	// LIGHTS
		
	public void addLight( double x, double y, double z, double power ) 
	{
		this.currentLight = world.addLight( x, y, z, power, 0 );
	}
	
	public void setLightRadius( double radius )	{
		this.currentLight.radius = radius;
	}
	
	public void setLightColor( float r, float g, float b ) {
		this.currentLight.color = new Color3f( r, g, b );
	}
		
	public void setLightCastsShadow( boolean castsShadow ) {
		currentLight.castsShadow = castsShadow;
	}
	
	public void setLightHasSpecular( boolean hasSpecular ) {
		currentLight.hasSpecular = hasSpecular;
	}
	
	public void setLightHasDiffuse( boolean hasDiffuse) {
		currentLight.hasDiffuse = hasDiffuse;
	}

	public void setLightFalloffLinear( double falloffScale ) {
		currentLight.falloffType = RtLight.FALLOFF_LINEAR;
		currentLight.falloffScale = falloffScale;		
	}
	
	public void setLightFalloffQuadratic( double scale ) {
		currentLight.falloffType = RtLight.FALLOFF_QUADRATIC;
		currentLight.falloffScale = scale;		
	}

	public void setLightFalloffAngle( double angle ) {		
		currentLight.falloffAngle = angle;		
	}


	// GENERAL SETTINGS
	
	public void setShowStats( boolean flag ) {
		world.showStats = flag;
	}
		
	public void setOversample( int jitter, boolean sampleEverthing ) {
		world.jitter = jitter;
	}

	public void setMaxRecursionDepth( int max ) {
		world.maxDepth = max;
	}

	// SOFT SHADOWS
	public void setSoftShadowSamples( int samples ) {
		if (samples == 0) {
			world.useSoftShadows = false;
		} else {
			world.useSoftShadows = true;
			world.softShadowSamples = samples;
		}
	}
	
	public void setTransparentShadows( boolean flag ) {
		world.useBlackShadows = !flag;
	}
	
	public void setCaustics( boolean flag, boolean oversample ) {
		world.useFakeCaustics = flag;
		world.useOversampledCaustics = oversample;
	}

	public void setUseAmbientOcclusion( boolean flag ) {
		world.useAmbientOcclusion = flag;
	}
	
	public void setAmbientOcclusion( double maxDistance ) {
		world.ambientOcclusionDistance = maxDistance;
	}
	
	public void setAmbientOcclusionSamples( int samples ) {
		world.ambientOcclusionSamples = samples;
	}
	
	public void ambientOcclusionColorBleed( float colorBleed ) {
		world.colorBleed = colorBleed;
	}

	// PATH TRACING
	
	public void setUsePathTrace( boolean flag ) {
		world.pathTracing = RtWorld.PATHTRACE_COSINE;
	}
	
	public void setPathTracePhi( int phi ) {
		world.pathTracingPhiSamples = phi;
	}

	public void setPathTraceTheta( int theta ) {		
		world.pathTracingThetaSamples = theta;		
	}

	public void setPathTraceMaxBounces( int maxBounces ) {
		world.pathTracingMaxBounces = maxBounces;
	}	
	
	/**
	 * Sets golobal frame parameters
	 * @param width the width of the frame in pixel - that is it's X-resolution ;-)
	 * @param height the height of the frame in pixel
	 * @param aspectRatio the aspect ratio (width/height) allows to use non square pixel, e.g. for VCDs or DVDs.
	 */

	// CAMERA
	
	public void setCamera(javax.vecmath.Matrix4d transformationMatrix ) {
		world.camera.setMatrix( transformationMatrix );
		world.setCamera(world.camera);
	}
	
	public void setCameraFocalLength( float focalLength ) {
		world.camera.setFocalLength( focalLength );
		world.setCamera(world.camera);
	}
	
	public void setCameraPosition( double x, double y, double z ) {
		world.camera.setPosition( x, y, z );
		world.setCamera(world.camera);
	}
	
	public void setCameraPointTo( double x, double y, double z ) {
		world.camera.pointAt(x, y, z, null);
		world.setCamera(world.camera);
	}
	

	public void setImageSize( int height, int width, double scale ) {
		world.height = height;
		world.width = width;
		world.scale = scale;
	}
	
	// RENDERING
	
	public void startRendering(InyoJPatchInterface callback) {
		//RenderOnThread renderingThread = new RenderOnThread( world, callback );
		//renderingThread.start();
		
		System.out.println("startRendering()");
		RtRayTracer raytracer = new RtRayTracer();
		Image image = raytracer.renderImage(world);
		callback.renderingDone(image);
	}
	
	public double getProgress() {
		return world.progress;
	}
	
	public void stopRendering() {
		world.stopRendering = true;
	}
	
	class RenderOnThread extends Thread {
		
		InyoJPatchInterface callback;
		RtWorld world;
		
		public RenderOnThread( RtWorld world, InyoJPatchInterface callback ) {
			this.world = world;
			this.callback = callback;
		}
		
		public void run() {
			// create a raytracer
			RtRayTracer raytracer = new RtRayTracer();
			
			// render the image
			Image image = raytracer.renderImage( world );
			
			// signal the image is done
			callback.renderingDone(image);
			
		}
	}
}
