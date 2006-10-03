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
	private Map<Edge, Edge> edgeMap = new HashMap<Edge, Edge>();
	public List<Vertex> vertexList = new ArrayList<Vertex>();
	public List<Face> faceList = new ArrayList<Face>();
	
	public static void main(String[] args) throws IOException {
		new Sds(new FileInputStream(args[0]));
//		new Test();
	}
	
	public Sds() {
//		addVertex(0, 0, 0);
//		addVertex(1, 1, 1);
//		new Edge(vertexList.get(0), vertexList.get(1)).hashCode();
//		new Edge(vertexList.get(1), vertexList.get(0)).hashCode();
//		new Edge(vertexList.get(0), vertexList.get(1)).hashCode();
//		new Edge(vertexList.get(1), vertexList.get(0)).hashCode();
	}
	
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
			addVertex(Double.parseDouble(tokens[0]), Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]));
		}
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
		edgeMap = null;
		offInputStream.close();
		validateVertices();
	}
	
	public Sds subdivide() {
		Sds newSds = new Sds();
		for (Face face : faceList) {
			face.facePoint.position.set(0, 0, 0);
			for (Edge edge : face.getEdges()) {
				face.facePoint.position.add(edge.vertex0.position);
				}
			face.facePoint.position.scale(1.0 / face.sides);
		}
		for (Face face : faceList) {
			for (Edge edge : face.getEdges()) {
				if (!edge.isMaster()) {
					continue;
				}
				edge.edgePoint.position.set(edge.vertex0.position);
				edge.edgePoint.position.add(edge.vertex1.position);
				edge.edgePoint.position.add(edge.face.facePoint.position);
				if (edge.neighbor != null) {
					edge.edgePoint.position.add(edge.neighbor.face.facePoint.position);
					edge.edgePoint.position.scale(0.25);
				} else {
					edge.edgePoint.position.scale(1.0 / 3);
				}
			}
		}
		Map<Vertex, Vertex> vertexMap = new HashMap<Vertex, Vertex>();
		Set<Vertex> vertexSet = new HashSet<Vertex>();
		for (Vertex vertex : vertexList) {
			Point3d F = new Point3d();
			Point3d R = new Point3d();
			
			int n = 0;
			for (Edge edge : vertex.getAdjacentEdges()) {
				F.add(edge.face.facePoint.position);
				n++;
			}
			F.scale(1.0 / n);
			int r = 0;
			for (Edge edge : vertex.getAdjacentEdges()) {
				R.add(edge.vertex0.position);
				R.add(edge.vertex1.position);
				r++;
			}
			R.scale(1.0 / r);
			Vertex v = new Vertex(0, 0, 0);
			v.position.set(vertex.position);
			v.position.scale(n - 3);
			v.position.add(R);
			v.position.add(F);
			v.position.scale(1.0 / n);
			vertexMap.put(vertex, v);
		}
		Vertex[] vertices = new Vertex[4];
		for (Face face : faceList) {
			for (Edge edge : face.getEdges()) {
				vertices[0] = face.facePoint;
				vertices[1] = edge.prev.isMaster() ? edge.prev.edgePoint : edge.prev.neighbor.edgePoint;
				vertices[2] = vertexMap.get(edge.vertex0);
				vertices[3] = edge.isMaster() ? edge.edgePoint : edge.neighbor.edgePoint;
				newSds.addFace(vertices);
				for (int i = 0; i < 4; i++) {
					vertexSet.add(vertices[i]);
				}
			}
		}
		newSds.vertexList.addAll(vertexSet);
		newSds.validateVertices();
		return newSds;
	}
	
	private void validateVertices() {
		for (Vertex vertex : vertexList) {
			vertex.validate();
			vertex.smooth();
		}
	}
	
	private void addVertex(double x, double y, double z) {
		vertexList.add(new Vertex(x, y, z));
	}
	
	private void addFace(int[] vertices) {
		Face face = new Face();
		face.sides = vertices.length;
		Edge start = createEdge(face, vertexList.get(vertices[0]), vertexList.get(vertices[1]));
		Edge prev = start;
		Edge edge = null;
		for (int i = 1; i < vertices.length; i++) {
			edge = createEdge(face, vertexList.get(vertices[i]), vertexList.get(vertices[(i + 1) % vertices.length]));
			prev.next = edge;
			edge.prev = prev;
			prev = edge;
		}
		edge.next = start;
		start.prev = edge;
		face.edge = start;
		faceList.add(face);
	}
	
	private void addFace(Vertex[] vertices) {
		Face face = new Face();
		face.sides = vertices.length;
		Edge start = createEdge(face, vertices[0], vertices[1]);
		Edge prev = start;
		Edge edge = null;
		for (int i = 1; i < vertices.length; i++) {
			edge = createEdge(face, vertices[i], vertices[(i + 1) % vertices.length]);
			prev.next = edge;
			edge.prev = prev;
			prev = edge;
		}
		edge.next = start;
		start.prev = edge;
		face.edge = start;
		faceList.add(face);
	}
	
	private Edge createEdge(Face face, Vertex vertex0, Vertex vertex1) {
		Edge edge = new Edge(vertex0, vertex1);
		if (edgeMap.containsKey(edge)) {
			throw new IllegalArgumentException("Surface is non-manifold");
		}
		edge.face = face;
		Edge neighbor = edgeMap.get(new Edge(vertex1, vertex0));
		if (neighbor != null) {
			edge.neighbor = neighbor;
			neighbor.neighbor = edge;
		}
		edgeMap.put(edge, edge);
		vertex0.edge = edge;
		return edge;
	}
}
