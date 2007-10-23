package com.jpatch.boundary.tools;

import java.awt.Dimension;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.util.Arrays;

import com.jpatch.afw.attributes.Tuple3Attr;
import com.jpatch.afw.vecmath.Rotation3d;
import com.jpatch.afw.vecmath.TransformUtil;
import com.jpatch.afw.vecmath.Utils3d;
import com.jpatch.boundary.Main;
import com.jpatch.boundary.OrthoViewDef;
import com.jpatch.boundary.PerspectiveViewDef;
import com.jpatch.boundary.ViewDef;
import com.jpatch.boundary.ViewDirection;
import com.jpatch.boundary.Viewport;
import com.jpatch.boundary.ViewportGl;
import com.jpatch.entity.BasicMaterial;
import com.jpatch.entity.GlMaterial;
import com.jpatch.settings.ColorSettings;
import com.jpatch.settings.Settings;

import javax.media.opengl.GL;
import javax.vecmath.*;


import static javax.media.opengl.GL.*;

public class RotateTool implements VisibleTool {
	private static final int SEGMENTS = 128;
	private static final int CIRCLE_SEGMENTS = 128; //4096;
	private static final double[] COS = new double[CIRCLE_SEGMENTS];
	private static final double[] SIN = new double[CIRCLE_SEGMENTS];
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
	private final static GlMaterial FRONT_MATERIAL, BACK_MATERIAL;
	static int n = 0;
	
	private final Tuple3Attr pivotAttr = new Tuple3Attr();
	private final Tuple3Attr axisRotationAttr = new Tuple3Attr();
	private final Tuple3Attr rotationAttr = new Tuple3Attr();
	private final Point3d pivot = new Point3d();
	private final Rotation3d axisRotation = new Rotation3d();
	private final Rotation3d rotation = new Rotation3d();
	private double radius = 10.0;
	int axisConstraint = -1;
	private Matrix4d matrix = new Matrix4d();
	private MouseListener[] mouseListeners;
	private MouseMotionAdapter mouseMotionListener;
	
	private Vector3d fromVector = new Vector3d();
	private Vector3d toVector = new Vector3d();
	
	
	
	static {
		Color3f black = new Color3f(0, 0, 0);
		FRONT_MATERIAL = new GlMaterial(black, black, black, black, 0);
		BACK_MATERIAL = new GlMaterial(black, black, black, black, 0);
		for (int i = 0; i < CIRCLE_SEGMENTS; i++) {
			COS[i] = Math.cos(i * 2 * Math.PI / CIRCLE_SEGMENTS);
			SIN[i] = Math.sin(i * 2 * Math.PI / CIRCLE_SEGMENTS);
		}
	}
	
	public RotateTool() {
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
		
		radius = computeRadius(viewport);
		
		/* initialize GL for rendering */
		GL gl = ((ViewportGl) viewport).getGl();
		gl.glEnable(GL_BLEND);
		gl.glEnable(GL_LINE_SMOOTH);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		gl.glDisable(GL_LIGHTING);
		gl.glDisable(GL_CULL_FACE);
		
		TransformUtil transformUtil = viewport.getViewDef().getTransformUtil();
		
		/* compute rotation matrix */
		rotation.getRotationMatrix(matrix);
		axisRotation.rotateMatrix(matrix);
		/* add pivot translation */
		matrix.m03 = pivot.x;
		matrix.m13 = pivot.y;
		matrix.m23 = pivot.z;
		
		matrix.m33 = 1;
		
		/* set local2world to rotate-tool matrix */ 
		transformUtil.setLocal2World(matrix);
		
		/* set cameraPivot to pivot in camera space */
		Point3d cameraPivot = new Point3d();
		transformUtil.world2Camera(pivot, cameraPivot);
		
		Point3d p = new Point3d();	// Point3d object to perform temporary transformations and rendering
		Point3d p1 = new Point3d();
		
		Matrix3d orientMatrix = createOrientTowardsCameraMatrix(transformUtil);
		gl.glClear(GL_DEPTH_BUFFER_BIT);	// clear depth buffer
		
		/* compute plane offset if perspective projection is used */
		double offsetFactor = 1;
		double offsetRadius = radius;
		if (transformUtil.isPerspective()) {
			double distanceToPivot = Math.sqrt(cameraPivot.x * cameraPivot.x + cameraPivot.y * cameraPivot.y + cameraPivot.z * cameraPivot.z);
			double offset = radius * radius / distanceToPivot;
			offsetFactor = 1.0 - offset / distanceToPivot;
			offsetRadius = Math.sqrt(radius * radius - offset * offset);
		}
		
		/*
		 * draw sphere outline and
		 * draw disc into the depth-buffer to distinguish front and backside of the sphere
		 */
		for (int pass = 0; pass < 2; pass++) {
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
				p.scale(offsetRadius);
				orientMatrix.transform(p);
				p.x += offsetFactor * cameraPivot.x;
				p.y += offsetFactor * cameraPivot.y;
				p.z += offsetFactor * cameraPivot.z;
				gl.glVertex3d(p.x, p.y, p.z);
			}
			gl.glEnd();
		}
		gl.glColorMask(true, true, true, true);			// enable rendering into the color buffer
		
