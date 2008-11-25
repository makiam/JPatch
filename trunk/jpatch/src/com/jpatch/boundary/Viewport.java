package com.jpatch.boundary;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.ui.AttributeManager;
import com.jpatch.afw.vecmath.*;
import com.jpatch.boundary.tools.*;
import com.jpatch.boundary.tools.Shape;
import com.jpatch.entity.*;
import com.jpatch.entity.sds2.*;
import com.jpatch.settings.*;
import com.sun.corba.se.spi.legacy.connection.*;
import com.sun.opengl.util.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.*;
import java.util.*;

import javax.media.opengl.*;
import javax.swing.*;
import javax.vecmath.*;

import static com.jpatch.afw.vecmath.TransformUtil.*;
import static javax.media.opengl.GL.*;

public class Viewport implements NamedObject {
	public static final double MIN_DIST_SQ = 64;
	
	private static final GlMaterial BONE_MATERIAL = new GlMaterial(new Color3f(), 0.25f, 0.0f, 10);
	private final static float BONE_LENGTH = 1.0f;
	private final static float BONE_WIDTH = 0.1f;
	private static final Shape BONE = new Shape(
			new Point3f[] {
					new Point3f(0, 0, 0),
					new Point3f(BONE_WIDTH, 0, BONE_WIDTH),
					new Point3f(0, BONE_WIDTH, BONE_WIDTH),
					new Point3f(-BONE_WIDTH, 0, BONE_WIDTH),
					new Point3f(0,-BONE_WIDTH, BONE_WIDTH),
					new Point3f(0, 0, BONE_LENGTH),
			},
			new int[] { 0, 2, 1, 0, 3, 2, 0, 4, 3, 0, 1, 4, 5, 1, 2, 5, 2, 3, 5, 3, 4, 5, 4, 1 }
	);
	private static final BasicMaterial BACK_MATERIAL = new BasicMaterial(new Color4f(), new Color4f(), new Color4f(), new Color4f(1, 0, 0, 1), 0);
	private static final BasicMaterial LINE_MATERIAL = new BasicMaterial(new Color4f(0.5f, 0.5f, 0.5f, 1.0f), new Color4f(0.5f, 0.5f, 0.5f, 1.0f), new Color4f(1, 1, 1, 1), new Color4f(), 100);
	private static final Color4f LINE_COLOR = new Color4f(1, 1, 1, 1);
	
	private final GenericAttr<String> nameAttr = new GenericAttr<String>();
	private final BooleanAttr showControlMeshAttr = new BooleanAttr(true);
	private final BooleanAttr showLimitSurfaceAttr = new BooleanAttr(true);
	private final BooleanAttr showProjectedMeshAttr = new BooleanAttr(false);
	private final BooleanAttr showNodeNamesAttr = new BooleanAttr(true);
	private final StateMachine<ViewDirection> viewDirectionAttr;
	
	private final int id;
	private final GLAutoDrawable drawable;
	private final Component component;
	
	static final int maxSubdiv = 10;
	public static final float nearClip = 1;
	public static final float farClip = 1 << 15;
	static final RealtimeRendererSettings RENDERER_SETTINGS = Settings.getInstance().realtimeRenderer;
	
	private ViewDef viewDef;
	private final Collection<ViewportOverlay> overlays = new ArrayList<ViewportOverlay>();
	private OverlayStrategy overlayStrategy = new TextureOverlayStrategy();
	
	private FloatBuffer depthBuffer;
	private boolean depthBufferValid;
	private boolean depthBufferFrozen;
	
	private double[] modelView = new double[16];
	private TransformUtil transformUtil = new TransformUtil();
	private int fontOffset;
	
	private long time;
	private int repaintCount;
	
	private static final FloatBuffer limitSurfaceBuffer = BufferUtil.newFloatBuffer(SdsConstants.MAX_VALENCE * 2 * 3);
	
