package com.jpatch.entity.sds2;

import com.jpatch.entity.*;

import java.io.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.vecmath.*;

public class JptLoader {
	private Map<Integer, Cp> cpList = new HashMap<Integer, Cp>();
	private Map<Integer, Cp> childHookIndex = new HashMap<Integer, Cp>();
	private int cpIndex;
	private Sds sds;
	private List<Material> materials = new ArrayList<Material>();
	private Map<String, Integer> materialNameMap = new HashMap<String, Integer>();
	
	public ContentHandler handler = new DefaultHandler() {
		StringBuilder chars;
		Cp startCp;
		Cp prevCp;
		boolean loop;
		boolean patch;
		boolean material;
		int materialIndex = 0;
		int cpIndex = 0;
		Color3f color = new Color3f(1.0f, 1.0f, 1.0f);
	
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (chars != null) {
				chars.append(ch, start, length);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (localName.equals("curve")) {
				if (loop) {
					startCp.prev = prevCp;
					prevCp.next = startCp;
				}
			}
			if (patch) {
				if (localName.equals("patch")) {
					patch = false;
				} else if (localName.equals("points")) {
					String[] cpIndexes = chars.toString().split("[,\\ ]");
					List<BaseVertex> vertexList = new ArrayList<BaseVertex>();
					for (int i = 0; i < cpIndexes.length; i += 2) {
						Cp cp = cpList.get(Integer.parseInt(cpIndexes[i]));
						Cp cpNext = cpList.get(Integer.parseInt(cpIndexes[i + 1]));
						Cp childHook0 = childHookIndex.get(cp.index);
						Cp childHook1 = childHookIndex.get(cpNext.index);
						vertexList.add(cp.getVertex());
						if (childHook0 != null) {
							Cp end = null;
							for (end = childHook0.next; end.next != null; end = end.next);
							if (end.getHead() == cpNext.getHead()) {
								for (Cp hook = childHook0.next; hook.next != null; hook = hook.next) {
									vertexList.add(hook.getVertex());
								}
							}
						}
						if (childHook1 != null) {
							Cp end = null;
							for (end = childHook1.next; end.next != null; end = end.next);
							if (end.getHead() == cp.getHead()) {
								for (Cp hook = end.prev; hook.prev != null; hook = hook.prev) {
									vertexList.add(hook.getVertex());
								}
							}
						}
					}
//					System.out.println(vertexList);
					Collections.reverse(vertexList);
					BaseVertex[] vertices = vertexList.toArray(new BaseVertex[vertexList.size()]);
					Face face = sds.addFace(0, vertices);
//					if (face != null) {
						face.setMaterial(materials.get(materialIndex));
						System.out.println(materials.get(materialIndex));
//					}
//					sds.addCandidateFace(vertexList.toArray(new TopLevelVertex[vertexList.size()]));
					chars = null;
				}
			}
			if (material) {
				if (localName.equals("name")) {
					String name = chars.toString();
					chars = null;
					if (name.equals("Default Material")) {
						materialIndex = 0;
					} else {
						materialIndex = materials.size();
					}
					materialNameMap.put(name, materialIndex);
				}
				if (localName.equals("material")) {
					if (materialIndex < materials.size()) {
						materials.remove(materialIndex);
						materials.add(materialIndex, new BasicMaterial(color));
					} else {
						materials.add(new BasicMaterial(color));
					}
					material = false;
				}
			}
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
//			System.out.print("<" + localName + " ");
//			for (int i = 0; i < attributes.getLength(); i++) {
//				System.out.print(attributes.getLocalName(i) + "=\"" + attributes.getValue(i) + "\" ");
//			}
//			System.out.println(">");
			if (localName.equals("curve")) {
				prevCp = null;
				String closed = attributes.getValue("closed");
				loop = (closed != null && closed.equals("true"));
			} else if (localName.equals("cp")) {
				String attach = attributes.getValue("attach");
				String hook = attributes.getValue("hook");
				String hookPos = attributes.getValue("hookpos");
				String id = attributes.getValue("id");
				Cp cp;
				if (attach != null) {
					cp = new Cp(Integer.parseInt(attach));
				} else if (hook != null) {
					int parentHookIndex = Integer.parseInt(hook);
					cp = new Cp(parentHookIndex);
					if (prevCp == null) {
						childHookIndex.put(parentHookIndex, cp);
					}
				} else if (hookPos != null) {
					cp = new Cp(Double.parseDouble(hookPos));
				} else {
					cp = new Cp(
							Double.parseDouble(attributes.getValue("x")),
							Double.parseDouble(attributes.getValue("y")),
							-Double.parseDouble(attributes.getValue("z"))
					);
				}
				int cpId = cpIndex++;
				if (id != null) {
					cpId = Integer.parseInt(id);
				}
				cpList.put(cpId, cp);
				if (prevCp != null) {
					prevCp.next = cp;
					cp.prev = prevCp;
				} else {
					startCp = cp;
				}
				prevCp = cp;
			} else if (localName.equals("points") && patch == true) {
				chars = new StringBuilder();
			} else if (localName.equals("patch")) {
				patch = true;
				try {
					materialIndex = Integer.parseInt(attributes.getValue("material"));
				} catch (NumberFormatException e) {
					materialIndex = materialNameMap.get(attributes.getValue("material"));
				}
			} else if (localName.equals("material")) {
				material = true;
			} else if (material) {
				if (localName.equals("name")) {
					chars = new StringBuilder();
				}
				if (localName.equals("color")) {
					color.set(
							Float.parseFloat(attributes.getValue("r")),
							Float.parseFloat(attributes.getValue("g")),
							Float.parseFloat(attributes.getValue("b"))
					);
				}
			}
		}
		
	};
	
