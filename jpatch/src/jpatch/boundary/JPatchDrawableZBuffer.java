package jpatch.boundary;

import java.awt.image.*;
import javax.vecmath.*;
import jpatch.auxilary.*;
import jpatch.entity.*;
import jpatch.renderer.*;

public final class JPatchDrawableZBuffer extends ZBufferRenderer
implements JPatchDrawable, HashPatchSubdivision.QuadDrain {

	private static final int GHOST = JPatchSettings.getInstance().iGhost;
	private static float fFlatness = 3f;
	//private static float fFlatnessSquared = fFlatness * fFlatness;
	private static int iMaxSubdiv = 10;
	
	ControlPoint cp;
	Point3f p3A = new Point3f();
	Point3f p3B = new Point3f();
	Point3f p3C = new Point3f();
	Point3f p3D = new Point3f();
		
	private static Vector3f v3a = new Vector3f();
	private static Vector3f v3b = new Vector3f();
	private static Vector3f v3c = new Vector3f();
	
	//private Point3f[] ap3New = new Point3f[12];
	private Vector3f[] av3New = new Vector3f[4];
	
	
	
	
	
	
	//private HashPatchSubdivision hashPatchSubdivision = new HashPatchSubdivision(0, 1, this);
	private MaterialProperties materialProperties;
	
	
	
	public JPatchDrawableZBuffer(BufferedImage image, Lighting lighting) {
		super(lighting);
		setImage(image);
		renderToImage();
		//for (int i = 0; i < 12; ap3New[i++] = new Point3f());
		for (int i = 0; i < 4; av3New[i++] = new Vector3f());
		setQuality(JPatchSettings.getInstance().iTesselationQuality);
	}

	/*
	public void drawTriangleArray3D(Point3f[] ap3, Color[] ac) {
		int c = 0;
		for (int p = 0; p < ap3.length;) {
			if (Line2D.relativeCCW(ap3[p].x, ap3[p].y, ap3[p + 1].x, ap3[p + 1].y, ap3[p + 2].x, ap3[p + 2].y) < 0) {
				draw3DTriangleFlat(ap3[p++], ap3[p++], ap3[p++], ac[c].getRGB());
			}
			c++;
		}
	}
	*/

	public static void setQuality(int q) {
		if (q == 0) fFlatness = 16;
		if (q == 1) fFlatness = 8;
		if (q == 2) fFlatness = 4;
		if (q == 3) fFlatness = 2;
		if (q == 4) fFlatness = 1;
	}
	
	public final void drawSimpleShape(SimpleShape shape, Matrix4f matrix) {
		Point3f[] ap3Points = shape.getPoints();
		Vector3f[] av3Normals = shape.getNormals();
		int[] aiTriangles = shape.getTriangles();
		int[] aiNormalIndices = shape.getNormalIndices();
		int triangles = aiTriangles.length / 3;
		MaterialProperties mp = shape.getMaterialProperties();
		
		if (matrix != null) {
			for (int i = 0; i < ap3Points.length; i++) {
				matrix.transform(ap3Points[i]);
			}
			for (int i = 0; i < av3Normals.length; i++) {
				matrix.transform(av3Normals[i]);
				av3Normals[i].normalize();
			}
		}
		
		//setColor(new Color(mp.red, mp.green, mp.blue));
		//for (int line = 0; line < aiLines.length;) {
		//	drawLine((int)ap3Points[aiLines[line]].x, (int)ap3Points[aiLines[line++]].y, (int)ap3Points[aiLines[line]].x, (int)ap3Points[aiLines[line++]].y);
		//}
		
		int p = 0;
		for (int triangle = 0; triangle < triangles; triangle++) {
			Vector3f normal = av3Normals[aiNormalIndices[triangle]];
			if (normal.z < 0) {
				int color = shade(null, normal, mp);
				draw3DTriangleFlatGhost(ap3Points[aiTriangles[p++]], ap3Points[aiTriangles[p++]], ap3Points[aiTriangles[p++]], color, GHOST);
			} else {
				p += 3;
			}	
		}
	}
	
	//public final void drawJPatchCurve3D(Curve curve, Matrix4f matrix) {		
	//	cp = curve.getStart();
	//	p3A.set(cp.getPosition());
	//	p3B.set(cp.getOutTangent());
	//	matrix.transform(p3A);
	//	matrix.transform(p3B);
	//	cp = cp.getNext();
	//	while (cp != null) {
	//		p3C.set(cp.getInTangent());
	//		p3D.set(cp.getPosition());
	//		matrix.transform(p3C);
	//		matrix.transform(p3D);
	//		drawCurveSegment3D(p3A,p3B,p3C,p3D);
	//		ControlPoint cpNext = cp.getNextCheckLoop();
	//		if (cpNext != null) { 
	//			p3B.set(cp.getOutTangent());
	//			matrix.transform(p3B);
	//			p3A.set(p3D);
	//		}
	//		cp = cpNext;
	//	}
	//}
	
	public final void drawJPatchCurve3D(Curve curve, Matrix4f matrix) {		
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
							//generalPath.moveTo(p3D.x, p3D.y);
						} else {
							break loop;
						}
					} else {
						p3C.set(cp.getInTangent());
						p3D.set(cp.getPosition());
						matrix.transform(p3C);
						matrix.transform(p3D);
						drawCurveSegment3D(p3A,p3B,p3C,p3D, false);
					}
				ControlPoint cpNext = cp.getNextCheckLoop();
				if (cpNext != null) { 
					p3B.set(cp.getOutTangent());
					matrix.transform(p3B);
					p3A.set(p3D);
				}
				cp = cpNext;
			}
		}
	}
	
