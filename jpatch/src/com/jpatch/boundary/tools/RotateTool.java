package com.jpatch.boundary.tools;

import java.awt.event.*;
import java.awt.geom.Line2D;
import java.util.Arrays;

import com.jpatch.afw.attributes.Tuple3Attr;
import com.jpatch.afw.vecmath.Rotation3d;
import com.jpatch.afw.vecmath.Utils3d;
import com.jpatch.boundary.Main;
import com.jpatch.boundary.OrthoViewDef;
import com.jpatch.boundary.PerspectiveViewDef;
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
	private double radius = 10.0;
	private Matrix3d matrix = new Matrix3d();
	private MouseListener[] mouseListeners;
	private MouseMotionAdapter mouseMotionListener;
	
	private Vector3d fromVector = new Vector3d();
	private Vector3d toVector = new Vector3d();
	
	
	
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
		final double scale = 1.0 / (float) m.getScale() / radius;
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
					float alpha = (float) Math.max(0.2, Math.min(0.9, (p.z - z) * scale + 0.6));
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
		gl.glLineWidth(1.0f);
		gl.glDisable(GL_CULL_FACE);
		
		for (int pass = 0; pass < 2; pass++) {
			if (pass == 0) {
				gl.glColor4f(1, 1, 1, 0.25f);
				gl.glBegin(GL_TRIANGLE_FAN);
			} else {
				gl.glEnd();
				gl.glColor4f(1, 1, 1, 0.75f);
				gl.glBegin(GL_LINE_STRIP);
			}
			p.set(pivot);
			m.transform(p);
			gl.glVertex3d(p.x, p.y, p.z);
			Vector3d axis = new Vector3d();
			axis.cross(fromVector, toVector);
			double angle = Math.acos(fromVector.dot(toVector));
			Matrix3d ma = new Matrix3d();
			AxisAngle4d axisAngle = new AxisAngle4d(axis, 0);
			Vector3d ve = new Vector3d();
			for (double a = 0; a < angle; a += Math.PI / 64.0) {
				axisAngle.angle = a;
				ma.set(axisAngle);
				ve.set(fromVector);
				ma.transform(ve);
				ve.scale(radius);
				p.set(pivot);
				p.add(ve);
				m.transform(p);
				gl.glVertex3d(p.x, p.y, p.z);
			}
			p.set(pivot);
			ve.set(toVector);
			ve.scale(radius);
			p.add(ve);
			m.transform(p);
			gl.glVertex3d(p.x, p.y, p.z);
		}
		p.set(pivot);
		m.transform(p);
		gl.glVertex3d(p.x, p.y, p.z);
		gl.glEnd();
