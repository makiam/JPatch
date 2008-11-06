package com.jpatch.entity.sds2;

import com.jpatch.afw.*;
import com.jpatch.afw.attributes.*;
import com.jpatch.afw.control.*;
import com.jpatch.afw.ui.*;
import com.jpatch.boundary.*;
import com.jpatch.entity.*;

import java.util.*;

public class Sds {
	public static final class Type {
		public static final int VERTEX = 1 << 0;
		public static final int LIMIT = 1 << 1;
		public static final int EDGE = 1 << 2;
		public static final int FACE = 1 << 3;
		public static final int STRAY_VERTEX = 1 << 4;
		public static final int STRAY_EDGE = 1 << 5;
		public static final int BOUNDARY_EDGE = 1 << 6;
		
		private Type() {
			assert false;	// not instanciable
		}
	}
	
	
	private int currentMaxLevel = 1;
	private IntAttr maxLevelAttr = AttributeManager.getInstance().createBoundedIntAttr(currentMaxLevel, 0, SdsConstants.MAX_LEVEL);
	private IntAttr renderLevelAttr = AttributeManager.getInstance().createBoundedIntAttr(currentMaxLevel, 0, SdsConstants.MAX_LEVEL);
	private IntAttr editLevelAttr = AttributeManager.getInstance().createBoundedIntAttr(0, 0, SdsConstants.MAX_LEVEL);
	
	private final static Comparator<Face> faceMaterialComparator = new Comparator<Face>() {
		public int compare(Face f1, Face f2) {
			int m1 = System.identityHashCode(f1.getMaterial());
			int m2 = System.identityHashCode(f2.getMaterial());
			return (m1 < m2) ? -1 : (m1 > m2) ? 1 : 0;
		}		
	};
	
	private final Map<Integer, Face> faceIdMap = new HashMap<Integer, Face>();
	@SuppressWarnings("unchecked")
	private final Set<Face>[] faceSets = new Set[SdsConstants.MAX_LEVEL + 1];
	@SuppressWarnings("unchecked")
	private final List<Face>[] faceLists = new List[SdsConstants.MAX_LEVEL + 1];
	private final Set<HalfEdge> strayEdges = new HashSet<HalfEdge>();
	private final Set<BaseVertex> strayVertices = new HashSet<BaseVertex>();
	private final Set<BaseVertex[]> strayFaces = new HashSet<BaseVertex[]>();
	@SuppressWarnings("unchecked")
	private final Set<Displacement>[] hierarchy = new Set[SdsConstants.MAX_LEVEL + 1];
	private final EdgeSet edgeSet = new EdgeSet();
	
//	private MorphTarget activeMorphTarget;
	private final MorphController morphController = new MorphController(this);
	private final NdeLayerManager ndeLayerManager = new NdeLayerManager(morphController);
	
	private boolean facesSorted;
	
	/**
	 * A special purpose hashtable that associates vertex-pairs with edges.
	 * It is used to find existing edges for given vertex-pairs.
	 * @author sascha
	 */
	public static class EdgeSet {
		final static double growFactor = 0.75;
		final static double shrinkFactor = 0.25;
		final static int MIN = 256;
		int size;
		int capacity;
		int mask;
		int count;
		int max;
		int min;
		private HalfEdge[][] buckets;
		
		private EdgeSet() {
			setSize(8);	//initial capacity = 256
			buckets = new HalfEdge[capacity][];
		}
		
		/**
		 * compute hashcode for vertex pairs.
		 * Hashcode for v0,v1 is identical to hashcode of v1,v0
		 */
		private int hash(final AbstractVertex v0, final AbstractVertex v1) {
			return (v0.hashCode() ^ v1.hashCode()) & mask;
		}
		
		/**
		 * Returns a HalfEdge that connects the specified vertices v0 and v1.
		 * It that HalfEdge already exists, it is returned (this is also true if only
		 * it's pair has been created yet). If not, a new HalfEdge is stored in the
		 * hashTable and returned.
		 * @param v0 the first vertex
		 * @param v1 the second vertex
		 * @return the HalfEdge connecting the specified vertices
		 */
		public HalfEdge getHalfEdge(final AbstractVertex v0, final AbstractVertex v1) {
			final int index = hash(v0, v1);
			HalfEdge[] bucket = buckets[index];
			/* check if the bucket is non-empty */
			if (bucket != null) {
				/* scan bucket sequentially for the halfEdge or its pair*/
				for (HalfEdge halfEdge : bucket) {
					if (halfEdge.getVertex() == v0 && halfEdge.getPairVertex() == v1) {
						return halfEdge;
					} else if (halfEdge.getVertex() == v1 && halfEdge.getPairVertex() == v0) {
						return halfEdge.getPair();
					}
				}
			}
			/* create a new halfedge and add it to the bucket */
			HalfEdge halfEdge = new HalfEdge(v0, v1);
			add(index, halfEdge);
			return halfEdge;
		}
		
		/**
		 * Removes the specified HalfEdge (or its pair) from the hashTable.
		 * @param halfEdge the HalfEdge to remove
		 */
		public void removeHalfEdge(final HalfEdge halfEdge) {
			remove(hash(halfEdge.getVertex(), halfEdge.getPairVertex()), halfEdge);
		}
		
		/**
		 * Adds a halfEdge to a bucket
		 */
		private void add(final int bucketIndex, final HalfEdge halfEdge) {
			if (buckets[bucketIndex] == null) {
				/* empty bucket, create new one */
				buckets[bucketIndex] = new HalfEdge[] { halfEdge };
			} else {
				/* non empty bucket, grow by one element and copy all elements */
				HalfEdge[] tmp = buckets[bucketIndex];
				buckets[bucketIndex] = new HalfEdge[tmp.length + 1];
				System.arraycopy(tmp, 0, buckets[bucketIndex], 0, tmp.length);
				buckets[bucketIndex][tmp.length] = halfEdge;
			}
			/* increase count, rehash if necessary */
			count++;
			if (count > max) {
				setSize(size + 1);
				rehash();
			}
		}
		
