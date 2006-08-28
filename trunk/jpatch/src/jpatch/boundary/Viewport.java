package jpatch.boundary;

import java.awt.*;

import javax.vecmath.*;

import jpatch.boundary.settings.*;
import jpatch.entity.*;

public abstract class Viewport extends AbstractJPatchXObject {
	public Attribute.Enum<View> viewType = new Attribute.Enum<View>(View.FRONT);
	public Attribute.Tuple2 viewRotation = new Attribute.Tuple2("Rotation", 0, 0, false);
	public Attribute.Tuple2 viewTranslation = new Attribute.Tuple2("Translation", 0, 0, false);
	public Attribute.Double viewScale = new Attribute.Double(1);
	public Attribute.Boolean showPoints = new Attribute.Boolean(true);
	public Attribute.Boolean showCurves = new Attribute.Boolean(true);
	public Attribute.Boolean showSurfaces = new Attribute.Boolean(true);
	
	
	protected final int id;
	protected Component component;
	protected Matrix4d matrix = new Matrix4d();
	protected double fw;
	protected Camera camera;
	protected static final int maxSubdiv = 10;
	protected static final float nearClip = 1;
	protected static final float farClip = 1 << 15;
	protected static final RealtimeRendererSettings RENDERER_SETTINGS = Settings.getInstance().realtimeRenderer;
	protected static final Point3d p0 = new Point3d();
	protected static final Point3d p1 = new Point3d();
	protected static final Point3d p2 = new Point3d();
	protected static final Point3d p3 = new Point3d();
	protected final Iterable<Model> models;
	public static enum View {
		FRONT, BACK, TOP, BOTTOM, LEFT, RIGHT, BIRDS_EYE, CAMERA;
		@Override
		public String toString() {
			switch(this) {
			case FRONT:
				return "Front";
			case BACK:
				return "Back";
			case TOP:
				return "Top";
			case BOTTOM:
				return "Bottom";
			case LEFT:
				return "Left";
			case RIGHT:
				return "Right";
			case BIRDS_EYE:
				return "Bird's eye";
			case CAMERA:
				return "Camera";
			}
			throw new IllegalStateException();
		}
	}
	private AttributeListener focalLengthAttributeListener = new AttributeListener() {
		public void attributeChanged(Attribute attribute) {
			fw = (float) camera.focalLength.get() / 35 * component.getWidth();
		}
	};
	private AttributeListener updateAttributeListener = new AttributeListener() {
		public void attributeChanged(Attribute attribute) {
			if (attribute == viewTranslation || attribute == viewRotation || attribute == viewScale) {
				computeMatrices();
			} else if (attribute == viewType) {
				viewRotation.setValueAdjusting(true);
				switch(viewType.get()) {
				case FRONT:
					viewRotation.x.set(0);
					viewRotation.y.set(0);
					break;
				case BACK:
					viewRotation.x.set(0);
					viewRotation.y.set(180);
					break;
				case TOP:
					viewRotation.x.set(90);
					viewRotation.y.set(0);
					break;
				case BOTTOM:
					viewRotation.x.set(-90);
					viewRotation.y.set(0);
					break;
				case LEFT:
					viewRotation.x.set(0);
					viewRotation.y.set(90);
					break;
				case RIGHT:
					viewRotation.x.set(0);
					viewRotation.y.set(-90);
					break;
				case BIRDS_EYE:
					viewRotation.x.set(45);
					viewRotation.y.set(45);
					break;
				}
				viewRotation.setValueAdjusting(false);
				computeMatrices();
			}
			component.repaint();
		}
	};
	
	public Viewport(int id, View view, Iterable<Model> models) {
		this.id = id;
		viewType.set(view);
		this.models = models;
		matrix.setIdentity();
		viewType.addAttributeListener(updateAttributeListener);
		showPoints.addAttributeListener(updateAttributeListener);
		showCurves.addAttributeListener(updateAttributeListener);
		showSurfaces.addAttributeListener(updateAttributeListener);
		viewTranslation.addAttributeListener(updateAttributeListener);
		viewRotation.addAttributeListener(updateAttributeListener);
		viewScale.addAttributeListener(updateAttributeListener);
	}

	
	public String getName() {
		return "Viewport " + id;
	}


	public void setParent(JPatchObject parent) {
		// TODO Auto-generated method stub
		
	}


	public String getInfo() {
		if (camera == null) {
			return "Viewport " + id + ": " + viewType.get();
		} else {
			return "Viewport " + id + ": " + camera.getName() + " " + camera.focalLength.get() + "mm";
		}
	}
	
	public abstract void draw();
	
	public void setView(View view) {
		viewType.set(view);
		if (camera != null) {
			camera.focalLength.removeAttributeListener(focalLengthAttributeListener);
		}
		camera = null;
	}
	
	public void setView(Camera camera) {
		if (this.camera != null) {
			this.camera.focalLength.removeAttributeListener(focalLengthAttributeListener);
		}
		this.camera = camera;
		camera.focalLength.addAttributeListener(focalLengthAttributeListener);
		viewType.set(View.CAMERA);
	}
	
	public Component getComponent() {
		return component;
	}
	
