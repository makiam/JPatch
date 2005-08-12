package jpatch.boundary;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import javax.vecmath.*;

import jpatch.entity.*;

public class JPatchDrawable2D implements JPatchDrawable2 {
	
	private JPatchDrawableEventListener listener;
	private Component component;
	private Graphics2D g;
	//private Matrix4f m4Transform = new Matrix4f();
	private boolean bPerspective = false;
	private float fFocalLength = 50;
	private float fW;
	private float fNearClip = 1;
	private int iPointSize = 3;
	private int iXoff = 0;
	private int iYoff = 0;
	private VolatileImage image;
	
	public JPatchDrawable2D(final JPatchDrawableEventListener listener, boolean lightweight) {
		this.listener = listener;
		if (lightweight) {
			component = new JPanel() {
				private static final long serialVersionUID = 1L;
				public void paint(Graphics g) {
					if (image == null || image.getWidth(this) != getWidth() || image.getHeight(this) != getHeight()) {
						updateImage();
					}
					g.drawImage(image, 0, 0, this);
				}
				public void update(Graphics g) {
					paint(g);
				}
				public String toString() {
					return "Viewport2D JPanel @" + hashCode();
				}
			};
		} else {
			component = new Canvas() {
				private static final long serialVersionUID = 1L;
				public void paint(Graphics g) {
					if (image == null || image.getWidth(this) != getWidth() || image.getHeight(this) != getHeight()) {
						updateImage();
					}
					g.drawImage(image, 0, 0, this);
				}
				public void update(Graphics g) {
					paint(g);
				}
				public String toString() {
					return "Viewport2D Canvas @" + hashCode();
				}
			};
		}
		
		component.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				iXoff = e.getComponent().getWidth() >> 1;
				iYoff = e.getComponent().getHeight() >> 1;
				fW = fFocalLength * iXoff / 12.5f;
			}
		});
	}
	
	public String getInfo() {
		return "Java2D renderer";
	}
	
	public void setLighting(RealtimeLighting lighting) { }
	
	
	private void updateImage() {
		image = component.createVolatileImage(component.getWidth(), component.getHeight());
		g = image.createGraphics();
	}
	
	public Component getComponent() {
		return component;
	}
	
	public void drawImage(BufferedImage image, int x, int y, float scaleX, float scaleY) {
		AffineTransform affineTransform = new AffineTransform(scaleX, 0, 0, scaleY, x, y);
		g.drawImage(image, affineTransform, null);
	}
	
	public void drawString(String string, int x, int y) {
		g.drawString(string, x, y);
	}
	
	public void display() {
		if (g == null) updateImage();
		listener.display(JPatchDrawable2D.this);
		Graphics cg = component.getGraphics();
		if (cg != null) component.paint(cg);
	}
	
	public void clear(int mode, Color3f color) {
		if ((mode & COLOR_BUFFER) != 0) {
			g.setColor(color.get());
			g.fillRect(0, 0, component.getWidth(), component.getHeight());
		}
	}
	
	public void setProjection(int projection) {
		bPerspective = (projection == PERSPECTIVE);
	}
	
	public void setClipping(float near, float far) {
		fNearClip = near;
	}
	
	//public void setTransformation(Matrix4f transform) {
	//	m4Transform.set(transform);
	//}
	
	public void setFocalLength(float focalLength) {
		fFocalLength = focalLength;
		fW = fFocalLength * iXoff / 12.5f;
	}
	
	public void setGhostRenderingEnabled(boolean enable) { }
	
	public void setTransparentRenderingEnabled(boolean enable) { }
	
	public void setLightingEnable(boolean enable) { }
	
	public void setColor(Color3f color) {
		g.setColor(color.get());
	}
	
	public void setColor(Color4f color) {
		g.setColor(color.get());
	}
	
	public void setMaterial(MaterialProperties mp) {
		throw new UnsupportedOperationException(this.getClass().getName() + " does not support lighting.");
	}
	
	public void setLighting() {
		throw new UnsupportedOperationException(this.getClass().getName() + " does not support lighting.");
	}
	
	public void setTransform(Matrix4f transform) {
		throw new UnsupportedOperationException(this.getClass().getName() + " does not support transform.");
	}
	
	public void setPointSize(int size) {
		iPointSize = size;
	}
	
	public void drawPoint(Point3f p) {
		if (bPerspective) {
			if (p.z > fNearClip) {
				g.fillRect(iXoff + ((int) (p.x / p.z * fW)) - (iPointSize >> 1), iYoff - ((int) (p.y / p.z * fW)) - (iPointSize >> 1), iPointSize, iPointSize);
			}
		} else {
			g.fillRect(iXoff + ((int) p.x) - (iPointSize >> 1), iYoff - ((int) p.y) - (iPointSize >> 1), iPointSize, iPointSize);
		}
	}
	
	public void drawLine(Point3f p0, Point3f p1) {
		if (bPerspective) {
			if (p0.z < fNearClip) {
				if (p1.z < fNearClip) return;
				float s = (fNearClip -p0.z) / (p1.z - p0.z);
				int x = (int) ((p0.x * (1 - s) + p1.x * s) * fW);
				int y = (int) ((p0.y * (1 - s) + p1.y * s) * fW);
				int xx = (int) ((p1.x / p1.z) * fW);
				int yy = (int) ((p1.y / p1.z) * fW);
				g.drawLine(iXoff + x, iYoff - y, iXoff + xx, iYoff - yy);
			} else if (p1.z < fNearClip) {
				float s = (fNearClip - p1.z) / (p0.z - p1.z);
				int x = (int) ((p1.x * (1 - s) + p0.x * s) * fW);
				int y = (int) ((p1.y * (1 - s) + p0.y * s) * fW);
				int xx = (int) ((p0.x / p0.z) * fW);
				int yy = (int) ((p0.y / p0.z) * fW);
				g.drawLine(iXoff + x, iYoff - y, iXoff + xx, iYoff - yy);
			} else {
				int x = (int) ((p0.x / p0.z) * fW);
				int y = (int) ((p0.y / p0.z) * fW);
				int xx = (int) ((p1.x / p1.z) * fW);
				int yy = (int) ((p1.y / p1.z) * fW);
				g.drawLine(iXoff + x, iYoff - y, iXoff + xx, iYoff - yy);
			}
		} else {
			g.drawLine(iXoff + (int) p0.x, iYoff - (int) p0.y, iXoff + (int) p1.x, iYoff - (int) p1.y);
		}
	}
	
	public void drawTriangle(Point3f p0, Point3f p1, Point3f p2) {
		drawLine(p0, p1);
		drawLine(p1, p2);
		drawLine(p2, p0);
	}
	
	public void drawTriangle(Point3f p0, Color3f c0, Point3f p1, Color3f c1, Point3f p2, Color3f c2) {
		throw new UnsupportedOperationException(this.getClass().getName() + " does not support shading");
	}
	
	public void drawTriangle(Point3f p0, Color4f c0, Point3f p1, Color4f c1, Point3f p2, Color4f c2) {
		throw new UnsupportedOperationException(this.getClass().getName() + " does not support shading");
	}
	
	public void drawTriangle(Point3f p0, Vector3f n0, Point3f p1, Vector3f n1, Point3f p2, Vector3f n2) {
		throw new UnsupportedOperationException(this.getClass().getName() + " does not support shading");
	}
	
	public void drawCurve(Curve curve) { }
	
	public boolean isShadingSupported() {
		return false;
	}
	
	public boolean isDepthBufferSupported() {
		return false;
	}
	
	public boolean isLightingSupported() {
		return false;
	}
	
	public boolean isTransformSupported() {
		return false;
	}
	
	public Graphics getGraphics() {
		return g;
	}
}
