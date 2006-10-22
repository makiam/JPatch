package sds;

import java.io.*;
import java.util.*;

import javax.vecmath.*;

/**
 * 
 */

/**
 * @author sascha
 *
 */
public class Sds {
	private Map<EdgeKey, HalfEdge> edgeMap = new HashMap<EdgeKey, HalfEdge>();
	public List<Vertex> vertexList = new ArrayList<Vertex>();
	public List<Face> faceList = new LinkedList<Face>();
	public final Vertex[] topLevelVertices;
	
	private boolean interpolateBoundary = true;
	
	public static void main(String[] args) throws IOException {
		new Sds(new FileInputStream(args[0]));
//		new Test();
	}
	
//	public Sds() {
//		addVertex(0, 0, 0);
//		addVertex(1, 1, 1);
//		new Edge(vertexList.get(0), vertexList.get(1)).hashCode();
//		new Edge(vertexList.get(1), vertexList.get(0)).hashCode();
//		new Edge(vertexList.get(0), vertexList.get(1)).hashCode();
//		new Edge(vertexList.get(1), vertexList.get(0)).hashCode();
//	}
	
	public Sds(InputStream offInputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(offInputStream));
		String[] tokens;
		String line;
		if (!(line = reader.readLine()).equals("OFF")) {
			throw new IOException("Illegal file format");
		}
		line = reader.readLine();
//		System.out.println(line);
		tokens = line.trim().split("\\s+");
		int numVertices = Integer.parseInt(tokens[0]);
		int numFaces = Integer.parseInt(tokens[1]);
		for (int i = 0; i < numVertices; i++) {
			line = reader.readLine();
//			System.out.println(line);
			tokens = line.trim().split("\\s+");
			Vertex vertex = new Vertex(Double.parseDouble(tokens[0]) - 1, Double.parseDouble(tokens[1]) - 1, Double.parseDouble(tokens[2]) - 1);
			vertexList.add(vertex);
		}
		topLevelVertices = vertexList.toArray(new Vertex[vertexList.size()]);
		
		for (int i = 0; i < numFaces; i++) {
			line = reader.readLine();
			if (line.equals("")) {
				continue;
			}
//			System.out.println(line);
			tokens = line.trim().split("\\s+");
			int[] vertices = new int[Integer.parseInt(tokens[0])];
			for (int j = 0; j < vertices.length; j++) {
				vertices[j] = Integer.parseInt(tokens[j + 1]);
			}
			addFace(vertices);
		}
//		edgeMap = null;
		offInputStream.close();
		validateVertices();
	}
	
