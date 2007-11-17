package com.jpatch.boundary.tools;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_LINES;
import static javax.media.opengl.GL.GL_LINE_SMOOTH;
import static javax.media.opengl.GL.GL_ONE_MINUS_SRC_ALPHA;
import static javax.media.opengl.GL.GL_SRC_ALPHA;
import com.jpatch.afw.vecmath.TransformUtil;
import static com.jpatch.afw.vecmath.TransformUtil.*;
import com.jpatch.boundary.*;
import com.jpatch.entity.SdsModel;
import com.jpatch.entity.sds.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import javax.media.opengl.GL;
import javax.media.opengl.GLCanvas;
import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import jpatch.boundary.settings.Settings;

public class MoveVertexTool implements VisibleTool {
	private static final Color XOR_MODE = new Color(Settings.getInstance().colors.background.get().getRGB() ^ Settings.getInstance().colors.selection.get().getRGB());
	private static final Stroke DASHES = new BasicStroke(1.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,0.0f,new float[] { 1.0f, 1.0f }, 0.0f);
	
	
	private MouseListener[] mouseListeners;
	
	private final TransformUtil transformUtil = new TransformUtil();
	
	public void registerListeners(Viewport[] viewports) {
		System.out.println("MoveVertexTool registerListeners");
		if (mouseListeners != null) {
			throw new IllegalStateException("already registered");
		}
		mouseListeners = new MouseListener[viewports.length];
		for (int i = 0; i < viewports.length; i++) {
			mouseListeners[i] = new MoveVertexMouseListener(viewports[i]);
			viewports[i].getComponent().addMouseListener(mouseListeners[i]);
		}
	}

	public void unregisterListeners(Viewport[] viewports) {
		System.out.println("MoveVertexTool unregisterListeners");
		for (int i = 0; i < viewports.length; i++) {
			viewports[i].getComponent().removeMouseListener(mouseListeners[i]);
		}
		mouseListeners = null;
	}

