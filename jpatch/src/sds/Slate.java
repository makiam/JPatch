package sds;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;

import javax.vecmath.*;

public class Slate {
	final Point3d[][] fans = new Point3d[4][];
	final Point3f[][] screenFans = new Point3f[4][];
	final Point3f[] limitPoints = new Point3f[4];
	final Vector3f[] limitNormals = new Vector3f[4];
	final Point3f[][] facePoints = new Point3f[4][];
	final Point3f[][] edgePoints = new Point3f[4][];
	int subdivLevel = 0;
	
	Slate(Point3d[][] fans) {
		this.fans[0] = fans[0].clone();
		this.fans[1] = fans[1].clone();
		this.fans[2] = fans[2].clone();
		this.fans[3] = fans[3].clone();
		screenFans[0] = new Point3f[fans[0].length];
		screenFans[1] = new Point3f[fans[1].length];
		screenFans[2] = new Point3f[fans[2].length];
		screenFans[3] = new Point3f[fans[3].length];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < fans[i].length; j++) {
				screenFans[i][j] = new Point3f();
			}
		}
		for (int corner = 0; corner < 4; corner++) {
			int prevCorner = (corner + 3) % 4;
			int nextCorner = (corner + 1) % 4;
			int nextNextCorner = (corner + 2) % 4;
			int valence = screenFans[corner].length / 2 + 2;
			
			facePoints[corner] = new Point3f[valence];
			facePoints[corner][0] = screenFans[nextCorner][1];
			facePoints[corner][1] = screenFans[nextNextCorner][0];
			facePoints[corner][2] = screenFans[prevCorner][screenFans[prevCorner].length - 1];
			int n = 3;
			for (int i = 2; i < screenFans[corner].length; i+= 2) {
				facePoints[corner][n++] = screenFans[corner][i];
			}
			
			edgePoints[corner] = new Point3f[valence];
			edgePoints[corner][0] = screenFans[nextCorner][0];
			edgePoints[corner][1] = screenFans[prevCorner][0];
			n = 2;
			for (int i = 1; i < screenFans[corner].length; i+= 2) {
				edgePoints[corner][n++] = screenFans[corner][i];
			}
			
			limitPoints[corner] = new Point3f();
			limitNormals[corner] = new Vector3f();
		}
	}
	
	public void transform(Matrix4f matrix) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < fans[i].length; j++) {
				Point3f p = screenFans[i][j];
				p.set(fans[i][j]);
				matrix.transform(p);
			}
		}
	}
	
	public int project(int halfWidth, int halfHeight) {
		int xmin = Integer.MAX_VALUE;
		int xmax = Integer.MIN_VALUE;
		int ymin = Integer.MAX_VALUE;
		int ymax = Integer.MIN_VALUE;
		for (int corner = 0; corner < 4; corner++) {
			final int valence = screenFans[corner].length / 2 + 2;
			float f0 = 0;
			float f1 = 0;
			float f2 = 0;
			float e0 = 0;
			float e1 = 0;
			float e2 = 0;
			for (int i = 0; i < valence; i++) {
				Point3f f = facePoints[corner][i];
				Point3f e = edgePoints[corner][i];
				f0 += f.x;
				f1 += f.y;
				f2 += f.z;
				e0 += e.x;
				e1 += e.y;
				e2 += e.z;
			}
			final float ik = 1.0f / (valence * (valence + 5));			// TODO:
			final float edgeWeight = 4;										// precompute these values
			final float faceWeight = 1;										// for each valence and
			final float pointWeight = valence * valence;						// use loopup table
			f0 *= faceWeight;
			f1 *= faceWeight;
			f2 *= faceWeight;
			e0 *= edgeWeight;
			e1 *= edgeWeight;
			e2 *= edgeWeight;
			limitPoints[corner].set(
					(e0 + f0 + screenFans[corner][0].x * pointWeight) * ik,
					(e1 + f1 + screenFans[corner][0].y * pointWeight) * ik,
					(e2 + f2 + screenFans[corner][0].z * pointWeight) * ik
			);
			Point3f p = screenFans[corner][0];
			int x = (int) p.x;
			int y = (int) p.y;
			if (x < xmin) xmin = x;
			if (x > xmax) xmax = x;
			if (y < ymin) ymin = y;
			if (y > ymax) ymax = y;
			p = limitPoints[corner];
			x = (int) p.x;
			y = (int) p.y;
			if (x < xmin) xmin = x;
			if (x > xmax) xmax = x;
			if (y < ymin) ymin = y;
			if (y > ymax) ymax = y;
		}
		if (xmax < -halfWidth || xmin > halfWidth || ymax < -halfHeight || ymin > halfHeight) {
			subdivLevel = -1;
			return subdivLevel;
		}
		int dx = xmax - xmin;
		int dy = ymax - ymin;
		int s = Math.max(dx, dy);
		s >>= 4;
		if ((s & 0xffffffe0) > 0) {
			subdivLevel = 6;
		} else if ((s & 0x10) > 0) {
			subdivLevel = 5;
		} else if ((s & 0x8) > 0) {
			subdivLevel = 4;
		} else if ((s & 0x4) > 0) {
			subdivLevel = 3;
		} else if ((s & 0x2) > 0) {
			subdivLevel = 2;
		} else {
			subdivLevel = 1;
		}
		return subdivLevel;
	}
}
		
		
		
		
	