		/*
		 * draw RGB circles
		 * pass 0: outline
		 * pass 1: fill color, thick
		 * pass 2: fill color, thin, with depth test disabled ("hidden lines")
		 */
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
			for (int i = 0; i < 6; i++) {
				if (i == 3 || i == 4) {
					continue;
				}
				double ringRadius = (i == 5) ? offsetRadius * 1.2: radius;
				int colorIndex;
				switch (pass) {
				case 0:
					colorIndex = 7;
					break;
				case 1:
					if (i < 3) {
						colorIndex = i;
					} else {
						colorIndex = 8;
					}
					break;
				case 2:
					if (i < 3) {
						colorIndex = i + 3;
					} else {
						colorIndex = 8;
					}
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
					if (i < 3) {
						transformUtil.local2Camera(p, p);
					} else {
						orientMatrix.transform(p);
						p.x += offsetFactor * cameraPivot.x;
						p.y += offsetFactor * cameraPivot.y;
						p.z += offsetFactor * cameraPivot.z;
					}
					p1.set(p);
					orientMatrix.transform(p1);
					p1.sub(cameraPivot);
					p1.scale(1 / ringRadius / transformUtil.getCameraScale() / transformUtil.getCameraScale());
					color[3] = alpha * ((float) p1.z * 0.4f + 0.6f);
					gl.glColor4fv(color, 0);
					gl.glVertex3d(p.x, p.y, p.z);
				}
				gl.glEnd();
			}
		}
		
		/*
		 * draw plane of rotation
		 */
		
		/* compute rotation matrix */
		axisRotation.getRotationMatrix(matrix);
		/* add pivot translation */
		matrix.m03 = pivot.x;
		matrix.m13 = pivot.y;
		matrix.m23 = pivot.z;
		
		matrix.m33 = 1;
		
		/* set local2world to rotate-tool axis-rotation matrix */ 
		transformUtil.setLocal2World(matrix);
		
		Vector3d axis = new Vector3d();
		axis.cross(fromVector, toVector);
		double angle = Math.acos(fromVector.dot(toVector));
		
		AxisAngle4d axisAngle = new AxisAngle4d(axis, 0);
		Matrix3d matrix = new Matrix3d();
		
		for (int pass = 0; pass < 3; pass++) {
			float gray, alpha;
			switch (pass) {
			case 0:
				gl.glDepthMask(false);
				gl.glBegin(GL_TRIANGLE_FAN);
				gray = 0.5f;
				alpha = 0.25f;
				break;
			case 1:
				gl.glBegin(GL_LINE_STRIP);
				gray = 0.5f;
				alpha = 0.5f;
				break;
			case 2:
				gl.glDepthMask(true);
				gl.glEnable(GL_DEPTH_TEST);
				gl.glBegin(GL_LINE_STRIP);
				gray = 0.5f;
				alpha = 1.0f;
				break;
			default:
				throw new RuntimeException();
			}
			for (int i = 0; i <= SEGMENTS; i++) {
				axisAngle.angle = i * 2 * Math.PI / SEGMENTS;
				matrix.set(axisAngle);
				p.scale(radius, fromVector);
				matrix.transform(p);
				transformUtil.local2Camera(p, p);
				
				p1.set(p);
				orientMatrix.transform(p1);
				p1.sub(cameraPivot);
				p1.scale(1 / radius / transformUtil.getCameraScale() / transformUtil.getCameraScale());
				gl.glColor4f(gray, gray, gray, alpha * ((float) p1.z * 0.4f + 0.6f));
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
			p.scale(radius, fromVector);
			p1.scale(factor, p);
			matrix.transform(p);
			matrix.transform(p1);
			transformUtil.local2Camera(p, p);
			transformUtil.local2Camera(p1, p1);
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
		
		/* cleanup gl */
		gl.glDisable(GL_BLEND);
		gl.glDisable(GL_LINE_SMOOTH);
		gl.glEnable(GL_LIGHTING);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_CULL_FACE);
	}
	
	private double computeRadius(Viewport viewport) {
		TransformUtil transformUtil = viewport.getViewDef().getTransformUtil();
		if (!transformUtil.isPerspective()) {
			Dimension size = viewport.getComponent().getSize();
			return Math.min(size.width, size.height) / transformUtil.getCameraScale() * 0.2;
		} else {
			/* set cameraPivot to camera-space pivot and compute vector from pivot to camera*/
			Point3d cameraPivot = new Point3d();
			transformUtil.world2Camera(pivot, cameraPivot);
			return cameraPivot.z / transformUtil.getRelativeFocalLength() * -0.2;
		}
	}
	
	/**
	 * Returns a new Matrix3d that transforms from camera space to a space that always faces
	 * towards the viewer.
	 * @param transformUtil the ViewDef's TransformUtil
	 * @return a new Matrix3d that transforms from camera space to a space that always faces
	 * towards the viewer.
	 */
	private Matrix3d createOrientTowardsCameraMatrix(TransformUtil transformUtil) {
		if (!transformUtil.isPerspective()) {
			/* if this is an orthographic projection, return the identity matrix */
			double scale = transformUtil.getCameraScale();
			return new Matrix3d(
					scale, 0, 0,
					0, scale, 0,
					0, 0, scale
			);
		} else {
			/* perspective projection */
			/* set cameraPivot to camera-space pivot and compute vector from pivot to camera*/
			Point3d cameraPivot = new Point3d();
			transformUtil.world2Camera(pivot, cameraPivot);

			Vector3d x = new Vector3d(), y = new Vector3d(), z = new Vector3d();
			
			/* set z vector to point towards camera and normlaize it */
			z.set(-cameraPivot.x, -cameraPivot.y, -cameraPivot.z);
			z.normalize();
			
			x.set(1, 0, 0);		// camera space right vector
			y.cross(x, z);		// compute up vector
			y.normalize();
			x.cross(z, y);		// compute right vector
			x.normalize();
			
			/* create and return the 3x3 transformation matrix */
			return new Matrix3d(
					x.x, y.x, z.x,
					x.y, y.y, z.y,
					x.z, y.z, z.z
			);
		}
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
	
//	private int getHitCircle(Viewport viewport, int mouseX, int mouseY) {
//		axisRotation.getRotationMatrix(matrix);
//		rotation.rotateMatrix(matrix);
//		double x = mouseX - viewport.getComponent().getWidth() * 0.5;
//		double y = viewport.getComponent().getHeight() * 0.5 - mouseY;
//		int hit = -1;
//		double distSqMin = 64;
//		Line2D.Double line = new Line2D.Double();
//		Matrix3d antiRotation = getAntiRotation(viewport.getViewDef());
//		
//		Point3d p = new Point3d(pivot);
//		Vector3d v = new Vector3d();
//		Matrix4d m = viewport.getViewDef().getMatrix(new Matrix4d());
//		final double scale = 1.5 / (float) m.getScale() / radius;
//		m.transform(p);
//		double z;
//		if (viewport.getViewDef() instanceof OrthoViewDef) {
//			z = p.z;
//		} else {
//			z = Math.sqrt(p.x * p.x + p.y * p.y + p.z * p.z);
//		}
//		Point3d p0 = new Point3d();
//		
//		for (int i = 0; i < 6; i++) {
//			for (int j = 0; j <= SEGMENTS; j++) {
//				p.set(points[i % 3][j % SEGMENTS]);
//				p.scale(radius);
//				if (i >= 3) {
//					matrix.transform(p);
//				} else {
//					antiRotation.transform(p);
//				}
//				p.add(pivot);
//				viewport.getViewDef().transform(p);
//				line.x1 = p.x;
//				line.y1 = p.y;
//				p.set(points[i % 3][(j + 1) % SEGMENTS]);
//				p.scale(radius);
//				if (i >= 3) {
//					matrix.transform(p);
//				} else {
//					antiRotation.transform(p);
//				}
//				p.add(pivot);
//				p0.set(p);
//				viewport.getViewDef().transform(p);
//				m.transform(p0);
//				line.x2 = p.x;
//				line.y2 = p.y;
//				
//				double distSq = line.ptSegDistSq(x, y);
//				
//				double dist;
//				if (viewport.getViewDef() instanceof OrthoViewDef) {
//					dist = p0.z;
//				} else {
//					dist = 2 * z - Math.sqrt(p0.x * p0.x + p0.y * p0.y + p0.z * p0.z);
//				}
//				
////				System.out.println("z=" + z + " dist=" + dist);
////				System.out.println(line.x1 + "/" + line.y1 + " - " + line.x2 + "/" + line.y2 + " \t " + Math.sqrt(distSq));
//				if (z < (dist + 0.001) && distSq < distSqMin) {
//					distSqMin = distSq;
//					hit = i;
//					System.out.println("z=" + z + " dist=" + dist);
//				}
//			}
//		}
//		return hit;
//	}

//	private Matrix3d getAntiRotation(ViewDef viewdef) {
//		Matrix3d antiRotation = new Matrix3d();
//		viewdef.getInverseMatrix(new Matrix4d()).getRotationScale(antiRotation);
//		if (viewdef instanceof PerspectiveViewDef) {
//			Point3d p = new Point3d(pivot);
//			Matrix4d m = viewdef.getMatrix(new Matrix4d());
//			m.transform(p);
//			Vector3d x = new Vector3d();
//			Vector3d y = new Vector3d();
//			Vector3d z = new Vector3d(p);
//			Vector3d up = new Vector3d(0, 1, 0);
//			x.cross(z, up);
//			y.cross(x, z);
//			x.normalize();
//			y.normalize();
//			z.normalize();
//			Matrix3d matrix = new Matrix3d(
//					x.x, y.x, z.x,
//					x.y, y.y, z.y,
//					x.z, y.z, z.z
//			);
//			matrix.normalize();
//			antiRotation.mul(matrix);
//		}
//		antiRotation.normalize();
//		return antiRotation;
//	}
	
	/**
	 * Computes and returns the ray/sphere intersection of a ray shot through the point (on screen) specified
	 * by the mouseX and mouseY parameters and the rotate-tool's enclosing sphere.
	 */
	private Point3d getIntersectionPoint(TransformUtil transformUtil, int mouseX, int mouseY) {
		/* set cameraPivot to pivot in camera space */
		Point3d cameraPivot = new Point3d();
		transformUtil.world2Camera(pivot, cameraPivot);
		
		double cameraRadius = radius * transformUtil.getCameraScale();
		
		Point3d rayOrigin = new Point3d();
		Vector3d rayDirection = new Vector3d();
		if (transformUtil.isPerspective()) {
			rayOrigin.set(0, 0, 0);
			rayDirection.set(mouseX, mouseY, 1);
			transformUtil.screen2Camera(rayDirection, rayDirection);
			rayDirection.z = -1; // TODO: WHY?
		} else {
			rayOrigin.set(mouseX, mouseY, 0);
			transformUtil.screen2Camera(rayOrigin, rayOrigin);
			rayDirection.set(0, 0, -1);
		}
		
//		System.out.println("ray origin=" + rayOrigin + " direction=" + rayDirection);
		Point3d intersectionPoint = new Point3d();
		
		boolean hit = Utils3d.raySphereIntersection(rayOrigin, rayDirection, cameraPivot, cameraRadius, intersectionPoint, true);
		if (hit) {
			/* compute rotation matrix */
			axisRotation.getRotationMatrix(matrix);
			/* add pivot translation */
			matrix.m03 = pivot.x;
			matrix.m13 = pivot.y;
			matrix.m23 = pivot.z;
			
			matrix.m33 = 1;
			
			/* set local2world to rotate-tool axis-rotation matrix */ 
			transformUtil.setLocal2World(matrix);
			
			transformUtil.camera2Local(intersectionPoint, intersectionPoint);
//			System.out.println("hit at " + intersectionPoint);
			return intersectionPoint;
		} else {
//			System.out.println("miss");
		}
		
		return null;
	}
	
//	private boolean setIntersectionVector2(Viewport viewport, int mouseX, int mouseY, int constraintAxis, Vector3d vector) {
//		double x = mouseX - viewport.getComponent().getWidth() * 0.5;
//		double y = viewport.getComponent().getHeight() * 0.5 - mouseY;
//		Point3d rayOrigin = new Point3d();
//		Vector3d rayDirection = new Vector3d();
//		if (viewport.getViewDef() instanceof OrthoViewDef) {
//			rayOrigin.set(x, y, 1);
//			rayDirection.set(0, 0, -1);
//			
//		} else {
//			PerspectiveViewDef perspective = (PerspectiveViewDef) viewport.getViewDef();
//			double z = perspective.getRelativeFocalLength() * viewport.getComponent().getWidth();
//			rayOrigin.set(0, 0, 0);
//			rayDirection.set(x, y, -z);
//		}
//		Matrix4d m = viewport.getViewDef().getInverseMatrix(new Matrix4d());
//		m.transform(rayOrigin);
//		m.transform(rayDirection);
//		
//		Matrix3d antiRotation = getAntiRotation(viewport.getViewDef());
//		
//		if (constraintAxis < 0) {
//			/* rotate freely, compute ray/sphere intersection */
//			if (Utils3d.raySphereIntersection(rayOrigin, rayDirection, pivot, radius, vector, true)) {
//				vector.sub(pivot);
//				vector.normalize();
//				return true;
//			}
//		} else {
//			/* rotation is constrained to an axis, compute ray/plane intersection */
//			Vector3d normal = new Vector3d();
//			switch (constraintAxis) {
//			case 0: // fallthrough intentional
//			case 3:
//				normal.set(1, 0, 0);
//				break;
//			case 1: // fallthrough intentional
//			case 4:
//				normal.set(0, 1, 0);
//				break;
//			case 2: // fallthrough intentional
//			case 5:
//				normal.set(0, 0, 1);
//				break;
//			default:
//				throw new RuntimeException();
//			}
//			if (constraintAxis >= 3) {
//				matrix.transform(normal);
//			} else {
//				antiRotation.transform(normal);
//			}
//			
//			/* ray/plane */
//			if (Utils3d.rayPlaneIntersection(rayOrigin, rayDirection, pivot, normal, vector)) {
//				vector.sub(pivot);
//				if (vector.length() < radius) {
//					/* when inside the disc, use ray/plane intersection point */
//					vector.normalize();
////					System.out.println("inside");
//					return true;
//				}
//			}
//		
//			/* outside of disc, use closest on screen */
//			getAxisHit(viewport, mouseX, mouseY, normal, vector);
////			System.out.println(vector);
//			vector.normalize();
//			return true;
//			
//		}
//		return false;
//	}
	
	/**
	 * Computes the distance in pixel from the screen point specified by mouseX and mouseY to the
	 * circle specified by cameraAxis and the member-variables pivot and radius (projected into
	 * screen space).
	 * @param transformUtil
	 * @param mouseX
	 * @param mouseY
	 * @param cameraAxis the axis of the circle (in camera space)
	 * @param result a vector pointing from the pivot to the point on the circle closest to mouseX, mouseY (in camera space)
	 * @returns the screen-space distance, in pixel
	 */
	private double distToCircle(Viewport viewport, TransformUtil transformUtil, int mouseX, int mouseY, Vector3d cameraAxis, Vector3d result) {
		
		/* set cameraPivot to pivot in camera space */
		Point3d cameraPivot = new Point3d();
		transformUtil.world2Camera(pivot, cameraPivot);
		
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
		
		double minDistSq = 64;
		boolean frontsideOnly = false;
		double r = transformUtil.getCameraScale() * radius;
		
		for (int i = -1; i < CIRCLE_SEGMENTS; i++) {
			int index = i == -1 ? CIRCLE_SEGMENTS - 1 : i;
			double rcos = r * COS[index];
			double rsin = r * SIN[index];
			p1.set(p0);
			p0.set(
					u.x * rcos + v.x * rsin + cameraPivot.x,
					u.y * rcos + v.y * rsin + cameraPivot.y,
					u.z * rcos + v.z * rsin + cameraPivot.z
			);
			if (i >= 0) {
				transformUtil.camera2Screen(p0, p0s);
				transformUtil.camera2Screen(p1, p1s);
				
				System.out.print(mouseX + "/" + mouseY + "    " + p0s + "-" + p1s + "    ");
				double t = Utils3d.closestPointOnLine(p0s.x, p0s.y, p1s.x, p1s.y, mouseX, mouseY);
				t = Math.min(1, Math.max(0, t));
				
				ps.interpolate(p0s, p1s, t);
				transformUtil.screen2Camera(ps, p);
				
				viewport.getComponent().getGraphics().fillRect((int) p0s.x, (int) p0s.y, 3, 3);
				
				double dx = ps.x - mouseX;
				double dy = ps.y - mouseY;
				double distSq = (dx * dx + dy * dy);
				System.out.println(dx + "/" + dy);
				if (distSq < minDistSq) {
					p.sub(cameraPivot);
					boolean frontside = p.z > cameraPivot.z;
					if (frontside) {
						frontsideOnly = true;
					}
					if (!frontsideOnly || frontside) {
						minDistSq = distSq;
						result.sub(p, cameraPivot);
						result.normalize();
					}
				}
			}
		}
		return Math.sqrt(minDistSq);
	}
	
//	private double getAxisHit(TransformUtil transformUtil, int mouseX, int mouseY, Vector3d axis, Vector3d vector) {
//		double x = mouseX - viewport.getComponent().getWidth() * 0.5;
//		double y = viewport.getComponent().getHeight() * 0.5 - mouseY;
////		System.out.println("getAxisHit " + x + "," + y);
//		Vector3d normal = new Vector3d(axis);
//		normal.normalize();
//		Vector3d u0 = Utils3d.perpendicularVector(normal, new Vector3d());
//		Vector3d u1 = new Vector3d();
//		u1.cross(u0, normal);
////		System.out.println("u0=" + u0 + " u1=" + u1);
//		Point3d p0 = new Point3d();
//		Point3d p1 = new Point3d();
//		Point3d p0s = new Point3d();
//		Point3d p1s = new Point3d();
////		int sx = 0, sy = 0;
//		double minDistSq = Double.MAX_VALUE;
//		double z0 = -Double.MAX_VALUE;
////		double tt = -1;
////		long time = System.currentTimeMillis();
//		for (int i = 0; i < CIRCLE_SEGMENTS; i++) {
//			double rcos = radius * COS[i];
//			double rsin = radius * SIN[i];
//			p0.set(
//					u0.x * rcos + u1.x * rsin + pivot.x,
//					u0.y * rcos + u1.y * rsin + pivot.y,
//					u0.z * rcos + u1.z * rsin + pivot.z
//			);
//			rcos = radius * COS[(i + 1) % CIRCLE_SEGMENTS];
//			rsin = radius * SIN[(i + 1) % CIRCLE_SEGMENTS];
//			p1.set(
//					u0.x * rcos + u1.x * rsin + pivot.x,
//					u0.y * rcos + u1.y * rsin + pivot.y,
//					u0.z * rcos + u1.z * rsin + pivot.z
//			);
////			matrix.transform(p0);
//			p0s.set(p0);
//			viewport.getViewDef().transform(p0s);
////			matrix.transform(p1);
//			p1s.set(p1);
//			viewport.getViewDef().transform(p1s);
//			double t = Utils3d.closestPointOnLine(p0s.x, p0s.y, p1s.x, p1s.y, x, y);
//			
//			if (t < 0) {
//				t = 0;
//			} else if (t > 1) {
//				t = 1;
//			}
//			
//			double dx = p0s.x * (1.0 - t) + p1s.x * t - x;
//			double dy = p0s.y * (1.0 - t) + p1s.y * t - y;
//			double z = p0.z * (1.0 - t) + p1.z * t;
////				System.out.println(t + "     " + dx + "," + dy);
//			double distSq = (dx * dx + dy * dy);
//			double factor = (z > z0) ? 0.99 : 1;
//			distSq *= factor;
//			if (distSq < minDistSq) {
//				minDistSq = distSq;
//				z0 = z;
//				vector.interpolate(p0, p1, t);
//				vector.sub(pivot);
////				sx = (int) (p0s.x * (1.0 - t) + p1s.x * t);
////				sy = (int) (p0s.y * (1.0 - t) + p1s.y * t);
////				tt = t;
//			}
//			
//		}
////		System.out.println(System.currentTimeMillis() - time);
////		System.out.println(minDistSq + " " + vector);
////		System.out.println("mouse=" + x + "/" + y + "\t hit=" + sx + "/" + sy + "\t distance=" + Math.round(Math.sqrt(minDistSq)) + "\t t=" + tt);
//		return minDistSq;
//	}
//	
	private class HitMouseListener extends MouseAdapter {
		final Viewport viewport;
		private HitMouseListener(Viewport viewport) {
			this.viewport = viewport;
		}
		@Override
		public void mousePressed(MouseEvent e) {
//			int constraint = getHitCircle(viewport, e.getX(), e.getY());
//			axisConstraint = constraint;
			TransformUtil transformUtil = viewport.getViewDef().getTransformUtil();
			
			/* compute rotation matrix */
			axisRotation.getRotationMatrix(matrix);
			/* add pivot translation */
			matrix.m03 = pivot.x;
			matrix.m13 = pivot.y;
			matrix.m23 = pivot.z;
			
			matrix.m33 = 1;
			
			/* set local2world to rotate-tool axis-rotation matrix */ 
			transformUtil.setLocal2World(matrix);
			
			
			Vector3d cameraAxis = new Vector3d(1, 0, 0);
			transformUtil.local2Camera(cameraAxis, cameraAxis);
			cameraAxis.normalize();
			System.out.println(distToCircle(viewport, transformUtil, e.getX(), e.getY(), cameraAxis, new Vector3d()));
			
			Point3d hitPoint = getIntersectionPoint(transformUtil, e.getX(), e.getY());
			if (hitPoint != null) {
				if (e.getClickCount() == 2) {
					/* align axisrotation, z should point to viewer */
					Matrix3d orientMatrix = createOrientTowardsCameraMatrix(transformUtil);
					orientMatrix.normalize();
					Matrix3d m = transformUtil.getCameraToWorldRotation(new Matrix3d());
					m.mul(orientMatrix);
					axisRotation.setRotation(m);
					axisRotationAttr.setTuple(axisRotation);
					Main.getInstance().repaintViewports();
					return;
				}
				fromVector.set(hitPoint);
				fromVector.normalize();
				mouseMotionListener = new HitMouseMotionListener(viewport, 0);
				viewport.getComponent().addMouseMotionListener(mouseMotionListener);
			}
//			if (setIntersectionVector(viewport, e.getX(), e.getY(), constraint, fromVector)) {
//				mouseMotionListener = new HitMouseMotionListener(viewport, constraint);
//				viewport.getComponent().addMouseMotionListener(mouseMotionListener);
//			}
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
//			System.out.println(constraint);
			Point3d hitPoint = getIntersectionPoint(viewport.getViewDef().getTransformUtil(), e.getX(), e.getY());
			if (hitPoint != null) {
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
				m.set(new AxisAngle4d(axis, angle));
				rotation.setRotation(m);
//				switch (constraint) {
//				case 0:
//					rotation.y = 0;
//					rotation.z = 0;
//					break;
//				case 1:
//					rotation.x = 0;
//					rotation.z = 0;
//					break;
//				case 2:
//					rotation.x = 0;
//					rotation.y = 0;
//					break;
//				}
				rotationAttr.setTuple(rotation);
				Main.getInstance().syncRepaintViewport(viewport);
			}
		}
	}
}
