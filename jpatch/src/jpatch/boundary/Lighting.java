package jpatch.boundary;

import javax.vecmath.*;
import jpatch.entity.*;

public class Lighting {
	private Vector3f[] av3LightDirection;
	private Vector3f[] av3TransformedLightDirection;
	
	private float[] afLightIntensity;
	private boolean[] abLightSpecular;
	private float fAmbientLight;
	private Vector3f v3View = new Vector3f(0, 0, 1);
	private Matrix3f m3View = new Matrix3f();
	private boolean bStickyLight;
	private boolean bBackfaceNormalFlip = false;
	
	private Vector3f N = new Vector3f();
	private Vector3f LV = new Vector3f();
	
	public static Lighting createSimpleLight() {
		Lighting l = new Lighting();
		l.initSimpleLight();
		return l;
	}
	
	public static Lighting createHeadLight() {
		Lighting l = new Lighting();
		l.initHeadLight();
		return l;
	}
	
	public static Lighting createThreePointLight() {
		Lighting l = new Lighting();
		l.initThreePointLight();
		return l;
	}
	
	public static Lighting createDefaultLight() {
		return createSimpleLight();
	}
	
	public void setBackfaceNormalFlip(boolean flip) {
		bBackfaceNormalFlip = flip;
	}
	
	public boolean isBackfaceNormalFlip() {
		return bBackfaceNormalFlip;
	}
	
	/**
	 * inizializes basic three point light
	 */
	private void initThreePointLight() {
		av3LightDirection = new Vector3f[] {
			new Vector3f(-1,-1,-1),
			new Vector3f(0,-1,1),
			new Vector3f(1,0,-1)
		};
		afLightIntensity = new float[] { 0.6f, 0.9f, 0.3f };
		abLightSpecular = new boolean[] { true, true, false };
		fAmbientLight = 1;
		for (int l = 0; l < av3LightDirection.length; l++) {
			av3LightDirection[l].normalize();
		}
		init();
	}
	
	/**
	 * initializes simple light (from upper left, no highlights)
	 */
	private void initSimpleLight() {
		av3LightDirection = new Vector3f[] { new Vector3f(-1,-1,-1) };
		afLightIntensity = new float[] { 1f };
		abLightSpecular = new boolean[] { false };
		fAmbientLight = 1;
		av3LightDirection[0].normalize();
		init();
	}
	
	/**
	 * initializes head light (with highlights)
	 */
	private void initHeadLight() {
		av3LightDirection = new Vector3f[] { new Vector3f(0,0,-1) };
		afLightIntensity = new float[] { 1f };
		abLightSpecular = new boolean[] { true };
		fAmbientLight = 1;
		init();
	}
	
	private void init() {
		av3TransformedLightDirection = new Vector3f[av3LightDirection.length];
		for (int l = 0; l < av3LightDirection.length; l++) {
			av3TransformedLightDirection[l] = new Vector3f();
		}
		setStickyLight(JPatchSettings.getInstance().bStickyLight);
	}
	
	public final void setStickyLight(boolean sticky) {
		bStickyLight = sticky;
		for (int l = 0; l < av3LightDirection.length; l++) {
		 av3TransformedLightDirection[l].set(av3LightDirection[l]);
		}
	}
	
	public final boolean isStickyLight() {
		return bStickyLight;
	}
	
	public final void setRotation(float rotX, float rotY) {
		Matrix3f m3Transform = new Matrix3f();
		//m3View.setIdentity();
		m3View.rotX(-rotX);
		m3Transform.rotY(rotY);
		m3View.mul(m3Transform);
	}
	
	public final void transform() {
		if (!bStickyLight) {
			 for (int l = 0; l < av3LightDirection.length; l++) {
				 av3TransformedLightDirection[l].set(av3LightDirection[l]);
				 m3View.transform(av3TransformedLightDirection[l]);
				 av3TransformedLightDirection[l].normalize();
			 }
		}
	}

	public final void shade(Point3f point, Vector3f normal, MaterialProperties mp, Color3f color) {
		float diffuse = mp.ambient * fAmbientLight;
		float specular = 0;
		//if (!bBackfaceNormalFlip || normal.z < 0) N.set(normal);
		if (normal.z < 0) N.set(normal);
		else N.set(-normal.x, -normal.y, -normal.z);
		
		for (int l = 0; l < av3LightDirection.length; l++) {
			if (mp.diffuse != 0) {
				float diff = Math.max(av3TransformedLightDirection[l].dot(N) * afLightIntensity[l], 0) ;
				diffuse += (float)Math.pow(diff,mp.brilliance) * mp.diffuse;
			}
			if (abLightSpecular[l] && mp.specular != 0) {
				LV.sub(av3TransformedLightDirection[l],v3View);
				LV.scale(1f/(float)Math.sqrt(LV.dot(LV)));
				LV.normalize();
				float spec = Math.max(0,LV.dot(N));
				specular += (float)Math.pow(spec,1f/mp.roughness) * mp.specular;
			}
		}

		float red = mp.red * diffuse + specular;
		float green = mp.green * diffuse + specular;
		float blue = mp.blue * diffuse + specular;

		color.set(red, green, blue);
	}
	
	public final int shade(Point3f point, Vector3f normal, MaterialProperties mp) {
		float diffuse = mp.ambient * fAmbientLight;
		float specular = 0;
		//if (!bBackfaceNormalFlip || normal.z < 0) N.set(normal);
		if (normal.z < 0) N.set(normal);
		else N.set(-normal.x, -normal.y, -normal.z);
		
		for (int l = 0; l < av3LightDirection.length; l++) {
			if (mp.diffuse != 0) {
				float diff = Math.max(av3TransformedLightDirection[l].dot(N) * afLightIntensity[l], 0) ;
				diffuse += (float)Math.pow(diff,mp.brilliance) * mp.diffuse;
			}
			if (abLightSpecular[l] && mp.specular != 0) {
				LV.sub(av3TransformedLightDirection[l],v3View);
				LV.scale(1f/(float)Math.sqrt(LV.dot(LV)));
				LV.normalize();
				float spec = Math.max(0,LV.dot(N));
				specular += (float)Math.pow(spec,1f/mp.roughness) * mp.specular;
			}
		}

		float red = Math.min(mp.red * diffuse + specular, 1) * 255;
		float green = Math.min(mp.green * diffuse + specular, 1) * 255;
		float blue = Math.min(mp.blue * diffuse + specular, 1) * 255;

		int color = 0xFF000000 | (((int)red) << 16) | (((int)green) << 8) | ((int)blue);
		return color;
	}
}

