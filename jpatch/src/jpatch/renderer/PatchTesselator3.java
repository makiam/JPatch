package jpatch.renderer;

import java.util.*;
import javax.vecmath.*;
import jpatch.entity.*;

public class PatchTesselator3 implements HashPatchSubdivision.QuadDrain {
	
	private HashPatchSubdivision hashPatchSubdivision;
	private JPatchMaterial material;
	private ArrayList matTriangleList = new ArrayList();
	private ArrayList matQuadList = new ArrayList();
	private ArrayList listAveragedNormals = new ArrayList();
	private ArrayList listVertices = new ArrayList();
	private Matrix4d matrix;
	
	private HashMap mapVertices = new HashMap();
	
	//private Point3f p3GridMin = new Point3f();
	//private Point3f p3GridMax = new Point3f();
	//private Vector3f v3GridSize = new Vector3f();
	private ArrayList listQuads = new ArrayList();
	
	private static final Vector3f v3_1 = new Vector3f();
	private static final Vector3f v3_2 = new Vector3f();
	static final double EPSILON = 0.0001f;
	
	private int iVertexNumber;
	private static boolean bExportNormals;
	
	public void tesselate(Model model, int subdiv, Matrix4d matrix, boolean exportNormals) {
		bExportNormals = exportNormals;
		this.matrix = matrix;
		hashPatchSubdivision = new HashPatchSubdivision(0.0f, subdiv, this);
		
		model.computePatches();
		model.setReferenceGeometry();
		
		//model.unapplyMorphs();
		//mapReferenceGeometry = new HashMap();
		//mapReferenceCp = new HashMap();
		//for (Patch patch = model.getFirstPatch(); patch != null; ) {
		//	Point3f[] hashPatch = patch.coonsPatch();
		//	Point3f[] copy = new Point3f[hashPatch.length];
		//	for (int i = 0; i < hashPatch.length; copy[i] = new Point3f(hashPatch[i++]));
		//	mapReferenceGeometry.put(patch, copy);
		//	
		//	ControlPoint[] acp = patch.getControlPoints();
		//	for (int i = 0; i < acp.length; i++) {
		//		mapReferenceCp.put(acp[i], new Point3f[] { acp[i].getPosition(), acp[i].getInTangent(), acp[i].getOutTangent() });
		//	}
		//	patch = patch.getNext();
		//}
		//model.applyMorphs();
	
		Vector3f v3a = new Vector3f();
		Vector3f v3b = new Vector3f();
		Vector3f[] normals = new Vector3f[] {new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};
		
		//mapVertices = new HashMap();
		//VertexNumber = 0;
		//listTriangles = new ArrayList();
		//mapMaterials = new HashMap();
		//mapNormals = new HashMap();
		
		mapVertices.clear();
		listVertices.clear();
		listQuads.clear();
		listAveragedNormals.clear();
		iVertexNumber = 0;
		
		for (Iterator iterator = model.getMaterialList().iterator(); iterator.hasNext();) {
			material = (JPatchMaterial)iterator.next();
			
			for (Iterator it = model.getPatchSet().iterator(); it.hasNext(); ) {
				Patch patch = (Patch) it.next();
				if (patch.getMaterial() == material) {
					
					Point3f[] hashPatch = patch.coonsPatch();
					Point3f[] referencePatch = patch.referenceCoonsPatch();
					//Point3f[] referencePatch = (mapReferenceGeometry == null) ? null : (Point3f[]) mapReferenceGeometry.get(patch);
					//if (referencePatch == null) {
					//	System.out.println("reference = null");
					//	referencePatch = new Point3f[hashPatch.length];
					//	for (int i = 0; i < hashPatch.length; referencePatch[i] = new Point3f(hashPatch[i++]));
					//}
					ControlPoint[] acp = patch.getControlPoints();
               
					//if (hashPatch.length == 9) {
					//	int apex = -1;
					//	for (int i = 0; i < 6; i += 2) {
					//		if (apex == -1 && acp[i].getHead().getStack().length > 2) apex = i;
					//	}
					//	if (apex != -1) {
					//		//System.out.print("apex " + apex + "   " + "cp = " + acpPoint[apex] + "   ");
					//		apex = apex / 2;
					//		Patch.shift(hashPatch,apex * 3 + 3);
					//	}
					//}
					
					/* set up corner normals */
					
					//int[] levels = new int[patch.getType()];
					Point3d[][] aap3HookCurve = new Point3d[patch.getType()][];
					Point3d[][] aap3RefHookCurve = new Point3d[patch.getType()][];
					Vector3f[][] aav3HookNormals = new Vector3f[patch.getType()][];
					
					//for (int i = 0; i < patch.getType(); levels[i++] = 0);
					for (int i = 0, n = patch.getType(), pl = hashPatch.length, cn = n * 2; i < n; i++) {
						boolean reversePatch = false;
						
							
						int i2 = i * 2;
						ControlPoint targetHook = null;
						ControlPoint hookCurveStart = acp[i2].getStart().getParentHook();
						if (hookCurveStart != null) {
							if (acp[(i2 + cn -1) % cn].isTargetHook()) {
								targetHook = acp[(i2 + cn - 1) % cn];
							}
							if (acp[(i2 + 2) % cn].isTargetHook()) {
								targetHook = acp[(i2 + 2) % cn];
							}
							Vector3f v3Dir = new Vector3f();
							Point3f p = targetHook.getRefPosition();
							if (targetHook.getNext() != null) v3Dir.sub(targetHook.getNext().getRefPosition(), p);
							else v3Dir.sub(targetHook.getPrev().getRefPosition(), p);
							Vector3f v3Start = new Vector3f(v3Dir);
							Vector3f v3End = new Vector3f(v3Dir);
							targetHook.computeTargetHookReferenceBorderTangents(v3Dir, v3Start, v3End);
							ControlPoint cpStart = targetHook.getHead().getStart().getParentHook();
							ControlPoint cpEnd = cpStart.getNext();
							Point3f p0 = new Point3f(cpStart.getPosition());
							Point3f p1 = new Point3f(cpStart.getOutTangent());
							Point3f p2 = new Point3f(cpEnd.getInTangent());
							Point3f p3 = new Point3f(cpEnd.getPosition());
							
							aap3HookCurve[i] = hashPatchSubdivision.subdivCurve(new Point3d(p0), new Point3d(p1), new Point3d(p2), new Point3d(p3));
							
							Point3f r0 = new Point3f(cpStart.getRefPosition());
							Point3f r1 = new Point3f(cpStart.getRefOutTangent());
							Point3f r2 = new Point3f(cpEnd.getRefInTangent());
							Point3f r3 = new Point3f(cpEnd.getRefPosition());
							
							aap3RefHookCurve[i] = hashPatchSubdivision.subdivCurve(new Point3d(r0), new Point3d(r1), new Point3d(r2), new Point3d(r3));
							
							
							Vector3f v = new Vector3f();
							Vector3f n1 = new Vector3f();
							Vector3f n2 = new Vector3f();
							v.sub(r1, r0);
							n1.cross(v, v3Start);
							n1.normalize();
							v.sub(r3, r2);
							n2.cross(v, v3End);
							n2.normalize();
							
							ControlPoint hook = targetHook.getHead();
							loop:
							for (int ii = 0; ii < acp.length; ii++) {
								if (acp[ii] == hook) {
									int ip = (ii + 1) % acp.length;
									int im = (ii + acp.length - 1) % acp.length;
									reversePatch = (acp[ii].getNext() == acp[ip] || acp[ii].getPrev() == acp[im]);
									break loop;
								}
							}
							
							if (!reversePatch) {
								n1.scale(-1);
								n2.scale(-1);
							}
							aav3HookNormals[i] = hashPatchSubdivision.interpolateNormals(n1, n2);
							targetHook = null;
						}
						
						if (acp[i2].isTargetHook()) {
							targetHook = acp[i2];
							//levels[(i + n - 1) % n] = 1;
						}
						if (acp[(i2 + cn - 1) % cn].isTargetHook()) {
							targetHook = acp[(i2 + cn - 1) % cn];
							//levels[i] = 1;
						}
						
						if (targetHook == null) {
							int p = i * 3;
							//v3a.sub(hashPatch[(p + 1) % pl], hashPatch[p]);
							//v3b.sub(hashPatch[(p + pl - 1) % pl], hashPatch[p]);
							if (referencePatch[p].equals(referencePatch[(p + 3) % pl])) {
								v3a.sub(referencePatch[(p + 4) % pl], referencePatch[p]);
							} else  {
								v3a.sub(referencePatch[(p + 1) % pl], referencePatch[p]);
							}
							if (referencePatch[p].equals(referencePatch[(p + pl - 3) % pl])) {
								v3b.sub(referencePatch[(p + pl - 4) % pl], referencePatch[p]);
							} else {
								v3b.sub(referencePatch[(p + pl - 1) % pl], referencePatch[p]);
							}
							normals[i].cross(v3a, v3b);
							normals[i].normalize();
						}
						else {
							ControlPoint hook = targetHook.getHead();
							loop:
							for (int ii = 0; ii < acp.length; ii++) {
								if (acp[ii] == hook) {
									int ip = (ii + 1) % acp.length;
									int im = (ii + acp.length - 1) % acp.length;
									reversePatch = (acp[ii].getNext() == acp[ip] || acp[ii].getPrev() == acp[im]);
									break loop;
								}
							}
							
							Vector3f v3Dir = new Vector3f();
							Point3f p = targetHook.getRefPosition();
							if (targetHook.getNext() != null) v3Dir.sub(targetHook.getNext().getRefPosition(), p);
							else v3Dir.sub(targetHook.getPrev().getRefPosition(), p);
							Vector3f v3Start = new Vector3f(v3Dir);
							Vector3f v3End = new Vector3f(v3Dir);
							targetHook.computeTargetHookReferenceBorderTangents(v3Dir, v3Start, v3End);
							ControlPoint cpStart = targetHook.getHead().getStart().getParentHook();
							ControlPoint cpEnd = cpStart.getNext();
							Point3f r0 = cpStart.getRefPosition();
							Point3f r1 = cpStart.getRefOutTangent();
							Point3f r2 = cpEnd.getRefInTangent();
							Point3f r3 = cpEnd.getRefPosition();
							Vector3f v = new Vector3f();
							Vector3f n1 = new Vector3f();
							Vector3f n2 = new Vector3f();
							v.sub(r1, r0);
							n1.cross(v, v3Start);
							n1.normalize();
							v.sub(r3, r2);
							n2.cross(v, v3End);
							n2.normalize();
							Vector3f ncenter = hashPatchSubdivision.interpolateNormal(n1, n2);
							
							float hookpos = targetHook.getHead().getHookPos();
							
							if (hookpos == 0.5f) v.set(ncenter);
							else if (hookpos == 0.25f) v = hashPatchSubdivision.interpolateNormal(n1, ncenter);
							else v = hashPatchSubdivision.interpolateNormal(ncenter, n2);
							//m4View.transform(v);
							v.normalize();
							//if (!reversePatch) v.scale(-1f);
							normals[i].set(v);
							//normals[i].set(0,0,0);
						}
							
					}
					//if (matrix != null) {
					//	for (int i = 0; i < hashPatch.length; matrix.transform(hashPatch[i++]));
					//	for (int i = 0; i < normals.length; matrix.transform(normals[i++]));
					//}
					//for (int i = 0; i < hashPatch.length; System.out.println("patch p" + i + "\t" + hashPatch[i++]));
					//for (int i = 0; i < normals.length; System.out.println("patch n" + i + "\t" + normals[i++]));
					//System.out.println();
					hashPatchSubdivision.subdivHashPatch(hashPatch, referencePatch, normals, aap3HookCurve, aap3RefHookCurve, aav3HookNormals);
				}
			}
		}
	
		//aVertices = (Vertex[]) mapVertices.keySet().toArray(new Vertex[0]);
		//Arrays.sort(aVertices);
		//for (int i = 0; i < aVertices.length; i++) {
		//	Vector3f normal = (Vector3f) mapNormals.get(new Integer(i));
		//	normal.normalize();
		//	if (!Float.isNaN(normal.x) && !Float.isNaN(normal.y) && !Float.isNaN(normal.z)) aVertices[i].n.set(normal);
		//}
		if (bExportNormals) {
			Iterator itN = listAveragedNormals.iterator();
			for (Iterator itV = listVertices.iterator(); itV.hasNext(); ) {
				Vertex v = (Vertex) itV.next();
				Vector3f n = (Vector3f) itN.next();
				//if (average) {
					n.normalize();
					if (!Float.isNaN(n.x) && !Float.isNaN(n.y) && !Float.isNaN(n.z)) v.n = n;
					else {
						v.n.set(0,0,0);
						System.out.println("NaN! at " + v.p);
					}
				//}
			}
		}
	}
	
