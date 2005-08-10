package jpatch.boundary;

import java.awt.*;
import javax.vecmath.*;
import jpatch.entity.*;

public interface JPatchDrawable {
	public Graphics getGraphics();
	public Image getImage();
	public void setColor(Color color);
	public void setColor(int rgb);
	public void clear();
	public void clearZBuffer();
	public void drawLine(int x0, int y0, int x1, int y1);
	public void drawRect(int x0, int y0, int x1, int y1);
	public void drawLine3D(Point3f p0, Point3f p1);
	public void drawGhostLine3D(Point3f p0, Point3f p1, int ghost);
	public void drawPoint3D(Point3f p, int size);
	public void drawXPoint3D(Point3f p);
	public void drawCurveSegment(Point3f a, Point3f b, Point3f c, Point3f d);
	public void drawJPatchCurve3D(Curve curve, Matrix4f matrix);
	//public void drawBicubicPatchPhong(Point3f[] controlPointArray, MaterialProperties materialProperties);
	public void drawBicubicPatchGourad(Point3f[] controlPointArray, MaterialProperties materialProperties);
	public void drawBicubicPatchFlat(Point3f[] controlPointArray, MaterialProperties materialProperties);
	public void drawHashPatchGourad(Point3f[] controlPointArray, Vector3f[] normalArray, int[] levelArray, MaterialProperties materialProperties);
	public void drawHashPatchFlat(Point3f[] controlPointArray, int[] levelArray, MaterialProperties materialProperties);
	public void drawSimpleShape(SimpleShape shape, Matrix4f matrix);
	public void setLighting(Lighting lighting);
	public Lighting getLighting();
}

