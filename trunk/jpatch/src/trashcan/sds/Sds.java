package trashcan.sds;

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
	private final static Comparator<Face> faceMaterialComparator = new Comparator<Face>() {

		public int compare(Face f1, Face f2) {
			int m1 = System.identityHashCode(f1.getMaterial());
			int m2 = System.identityHashCode(f2.getMaterial());
			return (m1 < m2) ? -1 : (m1 > m2) ? 1 : 0;
		}
		
	};
	private Map<EdgeKey, HalfEdge> edgeMap = new HashMap<EdgeKey, HalfEdge>();
	private Set<HalfEdge> poisonedEdges = new HashSet<HalfEdge>();
	
	public List<TopLevelVertex> vertexList = new ArrayList<TopLevelVertex>();
	public List<Face> faceList = new ArrayList<Face>();
	public List<TopLevelVertex[]> candidateFaceList = new ArrayList<TopLevelVertex[]>();
	public Map<CandidateEdge, Integer> candidateEdgeMap = new HashMap<CandidateEdge, Integer>();
	
	public Level2Vertex[] level2Vertices = new Level2Vertex[0];
	
//	public final Vertex[] topLevelVertices;
	
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
	
	public Sds() { }
	
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
			TopLevelVertex vertex = new TopLevelVertex(Double.parseDouble(tokens[0]), Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]));
			vertexList.add(vertex);
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
//		edgeMap = null;
		offInputStream.close();
		validateVertices();
//		dump();
		makeSlates();
		rethinkSlates();
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
	
	
//	public void adaptiveSubdivide() {
//		for (Face face : faceList) {
//			if (face.sides != 4) {
//				face.needsSubdivision = true;
//			} else {
//				face.needsSubdivision = face.getScreenSize() > 8;
//			}
//		}
//		
//		
//	}
//	
//	private void subdivideFace(Face face) {
//		face.facePoint = new Vertex();
//		for (HalfEdge edge : face.getEdges()) {
//			face.facePoint.position.add(edge.vertex.position);
//		}
//		face.facePoint.position.scale(1.0 / face.sides);
//	}
	
	public void sortFaces() {
		Collections.sort(faceList, faceMaterialComparator);
	}
	
	public void makeSlates() {
//		for (Face face : faceList) {
//			face.bindFacePoint();
//		}
//		for (Face face : faceList) {
//			for (HalfEdge edge : face.getEdges()) {
//				if (edge.isPrimary()) {
//					edge.bindEdgePoint();
//				}
//			}
//		}
//		for (TopLevelVertex vertex : vertexList) {
//			vertex.bindVertexPoint();
//		}
//		for (Vertex vertex : vertexList) {
//			vertex.bindLimitPoint();
//		}
		for (Face face : faceList) {
			face.prepareSlates();
		}
		
//		for (Face face : faceList) {
//			for (HalfEdge edge : face.getEdges()) {
//				System.out.println(edge.slateEdge0);
//				System.out.println(edge.slateEdge1);
//				System.out.println(edge.pair.slateEdge0);
//				System.out.println(edge.pair.slateEdge1);
//			}
//		}
		
		List<Level2Vertex> list = new ArrayList<Level2Vertex>();
		for (Face face : faceList) {
			face.setupSlates();
		}
		for (Face face : faceList) {
			face.initFans();
			list.add(face.getFacePoint());
		}
		for (Face face : faceList) {
			for (HalfEdge edge : face.getEdges()) {
				if (edge.isPrimary()) {
					list.add(edge.getEdgePoint());
				}
			}
		}
		for (TopLevelVertex vertex : vertexList) {
			list.add(vertex.getVertexPoint());
//			System.out.println(list.size());
		}
		level2Vertices = list.toArray(new Level2Vertex[list.size()]);
		
//		for (Face face : faceList) {
//			face.setupSlateNeighbors();
//		}
//		System.exit(0);
//		rethinkSlates();
		
//		// test slate neighbors
//		for (Face face: faceList) {
//			for (Slate slate : face.getSlates()) {
//				for (int i = 0; i < 4; i++) {
//					if (slate.adjacentSlates[i].adjacentSlates[3 - i] != slate) {
//						System.err.println("error");
//					}
//				}
//			}
//		}
	}
	
	public void rethinkSlates() {
		for (TopLevelVertex vertex : vertexList) {
			vertex.analyzeEdges();
		}
		computeLevel2Vertices();
		for (Face face : faceList) {
			for (Slate2 slate : face.getSlates()) {
				slate.initCreases();
			}
		}
//		for (Vertex vertex : vertexList) {
//			vertex.limitPoint.computeDerivedPosition();
//		}
	}
	
	public void computeLevel2Vertices() {
//		System.out.println("cumputeLevel2Vertices...");
//		for (Level2Vertex v : level2Vertices) {
//			v.computeDerivedPosition();
//		}
		for (Level2Vertex v : level2Vertices) {
			v.validateLimit();
		}
		for (Face face : faceList) {
			for (Slate2 slate : face.getSlates()) {
				slate.computeNormalCone();
			}
		}
//		System.out.println("cumputeLevel2Vertices done.");
	}
	
	public void project(Matrix4f matrix) {
		for (AbstractVertex v : vertexList) {
			v.project(matrix);
		}
		for (AbstractVertex v : level2Vertices) {
			v.project(matrix);
		}
	}
	
