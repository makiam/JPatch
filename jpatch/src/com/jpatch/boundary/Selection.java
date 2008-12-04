package com.jpatch.boundary;

import java.util.*;

import javax.vecmath.*;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.control.*;
import com.jpatch.afw.vecmath.*;
import com.jpatch.entity.*;
import com.jpatch.entity.sds2.*;

public class Selection {
	public static enum Type { LIMIT, VERTICES, EDGES, FACES, NODE }
	
	private Type type = Type.NODE;
	private final GenericAttr<XFormNode> nodeAttr = new GenericAttr<XFormNode>();
	private final Set<AbstractVertex> vertices = new HashSet<AbstractVertex>();
	private final Collection<AbstractVertex> unmodifyableVertices = Collections.unmodifiableCollection(vertices);
	private final Set<HalfEdge> edges = new HashSet<HalfEdge>();
	private final Collection<HalfEdge> unmodifyableEdges = Collections.unmodifiableCollection(edges);
	private final Set<Face> faces = new HashSet<Face>();
	private final Collection<Face> unmodifyableFaces = Collections.unmodifiableCollection(faces);
	private boolean verticesValid;
	private boolean edgesValid;
	private boolean facesValid;
//	private boolean nodeChanging;
	
	private final Transformable transformable = new Transformable() {
		private Point3d[] startPositions;
		private Point3d[] startTuples;
		private Matrix4d matrix = new Matrix4d();
		
		public void begin() {
			validateVertices();
			int count =vertices.size();
			startPositions = new Point3d[count];
			startTuples = new Point3d[count];
			int i = 0;
			for (AbstractVertex vertex : vertices) {
				startPositions[i] = new Point3d();
				vertex.getPosition(startPositions[i]);
				startTuples[i] = new Point3d();
				vertex.getPosition(startTuples[i]);
				i++;
			}
		}

		public void end(List<JPatchUndoableEdit> editList) {
			int i = 0;
			for (AbstractVertex vertex : vertices) {
//				editList.add(AttributeEdit.changeAttribute(vertex.getPositionAttribute(), startTuples[i], false));
				editList.add(vertex.new ChangePositionEdit(startTuples[i], false));
				i++;
			}
		}

		public void rotate(Point3d pivot, Vector3d axis, double angle) {
			/* set matrix to the rotation matrix specified by axisAngle around specivied pivot */
			AxisAngle4d axisAngle = new AxisAngle4d(axis, angle);
			matrix.set(axisAngle);
			matrix.m03 = pivot.x;
			matrix.m13 = pivot.y;
			matrix.m23 = pivot.z;
			
			matrix.m03 = pivot.x - matrix.m00 * pivot.x - matrix.m01 * pivot.y - matrix.m02 * pivot.z;
			matrix.m13 = pivot.y - matrix.m10 * pivot.x - matrix.m11 * pivot.y - matrix.m12 * pivot.z;
			matrix.m23 = pivot.z - matrix.m20 * pivot.x - matrix.m21 * pivot.y - matrix.m22 * pivot.z;
			transformVertices();
		}

		private void transformVertices() {
			Point3d p = new Point3d();
			int i = 0;
			for (AbstractVertex vertex : vertices) {
				p.set(startPositions[i++]);
				matrix.transform(p);
				vertex.setPosition(p);
//				vertex.setPosition(p);
			}
		}

		public void scale(Scale3d scale) {
			matrix.setIdentity();
			scale.getScaleMatrix(matrix);
			transformVertices();
		}

		public void translate(Vector3d vector) {
			matrix.set(vector);
			transformVertices();
		}
		
		public void getBaseTransform(TransformUtil transformUtil, int space) {
			nodeAttr.getValue().getLocal2WorldTransform(transformUtil, space);
		}
		
		public void getPivot(Point3d pivot) {
			getCenter(pivot, null);
		}
	};
	
	public Selection() {
//		nodeAttr.addAttributePostChangeListener(new AttributePostChangeListener() {
//			public void attributeHasChanged(Attribute source) {
//				assert nodeChanging;
//			}
//		});
	}
	
	public void set(Selection selection) {
		new State(selection).copyTo(this);
	}
	
	public Type getType() {
		return type;
	}
	
	public GenericAttr<XFormNode> getNodeAttribute() {
		return nodeAttr;
	}
	
	public XFormNode getNode() {
		return nodeAttr.getValue();
	}
	