	//public Vertex[] getVertexArray() {
	//	return aVertices;
	//}
	//
	//public Triangle[] getTriangleArray() {
	//	return (Triangle[]) listTriangles.toArray(new Triangle[0]);
	//}
	//
	//public Vertex[] getPerMaterialVertexArray(JPatchMaterial material) {
	//	Vertex[] vertices = getVertexArray();
	//	int[] map = new int[vertices.length];
	//	for (int m = 0; m < map.length; map[m++] = -1);
	//	matTriangleList = new ArrayList();
	//	int n = 0;
	//	for (Iterator it = listTriangles.iterator(); it.hasNext(); ) {
	//		Triangle triangle = (Triangle) it.next();
	//		if (triangle.mat == material) {
	//			if (map[triangle.v0] == -1) map[triangle.v0] = n++;
	//			if (map[triangle.v1] == -1) map[triangle.v1] = n++;
	//			if (map[triangle.v2] == -1) map[triangle.v2] = n++;
	//			matTriangleList.add(new int[] { map[triangle.v0], map[triangle.v1], map[triangle.v2] });
	//		}
	//	}
	//	Vertex[] vtx = new PatchTesselator.Vertex[n];
	//	for (int v = 0; v < vertices.length; v++) if (map[v] != -1) vtx[map[v]] = vertices[v];
	//	return vtx;
	//}
	//
	//public int[][] getPerMaterialTriangleArray() {
	//	return (int[][]) matTriangleList.toArray(new int[0][3]);
	//}
	
