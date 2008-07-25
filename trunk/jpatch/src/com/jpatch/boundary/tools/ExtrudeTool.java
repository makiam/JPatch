package com.jpatch.boundary.tools;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.vecmath.*;
import com.jpatch.boundary.*;
import com.jpatch.boundary.tools.MouseSelector.*;
import com.jpatch.boundary.tools.TweakTool.*;
import com.jpatch.entity.sds2.*;

import java.awt.event.*;
import java.util.*;

import javax.vecmath.*;

public class ExtrudeTool extends AbstractBasicTool {
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
	private final BooleanAttr keepConnectedAttr = new BooleanAttr(true);
	
	private Normal normal;

	private boolean extrudeDone;
	
	public ExtrudeTool() {
		STANDARD_SELECTION_TYPE = Sds.Type.FACE | Sds.Type.BOUNDARY_EDGE | Sds.Type.STRAY_EDGE;
	}
	
	public StateMachine<ToolMode> getToolModeAttribute() {
		return toolModeAttr;
	}
	
	public BooleanAttr getKeepConnectedAttribute() {
		return keepConnectedAttr;
	}
	
	@Override
	public void registerListeners(Viewport[] viewports) {
		super.registerListeners(viewports);
		mouseMotionListeners = new MouseMotionListener[viewports.length];
		for (int i = 0; i < viewports.length; i++) {
			mouseMotionListeners[i] = new ExtrudeMouseMotionListener(viewports[i]);
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
		extrudeDone = false;
	}
	
	
	
	
	
	private class ExtrudeMouseMotionListener extends MouseMotionAdapter {
		final Viewport viewport;
		Point3d mouse = new Point3d();
		Vector3d vector = new Vector3d();
		
		private ExtrudeMouseMotionListener(Viewport viewport) {
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
					if (!extrudeDone) {
						Operations.extrude(hitSelection, null);
						hitSelection.getTransformable().begin();
						extrudeDone = true;
					}
					mouse.set(e.getX(), e.getY(), localStart.z);
					viewport.projectFromScreen(mouse, mouse);
					vector.sub(mouse, localStart);
					
					hitSelection.getTransformable().translate(vector);
	//				Main.getInstance().syncRepaintViewport(viewport);
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
		private final Map<AbstractVertex, VertexNormal> vertexPos = new HashMap<AbstractVertex, VertexNormal>();
		
		Normal(Viewport viewport, Selection selection, HitObject hitObject) {
			if (hitObject instanceof HitVertex) {
				AbstractVertex vertex = ((HitVertex) hitObject).vertex;
				
				vertex.getPosition(p0);
				
				vertex.getNormal(p1);
				p1.add(p0);
				
				for (AbstractVertex v : selection.getVertices()) {
					vertexPos.put(v, new VertexNormal(v));
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
						VertexNormal vn = vertexPos.get(v);
						if (vn == null) {
							vn = new VertexNormal(v, new Vector3d());
							vertexPos.put(v, vn);
						}
						vn.pNormal.add(n0);
						vn.pNormal.add(n1);
					}
				}
				
				for (AbstractVertex v : vertexPos.keySet()) {
					vertexPos.get(v).pNormal.normalize();
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
						VertexNormal vn = vertexPos.get(v);
						if (vn == null) {
							vn = new VertexNormal(v, new Vector3d());
							vertexPos.put(v, vn);
						}
						vn.pNormal.add(n0);
					}
				}
				
				for (AbstractVertex v : vertexPos.keySet()) {
					vertexPos.get(v).pNormal.normalize();
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
			if (!extrudeDone) {
				System.out.println("Extruding...");
				System.out.println("Selection was " + hitSelection);
				Map<BaseVertex, BaseVertex> newVertices = Operations.extrude(hitSelection, null);
				System.out.println("Selection now is " + hitSelection);
				Set<AbstractVertex> oldVertices = new HashSet<AbstractVertex>(vertexPos.keySet());
				for (AbstractVertex oldVertex : oldVertices) {
					VertexNormal vertexNormal = vertexPos.get(oldVertex);
					vertexPos.remove(oldVertex);
					vertexPos.put(newVertices.get(oldVertex), vertexNormal);
				}
				
				
				for (AbstractVertex v : vertexPos.keySet()) {
					System.out.print(v + ",");
				}
				System.out.println();
				hitSelection.getTransformable().begin();
				extrudeDone = true;
			}
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
			for (AbstractVertex v : vertexPos.keySet()) {
				vertexPos.get(v).setFactor(v, factor);
			}
		}
	}
	
	private static class VertexNormal {
		Point3d pStart = new Point3d();
		Vector3d pNormal = new Vector3d();
		
		VertexNormal(AbstractVertex v) {
			v.getPosition(pStart);
			v.getVertexPoint().getNormal(pNormal);
			pNormal.normalize();
		}
		
		VertexNormal(AbstractVertex v, Vector3d normal) {
			v.getPosition(pStart);
			pNormal.set(normal);
		}
		
		void setFactor(AbstractVertex v, double f) {
			v.setPosition(pStart.x + pNormal.x * f, pStart.y + pNormal.y * f, pStart.z + pNormal.z * f);
		}
	}
}
