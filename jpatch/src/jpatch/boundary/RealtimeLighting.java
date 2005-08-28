/*
 * $Id: RealtimeLighting.java,v 1.4 2005/08/28 19:05:39 sascha_l Exp $
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package jpatch.boundary;

import javax.vecmath.*;
import jpatch.entity.*;

/**
 * This class holds information needed for realtime lighing (a list of lightsources and some global settings)
 * @author sascha
 * @version $$Revision: 1.4 $$
 */
public class RealtimeLighting {
	
	/** The global ambient color */
	private Color3f c3Ambient = new Color3f(1, 1, 1);
	/** The eye or camera position */
	private Point3f p3EyePosition = new Point3f(0, 0, 0);
	/** The viewing direction */
	private Vector3f v3ViewDirection = new Vector3f(0, 0, 1);
	/** True if the eye/camera position should be used for specular lighting calculations */
	private boolean bLocalViewer;
	/** An array holding all lightsources */
	private LightSource[] aLightSources;
	/** The current light number */
	private int iLightNum = 0;
	
	/**
	 * @param numberOfLightsources The number of lightsources
	 */
	public RealtimeLighting(int numberOfLightsources) {
		aLightSources = new LightSource[numberOfLightsources];
	}
	
	/**
	 * A factory method that returns a three point light
	 * @return
	 */
	public static RealtimeLighting createTestLight() {
		RealtimeLighting rtl = new RealtimeLighting(1);
//		rtl.add(rtl.new DirectionalLight(new Color3f (1.00f, 0.00f, 0.00f), true, true, new Vector3f( 0, 1, 0)));
		rtl.add(rtl.new PointLight(new Color3f (1.00f, 0.00f, 0.00f), true, true, new Point3f(-70,0,0), 2, 70));
//		rtl.add(rtl.new SpotLight(new Color3f (0.00f, 1.00f, 0.00f), true, true, new Point3f(-60,0,0), 0, 1, new Vector3f(1,0,1),30,30));
//		rtl.add(rtl.new SpotLight(new Color3f (0.00f, 0.00f, 1.00f), true, true, new Point3f(-50,0,0), 0, 1, new Vector3f(1,0,1),30,30));
		return rtl;
	}
	
	/**
	 * A factory method that returns a headlight
	 * @return
	 */
	public static RealtimeLighting createSimpleLight() {
		RealtimeLighting rtl = new RealtimeLighting(1);
		rtl.add(rtl.new DirectionalLight(new Color3f (1.00f, 1.00f, 1.00f), true, false, new Vector3f( -1, 1,-1)));
		return rtl;
	}
	
	/**
	 * A factory method that returns a headlight
	 * @return
	 */
	public static RealtimeLighting createHeadLight() {
		RealtimeLighting rtl = new RealtimeLighting(1);
		rtl.add(rtl.new DirectionalLight(new Color3f (1.00f, 1.00f, 1.00f), true, true, new Vector3f( 0, 0,-1)));
		return rtl;
	}
	
	/**
	 * A factory method that returns a three point light
	 * @return
	 */
	public static RealtimeLighting createThreepointLight() {
		RealtimeLighting rtl = new RealtimeLighting(3);
		rtl.add(rtl.new DirectionalLight(new Color3f (0.80f, 0.80f, 0.80f), true, true, new Vector3f( -1,  1, -1)));	// key
		rtl.add(rtl.new DirectionalLight(new Color3f (0.40f, 0.40f, 0.40f), true, true, new Vector3f(  1,  0, -1)));	// fill
		rtl.add(rtl.new DirectionalLight(new Color3f (1.20f, 1.20f, 1.20f), true, true, new Vector3f(  0,  1,  1)));	// back
		return rtl;
	}
	
	public static RealtimeLighting createAnimLight(AnimLight[] lights) {
		RealtimeLighting rtl = new RealtimeLighting(lights.length);
		Point3f pos = new Point3f();
		Color3f color = new Color3f();
		for (int i = 0; i < lights.length; i++) {
			pos.set(lights[i].getPosition());
			color.set(lights[i].getColor());
			color.scale(lights[i].getIntensity());
			rtl.add(rtl.new PointLight(color, true, true, pos, 0, 1));
		}
		return rtl;
	}
	
