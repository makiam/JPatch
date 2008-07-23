package com.jpatch.boundary.tools;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.vecmath.*;
import com.jpatch.boundary.*;
import com.jpatch.boundary.tools.AbstractBasicTool.*;
import com.jpatch.boundary.tools.MouseSelector.*;
import com.jpatch.entity.sds2.*;

import java.awt.event.*;
import java.util.*;

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
