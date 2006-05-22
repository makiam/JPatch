package jpatch.renderer;

import javax.vecmath.*;
import jpatch.auxilary.*;

public class HashPatchSubdivision {
	
	private Vector3f v3a = new Vector3f();
	private Vector3f v3c = new Vector3f();
	
	private int iMaxSubdiv;
	private QuadDrain quadDrain;
	
	public interface QuadDrain {
		public void addQuad(Point3d p0, Point3d p1, Point3d p2, Point3d p3, Point3d r0, Point3d r1, Point3d r2, Point3d r3, Vector3f n0, Vector3f n1, Vector3f n2, Vector3f n3);
	}
	
	private HashPatchSubdivision() {
	}
	
	public HashPatchSubdivision(float flatness, int maxSubdiv, QuadDrain quadDrain) {
		iMaxSubdiv = maxSubdiv;
		this.quadDrain = quadDrain;
	}
	
	//private boolean makeFlat(Point3d p0, Point3d p1, Point3d p2, Point3d p3) {
	//	v3a.sub(p1, p0);
	//	v3b.sub(p2, p3);
	//	v3c.sub(p3, p0);
	//	if (v3c.x != 0 || v3c.y != 0 || v3c.z != 0) v3c.normalize();
	//	p1.set(v3c);
	//	p1.scale(v3a.length());
	//	p1.add(p0);
	//	p2.set(v3c);
	//	p2.scale(-v3b.length());
	//	p2.add(p3);
	//	return true;
	//}
	//
	//private float subdiv(Point3d p0, Point3d p1, Point3d p2, Point3d p3, boolean simple) {
	//	if (!simple) {
	//		v3a.set(4 * p0.x - 6 *  p1.x + 2 * p3.x, 4 * p0.y - 6 *  p1.y + 2 * p3.y, 4 * p0.z - 6 *  p1.z + 2 * p3.z);
	//		v3b.set(2 * p0.x - 6 *  p1.x + 4 * p3.x, 2 * p0.y - 6 *  p1.y + 4 * p3.y, 2 * p0.z - 6 *  p1.z + 4 * p3.z);
	//		return v3a.length() + v3b.length();
	//	} else {
	//		v3a.set(p0.x - p1.x - p2.x + p3.x, p0.y - p1.y - p2.y + p3.y, p0.z - p1.z - p2.z + p3.z);
	//		return v3a.length();
	//	}
	//}
	
	public Vector3f interpolateNormal(Vector3f n0, Vector3f n1) {
		Vector3f n = new Vector3f(n0.x + n1.x, n0.y + n1.y, n0.z + n1.z);
		n.normalize();
		return n;
		//return new Vector3f(0.5f * (n0.x + n1.x), 0.5f * (n0.y + n1.y), 0.5f * (n0.z + n1.z));
	}
	
	//public Vector3f interpolateNormal(Vector3f n0, Vector3f n1, Point3d p0, Point3d p1, Point3d p2, Point3d p3) {
	//	v3a.set(p0.x + p1.x - p2.x - p3.x, p0.y + p1.y - p2.y - p3.y, p0.z + p1.z - p2.z - p3.z);
	//	Vector3f n = new Vector3f(n0.x + n1.x, n0.y + n1.y, n0.z + n1.z);
	//	if (v3a.x != 0 || v3a.y != 0 || v3a.z != 0) {
	//		v3a.normalize();
	//		n.cross(n, v3a);
	//		n.cross(n, v3a);
	//		n.scale(-1f / n.length());
	//	}
	//	else n.normalize();
	//	return n;
	//}
	
	private Point3d[] newPatch(int n) {
		Point3d[] p = new Point3d[n];
		for (int i = 0; i < n; p[i++] = new Point3d());
		return p;
	}
	
	private Vector3f[] newNormals(int n) {
		Vector3f[] v = new Vector3f[n];
		for (int i = 0; i < n; v[i++] = new Vector3f());
		return v;
	}
	
	private void deCasteljauSplit(Point3d p0, Point3d p1, Point3d p2, Point3d p3, Point3d pn0, Point3d pn1, Point3d pn2, Point3d pn3) {
		pn0.set((p1.x + p2.x) * 0.5, (p1.y + p2.y) * 0.5, (p1.z + p2.z) * 0.5);
		pn3.set(p3);
		pn2.set((p2.x + p3.x) * 0.5, (p2.y + p3.y) * 0.5, (p2.z + p3.z) * 0.5);
		pn1.set((pn2.x + pn0.x) * 0.5, (pn2.y + pn0.y) * 0.5, (pn2.z + pn0.z) * 0.5);
		p1.set((p0.x + p1.x) * 0.5, (p0.y + p1.y) * 0.5, (p0.z + p1.z) * 0.5);
		p2.set((p1.x + pn0.x) * 0.5, (p1.y + pn0.y) * 0.5, (p1.z + pn0.z) * 0.5);
		p3.set((p2.x + pn1.x) * 0.5, (p2.y + pn1.y) * 0.5, (p2.z + pn1.z) * 0.5);
		pn0.set(p3);
	}
	
