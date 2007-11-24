package com.jpatch.boundary.tools;

import java.awt.Dimension;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jpatch.afw.attributes.GenericAttr;
import com.jpatch.afw.attributes.StateMachine;
import com.jpatch.afw.attributes.Tuple3Attr;
import com.jpatch.afw.control.AttributeEdit;
import com.jpatch.afw.control.JPatchUndoableEdit;
import com.jpatch.afw.vecmath.Rotation3d;
import com.jpatch.afw.vecmath.Sphere;
import com.jpatch.afw.vecmath.TransformUtil;
import static com.jpatch.afw.vecmath.TransformUtil.*;
import com.jpatch.afw.vecmath.Utils3d;
import com.jpatch.boundary.Main;
import com.jpatch.boundary.OrthoViewDef;
import com.jpatch.boundary.PerspectiveViewDef;
import com.jpatch.boundary.Selection;
import com.jpatch.boundary.ViewDef;
import com.jpatch.boundary.ViewDirection;
import com.jpatch.boundary.Viewport;
import com.jpatch.boundary.ViewportGl;
import com.jpatch.boundary.actions.Actions;
import com.jpatch.entity.BasicMaterial;
import com.jpatch.entity.GlMaterial;
import com.jpatch.settings.ColorSettings;
import com.jpatch.settings.Settings;

import javax.media.opengl.GL;
import javax.vecmath.*;

import jpatch.boundary.action.EditAnimObjectAction;


import static javax.media.opengl.GL.*;

public class RotateTool extends AbstractManipulatorTool implements VisibleTool {
	public static final GenericAttr<String> EDIT_NAME = new GenericAttr<String>("rotate");
	private static final double SCREEN_ROTATE_FACTOR = 1.2;
	private static final int SEGMENTS = 128;
	private static final int CIRCLE_SEGMENTS = 4096;
	private static final double[] COS = new double[CIRCLE_SEGMENTS];
//	private static final double[] SIN = new double[CIRCLE_SEGMENTS];
	private final ColorSettings colorSettings = Settings.getInstance().colors;
	private final Point3d[][] points = new Point3d[3][SEGMENTS + 1];
	private final float[][] COLORS = new float[][] {
			{ 1.0f, 0.0f, 0.0f, 1.0f },	// x axis
			{ 0.0f, 0.9f, 0.0f, 1.0f },	// y axis
			{ 0.4f, 0.4f, 1.0f, 1.0f },	// z axis
			{ 1.0f, 0.0f, 0.0f, 0.5f },	// x axis hidden
			{ 0.0f, 0.9f, 0.0f, 0.5f },	// y axis hidden
			{ 0.4f, 0.4f, 1.0f, 0.5f },	// z axis hidden
			{ 0.5f, 0.5f, 0.5f, 1.0f },	// grey axis
			{ 0.0f, 0.0f, 0.0f, 1.0f },	// outline
			{ 0.9f, 0.9f, 0.0f, 1.0f }	// yellow axis
	};
	static int n = 0;
	
	private final Tuple3Attr pivotAttr = new Tuple3Attr();
	private final Tuple3Attr axisRotationAttr = new Tuple3Attr();
	private final Tuple3Attr rotationAttr = new Tuple3Attr();
	private final Point3d pivot = new Point3d();
	private final Rotation3d axisRotation = new Rotation3d();
	private final Rotation3d rotation = new Rotation3d();
	private final Rotation3d startRotation = new Rotation3d();
	
	int axisConstraint = -1;
	private MouseListener[] mouseListeners;
	private MouseMotionListener mouseMotionListener;
	
	private Vector3d fromVector = new Vector3d();
	private Vector3d toVector = new Vector3d();
	private TransformUtil transformUtil = new TransformUtil("axisRotation", "startRotation", "rotation");
	private static final int AXIS_ROTATION = 3;
	private static final int START_ROTATION = 4;
	private static final int ROTATION = 5;
	
	static {
//		Color3f black = new Color3f(0, 0, 0);
//		FRONT_MATERIAL = new GlMaterial(black, black, black, black, 0);
//		BACK_MATERIAL = new GlMaterial(black, black, black, black, 0);
		for (int i = 0; i < CIRCLE_SEGMENTS; i++) {
			COS[i] = Math.cos(i * 2 * Math.PI / CIRCLE_SEGMENTS);
//			SIN[i] = Math.sin(i * 2 * Math.PI / CIRCLE_SEGMENTS);
		}
	}
	