		/**
		 * Removes a halfEdge from a bucket
		 */
		private void remove(final int bucketIndex, final HalfEdge halfEdge) {
			HalfEdge[] tmp = buckets[bucketIndex];
			if (tmp.length == 1) {
				/* length was 1, bucket is empty now */
				buckets[bucketIndex] = null;
			} else {
				/* shrink bucket by 1*/
				buckets[bucketIndex] = new HalfEdge[tmp.length - 1];
				/* scan for element to discard */
				int i = 0;
				final HalfEdge pair = halfEdge.getPair();
		    	while (i < tmp.length && tmp[i] != halfEdge && tmp[i] != pair) {
		    		i++;
		    	}
		    	assert(i < tmp.length) : "element not found in hash bucket";
		    	/* copy the other elements */
		    	System.arraycopy(tmp, 0, buckets[bucketIndex], 0, i);
		    	if (i < buckets[bucketIndex].length) {
	    	    	System.arraycopy(tmp, i + 1, buckets[bucketIndex], i, buckets[bucketIndex].length - i);
		    	}
			}
			/* decrease count, rehash if necessary */
			count--;
			if (count < min) {
				setSize(size - 1);
				rehash();
			}
		}
		
		/**
		 * Modify number of buckets and reshash
		 */
		private void rehash() {
			System.out.println("rehashing, new capacity=" + capacity);
			HalfEdge[][] tmp = buckets;
			/* new number of buckets */
			buckets = new HalfEdge[capacity][];
			count = 0; // reset count!
			/* add all old elements to new buckets */
			for (HalfEdge[] bucket : tmp) {
				if (bucket != null) {
					for (int i = 0; i < bucket.length; i ++) {
						HalfEdge halfEdge = bucket[i];
						final int index = hash(halfEdge.getVertex(), halfEdge.getPairVertex());
						add(index, halfEdge);
					}
				}
			}
			dump();
		}
		
		/**
		 * compute capacity, bitmask and min/max load values for a specified size
		 * @param size capacity = 2^size
		 */
		private void setSize(int size) {
			this.size = size;
			capacity = 1 << size;
			max = (int) (capacity * growFactor);
			min = (int) (capacity * shrinkFactor);
			if (min < MIN) {
				min = 0;
			}
			mask = capacity - 1;
		}
		
		private void dump() {
			dump(buckets);
		}
		
		private void dump(HalfEdge[][] buckets) {
			for (int i = 0; i < buckets.length; i++) {
				System.out.print("Bucket " + i + ":\t");
				if (buckets[i] != null) {
					for (HalfEdge edge : buckets[i]) {
						System.out.print(" " + edge);
					}
					System.out.println();
				} else {
					System.out.println(" *empty*");
				}
			}
		}
	}
	
	public Sds(final JPatchUndoManager undoManager) {
		
		for (int i = 0; i < faceSets.length; i++) {
			faceSets[i] = new HashSet<Face>();
			faceLists[i] = new ArrayList<Face>();
			hierarchy[i] = new HashSet<Displacement>();
		}
		
		/* add or remove faces on new (old) levels when maxLevel changes */
		maxLevelAttr.addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				// add or remove faces on new (old) levels
				setMaxLevel();
			}
		});
		
		/* ensures that maxLevel can't be lower than renderLevel */
		maxLevelAttr.addAttributePreChangeListener(new AttributePreChangeAdapter<Object>() {
			@Override
			public int attributeWillChange(ScalarAttribute source, int value) {
				return Math.max(renderLevelAttr.getInt(), value);
			}
		});
		
		/* ensures that render-level is lower than max level */
		renderLevelAttr.addAttributePreChangeListener(new AttributePreChangeAdapter<Object>() {
			@Override
			public int attributeWillChange(ScalarAttribute source, int value) {
				int level = Math.min(maxLevelAttr.getInt(), value);
				level = Math.max(editLevelAttr.getInt(), level);
				return level;
			}
		});
		
		/* ensures that edit-level is lower than render level */
		editLevelAttr.addAttributePreChangeListener(new AttributePreChangeAdapter<Object>() {
			@Override
			public int attributeWillChange(ScalarAttribute source, int value) {
				return Math.min(renderLevelAttr.getInt(), value);
			}
		});
	}
	
	public IntAttr getMaxLevelAttribute() {
		return maxLevelAttr;
	}
	
	public IntAttr getRenderLevelAttribute() {
		return renderLevelAttr;
	}
	
	public IntAttr getEditLevelAttribute() {
		return editLevelAttr;
	}
	
	public NdeLayerManager getNdeLayerManager() {
		return ndeLayerManager;
	}
//	
	public MorphTarget getActiveMorphTarget() {
		return morphController.getActiveMorphTarget();
	}
	
	public MorphController getMorphController() {
		return morphController;
	}
