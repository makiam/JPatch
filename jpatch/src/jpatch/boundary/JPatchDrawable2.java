package jpatch.boundary;

import java.awt.*;
import java.awt.image.*;
import javax.vecmath.*;

import jpatch.entity.*;

public interface JPatchDrawable2 {
	public static final int COLOR_BUFFER = 1;
	public static final int DEPTH_BUFFER = 2;
	public static final int ORTHOGONAL = 1;
	public static final int PERSPECTIVE = 2;
	
	public Component getComponent();
	public void display();
	public void clear(int mode, Color3f color);
	public void setProjection(int projection);
	public void setTransform(Matrix4f transform);
	public void setClipping(float near, float far);
	public void setFocalLength(float focalLength);
	public void setGhostRenderingEnabled(boolean enable);
	public void setTransparentRenderingEnabled(boolean enable);
	public void setLightingEnable(boolean enable);
	public void setColor(Color3f color);
	public void setColor(Color4f color);
	public void setMaterial(MaterialProperties mp);
	public void setLighting(RealtimeLighting lighting);
	public void setPointSize(int size);
	public void drawPoint(Point3f p);
	public void drawLine(Point3f p0, Point3f p1);
	public void drawImage(BufferedImage image, int x, int y, float scaleX, float scaleY);
	public void drawTriangle(Point3f p0, Point3f p1, Point3f p2);
	public void drawTriangle(Point3f p0, Color3f c0, Point3f p1, Color3f c1, Point3f p2, Color3f c2);
	public void drawTriangle(Point3f p0, Color4f c0, Point3f p1, Color4f c1, Point3f p2, Color4f c2);
	public void drawTriangle(Point3f p0, Vector3f n0, Point3f p1, Vector3f n1, Point3f p2, Vector3f n2);
	public void drawCurve(Curve curve);
	public void drawString(String string, int x, int y);
	public boolean isDepthBufferSupported();
	public boolean isShadingSupported();
	public boolean isLightingSupported();
	public boolean isTransformSupported();
}