//	public void subdivide() {
//		edgeMap = new HashMap<EdgeKey, HalfEdge>(edgeMap.size() * 4);
//		for (Face face : faceList) {
//			face.bindFacePoint();
//		}
//		for (Face face : faceList) {
//			for (HalfEdge edge : face.getEdges()) {
//				if (edge.isMaster()) {
//					edge.bindEdgePoint();
//				}
//			}
//		}
//		for (Vertex vertex : vertexList) {
//			vertex.bindVertexPoint();
//		}
//		
//		Vertex[] vertices = new Vertex[4];
//		List<Face> newFaces = new LinkedList<Face>();
//		for (Iterator<Face> it = faceList.iterator(); it.hasNext(); ) {
//			Face face = it.next();
//			for (HalfEdge edge : face.getEdges()) {
//				vertices[0] = face.facePoint;
//				vertices[1] = edge.prev.isMaster() ? edge.prev.edgePoint : edge.prev.pair.edgePoint;
//				vertices[2] = edge.vertex.subVertex;
//				vertices[3] = edge.isMaster() ? edge.edgePoint : edge.pair.edgePoint;
//				if (edge.isMaster()) {
//					vertices[3] = edge.edgePoint;
//					vertexList.add(edge.edgePoint);
//				} else {
//					vertices[3] = edge.pair.edgePoint;
//				}
//				newFaces.add(createFace(vertices, face.level + 1));
//				
//			}
//			it.remove();
//			vertexList.add(face.facePoint);
//		}
//		faceList.addAll(newFaces);
//		validateVertices();
//	}
	