//		p.set(pivot);
//		p.add(fromVector);
//		m.transform(p);
//		gl.glVertex3d(p.x, p.y, p.z);
//		p.set(pivot);
//		m.transform(p);
//		gl.glVertex3d(p.x, p.y, p.z);
//		p.set(pivot);
//		p.add(toVector);
//		m.transform(p);
//		gl.glVertex3d(p.x, p.y, p.z);
		
		gl.glDisable(GL_BLEND);
		gl.glDisable(GL_LINE_SMOOTH);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_LIGHTING);
		gl.glEnable(GL_CULL_FACE);
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
	
	private int getHitCircle(Viewport viewport, int mouseX, int mouseY) {
		axisRotation.getRotationMatrix(matrix);
		rotation.rotateMatrix(matrix);
		double x = mouseX - viewport.getComponent().getWidth() * 0.5;
		double y = viewport.getComponent().getHeight() * 0.5 - mouseY;
		int hit = -1;
		double distSqMin = 64;
		Point3d p = new Point3d();
		Line2D.Double line = new Line2D.Double();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j <= SEGMENTS; j++) {
				p.set(points[i][j % SEGMENTS]);
				p.scale(radius);
				p.add(pivot);
				matrix.transform(p);
				viewport.getViewDef().transform(p);
				line.x1 = p.x;
				line.y1 = p.y;
				p.set(points[i][(j + 1) % SEGMENTS]);
				p.scale(radius);
				p.add(pivot);
				matrix.transform(p);
				viewport.getViewDef().transform(p);
				line.x2 = p.x;
				line.y2 = p.y;
				
				double distSq = line.ptSegDistSq(x, y);
//				System.out.println(line.x1 + "/" + line.y1 + " - " + line.x2 + "/" + line.y2 + " \t " + Math.sqrt(distSq));
				if (distSq < distSqMin) {
					distSqMin = distSq;
					hit = i;
				}
			}
		}
		return hit;
	}

	private boolean setIntersectionVector(Viewport viewport, int mouseX, int mouseY, int constraintAxis, Vector3d vector) {
		double x = mouseX - viewport.getComponent().getWidth() * 0.5;
		double y = viewport.getComponent().getHeight() * 0.5 - mouseY;
		Point3d rayOrigin = new Point3d();
		Vector3d rayDirection = new Vector3d();
		if (viewport.getViewDef() instanceof OrthoViewDef) {
			rayOrigin.set(x, y, 1);
			rayDirection.set(0, 0, -1);
			
		} else {
			PerspectiveViewDef perspective = (PerspectiveViewDef) viewport.getViewDef();
			double z = perspective.getRelativeFocalLength() * viewport.getComponent().getWidth();
			rayOrigin.set(0, 0, 0);
			rayDirection.set(x, y, -z);
		}
		Matrix4d m = viewport.getViewDef().getInverseMatrix(new Matrix4d());
		m.transform(rayOrigin);
		m.transform(rayDirection);
		if (constraintAxis < 0) {
			/* rotate freely, compute ray/sphere intersection */
			if (Utils3d.raySphereIntersection(rayOrigin, rayDirection, pivot, radius, vector, true)) {
				vector.sub(pivot);
				vector.normalize();
				return true;
			}
		} else {
			/* rotation is constrained to an axis, compute ray/plane intersection */
			Vector3d normal = new Vector3d();
			switch (constraintAxis) {
			case 0:
				normal.set(1, 0, 0);
				break;
			case 1:
				normal.set(0, 1, 0);
				break;
			case 2:
				normal.set(0, 0, 1);
				break;
			default:
				throw new RuntimeException();
			}
			matrix.transform(normal);
//			m.transform(normal);
			if (Utils3d.rayPlaneIntersection(rayOrigin, rayDirection, pivot, normal, vector)) {
				Point3d p = new Point3d(vector);
//				System.out.println(p.distance(pivot));
				if (p.distance(pivot) < radius * 0.2) {
					return false;
				}
				vector.sub(pivot);
				vector.normalize();
				return true;
			}
		}
		return false;
	}
	
	private class HitMouseListener extends MouseAdapter {
		final Viewport viewport;
		private HitMouseListener(Viewport viewport) {
			this.viewport = viewport;
		}
		@Override
		public void mousePressed(MouseEvent e) {
			int constraint = getHitCircle(viewport, e.getX(), e.getY());
			if (setIntersectionVector(viewport, e.getX(), e.getY(), constraint, fromVector)) {
				mouseMotionListener = new HitMouseMotionListener(viewport, constraint);
				viewport.getComponent().addMouseMotionListener(mouseMotionListener);
			}
//			System.out.println(getHitCircle(viewport, e.getX(), e.getY()));
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			if (mouseMotionListener != null) {
				viewport.getComponent().removeMouseMotionListener(mouseMotionListener);
				mouseMotionListener = null;
				Matrix3d m = new Matrix3d();
				rotation.getRotationMatrix(m);
				axisRotation.rotateMatrix(m);
				axisRotation.setRotation(m);
				rotation.set(0, 0, 0);
				axisRotationAttr.setTuple(axisRotation);
				rotationAttr.setTuple(rotation);
				Main.getInstance().repaintViewports();
			}
		}
	};
	
	private class HitMouseMotionListener extends MouseMotionAdapter {
		final Viewport viewport;
		final int constraint;
		private HitMouseMotionListener(Viewport viewport, int constraint) {
			this.viewport = viewport;
			this.constraint = constraint;
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			if (setIntersectionVector(viewport, e.getX(), e.getY(), constraint, toVector)) {
				Vector3d axis = new Vector3d();
				axis.cross(fromVector, toVector);
				double angle = Math.acos(fromVector.dot(toVector));
				Matrix3d m = new Matrix3d();
				axisRotation.getRotationMatrix(m);
				m.invert();
				m.transform(axis);
				axis.normalize();
				m.set(new AxisAngle4d(axis, angle));
				rotation.setRotation(m);
				switch (constraint) {
				case 0:
					rotation.y = 0;
					rotation.z = 0;
					break;
				case 1:
					rotation.x = 0;
					rotation.z = 0;
					break;
				case 2:
					rotation.x = 0;
					rotation.y = 0;
					break;
				}
				rotationAttr.setTuple(rotation);
				Main.getInstance().syncRepaintViewport(viewport);
			}
		}
	}
}
