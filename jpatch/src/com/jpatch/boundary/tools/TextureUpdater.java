package com.jpatch.boundary.tools;

import com.jpatch.boundary.*;

import java.awt.*;

import javax.media.opengl.*;

/**
 * This Thread, once started, keeps the screenshotTextures of the viewports up to date.
 * It checks every INTERVAL if the content of the viewport is older than 2 * INTERVAL.
 * If yes, it calles the viewports validateScreenShotTexture() method.
 * 
 * This prevents unnecessary texure-updates when the viewport is redrawn frequently (e.g. during
 * rotating the view), but also ensures that the screenshot is valid as soon as possible - thus
 * preventing the noticable delay when the user first uses a lasso tool after the viewport has
 * been redrawn.
 *
 * @author sascha
 *
 */
class TextureUpdater {
	private final int INTERVAL = 100; // ms
	
	volatile boolean running = true;
	boolean started = false;
	final Viewport[] viewports;
	
	TextureUpdater(Viewport[] viewports) {
		this.viewports = viewports.clone();
	}
	
	Thread thread = new Thread() {
		public void run() {
			try {
				while (running) {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							for (Viewport viewport : viewports) {
								ViewportGl viewportGl = (ViewportGl) viewport;
								if (viewportGl.getContentAge() > 2 * INTERVAL) {
									GLAutoDrawable glDrawable = (GLAutoDrawable) viewportGl.getComponent();
									glDrawable.getContext().makeCurrent();
									if (glDrawable.getContext() == GLContext.getCurrent()) {
										viewportGl.validateScreenShotTexture();
										glDrawable.getContext().release();
									}
								}
							}
						}
					});
					sleep(INTERVAL);
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	};
	
	public void start() {
		if (started) {
			throw new IllegalStateException("Thread can only be started once");
		}
//		thread.start();
		started = true;
	}
	
	public void stop() {
		if (!started) {
			throw new IllegalStateException("Thread hasn't been started yet");
		}
		running = false;
		thread = null;
	}
}