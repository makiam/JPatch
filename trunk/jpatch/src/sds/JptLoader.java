package sds;

import java.io.*;
import java.util.*;

import no.uib.cipr.matrix.*;
import no.uib.cipr.matrix.sparse.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.vecmath.*;

public class JptLoader {
	private List<Cp> cpList = new ArrayList<Cp>();
	private Map<Integer, Cp> childHookIndex = new HashMap<Integer, Cp>();
	private int cpIndex;
	private Sds sds;
	
	public ContentHandler handler = new DefaultHandler() {
		StringBuilder chars;
		Cp startCp;
		Cp prevCp;
		boolean loop;
		boolean patch;
		
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
					List<TopLevelVertex> vertexList = new ArrayList<TopLevelVertex>();
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
					System.out.println(vertexList);
					sds.addFace(vertexList.toArray(new TopLevelVertex[vertexList.size()]));
					chars = null;
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
							Double.parseDouble(attributes.getValue("z"))
					);
				}
				cpList.add(cp);
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
			}
		}
		
	};
	
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
			
			for (Cp cp : cpList) {
				cp.computeG1Tangents();
			}
			for (Cp cp : cpList) {
				if (cp.hookPos != -1) {
					Cp start, end;
					for (start = cp; start.prev != null; start = start.prev);
					for (end = cp; end.next != null; end = end.next);
					Point3d outTangent = cpList.get(start.attach).outTangent;
					Point3d inTangent = cpList.get(end.attach).inTangent;
					evaluateBezier(start.getVertex().pos, outTangent, inTangent, end.getVertex().pos, cp.hookPos, cp.getVertex().pos);
//					cp.vertex.pos.interpolate(start.getVertex().pos, end.getVertex().pos, cp.hookPos);
				}
			}
			
			sds.validateVertices();
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
			sds.makeSlates();
			sds.rethinkSlates();
			Map<TopLevelVertex, Integer> vertexIndexMap = new HashMap<TopLevelVertex, Integer>();
			for (int i = 0; i < sds.vertexList.size(); i++) {
				vertexIndexMap.put(sds.vertexList.get(i), i);
			}
			int size = sds.vertexList.size();
			FlexCompRowMatrix matrix = new FlexCompRowMatrix(size, size);
			DenseVector xLimit = new DenseVector(size);
			DenseVector yLimit = new DenseVector(size);
			DenseVector zLimit = new DenseVector(size);
			DenseVector xControl = new DenseVector(size);
			DenseVector yControl = new DenseVector(size);
			DenseVector zControl = new DenseVector(size);
			for (int i = 0; i < sds.vertexList.size(); i++) {
				TopLevelVertex v = sds.vertexList.get(i);
				xLimit.set(i, v.pos.x);
				yLimit.set(i, v.pos.y);
				zLimit.set(i, v.pos.z);
				xControl.set(i, v.pos.x);
				yControl.set(i, v.pos.y);
				zControl.set(i, v.pos.z);
				LinearCombination<TopLevelVertex> lc = v.getLimitLc();
				for (int j = 0; j < lc.size(); j++) {
					int index = vertexIndexMap.get(lc.getEntities().get(j));
					matrix.add(i, index, lc.getWeights()[j]);
				}
			}
		
			CompRowMatrix m = new CompRowMatrix(matrix);
			
			IterativeSolver solver = new IR(xControl);
			Preconditioner preconditioner = new ICC(m.copy());
			preconditioner.setMatrix(m);
			solver.setPreconditioner(preconditioner);
			solver.getIterationMonitor().setIterationReporter(new OutputIterationReporter());
			try {
				solver.solve(m, xLimit, xControl);
				solver.solve(m, yLimit, yControl);
				solver.solve(m, zLimit, zControl);
				for (int i = 0; i < size; i++) {
					
//					sds.vertexList.get(i).pos.set(
//							xControl.get(i),
//							yControl.get(i),
//							zControl.get(i)
//					);
					
					
				}
				sds.rethinkSlates();
				for (int i = 0; i < size; i++) {
//					System.out.print(sds.vertexList.get(i).pos);
//					System.out.print(" => ");
					Level2Vertex l2v = sds.vertexList.get(i).vertexPoint;
					LinearCombination<TopLevelVertex> lc = sds.vertexList.get(i).getLimitLc();
					double x = 0, y = 0, z = 0;
					for (int j = 0; j < lc.size(); j++) {
						x += lc.getEntities().get(j).pos.x * lc.getWeights()[j];
						y += lc.getEntities().get(j).pos.y * lc.getWeights()[j];
						z += lc.getEntities().get(j).pos.z * lc.getWeights()[j];
					}
					
					System.out.println("(" + x + ", " + y + ", " + z + ")");
					System.out.println(l2v.limit);
				}
//				for (Face face : sds.faceList) {
//					for (HalfEdge edge : face.edgeIterable) {
//						LinearCombination<TopLevelVertex> lc = edge.getEdgePointLc();
//						double x = 0, y = 0, z = 0;
//						for (int j = 0; j < lc.size(); j++) {
//							x += lc.getEntities().get(j).pos.x * lc.getWeights()[j];
//							y += lc.getEntities().get(j).pos.y * lc.getWeights()[j];
//							z += lc.getEntities().get(j).pos.z * lc.getWeights()[j];
//						}
//						
//						System.out.println("(" + x + ", " + y + ", " + z + ")");
//						System.out.println(edge.edgePoint.pos);
//					}
//				}
			} catch (IterativeSolverNotConvergedException e) {
				  System.err.println("Iterative solver failed to converge");
			}
//			System.exit(0);
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
		final TopLevelVertex vertex;
		final double hookPos;
		final Point3d inTangent = new Point3d();
		final Point3d outTangent = new Point3d();
		Cp prev, next;
		
		Cp(double x, double y, double z) {
			vertex = new TopLevelVertex(x, y, z);
			attach = -1;
			hookPos = -1;
			sds.vertexList.add(vertex);
		}
		
		Cp(int attach) {
			this.attach = attach;
			vertex = null;
			hookPos = -1;
		}
		
		Cp(double hookPos) {
			vertex = new TopLevelVertex();
			this.hookPos = hookPos;
			attach = -1;
			sds.vertexList.add(vertex);
		}
		
		Cp getHead() {
			return (attach == -1) ? this : cpList.get(attach).getHead();
		}
		
		TopLevelVertex getVertex() {
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
				Point3d A = new Point3d(prev.getVertex().pos);
				Point3d B = new Point3d(next.getVertex().pos);
				Point3d C = new Point3d(getVertex().pos);
				
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
					Point3d A = new Point3d(getVertex().pos);
					Point3d B = new Point3d(next.next.getVertex().pos);
					Point3d C = new Point3d(next.getVertex().pos);
					
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
					outTangent.interpolate(getVertex().pos, next.getVertex().pos, magnitude / 3.0);
				}
			} else {	//cpNext == null
				if (prev != null && prev.prev != null) {
					Point3d A = new Point3d(getVertex().pos);
					Point3d B = new Point3d(prev.prev.getVertex().pos);
					Point3d C = new Point3d(prev.getVertex().pos);
					
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
					inTangent.interpolate(getVertex().pos, prev.getVertex().pos, magnitude / 3.0);
				}
			}		
		}
	}
}