//	void replaceFaces() {
//		for (HalfEdge edge : new HashSet<HalfEdge>(poisonedEdges)) {
//			replaceEdge(edge);
//		}
//		poisonedEdges = null;
//	}
	
	void validateVertices() {
		for (TopLevelVertex vertex : vertexList) {
			vertex.validate();
		}
	}
	
	public void dump() {
		System.out.println("Faces:");
		for (Face face : faceList) {
			System.out.print(face + ": ");
			for (HalfEdge edge : face.getEdges()) {
				System.out.print(edge + " ");
			}
			System.out.println();
		}
		List<HalfEdge> edgeList = new ArrayList<HalfEdge>();
		for (Face face : faceList) {
			for (HalfEdge edge : face.getEdges()) {
				edgeList.add(edge);
			}
		}
		Collections.sort(edgeList, new Comparator<HalfEdge>() {
			public int compare(HalfEdge o1, HalfEdge o2) {
				return o1.num < o2.num ? -1 : o2.num < o1.num ? 1 : 0;
			}
		});
		System.out.println("Edges:");
		for (HalfEdge e : edgeList) {
			System.out.println(e + ": v1=" + e.getVertex() + " v2=" + e.getPairVertex() + " f=" + e.getFace() + " n=" + e.getNext() + " p=" + e.getPrev());
		}
		
		System.out.println("Vertices:");
		for (TopLevelVertex vertex : vertexList) {
			System.out.print(vertex + ": ");
			for (HalfEdge edge : vertex.edges) {
				System.out.print(edge + " ");
			}
			System.out.println();
		}
	}
	
	public void verify() {
		Set<TopLevelVertex> vertexSet = new HashSet<TopLevelVertex>(vertexList);
		System.out.println(vertexList.size() + " " + vertexSet.size());
		for (Face face : faceList) {
			for (HalfEdge edge : face.getEdges()) {
				if (!vertexSet.contains(edge.getVertex())) {
					System.out.println(edge.getVertex());
				}
				if (!vertexSet.contains(edge.getPairVertex())) {
					System.out.println(edge.getPairVertex());
				}
			}
		}		
	}
	
	
	
	
	public String toString() {
		return faceList.size() + " faces, " + vertexList.size() + " vertices";
	}
	
	void addFace(int[] vertices) {
		HalfEdge start = getHalfEdge(vertexList.get(vertices[0]), vertexList.get(vertices[1]));
		HalfEdge prev = start;
		HalfEdge edge = null;
		for (int i = 1; i < vertices.length; i++) {
			edge = getHalfEdge(vertexList.get(vertices[i]), vertexList.get(vertices[(i + 1) % vertices.length]));
			edge.appendTo(prev);
			prev = edge;
		}
		start.appendTo(edge);
		Face face = new Face(vertices.length, start);
		faceList.add(face);
	}
	
	void addCandidateFace(TopLevelVertex[] vertices) {
		candidateFaceList.add(vertices);
		for (int i = 0; i < vertices.length; i++) {
			CandidateEdge ce = new CandidateEdge(vertices[i], vertices[(i + 1) % vertices.length]);
			if (candidateEdgeMap.containsKey(ce)) {
				candidateEdgeMap.put(ce, candidateEdgeMap.get(ce) + 1);
			} else {
				candidateEdgeMap.put(ce, 1);
			}
		}
	}
	
	void addCandidateFaces() {
		Set<Integer> verticesToReplace = new HashSet<Integer>();
		Set<TopLevelVertex> vertexSet = new HashSet<TopLevelVertex>();
		for (TopLevelVertex[] vertices : candidateFaceList) {
			verticesToReplace.clear();
			for (int i = 0; i < vertices.length; i++) {
				TopLevelVertex v0 = vertices[i];
				TopLevelVertex v1 = vertices[(i + 1) % vertices.length];
				int edgeCount = candidateEdgeMap.get(new CandidateEdge(v0, v1));
				if (edgeCount > 2) {
					verticesToReplace.add(i);
					verticesToReplace.add((i + 1) % vertices.length);
				}
			}
			for (int i : verticesToReplace) {
				vertices[i] = new TopLevelVertex(vertices[i]);
				vertices[i].sharpness.setDouble(10.0);
			}
			for (TopLevelVertex v : vertices) {
				vertexSet.add(v);
			}
			addFace(vertices);
		}
		vertexList.addAll(vertexSet);
	}
	
	Face addFace(TopLevelVertex[] vertices) {
		for (int i = 0; i < vertices.length; i++) {
			TopLevelVertex v0 = vertices[i];
			TopLevelVertex v1 = vertices[(i + 1) % vertices.length];
			HalfEdge nonManifoldEdge = checkEdge(v0, v1);
			if (nonManifoldEdge != null) {
				v0.sharpness.setDouble(10.0);
				v1.sharpness.setDouble(10.0);
				v0 = new TopLevelVertex(v0);
				v1 = new TopLevelVertex(v1);
				v0.sharpness.setDouble(10.0);
				v1.sharpness.setDouble(10.0);
				vertexList.add(v0);
				vertexList.add(v1);
				vertices[i] = v0;
				vertices[(i + 1) % vertices.length] = v1;
				nonManifoldEdge.getSharpness().setDouble(10.0);
			}
		}
		HalfEdge start = getHalfEdge(vertices[0], vertices[1]);
		HalfEdge prev = start;
		HalfEdge edge = null;
		for (int i = 1; i < vertices.length; i++) {
			edge = getHalfEdge(vertices[i], vertices[(i + 1) % vertices.length]);
			edge.appendTo(prev);
			prev = edge;
		}
		start.appendTo(edge);
		Face face = new Face(vertices.length, start);
		faceList.add(face);
		return face;
	}
	
	public void removeFace(Face face) {
		System.out.println("removeing face " + face);
		/* remove face from facelist (or throw Exception if it isn't in facelist) */
		int index = faceList.indexOf(face);
		if (index == -1) {
			throw new IllegalArgumentException("Face " + face + " is not in SDS");
		}
		faceList.remove(index);
		
		/* remove face from edges */
		for (HalfEdge halfEdge : face.getEdges()) {
			halfEdge.setFace(null);
			halfEdge.getEdgePoint().invalidate();
			halfEdge.getVertex().getVertexPoint().invalidate();
			halfEdge.getPairVertex().getVertexPoint().invalidate();
			
			/* check if the pair edge is in use, and remove it if not */
			if (halfEdge.getPairFace() != null) {
				halfEdge.getPairFace().getFacePoint().invalidate();
			} else {
				removeEdge(halfEdge);
			}
		}
	}
	