//	private Vector3f biquadraticInterpolate(Vector3f[] v, float s, float t) {
//		float s1 = (1 - s);
//		float t1 = (1 - t);
//		float B0s = s1 * s1;
//		float B1s = 2 * s * s1;
//		float B2s = s * s;
//		float B0t = t1 * t1;
//		float B1t = 2 * t * t1;
//		float B2t = t * t;
//		Vector3f V = new Vector3f(
//			B0t * (v[0].x * B0s + v[1].x * B1s + v[2].x * B2s) +
//			B1t * (v[3].x * B0s + v[4].x * B1s + v[5].x * B2s) +
//			B2t * (v[6].x * B0s + v[7].x * B1s + v[8].x * B2s),
//			B0t * (v[0].y * B0s + v[1].y * B1s + v[2].y * B2s) +
//			B1t * (v[3].y * B0s + v[4].y * B1s + v[5].y * B2s) +
//			B2t * (v[6].y * B0s + v[7].y * B1s + v[8].y * B2s),
//			B0t * (v[0].z * B0s + v[1].z * B1s + v[2].z * B2s) +
//			B1t * (v[3].z * B0s + v[4].z * B1s + v[5].z * B2s) +
//			B2t * (v[6].z * B0s + v[7].z * B1s + v[8].z * B2s)
//		);
//		V.normalize();
//		return V;
//	}
	
	private boolean makeFlat(Point3f p0, Point3f p1, Point3f p2, Point3f p3) {
		v3a.sub(p1, p0);
		v3b.sub(p2, p3);
		v3c.sub(p3, p0);
		
		if (v3c.x != 0 || v3c.y != 0 || v3c.z != 0) v3c.normalize();
		
		p1.set(v3c);
		p1.scale(v3a.length());
		p1.add(p0);
		
		p2.set(v3c);
		p2.scale(-v3b.length());
		p2.add(p3);
		//System.out.println("makeflat");
		return true;
	}
	
	//private boolean subdiv(Point3f p0, Point3f p1, Point3f p2, Point3f p3, boolean simple) {
	//	if (!simple) {
	//		v3a.set(4 * p0.x - 6 *  p1.x + 2 * p3.x, 4 * p0.y - 6 *  p1.y + 2 * p3.y, 4 * p0.z - 6 *  p1.z + 2 * p3.z);
	//		float la = v3a.lengthSquared();
	//		if (la > fFlatnessSquared) return true;
	//		v3a.set(2 * p0.x - 6 *  p1.x + 4 * p3.x, 2 * p0.y - 6 *  p1.y + 4 * p3.y, 2 * p0.z - 6 *  p1.z + 4 * p3.z);
	//		return ((float) Math.sqrt(la) + v3b.length() > fFlatness);
	//	} else {
	//		v3a.set(p0.x - p1.x - p2.x + p3.x, p0.y - p1.y - p2.y + p3.y, p0.z - p1.z - p2.z + p3.z);
	//		return (v3a.lengthSquared() > fFlatnessSquared);
	//	}
	//}
	
	private float subdiv(Point3f p0, Point3f p1, Point3f p2, Point3f p3, boolean simple) {
		if (!simple) {
			v3a.set(4 * p0.x - 6 *  p1.x + 2 * p3.x, 4 * p0.y - 6 *  p1.y + 2 * p3.y, 4 * p0.z - 6 *  p1.z + 2 * p3.z);
			v3b.set(2 * p0.x - 6 *  p2.x + 4 * p3.x, 2 * p0.y - 6 *  p2.y + 4 * p3.y, 2 * p0.z - 6 *  p2.z + 4 * p3.z);
			return v3a.length() + v3b.length();
		} else {
			v3a.set(p0.x - p1.x - p2.x + p3.x, p0.y - p1.y - p2.y + p3.y, p0.z - p1.z - p2.z + p3.z);
			return v3a.length();
		}
	}
	
	private int interpolateColor(int c0, int c1) {
		int r0 = (c0 & 0x00fe0000) >> 1;
		int g0 = (c0 & 0x0000fe00) >> 1;
		int b0 = (c0 & 0x000000fe) >> 1;
		int r1 = (c1 & 0x00fe0000) >> 1;
		int g1 = (c1 & 0x0000fe00) >> 1;
		int b1 = (c1 & 0x000000fe) >> 1;
		return (r0 + r1) | (g0 + g1) | (b0 + b1);
	}
	
	public static Vector3f interpolateNormal(Vector3f n0, Vector3f n1) {
		//return new Vector3f((n0.x + n1.x) * 0.5f, (n0.y + n1.y) * 0.5f, (n0.z + n1.z) * 0.5f);
		Vector3f n = new Vector3f(n0.x + n1.x, n0.y + n1.y, n0.z + n1.z);
		n.normalize();
		return n;
	}

	public static Vector3f interpolateNormal(Vector3f n0, Vector3f n1, Point3f p0, Point3f p1, Point3f p2, Point3f p3) {
		//if (true) return interpolateNormal(n0, n1);
		////v3a.set(3 * p0.x - 3 * p1.x - 3 * p2.x + 3 * p3.x, 3 * p0.y - 3 * p1.y - 3 * p2.y + 3 * p3.y, 3 * p0.z - 3 * p1.z - 3 * p2.z + 3 * p3.z);
		////Vector3f n = new Vector3f((n0.x + n1.x) * 0.5f, (n0.y + n1.y) * 0.5f, (n0.z + n1.z) * 0.5f);
		////n.cross(n, v3a);
		////n.cross(n, v3a);
		////return n;
		//v3a.set(p0.x - p1.x - p2.x + p3.x, p0.y - p1.y - p2.y + p3.y, p0.z - p1.z - p2.z + p3.z);
		//v3b.set((n0.x + n1.x) * 0.5f, (n0.y + n1.y) * 0.5f, (n0.z + n1.z) * 0.5f);
		//v3c.cross(v3b, v3a);
		//Vector3f n = new Vector3f();
		//n.cross(v3c, v3a);
		//return new Vector3f(v3a);
		////return interpolateNormal(n0, n1);
		
		
		//v3a.sub(p3,p0);
		//v3b.add(n0,n1);
		//Vector3f n = new Vector3f(v3b);
		//v3a.scale(-1.5f * v3a.dot(v3b) / v3a.dot(v3a));
		//n.add(v3a);
		//n.normalize();
		//return n;
		
		v3a.set(p0.x + p1.x - p2.x - p3.x, p0.y + p1.y - p2.y - p3.y, p0.z + p1.z - p2.z - p3.z);
		//v3a.set(p0.x - p1.x - p2.x + p3.x, p0.y - p1.y - p2.y + p3.y, p0.z - p1.z - p2.z + p3.z);
		Vector3f n = new Vector3f(n0.x + n1.x, n0.y + n1.y, n0.z + n1.z);
		if (v3a.x != 0 || v3a.y != 0 || v3a.z != 0) {
			v3a.normalize();
			n.cross(n, v3a);
			n.cross(n, v3a);
			n.scale(-1f / n.length());
		}
		else n.normalize();
		return n;

	}
	
	//private void drawHashPatch3(Point3f[] ap3, Vector3f[] av3, int level, MaterialProperties mp) {
	//	boolean visible = false;
	//	loop:
	//	for (int i = 0; i < 8; i++) {
	//		if (ap3[i].x > 0 && ap3[i].x < iWidth && ap3[i].y > 0 && ap3[i].y < iHeight) {
	//			visible = true;
	//			break loop;
	//		}
	//	}
	//	if (!visible) return;
	//	/* check if we need to split */
	//	float s0 = subdiv(ap3[0], ap3[1], ap3[2], ap3[3], level > 0);
	//	float s1 = subdiv(ap3[3], ap3[4], ap3[5], ap3[6], level > 0);
	//	float s2 = subdiv(ap3[6], ap3[7], ap3[8], ap3[0], level > 0);
	//	
	//	if (level < iMaxSubdiv && (s0 > fFlatness || s1 > fFlatness || s2 > fFlatness)) {
	//		if (s0 <= fFlatness) makeFlat(ap3[0], ap3[1], ap3[2], ap3[3]);
	//		if (s1 <= fFlatness) makeFlat(ap3[3], ap3[4], ap3[5], ap3[6]);
	//		if (s2 <= fFlatness) makeFlat(ap3[6], ap3[7], ap3[8], ap3[0]);
	//		
	//		Point3f[] ap3new1 = newPatch(9);
	//		Point3f[] ap3new2 = newPatch(9);
	//		Point3f[] ap3new3 = newPatch(9);
	//		ap3new1[0].set(ap3[0]);
	//		ap3new1[1].set(ap3[1]);
	//		ap3new1[2].set(ap3[2]);
	//		ap3new1[3].set(ap3[3]);
	//		
	//		ap3new2[4].set(ap3[4]);
	//		ap3new2[5].set(ap3[5]);
	//		ap3new2[6].set(ap3[6]);
	//		
	//		ap3new3[7].set(ap3[7]);
	//		ap3new3[8].set(ap3[8]);
	//		ap3new3[0].set(ap3[0]);
	//		
	//		deCasteljauSplit(ap3new1[0], ap3new1[1], ap3new1[2], ap3new1[3], ap3new2[0], ap3new2[1], ap3new2[2], ap3new2[3]);
	//		deCasteljauSplit(ap3new2[3], ap3new2[4], ap3new2[5], ap3new2[6], ap3new3[3], ap3new3[4], ap3new3[5], ap3new3[6]);
	//		deCasteljauSplit(ap3new3[6], ap3new3[7], ap3new3[8], ap3new3[0], ap3new1[6], ap3new1[7], ap3new1[8], ap3new1[0]);
	//		
	//		Point3f[] p3a = newPatch(6);
	//		Point3f[] p3b = newPatch(6);
	//		p3a[0].add(ap3new1[3], ap3new2[4]);
	//		p3a[0].sub(ap3new2[3]);
	//		p3a[1].add(ap3new1[6], ap3new3[5]);
	//		p3a[1].sub(ap3new3[6]);
	//		
	//		p3a[2].add(ap3new2[6], ap3new3[7]);
	//		p3a[2].sub(ap3new3[6]);
	//		p3a[3].add(ap3new2[0], ap3new1[8]);
	//		p3a[3].sub(ap3new1[0]);
	//		
	//		p3a[4].add(ap3new3[0], ap3new1[1]);
	//		p3a[4].sub(ap3new1[0]);
	//		p3a[5].add(ap3new3[3], ap3new2[2]);
	//		p3a[5].sub(ap3new2[3]);
	//		
	//		p3b[0].add(ap3new1[3], ap3new3[6]);
	//		p3b[0].sub(ap3new3[5]);
	//		p3b[1].add(ap3new1[6], ap3new2[3]);
	//		p3b[1].sub(ap3new2[4]);
	//		
	//		p3b[2].add(ap3new2[6], ap3new1[0]);
	//		p3b[2].sub(ap3new1[8]);
	//		p3b[3].add(ap3new2[0], ap3new3[6]);
	//		p3b[3].sub(ap3new3[7]);
	//		
	//		p3b[4].add(ap3new3[0], ap3new2[3]);
	//		p3b[4].sub(ap3new2[2]);
	//		p3b[5].add(ap3new3[3], ap3new1[0]);
	//		p3b[5].sub(ap3new1[1]);
	//		
	//		float ff = 0.33f;
	//		
	//		ap3new1[4].interpolate(p3a[0],p3b[0],ff);
	//		ap3new1[5].interpolate(p3a[1],p3b[1],ff);
        //
	//		ap3new2[7].interpolate(p3a[2],p3b[2],ff);
	//		ap3new2[8].interpolate(p3a[3],p3b[3],ff);
        //
	//		ap3new3[1].interpolate(p3a[4],p3b[4],ff);
	//		ap3new3[2].interpolate(p3a[5],p3b[5],ff);
	//		
	//		Point3f[] ap3new4 = new Point3f[] { ap3new3[3], ap3new3[2], ap3new3[1], ap3new1[6], ap3new1[5], ap3new1[4], ap3new2[0], ap3new2[8], ap3new2[7] };
	//		
	//		Vector3f[] av3new4 = new Vector3f[] {
	//			(level > 0) ? interpolateNormal(av3[1], av3[2]) : interpolateNormal(av3[1], av3[2], ap3[3], ap3[4], ap3[5], ap3[6]),
	//			(level > 0) ? interpolateNormal(av3[2], av3[0]) : interpolateNormal(av3[2], av3[0], ap3[6], ap3[7], ap3[8], ap3[0]),
	//			(level > 0) ? interpolateNormal(av3[0], av3[1]) : interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]),
	//		};
	//		Vector3f[] av3new1 = new Vector3f[] { av3[0], av3new4[2], av3new4[1] };
	//		Vector3f[] av3new2 = new Vector3f[] { av3new4[2], av3[1], av3new4[0] };
	//		Vector3f[] av3new3 = new Vector3f[] { av3new4[1], av3new4[0], av3[2] };
	//		
	//		level++;
	//		drawHashPatch3(ap3new1, av3new1, level, mp);
	//		drawHashPatch3(ap3new2, av3new2, level, mp);
	//		drawHashPatch3(ap3new3, av3new3, level, mp);
	//		drawHashPatch3(ap3new4, av3new4, level, mp);
	//	}
	//	
	//	else {
	//		av3New[0].set(av3[0]);
	//		av3New[1].set(av3[1]);
	//		av3New[2].set(av3[2]);
	//		if (bBackfaceNormalFlip) flipBackfaceNormals(av3New);
	//		int c0 = lighting.shade(ap3[0], av3New[0], mp);
	//		int c1 = lighting.shade(ap3[3], av3New[1], mp);
	//		int c2 = lighting.shade(ap3[6], av3New[2], mp);
	//		draw3DTriangleGourad(ap3[0], ap3[3], ap3[6], c0, c1, c2);
	//		
	//		//drawLine3D(ap3[0], ap3[3]);
	//		//drawLine3D(ap3[3], ap3[6]);
	//		//drawLine3D(ap3[6], ap3[0]);
	//		
	//		Point3f p = new Point3f();
	//		p.set(ap3[0]);
	//		v3a.set(av3[0]);
	//		v3a.scale(30f);
	//		p.add(v3a);
	//		drawLine3D(ap3[0],p);
	//		
	//		p.set(ap3[3]);
	//		v3a.set(av3[1]);
	//		v3a.scale(30f);
	//		p.add(v3a);
	//		drawLine3D(ap3[3],p);
	//		
	//		p.set(ap3[6]);
	//		v3a.set(av3[2]);
	//		v3a.scale(30f);
	//		p.add(v3a);
	//		drawLine3D(ap3[6],p);
	//	
	//		for (int i = 0; i < 9; i++) {
	//			drawLine3D(ap3[i], ap3[(i + 1) % 9]);
	//			drawPoint3D(ap3[i],2);
	//		}
	//	}
	//}
	
	private void drawCurveSegment3D(Point3f p3A, Point3f p3B, Point3f p3C, Point3f p3D, boolean simple) {
		if (subdiv(p3A, p3B, p3C, p3D, simple) >= fFlatness) {
			Point3f p0 = new Point3f(p3A);
			Point3f p1 = new Point3f(p3B);
			Point3f p2 = new Point3f(p3C);
			Point3f p3 = new Point3f(p3D);
			Point3f p4 = new Point3f();
			Point3f p5 = new Point3f();
			Point3f p6 = new Point3f();
			Point3f p7 = new Point3f();
			deCasteljauSplit(p0, p1, p2, p3, p4, p5, p6, p7);
			
			drawCurveSegment3D(p0, p1, p2, p3, true);
			drawCurveSegment3D(p4, p5, p6, p7, true);
		} else {
			drawLine3D(p3A, p3D);
		}
	}
	
	private void drawHashPatch4Gaurad(Point3f[] ap3, Vector3f[] av3, int[] ac, boolean[] abFlat, int[] aiLevel, MaterialProperties mp) {
		
		//materialProperties = mp;
		//Point3d[] adp3 = new Point3d[ap3.length];
		//for (int i = 0; i < adp3.length; adp3[i] = new Point3d(ap3[i++]));
		//hashPatchSubdivision.subdivHashPatch4(adp3, adp3, av3, 2, new Point3d[][] { null, null, null, null }, new Vector3f[][] { null, null, null, null });
		//
		//if (true) return;
		
		boolean visible = false;
		loop:
		for (int i = 0; i < 12; i++) {
			if (ap3[i].x > 0 && ap3[i].x < iWidth && ap3[i].y > 0 && ap3[i].y < iHeight) {
				visible = true;
				break loop;
			}
		}
		if (!visible) return;
		//System.out.println(abFlat[0] + "\t" + abFlat[1] + "\t" + abFlat[2] + "\t" + abFlat[3]);
		
		/* check if we need to u-split */
		float u0 = subdiv(ap3[0], ap3[1], ap3[2], ap3[3], aiLevel[0] > 0);
		float u2 = subdiv(ap3[9], ap3[8], ap3[7], ap3[6], aiLevel[2] > 0);
		float v3 = subdiv(ap3[0], ap3[11], ap3[10], ap3[9], aiLevel[3] > 0);
		float v1 = subdiv(ap3[3], ap3[4], ap3[5], ap3[6], aiLevel[1] > 0);
		
		float ul = (aiLevel[0] >= iMaxSubdiv || aiLevel[2] >= iMaxSubdiv) ? -1 : (float) Math.max(u0, u2);
		float uv = (aiLevel[1] >= iMaxSubdiv || aiLevel[3] >= iMaxSubdiv) ? -1 : (float) Math.max(v1, v3);
		
		if (true && ul >= fFlatness && ul > uv * 1.1f) {
		//if (false) {
		//if (subdiv(ap3[0], ap3[1], ap3[2], ap3[3], uSplit) || subdiv(ap3[9], ap3[8], ap3[7], ap3[6], uSplit)) {
			
			/* flatten cubics if flat enough */
			if (!abFlat[0] && (u0 <= fFlatness)) abFlat[0] = makeFlat(ap3[0], ap3[1], ap3[2], ap3[3]);
			if (!abFlat[1] && (v1 <= fFlatness)) abFlat[1] = makeFlat(ap3[3], ap3[4], ap3[5], ap3[6]);
			if (!abFlat[2] && (u2 <= fFlatness)) abFlat[2] = makeFlat(ap3[9], ap3[8], ap3[7], ap3[6]);
			if (!abFlat[3] && (v3 <= fFlatness)) abFlat[3] = makeFlat(ap3[0], ap3[11], ap3[10], ap3[9]);
			
			
			/* compute new normals */
			Vector3f[] av3new = newNormals(4);
			av3new[1].set(av3[1]);
			av3new[0] = av3[1] = (aiLevel[0] > 0) ? interpolateNormal(av3[0], av3[1]) : interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]);
			av3new[2].set(av3[2]);
			av3new[3] = av3[2] = (aiLevel[2] > 0) ? interpolateNormal(av3[3], av3[2]) : interpolateNormal(av3[3], av3[2], ap3[9], ap3[8], ap3[7], ap3[6]);
			
			/* compute new patches */
			Point3f[] ap3new = newPatch(12);
			v3a.sub(ap3[11], ap3[0]);
			v3b.sub(ap3[4], ap3[3]);
			v3c.add(v3a, v3b);
			v3c.scale(0.5f);
			v3a.sub(ap3[10], ap3[9]);
			v3b.sub(ap3[5], ap3[6]);
			v3a.add(v3b);
			v3a.scale(0.5f);
			deCasteljauSplit(ap3[0], ap3[1], ap3[2], ap3[3], ap3new[0], ap3new[1], ap3new[2], ap3new[3]);
			deCasteljauSplit(ap3[9], ap3[8], ap3[7], ap3[6], ap3new[9], ap3new[8], ap3new[7], ap3new[6]);
			ap3new[4].set(ap3[4]);
			ap3new[5].set(ap3[5]);
			ap3[4].add(ap3[3], v3c);
			ap3[5].add(ap3[6], v3a);
			ap3new[11].set(ap3[4]);
			ap3new[10].set(ap3[5]);
			
			///* compute new normals */
			//Vector3f[] av3new = newNormals(4);
			//av3new[1].set(av3[1]);
			////av3new[0] = av3[1] = (uLevel > 99) ? interpolateNormal(av3[0], av3[1]) : interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]);
			//av3new[2].set(av3[2]);
			////av3new[3] = av3[2] = (uLevel > 99) ? interpolateNormal(av3[3], av3[2]) : interpolateNormal(av3[3], av3[2], ap3[9], ap3[8], ap3[7], ap3[6]);
			//
			//v3a.sub(ap3[4], ap3[3]);
			//v3b.sub(ap3[2], ap3[3]);
			//av3[1].cross(v3b, v3a);
			//av3[1].normalize();
			//av3new[0].set(av3[1]);
			//v3a.sub(ap3[5], ap3[6]);
			//v3b.sub(ap3[7], ap3[6]);
			//av3[2].cross(v3a, v3b);
			//av3[2].normalize();
			//av3new[3].set(av3[2]);
			
			/* compute new colors */
			int[] acnew = new int[4];
			acnew[1] = ac[1];
			acnew[0] = ac[1] = (!abFlat[0]) ? lighting.shade(ap3[3], av3[1], mp) : interpolateColor(ac[0], ac[1]);
			acnew[2] = ac[2];
			acnew[3] = ac[2] = (!abFlat[2]) ? lighting.shade(ap3[6], av3[2], mp) : interpolateColor(ac[2], ac[3]);
			
			/* set up new flatenough flags */
			boolean[] abnew = new boolean[4];
			abnew[0] = abFlat[0];
			abnew[1] = abFlat[1];
			abnew[2] = abFlat[2];
			abnew[3] = abFlat[1] = false;
			
			///* compute new normals */
			//Vector3f[] av3new = newNormals();
			//av3new[1].set(av3[1]);
			//v3b.sub(ap3[3],ap3[2]);
			//av3[1].cross(v3b, v3c);
			//av3[1].normalize();
			//av3new[0].set(av3[1]);
			//av3new[2].set(av3[2]);
			//v3b.sub(ap3[7],ap3[6]);
			//av3[2].cross(v3b, v3a);
			//av3[2].normalize();
			//av3new[3].set(av3[2]);
			
			/* recurse */
			int l = (aiLevel[1] < aiLevel[3]) ? aiLevel[1] : aiLevel[3];
			aiLevel[0]++;
			aiLevel[2]++;
			int[] newLevels = new int[aiLevel.length];
			for (int i = 0; i < aiLevel.length; newLevels[i] = aiLevel[i++]);
			aiLevel[1] = newLevels[3] = l;
			drawHashPatch4Gaurad(ap3, av3, ac, abFlat, aiLevel, mp);
			drawHashPatch4Gaurad(ap3new, av3new, acnew, abnew, newLevels, mp);
			return;
		}
		/* check if we need to v-split */
		else if (true && uv >= fFlatness) {
		//else if (false) {
		//else if (subdiv(ap3[0], ap3[11], ap3[10], ap3[9], vSplit) || subdiv(ap3[3], ap3[4], ap3[5], ap3[6], vSplit)) {
			/* flatten cubics if flat enough */
			if (!abFlat[0] && (u0 <= fFlatness)) abFlat[0] = makeFlat(ap3[0], ap3[1], ap3[2], ap3[3]);
			if (!abFlat[1] && (v1 <= fFlatness)) abFlat[1] = makeFlat(ap3[3], ap3[4], ap3[5], ap3[6]);
			if (!abFlat[2] && (u2 <= fFlatness)) abFlat[2] = makeFlat(ap3[9], ap3[8], ap3[7], ap3[6]);
			if (!abFlat[3] && (v3 <= fFlatness)) abFlat[3] = makeFlat(ap3[0], ap3[11], ap3[10], ap3[9]);
			
			
			/* compute new normals */
			Vector3f[] av3new = newNormals(4);
			av3new[3].set(av3[3]);
			av3new[0] = av3[3] = (aiLevel[3] > 0) ? interpolateNormal(av3[0], av3[3]) : interpolateNormal(av3[0], av3[3], ap3[0], ap3[11], ap3[10], ap3[9]);
			av3new[2].set(av3[2]);
			av3new[1] = av3[2] = (aiLevel[1] > 0) ? interpolateNormal(av3[1], av3[2]) : interpolateNormal(av3[1], av3[2], ap3[3], ap3[4], ap3[5], ap3[6]);
			
			/* compute new patches */
			Point3f[] ap3new = newPatch(12);
			v3a.sub(ap3[1], ap3[0]);
			v3b.sub(ap3[8], ap3[9]);
			v3c.add(v3a, v3b);
			v3c.scale(0.5f);
			v3a.sub(ap3[2], ap3[3]);
			v3b.sub(ap3[7], ap3[6]);
			v3a.add(v3b);
			v3a.scale(0.5f);
			deCasteljauSplit(ap3[0], ap3[11], ap3[10], ap3[9], ap3new[0], ap3new[11], ap3new[10], ap3new[9]);
			deCasteljauSplit(ap3[3], ap3[4], ap3[5], ap3[6], ap3new[3], ap3new[4], ap3new[5], ap3new[6]);
			ap3new[8].set(ap3[8]);
			ap3new[7].set(ap3[7]);
			ap3[8].add(ap3[9], v3c);
			ap3[7].add(ap3[6], v3a);
			ap3new[1].set(ap3[8]);
			ap3new[2].set(ap3[7]);
			
			///* compute new normals */
			//Vector3f[] av3new = newNormals(4);
			//av3new[3].set(av3[3]);
			////av3new[0] = av3[3] = (vLevel > 99) ? interpolateNormal(av3[0], av3[3]) : interpolateNormal(av3[0], av3[3], ap3[0], ap3[11], ap3[10], ap3[9]);
			//av3new[2].set(av3[2]);
			////av3new[1] = av3[2] = (vLevel > 99) ? interpolateNormal(av3[1], av3[2]) : interpolateNormal(av3[1], av3[2], ap3[3], ap3[4], ap3[5], ap3[6]);
			//
			//v3a.sub(ap3[10], ap3[9]);
			//v3b.sub(ap3[8], ap3[9]);
			//av3[3].cross(v3b, v3a);
			//av3[3].normalize();
			//av3new[0].set(av3[3]);
			//v3a.sub(ap3[5], ap3[6]);
			//v3b.sub(ap3[7], ap3[6]);
			//av3[2].cross(v3a, v3b);
			//av3[2].normalize();
			//av3new[1].set(av3[2]);
			
			/* compute new colors */
			int[] acnew = new int[4];
			acnew[3] = ac[3];
			acnew[0] = ac[3] = (!abFlat[3]) ? lighting.shade(ap3[9], av3[3], mp) : interpolateColor(ac[0], ac[3]);
			acnew[2] = ac[2];
			acnew[1] = ac[2] = (!abFlat[1]) ? lighting.shade(ap3[6], av3[2], mp) : interpolateColor(ac[1], ac[2]);
			
			/* set up new flatenough flags */
			boolean[] abnew = new boolean[4];
			abnew[1] = abFlat[1];
			abnew[3] = abFlat[3];
			abnew[2] = abFlat[2];
			abnew[0] = abFlat[2] = false;
			
			///* compute new normals */
			//Vector3f[] av3new = newNormals();
			//av3new[3].set(av3[3]);
			//v3b.sub(ap3[10],ap3[9]);
			//av3[3].cross(v3b, v3c);
			//av3[3].normalize();
			//av3new[0].set(av3[3]);
			//av3new[2].set(av3[2]);
			//v3b.sub(ap3[6],ap3[5]);
			//av3[2].cross(v3b, v3a);
			//av3[2].normalize();
			//av3new[1].set(av3[2]);
			
			/* recurse */
			int l = (aiLevel[0] < aiLevel[2]) ? aiLevel[0] : aiLevel[2];
			aiLevel[1]++;
			aiLevel[3]++;
			int[] newLevels = new int[aiLevel.length];
			for (int i = 0; i < aiLevel.length; newLevels[i] = aiLevel[i++]);
			aiLevel[2] = newLevels[0] = l;
			drawHashPatch4Gaurad(ap3, av3, ac, abFlat, aiLevel, mp);
			drawHashPatch4Gaurad(ap3new, av3new, acnew, abnew, newLevels, mp);
			return;
		}
		//}
		/* draw the patch */
		
		//System.out.println("drawPatch");
		//for (int i = 0; i < 12; System.out.println(ap3[i++]));
		
		//if (bBackfaceNormalFlip) flipBackfaceNormals(av3);
		//av3[0].normalize();
		//av3[1].normalize();
		//av3[2].normalize();
		//av3[3].normalize();
		
		//av3New[0].set(av3[0]);
		//av3New[1].set(av3[1]);
		//av3New[2].set(av3[2]);
		//av3New[3].set(av3[3]);
		//if (bBackfaceNormalFlip) flipBackfaceNormals(av3New);
		//int c0 = lighting.shade(ap3[0], av3New[0], mp);
		//int c1 = lighting.shade(ap3[3], av3New[1], mp);
		//int c2 = lighting.shade(ap3[6], av3New[2], mp);
		//int c3 = lighting.shade(ap3[9], av3New[3], mp);
		
		//drawLine3D(ap3[0], ap3[3]);
		//drawLine3D(ap3[3], ap3[6]);
		//drawLine3D(ap3[6], ap3[9]);
		//drawLine3D(ap3[9], ap3[0]);
		
		//for (int i = 0; i < 12; i++) {
		//	drawLine3D(ap3[i], ap3[(i + 1) % 12]);
		//	drawPoint3D(ap3[i],2);
		//}
		
		//Point3f p = new Point3f();
		//p.set(ap3[0]);
		//v3a.set(av3[0]);
		//v3a.scale(30f);
		//p.add(v3a);
		//drawLine3D(ap3[0],p);
		//
		//p.set(ap3[3]);
		//v3a.set(av3[1]);
		//v3a.scale(30f);
		//p.add(v3a);
		//drawLine3D(ap3[3],p);
		//
		//p.set(ap3[6]);
		//v3a.set(av3[2]);
		//v3a.scale(30f);
		//p.add(v3a);
		//drawLine3D(ap3[6],p);
		//
		//p.set(ap3[9]);
		//v3a.set(av3[3]);
		//v3a.scale(30f);
		//p.add(v3a);
		//drawLine3D(ap3[9],p);
		
		//int ca = 0xFF777777;
		//int cb = 0xFF999999;
		//
		//draw3DTriangleGourad(ap3[0], ap3[3], ap3[9], ca, ca, ca);
		//draw3DTriangleGourad(ap3[6], ap3[9], ap3[3], cb, cb, cb);
		
		if (mp.isOpaque()) {
			draw3DTriangleGourad(ap3[9], ap3[3], ap3[0], ac[3], ac[1], ac[0]);
			draw3DTriangleGourad(ap3[3], ap3[9], ap3[6], ac[1], ac[3], ac[2]);
		} else {
			int transparency = (int) (Math.min(1f,mp.transmit + mp.filter) * 255f);
			draw3DTriangleGouradTransparent(ap3[9], ap3[3], ap3[0], ac[3], ac[1], ac[0], transparency);
			draw3DTriangleGouradTransparent(ap3[3], ap3[9], ap3[6], ac[1], ac[3], ac[2], transparency);
		}
	}
	
	private void drawHashPatch4Flat(Point3f[] ap3, boolean[] abFlat, int[] aiLevel, int color, int transparency) {
		
		boolean visible = false;
		loop:
		for (int i = 0; i < 12; i++) {
			if (ap3[i].x > 0 && ap3[i].x < iWidth && ap3[i].y > 0 && ap3[i].y < iHeight) {
				visible = true;
				break loop;
			}
		}
		if (!visible) return;
		
		/* check if we need to u-split */
		float u0 = subdiv(ap3[0], ap3[1], ap3[2], ap3[3], aiLevel[0] > 0);
		float u2 = subdiv(ap3[9], ap3[8], ap3[7], ap3[6], aiLevel[2] > 0);
		float v3 = subdiv(ap3[0], ap3[11], ap3[10], ap3[9], aiLevel[3] > 0);
		float v1 = subdiv(ap3[3], ap3[4], ap3[5], ap3[6], aiLevel[1] > 0);
		
		float ul = (aiLevel[0] >= iMaxSubdiv || aiLevel[2] >= iMaxSubdiv) ? -1 : (float) Math.max(u0, u2);
		float uv = (aiLevel[1] >= iMaxSubdiv || aiLevel[3] >= iMaxSubdiv) ? -1 : (float) Math.max(v1, v3);
		
		if (true && ul >= fFlatness && ul > uv * 1.1f) {
			
			/* flatten cubics if flat enough */
			if (!abFlat[0] && (u0 <= fFlatness)) abFlat[0] = makeFlat(ap3[0], ap3[1], ap3[2], ap3[3]);
			if (!abFlat[1] && (v1 <= fFlatness)) abFlat[1] = makeFlat(ap3[3], ap3[4], ap3[5], ap3[6]);
			if (!abFlat[2] && (u2 <= fFlatness)) abFlat[2] = makeFlat(ap3[9], ap3[8], ap3[7], ap3[6]);
			if (!abFlat[3] && (v3 <= fFlatness)) abFlat[3] = makeFlat(ap3[0], ap3[11], ap3[10], ap3[9]);
			
			/* compute new patches */
			Point3f[] ap3new = newPatch(12);
			v3a.sub(ap3[11], ap3[0]);
			v3b.sub(ap3[4], ap3[3]);
			v3c.add(v3a, v3b);
			v3c.scale(0.5f);
			v3a.sub(ap3[10], ap3[9]);
			v3b.sub(ap3[5], ap3[6]);
			v3a.add(v3b);
			v3a.scale(0.5f);
			deCasteljauSplit(ap3[0], ap3[1], ap3[2], ap3[3], ap3new[0], ap3new[1], ap3new[2], ap3new[3]);
			deCasteljauSplit(ap3[9], ap3[8], ap3[7], ap3[6], ap3new[9], ap3new[8], ap3new[7], ap3new[6]);
			ap3new[4].set(ap3[4]);
			ap3new[5].set(ap3[5]);
			ap3[4].add(ap3[3], v3c);
			ap3[5].add(ap3[6], v3a);
			ap3new[11].set(ap3[4]);
			ap3new[10].set(ap3[5]);
			
			/* set up new flatenough flags */
			boolean[] abnew = new boolean[4];
			abnew[0] = abFlat[0];
			abnew[1] = abFlat[1];
			abnew[2] = abFlat[2];
			abnew[3] = abFlat[1] = false;
			
			/* recurse */
			int l = (aiLevel[1] < aiLevel[3]) ? aiLevel[1] : aiLevel[3];
			aiLevel[0]++;
			aiLevel[2]++;
			int[] newLevels = new int[aiLevel.length];
			for (int i = 0; i < aiLevel.length; newLevels[i] = aiLevel[i++]);
			aiLevel[1] = newLevels[3] = l;
			drawHashPatch4Flat(ap3, abFlat, aiLevel, color, transparency);
			drawHashPatch4Flat(ap3new, abnew, newLevels, color, transparency);
			return;
		}
		/* check if we need to v-split */
		else if (true && uv >= fFlatness) {
		
			/* flatten cubics if flat enough */
			if (!abFlat[0] && (u0 <= fFlatness)) abFlat[0] = makeFlat(ap3[0], ap3[1], ap3[2], ap3[3]);
			if (!abFlat[1] && (v1 <= fFlatness)) abFlat[1] = makeFlat(ap3[3], ap3[4], ap3[5], ap3[6]);
			if (!abFlat[2] && (u2 <= fFlatness)) abFlat[2] = makeFlat(ap3[9], ap3[8], ap3[7], ap3[6]);
			if (!abFlat[3] && (v3 <= fFlatness)) abFlat[3] = makeFlat(ap3[0], ap3[11], ap3[10], ap3[9]);
			
			/* compute new patches */
			Point3f[] ap3new = newPatch(12);
			v3a.sub(ap3[1], ap3[0]);
			v3b.sub(ap3[8], ap3[9]);
			v3c.add(v3a, v3b);
			v3c.scale(0.5f);
			v3a.sub(ap3[2], ap3[3]);
			v3b.sub(ap3[7], ap3[6]);
			v3a.add(v3b);
			v3a.scale(0.5f);
			deCasteljauSplit(ap3[0], ap3[11], ap3[10], ap3[9], ap3new[0], ap3new[11], ap3new[10], ap3new[9]);
			deCasteljauSplit(ap3[3], ap3[4], ap3[5], ap3[6], ap3new[3], ap3new[4], ap3new[5], ap3new[6]);
			ap3new[8].set(ap3[8]);
			ap3new[7].set(ap3[7]);
			ap3[8].add(ap3[9], v3c);
			ap3[7].add(ap3[6], v3a);
			ap3new[1].set(ap3[8]);
			ap3new[2].set(ap3[7]);
			
			/* set up new flatenough flags */
			boolean[] abnew = new boolean[4];
			abnew[1] = abFlat[1];
			abnew[3] = abFlat[3];
			abnew[2] = abFlat[2];
			abnew[0] = abFlat[2] = false;
			
			/* recurse */
			int l = (aiLevel[0] < aiLevel[2]) ? aiLevel[0] : aiLevel[2];
			aiLevel[1]++;
			aiLevel[3]++;
			int[] newLevels = new int[aiLevel.length];
			for (int i = 0; i < aiLevel.length; newLevels[i] = aiLevel[i++]);
			aiLevel[2] = newLevels[0] = l;
			drawHashPatch4Flat(ap3, abFlat, aiLevel, color, transparency);
			drawHashPatch4Flat(ap3new, abnew, newLevels, color, transparency);
			return;
		}
		/* draw the patch */
		
		//draw3DTriangleFlat(ap3[9], ap3[3], ap3[0], color);
		//draw3DTriangleFlat(ap3[3], ap3[9], ap3[6], color);
		if (transparency == 0) {
			draw3DTriangleFlat(ap3[9], ap3[3], ap3[0], color);
			draw3DTriangleFlat(ap3[3], ap3[9], ap3[6], color);
		} else {
			draw3DTriangleFlatTransparent(ap3[9], ap3[3], ap3[0], color, transparency);
			draw3DTriangleFlatTransparent(ap3[3], ap3[9], ap3[6], color, transparency);
		}
	}
	
	//private void drawHashPatch4(Point3f[] ap3, Vector3f[] av3, int[] ac, boolean[] abFlat, int[] aiLevel, MaterialProperties mp) {
	//	Vector3f[] av3Av = new Vector3f[] {
	//		new Vector3f(0.5f * (av3[0].x + av3[1].x), 0.5f * (av3[0].y + av3[1].y), 0.5f * (av3[0].z + av3[1].z)),
	//		new Vector3f(0.5f * (av3[1].x + av3[2].x), 0.5f * (av3[1].y + av3[2].y), 0.5f * (av3[1].z + av3[2].z)),
	//		new Vector3f(0.5f * (av3[2].x + av3[3].x), 0.5f * (av3[2].y + av3[3].y), 0.5f * (av3[2].z + av3[3].z)),
	//		new Vector3f(0.5f * (av3[3].x + av3[0].x), 0.5f * (av3[3].y + av3[0].y), 0.5f * (av3[3].z + av3[0].z))
	//	};
	//	
	//	Vector3f[] av3New = new Vector3f[9];
	//	av3New[0] = av3[0];
	//	av3New[1] = (aiLevel[0] > 0) ? interpolateNormal(av3[0], av3[1]) : interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]);
	//	av3New[1].add(av3New[1]);
	//	av3New[1].sub(av3Av[0]);
	//	av3New[2] = av3[1];
	//	av3New[3] = (aiLevel[3] > 0) ? interpolateNormal(av3[0], av3[3]) : interpolateNormal(av3[0], av3[3], ap3[0], ap3[11], ap3[10], ap3[9]);
	//	av3New[3].add(av3New[3]);
	//	av3New[3].sub(av3Av[3]);
	//	av3New[5] = (aiLevel[1] > 0) ? interpolateNormal(av3[1], av3[2]) : interpolateNormal(av3[1], av3[2], ap3[3], ap3[4], ap3[5], ap3[6]);
	//	av3New[5].add(av3New[5]);
	//	av3New[5].sub(av3Av[1]);
	//	av3New[6] = av3[3];
	//	av3New[7] = (aiLevel[2] > 0) ? interpolateNormal(av3[3], av3[2]) : interpolateNormal(av3[3], av3[2], ap3[9], ap3[8], ap3[7], ap3[6]);
	//	av3New[7].add(av3New[7]);
	//	av3New[7].sub(av3Av[2]);
	//	av3New[8] = av3[2];
	//	
	//	Point3f p0 = new Point3f(
	//		0.125f * ap3[0].x + 0.375f * ap3[1].x + 0.375f * ap3[2].x + 0.125f * ap3[3].x,
	//		0.125f * ap3[0].y + 0.375f * ap3[1].y + 0.375f * ap3[2].y + 0.125f * ap3[3].y,
	//		0.125f * ap3[0].z + 0.375f * ap3[1].z + 0.375f * ap3[2].z + 0.125f * ap3[3].z
	//	);
	//	Point3f p1 = new Point3f(
	//		0.125f * ap3[3].x + 0.375f * ap3[4].x + 0.375f * ap3[5].x + 0.125f * ap3[6].x,
	//		0.125f * ap3[3].y + 0.375f * ap3[4].y + 0.375f * ap3[5].y + 0.125f * ap3[6].y,
	//		0.125f * ap3[3].z + 0.375f * ap3[4].z + 0.375f * ap3[5].z + 0.125f * ap3[6].z
	//	);
	//	Point3f p2 = new Point3f(
	//		0.125f * ap3[6].x + 0.375f * ap3[7].x + 0.375f * ap3[8].x + 0.125f * ap3[9].x,
	//		0.125f * ap3[6].y + 0.375f * ap3[7].y + 0.375f * ap3[8].y + 0.125f * ap3[9].y,
	//		0.125f * ap3[6].z + 0.375f * ap3[7].z + 0.375f * ap3[8].z + 0.125f * ap3[9].z
	//	);
	//	Point3f p3 = new Point3f(
	//		0.125f * ap3[9].x + 0.375f * ap3[10].x + 0.375f * ap3[11].x + 0.125f * ap3[0].x,
	//		0.125f * ap3[9].y + 0.375f * ap3[10].y + 0.375f * ap3[11].y + 0.125f * ap3[0].y,
	//		0.125f * ap3[9].z + 0.375f * ap3[10].z + 0.375f * ap3[11].z + 0.125f * ap3[0].z
	//	);
	//	Point3f pa = new Point3f(
	//		0.5f * (ap3[4].x - ap3[3].x + ap3[11].x - ap3[0].x) + p0.x,
	//		0.5f * (ap3[4].y - ap3[3].y + ap3[11].y - ap3[0].y) + p0.y,
	//		0.5f * (ap3[4].z - ap3[3].z + ap3[11].z - ap3[0].z) + p0.z
	//	);
	//	Point3f pb = new Point3f(
	//		0.5f * (ap3[2].x - ap3[3].x + ap3[7].x - ap3[6].x) + p1.x,
	//		0.5f * (ap3[2].y - ap3[3].y + ap3[7].y - ap3[6].y) + p1.y,
	//		0.5f * (ap3[2].z - ap3[3].z + ap3[7].z - ap3[6].z) + p1.z
	//	);
	//	Point3f pc = new Point3f(
	//		0.5f * (ap3[5].x - ap3[6].x + ap3[10].x - ap3[9].x) + p2.x,
	//		0.5f * (ap3[5].y - ap3[6].y + ap3[10].y - ap3[9].y) + p2.y,
	//		0.5f * (ap3[5].z - ap3[6].z + ap3[10].z - ap3[9].z) + p2.z
	//	);
	//	Point3f pd = new Point3f(
	//		0.5f * (ap3[8].x - ap3[9].x + ap3[1].x - ap3[0].x) + p3.x,
	//		0.5f * (ap3[8].y - ap3[9].y + ap3[1].y - ap3[0].y) + p3.y,
	//		0.5f * (ap3[8].z - ap3[9].z + ap3[1].z - ap3[0].z) + p3.z
	//	);
	//	Vector3f av1 = new Vector3f(0.5f * (av3New[1].x + av3New[7].x), 0.5f * (av3New[1].y + av3New[7].y), 0.5f * (av3New[1].z + av3New[7].z));
	//	Vector3f av2 = new Vector3f(0.5f * (av3New[3].x + av3New[5].x), 0.5f * (av3New[3].y + av3New[5].y), 0.5f * (av3New[3].z + av3New[5].z));
	//	Vector3f A = (aiLevel[1] > 0 || aiLevel[3] > 0) ? interpolateNormal(av3New[1], av3New[7]) : interpolateNormal(av3New[1], av3New[7], p0, pa, pc, p2);
	//	A.add(A);
	//	A.sub(av1);
	//	Vector3f B = (aiLevel[0] > 0 || aiLevel[2] > 0) ? interpolateNormal(av3New[3], av3New[5]) : interpolateNormal(av3New[3], av3New[5], p3, pd, pb, p1);
	//	B.add(B);
	//	B.sub(av2);
	//	av3New[4] = new Vector3f(A.x + B.x, A.y + B.y, A.z + B.z);
	//	drawHashPatch4(ap3, av3New, 0, 1, 0, 1, ac, abFlat, aiLevel, mp);
	//}
	
	//private void drawHashPatch4(Point3f[] ap3, Vector3f[] av3, float u0, float u1, float v0, float v1, int[] ac, boolean[] abFlat, int[] aiLevel, MaterialProperties mp) {
	//	//System.out.println("u = " + u0 + " ... " + u1);
	//	//System.out.println("v = " + v0 + " ... " + v1);
	//	//for (int i = 0; i < av3.length; System.out.println("\t" + av3[i++]));
	//	
	//	boolean visible = false;
	//	loop:
	//	for (int i = 0; i < 12; i++) {
	//		if (ap3[i].x > 0 && ap3[i].x < iWidth && ap3[i].y > 0 && ap3[i].y < iHeight) {
	//			visible = true;
	//			break loop;
	//		}
	//	}
	//	if (!visible) return;
	//	//System.out.println(abFlat[0] + "\t" + abFlat[1] + "\t" + abFlat[2] + "\t" + abFlat[3]);
	//	
	//	/* check if we need u-split */
	//	float us0 = subdiv(ap3[0], ap3[1], ap3[2], ap3[3], aiLevel[0] > 0);
	//	float us2 = subdiv(ap3[9], ap3[8], ap3[7], ap3[6], aiLevel[2] > 0);
	//	float vs3 = subdiv(ap3[0], ap3[11], ap3[10], ap3[9], aiLevel[3] > 0);
	//	float vs1 = subdiv(ap3[3], ap3[4], ap3[5], ap3[6], aiLevel[1] > 0);
	//	
	//	//float ul = (aiLevel[0] >= iMaxSubdiv || aiLevel[2] >= iMaxSubdiv) ? -1 : (float) Math.max(u0, u2);
	//	//float uv = (aiLevel[1] >= iMaxSubdiv || aiLevel[3] >= iMaxSubdiv) ? -1 : (float) Math.max(v1, v3);
	//	
	//	if ((aiLevel[0] < iMaxSubdiv && aiLevel[1] < iMaxSubdiv && aiLevel[2] < iMaxSubdiv && aiLevel[3] < iMaxSubdiv) && (us0 > fFlatness || us2 > fFlatness || vs1 > fFlatness || vs3 > fFlatness)) {
	//	//if (false) {
	//	//if (subdiv(ap3[0], ap3[1], ap3[2], ap3[3], uSplit) || subdiv(ap3[9], ap3[8], ap3[7], ap3[6], uSplit)) {
	//		
	//		/* flatten cubics if flat enough */
	//		if (!abFlat[0] && (us0 <= fFlatness)) abFlat[0] = makeFlat(ap3[0], ap3[1], ap3[2], ap3[3]);
	//		if (!abFlat[1] && (vs1 <= fFlatness)) abFlat[1] = makeFlat(ap3[3], ap3[4], ap3[5], ap3[6]);
	//		if (!abFlat[2] && (us2 <= fFlatness)) abFlat[2] = makeFlat(ap3[9], ap3[8], ap3[7], ap3[6]);
	//		if (!abFlat[3] && (vs3 <= fFlatness)) abFlat[3] = makeFlat(ap3[0], ap3[11], ap3[10], ap3[9]);
	//		
	//		
	//		///* compute new normals */
	//		//Vector3f[] av3new = newNormals(4);
	//		//av3new[1].set(av3[1]);
	//		//av3new[0] = av3[1] = (aiLevel[0] > 0) ? interpolateNormal(av3[0], av3[1]) : interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]);
	//		//av3new[2].set(av3[2]);
	//		//av3new[3] = av3[2] = (aiLevel[2] > 0) ? interpolateNormal(av3[3], av3[2]) : interpolateNormal(av3[3], av3[2], ap3[9], ap3[8], ap3[7], ap3[6]);
	//		
	//		/* compute new patches */
	//		Point3f[] ap3a = newPatch(12);
	//		Point3f[] ap3b = newPatch(12);
	//		Point3f[] ap3c = newPatch(12);
	//		Point3f[] ap3d = newPatch(12);
	//		
	//		/* deCasteljau outer boundaries */
	//		ap3a[0].set(ap3[0]);
	//		ap3a[1].set(ap3[1]);
	//		ap3a[2].set(ap3[2]);
	//		ap3a[3].set(ap3[3]);
	//		deCasteljauSplit(ap3a[0], ap3a[1], ap3a[2], ap3a[3], ap3b[0], ap3b[1], ap3b[2], ap3b[3]);
	//		
	//		ap3b[4].set(ap3[4]);
	//		ap3b[5].set(ap3[5]);
	//		ap3b[6].set(ap3[6]);
	//		deCasteljauSplit(ap3b[3], ap3b[4], ap3b[5], ap3b[6], ap3c[3], ap3c[4], ap3c[5], ap3c[6]);
	//		
	//		ap3c[7].set(ap3[7]);
	//		ap3c[8].set(ap3[8]);
	//		ap3c[9].set(ap3[9]);
	//		deCasteljauSplit(ap3c[6], ap3c[7], ap3c[8], ap3c[9], ap3d[6], ap3d[7], ap3d[8], ap3d[9]);
	//		
	//		ap3d[10].set(ap3[10]);
	//		ap3d[11].set(ap3[11]);
	//		ap3d[0].set(ap3[0]);
	//		deCasteljauSplit(ap3d[9], ap3d[10], ap3d[11], ap3d[0], ap3a[9], ap3a[10], ap3a[11], ap3a[0]);
	//		
	//		/* setup average tangents */
	//		Point3f p3A = new Point3f(
	//			0.5f * (ap3[4].x - ap3[3].x + ap3[11].x - ap3[0].x) + ap3b[0].x,
	//			0.5f * (ap3[4].y - ap3[3].y + ap3[11].y - ap3[0].y) + ap3b[0].y,
	//			0.5f * (ap3[4].z - ap3[3].z + ap3[11].z - ap3[0].z) + ap3b[0].z
	//		);
	//		Point3f p3B = new Point3f(
	//			0.5f * (ap3[2].x - ap3[3].x + ap3[7].x - ap3[6].x) + ap3c[3].x,
	//			0.5f * (ap3[2].y - ap3[3].y + ap3[7].y - ap3[6].y) + ap3c[3].y,
	//			0.5f * (ap3[2].z - ap3[3].z + ap3[7].z - ap3[6].z) + ap3c[3].z
	//		);
	//		Point3f p3C = new Point3f(
	//			0.5f * (ap3[5].x - ap3[6].x + ap3[10].x - ap3[9].x) + ap3d[6].x,
	//			0.5f * (ap3[5].y - ap3[6].y + ap3[10].y - ap3[9].y) + ap3d[6].y,
	//			0.5f * (ap3[5].z - ap3[6].z + ap3[10].z - ap3[9].z) + ap3d[6].z
	//		);
	//		Point3f p3D = new Point3f(
	//			0.5f * (ap3[8].x - ap3[9].x + ap3[1].x - ap3[0].x) + ap3a[9].x,
	//			0.5f * (ap3[8].y - ap3[9].y + ap3[1].y - ap3[0].y) + ap3a[9].y,
	//			0.5f * (ap3[8].z - ap3[9].z + ap3[1].z - ap3[0].z) + ap3a[9].z
	//		);
	//		//drawPoint3D(p3A,5);
	//		//drawPoint3D(p3B,5);
	//		//drawPoint3D(p3C,5);
	//		//drawPoint3D(p3D,5);
	//		
	//		/* deCasteljau split inner curves */
	//		ap3a[8].set(p3D);
	//		ap3a[7].set(p3B);
	//		ap3a[6].set(ap3b[6]);
	//		deCasteljauSplit(ap3a[9], ap3a[8], ap3a[7], ap3a[6], ap3b[9], ap3b[8], ap3b[7], ap3b[6]);
	//		
	//		ap3a[4].set(p3A);
	//		ap3a[5].set(p3C);
	//		ap3a[6].set(ap3d[6]);
	//		deCasteljauSplit(ap3a[3], ap3a[4], ap3a[5], ap3a[6], ap3d[3], ap3d[4], ap3d[5], ap3d[6]);
	//		
	//		/* average center */
	//		ap3c[0].set(
	//			0.5f * (ap3b[9].x + ap3d[3].x),
	//			0.5f * (ap3b[9].y + ap3d[3].y),
	//			0.5f * (ap3b[9].z + ap3d[3].z)
	//		);
	//		
	//		/* correct center tangents */
	//		Vector3f v3corr = new Vector3f(
	//			ap3a[6].x - ap3c[0].x,
	//			ap3a[6].y - ap3c[0].y,
	//			ap3a[6].z - ap3c[0].z
	//		);
	//		ap3a[7].sub(v3corr);
	//		ap3b[8].sub(v3corr);
	//		ap3a[5].add(v3corr);
	//		ap3d[4].add(v3corr);
	//		
	//		ap3a[6].set(ap3c[0]);
	//		ap3b[9].set(ap3c[0]);
	//		ap3d[3].set(ap3c[0]);
	//		
	//		ap3b[11].set(ap3a[4]);
	//		ap3b[10].set(ap3a[5]);
	//		ap3c[11].set(ap3d[4]);
	//		ap3c[10].set(ap3d[5]);
	//		ap3d[1].set(ap3a[8]);
	//		ap3d[2].set(ap3a[7]);
	//		ap3c[1].set(ap3b[8]);
	//		ap3c[2].set(ap3b[7]);
	//		
	//		
	//		//Point3f[] ap3new = newPatch(12);
	//		//v3a.sub(ap3[11], ap3[0]);
	//		//v3b.sub(ap3[4], ap3[3]);
	//		//v3c.add(v3a, v3b);
	//		//v3c.scale(0.5f);
	//		//v3a.sub(ap3[10], ap3[9]);
	//		//v3b.sub(ap3[5], ap3[6]);
	//		//v3a.add(v3b);
	//		//v3a.scale(0.5f);
	//		//deCasteljauSplit(ap3[0], ap3[1], ap3[2], ap3[3], ap3new[0], ap3new[1], ap3new[2], ap3new[3]);
	//		//deCasteljauSplit(ap3[9], ap3[8], ap3[7], ap3[6], ap3new[9], ap3new[8], ap3new[7], ap3new[6]);
	//		//ap3new[4].set(ap3[4]);
	//		//ap3new[5].set(ap3[5]);
	//		//ap3[4].add(ap3[3], v3c);
	//		//ap3[5].add(ap3[6], v3a);
	//		//ap3new[11].set(ap3[4]);
	//		//ap3new[10].set(ap3[5]);
	//		
	//		///* compute new normals */
	//		//Vector3f[] av3new = newNormals(4);
	//		//av3new[1].set(av3[1]);
	//		////av3new[0] = av3[1] = (uLevel > 99) ? interpolateNormal(av3[0], av3[1]) : interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]);
	//		//av3new[2].set(av3[2]);
	//		////av3new[3] = av3[2] = (uLevel > 99) ? interpolateNormal(av3[3], av3[2]) : interpolateNormal(av3[3], av3[2], ap3[9], ap3[8], ap3[7], ap3[6]);
	//		//
	//		//v3a.sub(ap3[4], ap3[3]);
	//		//v3b.sub(ap3[2], ap3[3]);
	//		//av3[1].cross(v3b, v3a);
	//		//av3[1].normalize();
	//		//av3new[0].set(av3[1]);
	//		//v3a.sub(ap3[5], ap3[6]);
	//		//v3b.sub(ap3[7], ap3[6]);
	//		//av3[2].cross(v3a, v3b);
	//		//av3[2].normalize();
	//		//av3new[3].set(av3[2]);
	//		
	//		///* prepare new normals */
	//		//Vector3f[] av3a = newNormals(4);
	//		//Vector3f[] av3b = newNormals(4);
	//		//Vector3f[] av3c = newNormals(4);
	//		//Vector3f[] av3d = newNormals(4);
	//		//
	//		///* set corner normals */
	//		//av3a[0].set(av3[0]);
	//		//av3b[1].set(av3[1]);
	//		//av3c[2].set(av3[2]);
	//		//av3d[3].set(av3[3]);
	//		//
	//		///* compute missing normals */
	//		//if (abFlat[0]) {
	//		//	v3c.set(interpolateNormal(av3[0], av3[1]));
	//		//} else {
	//		//	v3a.sub(ap3a[4], ap3a[3]);
	//		//	v3b.sub(ap3a[2], ap3a[3]);
	//		//	v3c.cross(v3b, v3a);
	//		//	v3c.normalize();
	//		//}
	//		//av3a[1].set(v3c);
	//		//av3b[3].set(v3c);
	//		//
	//		//if (abFlat[1]) {
	//		//	v3c.set(interpolateNormal(av3[1], av3[2]));
	//		//} else {
	//		//	v3a.sub(ap3b[7], ap3b[6]);
	//		//	v3b.sub(ap3b[5], ap3b[6]);
	//		//	v3c.cross(v3b, v3a);
	//		//	v3c.normalize();
	//		//}
	//		//av3b[2].set(v3c);
	//		//av3c[1].set(v3c);
	//		//
	//		//if (abFlat[2]) {
	//		//	v3c.set(interpolateNormal(av3[2], av3[3]));
	//		//} else {
	//		//	v3a.sub(ap3c[10], ap3c[9]);
	//		//	v3b.sub(ap3c[8], ap3c[9]);
	//		//	v3c.cross(v3b, v3a);
	//		//	v3c.normalize();
	//		//}
	//		//av3c[3].set(v3c);
	//		//av3d[2].set(v3c);
	//		//
	//		//if (abFlat[3]) {
	//		//	v3c.set(interpolateNormal(av3[3], av3[0]));
	//		//} else {
	//		//	v3a.sub(ap3d[1], ap3d[0]);
	//		//	v3b.sub(ap3d[11], ap3d[0]);
	//		//	v3c.cross(v3b, v3a);
	//		//	v3c.normalize();
	//		//}
	//		//av3d[0].set(v3c);
	//		//av3a[3].set(v3c);
	//		
	//		//v3a = (aiLevel[0] > 0) ? interpolateNormal(av3[0], av3[1]) : interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]);
	//		//av3a[1].set(v3a);
	//		//av3b[0].set(v3a);
	//		//v3a = (aiLevel[1] > 0) ? interpolateNormal(av3[1], av3[2]) : interpolateNormal(av3[1], av3[2], ap3[3], ap3[4], ap3[5], ap3[6]);
	//		//av3b[2].set(v3a);
	//		//av3c[1].set(v3a);
	//		//v3a = (aiLevel[2] > 0) ? interpolateNormal(av3[2], av3[3]) : interpolateNormal(av3[2], av3[3], ap3[6], ap3[7], ap3[8], ap3[9]);
	//		//av3c[3].set(v3a);
	//		//av3d[2].set(v3a);
	//		//v3a = (aiLevel[3] > 0) ? interpolateNormal(av3[3], av3[0]) : interpolateNormal(av3[3], av3[0], ap3[9], ap3[10], ap3[11], ap3[0]);
	//		//av3d[0].set(v3a);
	//		//av3a[3].set(v3a);
	//		//
	//		//Vector3f A = (aiLevel[1] > 0 || aiLevel[3] > 0) ? interpolateNormal(av3a[1], av3c[3]) : interpolateNormal(av3a[1], av3c[3], ap3a[3], p3A, p3C, ap3c[9]);
	//		//Vector3f B = (aiLevel[0] > 0 || aiLevel[2] > 0) ? interpolateNormal(av3a[3], av3c[1]) : interpolateNormal(av3a[3], av3c[1], ap3a[9], p3D, p3B, ap3c[3]);
	//		//v3c = interpolateNormal(A, B);
	//		//
	//		////v3a.sub(ap3c[1], ap3c[0]);
	//		////v3b.sub(ap3c[11], ap3c[0]);
	//		////v3c.cross(v3b, v3a);
	//		////v3c.normalize();
	//		//av3a[2].set(v3c);
	//		//av3b[3].set(v3c);
	//		//av3c[0].set(v3c);
	//		//av3d[1].set(v3c);
	//		
	//		/* new u/v's */
	//		float u0a, u1a, v0a, v1a;
	//		float u0b, u1b, v0b, v1b;
	//		float u0c, u1c, v0c, v1c;
	//		float u0d, u1d, v0d, v1d;
	//		
	//		u0a = u0d = u0;
	//		u1a = u1d = u0b = u0c = 0.5f * (u0 + u1);
	//		u1b = u1c = u1;
	//		v0a = v0b = v0;
	//		v1a = v1b = v0d = v0c = 0.5f * (v0 + v1);
	//		v1d = v1c = v1;
	//		
	//		/* compute new colors */
	//		int[] aca = new int[4];
	//		int[] acb = new int[4];
	//		int[] acc = new int[4];
	//		int[] acd = new int[4];
	//		aca[0] = ac[0];
	//		acb[1] = ac[1];
	//		acc[2] = ac[2];
	//		acd[3] = ac[3];
	//		aca[1] = acb[0] = (!abFlat[0]) ? lighting.shade(ap3a[3], biquadraticInterpolate(av3, u1a, v0), mp) : interpolateColor(ac[0], ac[1]);
	//		acb[2] = acc[1] = (!abFlat[1]) ? lighting.shade(ap3b[6], biquadraticInterpolate(av3, u1, v1a), mp) : interpolateColor(ac[1], ac[2]);
	//		acc[3] = acd[2] = (!abFlat[2]) ? lighting.shade(ap3c[9], biquadraticInterpolate(av3, u1a, v1), mp) : interpolateColor(ac[2], ac[3]);
	//		acd[0] = aca[3] = (!abFlat[3]) ? lighting.shade(ap3d[0], biquadraticInterpolate(av3, u0, v1a), mp) : interpolateColor(ac[3], ac[0]);
	//		aca[2] = acb[3] = acc[0] = acd[1] = lighting.shade(ap3c[0], biquadraticInterpolate(av3, u1a, v1a), mp);
	//		
	//		//int[] acnew = new int[4];
	//		//acnew[1] = ac[1];
	//		//acnew[0] = ac[1] = (!abFlat[0]) ? lighting.shade(ap3[3], av3[1], mp) : interpolateColor(ac[0], ac[1]);
	//		//acnew[2] = ac[2];
	//		//acnew[3] = ac[2] = (!abFlat[2]) ? lighting.shade(ap3[6], av3[2], mp) : interpolateColor(ac[2], ac[3]);
	//		
	//		/* set up new flatenough flags */
	//		boolean[] aba = new boolean[4];
	//		boolean[] abb = new boolean[4];
	//		boolean[] abc = new boolean[4];
	//		boolean[] abd = new boolean[4];
	//		
	//		aba[0] = abb[0] = abFlat[0];
	//		abb[1] = abc[1] = abFlat[1];
	//		abc[2] = abd[2] = abFlat[2];
	//		abd[3] = aba[3] = abFlat[3];
	//		aba[1] = aba[2] = abb[2] = abb[3] = abc[3] = abc[0] = abd[0] = abd[1] = false;
	//		
	//		
	//		//boolean[] abnew = new boolean[4];
	//		//abnew[0] = abFlat[0];
	//		//abnew[1] = abFlat[1];
	//		//abnew[2] = abFlat[2];
	//		//abnew[3] = abFlat[1] = false;
	//		
	//		///* compute new normals */
	//		//Vector3f[] av3new = newNormals();
	//		//av3new[1].set(av3[1]);
	//		//v3b.sub(ap3[3],ap3[2]);
	//		//av3[1].cross(v3b, v3c);
	//		//av3[1].normalize();
	//		//av3new[0].set(av3[1]);
	//		//av3new[2].set(av3[2]);
	//		//v3b.sub(ap3[7],ap3[6]);
	//		//av3[2].cross(v3b, v3a);
	//		//av3[2].normalize();
	//		//av3new[3].set(av3[2]);
	//		
	//		/* recurse */
	//		//int l = (aiLevel[1] < aiLevel[3]) ? aiLevel[1] : aiLevel[3];
	//		//aiLevel[0]++;
	//		//aiLevel[2]++;
	//		//int[] newLevels = new int[aiLevel.length];
	//		//for (int i = 0; i < aiLevel.length; newLevels[i] = aiLevel[i++]);
	//		//aiLevel[1] = newLevels[3] = l;
	//		//drawHashPatch4(ap3, av3, ac, abFlat, aiLevel, mp);
	//		//drawHashPatch4(ap3new, av3new, acnew, abnew, newLevels, mp);
	//		int l = aiLevel[0] + 1;
	//		int[] levels = new int[] { l, l, l, l };
	//		
	//		drawHashPatch4(ap3a, av3, u0a, u1a, v0a, v1a, aca, aba, levels, mp);
	//		drawHashPatch4(ap3b, av3, u0b, u1b, v0b, v1b, acb, abb, levels, mp);
	//		drawHashPatch4(ap3c, av3, u0c, u1c, v0c, v1c, acc, abc, levels, mp);
	//		drawHashPatch4(ap3d, av3, u0d, u1d, v0d, v1d, acd, abd, levels, mp);
	//		return;
	//	}
	//	draw3DTriangleGourad(ap3[9], ap3[3], ap3[0], ac[3], ac[1], ac[0]);
	//	draw3DTriangleGourad(ap3[3], ap3[9], ap3[6], ac[1], ac[3], ac[2]);
	//}
	
	//private void drawHashPatch4(Point3f[] ap3, Vector3f[] av3, int[] ac, boolean[] abFlat, int[] aiLevel, MaterialProperties mp) {
	//	//System.out.println("u = " + u0 + " ... " + u1);
	//	//System.out.println("v = " + v0 + " ... " + v1);
	//	//for (int i = 0; i < av3.length; System.out.println("\t" + av3[i++]));
	//	
	//	boolean visible = false;
	//	loop:
	//	for (int i = 0; i < 12; i++) {
	//		if (ap3[i].x > 0 && ap3[i].x < iWidth && ap3[i].y > 0 && ap3[i].y < iHeight) {
	//			visible = true;
	//			break loop;
	//		}
	//	}
	//	if (!visible) return;
	//	//System.out.println(abFlat[0] + "\t" + abFlat[1] + "\t" + abFlat[2] + "\t" + abFlat[3]);
	//	
	//	/* check if we need u-split */
	//	float us0 = subdiv(ap3[0], ap3[1], ap3[2], ap3[3], aiLevel[0] > 0);
	//	float us2 = subdiv(ap3[9], ap3[8], ap3[7], ap3[6], aiLevel[2] > 0);
	//	float vs3 = subdiv(ap3[0], ap3[11], ap3[10], ap3[9], aiLevel[3] > 0);
	//	float vs1 = subdiv(ap3[3], ap3[4], ap3[5], ap3[6], aiLevel[1] > 0);
	//	
	//	//float ul = (aiLevel[0] >= iMaxSubdiv || aiLevel[2] >= iMaxSubdiv) ? -1 : (float) Math.max(u0, u2);
	//	//float uv = (aiLevel[1] >= iMaxSubdiv || aiLevel[3] >= iMaxSubdiv) ? -1 : (float) Math.max(v1, v3);
	//	
	//	if ((aiLevel[0] < iMaxSubdiv && aiLevel[1] < iMaxSubdiv && aiLevel[2] < iMaxSubdiv && aiLevel[3] < iMaxSubdiv) && (us0 > fFlatness || us2 > fFlatness || vs1 > fFlatness || vs3 > fFlatness)) {
	//	//if (false) {
	//	//if (subdiv(ap3[0], ap3[1], ap3[2], ap3[3], uSplit) || subdiv(ap3[9], ap3[8], ap3[7], ap3[6], uSplit)) {
	//		
	//		/* flatten cubics if flat enough */
	//		if (!abFlat[0] && (us0 <= fFlatness)) abFlat[0] = makeFlat(ap3[0], ap3[1], ap3[2], ap3[3]);
	//		if (!abFlat[1] && (vs1 <= fFlatness)) abFlat[1] = makeFlat(ap3[3], ap3[4], ap3[5], ap3[6]);
	//		if (!abFlat[2] && (us2 <= fFlatness)) abFlat[2] = makeFlat(ap3[9], ap3[8], ap3[7], ap3[6]);
	//		if (!abFlat[3] && (vs3 <= fFlatness)) abFlat[3] = makeFlat(ap3[0], ap3[11], ap3[10], ap3[9]);
	//		
	//		
	//		///* compute new normals */
	//		//Vector3f[] av3new = newNormals(4);
	//		//av3new[1].set(av3[1]);
	//		//av3new[0] = av3[1] = (aiLevel[0] > 0) ? interpolateNormal(av3[0], av3[1]) : interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]);
	//		//av3new[2].set(av3[2]);
	//		//av3new[3] = av3[2] = (aiLevel[2] > 0) ? interpolateNormal(av3[3], av3[2]) : interpolateNormal(av3[3], av3[2], ap3[9], ap3[8], ap3[7], ap3[6]);
	//		
	//		/* compute new patches */
	//		Point3f[] ap3a = newPatch(12);
	//		Point3f[] ap3b = newPatch(12);
	//		Point3f[] ap3c = newPatch(12);
	//		Point3f[] ap3d = newPatch(12);
	//		
	//		/* deCasteljau outer boundaries */
	//		ap3a[0].set(ap3[0]);
	//		ap3a[1].set(ap3[1]);
	//		ap3a[2].set(ap3[2]);
	//		ap3a[3].set(ap3[3]);
	//		deCasteljauSplit(ap3a[0], ap3a[1], ap3a[2], ap3a[3], ap3b[0], ap3b[1], ap3b[2], ap3b[3]);
	//		
	//		ap3b[4].set(ap3[4]);
	//		ap3b[5].set(ap3[5]);
	//		ap3b[6].set(ap3[6]);
	//		deCasteljauSplit(ap3b[3], ap3b[4], ap3b[5], ap3b[6], ap3c[3], ap3c[4], ap3c[5], ap3c[6]);
	//		
	//		ap3c[7].set(ap3[7]);
	//		ap3c[8].set(ap3[8]);
	//		ap3c[9].set(ap3[9]);
	//		deCasteljauSplit(ap3c[6], ap3c[7], ap3c[8], ap3c[9], ap3d[6], ap3d[7], ap3d[8], ap3d[9]);
	//		
	//		ap3d[10].set(ap3[10]);
	//		ap3d[11].set(ap3[11]);
	//		ap3d[0].set(ap3[0]);
	//		deCasteljauSplit(ap3d[9], ap3d[10], ap3d[11], ap3d[0], ap3a[9], ap3a[10], ap3a[11], ap3a[0]);
	//		
	//		/* setup average tangents */
	//		Point3f p3A = new Point3f(
	//			0.5f * (ap3[4].x - ap3[3].x + ap3[11].x - ap3[0].x) + ap3b[0].x,
	//			0.5f * (ap3[4].y - ap3[3].y + ap3[11].y - ap3[0].y) + ap3b[0].y,
	//			0.5f * (ap3[4].z - ap3[3].z + ap3[11].z - ap3[0].z) + ap3b[0].z
	//		);
	//		Point3f p3B = new Point3f(
	//			0.5f * (ap3[2].x - ap3[3].x + ap3[7].x - ap3[6].x) + ap3c[3].x,
	//			0.5f * (ap3[2].y - ap3[3].y + ap3[7].y - ap3[6].y) + ap3c[3].y,
	//			0.5f * (ap3[2].z - ap3[3].z + ap3[7].z - ap3[6].z) + ap3c[3].z
	//		);
	//		Point3f p3C = new Point3f(
	//			0.5f * (ap3[5].x - ap3[6].x + ap3[10].x - ap3[9].x) + ap3d[6].x,
	//			0.5f * (ap3[5].y - ap3[6].y + ap3[10].y - ap3[9].y) + ap3d[6].y,
	//			0.5f * (ap3[5].z - ap3[6].z + ap3[10].z - ap3[9].z) + ap3d[6].z
	//		);
	//		Point3f p3D = new Point3f(
	//			0.5f * (ap3[8].x - ap3[9].x + ap3[1].x - ap3[0].x) + ap3a[9].x,
	//			0.5f * (ap3[8].y - ap3[9].y + ap3[1].y - ap3[0].y) + ap3a[9].y,
	//			0.5f * (ap3[8].z - ap3[9].z + ap3[1].z - ap3[0].z) + ap3a[9].z
	//		);
	//		//drawPoint3D(p3A,5);
	//		//drawPoint3D(p3B,5);
	//		//drawPoint3D(p3C,5);
	//		//drawPoint3D(p3D,5);
	//		
	//		/* deCasteljau split inner curves */
	//		ap3a[8].set(p3D);
	//		ap3a[7].set(p3B);
	//		ap3a[6].set(ap3b[6]);
	//		deCasteljauSplit(ap3a[9], ap3a[8], ap3a[7], ap3a[6], ap3b[9], ap3b[8], ap3b[7], ap3b[6]);
	//		
	//		ap3a[4].set(p3A);
	//		ap3a[5].set(p3C);
	//		ap3a[6].set(ap3d[6]);
	//		deCasteljauSplit(ap3a[3], ap3a[4], ap3a[5], ap3a[6], ap3d[3], ap3d[4], ap3d[5], ap3d[6]);
	//		
	//		/* average center */
	//		ap3c[0].set(
	//			0.5f * (ap3b[9].x + ap3d[3].x),
	//			0.5f * (ap3b[9].y + ap3d[3].y),
	//			0.5f * (ap3b[9].z + ap3d[3].z)
	//		);
	//		
	//		/* correct center tangents */
	//		Vector3f v3corr = new Vector3f(
	//			ap3a[6].x - ap3c[0].x,
	//			ap3a[6].y - ap3c[0].y,
	//			ap3a[6].z - ap3c[0].z
	//		);
	//		ap3a[7].sub(v3corr);
	//		ap3b[8].sub(v3corr);
	//		ap3a[5].add(v3corr);
	//		ap3d[4].add(v3corr);
	//		
	//		ap3a[6].set(ap3c[0]);
	//		ap3b[9].set(ap3c[0]);
	//		ap3d[3].set(ap3c[0]);
	//		
	//		ap3b[11].set(ap3a[4]);
	//		ap3b[10].set(ap3a[5]);
	//		ap3c[11].set(ap3d[4]);
	//		ap3c[10].set(ap3d[5]);
	//		ap3d[1].set(ap3a[8]);
	//		ap3d[2].set(ap3a[7]);
	//		ap3c[1].set(ap3b[8]);
	//		ap3c[2].set(ap3b[7]);
	//		
	//		
	//		//Point3f[] ap3new = newPatch(12);
	//		//v3a.sub(ap3[11], ap3[0]);
	//		//v3b.sub(ap3[4], ap3[3]);
	//		//v3c.add(v3a, v3b);
	//		//v3c.scale(0.5f);
	//		//v3a.sub(ap3[10], ap3[9]);
	//		//v3b.sub(ap3[5], ap3[6]);
	//		//v3a.add(v3b);
	//		//v3a.scale(0.5f);
	//		//deCasteljauSplit(ap3[0], ap3[1], ap3[2], ap3[3], ap3new[0], ap3new[1], ap3new[2], ap3new[3]);
	//		//deCasteljauSplit(ap3[9], ap3[8], ap3[7], ap3[6], ap3new[9], ap3new[8], ap3new[7], ap3new[6]);
	//		//ap3new[4].set(ap3[4]);
	//		//ap3new[5].set(ap3[5]);
	//		//ap3[4].add(ap3[3], v3c);
	//		//ap3[5].add(ap3[6], v3a);
	//		//ap3new[11].set(ap3[4]);
	//		//ap3new[10].set(ap3[5]);
	//		
	//		///* compute new normals */
	//		//Vector3f[] av3new = newNormals(4);
	//		//av3new[1].set(av3[1]);
	//		////av3new[0] = av3[1] = (uLevel > 99) ? interpolateNormal(av3[0], av3[1]) : interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]);
	//		//av3new[2].set(av3[2]);
	//		////av3new[3] = av3[2] = (uLevel > 99) ? interpolateNormal(av3[3], av3[2]) : interpolateNormal(av3[3], av3[2], ap3[9], ap3[8], ap3[7], ap3[6]);
	//		//
	//		//v3a.sub(ap3[4], ap3[3]);
	//		//v3b.sub(ap3[2], ap3[3]);
	//		//av3[1].cross(v3b, v3a);
	//		//av3[1].normalize();
	//		//av3new[0].set(av3[1]);
	//		//v3a.sub(ap3[5], ap3[6]);
	//		//v3b.sub(ap3[7], ap3[6]);
	//		//av3[2].cross(v3a, v3b);
	//		//av3[2].normalize();
	//		//av3new[3].set(av3[2]);
	//		
	//		/* prepare new normals */
	//		Vector3f[] av3a = newNormals(4);
	//		Vector3f[] av3b = newNormals(4);
	//		Vector3f[] av3c = newNormals(4);
	//		Vector3f[] av3d = newNormals(4);
	//		
	//		/* set corner normals */
	//		av3a[0].set(av3[0]);
	//		av3b[1].set(av3[1]);
	//		av3c[2].set(av3[2]);
	//		av3d[3].set(av3[3]);
	//		
	//		/* compute missing normals */
	//		//if (abFlat[0]) {
	//		//	v3c.set(interpolateNormal(av3[0], av3[1]));
	//		//} else {
	//		//	v3a.sub(ap3a[4], ap3a[3]);
	//		//	v3b.sub(ap3a[2], ap3a[3]);
	//		//	v3c.cross(v3b, v3a);
	//		//	v3c.normalize();
	//		//}
	//		//av3a[1].set(v3c);
	//		//av3b[3].set(v3c);
	//		//
	//		//if (abFlat[1]) {
	//		//	v3c.set(interpolateNormal(av3[1], av3[2]));
	//		//} else {
	//		//	v3a.sub(ap3b[7], ap3b[6]);
	//		//	v3b.sub(ap3b[5], ap3b[6]);
	//		//	v3c.cross(v3b, v3a);
	//		//	v3c.normalize();
	//		//}
	//		//av3b[2].set(v3c);
	//		//av3c[1].set(v3c);
	//		//
	//		//if (abFlat[2]) {
	//		//	v3c.set(interpolateNormal(av3[2], av3[3]));
	//		//} else {
	//		//	v3a.sub(ap3c[10], ap3c[9]);
	//		//	v3b.sub(ap3c[8], ap3c[9]);
	//		//	v3c.cross(v3b, v3a);
	//		//	v3c.normalize();
	//		//}
	//		//av3c[3].set(v3c);
	//		//av3d[2].set(v3c);
	//		//
	//		//if (abFlat[3]) {
	//		//	v3c.set(interpolateNormal(av3[3], av3[0]));
	//		//} else {
	//		//	v3a.sub(ap3d[1], ap3d[0]);
	//		//	v3b.sub(ap3d[11], ap3d[0]);
	//		//	v3c.cross(v3b, v3a);
	//		//	v3c.normalize();
	//		//}
	//		//av3d[0].set(v3c);
	//		//av3a[3].set(v3c);
	//		
	//		v3a = (aiLevel[0] > 0) ? interpolateNormal(av3[0], av3[1]) : interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]);
	//		av3a[1].set(v3a);
	//		av3b[0].set(v3a);
	//		v3a = (aiLevel[1] > 0) ? interpolateNormal(av3[1], av3[2]) : interpolateNormal(av3[1], av3[2], ap3[3], ap3[4], ap3[5], ap3[6]);
	//		av3b[2].set(v3a);
	//		av3c[1].set(v3a);
	//		v3a = (aiLevel[2] > 0) ? interpolateNormal(av3[2], av3[3]) : interpolateNormal(av3[2], av3[3], ap3[6], ap3[7], ap3[8], ap3[9]);
	//		av3c[3].set(v3a);
	//		av3d[2].set(v3a);
	//		v3a = (aiLevel[3] > 0) ? interpolateNormal(av3[3], av3[0]) : interpolateNormal(av3[3], av3[0], ap3[9], ap3[10], ap3[11], ap3[0]);
	//		av3d[0].set(v3a);
	//		av3a[3].set(v3a);
	//		
	//		Vector3f A = (aiLevel[1] > 0 || aiLevel[3] > 0) ? interpolateNormal(av3a[1], av3c[3]) : interpolateNormal(av3a[1], av3c[3], ap3a[3], p3A, p3C, ap3c[9]);
	//		Vector3f B = (aiLevel[0] > 0 || aiLevel[2] > 0) ? interpolateNormal(av3a[3], av3c[1]) : interpolateNormal(av3a[3], av3c[1], ap3a[9], p3D, p3B, ap3c[3]);
	//		v3c = interpolateNormal(A, B);
	//		//
	//		//v3a.sub(ap3c[1], ap3c[0]);
	//		//v3b.sub(ap3c[11], ap3c[0]);
	//		//v3c.cross(v3b, v3a);
	//		//v3c.normalize();
	//		av3a[2].set(v3c);
	//		av3b[3].set(v3c);
	//		av3c[0].set(v3c);
	//		av3d[1].set(v3c);
	//		
	//		///* new u/v's */
	//		//float u0a, u1a, v0a, v1a;
	//		//float u0b, u1b, v0b, v1b;
	//		//float u0c, u1c, v0c, v1c;
	//		//float u0d, u1d, v0d, v1d;
	//		//
	//		//u0a = u0d = u0;
	//		//u1a = u1d = u0b = u0c = 0.5f * (u0 + u1);
	//		//u1b = u1c = u1;
	//		//v0a = v0b = v0;
	//		//v1a = v1b = v0d = v0c = 0.5f * (v0 + v1);
	//		//v1d = v1c = v1;
	//		
	//		/* compute new colors */
	//		int[] aca = new int[4];
	//		int[] acb = new int[4];
	//		int[] acc = new int[4];
	//		int[] acd = new int[4];
	//		aca[0] = ac[0];
	//		acb[1] = ac[1];
	//		acc[2] = ac[2];
	//		acd[3] = ac[3];
	//		aca[1] = acb[0] = (!abFlat[0]) ? lighting.shade(ap3a[3], av3a[1], mp) : interpolateColor(ac[0], ac[1]);
	//		acb[2] = acc[1] = (!abFlat[1]) ? lighting.shade(ap3b[6], av3b[2], mp) : interpolateColor(ac[1], ac[2]);
	//		acc[3] = acd[2] = (!abFlat[2]) ? lighting.shade(ap3c[9], av3c[3], mp) : interpolateColor(ac[2], ac[3]);
	//		acd[0] = aca[3] = (!abFlat[3]) ? lighting.shade(ap3d[0], av3d[0], mp) : interpolateColor(ac[3], ac[0]);
	//		aca[2] = acb[3] = acc[0] = acd[1] = lighting.shade(ap3c[0], av3c[0], mp);
	//		
	//		//int[] acnew = new int[4];
	//		//acnew[1] = ac[1];
	//		//acnew[0] = ac[1] = (!abFlat[0]) ? lighting.shade(ap3[3], av3[1], mp) : interpolateColor(ac[0], ac[1]);
	//		//acnew[2] = ac[2];
	//		//acnew[3] = ac[2] = (!abFlat[2]) ? lighting.shade(ap3[6], av3[2], mp) : interpolateColor(ac[2], ac[3]);
	//		
	//		/* set up new flatenough flags */
	//		boolean[] aba = new boolean[4];
	//		boolean[] abb = new boolean[4];
	//		boolean[] abc = new boolean[4];
	//		boolean[] abd = new boolean[4];
	//		
	//		aba[0] = abb[0] = abFlat[0];
	//		abb[1] = abc[1] = abFlat[1];
	//		abc[2] = abd[2] = abFlat[2];
	//		abd[3] = aba[3] = abFlat[3];
	//		aba[1] = aba[2] = abb[2] = abb[3] = abc[3] = abc[0] = abd[0] = abd[1] = false;
	//		
	//		
	//		//boolean[] abnew = new boolean[4];
	//		//abnew[0] = abFlat[0];
	//		//abnew[1] = abFlat[1];
	//		//abnew[2] = abFlat[2];
	//		//abnew[3] = abFlat[1] = false;
	//		
	//		///* compute new normals */
	//		//Vector3f[] av3new = newNormals();
	//		//av3new[1].set(av3[1]);
	//		//v3b.sub(ap3[3],ap3[2]);
	//		//av3[1].cross(v3b, v3c);
	//		//av3[1].normalize();
	//		//av3new[0].set(av3[1]);
	//		//av3new[2].set(av3[2]);
	//		//v3b.sub(ap3[7],ap3[6]);
	//		//av3[2].cross(v3b, v3a);
	//		//av3[2].normalize();
	//		//av3new[3].set(av3[2]);
	//		
	//		/* recurse */
	//		//int l = (aiLevel[1] < aiLevel[3]) ? aiLevel[1] : aiLevel[3];
	//		//aiLevel[0]++;
	//		//aiLevel[2]++;
	//		//int[] newLevels = new int[aiLevel.length];
	//		//for (int i = 0; i < aiLevel.length; newLevels[i] = aiLevel[i++]);
	//		//aiLevel[1] = newLevels[3] = l;
	//		//drawHashPatch4(ap3, av3, ac, abFlat, aiLevel, mp);
	//		//drawHashPatch4(ap3new, av3new, acnew, abnew, newLevels, mp);
	//		int lu = (aiLevel[0] < aiLevel[2]) ? aiLevel[0] + 1 : aiLevel[2] + 1;
	//		int lv = (aiLevel[1] < aiLevel[3]) ? aiLevel[1] + 1 : aiLevel[3] + 1;
	//		
	//		drawHashPatch4(ap3a, av3a, aca, aba, new int[] {aiLevel[0] + 1, lv, lu, aiLevel[3] + 1}, mp);
	//		drawHashPatch4(ap3b, av3b, acb, abb, new int[] {aiLevel[0] + 1, aiLevel[1] + 1, lu, lv}, mp);
	//		drawHashPatch4(ap3c, av3c, acc, abc, new int[] {lu, aiLevel[1] + 1, aiLevel[2] + 1, lv}, mp);
	//		drawHashPatch4(ap3d, av3d, acd, abd, new int[] {lu, lv, aiLevel[2] + 1, aiLevel[3] + 1}, mp);
	//		return;
	//	}
	//	
	//	//}
	//	/* draw the patch */
	//	
	//	//System.out.println("drawPatch");
	//	//for (int i = 0; i < 12; System.out.println(ap3[i++]));
	//	
	//	//if (bBackfaceNormalFlip) flipBackfaceNormals(av3);
	//	//av3[0].normalize();
	//	//av3[1].normalize();
	//	//av3[2].normalize();
	//	//av3[3].normalize();
	//	
	//	//av3New[0].set(av3[0]);
	//	//av3New[1].set(av3[1]);
	//	//av3New[2].set(av3[2]);
	//	//av3New[3].set(av3[3]);
	//	//if (bBackfaceNormalFlip) flipBackfaceNormals(av3New);
	//	//int c0 = lighting.shade(ap3[0], av3New[0], mp);
	//	//int c1 = lighting.shade(ap3[3], av3New[1], mp);
	//	//int c2 = lighting.shade(ap3[6], av3New[2], mp);
	//	//int c3 = lighting.shade(ap3[9], av3New[3], mp);
	//	
	//	//drawLine3D(ap3[0], ap3[3]);
	//	//drawLine3D(ap3[3], ap3[6]);
	//	//drawLine3D(ap3[6], ap3[9]);
	//	//drawLine3D(ap3[9], ap3[0]);
	//	
	//	//for (int i = 0; i < 12; i++) {
	//	//	drawLine3D(ap3[i], ap3[(i + 1) % 12]);
	//	//	drawPoint3D(ap3[i],2);
	//	//}
	//	//
	//	//Point3f p = new Point3f();
	//	//p.set(ap3[0]);
	//	//v3a.set(av3[0]);
	//	//v3a.scale(30f);
	//	//p.add(v3a);
	//	//drawLine3D(ap3[0],p);
	//	//
	//	//p.set(ap3[3]);
	//	//v3a.set(av3[1]);
	//	//v3a.scale(30f);
	//	//p.add(v3a);
	//	//drawLine3D(ap3[3],p);
	//	//
	//	//p.set(ap3[6]);
	//	//v3a.set(av3[2]);
	//	//v3a.scale(30f);
	//	//p.add(v3a);
	//	//drawLine3D(ap3[6],p);
	//	//
	//	//p.set(ap3[9]);
	//	//v3a.set(av3[3]);
	//	//v3a.scale(30f);
	//	//p.add(v3a);
	//	//drawLine3D(ap3[9],p);
	//	
	//	//int ca = 0xFF777777;
	//	//int cb = 0xFF999999;
	//	//
	//	//draw3DTriangleGourad(ap3[0], ap3[3], ap3[9], ca, ca, ca);
	//	//draw3DTriangleGourad(ap3[6], ap3[9], ap3[3], cb, cb, cb);
	//	
	//	draw3DTriangleGourad(ap3[9], ap3[3], ap3[0], ac[3], ac[1], ac[0]);
	//	draw3DTriangleGourad(ap3[3], ap3[9], ap3[6], ac[1], ac[3], ac[2]);
	//	
	//}
	
	private Point3f[] newPatch(int n) {
		Point3f[] p = new Point3f[n];
		for (int i = 0; i < n; p[i++] = new Point3f());
		return p;
	}
	
	private Vector3f[] newNormals(int n) {
		Vector3f[] v = new Vector3f[n];
		for (int i = 0; i < n; v[i++] = new Vector3f());
		return v;
	}
	
	private void deCasteljauSplit(Point3f p0, Point3f p1, Point3f p2, Point3f p3, Point3f pn0, Point3f pn1, Point3f pn2, Point3f pn3) {
		pn0.set((p1.x + p2.x) * 0.5f, (p1.y + p2.y) * 0.5f, (p1.z + p2.z) * 0.5f);
		pn3.set(p3);
		pn2.set((p2.x + p3.x) * 0.5f, (p2.y + p3.y) * 0.5f, (p2.z + p3.z) * 0.5f);
		pn1.set((pn2.x + pn0.x) * 0.5f, (pn2.y + pn0.y) * 0.5f, (pn2.z + pn0.z) * 0.5f);
		p1.set((p0.x + p1.x) * 0.5f, (p0.y + p1.y) * 0.5f, (p0.z + p1.z) * 0.5f);
		p2.set((p1.x + pn0.x) * 0.5f, (p1.y + pn0.y) * 0.5f, (p1.z + pn0.z) * 0.5f);
		p3.set((p2.x + pn1.x) * 0.5f, (p2.y + pn1.y) * 0.5f, (p2.z + pn1.z) * 0.5f);
		pn0.set(p3);
	}
	
	//private void deCasteljauSplit(Vector3f v0, Vector3f v1, Vector3f v2, Vector3f vn0, Vector3f vn1, Vector3f vn2) {
	//	vn0.set(v1);
	//	vn1.set(v1.x + v2.x) * 0.5f, v1.y + v2.y) * 0.5f, v1.z + v2.z) * 0.5f);
	//	v1.set(v0.x + v1.x) * 0.5f, v0.y + v1.y) * 0.5f, v0.z + v1.z) * 0.5f);		
	//private void drawRectHashPatchGourad(Point3f[] ap3, Vector3f[] normals, MaterialProperties mp, int level) {
	//	
	//	/* set up corner colors */
	//	int[] colors = new int[4];
	//	colors[0] = lighting.shade(ap3[0], normals[0], mp);
	//	colors[1] = lighting.shade(ap3[3], normals[1], mp);
	//	colors[2] = lighting.shade(ap3[6], normals[2], mp);
	//	colors[3] = lighting.shade(ap3[9], normals[3], mp);
	//	
	//	/* set up flat-enough flags */
	//	boolean[] flat = new boolean[] { false, false, false, false };
	//	
	//	/* begin recursive subdivision and render patches */
	//	drawHashPatch4(ap3, normals, colors, flat, level, level, mp);
	//}

	public void drawHashPatchGourad(Point3f[] ap3, Vector3f[] av3, int[] ail, MaterialProperties mp) {
		boolean[] flat = new boolean[] { false, false, false, false };
		int[] colors = new int[4];
		switch (ap3.length) {
			case 9: {
				
				/* compute colors */
				colors[0] = lighting.shade(ap3[0], av3[0], mp);
				colors[1] = lighting.shade(ap3[3], av3[1], mp);
				colors[2] = lighting.shade(ap3[6], av3[2], mp);
				colors[3] = colors[0];
				
				///* set up corner normals */
				//Vector3f[] cn = newNormals(4);
				//v3a.sub(ap3[1], ap3[0]);
				//v3b.sub(ap3[8], ap3[0]);
				//cn[0].cross(v3b, v3a);
				//v3a.sub(ap3[4], ap3[3]);
				//v3b.sub(ap3[2], ap3[3]);
				//cn[1].cross(v3b, v3a);
				//v3a.sub(ap3[7], ap3[6]);
				//v3b.sub(ap3[5], ap3[6]);
				//cn[2].cross(v3b, v3a);
				//cn[0].normalize();
				//cn[1].normalize();
				//cn[2].normalize();
				//cn[3].set(cn[0]);
				
				Vector3f[] n = new Vector3f[] {av3[0], av3[1], av3[2], new Vector3f(av3[0])};
				
				Point3f[] p = new Point3f[] {ap3[0], ap3[1], ap3[2], ap3[3], ap3[4], ap3[5], ap3[6], ap3[7], ap3[8], new Point3f(ap3[0]), new Point3f(ap3[0]), new Point3f(ap3[0])};
				
				//p[0].set(ap3[0]);
				//p[1].set(ap3[1]);
				//p[2].set(ap3[2]);
				//p[3].set(ap3[3]);
				//p[4].set(ap3[4]);
				//p[5].set(ap3[5]);
				//p[6].set(ap3[6]);
				//p[7].set(ap3[7]);
				//p[8].set(ap3[8]);
				//p[9].set(ap3[0]);
				//p[10].set(ap3[0]);
				//p[11].set(ap3[0]);
				//
				//drawRectHashPatchGourad(p, n, mp, 0);
				int[] levels = new int[] { ail[0], ail[1], ail[2], 0 };
				drawHashPatch4Gaurad(p, n, colors, flat, levels, mp);
				//if (true) return;
				//
				/////* set up corner normals */
				////Vector3f[] cn = newNormals(3);
				////v3a.sub(ap3[1], ap3[0]);
				////v3b.sub(ap3[8], ap3[0]);
				////cn[0].cross(v3b, v3a);
				////v3a.sub(ap3[4], ap3[3]);
				////v3b.sub(ap3[2], ap3[3]);
				////cn[1].cross(v3b, v3a);
				////v3a.sub(ap3[7], ap3[6]);
				////v3b.sub(ap3[5], ap3[6]);
				////cn[2].cross(v3b, v3a);
				////cn[0].normalize();
				////cn[1].normalize();
				////cn[2].normalize();
				//
				////drawHashPatch3(ap3, cn, 0, mp);
				////if (true) return;
				//
				///* set up midpoint normals */
				//Vector3f[] mn = new Vector3f[5];
				//mn[0] = interpolateNormal(cn[0], cn[1], ap3[0], ap3[1], ap3[2], ap3[3]);
				//mn[1] = interpolateNormal(cn[1], cn[2], ap3[3], ap3[4], ap3[5], ap3[6]);
				//mn[2] = interpolateNormal(cn[2], cn[0], ap3[6], ap3[7], ap3[8], ap3[0]);
				//
				//Point3f A = Functions.parallelogram(ap3[0],ap3[1],ap3[8]);
				//Point3f B = Functions.parallelogram(ap3[3],ap3[4],ap3[2]);
				//Point3f C = Functions.parallelogram(ap3[6],ap3[7],ap3[5]);
				//
				//Point3f F = Functions.average(A,ap3[0],ap3[1],ap3[8]);
				//Point3f H = Functions.average(B,ap3[3],ap3[4],ap3[2]);
				//Point3f J = Functions.average(C,ap3[6],ap3[7],ap3[5]);
				//
				//Point3f G = Functions.average(A,B,ap3[1],ap3[2]);
				//Point3f I = Functions.average(B,C,ap3[4],ap3[5]);
				//Point3f K = Functions.average(C,A,ap3[7],ap3[8]);
				//
				//Point3f U = Functions.average(A,B,C);
				//
				//Point3f P = Functions.average(U,K,F,G);
				//Point3f Q = Functions.average(U,G,H,I);
				//Point3f R = Functions.average(U,I,J,K);
				//
				//Point3f V = Functions.average(R,P);
				//Point3f W = Functions.average(P,Q);
				//Point3f X = Functions.average(Q,R);
				//
				//Point3f Center = Functions.average(P,Q,R);
				//
				//Vector3f vc = Functions.vector(V, Center);
				//Vector3f wc = Functions.vector(W, Center);
				//Vector3f xc = Functions.vector(X, Center);
				//
				//Vector3f[] nc = newNormals(3);
				//nc[0].cross(wc, vc);
				//nc[1].cross(xc, wc);
				//nc[2].cross(vc, xc);
				//nc[0].normalize();
				//nc[1].normalize();
				//nc[2].normalize();
				//Vector3f centerNormal = Functions.vaverage(nc[0], nc[1], nc[2]);
				//centerNormal.normalize();
				//
				//Vector3f[] normals = newNormals(4);
				//
				//Point3f[][] aap3Boundary = new Point3f[3][7];
				//
				//aap3Boundary[0] = Bezier.deCasteljau(ap3[0],ap3[1],ap3[2],ap3[3],0.5f);
				//aap3Boundary[1] = Bezier.deCasteljau(ap3[3],ap3[4],ap3[5],ap3[6],0.5f);
				//aap3Boundary[2] = Bezier.deCasteljau(ap3[6],ap3[7],ap3[8],ap3[0],0.5f);
				//
				//Point3f[] ap3Patches = new Point3f[12];
				//
				//Vector3f v;
				//float sc = 0.33f;
				//
				//ap3Patches[0] = new Point3f(aap3Boundary[0][0]);
				//ap3Patches[1] = new Point3f(aap3Boundary[0][1]);
				//ap3Patches[2] = new Point3f(aap3Boundary[0][2]);
				//ap3Patches[3] = new Point3f(aap3Boundary[0][3]);
				//ap3Patches[11] = new Point3f(aap3Boundary[2][5]);
				//v = Functions.vaverage(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[8]));
				//v.scale(sc);
				//ap3Patches[4] = new Point3f(aap3Boundary[0][3]);
				//ap3Patches[4].add(v);
				////ap3Patches[4].add(Functions.average(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[8])));
				//ap3Patches[10] = new Point3f(aap3Boundary[2][4]);
				//ap3Patches[5] = new Point3f(W);
				//ap3Patches[9] = new Point3f(aap3Boundary[2][3]);
				//v = Functions.vaverage(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[6],ap3[5]));
				//v.scale(sc);
				//ap3Patches[8] = new Point3f(aap3Boundary[2][3]);
				//ap3Patches[8].add(v);
				////ap3Patches[8].add(Functions.average(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[6],ap3[5])));
				//ap3Patches[7] = new Point3f(V);
				//ap3Patches[6] = new Point3f(Center);
				//normals[0].set(cn[0]);
				//normals[1].set(mn[0]);
				//normals[2].set(centerNormal);
				//normals[3].set(mn[2]);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				//    
				//ap3Patches[0].set(aap3Boundary[1][0]);
				//ap3Patches[1].set(aap3Boundary[1][1]);
				//ap3Patches[2].set(aap3Boundary[1][2]);
				//ap3Patches[3].set(aap3Boundary[1][3]);
				//ap3Patches[11].set(aap3Boundary[0][5]);
				//v = Functions.vaverage(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2]));
				//v.scale(sc);
				//ap3Patches[4].add(aap3Boundary[1][3], v);
				////ap3Patches[4].add(Functions.average(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2])));
				//ap3Patches[10].set(aap3Boundary[0][4]);
				//ap3Patches[5].set(X);
				//ap3Patches[9].set(aap3Boundary[0][3]);
				//v = Functions.vaverage(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[8]));
				//v.scale(sc);
				//ap3Patches[8].add(aap3Boundary[0][3], v);
				////ap3Patches[8].add(Functions.average(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[8])));
				//ap3Patches[7].set(W);
				//ap3Patches[6].set(Center);
				//normals[0].set(cn[1]);
				//normals[1].set(mn[1]);
				//normals[2].set(centerNormal);
				//normals[3].set(mn[0]);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				//
				//ap3Patches[0].set(aap3Boundary[2][0]);
				//ap3Patches[1].set(aap3Boundary[2][1]);
				//ap3Patches[2].set(aap3Boundary[2][2]);
				//ap3Patches[3].set(aap3Boundary[2][3]);
				//ap3Patches[11].set(aap3Boundary[1][5]);
				//v = Functions.vaverage(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[6],ap3[5]));
				//v.scale(sc);
				//ap3Patches[4].add(aap3Boundary[2][3], v);
				////ap3Patches[4].add(Functions.average(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[6],ap3[5])));
				//ap3Patches[10] = new Point3f(aap3Boundary[1][4]);
				//ap3Patches[5].set(V);
				//ap3Patches[9].set(aap3Boundary[1][3]);
				//v = Functions.vaverage(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2]));
				//v.scale(sc);
				//ap3Patches[8].add(aap3Boundary[1][3], v);
				////ap3Patches[8].add(Functions.average(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2])));
				//ap3Patches[7].set(X);
				//ap3Patches[6].set(Center);
				//normals[0].set(cn[2]);
				//normals[1].set(mn[2]);
				//normals[2].set(centerNormal);
				//normals[3].set(mn[1]);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
			}
			break;
			case 12: {
				///* set up corner normals */
				//Vector3f[] normals = newNormals(4);
				//v3a.sub(ap3[1], ap3[0]);
				//v3b.sub(ap3[11], ap3[0]);
				//normals[0].cross(v3b, v3a);
				//v3a.sub(ap3[4], ap3[3]);
				//v3b.sub(ap3[2], ap3[3]);
				//normals[1].cross(v3b, v3a);
				//v3a.sub(ap3[7], ap3[6]);
				//v3b.sub(ap3[5], ap3[6]);
				//normals[2].cross(v3b, v3a);
				//v3a.sub(ap3[10], ap3[9]);
				//v3b.sub(ap3[8], ap3[9]);
				//normals[3].cross(v3b, v3a);
				//normals[0].normalize();
				//normals[1].normalize();
				//normals[2].normalize();
				//normals[3].normalize();
				colors[0] = lighting.shade(ap3[0], av3[0], mp);
				colors[1] = lighting.shade(ap3[3], av3[1], mp);
				colors[2] = lighting.shade(ap3[6], av3[2], mp);
				colors[3] = lighting.shade(ap3[9], av3[3], mp);
				drawHashPatch4Gaurad(ap3, av3, colors, flat, ail, mp);
				//drawRectHashPatchGourad(ap3, av3, mp, 0);
			}
			break;
			case 15: {
				///* set up corner normals */
				//Vector3f[] cn = newNormals(5);
				//v3a.sub(ap3[1], ap3[0]);
				//v3b.sub(ap3[14], ap3[0]);
				//cn[0].cross(v3b, v3a);
				//v3a.sub(ap3[4], ap3[3]);
				//v3b.sub(ap3[2], ap3[3]);
				//cn[1].cross(v3b, v3a);
				//v3a.sub(ap3[7], ap3[6]);
				//v3b.sub(ap3[5], ap3[6]);
				//cn[2].cross(v3b, v3a);
				//v3a.sub(ap3[10], ap3[9]);
				//v3b.sub(ap3[8], ap3[9]);
				//cn[3].cross(v3b, v3a);
				//v3a.sub(ap3[13], ap3[12]);
				//v3b.sub(ap3[11], ap3[12]);
				//cn[4].cross(v3b, v3a);
				//cn[0].normalize();
				//cn[1].normalize();
				//cn[2].normalize();
				//cn[3].normalize();
				//cn[4].normalize();
					
				/* set up midpoint normals */
				Vector3f[] mn = new Vector3f[5];
				mn[0] = interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]);
				mn[1] = interpolateNormal(av3[1], av3[2], ap3[3], ap3[4], ap3[5], ap3[6]);
				mn[2] = interpolateNormal(av3[2], av3[3], ap3[6], ap3[7], ap3[8], ap3[9]);
				mn[3] = interpolateNormal(av3[3], av3[4], ap3[9], ap3[10], ap3[11], ap3[12]);
				mn[4] = interpolateNormal(av3[4], av3[0], ap3[12], ap3[13], ap3[14], ap3[0]);
				
				/* compute corner-colors */
				int[] ccol = new int[] {
					lighting.shade(ap3[0], av3[0], mp),
					lighting.shade(ap3[3], av3[1], mp),
					lighting.shade(ap3[6], av3[2], mp),
					lighting.shade(ap3[9], av3[3], mp),
					lighting.shade(ap3[12], av3[4], mp)
				};
					
				Point3f A = Functions.parallelogram(ap3[0],ap3[1],ap3[14]);
				Point3f B = Functions.parallelogram(ap3[3],ap3[4],ap3[2]);
				Point3f C = Functions.parallelogram(ap3[6],ap3[7],ap3[5]);
				Point3f D = Functions.parallelogram(ap3[9],ap3[10],ap3[8]);
				Point3f E = Functions.parallelogram(ap3[12],ap3[13],ap3[11]);
				
				Point3f F = Functions.average(A,ap3[0],ap3[1],ap3[14]);
				Point3f H = Functions.average(B,ap3[3],ap3[4],ap3[2]);
				Point3f J = Functions.average(C,ap3[6],ap3[7],ap3[5]);
				Point3f L = Functions.average(D,ap3[9],ap3[10],ap3[8]);
				Point3f N = Functions.average(E,ap3[12],ap3[13],ap3[11]);
				
				Point3f G = Functions.average(A,B,ap3[1],ap3[2]);
				Point3f I = Functions.average(B,C,ap3[4],ap3[5]);
				Point3f K = Functions.average(C,D,ap3[7],ap3[8]);
				Point3f M = Functions.average(D,E,ap3[10],ap3[11]);
				Point3f O = Functions.average(E,A,ap3[13],ap3[14]);
				
				Point3f U = Functions.average(A,B,C,D,E);
				
				Point3f P = Functions.average(U,O,F,G);
				Point3f Q = Functions.average(U,G,H,I);
				Point3f R = Functions.average(U,I,J,K);
				Point3f S = Functions.average(U,K,L,M);
				Point3f T = Functions.average(U,M,N,O);
				
				Point3f V = Functions.average(T,P);
				Point3f W = Functions.average(P,Q);
				Point3f X = Functions.average(Q,R);
				Point3f Y = Functions.average(R,S);
				Point3f Z = Functions.average(S,T);
				
				Point3f Center = Functions.average(P,Q,R,S,T);
				
				Vector3f vc = Functions.vector(V, Center);
				Vector3f wc = Functions.vector(W, Center);
				Vector3f xc = Functions.vector(X, Center);
				Vector3f yc = Functions.vector(Y, Center);
				Vector3f zc = Functions.vector(Z, Center);
				
				Vector3f[] nc = newNormals(5);
				nc[0].cross(wc, vc);
				nc[1].cross(xc, wc);
				nc[2].cross(yc, xc);
				nc[3].cross(zc, yc);
				nc[4].cross(vc, zc);
				nc[0].normalize();
				nc[1].normalize();
				nc[2].normalize();
				nc[3].normalize();
				nc[4].normalize();
				Vector3f centerNormal = Functions.vaverage(nc[0], nc[1], nc[2], nc[3], nc[4]);
				centerNormal.normalize();
				
				Vector3f[] normals = newNormals(4);
				
				Point3f[][] aap3Boundary = new Point3f[5][7];
				
				aap3Boundary[0] = Bezier.deCasteljau(ap3[0],ap3[1],ap3[2],ap3[3],0.5f);
				aap3Boundary[1] = Bezier.deCasteljau(ap3[3],ap3[4],ap3[5],ap3[6],0.5f);
				aap3Boundary[2] = Bezier.deCasteljau(ap3[6],ap3[7],ap3[8],ap3[9],0.5f);
				aap3Boundary[3] = Bezier.deCasteljau(ap3[9],ap3[10],ap3[11],ap3[12],0.5f);
				aap3Boundary[4] = Bezier.deCasteljau(ap3[12],ap3[13],ap3[14],ap3[0],0.5f);
				
				/* compute midpoint colors */
				int[] mcol = new int[] {
					lighting.shade(aap3Boundary[0][3], mn[0], mp),
					lighting.shade(aap3Boundary[1][3], mn[1], mp),
					lighting.shade(aap3Boundary[2][3], mn[2], mp),
					lighting.shade(aap3Boundary[3][3], mn[3], mp),
					lighting.shade(aap3Boundary[4][3], mn[4], mp)
				};
				
				int centerColor = lighting.shade(Center, centerNormal, mp);
				
				Point3f[] ap3Patches = new Point3f[12];
				
				int[] levels = new int[4];
				
				ap3Patches[0] = new Point3f(aap3Boundary[0][0]);
				ap3Patches[1] = new Point3f(aap3Boundary[0][1]);
				ap3Patches[2] = new Point3f(aap3Boundary[0][2]);
				ap3Patches[3] = new Point3f(aap3Boundary[0][3]);
				ap3Patches[11] = new Point3f(aap3Boundary[4][5]);
				ap3Patches[4] = new Point3f(aap3Boundary[0][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[14])));
				ap3Patches[10] = new Point3f(aap3Boundary[4][4]);
				ap3Patches[5] = new Point3f(W);
				ap3Patches[9] = new Point3f(aap3Boundary[4][3]);
				ap3Patches[8] = new Point3f(aap3Boundary[4][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[12],ap3[11])));
				ap3Patches[7] = new Point3f(V);
				ap3Patches[6] = new Point3f(Center);
				normals[0].set(av3[0]);
				normals[1].set(mn[0]);
				normals[2].set(centerNormal);
				normals[3].set(mn[4]);
				colors[0] = ccol[0];
				colors[1] = mcol[0];
				colors[2] = centerColor;
				colors[3] = mcol[4];
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[0] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[4] + 1;
				drawHashPatch4Gaurad(ap3Patches, normals, colors, flat, levels, mp);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				    
				ap3Patches[0].set(aap3Boundary[1][0]);
				ap3Patches[1].set(aap3Boundary[1][1]);
				ap3Patches[2].set(aap3Boundary[1][2]);
				ap3Patches[3].set(aap3Boundary[1][3]);
				ap3Patches[11].set(aap3Boundary[0][5]);
				ap3Patches[4].set(aap3Boundary[1][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2])));
				ap3Patches[10].set(aap3Boundary[0][4]);
				ap3Patches[5].set(X);
				ap3Patches[9].set(aap3Boundary[0][3]);
				ap3Patches[8].set(aap3Boundary[0][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[14])));
				ap3Patches[7].set(W);
				ap3Patches[6].set(Center);
				normals[0].set(av3[1]);
				normals[1].set(mn[1]);
				normals[2].set(centerNormal);
				normals[3].set(mn[0]);
				colors[0] = ccol[1];
				colors[1] = mcol[1];
				colors[2] = centerColor;
				colors[3] = mcol[0];
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[1] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[0] + 1;
				drawHashPatch4Gaurad(ap3Patches, normals, colors, flat, levels, mp);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				
				ap3Patches[0].set(aap3Boundary[2][0]);
				ap3Patches[1].set(aap3Boundary[2][1]);
				ap3Patches[2].set(aap3Boundary[2][2]);
				ap3Patches[3].set(aap3Boundary[2][3]);
				ap3Patches[11].set(aap3Boundary[1][5]);
				ap3Patches[4].set(aap3Boundary[2][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[9],ap3[10]),Functions.vector(ap3[6],ap3[5])));
				ap3Patches[10] = new Point3f(aap3Boundary[1][4]);
				ap3Patches[5].set(Y);
				ap3Patches[9].set(aap3Boundary[1][3]);
				ap3Patches[8].set(aap3Boundary[1][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2])));
				ap3Patches[7].set(X);
				ap3Patches[6].set(Center);
				normals[0].set(av3[2]);
				normals[1].set(mn[2]);
				normals[2].set(centerNormal);
				normals[3].set(mn[1]);
				colors[0] = ccol[2];
				colors[1] = mcol[2];
				colors[2] = centerColor;
				colors[3] = mcol[1];
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[2] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[1] + 1;
				drawHashPatch4Gaurad(ap3Patches, normals, colors, flat, levels, mp);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				
				ap3Patches[0].set(aap3Boundary[3][0]);
				ap3Patches[1].set(aap3Boundary[3][1]);
				ap3Patches[2].set(aap3Boundary[3][2]);
				ap3Patches[3].set(aap3Boundary[3][3]);
				ap3Patches[11].set(aap3Boundary[2][5]);
				ap3Patches[4].set(aap3Boundary[3][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[9],ap3[8]),Functions.vector(ap3[12],ap3[13])));
				ap3Patches[10].set(aap3Boundary[2][4]);
				ap3Patches[5].set(Z);
				ap3Patches[9].set(aap3Boundary[2][3]);
				ap3Patches[8].set(aap3Boundary[2][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[9],ap3[10]),Functions.vector(ap3[6],ap3[5])));
				ap3Patches[7].set(Y);
				ap3Patches[6].set(Center);
				normals[0].set(av3[3]);
				normals[1].set(mn[3]);
				normals[2].set(centerNormal);
				normals[3].set(mn[2]);
				colors[0] = ccol[3];
				colors[1] = mcol[3];
				colors[2] = centerColor;
				colors[3] = mcol[2];
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[3] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[2] + 1;
				drawHashPatch4Gaurad(ap3Patches, normals, colors, flat, levels, mp);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				
				ap3Patches[0].set(aap3Boundary[4][0]);
				ap3Patches[1].set(aap3Boundary[4][1]);
				ap3Patches[2].set(aap3Boundary[4][2]);
				ap3Patches[3].set(aap3Boundary[4][3]);
				ap3Patches[11].set(aap3Boundary[3][5]);
				ap3Patches[4].set(aap3Boundary[4][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[12],ap3[11]),Functions.vector(ap3[0],ap3[1])));
				ap3Patches[10].set(aap3Boundary[3][4]);
				ap3Patches[5].set(V);
				ap3Patches[9].set(aap3Boundary[3][3]);
				ap3Patches[8].set(aap3Boundary[3][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[9],ap3[8]),Functions.vector(ap3[12],ap3[13])));
				ap3Patches[7].set(Z);
				ap3Patches[6].set(Center);
				normals[0].set(av3[4]);
				normals[1].set(mn[4]);
				normals[2].set(centerNormal);
				normals[3].set(mn[3]);
				colors[0] = ccol[4];
				colors[1] = mcol[4];
				colors[2] = centerColor;
				colors[3] = mcol[3];
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[4] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[3] + 1;
				drawHashPatch4Gaurad(ap3Patches, normals, colors, flat, levels, mp);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
			}
			break;
		}
	}
	
	public void drawHashPatchFlat(Point3f[] ap3, int[] ail, MaterialProperties mp) {
		boolean[] flat = new boolean[] { false, false, false, false };
		int color = mp.getRGB();
		int transparency = (int) (Math.min(1f,mp.transmit + mp.filter) * 255f);
		switch (ap3.length) {
			case 9: {
				Point3f[] p = new Point3f[] {ap3[0], ap3[1], ap3[2], ap3[3], ap3[4], ap3[5], ap3[6], ap3[7], ap3[8], new Point3f(ap3[0]), new Point3f(ap3[0]), new Point3f(ap3[0])};
				int[] levels = new int[] { ail[0], ail[1], ail[2], 0 };
				drawHashPatch4Flat(p, flat, levels, color, transparency);
			}
			break;
			case 12: {
				drawHashPatch4Flat(ap3, flat, ail, color, transparency);
			}
			break;
			case 15: {
				Point3f A = Functions.parallelogram(ap3[0],ap3[1],ap3[14]);
				Point3f B = Functions.parallelogram(ap3[3],ap3[4],ap3[2]);
				Point3f C = Functions.parallelogram(ap3[6],ap3[7],ap3[5]);
				Point3f D = Functions.parallelogram(ap3[9],ap3[10],ap3[8]);
				Point3f E = Functions.parallelogram(ap3[12],ap3[13],ap3[11]);
				
				Point3f F = Functions.average(A,ap3[0],ap3[1],ap3[14]);
				Point3f H = Functions.average(B,ap3[3],ap3[4],ap3[2]);
				Point3f J = Functions.average(C,ap3[6],ap3[7],ap3[5]);
				Point3f L = Functions.average(D,ap3[9],ap3[10],ap3[8]);
				Point3f N = Functions.average(E,ap3[12],ap3[13],ap3[11]);
				
				Point3f G = Functions.average(A,B,ap3[1],ap3[2]);
				Point3f I = Functions.average(B,C,ap3[4],ap3[5]);
				Point3f K = Functions.average(C,D,ap3[7],ap3[8]);
				Point3f M = Functions.average(D,E,ap3[10],ap3[11]);
				Point3f O = Functions.average(E,A,ap3[13],ap3[14]);
				
				Point3f U = Functions.average(A,B,C,D,E);
				
				Point3f P = Functions.average(U,O,F,G);
				Point3f Q = Functions.average(U,G,H,I);
				Point3f R = Functions.average(U,I,J,K);
				Point3f S = Functions.average(U,K,L,M);
				Point3f T = Functions.average(U,M,N,O);
				
				Point3f V = Functions.average(T,P);
				Point3f W = Functions.average(P,Q);
				Point3f X = Functions.average(Q,R);
				Point3f Y = Functions.average(R,S);
				Point3f Z = Functions.average(S,T);
				
				Point3f Center = Functions.average(P,Q,R,S,T);
				
//				Vector3f vc = Functions.vector(V, Center);
//				Vector3f wc = Functions.vector(W, Center);
//				Vector3f xc = Functions.vector(X, Center);
//				Vector3f yc = Functions.vector(Y, Center);
//				Vector3f zc = Functions.vector(Z, Center);
				
				Point3f[][] aap3Boundary = new Point3f[5][7];
				
				aap3Boundary[0] = Bezier.deCasteljau(ap3[0],ap3[1],ap3[2],ap3[3],0.5f);
				aap3Boundary[1] = Bezier.deCasteljau(ap3[3],ap3[4],ap3[5],ap3[6],0.5f);
				aap3Boundary[2] = Bezier.deCasteljau(ap3[6],ap3[7],ap3[8],ap3[9],0.5f);
				aap3Boundary[3] = Bezier.deCasteljau(ap3[9],ap3[10],ap3[11],ap3[12],0.5f);
				aap3Boundary[4] = Bezier.deCasteljau(ap3[12],ap3[13],ap3[14],ap3[0],0.5f);
				
				Point3f[] ap3Patches = new Point3f[12];
				
				int[] levels = new int[4];
				
				ap3Patches[0] = new Point3f(aap3Boundary[0][0]);
				ap3Patches[1] = new Point3f(aap3Boundary[0][1]);
				ap3Patches[2] = new Point3f(aap3Boundary[0][2]);
				ap3Patches[3] = new Point3f(aap3Boundary[0][3]);
				ap3Patches[11] = new Point3f(aap3Boundary[4][5]);
				ap3Patches[4] = new Point3f(aap3Boundary[0][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[14])));
				ap3Patches[10] = new Point3f(aap3Boundary[4][4]);
				ap3Patches[5] = new Point3f(W);
				ap3Patches[9] = new Point3f(aap3Boundary[4][3]);
				ap3Patches[8] = new Point3f(aap3Boundary[4][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[12],ap3[11])));
				ap3Patches[7] = new Point3f(V);
				ap3Patches[6] = new Point3f(Center);
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[0] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[4] + 1;
				drawHashPatch4Flat(ap3Patches, flat, levels, color, transparency);
				    
				ap3Patches[0].set(aap3Boundary[1][0]);
				ap3Patches[1].set(aap3Boundary[1][1]);
				ap3Patches[2].set(aap3Boundary[1][2]);
				ap3Patches[3].set(aap3Boundary[1][3]);
				ap3Patches[11].set(aap3Boundary[0][5]);
				ap3Patches[4].set(aap3Boundary[1][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2])));
				ap3Patches[10].set(aap3Boundary[0][4]);
				ap3Patches[5].set(X);
				ap3Patches[9].set(aap3Boundary[0][3]);
				ap3Patches[8].set(aap3Boundary[0][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[14])));
				ap3Patches[7].set(W);
				ap3Patches[6].set(Center);
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[1] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[0] + 1;
				drawHashPatch4Flat(ap3Patches, flat, levels, color, transparency);
				
				ap3Patches[0].set(aap3Boundary[2][0]);
				ap3Patches[1].set(aap3Boundary[2][1]);
				ap3Patches[2].set(aap3Boundary[2][2]);
				ap3Patches[3].set(aap3Boundary[2][3]);
				ap3Patches[11].set(aap3Boundary[1][5]);
				ap3Patches[4].set(aap3Boundary[2][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[9],ap3[10]),Functions.vector(ap3[6],ap3[5])));
				ap3Patches[10] = new Point3f(aap3Boundary[1][4]);
				ap3Patches[5].set(Y);
				ap3Patches[9].set(aap3Boundary[1][3]);
				ap3Patches[8].set(aap3Boundary[1][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2])));
				ap3Patches[7].set(X);
				ap3Patches[6].set(Center);
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[2] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[1] + 1;
				drawHashPatch4Flat(ap3Patches, flat, levels, color, transparency);
				
				ap3Patches[0].set(aap3Boundary[3][0]);
				ap3Patches[1].set(aap3Boundary[3][1]);
				ap3Patches[2].set(aap3Boundary[3][2]);
				ap3Patches[3].set(aap3Boundary[3][3]);
				ap3Patches[11].set(aap3Boundary[2][5]);
				ap3Patches[4].set(aap3Boundary[3][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[9],ap3[8]),Functions.vector(ap3[12],ap3[13])));
				ap3Patches[10].set(aap3Boundary[2][4]);
				ap3Patches[5].set(Z);
				ap3Patches[9].set(aap3Boundary[2][3]);
				ap3Patches[8].set(aap3Boundary[2][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[9],ap3[10]),Functions.vector(ap3[6],ap3[5])));
				ap3Patches[7].set(Y);
				ap3Patches[6].set(Center);
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[3] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[2] + 1;
				drawHashPatch4Flat(ap3Patches, flat, levels, color, transparency);
				
				ap3Patches[0].set(aap3Boundary[4][0]);
				ap3Patches[1].set(aap3Boundary[4][1]);
				ap3Patches[2].set(aap3Boundary[4][2]);
				ap3Patches[3].set(aap3Boundary[4][3]);
				ap3Patches[11].set(aap3Boundary[3][5]);
				ap3Patches[4].set(aap3Boundary[4][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[12],ap3[11]),Functions.vector(ap3[0],ap3[1])));
				ap3Patches[10].set(aap3Boundary[3][4]);
				ap3Patches[5].set(V);
				ap3Patches[9].set(aap3Boundary[3][3]);
				ap3Patches[8].set(aap3Boundary[3][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[9],ap3[8]),Functions.vector(ap3[12],ap3[13])));
				ap3Patches[7].set(Z);
				ap3Patches[6].set(Center);
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[4] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[3] + 1;
				drawHashPatch4Flat(ap3Patches, flat, levels, color, transparency);
			}
			break;
		}
	}
	
	public void addQuad(Point3d dp0, Point3d dp1, Point3d dp2, Point3d dp3, Point3d dr0, Point3d dr1, Point3d dr2, Point3d dr3, Vector3f n0, Vector3f n1, Vector3f n2, Vector3f n3) {
		Point3f p0 = new Point3f(dp0);
		Point3f p1 = new Point3f(dp1);
		Point3f p2 = new Point3f(dp2);
		Point3f p3 = new Point3f(dp3);
		int c0 = lighting.shade(p0, n0, materialProperties);
		int c1 = lighting.shade(p1, n1, materialProperties);
		int c2 = lighting.shade(p2, n2, materialProperties);
		int c3 = lighting.shade(p3, n3, materialProperties);
		//drawLine3D(p0, p1);
		//drawLine3D(p1, p2);
		//drawLine3D(p2, p0);
		draw3DTriangleGourad(p0, p1, p2, c0, c1, c2);
		draw3DTriangleGourad(p2, p3, p0, c2, c3, c0);
	}
}
