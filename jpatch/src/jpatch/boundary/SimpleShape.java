package jpatch.boundary;

import javax.vecmath.*;
import jpatch.entity.*;

public class SimpleShape {
	Point3f[] ap3Points;
	Vector3f[] av3Normals;
	int[] aiTriangles;
	int[] aiNormalIndices;
	MaterialProperties mp;
	
	public Point3f[] getPoints() {
		Point3f[] ap3 = new Point3f[ap3Points.length];
		for (int p = 0; p < ap3Points.length; p++) {
			ap3[p] = new Point3f(ap3Points[p]);
		}
		return ap3;
	}
	
	public Vector3f[] getNormals() {
		Vector3f[] av3 = new Vector3f[av3Normals.length];
		for (int n = 0; n < av3Normals.length; n++) {
			av3[n] = new Vector3f(av3Normals[n]);
		}
		return av3;
	}
	
	public int[] getTriangles() {
		return aiTriangles;
	}
	
	public int[] getNormalIndices() {
		return aiNormalIndices;
	}
	
	public MaterialProperties getMaterialProperties() {
		return mp;
	}
	
	public void setPoints(Point3f[] ap3) {
		ap3Points = ap3;
	}
	
	public void setNormals(Vector3f[] av3) {
		av3Normals = av3;
	}
	
	public void setTriangles(int[] ai) {
		aiTriangles = ai;
	}
	
	public void setNormalIndices(int[] ai) {
		aiNormalIndices = ai;
	}
	
	public void setMaterialProperties(MaterialProperties mp) {
		this.mp = mp;
	}
	
	public void transform(Matrix4f matrix) {
		for (int i = 0; i < ap3Points.length; i++) {
			matrix.transform(ap3Points[i]);
		}
		for (int i = 0; i < av3Normals.length; i++) {
			matrix.transform(av3Normals[i]);
			av3Normals[i].normalize();
		}
	}
	
	public void transform(Matrix3f matrix) {
		for (int i = 0; i < ap3Points.length; i++) {
			matrix.transform(ap3Points[i]);
		}
		for (int i = 0; i < av3Normals.length; i++) {
			matrix.transform(av3Normals[i]);
			av3Normals[i].normalize();
		}
	}
	
	public void translate(Vector3f vector) {
		for (int i = 0; i < ap3Points.length; i++) {
			ap3Points[i].add(vector);
		}
	}
	
	public void setColor(float r, float g, float b) {
		mp = new MaterialProperties();
		mp.red = r;
		mp.green = g;
		mp.blue = b;
		mp.ambient = 0.4f;
		mp.diffuse = 0.9f;
		mp.specular = 0.3f;
		mp.roughness = 0.01f;
	}
	
	public static SimpleShape createCube(float size, float r, float g, float b) {
		SimpleShape s = new SimpleShape();
		s.setColor(r, g, b);
		s.setCube(size);
		return s;
	}
	
	public static SimpleShape createArrow(float length, float size, float r, float g, float b) {
		SimpleShape s = new SimpleShape();
		s.setColor(r, g, b);
		s.setArrow(length, size);
		return s;
	}
	
	//public void setBone() {
	//	float r = 0.2;
	//		float a = 0.2;
	//	ap3Points = new Point3f[] {
	//		new Point3f(0,0,0);
	//		new Point3f(r,0,a);
	//		new Point3f(0,r,a);
	//		new Point3f(-r,0,a);
	//		new Point3f(0,-r,a);
	//		new Point3f(0,0,1);
	//	}
		
			
	public void setArrow(float length, float size) {
		ap3Points = new Point3f[] {
			new Point3f( -size, -size,  length),
			new Point3f(  size, -size,  length),
			new Point3f(  size,  size,  length),
			new Point3f( -size,  size,  length),
			new Point3f(     0,     0, 0)
		};	
		av3Normals = new Vector3f[] {
			new Vector3f(  0,  length, -size),
			new Vector3f( -length,  0, -size),
			new Vector3f(  0, -length, -size),
			new Vector3f(  length,  0, -size),
			new Vector3f(  0,  0,  1),
		};
		aiTriangles = new int[] {
			4,2,3,
			4,3,0,
			4,0,1,
			4,1,2,
			2,1,0,
			0,3,2
		};
		aiNormalIndices = new int[] {
			0,
			1,
			2,
			3,
			4,
			4
		};
	}
	
	public void setCube(float size) {
		ap3Points = new Point3f[] {
			new Point3f( -size, -size, -size),
			new Point3f(  size, -size, -size),
			new Point3f(  size, -size,  size),
			new Point3f( -size, -size,  size),
			new Point3f( -size,  size, -size),
			new Point3f(  size,  size, -size),
			new Point3f(  size,  size,  size),
			new Point3f( -size,  size,  size)
		};	
		av3Normals = new Vector3f[] {
			new Vector3f(  0,  0, -1),
			new Vector3f(  1,  0,  0),
			new Vector3f(  0,  0,  1),
			new Vector3f( -1,  0,  0),
			new Vector3f(  0,  1,  0),
			new Vector3f(  0, -1,  0)
		};
		aiTriangles = new int[] {
			0,1,5,
			5,4,0,
			1,2,6,
			6,5,1,
			2,3,7,
			7,6,2,
			3,0,4,
			7,4,3,
			4,5,6,
			6,7,4,
			0,3,2,
			2,1,0
		};
		aiNormalIndices = new int[] {
			0,0,
			1,1,
			2,2,
			3,3,
			4,4,
			5,5
		};
	}
}
