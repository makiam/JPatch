/*
 * $Id: Patch.java,v 1.8 2006/02/01 21:11:28 sascha_l Exp $
 *
 * Copyright (c) 2004 Sascha Ledinsky
 *
 * This file is part of JPatch.
 * 
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package jpatch.entity;

import javax.vecmath.*;
import jpatch.boundary.*;
import jpatch.auxilary.*;

/**
 * A Patch. Currently 3-, 4- and 5-point patches are supported
 *
 * @author     Sascha Ledinsky
 * @version    $Revision: 1.8 $
 * @see		jpatch.entity.Curve
 * @see		jpatch.entity.ControlPoint
 */
public final class Patch {
	private ControlPoint[] acpPoint;
	//private boolean bReTriangulize = true;
	//private TriangulizedPatch triangulizedPatch;
	private JPatchMaterial material;
	private boolean bDynamic = false;
	private boolean bValid = true;
	//private Point3f[] ap3ReferenceCoonsPatch;
	
	private static final Point3f[] ap3CoonsPatch9 = new Point3f[9];
	private static final Point3f[] ap3CoonsPatch12 = new Point3f[12];
	private static final Point3f[] ap3CoonsPatch15 = new Point3f[15];
	private static final Point3f[][] aap3Patches1 = new Point3f[1][16];
	static {
		for (int i = 0; i < 9; ap3CoonsPatch9[i++] = new Point3f());
		for (int i = 0; i < 12; ap3CoonsPatch12[i++] = new Point3f());
		for (int i = 0; i < 15; ap3CoonsPatch15[i++] = new Point3f());
		for (int i = 0; i < 16; aap3Patches1[0][i++] = new Point3f());
	}
	
	public Patch(ControlPoint[] controlPoints) {
		acpPoint = controlPoints;
		if (MainFrame.getInstance().getModel() != null) material = MainFrame.getInstance().getModel().getMaterial(0);
		//setGroup();
		//reset();
		//for (int i = 0; i < acpPoint.length; i++) {
		//	acpPoint[i].addPatch(this);
		//}
	}
	
	public void flip() {
		ControlPoint[] old = acpPoint;
		acpPoint = new ControlPoint[old.length];
		for (int i = 0, n = old.length - 1; i <= n; acpPoint[i] = old[n - i++]);
	}
	
	public final boolean contains(ControlPoint cp) {
		for (int n = 0; n < acpPoint.length; n++) {
			if (cp == acpPoint[n]) return true;
		}
		return false;
	}
	
	public boolean isHidden() {
		for (int i = 0; i < acpPoint.length; i++) {
			if (acpPoint[i].isHidden()) {
				return true;
			}
		}
		return false;
	}
	
	public ControlPoint[] getControlPoints() {
		return acpPoint;
	}
	
	public String toString() {
		String s = "Patch@" + hashCode() + " " + material + " ";
		for (int n = 0; n < acpPoint.length; n++) {
			s += acpPoint[n].toString() + " ";
		}
		return s;
	}
	
	public final boolean equals(Object o) {
		ControlPoint[] acp = ((Patch) o).acpPoint;
		/*
		 * check for equal size
		 */
		if (acpPoint.length == acp.length) {		
			int l = acpPoint.length;
			for (int n = 0; n < l; n++) {			
				if (acpPoint[0] == acp[n]) {
					if (acpPoint[1] == acp[(n + 1) % l]) {	
						for (int m = 2; m < l; m++) {
							if (acpPoint[m] != acp[(n + m) % l]) {
								return false;
							}
						}
						return true;
					} else if (acpPoint[1] == acp[(n + l - 1) % l]) {
						for (int m = 2; m < l; m++) {
							if (acpPoint[m] != acp[(n + l - m) % l]) {
								return false;
							}
						}
						return true;
					} else return false;
				}
			}
		}
		return false;
	}

	public int hashCode() {
		int hash = 0;
		for (int i = 0; i < acpPoint.length; i++)
			hash += acpPoint[i].hashCode();
		return hash;
	}
	
//	public final void check(PointSelection ps) {
//		bDynamic = false;
//		loop:
//		for (int i = 0; i < acpPoint.length; i++) {
//			if (ps.contains(acpPoint[i].getHead())) bDynamic = true;
//			if (acpPoint[i].getPrev() != null && ps.contains(acpPoint[i].getPrev().getHead())) bDynamic = true;
//			if (acpPoint[i].getNext() != null && ps.contains(acpPoint[i].getNext().getHead())) bDynamic = true;
//			if (bDynamic) break loop;
//		}
//	}
	
	public final boolean isSelected(Selection selection) {
		for (int i = 0; i < acpPoint.length; i++) {
			if (!selection.contains(acpPoint[i].getHead())) {
				if (!acpPoint[i].isTargetHook() && !acpPoint[i].isChildHook()) return false;
			}
		}
		return true;
	}
	
	public final boolean isValid() {
		return bValid;
	}
	
	public final void setValid(boolean valid) {
		bValid = valid;
	}
	
	public final boolean isDynamic() {
		return bDynamic;
	}
	/*
	public final void reset() {
		triangulizedPatch = MainFrame.getInstance().getDisplayFactory().createTriangulizedPatch(this);
	}
	
	public final TriangulizedPatch getTriangulizedPatch() {
		return triangulizedPatch;
	}
	*/
	
	
	public final int getType() {
		int l = acpPoint.length;
		if (l == 6 || l == 8 || l == 10) {
			return l / 2;
		} else {
			return -1;
		}
	}
	
	public final int getSize() {
		switch(getType()) {
			case 3:
				return 1;
			case 4:
				return 1;
			case 5:
				return 5;
		}
		return -1;
	}
	
	
	/**
	 * returns StringBuffer containing an XML representation of the curve, used to save
	 * models in XML format
	 *
	 * @return A StringBuffer containing an XML representation of this curve
	 */
	public StringBuffer xml(String prefix) {
		StringBuffer sb = new StringBuffer();
		int materialNumber = (material != null) ? material.getXmlNumber() : -1;
		sb.append(prefix).append("<patch material=\"" + materialNumber + "\">").append("\n");
		sb.append(prefix).append("\t<points>");
		//int size = getType();
		//int p = 0;
		for (int i = 0; i < acpPoint.length; i++) {
			sb.append(acpPoint[i].getXmlNumber());
			if (i < acpPoint.length - 1) {
				sb.append(",");
			}
		}
		sb.append("</points>").append("\n");
		sb.append(prefix).append("</patch>").append("\n");
		return sb;
	}
	
	public Point3f[] coonsPatch() {
		Point3f[] ap3CoonsPatch = null;
		if (acpPoint.length == 6) {
			ap3CoonsPatch = new Point3f[9];
		}
		if (acpPoint.length == 8) {
			ap3CoonsPatch = new Point3f[12];
		}
		if (acpPoint.length == 10) {
			ap3CoonsPatch = new Point3f[15];
		}
		int coon = 0;
		int p = 0;
		while (p < acpPoint.length) {
			ap3CoonsPatch[coon++] = new Point3f(acpPoint[p].getPosition());
			if (acpPoint[p].getNext() == acpPoint[p + 1]) {
				ap3CoonsPatch[coon++] = new Point3f(acpPoint[p].getOutTangent());
				ap3CoonsPatch[coon++] = new Point3f(acpPoint[++p].getInTangent());
			} else {
				ap3CoonsPatch[coon++] = new Point3f(acpPoint[p].getInTangent());
				ap3CoonsPatch[coon++] = new Point3f(acpPoint[++p].getOutTangent());
			}
			p++;
		}
		return ap3CoonsPatch;
	}
	
	public Point3f[] referenceCoonsPatch() {
		Point3f[] ap3CoonsPatch = null;
		if (acpPoint.length == 6) {
			ap3CoonsPatch = new Point3f[9];
		}
		if (acpPoint.length == 8) {
			ap3CoonsPatch = new Point3f[12];
		}
		if (acpPoint.length == 10) {
			ap3CoonsPatch = new Point3f[15];
		}
		int coon = 0;
		int p = 0;
		while (p < acpPoint.length) {
			ap3CoonsPatch[coon++] = new Point3f(acpPoint[p].getReferencePosition());
			if (acpPoint[p].getNext() == acpPoint[p + 1]) {
				ap3CoonsPatch[coon++] = new Point3f(acpPoint[p].getReferenceOutTangent());
				ap3CoonsPatch[coon++] = new Point3f(acpPoint[++p].getReferenceInTangent());
			} else {
				ap3CoonsPatch[coon++] = new Point3f(acpPoint[p].getReferenceInTangent());
				ap3CoonsPatch[coon++] = new Point3f(acpPoint[++p].getReferenceOutTangent());
			}
			p++;
		}
		return ap3CoonsPatch;
	}
	
	public static void shift(Point3f[] ap3CoonsPatch, int s) {
		//System.out.println("shift " + s);
		int n = ap3CoonsPatch.length;
		Point3f[] helper = new Point3f[n];
		for (int i = 0; i < n; i++) {
			helper[i] = ap3CoonsPatch[(i + s + n) % n];
		}
		for (int i = 0; i < n; i++) {
			ap3CoonsPatch[i] = helper[i];
		}
	}
	
	public void setMaterial(JPatchMaterial material) {
		this.material = material;
	}
	
	public JPatchMaterial getMaterial() {
		return material;
	}

	//public void setReferenceGeometry() {
	//	ap3ReferenceCoonsPatch = coonsPatch();
	//	System.out.println("setReferenceGeometry() " + this);
	//}
	//
	//public Point3f[][] referenceBicubicPatches() {
	//	System.out.println("referenceBicubicPatches() " + this);
	//	return bicubicPatches(ap3ReferenceCoonsPatch);
	//}
	
	public Point3f[][] bicubicPatches() {
		return bicubicPatches(coonsPatch());
	}
	
	public Point3f[][] bicubicReferencePatches() {
		return bicubicPatches(referenceCoonsPatch());
	}
	