//	
//	
//	public void setActiveMorphTarget(MorphTarget morphTarget) {
//		this.activeMorphTarget = morphTarget;
//	}
	
	public void addSegment(List<JPatchUndoableEdit> editList, AbstractVertex vertex0, AbstractVertex vertex1) {
//		edgeKey.set(vertex0, vertex1);
//		assert (!edgeMap.containsKey(edgeKey));
//		HalfEdge edge = new HalfEdge(vertex0, vertex1);
//		JPatchUndoableEdit addEdgeEdit = new AddEdgeEdit(edge, 0);
		JPatchUndoableEdit addStrayEdgeEdit = new AddStrayEdgeEdit(vertex0, vertex1);
		if (editList != null) {
//			editList.add(addEdgeEdit);
			editList.add(addStrayEdgeEdit);
		}
	}
	
	public void removeSegment(List<JPatchUndoableEdit> editList, HalfEdge strayEdge) {
		assert (strayEdges.contains(strayEdge));
		assert (strayVertices.contains(strayEdge.getVertex()));
		assert (strayVertices.contains(strayEdge.getPairVertex()));
		JPatchUndoableEdit removeStrayEdgeEdge = new RemoveStrayEdgeEdit(strayEdge);
//		JPatchUndoableEdit removeEdgeEdit = new RemoveEdgeEdit(strayEdge, 0);
		if (editList != null) {
			editList.add(removeStrayEdgeEdge);
//			editList.add(removeEdgeEdit);
		}
	}
	
	public Displacement createHierarchyModification(int[] path) {
		int level = path.length - 2;
		Displacement hierarchicalVertexModification = new Displacement(path);
		hierarchy[level].add(hierarchicalVertexModification);
		return hierarchicalVertexModification;
	}
	
	public void discardHierarchicalVertexModification(Displacement hierarchicalVertexModification) {
		int level = hierarchicalVertexModification.hierarchyPath.length - 2;
		assert hierarchy[level].contains(hierarchicalVertexModification);
		hierarchy[level].remove(hierarchicalVertexModification);
	}
	
	public void addStrayFace(List<JPatchUndoableEdit> editList, BaseVertex[] vertices) {
		AddStrayFaceEdit edit = new AddStrayFaceEdit(vertices);
		if (editList != null) {
			editList.add(edit);
		}
	}
	
	public void removeStrayEdge(List<JPatchUndoableEdit> editList, HalfEdge strayEdge) {
		RemoveStrayEdgeEdit edit = new RemoveStrayEdgeEdit(strayEdge);
		if (editList != null) {
			editList.add(edit);
		}
	}
	
	public void removeStrayFace(List<JPatchUndoableEdit> editList, BaseVertex[] vertices) {
		RemoveStrayFaceEdit edit = new RemoveStrayFaceEdit(vertices);
		if (editList != null) {
			editList.add(edit);
		}
	}
	
	private Face createFace(int level, Face parent, int edgeIndex, Material material, HalfEdge... edges) {
		assert level > 0;
		assert parent != null;
//		System.out.print("add face called: ");
//		for (AbstractVertex vertex : vertices) {
//			System.out.print(vertex + " ");
//		}
//		System.out.println();
		
//		for (int i = 0; i < vertices.length; i++) {
//			int j = i + 1;
//			if (j == vertices.length) {
//				j = 0;
//			}
//			edges[i] = getHalfEdge(vertices[i], vertices[j]);
//		}
		
		Face face = new Face(material, edges, parent, edgeIndex);
		
//		if (level == 0) System.out.println(this + " adding face " + face);
		
		assert !faceSets[level].contains(face) : "Face " + face + " has already been added to " + Sds.this + " at level " + level;
		faceSets[level].add(face);
		
		
		if (level < maxLevelAttr.getInt()) {
			subdivideFace(level, face);
		}
		return face;
	}
	
//	public Face addFace(List<JPatchUndoableEdit> editList, Material material, AbstractVertex... invertices) {
//		AbstractVertex[] vertices = invertices.clone();
//		HalfEdge[] edges = new HalfEdge[vertices.length];
//
////		System.out.print("add face called: ");
////		for (AbstractVertex vertex : vertices) {
////			System.out.print(vertex + " ");
////		}
////		System.out.println();
//		
//		for (int i = 0; i < vertices.length; i++) {
//			int j = i + 1;
//			if (j == vertices.length) {
//				j = 0;
//			}
//			edges[i] = getHalfEdge(editList, vertices[i], vertices[j]);
//		}
//		for (AbstractVertex vertex : vertices) {
////			System.out.println("  saving edges for " + vertex);
//			vertex.saveEdges(editList);
//		}
//		
//		Face face;
//		
//		face = new Face(material, edges);
//		
////		if (level == 0) System.out.println(this + " adding face " + face);
//		AddFaceEdit addFaceEdit = new AddFaceEdit(0, face);
//		if (editList != null) {
//			editList.add(addFaceEdit);
//		}
//		if (0 < maxLevelAttr.getInt()) {
//			subdivideFace(0, face);
//		}
//		return face;
//	}
	
	public void setMaxLevel() {
		System.gc();
		int newLevel = maxLevelAttr.getInt();
		System.out.println("currentMaxLevel = " + currentMaxLevel + ", newLevel = " + newLevel);
		if (newLevel < currentMaxLevel) {
			/* clear references to facepoints on all faces on the new level */
			for (Face face : faceSets[newLevel]) {
				face.disposeFacePoint();
				for (HalfEdge edge : face.getEdges()) {
					edge.disposeEdgePoint();
					edge.getVertex().disposeVertexPoint();
				}
			}
			/* remove all faces at levels higher than the new level */
			for (int level = newLevel + 1; level <= currentMaxLevel; level++) {
				for (Face face : new ArrayList<Face>(faceSets[level])) {
					discardFace(level, face);
				}
			}
		} else if (newLevel > currentMaxLevel) {
			/* create subdivided faces for each level higher than currentlevel (up to newlevel) */
//			int n = faceSets[currentMaxLevel].size();
//			int i = 0, p = 0;
			for (Face face : faceSets[currentMaxLevel]) {
				subdivideFace(currentMaxLevel, face);
//				int np = (i++) * 100 / n;
//				if (np > p) {
//					p = np;
//					System.out.println(i + " (" + p + "%) " + Runtime.getRuntime().maxMemory() + "," + Runtime.getRuntime().totalMemory() + "," + Runtime.getRuntime().freeMemory());
//				}
			}
		}
		currentMaxLevel = newLevel;
		System.gc();
		facesSorted = false;
	}
	
	public void flipFaces(List<JPatchUndoableEdit> editList, Collection<Face> faces) {
		FlipFacesEdit edit = new FlipFacesEdit(faces);
		if (editList != null) {
			editList.add(edit);
		}
	}
	
	private void subdivideFace(int level, Face face) {
		HalfEdge[] edges = face.getEdges();
		HalfEdge[] hubEdges = new HalfEdge[face.getSides()];
		HalfEdge[] newEdges = new HalfEdge[4];
		
		AbstractVertex facePoint = face.createFacePoint();
		for (int i = 0; i < edges.length; i++) {
			if (edges[i].getVertex().getVertexPoint() == null) {
				edges[i].getVertex().createVertexPoint();
			}
		}
		for (int i = 0; i < edges.length; i++) {
			DerivedVertex edgePoint = edges[i].getEdgePoint();
			if (edgePoint == null) {
				edgePoint = edges[i].createEdgePoint();
			}
			hubEdges[i] = new HalfEdge(facePoint, edgePoint);
		}
		for (int i = 0; i < edges.length; i++) {
			int j = (i == edges.length - 1) ? 0 : i + 1;
			newEdges[0] = hubEdges[i];
			newEdges[1] = edges[i].getPair().getSubEdge().getPair();
			newEdges[2] = edges[j].getSubEdge();
			newEdges[3] = hubEdges[j].getPair();
			createFace(level + 1, face, i, face.getMaterial(), newEdges);
//			AbstractVertex v1 = edges[i].getPrev().getEdgePoint();
//			AbstractVertex v2 = edges[i].getVertex().getVertexPoint();
//			AbstractVertex v3 = edges[i].getEdgePoint();
//			createFace(level + 1, face, i, face.getMaterial(), v0, v1, v2, v3);
		}
	}
	
