package jpatch.boundary;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.vecmath.*;
import jpatch.entity.*;

public final class JPatchDrawableJava2D
implements JPatchDrawable {
	
	private Graphics2D g2;
	private VolatileImage volatileImage;
	private Lighting lighting;
	private JPatchSettings settings = JPatchSettings.getInstance();
	
	public JPatchDrawableJava2D(VolatileImage volatileImage, Lighting lighting) {
		this.volatileImage = volatileImage;
		g2 = (Graphics2D)volatileImage.getGraphics();
		g2.setBackground(settings.cBackground);
		//RenderingHints renderingHints = new RenderingHints(null);
		//renderingHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
		//renderingHints.put(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
		//renderingHints.put(RenderingHints.KEY_COLOR_RENDERING,RenderingHints.VALUE_COLOR_RENDER_SPEED);
		//renderingHints.put(RenderingHints.KEY_DITHERING,RenderingHints.VALUE_DITHER_DISABLE);
		//renderingHints.put(RenderingHints.KEY_FRACTIONALMETRICS,RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
		//renderingHints.put(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		//renderingHints.put(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_SPEED);
		//renderingHints.put(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_PURE);
		//renderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		//g2.setRenderingHints(renderingHints);
		this.lighting = lighting;
	}
	
	public void setLighting(Lighting lighting) {
		this.lighting = lighting;
	}
	
	public Lighting getLighting() {
		return lighting;
	}
	
	public Graphics getGraphics() {
		return g2;
	}
	
	public Image getImage() {
		return volatileImage;
	}
	
	public void clear() {
		g2.setBackground(settings.cBackground);
		g2.clearRect(0, 0, volatileImage.getWidth(), volatileImage.getHeight());
	}
	
	public void clearZBuffer() {
		;
	}
	
	public void setColor(Color color) {
		g2.setColor(color);
	}
	
	public void setColor(int rgb) {
		g2.setColor(new Color(rgb));
	}
	
	public final void drawRect(int x1, int y1, int x2, int y2) {
		g2.drawLine(x1,y1,x2,y1);
		g2.drawLine(x2,y1,x2,y2);
		g2.drawLine(x2,y2,x1,y2);
		g2.drawLine(x1,y2,x1,y1);
	}
	
	public void drawLine(int x0, int y0, int x1, int y1) {
		g2.drawLine(x0, y0, x1, y1);
	}
	
	public void drawLine3D(Point3f p0, Point3f p1) {
		g2.drawLine((int)p0.x, (int)p0.y, (int)p1.x, (int)p1.y);
	}
	
	public void drawGhostLine3D(Point3f p0, Point3f p1, int ghost) {
		g2.drawLine((int)p0.x, (int)p0.y, (int)p1.x, (int)p1.y);
	}
	
	public void drawPoint3D(Point3f p, int size) {
		g2.fillRect((int)p.x - size, (int)p.y - size, size * 2 + 1, size * 2 + 1);
	}
	
	public void drawXPoint3D(Point3f p) {
		int x = (int) p.x;
		int y = (int) p.y;
		int[] X = new int[] { x, x + 3, x, x - 2 };
		int[] Y = new int[] { y - 3, y, y + 3, y };
		g2.fillPolygon(X,Y,4);
	}
	
	public final void drawSimpleShape(SimpleShape shape, Matrix4f matrix) {
		Point3f[] ap3Points = shape.getPoints();
		Vector3f[] av3Normals = shape.getNormals();
		int[] aiTriangles = shape.getTriangles();
		int[] aiNormalIndices = shape.getNormalIndices();
		MaterialProperties mp = shape.getMaterialProperties();
		int triangles = aiTriangles.length / 3;
		
		if (matrix != null) {
			for (int i = 0; i < ap3Points.length; i++) {
				matrix.transform(ap3Points[i]);
			}
			for (int i = 0; i < av3Normals.length; i++) {
				matrix.transform(av3Normals[i]);
				av3Normals[i].normalize();
			}
		}
		
		int p = 0;
		for (int triangle = 0; triangle < triangles; triangle++) {
			Vector3f normal = av3Normals[aiNormalIndices[triangle]];
			if (normal.z < 0) {
				g2.setColor(new Color(lighting.shade(null, normal, mp)));
				g2.fillPolygon(setPoly(ap3Points[aiTriangles[p++]], ap3Points[aiTriangles[p++]], ap3Points[aiTriangles[p++]]));
				//System.out.println("drawPoly()");
			} else {
				p += 3;
			}	
		}
	}
	
	private Polygon setPoly(Point3f a, Point3f b, Point3f c) {
		int[] xPoints = new int[3];
		int[] yPoints = new int[3];
		xPoints[0] = (int)a.x;
		xPoints[1] = (int)b.x;
		xPoints[2] = (int)c.x;
		yPoints[0] = (int)a.y;
		yPoints[1] = (int)b.y;
		yPoints[2] = (int)c.y;
		return new Polygon(xPoints,yPoints,3);
	}
	
	public void drawCurveSegment(Point3f a, Point3f b, Point3f c, Point3f d) {
		g2.draw(new CubicCurve2D.Float(a.x,a.y,b.x,b.y,c.x,c.y,d.x,d.y));
	}
	
	public void drawJPatchCurve3D(Curve curve, Matrix4f matrix) {
		Point3f p3A = new Point3f();
		Point3f p3B = new Point3f();
		Point3f p3C = new Point3f();
		Point3f p3D = new Point3f();
		ControlPoint cp;
		GeneralPath generalPath = new GeneralPath();
		cp = curve.getStart();
		
		/* go to first not hidden point */
		while (cp != null && cp.isHidden()) {
			cp = cp.getNextCheckNextLoop();
		}
		if (cp != null && cp.getNext() != null) {
			p3A.set(cp.getPosition());
			p3B.set(cp.getOutTangent());
			matrix.transform(p3A);
			matrix.transform(p3B);
			generalPath.moveTo(p3A.x,p3A.y);
			cp = cp.getNext();
			loop:
			while (cp != null) {
				if (cp.isHidden()) {
					while (cp != null && cp.isHidden()) {
						cp = cp.getNextCheckLoop();
					}
					if (cp != null) {
						p3D.set(cp.getPosition());
						matrix.transform(p3D);
						generalPath.moveTo(p3D.x, p3D.y);
					} else {
						break loop;
					}
				} else {
					p3C.set(cp.getInTangent());
					p3D.set(cp.getPosition());
					matrix.transform(p3C);
					matrix.transform(p3D);
					generalPath.curveTo(p3B.x, p3B.y, p3C.x, p3C.y, p3D.x, p3D.y);
				}
					
				
				ControlPoint cpNext = cp.getNextCheckLoop();
				if (cpNext != null) { 
					p3B.set(cp.getOutTangent());
					matrix.transform(p3B);
				}
				cp = cpNext;
			}
			g2.draw(generalPath);
		}
	}
	
	public void drawBicubicPatchPhong(Point3f[] controlPointArray, MaterialProperties materialProperties) { }
	public void drawBicubicPatchGourad(Point3f[] controlPointArray, MaterialProperties materialProperties) { }
	public void drawBicubicPatchFlat(Point3f[] controlPointArray, MaterialProperties materialProperties) { }
	
	public void drawHashPatchGourad(Point3f[] controlPointArray, Vector3f[] normalArray, int[] levelArray, MaterialProperties materialProperties) { }
	public void drawHashPatchFlat(Point3f[] controlPointArray, int[] levelArray, MaterialProperties materialProperties) { }

}