	public void setNode(XFormNode node, List<JPatchUndoableEdit> editList) {
		addChangeSelectionEdit(editList);
//		nodeChanging = true;
		nodeAttr.setValue(node);
//		setType(Type.NODE, null);
//		nodeChanging = false;
	}
	
	public SdsModel getSdsModel() {
		if (nodeAttr.getValue() instanceof SdsModel) {
			return (SdsModel) nodeAttr.getValue();
		}
		return null;
	}
	
	public Collection<? extends AbstractVertex> getVertices() {
		validateVertices();
		return unmodifyableVertices;
	}
	
	public Collection<HalfEdge> getEdges() {
		validateEdges();
		return unmodifyableEdges;
	}
	
	public Collection<Face> getFaces() {
		validateFaces();
		return unmodifyableFaces;
	}
	
	public AbstractVertex getLimit() {
		if (type != Type.LIMIT) {
			throw new IllegalStateException();
		}
		validateVertices();
		return vertices.iterator().next();
	}
	
	public Transformable getTransformable() {
		if (type == Type.NODE) {
			return nodeAttr.getValue();
		}
		return transformable;
	}
	
	public void clear(List<JPatchUndoableEdit> editList) {
		addChangeSelectionEdit(editList);
		invalidate();
		vertices.clear();
		edges.clear();
		faces.clear();
	}
	
	public void addVertex(AbstractVertex vertex, List<JPatchUndoableEdit> editList) {
		addChangeSelectionEdit(editList);
		setType(Type.VERTICES, null);
		vertices.add(vertex);
		invalidate();
	}
	
	public void removeVertex(AbstractVertex vertex, List<JPatchUndoableEdit> editList) {
		addChangeSelectionEdit(editList);
		setType(Type.VERTICES, null);
		vertices.remove(vertex);
		invalidate();
	}
	
	public void addVertices(Collection<AbstractVertex> vertices, List<JPatchUndoableEdit> editList) {
		addChangeSelectionEdit(editList);
		setType(Type.VERTICES, null);
		this.vertices.addAll(vertices);
		invalidate();
	}
	
	public void removeVertices(Collection<AbstractVertex> vertices, List<JPatchUndoableEdit> editList) {
		addChangeSelectionEdit(editList);
		setType(Type.VERTICES, null);
		this.vertices.removeAll(vertices);
		invalidate();
	}
	
	public void setLimit(AbstractVertex vertex, List<JPatchUndoableEdit> editList) {
		addChangeSelectionEdit(editList);
		setType(Type.LIMIT, null);
		clear(null);
		vertices.add(vertex);
		invalidate();
	}
	
	public void setVertex(AbstractVertex vertex, List<JPatchUndoableEdit> editList) {
		addChangeSelectionEdit(editList);
		setType(Type.VERTICES, null);
		clear(null);
		addVertex(vertex, null);
	}
	
	public void addEdge(HalfEdge edge, List<JPatchUndoableEdit> editList) {
		addChangeSelectionEdit(editList);
		setType(Type.EDGES, null);
		edges.add(edge.getPrimary());
		invalidate();
	}
	
	public void removeEdge(HalfEdge edge, List<JPatchUndoableEdit> editList) {
		addChangeSelectionEdit(editList);
		setType(Type.EDGES, null);
		edges.remove(edge.getPrimary());
		invalidate();
	}
	
	public void addEdges(Collection<HalfEdge> edges, List<JPatchUndoableEdit> editList) {
		addChangeSelectionEdit(editList);
		setType(Type.EDGES, null);
		for (HalfEdge edge : edges) {
			this.edges.add(edge.getPrimary());
		}
		invalidate();
	}
	
	public void removeEdges(Collection<HalfEdge> edges, List<JPatchUndoableEdit> editList) {
		addChangeSelectionEdit(editList);
		setType(Type.EDGES, null);
		for (HalfEdge edge : edges) {
			this.edges.remove(edge.getPrimary());
		}
		invalidate();
	}
	
	public void setEdge(HalfEdge edge, List<JPatchUndoableEdit> editList) {
		addChangeSelectionEdit(editList);
		setType(Type.EDGES, null);
		clear(null);
		addEdge(edge, null);
	}
	
	public void addFace(Face face, List<JPatchUndoableEdit> editList) {
		addChangeSelectionEdit(editList);
		setType(Type.FACES, null);
		faces.add(face);
		invalidate();
	}
	
