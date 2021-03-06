package com.jpatch.boundary.tools;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.vecmath.*;
import com.jpatch.boundary.*;
import com.jpatch.boundary.tools.MouseSelector.*;
import com.jpatch.entity.sds2.*;

import java.awt.event.*;
import java.util.*;

import javax.vecmath.*;

public class TweakTool extends AbstractBasicTool {
	private enum ToolMode {
		FREE ("unconstrained"),
		NORMAL ("along surface normal");
		
		private final String displayString;
	
		private ToolMode(String displayString) {
			this.displayString = displayString;
		}
		
		public String toString() {
			return displayString;
		}
		
	}
	
	private MouseMotionListener[] mouseMotionListeners;
	
	private final StateMachine<ToolMode> toolModeAttr = new StateMachine<ToolMode>(ToolMode.class, ToolMode.FREE);
	
	private Normal normal;

	
	
	public StateMachine<ToolMode> getToolModeAttribute() {
		return toolModeAttr;
	}
	
	@Override
	public void registerListeners(Viewport[] viewports) {
		super.registerListeners(viewports);
		mouseMotionListeners = new MouseMotionListener[viewports.length];
		for (int i = 0; i < viewports.length; i++) {
			mouseMotionListeners[i] = new TweakMouseMotionListener(viewports[i]);
			viewports[i].getComponent().addMouseMotionListener(mouseMotionListeners[i]);
			viewports[i].addOverlay(this);
		}
	}

	@Override
	public void unregisterListeners(Viewport[] viewports) {
		super.unregisterListeners(viewports);
		for (int i = 0; i < viewports.length; i++) {
			viewports[i].getComponent().removeMouseMotionListener(mouseMotionListeners[i]);
			viewports[i].removeOverlay(this);
		}
	}
	

	
	
	@Override
	protected void setActionMode(Viewport viewport, HitObject hitObject, MouseEvent e) {
		super.setActionMode(viewport, hitObject, e);
		if (toolModeAttr.getValue() == ToolMode.NORMAL) {
			normal = new Normal(viewport, hitSelection, hitObject);
		}
	}
	
	
	
	
	
	private class TweakMouseMotionListener extends MouseMotionAdapter {
		final Viewport viewport;
		Point3d mouse = new Point3d();
		Vector3d vector = new Vector3d();
		
		private TweakMouseMotionListener(Viewport viewport) {
			this.viewport = viewport;
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			switch (mode) {
			case ACTION:
				if (hitPoint != null && hitPoint.x == e.getX() && hitPoint.y == e.getY()) {
					hitPoint = null;
					return;
				}
				if (toolModeAttr.getValue() == ToolMode.NORMAL) {
					normal.mouseDragged(viewport, e.getX(), e.getY());
				} else {
					mouse.set(e.getX(), e.getY(), localStart.z);
					viewport.projectFromScreen(mouse, mouse);
					vector.sub(mouse, localStart);
					if (hitSelection.getType() == Selection.Type.LIMIT) {
						vector.scale(1.0 / hitSelection.getLimit().getLimitFactor());
					}
					hitSelection.getTransformable().translate(vector);
	//				Main.getInstance().syncRepaintViewport(viewport);
				}
				if (hitEdge != null) {
					HitEdge otherEdge = (HitEdge) MouseSelector.getObjectAt(viewport, e.getX(), e.getY(), 100, hitSelection.getSdsModel(), 0, EnumSet.of(Sds.Type.BOUNDARY_EDGE), hitEdgeFilter);
					if (otherEdge != null) {
						HalfEdge source = hitEdge;
						HalfEdge target = otherEdge.halfEdge;
						Collection<HalfEdge> selectedEdges = hitSelection.getEdges();
						System.out.println("selectedEdges=" + selectedEdges);
						Operations.canWeldEdges(source, selectedEdges, target);
					}
				}
				
				
				
				
				Main.getInstance().repaintViewport(viewport);

				break;
			}
		}

		
	};
	
	private class Normal {
		private final Point3d p0 = new Point3d();
		private final Point3d p1 = new Point3d();
		private final Point3d p0s = new Point3d();
		private final Point3d p1s = new Point3d();
		private final Point3d p = new Point3d();
		private final double delta;
		private final int axis;
		private final Map<AbstractVertex, ConstraintVertexTranslation> vertexPos = new HashMap<AbstractVertex, ConstraintVertexTranslation>();
		private AbstractVertex limit;
		
