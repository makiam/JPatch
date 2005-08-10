package jpatch.boundary;

import java.util.*;
import java.awt.*;

import javax.swing.*;
import javax.vecmath.*;
import jpatch.entity.*;
import jpatch.boundary.selection.*;
import jpatch.boundary.tools.*;

public class JPatchCanvas extends JPanel
implements Viewport {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final BoneShape[] ABONESHAPES = new BoneShape[0];
	
	private static final int GHOST = JPatchSettings.getInstance().iGhost;
	
	private static final int EVERYTHING = 0;
	private static final int BACKGROUND = 1;
	private static final int FOREGROUND = 2;
	
	private int iMode = EVERYTHING;
	
	//private Model model;
	private ViewDefinition viewDefinition;
	private JPatchDrawable drawable;
	private Image image;
	private Matrix4f m4View;
	private boolean bZBuffer = false;
	private int[] aiStaticFrameBuffer;
	private int[] aiStaticZBuffer;
	private JPatchTool tool;
	private Grid grid = new Grid();
	private boolean bFlat = true;
	private Lighting lighting = Lighting.createDefaultLight();
	private JPatchSettings settings = JPatchSettings.getInstance();
	
	private Font font = new Font("SansSerif",Font.PLAIN,12);
	
	private ArrayList listTransparentPatches = new ArrayList();
	private Image imageLock = (new ImageIcon(ClassLoader.getSystemResource("jpatch/images/viewlock.png"))).getImage();
	// remove !//
	//Rotoscope rotoscope = new Rotoscope("f:/zbuffer.png");
	//
	
	public JPatchCanvas(Model model, ViewDefinition viewDefinition) {
		//this.model = model;
		this.viewDefinition = viewDefinition;
//		viewDefinition.setViewport(this);
		m4View = viewDefinition.getMatrix();
		//drawable = new JPatchDrawableJava2D(createVolatileImage(getWidth(), getHeight()), lighting);
	}

	public void updateImage() {
		if (viewDefinition.renderPatches() || viewDefinition.alwaysUseZBuffer()) {
			drawable = new JPatchDrawableZBuffer(getGraphicsConfiguration().createCompatibleImage(getWidth(), getHeight()), lighting);
			aiStaticFrameBuffer = new int[getWidth() * getHeight()];
			aiStaticZBuffer = new int[getWidth() * getHeight()];
			bZBuffer = true;
		} else {
			drawable = new JPatchDrawableJava2D(createVolatileImage(getWidth(), getHeight()), lighting);
			bZBuffer = false;
		}
		image = drawable.getImage();
		render();
	}
	
	public ViewDefinition getViewDefinition() {
		return viewDefinition;
	}
	
	public JPatchDrawable getDrawable() {
		return drawable;
	}
	
	public Grid getGrid() {
		return grid;
	}
	
	public void flatShade(boolean flat) {
		bFlat = flat;
	}
	
	public void setLighting(Lighting lighting) {
		this.lighting = lighting;
		if (drawable != null) drawable.setLighting(lighting);
		//if (bZBuffer) {
		//	((JPatchDrawableZBuffer) drawable).setLighting(lighting);
		//}
	}
	
	public Lighting getLighting() {
		return lighting;
	}
	
	public void prepareBackground() {
		if (viewDefinition.renderPatches()) {
			PointSelection ps = MainFrame.getInstance().getPointSelection();
			if (ps != null) {
				for (Patch patch = MainFrame.getInstance().getModel().getFirstPatch(); patch != null; patch = patch.getNext()) {
					patch.check(ps);
				}
			}
			((ZBufferRenderer)drawable).setActiveBuffers(aiStaticFrameBuffer, aiStaticZBuffer);
			drawable.clear();
			renderPatches(BACKGROUND);
			iMode = FOREGROUND;
			((ZBufferRenderer)drawable).renderToImage();
		}
	}
	
	public void clearBackground() {
		iMode = EVERYTHING;
		;
	}
	
	public void setTool(JPatchTool tool) {
		
		//tool = new RotoscopeTool(rotoscope);
		this.tool = tool;
		//System.out.println("set tool " + tool);
		if (tool != null) {
			addMouseListener(tool);
		}
		repaint();
	}
	
	//public void updateComponent(Graphics g) {
	//	//if (image == null || image.getWidth(this) != getWidth() || image.getHeight(this) != getHeight()) {
	//	//	updateImage();
	//	//}
	//	//g.drawImage(image, 0, 0, this);
	//}
	
	public void clearImage() {
		image = null;
		viewDefinition.computeMatrix();
	}
	
	public void paintComponent(Graphics g) {
		//super.paintComponent(g);
		//System.out.print(COUNT++ + " JPatchCanvas " + num + " paintComponent() " + getWidth() + "x" + getHeight());
		if (image == null || image.getWidth(this) != getWidth() || image.getHeight(this) != getHeight()) {
		//	System.out.print(" updateImage()");
			updateImage();
		}
		//System.out.println();
		g.drawImage(image, 0, 0, this);
	}

	private void drawRotoscope() {
		Rotoscope rotoscope = MainFrame.getInstance().getModel().getRotoscope(viewDefinition.getView());
		if (rotoscope != null) {
			rotoscope.paint(this);
		}
	}
		
		
	private void drawGrid() {
		grid.paint(this);
		Graphics2D g2 = (Graphics2D)drawable.getGraphics();
		g2.setColor(settings.cBackground);
		g2.fillOval(-5,getHeight() - 85,90,90);
		g2.setColor(settings.cGridMin);
		g2.drawOval(-5,getHeight() - 85,90,90);
	}
	
	public void drawActiveViewportMarker(boolean active) {
		Graphics2D g2 = (Graphics2D)drawable.getGraphics();
		if (active) {
			g2.setColor(settings.cSelection);
		} else {
			g2.setColor(settings.cBackground);
		}
		g2.drawRect(0,0,getWidth() - 1,getHeight() - 1);
	}
	
	private void drawDisplayName() {
		Graphics2D g2 = (Graphics2D)drawable.getGraphics();
		g2.setColor(settings.cText);
		g2.setFont(font);
		g2.drawString(viewDefinition.getViewName(),7,15);
		if (bZBuffer) {
			g2.drawLine(getWidth() - 13,7,getWidth() - 6, 7);
			g2.drawLine(getWidth() - 13,14,getWidth() - 6, 14);
			g2.drawLine(getWidth() - 13,14,getWidth() - 6, 7);
		}
		if (grid.isSnapping()) {
			g2.drawLine(getWidth() - 8,20,getWidth() - 8,29);
			g2.drawLine(getWidth() - 11,20,getWidth() - 11,29);
			g2.drawLine(getWidth() - 5,23,getWidth() - 14,23);
			g2.drawLine(getWidth() - 5,26,getWidth() - 14,26);
		}
		if (viewDefinition.isLocked()) {
			g2.drawImage(imageLock,getWidth() - 12,34,null);
		}
	}
	
	private void drawOrigin() {
		float len = 16f/m4View.getScale();;
		Point3f p1 = new Point3f();
		Point3f p2 = new Point3f();
		p1.set(len,0,0);
		p2.set(-len,0,0);
		m4View.transform(p1);
		m4View.transform(p2);
		//p1.z -= 10000f;
		//p2.z -= 10000f;
		drawable.setColor(settings.cX);
		drawable.drawGhostLine3D(p1,p2,GHOST);
		p1.set(0,len,0);
		p2.set(0,-len,0);
		m4View.transform(p1);
		m4View.transform(p2);
		//p1.z -= 10000f;
		//p2.z -= 10000f;
		drawable.setColor(settings.cY);
		drawable.drawGhostLine3D(p1,p2,GHOST);
		p1.set(0,0,len);
		p2.set(0,0,-len);
		m4View.transform(p1);
		m4View.transform(p2);
		//p1.z -= 10000f;
		//p2.z -= 10000f;
		drawable.setColor(settings.cZ);
		drawable.drawGhostLine3D(p1,p2,GHOST);
	}
	
	private void renderHashPatches() {
		Vector3f v3a = new Vector3f();
		Vector3f v3b = new Vector3f();
		Vector3f[] normals = new Vector3f[] {new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};
		
		/* transform the lights */
		lighting.setRotation(viewDefinition.getRotateX(),viewDefinition.getRotateY());
		lighting.transform();
		
		for (int pass = 0; pass < 2; pass++) {
			for (Patch patch = MainFrame.getInstance().getModel().getFirstPatch(); patch != null; patch = patch.getNext()) {
				if (!patch.isHidden() && patch.getMaterial() != null) {
					MaterialProperties materialProperties = patch.getMaterial().getMaterialProperties();
					if ((pass == 0 && !materialProperties.isOpaque()) || pass == 1 && materialProperties.isOpaque()) continue;
					Point3f[] hashPatch = patch.coonsPatch();
					ControlPoint[] acp = patch.getControlPoints();
					for (int i = 0; i < hashPatch.length; i++) {
						m4View.transform(hashPatch[i]);
					}
					
					//if (hashPatch.length == 9) {
					//	int apex = -1;
					//	for (int i = 0; i < 6; i += 2) {
					//		if (apex == -1 && acp[i].getHead().getStack().length > 2) apex = i;
					//	}
					//	if (apex != -1) {
					//		//System.out.print("apex " + apex + "   " + "cp = " + acpPoint[apex] + "   ");
					//		apex = apex / 2;
					//		Patch.shift(hashPatch,apex * 3 + 3);
					//	}
					//}
					
					if (!bFlat) {
						
						
						
						/* set up corner normals */
						
						int[] levels = new int[patch.getType()];
						for (int i = 0; i < patch.getType(); levels[i++] = 0);
						for (int i = 0, n = patch.getType(), pl = hashPatch.length, cn = n * 2; i < n; i++) {
							ControlPoint targetHook = null;
							boolean reversePatch = false;
							int i2 = i * 2;
							if (acp[i2].isTargetHook()) {
								targetHook = acp[i2];
								levels[(i + n - 1) % n] = 1;
								//levels[i] = 1;
								//System.out.println("1");
							}
							if (acp[(i2 + cn - 1) % cn].isTargetHook()) {
								targetHook = acp[(i2 + cn - 1) % cn];
								levels[i] = 1;
								//System.out.println("2");
							}
							if (targetHook == null) {
								int p = i * 3;
								if (hashPatch[p].equals(hashPatch[(p + 3) % pl])) {
									v3a.sub(hashPatch[(p + 4) % pl], hashPatch[p]);
								} else  {
									v3a.sub(hashPatch[(p + 1) % pl], hashPatch[p]);
								}
								if (hashPatch[p].equals(hashPatch[(p + pl - 3) % pl])) {
									v3b.sub(hashPatch[(p + pl - 4) % pl], hashPatch[p]);
								} else {
									v3b.sub(hashPatch[(p + pl - 1) % pl], hashPatch[p]);
								}
								normals[i].cross(v3b, v3a);
								normals[i].normalize();
								//levels[i] = 1;
							}
							else {
								ControlPoint hook = targetHook.getHead();
								loop:
								for (int ii = 0; ii < acp.length; ii++) {
									if (acp[ii] == hook) {
										int ip = (ii + 1) % acp.length;
										int im = (ii + acp.length - 1) % acp.length;
										reversePatch = (acp[ii].getNext() == acp[ip] || acp[ii].getPrev() == acp[im]);
										break loop;
									}
								}
								
								Vector3f v3Dir = new Vector3f();
								Point3f p = targetHook.getPosition();
								if (targetHook.getNext() != null) v3Dir.sub(targetHook.getNext().getPosition(), p);
								else v3Dir.sub(targetHook.getPrev().getPosition(), p);
								Vector3f v3Start = new Vector3f(v3Dir);
								Vector3f v3End = new Vector3f(v3Dir);
								targetHook.computeTargetHookBorderTangents(v3Dir, v3Start, v3End);
								ControlPoint cpStart = targetHook.getHead().getStart().getParentHook();
								ControlPoint cpEnd = cpStart.getNext();
								Point3f p0 = cpStart.getPosition();
								Point3f p1 = cpStart.getOutTangent();
								Point3f p2 = cpEnd.getInTangent();
								Point3f p3 = cpEnd.getPosition();
								Vector3f v = new Vector3f();
								Vector3f n1 = new Vector3f();
								Vector3f n2 = new Vector3f();
								//System.out.println(cpStart + " " + cpEnd);
								//System.out.println(p0 + " " + p1 + " " + p2 + " " + p3);
								//System.out.println(v3Start + " " + v3End);
								v.sub(p1, p0);
								n1.cross(v3Start, v);
								n1.normalize();
								v.sub(p3, p2);
								n2.cross(v3End, v);
								n2.normalize();
								//System.out.println(n1 + " " + n2);
								Vector3f ncenter = JPatchDrawableZBuffer.interpolateNormal(n1, n2, p0, p1, p2, p3);
								
								//System.out.println(ncenter);
								//System.out.println();
								float hookpos = targetHook.getHead().getHookPos();
								
								//System.out.println(reversePatch + " " + hookpos);
								
								if (hookpos == 0.5f) v.set(ncenter);
								else if (hookpos == 0.25f) v = JPatchDrawableZBuffer.interpolateNormal(n1, ncenter);
								else v = JPatchDrawableZBuffer.interpolateNormal(ncenter, n2);
								//else if (hookpos == 0.25f ^ !reversePatch) {
								//	v = JPatchDrawableZBuffer.interpolateNormal(n1, ncenter);
								//	System.out.println("n1");
								//}
								//else {
								//	v = JPatchDrawableZBuffer.interpolateNormal(ncenter, n2);
								//	System.out.println("n2");
								//}
								m4View.transform(v);
								v.normalize();
								if (reversePatch) v.scale(-1f);
								normals[i].set(v);
							}
								
								//System.out.println(i);
								//normals[i].set(1,0,0);
								//Point3f P = targethook.getPosition();
								//m4View.transform(P);
								//drawable.drawPoint3D(P,5);
								//drawable.drawPoint3D(hashPatch[i * 3],7);
							
							//drawable.drawPoint3D(hashPatch[i], 2+i);
						}
							///* set up corner normals */
							//Vector3f[] cn = newNormals(4);
							//v3a.sub(ap3[1], ap3[0]);
							//v3b.sub(ap3[8], ap3[0]);
							//cn[0].cross(v3b, v3a);
							//v3a.sub(ap3[4], ap3[3]);
							//v3b.sub(ap3[2], ap3[3]);
							//cn[1].cross(v3b, v3a);
							//v3a.sub(ap3[7], ap3[6]);
							//v3b.sub(ap3[5], ap3[6]);
							//cn[2].cross(v3b, v3a);
							//cn[0].normalize();
							//cn[1].normalize();
							//cn[2].normalize();
							//cn[3].set(cn[0]);
						
						//System.out.println(patch + " " + hashPatch.length);
						//if (hashPatch.length == 12) {
						drawable.drawHashPatchGourad(hashPatch, normals, levels, materialProperties);
						//}
					} else {
						int[] levels = new int[patch.getType()];
						for (int i = 0; i < patch.getType(); levels[i++] = 0);
						for (int i = 0, n = patch.getType(), cn = n * 2; i < n; i++) {
							int i2 = i * 2;
							if (acp[i2].isTargetHook()) {
								levels[(i + n - 1) % n] = 1;
								//levels[i] = 1;
								//System.out.println("1");
							}
							if (acp[(i2 + cn - 1) % cn].isTargetHook()) {
								levels[i] = 1;
								//System.out.println("2");
							}
						}
						drawable.drawHashPatchFlat(hashPatch, levels, materialProperties);
					}
				}
			}
		}
	}
	
	private void renderPatches(int mode) {
		if (!bFlat) {
			lighting.setRotation(viewDefinition.getRotateX(),viewDefinition.getRotateY());
			lighting.transform();
		}
		//switch (mode) {
		listTransparentPatches.clear();
		
		/* opaque pass */
		switch (EVERYTHING) {
			case EVERYTHING:
				for (Patch patch = MainFrame.getInstance().getModel().getFirstPatch(); patch != null; patch = patch.getNext()) {
					if (!patch.isHidden()) {
						MaterialProperties materialProperties = patch.getMaterial().getMaterialProperties();
						if (materialProperties.isOpaque()) {
							Point3f[][] bicubicPatches = patch.bicubicPatches();
							for (int p = 0; p < bicubicPatches.length; p++) {
								Point3f[] bezierCPs = bicubicPatches[p];
								for (int i = 0; i < bezierCPs.length; i++) {
									m4View.transform(bezierCPs[i]);
								}
								if (bFlat) {
									drawable.drawBicubicPatchFlat(bezierCPs,materialProperties);
								} else {
									drawable.drawBicubicPatchGourad(bezierCPs,materialProperties);
								}
							}
						} else {
							listTransparentPatches.add(patch);
						}
					}
				}
			break;
			case BACKGROUND:
				drawGrid();
				for (Patch patch = MainFrame.getInstance().getModel().getFirstPatch(); patch != null; patch = patch.getNext()) {
					if (!patch.isHidden() && !patch.isDynamic()) {
						MaterialProperties materialProperties = patch.getMaterial().getMaterialProperties();
						if (materialProperties.isOpaque()) {
							Point3f[][] bicubicPatches = patch.bicubicPatches();
							for (int p = 0; p < bicubicPatches.length; p++) {
								Point3f[] bezierCPs = bicubicPatches[p];
								for (int i = 0; i < bezierCPs.length; i++) {
									m4View.transform(bezierCPs[i]);
								}
								if (bFlat) {
									drawable.drawBicubicPatchFlat(bezierCPs,materialProperties);
								} else {
									drawable.drawBicubicPatchGourad(bezierCPs,materialProperties);
								}
							}
						} else {
							listTransparentPatches.add(patch);
						}
					}
				}
			break;
			case FOREGROUND:
				for (Patch patch = MainFrame.getInstance().getModel().getFirstPatch(); patch != null; patch = patch.getNext()) {
					if (!patch.isHidden() && patch.isDynamic()) {
						MaterialProperties materialProperties = patch.getMaterial().getMaterialProperties();
						if (materialProperties.isOpaque()) {
							Point3f[][] bicubicPatches = patch.bicubicPatches();
							for (int p = 0; p < bicubicPatches.length; p++) {
								Point3f[] bezierCPs = bicubicPatches[p];
								for (int i = 0; i < bezierCPs.length; i++) {
									m4View.transform(bezierCPs[i]);
								}
								if (bFlat) {
									drawable.drawBicubicPatchFlat(bezierCPs,materialProperties);
								} else {
									drawable.drawBicubicPatchGourad(bezierCPs,materialProperties);
								}
							}
						} else {
							listTransparentPatches.add(patch);
						}
					}
				}
			break;
		}
		
		/* transparent pass */
		for (Iterator it = listTransparentPatches.iterator(); it.hasNext(); ) {
			Patch patch = (Patch) it.next();
			MaterialProperties materialProperties = patch.getMaterial().getMaterialProperties();
			Point3f[][] bicubicPatches = patch.bicubicPatches();
			for (int p = 0; p < bicubicPatches.length; p++) {
				Point3f[] bezierCPs = bicubicPatches[p];
				for (int i = 0; i < bezierCPs.length; i++) {
					m4View.transform(bezierCPs[i]);
				}
				if (bFlat) {
					drawable.drawBicubicPatchFlat(bezierCPs,materialProperties);
				} else {
					drawable.drawBicubicPatchGourad(bezierCPs,materialProperties);
				}
			}
		}
	}
	
	private void renderBones() {
		//Point3f p3A = new Point3f();
		//Point3f p3B = new Point3f();
		//drawable.setColor(new Color(255,255,255));
		//BoneShape boneShape = new BoneShape();
		//for (Bone bone = MainFrame.getInstance().getModel().getFirstBone(); bone != null; bone = bone.getNext()) {
		//	//p3A.set(bone.getStart());
		//	//p3B.set(bone.getEnd());
		//	//m4View.transform(p3A);
		//	//m4View.transform(p3B);
		//	//drawable.drawLine3D(p3A,p3B);
		//	boneShape.setBone(bone);
		//	//boneShape.reset();
		//	drawable.drawSimpleShape(boneShape, m4View);
		//}
		BoneShape[] boneShapes = (BoneShape[]) MainFrame.getInstance().getModel().getBoneShapeList().toArray(ABONESHAPES);
		for (int i = 0; i < boneShapes.length; i++) {
			boneShapes[i].set();
			boneShapes[i].transform(m4View);
			//System.out.println(boneShapes[i]);
		}
		Arrays.sort(boneShapes);
		for (int i = 0; i < boneShapes.length; i++) {
			drawable.drawSimpleShape(boneShapes[i], null);
		}
		//for (Iterator it = boneShapes.iterator(); it.hasNext(); ) {
		//	BoneShape boneShape = (BoneShape) it.next();
		//	boneShape.set();
		//	boneShape.transform(m4View);
		//}
		////Collections.sort(boneShapes);
		//for (Iterator it = boneShapes.iterator(); it.hasNext(); ) {
		//	drawable.drawSimpleShape((BoneShape) it.next(), null);
		//}
	}
			
	private void paintAxis() {
		Matrix3f m3 = new Matrix3f();
		m4View.get(m3);
		Vector3f v3x = new Vector3f(30,0,0);
		Vector3f v3y = new Vector3f(0,30,0);
		Vector3f v3z = new Vector3f(0,0,30);
		m3.transform(v3x);
		m3.transform(v3y);
		m3.transform(v3z);
		int cx = 40;
		int cy = getHeight() - 40;
		int X,Y;
		float[] rgbx = new float[3];
		float[] rgby = new float[3];
		float[] rgbz = new float[3];
		
		settings.cX.getRGBColorComponents(rgbx);
		settings.cY.getRGBColorComponents(rgby);
		settings.cZ.getRGBColorComponents(rgbz);
		
		SimpleShape arrowX = SimpleShape.createArrow(9,3,rgbx[0],rgbx[1],rgbx[2]);
		SimpleShape arrowY = SimpleShape.createArrow(9,3,rgby[0],rgby[1],rgby[2]);
		SimpleShape arrowZ = SimpleShape.createArrow(9,3,rgbz[0],rgbz[1],rgbz[2]);
		Matrix3f rot = new Matrix3f();
		rot.rotX((float)Math.PI);
		arrowZ.transform(rot);
		arrowZ.translate(new Vector3f(0,0,31));
		rot.rotX((float)Math.PI / 2);
		arrowY.transform(rot);
		arrowY.translate(new Vector3f(0,31,0));
		rot.rotY(-(float)Math.PI / 2);
		arrowX.transform(rot);
		arrowX.translate(new Vector3f(31,0,0));
		arrowX.transform(m3);
		arrowY.transform(m3);
		arrowZ.transform(m3);
		arrowX.translate(new Vector3f(cx,cy,-10000));
		arrowY.translate(new Vector3f(cx,cy,-10000));
		arrowZ.translate(new Vector3f(cx,cy,-10000));
		
		if (v3x.z > v3y.z && v3y.z > v3z.z) {
			X = 0;
			Y = 1;
		} else if (v3x.z > v3z.z && v3z.z > v3y.z) {
			X = 0;
			Y = 2;
		} else if (v3y.z > v3x.z && v3x.z > v3z.z) {
			Y = 0;
			X = 1;
		} else if (v3z.z > v3x.z && v3x.z > v3y.z) {
			X = 1;
			Y = 2;
		} else if (v3y.z > v3z.z && v3z.z > v3x.z) {
			Y = 0;
			X = 2;
		} else {
			Y = 1;
			X = 2;
		}
		for (int i = 0; i < 3; i++) {
			if (i == X) {
				drawable.setColor(settings.cX);
				drawable.drawLine(cx,cy,cx + (int)v3x.x, cy + (int)v3x.y);
				v3x.scale(1.3f);
				//g2.setColor(new Color(255,64,64));
				drawable.drawLine(cx + (int)v3x.x - 3, cy + (int)v3x.y - 3, cx + (int)v3x.x + 3, cy + (int)v3x.y + 3);
				drawable.drawLine(cx + (int)v3x.x - 3, cy + (int)v3x.y + 3, cx + (int)v3x.x + 3, cy + (int)v3x.y - 3);
				drawable.drawSimpleShape(arrowX,null);
			} else if (i == Y) {
				drawable.setColor(settings.cY);
				drawable.drawLine(cx,cy,cx + (int)v3y.x, cy + (int)v3y.y);
				v3y.scale(1.3f);
				//g2.setColor(new Color(32,255,32));
				drawable.drawLine(cx + (int)v3y.x, cy + (int)v3y.y, cx + (int)v3y.x - 3, cy + (int)v3y.y - 3);
				drawable.drawLine(cx + (int)v3y.x, cy + (int)v3y.y, cx + (int)v3y.x + 3, cy + (int)v3y.y - 3);
				drawable.drawLine(cx + (int)v3y.x, cy + (int)v3y.y, cx + (int)v3y.x, cy + (int)v3y.y + 3);
				drawable.drawSimpleShape(arrowY,null);
			} else {
				drawable.setColor(settings.cZ);
				drawable.drawLine(cx,cy,cx + (int)v3z.x, cy + (int)v3z.y);
				v3z.scale(1.3f);
				//drawable.setColor(new Color(64,64,255));
				drawable.drawLine(cx + (int)v3z.x - 3, cy + (int)v3z.y - 3, cx + (int)v3z.x + 3, cy + (int)v3z.y - 3);
				drawable.drawLine(cx + (int)v3z.x - 3, cy + (int)v3z.y + 3, cx + (int)v3z.x + 3, cy + (int)v3z.y + 3);
				drawable.drawLine(cx + (int)v3z.x - 3, cy + (int)v3z.y + 3, cx + (int)v3z.x + 3, cy + (int)v3z.y - 3);
				drawable.drawSimpleShape(arrowZ,null);
			}
		}
		//g2.setStroke(new BasicStroke(1));
	}
	
	public void drawBezier() {
		drawable.setColor(Color.GREEN);
		for (Patch patch = MainFrame.getInstance().getModel().getFirstPatch(); patch != null; patch = patch.getNext()) {
			Point3f[][] bicubicPatches = patch.bicubicPatches();
			for (int b = 0; b < bicubicPatches.length; b++) {
				Point3f[] bezierCP = bicubicPatches[b];
				for (int p = 0; p < 16; m4View.transform(bezierCP[p++]));
				drawable.drawLine3D(bezierCP[0],bezierCP[1]);
				drawable.drawLine3D(bezierCP[1],bezierCP[2]);
				drawable.drawLine3D(bezierCP[2],bezierCP[3]);
				
				drawable.drawLine3D(bezierCP[4],bezierCP[5]);
				drawable.drawLine3D(bezierCP[5],bezierCP[6]);
				drawable.drawLine3D(bezierCP[6],bezierCP[7]);
				
				drawable.drawLine3D(bezierCP[8],bezierCP[9]);
				drawable.drawLine3D(bezierCP[9],bezierCP[10]);
				drawable.drawLine3D(bezierCP[10],bezierCP[11]);
				
				drawable.drawLine3D(bezierCP[12],bezierCP[13]);
				drawable.drawLine3D(bezierCP[13],bezierCP[14]);
				drawable.drawLine3D(bezierCP[14],bezierCP[15]);
				
				drawable.drawLine3D(bezierCP[0],bezierCP[4]);
				drawable.drawLine3D(bezierCP[1],bezierCP[5]);
				drawable.drawLine3D(bezierCP[2],bezierCP[6]);
				drawable.drawLine3D(bezierCP[3],bezierCP[7]);
				
				drawable.drawLine3D(bezierCP[4],bezierCP[8]);
				drawable.drawLine3D(bezierCP[5],bezierCP[9]);
				drawable.drawLine3D(bezierCP[6],bezierCP[10]);
				drawable.drawLine3D(bezierCP[7],bezierCP[11]);
				
				drawable.drawLine3D(bezierCP[8],bezierCP[12]);
				drawable.drawLine3D(bezierCP[9],bezierCP[13]);
				drawable.drawLine3D(bezierCP[10],bezierCP[14]);
				drawable.drawLine3D(bezierCP[11],bezierCP[15]);
				
				//drawable.drawPoint3D(bezierCP[9],2);
			}
		}
	}
			
	public void render() {
		//System.out.println("\n" + COUNT++ + " JPatchCanvas " + num + " render()");
		Model model = MainFrame.getInstance().getModel();
		//System.out.println("repaint " + count++);
		if (drawable != null && image != null) {
			long start = System.currentTimeMillis();
			if (!bZBuffer || iMode == EVERYTHING) {
				drawable.clear();
				if (viewDefinition.showRotoscope()) {
					drawRotoscope();
				}
				drawGrid();
			} else {
				((ZBufferRenderer)drawable).reset(aiStaticFrameBuffer, aiStaticZBuffer);
			}
			if (iMode != BACKGROUND) {
				if (viewDefinition.renderCurves()) {
					drawable.setColor(settings.cCurve);
					//long start = System.currentTimeMillis();
					//for (int i = 0; i < 100; i++) {
					for(Curve curve = model.getFirstCurve(); curve != null; curve = curve.getNext()) {
						if (!curve.getStart().isStartHook()) {
							drawable.drawJPatchCurve3D(curve, m4View);
						}
					}
					//}
					//System.out.println(System.currentTimeMillis() - start);
				}
				if (viewDefinition.renderPoints()) {
					
					Graphics2D g2 = (Graphics2D)drawable.getGraphics();
					g2.setColor(settings.cText);
					g2.setFont(font);
							
					PointSelection ps = MainFrame.getInstance().getPointSelection();
					Point3f p = new Point3f();
					for(Curve curve = model.getFirstCurve(); curve != null; curve = curve.getNext()) {
						for(ControlPoint cp = curve.getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
							if (cp.isHead()) {
								p.set(cp.getPosition());
								m4View.transform(p);
								if (ps != null && ps.contains(cp)) {
									drawable.setColor(settings.cSelected);
									drawable.drawPoint3D(p,1);
								} else if (!cp.isHook() && ! cp.isHidden()){
									if (cp.isSingle()) {
										drawable.setColor(settings.cPoint);
										drawable.drawXPoint3D(p);
									} else if (!cp.isMulti()) {
										drawable.setColor(settings.cHeadPoint);
										drawable.drawPoint3D(p,1);
									} else {
										drawable.setColor(settings.cMultiPoint);
										drawable.drawPoint3D(p,2);
									}
									//if (cp.isHidden()) drawable.setColor(Color.BLUE);
									
								}
								//if (cp.getHookPos() != 0 && cp.getHookPos() != 1) {
								//	g2.drawString("" + cp.number(),(int) p.x,(int) p.y);
								//}
								///* render Tangents... */
								//
								//
								//Point3f p3OutTangent = cp.getOutTangent();
								//Point3f p3InTangent = cp.getInTangent();
								//drawable.setColor(settings.cSelection);
								//if (p3OutTangent != null) {
								//	Point3f pt = new Point3f(p3OutTangent);
								//	m4View.transform(pt);
								//	drawable.drawLine3D(p,pt);
								//}
								//if (p3InTangent != null) {
								//	Point3f pt = new Point3f(p3InTangent);
								//	m4View.transform(pt);
								//	drawable.drawLine3D(p,pt);
								//}
								
							}
							//else if (cp.number() == 31 || cp.number() == 29 || cp.number() == 256 || cp.number() == 302) {
							//	p.set(cp.getPosition());
							//	m4View.transform(p);
							//	g2.drawString("" + cp.number(),(int) p.x + 4,(int) p.y + 12);
							//}
							
							
						}
					}
					if (ps != null && ps.getHotCp() != null) {
						if (ps.getHotCp().getCurve() != null && ps.getHotCp().getCurve().getModel() != null) {
							p.set(ps.getHotCp().getPosition());
							m4View.transform(p);
							drawable.setColor(settings.cHot);
							drawable.drawPoint3D(p,2);
						}
					}
					if (ps != null && ps.isCurve()) {
						ControlPoint cp = ps.getControlPoint();
						if (ps.getDirection() && cp.getNext() != null) {
							Point3f p3A = new Point3f(cp.getPosition());
							Point3f p3B = new Point3f(cp.getOutTangent());
							Point3f p3C = new Point3f(cp.getNext().getInTangent());
							Point3f p3D = new Point3f(cp.getNext().getPosition());
							m4View.transform(p3A);
							m4View.transform(p3B);
							m4View.transform(p3C);
							m4View.transform(p3D);
							drawable.setColor(settings.cSelected);
							drawable.drawCurveSegment(p3A,p3B,p3C,p3D);
						} else if (!ps.getDirection() && cp.getPrev() != null) {
							Point3f p3A = new Point3f(cp.getPrev().getPosition());
							Point3f p3B = new Point3f(cp.getPrev().getOutTangent());
							Point3f p3C = new Point3f(cp.getInTangent());
							Point3f p3D = new Point3f(cp.getPosition());
							m4View.transform(p3A);
							m4View.transform(p3B);
							m4View.transform(p3C);
							m4View.transform(p3D);
							drawable.setColor(settings.cSelected);
							drawable.drawCurveSegment(p3A,p3B,p3C,p3D);
						}
					}
				}
				
				if (viewDefinition.renderPatches()) {
					if (bZBuffer) {
						//renderPatches(iMode);
						renderHashPatches();
					} else {
						updateImage();
						return;
					}
				}
				if (viewDefinition.renderBezierCPs()) {
					drawBezier();
				}
				
				renderBones();
				
				//paintTest();
				if (tool != null) tool.paint(this, drawable);
//				drawActiveViewportMarker(MainFrame.getInstance().getJPatchScreen().getActiveViewport() == this);
				paintAxis();
				drawOrigin();
				drawDisplayName();
				
				Graphics2D g2 = (Graphics2D)drawable.getGraphics();
				
				/*
				 * output milliseconds
				 */
				g2.setColor(settings.cText);
				g2.setFont(font);
				g2.drawString("" + (System.currentTimeMillis() - start) +"ms", 7,30);
				
				/*
				 * copy image
				 */
				getGraphics().drawImage(image, 0, 0, this);
				
			}
		}
	}
}