	public void removeFace(Face face, List<JPatchUndoableEdit> editList) {
		addChangeSelectionEdit(editList);
		setType(Type.FACES, null);
		faces.remove(face);
		invalidate();
	}
	
	public void addFaces(Collection<Face> faces, List<JPatchUndoableEdit> editList) {
		addChangeSelectionEdit(editList);
		setType(Type.FACES, null);
		this.faces.addAll(faces);
		invalidate();
	}
	
	public void removeFaces(Collection<Face> faces, List<JPatchUndoableEdit> editList) {
		addChangeSelectionEdit(editList);
		setType(Type.FACES, null);
		this.faces.removeAll(faces);
		invalidate();
	}
	
	public void setFace(Face face, List<JPatchUndoableEdit> editList) {
		addChangeSelectionEdit(editList);
		setType(Type.FACES, null);
		clear(null);
		addFace(face, null);
	}
	
	public void setType(Type type, List<JPatchUndoableEdit> editList) {
		if (type != this.type) {
			addChangeSelectionEdit(editList);
			switch(type) {
			case NODE:
				invalidate();
				break;
			case LIMIT:		//fallthrouh intentional
			case VERTICES:
				invalidate();
				validateVertices();
				break;
			case EDGES:
				invalidate();
				validateEdges();
				break;
			case FACES:
				invalidate();
				validateFaces();
				break;
			default:
				assert false; // should never get here	
			}
			this.type = type;
		}
	}
	
	private void invalidate() {
		verticesValid = false;
		edgesValid = false;
		facesValid = false;
	}
	
	private void validateVertices() {
		if (verticesValid || type == Type.VERTICES || type == Type.LIMIT) {
			return;
		}
		vertices.clear();
		switch (type) {
		case NODE:
			SdsModel sdsModel = getSdsModel();
			if (sdsModel != null) {
				Sds sds = sdsModel.getSds();
				for (AbstractVertex vertex : sds.getVertices(0)) {
					vertices.add(vertex);
				}
			}
			break;
		case EDGES:
			for (HalfEdge edge : edges) {
				vertices.add(edge.getVertex());
				vertices.add(edge.getPairVertex());
			}
			break;
		case FACES:
			for (Face face : faces) {
				for (HalfEdge edge : face.getEdges()) {
					vertices.add(edge.getVertex());
				}
			}
			break;
		default:
			assert false; // should never get here	
		}
		verticesValid = true;
	}
	
	private void validateEdges() {
		if (edgesValid || type == Type.EDGES) {
			return;
		}
		edges.clear();
		switch (type) {
		case NODE:
			SdsModel sdsModel = getSdsModel();
			if (sdsModel != null) {
				Sds sds = sdsModel.getSds();
				for (HalfEdge edge : sds.getEdges(0)) {
					edges.add(edge.getPrimary());
				}
			}
			break;
		case VERTICES:
			for (AbstractVertex vertex : vertices) {
				for (HalfEdge edge : vertex.getEdges()) {
					if (vertices.contains(edge.getPairVertex())) {
						edges.add(edge.getPrimary());
					}
				}
			}
			break;
		case FACES:
			for (Face face : faces) {
				for (HalfEdge edge : face.getEdges()) {
					edges.add(edge.getPrimary());
				}
			}
			break;
		default:
			assert false : "type was " + type; // should never get here	
		}	
		edgesValid = true;
	}
	
	private void validateFaces() {
		if (facesValid || type == Type.FACES) {
			return;
		}
		faces.clear();
		switch (type) {
		case NODE:
			SdsModel sdsModel = getSdsModel();
			if (sdsModel != null) {
				Sds sds = sdsModel.getSds();
				for (Face face : sds.getFaces(0)) {
					faces.add(face);
				}
			}
			break;
		case VERTICES:
			for (AbstractVertex vertex : vertices) {
				for (HalfEdge edge : vertex.getEdges()) {
					Face face = edge.getFace();
					if (face != null) {
						boolean add = true;
						for (HalfEdge faceEdge : face.getEdges()) {
							if (!vertices.contains(faceEdge.getVertex())) {
								add = false;
								break;
							}
						}
						if (add) {
							faces.add(face);
						}
					}
				}
			}
			break;
		case EDGES:
			for (HalfEdge edge : edges) {
				for (int i = 0; i < 2; i++) {
					Face face = (i == 0) ? edge.getFace() : edge.getPairFace();
					if (face != null) {
						boolean add = true;
						for (HalfEdge faceEdge : face.getEdges()) {
							if (!vertices.contains(faceEdge.getVertex())) {
								add = false;
								break;
							}
						}
						if (add) {
							faces.add(face);
						}
					}
				}
			}
			break;
		default:
			assert false; // should never get here	
		}
		facesValid = true;
	}
	
