package com.jpatch.boundary.tools;

import java.awt.event.*;
import java.util.Arrays;

import com.jpatch.afw.attributes.Tuple3Attr;
import com.jpatch.afw.vecmath.Rotation3d;
import com.jpatch.afw.vecmath.Utils3d;
import com.jpatch.boundary.Main;
import com.jpatch.boundary.Viewport;
import com.jpatch.boundary.ViewportGl;
import com.jpatch.entity.BasicMaterial;
import com.jpatch.entity.GlMaterial;
import com.jpatch.settings.ColorSettings;
import com.jpatch.settings.Settings;

import javax.media.opengl.GL;
import javax.vecmath.*;

import jpatch.auxilary.Utils3D;

import static javax.media.opengl.GL.*;

public class RotateTool implements JPatchTool {
	private static final int SEGMENTS = 128;
	private final ColorSettings colorSettings = Settings.getInstance().colors;
	private final Point3d[][] points = new Point3d[3][SEGMENTS + 1];
	private final static GlMaterial FRONT_MATERIAL, BACK_MATERIAL;
	static int n = 0;
	
	private final Tuple3Attr pivotAttr = new Tuple3Attr();
	private final Tuple3Attr axisRotationAttr = new Tuple3Attr();
	private final Tuple3Attr rotationAttr = new Tuple3Attr();
	private final Point3d pivot = new Point3d();
	private final Rotation3d axisRotation = new Rotation3d();
	private final Rotation3d rotation = new Rotation3d();
	private double radius = 1.0;
	private Matrix3d matrix = new Matrix3d();
	private MouseListener[] mouseListeners;
	
	private Point3d hitPoint = new Point3d();
	
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
		pivotAttr.bindTuple(pivot);
		axisRotationAttr.bindTuple(axisRotation);
		rotationAttr.bindTuple(rotation);
	}
	
	public void draw(Viewport viewport) {
		rotation.getRotationMatrix(matrix);
		axisRotation.rotateMatrix(matrix);
		
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
	
		Point3d pivot = (Point3d) pivotAttr.getTuple(new Point3d());

		for (int pass = 0; pass < 2; pass++) {
			if (pass == 0) {
				gl.glLineWidth(3f);
			} else {
				gl.glLineWidth(2f);
			}
			for (int i = 0; i < 3; i++) {
				gl.glBegin(GL_LINE_STRIP);
				for (int j = 0; j <= SEGMENTS; j++) {
					p.set(points[i][(j < SEGMENTS) ? j : 0]);
					p.scale(radius);
					p.add(pivot);
					matrix.transform(p);
					m.transform(p);
					float alpha = (float) Math.min(1.0, (p.z - z) * scale + 0.6);
					if (pass == 0) {
						gl.glColor4f(0, 0, 0, alpha);
					} else {
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
					}
					gl.glVertex3d(p.x, p.y, p.z);
				}
				gl.glEnd();
			}
		}
		gl.glColor3f(1, 1, 1);
		gl.glBegin(GL_LINES);
		p.set(pivot);
		m.transform(p);
		gl.glVertex3d(p.x, p.y, p.z);
		p.set(hitPoint);
		m.transform(p);
		gl.glVertex3d(p.x, p.y, p.z);
		gl.glEnd();
		
		gl.glDisable(GL_BLEND);
		gl.glDisable(GL_LINE_SMOOTH);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_LIGHTING);
		gl.glLineWidth(1.0f);
	}
	
	public void registerListeners(Viewport[] viewports) {
		if (mouseListeners != null) {
			throw new IllegalStateException("already registered");
		}
		mouseListeners = new MouseListener[viewports.length];
		for (int i = 0; i < viewports.length; i++) {
			mouseListeners[i] = new HitMouseListener(viewports[i]);
			viewports[i].getComponent().addMouseListener(mouseListeners[i]);
		}
	}
	
	public void unregisterListeners(Viewport[] viewports) {
		System.out.println("MoveVertexTool unregisterListeners");
		for (int i = 0; i < viewports.length; i++) {
			viewports[i].getComponent().removeMouseListener(mouseListeners[i]);
		}
		mouseListeners = null;
	}

	public Tuple3Attr getAxisRotationAttribute() {
		return axisRotationAttr;
	}

	public Tuple3Attr getPivotAttribute() {
		return pivotAttr;
	}

	public Tuple3Attr getRotationAttribute() {
		return rotationAttr;
	}
	
	private class HitMouseListener extends MouseAdapter {
		final Viewport viewport;
		
		private HitMouseListener(Viewport viewport) {
			this.viewport = viewport;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			Point3d rayOrigin = new Point3d(
					e.getX() - viewport.getComponent().getWidth() / 2,
					viewport.getComponent().getHeight() / 2 - e.getY(),
					1
			);
			Vector3d rayDirection = new Vector3d(0, 0, -1);
			
			Matrix4d m = viewport.getViewDef().getInverseMatrix(new Matrix4d());
			m.transform(rayOrigin);
			m.transform(rayDirection);
			
			System.out.println(Utils3d.raySphereIntersection(rayOrigin, rayDirection, pivot, radius, hitPoint, true));
			Main.getInstance().repaintViewport(viewport);
		}
		
	};

}
