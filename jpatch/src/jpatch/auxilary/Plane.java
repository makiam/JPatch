package jpatch.auxilary;

import javax.vecmath.*;

public class Plane {
	public float fA;
	public float fB;
	public float fC;
	public float fD;
	
	private static final int X = 1;
	private static final int Y = 2;
	private static final int Z = 3;
	private int iDominant;
	
	public Plane(float A, float B, float C, float D) {
		fA = A;
		fB = B;
		fC = C;
		fD = D;
		setDominant();
	}
	
	public Plane(Point3f A, Point3f B, Point3f C) {
		Vector3f v3Normal = normal(A,B,C);
		fA = v3Normal.x;
		fB = v3Normal.y;
		fC = v3Normal.z;
		Vector3f v3A = new Vector3f(A);
		fD = -v3Normal.dot(v3A);
		setDominant();
		//System.out.println("plane = " + fA + " " + fB + " " + fC + " " + fD);
	}
	
	public Plane(Point3f p, Vector3f normal) {
		fA = normal.x;
		fB = normal.y;
		fC = normal.z;
		Vector3f v3 = new Vector3f(p);
		fD = -normal.dot(v3);
		setDominant();
	}
	
	public static Vector3f normal(Point3f A, Point3f B, Point3f C) {
		Vector3f v3AB = new Vector3f(B);
		v3AB.sub(A);
		Vector3f v3AC = new Vector3f(C);
		v3AC.sub(A);
		Vector3f v3Normal = new Vector3f();
		v3Normal.cross(v3AB,v3AC);
		v3Normal.normalize();
		return v3Normal;
	}
	
	public Point3f intersection(Line3f line) {
		//System.out.println("intersection");
		//System.out.println("line = " + line);
		//System.out.println("plane = " + fA + " " + fB + " " + fC + " " + fD);
		float t = -(fA * line.p3.x + fB * line.p3.y + fC * line.p3.z + fD) / (fA * line.v3.x + fB * line.v3.y + fC * line.v3.z);
		//System.out.println("t = " + t);
		return line.evaluate(t);
	}
	
	public Line3f normal(Point3f P) {
		return new Line3f(P, new Vector3f(fA,fB,fC));
	}
	
	public Point3f projectedPoint(Point3f P) {
		return intersection(normal(P));
	}
	
	private void setDominant() {
		
		if (Math.abs(fA) > Math.abs(fB)) {
			if (Math.abs(fA) > Math.abs(fC)) {
				iDominant = X;
			} else {
				iDominant = Z;
			}
		} else {
			if (Math.abs(fB) > Math.abs(fC)) {
				iDominant = Y;
			} else {
				iDominant = Z;
			}
		}
		
		
		//System.out.println("iDominant = " + iDominant);
	}
	
	public Point3f getP3(Point2f P) {
		switch(iDominant) {
			case Z:
				return new Point3f(P.x,P.y,-(fA * P.x + fB * P.y + fD) / fC);
			case Y:
				return new Point3f(P.x,-(fA * P.x + fC * P.y + fD) / fB,P.y);
			case X:
				return new Point3f(-(fB * P.x + fC * P.y + fD) / fA,P.x,P.y);
		}
		return null;
	}
	
	public Point2f getP2(Point3f P) {
		switch(iDominant) {
			case Z:
				return new Point2f(P.x,P.y);
			case Y:
				return new Point2f(P.x,P.z);	
			case X:
				return new Point2f(P.y,P.z);
		}
		return null;
	}
}