	public JptLoader() {
		materials.add(new BasicMaterial(new Color3f(1.0f, 1.0f, 1.0f)));
	}
	
	public Sds importModel(InputStream inputStream) throws IOException {
		XMLReader xmlReader;
		try {
			xmlReader = XMLReaderFactory.createXMLReader();
			xmlReader.setContentHandler(handler);
			sds = new Sds();
			cpList.clear();
			childHookIndex.clear();
			cpIndex = 0;
			xmlReader.parse(new InputSource(inputStream));
			inputStream.close();
			
			for (Integer cpId : cpList.keySet()) {
				cpList.get(cpId).computeG1Tangents();
			}
			
			Point3d p0 = new Point3d();
			Point3d p1 = new Point3d();
			Point3d p2 = new Point3d();
			for (Integer cpId : cpList.keySet()) {
				Cp cp = cpList.get(cpId);
				if (cp.hookPos != -1) {
					Cp start, end;
					for (start = cp; start.prev != null; start = start.prev);
					for (end = cp; end.next != null; end = end.next);
					start.getVertex().getPosition(p0);
					Point3d outTangent = cpList.get(start.attach).outTangent;
					Point3d inTangent = cpList.get(end.attach).inTangent;
					end.getVertex().getPosition(p1);
					evaluateBezier(p0, outTangent, inTangent, p1, cp.hookPos, p2);
					cp.getVertex().setPosition(p2);
//					start.getVertex().getPosition(p0);
//					end.getVertex().getPosition(p1);
					p2.interpolate(p0, p1, cp.hookPos);
					cp.vertex.setPosition(p2);
//					cp.vertex.pos.interpolate(start.getVertex().pos, end.getVertex().pos, cp.hookPos);
				}
			}
//			sds.addCandidateFaces();
//			sds.replaceFaces();
			sds.sortFaces();
//			sds.validateVertices();
//			Map<Point3d, Vector3d> compensationMap = new HashMap<Point3d, Vector3d>();
//			for (TopLevelVertex vertex : sds.vertexList) {
//				Vector3d v = new Vector3d();
//				for (HalfEdge edge : vertex.edges) {
//					v.add(edge.getSecondVertex().pos);
//				}
//				v.scale(-1.0 / vertex.edges.length);
//				v.add(vertex.pos);
//				v.scale(0.75);
//				compensationMap.put(vertex.pos, v);
//			}
//			for (Point3d p : compensationMap.keySet()) {
//				p.add(compensationMap.get(p));
//			}
//			sds.makeSlates();
//			sds.rethinkSlates();
			
//			System.out.println(sds.vertexList.size() + " top level, " + sds.level2Vertices.length + " level 2 vertices");
			
			return sds;
			
			
			
		} catch (SAXException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static final void evaluateBezier(Point3d p0, Point3d p1, Point3d p2, Point3d p3, double t, Point3d result) {
		double t1 = 1 - t;
		double t1_2 = t1 * t1;
		double t_2 = t * t;
		double B0 = t1_2 * t1;
		double B1 = 3 * t * t1_2;
		double B2 = 3 * t_2 * t1;
		double B3 = t_2 * t;
		result.set(
			B0 * p0.x + B1 * p1.x + B2 * p2.x + B3 * p3.x,
			B0 * p0.y + B1 * p1.y + B2 * p2.y + B3 * p3.y,
			B0 * p0.z + B1 * p1.z + B2 * p2.z + B3 * p3.z
		);
	 }
	
	private class Cp {
		final int index = cpIndex++, attach;
		final BaseVertex vertex;
		final double hookPos;
		final Point3d inTangent = new Point3d();
		final Point3d outTangent = new Point3d();
		Cp prev, next;
		
		Cp(double x, double y, double z) {
			vertex = new BaseVertex(x, y, z);
			attach = -1;
			hookPos = -1;
//			sds.vertexList.add(vertex);
		}
		
		Cp(int attach) {
			this.attach = attach;
			vertex = null;
			hookPos = -1;
		}
		
		Cp(double hookPos) {
			vertex = new BaseVertex();
			this.hookPos = hookPos;
			attach = -1;
//			sds.vertexList.add(vertex);
		}
		
		Cp getHead() {
			return (attach == -1) ? this : cpList.get(attach).getHead();
		}
		
		BaseVertex getVertex() {
			return getHead().vertex;
		}
		
		/**
		 * Compute G1 tangents
		 */
		private void computeG1Tangents() {
			double magnitude = 1;
			double s;
			Vector3d v3 = new Vector3d();
			if (prev != null && next != null) {
				Point3d A = new Point3d();
				Point3d B = new Point3d();
				Point3d C = new Point3d();
				prev.getVertex().getPosition(A);
				next.getVertex().getPosition(B);
				getVertex().getPosition(C);
				
				double ca = C.distance(A);
				double cb = C.distance(B);
				
				Point3d AC = new Point3d(C);
				AC.sub(A);
				s = (ca == 0) ? 0 : cb / ca / 3.0;
				AC.scaleAdd(s, C);
				
				Point3d BC = new Point3d(C);
				BC.sub(B);
				s = (cb == 0) ? 0 : ca / cb / 3.0;
				BC.scaleAdd(s, C);
				
				double a = Math.sqrt(ca);
				double b = Math.sqrt(cb);
				double t = (a != 0) ? a / (a + b) : 0;
				double t1 = 1 - t;
				double b0 = t1 * t1;
				double b1 = 2 * t1 * t;
				double b2 = t * t;
				
				double x = A.x * b0 + BC.x * b1 + AC.x * b2;
				double y = A.y * b0 + BC.y * b1 + AC.y * b2;
				double z = A.z * b0 + BC.z * b1 + AC.z * b2;
				
				v3.set(x,y,z);
				v3.sub(C);
				v3.scale(magnitude);
				inTangent.add(C,v3);
				
				x = BC.x * b0 + AC.x * b1 + B.x * b2; 
				y = BC.y * b0 + AC.y * b1 + B.y * b2; 
				z = BC.z * b0 + AC.z * b1 + B.z * b2; 
				
				v3.set(x,y,z);
				v3.sub(C);
				v3.scale(magnitude);
				outTangent.add(C,v3);
				
			} else if (prev == null) {
				if (next != null && next.next != null) {
					Point3d A = new Point3d();
					Point3d B = new Point3d();
					Point3d C = new Point3d();
					getVertex().getPosition(A);
					next.next.getVertex().getPosition(B);
					next.getVertex().getPosition(C);
					
					double ca = C.distance(A);
					double cb = C.distance(B);
					
					Point3d AC = new Point3d(C);
					AC.sub(A);
					s = (ca == 0) ? 0 : cb / ca / 3.0;
					AC.scaleAdd(s, C);
					
					Point3d BC = new Point3d(C);
					BC.sub(B);
					s = (cb == 0) ? 0 : ca / cb / 3.0;
					BC.scaleAdd(s, C);
					
					double a = Math.sqrt(ca);
					double b = Math.sqrt(cb);
					double t = a / (a + b);
					outTangent.interpolate(A,BC,t * magnitude);
				} else {
					Point3d A = new Point3d();
					getVertex().getPosition(A);
					Point3d B = new Point3d();
					next.getVertex().getPosition(B);
					outTangent.interpolate(A, B, magnitude / 3.0);
				}
			} else {	//cpNext == null
				if (prev != null && prev.prev != null) {
					Point3d A = new Point3d();
					Point3d B = new Point3d();
					Point3d C = new Point3d();
					getVertex().getPosition(A);
					prev.prev.getVertex().getPosition(B);
					prev.getVertex().getPosition(C);
					
					double ca = C.distance(A);
					double cb = C.distance(B);
					
					Point3d AC = new Point3d(C);
					AC.sub(A);
					s = (ca == 0) ? 0 : cb / ca / 3.0;
					AC.scaleAdd(s, C);
					
					Point3d BC = new Point3d(C);
					BC.sub(B);
					s = (cb == 0) ? 0 : ca / cb / 3.0;
					BC.scaleAdd(s, C);
					
					double a = Math.sqrt(ca);
					double b = Math.sqrt(cb);
					double t = a / (a + b);
					inTangent.interpolate(A, BC, t * magnitude);
				} else {
					Point3d A = new Point3d();
					Point3d B = new Point3d();
					getVertex().getPosition(A);
					prev.getVertex().getPosition(B);
					inTangent.interpolate(A, B, magnitude / 3.0);
				}
			}		
		}
	}
}