//	public Sds subdivide() {
//		Sds newSds = new Sds();
//		for (Face face : faceList) {
//			face.facePoint = new Vertex();
//			for (HalfEdge edge : face.getEdges()) {
//				face.facePoint.position.add(edge.vertex.position);
//				}
//			face.facePoint.position.scale(1.0 / face.sides);
//		}
//		for (Face face : faceList) {
//			for (HalfEdge edge : face.getEdges()) {
//				if (!edge.isMaster()) {
//					continue;
//				}
//				edge.edgePoint = new Vertex();
//				edge.edgePoint.position.set(edge.vertex.position);
//				edge.edgePoint.position.add(edge.pair.vertex.position);
//				edge.edgePoint.position.add(edge.face.facePoint.position);
//				if (edge.pair != null) {
//					edge.edgePoint.position.add(edge.pair.face.facePoint.position);
//					edge.edgePoint.position.scale(0.25);
//				} else {
//					edge.edgePoint.position.scale(1.0 / 3);
//				}
//			}
//		}
//		Map<Vertex, Vertex> vertexMap = new HashMap<Vertex, Vertex>();
//		Set<Vertex> vertexSet = new HashSet<Vertex>();
//		for (Vertex vertex : vertexList) {
//			Point3d F = new Point3d();
//			Point3d R = new Point3d();
//			
//			int n = 0;
//			for (HalfEdge edge : vertex.getAdjacentEdges()) {
//				F.add(edge.face.facePoint.position);
//				n++;
//			}
//			F.scale(1.0 / n);
//			int r = 0;
//			for (HalfEdge edge : vertex.getAdjacentEdges()) {
//				R.add(edge.vertex.position);
//				R.add(edge.pair.vertex.position);
//				r++;
//			}
//			R.scale(1.0 / r);
//			Vertex v = new Vertex(0, 0, 0);
//			v.position.set(vertex.position);
//			v.position.scale(n - 3);
//			v.position.add(R);
//			v.position.add(F);
//			v.position.scale(1.0 / n);
//			vertexMap.put(vertex, v);
//		}
//		Vertex[] vertices = new Vertex[4];
//		for (Face face : faceList) {
//			for (HalfEdge edge : face.getEdges()) {
//				vertices[0] = face.facePoint;
//				vertices[1] = edge.prev.isMaster() ? edge.prev.edgePoint : edge.prev.pair.edgePoint;
//				vertices[2] = vertexMap.get(edge.vertex);
//				vertices[3] = edge.isMaster() ? edge.edgePoint : edge.pair.edgePoint;
//				newSds.addFace(newSds.faceList, vertices, face.level + 1);
//				for (int i = 0; i < 4; i++) {
//					vertexSet.add(vertices[i]);
//				}
//			}
//		}
//		newSds.vertexList.addAll(vertexSet);
//		newSds.validateVertices();
//		return newSds;
//	}
	
	
	public void adaptiveSubdivide() {
		for (Face face : faceList) {
			if (face.sides != 4) {
				face.needsSubdivision = true;
			} else {
				face.needsSubdivision = face.getScreenSize() > 8;
			}
		}
		
		
	}
	
	private void subdivideFace(Face face) {
		face.facePoint = new Vertex();
		for (HalfEdge edge : face.getEdges()) {
			face.facePoint.position.add(edge.vertex.position);
		}
		face.facePoint.position.scale(1.0 / face.sides);
	}
	
	public void subdivide() {
		edgeMap = new HashMap<EdgeKey, HalfEdge>(edgeMap.size() * 4);
		for (Face face : faceList) {
			face.computeFacePoint();
		}
		for (Face face : faceList) {
			for (HalfEdge edge : face.getEdges()) {
				if (edge.isMaster()) {
					edge.computeEdgePoint();
				}
			}
		}
		for (Vertex vertex : vertexList) {
			vertex.moveVertex();
		}
		Vertex[] vertices = new Vertex[4];
		List<Face> newFaces = new LinkedList<Face>();
		for (Iterator<Face> it = faceList.iterator(); it.hasNext(); ) {
			Face face = it.next();
			for (HalfEdge edge : face.getEdges()) {
				vertices[0] = face.facePoint;
				vertices[1] = edge.prev.isMaster() ? edge.prev.edgePoint : edge.prev.pair.edgePoint;
				vertices[2] = edge.vertex;
				if (edge.isMaster()) {
					vertices[3] = edge.edgePoint;
					vertexList.add(edge.edgePoint);
				} else {
					vertices[3] = edge.pair.edgePoint;
				}
				newFaces.add(createFace(vertices, face.level + 1));
				
			}
			it.remove();
			vertexList.add(face.facePoint);
		}
		faceList.addAll(newFaces);
		validateVertices();
	}
	
	private void validateVertices() {
		for (Vertex vertex : vertexList) {
			vertex.validate();
		}
	}
	
	public void smooth() {
		for (Vertex vertex : vertexList) {
			vertex.smooth();
		}
	}
	
	public void dump() {
		System.out.println("Faces:");
		for (Face face : faceList) {
			System.out.print(face + ": ");
			for (HalfEdge edge : face.edgeIterable) {
				System.out.print(edge + " ");
			}
			System.out.println();
		}
		System.out.println("Vertices:");
		for (Vertex vertex : vertexList) {
			System.out.println(vertex + "  valence" + vertex.valence());
		}
	}
	
	public void verify() {
		Set<Vertex> vertexSet = new HashSet<Vertex>(vertexList);
		System.out.println(vertexList.size() + " " + vertexSet.size());
		for (Face face : faceList) {
			for (HalfEdge edge : face.getEdges()) {
				if (!vertexSet.contains(edge.vertex)) {
					System.out.println(edge.vertex);
				}
				if (!vertexSet.contains(edge.pair.vertex)) {
					System.out.println(edge.pair.vertex);
				}
			}
		}		
	}
	
	
	
	
	public String toString() {
		return faceList.size() + " faces, " + vertexList.size() + " vertices";
	}
	
	private void addFace(int[] vertices) {
		Face face = new Face(vertices.length, 0);
		HalfEdge start = createEdge(face, vertexList.get(vertices[0]), vertexList.get(vertices[1]), 0);
		HalfEdge prev = start;
		HalfEdge edge = null;
		for (int i = 1; i < vertices.length; i++) {
			edge = createEdge(face, vertexList.get(vertices[i]), vertexList.get(vertices[(i + 1) % vertices.length]), 0);
			prev.next = edge;
			edge.prev = prev;
			prev = edge;
		}
		edge.next = start;
		start.prev = edge;
		face.edge = start;
		faceList.add(face);
	}
	
	private Face createFace(Vertex[] vertices, int level) {
		Face face = new Face(vertices.length, level);
		HalfEdge start = createEdge(face, vertices[0], vertices[1], level);
		HalfEdge prev = start;
		HalfEdge edge = null;
		for (int i = 1; i < vertices.length; i++) {
			edge = createEdge(face, vertices[i], vertices[(i + 1) % vertices.length], level);
			prev.next = edge;
			edge.prev = prev;
			prev = edge;
		}
		edge.next = start;
		start.prev = edge;
		face.edge = start;
		return face;
	}
	
	private HalfEdge createEdge(Face face, Vertex vertex0, Vertex vertex1, int level) {
		EdgeKey key = new EdgeKey(vertex1, vertex0);
		HalfEdge neighbor = edgeMap.get(key);
		HalfEdge edge;
		if (neighbor == null) {
			edge = new HalfEdge(vertex0, vertex1, level);
			edgeMap.put(new EdgeKey(vertex0, vertex1), edge);
		} else {
			edge = neighbor.pair;
			if (edge.face != null) {
				throw new IllegalArgumentException("Surface is non-manifold.");
			}
		}
		edge.face = face;
		vertex0.edge = edge;
		return edge;
	}
	
	private static final class EdgeKey {
		private final Vertex firstVertex;
		private final Vertex secondVertex;
		private final int hashCode;
		
		private EdgeKey(Vertex firstVertex, Vertex secondVertex) {
			this.firstVertex = firstVertex;
			this.secondVertex = secondVertex;
			hashCode = (System.identityHashCode(firstVertex) << 1) ^ System.identityHashCode(secondVertex);
		}
		
		@Override
		public int hashCode() {
			return hashCode;
		}
		
		@Override
		public boolean equals(Object o) {
			EdgeKey ek = (EdgeKey) o;
			return firstVertex == ek.firstVertex && secondVertex == ek.secondVertex;
		}
	}
}