	public RotateTool() {
		System.out.println(transformUtil.getValidMatrices());
		for (int i = 0; i < SEGMENTS; i++) {
			double sin = Math.sin(i * 2 * Math.PI / SEGMENTS);
			double cos = Math.cos(i * 2 * Math.PI / SEGMENTS);
			points[0][i] = new Point3d(0, -sin, cos);
			points[1][i] = new Point3d(cos, 0, sin);
			points[2][i] = new Point3d(cos, -sin, 0);
		}
		pivotAttr.bindTuple(pivot);
		axisRotationAttr.bindTuple(axisRotation);
		rotationAttr.bindTuple(rotation);
	}
	
	public void draw(Viewport viewport) {
		
		/* initialize GL for rendering */
		GL gl = ((ViewportGl) viewport).getGl();
		gl.glEnable(GL_BLEND);
		gl.glEnable(GL_LINE_SMOOTH);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		gl.glDisable(GL_LIGHTING);
		gl.glDisable(GL_CULL_FACE);
		
		ViewportVars vv = new ViewportVars(viewport);
		
		Point3d p = new Point3d();	// Point3d object to perform temporary transformations and rendering
		Point3d p1 = new Point3d();
		
		/*
		 * draw sphere outline and
		 * draw disc into the depth-buffer to distinguish front and backside of the sphere
		 */
		gl.glDepthFunc(GL_ALWAYS);								// always path depth-test
		for (int pass = 0; pass < 2; pass++) {
			double r = vv.offsetRadius;
			switch (pass) {
			case 0:
				gl.glColor3fv(COLORS[6], 0);
				gl.glBegin(GL_LINE_STRIP);
				break;
			case 1:
				gl.glColorMask(false, false, false, false);		// disable rendering into the color buffer
				gl.glBegin(GL_TRIANGLE_FAN);
				break;
			default:
				throw new RuntimeException();	
			}
			for (int i = 0; i <= SEGMENTS; i++) {
				p.set(points[2][(i < SEGMENTS) ? i : 0]);
				p.scale(r);
				vv.orientMatrix.transform(p);
				p.x += vv.offsetFactor * vv.cameraPivot.x;
				p.y += vv.offsetFactor * vv.cameraPivot.y;
				p.z += vv.offsetFactor * vv.cameraPivot.z;
				gl.glVertex3d(p.x, p.y, p.z);
			}
			gl.glEnd();
		}
		gl.glColorMask(true, true, true, true);			// enable rendering into the color buffer
		gl.glDepthFunc(GL_LEQUAL);						// normal depth test operation
		
		/*
		 * draw RGB circles
		 * pass 0: outline
		 * pass 1: fill color, thick
		 * pass 2: fill color, thin, with depth test disabled ("hidden lines")
		 */
		gl.glEnable(GL_DEPTH_TEST);
		for (int pass = 0; pass < 3; pass++) {
			switch (pass) {
			case 0:
				gl.glDepthMask(false);
				gl.glLineWidth(3.5f);
				break;
			case 1:
				gl.glDepthMask(true);
				gl.glLineWidth(2.5f);
				break;
			case 2:
				gl.glDisable(GL_DEPTH_TEST);
				gl.glLineWidth(1.0f);
				break;
			default:
				throw new RuntimeException();
			}
			
			/*
			 * index 0,1,2 : x,y,z
			 * index 3,4,5 : x,y,z oriented towards camera
			 */
			for (int i = 0; i < 3; i++) {
				double ringRadius = vv.radius;
				int colorIndex;
				switch (pass) {
				case 0:
					colorIndex = 7;
					break;
				case 1:
					colorIndex = i;
					break;
				case 2:
					colorIndex = i + 3;
					break;
				default:
					throw new RuntimeException();
				}
				
				float[] color = COLORS[colorIndex].clone();
				float alpha = color[3];
				
				gl.glColor4fv(COLORS[colorIndex], 0);
				gl.glBegin(GL_LINE_STRIP);
				for (int j = 0; j <= SEGMENTS; j++) {
					p.set(points[i % 3][(j < SEGMENTS) ? j : 0]);
					p.scale(ringRadius);
					
					transformUtil.transform(ROTATION, p, CAMERA, p);
					
					p1.set(p);
					p1.sub(vv.cameraPivot);
//					vv.orientMatrix.transform(p1);
					
					p1.scale(1 / ringRadius / transformUtil.getCameraScale());
//					System.out.println(p1);
					color[3] = alpha * ((float) p1.z * 0.4f + 0.6f);
					gl.glColor4fv(color, 0);
					gl.glVertex3d(p.x, p.y, p.z);
				}
				gl.glEnd();
			}
		}
		
		gl.glDisable(GL_DEPTH_TEST);
		for (int pass = 0; pass < 2; pass++) {
			int colorIndex;
			if (pass == 0) {
				gl.glColor4fv(COLORS[7], 0);
				gl.glLineWidth(3.5f);
			} else {
				gl.glColor4fv(COLORS[8], 0);
				gl.glLineWidth(2.5f);
			}
			
			/*
			 * index 0,1,2 : x,y,z
			 * index 3,4,5 : x,y,z oriented towards camera
			 */
			
			double ringRadius = vv.offsetRadius * SCREEN_ROTATE_FACTOR;
			
			gl.glBegin(GL_LINE_STRIP);
			for (int j = 0; j <= SEGMENTS; j++) {
				p.set(points[2][(j < SEGMENTS) ? j : 0]);
				p.scale(ringRadius);
				
				vv.orientMatrix.transform(p);
				p.x += vv.offsetFactor * vv.cameraPivot.x;
				p.y += vv.offsetFactor * vv.cameraPivot.y;
				p.z += vv.offsetFactor * vv.cameraPivot.z;
				
				gl.glVertex3d(p.x, p.y, p.z);
			}
			gl.glEnd();
		}
		gl.glLineWidth(1.0f);
		
		if (mouseMotionListener != null) {
			/*
			 * draw plane of rotation
			 */
			
			int constraint = ((HitMouseMotionListener) mouseMotionListener).constraint;
			float r, g, b;
			double extraScale = 1;
			switch (constraint) {
			case -1:
				r = 0.5f; g = 0.5f; b = 0.5f;
				break;
			case 0:
				r = 1.0f; g = 0.25f; b = 0.25f;
				break;
			case 1:
				r = 0.25f; g = 0.75f; b = 0.25f;
				break;
			case 2:
				r = 0.25f; g = 0.25f; b = 1.0f;
				break;
			case 3:
				r = 0.66f; g = 0.66f; b = 0.25f;
				extraScale = SCREEN_ROTATE_FACTOR;
				break;
			default:
				throw new RuntimeException();
			}
			
			Vector3d axis = new Vector3d();
			axis.cross(fromVector, toVector);
			double angle = Math.acos(fromVector.dot(toVector));
			
			AxisAngle4d axisAngle = new AxisAngle4d(axis, 0);
			Matrix3d matrix = new Matrix3d();
			
			for (int pass = 0; pass < 3; pass++) {
				float alpha;
				switch (pass) {
				case 0:
					gl.glDepthMask(false);
					gl.glBegin(GL_TRIANGLE_FAN);
					alpha = 0.25f;
					break;
				case 1:
					gl.glBegin(GL_LINE_STRIP);
					alpha = 0.5f;
					break;
				case 2:
					gl.glDepthMask(true);
					gl.glEnable(GL_DEPTH_TEST);
					gl.glBegin(GL_LINE_STRIP);
					alpha = 1.0f;
					break;
				default:
					throw new RuntimeException();
				}
				for (int i = 0; i <= SEGMENTS; i++) {
					axisAngle.angle = i * 2 * Math.PI / SEGMENTS;
					matrix.set(axisAngle);
					p.scale(vv.radius * extraScale, fromVector);
					matrix.transform(p);
					transformUtil.transform(START_ROTATION, p, CAMERA, p);
					
					p1.set(p);
					p1.sub(vv.cameraPivot);
//					vv.orientMatrix.transform(p1);
					
					p1.scale(1 / vv.radius / extraScale / transformUtil.getCameraScale());
					
					
					gl.glColor4f(r, g, b, alpha * ((float) p1.z * 0.4f + 0.6f));
					gl.glVertex3d(p.x, p.y, p.z);
					
				}
				gl.glEnd();
			}
			
			/* draw ticks */
			gl.glDisable(GL_DEPTH_TEST);
			gl.glBegin(GL_LINES);
			gl.glColor4f(1, 1, 1, 0.75f);
			Point3d pOld = new Point3d();
			for (int a = 0; a < (Math.toDegrees(angle) + 1); a++) {
				double factor = 0.0;
				if (a < Math.toDegrees(angle)) {
					axisAngle.angle = Math.toRadians(a);
					if (a == 0) factor = 0.0;
					else if (a % 45 == 0) factor = 1 - 0.32;
					else if (a % 15 == 0) factor = 1 - 0.16;
					else if (a % 5 == 0) factor = 1 - 0.08;
					else factor = 1;
				} else {
					axisAngle.angle = angle;
				}
				matrix.set(axisAngle);
				p.scale(vv.radius * extraScale, fromVector);
				p1.scale(factor, p);
				matrix.transform(p);
				matrix.transform(p1);
				transformUtil.transform(START_ROTATION, p, CAMERA, p);
				transformUtil.transform(START_ROTATION, p1, CAMERA, p1);
				if (factor < 1) {
					gl.glVertex3d(p.x, p.y, p.z);
					gl.glVertex3d(p1.x, p1.y, p1.z);
				}
				if (a > 0) {
					gl.glVertex3d(pOld.x, pOld.y, pOld.z);
					gl.glVertex3d(p.x, p.y, p.z);
				}
				pOld.set(p);
			}
			gl.glEnd();
		}
		
		/* cleanup gl */
		gl.glDisable(GL_BLEND);
		gl.glDisable(GL_LINE_SMOOTH);
		gl.glEnable(GL_LIGHTING);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_CULL_FACE);
	}
	
	public void registerListeners(Viewport[] viewports) {
		resetPivot(pivot);
		pivotAttr.setTuple(pivot);
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
	
	/**
	 * Computes the ray/sphere intersection of a ray shot through the point (on screen) specified
	 * by the mouseX and mouseY parameters and the rotate-tool's enclosing sphere in local space.
	 * @param vv ViewportVars
	 * @param mouseX mouse x position (screen space)
	 * @param mouseY mouse y position (screen space)
	 * @param result the coordinates of the intersection point (in local space) will be stored in this Point3d object. Note that this object will be modified,
	 * even if there was no intersection
	 * @return true if there was an intersection, false otherwise
	 */
	private boolean computeIntersectionPoint(ViewportVars vv, int mouseX, int mouseY, int constraint, Point3d result) {
		double cameraRadius = vv.radius * transformUtil.getCameraScale();
		
		Point3d rayOrigin = new Point3d();
		Vector3d rayDirection = new Vector3d();
		if (transformUtil.isPerspective()) {
			rayOrigin.set(0, 0, 0);
			rayDirection.set(mouseX, mouseY, 1);
			transformUtil.projectFromScreen(CAMERA, rayDirection, rayDirection);
			rayDirection.z = -1; // TODO: WHY?
		} else {
			rayOrigin.set(mouseX, mouseY, 0);
			transformUtil.projectFromScreen(CAMERA, rayOrigin, rayOrigin);
			rayDirection.set(0, 0, -1);
		}
		
		boolean hit = false;
		
		if (constraint == -1) {
			/* no axis constraint, use ray/shpere intersection */
			hit = Utils3d.raySphereIntersection(rayOrigin, rayDirection, vv.cameraPivot, cameraRadius, result, true);
		} else {
			/* axis constraint active, setup axis in camera space */
			Vector3d cameraAxis = new Vector3d();
			switch (constraint) {
			case 0:
				cameraAxis.set(1, 0, 0);
				transformUtil.transform(START_ROTATION, cameraAxis, CAMERA, cameraAxis);
				break;
			case 1:
				cameraAxis.set(0, 1, 0);
				transformUtil.transform(START_ROTATION, cameraAxis, CAMERA, cameraAxis);
				break;
			case 2:
				cameraAxis.set(0, 0, 1);
				transformUtil.transform(START_ROTATION, cameraAxis, CAMERA, cameraAxis);
				break;
			case 3:
				cameraAxis.set(0, 0, 1);
				vv.orientMatrix.transform(cameraAxis);
				break;
			}
			cameraAxis.normalize();
			/* compute ray/plane intersection */
			hit = Utils3d.rayPlaneIntersection(rayOrigin, rayDirection, vv.cameraPivot, cameraAxis, result);
			
			/* outside of radius? */
			if (!hit || (constraint != 3 && result.distance(vv.cameraPivot) > cameraRadius)) {
				distSqToCircle(vv, mouseX, mouseY, cameraAxis, false, false, result);
				hit = true;
			}
		}
		if (hit) {
			transformUtil.transform(CAMERA, result, START_ROTATION, result);
			return true;
		}
		return false;
	}
	
	/**
	 * Computes the distance in pixel from the screen point specified by mouseX and mouseY to the
	 * circle specified by cameraAxis and the member-variables pivot and radius (projected into
	 * screen space). // FIXME
	 * @param transformUtil
	 * @param mouseX
	 * @param mouseY
	 * @param cameraAxis the axis of the circle (in camera space)
	 * @param result closest point on circle (in camera space)
	 * @returns the squared screen-space distance, in pixel
	 */
	private double distSqToCircle(ViewportVars vv, int mouseX, int mouseY, Vector3d cameraAxis, boolean orient, boolean frontsideOnly, Tuple3d result) {
		
		
		Vector3d axis = new Vector3d();
		axis.normalize(cameraAxis);
		Vector3d u = Utils3d.perpendicularVector(axis, new Vector3d());
		Vector3d v = new Vector3d();
		v.cross(u, cameraAxis);
		v.normalize();
		Point3d p0 = new Point3d();		// start point of line segment in camera space
		Point3d p1 = new Point3d();		// end point of line segment in camera space
		Point3d p = new Point3d();		// point on line segment closest to mouseX/mouseY in camera space
		Point3d p0s = new Point3d();	// start point of line segment in screen space
		Point3d p1s = new Point3d();	// end point of line segment in screen space
		Point3d ps = new Point3d();		// point on line segment closest to mouseX/mouseY in screen space
		
		double minDistSq = Double.MAX_VALUE;
		double minFrontSq = 64;
		double r, offset;
		if (orient) {
			r = transformUtil.getCameraScale() * vv.offsetRadius * SCREEN_ROTATE_FACTOR;
			offset = vv.offsetFactor;
		} else {
			r = transformUtil.getCameraScale() * vv.radius;
			offset = 1;
		}
		for (int i = -1; i < CIRCLE_SEGMENTS; i++) {
			int index = i == -1 ? CIRCLE_SEGMENTS - 1 : i;
			double rcos = r * COS[index];
			int sinIndex = index + (CIRCLE_SEGMENTS >> 2);
			if (sinIndex >= CIRCLE_SEGMENTS) {
				sinIndex -= CIRCLE_SEGMENTS;
			}
			double rsin = r * COS[sinIndex];
			p1.set(p0);
			p0.set(
					u.x * rcos + v.x * rsin + offset * vv.cameraPivot.x,
					u.y * rcos + v.y * rsin + offset * vv.cameraPivot.y,
					u.z * rcos + v.z * rsin + offset * vv.cameraPivot.z
			);
			if (i >= 0) {
				transformUtil.projectToScreen(transformUtil.CAMERA, p0, p0s);
				transformUtil.projectToScreen(transformUtil.CAMERA, p1, p1s);
				
//				vv.viewport.getComponent().getGraphics().drawLine((int) p0s.x, (int) p0s.y, (int) p1s.x, (int) p1s.y);
				double t = Utils3d.closestPointOnLine(p0s.x, p0s.y, p1s.x, p1s.y, mouseX, mouseY);
				t = Math.min(1, Math.max(0, t));
				
				ps.interpolate(p0s, p1s, t);
				transformUtil.projectFromScreen(transformUtil.CAMERA, ps, p);

				double dx = ps.x - mouseX;
				double dy = ps.y - mouseY;
				double distSq = (dx * dx + dy * dy);
				boolean frontside = distSq < minFrontSq && p.z >= vv.cameraPivot.z;
				
				if (p.z < vv.cameraPivot.z) {
					distSq += minFrontSq;
				}
				
				if (distSq < minDistSq) {
					if (frontside) {
						frontsideOnly = true;
					}
					if (!frontsideOnly || frontside) {
						minDistSq = distSq;
//						result.sub(p, vv.cameraPivot);
						result.set(p);
						/* normalize */
//						result.scale(1.0 / Math.sqrt (result.x * result.x + result.y * result.y + result.z * result.z));
						
//						vv.viewport.getComponent().getGraphics().fillRect((int) ps.x - 2, (int) ps.y - 2, 5, 5);
					}
				}
			}
		}
		return minDistSq;
	}
	
	private class HitMouseListener extends MouseAdapter {
		final Viewport viewport;
		final ViewportVars vv;
		final Tuple3d oldAxisRotation = new Point3d();
		final Tuple3d oldRotation = new Point3d();
		
		private HitMouseListener(Viewport viewport) {
			this.viewport = viewport;
			this.vv = new ViewportVars(viewport);
		}
		@Override
		public void mousePressed(MouseEvent e) {
			
			ViewportVars vv = new ViewportVars(viewport);
			
			/* check if an axis-circle was hit */
			Vector3d cameraAxis = new Vector3d();
			int axis = -1;
			double min = 64;
			for (int i = 0; i < 4; i++) {
				boolean orient = false;
				switch (i) {
				case 0:
					cameraAxis.set(1, 0, 0);
					transformUtil.transform(START_ROTATION, cameraAxis, CAMERA, cameraAxis);
					break;
				case 1:
					cameraAxis.set(0, 1, 0);
					transformUtil.transform(START_ROTATION, cameraAxis, CAMERA, cameraAxis);
					break;
				case 2:
					cameraAxis.set(0, 0, 1);
					transformUtil.transform(START_ROTATION, cameraAxis, CAMERA, cameraAxis);
					break;
				case 3:
					cameraAxis.set(0, 0, 1);
					vv.orientMatrix.transform(cameraAxis);
					orient = true;
					break;
				}
				cameraAxis.normalize();
				
				double distSq = distSqToCircle(vv, e.getX(), e.getY(), cameraAxis, orient, true, fromVector);
				if (distSq < min) {
					min = distSq;
					axis = i;
				}
			}
			
			Point3d hitPoint = new Point3d();
			boolean intersection = computeIntersectionPoint(vv, e.getX(), e.getY(), axis, hitPoint);
			if (intersection) {
				if (e.getClickCount() == 2) {
					rotation.set(0, 0, 0);
					rotationAttr.setTuple(rotation);
					startRotation.set(rotation);
					Main.getInstance().repaintViewports();
				}
				else if (e.getClickCount() == 3) {
					/* align axisrotation, z should point to viewer */
					Matrix3d orientMatrix = new Matrix3d(vv.orientMatrix);
					orientMatrix.normalize();
					Matrix3d m = getCameraToWorldRotation(transformUtil, new Matrix3d());
					m.mul(orientMatrix);
					
					transformUtil.getRotationScaleMatrix(WORLD, LOCAL, orientMatrix);
					orientMatrix.mul(m);
					rotation.setRotation(orientMatrix);
					rotationAttr.setTuple(rotation);
					startRotation.set(rotation);
					Main.getInstance().repaintViewports();
				} else {
					fromVector.set(hitPoint);
					fromVector.normalize();
					mouseMotionListener = new HitMouseMotionListener(viewport, axis);
					Selection selection = Main.getInstance().getSelection();
					selection.begin();
					oldAxisRotation.set(axisRotation);
					oldRotation.set(rotation);
					startRotation.set(rotation);
					viewport.getComponent().addMouseMotionListener(mouseMotionListener);
				}
			}
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			if (mouseMotionListener != null) {
				viewport.getComponent().removeMouseMotionListener(mouseMotionListener);
				mouseMotionListener = null;
//				Matrix3d m = new Matrix3d();
//				rotation.getRotationMatrix(m);
//				axisRotation.rotateMatrix(m);
//				axisRotation.setRotation(m);
//				rotation.set(0, 0, 0);
				Selection selection = Main.getInstance().getSelection();
				List<JPatchUndoableEdit> editList = new ArrayList<JPatchUndoableEdit>(selection.getVertexCount() + 3);
//				axisRotationAttr.setTuple(oldAxisRotation);
//				rotationAttr.setTuple(oldRotation);
//				editList.add(AttributeEdit.changeAttribute(axisRotationAttr, oldAxisRotation, true));
				editList.add(AttributeEdit.changeAttribute(rotationAttr, oldRotation, false));
				if (LastModifierTool.getInstance().get() != RotateTool.this) {
					editList.add(AttributeEdit.changeAttribute(Main.getInstance().getActions().toolSM, LastModifierTool.getInstance().get(), false));
					LastModifierTool.getInstance().set(RotateTool.this);
				}
				selection.end(editList);
				Main.getInstance().getUndoManager().addEdit(EDIT_NAME, editList);
				startRotation.set(rotation);
				Main.getInstance().repaintViewports();
			}
		}
	};
	
	private class HitMouseMotionListener extends MouseMotionAdapter {
		final Viewport viewport;
		final ViewportVars vv;
		final int constraint;
		private HitMouseMotionListener(Viewport viewport, int constraint) {
			this.viewport = viewport;
			this.constraint = constraint;
			this.vv = new ViewportVars(viewport);
		}
		@Override
		public void mouseDragged(MouseEvent e) {
//			System.out.println(constraint);
			Point3d hitPoint = new Point3d();
			boolean intersection = computeIntersectionPoint(vv, e.getX(), e.getY(), constraint, hitPoint);
			if (intersection) {
				toVector.set(hitPoint);
				toVector.normalize();
				
//				System.out.println(fromVector + " - " + toVector);
				Vector3d axis = new Vector3d(1, 0, 0);
//				System.out.println(fromVector.dot(toVector));
				double angle = 0;
				double dot = fromVector.dot(toVector);
				if (dot < 0.999999) {
					axis.cross(fromVector, toVector);
					angle = Math.acos(dot);
				}
				
				Matrix3d m = new Matrix3d();
				axisRotation.getRotationMatrix(m);
				m.invert();
				//m.transform(axis);
				//axis.normalize();
				AxisAngle4d axisAngle = new AxisAngle4d(axis, angle);
				m.set(axisAngle);
				
				Matrix3d m2 = startRotation.getRotationMatrix(new Matrix3d());
				m2.mul(m);
				
				
				
				if (isObjectMode()) {
					Rotation3d r1 = new Rotation3d();
					r1.setRotation(m);
					getSelectedNode().getRotationAttribute().setTuple(r1);
					getSelectedNode().getTransform().computeBranch();
				} else {
					rotation.setRotation(m2);

					transformUtil.transform(START_ROTATION, axis, LOCAL, axis);
					Selection selection = Main.getInstance().getSelection();
					selection.rotateTo(pivot, new AxisAngle4d(axis, angle));
				}
//				selection.getSelectedSdsModelAttribute().getValue().getSds().computeLevel2Vertices();
				
				rotationAttr.setTuple(rotation);
				
				
				Main.getInstance().syncRepaintViewport(viewport);
			}
		}
	}
	
	class ViewportVars {
		final Viewport viewport; //FIXME only needed for debugging
		final double radius;
		final double offsetFactor;
		final double offsetRadius;
		final Point3d cameraPivot = new Point3d();
		final Point3d worldPivot = new Point3d();
		final Matrix3d orientMatrix = new Matrix3d();
		
		ViewportVars(Viewport viewport) {
			this.viewport = viewport;
			viewport.getViewDef().computeMatrix();
			viewport.getViewDef().configureTransformUtil(transformUtil);
			Main.getInstance().getSelection().configureTransformUtil(transformUtil);
			
			/* set the transformUtil's local->world matrix's scale to 1.0 */
			transformUtil.setScale(LOCAL, WORLD, 1.0);
			
			/* set cameraPivot to pivot in camera space */
			transformUtil.transform(LOCAL, pivot, CAMERA, cameraPivot);
			transformUtil.transform(LOCAL, pivot, WORLD, worldPivot);
			
			Matrix4d matrix = new Matrix4d();
			
			/* compute rotation matrix */
//			Main.getInstance().getSelection().getSelectedSdsModelAttribute().getValue().getTransform().getMatrix(matrix);
			axisRotation.getRotationMatrix(matrix);
//			axisRotation.rotateMatrix(matrix);
			
			/* add pivot translation */
			matrix.m03 = pivot.x;
			matrix.m13 = pivot.y;
			matrix.m23 = pivot.z;
			matrix.m33 = 1;
			
			
			/* set local2world to rotate-tool matrix */ 
			transformUtil.setSpace2World(AXIS_ROTATION, LOCAL, matrix);
//			transformUtil.setSpace2World (AXIS_ROTATION, matrix);
			
			/* compute rotation matrix */
//			Main.getInstance().getSelection().getSelectedSdsModelAttribute().getValue().getTransform().getMatrix(matrix);
			rotation.getRotationMatrix(matrix);
			axisRotation.rotateMatrix(matrix);
			
			/* add pivot translation */
			matrix.m03 = pivot.x;
			matrix.m13 = pivot.y;
			matrix.m23 = pivot.z;
			matrix.m33 = 1;
			
			
			/* set local2world to rotate-tool matrix */ 
			transformUtil.setSpace2World(ROTATION, LOCAL, matrix);
			
			/* compute rotation matrix */
//			Main.getInstance().getSelection().getSelectedSdsModelAttribute().getValue().getTransform().getMatrix(matrix);
			startRotation.getRotationMatrix(matrix);
			axisRotation.rotateMatrix(matrix);
			
			/* add pivot translation */
			matrix.m03 = pivot.x;
			matrix.m13 = pivot.y;
			matrix.m23 = pivot.z;
			matrix.m33 = 1;
			
			
			/* set local2world to rotate-tool matrix */ 
			transformUtil.setSpace2World(START_ROTATION, LOCAL, matrix);
			
			/*
			 * set the radius so that the tool will occupy about 1/3rd of the screen
			 */
			radius = transformUtil.computeNiceRadius(-cameraPivot.z, viewport.getComponent().getWidth(), viewport.getComponent().getHeight());
			
			/*
			 * compute shilouette offset
			 */
			if (transformUtil.isPerspective()) {
				double distanceToPivot = Math.sqrt(cameraPivot.x * cameraPivot.x + cameraPivot.y * cameraPivot.y + cameraPivot.z * cameraPivot.z);
				double offset = radius * radius / distanceToPivot;
				offsetFactor = 1.0 - offset / distanceToPivot;
				offsetRadius = Math.sqrt(radius * radius - offset * offset);
			} else {
				offsetFactor = 1.0;
				offsetRadius = radius;
			}
			
			/*
			 * compute the orient towards camera matrix
			 */
			if (!transformUtil.isPerspective()) {
				/* if this is an orthographic projection, use the scaled identity matrix */
				double scale = transformUtil.getCameraScale();
				orientMatrix.m00 = scale; orientMatrix.m01 = 0; orientMatrix.m02 = 0;
				orientMatrix.m10 = 0; orientMatrix.m11 = scale; orientMatrix.m12 = 0;
				orientMatrix.m20 = 0; orientMatrix.m21 = 0; orientMatrix.m22 = scale;
			} else {
				/* perspective projection */
				
				Vector3d x = new Vector3d(), y = new Vector3d(), z = new Vector3d();
				
				/* set z vector to point towards camera and normlaize it */
				z.set(-cameraPivot.x, -cameraPivot.y, -cameraPivot.z);
				z.normalize();
				
				x.set(1, 0, 0);		// camera space right vector
				y.cross(x, z);		// compute up vector
				y.normalize();
				x.cross(z, y);		// compute right vector
				x.normalize();
				
				/* setup the 3x3 transformation matrix */
				orientMatrix.m00 = x.x; orientMatrix.m01 = y.x; orientMatrix.m02 = z.x;
				orientMatrix.m10 = x.y; orientMatrix.m11 = y.y; orientMatrix.m12 = z.y;
				orientMatrix.m20 = x.z; orientMatrix.m21 = y.z; orientMatrix.m22 = z.z;
			}
		}
	}
	
	public Matrix3d getCameraToWorldRotation(TransformUtil transformUtil, Matrix3d rotationMatrix) {
		transformUtil.getRotationScaleMatrix(CAMERA, WORLD, rotationMatrix);
		rotationMatrix.normalize();
		return rotationMatrix;
	}
	
}
