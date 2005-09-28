package jpatch.boundary;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

import javax.swing.*;
import javax.vecmath.*;
import buoy.widget.*;
import jpatch.entity.*;
import jpatch.renderer.PatchTesselator3;

public class CameraViewport implements JPatchDrawableEventListener {
	public static final int ZOOM = 1;
	public static final int WALK = 2;
	public static final int MOVE = 3;
	public static final int PAN = 4;
	public static final int ROLL = 5;
	public static final int OBJECT_MOVE = 6;
	public static final int OBJECT_ROLL = 7;
	public static final int OBJECT_SCALE = 8;
	public static final String[] MODE_NAME = new String[] {
		"",
		"Zoom",
		"Walk",
		"Move",
		"Pan",
		"Roll",
		"Move Object",
		"Roll Object",
		"Scale Object"
	};
	private static JPatchSettings settings = JPatchSettings.getInstance();
	private static int iCurveSubdiv = 5;
	
	private static float[] cB0;
	private static float[] cB1;
	private static float[] cB2;
	private static float[] cB3;
	
	private float fNearClip = 1.0f;
	private float fOverScan = 1.2f;
	private ArrayList listObjects;
	private int X, Y;
	private int mouseX, mouseY;
	// ADDED DFC
	private AnimObject activeObject = null;
	private static int iMode = 0;
	
	//private Matrix4d m3Cam = new Matrix4d();
	
	private double dRoll, dPitch, dYaw;
	
	private PatchTesselator3 patchTesselator = new PatchTesselator3();
	
	// camera view buttons
//	private VButton buttonPan = new VButton(-40, 10, 31, 31, (new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/pan.png"))).getImage());
//	private VButton buttonWalk = new VButton(-40, 50, 31, 31, (new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/walk.png"))).getImage());
//	private VButton buttonMove = new VButton(-40, 90, 31, 31, (new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/move.png"))).getImage());
//	private VButton buttonZoom = new VButton(-40, 130, 31, 31, (new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/zoom.png"))).getImage());
//	private VButton buttonRoll = new VButton(-40, 170, 31, 31, (new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/roll.png"))).getImage());
//	
	// object buttons
//	private VButton buttonObjectRoll = new VButton(-40, 250, 31, 31, (new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/rotateobject.png"))).getImage());
//	private VButton buttonObjectMove = new VButton(-40, 290, 31, 31, (new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/moveobject.png"))).getImage());
//	private VButton buttonObjectScale = new VButton(-40, 330, 31, 31, (new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/scaleobject.png"))).getImage());
//	
//	private VButton[] aButtons = new VButton[] { buttonPan, buttonWalk, buttonMove, buttonZoom, buttonRoll, buttonObjectRoll, buttonObjectMove, buttonObjectScale };
//	
	// prevent mouse from triggering action if button wasn't released after mouse press
	private boolean ignoreMouseMove = false;
	
//	private JPanel panel = new JPanel() {
//		/**
//		 * 
//		 */
//		private static final long serialVersionUID = 1L;
//
//		public void paintComponent(Graphics g) {
//			if (image == null || image.getWidth(null) != panel.getWidth() || image.getHeight(null) != panel.getHeight()) updateImage();
//			//else {
//				g.drawImage(image, 0, 0, this);
//				//for (int i = 0; i < aButtons.length; aButtons[i++].paint(g));
//			//}
//		}
//	};
	
	private JPatchDrawable2 viewport = new JPatchDrawable3D(this, false);
	private Component component = viewport.getComponent();
	private JPanel panel = new JPanel();
	private AWTWidget widget = new AWTWidget(panel);
	private final Camera camera;
	//private BufferedImage image;
	//private int[] aiActiveFrameBuffer;
	//private int iWidth, iHeight, color;
	
	static {
		init();
	}
	
	static void init() {
		cB0 = new float[iCurveSubdiv];
		cB1 = new float[iCurveSubdiv];
		cB2 = new float[iCurveSubdiv];
		cB3 = new float[iCurveSubdiv];
		for (int i = 0; i < iCurveSubdiv; i++) {
			float s = (float) i / (float) (iCurveSubdiv - 1);
			cB0[i] = (1 - s) * (1 - s) * (1 - s);
			cB1[i] = 3 * s * (1 - s) * (1 - s);
			cB2[i] = 3 * s * s * (1 - s);
			cB3[i] = s * s * s;
		}
	}
	
