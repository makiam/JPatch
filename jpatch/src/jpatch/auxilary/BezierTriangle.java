package jpatch.auxilary;

import javax.vecmath.*;

public class BezierTriangle
{
	private Point3f p003;
	private Point3f p102;
	private Point3f p201;
	private Point3f p300;
	private Point3f p210;
	private Point3f p120;
	private Point3f p030;
	private Point3f p021;
	private Point3f p012;
	private Point3f p111;
	
	private static final float r = 1f/3f;
	
	public BezierTriangle(Point3f p003, Point3f p102, Point3f p201, Point3f p300, Point3f p210, Point3f p120, Point3f p030, Point3f p021, Point3f p012, Point3f p111) {
		this.p003 = p003;
		this.p102 = p102;
		this.p201 = p201;
		this.p300 = p300;
		this.p210 = p210;
		this.p120 = p120;
		this.p030 = p030;
		this.p021 = p021;
		this.p012 = p012;
		this.p111 = p111;
	}
	
	public BezierTriangle(Point3f p003, Point3f p102, Point3f p201, Point3f p300, Point3f p210, Point3f p120, Point3f p030, Point3f p021, Point3f p012) {
		this(p003,p102,p201,p300,p210,p120,p030,p021,p012,new Point3f(0,0,0));
		setCentroid();
	}
	
	public Point3f computeCentroid() {
		Point3f p111 = new Point3f(0,0,0);
		p111.add(Functions.parallelogram(p003,p102,p012));
		p111.add(Functions.parallelogram(p030,p120,p021));
		p111.add(Functions.parallelogram(p300,p201,p210));
		p111.scale(r);
		return p111;
	}
	
	public void setCentroid() {
		p111.set(computeCentroid());
	}
	
	
	
	public Point3f getPoint(float u, float v, float w) {
	 	float b300 = w*w*w;
	 	float b030 = u*u*u;
	 	float b003 = v*v*v;
	 	float b210 = 3*w*w*u;
	 	float b120 = 3*w*u*u;
	 	float b201 = 3*w*w*v;
	 	float b021 = 3*u*u*v;
	 	float b102 = 3*w*v*v;
	 	float b012 = 3*u*v*v;
	 	float b111 = 6*w*u*v;
	 	
	 	float x = p300.x*b300 + p030.x*b030 + p003.x*b003 + p210.x*b210 + p120.x*b120 + p201.x*b201 + p021.x*b021 + p102.x*b102 + p012.x*b012 + p111.x*b111;
	 	float y = p300.y*b300 + p030.y*b030 + p003.y*b003 + p210.y*b210 + p120.y*b120 + p201.y*b201 + p021.y*b021 + p102.y*b102 + p012.y*b012 + p111.y*b111;
	 	float z = p300.z*b300 + p030.z*b030 + p003.z*b003 + p210.z*b210 + p120.z*b120 + p201.z*b201 + p021.z*b021 + p102.z*b102 + p012.z*b012 + p111.z*b111;
	 	
	 	return new Point3f(x,y,z);
	}
	
		public Vector3f getNormal(float u, float v, float w) {
		Vector3f du = getPoint_du(u,v,w);
		Vector3f dv = getPoint_dv(u,v,w);
		Vector3f n = new Vector3f();
		n.cross(du,dv);
		n.normalize();
		return n;
	}
	
	public Vector3f getPoint_du(float u, float v, float w) {
		float dw = -1;
		float dw2 = -2 + 2*v + 2*u;
		float dw3 = -3 + 6*v - 6*u*v - 3*v*v + 6*u - 3*u*u;
		
	 	float b300 = dw3;
	 	float b030 = 2*u*u;
	 	float b003 = 0;
	 	float b210 = 3*(1 - 4*u - 2*v + 4*u*v + 3*u*u + v*v);
	 	float b120 = 3*(2*u - 3*u*u - 2*u*v);
	 	float b201 = 3*dw2*v;
	 	float b021 = 6*u*v;
	 	float b102 = 3*dw*v*v;
	 	float b012 = 3*v*v;
	 	float b111 = 6*(v - 2*u*v - v*v);
	 	
	 	float x = p300.x*b300 + p030.x*b030 + p003.x*b003 + p210.x*b210 + p120.x*b120 + p201.x*b201 + p021.x*b021 + p102.x*b102 + p012.x*b012 + p111.x*b111;
	 	float y = p300.y*b300 + p030.y*b030 + p003.y*b003 + p210.y*b210 + p120.y*b120 + p201.y*b201 + p021.y*b021 + p102.y*b102 + p012.y*b012 + p111.y*b111;
	 	float z = p300.z*b300 + p030.z*b030 + p003.z*b003 + p210.z*b210 + p120.z*b120 + p201.z*b201 + p021.z*b021 + p102.z*b102 + p012.z*b012 + p111.z*b111;
	 	
	 	return new Vector3f(x,y,z);
	}
	
	public Vector3f getPoint_dv(float u, float v, float w) {
		float dw = -1;
		float dw2 = -2 + 2*v + 2*u;
		float dw3 = -3 + 6*v - 6*u*v - 3*v*v + 6*u - 3*u*u;
		
	 	float b300 = dw3;
	 	float b030 = 0;
	 	float b003 = 2*v*v;
	 	float b210 = 3*dw2*u;
	 	float b120 = 3*dw*u*u;
	 	float b201 = 3*(1 - 4*v - 2*u + 4*u*v + 3*v*v + u*u);
	 	float b021 = 3*u*u;
	 	float b102 = 3*(2*v - 3*v*v - 2*u*v);
	 	float b012 = 6*u*v;
	 	float b111 = 6*(u - 2*u*v - u*u);
	 	
	 	float x = p300.x*b300 + p030.x*b030 + p003.x*b003 + p210.x*b210 + p120.x*b120 + p201.x*b201 + p021.x*b021 + p102.x*b102 + p012.x*b012 + p111.x*b111;
	 	float y = p300.y*b300 + p030.y*b030 + p003.y*b003 + p210.y*b210 + p120.y*b120 + p201.y*b201 + p021.y*b021 + p102.y*b102 + p012.y*b012 + p111.y*b111;
	 	float z = p300.z*b300 + p030.z*b030 + p003.z*b003 + p210.z*b210 + p120.z*b120 + p201.z*b201 + p021.z*b021 + p102.z*b102 + p012.z*b012 + p111.z*b111;
	 	
	 	return new Vector3f(x,y,z);
	}	
	
	public Point3f getCenterPoint() {
		return getPoint(1f/3f,1f/3f,1f/3f);
	}
	
	public Vector3f getCenterNormal() {
		return getNormal(1f/3f,1f/3f,1f/3f);
	}
}

