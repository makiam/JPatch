package com.jpatch.boundary.tools;


import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import jpatch.boundary.Main;
import jpatch.boundary.Viewport;
import jpatch.entity.ControlPoint;

public class TranslateTool implements JPatchTool{
	private MouseListener[] mouseListeners;
	//private TriangleMesh cone = TriangleMesh.createCone();
	
	public void registerListeners(Viewport[] viewports) {
		mouseListeners = new MouseListener[viewports.length];
		for (int i = 0; i < viewports.length; i++) {
			mouseListeners[i] = new TranslateMouseListener(viewports[i]);
			viewports[i].getComponent().addMouseListener(mouseListeners[i]);
		}
	}

	public void unregisterListeners(Viewport[] viewports) {
		for (int i = 0; i < viewports.length; i++) {
			viewports[i].getComponent().removeMouseListener(mouseListeners[i]);
		}
		mouseListeners = null;
	}

	public void draw(Viewport viewport) {
//		viewport.drawShape(cone);
	}

	private static class TranslateMouseListener extends MouseAdapter {
		private Viewport viewport;
		
		TranslateMouseListener(Viewport viewport) {
			this.viewport = viewport;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				ControlPoint hitCp = viewport.getControlPointAt(e.getX(), e.getY(), Main.getInstance().getActiveModel(), null);
				if (hitCp == null) {
					viewport.getComponent().addMouseListener(LassoSelect.createLassoMouseListener(viewport, e));
				}
			}
		}
	}
}
