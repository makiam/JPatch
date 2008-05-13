package com.jpatch.boundary.tools;

import java.awt.*;
import java.awt.event.*;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.jpatch.boundary.*;
import jpatch.boundary.settings.*;
import trashcan.*;

public class LassoSelect {
	private static final Color XOR_MODE = new Color(Settings.getInstance().colors.background.get().getRGB() ^ Settings.getInstance().colors.selection.get().getRGB());
	private static final Stroke DASHES = new BasicStroke(1.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,0.0f,new float[] { 1.0f, 1.0f }, 0.0f);
	
	public static MouseListener createLassoMouseListener(Viewport viewport, MouseEvent mouseEvent) {
		return new LassoMouseListener(viewport, mouseEvent);
	}
	
	private static class LassoMouseListener extends MouseAdapter {
		private final Viewport viewport;
		private final int button, x, y;
		private final Rectangle rectangle = new Rectangle();
		private final MouseMotionListener mouseMotionListener = new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				GL gl = ((GLAutoDrawable) e.getComponent()).getGL();
				Graphics2D g = (Graphics2D) e.getComponent().getGraphics().create();
				int mx = e.getX();
				int my = e.getY();
				drawRectangle(g, rectangle, gl);
				rectangle.x = mx > x ? x : mx;
				rectangle.y = my > y ? y : my;
				rectangle.width = Math.abs(mx - x);
				rectangle.height = Math.abs(my - y);
				drawRectangle(g, rectangle,gl );
				g.dispose();
			}
		};
		
		private LassoMouseListener(Viewport viewport, MouseEvent mouseEvent) {
			this.viewport = viewport;
			button = mouseEvent.getButton();
			x = mouseEvent.getX();
			y = mouseEvent.getY();
			viewport.getComponent().addMouseMotionListener(mouseMotionListener);
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == button) {
				GL gl = ((GLAutoDrawable) e.getComponent()).getGL();
				viewport.getComponent().removeMouseMotionListener(mouseMotionListener);
				viewport.getComponent().removeMouseListener(this);
				Graphics2D g = (Graphics2D) e.getComponent().getGraphics();
				drawRectangle(g, rectangle, gl);
			}
		}
		
		private void drawRectangle(Graphics2D g, Rectangle r, GL gl) {
			System.out.println("draw rectangle " + r);
//			g.setXORMode(XOR_MODE);
//			g.setStroke(DASHES);
//			g.drawLine(r.x, r.y, r.x + r.width, r.y);
//			g.drawLine(r.x + r.width, r.y + 1, r.x + r.width, r.y + r.height);
//			g.drawLine(r.x + 1, r.y + r.height, r.x + r.width, r.y + r.height);
//			g.drawLine(r.x, r.y + 2, r.x, r.y + r.height);
			gl.glDrawBuffer(GL.GL_FRONT);
			((ViewportGl) viewport).rasterMode();
			gl.glColor3f(1, 1, 0);
			gl.glBegin(GL.GL_LINE_LOOP);
			gl.glVertex2f(r.x, r.y);
			gl.glVertex2f(r.x + r.width, r.y);
			gl.glVertex2f(r.x + r.width, r.y + r.height);
			gl.glVertex2f(r.x, r.y + r.height);
			gl.glEnd();
			gl.glFlush();
		}
	}
}
