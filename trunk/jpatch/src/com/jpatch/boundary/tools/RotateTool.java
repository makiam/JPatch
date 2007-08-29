package com.jpatch.boundary.tools;

import java.util.Arrays;

import com.jpatch.boundary.Viewport;
import com.jpatch.boundary.ViewportGl;
import com.jpatch.entity.BasicMaterial;
import com.jpatch.entity.GlMaterial;
import com.jpatch.settings.ColorSettings;
import com.jpatch.settings.Settings;

import javax.media.opengl.GL;
import javax.vecmath.*;

import static javax.media.opengl.GL.*;

public class RotateTool implements JPatchTool {
	private static final int SEGMENTS = 72;
	private final ColorSettings colorSettings = Settings.getInstance().colors;
	private final Point3d[][] points = new Point3d[3][SEGMENTS + 1];
	private final static GlMaterial FRONT_MATERIAL, BACK_MATERIAL;
	static int n = 0;
	
	static {
		Color3f black = new Color3f(0, 0, 0);
		FRONT_MATERIAL = new GlMaterial(black, black, black, black, 0);
		BACK_MATERIAL = new GlMaterial(black, black, black, black, 0);
	}
	
	public RotateTool() {
		for (int i = 0; i < SEGMENTS; i++) {
			double sin = Math.sin(2 * Math.PI / SEGMENTS * i);
			double cos = Math.cos(2 * Math.PI / SEGMENTS * i);
			points[0][i] = new Point3d(0, -sin, cos);
			points[1][i] = new Point3d(cos, 0, sin);
			points[2][i] = new Point3d(cos, -sin, 0);
		}
	}
	
	public void draw(Viewport viewport) {
		ViewportGl glViewport = (ViewportGl) viewport;
		GL gl = glViewport.getGl();
		gl.glEnable(GL_BLEND);
		gl.glEnable(GL_LINE_SMOOTH);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDisable(GL_LIGHTING);
		
//		gl.glDisable(GL_COLOR_MATERIAL);
//		BasicMaterial mat1 = new BasicMaterial(new Color3f(0, 0, 0));
//		BasicMaterial mat2 = new BasicMaterial(new Color3f(0, 0, 0));
//		glViewport.setMaterial(GL_FRONT, FRONT_MATERIAL.getArray());
//		glViewport.setMaterial(GL_BACK, FRONT_MATERIAL.getArray());
//		gl.glColorMaterial(GL_FRONT_AND_BACK, GL_DIFFUSE);
//		gl.glEnable(GL_COLOR_MATERIAL);
		
//		gl.glMaterialfv(GL_FRONT, GL_EMISSION, new float[] { 0, 0, 0, 1 }, 0);
//		gl.glMaterialfv(GL_FRONT, GL_AMBIENT, new float[] { 0, 0, 0, 1 }, 0);
//		gl.glMaterialfv(GL_FRONT, GL_DIFFUSE, new float[] { 0, 0, 0, 1 }, 0);
//		gl.glMaterialfv(GL_FRONT, GL_SPECULAR, new float[] { 0, 0, 0, 1 }, 0);
//		gl.glMaterialf(GL_FRONT, GL_SHININESS, 0);
		
		
//		System.out.println("drawing rotateTool " + n++);
		Point3d p = new Point3d();
		Vector3d v = new Vector3d();
		Matrix4d m = viewport.getViewDef().getMatrix(new Matrix4d());
		final double scale = 3.0 / (float) m.getScale();
		m.transform(p);
		double z = p.z;
	
		gl.glLineWidth(4.0f);
		
		for (int i = 0; i < 3; i++) {
			gl.glBegin(GL_LINE_STRIP);
			for (int j = 0; j <= SEGMENTS; j++) {
				p.set(points[i][(j < SEGMENTS) ? j : 0]);
				m.transform(p);
				float alpha = (float) Math.max(0.02, Math.min(1.0, (p.z - z) * scale + 0.5));
				gl.glColor4f(0, 0, 0, alpha);
				gl.glVertex3d(p.x, p.y, p.z);
			}
			gl.glEnd();
		}

		gl.glLineWidth(2.0f);
		for (int i = 0; i < 3; i++) {
			
			gl.glBegin(GL_LINE_STRIP);
			for (int j = 0; j <= SEGMENTS; j++) {
				p.set(points[i][(j < SEGMENTS) ? j : 0]);
//				v.set(points[i][(j < SEGMENTS) ? j : 0]);
				m.transform(p);
//				m.transform(v);
//				v.normalize();
				float alpha = (float) Math.min(1.0, (p.z - z) * scale + 0.6);
				
				switch (i) {
				case 0:
					gl.glColor4f(colorSettings.xAxis.x, colorSettings.xAxis.y, colorSettings.xAxis.z, alpha);
					break;
				case 1:
					gl.glColor4f(colorSettings.yAxis.x, colorSettings.yAxis.y, colorSettings.yAxis.z, alpha);
					break;
				case 2:
					gl.glColor4f(colorSettings.zAxis.x, colorSettings.zAxis.y, colorSettings.zAxis.z, alpha);
					break;
				}
				
				gl.glVertex3d(p.x, p.y, p.z);
			}
			gl.glEnd();
		}
		gl.glDisable(GL_BLEND);
		gl.glDisable(GL_LINE_SMOOTH);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_LIGHTING);
		gl.glLineWidth(1.0f);
	}
	
	public void registerListeners(Viewport[] viewports) {
		// TODO Auto-generated method stub
		
	}
	
	public void unregisterListeners(Viewport[] viewports) {
		// TODO Auto-generated method stub
		
	}

}
