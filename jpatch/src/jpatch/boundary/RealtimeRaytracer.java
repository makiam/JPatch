package jpatch.boundary;

import javax.vecmath.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import jpatch.entity.*;

public class RealtimeRaytracer extends JPanel
implements MouseListener, MouseMotionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final float SQRT2INV = 1f/(float)Math.sqrt(2);
	private static final int MAX_TRACE_LEVEL = 2;
	
	private final Vector3f v3Camera = new Vector3f(0,0,-5);
	private final Vector3f v3SphereCenter = new Vector3f(0,0,0);
	private final Vector3f v3Light = new Vector3f(1,-1,0);
	private final Color3f c3BackgroundColor = new Color3f(0.5f,0.5f,0.5f);
	private final float fSphereRadius = 1;
	private final float fSphereRadius2 = fSphereRadius * fSphereRadius;
	
	private MaterialProperties materialProperties;
	private Color3f c3Color = new Color3f();
	
	//private Color3f c3FilteredColor = new Color3f();
	//private Color3f c3TransmitColor = new Color3f();
	//private Color3f c3ShadowColor = new Color3f();
	//private Point3f p3Plane = new Point3f();
	//private Color3f c3Result = new Color3f();
	//private Vector3f V = new Vector3f();
	//private Vector3f v3Intersection = new Vector3f();
	//private Vector3f v3Normal = new Vector3f();
	//private Vector3f v3Reflection = new Vector3f();
	//private Vector3f v3Refraction = new Vector3f();
	//private Color3f c3ReflectionColor = new Color3f();
	
	private Matrix4f m4Matrix = new Matrix4f();
	private Matrix4f m4View = new Matrix4f();
	private Vector3f v3Cam = new Vector3f();
	private Vector3f v3Ray = new Vector3f();
	private Color3f c3Pixel = new Color3f();
	
	private float fIor;
	private float fIor2;
	private float fInvIor;
	private float fInvIor2;
	
	private int iMouseX;
	private int iMouseY;
	private float fRotX = -0.2f;
	private float fRotY = -3 * (float)Math.PI / 4;
	
	//private float fFactor;
	
	
	//private boolean bConserveEnergy = true;
	private boolean bLoRes = true;

	public RealtimeRaytracer(MaterialProperties materialProperties) {
		this.materialProperties = materialProperties;
		v3Light.normalize();
		addMouseListener(this);
		addMouseMotionListener(this);
		Dimension dim = new Dimension(160,160);
		setPreferredSize(dim);
		setMinimumSize(dim);
		setMaximumSize(dim);
	}
	
	public void setLoRes(boolean loRes) {
		bLoRes = loRes;
	}
	/*
	public void setConserveEnergy(boolean conserveEnergy) {
		bConserveEnergy = conserveEnergy;
	}
	*/
	private final void filterColor(Color3f color, Color3f filter) {
		color.x *= filter.x;
		color.y *= filter.y;
		color.z *= filter.z;
	}
	
	/*
	private final void filter(Color3f rayColor, Color3f objectColor) {
		c3FilteredColor.set(rayColor);
		c3FilteredColor.x *= objectColor.x * fFilter;
		c3FilteredColor.y *= objectColor.y * fFilter;
		c3FilteredColor.z *= objectColor.z * fFilter;
		c3TransmitColor.scale(materialProperties.transmit,rayColor);
		rayColor.scale(1 - materialProperties.transmit - materialProperties.filter, objectColor);
		rayColor.add(c3FilteredColor);
		rayColor.add(c3TransmitColor);
	}
	*/
	private final Color3f rayPlaneIntersection(Vector3f rayP, Vector3f rayD) {
		//System.out.println("rayPlaneIntersection(" + rayP + "," + rayD + ")");
		Color3f c3Result = new Color3f(c3BackgroundColor);
		float t = (fSphereRadius - rayP.y) / rayD.y;
		if (t > 0) {
			Point3f p3Plane = new Point3f(rayD);
			p3Plane.scale(t);
			p3Plane.add(rayP);
			if (((float)Math.floor(p3Plane.x * 2) + (float)Math.floor(p3Plane.z * 2)) % 2 == 0) {
				c3Result.set(1,1,1);
			} else {
				c3Result.set(0,0,0);
			}
			float xx = p3Plane.x * SQRT2INV + SQRT2INV;
			float xx2 = xx * xx;
			float z2 = p3Plane.z * p3Plane.z;
			//
			// test shadow
			//
			if ((xx2 + z2) < 1) {
				Color3f c3ShadowT = new Color3f(c3Result);
				c3ShadowT.scale(materialProperties.transmit * 0.7f);
				Color3f c3ShadowF = new Color3f(c3Result);
				c3ShadowF.x *= c3Color.x * materialProperties.filter * 0.7f;
				c3ShadowF.y *= c3Color.y * materialProperties.filter * 0.7f;
				c3ShadowF.z *= c3Color.z * materialProperties.filter * 0.7f;
				c3Result.scale(0.3f);
				c3Result.add(c3ShadowF);
				c3Result.add(c3ShadowT);
			}
			//
			//fog
			//
			c3Result.interpolate(c3BackgroundColor,1 - 1 / (p3Plane.x * p3Plane.x * 0.1f + z2 * 0.1f + 1));
		}
		return c3Result;
	}
	
	private final Color3f raySphereIntersection(Vector3f rayP, Vector3f rayD, int level, boolean inside) {
		//System.out.println("raySphereIntersection(" + rayP + "," + rayD + "," + level + ")");
		if (level++ > MAX_TRACE_LEVEL) {
			return new Color3f();
		}
		Color3f c3Result = new Color3f();
		Vector3f V = new Vector3f(rayP);
		V.sub(v3SphereCenter);
		float D2 = rayD.dot(rayD);
		float DV = rayD.dot(V);
		float V2 = V.dot(V);
		float discriminant = DV * DV - D2 * (V2 - fSphereRadius2);
		//
		// check if ray hit sphere
		//
		if (discriminant < 0) {
			//
			// sphere missed
			//
			return rayPlaneIntersection(rayP, rayD);
		} else {
			//
			//sphere hit
			//
			Vector3f v3Intersection = new Vector3f();
			Vector3f v3Normal = new Vector3f();
			float t1 = -DV+(float)Math.sqrt(discriminant);
			float t2 = -DV-(float)Math.sqrt(discriminant);
			float t;
			if (inside) {
				t = (t1 > t2) ? t1/D2 : t2/D2;
			} else {
				t = (t1 < t2) ? t1/D2 : t2/D2;
			}
			v3Intersection.scaleAdd(t,rayD,rayP);
			v3Normal.sub(v3Intersection,v3SphereCenter);
			v3Normal.normalize();
			if (inside) {
				v3Normal.scale(-1);
			}
			//
			//compute diffuse color
			//
			float diff = v3Normal.dot(v3Light);
			float diffuse = (diff > 0) ? (float)Math.pow(diff,materialProperties.brilliance) * materialProperties.diffuse + materialProperties.ambient : materialProperties.ambient;
			float fFactor = 1 - materialProperties.transmit - materialProperties.filter;
			c3Result.scale(diffuse * fFactor,c3Color);
			//
			//compute specular color;
			//
			Vector3f LV = new Vector3f(v3Light);
			LV.sub(rayD);
			LV.scale(1f/(float)Math.sqrt(LV.dot(LV)));
			LV.normalize();
			float spec = LV.dot(v3Normal);
			float specular = (spec > 0) ? (float)Math.pow(spec,1f/materialProperties.roughness) * materialProperties.specular : 0;
			Color3f c3Specular = new Color3f(1,1,1);
			c3Specular.interpolate(c3Color,materialProperties.metallic);
			c3Specular.scale(specular);
			
			c3Result.add(c3Specular);
			
			//
			//compute reflection
			//
			float reflectionAmount = 0;
			if (materialProperties.reflectionMax > 0 || materialProperties.reflectionMin > 0) {
				float variableReflection = 1 + rayD.dot(v3Normal);
				//float variableReflection = -rayD.dot(v3Normal);
				reflectionAmount = materialProperties.reflectionMin + (float)Math.pow(variableReflection,materialProperties.reflectionFalloff) * (materialProperties.reflectionMax - materialProperties.reflectionMin);
				//reflectionAmount = variableReflection;
				Vector3f v3Reflection = new Vector3f();
				//v3Reflection.scale(2 * v3Normal.dot(rayD),v3Normal);
				//v3Reflection.sub(V);
				//v3Reflection.normalize();
				v3Reflection.scale(-2 * rayD.dot(v3Normal),v3Normal);
				v3Reflection.add(rayD);
				Color3f c3ReflectionColor;
				if (inside) {
					c3ReflectionColor = raySphereIntersection(v3Intersection,v3Reflection,level,true);
				} else {
					c3ReflectionColor = rayPlaneIntersection(v3Intersection,v3Reflection);
				}
				Color3f c3ReflectionMetallic = new Color3f(c3ReflectionColor);
				filterColor(c3ReflectionMetallic,c3Color);
				c3ReflectionColor.interpolate(c3ReflectionMetallic,materialProperties.metallic);
				c3ReflectionColor.scale(reflectionAmount);
				c3Result.add(c3ReflectionColor);
			}
			//
			//transparency
			//
			float fTransmit;
			float fFilter;
			if (materialProperties.conserveEnergy) {
				fTransmit = materialProperties.transmit * (1 - reflectionAmount);
				fFilter = materialProperties.filter * (1 - reflectionAmount);
			} else {
				fTransmit = materialProperties.transmit;
				fFilter = materialProperties.filter;
			}
			if (fTransmit > 0 || fFilter > 0) {
				Vector3f v3Refraction = new Vector3f();
				if (materialProperties.refraction > 0) {
					v3Refraction = new Vector3f();
					if (inside) {
						//v3Normal.scale(-1);
					}
					float cosThetaI = -rayD.dot(v3Normal);
					float cosThetaR;
					float ior = (inside) ? fIor : fInvIor;
					float ior2 = (inside) ? fIor2 : fInvIor2;
					//System.out.println(ior);
					discriminant = 1 - ior2 * ( 1 - cosThetaI * cosThetaI);
					if (discriminant > 0) {
						cosThetaR = (float)Math.sqrt(discriminant);
						v3Refraction.set(rayD);
						v3Refraction.scale(ior);
						Vector3f dummy = new Vector3f(v3Normal);
						dummy.scale(ior * cosThetaI - cosThetaR);
						v3Refraction.add(dummy);
					} else {
						System.out.println(discriminant);
						System.exit(-1);
						v3Refraction.scale(-2 * rayD.dot(v3Normal),v3Normal);
						v3Refraction.add(rayD);
					}
					//v3Refraction.scale(-1);
				} else {
					v3Refraction.set(rayD);
				}
				Color3f c3Transmitted = new Color3f();
				if (inside) {
					c3Transmitted = rayPlaneIntersection(v3Intersection,v3Refraction);
				} else {
					c3Transmitted = raySphereIntersection(v3Intersection,v3Refraction,level,true);
				}
				Color3f c3Filtered = new Color3f(c3Color);
				c3Filtered.x *= c3Transmitted.x * fFilter;
				c3Filtered.y *= c3Transmitted.y * fFilter;
				c3Filtered.z *= c3Transmitted.z * fFilter;
				c3Transmitted.scale(fTransmit);
				c3Result.add(c3Transmitted);
				c3Result.add(c3Filtered);
				//System.out.println(c3Filtered);
			}
				
			return c3Result;
		}
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		v3Cam.set(v3Camera);
		m4View.rotY(fRotY);
		m4Matrix.rotX(fRotX);
		m4View.mul(m4Matrix);
		m4View.transform(v3Cam);
		fIor = materialProperties.refraction;
		fIor2 = fIor * fIor;
		fInvIor = 1 / fIor;
		fInvIor2 = fInvIor * fInvIor;
		c3Color.set(materialProperties.red,materialProperties.green,materialProperties.blue);
		if (bLoRes) {
			for (int Y = 0; Y < 80; Y++) {
				for (int X = 0; X < 80; X++) {
					float x = ((float)X - 40) / 150f;
					float y = ((float)Y - 40) / 150f;
					v3Ray.set(x,y,1);
					m4View.transform(v3Ray);
					v3Ray.normalize();
					c3Pixel = raySphereIntersection(v3Cam, v3Ray, 0, false);
					c3Pixel.clamp(0,1);
					g.setColor(c3Pixel.get());
					g.fillRect(X + X,Y + Y,2,2);
				}
			}
		} else {
			for (int Y = 0; Y < 160; Y++) {
				for (int X = 0; X < 160; X++) {
					float x = ((float)X - 80) / 300f;
					float y = ((float)Y - 80) / 300f;
					v3Ray.set(x,y,1);
					m4View.transform(v3Ray);
					v3Ray.normalize();
					c3Pixel = raySphereIntersection(v3Cam, v3Ray, 0, false);
					c3Pixel.clamp(0,1);
					g.setColor(c3Pixel.get());
					g.drawLine(X,Y,X,Y);
				}
			}
		}
	}
	
	public void mousePressed(MouseEvent event)
	{
		iMouseX = event.getX();
		iMouseY = event.getY();
	}

	public void mouseClicked(MouseEvent event){
	}
	
	public void mouseEntered(MouseEvent event){
	}
	
	public void mouseExited(MouseEvent event){
	}
	
	public void mouseReleased(MouseEvent event){
	}
					
	public void mouseDragged(MouseEvent event){
		int dx = event.getX() - iMouseX;
		int dy = event.getY() - iMouseY;
		iMouseX = event.getX();
		iMouseY = event.getY();			
		fRotX -= (float)dy/150f;
		fRotY += (float)dx/150f;
		fRotX = (float)Math.min(0.15f,fRotX);
		fRotX = (float)Math.max(-Math.PI/2,fRotX);
		repaint();
	}

	public void mouseMoved(MouseEvent event) {
	}
}
			
