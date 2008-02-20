package trashcan;

import static javax.media.opengl.GL.*;
import com.jpatch.afw.attributes.GenericAttr;
import com.jpatch.afw.control.AttributeEdit;
import com.jpatch.afw.control.JPatchUndoableEdit;
import com.jpatch.afw.vecmath.TransformUtil;
import static com.jpatch.afw.vecmath.TransformUtil.*;
import com.jpatch.boundary.*;
import com.jpatch.boundary.tools.*;
import com.jpatch.entity.SdsModel;
import com.jpatch.entity.sds2.*;
import com.sun.opengl.util.*;
import com.sun.opengl.util.texture.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.image.*;
import java.nio.*;
import java.util.*;
import java.util.List;

import javax.media.opengl.*;
import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

import jpatch.boundary.settings.Settings;

public class MoveVertexTool implements VisibleTool {
	public static final GenericAttr<String> EDIT_NAME = new GenericAttr<String>("movevertextool");
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
		
		selection.getNode().getLocal2WorldTransform(transformUtil, LOCAL);
		double[] modelView = transformUtil.getMatrix(TransformUtil.LOCAL, TransformUtil.CAMERA, new double[16]);
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadMatrixd(modelView, 0);
		
		selection.getNode().getLocal2WorldTransform(transformUtil, LOCAL);
//		transformUtil.setTransform(LOCAL, selection.getSelectedSdsModelAttribute().getValue().getTransform());
//		Matrix4f modelView = transformUtil.getMatrix(LOCAL, CAMERA, new Matrix4f());
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
//		modelView.transform(p000);
//		modelView.transform(p001);
//		modelView.transform(p010);
//		modelView.transform(p011);
//		modelView.transform(p100);
//		modelView.transform(p101);
//		modelView.transform(p110);
//		modelView.transform(p111);
		gl.glEnable(GL_BLEND);
		gl.glEnable(GL_LINE_SMOOTH);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDisable(GL_LIGHTING);
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

	private class MoveVertexMouseListener extends MouseAdapter {
		private Viewport viewport;
		private MouseMotionListener mouseMotionListener;
		