	public Vertex[] getVertexArray() {
		return (Vertex[]) listVertices.toArray(new Vertex[0]);
	}
	
	public Vertex[] getPerMaterialVertexArray(JPatchMaterial material) {
	//	Vertex[] vertices = getVertexArray();
		int[] map = new int[listVertices.size()];
		for (int m = 0; m < map.length; map[m++] = -1);
		matTriangleList.clear();
		matQuadList.clear();
		int n = 0;
		for (Iterator it = listQuads.iterator(); it.hasNext(); ) {
			Quad quad = (Quad) it.next();
			if (quad.material == material) {
				if (map[quad.i0] == -1) map[quad.i0] = n++;
				if (map[quad.i1] == -1) map[quad.i1] = n++;
				if (map[quad.i2] == -1) map[quad.i2] = n++;
				if (map[quad.i3] == -1) map[quad.i3] = n++;
				
				int[] t;
				if (!quad.isTriangle()) {
					t = quad.getTriangle1();
					matTriangleList.add(new int[] { map[t[0]], map[t[1]], map[t[2]] });
					t = quad.getTriangle2();
					matTriangleList.add(new int[] { map[t[0]], map[t[1]], map[t[2]] });
				} else {
					t = quad.getQuad();
					matTriangleList.add(new int[] { map[t[0]], map[t[1]], map[t[2]] });
				}
				t = quad.getQuad();
				int[] v = new int[t.length];
				for (int i = 0; i < t.length; v[i] = map[t[i++]]);
				matQuadList.add(v);
			}
		}
		Vertex[] vtx = new Vertex[n];
		for (int v = 0, s = listVertices.size(); v < s; v++) {
			if (map[v] != -1) {
				Vertex v2 = (Vertex) listVertices.get(v);
				vtx[map[v]] = new Vertex(v2.p, v2.r, v2.n);
			}
		}
		return vtx;
	}
	
