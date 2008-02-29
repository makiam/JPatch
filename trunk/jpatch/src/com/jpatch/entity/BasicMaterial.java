package com.jpatch.entity;

import javax.vecmath.*;

public class BasicMaterial implements Material {
	public static final float DEFAULT_AMBIENT = 0.2f;
	public static final float DEFAULT_DIFFUSE = 0.8f;
	public static final float DEFAULT_SPECULAR = 1.0f;
	public static final float DEFAULT_EMISSION = 0.0f;
	public static final float DEFAULT_SHININESS = 100f;
	
	private final GlMaterial glMaterial;
	
	public BasicMaterial(Color3f color) {
		this(color, DEFAULT_AMBIENT, DEFAULT_DIFFUSE, DEFAULT_SPECULAR, 0, 1.0f / DEFAULT_SHININESS);
	}
	
	public BasicMaterial(Color3f color, float ambient, float diffuse, float specular, float metallic, float roughness) {
		this(new Color4f(color.x, color.y, color.z, 1.0f), ambient, diffuse, specular, metallic, roughness);
	}
	
	public BasicMaterial(Color4f color, float ambient, float diffuse, float specular, float metallic, float roughness) {
		Color4f ka = new Color4f(color);
		Color4f kd = new Color4f(color);
		Color4f ks = new Color4f(color);
		Color4f ke = new Color4f(color);
		ka.scale(ambient);
		kd.scale(diffuse);
		ks.interpolate(new Color4f(1.0f, 1.0f, 1.0f, 1.0f), 1.0f - metallic);
		ks.scale(specular);
		ke.scale(DEFAULT_EMISSION);
		glMaterial = new GlMaterial(ka, kd, ks, ke, 1.0f / roughness);
	}
	
	public BasicMaterial(final Color4f ka, final Color4f kd, final Color4f ks, final Color4f ke, final float shininess) {
		glMaterial = new GlMaterial(ka, kd, ks, ke, shininess);
	}
	
	public GlMaterial getGlMaterial() {
		return glMaterial;
	}

}