	/**
	 * Adds a lightsource
	 * @param lightSource The lightsource to add
	 */
	public void add(LightSource lightSource) {
		aLightSources[iLightNum++] = lightSource;
	}
	
	public LightSource getLight(int num) {
		return aLightSources[num];
	}
	
	public int numLights() {
		return aLightSources.length;
	}
	
	public Color3f getAmbientColor() {
		return c3Ambient;
	}
	
	/**
	 * Sets the global ambient color
	 * @param ambientColor The new abient color value
	 */
	public void setAmbientColor(Color3f ambientColor) {
		c3Ambient = ambientColor;
	}
	
	/**
	 * Enables/Disable local lighting model
	 * @param enable
	 */
	public void setLocalViewer(boolean enable) {
		bLocalViewer = enable;
	}
	
	/**
	 * Sets the camera transformation
	 * @param matrix The transformation matrix
	 */
	public void transform(Matrix4f matrix) {
		p3EyePosition.set(0, 0, 0);
		v3ViewDirection.set(0, 0, 1);
		matrix.transform(p3EyePosition);
		matrix.transform(v3ViewDirection);
		for (int i = 0; i < aLightSources.length; i++) {
			aLightSources[i].transform(matrix);
		}
	}
	
	/**
	 * Computes shading
	 * @param point The point to shade
	 * @param normal The normal vector
	 * @param mp The material properties
	 * @param color A reference to the color object in which the computed color value will be stored
	 */
	public void shade(Point3f point, Vector3f normal, MaterialProperties mp, Color3f color) {
		color.set(mp.red * mp.ambient * c3Ambient.x, mp.green * mp.ambient * c3Ambient.y, mp.blue * mp.ambient * c3Ambient.z);
		for (int i = 0; i < aLightSources.length; i++) {
			aLightSources[i].shade(point, normal, mp, color);
		}
		color.clamp(0, 1);
	}
	
	/**
	 * The base class for all lightsources
	 * @author sascha
	 */
	public static abstract class LightSource {
		/** The color of the light */
		Color3f c3Color;
		/** cast highlights flag*/
		boolean bHighlight;
		/** cast shadows flag */
		boolean bShadow;
		
		/**
		 * Non public constructor
		 * @param color The color of the light
		 * @param highlight cast highlights flag
		 * @param shadow cast shadows flag
		 */
		LightSource(Color3f color, boolean highlight, boolean shadow) {
			c3Color = color;
			bHighlight = highlight;
			bShadow = shadow;
		}
		
		/**
		 * Computes shading
		 * @param point The point to shade
		 * @param normal The normal vector
		 * @param mp The material properties
		 * @param color A reference to the color object in which the computed color value will be stored
		 */
		public abstract void shade(Point3f point, Vector3f normal, MaterialProperties mp, Color3f color);
		
		/**
		 * Applies camera transformation
		 * @param matrix transformation matrix
		 */
		public abstract void transform(Matrix4f matrix);
		
		/**
		 * @return The color of the light
		 */
		public Color3f getColor() {
			return c3Color;
		}
		
		/**
		 * @return true of this lightsource casts highlights
		 */
		public boolean castsHighlight() {
			return bHighlight;
		}
		
		/**
		 * @return true if this lightsource casts shadows
		 */
		public boolean castsShadow() {
			return bShadow;
		}
	}
	
	/**
	 * A directional (infinitly distant) lightsource
	 * @author sascha
	 */
	public class DirectionalLight extends LightSource {
		/** The world space direction */
		private Vector3f v3Direction = new Vector3f();
		/** The camera space direction */
		private Vector3f v3TransformedDirection = new Vector3f();
		/** Dummy field */
		private Vector3f N = new Vector3f();
		/** Dummy field */
		private Vector3f LV = new Vector3f();
		/** Dummy field */
		private Vector3f V = new Vector3f();
		
		/**
		 * @param color The color of the light
		 * @param highlight cast highlights flag
		 * @param shadow cast shadows flag
		 * @param direction The (normalized!) direction TO the lightsource.
		 */
		public DirectionalLight(Color3f color, boolean highlight, boolean shadow, Vector3f direction) {
			super(color, highlight, shadow);
			v3Direction.set(direction);
			v3Direction.normalize();
			v3TransformedDirection.set(v3Direction);
		}
		