	public int[][] getPerMaterialTriangleArray() {
		return (int[][]) matTriangleList.toArray(new int[0][0]);
	}
	
	public int[][] getPerMaterialQuadArray() {
		return (int[][]) matQuadList.toArray(new int[0][0]);
	}
	
	public int[][] getMaterialTriangleArray(JPatchMaterial material) {
		ArrayList triangleList = new ArrayList();
		for (Iterator it = listQuads.iterator(); it.hasNext(); ) {
			Quad quad = (Quad) it.next();
			if (quad.material == material) {
				if (!quad.isTriangle()) {
					triangleList.add(quad.getTriangle1());
					triangleList.add(quad.getTriangle2());
				} else triangleList.add(quad.getQuad());
			}
		}
		return (int[][]) triangleList.toArray(new int[0][0]);
	}
	
	public int[][] getMaterialQuadArray(JPatchMaterial material) {
		ArrayList quadList = new ArrayList();
		for (Iterator it = listQuads.iterator(); it.hasNext(); ) {
			Quad quad = (Quad) it.next();
			if (quad.material == material) {
				quadList.add(quad.getQuad());
			}
		}
		return (int[][]) quadList.toArray(new int[0][0]);
	}
	
	public List getVertexList() {
		return listVertices;
	}
	
	public List getQuadList() {
		return listQuads;	
	}
	
	public void addQuad(Point3d p0, Point3d p1, Point3d p2, Point3d p3, Point3d r0, Point3d r1, Point3d r2, Point3d r3, Vector3f n0, Vector3f n1, Vector3f n2, Vector3f n3) {
		//System.out.println("quad: \np:" + p0 + "\n" + p1 + "\n" + p2 + "\n" + p3 + "\n");
		//System.out.println("quad: \nr:" + r0 + "\n" + r1 + "\n" + r2 + "\n" + r3 + "\n");
		//System.out.println("quad: \nn:" + n0 + "\n" + n1 + "\n" + n2 + "\n" + n3 + "\n");
		if (matrix != null) {
			matrix.transform(p0);
			matrix.transform(p1);
			matrix.transform(p2);
			matrix.transform(p3);
			matrix.transform(r0);
			matrix.transform(r1);
			matrix.transform(r2);
			matrix.transform(r3);
			matrix.transform(n0);
			matrix.transform(n1);
			matrix.transform(n2);
			matrix.transform(n3);
		}
		
		Vertex v0 = new Vertex(p0, r0, n0);
		Vertex v1 = new Vertex(p1, r1, n1);
		Vertex v2 = new Vertex(p2, r2, n2);
		Vertex v3 = new Vertex(p3, r3, n3);
		
		//System.out.println("out: " + v0.number + " " + v0.p + "\n" + v1.number + " " + v1.p + "\n" + v2.number + " " + v2.p + "\n" + v3.number + " " + v3.p + "\n");
		
		int i0 = getVertexNumber(v0);
		int i1 = getVertexNumber(v1);
		int i2 = getVertexNumber(v2);
		int i3 = getVertexNumber(v3);
		
		//System.out.println(i0 + " " + i1 + " " + i2 + " " + i3);
		
		
		if ((i0 != i2) && (i1 != i3)) {
			if (bExportNormals) {
				Vector3f an0 = (Vector3f) listAveragedNormals.get(i0);
				Vector3f an1 = (Vector3f) listAveragedNormals.get(i1);
				Vector3f an2 = (Vector3f) listAveragedNormals.get(i2);
				Vector3f an3 = (Vector3f) listAveragedNormals.get(i3);
				if (i0 == i1) {
					addNormal(an0, getCornerNormal(v0.p, v2.p, v3.p));
					addNormal(an2, getCornerNormal(v2.p, v3.p, v1.p));
					addNormal(an3, getCornerNormal(v3.p, v0.p, v2.p));
				} else if (i1 == i2) {
					addNormal(an0, getCornerNormal(v0.p, v1.p, v3.p));
					addNormal(an1, getCornerNormal(v1.p, v3.p, v0.p));
					addNormal(an3, getCornerNormal(v3.p, v0.p, v2.p));
				} else if (i2 == i3) {
					addNormal(an0, getCornerNormal(v0.p, v1.p, v3.p));
					addNormal(an1, getCornerNormal(v1.p, v2.p, v0.p));
					addNormal(an2, getCornerNormal(v2.p, v0.p, v1.p));
				} else if (i3 == i0) {
					addNormal(an1, getCornerNormal(v1.p, v2.p, v0.p));
					addNormal(an2, getCornerNormal(v2.p, v0.p, v1.p));
					addNormal(an3, getCornerNormal(v3.p, v1.p ,v2.p));
				} else {
					addNormal(an0, getCornerNormal(v0.p, v1.p, v3.p));
					addNormal(an1, getCornerNormal(v1.p, v2.p, v0.p));
					addNormal(an2, getCornerNormal(v2.p, v3.p, v1.p));
					addNormal(an3, getCornerNormal(v3.p, v0.p, v2.p));
				}
			}
			if (v1.p.distanceSquared(v3.p) < v0.p.distanceSquared(v2.p)) listQuads.add(new Quad(i0, i1, i2, i3, material));
			else listQuads.add(new Quad(i1, i2, i3, i0, material));
		}
	}