	private JRadioButton modeButton(String image, String tooltip, final int mode, ButtonGroup bg) {
		JRadioButton button = new JRadioButton(new ImageIcon(ClassLoader.getSystemResource(image)));
		bg.add(button);
		button.setToolTipText(tooltip);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setMode(mode);
			}
		});
		button.setBackground(Color.BLACK);
		button.setBorderPainted(true);
		return button;
	}
	
	public CameraViewport(Camera cam, ArrayList objects) {
		camera = cam;
		listObjects = objects;
		//viewport.getComponent().setPreferredSize(new Dimension(640, 480));
		viewport.setProjection(JPatchDrawable2.PERSPECTIVE);
		panel.setPreferredSize(new Dimension(640, 480));
		panel.setLayout(new BorderLayout());
		JPanel panelButtons = new JPanel();
		ButtonGroup bg = new ButtonGroup();
		panelButtons.add(modeButton("jpatch/images/anim/pan.png", "pan", PAN, bg));
		panelButtons.add(modeButton("jpatch/images/anim/walk.png", "walk", WALK, bg));
		panelButtons.add(modeButton("jpatch/images/anim/move.png", "move", MOVE, bg));
		panelButtons.add(modeButton("jpatch/images/anim/zoom.png", "zoom", ZOOM, bg));
		panelButtons.add(modeButton("jpatch/images/anim/roll.png", "roll", ROLL, bg));
		panelButtons.add(modeButton("jpatch/images/anim/rotateobject.png", "rotate object", OBJECT_ROLL, bg));
		panelButtons.add(modeButton("jpatch/images/anim/moveobject.png", "move object", OBJECT_MOVE, bg));
		panelButtons.add(modeButton("jpatch/images/anim/scaleobject.png", "scale object", OBJECT_SCALE, bg));

		panel.add(panelButtons, BorderLayout.NORTH);
		panel.add(component, BorderLayout.CENTER);
		
		component.addMouseMotionListener(new MouseMotionAdapter() {
			
			public void mouseMoved(MouseEvent mouseEvent) {
				// nothing to do
			}
			
			public void mouseDragged(MouseEvent mouseEvent) {
				
				AnimObject activeObject = Animator.getInstance().getActiveObject();
				
				int dx = mouseEvent.getX() - mouseX;
				int dy = mouseEvent.getY() - mouseY;
				mouseX = mouseEvent.getX();
				mouseY = mouseEvent.getY();

				// ignore?
				if (ignoreMouseMove) return;
				
				//Point l = panel.getLocationOnScreen();
				//robot.mouseMove(l.x + mouseX, l.y + mouseY);
				switch (iMode) {
					case ZOOM: {
						float f = (float) Math.min(2.0f, (float) Math.max(0.5, 1 + (float) dx / 500.0f - (float) dy / 500.0f));
						camera.setFocalLength(camera.getFocalLength() * f);
						repaint();
					} break;
					case WALK: {
						//Vector3d v = new Vector3d(dx * Math.sqrt(Math.abs(dx)) * 0.01, 0, dy * Math.sqrt(Math.abs(dy)) * -0.01);
						Vector3d v = new Vector3d(0, 0, dy * Math.sqrt(Math.abs(dy)) * -0.05);
						Matrix3d m = new Matrix3d();
						camera.getTransform().getRotationScale(m);
						m.transform(v);
						Point3d p = camera.getPosition();
						p.add(v);
						camera.setPosition(p);
						
						//fRotY += (float) dx * 0.05f / camera.getFocalLength();
						//m3Rot.rotY(fRotY);
						//m.set(m3RotBackup);
						//m.mul(m3Rot);
						//camera.getTransform().setRotationScale(m);
						
						//camera.setRotationY(rotY);
						//camera.setRotation();
						//Animator.getInstance().setObjectPosition(camera);
						//Animator.getInstance().setCameraRotation(camera);
						repaint();
					} break;
					
					// move button pressed?
					case MOVE: {
						Vector3d v = new Vector3d(dx * Math.sqrt(Math.abs(dx)) * 0.01, dy * Math.sqrt(Math.abs(dy)) * -0.01, 0);
						Matrix3d m = new Matrix3d();
						camera.getTransform().getRotationScale(m);
						m.transform(v);
						Point3d p = camera.getPosition();
						p.add(v);
						camera.setPosition(p);
						repaint();
					} break;
					
					// pan button pressed?
					case PAN: {
						dPitch -= (double) dy * 0.1 / camera.getFocalLength();
						dYaw += (double) dx * 0.1 / camera.getFocalLength();
						//Matrix3d m = new Matrix3d();
						camera.setOrientation(dRoll, dPitch, dYaw);
						
						//m3Rot.rotX(fRotX);
						//m.set(m3Rot);
						//m3Rot.rotY(fRotY);
						//m.mul(m3Rot);
						//m.mul(m3RotBackup);
						//camera.getTransform().setRotationScale(m);
						////camera.setRotationX(rotX);
						////camera.setRotationY(rotY);
						////camera.setRotation();
						repaint();
					} break;
					
					// roll button pressed?
					case ROLL: {
						dRoll -= (double) dx * -0.003;
						camera.setOrientation(dRoll, dPitch, dYaw);
						//fRotZ += (float) dx * -0.05f / camera.getFocalLength();
						//Matrix3d m = new Matrix3d(m3RotBackup);
						//m3Rot.rotZ(fRotZ);
						//m.mul(m3Rot);
						//camera.getTransform().setRotationScale(m);
						////camera.setRotationZ(rotZ);
						////camera.setRotation();
						repaint();
					} break;
				}
				// is an object other than the camera selected?
				if (activeObject != camera) {
						// move object button pressed?
						switch (iMode) {
							case OBJECT_MOVE: {
							// get the current position of the object
							Point3d p = activeObject.getPosition(); 
							
							// middle mouse?
							if (SwingUtilities.isMiddleMouseButton(mouseEvent)) {
								// move only z
								p.z += dy * 0.5;
							} else {
								// move x and y
								p.x += dx * .5;
								p.y -= dy * 0.5;
							}					
							
							// update the object's position
							activeObject.setPosition(p);
							// update the screen
							repaint();
						} break;
						
						// rotate object button pressed?
						case OBJECT_ROLL: {
							// get the current position of the object
							double roll = activeObject.getRoll();
							double pitch = activeObject.getPitch();
							double yaw = activeObject.getYaw();
							
							// middle mouse?
							if (SwingUtilities.isMiddleMouseButton(mouseEvent)) {
								// roll
								roll += dy * .01;
							} else {
								// only move based on which moved more
								if (Math.abs(dx) > Math.abs(dy)) {
									yaw -= dx * .01;
								} else {
									pitch += dy * .01;
								}						
							}
							
							// update the object's position
							activeObject.setOrientation(roll, pitch, yaw);
							// update the screen
							repaint();
						} break;
						
						// scale object button pressed?
						case OBJECT_SCALE: {
							float factor = 1.0f + (dx - dy) * 0.01f;
							if (factor < 0.5f) factor = 0.5f;
							if (factor > 2.0f) factor = 2.0f;
							activeObject.setScale(activeObject.getScale() * factor);
							repaint();
						} break;
					}
				}

			}
		});
		
		component.addMouseListener(new MouseAdapter() {
			public void mouseExited(MouseEvent mouseEvent) {
				// clear ignore flag
				ignoreMouseMove = false;
			}
			public void mousePressed(MouseEvent mouseEvent) {
				// get the mouse position
				mouseX = mouseEvent.getX();
				mouseY = mouseEvent.getY();
			}
		});
//				if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
//					// button hit?
//					int hitButton = -1;
//					for (int i = 0; i < aButtons.length; i++) {
//						// button hit?
//						if (aButtons[i].isHit(mouseX, mouseY)) {
//							// store index
//							hitButton = i;
//							// exit loop
//							break;
//						}
//					}
//					
//					// button hit?
//					if (hitButton != -1) {
//						// ignore mouse move until release
//						ignoreMouseMove = true;
//						
//						// toggle buttons
//						for (int i = 0; i < aButtons.length; i++) {
//							aButtons[i].setPressedState(hitButton==i);	
//						}
//						
//						// rerender display
//						rerender();
//					}
//					
//				}
//				//fRotX = fRotY = fRotZ = 0;
//				//camera.getTransform().getRotationScale(m3RotBackup);
//				
//				// get the initial values
//				dRoll = camera.getRoll();
//				dPitch = camera.getPitch();
//				dYaw = camera.getYaw();
//				
//				//panel.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "test"));
//			}
//			public void mouseReleased(MouseEvent mouseEvent) {
//				// stop mouse events from being suppressed
//				ignoreMouseMove = false;
//				
//				// update camera and obbject motioncurves
//				Animator.getInstance().updateCurvesFor(camera);
//				if (activeObject != camera)
//					Animator.getInstance().updateCurvesFor(Animator.getInstance().getActiveObject());
//			}
//		});
//		panel.setPreferredSize(new Dimension(640,480));
	}

	// ADDED DFC
	public void setActiveObject( AnimObject object ) {
		this.activeObject = object;
	}
	
	public AWTWidget getWidget() {
		return widget;
	}
	
	public void setMode(int mode) {
		iMode = mode;
		repaint();
	}
	
