package com.jpatch.boundary.tools;

import static com.jpatch.afw.vecmath.TransformUtil.*;
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
import com.jpatch.afw.control.AttributeEdit;
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
	private final Point3d cameraPivot = new Point3d();
	
	private final Vector3d vector = new Vector3d(2, 1, 0);
	private final Rotation3d axisRotation = new Rotation3d();
	private final StateMachine<Integer> modeAttr = new StateMachine<Integer>(new Integer[] { 3, 6 }, 3);
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
	
	private TransformUtil transformUtil = new TransformUtil("axis_rotation");
	private static final int AXIS_ROTATION = 3;
	
	private double radius;
	
	private MouseListener[] mouseListeners;
	private MouseMotionListener mouseMotionListener;
	
	private boolean toolStateChange;
	
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
		selection.getCenter(pivot, null);
		pivotAttr.setTuple(pivot);
		toolStateChange = true;
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
	
	private void configureFor(Viewport viewport) {
		viewport.getViewDef().computeMatrix();
		viewport.getViewDef().configureTransformUtil(transformUtil);
		Main.getInstance().getSelection().configureTransformUtil(transformUtil);
		
		/* set the transformUtil's local->world matrix's scale to 1.0 */
		transformUtil.setScale(LOCAL, WORLD, 1.0);
		
		transformUtil.transform(LOCAL, pivot, CAMERA, cameraPivot);
		/*
		 * set the radius so that the tool will occupy about 1/3rd of the screen
		 */
		radius = transformUtil.computeNiceRadius(-cameraPivot.z, viewport.getComponent().getWidth(), viewport.getComponent().getHeight());
		
		Matrix4d matrix = new Matrix4d();
		
		/* compute rotation matrix */
		axisRotation.getRotationMatrix(matrix);
		matrix.mul(radius);
		
		/* add pivot translation */
		matrix.m03 = pivot.x;
		matrix.m13 = pivot.y;
		matrix.m23 = pivot.z;
		matrix.m33 = 1;
		
		/* set local2world to rotate-tool matrix */ 
		transformUtil.setSpace2World(AXIS_ROTATION, LOCAL, matrix);
	}
	
	public void draw(Viewport viewport) {
		configureFor(viewport);
		
		final Point3f[] axisPoints = new Point3f[matrices.length];
		Integer[] order = new Integer[matrices.length];
		for (int i = 0; i < matrices.length; i++) {
			axisPoints[i] = new Point3f(1, 0, 0);
			matrices[i].transform(axisPoints[i]);
			transformUtil.transform(AXIS_ROTATION, axisPoints[i], CAMERA,axisPoints[i]);
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
		Matrix4d modelView = transformUtil.getMatrix(AXIS_ROTATION, CAMERA, new Matrix4d());
		
		Matrix4d m = new Matrix4d();
		/* draw x/y/z lines */
		
		int start = mouseMotionListener == null ? 1 : 0;
		for (int ghost = start; ghost < 2; ghost++) {
			if (ghost == 1) {
				Vector3d v = new Vector3d(vector);
				axisRotation.getRotationMatrix(new Matrix3d()).transform(v);
				axisRotation.getRotationMatrix(m);
				m.mul(radius);
				
				/* add pivot translation */
				m.m03 = pivot.x + v.x;
				m.m13 = pivot.y + v.y;
				m.m23 = pivot.z + v.z;
				
				m.m33 = 1;
			
				transformUtil.setSpace2World(AXIS_ROTATION, LOCAL, m);
				transformUtil.getMatrix(AXIS_ROTATION, CAMERA, modelView);
//				transformUtil.getModelViewMatrix(modelView);
			}
			
			
			for (int i = 0; i < 6; i++) {
				int axis = order[i];
				gl.glColor4fv(COLORS[3], 0);
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
		final Point3d pScreen = new Point3d();
		final Point3d pLocal = new Point3d();
		HitMouseMotionListener(Viewport viewport, int constraint, double z) {
			this.viewport = viewport;
			this.constraint = constraint;
			pScreen.z = z;
			System.out.println("screenZ = " + z);
			transformMatrix.setIdentity();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			configureFor(viewport);
			pScreen.x = e.getX();
			pScreen.y = e.getY();
			if (constraint < 0) {
				transformUtil.projectFromScreen(AXIS_ROTATION, pScreen, pLocal);
				vector.set(pLocal);
			} else {
				Point3d p0s = new Point3d();
				transformUtil.projectToScreen(AXIS_ROTATION, p0s, p0s);
				Point3d p1 = new Point3d(1 - ARROW_LENGTH * 0.75, 0, 0);
				matrices[constraint].transform(p1);
				Point3d p1s = new Point3d();
				transformUtil.projectToScreen(AXIS_ROTATION, p1, p1s);
				double t = Utils3d.closestPointOnLine(p0s.x, p0s.y, p1s.x, p1s.y, pScreen.x, pScreen.y);
//				System.out.println(p0s.x + "," + p0s.y + "-" + p1s.x + "," + p1s.y + " " + t);
				pScreen.interpolate(p0s, p1s, t);
				transformUtil.projectFromScreen(AXIS_ROTATION, pScreen, pLocal);
				pLocal.sub(p1);
				vector.set(pLocal);
			}
			vector.scale(radius);
			Vector3d vector2 = new Vector3d(vector);
			axisRotation.getRotationMatrix(new Matrix3d()).transform(vector2);
//			System.out.println(vector);
//			transformUtil.transform(AXIS_ROTATION, vector, vector);
			transformMatrix.setTranslation(vector2);
			Selection selection = Main.getInstance().getSelection();
			selection.transform(transformMatrix);
			Main.getInstance().syncRepaintViewport(viewport);
		}
	}
	
	private class HitMouseListener extends MouseAdapter {
		Viewport viewport;
		
		HitMouseListener(Viewport viewport) {
			this.viewport = viewport;
		}
		@Override
		public void mousePressed(MouseEvent e) {
			configureFor(viewport);
			
			double minDistSq = 100;
			Point3d p = new Point3d(0, 0, 0);
			Point hitPoint = new Point();
			double hitZ = 0;
			transformUtil.projectToScreen(AXIS_ROTATION, p, p);
			int hit = -2;
			if (distSq(e.getX(), e.getY(), p) < minDistSq) {
				hit = -1;
				hitPoint.setLocation((int) Math.round(p.x), (int) Math.round(p.y));
				hitZ = p.z;
			} else {
				for (int i = 0; i < matrices.length; i++) {
					p.set(1 - ARROW_LENGTH * 0.75, 0, 0);
					matrices[i].transform(p);
					transformUtil.projectToScreen(AXIS_ROTATION, p, p);
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
				List<JPatchUndoableEdit> editList = new ArrayList<JPatchUndoableEdit>(selection.getVertexCount() + 3);
				if (toolStateChange) { // FIXME this doesn't work
					StateMachine toolStateMachine = Main.getInstance().getActions().toolSM;
					editList.add(AttributeEdit.changeAttribute(toolStateMachine, toolStateMachine.getPreviousState(), false));
					toolStateChange = false;
				}
				
				selection.end(editList);
				
				
				
				Matrix4d matrix = new Matrix4d();
				axisRotation.getRotationMatrix(matrix);
				matrix.transform(vector);
				pivot.add(vector);
				vector.set(0, 0, 0);
//				pivotAttr.setTuple(pivot);
//				vectorAttr.setTuple(vector);
				
				editList.add(AttributeEdit.changeAttribute(pivotAttr, pivot, true));
				editList.add(AttributeEdit.changeAttribute(vectorAttr, vector, true));
				Main.getInstance().getUndoManager().addEdit(EDIT_NAME, editList);
				Main.getInstance().repaintViewports();	// need to repaint all viewports to make the ghost disappear,
														// therefore no syncRepaintViewports()
			}
		}
		
		private double distSq(int x, int y, Point3d screenPoint) {
			double dx = screenPoint.x - x;
			double dy = screenPoint.y - y;
			return dx * dx + dy * dy;
		}
		
	}
}