//	private Face createFace(TopLevelVertex[] vertices) {
//		HalfEdge start = createEdge(vertices[0], vertices[1]);
//		HalfEdge prev = start;
//		HalfEdge edge = null;
//		for (int i = 1; i < vertices.length; i++) {
//			edge = createEdge(vertices[i], vertices[(i + 1) % vertices.length]);
//			prev.next = edge;
//			edge.prev = prev;
//			prev = edge;
//		}
//		edge.next = start;
//		start.prev = edge;
//		Face face = new Face(vertices.length, start);
//		return face;
//	}
	
	private HalfEdge checkEdge(TopLevelVertex vertex0, TopLevelVertex vertex1) {
		EdgeKey key = new EdgeKey(vertex1, vertex0);
		HalfEdge neighbor = edgeMap.get(key);
		if (neighbor != null && (neighbor.getPairFace() != null || poisonedEdges.contains(neighbor) || poisonedEdges.contains(neighbor.getPair()))) {
			return neighbor.getPrimary();
		} else {
			return null;
		}
	}
	
	/**
	 * Checks if the halfEdge vertex0->vertex1 already exists. If it exists and it's face is null,
	 * it is returned, if the face is not null, an IllegalStateException is thrown (non-manifold surface,
	 * each HalfEdge can only belong to one face). If it doesn't exist, a new edge is created, added to the SDS
	 * and returned
	 * @param vertex0
	 * @param vertex1
	 * @return
	 */
	private HalfEdge getHalfEdge(TopLevelVertex vertex0, TopLevelVertex vertex1) {
		/* check if the HalfEdge (v0->v1) already exists */
		HalfEdge edge = edgeMap.get(new EdgeKey(vertex0, vertex1));
		if (edge == null) {
			/* if no edge is found, create a new one and store it in the maps */
			edge = new HalfEdge(vertex0, vertex1);
			addHalfEdge(edge);
		} else {
			if (edge.getFace() != null) {
				throw new IllegalStateException("Surface is non-manifold");
			}
		}
		return edge;
	}
	
	/**
	 * Adds specified halfEdge and its pair to edgeMap.
	 * Adds specified halfEdge and its pair to their vertices
	 * @param halfEdge
	 */
	private void addHalfEdge(HalfEdge halfEdge) {
		TopLevelVertex v0 = halfEdge.getVertex();
		TopLevelVertex v1 = halfEdge.getPairVertex();
		EdgeKey key = new EdgeKey(v0, v1);
		EdgeKey pairKey = new EdgeKey(v1, v0);
		if (edgeMap.containsKey(key) || edgeMap.containsKey(pairKey)) {
			throw new IllegalStateException("HalfEdge " + halfEdge + " already in SDS");
		}
		edgeMap.put(key, halfEdge);
		edgeMap.put(pairKey, halfEdge.getPair());
		v0.addEdge(halfEdge);
		v1.addEdge(halfEdge.getPair());
	}
	
	/**
	 * Removes specified halfEdge and its pair from edgeMap.
	 * Removes specified halfEdge and its pair from their vertices
	 * @param halfEdge
	 */
	private void removeEdge(HalfEdge halfEdge) {
		if (halfEdge.getFace() != null || halfEdge.getPairFace() != null) {
			throw new IllegalStateException("Edge " + halfEdge + " still has faces");
		}
		TopLevelVertex v0 = halfEdge.getVertex();
		TopLevelVertex v1 = halfEdge.getPairVertex();
		EdgeKey key = new EdgeKey(v0, v1);
		EdgeKey pairKey = new EdgeKey(v1, v0);
		if (!edgeMap.containsKey(key) || !edgeMap.containsKey(pairKey)) {
			throw new IllegalStateException("HalfEdge " + halfEdge + " not in SDS");
		}
		edgeMap.remove(key);
		edgeMap.remove(pairKey);
		v0.removeEdge(halfEdge);
		v1.removeEdge(halfEdge.getPair());
	}
	
	private static final class EdgeKey {
		private final TopLevelVertex firstVertex;
		private final TopLevelVertex secondVertex;
		private final int hashCode;
		
		private EdgeKey(TopLevelVertex firstVertex, TopLevelVertex secondVertex) {
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
	
	private static final class CandidateEdge {
		private final TopLevelVertex firstVertex;
		private final TopLevelVertex secondVertex;
		private final int hashCode;
		
		private CandidateEdge(TopLevelVertex firstVertex, TopLevelVertex secondVertex) {
			this.firstVertex = firstVertex;
			this.secondVertex = secondVertex;
			hashCode = System.identityHashCode(firstVertex) ^ System.identityHashCode(secondVertex);
		}
		
		@Override
		public int hashCode() {
			return hashCode;
		}
		
		@Override
		public boolean equals(Object o) {
			CandidateEdge ce = (CandidateEdge) o;
			return (firstVertex == ce.firstVertex && secondVertex == ce.secondVertex) || (firstVertex == ce.secondVertex && secondVertex == ce.firstVertex);
		}
	}
}
