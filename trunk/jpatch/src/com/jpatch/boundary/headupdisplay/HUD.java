package com.jpatch.boundary.headupdisplay;

import static javax.media.opengl.GL.*;

import java.util.*;

import javax.media.opengl.*;

import com.jpatch.boundary.*;

public class HUD implements ViewportOverlay {
	private final List<Slider> widgets = new ArrayList<Slider>();
	
	private Viewport viewport;
	
	public void bindToViewport(Viewport viewport) {
		System.out.println("### bindToViewport(" + viewport + ") called");
		if (this.viewport != null) {
			this.viewport.removeOverlay(this);
		}
		this.viewport = viewport;
		if (viewport != null) {
			viewport.addOverlay(this);
		}
		System.out.println("viewport=" + viewport + " " + this.viewport);
	}
	
	public void addWidget(Slider slider) {
		widgets.add(slider);
		System.out.println("viewport=" + viewport);
		viewport.getComponent().addMouseListener(slider.getMouseAdapter());
		viewport.getComponent().addMouseMotionListener(slider.getMouseAdapter());
	}
	
	public void redraw() {
		viewport.redrawOverlays();
	}

	public void drawOverlay(Viewport viewport) {
		GL gl = viewport.getGL();
		viewport.rasterMode(gl);	
		for (Slider slider : widgets) {
			slider.draw(viewport.getGL());
		}
		viewport.spatialMode(gl);
	}
}