//	private void updateImage() {
//		iWidth = panel.getWidth();
//		iHeight = panel.getHeight();
//		image = new BufferedImage(iWidth, iHeight, BufferedImage.TYPE_INT_RGB);
//		aiActiveFrameBuffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
//		render();
//	}
//	
	public void repaint() {
		viewport.display();
	}
//	
//	public void rerender() {
//		if (image == null) updateImage();
//		else render();
//	}
	
	public void display(JPatchDrawable2 viewport) {
		viewport.clear(JPatchDrawable2.COLOR_BUFFER | JPatchDrawable2.DEPTH_BUFFER, new Color3f(0,0,0)); // FIXME
		viewport.setFocalLength(camera.getFocalLength());
		int W = viewport.getComponent().getWidth();
		int H = viewport.getComponent().getHeight();
//		Graphics2D g = (Graphics2D) image.getGraphics();
//		g.setColor(settings.cBackground);
//		g.fillRect(0, 0, W, H);
		float w;
		int hh, ww;
		float ar = settings.fRenderAspectWidth / settings.fRenderAspectHeight;
		if (W / ar < H) {
			ww = (int) (W / fOverScan);
			hh = (int) (ww / ar);
			w = (float) camera.getFocalLength() * (float) W / fOverScan / (float) camera.getFilmSize();
		} else {
			hh = (int) (H / fOverScan);
			ww = (int) (hh * ar);
			w = (float) camera.getFocalLength() * (float) H / fOverScan / (float) (camera.getFilmSize() / ar);
		}
		Matrix4f m4Cam = new Matrix4f(camera.getTransform());
		m4Cam.invert();
		Matrix4f m4View = new Matrix4f();
		Matrix4f m4Screen = new Matrix4f(
			w, 0f, 0f, 0f,
			0f, -w, 0f, 0f,
			0f, 0f, 1, 0f,
			0f, 0f, 0f, 1f
		);
		Matrix4f m4Model = new Matrix4f();
		X = H >> 1;
		Y = Y >> 1;
		
		ArrayList models = new ArrayList();
		ArrayList lights = new ArrayList();
		for (Iterator it = Animator.getInstance().getObjectList().iterator(); it.hasNext(); ) {
			Object o = it.next();
			if (o instanceof AnimModel) {
				models.add(o);
			}
			if (o instanceof AnimLight) {
				lights.add(o);
			}
		}
		
		RealtimeLighting rtl = null;
		if (viewport.isShadingSupported()) {
			rtl = RealtimeLighting.createAnimLight((AnimLight[]) lights.toArray(new AnimLight[lights.size()]));
			rtl.setAmbientColor(new Color3f(1,1,1));
			if (viewport.isLightingSupported())
				viewport.setLighting(rtl);
		}
		
		Matrix4d m4d = new Matrix4d();
		Point3f p0 = new Point3f();
		Point3f p1 = new Point3f();
		Point3f p2 = new Point3f();
		Point3f p3 = new Point3f();
		
		for (Iterator it = listObjects.iterator(); it.hasNext(); ) {
			Object o = it.next();
			if (o instanceof AnimModel) {
				AnimModel character = (AnimModel) o;
								
				m4Model.set(character.getTransform());
				m4View.setIdentity();
				//m4View.mul(m4Screen);
				m4View.mul(m4Cam);
				m4View.mul(m4Model);
				
				m4d.set(m4View);
				
				if (Animator.getInstance().getActiveObject() == character) viewport.setColor(new Color3f(1,1,0));
				else viewport.setColor(new Color3f(1,1,1));
				if (true) { // draw curves
					for (Iterator it2 = character.getModel().getCurveSet().iterator(); it.hasNext(); ) {
//					for (Curve curve = character.getModel().getFirstCurve(); curve != null; curve = curve.getNext()) {
						ControlPoint start = (ControlPoint) it2.next();
						if (!start.isStartHook()) {
							for (ControlPoint cp = start; cp != null; cp = cp.getNextCheckNextLoop()) {
								if (cp.getNext() != null) {
									p0.set(cp.getPosition());
									p1.set(cp.getOutTangent());
									p2.set(cp.getNext().getInTangent());
									p3.set(cp.getNext().getPosition());
									m4View.transform(p0);
									m4View.transform(p1);
									m4View.transform(p2);
									m4View.transform(p3);
									drawCurve3D(viewport, p0, p1, p2, p3);
								}
							}
						}
					}
				}
				if (false) { // draw shaded, lit patches (GL only)
					patchTesselator.tesselate(character.getModel(), 1, m4d, true);
					PatchTesselator3.Vertex[] vertex = patchTesselator.getVertexArray();
					java.util.List quads = patchTesselator.getQuadList();
					int[] triangle;
					for (int i = 0, n = quads.size(); i < n; i++) {
						PatchTesselator3.Quad quad = (PatchTesselator3.Quad) quads.get(i);
						viewport.setMaterial(quad.material.getMaterialProperties());
						if (quad.isTriangle()) {
							triangle = quad.getTriangle();
							p0.set(vertex[triangle[0]].p);
							p1.set(vertex[triangle[1]].p);
							p2.set(vertex[triangle[2]].p);
							viewport.drawTriangle(p2, vertex[triangle[2]].n, p1, vertex[triangle[1]].n, p0, vertex[triangle[0]].n);
						} else {
							triangle = quad.getTriangle1();
							p0.set(vertex[triangle[0]].p);
							p1.set(vertex[triangle[1]].p);
							p2.set(vertex[triangle[2]].p);
//							m4View.transform(p0);
//							m4View.transform(p1);
//							m4View.transform(p2);
//							viewport.drawLine(p0, p1);
//							viewport.drawLine(p1, p2);
//							viewport.drawLine(p2, p0);
							viewport.drawTriangle(p2, vertex[triangle[2]].n, p1, vertex[triangle[1]].n, p0, vertex[triangle[0]].n);
							triangle = quad.getTriangle2();
							p0.set(vertex[triangle[0]].p);
							p1.set(vertex[triangle[1]].p);
							p2.set(vertex[triangle[2]].p);
//							m4View.transform(p0);
//							m4View.transform(p1);
//							m4View.transform(p2);
//							viewport.drawLine(p0, p1);
//							viewport.drawLine(p1, p2);
//							viewport.drawLine(p2, p0);
							viewport.drawTriangle(p2, vertex[triangle[2]].n, p1, vertex[triangle[1]].n, p0, vertex[triangle[0]].n);
						}
					}
				}
			}
			else if (o instanceof AnimLight) {
				AnimLight light = (AnimLight) o;
//				if (Animator.getInstance().getActiveObject() == light) setColor(settings.cSelection);
//				else setColor(settings.cCurve);
				m4Model.set(light.getTransform());
				m4View.setIdentity();
				//m4View.mul(m4Screen);
				m4View.mul(m4Cam);
				m4View.mul(m4Model);
				float s = 1f / (float) Math.sqrt(3);
				Point3f[] p = new Point3f[] {
					new Point3f(-1,0,0), new Point3f(1,0,0),
					new Point3f(0,-1,0), new Point3f(0,1,0),
					new Point3f(0,0,-1), new Point3f(0,0,1),
					new Point3f(-s,-s,-s), new Point3f(s,s,s),
					new Point3f(-s,-s,s), new Point3f(s,s,-s),
					new Point3f(s,-s,-s), new Point3f(-s,s,s),
					new Point3f(s,-s,s), new Point3f(-s,s,-s)
				};
				for (int i = 0; i < p.length; m4View.transform(p[i++]));
				viewport.drawLine(p[0], p[1]);
				viewport.drawLine(p[2], p[3]);
				viewport.drawLine(p[4], p[5]);
				viewport.drawLine(p[6], p[7]);
				viewport.drawLine(p[8], p[9]);
				viewport.drawLine(p[10], p[11]);
				viewport.drawLine(p[12], p[13]);
			}
		}
//		if (Animator.getInstance().getActiveObject() == camera) viewport.setColor(settings.cSelection);
//		else viewport.setColor(settings.cGrid);
//		g.drawRect((W - ww) >> 1, (H - hh) >> 1, ww, hh);
//		g.drawRect(((W - ww) >> 1) - 1, ((H - hh) >> 1) - 1, ww + 2, hh + 2);
//		g.setColor(settings.cText);
		
		viewport.drawString(camera.getName(), 8, 15);
		viewport.drawString("Position", 158, 15);
		viewport.drawString(number(camera.getPosition().x) + "/" + number(camera.getPosition().y) + "/" + number(camera.getPosition().z), 158, 30);
		viewport.drawString("Roll/Pitch/Yaw", 308, 15);
		viewport.drawString(number(camera.getRoll() * 180 / Math.PI) + "/" + number(camera.getPitch() * 180 / Math.PI) + "/" + number(camera.getYaw() * 180 / Math.PI), 308, 30);
		//g.drawString("Roll/Pitch/YAW: \t" + (int) (dRoll * 180 / Math.PI) + "/" + (int) (dPitch * 180 / Math.PI) + "/" + (int) (dYaw * 180 / Math.PI), 8, 64);
		viewport.drawString("Focal length", 458, 15);
		viewport.drawString("" + number(camera.getFocalLength()), 458, 30);
		
		AnimObject animObject = Animator.getInstance().getActiveObject();
		if (animObject != camera) {
			viewport.drawString(animObject.getName(), 9, H - 25);
			viewport.drawString("Position", 158, H - 25);
			viewport.drawString(number(animObject.getPosition().x) + "/" + number(animObject.getPosition().y) + "/" + number(animObject.getPosition().z), 158, H - 10);
			viewport.drawString("Roll/Pitch/Yaw", 308, H - 25);
			viewport.drawString(number(animObject.getRoll() * 180 / Math.PI) + "/" + number(animObject.getPitch() * 180 / Math.PI) + "/" + number(animObject.getYaw() * 180 / Math.PI), 308, H - 10);
			viewport.drawString("Scale", 458, H - 25);
			viewport.drawString("" + number(animObject.getScale()), 458, H - 10);
		}
		
		viewport.drawString("Frame " + (int) (Animator.getInstance().getPosition() - Animator.getInstance().getStart() + 1), ((W - ww) >> 1) + 3, ((H - hh) >> 1) + 13);
		viewport.drawString(MODE_NAME[iMode], ((W - ww) >> 1) + 3, ((H - hh) >> 1) + hh - 7);
		
//		for (int i = 0; i < aButtons.length; aButtons[i++].paint(g));
//		panel.getGraphics().drawImage(image, 0, 0, panel);
		
	}
	
	private String number(double n) {
		double d = (Math.round(n * 10.0)) / 10.0;
		return Double.toString(d);
	}
	