	public void draw(Viewport viewport) {
		GL gl = ((ViewportGl) viewport).getGl();
		viewport.getViewDef().configureTransformUtil(transformUtil);
		Selection selection = Main.getInstance().getSelection();
		if (selection == null) {
			return;
		}
		transformUtil.setTransform(LOCAL, selection.getSelectedSdsModelAttribute().getValue().getTransform());
		Matrix4f modelView = transformUtil.getMatrix(LOCAL, CAMERA, new Matrix4f());
		Point3d p0 = new Point3d();
		Point3d p1 = new Point3d();
		selection.getBounds(p0, p1, null);
		double sc = p0.distance(p1) * 0.02;
		p0.x -= sc;
		p0.y -= sc;
		p0.z -= sc;
		p1.x += sc;
		p1.y += sc;
		p1.z += sc;
		
		Point3f p000 = new Point3f(p0);
		Point3f p111 = new Point3f(p1);
		Point3f p001 = new Point3f(p000.x, p000.y, p111.z);
		Point3f p010 = new Point3f(p000.x, p111.y, p000.z);
		Point3f p011 = new Point3f(p000.x, p111.y, p111.z);
		Point3f p100 = new Point3f(p111.x, p000.y, p000.z);
		Point3f p101 = new Point3f(p111.x, p000.y, p111.z);
		Point3f p110 = new Point3f(p111.x, p111.y, p000.z);
		modelView.transform(p000);
		modelView.transform(p001);
		modelView.transform(p010);
		modelView.transform(p011);
		modelView.transform(p100);
		modelView.transform(p101);
		modelView.transform(p110);
		modelView.transform(p111);
		gl.glEnable(GL_BLEND);
		gl.glEnable(GL_LINE_SMOOTH);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		gl.glDisable(GL_DEPTH_TEST);
		gl.glColor4f(1, 1, 0, 0.25f);
		gl.glBegin(GL_LINES);
		gl.glVertex3f(p000.x, p000.y, p000.z); gl.glVertex3f(p001.x, p001.y, p001.z);
		gl.glVertex3f(p001.x, p001.y, p001.z); gl.glVertex3f(p011.x, p011.y, p011.z);
		gl.glVertex3f(p011.x, p011.y, p011.z); gl.glVertex3f(p010.x, p010.y, p010.z);
		gl.glVertex3f(p010.x, p010.y, p010.z); gl.glVertex3f(p000.x, p000.y, p000.z);
		gl.glVertex3f(p100.x, p100.y, p100.z); gl.glVertex3f(p101.x, p101.y, p101.z);
		gl.glVertex3f(p101.x, p101.y, p101.z); gl.glVertex3f(p111.x, p111.y, p111.z);
		gl.glVertex3f(p111.x, p111.y, p111.z); gl.glVertex3f(p110.x, p110.y, p110.z);
		gl.glVertex3f(p110.x, p110.y, p110.z); gl.glVertex3f(p100.x, p100.y, p100.z);
		gl.glVertex3f(p000.x, p000.y, p000.z); gl.glVertex3f(p100.x, p100.y, p100.z);
		gl.glVertex3f(p001.x, p001.y, p001.z); gl.glVertex3f(p101.x, p101.y, p101.z);
		gl.glVertex3f(p010.x, p010.y, p010.z); gl.glVertex3f(p110.x, p110.y, p110.z);
		gl.glVertex3f(p011.x, p011.y, p011.z); gl.glVertex3f(p111.x, p111.y, p111.z);
		gl.glEnd();
		gl.glColor4f(1, 1, 0, 1.0f);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glBegin(GL_LINES);
		gl.glVertex3f(p000.x, p000.y, p000.z); gl.glVertex3f(p001.x, p001.y, p001.z);
		gl.glVertex3f(p001.x, p001.y, p001.z); gl.glVertex3f(p011.x, p011.y, p011.z);
		gl.glVertex3f(p011.x, p011.y, p011.z); gl.glVertex3f(p010.x, p010.y, p010.z);
		gl.glVertex3f(p010.x, p010.y, p010.z); gl.glVertex3f(p000.x, p000.y, p000.z);
		gl.glVertex3f(p100.x, p100.y, p100.z); gl.glVertex3f(p101.x, p101.y, p101.z);
		gl.glVertex3f(p101.x, p101.y, p101.z); gl.glVertex3f(p111.x, p111.y, p111.z);
		gl.glVertex3f(p111.x, p111.y, p111.z); gl.glVertex3f(p110.x, p110.y, p110.z);
		gl.glVertex3f(p110.x, p110.y, p110.z); gl.glVertex3f(p100.x, p100.y, p100.z);
		gl.glVertex3f(p000.x, p000.y, p000.z); gl.glVertex3f(p100.x, p100.y, p100.z);
		gl.glVertex3f(p001.x, p001.y, p001.z); gl.glVertex3f(p101.x, p101.y, p101.z);
		gl.glVertex3f(p010.x, p010.y, p010.z); gl.glVertex3f(p110.x, p110.y, p110.z);
		gl.glVertex3f(p011.x, p011.y, p011.z); gl.glVertex3f(p111.x, p111.y, p111.z);
		gl.glEnd();
		gl.glDisable(GL_BLEND);
		gl.glDisable(GL_LINE_SMOOTH);
	}

	private static class MoveVertexMouseListener extends MouseAdapter {
		private Viewport viewport;
		private MouseMotionListener mouseMotionListener;
		