	private Point3f[][] bicubicPatches(Point3f[] ap3CoonsPatch) {
		Point3f[][] aap3Patches = null;
		switch(acpPoint.length) {
			
			case 10:
				Point3f A = Functions.parallelogram(ap3CoonsPatch[0],ap3CoonsPatch[1],ap3CoonsPatch[14]);
				Point3f B = Functions.parallelogram(ap3CoonsPatch[3],ap3CoonsPatch[4],ap3CoonsPatch[2]);
				Point3f C = Functions.parallelogram(ap3CoonsPatch[6],ap3CoonsPatch[7],ap3CoonsPatch[5]);
				Point3f D = Functions.parallelogram(ap3CoonsPatch[9],ap3CoonsPatch[10],ap3CoonsPatch[8]);
				Point3f E = Functions.parallelogram(ap3CoonsPatch[12],ap3CoonsPatch[13],ap3CoonsPatch[11]);
				
				Point3f F = Functions.average(A,ap3CoonsPatch[0],ap3CoonsPatch[1],ap3CoonsPatch[14]);
				Point3f H = Functions.average(B,ap3CoonsPatch[3],ap3CoonsPatch[4],ap3CoonsPatch[2]);
				Point3f J = Functions.average(C,ap3CoonsPatch[6],ap3CoonsPatch[7],ap3CoonsPatch[5]);
				Point3f L = Functions.average(D,ap3CoonsPatch[9],ap3CoonsPatch[10],ap3CoonsPatch[8]);
				Point3f N = Functions.average(E,ap3CoonsPatch[12],ap3CoonsPatch[13],ap3CoonsPatch[11]);
				
				Point3f G = Functions.average(A,B,ap3CoonsPatch[1],ap3CoonsPatch[2]);
				Point3f I = Functions.average(B,C,ap3CoonsPatch[4],ap3CoonsPatch[5]);
				Point3f K = Functions.average(C,D,ap3CoonsPatch[7],ap3CoonsPatch[8]);
				Point3f M = Functions.average(D,E,ap3CoonsPatch[10],ap3CoonsPatch[11]);
				Point3f O = Functions.average(E,A,ap3CoonsPatch[13],ap3CoonsPatch[14]);
				
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
				
				//Vector3f v3_n0 = new Vector3f();
				//Vector3f v3_n1 = new Vector3f();
				//Vector3f v3_n2 = new Vector3f();
				//Vector3f v3_n3 = new Vector3f();
				//Vector3f v3_n4 = new Vector3f();
				//
				//v3_n0.cross(Functions.vector(Center,P),Functions.vector(Center,Q));
				//v3_n1.cross(Functions.vector(Center,Q),Functions.vector(Center,R));
				//v3_n2.cross(Functions.vector(Center,R),Functions.vector(Center,S));
				//v3_n3.cross(Functions.vector(Center,S),Functions.vector(Center,T));
				//v3_n4.cross(Functions.vector(Center,T),Functions.vector(Center,P));
				//
				//Vector3f v3Normal = new Vector3f(v3_n0);
				//v3Normal.add(v3_n1);
				//v3Normal.add(v3_n2);
				//v3Normal.add(v3_n3);
				//v3Normal.add(v3_n4);
				//v3Normal.normalize();
				//
				//Plane plane = new Plane(Center,v3Normal);
				//
				//V.set(plane.projectedPoint(V));
				//W.set(plane.projectedPoint(W));
				//X.set(plane.projectedPoint(X));
				//Y.set(plane.projectedPoint(Y));
				//Z.set(plane.projectedPoint(Z));
				
				Point3f[][] aap3Boundary = new Point3f[5][7];
				
				aap3Boundary[0] = Bezier.deCasteljau(ap3CoonsPatch[0],ap3CoonsPatch[1],ap3CoonsPatch[2],ap3CoonsPatch[3],0.5f);
				aap3Boundary[1] = Bezier.deCasteljau(ap3CoonsPatch[3],ap3CoonsPatch[4],ap3CoonsPatch[5],ap3CoonsPatch[6],0.5f);
				aap3Boundary[2] = Bezier.deCasteljau(ap3CoonsPatch[6],ap3CoonsPatch[7],ap3CoonsPatch[8],ap3CoonsPatch[9],0.5f);
				aap3Boundary[3] = Bezier.deCasteljau(ap3CoonsPatch[9],ap3CoonsPatch[10],ap3CoonsPatch[11],ap3CoonsPatch[12],0.5f);
				aap3Boundary[4] = Bezier.deCasteljau(ap3CoonsPatch[12],ap3CoonsPatch[13],ap3CoonsPatch[14],ap3CoonsPatch[0],0.5f);
				
				aap3Patches = new Point3f[5][16];
				
				aap3Patches[0][0] = new Point3f(aap3Boundary[0][0]);
				aap3Patches[0][1] = new Point3f(aap3Boundary[0][1]);
				aap3Patches[0][2] = new Point3f(aap3Boundary[0][2]);
				aap3Patches[0][3] = new Point3f(aap3Boundary[0][3]);
				aap3Patches[0][4] = new Point3f(aap3Boundary[4][5]);
				aap3Patches[0][7] = new Point3f(aap3Boundary[0][3]);
				aap3Patches[0][7].add(Functions.average(Functions.vector(ap3CoonsPatch[3],ap3CoonsPatch[4]),Functions.vector(ap3CoonsPatch[0],ap3CoonsPatch[14])));
				aap3Patches[0][8] = new Point3f(aap3Boundary[4][4]);
				aap3Patches[0][11] = new Point3f(W);
				aap3Patches[0][12] = new Point3f(aap3Boundary[4][3]);
				aap3Patches[0][13] = new Point3f(aap3Boundary[4][3]);
				aap3Patches[0][13].add(Functions.average(Functions.vector(ap3CoonsPatch[0],ap3CoonsPatch[1]),Functions.vector(ap3CoonsPatch[12],ap3CoonsPatch[11])));
				aap3Patches[0][14] = new Point3f(V);
				aap3Patches[0][15] = new Point3f(Center);
				BezierPatch.computeInnerControlPoints(aap3Patches[0]);
				aap3Patches[0][10].set(P);
				//aap3Patches[0][5].set(F);
				
				aap3Patches[1][0] = new Point3f(aap3Boundary[1][0]);
				aap3Patches[1][1] = new Point3f(aap3Boundary[1][1]);
				aap3Patches[1][2] = new Point3f(aap3Boundary[1][2]);
				aap3Patches[1][3] = new Point3f(aap3Boundary[1][3]);
				aap3Patches[1][4] = new Point3f(aap3Boundary[0][5]);
				aap3Patches[1][7] = new Point3f(aap3Boundary[1][3]);
				aap3Patches[1][7].add(Functions.average(Functions.vector(ap3CoonsPatch[6],ap3CoonsPatch[7]),Functions.vector(ap3CoonsPatch[3],ap3CoonsPatch[2])));
				aap3Patches[1][8] = new Point3f(aap3Boundary[0][4]);
				aap3Patches[1][11] = new Point3f(X);
				aap3Patches[1][12] = new Point3f(aap3Boundary[0][3]);
				aap3Patches[1][13] = new Point3f(aap3Boundary[0][3]);
				aap3Patches[1][13].add(Functions.average(Functions.vector(ap3CoonsPatch[3],ap3CoonsPatch[4]),Functions.vector(ap3CoonsPatch[0],ap3CoonsPatch[14])));
				aap3Patches[1][14] = new Point3f(W);
				aap3Patches[1][15] = new Point3f(Center);
				BezierPatch.computeInnerControlPoints(aap3Patches[1]);
				aap3Patches[1][10].set(Q);
				//aap3Patches[1][5].set(H);
				
				aap3Patches[2][0] = new Point3f(aap3Boundary[2][0]);
				aap3Patches[2][1] = new Point3f(aap3Boundary[2][1]);
				aap3Patches[2][2] = new Point3f(aap3Boundary[2][2]);
				aap3Patches[2][3] = new Point3f(aap3Boundary[2][3]);
				aap3Patches[2][4] = new Point3f(aap3Boundary[1][5]);
				aap3Patches[2][7] = new Point3f(aap3Boundary[2][3]);
				aap3Patches[2][7].add(Functions.average(Functions.vector(ap3CoonsPatch[9],ap3CoonsPatch[10]),Functions.vector(ap3CoonsPatch[6],ap3CoonsPatch[5])));
				aap3Patches[2][8] = new Point3f(aap3Boundary[1][4]);
				aap3Patches[2][11] = new Point3f(Y);
				aap3Patches[2][12] = new Point3f(aap3Boundary[1][3]);
				aap3Patches[2][13] = new Point3f(aap3Boundary[1][3]);
				aap3Patches[2][13].add(Functions.average(Functions.vector(ap3CoonsPatch[6],ap3CoonsPatch[7]),Functions.vector(ap3CoonsPatch[3],ap3CoonsPatch[2])));
				aap3Patches[2][14] = new Point3f(X);
				aap3Patches[2][15] = new Point3f(Center);
				BezierPatch.computeInnerControlPoints(aap3Patches[2]);
				aap3Patches[2][10].set(R);
				//aap3Patches[2][5].set(J);
				
				aap3Patches[3][0] = new Point3f(aap3Boundary[3][0]);
				aap3Patches[3][1] = new Point3f(aap3Boundary[3][1]);
				aap3Patches[3][2] = new Point3f(aap3Boundary[3][2]);
				aap3Patches[3][3] = new Point3f(aap3Boundary[3][3]);
				aap3Patches[3][4] = new Point3f(aap3Boundary[2][5]);
				aap3Patches[3][7] = new Point3f(aap3Boundary[3][3]);
				aap3Patches[3][7].add(Functions.average(Functions.vector(ap3CoonsPatch[9],ap3CoonsPatch[8]),Functions.vector(ap3CoonsPatch[12],ap3CoonsPatch[13])));
				aap3Patches[3][8] = new Point3f(aap3Boundary[2][4]);
				aap3Patches[3][11] = new Point3f(Z);
				aap3Patches[3][12] = new Point3f(aap3Boundary[2][3]);
				aap3Patches[3][13] = new Point3f(aap3Boundary[2][3]);
				aap3Patches[3][13].add(Functions.average(Functions.vector(ap3CoonsPatch[9],ap3CoonsPatch[10]),Functions.vector(ap3CoonsPatch[6],ap3CoonsPatch[5])));
				aap3Patches[3][14] = new Point3f(Y);
				aap3Patches[3][15] = new Point3f(Center);
				BezierPatch.computeInnerControlPoints(aap3Patches[3]);
				aap3Patches[3][10].set(S);
				//aap3Patches[3][5].set(L);
				
				aap3Patches[4][0] = new Point3f(aap3Boundary[4][0]);
				aap3Patches[4][1] = new Point3f(aap3Boundary[4][1]);
				aap3Patches[4][2] = new Point3f(aap3Boundary[4][2]);
				aap3Patches[4][3] = new Point3f(aap3Boundary[4][3]);
				aap3Patches[4][4] = new Point3f(aap3Boundary[3][5]);
				aap3Patches[4][7] = new Point3f(aap3Boundary[4][3]);
				aap3Patches[4][7].add(Functions.average(Functions.vector(ap3CoonsPatch[12],ap3CoonsPatch[11]),Functions.vector(ap3CoonsPatch[0],ap3CoonsPatch[1])));
				aap3Patches[4][8] = new Point3f(aap3Boundary[3][4]);
				aap3Patches[4][11] = new Point3f(V);
				aap3Patches[4][12] = new Point3f(aap3Boundary[3][3]);
				aap3Patches[4][13] = new Point3f(aap3Boundary[3][3]);
				aap3Patches[4][13].add(Functions.average(Functions.vector(ap3CoonsPatch[9],ap3CoonsPatch[8]),Functions.vector(ap3CoonsPatch[12],ap3CoonsPatch[13])));
				aap3Patches[4][14] = new Point3f(Z);
				aap3Patches[4][15] = new Point3f(Center);
				BezierPatch.computeInnerControlPoints(aap3Patches[4]);
				aap3Patches[4][10].set(T);
				//aap3Patches[4][5].set(N);
				
				break;
				
			//case 10 - 1:
			//	
			//
			//
			//	
			//	
			//	
			//	
			//	aap3Patches = new Point3f[5][16];
			//	Point3f[] ap3 = new Point3f[41];
			//	Point3f p3_helper1 = new Point3f();
			//	Point3f p3_helper2 = new Point3f();
			//	Point3f[] ap3Helper = new Point3f[7];
			//	
			//	Bezier.deCasteljau(ap3CoonsPatch[0],ap3CoonsPatch[1],ap3CoonsPatch[2],ap3CoonsPatch[3],0.5f,ap3,0);
			//	Bezier.deCasteljau(ap3CoonsPatch[3],ap3CoonsPatch[4],ap3CoonsPatch[5],ap3CoonsPatch[6],0.5f,ap3,6);
			//	Bezier.deCasteljau(ap3CoonsPatch[6],ap3CoonsPatch[7],ap3CoonsPatch[8],ap3CoonsPatch[9],0.5f,ap3,12);
			//	Bezier.deCasteljau(ap3CoonsPatch[9],ap3CoonsPatch[10],ap3CoonsPatch[11],ap3CoonsPatch[12],0.5f,ap3,18);
			//	Bezier.deCasteljau(ap3CoonsPatch[12],ap3CoonsPatch[13],ap3CoonsPatch[14],ap3CoonsPatch[0],0.5f,ap3,24);
			//	
			//	p3_helper1.interpolate(ap3CoonsPatch[2],ap3CoonsPatch[13],0.5f);
			//	p3_helper2.interpolate(ap3CoonsPatch[4],ap3CoonsPatch[11],0.5f);
			//	ap3Helper = Bezier.deCasteljau(ap3[0],p3_helper1,p3_helper2,ap3[15],3f/5f);
			//	ap3[30] = ap3Helper[3];
			//	ap3[31] = ap3Helper[4];
			//	//ap3[32] = ap3Helper[5];
			//	
			//	p3_helper1.interpolate(ap3CoonsPatch[1],ap3CoonsPatch[5],0.5f);
			//	p3_helper2.interpolate(ap3CoonsPatch[14],ap3CoonsPatch[7],0.5f);
			//	ap3Helper = Bezier.deCasteljau(ap3[6],p3_helper1,p3_helper2,ap3[21],3f/5f);
			//	ap3[30].add(ap3Helper[3]);
			//	ap3[33] = ap3Helper[4];
			//	//ap3[34] = ap3Helper[5];
			//	
			//	p3_helper1.interpolate(ap3CoonsPatch[4],ap3CoonsPatch[8],0.5f);
			//	p3_helper2.interpolate(ap3CoonsPatch[2],ap3CoonsPatch[10],0.5f);
			//	ap3Helper = Bezier.deCasteljau(ap3[12],p3_helper1,p3_helper2,ap3[27],3f/5f);
			//	ap3[30].add(ap3Helper[3]);
			//	ap3[35] = ap3Helper[4];
			//	//ap3[36] = ap3Helper[5];
			//	
			//	p3_helper1.interpolate(ap3CoonsPatch[7],ap3CoonsPatch[11],0.5f);
			//	p3_helper2.interpolate(ap3CoonsPatch[5],ap3CoonsPatch[13],0.5f);
			//	ap3Helper = Bezier.deCasteljau(ap3[18],p3_helper1,p3_helper2,ap3[3],3f/5f);
			//	ap3[30].add(ap3Helper[3]);
			//	ap3[37] = ap3Helper[4];
			//	//ap3[38] = ap3Helper[5];
			//	
			//	p3_helper1.interpolate(ap3CoonsPatch[10],ap3CoonsPatch[14],0.5f);
			//	p3_helper2.interpolate(ap3CoonsPatch[1],ap3CoonsPatch[8],0.5f);
			//	ap3Helper = Bezier.deCasteljau(ap3[24],p3_helper1,p3_helper2,ap3[9],3f/5f);
			//	ap3[30].add(ap3Helper[3]);
			//	ap3[39] = ap3Helper[4];
			//	//ap3[40] = ap3Helper[5];
			//	
			//	
			//	ap3[30].scale(1f/5f);
			//	
			//	Vector3f v3_n0 = new Vector3f();
			//	Vector3f v3_n1 = new Vector3f();
			//	Vector3f v3_n2 = new Vector3f();
			//	Vector3f v3_n3 = new Vector3f();
			//	Vector3f v3_n4 = new Vector3f();
			//	
			//	v3_n0.cross(Functions.vector(ap3[30],ap3[35]),Functions.vector(ap3[30],ap3[37]));
			//	v3_n1.cross(Functions.vector(ap3[30],ap3[37]),Functions.vector(ap3[30],ap3[39]));
			//	v3_n2.cross(Functions.vector(ap3[30],ap3[39]),Functions.vector(ap3[30],ap3[31]));
			//	v3_n3.cross(Functions.vector(ap3[30],ap3[31]),Functions.vector(ap3[30],ap3[33]));
			//	v3_n4.cross(Functions.vector(ap3[30],ap3[33]),Functions.vector(ap3[30],ap3[35]));
			//	
			//	Vector3f v3Normal = new Vector3f(v3_n0);
			//	v3Normal.add(v3_n1);
			//	v3Normal.add(v3_n2);
			//	v3Normal.add(v3_n3);
			//	v3Normal.add(v3_n4);
			//	
			//	
			//	/* test */
			//	//Vector3f v3x = new Vector3f(v3Normal);
			//	//v3x.scale(0.1f);
			//	//ap3[30].add(v3x);
			//	/* end test */
			//	
			//	v3Normal.normalize();
			//	
			//	Plane plane = new Plane(ap3[30],v3Normal);
			//	
			//	ap3[31].set(plane.projectedPoint(ap3[31]));
			//	ap3[33].set(plane.projectedPoint(ap3[33]));
			//	ap3[35].set(plane.projectedPoint(ap3[35]));
			//	ap3[37].set(plane.projectedPoint(ap3[37]));
			//	ap3[39].set(plane.projectedPoint(ap3[39]));
			//	
			//	Vector3f v3_3 = new Vector3f();
			//	v3_3.interpolate(Functions.vector(ap3[0],ap3[29]),Functions.vector(ap3[6],ap3[7]),0.5f);
			//	Vector3f v3_9 = new Vector3f();
			//	v3_9.interpolate(Functions.vector(ap3[6],ap3[5]),Functions.vector(ap3[12],ap3[13]),0.5f);
			//	Vector3f v3_15 = new Vector3f();
			//	v3_15.interpolate(Functions.vector(ap3[12],ap3[11]),Functions.vector(ap3[18],ap3[19]),0.5f);
			//	Vector3f v3_21 = new Vector3f();
			//	v3_21.interpolate(Functions.vector(ap3[18],ap3[17]),Functions.vector(ap3[24],ap3[25]),0.5f);
			//	Vector3f v3_27 = new Vector3f();
			//	v3_27.interpolate(Functions.vector(ap3[24],ap3[23]),Functions.vector(ap3[0],ap3[1]),0.5f);
			//	
			//	ap3[38] = new Point3f(ap3[3]);
			//	ap3[38].add(v3_3);
			//	ap3[40] = new Point3f(ap3[9]);
			//	ap3[40].add(v3_9);
			//	ap3[32] = new Point3f(ap3[15]);
			//	ap3[32].add(v3_15);
			//	ap3[34] = new Point3f(ap3[21]);
			//	ap3[34].add(v3_21);
			//	ap3[36] = new Point3f(ap3[27]);
			//	ap3[36].add(v3_27);
			//	
			//	
			//	aap3Patches[0][0] = new Point3f(ap3[0]);
			//	aap3Patches[0][1] = new Point3f(ap3[1]);
			//	aap3Patches[0][2] = new Point3f(ap3[2]);
			//	aap3Patches[0][3] = new Point3f(ap3[3]);
			//	aap3Patches[0][7] = new Point3f(ap3[38]);
			//	aap3Patches[0][11] = new Point3f(ap3[37]);
			//	aap3Patches[0][15] = new Point3f(ap3[30]);
			//	aap3Patches[0][14] = new Point3f(ap3[35]);
			//	aap3Patches[0][13] = new Point3f(ap3[36]);
			//	aap3Patches[0][12] = new Point3f(ap3[27]);
			//	aap3Patches[0][8] = new Point3f(ap3[28]);
			//	aap3Patches[0][4] = new Point3f(ap3[29]);
			//	
			//	aap3Patches[1][0] = new Point3f(ap3[6]);
			//	aap3Patches[1][1] = new Point3f(ap3[7]);
			//	aap3Patches[1][2] = new Point3f(ap3[8]);
			//	aap3Patches[1][3] = new Point3f(ap3[9]);
			//	aap3Patches[1][7] = new Point3f(ap3[40]);
			//	aap3Patches[1][11] = new Point3f(ap3[39]);
			//	aap3Patches[1][15] = new Point3f(ap3[30]);
			//	aap3Patches[1][14] = new Point3f(ap3[37]);
			//	aap3Patches[1][13] = new Point3f(ap3[38]);
			//	aap3Patches[1][12] = new Point3f(ap3[3]);
			//	aap3Patches[1][8] = new Point3f(ap3[4]);
			//	aap3Patches[1][4] = new Point3f(ap3[5]);
			//	
			//	aap3Patches[2][0] = new Point3f(ap3[12]);
			//	aap3Patches[2][1] = new Point3f(ap3[13]);
			//	aap3Patches[2][2] = new Point3f(ap3[14]);
			//	aap3Patches[2][3] = new Point3f(ap3[15]);
			//	aap3Patches[2][7] = new Point3f(ap3[32]);
			//	aap3Patches[2][11] = new Point3f(ap3[31]);
			//	aap3Patches[2][15] = new Point3f(ap3[30]);
			//	aap3Patches[2][14] = new Point3f(ap3[39]);
			//	aap3Patches[2][13] = new Point3f(ap3[40]);
			//	aap3Patches[2][12] = new Point3f(ap3[9]);
			//	aap3Patches[2][8] = new Point3f(ap3[10]);
			//	aap3Patches[2][4] = new Point3f(ap3[11]);
			//	
			//	aap3Patches[3][0] = new Point3f(ap3[18]);
			//	aap3Patches[3][1] = new Point3f(ap3[19]);
			//	aap3Patches[3][2] = new Point3f(ap3[20]);
			//	aap3Patches[3][3] = new Point3f(ap3[21]);
			//	aap3Patches[3][7] = new Point3f(ap3[34]);
			//	aap3Patches[3][11] = new Point3f(ap3[33]);
			//	aap3Patches[3][15] = new Point3f(ap3[30]);
			//	aap3Patches[3][14] = new Point3f(ap3[31]);
			//	aap3Patches[3][13] = new Point3f(ap3[32]);
			//	aap3Patches[3][12] = new Point3f(ap3[15]);
			//	aap3Patches[3][8] = new Point3f(ap3[16]);
			//	aap3Patches[3][4] = new Point3f(ap3[17]);
			//	
			//	aap3Patches[4][0] = new Point3f(ap3[24]);
			//	aap3Patches[4][1] = new Point3f(ap3[25]);
			//	aap3Patches[4][2] = new Point3f(ap3[26]);
			//	aap3Patches[4][3] = new Point3f(ap3[27]);
			//	aap3Patches[4][7] = new Point3f(ap3[36]);
			//	aap3Patches[4][11] = new Point3f(ap3[35]);
			//	aap3Patches[4][15] = new Point3f(ap3[30]);
			//	aap3Patches[4][14] = new Point3f(ap3[33]);
			//	aap3Patches[4][13] = new Point3f(ap3[34]);
			//	aap3Patches[4][12] = new Point3f(ap3[21]);
			//	aap3Patches[4][8] = new Point3f(ap3[22]);
			//	aap3Patches[4][4] = new Point3f(ap3[23]);
			//	
			//	BezierPatch.computeInnerControlPoints(aap3Patches[0]);
			//	BezierPatch.computeInnerControlPoints(aap3Patches[1]);
			//	BezierPatch.computeInnerControlPoints(aap3Patches[2]);
			//	BezierPatch.computeInnerControlPoints(aap3Patches[3]);
			//	BezierPatch.computeInnerControlPoints(aap3Patches[4]);
			//	
			//	/*
			//	aap3Patches[0][0] = ap3CoonsPatch[0];
			//	aap3Patches[0][1] = ap3CoonsPatch[1];
			//	aap3Patches[0][2] = ap3CoonsPatch[2];
			//	aap3Patches[0][3] = ap3CoonsPatch[3];
			//	aap3Patches[0][7] = ap3CoonsPatch[4];
			//	aap3Patches[0][11] = ap3CoonsPatch[5];
			//	aap3Patches[0][15] = ap3CoonsPatch[6];
			//	aap3Patches[0][14] = ap3CoonsPatch[7];
			//	aap3Patches[0][13] = ap3CoonsPatch[8];
			//	aap3Patches[0][12] = ap3CoonsPatch[9];
			//	aap3Patches[0][8] = ap3CoonsPatch[10];
			//	aap3Patches[0][4] = ap3CoonsPatch[11];
			//	/*
			//	aap3Patches[0][5] = parallelogram(aap3Patches[0][0],aap3Patches[0][1],aap3Patches[0][4]);
			//	aap3Patches[0][6] = parallelogram(aap3Patches[0][3],aap3Patches[0][2],aap3Patches[0][7]);
			//	aap3Patches[0][9] = parallelogram(aap3Patches[0][12],aap3Patches[0][8],aap3Patches[0][13]);
			//	aap3Patches[0][10] = parallelogram(aap3Patches[0][15],aap3Patches[0][11],aap3Patches[0][14]);
			//	
			//	BezierPatch.computeInnerControlPoints(aap3Patches[0]);
			//	*/
			//	/*
			//	Point3f[] ap3Helper;
			//	Point3f p3_1 = new Point3f();
			//	Point3f p3_2 = new Point3f();
			//	p3_1.interpolate(ap3CoonsPatch[2],ap3CoonsPatch[13],0.5f);
			//	p3_2.interpolate(ap3CoonsPatch[4],ap3CoonsPatch[11],0.5f);
			//	ap3Helper = Bezier.deCasteljau(ap3CoonsPatch[9],ap3CoonsPatch[8],ap3CoonsPatch[7],ap3CoonsPatch[6],0.5f);
			//	
			//	aap3Patches[0][0] = ap3CoonsPatch[12];
			//	aap3Patches[0][1] = ap3CoonsPatch[13];
			//	aap3Patches[0][2] = ap3CoonsPatch[14];
			//	aap3Patches[0][3] = ap3CoonsPatch[0];
			//	aap3Patches[0][7] = new Point3f(p3_1);
			//	aap3Patches[0][11] = new Point3f(p3_2);
			//	aap3Patches[0][15] = new Point3f(ap3Helper[3]);
			//	aap3Patches[0][14] = ap3Helper[2];
			//	aap3Patches[0][13] = ap3Helper[1];
			//	aap3Patches[0][12] = ap3Helper[0];
			//	aap3Patches[0][8] = ap3CoonsPatch[10];
			//	aap3Patches[0][4] = ap3CoonsPatch[11];
			//	BezierPatch.computeInnerControlPoints(aap3Patches[0]);
			//	
			//	aap3Patches[1][0] = new Point3f(ap3CoonsPatch[0]);
			//	aap3Patches[1][1] = ap3CoonsPatch[1];
			//	aap3Patches[1][2] = ap3CoonsPatch[2];
			//	aap3Patches[1][3] = ap3CoonsPatch[3];
			//	aap3Patches[1][7] = ap3CoonsPatch[4];
			//	aap3Patches[1][11] = ap3CoonsPatch[5];
			//	aap3Patches[1][15] = ap3Helper[6];
			//	aap3Patches[1][14] = ap3Helper[5];
			//	aap3Patches[1][13] = ap3Helper[4];
			//	aap3Patches[1][12] = ap3Helper[3];
			//	aap3Patches[1][8] = p3_2;
			//	aap3Patches[1][4] = p3_1;
			//	BezierPatch.computeInnerControlPoints(aap3Patches[1]);
			//	*/
			//break;
			case 8:
				aap3Patches = new Point3f[1][16];
				aap3Patches[0][0] = ap3CoonsPatch[0];
				aap3Patches[0][1] = ap3CoonsPatch[1];
				aap3Patches[0][2] = ap3CoonsPatch[2];
				aap3Patches[0][3] = ap3CoonsPatch[3];
				aap3Patches[0][7] = ap3CoonsPatch[4];
				aap3Patches[0][11] = ap3CoonsPatch[5];
				aap3Patches[0][15] = ap3CoonsPatch[6];
				aap3Patches[0][14] = ap3CoonsPatch[7];
				aap3Patches[0][13] = ap3CoonsPatch[8];
				aap3Patches[0][12] = ap3CoonsPatch[9];
				aap3Patches[0][8] = ap3CoonsPatch[10];
				aap3Patches[0][4] = ap3CoonsPatch[11];
				/*
				aap3Patches[0][5] = parallelogram(aap3Patches[0][0],aap3Patches[0][1],aap3Patches[0][4]);
				aap3Patches[0][6] = parallelogram(aap3Patches[0][3],aap3Patches[0][2],aap3Patches[0][7]);
				aap3Patches[0][9] = parallelogram(aap3Patches[0][12],aap3Patches[0][8],aap3Patches[0][13]);
				aap3Patches[0][10] = parallelogram(aap3Patches[0][15],aap3Patches[0][11],aap3Patches[0][14]);
				*/
				BezierPatch.computeInnerControlPoints(aap3Patches[0]);
				
			break;
			
//			case 60:
//				/* deCasteljau 3-sided patch */
//				Point3f p3A = Functions.parallelogram(ap3CoonsPatch[0],ap3CoonsPatch[1],ap3CoonsPatch[8]);
//				Point3f p3B = Functions.parallelogram(ap3CoonsPatch[3],ap3CoonsPatch[4],ap3CoonsPatch[2]);
//				Point3f p3C = Functions.parallelogram(ap3CoonsPatch[6],ap3CoonsPatch[7],ap3CoonsPatch[5]);
//				
//				Point3f p3D = Functions.average(ap3CoonsPatch[0],ap3CoonsPatch[1],p3A,ap3CoonsPatch[8]);
//				Point3f p3E = Functions.average(ap3CoonsPatch[1],ap3CoonsPatch[2],p3B,p3A);
//				Point3f p3F = Functions.average(ap3CoonsPatch[3],ap3CoonsPatch[4],p3B,ap3CoonsPatch[2]);
//				Point3f p3G = Functions.average(ap3CoonsPatch[4],ap3CoonsPatch[5],p3C,p3B);
//				Point3f p3H = Functions.average(ap3CoonsPatch[6],ap3CoonsPatch[7],p3C,ap3CoonsPatch[5]);
//				Point3f p3I = Functions.average(ap3CoonsPatch[7],ap3CoonsPatch[8],p3A,p3C);
//				Point3f p3J = Functions.average(p3A,p3B,p3C);
//				
//				Point3f p3K = Functions.average(p3D,p3E,p3J,p3I);
//				Point3f p3L = Functions.average(p3F,p3G,p3J,p3E);
//				Point3f p3M = Functions.average(p3H,p3I,p3J,p3G);
//				
//				Point3f p3N = Functions.average(p3K,p3L,p3M);
//				
//				Point3f p3KM = Functions.average(p3K,p3M);
//				Point3f p3KL = Functions.average(p3K,p3L);
//				Point3f p3LM = Functions.average(p3L,p3M);
//				
//				
//				aap3Patches = new Point3f[3][16];
//				
//				aap3Boundary = new Point3f[3][7];
//				
//				aap3Boundary[0] = Bezier.deCasteljau(ap3CoonsPatch[0],ap3CoonsPatch[1],ap3CoonsPatch[2],ap3CoonsPatch[3],0.5f);
//				aap3Boundary[1] = Bezier.deCasteljau(ap3CoonsPatch[3],ap3CoonsPatch[4],ap3CoonsPatch[5],ap3CoonsPatch[6],0.5f);
//				aap3Boundary[2] = Bezier.deCasteljau(ap3CoonsPatch[6],ap3CoonsPatch[7],ap3CoonsPatch[8],ap3CoonsPatch[0],0.5f);
//				
//				Point3f p0a = new Point3f(aap3Boundary[0][4]);
//				Point3f q0a = new Point3f(aap3Boundary[0][3]);
//				Point3f r0a = new Point3f(aap3Boundary[0][2]);
//				Point3f p0b = new Point3f(aap3Boundary[1][4]);
//				Point3f q0b = new Point3f(aap3Boundary[1][3]);
//				Point3f r0b = new Point3f(aap3Boundary[1][2]);
//				Point3f p0c = new Point3f(aap3Boundary[2][4]);
//				Point3f q0c = new Point3f(aap3Boundary[2][3]);
//				Point3f r0c = new Point3f(aap3Boundary[2][2]);
//				
//				Point3f q1a = Functions.average(Bezier.evaluate(ap3CoonsPatch[4],p3C,p3A,ap3CoonsPatch[8],0.5f),q0a);
//				Point3f q1b = Functions.average(Bezier.evaluate(ap3CoonsPatch[7],p3B,p3C,ap3CoonsPatch[2],0.5f),q0b);
//				Point3f q1c = Functions.average(Bezier.evaluate(ap3CoonsPatch[5],p3B,p3A,ap3CoonsPatch[1],0.5f),q0c);
//				
//				Point3f q2a,p3c,r3b,q2b,p3a,r3c,q2c,p3b,r3a;
//				
//				q2a = p3c = r3b = p3KL;
//				q2b = p3a = r3c = p3LM;
//				q2c = p3b = r3a = p3KM;
//				
//				Point3f q3 = p3N;
//				
//				Point3f[] qa = Bezier.elevateDegree(q0a,q1a,q2a,q3);
//				Point3f[] qb = Bezier.elevateDegree(q0b,q1b,q2b,q3);
//				Point3f[] qc = Bezier.elevateDegree(q0c,q1c,q2c,q3);
//				
//				Tuple2f t2Intersection;
//				
//				t2Intersection = Functions.lineLineIntersection(qb[3],qc[3],qa[3],qa[4]);
//				float la = t2Intersection.x;
//				float ua = t2Intersection.y;
//				
//				t2Intersection = Functions.lineLineIntersection(qc[3],qa[3],qb[3],qb[4]);
//				float lb = t2Intersection.x;
//				float ub = t2Intersection.y;
//				
//				t2Intersection = Functions.lineLineIntersection(qa[3],qb[3],qc[3],qc[4]);
//				float lc = t2Intersection.x;
//				float uc = t2Intersection.y;
//				
//				float la_ = 1f - la;
//				float ua_ = 1f - ua;
//				float lb_ = 1f - lb;
//				float ub_ = 1f - ub;
//				float lc_ = 1f - lc;
//				float uc_ = 1f - uc;
//				
//				la = la_ = lb = lb_ = lc = lc_ = 0.5f;
//				ua = ub = uc = 1.5f;
//				ua_ = ub_ = uc_ = -0.5f;
//				
//				System.out.println("  l,u");
//				System.out.println("a:" + la + "," + ua);
//				System.out.println("b:" + lb + "," + ub);
//				System.out.println("c:" + lc + "," + uc);
//				
//				//float rhs1ax = 2 * (qa[1].x - la_ * p0a.x + la * r0a.x) - (ua_ * qa[0].x + ua * qa[1].x);
//				//float rhs1ay = 2 * (qa[1].y - la_ * p0a.y + la * r0a.y) - (ua_ * qa[0].y + ua * qa[1].y);
//				//float rhs1az = 2 * (qa[1].z - la_ * p0a.z + la * r0a.z) - (ua_ * qa[0].z + ua * qa[1].z);
//				//float rhs2ax = ua_ * qa[1].x + (1 + ua) * qa[2].x;
//				//float rhs2ay = ua_ * qa[1].y + (1 + ua) * qa[2].y;
//				//float rhs2az = ua_ * qa[1].z + (1 + ua) * qa[2].z;
//				//float rhs3ax = ua_ * qa[2].x + ua * qa[3].x - 1f/6f * (p3a.x + r3a.x) + 1f/3f * qa[3].x;
//				//float rhs3ay = ua_ * qa[2].y + ua * qa[3].y - 1f/6f * (p3a.y + r3a.y) + 1f/3f * qa[3].y;
//				//float rhs3az = ua_ * qa[2].z + ua * qa[3].z - 1f/6f * (p3a.z + r3a.z) + 1f/3f * qa[3].z;
//				//
//				//float rhs1bx = 2 * (qb[1].x - lb_ * p0b.x + lb * r0b.x) - (ub_ * qb[0].x + ub * qb[1].x);
//				//float rhs1by = 2 * (qb[1].y - lb_ * p0b.y + lb * r0b.y) - (ub_ * qb[0].y + ub * qb[1].y);
//				//float rhs1bz = 2 * (qb[1].z - lb_ * p0b.z + lb * r0b.z) - (ub_ * qb[0].z + ub * qb[1].z);
//				//float rhs2bx = ub_ * qb[1].x + (1 + ub) * qb[2].x;
//				//float rhs2by = ub_ * qb[1].y + (1 + ub) * qb[2].y;
//				//float rhs2bz = ub_ * qb[1].z + (1 + ub) * qb[2].z;
//				//float rhs3bx = ub_ * qb[2].x + ub * qb[3].x - 1f/6f * (p3b.x + r3b.x) + 1f/3f * qb[3].x;
//				//float rhs3by = ub_ * qb[2].y + ub * qb[3].y - 1f/6f * (p3b.y + r3b.y) + 1f/3f * qb[3].y;
//				//float rhs3bz = ub_ * qb[2].z + ub * qb[3].z - 1f/6f * (p3b.z + r3b.z) + 1f/3f * qb[3].z;
//				//
//				//float rhs1cx = 2 * (qc[1].x - lc_ * p0c.x + lc * r0c.x) - (uc_ * qc[0].x + uc * qc[1].x);
//				//float rhs1cy = 2 * (qc[1].y - lc_ * p0c.y + lc * r0c.y) - (uc_ * qc[0].y + uc * qc[1].y);
//				//float rhs1cz = 2 * (qc[1].z - lc_ * p0c.z + lc * r0c.z) - (uc_ * qc[0].z + uc * qc[1].z);
//				//float rhs2cx = uc_ * qc[1].x + (1 + uc) * qc[2].x;
//				//float rhs2cy = uc_ * qc[1].y + (1 + uc) * qc[2].y;
//				//float rhs2cz = uc_ * qc[1].z + (1 + uc) * qc[2].z;
//				//float rhs3cx = uc_ * qc[2].x + uc * qc[3].x - 1f/6f * (p3c.x + r3c.x) + 1f/3f * qc[3].x;
//				//float rhs3cy = uc_ * qc[2].y + uc * qc[3].y - 1f/6f * (p3c.y + r3c.y) + 1f/3f * qc[3].y;
//				//float rhs3cz = uc_ * qc[2].z + uc * qc[3].z - 1f/6f * (p3c.z + r3c.z) + 1f/3f * qc[3].z;
//				
//				//float rhs1ax = -1f/3f * (p0a.x + r0a.x + qa[0].x) + 3f * qa[1].x;
//				//float rhs2ax = -qa[1].x + 5f * qa[2].x;
//				//float rhs3ax = -qa[2].x + 3f * qa[3].x - 1f/3f * (p3a.x + r3a.x - 2f * qa[3].x);
//				//float rhs1ay = -1f/3f * (p0a.y + r0a.y + qa[0].y) + 3f * qa[1].y;
//				//float rhs2ay = -qa[1].y + 5f * qa[2].y;
//				//float rhs3ay = -qa[2].y + 3f * qa[3].y - 1f/3f * (p3a.y + r3a.y - 2f * qa[3].y);
//				//float rhs1az = -1f/3f * (p0a.z + r0a.z + qa[0].z) + 3f * qa[1].z;
//				//float rhs2az = -qa[1].z + 5f * qa[2].z;
//				//float rhs3az = -qa[2].z + 3f * qa[3].z - 1f/3f * (p3a.z + r3a.z - 2f * qa[3].z);
//				//float rhs1bx = -1f/3f * (p0b.x + r0b.x + qb[0].x) + 3f * qb[1].x;
//				//float rhs2bx = -qb[1].x + 5f * qb[2].x;
//				//float rhs3bx = -qb[2].x + 3f * qb[3].x - 1f/3f * (p3b.x + r3b.x - 2f * qb[3].x);
//				//float rhs1by = -1f/3f * (p0b.y + r0b.y + qb[0].y) + 3f * qb[1].y;
//				//float rhs2by = -qb[1].y + 5f * qb[2].y;
//				//float rhs3by = -qb[2].y + 3f * qb[3].y - 1f/3f * (p3b.y + r3b.y - 2f * qb[3].y);
//				//float rhs1bz = -1f/3f * (p0b.z + r0b.z + qb[0].z) + 3f * qb[1].z;
//				//float rhs2bz = -qb[1].z + 5f * qb[2].z;
//				//float rhs3bz = -qb[2].z + 3f * qb[3].z - 1f/3f * (p3b.z + r3b.z - 2f * qb[3].z);
//				//float rhs1cx = -1f/3f * (p0c.x + r0c.x + qc[0].x) + 3f * qc[1].x;
//				//float rhs2cx = -qc[1].x + 5f * qc[2].x;
//				//float rhs3cx = -qc[2].x + 3f * qc[3].x - 1f/3f * (p3c.x + r3c.x - 2f * qc[3].x);
//				//float rhs1cy = -1f/3f * (p0c.y + r0c.y + qc[0].y) + 3f * qc[1].y;
//				//float rhs2cy = -qc[1].y + 5f * qc[2].y;
//				//float rhs3cy = -qc[2].y + 3f * qc[3].y - 1f/3f * (p3c.y + r3c.y - 2f * qc[3].y);
//				//float rhs1cz = -1f/3f * (p0c.z + r0c.z + qc[0].z) + 3f * qc[1].z;
//				//float rhs2cz = -qc[1].z + 5f * qc[2].z;
//				//float rhs3cz = -qc[2].z + 3f * qc[3].z - 1f/3f * (p3c.z + r3c.z - 2f * qc[3].z);
//				
//				float rhs1ax = -1f/3f * (p0a.x + r0a.x + 3f/4f * qa[0].x - 35f/4f * qa[1].x);
//				float rhs2ax = -3f/4f * qa[1].x + 19f/4f * qa[2].x;
//				float rhs3ax = -3f/4f * qa[2].x + 41f/12f * qa[3].x - 1f/3f * (p3a.x + r3a.x);
//				float rhs1ay = -1f/3f * (p0a.y + r0a.y + 3f/4f * qa[0].y - 35f/4f * qa[1].y);
//				float rhs2ay = -3f/4f * qa[1].y + 19f/4f * qa[2].y;
//				float rhs3ay = -3f/4f * qa[2].y + 41f/12f * qa[3].y - 1f/3f * (p3a.y + r3a.y);
//				float rhs1az = -1f/3f * (p0a.z + r0a.z + 3f/4f * qa[0].z - 35f/4f * qa[1].z);
//				float rhs2az = -3f/4f * qa[1].z + 19f/4f * qa[2].z;
//				float rhs3az = -3f/4f * qa[2].z + 41f/12f * qa[3].z - 1f/3f * (p3a.z + r3a.z);
//				float rhs1bx = -1f/3f * (p0b.x + r0b.x + 3f/4f * qb[0].x - 35f/4f * qb[1].x);
//				float rhs2bx = -3f/4f * qb[1].x + 19f/4f * qb[2].x;
//				float rhs3bx = -3f/4f * qb[2].x + 41f/12f * qb[3].x - 1f/3f * (p3b.x + r3b.x);
//				float rhs1by = -1f/3f * (p0b.y + r0b.y + 3f/4f * qb[0].y - 35f/4f * qb[1].y);
//				float rhs2by = -3f/4f * qb[1].y + 19f/4f * qb[2].y;
//				float rhs3by = -3f/4f * qb[2].y + 41f/12f * qb[3].y - 1f/3f * (p3b.y + r3b.y);
//				float rhs1bz = -1f/3f * (p0b.z + r0b.z + 3f/4f * qb[0].z - 35f/4f * qb[1].z);
//				float rhs2bz = -3f/4f * qb[1].z + 19f/4f * qb[2].z;
//				float rhs3bz = -3f/4f * qb[2].z + 41f/12f * qb[3].z - 1f/3f * (p3b.z + r3b.z);
//				float rhs1cx = -1f/3f * (p0c.x + r0c.x + 3f/4f * qc[0].x - 35f/4f * qc[1].x);
//				float rhs2cx = -3f/4f * qc[1].x + 19f/4f * qc[2].x;
//				float rhs3cx = -3f/4f * qc[2].x + 41f/12f * qc[3].x - 1f/3f * (p3c.x + r3c.x);
//				float rhs1cy = -1f/3f * (p0c.y + r0c.y + 3f/4f * qc[0].y - 35f/4f * qc[1].y);
//				float rhs2cy = -3f/4f * qc[1].y + 19f/4f * qc[2].y;
//				float rhs3cy = -3f/4f * qc[2].y + 41f/12f * qc[3].y - 1f/3f * (p3c.y + r3c.y);
//				float rhs1cz = -1f/3f * (p0c.z + r0c.z + 3f/4f * qc[0].z - 35f/4f * qc[1].z);
//				float rhs2cz = -3f/4f * qc[1].z + 19f/4f * qc[2].z;
//				float rhs3cz = -3f/4f * qc[2].z + 41f/12f * qc[3].z - 1f/3f * (p3c.z + r3c.z);
//				
//				Point3f p1a = Functions.average(ap3CoonsPatch[2],p3C);
//				Point3f p1b = Functions.average(ap3CoonsPatch[5],p3B);
//				Point3f p1c = Functions.average(ap3CoonsPatch[8],p3A);
//				
//				Point3f[] u = Functions.solve(new float[][] {
//					// p1a,r1a,p2a=r2b,r2a=p2c,p1b,r1b,p2b=r2c,p1c,r1c
//					{ 1,0,0,0,0,0,0,0,0,p1a.x,p1a.y,p1a.z },
//				      //{ 1,1,0,0,0,0,0,0,0,rhs1ax,rhs1ay,rhs1az },
//					{ 1,1,1,1,0,0,0,0,0,rhs2ax,rhs2ay,rhs2az },
//					{ 0,0,1,1,0,0,0,0,0,rhs3ax,rhs3ay,rhs3az },
//				      //{ 0,0,0,0,1,1,0,0,0,rhs1bx,rhs1by,rhs1bz },
//					{ 0,0,0,0,1,0,0,0,0,p1b.x,p1b.y,p1b.z },
//					{ 0,0,1,0,1,1,1,0,0,rhs2bx,rhs2by,rhs2bz },
//					{ 0,0,1,0,0,0,1,0,0,rhs3bx,rhs3by,rhs3bz },
//				      //{ 0,0,0,0,0,0,0,1,1,rhs1cx,rhs1cy,rhs1cz },
//					{ 0,0,0,0,0,0,0,1,0,p1c.x,p1c.y,p1c.z },
//					{ 0,0,0,1,0,0,1,1,1,rhs2cx,rhs2cy,rhs2cz },
//					{ 0,0,0,1,0,0,1,0,0,rhs3cx,rhs3cy,rhs3cz },
//				});
//				
//				//Point3f[] u = new Point3f[9];
//				for (int i = 0; i < u.length; i++) {
//					//u[i] = new Point3f();
//					System.out.println(i + " " + u[i]);
//				}
//				
//				aap3Patches[0][0] = new Point3f(aap3Boundary[1][0]);
//				aap3Patches[0][1] = new Point3f(aap3Boundary[1][1]);
//				aap3Patches[0][2] = new Point3f(aap3Boundary[1][2]);
//				aap3Patches[0][3] = new Point3f(aap3Boundary[1][3]);
//				aap3Patches[0][4] = new Point3f(aap3Boundary[0][5]);
//				aap3Patches[0][5] = Functions.parallelogram(aap3Boundary[1][0],aap3Boundary[1][1],aap3Boundary[0][5]);
//				aap3Patches[0][6] = new Point3f(u[5]);
//				aap3Patches[0][7] = new Point3f(q1b);
//				aap3Patches[0][8] = new Point3f(aap3Boundary[0][4]);
//				aap3Patches[0][9] = new Point3f(u[0]);
//				aap3Patches[0][10] = new Point3f(u[2]);
//				aap3Patches[0][11] = new Point3f(q2b);
//				aap3Patches[0][12] = new Point3f(q0a);
//				aap3Patches[0][13] = new Point3f(q1a);
//				aap3Patches[0][14] = new Point3f(q2a);
//				aap3Patches[0][15] = new Point3f(q3);
//				
//				aap3Patches[1][0] = new Point3f(aap3Boundary[2][0]);
//				aap3Patches[1][1] = new Point3f(aap3Boundary[2][1]);
//				aap3Patches[1][2] = new Point3f(aap3Boundary[2][2]);
//				aap3Patches[1][3] = new Point3f(aap3Boundary[2][3]);
//				aap3Patches[1][4] = new Point3f(aap3Boundary[1][5]);
//				aap3Patches[1][5] = Functions.parallelogram(aap3Boundary[2][0],aap3Boundary[2][1],aap3Boundary[1][5]);
//				aap3Patches[1][6] = new Point3f(u[8]);
//				aap3Patches[1][7] = new Point3f(q1c);
//				aap3Patches[1][8] = new Point3f(aap3Boundary[1][4]);
//				aap3Patches[1][9] = new Point3f(u[4]);
//				aap3Patches[1][10] = new Point3f(u[6]);
//				aap3Patches[1][11] = new Point3f(q2c);
//				aap3Patches[1][12] = new Point3f(q0b);
//				aap3Patches[1][13] = new Point3f(q1b);
//				aap3Patches[1][14] = new Point3f(q2b);
//				aap3Patches[1][15] = new Point3f(q3);
//				
//				aap3Patches[2][0] = new Point3f(aap3Boundary[0][0]);
//				aap3Patches[2][1] = new Point3f(aap3Boundary[0][1]);
//				aap3Patches[2][2] = new Point3f(aap3Boundary[0][2]);
//				aap3Patches[2][3] = new Point3f(aap3Boundary[0][3]);
//				aap3Patches[2][4] = new Point3f(aap3Boundary[2][5]);
//				aap3Patches[2][5] = Functions.parallelogram(aap3Boundary[0][0],aap3Boundary[0][1],aap3Boundary[2][5]);
//				aap3Patches[2][6] = new Point3f(u[1]);
//				aap3Patches[2][7] = new Point3f(q1a);
//				aap3Patches[2][8] = new Point3f(aap3Boundary[2][4]);
//				aap3Patches[2][9] = new Point3f(u[7]);
//				aap3Patches[2][10] = new Point3f(u[3]);
//				aap3Patches[2][11] = new Point3f(q2a);
//				aap3Patches[2][12] = new Point3f(q0c);
//				aap3Patches[2][13] = new Point3f(q1c);
//				aap3Patches[2][14] = new Point3f(q2c);
//				aap3Patches[2][15] = new Point3f(q3);
//				
//				//Point3f v;
//				//Point3f p3A0 = new Point3f(aap3Boundary[0][3]);
//				//v = Functions.average(Functions.vector(ap3CoonsPatch[0],ap3CoonsPatch[8]),Functions.vector(ap3CoonsPatch[3],ap3CoonsPatch[4]));
//				//v.scale(0.5f);
//				//p3A0.add(v);
//				//Point3f p3A1 = new Point3f(aap3Boundary[1][3]);
//				//v = Functions.average(Functions.vector(ap3CoonsPatch[3],ap3CoonsPatch[2]),Functions.vector(ap3CoonsPatch[6],ap3CoonsPatch[7]));
//				//v.scale(0.5f);
//				//p3A1.add(v);
//				//Point3f p3A2 = new Point3f(aap3Boundary[2][3]);
//				//v = Functions.average(Functions.vector(ap3CoonsPatch[0],ap3CoonsPatch[1]),Functions.vector(ap3CoonsPatch[6],ap3CoonsPatch[5]));
//				//v.scale(0.5f);
//				//p3A2.add(v);
//				//
//				//aap3Patches[0][0] = new Point3f(aap3Boundary[0][3]);
//				//aap3Patches[0][1] = new Point3f(p3A0);
//				//aap3Patches[0][2] = new Point3f(p3KL);
//				//aap3Patches[0][3] = new Point3f(p3N);
//				//aap3Patches[0][4] = new Point3f(aap3Boundary[0][2]);
//				//aap3Patches[0][7] = new Point3f(p3KM);
//				//aap3Patches[0][8] = new Point3f(aap3Boundary[0][1]);
//				//aap3Patches[0][11] = new Point3f(p3A2);
//				//aap3Patches[0][12] = new Point3f(aap3Boundary[2][6]);
//				//aap3Patches[0][13] = new Point3f(aap3Boundary[2][5]);
//				//aap3Patches[0][14] = new Point3f(aap3Boundary[2][4]);
//				//aap3Patches[0][15] = new Point3f(aap3Boundary[2][3]);
//				//BezierPatch.computeInnerControlPoints(aap3Patches[0]);
//				//aap3Patches[0][6].set(p3K);
//				//
//				//aap3Patches[1][0] = new Point3f(aap3Boundary[1][3]);
//				//aap3Patches[1][1] = new Point3f(p3A1);
//				//aap3Patches[1][2] = new Point3f(p3LM);
//				//aap3Patches[1][3] = new Point3f(p3N);
//				//aap3Patches[1][4] = new Point3f(aap3Boundary[1][2]);
//				//aap3Patches[1][7] = new Point3f(p3KL);
//				//aap3Patches[1][8] = new Point3f(aap3Boundary[1][1]);
//				//aap3Patches[1][11] = new Point3f(p3A0);
//				//aap3Patches[1][12] = new Point3f(aap3Boundary[0][6]);
//				//aap3Patches[1][13] = new Point3f(aap3Boundary[0][5]);
//				//aap3Patches[1][14] = new Point3f(aap3Boundary[0][4]);
//				//aap3Patches[1][15] = new Point3f(aap3Boundary[0][3]);
//				//BezierPatch.computeInnerControlPoints(aap3Patches[1]);
//				//aap3Patches[1][6].set(p3L);
//				//
//				//aap3Patches[2][0] = new Point3f(aap3Boundary[2][3]);
//				//aap3Patches[2][1] = new Point3f(p3A2);
//				//aap3Patches[2][2] = new Point3f(p3KM);
//				//aap3Patches[2][3] = new Point3f(p3N);
//				//aap3Patches[2][4] = new Point3f(aap3Boundary[2][2]);
//				//aap3Patches[2][7] = new Point3f(p3LM);
//				//aap3Patches[2][8] = new Point3f(aap3Boundary[2][1]);
//				//aap3Patches[2][11] = new Point3f(p3A1);
//				//aap3Patches[2][12] = new Point3f(aap3Boundary[1][6]);
//				//aap3Patches[2][13] = new Point3f(aap3Boundary[1][5]);
//				//aap3Patches[2][14] = new Point3f(aap3Boundary[1][4]);
//				//aap3Patches[2][15] = new Point3f(aap3Boundary[1][3]);
//				//BezierPatch.computeInnerControlPoints(aap3Patches[2]);
//				//aap3Patches[2][6].set(p3M);
//				
//				break;
//				
			case 6:
				int apex = -1;
				for (int i = 0; i < 6; i += 2) {
					if (apex == -1 && acpPoint[i].getHead().getStack().length > 2) apex = i;
				}
				if (apex != -1) {
					//System.out.print("apex " + apex + "   " + "cp = " + acpPoint[apex] + "   ");
					apex = apex / 2;
					shift(ap3CoonsPatch,apex * 3 + 3);
				}
				
			//case 6 - 2:	// old 3-sided patch
			
				
				aap3Patches = new Point3f[1][16];
				aap3Patches[0][0] = ap3CoonsPatch[0];
				aap3Patches[0][1] = ap3CoonsPatch[1];
				aap3Patches[0][2] = ap3CoonsPatch[2];
				aap3Patches[0][3] = ap3CoonsPatch[3];
				aap3Patches[0][7] = ap3CoonsPatch[4];
				aap3Patches[0][11] = ap3CoonsPatch[5];
				aap3Patches[0][8] = ap3CoonsPatch[7];
				aap3Patches[0][4] = ap3CoonsPatch[8];
				/*
				aap3Patches[0][15] = ap3CoonsPatch[6];
				aap3Patches[0][14] = new Point3f(ap3CoonsPatch[6]);
				aap3Patches[0][13] = new Point3f(ap3CoonsPatch[6]);
				aap3Patches[0][12] = new Point3f(ap3CoonsPatch[6]);
				*/
				
				float m = 0.001f;
				aap3Patches[0][15] = Bezier.evaluate(ap3CoonsPatch[3],ap3CoonsPatch[4],ap3CoonsPatch[5],ap3CoonsPatch[6],1f - m);
				aap3Patches[0][12] = Bezier.evaluate(ap3CoonsPatch[0],ap3CoonsPatch[8],ap3CoonsPatch[7],ap3CoonsPatch[6],1f - m);
				
				
				aap3Patches[0][13] = new Point3f();
				aap3Patches[0][14] = new Point3f();
				
				if (apex != -1) {
					Vector3f v3a = new Vector3f(aap3Patches[0][1]);
					v3a.sub(aap3Patches[0][0]);
					Vector3f v3b = new Vector3f(aap3Patches[0][2]);
					v3b.sub(aap3Patches[0][3]);
					Vector3f v3 = new Vector3f();
					
					v3.set(v3a);
					v3.scale(2f/3f);
					aap3Patches[0][5] = new Point3f(aap3Patches[0][4]);
					aap3Patches[0][5].add(v3);
					
					v3.set(v3b);
					v3.scale(2f/3f);
					aap3Patches[0][6] = new Point3f(aap3Patches[0][7]);
					aap3Patches[0][6].add(v3);
					
					v3.set(v3a);
					v3.scale(1f/3f);
					aap3Patches[0][9] = new Point3f(aap3Patches[0][8]);
					aap3Patches[0][9].add(v3);
					
					v3.set(v3b);
					v3.scale(1f/3f);
					aap3Patches[0][10] = new Point3f(aap3Patches[0][11]);
					aap3Patches[0][10].add(v3);
					
					v3.set(v3a);
					v3.scale(m);
					aap3Patches[0][13] = new Point3f(aap3Patches[0][12]);
					aap3Patches[0][13].add(v3);
					
					v3.set(v3b);
					v3.scale(m);
					aap3Patches[0][14] = new Point3f(aap3Patches[0][15]);
					aap3Patches[0][14].add(v3);
				} else {
					
				aap3Patches[0][13].interpolate(aap3Patches[0][12],aap3Patches[0][15],2f/3f);
				aap3Patches[0][14].interpolate(aap3Patches[0][12],aap3Patches[0][15],2f/3f);
					//aap3Patches[0][13].set(
					//	(aap3Patches[0][1].x - aap3Patches[0][0].x) * m + aap3Patches[0][12].x,
					//	(aap3Patches[0][1].y - aap3Patches[0][0].y) * m + aap3Patches[0][12].y,
					//	(aap3Patches[0][1].z - aap3Patches[0][0].z) * m + aap3Patches[0][12].z
					//);
					//aap3Patches[0][14].set(
					//	(aap3Patches[0][2].x - aap3Patches[0][3].x) * m + aap3Patches[0][15].x,
					//	(aap3Patches[0][2].y - aap3Patches[0][3].y) * m + aap3Patches[0][15].y,
					//	(aap3Patches[0][2].z - aap3Patches[0][3].z) * m + aap3Patches[0][15].z
					//);
					
					BezierPatch.computeInnerControlPoints(aap3Patches[0]);
					Point3f p = Functions.parallelogram(ap3CoonsPatch[6],ap3CoonsPatch[5],ap3CoonsPatch[7]);
					aap3Patches[0][9].interpolate(aap3Patches[0][8],p,2f/3f);
					aap3Patches[0][10].interpolate(aap3Patches[0][11],p,2f/3f);
				}
				/*
				aap3Patches[0][5] = parallelogram(aap3Patches[0][0],aap3Patches[0][1],aap3Patches[0][4]);
				aap3Patches[0][6] = parallelogram(aap3Patches[0][3],aap3Patches[0][2],aap3Patches[0][7]);
				aap3Patches[0][9] = parallelogram(aap3Patches[0][12],aap3Patches[0][8],aap3Patches[0][13]);
				aap3Patches[0][10] = parallelogram(aap3Patches[0][15],aap3Patches[0][11],aap3Patches[0][14]);
				
				Point3f[] ap3Helper;
				Vector3f v3_a;
				Vector3f v3_b;
				Vector3f v3_3 = new Vector3f();
				Vector3f v3_9 = new Vector3f();
				Vector3f v3_15 = new Vector3f();
				
				ap3Helper = Bezier.deCasteljau(ap3CoonsPatch[0],ap3CoonsPatch[1],ap3CoonsPatch[2],ap3CoonsPatch[3],0.5f);
				Point3f p3_0 = ap3Helper[0];
				Point3f p3_1 = ap3Helper[1];
				Point3f p3_2 = ap3Helper[2];
				Point3f p3_3 = ap3Helper[3];
				Point3f p3_4 = ap3Helper[4];
				Point3f p3_5 = ap3Helper[5];
				Point3f p3_6 = ap3Helper[6];
				ap3Helper = Bezier.deCasteljau(ap3CoonsPatch[3],ap3CoonsPatch[4],ap3CoonsPatch[5],ap3CoonsPatch[6],0.5f);
				Point3f p3_7 = ap3Helper[1];
				Point3f p3_8 = ap3Helper[2];
				Point3f p3_9 = ap3Helper[3];
				Point3f p3_10 = ap3Helper[4];
				Point3f p3_11 = ap3Helper[5];
				Point3f p3_12 = ap3Helper[6];
				ap3Helper = Bezier.deCasteljau(ap3CoonsPatch[6],ap3CoonsPatch[7],ap3CoonsPatch[8],ap3CoonsPatch[0],0.5f);
				Point3f p3_13 = ap3Helper[1];
				Point3f p3_14 = ap3Helper[2];
				Point3f p3_15 = ap3Helper[3];
				Point3f p3_16 = ap3Helper[4];
				Point3f p3_17 = ap3Helper[5];
				
				
				BezierTriangle bt = new BezierTriangle(ap3CoonsPatch[0],ap3CoonsPatch[1],ap3CoonsPatch[2],ap3CoonsPatch[3],ap3CoonsPatch[4],ap3CoonsPatch[5],ap3CoonsPatch[6],ap3CoonsPatch[7],ap3CoonsPatch[8]);
				Point3f p3_18 = bt.getCenterPoint();
				/*
				Vector3f v3_n18 = bt.getCenterNormal();
				v3_n18.normalize();
				Plane plane = new Plane(p3_18,v3_n18);
				*/
				
				/*
				Point3f p3_18 = new Point3f(p3_3);
				p3_18.add(p3_9);
				p3_18.add(p3_15);
				p3_18.scale(1f/3f);
				*/
				/*
				Plane plane = new Plane(p3_18,Plane.normal(p3_3,p3_9,p3_15));
				
				v3_a = Functions.vector(p3_0,p3_17);
				v3_b = Functions.vector(p3_6,p3_7);
				v3_3.interpolate(v3_a,v3_b,0.5f);
				v3_3.scale(0.5f);
				Point3f p3_24 = new Point3f(p3_3);
				p3_24.add(v3_3);
				Point3f p3_23 = new Point3f(p3_18);
				p3_23.sub(v3_3);
				Point3f p3_27 = new Point3f(p3_18);
				p3_27.add(v3_3);
				p3_27.add(v3_3);
				
				v3_a = Functions.vector(p3_6,p3_5);
				v3_b = Functions.vector(p3_12,p3_13);
				v3_9.interpolate(v3_a,v3_b,0.5f);
				v3_9.scale(0.5f);
				Point3f p3_20 = new Point3f(p3_9);
				p3_20.add(v3_9);
				Point3f p3_19 = new Point3f(p3_18);
				p3_19.sub(v3_9);
				Point3f p3_25 = new Point3f(p3_18);
				p3_25.add(v3_9);
				p3_25.add(v3_9);
				
				v3_a = Functions.vector(p3_0,p3_1);
				v3_b = Functions.vector(p3_12,p3_11);
				v3_15.interpolate(v3_a,v3_b,0.5f);
				v3_15.scale(0.5f);
				Point3f p3_22 = new Point3f(p3_15);
				p3_22.add(v3_15);
				Point3f p3_21 = new Point3f(p3_18);
				p3_21.sub(v3_15);
				Point3f p3_26 = new Point3f(p3_18);
				p3_26.add(v3_15);
				p3_26.add(v3_15);
				
				p3_25 = plane.projectedPoint(p3_25);
				p3_26 = plane.projectedPoint(p3_26);
				p3_27 = plane.projectedPoint(p3_27);
				
				p3_19 = plane.projectedPoint(p3_19);
				p3_21 = plane.projectedPoint(p3_21);
				p3_23 = plane.projectedPoint(p3_23);
				
				/*
				Line2f line1;
				Line2f line2;
				line1 = new Line2f(plane.getP2(p3_18),plane.getP2(p3_19));
				line2 = new Line2f(plane.getP2(p3_26),plane.getP2(p3_27));
				p3_19 = plane.getP3(line1.intersection(line2));
				line1 = new Line2f(plane.getP2(p3_18),plane.getP2(p3_21));
				line2 = new Line2f(plane.getP2(p3_27),plane.getP2(p3_25));
				p3_21 = plane.getP3(line1.intersection(line2));
				line1 = new Line2f(plane.getP2(p3_18),plane.getP2(p3_23));
				line2 = new Line2f(plane.getP2(p3_25),plane.getP2(p3_26));
				p3_23 = plane.getP3(line1.intersection(line2));
				*/
				
				/*
				Point3f p3_19 = new Point3f();
				//Point3f p3_20 = new Point3f();
				Point3f p3_21 = new Point3f();
				//Point3f p3_22 = new Point3f();
				Point3f p3_23 = new Point3f();
				//Point3f p3_24 = new Point3f();
				
				p3_19.interpolate(p3_18,p3_9,1f/3f);
				//p3_20.interpolate(p3_18,p3_9,2f/3f);
				p3_21.interpolate(p3_18,p3_15,1f/3f);
				//p3_22.interpolate(p3_18,p3_15,2f/3f);
				p3_23.interpolate(p3_18,p3_3,1f/3f);
				//p3_24.interpolate(p3_18,p3_3,2f/3f);
				*/
				
				/*
				Point3f p3_c1_c8 = new Point3f();
				Point3f p3_c2_c7 = new Point3f();
				Point3f p3_c2_c4 = new Point3f();
				Point3f p3_c1_c5 = new Point3f();
				Point3f p3_c5_c7 = new Point3f();
				Point3f p3_c4_c8 = new Point3f();
				
				Point3f p3_c1_ = new Point3f(ap3CoonsPatch[1]);
				Point3f p3_c2_ = new Point3f(ap3CoonsPatch[2]);
				Point3f p3_c4_ = new Point3f(ap3CoonsPatch[4]);
				Point3f p3_c5_ = new Point3f(ap3CoonsPatch[5]);
				Point3f p3_c7_ = new Point3f(ap3CoonsPatch[7]);
				Point3f p3_c8_ = new Point3f(ap3CoonsPatch[8]);
				
				Vector3f v3;
				v3 = Functions.vector(ap3CoonsPatch[0],ap3CoonsPatch[1]);
				v3.scale(2f/3f);
				p3_c8_.add(v3);
				v3.scale(1f/2f);
				p3_c7_.add(v3);
				v3 = Functions.vector(ap3CoonsPatch[3],ap3CoonsPatch[2]);
				v3.scale(2f/3f);
				p3_c4_.add(v3);
				v3.scale(1f/2f);
				p3_c5_.add(v3);
				ap3Helper = Bezier.deCasteljau(ap3CoonsPatch[7],p3_c7_,p3_c5_,ap3CoonsPatch[5],0.5f);
				p3_c5_c7 = ap3Helper[3];
				ap3Helper = Bezier.deCasteljau(ap3CoonsPatch[4],p3_c4_,p3_c8_,ap3CoonsPatch[8],0.5f);
				p3_c4_c8 = ap3Helper[3];
				
				//p3_c5_c7.interpolate(p3_c5_,p3_c7_,0.5f);
				//p3_c4_c8.interpolate(p3_c4_,p3_c8_,0.5f);
				
				p3_c1_ = new Point3f(ap3CoonsPatch[1]);
				p3_c2_ = new Point3f(ap3CoonsPatch[2]);
				p3_c4_ = new Point3f(ap3CoonsPatch[4]);
				p3_c5_ = new Point3f(ap3CoonsPatch[5]);
				p3_c7_ = new Point3f(ap3CoonsPatch[7]);
				p3_c8_ = new Point3f(ap3CoonsPatch[8]);
				
				v3 = Functions.vector(ap3CoonsPatch[6],ap3CoonsPatch[5]);
				v3.scale(2f/3f);
				p3_c7_.add(v3);
				v3.scale(1f/2f);
				p3_c8_.add(v3);
				v3 = Functions.vector(ap3CoonsPatch[3],ap3CoonsPatch[4]);
				v3.scale(2f/3f);
				p3_c2_.add(v3);
				v3.scale(1f/2f);
				p3_c1_.add(v3);
				ap3Helper = Bezier.deCasteljau(ap3CoonsPatch[1],p3_c1_,p3_c8_,ap3CoonsPatch[8],0.5f);
				p3_c1_c8 = ap3Helper[3];
				ap3Helper = Bezier.deCasteljau(ap3CoonsPatch[2],p3_c2_,p3_c7_,ap3CoonsPatch[7],0.5f);
				p3_c2_c7 = ap3Helper[3];
				//p3_c1_c8.interpolate(p3_c1_,p3_c8_,0.5f);
				//p3_c2_c7.interpolate(p3_c2_,p3_c7_,0.5f);
				
				p3_c1_ = new Point3f(ap3CoonsPatch[1]);
				p3_c2_ = new Point3f(ap3CoonsPatch[2]);
				p3_c4_ = new Point3f(ap3CoonsPatch[4]);
				p3_c5_ = new Point3f(ap3CoonsPatch[5]);
				p3_c7_ = new Point3f(ap3CoonsPatch[7]);
				p3_c8_ = new Point3f(ap3CoonsPatch[8]);
				
				v3 = Functions.vector(ap3CoonsPatch[6],ap3CoonsPatch[7]);
				v3.scale(2f/3f);
				p3_c5_.add(v3);
				v3.scale(1f/2f);
				p3_c4_.add(v3);
				v3 = Functions.vector(ap3CoonsPatch[0],ap3CoonsPatch[8]);
				v3.scale(2f/3f);
				p3_c1_.add(v3);
				v3.scale(1f/2f);
				p3_c2_.add(v3);
				ap3Helper = Bezier.deCasteljau(ap3CoonsPatch[1],p3_c1_,p3_c5_,ap3CoonsPatch[5],0.5f);
				p3_c1_c5 = ap3Helper[3];
				ap3Helper = Bezier.deCasteljau(ap3CoonsPatch[2],p3_c2_,p3_c4_,ap3CoonsPatch[4],0.5f);
				p3_c2_c4 = ap3Helper[3];
				//p3_c1_c5.interpolate(p3_c1_,p3_c5_,0.5f);
				//p3_c2_c4.interpolate(p3_c2_,p3_c4_,0.5f);
				
				/*        
				p3_c1_c8.interpolate(ap3CoonsPatch[1],ap3CoonsPatch[8],0.5f);
				p3_c2_c7.interpolate(ap3CoonsPatch[2],ap3CoonsPatch[7],0.5f);
				p3_c2_c4.interpolate(ap3CoonsPatch[2],ap3CoonsPatch[4],0.5f);
				p3_c1_c5.interpolate(ap3CoonsPatch[1],ap3CoonsPatch[5],0.5f);
				
				
				BezierTriangle bt = new BezierTriangle(ap3CoonsPatch[0],ap3CoonsPatch[1],ap3CoonsPatch[2],ap3CoonsPatch[3],ap3CoonsPatch[4],ap3CoonsPatch[5],ap3CoonsPatch[6],ap3CoonsPatch[7],ap3CoonsPatch[8]);
				System.out.println("Center: " + bt.getCenterPoint());
				
				Point3f p3_18 = new Point3f();
				ap3Helper = Bezier.deCasteljau(p3_9,p3_c2_c7,p3_c1_c8,p3_0,1f/3f);
				Point3f p3_20 = ap3Helper[1];
				Point3f p3_19 = ap3Helper[2];
				//Point3f p3_18 = ap3Helper[3];
				//ap3Helper[3].scale(1f/3f);
				p3_18.add(ap3Helper[3]);
				Point3f p3_25 = ap3Helper[4];
				Point3f p3_28 = ap3Helper[5];
				System.out.println("p 18\n-----------------");
				System.out.println(ap3Helper[3]);
				
				ap3Helper = Bezier.deCasteljau(p3_15,p3_c1_c5,p3_c2_c4,p3_6,1f/3f);
				Point3f p3_22 = ap3Helper[1];
				Point3f p3_21 = ap3Helper[2];
				Point3f p3_26 = ap3Helper[4];
				Point3f p3_29 = ap3Helper[5];
				//ap3Helper[3].scale(1f/3f);
				//p3_18.add(ap3Helper[3]);
				System.out.println(ap3Helper[3]);
				
				ap3Helper = Bezier.deCasteljau(p3_3,p3_c4_c8,p3_c5_c7,p3_12,1f/3f);
				Point3f p3_24 = ap3Helper[1];
				Point3f p3_23 = ap3Helper[2];
				Point3f p3_27 = ap3Helper[4];
				Point3f p3_30 = ap3Helper[5];
				//ap3Helper[3].scale(1f/3f);
				//p3_18.add(ap3Helper[3]);
				System.out.println(ap3Helper[3]);
				
				
				v3_a = Functions.vector(p3_0,p3_17);
				v3_b = Functions.vector(p3_6,p3_7);
				v3_3.interpolate(v3_a,v3_b,0.5f);
				v3_3.scale(0.5f);
				Point3f p3_24 = new Point3f(p3_3);
				p3_24.add(v3_3);
				
				v3_a = Functions.vector(p3_6,p3_5);
				v3_b = Functions.vector(p3_12,p3_13);
				v3_9.interpolate(v3_a,v3_b,0.5f);
				v3_9.scale(0.5f);
				Point3f p3_20 = new Point3f(p3_9);
				p3_20.add(v3_9);
				
				v3_a = Functions.vector(p3_0,p3_1);
				v3_b = Functions.vector(p3_12,p3_11);
				v3_15.interpolate(v3_a,v3_b,0.5f);
				v3_15.scale(0.5f);
				Point3f p3_22 = new Point3f(p3_15);
				p3_22.add(v3_15);
				/*
				v3_3.scale(2f);
				v3_9.scale(2f);
				v3_15.scale(2f);
				
				Point3f p3_3_ = new Point3f(p3_3);
				Point3f p3_9_ = new Point3f(p3_9);
				Point3f p3_15_ = new Point3f(p3_15);
				
				p3_3_.add(v3_3);
				p3_9_.add(v3_9);
				p3_15_.add(v3_15);
				
				p3_3_.scale(1f/3f);
				p3_9_.scale(1f/3f);
				p3_15_.scale(1f/3f);
				
				//Point3f p3_18 = new Point3f(p3_3_);
				//p3_18.add(p3_9_);
				//p3_18.add(p3_15_);
				/*
				Plane plane = new Plane(p3_18,Plane.normal(p3_0,p3_6,p3_12));
				
				v3_3.scale(4f/3f);
				v3_9.scale(4f/3f);
				v3_15.scale(4f/3f);
				
				p3_3_ = new Point3f(p3_3);
				p3_9_ = new Point3f(p3_9);
				p3_15_ = new Point3f(p3_15);
				
				p3_3_.add(v3_3);
				p3_9_.add(v3_9);
				p3_15_.add(v3_15);
				
				Point3f p3_23 = plane.projectedPoint(p3_3_);
				Point3f p3_19 = plane.projectedPoint(p3_9_);
				Point3f p3_21 = plane.projectedPoint(p3_15_);
				
				
				
				
				
				Plane plane = new Plane(p3_18,Plane.normal(p3_19,p3_21,p3_23));
				p3_19 = plane.projectedPoint(p3_19);
				p3_21 = plane.projectedPoint(p3_21);
				p3_23 = plane.projectedPoint(p3_23);
				
				p3_25 = (new Plane(p3_18,p3_21,p3_23)).projectedPoint(p3_25);
				p3_26 = (new Plane(p3_18,p3_23,p3_19)).projectedPoint(p3_26);
				p3_27 = (new Plane(p3_18,p3_19,p3_21)).projectedPoint(p3_27);
				
				Line2f line1;
				Line2f line2;
				line1 = new Line2f(plane.getP2(p3_18),plane.getP2(p3_19));
				line2 = new Line2f(plane.getP2(p3_26),plane.getP2(p3_27));
				p3_19 = plane.getP3(line1.intersection(line2));
				line1 = new Line2f(plane.getP2(p3_18),plane.getP2(p3_21));
				line2 = new Line2f(plane.getP2(p3_27),plane.getP2(p3_25));
				p3_21 = plane.getP3(line1.intersection(line2));
				line1 = new Line2f(plane.getP2(p3_18),plane.getP2(p3_23));
				line2 = new Line2f(plane.getP2(p3_25),plane.getP2(p3_26));
				p3_23 = plane.getP3(line1.intersection(line2));
				
				
				Vector3f v3_0_9 = Functions.vector(p3_0,p3_9);
				Vector3f v3_6_15 = Functions.vector(p3_6,p3_15);
				Vector3f v3_12_3 = Functions.vector(p3_12,p3_3);
				v3_0_9.normalize();
				v3_6_15.normalize();
				v3_12_3.normalize();
				
				Point3f p3_19 = new Point3f(p3_18);
				Vector3f v3_19 = new Vector3f(v3_0_9);
				v3_19.scale(p3_18.distance(p3_9));
				v3_19.scale(1f/3f);
				p3_19.add(v3_19);
				Point3f p3_25 = new Point3f(p3_18);
				Vector3f v3_25 = new Vector3f(v3_0_9);
				v3_25.scale(p3_18.distance(p3_0));
				v3_25.scale(-1f/3f);
				p3_25.add(v3_25);
				
				Point3f p3_21 = new Point3f(p3_18);
				Vector3f v3_21 = new Vector3f(v3_6_15);
				v3_21.scale(p3_18.distance(p3_15));
				v3_21.scale(1f/3f);
				p3_21.add(v3_21);
				Point3f p3_26 = new Point3f(p3_18);
				Vector3f v3_26 = new Vector3f(v3_6_15);
				v3_26.scale(p3_18.distance(p3_6));
				v3_26.scale(-1f/3f);
				p3_26.add(v3_26);
				
				Point3f p3_23 = new Point3f(p3_18);
				Vector3f v3_23 = new Vector3f(v3_12_3);
				v3_23.scale(p3_18.distance(p3_3));
				v3_23.scale(1f/3f);
				p3_23.add(v3_23);
				Point3f p3_27 = new Point3f(p3_18);
				Vector3f v3_27 = new Vector3f(v3_12_3);
				v3_27.scale(p3_18.distance(p3_12));
				v3_27.scale(-1f/3f);
				p3_27.add(v3_27);
				
				
				
				Plane plane = new Plane(p3_18,Plane.normal(p3_19,p3_21,p3_23));
				p3_19 = plane.projectedPoint(p3_19);
				p3_21 = plane.projectedPoint(p3_21);
				p3_23 = plane.projectedPoint(p3_23);
				
				Line2f line1;
				Line2f line2;
				line1 = new Line2f(plane.getP2(p3_18),plane.getP2(p3_19));
				line2 = new Line2f(plane.getP2(p3_26),plane.getP2(p3_27));
				p3_19 = plane.getP3(line1.intersection(line2));
				line1 = new Line2f(plane.getP2(p3_18),plane.getP2(p3_21));
				line2 = new Line2f(plane.getP2(p3_27),plane.getP2(p3_25));
				p3_21 = plane.getP3(line1.intersection(line2));
				line1 = new Line2f(plane.getP2(p3_18),plane.getP2(p3_23));
				line2 = new Line2f(plane.getP2(p3_25),plane.getP2(p3_26));
				p3_23 = plane.getP3(line1.intersection(line2));
				*/
				/*
				aap3Patches = new Point3f[3][16];
				
				aap3Patches[0][0] = new Point3f(p3_0);
				aap3Patches[0][1] = new Point3f(p3_1);
				aap3Patches[0][2] = new Point3f(p3_2);
				aap3Patches[0][3] = new Point3f(p3_3);
				aap3Patches[0][7] = new Point3f(p3_24);
				aap3Patches[0][11] = new Point3f(p3_23);
				aap3Patches[0][15] = new Point3f(p3_18);
				aap3Patches[0][14] = new Point3f(p3_21);
				aap3Patches[0][13] = new Point3f(p3_22);
				aap3Patches[0][12] = new Point3f(p3_15);
				aap3Patches[0][8] = new Point3f(p3_16);
				aap3Patches[0][4] = new Point3f(p3_17);
				BezierPatch.computeInnerControlPoints(aap3Patches[0]);
				
				//aap3Patches[0][5] = parallelogram(aap3Patches[0][0],aap3Patches[0][1],aap3Patches[0][4]);
				//aap3Patches[0][6] = parallelogram(aap3Patches[0][3],aap3Patches[0][2],aap3Patches[0][7]);
				//aap3Patches[0][9] = parallelogram(aap3Patches[0][12],aap3Patches[0][8],aap3Patches[0][13]);
				
				aap3Patches[1][0] = new Point3f(p3_6);
				aap3Patches[1][1] = new Point3f(p3_7);
				aap3Patches[1][2] = new Point3f(p3_8);
				aap3Patches[1][3] = new Point3f(p3_9);
				aap3Patches[1][7] = new Point3f(p3_20);
				aap3Patches[1][11] = new Point3f(p3_19);
				aap3Patches[1][15] = new Point3f(p3_18);
				aap3Patches[1][14] = new Point3f(p3_23);
				aap3Patches[1][13] = new Point3f(p3_24);
				aap3Patches[1][12] = new Point3f(p3_3);
				aap3Patches[1][8] = new Point3f(p3_4);
				aap3Patches[1][4] = new Point3f(p3_5);
				BezierPatch.computeInnerControlPoints(aap3Patches[1]);
				
				//aap3Patches[1][5] = parallelogram(aap3Patches[1][0],aap3Patches[1][1],aap3Patches[1][4]);
				//aap3Patches[1][6] = parallelogram(aap3Patches[1][3],aap3Patches[1][2],aap3Patches[1][7]);
				//aap3Patches[1][9] = parallelogram(aap3Patches[1][12],aap3Patches[1][8],aap3Patches[1][13]);
				
				aap3Patches[2][0] = new Point3f(p3_12);
				aap3Patches[2][1] = new Point3f(p3_13);
				aap3Patches[2][2] = new Point3f(p3_14);
				aap3Patches[2][3] = new Point3f(p3_15);
				aap3Patches[2][7] = new Point3f(p3_22);
				aap3Patches[2][11] = new Point3f(p3_21);
				aap3Patches[2][15] = new Point3f(p3_18);
				aap3Patches[2][14] = new Point3f(p3_19);
				aap3Patches[2][13] = new Point3f(p3_20);
				aap3Patches[2][12] = new Point3f(p3_9);
				aap3Patches[2][8] = new Point3f(p3_10);
				aap3Patches[2][4] = new Point3f(p3_11);
				BezierPatch.computeInnerControlPoints(aap3Patches[2]);
				
				//aap3Patches[2][5] = parallelogram(aap3Patches[2][0],aap3Patches[2][1],aap3Patches[2][4]);
				//aap3Patches[2][6] = parallelogram(aap3Patches[2][3],aap3Patches[2][2],aap3Patches[2][7]);
				//aap3Patches[2][9] = parallelogram(aap3Patches[2][12],aap3Patches[2][8],aap3Patches[2][13]);

				//Plane plane = new Plane(p3_18,Plane.normal(p3_19,p3_21,p3_23));
				
				aap3Patches[0][10] = new Point3f(p3_25);
				aap3Patches[1][10] = new Point3f(p3_26);
				aap3Patches[2][10] = new Point3f(p3_27);
				/*
				aap3Patches[0][5] = new Point3f(p3_28);
				aap3Patches[1][5] = new Point3f(p3_29);
				aap3Patches[2][5] = new Point3f(p3_30);
				*/
				/*
				aap3Patches[0][10] = plane.projectedPoint(p3_25);
				aap3Patches[1][10] = plane.projectedPoint(p3_26);
				aap3Patches[2][10] = plane.projectedPoint(p3_27);
				
				/*
				aap3Patches[0][5] = plane.projectedPoint(p3_28);
				aap3Patches[1][5] = plane.projectedPoint(p3_29);
				aap3Patches[2][5] = plane.projectedPoint(p3_30);
				/
				/*
				aap3Patches[0][10] = new Point3f(p3_25);
				aap3Patches[1][10] = new Point3f(p3_26);
				aap3Patches[2][10] = new Point3f(p3_27);
				*/
				/*
				aap3Patches[0][5] = new Point3f(p3_28);
				aap3Patches[1][5] = new Point3f(p3_29);
				aap3Patches[2][5] = new Point3f(p3_30);
				*/
				/*
				aap3Patches[0][7].interpolate(aap3Patches[0][6],aap3Patches[1][9],0.5f);
				aap3Patches[1][13].set(aap3Patches[0][7]);
				//aap3Patches[0][11].interpolate(aap3Patches[0][10],aap3Patches[1][10],0.5f);
				//aap3Patches[1][14].set(aap3Patches[0][11]);
				
				aap3Patches[1][7].interpolate(aap3Patches[1][6],aap3Patches[2][9],0.5f);
				aap3Patches[2][13].set(aap3Patches[1][7]);
				//aap3Patches[1][11].interpolate(aap3Patches[1][10],aap3Patches[2][10],0.5f);
				//aap3Patches[2][14].set(aap3Patches[1][11]);
				
				aap3Patches[2][7].interpolate(aap3Patches[2][6],aap3Patches[0][9],0.5f);
				aap3Patches[0][13].set(aap3Patches[2][7]);
				//aap3Patches[2][11].interpolate(aap3Patches[2][10],aap3Patches[0][10],0.5f);
				//aap3Patches[0][14].set(aap3Patches[2][11]);
				*/
				//System.out.println(p3_19 + " " + p3_20 + " " + p3_21 + " " + p3_22 + " " + p3_23 + " " + p3_24);
			break;
		}
		return aap3Patches;
	}
}