	private int getVertexNumber(Vertex v) {
		Integer number = (Integer) mapVertices.get(v);
		if (number == null) {
			mapVertices.put(v, new Integer(iVertexNumber));
			listVertices.add(v);
			if (bExportNormals) listAveragedNormals.add(new Vector3f());
			return iVertexNumber++;
		} else {
			return number.intValue();
		}
	}
	
	//private Vertex getVertex(Point3f p, Point3f r, Vector3f n) {
	//	GridPos gridPos = getGridPos(p);
	//	ArrayList VertexList;
	//	VertexList = (ArrayList) mapGrid.get(gridPos);
	//	if (VertexList != null) for (int i = 0, s = VertexList.size(); i < s; i++) {
	//		Vertex v = (Vertex) VertexList.get(i);
	//		if (v.closeTo(p, r, n)) return v;
	//	}
	//	VertexList = (ArrayList) mapGrid.get(new GridPos(gridPos.x - 1, gridPos.y, gridPos.z));
	//	if (VertexList != null) for (int i = 0, s = VertexList.size(); i < s; i++) {
	//		Vertex v = (Vertex) VertexList.get(i);
	//		if (v.closeTo(p, r, n)) return v;
	//	}
	//	VertexList = (ArrayList) mapGrid.get(new GridPos(gridPos.x + 1, gridPos.y, gridPos.z));
	//	if (VertexList != null) for (int i = 0, s = VertexList.size(); i < s; i++) {
	//		Vertex v = (Vertex) VertexList.get(i);
	//		if (v.closeTo(p, r, n)) return v;
	//	}
	//	VertexList = (ArrayList) mapGrid.get(new GridPos(gridPos.x, gridPos.y - 1, gridPos.z));
	//	if (VertexList != null) for (int i = 0, s = VertexList.size(); i < s; i++) {
	//		Vertex v = (Vertex) VertexList.get(i);
	//		if (v.closeTo(p, r, n)) return v;
	//	}
	//	VertexList = (ArrayList) mapGrid.get(new GridPos(gridPos.x, gridPos.y + 1, gridPos.z));
	//	if (VertexList != null) for (int i = 0, s = VertexList.size(); i < s; i++) {
	//		Vertex v = (Vertex) VertexList.get(i);
	//		if (v.closeTo(p, r, n)) return v;
	//	}
	//	VertexList = (ArrayList) mapGrid.get(new GridPos(gridPos.x, gridPos.y, gridPos.z - 1));
	//	if (VertexList != null) for (int i = 0, s = VertexList.size(); i < s; i++) {
	//		Vertex v = (Vertex) VertexList.get(i);
	//		if (v.closeTo(p, r, n)) return v;
	//	}
	//	VertexList = (ArrayList) mapGrid.get(new GridPos(gridPos.x, gridPos.y, gridPos.z + 1));
	//	if (VertexList != null) for (int i = 0, s = VertexList.size(); i < s; i++) {
	//		Vertex v = (Vertex) VertexList.get(i);
	//		if (v.closeTo(p, r, n)) return v;
	//	}
	//	VertexList = (ArrayList) mapGrid.get(new GridPos(gridPos.x - 1, gridPos.y - 1, gridPos.z - 1));
	//	if (VertexList != null) for (int i = 0, s = VertexList.size(); i < s; i++) {
	//		Vertex v = (Vertex) VertexList.get(i);
	//		if (v.closeTo(p, r, n)) return v;
	//	}
	//	VertexList = (ArrayList) mapGrid.get(new GridPos(gridPos.x - 1, gridPos.y - 1, gridPos.z + 1));
	//	if (VertexList != null) for (int i = 0, s = VertexList.size(); i < s; i++) {
	//		Vertex v = (Vertex) VertexList.get(i);
	//		if (v.closeTo(p, r, n)) return v;
	//	}
	//	VertexList = (ArrayList) mapGrid.get(new GridPos(gridPos.x - 1, gridPos.y + 1, gridPos.z - 1));
	//	if (VertexList != null) for (int i = 0, s = VertexList.size(); i < s; i++) {
	//		Vertex v = (Vertex) VertexList.get(i);
	//		if (v.closeTo(p, r, n)) return v;
	//	}
	//	VertexList = (ArrayList) mapGrid.get(new GridPos(gridPos.x - 1, gridPos.y + 1, gridPos.z + 1));
	//	if (VertexList != null) for (int i = 0, s = VertexList.size(); i < s; i++) {
	//		Vertex v = (Vertex) VertexList.get(i);
	//		if (v.closeTo(p, r, n)) return v;
	//	}
	//	VertexList = (ArrayList) mapGrid.get(new GridPos(gridPos.x + 1, gridPos.y - 1, gridPos.z - 1));
	//	if (VertexList != null) for (int i = 0, s = VertexList.size(); i < s; i++) {
	//		Vertex v = (Vertex) VertexList.get(i);
	//		if (v.closeTo(p, r, n)) return v;
	//	}
	//	VertexList = (ArrayList) mapGrid.get(new GridPos(gridPos.x + 1, gridPos.y - 1, gridPos.z + 1));
	//	if (VertexList != null) for (int i = 0, s = VertexList.size(); i < s; i++) {
	//		Vertex v = (Vertex) VertexList.get(i);
	//		if (v.closeTo(p, r, n)) return v;
	//	}
	//	VertexList = (ArrayList) mapGrid.get(new GridPos(gridPos.x + 1, gridPos.y + 1, gridPos.z - 1));
	//	if (VertexList != null) for (int i = 0, s = VertexList.size(); i < s; i++) {
	//		Vertex v = (Vertex) VertexList.get(i);
	//		if (v.closeTo(p, r, n)) return v;
	//	}
	//	VertexList = (ArrayList) mapGrid.get(new GridPos(gridPos.x + 1, gridPos.y + 1, gridPos.z + 1));
	//	if (VertexList != null) for (int i = 0, s = VertexList.size(); i < s; i++) {
	//		Vertex v = (Vertex) VertexList.get(i);
	//		if (v.closeTo(p, r, n)) return v;
	//	}
	//	
	//	Vertex v = new Vertex(p, r, n);
	//	VertexList = (ArrayList) mapGrid.get(gridPos);
	//	if (VertexList == null) {
	//		VertexList = new ArrayList();
	//		mapGrid.put(gridPos, VertexList);
	//	}
	//	VertexList.add(v);
	//	return v;
	//}

		
	//private void addTriangle(Point3f p0, Point3f p1, Point3f p2, Point3f r0, Point3f r1, Point3f r2, Vector3f n0, Vector3f n1, Vector3f n2) {
	//	int v0 = getVertexNumber(new Vertex(p0, r0, n0));
	//	int v1 = getVertexNumber(new Vertex(p1, r1, n1));
	//	int v2 = getVertexNumber(new Vertex(p2, r2, n2));
	//	v3_1.sub(p1, p0);
	//	v3_2.sub(p2, p0);
	//	Vector3f nn0 = new Vector3f();
	//	nn0.cross(v3_1, v3_2);
	//	v3_1.sub(p2, p1);
	//	v3_2.sub(p0, p1);
	//	Vector3f nn1 = new Vector3f();
	//	nn1.cross(v3_1, v3_2);
	//	v3_1.sub(p0, p2);
	//	v3_2.sub(p1, p2);
	//	Vector3f nn2 = new Vector3f();
	//	nn2.cross(v3_1, v3_2);
	//	float l0 = nn0.lengthSquared();
	//	float l1 = nn1.lengthSquared();
	//	float l2 = nn2.lengthSquared();
	//	Vector3f n;
	//	if (l0 > l1) {
	//		if (l0 > l2) n = nn0;
	//		else n = nn2;
	//	} else {
	//		if (l1 > l2) n = nn1;
	//		else n = nn2;
	//	}
	//	n.normalize();
	//	listTriangles.add(new Triangle(v0, v1, v2, n));
	//}
	//
	