//	private void setColor(Color c) {
//		color = c.getRGB();
//	}
//
//	private void drawLine3D(Graphics2D g, Point3f p0, Point3f p1) {
//		if (p0.z < fNearClip) {
//			if (p1.z < fNearClip) return;
//			float s = (fNearClip - p0.z) / (p1.z - p0.z);
//			int x = (int) (p0.x * (1 - s) + p1.x * s);
//			int y = (int) (p0.y * (1 - s) + p1.y * s);
//			int xx = (int) (p1.x / p1.z);
//			int yy = (int) (p1.y / p1.z);
//			drawLine(x + X, y + Y, xx + X, yy + Y);
//		} else if (p1.z < fNearClip) {
//			float s = (fNearClip - p1.z) / (p0.z - p1.z);
//			int x = (int) (p1.x * (1 - s) + p0.x * s);
//			int y = (int) (p1.y * (1 - s) + p0.y * s);
//			int xx = (int) (p0.x / p0.z);
//			int yy = (int) (p0.y / p0.z);
//			drawLine(xx + X, yy + Y, x + X, y + Y);
//		} else {
//			int x = (int) (p0.x / p0.z);
//			int y = (int) (p0.y / p0.z);
//			int xx = (int) (p1.x / p1.z);
//			int yy = (int) (p1.y / p1.z);
//			drawLine(x + X, y + Y, xx + X, yy + Y);
//		}
//	}
//	
//	public final void drawLine(int x1, int y1, int x2, int y2) {
//		int x;
//		int y;
//		int end;
//		int edge;
//		int index;
//		if ((x1 < 0 && x2 < 0 )|| (y1 < 0 && y2 < 0) || (x1 >= iWidth && x2 >= iWidth) || (y1 >= iHeight && y2 >= iHeight)) {
//			return;
//		}
//		
//		int dx = x2 - x1;
//		int dy = y2 - y1;
//		if (dx == 0 && dy == 0) {
//			return;
//		}
//		if (Math.abs(dx) > Math.abs(dy)) {
//			if (dx > 0) {
//				x = x1;
//				y = y1 << 16 + 32768;
//				dy = (dy << 16) / dx;
//				end = (x2 < iWidth) ? x2 : iWidth;
//			} else {
//				x = x2;
//				y = y2 << 16 + 32768;
//				dy = (dy << 16) / dx;
//				end = (x1 < iWidth) ? x1 : iWidth;
//			}
//			if (x < 0) {
//				y -= dy * x;
//				x = 0;
//			}
//			edge = iHeight<<16;
//			while (x < end) {
//				if (y >= 0 && y < edge) {
//					index = iWidth * (y >> 16) + x;
//					aiActiveFrameBuffer[index] = color;
//					
//				}
//				x++;
//				y += dy;
//			}
//		} else {
//			if (dy > 0) {
//				y = y1;
//				x = x1 << 16 + 32768;
//				dx = (dx << 16) / dy;
//				end = (y2 < iHeight) ? y2 : iHeight;
//			} else {
//				y = y2;
//				x = x2 << 16 + 32768;
//				dx = (dx << 16) / dy;
//				end = (y1 < iHeight) ? y1 : iHeight;
//			}
//			if (y < 0) {
//				x -= dx * y;
//				y = 0;
//			}
//			edge = iWidth<<16;
//			while (y < end) {
//				if (x >= 0 && x < edge) {
//					index = iWidth * y + (x >> 16);
//					aiActiveFrameBuffer[index] = color;
//					
//				}
//				y++;
//				x += dx;
//			}
//		}
//	}
//	
	private void drawCurve3D(JPatchDrawable2 viewport, Point3f p0, Point3f p1, Point3f p2, Point3f p3) {
		Point3f pa = new Point3f(p0);
		Point3f pb = new Point3f();
		for (int t = 0; t < iCurveSubdiv - 1; t++) {
			pb.set(
				cB0[t] * p0.x + cB1[t] * p1.x + cB2[t] * p2.x + cB3[t] * p3.x,
				cB0[t] * p0.y + cB1[t] * p1.y + cB2[t] * p2.y + cB3[t] * p3.y,
				cB0[t] * p0.z + cB1[t] * p1.z + cB2[t] * p2.z + cB3[t] * p3.z
			);
			viewport.drawLine(pa, pb);
			pa.set(pb);
		}
		viewport.drawLine(pa, p3);
	}
	
