package com.jpatch.entity.sds;

import javax.vecmath.*;

import static com.jpatch.entity.sds.Dicer.*;

public class Slate2 {
	private static final Point3f NULL_POINT = new Point3f();
	
	SlateEdge[][] corners;
	Point3f[][] fans;
	int subdivLevel;
	float normalCone;
	int[] creaseIndex0 = new int[4];
	int[] creaseIndex1 = new int[4];
	
	int creaseType1, creaseType3;
	
//	Slate2(SlateEdge[][] corners) {
//		this.corners = corners;
//		for (int i = 0; i < 4; i++) {
//			corners[i][0].slate = this;
//		}
//	}
	
	Slate2() { }
	
	void setCorners(SlateEdge[][] corners) {
		this.corners = corners;
	}
	
	public SlateEdge[][] getCorners() {
		return corners;
	}
	
	void initCreases() {
		if (corners[0][0].vertex.crease > 0) {
			creaseIndex0[0] = getEdgeIndex(0, corners[0][0].vertex.creaseEdge0);
			creaseIndex1[0] = getEdgeIndex(0, corners[0][0].vertex.creaseEdge1);
			if (creaseIndex0[0] == -1 || creaseIndex1[0] == -1) {
//				System.out.println(corners[0][0].vertex.creaseEdge0 + " " + creaseIndex0[0]);
//				System.out.println(corners[0][0].vertex.creaseEdge1 + " " + creaseIndex1[0]);
//				for (int i = 0; i < corners[0].length; i++) {
//					SlateEdge e = corners[0][i];
//					System.out.println(i + ": " + e);
//				}
				throw new IllegalStateException();
			}
		}
		
		if (corners[1][0].vertex.crease > 0) {
			creaseType1 = CREASE_5_7;				// FIXME check for other creases once hierarchical modeling is possible
			creaseIndex0[1] = 1;
			creaseIndex1[1] = 3;
		} else {
			creaseType1 = POINT;
			creaseIndex0[1] = -1;
			creaseIndex1[1] = -1;
		}
		
		if (corners[2][0].vertex.crease > 0) {
			creaseIndex0[2] = getEdgeIndex(2, corners[2][0].vertex.creaseEdge0);
			creaseIndex1[2] = getEdgeIndex(2, corners[2][0].vertex.creaseEdge1);
			if (creaseIndex0[2] == -1 || creaseIndex1[2] == -1) {
//				System.out.println(corners[2][0].vertex.creaseEdge0 + " " + creaseIndex0[2]);
//				System.out.println(corners[2][0].vertex.creaseEdge1 + " " + creaseIndex1[2]);
				throw new IllegalStateException();
			}
		}
		
		if (corners[3][0].vertex.crease > 0) {
			creaseType3 = CREASE_4_6;				// FIXME check for other creases once hierarchical modeling is possible
			creaseIndex0[3] = 2;
			creaseIndex1[3] = 0;
		} else {
			creaseType3 = POINT;
			creaseIndex0[3] = -1;
			creaseIndex1[3] = -1;
		}
		
		for (int corner = 0; corner < 4; corner += 2) {
			if (creaseIndex0[corner] == 0 || (creaseIndex1[corner] != 0 && creaseIndex0[corner] > creaseIndex1[corner])) {
				/* swap the crease indexes to ensure that the tangent directions are consistent */
				int tmp = creaseIndex0[corner];
				creaseIndex0[corner] = creaseIndex1[corner];
				creaseIndex1[corner] = tmp;
			}
		}
	}
	
