package com.jpatch.boundary.tools;

import static javax.media.opengl.GL.*;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.media.opengl.GL;
import javax.swing.SwingUtilities;
import javax.vecmath.*;

import com.jpatch.afw.attributes.GenericAttr;
import com.jpatch.afw.attributes.StateMachine;
import com.jpatch.afw.attributes.Tuple3Attr;
import com.jpatch.afw.control.JPatchUndoableEdit;
import com.jpatch.afw.vecmath.*;
import com.jpatch.boundary.*;

import com.jpatch.entity.GlMaterial;

public class TranslateTool implements VisibleTool {
	public static final GenericAttr<String> EDIT_NAME = new GenericAttr<String>("translate");
	private static float ARROW_WIDTH = 0.05f;
	private static float ARROW_LENGTH = 0.1f;
	private static float ARROW_START = 0.05f;
	
	private static final float[][] COLORS = new float[][] {
			{ 1.0f, 0.0f, 0.0f, 1.0f },	// x axis
			{ 0.0f, 0.9f, 0.0f, 1.0f },	// y axis
			{ 0.4f, 0.4f, 1.0f, 1.0f },	// z axis
			{ 0.0f, 0.0f, 0.0f, 1.0f },	// outline
			{ 0.9f, 0.9f, 0.0f, 1.0f },	// yellow axis
			{ 0.6f, 0.6f, 0.6f, 0.7f }	// ghost
	};
	private static final GlMaterial[] MATERIALS = new GlMaterial[] {
		new GlMaterial(new Color3f(1.0f, 0.0f, 0.0f), 0.25f, 0.75f, 10),
		new GlMaterial(new Color3f(0.0f, 0.9f, 0.0f), 0.25f, 0.75f, 10),
		new GlMaterial(new Color3f(0.4f, 0.4f, 1.0f), 0.25f, 0.75f, 10),
		new GlMaterial(new Color4f(0.5f, 0.5f, 0.5f, 0.5f), new Color4f(), new Color4f(), new Color4f(0.5f, 0.5f, 0.5f, 0.5f), 0),
		new GlMaterial(new Color3f(1.0f, 1.0f, 0.0f), 0.25f, 0.75f, 10),
	};
	
	private static final Matrix4d[] matrices = new Matrix4d[] {
			new Matrix4d(
					 1,  0,  0,  0,
					 0,  1,  0,  0,
					 0,  0,  1,  0,
					 0,  0,  0,  1
			),
			new Matrix4d(
					-1,  0,  0,  0,
					 0,  1,  0,  0,
					 0,  0, -1,  0,
					 0,  0,  0,  1
			),
			new Matrix4d(
					 0, -1,  0,  0,
					 1,  0,  0,  0,
					 0,  0,  1,  0,
					 0,  0,  0,  1
			),
			new Matrix4d(
					 0,  1,  0,  0,
					-1,  0,  0,  0,
					 0,  0,  1,  0,
					 0,  0,  0,  1
			),
			new Matrix4d(
					 0,  0, -1,  0,
					 0,  1,  0,  0,
					 1,  0,  0,  0,
					 0,  0,  0,  1
			),
			new Matrix4d(
					 0,  0,  1,  0,
					 0,  1,  0,  0,
					-1,  0,  0,  0,
					 0,  0,  0,  1
			),
	};
	
	private final Matrix4d transformMatrix = new Matrix4d();
	
	private final Tuple3Attr pivotAttr = new Tuple3Attr();
	private final Tuple3Attr axisRotationAttr = new Tuple3Attr();
	private final Tuple3Attr vectorAttr = new Tuple3Attr();
	private final Point3d pivot = new Point3d();
	private final Vector3d vector = new Vector3d(2, 1, 0);
	private final Rotation3d axisRotation = new Rotation3d();
	private final StateMachine<Integer> modeAttr = new StateMachine<Integer>(new Integer[] { 3, 6 }, 6);
	private final static Shape arrow = new Shape(
			new Point3f[] {
					new Point3f(1, 0, 0),
					new Point3f(1 - ARROW_LENGTH, ARROW_WIDTH, ARROW_WIDTH),
					new Point3f(1 - ARROW_LENGTH, ARROW_WIDTH, -ARROW_WIDTH),
					new Point3f(1 - ARROW_LENGTH, -ARROW_WIDTH, -ARROW_WIDTH),
					new Point3f(1 - ARROW_LENGTH, -ARROW_WIDTH, ARROW_WIDTH)
			},
			new int[] { 0, 2, 1, 0, 3, 2, 0, 4, 3, 0, 1, 4, 1, 2, 3, 3, 4, 1 }
	);
	private final static Shape cube = new Shape(
			new Point3f[] {
					new Point3f(-ARROW_START, -ARROW_START, ARROW_START),
					new Point3f(-ARROW_START, -ARROW_START, -ARROW_START),
					new Point3f( ARROW_START, -ARROW_START, -ARROW_START),
					new Point3f( ARROW_START, -ARROW_START, ARROW_START),
					new Point3f(-ARROW_START, ARROW_START, ARROW_START),
					new Point3f(-ARROW_START, ARROW_START, -ARROW_START),
					new Point3f( ARROW_START, ARROW_START, -ARROW_START),
					new Point3f( ARROW_START, ARROW_START, ARROW_START),
			},
			new int[] { 0, 1, 2, 3, 0, 2,
						0, 7, 4, 3, 7, 0,
						1, 4, 5, 0, 4, 1,
						1, 5, 6, 2, 1, 6,
						3, 6, 7, 2, 6, 3,
						4, 6, 5, 7, 6, 4
			}
	);
	
