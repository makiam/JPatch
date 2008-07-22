package com.jpatch.boundary.tools;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.vecmath.*;
import com.jpatch.boundary.*;
import com.jpatch.boundary.tools.MouseSelector.*;
import com.jpatch.entity.sds2.*;

import java.awt.event.*;
import java.util.*;

import javax.vecmath.*;

public class CornerTool extends AbstractBasicTool {
	
	private MouseMotionListener[] mouseMotionListeners;
	private int mouseX;
	private double startValue;
	
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
				double value = startValue + (e.getX() - mouseX) * 0.1;
				System.out.println(value);
				for (AbstractVertex vertex : hitSelection.getVertices()) {
					vertex.getCornerSharpnessAttribute().setDouble(value);
				}
				Main.getInstance().repaintViewport(viewport);

				break;
			}
		}

		
	};
}
