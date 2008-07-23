package com.jpatch.boundary.tools;

import static javax.media.opengl.GL.*;
import com.jpatch.afw.attributes.*;
import com.jpatch.afw.vecmath.*;
import com.jpatch.boundary.*;
import com.jpatch.boundary.tools.AbstractBasicTool.*;
import com.jpatch.boundary.tools.MouseSelector.*;
import com.jpatch.entity.*;
import com.jpatch.entity.sds2.*;

import java.awt.event.*;
import java.util.*;

import javax.media.opengl.*;
import javax.vecmath.*;

public class CornerTool extends AbstractBasicTool {
	
	private MouseMotionListener[] mouseMotionListeners;
	private int mouseX;
	private AbstractVertex[] vertices;
	private double[] cornerSharpnesValues;
	
	public CornerTool() {
		STANDARD_SELECTION_TYPE = Sds.Type.VERTEX | Sds.Type.STRAY_VERTEX;
	}
	
	@Override
	public void registerListeners(Viewport[] viewports) {
		super.registerListeners(viewports);
		mouseMotionListeners = new MouseMotionListener[viewports.length];
		for (int i = 0; i < viewports.length; i++) {
			mouseMotionListeners[i] = new CornerMouseMotionListener(viewports[i]);
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
	protected void setActionMode(Viewport viewport, HitObject hitObject, MouseEvent event) {
		super.setActionMode(viewport, hitObject, event);
		mouseX = (int) Math.round(hitObject.screenPosition.x);
		vertices = new AbstractVertex[hitSelection.getVertices().size()];
		cornerSharpnesValues = new double[hitSelection.getVertices().size()];
		int i = 0;
		for (AbstractVertex vertex : hitSelection.getVertices()) {
			vertices[i] = vertex;
			cornerSharpnesValues[i] = vertex.getCornerSharpnessAttribute().getDouble();
			i++;
		}
	}
	
	public void drawOverlay(Viewport viewport) {
		super.drawOverlay(viewport);
		GL gl = viewport.getGL();
		viewport.resetModelviewMatrix(gl);
		gl.glDisable(GL_DEPTH_TEST);
		drawCorners(gl);
		gl.glEnable(GL_DEPTH_TEST);
	}
	
	private void drawCorners(GL gl) {
		final Point3f p0 = new Point3f();
		final Point3f p1 = new Point3f();
		final SdsModel sdsModel = hitSelection.getSdsModel();
		if (sdsModel == null) {
			System.err.println("sdsModel=null!");
			return;
		}
		final int level = sdsModel.getEditLevelAttribute().getInt();
		gl.glColor4f(1, 0, 0, 0.25f);
		gl.glLineWidth(3.0f);
		gl.glBegin(GL_LINES);
		for (AbstractVertex vertex : sdsModel.getSds().getVertices(level, false)) {
			if (vertex.getCornerSharpness() > 0) {
				vertex.getPosition(p0);
				for (HalfEdge edge : vertex.getEdges()) {
					AbstractVertex pair = edge.getPairVertex();
					pair.getPosition(p1);
					p1.interpolate(p0, 0.75f);
					gl.glVertex3f(p0.x, p0.y, p0.z);
					gl.glVertex3f(p1.x, p1.y, p1.z);
				}
			}
		}
		gl.glEnd();
	}
	
	private class CornerMouseMotionListener extends MouseMotionAdapter {
		final Viewport viewport;
		
		private CornerMouseMotionListener(Viewport viewport) {
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
				double delta = e.getX() - mouseX;
				System.out.println("delta = " + delta);
				for (int i = 0; i < vertices.length; i++) {
					DoubleAttr attr = vertices[i].getCornerSharpnessAttribute();
					attr.setDouble(cornerSharpnesValues[i] + delta);
				}
				Main.getInstance().repaintViewport(viewport);

				break;
			}
		}		
	};
	
	
}