	private Vector3f getCornerNormal(Point3d p0, Point3d p1, Point3d p2) {
		v3_1.set((float) p1.x - (float) p0.x, (float) p1.y - (float) p0.y, (float) p1.z - (float) p0.z);
		v3_2.set((float) p2.x - (float) p0.x, (float) p2.y - (float) p0.y, (float) p2.z - (float) p0.z);
		Vector3f n = new Vector3f();
		n.cross(v3_1, v3_2);
		n.normalize();
		return n;
	}
	
	private void addNormal(Vector3f n0, Vector3f n1) {
		n0.add(n1);
		//if (n0.dot(n1) > 0) n0.add(n1);
		//else n0.sub(n1);
	}
		
	//private Vector3f getTriangleNormal(Point3f p0, Point3f p1, Point3f p2) {
	//	v3_1.sub(p1, p0);
	//	v3_2.sub(p2, p0);
	//	Vector3f nn0 = new Vector3f();
	//	nn0.cross(v3_1, v3_2);
	//	v3_1.sub(p2, p1);
	//	v3_2.sub(p0, p1);
	//	Vector3f nn1 = new Vector3f();
	//	nn1.cross(v3_1, v3_2);
	//	v3_1.sub(p0, p2);
	//	v3_2.sub(p1, p2);
	//	Vector3f nn2 = new Vector3f();
	//	nn2.cross(v3_1, v3_2);
	//	float l0 = nn0.lengthSquared();
	//	float l1 = nn1.lengthSquared();
	//	float l2 = nn2.lengthSquared();
	//	Vector3f n;
	//	if (l0 > l1) {
	//		if (l0 > l2) n = nn0;
	//		else n = nn2;
	//	} else {
	//		if (l1 > l2) n = nn1;
	//		else n = nn2;
	//	}
	//	n.normalize();
	//	return n;
	//}
	//
	//private int getVertexNumber(Vertex v) {
	//	Integer i = (Integer) mapVertices.get(v);
	//	if (i != null) return i.intValue();
	//	mapVertices.put(v, new Integer(VertexNumber));
	//	v.setNumber(VertexNumber);
	//	VertexNumber++;
	//	return VertexNumber - 1;
	//}
	