	//private void subdivHashPatch4(Point3d[] ap3, Vector3f[] av3, boolean[] abFlat, int[] aiLevel) {
	//	/* check if we need to u-split */
	//	float u0 = subdiv(ap3[0], ap3[1], ap3[2], ap3[3], aiLevel[0] > 0);
	//	float u2 = subdiv(ap3[9], ap3[8], ap3[7], ap3[6], aiLevel[2] > 0);
	//	float v3 = subdiv(ap3[0], ap3[11], ap3[10], ap3[9], aiLevel[3] > 0);
	//	float v1 = subdiv(ap3[3], ap3[4], ap3[5], ap3[6], aiLevel[1] > 0);
	//	
	//	float ul = (aiLevel[0] >= iMaxSubdiv || aiLevel[2] >= iMaxSubdiv) ? -1 : (float) Math.max(u0, u2);
	//	float uv = (aiLevel[1] >= iMaxSubdiv || aiLevel[3] >= iMaxSubdiv) ? -1 : (float) Math.max(v1, v3);
	//	
	//	if (true && ul >= fFlatness && ul > uv * 1.1f) {
	//		
	//		/* flatten cubics if flat enough */
	//		if (!abFlat[0] && (u0 <= fFlatness)) abFlat[0] = makeFlat(ap3[0], ap3[1], ap3[2], ap3[3]);
	//		if (!abFlat[1] && (v1 <= fFlatness)) abFlat[1] = makeFlat(ap3[3], ap3[4], ap3[5], ap3[6]);
	//		if (!abFlat[2] && (u2 <= fFlatness)) abFlat[2] = makeFlat(ap3[9], ap3[8], ap3[7], ap3[6]);
	//		if (!abFlat[3] && (v3 <= fFlatness)) abFlat[3] = makeFlat(ap3[0], ap3[11], ap3[10], ap3[9]);
	//		
	//		/* compute new normals */
	//		Vector3f[] av3new = newNormals(4);
	//		av3new[1].set(av3[1]);
	//		av3new[0] = av3[1] = (aiLevel[0] > 0) ? interpolateNormal(av3[0], av3[1]) : interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]);
	//		av3new[2].set(av3[2]);
	//		av3new[3] = av3[2] = (aiLevel[2] > 0) ? interpolateNormal(av3[3], av3[2]) : interpolateNormal(av3[3], av3[2], ap3[9], ap3[8], ap3[7], ap3[6]);
	//		
	//		/* compute new patches */
	//		Point3d[] ap3new = newPatch(12);
	//		v3a.sub(ap3[11], ap3[0]);
	//		v3b.sub(ap3[4], ap3[3]);
	//		v3c.add(v3a, v3b);
	//		v3c.scale(0.5f);
	//		v3a.sub(ap3[10], ap3[9]);
	//		v3b.sub(ap3[5], ap3[6]);
	//		v3a.add(v3b);
	//		v3a.scale(0.5f);
	//		deCasteljauSplit(ap3[0], ap3[1], ap3[2], ap3[3], ap3new[0], ap3new[1], ap3new[2], ap3new[3]);
	//		deCasteljauSplit(ap3[9], ap3[8], ap3[7], ap3[6], ap3new[9], ap3new[8], ap3new[7], ap3new[6]);
	//		ap3new[4].set(ap3[4]);
	//		ap3new[5].set(ap3[5]);
	//		ap3[4].add(ap3[3], v3c);
	//		ap3[5].add(ap3[6], v3a);
	//		ap3new[11].set(ap3[4]);
	//		ap3new[10].set(ap3[5]);
	//		
	//		/* set up new flatenough flags */
	//		boolean[] abnew = new boolean[4];
	//		abnew[0] = abFlat[0];
	//		abnew[1] = abFlat[1];
	//		abnew[2] = abFlat[2];
	//		abnew[3] = abFlat[1] = false;
	//		
	//		/* recurse */
	//		int l = (aiLevel[1] < aiLevel[3]) ? aiLevel[1] : aiLevel[3];
	//		aiLevel[0]++;
	//		aiLevel[2]++;
	//		int[] newLevels = new int[aiLevel.length];
	//		for (int i = 0; i < aiLevel.length; newLevels[i] = aiLevel[i++]);
	//		aiLevel[1] = newLevels[3] = l;
	//		subdivHashPatch4(ap3, av3, abFlat, aiLevel);
	//		subdivHashPatch4(ap3new, av3new, abnew, newLevels);
	//		return;
	//	}
	//	
	//	/* check if we need to v-split */
	//	else if (true && uv >= fFlatness) {
	//	
	//		/* flatten cubics if flat enough */
	//		if (!abFlat[0] && (u0 <= fFlatness)) abFlat[0] = makeFlat(ap3[0], ap3[1], ap3[2], ap3[3]);
	//		if (!abFlat[1] && (v1 <= fFlatness)) abFlat[1] = makeFlat(ap3[3], ap3[4], ap3[5], ap3[6]);
	//		if (!abFlat[2] && (u2 <= fFlatness)) abFlat[2] = makeFlat(ap3[9], ap3[8], ap3[7], ap3[6]);
	//		if (!abFlat[3] && (v3 <= fFlatness)) abFlat[3] = makeFlat(ap3[0], ap3[11], ap3[10], ap3[9]);
	//		
	//		/* compute new normals */
	//		Vector3f[] av3new = newNormals(4);
	//		av3new[3].set(av3[3]);
	//		av3new[0] = av3[3] = (aiLevel[3] > 0) ? interpolateNormal(av3[0], av3[3]) : interpolateNormal(av3[0], av3[3], ap3[0], ap3[11], ap3[10], ap3[9]);
	//		av3new[2].set(av3[2]);
	//		av3new[1] = av3[2] = (aiLevel[1] > 0) ? interpolateNormal(av3[1], av3[2]) : interpolateNormal(av3[1], av3[2], ap3[3], ap3[4], ap3[5], ap3[6]);
	//		
	//		/* compute new patches */
	//		Point3d[] ap3new = newPatch(12);
	//		v3a.sub(ap3[1], ap3[0]);
	//		v3b.sub(ap3[8], ap3[9]);
	//		v3c.add(v3a, v3b);
	//		v3c.scale(0.5f);
	//		v3a.sub(ap3[2], ap3[3]);
	//		v3b.sub(ap3[7], ap3[6]);
	//		v3a.add(v3b);
	//		v3a.scale(0.5f);
	//		deCasteljauSplit(ap3[0], ap3[11], ap3[10], ap3[9], ap3new[0], ap3new[11], ap3new[10], ap3new[9]);
	//		deCasteljauSplit(ap3[3], ap3[4], ap3[5], ap3[6], ap3new[3], ap3new[4], ap3new[5], ap3new[6]);
	//		ap3new[8].set(ap3[8]);
	//		ap3new[7].set(ap3[7]);
	//		ap3[8].add(ap3[9], v3c);
	//		ap3[7].add(ap3[6], v3a);
	//		ap3new[1].set(ap3[8]);
	//		ap3new[2].set(ap3[7]);
	//		
	//		/* set up new flatenough flags */
	//		boolean[] abnew = new boolean[4];
	//		abnew[1] = abFlat[1];
	//		abnew[3] = abFlat[3];
	//		abnew[2] = abFlat[2];
	//		abnew[0] = abFlat[2] = false;
	//		
	//		/* recurse */
	//		int l = (aiLevel[0] < aiLevel[2]) ? aiLevel[0] : aiLevel[2];
	//		aiLevel[1]++;
	//		aiLevel[3]++;
	//		int[] newLevels = new int[aiLevel.length];
	//		for (int i = 0; i < aiLevel.length; newLevels[i] = aiLevel[i++]);
	//		aiLevel[2] = newLevels[0] = l;
	//		subdivHashPatch4(ap3, av3, abFlat, aiLevel);
	//		subdivHashPatch4(ap3new, av3new, abnew, newLevels);
	//		return;
	//	}
	//	/* draw the patch */
	//	
	//	addTriangle(ap3[9], ap3[3], ap3[0], av3[3], av3[1], av3[0]);
	//	addTriangle(ap3[3], ap3[9], ap3[6], av3[1], av3[3], av3[2]);
	//}
	
	
	public void subdivHashPatch4(Point3d[] ap3, Point3d[] ap3ref, Vector3f[] av3, int level, Point3d[][] aap3HookCurve, Point3d[][] aap3RefHookCurve, Vector3f[][] aav3HookNormals) {
		//System.out.println(ap3[0].x + "/" + ap3[0].y + " " + ap3[3].x + "/" + ap3[3].y + " " + ap3[6].x + "/" + ap3[6].y + " " + ap3[9].x + "/" + ap3[9].y);
		//System.out.println("level = " + level);
		//for (int i = 0; i < 4; i++) {
		//	System.out.print("Hookcurve: ");
		//	if (aap3HookCurve[i] != null) {
		//		for (int p = 0; p < aap3HookCurve[i].length; p++) {
		//			System.out.print(aap3HookCurve[i][p].x + " ");
		//		}
		//	}
		//	System.out.println();
		//}
		//System.out.println();
		/* check if we need to split */
		//float us0 = subdiv(ap3[0], ap3[1], ap3[2], ap3[3], aiLevel[0] > 0);
		//float us2 = subdiv(ap3[9], ap3[8], ap3[7], ap3[6], aiLevel[2] > 0);
		//float vs3 = subdiv(ap3[0], ap3[11], ap3[10], ap3[9], aiLevel[3] > 0);
		//float vs1 = subdiv(ap3[3], ap3[4], ap3[5], ap3[6], aiLevel[1] > 0);
		//if ((aiLevel[0] < iMaxSubdiv && aiLevel[1] < iMaxSubdiv && aiLevel[2] < iMaxSubdiv && aiLevel[3] < iMaxSubdiv) && (us0 > fFlatness || us2 > fFlatness || vs1 > fFlatness || vs3 > fFlatness)) {
		if (level < iMaxSubdiv) {
			///* flatten cubics if flat enough */
			//if (!abFlat[0] && aiLevel[0] >= iMaxSubdiv) {
			//	abFlat[0] = true;
			//	makeFlat(ap3[0], ap3[1], ap3[2], ap3[3]);
			//	makeFlat(ap3ref[0], ap3ref[1], ap3ref[2], ap3ref[3]);
			//}
			//if (!abFlat[1] && aiLevel[1] >= iMaxSubdiv) {
			//	abFlat[1] = true;
			//	makeFlat(ap3[3], ap3[4], ap3[5], ap3[6]);
			//	makeFlat(ap3ref[3], ap3ref[4], ap3ref[5], ap3ref[6]);
			//}
			//if (!abFlat[2] && aiLevel[2] >= iMaxSubdiv) {
			//	abFlat[2] = true;
			//	makeFlat(ap3[6], ap3[7], ap3[8], ap3[9]);
			//	makeFlat(ap3ref[6], ap3ref[7], ap3ref[8], ap3ref[9]);
			//}
			//if (!abFlat[3] && aiLevel[3] >= iMaxSubdiv) {
			//	abFlat[3] = true;
			//	makeFlat(ap3[9], ap3[10], ap3[11], ap3[0]);
			//	makeFlat(ap3ref[9], ap3ref[10], ap3ref[11], ap3ref[0]);
			//}
			
			/* prepare new patches */
			Point3d[] ap3a = newPatch(12);
			Point3d[] ap3b = newPatch(12);
			Point3d[] ap3c = newPatch(12);
			Point3d[] ap3d = newPatch(12);
			Point3d[] ap3refa = newPatch(12);
			Point3d[] ap3refb = newPatch(12);
			Point3d[] ap3refc = newPatch(12);
			Point3d[] ap3refd = newPatch(12);
			
			/* deCasteljau outer boundaries */
			ap3a[0].set(ap3[0]);
			ap3a[1].set(ap3[1]);
			ap3a[2].set(ap3[2]);
			ap3a[3].set(ap3[3]);
			ap3refa[0].set(ap3ref[0]);
			ap3refa[1].set(ap3ref[1]);
			ap3refa[2].set(ap3ref[2]);
			ap3refa[3].set(ap3ref[3]);
			deCasteljauSplit(ap3a[0], ap3a[1], ap3a[2], ap3a[3], ap3b[0], ap3b[1], ap3b[2], ap3b[3]);
			deCasteljauSplit(ap3refa[0], ap3refa[1], ap3refa[2], ap3refa[3], ap3refb[0], ap3refb[1], ap3refb[2], ap3refb[3]);
			
			ap3b[4].set(ap3[4]);
			ap3b[5].set(ap3[5]);
			ap3b[6].set(ap3[6]);
			ap3refb[4].set(ap3ref[4]);
			ap3refb[5].set(ap3ref[5]);
			ap3refb[6].set(ap3ref[6]);
			deCasteljauSplit(ap3b[3], ap3b[4], ap3b[5], ap3b[6], ap3c[3], ap3c[4], ap3c[5], ap3c[6]);
			deCasteljauSplit(ap3refb[3], ap3refb[4], ap3refb[5], ap3refb[6], ap3refc[3], ap3refc[4], ap3refc[5], ap3refc[6]);
			
			ap3c[7].set(ap3[7]);
			ap3c[8].set(ap3[8]);
			ap3c[9].set(ap3[9]);
			ap3refc[7].set(ap3ref[7]);
			ap3refc[8].set(ap3ref[8]);
			ap3refc[9].set(ap3ref[9]);
			deCasteljauSplit(ap3c[6], ap3c[7], ap3c[8], ap3c[9], ap3d[6], ap3d[7], ap3d[8], ap3d[9]);
			deCasteljauSplit(ap3refc[6], ap3refc[7], ap3refc[8], ap3refc[9], ap3refd[6], ap3refd[7], ap3refd[8], ap3refd[9]);
			
			ap3d[10].set(ap3[10]);
			ap3d[11].set(ap3[11]);
			ap3d[0].set(ap3[0]);
			ap3refd[10].set(ap3ref[10]);
			ap3refd[11].set(ap3ref[11]);
			ap3refd[0].set(ap3ref[0]);
			deCasteljauSplit(ap3d[9], ap3d[10], ap3d[11], ap3d[0], ap3a[9], ap3a[10], ap3a[11], ap3a[0]);
			deCasteljauSplit(ap3refd[9], ap3refd[10], ap3refd[11], ap3refd[0], ap3refa[9], ap3refa[10], ap3refa[11], ap3refa[0]);
			
			/* setup average tangents */
			Point3d p3A = new Point3d(
				0.5f * (ap3[4].x - ap3[3].x + ap3[11].x - ap3[0].x) + ap3b[0].x,
				0.5f * (ap3[4].y - ap3[3].y + ap3[11].y - ap3[0].y) + ap3b[0].y,
				0.5f * (ap3[4].z - ap3[3].z + ap3[11].z - ap3[0].z) + ap3b[0].z
			);
			Point3d p3B = new Point3d(
				0.5f * (ap3[2].x - ap3[3].x + ap3[7].x - ap3[6].x) + ap3c[3].x,
				0.5f * (ap3[2].y - ap3[3].y + ap3[7].y - ap3[6].y) + ap3c[3].y,
				0.5f * (ap3[2].z - ap3[3].z + ap3[7].z - ap3[6].z) + ap3c[3].z
			);
			Point3d p3C = new Point3d(
				0.5f * (ap3[5].x - ap3[6].x + ap3[10].x - ap3[9].x) + ap3d[6].x,
				0.5f * (ap3[5].y - ap3[6].y + ap3[10].y - ap3[9].y) + ap3d[6].y,
				0.5f * (ap3[5].z - ap3[6].z + ap3[10].z - ap3[9].z) + ap3d[6].z
			);
			Point3d p3D = new Point3d(
				0.5f * (ap3[8].x - ap3[9].x + ap3[1].x - ap3[0].x) + ap3a[9].x,
				0.5f * (ap3[8].y - ap3[9].y + ap3[1].y - ap3[0].y) + ap3a[9].y,
				0.5f * (ap3[8].z - ap3[9].z + ap3[1].z - ap3[0].z) + ap3a[9].z
			);
			Point3d p3refA = new Point3d(
				0.5f * (ap3ref[4].x - ap3ref[3].x + ap3ref[11].x - ap3ref[0].x) + ap3refb[0].x,
				0.5f * (ap3ref[4].y - ap3ref[3].y + ap3ref[11].y - ap3ref[0].y) + ap3refb[0].y,
				0.5f * (ap3ref[4].z - ap3ref[3].z + ap3ref[11].z - ap3ref[0].z) + ap3refb[0].z
			);
			Point3d p3refB = new Point3d(
				0.5f * (ap3ref[2].x - ap3ref[3].x + ap3ref[7].x - ap3ref[6].x) + ap3refc[3].x,
				0.5f * (ap3ref[2].y - ap3ref[3].y + ap3ref[7].y - ap3ref[6].y) + ap3refc[3].y,
				0.5f * (ap3ref[2].z - ap3ref[3].z + ap3ref[7].z - ap3ref[6].z) + ap3refc[3].z
			);
			Point3d p3refC = new Point3d(
				0.5f * (ap3ref[5].x - ap3ref[6].x + ap3ref[10].x - ap3ref[9].x) + ap3refd[6].x,
				0.5f * (ap3ref[5].y - ap3ref[6].y + ap3ref[10].y - ap3ref[9].y) + ap3refd[6].y,
				0.5f * (ap3ref[5].z - ap3ref[6].z + ap3ref[10].z - ap3ref[9].z) + ap3refd[6].z
			);
			Point3d p3refD = new Point3d(
				0.5f * (ap3ref[8].x - ap3ref[9].x + ap3ref[1].x - ap3ref[0].x) + ap3refa[9].x,
				0.5f * (ap3ref[8].y - ap3ref[9].y + ap3ref[1].y - ap3ref[0].y) + ap3refa[9].y,
				0.5f * (ap3ref[8].z - ap3ref[9].z + ap3ref[1].z - ap3ref[0].z) + ap3refa[9].z
			);
			
			/* deCasteljau split inner curves */
			ap3a[8].set(p3D);
			ap3a[7].set(p3B);
			ap3a[6].set(ap3b[6]);
			ap3refa[8].set(p3refD);
			ap3refa[7].set(p3refB);
			ap3refa[6].set(ap3refb[6]);
			deCasteljauSplit(ap3a[9], ap3a[8], ap3a[7], ap3a[6], ap3b[9], ap3b[8], ap3b[7], ap3b[6]);
			deCasteljauSplit(ap3refa[9], ap3refa[8], ap3refa[7], ap3refa[6], ap3refb[9], ap3refb[8], ap3refb[7], ap3refb[6]);
			
			ap3a[4].set(p3A);
			ap3a[5].set(p3C);
			ap3a[6].set(ap3d[6]);
			ap3refa[4].set(p3refA);
			ap3refa[5].set(p3refC);
			ap3refa[6].set(ap3refd[6]);
			deCasteljauSplit(ap3a[3], ap3a[4], ap3a[5], ap3a[6], ap3d[3], ap3d[4], ap3d[5], ap3d[6]);
			deCasteljauSplit(ap3refa[3], ap3refa[4], ap3refa[5], ap3refa[6], ap3refd[3], ap3refd[4], ap3refd[5], ap3refd[6]);
			
			/* average center */
			ap3c[0].set(
				0.5f * (ap3b[9].x + ap3d[3].x),
				0.5f * (ap3b[9].y + ap3d[3].y),
				0.5f * (ap3b[9].z + ap3d[3].z)
			);
			ap3refc[0].set(
				0.5f * (ap3refb[9].x + ap3refd[3].x),
				0.5f * (ap3refb[9].y + ap3refd[3].y),
				0.5f * (ap3refb[9].z + ap3refd[3].z)
			);
			
			/* correct center tangents */
			Vector3d v3corr = new Vector3d(
				ap3a[6].x - ap3c[0].x,
				ap3a[6].y - ap3c[0].y,
				ap3a[6].z - ap3c[0].z
			);
			Vector3d v3refcorr = new Vector3d(
				ap3refa[6].x - ap3refc[0].x,
				ap3refa[6].y - ap3refc[0].y,
				ap3refa[6].z - ap3refc[0].z
			);
			ap3a[7].sub(v3corr);
			ap3b[8].sub(v3corr);
			ap3a[5].add(v3corr);
			ap3d[4].add(v3corr);
			ap3refa[7].sub(v3refcorr);
			ap3refb[8].sub(v3refcorr);
			ap3refa[5].add(v3refcorr);
			ap3refd[4].add(v3refcorr);
			
			/* set new center */
			ap3a[6].set(ap3c[0]);
			ap3b[9].set(ap3c[0]);
			ap3d[3].set(ap3c[0]);
			ap3refa[6].set(ap3refc[0]);
			ap3refb[9].set(ap3refc[0]);
			ap3refd[3].set(ap3refc[0]);
			
			/* set missing inner curves */
			ap3b[11].set(ap3a[4]);
			ap3b[10].set(ap3a[5]);
			ap3c[11].set(ap3d[4]);
			ap3c[10].set(ap3d[5]);
			ap3d[1].set(ap3a[8]);
			ap3d[2].set(ap3a[7]);
			ap3c[1].set(ap3b[8]);
			ap3c[2].set(ap3b[7]);
			ap3refb[11].set(ap3refa[4]);
			ap3refb[10].set(ap3refa[5]);
			ap3refc[11].set(ap3refd[4]);
			ap3refc[10].set(ap3refd[5]);
			ap3refd[1].set(ap3refa[8]);
			ap3refd[2].set(ap3refa[7]);
			ap3refc[1].set(ap3refb[8]);
			ap3refc[2].set(ap3refb[7]);
			
			/* prepare new normals */
			Vector3f[] av3a = newNormals(4);
			Vector3f[] av3b = newNormals(4);
			Vector3f[] av3c = newNormals(4);
			Vector3f[] av3d = newNormals(4);
			
			/* set corner normals */
			av3a[0].set(av3[0]);
			av3b[1].set(av3[1]);
			av3c[2].set(av3[2]);
			av3d[3].set(av3[3]);
			
			/* compute missing normals */
			v3a = interpolateNormal(av3[0], av3[1]);
			av3a[1].set(v3a);
			av3b[0].set(v3a);
			v3a = interpolateNormal(av3[1], av3[2]);
			av3b[2].set(v3a);
			av3c[1].set(v3a);
			v3a = interpolateNormal(av3[2], av3[3]);
			av3c[3].set(v3a);
			av3d[2].set(v3a);
			v3a = interpolateNormal(av3[3], av3[0]);
			av3d[0].set(v3a);
			av3a[3].set(v3a);
			
			/* compute and average center normal */
			Vector3f A = interpolateNormal(av3a[1], av3c[3]);
			Vector3f B = interpolateNormal(av3a[3], av3c[1]);
			v3c = interpolateNormal(A, B);
			av3a[2].set(v3c);
			av3b[3].set(v3c);
			av3c[0].set(v3c);
			av3d[1].set(v3c);
			
			///* set up new flatenough flags */
			//boolean[] aba = new boolean[4];
			//boolean[] abb = new boolean[4];
			//boolean[] abc = new boolean[4];
			//boolean[] abd = new boolean[4];
			//
			//aba[0] = abb[0] = abFlat[0];
			//abb[1] = abc[1] = abFlat[1];
			//abc[2] = abd[2] = abFlat[2];
			//abd[3] = aba[3] = abFlat[3];
			//aba[1] = aba[2] = abb[2] = abb[3] = abc[3] = abc[0] = abd[0] = abd[1] = false;
			
			///* recurse */
			//int lu = (aiLevel[0] < aiLevel[2]) ? aiLevel[0] + 1 : aiLevel[2] + 1;
			//int lv = (aiLevel[1] < aiLevel[3]) ? aiLevel[1] + 1 : aiLevel[3] + 1;
			
			level++;
			
			subdivHashPatch4(ap3a, ap3refa, av3a, level, new Point3d[][]{ aap3HookCurve[0], null, null, aap3HookCurve[3] }, new Point3d[][]{ aap3RefHookCurve[0], null, null, aap3RefHookCurve[3] }, new Vector3f[][]{ aav3HookNormals[0], null, null, aav3HookNormals[3] });
			subdivHashPatch4(ap3b, ap3refb, av3b, level, new Point3d[][]{ aap3HookCurve[0], aap3HookCurve[1], null, null }, new Point3d[][]{ aap3RefHookCurve[0], aap3RefHookCurve[1], null, null }, new Vector3f[][]{ aav3HookNormals[0], aav3HookNormals[1], null, null });
			subdivHashPatch4(ap3c, ap3refc, av3c, level, new Point3d[][]{ null, aap3HookCurve[1], aap3HookCurve[2], null }, new Point3d[][]{ null, aap3RefHookCurve[1], aap3RefHookCurve[2], null }, new Vector3f[][]{ null, aav3HookNormals[1], aav3HookNormals[2], null });
			subdivHashPatch4(ap3d, ap3refd, av3d, level, new Point3d[][]{ null, null, aap3HookCurve[2], aap3HookCurve[3] }, new Point3d[][]{ null, null, aap3RefHookCurve[2], aap3RefHookCurve[3] }, new Vector3f[][]{ null, null, aav3HookNormals[2], aav3HookNormals[3] });
			return;
		}
		
		Point3d p0 = new Point3d(ap3[0]);
		Point3d p1 = new Point3d(ap3[3]);
		Point3d p2 = new Point3d(ap3[6]);
		Point3d p3 = new Point3d(ap3[9]);
		Point3d r0 = new Point3d(ap3ref[0]);
		Point3d r1 = new Point3d(ap3ref[3]);
		Point3d r2 = new Point3d(ap3ref[6]);
		Point3d r3 = new Point3d(ap3ref[9]);
		Vector3f n0 = new Vector3f(av3[0]);
		Vector3f n1 = new Vector3f(av3[1]);
		Vector3f n2 = new Vector3f(av3[2]);
		Vector3f n3 = new Vector3f(av3[3]);
		
		/* draw the patch */
		if (aap3HookCurve[0] != null) {
			snapPoint(p0, aap3HookCurve[0], r0, aap3RefHookCurve[0], n0, aav3HookNormals[0]);
			snapPoint(p1, aap3HookCurve[0], r1, aap3RefHookCurve[0], n1, aav3HookNormals[0]);
		}
		if (aap3HookCurve[1] != null) {
			snapPoint(p1, aap3HookCurve[1], r1, aap3RefHookCurve[1], n1, aav3HookNormals[1]);
			snapPoint(p2, aap3HookCurve[1], r2, aap3RefHookCurve[1], n2, aav3HookNormals[1]);
		}
		if (aap3HookCurve[2] != null) {
			snapPoint(p2, aap3HookCurve[2], r2, aap3RefHookCurve[2], n2, aav3HookNormals[2]);
			snapPoint(p3, aap3HookCurve[2], r3, aap3RefHookCurve[2], n3, aav3HookNormals[2]);
		}
		if (aap3HookCurve[3] != null) {
			snapPoint(p3, aap3HookCurve[3], r3, aap3RefHookCurve[3], n3, aav3HookNormals[3]);
			snapPoint(p0, aap3HookCurve[3], r0, aap3RefHookCurve[3], n0, aav3HookNormals[3]);
		}
		quadDrain.addQuad(p0, p1, p2, p3, r0, r1, r2, r3, n0, n1, n2, n3);
	}
	