	void initFans() {
		initCreases();
		fans = new Point3f[4][];
		fans[0] = new Point3f[Math.max(1, 2 * corners[0].length - 4)];
		fans[0][0] = corners[0][0].vertex.projectedPos;
		int j = 1;
		for (int i = 2; i < corners[0].length; i++) {
			if (corners[0][i] == null) {
				fans[0][j++] = NULL_POINT;
				fans[0][j++] = NULL_POINT;
				continue;
			}
			fans[0][j++] = corners[0][i].pair.vertex.projectedPos;
//			System.out.println("corners " + corners + "[0][" + i + "].slate=" + corners[0][i].slate);
			if (i < corners[0].length - 1) {
				fans[0][j++] = corners[0][i].slate == null ? NULL_POINT : corners[0][i].slate.corners[2][0].vertex.projectedPos;
			}
		}
		
		
		fans[1] = new Point3f[4];
		fans[1][0] = corners[1][0].vertex.projectedPos;
		j = 1;
		for (int i = 2; i < corners[1].length; i++) {
			fans[1][j++] = corners[1][i] == null ? NULL_POINT : corners[1][i].pair.vertex.projectedPos;
			fans[1][j++] = corners[1][i] == null || corners[1][i].slate == null ? NULL_POINT : corners[1][i].slate.corners[3][0].vertex.projectedPos;
			i++;
			fans[1][j++] = corners[1][i] == null ? NULL_POINT : corners[1][i].pair.vertex.projectedPos;
			if (i < corners[1].length - 1) {
				fans[1][j++] = corners[1][i] == null || corners[1][i].slate == null ? NULL_POINT : corners[1][i].slate.corners[1][0].vertex.projectedPos;
			}
		}
		
		fans[2] = new Point3f[2 * corners[2].length - 4];
		fans[2][0] = corners[2][0].vertex.projectedPos;
		j = 1;
		for (int i = 2; i < corners[2].length; i++) {
			fans[2][j++] = corners[2][i].pair.vertex.projectedPos;
			if (i < corners[2].length - 1) {
				fans[2][j++] = corners[2][i].slate.corners[0][0].vertex.projectedPos;
			}
		}
		
		fans[3] = new Point3f[4];
		fans[3][0] = corners[3][0].vertex.projectedPos;
		j = 1;
		for (int i = 2; i < corners[3].length; i++) {
			fans[3][j++] = corners[3][i] == null ? NULL_POINT : corners[3][i].pair.vertex.projectedPos;
			fans[3][j++] = corners[3][i] == null || corners[3][i].slate == null ? NULL_POINT : corners[3][i].slate.corners[1][0].vertex.projectedPos;
			i++;
			fans[3][j++] = corners[3][i] == null ? NULL_POINT : corners[3][i].pair.vertex.projectedPos;
			if (i < corners[3].length - 1) {
				fans[3][j++] = corners[3][i] == null || corners[3][i].slate == null ? NULL_POINT : corners[3][i].slate.corners[3][0].vertex.projectedPos;
			}
		}
	}
	
	int getEdgeIndex(int corner, SlateEdge edge) {
		SlateEdge[] c = corners[corner];
		for (int i = 0, n = c.length; i < n; i++) {
			if (c[i] == edge || c[i] == edge.pair) {
				return i;
			}
		}
		return -1;
	}
	
	void computeNormalCone() {
		float min  = 1;
		for (int i = 0; i < 3; i++) {
			for (int j = i + 1; j < 4; j++) {
				float dot = (float) corners[i][0].vertex.normal.dot(corners[j][0].vertex.normal);
				if (dot < min) {
					min = dot;
				}
			}
		}
		normalCone = 1.0f - 0.5f * min;
	}
	