	//private void initGrid(Model model) {
	//	//for (int z = 0; z < GRID_SIZE; z++) {
	//	//	for (int y = 0; y < GRID_SIZE; y++) {
	//	//		for (int x = 0; x < GRID_SIZE; x++) {
	//	//			Vertexgrid[x][y][z] = null;
	//	//		}
	//	//	}
	//	//}
	//	mapGrid.clear();
	//	model.getBounds(p3GridMin, p3GridMax);
	//	v3GridSize.sub(p3GridMax, p3GridMin);
	//}
	//
	//private static boolean close (Tuple3d t0, Tuple3d t1) {
	//	return (Math.abs(t0.x - t1.x) < EPSILON) && (Math.abs(t0.y - t1.y) < EPSILON) && (Math.abs(t0.z - t1.z) < EPSILON);
	//}
	//
	//private GridPos getGridPos(Point3d p) {
	//	return new GridPos(
	//		(int) ((p.x - p3GridMin.x) / v3GridSize.x * GRID_SIZE),
	//		(int) ((p.y - p3GridMin.y) / v3GridSize.y * GRID_SIZE),
	//		(int) ((p.z - p3GridMin.z) / v3GridSize.z * GRID_SIZE)
	//	);
	//	//if (g.x < 0) g.x = 0;
	//	//if (g.y < 0) g.y = 0;
	//	//if (g.z < 0) g.z = 0;
	//	//if (g.x >= GRID_SIZE) g.x = GRID_SIZE - 1;
	//	//if (g.y >= GRID_SIZE) g.y = GRID_SIZE - 1;
	//	//if (g.z >= GRID_SIZE) g.z = GRID_SIZE - 1;
	//}
	
	public static class Quad {
		public int i0, i1, i2, i3;
		public JPatchMaterial material;
		public Quad(int i0, int i1, int i2, int i3, JPatchMaterial material) {
			this.i0 = i3;
			this.i1 = i2;
			this.i2 = i1;
			this.i3 = i0;
			this.material = material;
		}
		
		public boolean isTriangle() {
			return (i0 == i1 || i1 == i2 || i2 == i3 || i3 == i0);
		}
		
		public int[] getTriangle() {
			if (i0 == i1) return new int[] { i0, i2, i3 };
			else if (i1 == i2) return new int[] { i0, i1, i3 };
			else if (i2 == i3) return new int[] { i0, i1, i2 };
			else if (i3 == i0) return new int[] { i3, i1, i2 };
			else throw new IllegalStateException();
		}
		
		public int[] getQuad() {
			return isTriangle() ? getTriangle() : new int[] { i0, i1, i2, i3 };
		}
		
		public int[] getTriangle1() {
			return new int[] { i0, i1, i2 };
		}
		