//	public void setFaceMaterial(Face face, Material material) {
//		face.setMaterial(material);
//		DerivedVertex facePoint = face.getFacePoint();
//		for (HalfEdge edge : facePoint.getEdges()) {
//			setFaceMaterial(edge.getFace(), material);
//		}
//	}
	
	public Face addFace(List<JPatchUndoableEdit> editList, Material material, AbstractVertex... vertices) {
		AddFaceEdit edit = new AddFaceEdit(material, vertices);
		if (editList != null) {
			editList.add(edit);
		}
		return edit.face;
	}
	
	public void removeFace(List<JPatchUndoableEdit> editList, Face face) {
		RemoveFaceEdit edit = new RemoveFaceEdit(face);
		if (editList != null) {
			editList.add(edit);
		}
	}
	
	private Face createFace(Material material, AbstractVertex... vertices) {
		System.out.println("createFace(" + material + ", " + Arrays.toString(vertices) + ") called");
		HalfEdge[] edges = new HalfEdge[vertices.length];
		
		for (int i = 0; i < vertices.length; i++) {
			int j = i + 1;
			if (j == vertices.length) {
				j = 0;
			}
			edges[i] = getHalfEdge(vertices[i], vertices[j]);
		}
		
		Face face = new Face(material, edges);
		faceSets[0].add(face);
		facesSorted = false;
		faceIdMap.put(face.id, face);
		
		if (0 < maxLevelAttr.getInt()) {
			subdivideFace(0, face);
		}
		return face;
	}
	
	private void discardFace(int level, Face face) {
		for (HalfEdge edge: face.getEdges()) {
			assert edge.getFace() == face;
			if (level == 0 && edge.getPairFace() == null) {
				discardEdge(edge);
			} else {
				edge.setFace(null);
			}
		}
		assert faceSets[level].contains(face);
		faceSets[level].remove(face);
		if (level == 0) {
			faceIdMap.remove(face.id);
			facesSorted = false;
		}
		DerivedVertex facePoint = face.getFacePoint();
		if (facePoint != null) {
			for (HalfEdge edge : facePoint.getEdges().clone()) {
				discardFace(level + 1, edge.getFace());
			}
		}
	}
	
	private void discardEdge(HalfEdge edge) {
//		edgeKey.set(edge.getVertex(), edge.getPairVertex());
//		assert edgeMap.containsKey(edgeKey);
//		edgeMap.remove(edgeKey);
//		edgeKey.swap();
//		assert edgeMap.containsKey(edgeKey);
//		edgeMap.remove(edgeKey);
		edgeSet.removeHalfEdge(edge);
		edge.dispose();
	}
	
