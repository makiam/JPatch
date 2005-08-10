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
 * Properties of a material. These are based on the properties of materials from the
 * POV-Ray raytracer (http://www.pov-ray.org).
 * 
 * 
 * Most of this is based on work by Sascha Ledinsky.
 */

package inyo;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import patterns.*;

class RtMaterial {
    
	// color shaders
	public static final int COLOR_RGB = 0;
	public static final int COLOR_IMAGE = 1;
	
	// diffuse shaders
	public static final int DIFFUSE_POV = 0;
	public static final int DIFFUSE_LAMBERT = 1;
	public static final int DIFFUSE_WRAPPED_LAMBERT = 2;
	public static final int DIFFUSE_OREN_NAYER = 3;
	public static final int DIFFUSE_MINNAERT = 4;
	public static final int DIFFUSE_IMAGE = 5;
	public static final int DIFFUSE_PHARR = 6;
	
	// specular shaders
	public static final int SPECULAR_POV = 0;
	public static final int SPECULAR_PHONG = 1;
	public static final int SPECULAR_BLINN = 2;
	public static final int SPECULAR_COOK_TORRENCE = 3;
	public static final int SPECULAR_REDUCED_COOK_TORRENCE = 4;
	public static final int SPECULAR_WARD_ISOTROPIC = 5;
	
    // This class emulates POV-Ray's materials
	public int colorShader;
	public int diffuseShader;
	public int specularShader;
	public int index = 0;
    public double red = 1;
    public double green = 1;
    public double blue = 1;
    public Color3f color = new Color3f();
    public double transmit = 0;
    public double filter = 0;
    public double ambient = 0.25;
    public double diffuse = 0.75;
    public double brilliance = 1;
    public double specular = 1;
    public double roughness = 0.001;
    public double metallic = 0;
    public double reflectionMin = 0;
    public double reflectionMax = 0;
    public double reflectionFalloff = 1;
    public double ior = 1;
    public boolean conserveEnergy = true;		
    public Texture texture;			// SL: pointer to procedural texture (class patterns.Texture)
    public RtImageMap imageMap = null;
    public RtScattering samples = new RtScattering();

    public double wrappedAngle = 90;	// wrapped angle for Lambert Wrapped shader
    public double darkness = 0;			// for minneart shader
    public double thickness = 0;		// for pharr skin shader
    public double rms = 0;				// for ward isotropic

    
    // subsurface scattering
	boolean singleScattering = false; // illumination from behind
	boolean fakeSingleScattering = false; // simpler version of single scattering
	boolean multipleScattering = false; // illumination from front
	double scatteringMaxRedDistance = 50; // maximum distance red light travels
	double scatteringMaxGreenDistance = 50; // maximum distance green light travels
	double scatteringMaxBlueDistance = 50; // maximum distance blue light travels
	double scatteringMultipleScale = .003; // amount to scale light multiple scattering contribution
	double scatteringSingleScale = 2.5; //.6;	// amount to scale single scattering contribution
	double scatteringSingleMaxDistance = 4; // maximum distance for single scattering
	double scatteringSingleBias = 1; // bias to offset back hitpoint by

    
    public RtMaterial(RtWorld world) {
        // hack
    	// this.imageMap = new RtImageMap( "pharr.jpg");
    }
    
    /**
     * Create a material with the color <b>{red, blue, green}</b>. All other
     * properties are set to the default values.
     * 
     * @param red
     * @param green
     * @param blue
     */
    public RtMaterial( double red, double green, double blue ) {
    	
    	// set red, green and blue
        this.red = red;
        this.green = green;
        this.blue = blue;
        
        // default shaders
        this.diffuseShader = RtMaterial.DIFFUSE_POV;
        this.specularShader = RtMaterial.SPECULAR_POV;
        
        // set as color for shaders
        this.color.set( (float)red, (float)green, (float)blue);        
    }
    
    /**
     * Returns true if material is textured.
     * @return
     */
    public boolean hasPigment() {
    	return (this.texture != null && this.texture.getPigment() != null);
    }
    
    public Color3f getColor( Point3d reference ) {
		// is there a material with a pigment?
		if (this.hasPigment()) {
			// get the color of the material in real space
			return this.texture.getPigment().colorAt((float)reference.x, (float)reference.y, (float)reference.z);
			
		} else {
			// return the color
			return this.color;
		}
	
    }
    
    /**
     * If there is a bump map associated with this material, peturb the normal.
     * @param normal
     */
    public void peturb( Vector3d normal, Point3d reference ) {
		// is there a texture?
		if (this.texture != null) {
			// get the normal from the bump map
			Vector3D bumpMap = this.texture.getNormal();
			
			// is there a bump map in the texture?
			if (bumpMap != null) {
				// get the normal from the bump map
				Vector3f perturberation = bumpMap.vectorAt((float)reference.x, (float)reference.y, (float)reference.z);
					
				// add the normal from the bump map to the normal, and normalize
				normal.x += perturberation.x;
				normal.y += perturberation.y;
				normal.z += perturberation.z;
				normal.normalize();
			}
		}

    }
    
    /**
     * Return the diffuse contribution, based on the shader for the material.
     * @param normal Surface normal
     * @param light Normalized vector to light
     * @param view Normalized vector to view
     * @return
     */
    public double getDiffuse( Vector3d normal, Vector3d light, Vector3d view ) {
    	switch (this.diffuseShader) {    		
    	case DIFFUSE_LAMBERT:
    		return RtShader.lambertDiffuse(normal, light);
    		
    	case DIFFUSE_WRAPPED_LAMBERT:
    		return RtShader.wrappedLambertDiffuse( normal, light, this.wrappedAngle );
    		
    	case DIFFUSE_OREN_NAYER:
    		return RtShader.orenNayarDiffuse( normal, light, view, this.brilliance );
    		
    	case DIFFUSE_MINNAERT:
    		return RtShader.minnaertDiffuse( normal, light, view, this.darkness );
    		
    	case DIFFUSE_PHARR:
    		return RtShader.pharrSkin(normal, view, light, this.thickness );

    	case DIFFUSE_POV:
    	default:
    		return RtShader.povDiffuse( normal, light, view, this.diffuse, this.roughness, this.brilliance );

    	}
    }

        public double getSpecular( Vector3d normal, Vector3d light, Vector3d view ) {
        	switch (this.specularShader) { 
        	case SPECULAR_PHONG:
        		return RtShader.phongSpecular(normal, light, view, this.roughness);
        		
        	case SPECULAR_BLINN:
        		return RtShader.blinnSpecular( normal, light, view, this.ior, this.specular );
        		
        	case SPECULAR_COOK_TORRENCE:
        		return RtShader.cookTorrenceSpecular( normal, light, view, this.roughness, this.ior );
        		
        	case SPECULAR_REDUCED_COOK_TORRENCE:
        		return RtShader.reducedCookTorranceSpecular(normal, light, view, this.roughness );
        		
        	case SPECULAR_WARD_ISOTROPIC:
        		return RtShader.wardIsotropicSpecular(normal, light, view, rms);

        	case SPECULAR_POV:
        	default:
        		return RtShader.povSpecular(normal, light, view, this.specular, this.roughness );
        	}
    }
           
}