		public int[] getTriangle2() {
			return new int[] { i2, i3, i0 };
		}
	}
	
	
	//public class Triangle {
	//	public int v0, v1, v2;
	//	public JPatchMaterial mat;
	//	
	//	Triangle(int v0, int v1, int v2, Vector3f n) {
	//		this.v0 = v0;
	//		this.v1 = v1;
	//		this.v2 = v2;
	//		mat = material;
	//		Vector3f n0 = (Vector3f) mapNormals.get(new Integer(v0));
	//		Vector3f n1 = (Vector3f) mapNormals.get(new Integer(v1));
	//		Vector3f n2 = (Vector3f) mapNormals.get(new Integer(v2));
	//		if (n0 == null) mapNormals.put(new Integer(v0), new Vector3f(n));
	//		else n0.add(n);
	//		if (n1 == null) mapNormals.put(new Integer(v1), new Vector3f(n));
	//		else n1.add(n);
	//		if (n2 == null) mapNormals.put(new Integer(v2), new Vector3f(n));
	//		else n2.add(n);
	//	}
	//}
	
	
		
	/**
	* A Vertex stores position, reference-position and normal-vector of a Vertex
	**/
	public static class Vertex {
		public Point3d p;
		public Point3d r;
		public Vector3f n;
		
		Vertex(Point3d pos, Point3d referencePos, Vector3f normal) {
			p = pos;
			r = referencePos;
			n = normal;
		}
		
		//Vertex(Vertex v, int num) {
		//	p = v.p;
		//	r = v.r;
		//	n = v.n;
		//	number = num;
		//}
		//
		//void setNumber(int n) {
		//	number = n;
		//}
		
		///**
		//* Overridden to support proper operation in HashMaps
		//* This function will return identical hashCodes for all
		//* InyoVertices that are equal (as tested by the quals method)
		//**/
		//public int hashCode() {	// this does not work and must be changed - colse vertices may fall in different buckets due to rounding!!!
		//	return (int) (p.x * 23) + (int) (p.y * 12) + (int) (p.z * 11);
		//}
		//
		///**
		//* Overridden to support proper operation in HashMaps
		//* Returns true if two InyoVertices are equal, false otherwise
		//* (Throws a ClassCastException if the object passed is not an
		//* assignable to InyoVertices).
		//* Two InyoVertices are equal if their positions, reference-positions
		//* and normal-vectors are equal
		//**/
		//public boolean equals(Object o) {
		//	Vertex v = (Vertex) o;
		//	return ((Math.abs(p.x - v.p.x) < EPSILON) && (Math.abs(p.y - v.p.y) < EPSILON) && (Math.abs(p.z - v.p.z) < EPSILON) && (Math.abs(n.x - v.n.x) < EPSILON) && (Math.abs(n.y - v.n.y) < EPSILON) && (Math.abs(n.z - v.n.z) < EPSILON));
		//}
		
		public int hashCode() {
			return r.hashCode();
		}
		
		public boolean equals(Object o) {
			Vertex v = (Vertex) o;
			if (r.equals(v.r)) {
				//System.out.println("p: " + p);
				//System.out.println("n: " + n + " " + v.n);
				//if (!n.equals(v.n)) {
				//	System.out.println("p: " + p);
				//	System.out.println("n: " + n + " " + v.n);
				//}
				//System.out.println(n.equals(v.n) + "\tn: " + n + " " + v.n);
				if (vDistSq(v.n, n.x, n.y, n.z) < EPSILON) return true;
				else if (!bExportNormals && vDistSq(v.n, -n.x, -n.y, -n.z) < EPSILON) return true;
				//else System.out.println(p + " " + n + " " + v.n + " " + vDistSq(v.n, -n.x, -n.y, -n.z));
				//else System.out.println("*");
				//}
			}
			return false;
		}
		
		//public boolean closeTo(Point3d vp, Point3d vr, Vector3f vn) {
		//	return close(p, vp);
		//		//((Math.abs(p.x - vp.x) < EPSILON) && (Math.abs(p.y - vp.y) < EPSILON) && (Math.abs(p.z - vp.z) < EPSILON));// &&
		//		 //(Math.abs(r.x - vr.x) < EPSILON) && (Math.abs(r.y - vr.y) < EPSILON) && (Math.abs(r.z - vr.z) < EPSILON) &&
		//		 //(Math.abs(n.x - vn.x) < EPSILON) && (Math.abs(n.y - vn.y) < EPSILON) && (Math.abs(n.z - vn.z) < EPSILON));
		//}
		
		///**
		//* Overridden to support sorting in a collection.
		//* Compares the "number" of the vertices.
		//**/
		//public int compareTo(Object o) {
		//	Vertex v = (Vertex) o;
		//	return number - v.number;
		//}
	}
		
	private static float vDistSq(Vector3f a, float x, float y, float z) {
		float dx = a.x - x;
		float dy = a.y - y;
		float dz = a.z - z;
		return (dx*dx + dy*dy + dz*dz);
	}
}