		MoveVertexMouseListener(Viewport viewport) {
			this.viewport = viewport;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			SdsModel sdsModel = Main.getInstance().getSelection().getSdsModel();
			if (e.getButton() == MouseEvent.BUTTON1) {
//				Point point = new Point(e.getX(), e.getY());
//				MouseSelector.HitObject hitObject = MouseSelector.getObjectAt(viewport, mouseX, mouseY, maxDistSq, sdsModel, level, type);
////				HalfEdge edge = MouseSelector.getEdgeAt(viewport, e.getX(), e.getY(), Main.getInstance().getActiveSds());
//				if (hit.object != null) {
//					Point hitPoint = new Point(point);
//					SwingUtilities.convertPointToScreen(hitPoint, viewport.getComponent());
//					Main.getInstance().getRobot().mouseMove(hitPoint.x, hitPoint.y);
//					mouseMotionListener = new MoveVertexMouseMotionListener(viewport, point, (SdsModel) hit.node, (AbstractVertex) hit.object);
//					viewport.getComponent().addMouseMotionListener(mouseMotionListener);
////					Main.getInstance().setSelectedObject(vertex);
////				} else if (edge != null) {
////					Main.getInstance().setSelectedObject(edge);
////				} else {
////					Main.getInstance().setSelectedObject(null);
//				} else {
//					SdsModel sdsModel = Main.getInstance().getSelection().getSdsModel();
					mouseMotionListener = new LassoSelectMouseMotionListener(viewport, sdsModel, e.getX(), e.getY());
					viewport.getComponent().addMouseMotionListener(mouseMotionListener);
//				}
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
						
						List<JPatchUndoableEdit> editList = new ArrayList<JPatchUndoableEdit>(1);
						if (LastModifierTool.getInstance().get() != MoveVertexTool.this) {
							editList.add(AttributeEdit.changeAttribute(Main.getInstance().getActions().toolSM, LastModifierTool.getInstance().get(), false));
							LastModifierTool.getInstance().set(MoveVertexTool.this);
						}
						Main.getInstance().getUndoManager().addEdit("change selection", editList);
						
						lassoListener.free();
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
//		private ByteBuffer buffer;
		private int cw, ch;
		private int texture;
		private GLContext glContext;
		
		private LassoSelectMouseMotionListener(Viewport viewport, SdsModel sdsModel, int x, int y) {
			this.viewport = viewport;
			this.sdsModel = sdsModel;
			this.x = x;
			this.y = y;
			
			
			ViewportGl viewportGl = (ViewportGl) viewport;
			GLAutoDrawable glDrawable = (GLAutoDrawable) viewportGl.getComponent();
			glContext = glDrawable.getContext();
			glContext.makeCurrent();
			//GL gl = glContext.getGL();
			viewportGl.validateScreenShotTexture();
			
			
//			cw = glDrawable.getWidth() / 2;
//			ch = glDrawable.getHeight() / 2;
//			
//			cw = 1024;
//			ch = 1024;
			
//			gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);
//			gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
			
//			BufferedImage image = new BufferedImage(cw, ch, BufferedImage.TYPE_3BYTE_BGR);
//			Graphics g = image.createGraphics();
//			g.setColor(Color.WHITE);
//			g.fillRect(10, 10, cw - 20, ch - 20);
//			texture = TextureIO.newTexture(image, false);
			
//			texture.enable();
//			texture.bind();
//			int[] tex = new int[1];
//			gl.glGenTextures(0, tex, 0);
//			texture = tex[0];
//			gl.glEnable(GL_TEXTURE_2D);
//			gl.glBindTexture(GL.GL_TEXTURE_2D, texture);
			
//			ByteBuffer data = BufferUtil.newByteBuffer(cw * ch * 4); 
//	        data.limit(data.capacity());
//	        data.rewind();
//	        Random rnd = new Random();
//	        while(data.remaining() > 0) {
//	        	data.put((byte) rnd.nextInt());
//	        }
//	        data.rewind();
	        
	        // Build Texture Using Information In data
//	        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, cw, ch, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, data);    
//	        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
//	        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
	        
//			gl.glReadBuffer(GL_FRONT);
//			
//			System.out.println("canvas size = " + cw + "x" + ch);
			
//			gl.glCopyTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 0, 0, cw, ch);
//			gl.glCopyTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 0, 0, cw, ch, 0);
//			buffer = BufferUtil.newByteBuffer(cw * ch * 3);
//			buffer = ByteBuffer.allocate(ch * cw * 3);
//			System.out.println(buffer.isDirect());
			
//			long t = System.currentTimeMillis();
//			viewportGl.rasterMode();
//			gl.glReadPixels(0, 1, cw, ch, GL_BGR, GL_UNSIGNED_BYTE, buffer);
//			gl.glReadBuffer(GL_FRONT);
//			gl.glDrawBuffer(GL_RIGHT);
//			gl.glCopyPixels(0, 0, cw, ch, GL_COLOR);
//			System.out.println(System.currentTimeMillis() - t);
//			texture.disable();
			glContext.release();
			drawRectangle(new Rectangle(0, 0, 0, 0), viewport.getComponent());
//			buffer.rewind();
//			for (int i = 0; i < buffer.capacity(); i++) {
//				System.out.print(buffer.get(i) + " ");
//				if (i % (3 * cw) == 0) {
//					System.out.println();
//				}
//			}
//			System.out.println();
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			int mx = e.getX();
			int my = e.getY();
			drawRectangle(rectangle, e.getComponent());
			rectangle.x = mx > x ? x : mx;
			rectangle.y = my > y ? y : my;
			rectangle.width = Math.abs(mx - x);
			rectangle.height = Math.abs(my - y);
			drawRectangle(rectangle, e.getComponent());
		}
		
		private void drawRectangle(Rectangle r, Component c) {
//			if (true) return;
			GLCanvas glCanvas = (GLCanvas) c;
			glCanvas.getContext().makeCurrent();
			GL gl = glCanvas.getGL();
//			gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//			gl.glDrawBuffer(GL.GL_BACK);
			((ViewportGl) viewport).rasterMode();
			gl.glDisable(GL_DEPTH_TEST);
//			gl.glEnable(GL_COLOR_MATRIX);
//			gl.glReadPixels(0, 0, c.getWidth(), c.getHeight(), GL_BGR, GL_UNSIGNED_BYTE, buffer);
//			gl.glRasterPos2i(0, ch - 2);
			
//			for (int i = 0; i < buffer.capacity(); i++) {
//				System.out.print(buffer.get(i) + " ");
//				if (i % (3 * cw) == 0) {
//					System.out.println();
//				}
//			}
//			System.out.println();
				
//			System.out.println("drawing " + cw + "x" + ch + " pixels, size = " + buffer.capacity());
//			gl.glDrawPixels(cw, ch, GL_BGR, GL_UNSIGNED_BYTE, buffer);
//			gl.glReadBuffer(GL_AUX0);
//			gl.glDrawBuffer(GL_BACK);
//			gl.glCopyPixels(0, 1, cw, ch - 1, GL_COLOR);
			
			gl.glDisable(GL_BLEND);
			gl.glDepthMask(false);
//			TextureCoords txCoords = texture.getImageTexCoords();
			
			
//				int ch = this.ch / 10;
//				int cw = this.cw / 10;
				
				
//				gl.glEnable(GL_TEXTURE_2D);
//				gl.glBindTexture(GL_TEXTURE_2D, texture);
//				gl.glColor3f(1, 0, 1);
//				gl.glBegin(GL_QUADS);
//				gl.glTexCoord2f(0, 1);
//				gl.glVertex2i(0, -1);
//				gl.glTexCoord2f(0, 0);
//				gl.glVertex2i(0, ch - 1);
//				gl.glTexCoord2f(1, 0);
//				gl.glVertex2i(cw, ch - 1);
//				gl.glTexCoord2f(1, 1);
//				gl.glVertex2i(cw, -1);
//				gl.glEnd();
//				gl.glDisable(GL_TEXTURE_2D);
//				gl.glMatrixMode(GL_COLOR);
//				gl.glLoadMatrixf(new float[] {
//						0.33f, 0.33f, 0.33f, 0,
//						0.33f, 0.33f, 0.33f, 0,
//						0.33f, 0.33f, 0.33f, 0,
//						0, 0, 0, 1
//				}, 0);
				((ViewportGl) viewport).drawScreenShot(0, 0, c.getWidth(), c.getHeight(), 0.667f);
				
			
//			
			
			((ViewportGl) viewport).drawScreenShot(r.x, r.y, r.x + r.width, r.y + r.height, 1.0f);
//			
//			gl.glVertex2i(0, ch);
			
//			gl.glLogicOp(GL_XOR);
//			gl.glEnable(GL.GL_COLOR_LOGIC_OP);
//			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			
			gl.glColor4f(1, 1, 0, 1.0f);
			
			gl.glBegin(GL.GL_LINE_LOOP);
			gl.glVertex2f(r.x, r.y);
			gl.glVertex2f(r.x, r.y + r.height);
			gl.glVertex2f(r.x + r.width, r.y + r.height);
			gl.glVertex2f(r.x + r.width, r.y);
			gl.glEnd();
			
//			gl.glColor4f(1, 1, 0, 0.1f);
//			gl.glBegin(GL.GL_QUADS);
//			gl.glVertex2f(r.x, r.y);
//			gl.glVertex2f(r.x, r.y + r.height);
//			gl.glVertex2f(r.x + r.width, r.y + r.height);
//			gl.glVertex2f(r.x + r.width, r.y);
//			gl.glEnd();
//			gl.glDisable(GL_BLEND);
			gl.glDepthMask(true);
			gl.glEnable(GL_DEPTH_TEST);
			gl.glFlush();
			glCanvas.swapBuffers();
			
//			gl.glDisable(GL.GL_COLOR_LOGIC_OP);
			
//			gl.glDrawBuffer(GL.GL_BACK);
			
//			gl.glMatrixMode(GL_COLOR);
//			gl.glLoadIdentity();
			
			glCanvas.getContext().release();
//			g.setXORMode(XOR_MODE);
//			g.setStroke(DASHES);
//			g.drawLine(r.x, r.y, r.x + r.width, r.y);
//			g.drawLine(r.x + r.width, r.y + 1, r.x + r.width, r.y + r.height);
//			g.drawLine(r.x + 1, r.y + r.height, r.x + r.width, r.y + r.height);
//			g.drawLine(r.x, r.y + 2, r.x, r.y + r.height);
		}
		
		private void getSelectedVertices(Selection selection) {
			int x0 = rectangle.x;
			int y0 = rectangle.y;
			int x1 = rectangle.x + rectangle.width;
			int y1 = rectangle.y + rectangle.height;
			
			Selection.State state = MouseSelector.getVertices(viewport, x0, y0, x1, y1, sdsModel, sdsModel.getEditLevelAttribute().getInt());
			state.copyTo(selection);
		}
		
		private void free() {
			glContext.makeCurrent();
//			texture.dispose();
			((ViewportGl) viewport).disposeScreenShotTexture();
			
//			glContext.getGL().glDeleteTextures(1, new int[] { texture }, 0);
			glContext.release();
		}
	}
	
	private static class MoveVertexMouseMotionListener extends MouseMotionAdapter {
		private final Viewport viewport;
		private final AbstractVertex vertex;
		private Point point;
		private final Point3d pStart = new Point3d();
		private final Point3d limitStart = new Point3d();
		private final Point3d k = new Point3d();
		private final Point3d p = new Point3d();
		private final Vector3d v = new Vector3d();
		private final SdsModel sdsModel;
		private final TransformUtil transformUtil = new TransformUtil();
		double z;
		boolean useLimit;
//		Point3d pos = new Point3d();
//		Point3d limit = new Point3d();
		
		MoveVertexMouseMotionListener(Viewport viewport, Point point, SdsModel sdsModel, AbstractVertex vertex) {
			this.point = new Point(point);
			this.viewport = viewport;
			this.vertex = vertex;
			this.sdsModel = sdsModel;
			
			useLimit = !viewport.getViewDef().getShowControlMeshAttribute().getBoolean();
			
			viewport.getViewDef().configureTransformUtil(transformUtil);
			sdsModel.getLocal2WorldTransform(transformUtil, LOCAL);
//			transformUtil.setTransform(TransformUtil.LOCAL, sdsModel.getTransform());
			
			vertex.getLimit(limitStart);
			vertex.getPosition(pStart);
			if (useLimit) {
				p.set(limitStart);
				k.set(pStart);
				k.scale(vertex.getLimitFactor());
				k.sub(limitStart, k);
			} else {
				p.set(pStart);
			}
			
			
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
			if (point != null && point.x == e.getX() && point.y == e.getY()) {
				point = null;
				return;
			}
//			System.out.println(point + " " + e.getX() + "," + e.getY());
			p.x = e.getX();
			p.y = e.getY();
			p.z = z;
//			System.out.println("Pscreen =" + p);
//			System.out.print("screen=" + p + " local=");
			transformUtil.projectFromScreen(TransformUtil.LOCAL, p, p);
			
			
			if (useLimit) {
				p.sub(k);
				p.scale(1.0 / vertex.getLimitFactor());
			} 
				
//			System.out.println(p);
//			System.out.println("Pworld  =" + p);
//			p.sub(limit);
//			double n = vertex.valence();
//			p.scale((n + 5) / n);
//			p.add(pos);
			vertex.setPosition(p);
//			sdsModel.getSds().computeLevel2Vertices();
			Main.getInstance().syncRepaintViewport(viewport);
		}
	}
}