	public Viewport(int id, ViewDirection direction, CollectionAttr<ViewDirection> orthoDirections, final JPatchInspector inspector) {
		drawable = new GLCanvas();
		drawable.addGLEventListener(new GLEventListener() {
			boolean init;
			public void init(GLAutoDrawable drawable) {
				drawable.getContext().makeCurrent();
				drawable.setGL(new DebugGL(drawable.getGL()));
				GL gl = drawable.getGL();

				/* Generate display lists to render font characters. */
				fontOffset = GlFont.generateFontLists(gl);
				
				/* setup lighting */
				setLighting(gl, RealtimeLighting.createThreepointLight());	
				gl.glLightModelf(GL_LIGHT_MODEL_TWO_SIDE, 1);
//				gl.glLightModelf(GL_LIGHT_MODEL_COLOR_CONTROL, GL_SEPARATE_SPECULAR_COLOR);
				
				gl.glEnable(GL_POLYGON_OFFSET_FILL);
				gl.glPolygonOffset(1.0f, 1.0f);
				gl.glDisable(GL_DEPTH_TEST);
				gl.glDisable(GL_NORMALIZE);
				gl.glDisable(GL_CULL_FACE);

				setMaterial(gl, GL_BACK, BACK_MATERIAL.getGlMaterial());
//				reshape(drawable, 0, 0, component.getWidth(), component.getHeight());
				init = true;
			}

			public void display(GLAutoDrawable drawable) {
				if (!init) {
					System.err.println("DRAWABLE NOT INITIALIZED!!!"); // should not happen??? TODO
					init(drawable);	
				}
				redrawOverlays();
			}

			public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
				System.out.println("reshape");
				depthBuffer = BufferUtil.newFloatBuffer(width * height);
				redrawViewport();
			}

			public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
				;
			}
		});
		drawable.setAutoSwapBufferMode(false);
		
		component = (Component) drawable;
		
		this.id = id;
		nameAttr.setValue("Viewport " + id);
		viewDirectionAttr = new StateMachine<ViewDirection>(orthoDirections, direction);
		viewDirectionAttr.getValue().bindViewport(this);
		
		/* this will unbind the old ViewDirection when ViewType changes */
		viewDirectionAttr.addAttributePreChangeListener(new AttributePreChangeAdapter<ViewDirection>() {
			@Override
			public ViewDirection attributeWillChange(ScalarAttribute source, ViewDirection value) {
				viewDirectionAttr.getValue().unbindViewport(Viewport.this);
				return value;
			}
		});
		
		/* this will bind the new ViewDirection when ViewType changes */
		viewDirectionAttr.addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				viewDirectionAttr.getValue().bindViewport(Viewport.this);
				inspector.setViewport(Viewport.this);
			}
		});
		
		showLimitSurfaceAttr.addAttributePostChangeListener(new AttributePostChangeListener() {
			private boolean showProj;
			public void attributeHasChanged(Attribute source) {
				if (!showLimitSurfaceAttr.getBoolean()) {
					showProj = showProjectedMeshAttr.getBoolean();
					showProjectedMeshAttr.setBoolean(false);
					AttributeManager.getInstance().lock(showProjectedMeshAttr);
				} else {
					AttributeManager.getInstance().unlock(showProjectedMeshAttr);
					showProjectedMeshAttr.setBoolean(showProj);
				}
			}
		});
		
		
		
		final JPopupMenu popup = new JPopupMenu();
		JMenuItem subdivideMenuItem = new JMenuItem("subdivide");
		popup.add(subdivideMenuItem);
		
		subdivideMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				Selection selection = Main.getInstance().getSelection();
				int level = selection.getSdsModel().getEditLevelAttribute().getInt();
				if (selection.getType() == Selection.Type.FACES) {
					for (Face face : selection.getFaces()) {
						selection.getSdsModel().getSds().subdivideFace(level, face, true);
					}
				}
				System.out.println(Main.getInstance().getSelection());
				
			}
			
		});
		component.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				popup(e);
			}

			public void mousePressed(MouseEvent e) {
				popup(e);
			}

			public void mouseReleased(MouseEvent e) {
				popup(e);
			}
			
			private void popup(MouseEvent e) {
				System.out.println(e);
				if (e.isPopupTrigger()) {
					popup.show((Component) component, e.getX(), e.getY());
				}
			}
		});
	}

	public GenericAttr<String> getNameAttribute() {
		return nameAttr;
	}
	
	public BooleanAttr getShowControlMeshAttribute() {
		return showControlMeshAttr;
	}

	public BooleanAttr getShowLimitSurfaceAttribute() {
		return showLimitSurfaceAttr;
	}

	public BooleanAttr getShowProjectedMeshAttribute() {
		return showProjectedMeshAttr;
	}
	
	public BooleanAttr getShowNodeNamesAttribute() {
		return showNodeNamesAttr;
	}
	
	public StateMachine<ViewDirection> getViewDirectionAttribute() {
		return viewDirectionAttr;
	}

	public String getName() {
		return "Viewport " + id;
	}

	public String getInfo() {
		return "Viewport " + id + ": " + viewDirectionAttr.getValue().toString();
	}
	
	
	public Component getComponent() {
		return component;
	}
	
	public GL getGL() {
		return drawable.getGL();
	}

	@Override
	public String toString() {
		return getName();
	}

	public ViewDef getViewDef() {
		return viewDef;
	}

	public void setViewDef(ViewDef viewDef) {
		this.viewDef = viewDef;
	}
	
	public void projectToScreen(Point3d fromPoint, Point3d toPoint) {
		transformUtil.projectToScreen(TransformUtil.WORLD, fromPoint, toPoint);
	}
	
	public void projectFromScreen(Point3d fromPoint, Point3d toPoint) {
		transformUtil.projectFromScreen(TransformUtil.WORLD, fromPoint, toPoint);
	}
	
	public float getDepthAt(int x, int y) {
		validateDepthBuffer();
		int h = drawable.getHeight();
		int w = drawable.getWidth();
		if (x < 0 || x >= w || y < 0 || y >= h) {
			return -farClip;
		}
		
		int index = (h - y - 1) * w + x;
		float depth = farClip - 2 * Viewport.farClip * depthBuffer.get(index) - 1;

		return depth;
	}
	
	public static void setMaterial(GL gl, int side, GlMaterial glMaterial) {
		float[] array = glMaterial.getArray();
		gl.glMaterialfv(side, GL_AMBIENT, array, GlMaterial.AMBIENT);
		gl.glMaterialfv(side, GL_DIFFUSE, array, GlMaterial.DIFFUSE);
		gl.glMaterialfv(side, GL_SPECULAR, array, GlMaterial.SPECULAR);
		gl.glMaterialfv(side, GL_EMISSION, array, GlMaterial.EMISSION);
		gl.glMaterialfv(side, GL_SHININESS, array, GlMaterial.SHININESS);
	}
	
	static float[] zero = new float[4];
	public static void setDiffuseMaterial(GL gl, int side, GlMaterial glMaterial) {
		float[] array = glMaterial.getArray();
		gl.glMaterialfv(side, GL_AMBIENT, array, GlMaterial.AMBIENT);
		gl.glMaterialfv(side, GL_DIFFUSE, array, GlMaterial.DIFFUSE);
		gl.glMaterialfv(side, GL_SPECULAR, zero, 0);
		gl.glMaterialfv(side, GL_EMISSION, array, GlMaterial.EMISSION);
		gl.glMaterialfv(side, GL_SHININESS, array, GlMaterial.SHININESS);
	}
	
	public static int getIntVar(GL gl, int var) {
		int[] tmp = new int[1];
		gl.glGetIntegerv(var, tmp, 0);
		return tmp[0];
	}
	
	public static void setLighting(GL gl, RealtimeLighting lighting) {
		Color3f ambient = lighting.getAmbientColor();
		gl.glLightModelfv(GL_LIGHT_MODEL_AMBIENT, new float[] { ambient.x, ambient.y, ambient.z, 1 }, 0);
		final int maxLights = getIntVar(gl, GL_MAX_LIGHTS);
		for (int i = 0; i < maxLights; i++) {
			if (i < lighting.numLights()) {
				gl.glEnable(GL_LIGHT0 + i);
				RealtimeLighting.LightSource light = lighting.getLight(i);
				if (light instanceof RealtimeLighting.DirectionalLight) {
					RealtimeLighting.DirectionalLight directionalLight = (RealtimeLighting.DirectionalLight) light;
					Color3f color = directionalLight.getColor();
					
					/*
					 * set GL light colors
					 */
					gl.glLightfv(GL_LIGHT0 + i, GL_AMBIENT, new float[] { 0, 0, 0, 1 }, 0);
					gl.glLightfv(GL_LIGHT0 + i, GL_DIFFUSE, new float[] { color.x, color.y, color.z, 1 }, 0);
					if (directionalLight.castsHighlight())
						gl.glLightfv(GL_LIGHT0 + i, GL_SPECULAR, new float[] { color.x, color.y, color.z, 1 }, 0);
					else
						gl.glLightfv(GL_LIGHT0 + i, GL_SPECULAR, new float[] { 0, 0, 0, 1 }, 0);
					Vector3f direction = directionalLight.getTransformedDirection();
					
					/*
					 * set GL light directions (w = 0 for directional light)
					 */
					gl.glLightfv(GL_LIGHT0 + i, GL_POSITION, new float[] { direction.x, direction.y, direction.z, 0 }, 0);
					
					/*
					 * set GL spot cutoff (180 = no spot)
					 */
					gl.glLightf(GL_LIGHT0 + i, GL_SPOT_CUTOFF, 180);
				} else if (light instanceof RealtimeLighting.PointLight) {
					RealtimeLighting.PointLight pointLight = (RealtimeLighting.PointLight) light;
					Color3f color = pointLight.getColor();
					
					/*
					 * set GL light colors
					 */
					gl.glLightfv(GL_LIGHT0 + i, GL_AMBIENT, new float[] { 0, 0, 0, 1 }, 0);
					gl.glLightfv(GL_LIGHT0 + i, GL_DIFFUSE, new float[] { color.x, color.y, color.z, 1 }, 0);
					if (pointLight.castsHighlight())
						gl.glLightfv(GL_LIGHT0 + i, GL_SPECULAR, new float[] { color.x, color.y, color.z, 1 }, 0);
					else
						gl.glLightfv(GL_LIGHT0 + i, GL_SPECULAR, new float[] { 0, 0, 0, 1 }, 0);
					Point3f position = pointLight.getTransformedPosition();
					
					/*
					 * set GL light position (w = 1 for point light)
					 */
					gl.glLightfv(GL_LIGHT0 + i, GL_POSITION, new float[] { position.x, position.y, position.z, 1 }, 0);
					
					/*
					 * set GL attenuation
					 */
					switch (pointLight.getAttenuation()) {
						case 0: {
							gl.glLightf(GL_LIGHT0 + i, GL_CONSTANT_ATTENUATION, 1);
							gl.glLightf(GL_LIGHT0 + i, GL_LINEAR_ATTENUATION, 0);
							gl.glLightf(GL_LIGHT0 + i, GL_QUADRATIC_ATTENUATION, 0);
						} break;
						case 1: {
							gl.glLightf(GL_LIGHT0 + i, GL_CONSTANT_ATTENUATION, 0);
							gl.glLightf(GL_LIGHT0 + i, GL_LINEAR_ATTENUATION, 1f / pointLight.getDistance());
							gl.glLightf(GL_LIGHT0 + i, GL_QUADRATIC_ATTENUATION, 0);
						} break;
						case 2: {
							gl.glLightf(GL_LIGHT0 + i, GL_CONSTANT_ATTENUATION, 0);
							gl.glLightf(GL_LIGHT0 + i, GL_LINEAR_ATTENUATION, 0);
							gl.glLightf(GL_LIGHT0 + i, GL_QUADRATIC_ATTENUATION, 1f / pointLight.getDistance() / pointLight.getDistance());
						} break;
					}
					
					/*
					 * set GL spot cutoff (180 = no spot)
					 */
					gl.glLightf(GL_LIGHT0 + i, GL_SPOT_CUTOFF, 180);
				} else if (light instanceof RealtimeLighting.SpotLight) {
					RealtimeLighting.SpotLight spotLight = (RealtimeLighting.SpotLight) light;
					Color3f color = spotLight.getColor();
					
					/*
					 * set GL light colors
					 */
					gl.glLightfv(GL_LIGHT0 + i, GL_AMBIENT, new float[] { 0, 0, 0, 1 }, 0);
					gl.glLightfv(GL_LIGHT0 + i, GL_DIFFUSE, new float[] { color.x, color.y, color.z, 1 }, 0);
					if (spotLight.castsHighlight())
						gl.glLightfv(GL_LIGHT0 + i, GL_SPECULAR, new float[] { color.x, color.y, color.z, 1 }, 0);
					else
						gl.glLightfv(GL_LIGHT0 + i, GL_SPECULAR, new float[] { 0, 0, 0, 1 }, 0);
					Point3f position = spotLight.getTransformedPosition();
					
					/*
					 * set GL light position (w = 1 for point light)
					 */
					gl.glLightfv(GL_LIGHT0 + i, GL_POSITION, new float[] { position.x, position.y, position.z, 1 }, 0);
					
					/*
					 * set GL attenuation
					 */
					switch (spotLight.getAttenuation()) {
						case 0: {
							gl.glLightf(GL_LIGHT0 + i, GL_CONSTANT_ATTENUATION, 1);
							gl.glLightf(GL_LIGHT0 + i, GL_LINEAR_ATTENUATION, 0);
							gl.glLightf(GL_LIGHT0 + i, GL_QUADRATIC_ATTENUATION, 0);
						} break;
						case 1: {
							gl.glLightf(GL_LIGHT0 + i, GL_CONSTANT_ATTENUATION, 0);
							gl.glLightf(GL_LIGHT0 + i, GL_LINEAR_ATTENUATION, 1f / spotLight.getDistance());
							gl.glLightf(GL_LIGHT0 + i, GL_QUADRATIC_ATTENUATION, 0);
						} break;
						case 2: {
							gl.glLightf(GL_LIGHT0 + i, GL_CONSTANT_ATTENUATION, 0);
							gl.glLightf(GL_LIGHT0 + i, GL_LINEAR_ATTENUATION, 0);
							gl.glLightf(GL_LIGHT0 + i, GL_QUADRATIC_ATTENUATION, 1f / spotLight.getDistance() / spotLight.getDistance());
						} break;
					}
					
					Vector3f direction = spotLight.getTransformedDirection();
					
					/*
					 * set GL spot direction
					 */
					gl.glLightfv(GL_LIGHT0 + i, GL_SPOT_DIRECTION, new float[] { direction.x, direction.y, direction.z }, 0);
					
					/*
					 * set GL spot cutoff
					 */
					gl.glLightf(GL_LIGHT0 + i, GL_SPOT_CUTOFF, spotLight.getRadius());
					
					/*
					 * set GL spot cutoff exponent
					 */
					gl.glLightf(GL_LIGHT0 + i, GL_SPOT_EXPONENT, 0);
				}
			} else {
				gl.glDisable(GL_LIGHT0 + i);
			}
		}
	}
	
	public static void drawSelection(GL gl, Selection selection, Color4f highlighColor) {
		final Point3f p = new Point3f();
		switch (selection.getType()) {
		case LIMIT:
			gl.glPointSize(6);
			gl.glColor4f(highlighColor.x, highlighColor.y, highlighColor.z, highlighColor.w);
			gl.glBegin(GL_POINTS);
			for (AbstractVertex vertex : selection.getVertices()) {
				vertex.getLimit(p);
				gl.glVertex3f(p.x, p.y, p.z);
			}
			gl.glEnd();
			break;
		case VERTICES:
			gl.glPointSize(6);
			gl.glColor4f(highlighColor.x, highlighColor.y, highlighColor.z, highlighColor.w);
			gl.glBegin(GL_POINTS);
			for (AbstractVertex vertex : selection.getVertices()) {
				vertex.getPosition(p);
				gl.glVertex3f(p.x, p.y, p.z);
			}
			gl.glEnd();
			break;
		case EDGES:
			gl.glLineWidth(3);
			gl.glColor4f(highlighColor.x, highlighColor.y, highlighColor.z, highlighColor.w);
			gl.glBegin(GL_LINES);
			for (HalfEdge edge : selection.getEdges()) {
				edge.getVertex().getPosition(p);
				gl.glVertex3f(p.x, p.y, p.z);
				edge.getPairVertex().getPosition(p);
				gl.glVertex3f(p.x, p.y, p.z);
			}
			gl.glEnd();
			break;
		case FACES:
			gl.glLineWidth(2);
			for (Face face : selection.getFaces()) {
				gl.glInterleavedArrays(GL_N3F_V3F, 0, face.getControlSurface());
				gl.glColor4f(highlighColor.x, highlighColor.y, highlighColor.z, highlighColor.w * 0.5f);
				gl.glDrawArrays(GL_TRIANGLE_FAN, 0, face.getSides() + 2);
				gl.glColor4f(highlighColor.x, highlighColor.y, highlighColor.z, highlighColor.w);
				gl.glDrawArrays(GL_LINE_LOOP, 1, face.getSides());
			}
			break;
		}
	}
	
	private void drawSceneGraphElement(GL gl, SceneGraphNode node) {
		if (node instanceof SdsModel) {
			Sds sds = ((SdsModel) node).getSds();
			drawSds(
				gl,
				sds,
				showControlMeshAttr.getBoolean(),
				showLimitSurfaceAttr.getBoolean(),
				showProjectedMeshAttr.getBoolean(),
//				sds.getMinLevelAttribute().getInt(),
				sds.getEditLevelAttribute().getInt(),
				LINE_COLOR,
				LINE_MATERIAL.getGlMaterial()
			);
		} else if (node instanceof Bone) {
			drawBone(gl, (Bone) node);
		}
		
		for (SceneGraphNode child : node.getChildrenAttribute().getElements()) {
			drawSceneGraphElement(gl, child);
		}
	}
	
	public void drawBone(GL gl, Bone bone) {
		double length = bone.getLengthAttribute().getDouble();
		bone.getLocal2WorldTransform(transformUtil, TransformUtil.LOCAL);
		transformUtil.getMatrix(TransformUtil.LOCAL, TransformUtil.WORLD, modelView);
		modelView[0] *= length;
		modelView[1] *= length;
		modelView[2] *= length;
		modelView[4] *= length;
		modelView[5] *= length;
		modelView[6] *= length;
		modelView[8] *= length;
		modelView[9] *= length;
		modelView[10] *= length;
		gl.glPushMatrix();
		gl.glLoadMatrixd(modelView, 0);
		bone.applyColor(BONE_MATERIAL);
		setMaterial(gl, GL_FRONT, BONE_MATERIAL);
		BONE.draw(gl);
		gl.glPopMatrix();
	}
	
	public void drawString(GL gl, String string, int x, int y) {
		gl.glEnable(GL_BLEND);
		char[] c = string.toCharArray();
		int width = 0;
		for (int i = 0; i < c.length; i++) {
			width += GlFont.fontAdvance[c[i]];
		}
		gl.glColor4f(0, 0, 0, 0.5f);
		gl.glBegin(GL_LINES);
		gl.glVertex2i(x - 6, y - 4); gl.glVertex2i(x - 6, y - 8);
		gl.glVertex2i(x - 5, y - 2); gl.glVertex2i(x - 5, y - 10);
		gl.glVertex2i(x - 4, y - 1); gl.glVertex2i(x - 4, y - 11);
		gl.glVertex2i(x - 3, y - 0); gl.glVertex2i(x - 3, y - 12);
		gl.glVertex2i(x - 2, y - 0); gl.glVertex2i(x - 2, y - 12);
		gl.glVertex2i(x + width + 5, y - 4); gl.glVertex2i(x + width + 5, y - 8);
		gl.glVertex2i(x + width + 4, y - 2); gl.glVertex2i(x + width + 4, y - 10);
		gl.glVertex2i(x + width + 3, y - 1); gl.glVertex2i(x + width + 3, y - 11);
		gl.glVertex2i(x + width + 2, y - 0); gl.glVertex2i(x + width + 2, y - 12);
		gl.glVertex2i(x + width + 1, y - 0); gl.glVertex2i(x + width + 1, y - 12);
		
		gl.glEnd();
		gl.glBegin(GL_TRIANGLE_FAN);
		gl.glVertex2i(x - 1, y + 1);
		gl.glVertex2i(x + width + 1, y + 1);
		gl.glVertex2i(x + width + 1, y - 13);
		gl.glVertex2i(x - 1, y - 13);
		gl.glEnd();
		
		gl.glColor4f(0, 0, 0, 0.25f);
		for (int yy = y - 1; yy < y + 2; yy++) {
			for (int xx = x - 1; xx < x + 2; xx++) {
				gl.glRasterPos2i(xx, yy);							// set raster position
				for (int i = 0, n = c.length; i < n; i++) {		// loop over characters
			    	if (c[i] < 256) {
			    		gl.glCallList(fontOffset + c[i]);		// call display list that draws a character
			    	}
			    }
			}
		}
		gl.glColor4f(1, 1, 1, 1f);
		gl.glRasterPos2i(x, y);							// set raster position
		
		for (int i = 0; i < c.length; i++) {	// loop over characters
	    	if (c[i] < 256) {
	    		gl.glCallList(fontOffset + c[i]);		// call display list that draws a character
	    	}
	    }
	    gl.glDisable(GL_BLEND);
	}
	
	public static void drawSds(GL gl, Sds sds, boolean showControlMesh, boolean showLimit, boolean showProjectedMesh, int editLevel, Color4f lineColor, GlMaterial meshMaterial) {		
		final Tuple3f p = new Point3f();
		
		for (int level = 0; level <= SdsConstants.MAX_LEVEL; level++) {
			/* draw limit surface */
			if (showLimit) {
				gl.glEnable(GL_LIGHTING);
				GlMaterial currentMaterial = null;
				for (Face face : sds.getFaces(level)) {
					if (!face.isDrawable()) {
						continue;
					}
					GlMaterial faceMaterial = face.getMaterial().getGlMaterial();
					if (currentMaterial != faceMaterial) {
						setMaterial(gl, GL_FRONT, faceMaterial);
						currentMaterial = faceMaterial;
					}
	//				face.getLimitSurface(limitSurfaceBuffer);
	//				gl.glInterleavedArrays(GL_N3F_V3F, 0, limitSurfaceBuffer);
					gl.glInterleavedArrays(GL_N3F_V3F, 0, face.getLimitSurface());
					gl.glDrawArrays(GL_TRIANGLE_FAN, 0, face.getSides());
				}
			}
		}
		
		/* draw control mesh */
		if (showControlMesh) {
			if (!showLimit) {
				// render solid control surface
				gl.glEnable(GL_LIGHTING);
				GlMaterial currentMaterial = null;
				for (Face face : sds.getFaces(editLevel)) {
					GlMaterial faceMaterial = face.getMaterial().getGlMaterial();
					if (currentMaterial != faceMaterial) {
						setDiffuseMaterial(gl, GL_FRONT, faceMaterial);
						currentMaterial = faceMaterial;
					}
//					face.getMidpointNormal(p);
//					gl.glNormal3d(p.x, p.y, p.z);
					gl.glInterleavedArrays(GL_N3F_V3F, 0, face.getControlSurface());
					gl.glDrawArrays(GL_TRIANGLE_FAN, 0, face.getSides() + 2);
				}
			} 
			// render wireframe
			gl.glDisable(GL_LIGHTING);
			gl.glColor4f(lineColor.x, lineColor.y, lineColor.z, lineColor.w);
			gl.glBegin(GL_LINES);
			for (HalfEdge edge : sds.getEdges(editLevel, false)) {
				if (edge.isPrimary() || edge.getPairFace() == null) {
					edge.getVertex().getPosition(p);
					gl.glVertex3f(p.x, p.y, p.z);
					edge.getPairVertex().getPosition(p);
					gl.glVertex3f(p.x, p.y, p.z);
				}
			}
			gl.glEnd();
			
		}
			
//		/* draw projected mesh */
//		if (showProjectedMesh) {
//			gl.glEnable(GL_LIGHTING);
//			setMaterial(gl, GL_FRONT, meshMaterial);
//			gl.glBegin(GL_LINES);
//			int levelsToGo = level - editLevel;
//			for (HalfEdge edge : sds.getEdges(editLevel, false)) {
//				if (edge.isPrimary() || edge.getPairFace() == null) {
//					drawEdgeLimit(gl, p, levelsToGo, edge);
//				}
//			}
//			gl.glEnd();
//		}
		
		
		/* draw stray edges/vertices */
		gl.glDisable(GL_LIGHTING);
		gl.glColor3f(0.5f, 0.5f, 1.0f);
		gl.glBegin(GL_LINES);
		for (HalfEdge strayEdge : sds.getStrayEdges()) {
			strayEdge.getVertex().getPosition(p);
			gl.glVertex3f(p.x, p.y, p.z);
			strayEdge.getPairVertex().getPosition(p);
			gl.glVertex3f(p.x, p.y, p.z);
		}
		gl.glEnd();
		gl.glPointSize(3);
		gl.glBegin(GL_POINTS);
		for (BaseVertex strayVertex : sds.getStrayVertices()) {
			strayVertex.getPosition(p);
			gl.glVertex3f(p.x, p.y, p.z);
		}
		gl.glEnd();
		
		
//		/* draw vertex limit */
//		gl.glPointSize(5.0f);
//		gl.glBegin(GL_POINTS);
//		for (AbstractVertex vertex : sds.getVertices(level, false)) {
//			vertex.getLimit(p);
//			gl.glVertex3f(p.x, p.y, p.z);
//		}
//		gl.glEnd();
	}
	
	private void validateDepthBuffer() {
		if (depthBufferValid || depthBufferFrozen) {
			return;
		}
		drawable.getContext().makeCurrent();
		GL gl = drawable.getGL();
		gl.glReadBuffer(GL_FRONT);
		gl.glReadPixels(0, 0, drawable.getWidth(), drawable.getHeight(), GL_DEPTH_COMPONENT, GL_FLOAT, depthBuffer);
		depthBufferValid = true;
		drawable.getContext().release();
	}
	
	public void freezeDepthBuffer(boolean freeze) {
		depthBufferFrozen = freeze;
	}
	
	private void draw(GL gl) {
		time = System.nanoTime();
		spatialMode(gl);
		Color3f background = Settings.getInstance().colors.background;
		gl.glClearColor(background.x, background.y, background.z, 1);
		component.setBackground(background.get());
		gl.glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);	// clear color and depth buffers
		
		resetModelviewMatrix(gl);
		
		gl.glShadeModel(GL_SMOOTH);
		gl.glEnable(GL_NORMALIZE);
		
		gl.glLineWidth(1);
		SceneGraphNode sceneGraphRoot = Main.getInstance().getSceneGraphRoot();
		drawSceneGraphElement(gl, sceneGraphRoot);

		rasterMode(gl);
		if (showNodeNamesAttr.getBoolean()) {
			drawSceneGraphNames(gl, sceneGraphRoot);
		}
		drawInfo(gl);
		depthBufferValid = false;
	}
	
	public void resetModelviewMatrix(GL gl) {
		viewDef.configureTransformUtil(transformUtil);
		transformUtil.getMatrix(TransformUtil.WORLD, TransformUtil.CAMERA, modelView);
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadMatrixd(modelView, 0);
	}
	
	private void drawSceneGraphNames(GL gl, SceneGraphNode node) {
		if (node instanceof XFormNode) {
			((XFormNode) node).getLocal2WorldTransform(transformUtil, LOCAL);
			Point3d p = new Point3d();
			transformUtil.projectToScreen(LOCAL, p, p);
			if (node instanceof XFormNode) {
				if (!transformUtil.isPerspective() || p.z > nearClip) {
					drawString(gl, ((XFormNode) node).getNameAttribute().getValue(), (int) p.x, (int) p.y);
				}
			}
		}
		for (SceneGraphNode child : node.getChildrenAttribute().getElements()) {
			drawSceneGraphNames(gl, child);
		}
	}
	
	private void drawInfo(GL gl) {
		String fps = Long.toString(1000000000 / (System.nanoTime() - time));
		drawString(gl, getInfo(), 4, 16);
		drawString(gl, "repaint " + (repaintCount++) + " " + fps + "fps", 4, 32);
	}
	
	private void drawOverlays(GL gl) {
		spatialMode(gl);
		gl.glDepthMask(false);									// disable writing to depth-buffer
		gl.glEnable(GL_BLEND);									// enable blending
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		for (ViewportOverlay overlay : overlays) {
			overlay.drawOverlay(Viewport.this);
		}
		gl.glDepthMask(true);									// enable writing to depth-buffer
		gl.glDisable(GL_BLEND);									// disable blending
		gl.glFinish();
		
		/* swap buffers and release context */
		drawable.swapBuffers();
		drawable.getContext().release();
	}
	
	private static void drawEdgeLimit(GL gl, Tuple3f p, int levelsToGo, HalfEdge edge) {
		System.out.println("drawEdgeLimit");
		if (levelsToGo > 0) {
//			Face face = edge.getFace();
//			face.getSubEdges(edge.getFaceEdgeIndex(), subEdges);
//			HalfEdge edge0 = subEdges[0];
//			HalfEdge edge1 = subEdges[1];
//			drawEdgeLimit(gl, p, levelsToGo - 1, edge0, subEdges);
//			drawEdgeLimit(gl, p, levelsToGo - 1, edge1, subEdges);
			drawEdgeLimit(gl, p, levelsToGo - 1, edge.getSubEdge());
			drawEdgeLimit(gl, p, levelsToGo - 1, edge.getPair().getSubEdge());
		} else {
			edge.getVertex().getNormal(p);
			gl.glNormal3d(p.x, p.y, p.z);
			edge.getVertex().getLimit(p);
			gl.glVertex3d(p.x, p.y, p.z);
			edge.getPairVertex().getNormal(p);
			gl.glNormal3d(p.x, p.y, p.z);
			edge.getPairVertex().getLimit(p);
			gl.glVertex3d(p.x, p.y, p.z);
		}
	}
	
	public void redrawViewport() {
		overlayStrategy.redrawViewport();
	}
	
	public void redrawOverlays() {
		overlayStrategy.redrawOverlays();
	}
	
	public void addOverlay(ViewportOverlay overlay) {
		overlays.add(overlay);
		System.out.println("overlay added, now " + overlays.size());
	}
	
	public void removeOverlay(ViewportOverlay overlay) {
		overlays.remove(overlay);
		System.out.println("overlay removed, now " + overlays.size());
	}
	
	private static interface OverlayStrategy {
		void redrawViewport();
		void redrawOverlays();
	}
	
	public void spatialMode(GL gl) {
		float w = (float) drawable.getWidth() / 2;
		float h = (float) drawable.getHeight() / 2;
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		if (viewDef instanceof PerspectiveViewDef) {
			PerspectiveViewDef perspectiveView = ((PerspectiveViewDef) viewDef);
			float a = 0.5f / (float) perspectiveView.getRelativeFocalLength();
			float b = a * h / w;
			gl.glFrustum(-a, a, -b, b, nearClip, farClip);
		} else {
			gl.glOrtho(-w, w, -h, h, -farClip, farClip);
		}
		gl.glDepthFunc(GL_LESS);
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glEnable(GL_DEPTH_TEST);
		gl.glShadeModel(GL_SMOOTH);
	}
	
	public void rasterMode(GL gl) {
		int w = drawable.getWidth();
		int h = drawable.getHeight();
		gl.glViewport(0, 0, w, h);
	    gl.glMatrixMode(GL_PROJECTION);
	    gl.glLoadIdentity();
	    gl.glOrtho(0, w, h, 0, 1, -1);
	    gl.glMatrixMode(GL_MODELVIEW);
	    gl.glLoadIdentity();
	    gl.glDisable(GL_LIGHTING);
		gl.glDisable(GL_DEPTH_TEST);
		gl.glShadeModel(GL_FLAT);
	}
	
	/**
	 * OverlayStrategy that does no overlay - everything is rendered directly to the BACK buffer,
	 * redrawOverlays() triggers a complete viewport redraw.
	 * 
	 * FOR DEBUGGING PURPOSES ONLY!
	 * 
	 * @author sascha
	 */
	private final class DebugOverlayStrategy implements OverlayStrategy {
		public void redrawViewport() {
			drawable.getContext().makeCurrent();
			GL gl = drawable.getGL();
			
			/* render to AUX0 */
			gl.glDrawBuffer(GL_BACK);
			draw(gl);
			
			drawOverlays(gl);
		}
		
		public void redrawOverlays() {
			redrawViewport();
		}
	}
	
	/**
	 * OverlayStrategy that uses the OpenGL auxiliary buffer AUX0 for rendering
	 */
	private final class AuxBufferOverlayStrategy implements OverlayStrategy {
		
		public void redrawViewport() {
			drawable.getContext().makeCurrent();
			GL gl = drawable.getGL();
			
			/* render to AUX0 */
			gl.glDrawBuffer(GL_AUX0);
			draw(gl);
			gl.glFlush();
			gl.glFinish();
			redrawOverlays();
		}
		
		public void redrawOverlays() {
			drawable.getContext().makeCurrent();
			GL gl = drawable.getGL();
			
			/* copy AUX0 content to BACK buffer */
			gl.glReadBuffer(GL_AUX0);
			gl.glDrawBuffer(GL_BACK);
			rasterMode(gl);
			int w = drawable.getWidth();
			int h = drawable.getHeight();
			gl.glRasterPos2i(0, h);
			gl.glCopyPixels(0, 0, w, h, GL_COLOR);
			
			drawOverlays(gl);
		}
	}
	
	/**
	 * This OverlayStrategy user a texture to store the viewport contents
	 * @author sascha
	 */
	private class TextureOverlayStrategy implements OverlayStrategy {
		private int maxTextureSize;
		private int texture;
		private float txBottom;
		private float txRight;
		
		public void redrawViewport() {
			drawable.getContext().makeCurrent();
			GL gl = drawable.getGL();
			
			/* render to BACK */
			gl.glDrawBuffer(GL_BACK);
			draw(gl);
			
			/* copy BACK buffer to texture */
			copyBackToTexture(gl);
			
			/* draw overlays */
			redrawOverlays();
		}
		
		public void redrawOverlays() {
			drawable.getContext().makeCurrent();
			GL gl = drawable.getGL();

			copyTextureToBack(gl);
				
			drawOverlays(gl);
		}
		
		void copyBackToTexture(GL gl) {
			rasterMode(gl);
			gl.glEnable(GL_TEXTURE_2D);
			gl.glBindTexture(GL.GL_TEXTURE_2D, texture);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
	        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
	        
			int txWidth = textureSize(gl, drawable.getWidth());
			int txHeight = textureSize(gl, drawable.getHeight());
			
			gl.glReadBuffer(GL_BACK);
			gl.glCopyTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 0, 0, txWidth, txHeight, 0);
			
			txBottom = (float) drawable.getHeight() / txHeight;
			txRight = (float) drawable.getWidth() / txWidth;
			
			gl.glDisable(GL_TEXTURE_2D);
		}
		
		void copyTextureToBack(GL gl) {
			rasterMode(gl);
			gl.glDrawBuffer(GL_BACK);
			gl.glEnable(GL_TEXTURE_2D); 
			gl.glBindTexture(GL_TEXTURE_2D, texture);
			
			int w = drawable.getWidth();
			int h = drawable.getHeight();
			
			gl.glColor3f(1, 1, 1);
			gl.glBegin(GL_QUADS);
			gl.glTexCoord2f(0, txBottom);
			gl.glVertex2f(0, 0);
			gl.glTexCoord2f(0, 0);
			gl.glVertex2f(0, h);
			gl.glTexCoord2f(txRight, 0);
			gl.glVertex2f(w, h);
			gl.glTexCoord2f(txRight, txBottom);
			gl.glVertex2f(w, 0);
			gl.glEnd();
			gl.glDisable(GL_TEXTURE_2D);
		}
		
		private int textureSize(GL gl, int size) {
			if (maxTextureSize == 0) {
				maxTextureSize = getIntVar(gl, GL_MAX_TEXTURE_SIZE);
			}
			if (size > maxTextureSize) {
				return maxTextureSize;
			}
			for (int i = 6; i < 24; i++) {
				int s = 1 << i;
				if (s >= size) {
					return s;
				}
			}
			assert false : "should never get here";
			return -1;
		}	
	}
	
	/**
	 * This OverlayStrategy user a texture to store the viewport contents. The texture contents are
	 * updated lazily.
	 * @author sascha
	 */
	private class LazyTextureOverlayStrategy extends TextureOverlayStrategy {
		private boolean textureValid;
		
		public void redrawViewport() {
			drawable.getContext().makeCurrent();
			GL gl = drawable.getGL();
			
			/* render to BACK */
			gl.glDrawBuffer(GL_BACK);
			draw(gl);
			
			/* draw overlays */
			redrawOverlays();
			
			textureValid = false;				// texture is now invalid
		}
		
		public void redrawOverlays() {
			drawable.getContext().makeCurrent();
			GL gl = drawable.getGL();

			if (textureValid) {
				copyTextureToBack(gl);
			} else {
				/* render to BACK */
				gl.glDrawBuffer(GL_BACK);
				draw(gl);
				
				copyBackToTexture(gl);
				textureValid = true;			// texture is now valid
			}
			
			drawOverlays(gl);
		}
	}
}