	private void snapPoint(Point3d p, Point3d[] ap, Point3d r, Point3d[] ar, Vector3f n, Vector3f[] av) {
		double dist = Double.MAX_VALUE;
		int num = -1;
		for (int i = 0; i < ar.length; i++) {
			double d = r.distanceSquared(ar[i]);
			if (d < dist) {
				dist = d;
				num = i;
			}
		}
		p.set(ap[num]);
		r.set(ar[num]);
		n.set(av[num]);
	}
	
	public Point3d[] subdivCurve(Point3d p0, Point3d p1, Point3d p2, Point3d p3) {
		int s = (1 << iMaxSubdiv ) + 1;
		Point3d[] result = new Point3d[s];
		for (int i = 0; i < s; result[i++] = new Point3d());
		Bezier.evaluate(p0, p1, p2, p3, result);
		//System.out.println("s=" + s);
		return result;
	}
	
	public Vector3f[] interpolateNormals(Vector3f n0, Vector3f n1) {
		//System.out.println("interpolate " + n0 + " " + n1);
		//int s = (1 << iMaxSubdiv ) + 1;
		//Vector3f[] n = new Vector3f[s];
		//for (int i = 0; i < s; i++) {
		//	float t = (float) i / (s - 1);
		//	float t1 = 1 - t;
		//	n[i] = new Vector3f(n0.x * t1 + n1.x * t, n0.y * t1 + n1.y * t, n0.z * t1 + n1.z * t);
		//}
		//return n;
			
		int s = (1 << iMaxSubdiv ) + 1;
		Vector3f[] n = new Vector3f[s];
		n[0] = new Vector3f(n0);
		n[s - 1] = new Vector3f(n1);
		switch (iMaxSubdiv) {
			case 1: n[1] = interpolateNormal(n[0], n[2]);
				break;
			case 2: n[2] = interpolateNormal(n[0], n[4]);
				n[1] = interpolateNormal(n[0], n[2]);
				n[3] = interpolateNormal(n[2], n[4]);
				break;
			case 3: n[4] = interpolateNormal(n[0], n[8]);
				n[2] = interpolateNormal(n[0], n[4]);
				n[6] = interpolateNormal(n[4], n[8]);
				n[1] = interpolateNormal(n[0], n[2]);
				n[3] = interpolateNormal(n[2], n[4]);
				n[5] = interpolateNormal(n[4], n[6]);
				n[7] = interpolateNormal(n[6], n[8]);
				break;
			case 4: n[8] = interpolateNormal(n[0], n[16]);
				n[4] = interpolateNormal(n[0], n[8]);
				n[12] = interpolateNormal(n[8], n[16]);
				n[2] = interpolateNormal(n[0], n[4]);
				n[6] = interpolateNormal(n[4], n[8]);
				n[10] = interpolateNormal(n[8], n[12]);
				n[14] = interpolateNormal(n[12], n[16]);
				n[1] = interpolateNormal(n[0], n[2]);
				n[3] = interpolateNormal(n[2], n[4]);
				n[5] = interpolateNormal(n[4], n[6]);
				n[7] = interpolateNormal(n[6], n[8]);
				n[9] = interpolateNormal(n[8], n[10]);
				n[11] = interpolateNormal(n[10], n[12]);
				n[13] = interpolateNormal(n[12], n[14]);
				n[15] = interpolateNormal(n[14], n[16]);
				break;
		}
		return n;
	}
	