		MoveVertexMouseListener(Viewport viewport) {
			this.viewport = viewport;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				MouseSelector.Hit hit = MouseSelector.getVertexAt(viewport, e.getX(), e.getY());
//				HalfEdge edge = MouseSelector.getEdgeAt(viewport, e.getX(), e.getY(), Main.getInstance().getActiveSds());
				if (hit.object != null) {
					mouseMotionListener = new MoveVertexMouseMotionListener(viewport, (SdsModel) hit.node, (TopLevelVertex) hit.object);
					viewport.getComponent().addMouseMotionListener(mouseMotionListener);
//					Main.getInstance().setSelectedObject(vertex);
//				} else if (edge != null) {
//					Main.getInstance().setSelectedObject(edge);
//				} else {
//					Main.getInstance().setSelectedObject(null);
				} else {
					SdsModel sdsModel = Main.getInstance().getSelection().getSelectedSdsModelAttribute().getValue();
					mouseMotionListener = new LassoSelectMouseMotionListener(viewport, sdsModel, e.getX(), e.getY());
					viewport.getComponent().addMouseMotionListener(mouseMotionListener);
				}
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (mouseMotionListener != null) {
					viewport.getComponent().removeMouseMotionListener(mouseMotionListener);
					if (mouseMotionListener instanceof LassoSelectMouseMotionListener) {
						LassoSelectMouseMotionListener lassoListener = (LassoSelectMouseMotionListener) mouseMotionListener;
						lassoListener.getSelectedVertices(Main.getInstance().getSelection());
						Main.getInstance().repaintViewports();
					} else {
						Main.getInstance().syncViewports(viewport);
					}
				}
			}
		}
	}
	
	private static class LassoSelectMouseMotionListener extends MouseMotionAdapter {
		private final Rectangle rectangle = new Rectangle();
		private final Viewport viewport;
		private final SdsModel sdsModel;
		private int x, y;
		
		private LassoSelectMouseMotionListener(Viewport viewport, SdsModel sdsModel, int x, int y) {
			this.viewport = viewport;
			this.sdsModel = sdsModel;
			this.x = x;
			this.y = y;
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			Graphics2D g = (Graphics2D) e.getComponent().getGraphics();
			int mx = e.getX();
			int my = e.getY();
			drawRectangle(g, rectangle);
			rectangle.x = mx > x ? x : mx;
			rectangle.y = my > y ? y : my;
			rectangle.width = Math.abs(mx - x);
			rectangle.height = Math.abs(my - y);
			drawRectangle(g, rectangle);
		}
		
		private void drawRectangle(Graphics2D g, Rectangle r) {
			g.setXORMode(XOR_MODE);
			g.setStroke(DASHES);
			g.drawLine(r.x, r.y, r.x + r.width, r.y);
			g.drawLine(r.x + r.width, r.y + 1, r.x + r.width, r.y + r.height);
			g.drawLine(r.x + 1, r.y + r.height, r.x + r.width, r.y + r.height);
			g.drawLine(r.x, r.y + 2, r.x, r.y + r.height);
		}
		
		private void getSelectedVertices(Selection selection) {
			int x0 = rectangle.x;
			int y0 = rectangle.y;
			int x1 = rectangle.x + rectangle.width;
			int y1 = rectangle.y + rectangle.height;
			MouseSelector.getVertices(viewport, x0, y0, x1, y1, sdsModel, selection);
		}
	}
	
	private static class MoveVertexMouseMotionListener extends MouseMotionAdapter {
		private final Viewport viewport;
		private final TopLevelVertex vertex;
		private final Point3d p = new Point3d();
		private final SdsModel sdsModel;
		private final TransformUtil transformUtil = new TransformUtil();
		double z;
//		Point3d pos = new Point3d();
//		Point3d limit = new Point3d();
		
		MoveVertexMouseMotionListener(Viewport viewport, SdsModel sdsModel, TopLevelVertex vertex) {
			this.viewport = viewport;
			this.vertex = vertex;
			this.sdsModel = sdsModel;
			
			viewport.getViewDef().configureTransformUtil(transformUtil);
			transformUtil.setTransform(TransformUtil.LOCAL, sdsModel.getTransform());
			
			vertex.getPos(p);
//			System.out.print("local=" + p + " screen=");
			transformUtil.projectToScreen(transformUtil.LOCAL, p, p);
//			System.out.println(p);
//			Point3d p2 = new Point3d();
//			transformUtil.projectToScreen(LOCAL, p, p2);
//			System.out.println("local=" + p2);
//			System.out.println(transformUtil);
			z = p.z;
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			p.x = e.getX();
			p.y = e.getY();
			p.z = z;
//			System.out.println("Pscreen =" + p);
//			System.out.print("screen=" + p + " local=");
			transformUtil.projectFromScreen(transformUtil.LOCAL, p, p);
//			System.out.println(p);
//			System.out.println("Pworld  =" + p);
//			p.sub(limit);
//			double n = vertex.valence();
//			p.scale((n + 5) / n);
//			p.add(pos);
			vertex.getPosition().setTuple(p);
//			sdsModel.getSds().computeLevel2Vertices();
			Main.getInstance().syncRepaintViewport(viewport);
		}
	}
}