		/**
		 * @return direction vector to lightsource
		 */
		public Vector3f getDirection() {
			return v3Direction;
		}
		
		/**
		 * @return transformeddirection vector to lightsource
		 */
		public Vector3f getTransformedDirection() {
			return v3TransformedDirection;
		}
		
		public void transform(Matrix4f matrix) {
			v3TransformedDirection.set(v3Direction);
			matrix.transform(v3TransformedDirection);
		}
		
		public void shade(Point3f point, Vector3f normal, MaterialProperties mp, Color3f color) {
			if (normal.z < 0) N.set(normal);
			else N.set(-normal.x, -normal.y, -normal.z);
			if (mp.diffuse != 0) {
				float diffuse = v3TransformedDirection.dot(N);
				if (diffuse > 0) {
					diffuse *= mp.diffuse;
					color.x += mp.red * c3Color.x * diffuse;
					color.y += mp.green * c3Color.y * diffuse;
					color.z += mp.blue * c3Color.z * diffuse;
				}
			}
			if (bHighlight && (mp.specular != 0)) {
				if (bLocalViewer) {
					V.sub(point, p3EyePosition);
					V.normalize();
					LV.sub(v3TransformedDirection, V);
				} else {
					LV.sub(v3TransformedDirection, v3ViewDirection);
				}
				LV.scale(1f / (float) Math.sqrt(LV.dot(LV)));
				LV.normalize();
				float spec = LV.dot(N);
				if (spec > 0) {
					float specular = (float)Math.pow(spec,1f/mp.roughness) * mp.specular;
					color.x += ((1 - mp.metallic) + mp.metallic * mp.red) * c3Color.x * specular;
					color.y += ((1 - mp.metallic) + mp.metallic * mp.green) * c3Color.y * specular;
					color.z += ((1 - mp.metallic) + mp.metallic * mp.blue) * c3Color.z * specular;
				}
			}
		}
	}
	
	/**
	 * A pointlight
	 * @author sascha
	 */
	public class PointLight extends LightSource {
		/** world space position of the lightsource */
		private Point3f p3Position = new Point3f();
		/** camera space position of the lightsource */
		private Point3f p3TransformedPosition = new Point3f();
		/** 0 = no attenuation, 1 = linear (1/x) attenuation, 2 = quadratic (1/x²) attenuation */
		private int iAttenuation;
		/** The distance at which the intensity equals the specified color value */
		private float fDistance;
		/**  */
		private float fDistanceSquared;
		
		/**  */
		private Vector3f N = new Vector3f();
		/**  */
		private Vector3f LV = new Vector3f();
		/**  */
		private Vector3f V = new Vector3f();
		/**  */
		private Vector3f D = new Vector3f();
		/**  */
		private Color3f C = new Color3f();
		
		/**
		 * @param color The color of the light
		 * @param highlight Cast highlights flag
		 * @param shadow Cast shadows flag
		 * @param position The position of the lightsource
		 * @param attenuation 0 = no attenuation, 1 = linear (1/x) attenuation, 2 = quadratic (1/x²) attenuation
		 * @param distance The distance at which the intensity equals the specified color value
		 */
		public PointLight(Color3f color, boolean highlight, boolean shadow, Point3f position, int attenuation, float distance) {
			super(color, highlight, shadow);
			p3Position.set(position);
			p3TransformedPosition.set(p3Position);
			iAttenuation = attenuation;
			fDistance = distance;
			fDistanceSquared = distance * distance;
		}
		
		/**
		 * @return The position of the lightsource
		 */
		public Point3f getPosition() {
			return p3Position;
		}
		
		/**
		 * @return transformed position of the lightsource
		 */
		public Point3f getTransformedPosition() {
			return p3TransformedPosition;
		}
		
		/**
		 * @return The attenuation (0 = no attenuation, 1 = linear (1/x) attenuation, 2 = quadratic (1/x²) attenuation)
		 */
		public int getAttenuation() {
			return iAttenuation;
		}
		
		/**
		 * @return The distance at which the intensity equals the specified color value
		 */
		public float getDistance() {
			return fDistance;
		}
		