	public int getSize() {
		switch (type) {
		case EDGES:
			return getEdges().size();
		case FACES:
			return getFaces().size();
		case VERTICES:
			return getVertices().size();
		case LIMIT:
			return getVertices().size();
		case NODE:
			return 1;
		default:
			throw new RuntimeException(); // should never get here
		}
	}
	
	public void getBounds(Tuple3d p0, Tuple3d p1, Matrix4d matrix) {
		p0.set(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		p1.set(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		Point3d p = new Point3d();
		validateVertices();
		for (AbstractVertex vertex : vertices) {
			vertex.getPosition(p);
			if (matrix != null) {
				matrix.transform(p);
			}
			if (p.x < p0.x) {
				p0.x = p.x;
			}
			if (p.y < p0.y) {
				p0.y = p.y;
			}
			if (p.z < p0.z) {
				p0.z = p.z;
			}
			if (p.x > p1.x) {
				p1.x = p.x;
			}
			if (p.y > p1.y) {
				p1.y = p.y;
			}
			if (p.z > p1.z) {
				p1.z = p.z;
			}
		}
	}
	
	public void getCenter(Point3d center, Matrix4d matrix) {
		validateVertices();
		if (vertices.size() == 0) {
			return;
		}
		Miniball mb = new Miniball();
		ArrayList<Point3d> points = new ArrayList<Point3d>();
		for (AbstractVertex vertex : vertices) {
			Point3d p = new Point3d();
			vertex.getPosition(p);
			if (matrix != null) {
				matrix.transform(p);
			}
			points.add(p);
		}
		mb.build(points);
		center.set(mb.center());
	}
	
	public static class State {
		private Object selectedEntities;
		private Type type;
		
		public State(Selection selection) {
			type = selection.type;
//			System.out.println("type=" + type);
			switch(type) {
			case NODE:
				selectedEntities = selection.nodeAttr.getValue();
				break;
			case EDGES:
				selection.validateEdges();
				selectedEntities = selection.edges.toArray(new HalfEdge[selection.edges.size()]);
				break;
			case FACES:
				selection.validateFaces();
				selectedEntities = selection.faces.toArray(new Face[selection.faces.size()]);
				break;
			case VERTICES:
				selection.validateVertices();
				selectedEntities = selection.vertices.toArray(new AbstractVertex[selection.vertices.size()]);
				break;
			default:
				assert false; // should never get here	
			}
		}
		
		public void copyTo(Selection selection) {
			selection.type = type;
			
			switch(type) {
			case NODE:
				selection.nodeAttr.setValue((XFormNode) selectedEntities);
				break;
			case EDGES:
				selection.edges.clear();
				selection.edges.addAll(Arrays.asList((HalfEdge[]) selectedEntities));
				selection.invalidate();
				break;
			case FACES:
				selection.faces.clear();
				selection.faces.addAll(Arrays.asList((Face[]) selectedEntities));
				selection.invalidate();
				break;
			case VERTICES:
				selection.vertices.clear();
				selection.vertices.addAll(Arrays.asList((AbstractVertex[]) selectedEntities));
				selection.invalidate();
				break;
			default:
				assert false; // should never get here	
			}
		}
	}
	
	private void addChangeSelectionEdit(List<JPatchUndoableEdit> editList) {
		if (editList != null) {
			editList.add(new ChangeSelectionEdit());
		}
	}
	
	private class ChangeSelectionEdit extends AbstractUndoableEdit {
		private State state;
		
		private ChangeSelectionEdit() {
			state = new State(Selection.this);
			apply(false);
		}
		
		private void swap() {
			State tmpState = new State(Selection.this);
			state.copyTo(Selection.this);
			state = tmpState;
		}
		
		@Override
		public void undo() {
			super.undo();
			swap();
		}
		
		@Override
		public void redo() {
			super.redo();
			swap();
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Selection@").append(System.identityHashCode(this)).append(": ");
		sb.append(nodeAttr.getValue()).append(" ");
		switch (type) {
		case VERTICES:
			sb.append(vertices);
			break;
		case EDGES:
			sb.append(edges);
			break;
		case FACES:
			sb.append(faces);
			break;
		}
		return sb.toString();
	}
}