	/**
	 * This implementation computes the subdivision level based on <ul>
	 * <li>the estimated screen size of the slate in pixels,</li>
	 * <li>the curvature (normal cone) of the slate,</li>
	 * <li>whether or not this slate is part of the surface's silhouette and</li>
	 * <li>whether or not an edge of this slate is part of a crease</li>
	 * </ul>
	 * @param halfWidth
	 * @param halfHeight
	 */
	public void estimateSubdivLevel(int halfWidth, int halfHeight) {
		int xmin = Integer.MAX_VALUE;
		int xmax = Integer.MIN_VALUE;
		int ymin = Integer.MAX_VALUE;
		int ymax = Integer.MIN_VALUE;
		boolean silhouette = true;
		boolean crease = false;
		if (
				corners[0][0].vertex.projectedNormal.z < 0 &&
				corners[1][0].vertex.projectedNormal.z < 0 &&
				corners[2][0].vertex.projectedNormal.z < 0 &&
				corners[3][0].vertex.projectedNormal.z < 0
		) {
			subdivLevel = -1;
			return;
		} else if (
				corners[0][0].vertex.projectedNormal.z > 0 &&
				corners[1][0].vertex.projectedNormal.z > 0 &&
				corners[2][0].vertex.projectedNormal.z > 0 &&
				corners[3][0].vertex.projectedNormal.z > 0
		) {
			silhouette = false;
		}
		for (int corner = 0; corner < 4; corner++) {
//			int valence = corners[corner].length;
//			float fx = 0, fy = 0;
//			float ex = 0, ey = 0;
//			for (int i = 1; i < fans[corner].length; i++) {
//				ex += fans[corner][i].x;
//				ey += fans[corner][i].y;
//				i++;
//				fx += fans[corner][i].x;
//				fy += fans[corner][i].y;
//			}
//			
//			float v2 = valence * valence;
//			float ik = 1.0f / (v2 + 5 * valence);
//			
//			int lx = (int) ((ex * 4 + fx + fans[corner][0].x * v2) * ik);
//			int ly = (int) ((ey * 4 + fy + fans[corner][0].y * v2) * ik);
			
			if (corners[corner][0].creaseSharpness() > 0) {
				crease = true;
			}
			
			int lx = (int) corners[corner][0].vertex.projectedLimit.x;
			int ly = (int) corners[corner][0].vertex.projectedLimit.y;
			
			if (lx < xmin) xmin = lx;
			if (lx > xmax) xmax = lx;
			if (ly < ymin) ymin = ly;
			if (ly > ymax) ymax = ly;
			
			int px = (int) fans[corner][0].x;
			int py = (int) fans[corner][0].y;
			if (px < xmin) xmin = px;
			if (px > xmax) xmax = px;
			if (py < ymin) ymin = py;
			if (py > ymax) ymax = py;
		}
		
		if (xmax < -halfWidth || xmin > halfWidth || ymax < -halfHeight || ymin > halfHeight) {
			subdivLevel = -1;
			return;
		}
		int dx = xmax - xmin;
		int dy = ymax - ymin;
		int s = dx + dy;
		s *= normalCone;
		if (silhouette || crease) {
			s >>= 4;
		} else {
			s >>= 5;
		}
//		if ((s & 0xffffffc0) > 0) {
//			subdivLevel = 5;
//		} else if ((s & 0x30) > 0) {
//			subdivLevel = 4;
//		} else if ((s & 0xc) > 0) {
//			subdivLevel = 3;
//		} else if ((s & 0x2) > 0) {
//			subdivLevel = 2;
//		} else {
//			subdivLevel = 1;
//		}
		if (s > 81)       subdivLevel = 5;
		else if (s > 27 ) subdivLevel = 4;
		else if (s > 9)   subdivLevel = 3;
		else if (s > 3)   subdivLevel = 2;
		else              subdivLevel = 1;
	}
	
//	public void test() {
//		if (corners[0][0].slate != this) throw new IllegalStateException();
//		if (corners[1][0].slate != this) throw new IllegalStateException();
//		if (corners[2][0].slate != this) throw new IllegalStateException();
//		if (corners[3][0].slate != this) throw new IllegalStateException();
//		
//		if (fans[0][1] != fans[1][0]) throw new IllegalStateException();
//		if (fans[0][2] != fans[2][0]) throw new IllegalStateException();
//		if (fans[0][3] != fans[3][0]) throw new IllegalStateException();
//		if (fans[1][1] != fans[2][0]) throw new IllegalStateException();
//		if (fans[1][2] != fans[3][0]) throw new IllegalStateException();
//		if (fans[1][3] != fans[0][0]) throw new IllegalStateException();
//		if (fans[2][1] != fans[3][0]) throw new IllegalStateException();
//		if (fans[2][2] != fans[0][0]) throw new IllegalStateException();
//		if (fans[2][3] != fans[1][0]) throw new IllegalStateException();
//		if (fans[3][1] != fans[0][0]) throw new IllegalStateException();
//		if (fans[3][2] != fans[1][0]) throw new IllegalStateException();
//		if (fans[3][3] != fans[2][0]) throw new IllegalStateException();
//		
//		if (fans[0][fans[0].length - 2] != fans[1][4]) throw new IllegalStateException();
//		if (fans[0][fans[0].length - 1] != fans[1][5]) throw new IllegalStateException();
//		if (fans[1][fans[1].length - 2] != fans[2][4]) throw new IllegalStateException();
//		if (fans[1][fans[1].length - 1] != fans[2][5]) throw new IllegalStateException();
//		if (fans[2][fans[2].length - 2] != fans[3][4]) throw new IllegalStateException();
//		if (fans[2][fans[2].length - 1] != fans[3][5]) throw new IllegalStateException();
//		if (fans[3][fans[3].length - 2] != fans[0][4]) throw new IllegalStateException();
//		if (fans[3][fans[3].length - 1] != fans[0][5]) throw new IllegalStateException();
//	}
	
	public Slate2 getAdjacentSlate(int side) {
		return corners[side][0].pair.slate;
	}
	
	public int getSubdivLevel() {
		return subdivLevel;
	}
}