	public void subdivHashPatch(Point3f[] fap3, Point3f[] fap3ref, Vector3f[] av3, Point3d[][] aap3HookCurve, Point3d[][] aap3RefHookCurve, Vector3f[][] aav3HookNormals) {
		Point3d[] ap3 = new Point3d[fap3.length];
		for (int i = 0; i < ap3.length; ap3[i] = new Point3d(fap3[i++]));
		Point3d[] ap3ref = new Point3d[fap3ref.length];
		for (int i = 0; i < ap3ref.length; ap3ref[i] = new Point3d(fap3ref[i++]));
		
		//boolean[] flat = new boolean[] { false, false, false, false };
		switch (ap3.length) {
			case 9: {
				//Vector3f[] n = new Vector3f[] {av3[0], av3[1], av3[2], new Vector3f(av3[0])};
				//Point3d[] p = new Point3d[] {ap3[0], ap3[1], ap3[2], ap3[3], ap3[4], ap3[5], ap3[6], ap3[7], ap3[8], new Point3d(ap3[0]), new Point3d(ap3[0]), new Point3d(ap3[0])};
				//Point3d[] pref = new Point3d[] {ap3ref[0], ap3ref[1], ap3ref[2], ap3ref[3], ap3ref[4], ap3ref[5], ap3ref[6], ap3ref[7], ap3ref[8], new Point3d(ap3ref[0]), new Point3d(ap3ref[0]), new Point3d(ap3ref[0])};
				//int[] levels = new int[] { ail[0], ail[1], ail[2], 0 };
				//subdivHashPatch4(p, pref, n, flat, levels);
				//if (true) return;
				
				double centerscale = 4f/3f;
				double scale = 2f/5f;
				
				/* set up midpoint normals */
				Vector3f[] mn = new Vector3f[3];
				mn[0] = interpolateNormal(av3[0], av3[1]);
				mn[1] = interpolateNormal(av3[1], av3[2]);
				mn[2] = interpolateNormal(av3[2], av3[0]);
				
				Point3d A = Functions.parallelogram(ap3[0],ap3[1],ap3[8]);
				Point3d B = Functions.parallelogram(ap3[3],ap3[4],ap3[2]);
				Point3d C = Functions.parallelogram(ap3[6],ap3[7],ap3[5]);
				
				Point3d F = Functions.average(A,ap3[0],ap3[1],ap3[8]);
				Point3d H = Functions.average(B,ap3[3],ap3[4],ap3[2]);
				Point3d J = Functions.average(C,ap3[6],ap3[7],ap3[5]);
				
				Point3d G = Functions.average(A,B,ap3[1],ap3[2]);
				Point3d I = Functions.average(B,C,ap3[4],ap3[5]);
				Point3d K = Functions.average(C,A,ap3[7],ap3[8]);
				
				Point3d U = Functions.average(A,B,C);
				
				Point3d P = Functions.average(U,K,F,G);
				Point3d Q = Functions.average(U,G,H,I);
				Point3d R = Functions.average(U,I,J,K);
				
				Point3d V = Functions.average(R,P);
				Point3d W = Functions.average(P,Q);
				Point3d X = Functions.average(Q,R);
				
				Point3d Center = Functions.average(P,Q,R);
				
				V.interpolate(Center, V, centerscale);
				W.interpolate(Center, W, centerscale);
				X.interpolate(Center, X, centerscale);
				
				Point3d Aref = Functions.parallelogram(ap3ref[0],ap3ref[1],ap3ref[8]);
				Point3d Bref = Functions.parallelogram(ap3ref[3],ap3ref[4],ap3ref[2]);
				Point3d Cref = Functions.parallelogram(ap3ref[6],ap3ref[7],ap3ref[5]);
				
				Point3d Fref = Functions.average(Aref,ap3ref[0],ap3ref[1],ap3ref[8]);
				Point3d Href = Functions.average(Bref,ap3ref[3],ap3ref[4],ap3ref[2]);
				Point3d Jref = Functions.average(Cref,ap3ref[6],ap3ref[7],ap3ref[5]);
				
				Point3d Gref = Functions.average(Aref,Bref,ap3ref[1],ap3ref[2]);
				Point3d Iref = Functions.average(Bref,Cref,ap3ref[4],ap3ref[5]);
				Point3d Kref = Functions.average(Cref,Aref,ap3ref[7],ap3ref[8]);
				
				Point3d Uref = Functions.average(Aref,Bref,Cref);
				
				Point3d Pref = Functions.average(Uref,Kref,Fref,Gref);
				Point3d Qref = Functions.average(Uref,Gref,Href,Iref);
				Point3d Rref = Functions.average(Uref,Iref,Jref,Kref);
				
				Point3d Vref = Functions.average(Rref,Pref);
				Point3d Wref = Functions.average(Pref,Qref);
				Point3d Xref = Functions.average(Qref,Rref);
				
				Point3d Centerref = Functions.average(Pref,Qref,Rref);
				
				Vref.interpolate(Centerref, Vref, centerscale);
				Wref.interpolate(Centerref, Wref, centerscale);
				Xref.interpolate(Centerref, Xref, centerscale);
				
				Vector3f vc = new Vector3f(Functions.vector(V, Center));
				Vector3f wc = new Vector3f(Functions.vector(W, Center));
				Vector3f xc = new Vector3f(Functions.vector(X, Center));
				
				Vector3f[] nc = newNormals(3);
				nc[0].cross(vc, wc);
				nc[1].cross(wc, xc);
				nc[2].cross(xc, vc);
				
				nc[0].normalize();
				nc[1].normalize();
				nc[2].normalize();
				
				Vector3f centerNormal = Functions.vaverage(nc[0], nc[1], nc[2]);
				centerNormal.normalize();
				
				Vector3f[] normals = newNormals(4);
				
				Point3d[][] aap3Boundary = new Point3d[3][7];
				aap3Boundary[0] = Bezier.deCasteljau(ap3[0],ap3[1],ap3[2],ap3[3],0.5);
				aap3Boundary[1] = Bezier.deCasteljau(ap3[3],ap3[4],ap3[5],ap3[6],0.5);
				aap3Boundary[2] = Bezier.deCasteljau(ap3[6],ap3[7],ap3[8],ap3[0],0.5);
				
				
				Point3d[][] aap3refBoundary = new Point3d[3][7];
				aap3refBoundary[0] = Bezier.deCasteljau(ap3ref[0],ap3ref[1],ap3ref[2],ap3ref[3],0.5);
				aap3refBoundary[1] = Bezier.deCasteljau(ap3ref[3],ap3ref[4],ap3ref[5],ap3ref[6],0.5);
				aap3refBoundary[2] = Bezier.deCasteljau(ap3ref[6],ap3ref[7],ap3ref[8],ap3ref[0],0.5);
				
				Point3d[] ap3Patches = new Point3d[12];
				Point3d[] ap3refPatches = new Point3d[12];
				
				Point3d[][] aap3hc;
				Point3d[][] aap3rhc;
				Vector3f[][] aav3hn;
				
				aap3hc = new Point3d[][] { aap3HookCurve[0], null, null, aap3HookCurve[2] };
				aap3rhc = new Point3d[][] { aap3RefHookCurve[0], null, null, aap3RefHookCurve[2] };
				aav3hn = new Vector3f[][] { aav3HookNormals[0], null, null, aav3HookNormals[2] };
				ap3Patches[0] = new Point3d(aap3Boundary[0][0]);
				ap3Patches[1] = new Point3d(aap3Boundary[0][1]);
				ap3Patches[2] = new Point3d(aap3Boundary[0][2]);
				ap3Patches[3] = new Point3d(aap3Boundary[0][3]);
				ap3Patches[11] = new Point3d(aap3Boundary[2][5]);
				ap3Patches[4] = new Point3d(aap3Boundary[0][3]);
				ap3Patches[4].add(Functions.average(Functions.scaledvector(ap3[3],ap3[4], scale), Functions.scaledvector(ap3[0],ap3[8], scale)));
				ap3Patches[10] = new Point3d(aap3Boundary[2][4]);
				ap3Patches[5] = new Point3d(W);
				ap3Patches[9] = new Point3d(aap3Boundary[2][3]);
				ap3Patches[8] = new Point3d(aap3Boundary[2][3]);
				ap3Patches[8].add(Functions.average(Functions.scaledvector(ap3[0],ap3[1], scale), Functions.scaledvector(ap3[6],ap3[5], scale)));
				ap3Patches[7] = new Point3d(V);
				ap3Patches[6] = new Point3d(Center);
				
				ap3refPatches[0] = new Point3d(aap3refBoundary[0][0]);
				ap3refPatches[1] = new Point3d(aap3refBoundary[0][1]);
				ap3refPatches[2] = new Point3d(aap3refBoundary[0][2]);
				ap3refPatches[3] = new Point3d(aap3refBoundary[0][3]);
				ap3refPatches[11] = new Point3d(aap3refBoundary[2][5]);
				ap3refPatches[4] = new Point3d(aap3refBoundary[0][3]);
				ap3refPatches[4].add(Functions.average(Functions.scaledvector(ap3ref[3],ap3ref[4], scale),Functions.scaledvector(ap3ref[0],ap3ref[8], scale)));
				ap3refPatches[10] = new Point3d(aap3refBoundary[2][4]);
				ap3refPatches[5] = new Point3d(Wref);
				ap3refPatches[9] = new Point3d(aap3refBoundary[2][3]);
				ap3refPatches[8] = new Point3d(aap3refBoundary[2][3]);
				ap3refPatches[8].add(Functions.average(Functions.scaledvector(ap3ref[0],ap3ref[1], scale),Functions.scaledvector(ap3ref[6],ap3ref[5], scale)));
				ap3refPatches[7] = new Point3d(Vref);
				ap3refPatches[6] = new Point3d(Centerref);
				
				normals[0].set(av3[0]);
				normals[1].set(mn[0]);
				normals[2].set(centerNormal);
				normals[3].set(mn[2]);
				//flat[0] = flat[1] = flat[2] = flat[3] = false;
				//levels[0] = ail[0] + 1;
				//levels[1] = 1;
				//levels[2] = 1;
				//levels[3] = ail[2] + 1;
				subdivHashPatch4(ap3Patches, ap3refPatches, normals, 1, aap3hc, aap3rhc, aav3hn);
				
				aap3hc = new Point3d[][] { aap3HookCurve[1], null, null, aap3HookCurve[0] };
				aap3rhc = new Point3d[][] { aap3RefHookCurve[1], null, null, aap3RefHookCurve[0] };
				aav3hn = new Vector3f[][] { aav3HookNormals[1], null, null, aav3HookNormals[0] };
				ap3Patches[0].set(aap3Boundary[1][0]);
				ap3Patches[1].set(aap3Boundary[1][1]);
				ap3Patches[2].set(aap3Boundary[1][2]);
				ap3Patches[3].set(aap3Boundary[1][3]);
				ap3Patches[11].set(aap3Boundary[0][5]);
				ap3Patches[4].set(aap3Boundary[1][3]);
				ap3Patches[4].add(Functions.average(Functions.scaledvector(ap3[6],ap3[7], scale),Functions.scaledvector(ap3[3],ap3[2], scale)));
				ap3Patches[10].set(aap3Boundary[0][4]);
				ap3Patches[5].set(X);
				ap3Patches[9].set(aap3Boundary[0][3]);
				ap3Patches[8].set(aap3Boundary[0][3]);
				ap3Patches[8].add(Functions.average(Functions.scaledvector(ap3[3],ap3[4], scale),Functions.scaledvector(ap3[0],ap3[8], scale)));
				ap3Patches[7].set(W);
				ap3Patches[6].set(Center);
				
				ap3refPatches[0].set(aap3refBoundary[1][0]);
				ap3refPatches[1].set(aap3refBoundary[1][1]);
				ap3refPatches[2].set(aap3refBoundary[1][2]);
				ap3refPatches[3].set(aap3refBoundary[1][3]);
				ap3refPatches[11].set(aap3refBoundary[0][5]);
				ap3refPatches[4].set(aap3refBoundary[1][3]);
				ap3refPatches[4].add(Functions.average(Functions.scaledvector(ap3ref[6],ap3ref[7], scale),Functions.scaledvector(ap3ref[3],ap3ref[2], scale)));
				ap3refPatches[10].set(aap3refBoundary[0][4]);
				ap3refPatches[5].set(Xref);
				ap3refPatches[9].set(aap3refBoundary[0][3]);
				ap3refPatches[8].set(aap3refBoundary[0][3]);
				ap3refPatches[8].add(Functions.average(Functions.scaledvector(ap3ref[3],ap3ref[4], scale),Functions.scaledvector(ap3ref[0],ap3ref[8], scale)));
				ap3refPatches[7].set(Wref);
				ap3refPatches[6].set(Centerref);
				
				normals[0].set(av3[1]);
				normals[1].set(mn[1]);
				normals[2].set(centerNormal);
				normals[3].set(mn[0]);
				//flat[0] = flat[1] = flat[2] = flat[3] = false;
				//levels[0] = ail[1] + 1;
				//levels[1] = 1;
				//levels[2] = 1;
				//levels[3] = ail[0] + 1;
				
				subdivHashPatch4(ap3Patches, ap3refPatches, normals, 1, aap3hc, aap3rhc, aav3hn);
				
				aap3hc = new Point3d[][] { aap3HookCurve[2], null, null, aap3HookCurve[1] };
				aap3rhc = new Point3d[][] { aap3RefHookCurve[2], null, null, aap3RefHookCurve[1] };
				aav3hn = new Vector3f[][] { aav3HookNormals[2], null, null, aav3HookNormals[1] };
				ap3Patches[0].set(aap3Boundary[2][0]);
				ap3Patches[1].set(aap3Boundary[2][1]);
				ap3Patches[2].set(aap3Boundary[2][2]);
				ap3Patches[3].set(aap3Boundary[2][3]);
				ap3Patches[11].set(aap3Boundary[1][5]);
				ap3Patches[4].set(aap3Boundary[2][3]);
				ap3Patches[4].add(Functions.average(Functions.scaledvector(ap3[0],ap3[1], scale),Functions.scaledvector(ap3[6],ap3[5], scale)));
				ap3Patches[10] = new Point3d(aap3Boundary[1][4]);
				ap3Patches[5].set(V);
				ap3Patches[9].set(aap3Boundary[1][3]);
				ap3Patches[8].set(aap3Boundary[1][3]);
				ap3Patches[8].add(Functions.average(Functions.scaledvector(ap3[6],ap3[7], scale),Functions.scaledvector(ap3[3],ap3[2], scale)));
				ap3Patches[7].set(X);
				ap3Patches[6].set(Center);
				
				ap3refPatches[0].set(aap3refBoundary[2][0]);
				ap3refPatches[1].set(aap3refBoundary[2][1]);
				ap3refPatches[2].set(aap3refBoundary[2][2]);
				ap3refPatches[3].set(aap3refBoundary[2][3]);
				ap3refPatches[11].set(aap3refBoundary[1][5]);
				ap3refPatches[4].set(aap3refBoundary[2][3]);
				ap3refPatches[4].add(Functions.average(Functions.scaledvector(ap3ref[0],ap3ref[1], scale),Functions.scaledvector(ap3ref[6],ap3ref[5], scale)));
				ap3refPatches[10] = new Point3d(aap3refBoundary[1][4]);
				ap3refPatches[5].set(Vref);
				ap3refPatches[9].set(aap3refBoundary[1][3]);
				ap3refPatches[8].set(aap3refBoundary[1][3]);
				ap3refPatches[8].add(Functions.average(Functions.scaledvector(ap3ref[6],ap3ref[7], scale),Functions.scaledvector(ap3ref[3],ap3ref[2], scale)));
				ap3refPatches[7].set(Xref);
				ap3refPatches[6].set(Centerref);
				
				normals[0].set(av3[2]);
				normals[1].set(mn[2]);
				normals[2].set(centerNormal);
				normals[3].set(mn[1]);
				//flat[0] = flat[1] = flat[2] = flat[3] = false;
				//levels[0] = ail[2] + 1;
				//levels[1] = 1;
				//levels[2] = 1;
				//levels[3] = ail[1] + 1;
				subdivHashPatch4(ap3Patches, ap3refPatches, normals, 1, aap3hc, aap3rhc, aav3hn);
			}
			break;
			case 12: {
				subdivHashPatch4(ap3, ap3ref, av3, 0, aap3HookCurve, aap3RefHookCurve, aav3HookNormals);
			}
			break;
			case 15: {
				/* set up midpoint normals */
				Vector3f[] mn = new Vector3f[5];
				mn[0] = interpolateNormal(av3[0], av3[1]);
				mn[1] = interpolateNormal(av3[1], av3[2]);
				mn[2] = interpolateNormal(av3[2], av3[3]);
				mn[3] = interpolateNormal(av3[3], av3[4]);
				mn[4] = interpolateNormal(av3[4], av3[0]);
				
				Point3d A = Functions.parallelogram(ap3[0],ap3[1],ap3[14]);
				Point3d B = Functions.parallelogram(ap3[3],ap3[4],ap3[2]);
				Point3d C = Functions.parallelogram(ap3[6],ap3[7],ap3[5]);
				Point3d D = Functions.parallelogram(ap3[9],ap3[10],ap3[8]);
				Point3d E = Functions.parallelogram(ap3[12],ap3[13],ap3[11]);
				Point3d F = Functions.average(A,ap3[0],ap3[1],ap3[14]);
				Point3d H = Functions.average(B,ap3[3],ap3[4],ap3[2]);
				Point3d J = Functions.average(C,ap3[6],ap3[7],ap3[5]);
				Point3d L = Functions.average(D,ap3[9],ap3[10],ap3[8]);
				Point3d N = Functions.average(E,ap3[12],ap3[13],ap3[11]);
				Point3d G = Functions.average(A,B,ap3[1],ap3[2]);
				Point3d I = Functions.average(B,C,ap3[4],ap3[5]);
				Point3d K = Functions.average(C,D,ap3[7],ap3[8]);
				Point3d M = Functions.average(D,E,ap3[10],ap3[11]);
				Point3d O = Functions.average(E,A,ap3[13],ap3[14]);
				Point3d U = Functions.average(A,B,C,D,E);
				Point3d P = Functions.average(U,O,F,G);
				Point3d Q = Functions.average(U,G,H,I);
				Point3d R = Functions.average(U,I,J,K);
				Point3d S = Functions.average(U,K,L,M);
				Point3d T = Functions.average(U,M,N,O);
				Point3d V = Functions.average(T,P);
				Point3d W = Functions.average(P,Q);
				Point3d X = Functions.average(Q,R);
				Point3d Y = Functions.average(R,S);
				Point3d Z = Functions.average(S,T);
				Point3d Center = Functions.average(P,Q,R,S,T);
				
				Point3d Aref = Functions.parallelogram(ap3ref[0],ap3ref[1],ap3ref[14]);
				Point3d Bref = Functions.parallelogram(ap3ref[3],ap3ref[4],ap3ref[2]);
				Point3d Cref = Functions.parallelogram(ap3ref[6],ap3ref[7],ap3ref[5]);
				Point3d Dref = Functions.parallelogram(ap3ref[9],ap3ref[10],ap3ref[8]);
				Point3d Eref = Functions.parallelogram(ap3ref[12],ap3ref[13],ap3ref[11]);
				Point3d Fref = Functions.average(Aref,ap3ref[0],ap3ref[1],ap3ref[14]);
				Point3d Href = Functions.average(Bref,ap3ref[3],ap3ref[4],ap3ref[2]);
				Point3d Jref = Functions.average(Cref,ap3ref[6],ap3ref[7],ap3ref[5]);
				Point3d Lref = Functions.average(Dref,ap3ref[9],ap3ref[10],ap3ref[8]);
				Point3d Nref = Functions.average(Eref,ap3ref[12],ap3ref[13],ap3ref[11]);
				Point3d Gref = Functions.average(Aref,Bref,ap3ref[1],ap3ref[2]);
				Point3d Iref = Functions.average(Bref,Cref,ap3ref[4],ap3ref[5]);
				Point3d Kref = Functions.average(Cref,Dref,ap3ref[7],ap3ref[8]);
				Point3d Mref = Functions.average(Dref,Eref,ap3ref[10],ap3ref[11]);
				Point3d Oref = Functions.average(Eref,Aref,ap3ref[13],ap3ref[14]);
				Point3d Uref = Functions.average(Aref,Bref,Cref,Dref,Eref);
				Point3d Pref = Functions.average(Uref,Oref,Fref,Gref);
				Point3d Qref = Functions.average(Uref,Gref,Href,Iref);
				Point3d Rref = Functions.average(Uref,Iref,Jref,Kref);
				Point3d Sref = Functions.average(Uref,Kref,Lref,Mref);
				Point3d Tref = Functions.average(Uref,Mref,Nref,Oref);
				Point3d Vref = Functions.average(Tref,Pref);
				Point3d Wref = Functions.average(Pref,Qref);
				Point3d Xref = Functions.average(Qref,Rref);
				Point3d Yref = Functions.average(Rref,Sref);
				Point3d Zref = Functions.average(Sref,Tref);
				Point3d Centerref = Functions.average(Pref,Qref,Rref,Sref,Tref);
				
				Vector3f vc = new Vector3f(Functions.vector(V, Center));
				Vector3f wc = new Vector3f(Functions.vector(W, Center));
				Vector3f xc = new Vector3f(Functions.vector(X, Center));
				Vector3f yc = new Vector3f(Functions.vector(Y, Center));
				Vector3f zc = new Vector3f(Functions.vector(Z, Center));
				
				Vector3f[] nc = newNormals(5);
				nc[0].cross(vc, wc);
				nc[1].cross(wc, xc);
				nc[2].cross(xc, yc);
				nc[3].cross(yc, zc);
				nc[4].cross(zc, vc);
				nc[0].normalize();
				nc[1].normalize();
				nc[2].normalize();
				nc[3].normalize();
				nc[4].normalize();
				Vector3f centerNormal = Functions.vaverage(nc[0], nc[1], nc[2], nc[3], nc[4]);
				centerNormal.normalize();
				
				Vector3f[] normals = newNormals(4);
				
				Point3d[][] aap3Boundary = new Point3d[5][7];
				aap3Boundary[0] = Bezier.deCasteljau(ap3[0],ap3[1],ap3[2],ap3[3],0.5);
				aap3Boundary[1] = Bezier.deCasteljau(ap3[3],ap3[4],ap3[5],ap3[6],0.5);
				aap3Boundary[2] = Bezier.deCasteljau(ap3[6],ap3[7],ap3[8],ap3[9],0.5);
				aap3Boundary[3] = Bezier.deCasteljau(ap3[9],ap3[10],ap3[11],ap3[12],0.5);
				aap3Boundary[4] = Bezier.deCasteljau(ap3[12],ap3[13],ap3[14],ap3[0],0.5);
				
				Point3d[][] aap3refBoundary = new Point3d[5][7];
				aap3refBoundary[0] = Bezier.deCasteljau(ap3ref[0],ap3ref[1],ap3ref[2],ap3ref[3],0.5);
				aap3refBoundary[1] = Bezier.deCasteljau(ap3ref[3],ap3ref[4],ap3ref[5],ap3ref[6],0.5);
				aap3refBoundary[2] = Bezier.deCasteljau(ap3ref[6],ap3ref[7],ap3ref[8],ap3ref[9],0.5);
				aap3refBoundary[3] = Bezier.deCasteljau(ap3ref[9],ap3ref[10],ap3ref[11],ap3ref[12],0.5);
				aap3refBoundary[4] = Bezier.deCasteljau(ap3ref[12],ap3ref[13],ap3ref[14],ap3ref[0],0.5);
				
				Point3d[] ap3Patches = new Point3d[12];
				Point3d[] ap3refPatches = new Point3d[12];
				
				//int[] levels = new int[4];
				Point3d[][] aap3hc;
				Point3d[][] aap3rhc;
				Vector3f[][] aav3hn;
				
				aap3hc = new Point3d[][] { aap3HookCurve[0], null, null, aap3HookCurve[4] };
				aap3rhc = new Point3d[][] { aap3RefHookCurve[0], null, null, aap3RefHookCurve[4] };
				aav3hn = new Vector3f[][] { aav3HookNormals[0], null, null, aav3HookNormals[4] };
				ap3Patches[0] = new Point3d(aap3Boundary[0][0]);
				ap3Patches[1] = new Point3d(aap3Boundary[0][1]);
				ap3Patches[2] = new Point3d(aap3Boundary[0][2]);
				ap3Patches[3] = new Point3d(aap3Boundary[0][3]);
				ap3Patches[11] = new Point3d(aap3Boundary[4][5]);
				ap3Patches[4] = new Point3d(aap3Boundary[0][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[14])));
				ap3Patches[10] = new Point3d(aap3Boundary[4][4]);
				ap3Patches[5] = new Point3d(W);
				ap3Patches[9] = new Point3d(aap3Boundary[4][3]);
				ap3Patches[8] = new Point3d(aap3Boundary[4][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[12],ap3[11])));
				ap3Patches[7] = new Point3d(V);
				ap3Patches[6] = new Point3d(Center);
				
				ap3refPatches[0] = new Point3d(aap3refBoundary[0][0]);
				ap3refPatches[1] = new Point3d(aap3refBoundary[0][1]);
				ap3refPatches[2] = new Point3d(aap3refBoundary[0][2]);
				ap3refPatches[3] = new Point3d(aap3refBoundary[0][3]);
				ap3refPatches[11] = new Point3d(aap3refBoundary[4][5]);
				ap3refPatches[4] = new Point3d(aap3refBoundary[0][3]);
				ap3refPatches[4].add(Functions.average(Functions.vector(ap3ref[3],ap3ref[4]),Functions.vector(ap3ref[0],ap3ref[14])));
				ap3refPatches[10] = new Point3d(aap3refBoundary[4][4]);
				ap3refPatches[5] = new Point3d(Wref);
				ap3refPatches[9] = new Point3d(aap3refBoundary[4][3]);
				ap3refPatches[8] = new Point3d(aap3refBoundary[4][3]);
				ap3refPatches[8].add(Functions.average(Functions.vector(ap3ref[0],ap3ref[1]),Functions.vector(ap3ref[12],ap3ref[11])));
				ap3refPatches[7] = new Point3d(Vref);
				ap3refPatches[6] = new Point3d(Centerref);
				
				normals[0].set(av3[0]);
				normals[1].set(mn[0]);
				normals[2].set(centerNormal);
				normals[3].set(mn[4]);
				//flat[0] = flat[1] = flat[2] = flat[3] = false;
				//levels[0] = ail[0] + 1;
				//levels[1] = 1;
				//levels[2] = 1;
				//levels[3] = ail[4] + 1;
				subdivHashPatch4(ap3Patches, ap3refPatches, normals, 1, aap3hc, aap3rhc, aav3hn);
				
				aap3hc = new Point3d[][] { aap3HookCurve[1], null, null, aap3HookCurve[0] };
				aap3rhc = new Point3d[][] { aap3RefHookCurve[1], null, null, aap3RefHookCurve[0] };
				aav3hn = new Vector3f[][] { aav3HookNormals[1], null, null, aav3HookNormals[0] };
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
				
				ap3refPatches[0].set(aap3refBoundary[1][0]);
				ap3refPatches[1].set(aap3refBoundary[1][1]);
				ap3refPatches[2].set(aap3refBoundary[1][2]);
				ap3refPatches[3].set(aap3refBoundary[1][3]);
				ap3refPatches[11].set(aap3refBoundary[0][5]);
				ap3refPatches[4].set(aap3refBoundary[1][3]);
				ap3refPatches[4].add(Functions.average(Functions.vector(ap3ref[6],ap3ref[7]),Functions.vector(ap3ref[3],ap3ref[2])));
				ap3refPatches[10].set(aap3refBoundary[0][4]);
				ap3refPatches[5].set(Xref);
				ap3refPatches[9].set(aap3refBoundary[0][3]);
				ap3refPatches[8].set(aap3refBoundary[0][3]);
				ap3refPatches[8].add(Functions.average(Functions.vector(ap3ref[3],ap3ref[4]),Functions.vector(ap3ref[0],ap3ref[14])));
				ap3refPatches[7].set(Wref);
				ap3refPatches[6].set(Centerref);
				
				normals[0].set(av3[1]);
				normals[1].set(mn[1]);
				normals[2].set(centerNormal);
				normals[3].set(mn[0]);
				//flat[0] = flat[1] = flat[2] = flat[3] = false;
				//levels[0] = ail[1] + 1;
				//levels[1] = 1;
				//levels[2] = 1;
				//levels[3] = ail[0] + 1;
				subdivHashPatch4(ap3Patches, ap3refPatches, normals, 1, aap3hc, aap3rhc, aav3hn);
				
				aap3hc = new Point3d[][] { aap3HookCurve[2], null, null, aap3HookCurve[1] };
				aap3rhc = new Point3d[][] { aap3RefHookCurve[2], null, null, aap3RefHookCurve[1] };
				aav3hn = new Vector3f[][] { aav3HookNormals[2], null, null, aav3HookNormals[1] };
				ap3Patches[0].set(aap3Boundary[2][0]);
				ap3Patches[1].set(aap3Boundary[2][1]);
				ap3Patches[2].set(aap3Boundary[2][2]);
				ap3Patches[3].set(aap3Boundary[2][3]);
				ap3Patches[11].set(aap3Boundary[1][5]);
				ap3Patches[4].set(aap3Boundary[2][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[9],ap3[10]),Functions.vector(ap3[6],ap3[5])));
				ap3Patches[10] = new Point3d(aap3Boundary[1][4]);
				ap3Patches[5].set(Y);
				ap3Patches[9].set(aap3Boundary[1][3]);
				ap3Patches[8].set(aap3Boundary[1][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2])));
				ap3Patches[7].set(X);
				ap3Patches[6].set(Center);
				
				ap3refPatches[0].set(aap3refBoundary[2][0]);
				ap3refPatches[1].set(aap3refBoundary[2][1]);
				ap3refPatches[2].set(aap3refBoundary[2][2]);
				ap3refPatches[3].set(aap3refBoundary[2][3]);
				ap3refPatches[11].set(aap3refBoundary[1][5]);
				ap3refPatches[4].set(aap3refBoundary[2][3]);
				ap3refPatches[4].add(Functions.average(Functions.vector(ap3ref[9],ap3ref[10]),Functions.vector(ap3ref[6],ap3ref[5])));
				ap3refPatches[10] = new Point3d(aap3refBoundary[1][4]);
				ap3refPatches[5].set(Yref);
				ap3refPatches[9].set(aap3refBoundary[1][3]);
				ap3refPatches[8].set(aap3refBoundary[1][3]);
				ap3refPatches[8].add(Functions.average(Functions.vector(ap3ref[6],ap3ref[7]),Functions.vector(ap3ref[3],ap3ref[2])));
				ap3refPatches[7].set(Xref);
				ap3refPatches[6].set(Centerref);
				
				normals[0].set(av3[2]);
				normals[1].set(mn[2]);
				normals[2].set(centerNormal);
				normals[3].set(mn[1]);
				//flat[0] = flat[1] = flat[2] = flat[3] = false;
				//levels[0] = ail[2] + 1;
				//levels[1] = 1;
				//levels[2] = 1;
				//levels[3] = ail[1] + 1;
				subdivHashPatch4(ap3Patches, ap3refPatches, normals, 1, aap3hc, aap3rhc, aav3hn);
				
				aap3hc = new Point3d[][] { aap3HookCurve[3], null, null, aap3HookCurve[2] };
				aap3rhc = new Point3d[][] { aap3RefHookCurve[3], null, null, aap3RefHookCurve[2] };
				aav3hn = new Vector3f[][] { aav3HookNormals[3], null, null, aav3HookNormals[2] };
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
				
				ap3refPatches[0].set(aap3refBoundary[3][0]);
				ap3refPatches[1].set(aap3refBoundary[3][1]);
				ap3refPatches[2].set(aap3refBoundary[3][2]);
				ap3refPatches[3].set(aap3refBoundary[3][3]);
				ap3refPatches[11].set(aap3refBoundary[2][5]);
				ap3refPatches[4].set(aap3refBoundary[3][3]);
				ap3refPatches[4].add(Functions.average(Functions.vector(ap3ref[9],ap3ref[8]),Functions.vector(ap3ref[12],ap3ref[13])));
				ap3refPatches[10].set(aap3refBoundary[2][4]);
				ap3refPatches[5].set(Zref);
				ap3refPatches[9].set(aap3refBoundary[2][3]);
				ap3refPatches[8].set(aap3refBoundary[2][3]);
				ap3refPatches[8].add(Functions.average(Functions.vector(ap3ref[9],ap3ref[10]),Functions.vector(ap3ref[6],ap3ref[5])));
				ap3refPatches[7].set(Yref);
				ap3refPatches[6].set(Centerref);
				
				normals[0].set(av3[3]);
				normals[1].set(mn[3]);
				normals[2].set(centerNormal);
				normals[3].set(mn[2]);
				//flat[0] = flat[1] = flat[2] = flat[3] = false;
				//levels[0] = ail[3] + 1;
				//levels[1] = 1;
				//levels[2] = 1;
				//levels[3] = ail[2] + 1;
				subdivHashPatch4(ap3Patches, ap3refPatches, normals, 1, aap3hc, aap3rhc, aav3hn);
				
				aap3hc = new Point3d[][] { aap3HookCurve[4], null, null, aap3HookCurve[3] };
				aap3rhc = new Point3d[][] { aap3RefHookCurve[4], null, null, aap3RefHookCurve[3] };
				aav3hn = new Vector3f[][] { aav3HookNormals[4], null, null, aav3HookNormals[3] };
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
				
				ap3refPatches[0].set(aap3refBoundary[4][0]);
				ap3refPatches[1].set(aap3refBoundary[4][1]);
				ap3refPatches[2].set(aap3refBoundary[4][2]);
				ap3refPatches[3].set(aap3refBoundary[4][3]);
				ap3refPatches[11].set(aap3refBoundary[3][5]);
				ap3refPatches[4].set(aap3refBoundary[4][3]);
				ap3refPatches[4].add(Functions.average(Functions.vector(ap3ref[12],ap3ref[11]),Functions.vector(ap3ref[0],ap3ref[1])));
				ap3refPatches[10].set(aap3refBoundary[3][4]);
				ap3refPatches[5].set(Vref);
				ap3refPatches[9].set(aap3refBoundary[3][3]);
				ap3refPatches[8].set(aap3refBoundary[3][3]);
				ap3refPatches[8].add(Functions.average(Functions.vector(ap3ref[9],ap3ref[8]),Functions.vector(ap3ref[12],ap3ref[13])));
				ap3refPatches[7].set(Zref);
				ap3refPatches[6].set(Centerref);
				
				normals[0].set(av3[4]);
				normals[1].set(mn[4]);
				normals[2].set(centerNormal);
				normals[3].set(mn[3]);
				//flat[0] = flat[1] = flat[2] = flat[3] = false;
				//levels[0] = ail[4] + 1;
				//levels[1] = 1;
				//levels[2] = 1;
				//levels[3] = ail[3] + 1;
				subdivHashPatch4(ap3Patches, ap3refPatches, normals, 1, aap3hc, aap3rhc, aav3hn);
			}
			break;
		}
	}
}