//	private class VButton {
//		private int x, y, w, h;
//		private Image image;
//		private boolean pressed;		
//		
//		
//		private VButton(int x, int y, int width, int height, Image image) {
//			this.x = x;
//			this.y = y;
//			this.w = width;
//			this.h = height;
//			this.image = image;
//		}
//		
//		private void setPressedState(boolean state) {
//			pressed = state;
//						
//			Graphics g = panel.getGraphics();			
//				
//			// get the size of the button
//			int xx = (this.x < 0) ? panel.getWidth() + this.x : this.x;
//			int yy = (this.y < 0) ? panel.getHeight() + this.y : this.y;
//				
//			// draw a 3d button
//			g.setColor(new Color(0xffffff));
//			g.draw3DRect(xx, yy, w, h, !state );				
//
//		}
//		
//		private boolean isPressed() {
//			return pressed;
//		}
//		
//		private boolean isHit(int x, int y) {
//			int xx = (this.x < 0) ? panel.getWidth() + this.x : this.x;
//			int yy = (this.y < 0) ? panel.getHeight() + this.y : this.y;
//			return (x > xx && x < xx + w && y > yy && y < yy + h);
//		}
//		
//		private void paint(Graphics g) {
//			int xx = (x < 0) ? panel.getWidth() + x : x;
//			int yy = (y < 0) ? panel.getHeight() + y : y;
//			g.setColor(new Color(0xffffff));
//			g.drawImage(image, xx, yy, null);
//			g.draw3DRect(xx, yy, w, h, !pressed );
//		}
//	}
}