		Normal(Viewport viewport, Selection selection, HitObject hitObject) {
			if (hitObject instanceof HitVertex) {
				AbstractVertex vertex = ((HitVertex) hitObject).vertex;
				if (hitObject instanceof HitLimit) {
					vertex.getLimit(p0);
					limit = vertex;
				} else {
					vertex.getPosition(p0);
				}
				vertex.getNormal(p1);
				p1.add(p0);
				
				Vector3d normal = new Vector3d();
				for (AbstractVertex v : selection.getVertices()) {
					v.getNormal(normal);
					normal.normalize();
					vertexPos.put(v, new ConstraintVertexTranslation(v, normal));
				}
			} else if (hitObject instanceof HitEdge) {
				HalfEdge edge = ((HitEdge) hitObject).halfEdge;
				edge.getVertex().getPosition(p0);
				edge.getPairVertex().getPosition(p1);
				p0.interpolate(p0, p1, ((HitEdge) hitObject).position);
				
				Vector3d n0 = new Vector3d();
				Vector3d n1 = new Vector3d();
				edge.getVertex().getNormal(n0);
				edge.getPairVertex().getNormal(n1);
				n0.interpolate(n0, n1, ((HitEdge) hitObject).position);
				n0.normalize();
				p1.add(p0, n0);
				
				AbstractVertex[] edgeVertices = new AbstractVertex[2];
				for (HalfEdge e : selection.getEdges()) {
					e.getVertices(edgeVertices);
					e.getVertex().getNormal(n0);
					e.getPairVertex().getNormal(n1);
					for (AbstractVertex v : edgeVertices) {
						ConstraintVertexTranslation vn = vertexPos.get(v);
						if (vn == null) {
							vn = new ConstraintVertexTranslation(v, new Vector3d());
							vertexPos.put(v, vn);
						}
						vn.getVector().add(n0);
						vn.getVector().add(n1);
					}
				}
				
				for (AbstractVertex v : vertexPos.keySet()) {
					vertexPos.get(v).getVector().normalize();
				}
				
			} else if (hitObject instanceof HitFace) {
				((HitFace) hitObject).face.getMidpointPosition(p0);
				((HitFace) hitObject).face.getMidpointNormal(p1);
				p1.add(p0);
				
				Vector3d n0 = new Vector3d();
				for (Face f : selection.getFaces()) {
					f.getMidpointNormal(n0);
					for (HalfEdge e : f.getEdges()) {
						AbstractVertex v = e.getVertex();
						ConstraintVertexTranslation vn = vertexPos.get(v);
						if (vn == null) {
							vn = new ConstraintVertexTranslation(v, new Vector3d());
							vertexPos.put(v, vn);
						}
						vn.getVector().add(n0);
					}
				}
				
				for (AbstractVertex v : vertexPos.keySet()) {
					vertexPos.get(v).getVector().normalize();
				}
			}
			
			viewport.projectToScreen(p0, p0s);
			viewport.projectToScreen(p1, p1s);
			
			double dx = Math.abs(p0.x - p1.x);
			double dy = Math.abs(p0.y - p1.y);
			double dz = Math.abs(p0.z - p1.z);
			
			if (dx > dy) {
				if (dx > dz) {
					axis = 0; // x
					delta = p1.x - p0.x;
				} else {
					axis = 2; // z
					delta = p1.z - p0.z;
				}
			} else {
				if (dy > dz) {
					axis = 1; // y
					delta = p1.y - p0.y;
				} else {
					axis = 2; // z
					delta = p1.z - p0.z;
				}
			}
			
			snapPointer(viewport, p0s);
		}
		
		public void mouseDragged(Viewport viewport, int mx, int my) {
			p.interpolate(p0s, p1s, Utils3d.closestPointOnLine(p0s.x, p0s.y, p1s.x, p1s.y, mx, my));
			viewport.projectFromScreen(p, p);
			double factor = 0;
			switch (axis) {
			case 0:	// x
				factor = (p.x - p0.x) / delta;
				break;
			case 1:	// y
				factor = (p.y - p0.y) / delta;
				break;
			case 2:	// z
				factor = (p.z - p0.z) / delta;
				break;
			}
			if (limit != null) {
				factor /= limit.getLimitFactor();
			}
			for (AbstractVertex v : vertexPos.keySet()) {
				vertexPos.get(v).moveTo(factor);
			}
		}
	}
}
