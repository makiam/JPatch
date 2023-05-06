package inyo;

/**
 * This interface is used by JPatch to invoke Inyo
 */
public interface JPatchInyoInterface {

	// OBJECTS
	public void objectBegin();
	public void objectEnd();

	// VERTICES
	public void addVertex( double x, double y, double z, double rx, double ry, double rz, double nx, double ny, double nz );

	// TRIANGLES
	public void addTriangle(int vertexIndex1, int vertexIndex2, int vertexIndex3);
	
	// MATERIAL ATTRIBUTES	
	public void addMaterial( float red, float green, float blue );
	public void setMaterialFilter( double filter );
	public void setMaterialTransmit( double transmit );
	public void setMaterialAmbient( double ambient );
	public void setMaterialDiffuse( double diffuse );
	public void setMaterialBrilliance( double brilliance );
	public void setMaterialSpecular( double specular);
	public void setMaterialRoughness( double roughness );
	public void setMaterialMetallic( double metallic );
	public void setMaterialReflection( double min, double max, double falloff );
	public void setMaterialRefraction( double refraction );
	public void setMaterialConserveEnergy( boolean conserveEnergy );
	public void setMaterialTexture( String textureName );

	// SKY ATTRIBUTES 
	
	public void setSkyColor( float red, float green, float blue );
	public void setSkyLightColor( float red, float green, float blue );
	public void setSkyPower( double power );
	public void setSkyTexture( String textureName );
	
	// LIGHTS
	public void addLight( double x, double y, double z, double power );
	public void setLightRadius( double radius );
	public void setLightColor( float r, float g, float b );
	public void setLightCastsShadow( boolean castsShadow );
	public void setLightHasSpecular( boolean hasSpecular );
	public void setLightHasDiffuse( boolean hasDiffuse);
	public void setLightFalloffLinear( double falloffScale );
	public void setLightFalloffQuadratic( double scale );
	public void setLightFalloffAngle( double angle );

	// GENERAL SETTINGS
	public void setShowStats( boolean flag );
	public void setOversample( int jitter, boolean sampleEverthing );
	public void setMaxRecursionDepth( int max );

	// SOFT SHADOWS
	public void setSoftShadowSamples( int samples );
	public void setTransparentShadows( boolean flag );
	public void setCaustics( boolean flag, boolean oversample );
	public void setUseAmbientOcclusion( boolean flag );
	public void setAmbientOcclusion( double maxDistance );
	public void setAmbientOcclusionSamples( int samples );
	public void ambientOcclusionColorBleed( float colorBleed );

	// PATH TRACING
	public void setUsePathTrace( boolean flag );
	public void setPathTracePhi( int phi );
	public void setPathTraceTheta( int theta );
	public void setPathTraceMaxBounces( int maxBounces );

	// CAMERA	
	public void setCamera(javax.vecmath.Matrix4d transformationMatrix );
	public void setCameraFocalLength( float focalLength );
	public void setCameraPosition( double x, double y, double z );
	public void setCameraPointTo( double x, double y, double z );
	public void setImageSize( int height, int width, double scale );
	
	// RENDERING
	
	public void startRendering(InyoJPatchInterface callback);
	public double getProgress();
	public void stopRendering();
}