		public void transform(Matrix4f matrix) {
			p3TransformedPosition.set(p3Position);
			matrix.transform(p3TransformedPosition);
		}
		
		public void shade(Point3f point, Vector3f normal, MaterialProperties mp, Color3f color) {
			if (normal.z < 0) N.set(normal);
			else N.set(-normal.x, -normal.y, -normal.z);
			
			D.sub(p3Position, point);
			D.normalize();
			
			C.set(c3Color);
			switch (iAttenuation) {
				case 1: {
					float dist = p3TransformedPosition.distance(point);
					if (dist > 0) C.scale(fDistance / dist);
				} break;
				case 2: {
					float dist = p3TransformedPosition.distanceSquared(point);
					if (dist > 0) C.scale(fDistanceSquared / dist);
				} break;
			}
			
			if (mp.diffuse != 0) {
				float diffuse = D.dot(N);
				if (diffuse > 0) {
					diffuse *= mp.diffuse;
					color.x += mp.red * C.x * diffuse;
					color.y += mp.green * C.y * diffuse;
					color.z += mp.blue * C.z * diffuse;
				}
			}
			
			if (bHighlight && (mp.specular != 0)) {
				if (bLocalViewer) {
					V.sub(point, p3EyePosition);
					V.normalize();
					LV.sub(D, V);
				} else {
					LV.sub(D, v3ViewDirection);
				}
				LV.scale(1f / (float) Math.sqrt(LV.dot(LV)));
				LV.normalize();
				float spec = LV.dot(N);
				if (spec > 0) {
					float specular = (float)Math.pow(spec,1f/mp.roughness) * mp.specular;
					color.x += ((1 - mp.metallic) + mp.metallic * mp.red) * C.x * specular;
					color.y += ((1 - mp.metallic) + mp.metallic * mp.green) * C.y * specular;
					color.z += ((1 - mp.metallic) + mp.metallic * mp.blue) * C.z * specular;
				}
			}
		}
	}
	
	/**
	 * A spotlight
	 * @author sascha
	 */
	public class SpotLight extends LightSource {
		/** world space direction of spot */
		private Vector3f v3Direction = new Vector3f();
		/** camera space direction of spot */
		private Vector3f v3TransformedDirection = new Vector3f();
		/** world space position of the lightsource */
		private Point3f p3Position = new Point3f();
		/** camera space position of the lightsource */
		private Point3f p3TransformedPosition = new Point3f();
		/** 0 = no attenuation, 1 = linear (1/x) attenuation, 2 = quadratic (1/x²) attenuation */
		private int iAttenuation;
		/** The distance at which the intensity equals the specified color value */
		private float fDistance;
		/**  */
		private float fDistanceSquared;
		/** Cosinus of spot radius */
		private float fCosRadius;
		/** Cosinus of spot falloff */
		private float fCosFalloff;
		/**  */
		private Vector3f N = new Vector3f();
		/**  */
		private Vector3f LV = new Vector3f();
		/**  */
		private Vector3f V = new Vector3f();
		/**  */
		private Vector3f D = new Vector3f();
		/**  */
		private Color3f C = new Color3f();
		
		/**
		 * @param color The color of the light
		 * @param highlight Cast highlights flag
		 * @param shadow Cast shadows flag
		 * @param position The position of the lightsource
		 * @param attenuation 0 = no attenuation, 1 = linear (1/x) attenuation, 2 = quadratic (1/x²) attenuation
		 * @param distance The distance at which the intensity equals the specified color value
		 * @param direction The direction of the spot
		 * @param radius The radius of the spot (in degrees)
		 * @param falloff The falloff of the spot (in degrees, must be greater than radius or 0)
		 */
		public SpotLight(Color3f color, boolean highlight, boolean shadow, Point3f position, int attenuation, float distance, Vector3f direction, float radius, float falloff) {
			super(color, highlight, shadow);
			if (falloff == 0) falloff = radius;
			if (radius < 0 || radius > 90) throw new IllegalArgumentException("Radius must be between 0 and 180 degrees");
			if (falloff < 0 || falloff > 90) throw new IllegalArgumentException("Falloff must be between 0 and 180 degrees");
			if (falloff < radius) throw new IllegalArgumentException("Falloff must be greater than radius");
			p3Position.set(position);
			p3TransformedPosition.set(p3Position);
			iAttenuation = attenuation;
			fDistance = distance;
			fDistanceSquared = distance * distance;
			v3Direction.set(direction);
			v3Direction.normalize();
			v3TransformedDirection.set(v3Direction);
			fCosRadius = (float) Math.cos(radius / 180 * Math.PI);
			fCosFalloff = (float) Math.cos(falloff / 180 * Math.PI);
		}
		