	private MouseListener[] mouseListeners;
	private MouseMotionListener mouseMotionListener;
	
	public TranslateTool() {
		axisRotationAttr.bindTuple(axisRotation);
		pivotAttr.bindTuple(pivot);
		vectorAttr.bindTuple(vector);
	}
	
	public Tuple3Attr getAxisRotationAttribute() {
		return axisRotationAttr;
	}

	public Tuple3Attr getPivotAttribute() {
		return pivotAttr;
	}
	
	public Tuple3Attr getVectorAttribute() {
		return vectorAttr;
	}
	
	public StateMachine<Integer> getModeAttribute() {
		return modeAttr;
	}
	
	
	public void registerListeners(Viewport[] viewports) {
		Selection selection = Main.getInstance().getSelection();
		selection.getCenter(pivot);
		pivotAttr.setTuple(pivot);
		if (mouseListeners != null) {
			throw new IllegalStateException("already registered");
		}
		mouseListeners = new MouseListener[viewports.length];
		for (int i = 0; i < viewports.length; i++) {
			mouseListeners[i] = new HitMouseListener(viewports[i]);
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
		TransformUtilOld transformUtil = viewport.getViewDef().getTransformUtil();
		double radius;
		/*
		 * set the radius so that the tool will occupy about 1/3rd of the screen
		 */
		if (!transformUtil.isPerspective()) {
			Dimension size = viewport.getComponent().getSize();
			radius = Math.min(size.width, size.height) / transformUtil.getCameraScale() * 0.2;
		} else {
			/* set cameraPivot to camera-space pivot and compute vector from pivot to camera*/
			Point3d cameraPivot = new Point3d();
			transformUtil.world2Camera(pivot, cameraPivot);
			radius = cameraPivot.z / transformUtil.getRelativeFocalLength() * -0.2;
		}
		
		Matrix4d matrix = new Matrix4d();
		axisRotation.getRotationMatrix(matrix);
		matrix.mul(radius);
		
		/* add pivot translation */
		matrix.m03 = pivot.x;
		matrix.m13 = pivot.y;
		matrix.m23 = pivot.z;
		
		matrix.m33 = 1;
		
		transformUtil.setLocal2World(matrix);
		
		Point3d cameraPivot = new Point3d();
		transformUtil.world2Camera(pivot, cameraPivot);
		
		final Point3f[] axisPoints = new Point3f[matrices.length];
		Integer[] order = new Integer[matrices.length];
		for (int i = 0; i < matrices.length; i++) {
			axisPoints[i] = new Point3f(1, 0, 0);
			matrices[i].transform(axisPoints[i]);
			transformUtil.local2Camera(axisPoints[i], axisPoints[i]);
			order[i] = i;
		};
		
		Arrays.sort(order, new Comparator<Integer>() {
			public int compare(Integer arg0, Integer arg1) {
				float z0 = axisPoints[arg0].z;
				float z1 = axisPoints[arg1].z;
				return z0 < z1 ? -1 : z0 > z1 ? 1 : 0;
			}
		});
		
		
		/* initialize GL for rendering */
		GL gl = ((ViewportGl) viewport).getGl();
		gl.glEnable(GL_BLEND);
		gl.glEnable(GL_LINE_SMOOTH);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		gl.glDisable(GL_LIGHTING);
		gl.glDisable(GL_COLOR_MATERIAL);
//		gl.glDisable(GL_CULL_FACE);
		gl.glDepthFunc(GL_ALWAYS);
		
//		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		Point3f p = new Point3f();
		Matrix4d modelView = transformUtil.getModelViewMatrix(new Matrix4d());
		
		Matrix4d m = new Matrix4d();
		/* draw x/y/z lines */
		
		int start = mouseMotionListener == null ? 1 : 0;
		for (int ghost = start; ghost < 2; ghost++) {
			if (ghost == 1) {
				Vector3d v = new Vector3d(vector);
				axisRotation.getRotationMatrix(new Matrix3d()).transform(v);
				axisRotation.getRotationMatrix(matrix);
				matrix.mul(radius);
				
				/* add pivot translation */
				matrix.m03 = pivot.x + v.x;
				matrix.m13 = pivot.y + v.y;
				matrix.m23 = pivot.z + v.z;
				
				matrix.m33 = 1;
			
				transformUtil.setLocal2World(matrix);
				transformUtil.getModelViewMatrix(modelView);
			}
			
			for (int i = 0; i < 6; i++) {
				int axis = order[i];
				if (modeAttr.getValue() == 6 || axis % 2 == 0) {
				m.set(modelView);
				m.mul(matrices[axis]);
					for (int j = 0; j < 2; j++) {
						if ((j == 0 && axisPoints[axis].z > cameraPivot.z) || (j == 1 && axisPoints[axis].z <= cameraPivot.z)) {
							gl.glEnable(GL_LINE_SMOOTH);
							for (int pass = 0; pass < 2; pass++) {
								if (pass != 1 && ghost == 0) {
									continue;
								}
								if (pass == 0) {
									gl.glLineWidth(3.5f);
								} else {
									gl.glLineWidth(2.5f);
								}
								
								if (pass == 1) {
									if (ghost == 0) {
										gl.glColor4fv(COLORS[5], 0);
									} else {
										gl.glColor3fv(COLORS[axis / 2], 0);
									}
								}
								gl.glBegin(GL_LINES);
								p.set(ARROW_START, 0, 0);
								m.transform(p);
								gl.glVertex3f(p.x, p.y, p.z);
								p.set(1 - ARROW_LENGTH, 0, 0);
								m.transform(p);
								gl.glVertex3f(p.x, p.y, p.z);
								gl.glEnd();
							}
						}
						if (j == 0) {
							m.set(modelView);
							m.mul(matrices[axis]);
							if (ghost == 1) {
								gl.glLineWidth(2f);
								gl.glColor3fv(COLORS[3], 0);
								arrow.drawOutline(gl, m);
								gl.glEnable(GL_LIGHTING);
								int mat = (ghost == 0) ? 3 : axis / 2;
								MATERIALS[mat].applyMaterial(gl, GL_FRONT);
								arrow.draw(gl, m);
								gl.glDisable(GL_LIGHTING);
							} else {
								gl.glColor4fv(COLORS[5], 0);
								arrow.draw(gl, m);
							}
						}
					}
				}
				if (i == 2) {
					if (ghost == 1) {
						gl.glLineWidth(2f);
						gl.glColor3fv(COLORS[3], 0);
						cube.drawOutline(gl, modelView);
						gl.glEnable(GL_LIGHTING);
						int mat = (ghost == 0) ? 3 : 4;
						MATERIALS[mat].applyMaterial(gl, GL_FRONT);
						cube.draw(gl, modelView);
						gl.glDisable(GL_LIGHTING);
					} else {
						gl.glColor4fv(COLORS[5], 0);
						cube.draw(gl, modelView);
					}
					
					
				}
			}
		}
		
		
		
		
		
//		gl.glColorMaterial(GL_FRONT, GL_DIFFUSE);
//		gl.glColor3fv(COLORS[0], 0);
		
//		gl.glLineWidth(1);
//		gl.glColor3fv(COLORS[3], 0);
//		gl.glPolygonMode(GL_FRONT, GL_LINE);
//		arrow.draw(gl, modelView);
		
//		for (int i = 0; i < 3; i++) {
//			int axis = order[i];
//			gl.glColor3fv(COLORS[axis], 0);
//			gl.glBegin(GL_TRIANGLE_FAN);
//			gl.glVertex3f(p[axis + 1].x, p[axis + 1].y, p[axis + 1].z);
//			if (axis == 0) {
//				gl.glVertex3f(p[axis + 1].x * 0.9f, p[axis + 1].x * );
		
		/* cleanup gl */
		gl.glLineWidth(1);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glDisable(GL_BLEND);
		gl.glDisable(GL_LINE_SMOOTH);
		gl.glDisable(GL_POLYGON_SMOOTH);
		gl.glEnable(GL_LIGHTING);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_CULL_FACE);
		
	}