//	public int getSubdivLevel(Matrix4f matrix, int halfWidth, int halfHeight) {
//		int s = computeScreenSize(matrix, halfWidth, halfHeight);
//		if (s < 0) {
//			return -1;
//		}
//		s /= 12;
//		if ((s & 0xffffffe0) > 0) {
//			return 6;
//		} else if ((s & 0x10) > 0) {
//			return 5;
//		} else if ((s & 0x8) > 0) {
//			return 4;
//		} else if ((s & 0x4) > 0) {
//			return 3;
//		} else if ((s & 0x2) > 0) {
//			return 2;
//		} else {
//			return 1;
//		}
//	}
	
//	private int computeScreenSize(Matrix4f matrix, int halfWidth, int halfHeight) {
//		Point3f p = new Point3f();
//		p.set(fans[0][0]);
//		matrix.transform(p);
//		int xmin = (int) p.x;
//		int xmax = xmin;
//		int ymin = (int) p.y;
//		int ymax = ymin;
//		for (int i = 1; i < 4; i++) {
//			p.set(fans[i][0]);
//			matrix.transform(p);
//			int x = (int) p.x;
//			int y = (int) p.y;
//			if (x < xmin) {
//				xmin = x;
//			} else if (x > xmax) {
//				xmax = x;
//			}
//			if (y < ymin) {
//				ymin = y;
//			} else if (y > ymax) {
//				ymax = y;
//			}
//		}
//		if (xmax < -halfWidth || xmin > halfWidth || ymax < -halfHeight || ymin > halfHeight) {
//			return -1;
//		}
//		int dx = xmax - xmin;
//		int dy = ymax - ymin;
//		return (dx > dy) ? dx : dy; 
//	}
	
//		assert sides == 4;
//		HalfEdge e = edge;
//		Point3d p0 = e.vertex.position;
//		e = e.next;
//		Point3d p1 = e.vertex.position;
//		e = e.next;
//		Point3d p2 = e.vertex.position;
//		Point3d p3 = e.next.vertex.position;
//		
//		// TODO z-devide for perspective viewports
//		
//		int x0 = (int) p0.x;
//		int x1 = (int) p1.x;
//		int x2 = (int) p2.x;
//		int x3 = (int) p3.x;
//		int y0 = (int) p0.y;
//		int y1 = (int) p1.y;
//		int y2 = (int) p2.y;
//		int y3 = (int) p3.y;
//		int xmin = x0;
//		int xmax = x0;
//		int ymin = y0;
//		int ymax = y0;
//		if (x1 < xmin) xmin = x1;
//		if (x2 < xmin) xmin = x2;
//		if (x3 < xmin) xmin = x3;
//		if (x1 > xmax) xmax = x1;
//		if (x2 > xmax) xmax = x2;
//		if (x3 > xmax) xmax = x3;
//		if (y1 < ymin) ymin = y1;
//		if (y2 < ymin) ymin = y2;
//		if (y3 < ymin) ymin = y3;
//		if (y1 > ymax) ymax = y1;
//		if (y2 > ymax) ymax = y2;
//		if (y3 > ymax) ymax = y3;
//		int dx = xmax - xmin;
//		int dy = ymax - ymin;
//		return (dx > dy) ? dx : dy; 
//	}