		/**
		 * @return The position of the lightsource
		 */
		public Point3f getPosition() {
			return p3Position;
		}
		
		/**
		 * @return transformed position of the lightsource
		 */
		public Point3f getTransformedPosition() {
			return p3TransformedPosition;
		}
		
		/**
		 * @return The attenuation (0 = no attenuation, 1 = linear (1/x) attenuation, 2 = quadratic (1/x²) attenuation)
		 */
		public int getAttenuation() {
			return iAttenuation;
		}
		
		/**
		 * @return The distance at which the intensity equals the specified color value
		 */
		public float getDistance() {
			return fDistance;
		}
		
		/**
		 * @return The direction of the spot
		 */
		public Vector3f getDirection() {
			return v3Direction;
		}
		
		/**
		 * @return transformed direction of the spot
		 */
		public Vector3f getTransformedDirection() {
			return v3TransformedDirection;
		}
		
		/**
		 * @return The radius of the spot (in degrees)
		 */
		public float getRadius() {
			return (float) (Math.acos(fCosRadius) * 180 / Math.PI);
		}
		
		/**
		 * @return The falloff of the spot (in degrees)
		 */
		public float getFalloff() {
			return (float) (Math.acos(fCosFalloff) * 180 / Math.PI);
		}
		
		public void transform(Matrix4f matrix) {
			p3TransformedPosition.set(p3Position);
			matrix.transform(p3TransformedPosition);
			v3TransformedDirection.set(v3Direction);
			matrix.transform(v3TransformedDirection);
		}
		
		public void shade(Point3f point, Vector3f normal, MaterialProperties mp, Color3f color) {
			if (normal.z < 0) N.set(normal);
			else N.set(-normal.x, -normal.y, -normal.z);
			
			D.sub(p3Position, point);
			D.normalize();
			
			float cosAngle = -D.dot(v3TransformedDirection);
			//System.out.println(cosAngle + " " + fCosRadius + " " + fCosFalloff);
			float spot = (cosAngle > fCosRadius) ? 1 :
						 (cosAngle < fCosFalloff) ? 0 :
						 (cosAngle - fCosFalloff) / (fCosRadius - fCosFalloff);
			
			C.set(c3Color);
			switch (iAttenuation) {
				case 0: {
					C.scale(spot);
				} break;
				case 1: {
					float dist = p3TransformedPosition.distance(point);
					if (dist > 0) C.scale(fDistance / dist * spot);
				} break;
				case 2: {
					float dist = p3TransformedPosition.distanceSquared(point);
					if (dist > 0) C.scale(fDistanceSquared / dist * spot);
				} break;
			}
			
			if (mp.diffuse != 0) {
				float diffuse = D.dot(N);
				if (diffuse > 0) {
					diffuse *= mp.diffuse;
					color.x += mp.red * C.x * diffuse;
					color.y += mp.green * C.y * diffuse;
					color.z += mp.blue * C.z * diffuse;
				}
			}
			
			if (bHighlight && (mp.specular != 0)) {
				if (bLocalViewer) {
					V.sub(point, p3EyePosition);
					V.normalize();
					LV.sub(D, V);
				} else {
					LV.sub(D, v3ViewDirection);
				}
				LV.scale(1f / (float) Math.sqrt(LV.dot(LV)));
				LV.normalize();
				float spec = LV.dot(N);
				if (spec > 0) {
					float specular = (float)Math.pow(spec,1f/mp.roughness) * mp.specular;
					color.x += ((1 - mp.metallic) + mp.metallic * mp.red) * C.x * specular;
					color.y += ((1 - mp.metallic) + mp.metallic * mp.green) * C.y * specular;
					color.z += ((1 - mp.metallic) + mp.metallic * mp.blue) * C.z * specular;
				}
			}
		}
	}
}