	private class HitMouseMotionListener extends MouseMotionAdapter {
		final int constraint;
		final Viewport viewport;
		final TransformUtilOld transformUtil;
		final Point3d pScreen = new Point3d();
		final Point3d pLocal = new Point3d();
		HitMouseMotionListener(Viewport viewport, int constraint, double z) {
			this.viewport = viewport;
			transformUtil = viewport.getViewDef().getTransformUtil();
			this.constraint = constraint;
			pScreen.z = z;
			System.out.println("screenZ = " + z);
			transformMatrix.setIdentity();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub
			Matrix4d matrix = new Matrix4d();
			axisRotation.getRotationMatrix(matrix);
			
			/* add pivot translation */
			matrix.m03 = pivot.x;
			matrix.m13 = pivot.y;
			matrix.m23 = pivot.z;
			
			matrix.m33 = 1;
			
			transformUtil.setLocal2World(matrix);
			
			if (constraint < 0) {
				pScreen.x = e.getX();
				pScreen.y = e.getY();
				transformUtil.screen2Local(pScreen, pLocal);
				vector.set(pLocal);
			} else {
				Point3d p0 = new Point3d();
				transformUtil.local2Screen(p0, p0);
				Point3d p1 = new Point3d(1 - ARROW_LENGTH * 0.75, 0, 0);
				matrices[constraint].transform(p1);
				transformUtil.local2Screen(p1, p1);
				double t = Utils3d.closestPointOnLine(p0.x, p0.y, p1.x, p1.y, pScreen.x, pScreen.y);
				System.out.println(t);
				pScreen.interpolate(p0, p1, t);
				transformUtil.screen2Local(pScreen, pLocal);
				vector.set(pLocal);
			}
			transformUtil.local2World(vector, vector);
			transformMatrix.setTranslation(vector);
			Selection selection = Main.getInstance().getSelection();
			selection.transform(transformMatrix);
			Main.getInstance().syncRepaintViewport(viewport);
		}
	}
	