//	public void removeFace(List<JPatchUndoableEdit> editList, int level, Face face) {
////		if (level == 0) System.out.println("removing face " + face);
//		assert faceSets[level].contains(face) : "unknown face";
//		
//		RemoveFaceEdit removeFaceEdit = new RemoveFaceEdit(level, face);
//		if (editList != null) {
//			editList.add(removeFaceEdit);
//		}
//		
//		for (HalfEdge edge : face.getEdges()) {
//			assert edge.getFace() == face : "edge " + edge + " doesn't belong to face " + face;
//			if (editList != null) {
//				edge.saveState(editList);
//				edge.getVertex().saveEdges(editList);
//			}
//			edge.setFace(null);
//		}
//		
//		for (HalfEdge edge : face.getEdges()) {
//			if (edge.getPairFace() == null) {
////				System.out.println("removing edge " + edge);
//				RemoveEdgeEdit removeEdgeEdit = new RemoveEdgeEdit(edge, level);
//				if (editList != null) {
//					editList.add(removeEdgeEdit);
//				}
//			}
//		}
//		
//		for (HalfEdge edge : face.getEdges()) {
//			edge.getVertex().organizeEdges();
//		}
//		
//		DerivedVertex facePoint = face.getFacePoint();
//		if (facePoint != null) {
//			for (HalfEdge subEdge : facePoint.getEdges().clone()) {
//				Face subFace = subEdge.getFace();
//				if (subFace != null) {
//					removeFace(editList, level + 1, subFace);
//				}
//			}
//		}
//		facesSorted = false;
//	}
	
	private void sortFaces() {
		if (facesSorted) {
			return;
		}
		for (int i = 0; i < faceSets.length; i++) {
			faceLists[i].clear();
			faceLists[i].addAll(faceSets[i]);
			Collections.sort(faceLists[i], faceMaterialComparator);
		}
		facesSorted = true;
	}
	
	public DerivedVertex locateVertex(int[] hierarchyId) {
//		System.out.println("hierarchyId = " + Arrays.toString(hierarchyId));
		Face face = faceIdMap.get(hierarchyId[hierarchyId.length - 1]);
		if (face == null) {
			throw new IllegalArgumentException("level-0 face with id " + hierarchyId[0] + " does not exist");
		}
//		System.out.println("base face found: " + face);
		HalfEdge[] subEdges = new HalfEdge[2];
		for (int i = hierarchyId.length - 2; i > 0; i--) {
			face.getSubEdges(hierarchyId[i], subEdges);
			face = subEdges[0].getFace();
//			System.out.println("descending to " + face);
		}
		return (DerivedVertex) face.getEdges()[hierarchyId[0]].getVertex();
	}
	
	public List<Face> getFaces(int level) {
		sortFaces();
		return faceLists[level];
	}
	
	@SuppressWarnings("unchecked")
	public Iterable<? extends AbstractVertex> getVertices(final int level, final boolean includeStrayVertices) {
		if (includeStrayVertices) {
			return new CombinedIterable(getFaceVertices(level), strayVertices);
		} else {
			return getFaceVertices(level);
		}
	}
	
	@SuppressWarnings("unchecked")
	public Iterable<HalfEdge> getEdges(final int level, final boolean includeStrayEdges) {
		if (includeStrayEdges) {
			return new CombinedIterable<HalfEdge>(getFaceEdges(level), strayEdges);
		} else {
			return getFaceEdges(level);
		}
	}
	
	public Iterable<? extends AbstractVertex> getFaceVertices(final int level) {
		return new Iterable<AbstractVertex>() {
			public Iterator<AbstractVertex> iterator() {
				if (faceLists[level].size() == 0) {
					return new Iterator<AbstractVertex>() {
						public boolean hasNext() {
							return false;
						}
						public AbstractVertex next() {
							throw new NoSuchElementException();
						}
						public void remove() {
							throw new UnsupportedOperationException();
						}		
					};
				}
				return new Iterator<AbstractVertex>() {
					Iterator<Face> faces = faceLists[level].iterator();
					Face face = faces.next();
					HalfEdge[] faceEdges = face.getEdges();
					int edgeIndex;
					AbstractVertex nextVertex = getNextVertex();
					
					private final AbstractVertex getNextVertex() {
						if (edgeIndex == faceEdges.length) {
							if (faces.hasNext()) {
								face = faces.next();
								faceEdges = face.getEdges();
								edgeIndex = 0;
							} else {
								return null;
							}
						}
						HalfEdge edge = faceEdges[edgeIndex++];
						AbstractVertex vertex = edge.getVertex();
						if (vertex.getPrimaryFace() != face) {
							return getNextVertex();
						}
						return vertex;
					}
					
					
					public boolean hasNext() {
						return nextVertex != null;
					}

					public AbstractVertex next() {
						AbstractVertex tmp = nextVertex;
						nextVertex = getNextVertex();
						return tmp;
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
	
//	public Iterable<? extends AbstractVertex> getVerticesOld(final int level, final boolean includeStrayVertices) {
//		sortFaces();
//		return new Iterable<AbstractVertex>() {
//			public Iterator<AbstractVertex> iterator() {
//				return new Iterator<AbstractVertex>() {
//					Iterator<Face> faces = faceLists[level].iterator();
//					HalfEdge[] faceEdges;
//					int edgeIndex;
//					Iterator<BaseVertex> vertices = strayVertices.iterator();
//					
//					public boolean hasNext() {
//						if (faceEdges != null && edgeIndex < faceEdges.length) {
//							return true;
//						}
//						if (faces.hasNext()) {
//							faceEdges = faces.next().getEdges();
//							edgeIndex = 0;
//							return hasNext();
//						}
//						if (includeStrayVertices) {
//							return vertices.hasNext();
//						}
//						return false;
//					}
//
//					public AbstractVertex next() {
//						if (faceEdges != null && edgeIndex < faceEdges.length) {
//							return faceEdges[edgeIndex++].getVertex();
//						}
//						if (faces.hasNext()) {
//							faceEdges = faces.next().getEdges();
//							edgeIndex = 0;
//							return next();
//						}
//						if (includeStrayVertices) {
//							return vertices.next();
//						}
//						throw new NoSuchElementException();
//					}
//
//					public void remove() {
//						throw new UnsupportedOperationException();
//					}
//				};
//			}
//		};
//	}
	
	public Collection<BaseVertex> getStrayVertices() {
		return strayVertices;
	}
	
	public Iterable<HalfEdge> getStrayEdges() {
		return strayEdges;
	}
	
	public Collection<BaseVertex[]> getStrayFaces() {
		return strayFaces;
	}
	
	private Iterable<HalfEdge> getFaceEdges(final int level) {
		return new Iterable<HalfEdge>() {
			public Iterator<HalfEdge> iterator() {
				return new Iterator<HalfEdge>() {
					Iterator<Face> faces = faceLists[level].iterator();
					HalfEdge[] faceEdges;
					int edgeIndex;
					
					public boolean hasNext() {
						if (faceEdges != null && edgeIndex < faceEdges.length) {
							return true;
						}
						if (faces.hasNext()) {
							faceEdges = faces.next().getEdges();
							edgeIndex = 0;
							return true;
						}
						return false;
					}


					public HalfEdge next() {
						if (faceEdges != null && edgeIndex < faceEdges.length) {
							return faceEdges[edgeIndex++];
						}
						if (faces.hasNext()) {
							faceEdges = faces.next().getEdges();
							edgeIndex = 1;
							return faceEdges[0];
						}
						throw new NoSuchElementException();
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
	
	public void dumpFaces(int level) {
		System.out.println("FACES");
		for (Face face : faceSets[level]) {
			System.out.print(face + " ");
			for (HalfEdge edge : face.getEdges()) {
				System.out.print(edge + " ");
			}
			System.out.println();
		}
		System.out.println("VERTICES");
		for (int l = level; l <= 0; l++) {
			for (AbstractVertex vertex : getVertices(l, true)) {
				System.out.print(vertex + " ");
				for (HalfEdge edge : vertex.getEdges()) {
					System.out.print(edge + " ");
				}
				System.out.println();
			}
		}
		System.out.println("EDGES");
		for (HalfEdge edge : getEdges(0, true)) {
			System.out.print(edge + " ");
			System.out.print("next=" + edge.getNext() + " ");
			System.out.print("prev=" + edge.getPrev() + " ");
			System.out.print("face=" + edge.getFace() + " ");
			System.out.print("pairFace=" + edge.getPairFace() + " ");
			System.out.println();
		}
		
		System.out.print("stray edges: ");
		for (HalfEdge edge : getStrayEdges()) {
			System.out.print(edge + " ");
		}
		System.out.println();
		
		System.out.print("stray vertices: ");
		for (AbstractVertex vertex : getStrayVertices()) {
			System.out.print(vertex + " ");
		}
		System.out.println();
		
		System.out.print("stray faces: ");
		for (AbstractVertex[] face : getStrayFaces()) {
			System.out.print("{ ");
			for (AbstractVertex vertex : face) {
				System.out.print(vertex + " ");
			}
			System.out.print("} ");
		}
//		System.out.println();
//		System.out.println("edgeset:");
//		edgeSet.dump();
	}
	
//	/**
//	 * Checks if the halfEdge vertex0->vertex1 already exists. If it exists and it's face is null,
//	 * it is returned, if the face is not null, an IllegalStateException is thrown (non-manifold surface,
//	 * each HalfEdge can only belong to one face). If it doesn't exist, a new edge is created, added to the SDS
//	 * and returned
//	 * @param vertex0
//	 * @param vertex1
//	 * @return
//	 */
//	private HalfEdge getHalfEdge(List<JPatchUndoableEdit> editList, AbstractVertex vertex0, AbstractVertex vertex1) {
//		assert vertex0 != vertex1 : "Vertices are identical: " + vertex0;
//		/* check if the HalfEdge (v0->v1) already exists */
//		edgeKey.set(vertex0, vertex1);
//		HalfEdge edge = edgeMaps[0].get(edgeKey);
//		if (edge == null) {
////			System.out.println("create new edge " + vertex0 + "-" + vertex1);
//			/* if no edge is found, create a new one and store it in the maps */
//			edge = new HalfEdge(vertex0, vertex1);
//			JPatchUndoableEdit addEdgeEdit = new AddEdgeEdit(edge, 0);
//			if (editList != null) {
//				editList.add(addEdgeEdit);
//			}
//		} else {
////			System.out.println("found edge " + edge);
//			assert edge.getFace() == null : "Surface is non-manifold, edge=" + edge + " face=" + edge.getFace();
//			if (editList != null) {
//				edge.saveState(editList);
//			}
//			if (strayEdges.contains(edge)) {
//				JPatchUndoableEdit removeStrayEdgeEdit = new RemoveStrayEdgeEdit(edge);
//				if (editList != null) {
//					editList.add(removeStrayEdgeEdit);
//				}
//			}
//		}
//		return edge;
//	}
	
	/**
	 * Checks if the halfEdge vertex0->vertex1 already exists. If it exists and it's face is null,
	 * it is returned, if the face is not null, an IllegalStateException is thrown (non-manifold surface,
	 * each HalfEdge can only belong to one face). If it doesn't exist, a new edge is created, added to the SDS
	 * and returned
	 * @param vertex0
	 * @param vertex1
	 * @return
	 */
	private HalfEdge getHalfEdge(AbstractVertex vertex0, AbstractVertex vertex1) {
		assert vertex0 != vertex1 : "Vertices are identical: " + vertex0;
		/* check if the HalfEdge (v0->v1) already exists */
//		edgeKey.set(vertex0, vertex1);
//		HalfEdge edge = edgeMap.get(edgeKey);
//		if (edge == null) {
////			System.out.println("create new edge " + vertex0 + "-" + vertex1);
//			/* if no edge is found, create a new one and store it in the maps */
//			edge = new HalfEdge(vertex0, vertex1);
//			
//			assert !edgeMap.containsKey(edgeKey) : "HalfEdge " + edge + " already in SDS";
//			edgeMap.put(edgeKey.clone(), edge);
//			edgeKey.swap();
//			edgeMap.put(edgeKey.clone(), edge.getPair());
//		}
//		return edge;
		return edgeSet.getHalfEdge(vertex0, vertex1);
	}
	
	private static class FlipFacesEdit extends AbstractSwapEdit {
		final Face[] faces;
		
		FlipFacesEdit(Collection<Face> faces) {
			this.faces = faces.toArray(new Face[faces.size()]);
			swap();
			applied = true;
		}
		
		@Override
		protected void swap() {
			Set<HalfEdge> edgesToFlip = new HashSet<HalfEdge>();
			Set<HalfEdge> uniqueEdges = new HashSet<HalfEdge>();
			Set<AbstractVertex> verticesToFlip = new HashSet<AbstractVertex>();
			for (Face face : faces) {
				face.flip(edgesToFlip, verticesToFlip);
			}
			for (HalfEdge edge : edgesToFlip) {
				if (!uniqueEdges.contains(edge.getPair())) {
					uniqueEdges.add(edge);
				}
			}
			for (HalfEdge edge : uniqueEdges) {
				edge.flip();
			}
			for (AbstractVertex vertex : verticesToFlip) {
				vertex.flip();
			}
		}
		
	}
	
//	private abstract class EdgeEdit extends AbstractUndoableEdit {
//		final HalfEdge halfEdge;
//		final Map<EdgeKey, HalfEdge> edgeMap;
//		
//		EdgeEdit(HalfEdge halfEdge, int level) {
//			this.halfEdge = halfEdge;
//			edgeMap = edgeMaps[level];
//			apply(true);
//		}
//		
//		void add() {
//			AbstractVertex v0 = halfEdge.getVertex();
//			AbstractVertex v1 = halfEdge.getPairVertex();
//			edgeKey.set(v0, v1);
//			assert !edgeMap.containsKey(edgeKey) : "HalfEdge " + halfEdge + " already in SDS";
//			edgeMap.put(edgeKey.clone(), halfEdge);
//			edgeKey.set(v1, v0);
//			assert !edgeMap.containsKey(edgeKey) : "HalfEdge " + halfEdge + " already in SDS";
//			edgeMap.put(edgeKey.clone(), halfEdge.getPair());
//			v0.addEdge(halfEdge);
//			v1.addEdge(halfEdge.getPair());
//		}
//		
//		void remove() {
//			AbstractVertex v0 = halfEdge.getVertex();
//			AbstractVertex v1 = halfEdge.getPairVertex();
//			edgeKey.set(v0, v1);
//			assert edgeMap.containsKey(edgeKey) : "HalfEdge " + halfEdge + " noz in SDS";
//			edgeMap.remove(edgeKey);
//			edgeKey.set(v1, v0);
//			assert edgeMap.containsKey(edgeKey) : "HalfEdge " + halfEdge + " noz in SDS";
//			edgeMap.remove(edgeKey);
//			v0.removeEdge(halfEdge);
//			v1.removeEdge(halfEdge.getPair());
//		}
//	}
//	
//	private class AddEdgeEdit extends EdgeEdit {
//		private AddEdgeEdit(HalfEdge halfEdge, int level) {
//			super(halfEdge, level);
//		}
//		
//		public void undo() {
//			super.undo();
//			remove();
//		}
//		
//		public void redo() {
//			super.redo();
//			add();
//		}
//	}
//	
//	private class RemoveEdgeEdit extends EdgeEdit {
//		private RemoveEdgeEdit(HalfEdge halfEdge, int level) {
//			super(halfEdge, level);
//		}
//		
//		public void undo() {
//			super.undo();
//			add();
//		}
//		
//		public void redo() {
//			super.redo();
//			remove();
//		}
//	}
	
	private abstract class StrayEdgeEdit extends AbstractUndoableEdit {
		final AbstractVertex v0;
		final AbstractVertex v1;
		StrayEdgeEdit(AbstractVertex v0, AbstractVertex v1) {
			this.v0 = v0;
			this.v1 = v1;
			apply(true);
		}
		
		void add() {
			final HalfEdge halfEdge = edgeSet.getHalfEdge(v0, v1);
			strayEdges.add(halfEdge);
			strayEdges.add(halfEdge.getPair());
			strayVertices.add((BaseVertex) halfEdge.getVertex());
			strayVertices.add((BaseVertex) halfEdge.getPairVertex());
//			edgeKey.set(halfEdge.getVertex(), halfEdge.getPairVertex());
//			edgeMap.put(edgeKey.clone(), halfEdge);
		}
		
		void remove() {
			final HalfEdge halfEdge = edgeSet.getHalfEdge(v0, v1);
			strayEdges.remove(halfEdge);
			strayEdges.remove(halfEdge.getPair());
//			edgeKey.set(halfEdge.getVertex(), halfEdge.getPairVertex());
//			edgeMap.remove(edgeKey);
//			edgeKey.swap();
//			edgeMap.remove(edgeKey);
//			halfEdge.getVertex().removeEdge(halfEdge);
			halfEdge.getVertex().removeEdge(halfEdge);
			halfEdge.getPairVertex().removeEdge(halfEdge.getPair());
			if (halfEdge.getVertex().getEdges().length == 0) {
				strayVertices.remove(halfEdge.getVertex());
			}
			if (halfEdge.getPairVertex().getEdges().length == 0) {
				strayVertices.remove(halfEdge.getPairVertex());
			}
			edgeSet.removeHalfEdge(halfEdge);
//			for (AbstractVertex v : halfEdge.getVertices(new AbstractVertex[2])) {
//				
//				
//				boolean removeStrayVertex = true;
//				for (HalfEdge e : v.getEdges()) {
//					if (!e.isStray()) {
//						removeStrayVertex = false;
//						continue;
//					}
//				}
//				if (removeStrayVertex) {
//					strayVertices.remove(v);
//				}
//			}
		}
	}
	
	private class AddStrayEdgeEdit extends StrayEdgeEdit {
		private AddStrayEdgeEdit(AbstractVertex v0, AbstractVertex v1) {
			super(v0, v1);
		}
		
		public void undo() {
			super.undo();
			remove();
		}
		
		public void redo() {
			super.redo();
			add();
		}
	}
	
	private class RemoveStrayEdgeEdit extends StrayEdgeEdit {
		private RemoveStrayEdgeEdit(HalfEdge halfEdge) {
			super(halfEdge.getVertex(), halfEdge.getPairVertex());
		}
		
		public void undo() {
			super.undo();
			add();
		}
		
		public void redo() {
			super.redo();
			remove();
		}
	}
	
	private abstract class StrayFaceEdit extends AbstractUndoableEdit {
		BaseVertex[] vertices;
		StrayFaceEdit(BaseVertex[] vertices) {
			this.vertices = vertices;
			apply(true);
		}
		
		void add() {
			strayFaces.add(vertices);
		}
		
		void remove() {
			strayFaces.remove(vertices);
		}
	}
	
	private class AddStrayFaceEdit extends StrayFaceEdit {
		private AddStrayFaceEdit(BaseVertex[] vertices) {
			super(vertices);
		}
		
		public void undo() {
			super.undo();
			remove();
		}
		
		public void redo() {
			super.redo();
			add();
		}
	}
	
	private class RemoveStrayFaceEdit extends StrayFaceEdit {
		private RemoveStrayFaceEdit(BaseVertex[] vertices) {
			super(vertices);
		}
		
		public void undo() {
			super.undo();
			add();
		}
		
		public void redo() {
			super.redo();
			remove();
		}
	}
	
	private abstract class FaceEdit extends AbstractUndoableEdit {
		Material material;
		AbstractVertex[] vertices;
		Face face;
		
		void add() {
			assert !faceSets[0].contains(face) : "Face " + face + " has already been added to " + Sds.this;
			face = createFace(material, vertices);
		}
		
		void remove() {
			assert faceSets[0].contains(face) : "Face " + face + " is unknown to " + Sds.this;
			discardFace(0, face);
		}
	}
	
	private class AddFaceEdit extends FaceEdit {
		private AddFaceEdit(Material material, AbstractVertex[] vertices) {
			this.vertices = vertices.clone();
			this.material = material;
			apply(true);
		}
		
		public void undo() {
			super.undo();
			remove();
		}
		
		public void redo() {
			super.redo();
			add();
		}
	}
	
	private class RemoveFaceEdit extends FaceEdit {
		private RemoveFaceEdit(Face face) {
			this.face = face;
			vertices = new AbstractVertex[face.getSides()];
			for (int i = 0; i < vertices.length; i++) {
				vertices[i] = face.getEdges()[i].getVertex();
			}
			material = face.getMaterial();
			apply(true);
		}
		
		public void undo() {
			super.undo();
			add();
		}
		
		public void redo() {
			super.redo();
			remove();
		}
	}
	
	public HalfEdge getNextStrayEdge(HalfEdge strayEdge) {
		assert strayEdges.contains(strayEdge) : "edge " + strayEdge + " not in " + strayEdges;
		HalfEdge[] vertexEdges = strayEdge.getPairVertex().getEdges();
		if (vertexEdges.length == 2) {
			assert (vertexEdges[0] == strayEdge.getPair() || vertexEdges[1] == strayEdge.getPair());
			return (vertexEdges[0] == strayEdge.getPair()) ? vertexEdges[1] : vertexEdges[0];
		} else {
			assert vertexEdges.length == 1;
			return null;
		}
	}
	
	public HalfEdge getPrevStrayEdge(HalfEdge strayEdge) {
		assert strayEdges.contains(strayEdge);
		HalfEdge[] vertexEdges = strayEdge.getVertex().getEdges();
		if (vertexEdges.length == 2) {
			assert (vertexEdges[0] == strayEdge || vertexEdges[1] == strayEdge);
			return (vertexEdges[0] == strayEdge) ? vertexEdges[1].getPair() : vertexEdges[0].getPair();
		} else {
			assert vertexEdges.length == 1;
			return null;
		}
	}
	
	public HalfEdge getStart(HalfEdge strayEdge) {
		assert strayEdges.contains(strayEdge);
		HalfEdge startEdge = strayEdge;
		HalfEdge prevEdge = getPrevStrayEdge(strayEdge);
		while (prevEdge != null && prevEdge != strayEdge) {
			HalfEdge tmp = startEdge;
			startEdge = prevEdge;
			prevEdge = getPrevStrayEdge(tmp);
		}
		return startEdge;
	}
	
	public BaseVertex[] getChain(BaseVertex strayVertex) {
		assert strayVertices.contains(strayVertex);
//		assert strayVertex.getEdges().length == 1;
		HalfEdge edge = strayVertex.getEdges()[0];
		List<BaseVertex> vertices = new ArrayList<BaseVertex>();
		BaseVertex startVertex = (BaseVertex) edge.getVertex();
		vertices.add(startVertex);
		while (edge != null) {
			BaseVertex v = (BaseVertex) edge.getPairVertex();
			vertices.add(v);
			if (v == startVertex) {
				break;
			}
			edge = getNextStrayEdge(edge);
		}
		return vertices.toArray(new BaseVertex[vertices.size()]);
	}
	
	public boolean isConnected(BaseVertex a, BaseVertex b) {
		assert strayVertices.contains(a);
		assert strayVertices.contains(b);
		assert a.getEdges().length == 1;
		assert b.getEdges().length == 1;
		HalfEdge strayEdge = a.getEdges()[0];
		while (strayEdge != null && strayEdge.getPairVertex() != b) {
			assert strayEdge.getPairVertex() != a;
			strayEdge = getNextStrayEdge(strayEdge);
		}
		if (strayEdge == null) {
			return false;
		} else {
			assert strayEdge.getPairVertex() == b;
			return true;
		}
	}
	
	public boolean isStartOfChain(BaseVertex strayVertex) {
		assert strayVertices.contains(strayVertex);
		assert strayVertex.getEdges().length == 1 || strayVertex.getEdges().length == 2;
		return strayVertex.getEdges().length == 1;
	}
}
