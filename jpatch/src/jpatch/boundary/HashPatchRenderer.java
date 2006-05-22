///*
// * $Id: HashPatchRenderer.java,v 1.5 2006/05/22 10:46:19 sascha_l Exp $
// *
// * Copyright (c) 2005 Sascha Ledinsky
// *
// * This file is part of JPatch.
// *
// * JPatch is free software; you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation; either version 2 of the License, or
// * (at your option) any later version.
// *
// * JPatch is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with JPatch; if not, write to the Free Software
// * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
// */
//package jpatch.boundary;
//
//import javax.vecmath.*;
//
///**
// * @author sascha
// *
// */
//public class HashPatchRenderer {
//	private JPatchDrawable2 viewport;
//	private float fNearClip;
//	private float fW;
//	private boolean bPerspective;
//	
//	private static Vector3f v3a = new Vector3f();
//	private static Vector3f v3b = new Vector3f();
//	private static Vector3f v3c = new Vector3f();
//	
//	private float subdiv(Point3f p0, Point3f p1, Point3f p2, Point3f p3, boolean simple) {
//		if (bPerspective) {
//			return 0;
//		} else {
//			if (!simple) {
//				v3a.set(4 * p0.x - 6 *  p1.x + 2 * p3.x, 4 * p0.y - 6 *  p1.y + 2 * p3.y, 4 * p0.z - 6 *  p1.z + 2 * p3.z);
//				v3b.set(2 * p0.x - 6 *  p1.x + 4 * p3.x, 2 * p0.y - 6 *  p1.y + 4 * p3.y, 2 * p0.z - 6 *  p1.z + 4 * p3.z);
//				return v3a.length() + v3b.length();
//			} else {
//				v3a.set(p0.x - p1.x - p2.x + p3.x, p0.y - p1.y - p2.y + p3.y, p0.z - p1.z - p2.z + p3.z);
//				return v3a.length();
//			}
//		}
//	}
//	
////	private void drawHashPatch4Gaurad(Point3f[] ap3, Vector3f[] av3, int[] ac, boolean[] abFlat, int[] aiLevel, MaterialProperties mp) {
////		
////		//materialProperties = mp;
////		//Point3d[] adp3 = new Point3d[ap3.length];
////		//for (int i = 0; i < adp3.length; adp3[i] = new Point3d(ap3[i++]));
////		//hashPatchSubdivision.subdivHashPatch4(adp3, adp3, av3, 2, new Point3d[][] { null, null, null, null }, new Vector3f[][] { null, null, null, null });
////		//
////		//if (true) return;
////		
////		boolean visible = false;
////		loop:
////		for (int i = 0; i < 12; i++) {
////			if (ap3[i].x > 0 && ap3[i].x < iWidth && ap3[i].y > 0 && ap3[i].y < iHeight && ap3[i].z) {
////				if (!bPerspective || ap3[i].z > fNearClip) {
////					visible = true;
////					break loop;
////				}
////			}
////		}
////		if (!visible) return;
////		//System.out.println(abFlat[0] + "\t" + abFlat[1] + "\t" + abFlat[2] + "\t" + abFlat[3]);
////		
////		/* check if we need to u-split */
////		float u0 = subdiv(ap3[0], ap3[1], ap3[2], ap3[3], aiLevel[0] > 0);
////		float u2 = subdiv(ap3[9], ap3[8], ap3[7], ap3[6], aiLevel[2] > 0);
////		float v3 = subdiv(ap3[0], ap3[11], ap3[10], ap3[9], aiLevel[3] > 0);
////		float v1 = subdiv(ap3[3], ap3[4], ap3[5], ap3[6], aiLevel[1] > 0);
////		
////		float ul = (aiLevel[0] >= iMaxSubdiv || aiLevel[2] >= iMaxSubdiv) ? -1 : (float) Math.max(u0, u2);
////		float uv = (aiLevel[1] >= iMaxSubdiv || aiLevel[3] >= iMaxSubdiv) ? -1 : (float) Math.max(v1, v3);
////		
////		if (true && ul >= fFlatness && ul > uv * 1.1f) {
////		//if (false) {
////		//if (subdiv(ap3[0], ap3[1], ap3[2], ap3[3], uSplit) || subdiv(ap3[9], ap3[8], ap3[7], ap3[6], uSplit)) {
////			
////			/* flatten cubics if flat enough */
////			if (!abFlat[0] && (u0 <= fFlatness)) abFlat[0] = makeFlat(ap3[0], ap3[1], ap3[2], ap3[3]);
////			if (!abFlat[1] && (v1 <= fFlatness)) abFlat[1] = makeFlat(ap3[3], ap3[4], ap3[5], ap3[6]);
////			if (!abFlat[2] && (u2 <= fFlatness)) abFlat[2] = makeFlat(ap3[9], ap3[8], ap3[7], ap3[6]);
////			if (!abFlat[3] && (v3 <= fFlatness)) abFlat[3] = makeFlat(ap3[0], ap3[11], ap3[10], ap3[9]);
////			
////			
////			/* compute new normals */
////			Vector3f[] av3new = newNormals(4);
////			av3new[1].set(av3[1]);
////			av3new[0] = av3[1] = (aiLevel[0] > 0) ? interpolateNormal(av3[0], av3[1]) : interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]);
////			av3new[2].set(av3[2]);
////			av3new[3] = av3[2] = (aiLevel[2] > 0) ? interpolateNormal(av3[3], av3[2]) : interpolateNormal(av3[3], av3[2], ap3[9], ap3[8], ap3[7], ap3[6]);
////			
////			/* compute new patches */
////			Point3f[] ap3new = newPatch(12);
////			v3a.sub(ap3[11], ap3[0]);
////			v3b.sub(ap3[4], ap3[3]);
////			v3c.add(v3a, v3b);
////			v3c.scale(0.5f);
////			v3a.sub(ap3[10], ap3[9]);
////			v3b.sub(ap3[5], ap3[6]);
////			v3a.add(v3b);
////			v3a.scale(0.5f);
////			deCasteljauSplit(ap3[0], ap3[1], ap3[2], ap3[3], ap3new[0], ap3new[1], ap3new[2], ap3new[3]);
////			deCasteljauSplit(ap3[9], ap3[8], ap3[7], ap3[6], ap3new[9], ap3new[8], ap3new[7], ap3new[6]);
////			ap3new[4].set(ap3[4]);
////			ap3new[5].set(ap3[5]);
////			ap3[4].add(ap3[3], v3c);
////			ap3[5].add(ap3[6], v3a);
////			ap3new[11].set(ap3[4]);
////			ap3new[10].set(ap3[5]);
////			
////			///* compute new normals */
////			//Vector3f[] av3new = newNormals(4);
////			//av3new[1].set(av3[1]);
////			////av3new[0] = av3[1] = (uLevel > 99) ? interpolateNormal(av3[0], av3[1]) : interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]);
////			//av3new[2].set(av3[2]);
////			////av3new[3] = av3[2] = (uLevel > 99) ? interpolateNormal(av3[3], av3[2]) : interpolateNormal(av3[3], av3[2], ap3[9], ap3[8], ap3[7], ap3[6]);
////			//
////			//v3a.sub(ap3[4], ap3[3]);
////			//v3b.sub(ap3[2], ap3[3]);
////			//av3[1].cross(v3b, v3a);
////			//av3[1].normalize();
////			//av3new[0].set(av3[1]);
////			//v3a.sub(ap3[5], ap3[6]);
////			//v3b.sub(ap3[7], ap3[6]);
////			//av3[2].cross(v3a, v3b);
////			//av3[2].normalize();
////			//av3new[3].set(av3[2]);
////			
////			/* compute new colors */
////			int[] acnew = new int[4];
////			acnew[1] = ac[1];
////			acnew[0] = ac[1] = (!abFlat[0]) ? lighting.shade(ap3[3], av3[1], mp) : interpolateColor(ac[0], ac[1]);
////			acnew[2] = ac[2];
////			acnew[3] = ac[2] = (!abFlat[2]) ? lighting.shade(ap3[6], av3[2], mp) : interpolateColor(ac[2], ac[3]);
////			
////			/* set up new flatenough flags */
////			boolean[] abnew = new boolean[4];
////			abnew[0] = abFlat[0];
////			abnew[1] = abFlat[1];
////			abnew[2] = abFlat[2];
////			abnew[3] = abFlat[1] = false;
////			
////			///* compute new normals */
////			//Vector3f[] av3new = newNormals();
////			//av3new[1].set(av3[1]);
////			//v3b.sub(ap3[3],ap3[2]);
////			//av3[1].cross(v3b, v3c);
////			//av3[1].normalize();
////			//av3new[0].set(av3[1]);
////			//av3new[2].set(av3[2]);
////			//v3b.sub(ap3[7],ap3[6]);
////			//av3[2].cross(v3b, v3a);
////			//av3[2].normalize();
////			//av3new[3].set(av3[2]);
////			
////			/* recurse */
////			int l = (aiLevel[1] < aiLevel[3]) ? aiLevel[1] : aiLevel[3];
////			aiLevel[0]++;
////			aiLevel[2]++;
////			int[] newLevels = new int[aiLevel.length];
////			for (int i = 0; i < aiLevel.length; newLevels[i] = aiLevel[i++]);
////			aiLevel[1] = newLevels[3] = l;
////			drawHashPatch4Gaurad(ap3, av3, ac, abFlat, aiLevel, mp);
////			drawHashPatch4Gaurad(ap3new, av3new, acnew, abnew, newLevels, mp);
////			return;
////		}
////		/* check if we need to v-split */
////		else if (true && uv >= fFlatness) {
////		//else if (false) {
////		//else if (subdiv(ap3[0], ap3[11], ap3[10], ap3[9], vSplit) || subdiv(ap3[3], ap3[4], ap3[5], ap3[6], vSplit)) {
////			/* flatten cubics if flat enough */
////			if (!abFlat[0] && (u0 <= fFlatness)) abFlat[0] = makeFlat(ap3[0], ap3[1], ap3[2], ap3[3]);
////			if (!abFlat[1] && (v1 <= fFlatness)) abFlat[1] = makeFlat(ap3[3], ap3[4], ap3[5], ap3[6]);
////			if (!abFlat[2] && (u2 <= fFlatness)) abFlat[2] = makeFlat(ap3[9], ap3[8], ap3[7], ap3[6]);
////			if (!abFlat[3] && (v3 <= fFlatness)) abFlat[3] = makeFlat(ap3[0], ap3[11], ap3[10], ap3[9]);
////			
////			
////			/* compute new normals */
////			Vector3f[] av3new = newNormals(4);
////			av3new[3].set(av3[3]);
////			av3new[0] = av3[3] = (aiLevel[3] > 0) ? interpolateNormal(av3[0], av3[3]) : interpolateNormal(av3[0], av3[3], ap3[0], ap3[11], ap3[10], ap3[9]);
////			av3new[2].set(av3[2]);
////			av3new[1] = av3[2] = (aiLevel[1] > 0) ? interpolateNormal(av3[1], av3[2]) : interpolateNormal(av3[1], av3[2], ap3[3], ap3[4], ap3[5], ap3[6]);
////			
////			/* compute new patches */
////			Point3f[] ap3new = newPatch(12);
////			v3a.sub(ap3[1], ap3[0]);
////			v3b.sub(ap3[8], ap3[9]);
////			v3c.add(v3a, v3b);
////			v3c.scale(0.5f);
////			v3a.sub(ap3[2], ap3[3]);
////			v3b.sub(ap3[7], ap3[6]);
////			v3a.add(v3b);
////			v3a.scale(0.5f);
////			deCasteljauSplit(ap3[0], ap3[11], ap3[10], ap3[9], ap3new[0], ap3new[11], ap3new[10], ap3new[9]);
////			deCasteljauSplit(ap3[3], ap3[4], ap3[5], ap3[6], ap3new[3], ap3new[4], ap3new[5], ap3new[6]);
////			ap3new[8].set(ap3[8]);
////			ap3new[7].set(ap3[7]);
////			ap3[8].add(ap3[9], v3c);
////			ap3[7].add(ap3[6], v3a);
////			ap3new[1].set(ap3[8]);
////			ap3new[2].set(ap3[7]);
////			
////			///* compute new normals */
////			//Vector3f[] av3new = newNormals(4);
////			//av3new[3].set(av3[3]);
////			////av3new[0] = av3[3] = (vLevel > 99) ? interpolateNormal(av3[0], av3[3]) : interpolateNormal(av3[0], av3[3], ap3[0], ap3[11], ap3[10], ap3[9]);
////			//av3new[2].set(av3[2]);
////			////av3new[1] = av3[2] = (vLevel > 99) ? interpolateNormal(av3[1], av3[2]) : interpolateNormal(av3[1], av3[2], ap3[3], ap3[4], ap3[5], ap3[6]);
////			//
////			//v3a.sub(ap3[10], ap3[9]);
////			//v3b.sub(ap3[8], ap3[9]);
////			//av3[3].cross(v3b, v3a);
////			//av3[3].normalize();
////			//av3new[0].set(av3[3]);
////			//v3a.sub(ap3[5], ap3[6]);
////			//v3b.sub(ap3[7], ap3[6]);
////			//av3[2].cross(v3a, v3b);
////			//av3[2].normalize();
////			//av3new[1].set(av3[2]);
////			
////			/* compute new colors */
////			int[] acnew = new int[4];
////			acnew[3] = ac[3];
////			acnew[0] = ac[3] = (!abFlat[3]) ? lighting.shade(ap3[9], av3[3], mp) : interpolateColor(ac[0], ac[3]);
////			acnew[2] = ac[2];
////			acnew[1] = ac[2] = (!abFlat[1]) ? lighting.shade(ap3[6], av3[2], mp) : interpolateColor(ac[1], ac[2]);
////			
////			/* set up new flatenough flags */
////			boolean[] abnew = new boolean[4];
////			abnew[1] = abFlat[1];
////			abnew[3] = abFlat[3];
////			abnew[2] = abFlat[2];
////			abnew[0] = abFlat[2] = false;
////			
////			///* compute new normals */
////			//Vector3f[] av3new = newNormals();
////			//av3new[3].set(av3[3]);
////			//v3b.sub(ap3[10],ap3[9]);
////			//av3[3].cross(v3b, v3c);
////			//av3[3].normalize();
////			//av3new[0].set(av3[3]);
////			//av3new[2].set(av3[2]);
////			//v3b.sub(ap3[6],ap3[5]);
////			//av3[2].cross(v3b, v3a);
////			//av3[2].normalize();
////			//av3new[1].set(av3[2]);
////			
////			/* recurse */
////			int l = (aiLevel[0] < aiLevel[2]) ? aiLevel[0] : aiLevel[2];
////			aiLevel[1]++;
////			aiLevel[3]++;
////			int[] newLevels = new int[aiLevel.length];
////			for (int i = 0; i < aiLevel.length; newLevels[i] = aiLevel[i++]);
////			aiLevel[2] = newLevels[0] = l;
////			drawHashPatch4Gaurad(ap3, av3, ac, abFlat, aiLevel, mp);
////			drawHashPatch4Gaurad(ap3new, av3new, acnew, abnew, newLevels, mp);
////			return;
////		}
////		//}
////		/* draw the patch */
////		
////		//System.out.println("drawPatch");
////		//for (int i = 0; i < 12; System.out.println(ap3[i++]));
////		
////		//if (bBackfaceNormalFlip) flipBackfaceNormals(av3);
////		//av3[0].normalize();
////		//av3[1].normalize();
////		//av3[2].normalize();
////		//av3[3].normalize();
////		
////		//av3New[0].set(av3[0]);
////		//av3New[1].set(av3[1]);
////		//av3New[2].set(av3[2]);
////		//av3New[3].set(av3[3]);
////		//if (bBackfaceNormalFlip) flipBackfaceNormals(av3New);
////		//int c0 = lighting.shade(ap3[0], av3New[0], mp);
////		//int c1 = lighting.shade(ap3[3], av3New[1], mp);
////		//int c2 = lighting.shade(ap3[6], av3New[2], mp);
////		//int c3 = lighting.shade(ap3[9], av3New[3], mp);
////		
////		//drawLine3D(ap3[0], ap3[3]);
////		//drawLine3D(ap3[3], ap3[6]);
////		//drawLine3D(ap3[6], ap3[9]);
////		//drawLine3D(ap3[9], ap3[0]);
////		
////		//for (int i = 0; i < 12; i++) {
////		//	drawLine3D(ap3[i], ap3[(i + 1) % 12]);
////		//	drawPoint3D(ap3[i],2);
////		//}
////		
////		//Point3f p = new Point3f();
////		//p.set(ap3[0]);
////		//v3a.set(av3[0]);
////		//v3a.scale(30f);
////		//p.add(v3a);
////		//drawLine3D(ap3[0],p);
////		//
////		//p.set(ap3[3]);
////		//v3a.set(av3[1]);
////		//v3a.scale(30f);
////		//p.add(v3a);
////		//drawLine3D(ap3[3],p);
////		//
////		//p.set(ap3[6]);
////		//v3a.set(av3[2]);
////		//v3a.scale(30f);
////		//p.add(v3a);
////		//drawLine3D(ap3[6],p);
////		//
////		//p.set(ap3[9]);
////		//v3a.set(av3[3]);
////		//v3a.scale(30f);
////		//p.add(v3a);
////		//drawLine3D(ap3[9],p);
////		
////		//int ca = 0xFF777777;
////		//int cb = 0xFF999999;
////		//
////		//draw3DTriangleGourad(ap3[0], ap3[3], ap3[9], ca, ca, ca);
////		//draw3DTriangleGourad(ap3[6], ap3[9], ap3[3], cb, cb, cb);
////		
////		if (mp.isOpaque()) {
////			draw3DTriangleGourad(ap3[9], ap3[3], ap3[0], ac[3], ac[1], ac[0]);
////			draw3DTriangleGourad(ap3[3], ap3[9], ap3[6], ac[1], ac[3], ac[2]);
////		} else {
////			int transparency = (int) (Math.min(1f,mp.transmit + mp.filter) * 255f);
////			draw3DTriangleGouradTransparent(ap3[9], ap3[3], ap3[0], ac[3], ac[1], ac[0], transparency);
////			draw3DTriangleGouradTransparent(ap3[3], ap3[9], ap3[6], ac[1], ac[3], ac[2], transparency);
////		}
////	}
//}
