package jpatch.auxilary;

import javax.vecmath.*;

public class Line2f {
	public Point2f p2;
	public Vector2f v2;
	
	public Line2f(Point2f P, Vector2f V) {
		p2 = P;
		v2 = V;
	}
	
	public Line2f(Point2f A, Point2f B) {
		p2 = A;
		v2 = new Vector2f(B);
		v2.sub(p2);
	}
	
	public Point2f evaluate(float t) {
		Vector2f v = new Vector2f(v2);
		v.scale(t);
		Point2f p = new Point2f(p2);
		p.add(v);
		return p;
	}
	
	public Point2f intersection(Line2f line) {
		float x1 = p2.x;
		float x2 = p2.x + v2.x;
		float x3 = line.p2.x;
		float x4 = line.p2.x + line.v2.x;
		float y1 = p2.y;
		float y2 = p2.y + v2.y;
		float y3 = line.p2.y;
		float y4 = line.p2.y + line.v2.y;
		Matrix2f matrixA = new Matrix2f(x1,y1,x2,y2);
		Matrix2f matrixB = new Matrix2f(x3,y3,x4,y4);
		Matrix2f matrixC = new Matrix2f(x1-x2,y1-y2,x3-x4,y3-y4);
		float dA = matrixA.determinant();
		float dB = matrixB.determinant();
		float dC = matrixC.determinant();
		Matrix2f matrixX = new Matrix2f(dA,x1-x2,dB,x3-x4);
		Matrix2f matrixY = new Matrix2f(dA,y1-y2,dB,y3-y4);
		float x = matrixX.determinant()/dC;
		float y = matrixY.determinant()/dC;
		return new Point2f(x,y);
	}
	
	public String toString() {
		return "Line from " + p2.toString() + " vector " + v2.toString();
	}
}