	protected void computeMatrices() {
		double scale = viewScale.get();
		double x = Math.toRadians(viewRotation.x.get());
		double y = Math.toRadians(viewRotation.y.get());
		double sx = Math.sin(x);
		double cx = Math.cos(x);
		double sy = Math.sin(y);
		double cy = Math.cos(y);
		
		matrix.m00 = cy * scale;
		matrix.m01 = sy * sx * scale;
		matrix.m02 = sy * cx * scale;
		matrix.m10 = 0;
		matrix.m11 = cx * scale;
		matrix.m12 = -sx * scale;
		matrix.m20 = -sy * scale;
		matrix.m21 = cy * sx * scale;
		matrix.m22 = cy * cx * scale;
		matrix.m03 = viewTranslation.x.get() * scale;
		matrix.m13 = viewTranslation.y.get() * scale;
		matrix.m23 = 0;
	}
	
	protected abstract void drawGrid();
	protected abstract void drawOrigin();
	protected abstract void drawModel(Model model);
	
	protected abstract void drawPatch(Patch patch);
	protected abstract void drawInfo();
	
	protected abstract void drawLine(double x0, double y0, double z0, double x1, double y1, double z1);
	
	protected void drawCurve(ControlPoint cp) {
		cp.getPos(p0);
		cp.getPathSegmentCVs(p1, p2, p3);
		matrix.transform(p0);
		matrix.transform(p1);
		matrix.transform(p2);
		matrix.transform(p3);
		drawCurveSegment(p0.x, p0.y, p0.z, p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, p3.x, p3.y, p3.z, false, 0);
		do {
			cp = cp.getNextNonHook();
			if (cp == null) {
				break;
			}
			p0.set(p3);
			cp.getPathSegmentCVs(p1, p2, p3);
			matrix.transform(p1);
			matrix.transform(p2);
			matrix.transform(p3);
			drawCurveSegment(p0.x, p0.y, p0.z, p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, p3.x, p3.y, p3.z, false, 0);
		} while (!cp.isLoop());
	}
	
	protected void drawCurveSegment(
			double x0, double y0, double z0,
			double x1, double y1, double z1,
			double x2, double y2, double z2,
			double x3, double y3, double z3,
			boolean simple, int level
	) {
		double error;
		if (camera == null) {
			/* orthographic projection */
			error = subdiv(x0, y0, x1, y1, x2, y2, x3, y3, simple);
		} else {
			/* prespective projection */
			if ((z0 < nearClip || z1 < nearClip || z2 < nearClip || z3 < nearClip) && (z0 >= nearClip || z1 >= nearClip || z2 >= nearClip || z3 >= nearClip)) {
				error = Float.MAX_VALUE;	// curve intersects near clipping plane - set high error value to force subdivision
			} else {
				error = subdiv(x0 / z0 * fw, y0 / z0 * fw, x1 / z1 * fw, y1 / z1 * fw, x2 / z2 * fw, y2 / z2 * fw, x3 / z3 * fw, y3 / z3 * fw, simple);
			}
		}
		
		/* check whether subdiv error is small enough or maxSubdiv has been reached */
		if (error < RENDERER_SETTINGS.realtimeRenererQuality || level >= maxSubdiv ) {
			drawLine(x0, y0, z0, x3, y3, z3);	// draw curve as line
		} else {
			/* split curve using deCasteljau algorithm */
			double ax1 = (x0 + x1) * 0.5f;
			double ay1 = (y0 + y1) * 0.5f;
			double az1 = (z0 + z1) * 0.5f;
			double bx2 = (x2 + x3) * 0.5f;
			double by2 = (y2 + y3) * 0.5f;
			double bz2 = (z2 + z3) * 0.5f;
			double cx = (x1 + x2) * 0.5f;
			double cy = (y1 + y2) * 0.5f;
			double cz = (z1 + z2) * 0.5f;
			double ax2 = (ax1 + cx) * 0.5f;
			double ay2 = (ay1 + cy) * 0.5f;
			double az2 = (az1 + cz) * 0.5f;
			double bx1 = (cx + bx2) * 0.5f;
			double by1 = (cy + by2) * 0.5f;
			double bz1 = (cz + bz2) * 0.5f;
			cx = (ax2 + bx1) * 0.5f;
			cy = (ay2 + by1) * 0.5f;
			cz = (az2 + bz1) * 0.5f;
			
			/* recursively call drawCurveSegment(...) with the two new (split) curves */
			drawCurveSegment(
					x0, y0, z0,
					ax1, ay1, az1,
					ax2, ay2, az2,
					cx, cy, cz,
					true, ++level
			);
			drawCurveSegment(
					cx, cy, cz,
					bx1, by1, bz1,
					bx2, by2, bz2,
					x3, y3, z3,
					true, ++level
			);
		}
	}
	
	protected double subdiv(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3, boolean simple) {
		if (simple) {
			double dx = x0 - x1 - x2 + x3;
			double dy = x0 - x1 - x2 + x3;
			return (float) (Math.sqrt(dx * dx + dy * dy));
		} else {
			double dx0 = 4 * x0 - 6 *  x1 + 2 * x3;
			double dy0 = 4 * y0 - 6 *  y1 + 2 * y3;
			double dx1 = 2 * x0 - 6 *  x2 + 4 * x3;
			double dy1 = 2 * y0 - 6 *  y2 + 4 * y3;
			return (float) (Math.sqrt(dx0 * dx0 + dy0 * dy0) + Math.sqrt(dx1 * dx1 + dy1 * dy1));
		}
	}
}