	private class HitMouseListener extends MouseAdapter {
		Viewport viewport;
		TransformUtilOld transformUtil;
		
		HitMouseListener(Viewport viewport) {
			this.viewport = viewport;
			transformUtil = viewport.getViewDef().getTransformUtil();
		}
		@Override
		public void mousePressed(MouseEvent e) {
			double minDistSq = 100;
			Point3d p = new Point3d(0, 0, 0);
			Point hitPoint = new Point();
			double hitZ = 0;
			transformUtil.local2Screen(p, p);
			int hit = -2;
			if (distSq(e.getX(), e.getY(), p) < minDistSq) {
				hit = -1;
				hitPoint.setLocation((int) Math.round(p.x), (int) Math.round(p.y));
				hitZ = p.z;
			} else {
				for (int i = 0; i < matrices.length; i++) {
					p.set(1 - ARROW_LENGTH * 0.75, 0, 0);
					matrices[i].transform(p);
					transformUtil.local2Screen(p, p);
					double distSq = distSq(e.getX(), e.getY(), p);
					if (distSq < minDistSq) {
						minDistSq = distSq;
						hit = i;
						hitPoint.setLocation((int) Math.round(p.x), (int) Math.round(p.y));
						hitZ = p.z;
					}
				}
			}
			if (hit > -2) {
				System.out.println(hit);
				SwingUtilities.convertPointToScreen(hitPoint, viewport.getComponent());
				Main.getInstance().getRobot().mouseMove(hitPoint.x, hitPoint.y);
				mouseMotionListener = new HitMouseMotionListener(viewport, hit, hitZ);
				viewport.getComponent().addMouseMotionListener(mouseMotionListener);
				Selection selection = Main.getInstance().getSelection();
				selection.begin();
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if (mouseMotionListener != null) {
				viewport.getComponent().removeMouseMotionListener(mouseMotionListener);
				mouseMotionListener = null;
				Selection selection = Main.getInstance().getSelection();
				List<JPatchUndoableEdit> editList = new ArrayList<JPatchUndoableEdit>(selection.getVertexCount());
				selection.end(editList);
				Main.getInstance().getUndoManager().addEdit(EDIT_NAME, editList);
				Main.getInstance().syncViewports(viewport);
				
				Matrix4d matrix = new Matrix4d();
				axisRotation.getRotationMatrix(matrix);
				matrix.transform(vector);
				pivot.add(vector);
				vector.set(0, 0, 0);
				pivotAttr.setTuple(pivot);
				vectorAttr.setTuple(vector);
				
				Main.getInstance().repaintViewports();
			}
		}
		
		private double distSq(int x, int y, Point3d screenPoint) {
			double dx = screenPoint.x - x;
			double dy = screenPoint.y - y;
			return dx * dx + dy * dy;
		}
		
	}
}
