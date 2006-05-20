/*
 * $Id: Viewport2.java,v 1.60 2006/05/20 10:15:44 sascha_l Exp $
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jpatch.boundary;

import java.awt.Graphics2D;
import java.util.*;

import javax.vecmath.*;

import jpatch.auxilary.*;
import jpatch.boundary.settings.Settings;
import jpatch.boundary.tools.*;
import jpatch.entity.*;
import jpatch.entity.Bone.BoneTransformable;

/**
 * @author sascha
 *
 */
public class Viewport2 {
//	private static int iCurveSubdiv = 5;
//	
//	private static float[] cB0;
//	private static float[] cB1;
//	private static float[] cB2;
//	private static float[] cB3;
//	
//	private static Point3f pa = new Point3f();
//	private static Point3f pb = new Point3f();
	private static Point3f p0 = new Point3f();
	private static Point3f p1 = new Point3f();
	private static Point3f p2 = new Point3f();
	private static Point3f p3 = new Point3f();
	
	public static final float OVERSCAN = 1.2f;
	
	private JPatchDrawable2 drawable;
	private ViewDefinition viewDef;
	private Settings settings = Settings.getInstance();
	
	private Matrix4f m4View = new Matrix4f();
//	private JPatchTool tool;
	
	private static float fFlatness;
	//private static float fFlatnessSquared = fFlatness * fFlatness;
	private static int iMaxSubdiv = 10;
	
//	ControlPoint cp;
//	Point3f p3A = new Point3f();
//	Point3f p3B = new Point3f();
//	Point3f p3C = new Point3f();
//	Point3f p3D = new Point3f();
	
	private static Vector3f v3_a = new Vector3f();
	private static Vector3f v3_b = new Vector3f();
	private static Vector3f v3a = new Vector3f();
	private static Vector3f v3b = new Vector3f();
	private static Vector3f v3c = new Vector3f();
	
//	private Grid grid = new Grid();
	private BoneRenderer boneRenderer = new BoneRenderer();
	
	private float fMinZ, fDeltaZ;
	
	static {
		setQuality(Settings.getInstance().realtimeRenderer.realtimeRenererQuality);
	}
	
//	private static void init() {
//		cB0 = new float[iCurveSubdiv];
//		cB1 = new float[iCurveSubdiv];
//		cB2 = new float[iCurveSubdiv];
//		cB3 = new float[iCurveSubdiv];
//		for (int i = 0; i < iCurveSubdiv; i++) {
//			float s = (float) i / (float) (iCurveSubdiv - 1);
//			cB0[i] = (1 - s) * (1 - s) * (1 - s);
//			cB1[i] = 3 * s * (1 - s) * (1 - s);
//			cB2[i] = 3 * s * s * (1 - s);
//			cB3[i] = s * s * s;
//		}
//	}
	
	public static void setQuality(int q) {
		if (q == 0) fFlatness = 16;
		else if (q == 1) fFlatness = 8;
		else if (q == 2) fFlatness = 4;
		else if (q == 3) fFlatness = 2;
		else if (q == 4) fFlatness = 1;
		else
			throw new IllegalArgumentException("Quality must be between 0 and 4, is " + q);
	}
	
	public Viewport2(JPatchDrawable2 drawable, ViewDefinition viewDefinition) {
		this.drawable = drawable;
		this.viewDef = viewDefinition;
		setQuality(settings.realtimeRenderer.realtimeRenererQuality);
	}
	
	public void setModelMatrix(Matrix4d matrix) {
		Matrix4f m = new Matrix4f(matrix);
		m4View.set(viewDef.getMatrix());
		m4View.mul(m);
	}
	
	public void prepare() {
		if (drawable.isTransformSupported())
			drawable.setTransform(viewDef.getScreenMatrix());
		else
			m4View.set(viewDef.getMatrix());
		drawable.clear(JPatchDrawable2.COLOR_BUFFER | JPatchDrawable2.DEPTH_BUFFER, settings.colors.background); // FIXME
		if (drawable.isLightingSupported()) {
			RealtimeLighting rtl = viewDef.getLighting();
			if (rtl != null) {
				Matrix4f identity = new Matrix4f();
				identity.setIdentity();
				if (!MainFrame.getInstance().getJPatchScreen().isStickyLight())
					rtl.transform(viewDef.getMatrix());
				else
					rtl.transform(identity);
			}
			drawable.setLighting(rtl);
		}
		if (viewDef.getCamera() != null)
			drawable.setFocalLength(viewDef.getCamera().getFocalLength() / OVERSCAN);
	}
	
	public ViewDefinition getViewDefinition() {
		return viewDef;
	}
	
	public JPatchDrawable2 getDrawable() {
		return drawable;
	}
	
//	public Grid getGrid() {
//		return grid;
//	}
	
	public void drawInfo() {
		String[] info = drawable.getInfo().split("\n");
		drawable.setColor(settings.colors.text);
		drawable.drawString(viewDef.getViewDescription(), 4, 16);
		for (int i = 0, y = 16; i < info.length; drawable.drawString(info[i++], 4, y += 16));
		if (MainFrame.getInstance().getEditedMorph() != null)
			drawable.drawString("!!!EDIT MORPH MODE!!!", (int) viewDef.getWidth() - 140, 16);
		if (MainFrame.getInstance().getAnimation() != null)
			drawable.drawString("Frame " + (int) MainFrame.getInstance().getAnimation().getPosition(), 4, (int) viewDef.getHeight() - 4);
		drawable.drawString(Long.toString(System.currentTimeMillis()), 8, 48);
		if (viewDef.getCamera() != null) {
			int W = (int) viewDef.getWidth();
			int H = (int) viewDef.getHeight();
//			float w;
			int hh, ww;
			float ar = settings.export.aspectWidth / settings.export.aspectHeight;
//			if (W / ar < H) {
				ww = (int) (W / OVERSCAN);
				hh = (int) (ww / ar);
//				w = (float) viewDef.getCamera().getFocalLength() * (float) W / OVERSCAN / (float) viewDef.getCamera().getFilmSize();
//			} else {
//				hh = (int) (H / OVERSCAN);
//				ww = (int) (hh * ar);
//				w = (float) viewDef.getCamera().getFocalLength() * (float) H / OVERSCAN / (float) (viewDef.getCamera().getFilmSize() / ar);
//			}
			
			drawable.setTransparentRenderingMode(JPatchDrawable2.TRANSPARENT);
			drawable.setColor(new Color4f(1, 1, 1, 0.25f));
			int left = (W - ww) >> 1;
			int top = (H - hh) >> 1;
			drawable.drawRect(left, top, ww, hh);
			drawable.drawRect(left - 1, top - 1, ww + 2, hh + 2);
			
//			drawable.setColor(settings.colors.minorGrid);
			drawable.drawLine(left + ww / 3, top + 1, left + ww / 3, top + hh);
			drawable.drawLine(left + ww / 2, top + 1, left + ww / 2, top + hh);
			drawable.drawLine(left + ww * 2 / 3, top + 1, left + ww * 2 / 3, top + hh);
			drawable.drawLine(left + 1, top + hh / 3, left + ww, top + hh / 3);
			drawable.drawLine(left + 1, top + hh / 2, left + ww, top + hh / 2);
			drawable.drawLine(left + 1, top + hh * 2/ 3, left + ww, top + hh * 2 / 3);
			drawable.setTransparentRenderingMode(JPatchDrawable2.OFF);
		}
		
		
		
	}
	
	public void drawTest() {
		/*
		 * test
		 */
		
		Matrix3f m3 = new Matrix3f();
		Matrix3f m = new Matrix3f();
		Point3f offset = new Point3f();
		m3.setIdentity();
//		drawTest(m3, 10);
		float alpha = (float) Math.PI / 4;
		m.rotX(alpha);
		m3.mul(m);
		offset.set(100,0,0);
		drawTest(m3, 10, offset);
		m.rotY(alpha);
		m3.mul(m);
		offset.set(200,0,0);
		drawTest(m3, 10, offset);
		m.rotZ(alpha);
		m3.mul(m);
		offset.set(300,0,0);
		drawTest(m3, 10, offset);
	}
	
	private void drawTest(Matrix3f mm, float s, Point3f offset) {
		Point3f p30 = new Point3f(0,0,0);
		Point3f p3x = new Point3f(s,0,0);
		Point3f p3y = new Point3f(0,s,0);
		Point3f p3z = new Point3f(0,0,s);
		mm.transform(p30);
		mm.transform(p3x);
		mm.transform(p3y);
		mm.transform(p3z);
		viewDef.getMatrix().transform(p30);
		viewDef.getMatrix().transform(p3x);
		viewDef.getMatrix().transform(p3y);
		viewDef.getMatrix().transform(p3z);
		p30.add(offset);
		p3x.add(offset);
		p3y.add(offset);
		p3z.add(offset);
		
		drawable.setColor(settings.colors.xAxis);
		drawable.drawLine(p30, p3x);
		drawable.setColor(settings.colors.yAxis);
		drawable.drawLine(p30, p3y);
		drawable.setColor(settings.colors.zAxis);
		drawable.drawLine(p30, p3z);

		

	}
	
	public void drawActiveBorder() {
		drawable.setColor(new Color3f(settings.colors.selection));
		drawable.drawRect(0, 0, (int) viewDef.getWidth() - 1, (int) viewDef.getHeight() - 1);
	}
	
	public void setTool(JPatchTool tool) {
//		this.tool = tool;
		if (tool != null)
			drawable.getComponent().addMouseListener(tool);
	}
	
	private void setFogColor(float z, Color3f colorIn, Color3f colorOut) {
		if (settings.realtimeRenderer.wireframeFogEffect) {
			colorOut.interpolate(colorIn, settings.colors.background, (z - fMinZ) / fDeltaZ * 0.75f);
			colorOut.clamp(0, 1);
		} else {
			colorOut.set(colorIn);
		}
	}
	
	public void drawRotoscope() {
		Rotoscope rotoscope = MainFrame.getInstance().getModel().getRotoscope(viewDef.getView());
		if (rotoscope != null) {
			rotoscope.paint(viewDef);
		}
	}
	
	public void drawGrid() {
		MainFrame.getInstance().getJPatchScreen().getGrid().paint(viewDef);
	}
	
	public void drawTool(JPatchTool tool) {
		Selection selection = MainFrame.getInstance().getSelection();
		if (selection != null && selection.getHotObject() != null && selection.getHotObject() == viewDef.getCamera())
			return;
		drawable.setGhostRenderingEnabled(true);
		tool.paint(viewDef);
		drawable.setGhostRenderingEnabled(false);
		if (drawable instanceof JPatchDrawableGL)
			tool.paint(viewDef);
	}
	
	private Color3f solidColor = new Color3f();
	private Color4f solidColor4 = new Color4f();
	
	private void drawBones(Model model) {
		for (Iterator it = model.getBoneSet().iterator(); it.hasNext(); ) {
			Bone bone = (Bone) it.next();
			boneRenderer.drawBone(drawable, viewDef, bone);
		}
	}
	
	public void drawAnimFrame(Animation animation) {
		for (AnimObject animObject:animation.getObjects()) {
			if (animObject == viewDef.getCamera())
				continue;
			if (animObject instanceof AnimLight) {
				animObject.getModel().getMaterial(0).setColor(((AnimLight) animObject).getColor());
			}
			setModelMatrix(animObject.getTransform());
			drawModel(animObject.getModel());
		}
	}
	
	public void drawModel(Model model) {
//		System.out.println(this + " drawModel");
		/*
		 * get max and min z-values (for fog effect)
		 */
		fMinZ = Short.MAX_VALUE;
		fDeltaZ = Short.MIN_VALUE;
		Point3f pz = new Point3f();
		for (Iterator it = model.getCurveSet().iterator(); it.hasNext(); ) {
			for(ControlPoint cp = (ControlPoint) it.next(); cp != null; cp = cp.getNextCheckNextLoop()) {
				if (cp.isHead() && !cp.isHidden()) {
					pz.set(cp.getPosition());
					m4View.transform(pz);
					if (pz.z > fDeltaZ)
						fDeltaZ = pz.z;
					if (pz.z < fMinZ)
						fMinZ = pz.z;
				}
			}
		}
		fDeltaZ -= fMinZ;
		fDeltaZ += 50;
		//fMinZ -= 1;
		
		Selection selection = MainFrame.getInstance().getSelection();
		
		if (viewDef.renderCurves()) {
			drawable.setColor(settings.colors.curves); // FIXME
			for (Iterator it = model.getCurveSet().iterator(); it.hasNext(); ) {
				ControlPoint start = (ControlPoint) it.next();
				if (!start.isStartHook())
					drawCurve(start);
			}
		}
		Color3f cSelected = settings.colors.selectedPoints;
		Color3f cPoint = settings.colors.points;
		Color3f cHeadPoint = settings.colors.headPoints;
		Color3f cMultiPoint = settings.colors.multiPoints;
		Color3f color = new Color3f();
		Color3f cWeight = new Color3f();
		Color3f cBlack = new Color3f(0,0,0);
		if (viewDef.renderPoints()) {
			drawable.setPointSize(3);
			for (Iterator it = model.getCurveSet().iterator(); it.hasNext(); ) {
				for(ControlPoint cp = (ControlPoint) it.next(); cp != null; cp = cp.getNextCheckNextLoop()) {
					if (cp.isHead()) {
						p0.set(cp.getPosition());
						if (!drawable.isTransformSupported()) 
							m4View.transform(p0);
						float w = 0;
						Float weight;
						if (selection != null && (weight = (Float) selection.getMap().get(cp)) != null) {
							w = ((Float) weight).floatValue();
							cWeight.interpolate(cBlack, cSelected, w);
//							System.out.println(cp + " " + w + " " + cWeight);
							setFogColor(p0.z, cWeight, color);
							drawable.setColor(color);
							drawable.drawPoint(p0);
						} else if (!cp.isHook() && ! cp.isHidden()){
							if (cp.getBone() != null) {
								drawable.setColor(cp.getBone().getColor());
								drawable.drawPoint(p0);
							} else if (cp.isSingle()) {
								setFogColor(p0.z, cPoint, color);
								drawable.setColor(color);
								drawable.drawPoint(p0);
							} else if (!cp.isMulti()) {
								setFogColor(p0.z, cHeadPoint, color);
								drawable.setColor(color);
								drawable.drawPoint(p0);
							} else {
								setFogColor(p0.z, cMultiPoint, color);
								drawable.setColor(color);
								drawable.drawPoint(p0);
							}
							//if (cp.isHidden()) drawable.setColor(Color.BLUE);
							
						}
						if (cp.toString().equals("cp_2389")) {
							drawable.setColor(new Color3f(1,1,1));
							drawable.drawPoint(p0);
						}
					}
				}
			}
		}
		if (viewDef.renderPatches() && (drawable.isShadingSupported() || drawable.isLightingSupported())) {
//			drawable.setColor(Settings.getInstance().colors.backfacingPatches);
			int passes = (drawable instanceof JPatchDrawableGL) ? 3 : 2;
			Vector3f[] normals = new Vector3f[] {new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};
			if (drawable.isLightingSupported())
				drawable.setLightingEnabled(viewDef.getLighting() != null);
			for (int pass = 0; pass < passes; pass++) {
				drawable.setTransparentRenderingMode(pass);
				pass:
				for (Iterator it = model.getPatchSet().iterator(); it.hasNext(); ) {
					Patch patch = (Patch) it.next();
					if (!patch.isHidden() && patch.getMaterial() != null) {
						MaterialProperties mp = patch.getMaterial().getMaterialProperties();
						if (pass == 0) {
							if (mp.transmit > 0 || mp.filter > 0)
								continue pass;
						} else if (pass == 1) {
							if (mp.transmit == 0 && mp.filter == 0)
								continue pass;
						} else if (pass == 2) {
							if ((mp.transmit == 0 && mp.filter == 0) || mp.specular == 0)
								continue pass;
						}
						if (drawable.isLightingSupported())
							drawable.setMaterial(patch.getMaterial().getMaterialProperties());
	//					MaterialProperties materialProperties = patch.getMaterial().getMaterialProperties();
	//					if ((pass == 0 && !materialProperties.isOpaque()) || pass == 1 && materialProperties.isOpaque()) continue;
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
						
	//					if (!bFlat) {
							
							
							
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
										v3_a.sub(hashPatch[(p + 4) % pl], hashPatch[p]);
									} else  {
										v3_a.sub(hashPatch[(p + 1) % pl], hashPatch[p]);
									}
									if (hashPatch[p].equals(hashPatch[(p + pl - 3) % pl])) {
										v3_b.sub(hashPatch[(p + pl - 4) % pl], hashPatch[p]);
									} else {
										v3_b.sub(hashPatch[(p + pl - 1) % pl], hashPatch[p]);
									}
									normals[i].cross(v3_a, v3_b);
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
									Vector3f ncenter = interpolateNormal(n1, n2, p0, p1, p2, p3);
									
									//System.out.println(ncenter);
									//System.out.println();
									float hookpos = targetHook.getHead().getHookPos();
									
									//System.out.println(reversePatch + " " + hookpos);
									
									if (hookpos == 0.5f) v.set(ncenter);
									else if (hookpos == 0.25f) v = interpolateNormal(n1, ncenter);
									else v = interpolateNormal(ncenter, n2);
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
								//v3_a.sub(ap3[1], ap3[0]);
								//v3_b.sub(ap3[8], ap3[0]);
								//cn[0].cross(v3_b, v3_a);
								//v3_a.sub(ap3[4], ap3[3]);
								//v3_b.sub(ap3[2], ap3[3]);
								//cn[1].cross(v3_b, v3_a);
								//v3_a.sub(ap3[7], ap3[6]);
								//v3_b.sub(ap3[5], ap3[6]);
								//cn[2].cross(v3_b, v3_a);
								//cn[0].normalize();
								//cn[1].normalize();
								//cn[2].normalize();
								//cn[3].set(cn[0]);
							
							//System.out.println(patch + " " + hashPatch.length);
							//if (hashPatch.length == 12) {
							//MaterialProperties mp = patch.getMaterial().getMaterialProperties();
							if (viewDef.getLighting() == null) {
								float f = mp.ambient + 0.75f * mp.diffuse;
								if (mp.transmit == 0 && mp.filter == 0) {
									solidColor.set(mp.red * f, mp.green * f, mp.blue * f);
									solidColor.clamp(0, 1);
									drawable.setColor(solidColor);
									drawFlatHashPatch(hashPatch, levels);
								} else {
									solidColor4.set(mp.red * f, mp.green * f, mp.blue * f, 1 - mp.transmit - mp.filter);
									solidColor4.clamp(0, 1);
									drawable.setColor(solidColor4);
									drawFlatHashPatch(hashPatch, levels);
								}
							} else {
								if (drawable.isLightingSupported()) {
									//((JPatchDrawableGL) drawable).setReflectionsEnabled(true);
									//((JPatchDrawableGL) drawable).setLightingEnabled(false);
									drawLitHashPatch(hashPatch, normals, levels);
								} else {
									//drawLitHashPatch(hashPatch, normals, levels);
									if (pass == 0)
										drawShadedHashPatch(hashPatch, normals, levels, mp);
									else
										drawShadedHashPatchAlpha(hashPatch, normals, levels, mp);
								}
							}
							//}
	//					} else {
	//						int[] levels = new int[patch.getType()];
	//						for (int i = 0; i < patch.getType(); levels[i++] = 0);
	//						for (int i = 0, n = patch.getType(), cn = n * 2; i < n; i++) {
	//							int i2 = i * 2;
	//							if (acp[i2].isTargetHook()) {
	//								levels[(i + n - 1) % n] = 1;
	//								//levels[i] = 1;
	//								//System.out.println("1");
	//							}
	//							if (acp[(i2 + cn - 1) % cn].isTargetHook()) {
	//								levels[i] = 1;
	//								//System.out.println("2");
	//							}
	//						}
	//						drawable.drawHashPatchFlat(hashPatch, levels, materialProperties);
	//					}
					}
				}
				drawable.setTransparentRenderingMode(0);
			}
		}
		if (selection != null)
			drawSelection(selection);
		
		drawable.setGhostRenderingEnabled(true);
		drawBones(model);
		drawable.setGhostRenderingEnabled(false);
		if (drawable instanceof JPatchDrawableGL)
			drawBones(model);
		
		if (drawable.isLightingSupported())
			drawable.setLightingEnabled(false);
//		System.out.println();
	}
	
	public void drawOrigin() {
		for (int i = 0; i < 2; i++) {
			drawable.setGhostRenderingEnabled(i == 0);
			float len = 24f/m4View.getScale();;
			Point3f p1 = new Point3f();
			Point3f p2 = new Point3f();
			p2.set(len,0,0);
			m4View.transform(p1);
			m4View.transform(p2);
			drawable.setColor(settings.colors.xAxis); // FIXME
			drawable.drawLine(p1,p2);
			p2.set(0,len,0);
			m4View.transform(p2);
			drawable.setColor(settings.colors.yAxis); // FIXME
			drawable.drawLine(p1,p2);
			p2.set(0,0,len);
			m4View.transform(p2);
			drawable.setColor(settings.colors.zAxis); // FIXME
			drawable.drawLine(p1,p2);
		}
	}
	
	private void drawSelection(Selection selection) {
//		System.out.println(selection.getHotObject());
		if (selection.getHotObject() == null || selection.getHotObject() instanceof AnimObject)
			return;
		Transformable transformable = (Transformable) selection.getHotObject();
//		ControlPoint cp = null;
//		if (hot instanceof ControlPoint) {
//			cp = (ControlPoint) hot;
		Point3f p = new Point3f(transformable.getPosition());
		m4View.transform(p);
		drawable.setColor(settings.colors.hotObject); // FIXME
		drawable.setPointSize(5);
		drawable.drawPoint(p);
		
		int direction = selection.getDirection();
		if (direction == 0)
			return;
		ControlPoint cp = (ControlPoint) transformable;
		if (direction == 1 && cp.getNext() != null) {
			Point3f p3A = new Point3f(cp.getPosition());
			Point3f p3B = new Point3f(cp.getOutTangent());
			Point3f p3C = new Point3f(cp.getNext().getInTangent());
			Point3f p3D = new Point3f(cp.getNext().getPosition());
			m4View.transform(p3A);
			m4View.transform(p3B);
			m4View.transform(p3C);
			m4View.transform(p3D);
//				drawable.setColor(new Color3f(settings.cSelected)); // FIXME
			drawCurveSegment(p3A,p3B,p3C,p3D, false, 0, settings.colors.selectedPoints); // FIXME
		} else if (direction == -1 && cp.getPrev() != null) {
			Point3f p3A = new Point3f(cp.getPrev().getPosition());
			Point3f p3B = new Point3f(cp.getPrev().getOutTangent());
			Point3f p3C = new Point3f(cp.getInTangent());
			Point3f p3D = new Point3f(cp.getPosition());
			m4View.transform(p3A);
			m4View.transform(p3B);
			m4View.transform(p3C);
			m4View.transform(p3D);
//				drawable.setColor(new Color3f(settings.cSelected)); // FIXME
			drawCurveSegment(p3A,p3B,p3C,p3D, false, 0, settings.colors.selectedPoints); // FIXME
		}
	}
	
	private Color3f c3Curve = settings.colors.curves;
	private void drawCurve(ControlPoint start) {
//		System.out.println("drawCurve(" + start + ")");
		ControlPoint cpNext;
		for (ControlPoint cp = start; cp != null; cp = cp.getNextCheckNextLoop()) {
			cpNext = cp.getNext();
			if (cpNext != null && !cp.isHidden() && !cpNext.isHidden()) {
				if (!drawable.isTransformSupported()) {
					if (viewDef.renderReference()) {
						p0.set(cp.getReferencePosition());
						p1.set(cp.getReferenceOutTangent());
						p2.set(cp.getNext().getReferenceInTangent());
						p3.set(cp.getNext().getReferencePosition());
					} else {
						p0.set(cp.getPosition());
						p1.set(cp.getOutTangent());
						p2.set(cp.getNext().getInTangent());
						p3.set(cp.getNext().getPosition());
					}
					m4View.transform(p0);
					m4View.transform(p1);
					m4View.transform(p2);
					m4View.transform(p3);
					drawCurveSegment(p0, p1, p2, p3, false, 0, c3Curve);
//					drawable.drawLine(p0, p1);
//					drawable.drawLine(p3, p2);
				} else {
//					drawCurveSegment(cp.getPosition(), cp.getOutTangent(), cp.getNext().getInTangent(), cp.getNext().getPosition(), false, 0);
				}
			}
		}
	}
	
	private Color3f c3SegmentA = new Color3f();
	private Color3f c3SegmentB = new Color3f();
	private void drawCurveSegment(Point3f p3A, Point3f p3B, Point3f p3C, Point3f p3D, boolean simple, int level, Color3f color) {
//		System.out.println("drawCurveSegment(" + level + ", " + p3A + ", " + p3B + ", " + p3C + ", " + p3D + ")");
		if (level < iMaxSubdiv && subdiv(p3A, p3B, p3C, p3D, simple) >= fFlatness) {
			Point3f p0 = new Point3f(p3A);
			Point3f p1 = new Point3f(p3B);
			Point3f p2 = new Point3f(p3C);
			Point3f p3 = new Point3f(p3D);
			Point3f p4 = new Point3f();
			Point3f p5 = new Point3f();
			Point3f p6 = new Point3f();
			Point3f p7 = new Point3f();
			deCasteljauSplit(p0, p1, p2, p3, p4, p5, p6, p7);
			
			drawCurveSegment(p0, p1, p2, p3, true, ++level, color);
			drawCurveSegment(p4, p5, p6, p7, true, level, color);
		} else {
			setFogColor(p3A.z, color, c3SegmentA);
			setFogColor(p3D.z, color, c3SegmentB);
			drawable.drawLine(p3A, c3SegmentA, p3D, c3SegmentB);
		}
	}
	
	public void drawFlatHashPatch(Point3f[] ap3, int[] ail) {
		boolean[] flat = new boolean[] { false, false, false, false };
		switch (ap3.length) {
			case 9: {
				
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
				
			
				Point3f[] p = new Point3f[] {ap3[0], ap3[1], ap3[2], ap3[3], ap3[4], ap3[5], ap3[6], ap3[7], ap3[8], new Point3f(ap3[0]), new Point3f(ap3[0]), new Point3f(ap3[0])};
				
				//p[0].set(ap3[0]);
				//p[1].set(ap3[1]);
				//p[2].set(ap3[2]);
				//p[3].set(ap3[3]);
				//p[4].set(ap3[4]);
				//p[5].set(ap3[5]);
				//p[6].set(ap3[6]);
				//p[7].set(ap3[7]);
				//p[8].set(ap3[8]);
				//p[9].set(ap3[0]);
				//p[10].set(ap3[0]);
				//p[11].set(ap3[0]);
				//
				//drawRectHashPatchGourad(p, n, mp, 0);
				int[] levels = new int[] { ail[0], ail[1], ail[2], 0 };
				drawFlatHashPatch4(p, flat, levels);
				//if (true) return;
				//
				/////* set up corner normals */
				////Vector3f[] cn = newNormals(3);
				////v3a.sub(ap3[1], ap3[0]);
				////v3b.sub(ap3[8], ap3[0]);
				////cn[0].cross(v3b, v3a);
				////v3a.sub(ap3[4], ap3[3]);
				////v3b.sub(ap3[2], ap3[3]);
				////cn[1].cross(v3b, v3a);
				////v3a.sub(ap3[7], ap3[6]);
				////v3b.sub(ap3[5], ap3[6]);
				////cn[2].cross(v3b, v3a);
				////cn[0].normalize();
				////cn[1].normalize();
				////cn[2].normalize();
				//
				////drawHashPatch3(ap3, cn, 0, mp);
				////if (true) return;
				//
				///* set up midpoint normals */
				//Vector3f[] mn = new Vector3f[5];
				//mn[0] = interpolateNormal(cn[0], cn[1], ap3[0], ap3[1], ap3[2], ap3[3]);
				//mn[1] = interpolateNormal(cn[1], cn[2], ap3[3], ap3[4], ap3[5], ap3[6]);
				//mn[2] = interpolateNormal(cn[2], cn[0], ap3[6], ap3[7], ap3[8], ap3[0]);
				//
				//Point3f A = Functions.parallelogram(ap3[0],ap3[1],ap3[8]);
				//Point3f B = Functions.parallelogram(ap3[3],ap3[4],ap3[2]);
				//Point3f C = Functions.parallelogram(ap3[6],ap3[7],ap3[5]);
				//
				//Point3f F = Functions.average(A,ap3[0],ap3[1],ap3[8]);
				//Point3f H = Functions.average(B,ap3[3],ap3[4],ap3[2]);
				//Point3f J = Functions.average(C,ap3[6],ap3[7],ap3[5]);
				//
				//Point3f G = Functions.average(A,B,ap3[1],ap3[2]);
				//Point3f I = Functions.average(B,C,ap3[4],ap3[5]);
				//Point3f K = Functions.average(C,A,ap3[7],ap3[8]);
				//
				//Point3f U = Functions.average(A,B,C);
				//
				//Point3f P = Functions.average(U,K,F,G);
				//Point3f Q = Functions.average(U,G,H,I);
				//Point3f R = Functions.average(U,I,J,K);
				//
				//Point3f V = Functions.average(R,P);
				//Point3f W = Functions.average(P,Q);
				//Point3f X = Functions.average(Q,R);
				//
				//Point3f Center = Functions.average(P,Q,R);
				//
				//Vector3f vc = Functions.vector(V, Center);
				//Vector3f wc = Functions.vector(W, Center);
				//Vector3f xc = Functions.vector(X, Center);
				//
				//Vector3f[] nc = newNormals(3);
				//nc[0].cross(wc, vc);
				//nc[1].cross(xc, wc);
				//nc[2].cross(vc, xc);
				//nc[0].normalize();
				//nc[1].normalize();
				//nc[2].normalize();
				//Vector3f centerNormal = Functions.vaverage(nc[0], nc[1], nc[2]);
				//centerNormal.normalize();
				//
				//Vector3f[] normals = newNormals(4);
				//
				//Point3f[][] aap3Boundary = new Point3f[3][7];
				//
				//aap3Boundary[0] = Bezier.deCasteljau(ap3[0],ap3[1],ap3[2],ap3[3],0.5f);
				//aap3Boundary[1] = Bezier.deCasteljau(ap3[3],ap3[4],ap3[5],ap3[6],0.5f);
				//aap3Boundary[2] = Bezier.deCasteljau(ap3[6],ap3[7],ap3[8],ap3[0],0.5f);
				//
				//Point3f[] ap3Patches = new Point3f[12];
				//
				//Vector3f v;
				//float sc = 0.33f;
				//
				//ap3Patches[0] = new Point3f(aap3Boundary[0][0]);
				//ap3Patches[1] = new Point3f(aap3Boundary[0][1]);
				//ap3Patches[2] = new Point3f(aap3Boundary[0][2]);
				//ap3Patches[3] = new Point3f(aap3Boundary[0][3]);
				//ap3Patches[11] = new Point3f(aap3Boundary[2][5]);
				//v = Functions.vaverage(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[8]));
				//v.scale(sc);
				//ap3Patches[4] = new Point3f(aap3Boundary[0][3]);
				//ap3Patches[4].add(v);
				////ap3Patches[4].add(Functions.average(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[8])));
				//ap3Patches[10] = new Point3f(aap3Boundary[2][4]);
				//ap3Patches[5] = new Point3f(W);
				//ap3Patches[9] = new Point3f(aap3Boundary[2][3]);
				//v = Functions.vaverage(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[6],ap3[5]));
				//v.scale(sc);
				//ap3Patches[8] = new Point3f(aap3Boundary[2][3]);
				//ap3Patches[8].add(v);
				////ap3Patches[8].add(Functions.average(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[6],ap3[5])));
				//ap3Patches[7] = new Point3f(V);
				//ap3Patches[6] = new Point3f(Center);
				//normals[0].set(cn[0]);
				//normals[1].set(mn[0]);
				//normals[2].set(centerNormal);
				//normals[3].set(mn[2]);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				//    
				//ap3Patches[0].set(aap3Boundary[1][0]);
				//ap3Patches[1].set(aap3Boundary[1][1]);
				//ap3Patches[2].set(aap3Boundary[1][2]);
				//ap3Patches[3].set(aap3Boundary[1][3]);
				//ap3Patches[11].set(aap3Boundary[0][5]);
				//v = Functions.vaverage(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2]));
				//v.scale(sc);
				//ap3Patches[4].add(aap3Boundary[1][3], v);
				////ap3Patches[4].add(Functions.average(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2])));
				//ap3Patches[10].set(aap3Boundary[0][4]);
				//ap3Patches[5].set(X);
				//ap3Patches[9].set(aap3Boundary[0][3]);
				//v = Functions.vaverage(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[8]));
				//v.scale(sc);
				//ap3Patches[8].add(aap3Boundary[0][3], v);
				////ap3Patches[8].add(Functions.average(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[8])));
				//ap3Patches[7].set(W);
				//ap3Patches[6].set(Center);
				//normals[0].set(cn[1]);
				//normals[1].set(mn[1]);
				//normals[2].set(centerNormal);
				//normals[3].set(mn[0]);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				//
				//ap3Patches[0].set(aap3Boundary[2][0]);
				//ap3Patches[1].set(aap3Boundary[2][1]);
				//ap3Patches[2].set(aap3Boundary[2][2]);
				//ap3Patches[3].set(aap3Boundary[2][3]);
				//ap3Patches[11].set(aap3Boundary[1][5]);
				//v = Functions.vaverage(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[6],ap3[5]));
				//v.scale(sc);
				//ap3Patches[4].add(aap3Boundary[2][3], v);
				////ap3Patches[4].add(Functions.average(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[6],ap3[5])));
				//ap3Patches[10] = new Point3f(aap3Boundary[1][4]);
				//ap3Patches[5].set(V);
				//ap3Patches[9].set(aap3Boundary[1][3]);
				//v = Functions.vaverage(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2]));
				//v.scale(sc);
				//ap3Patches[8].add(aap3Boundary[1][3], v);
				////ap3Patches[8].add(Functions.average(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2])));
				//ap3Patches[7].set(X);
				//ap3Patches[6].set(Center);
				//normals[0].set(cn[2]);
				//normals[1].set(mn[2]);
				//normals[2].set(centerNormal);
				//normals[3].set(mn[1]);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
			}
			break;
			case 12: {
				///* set up corner normals */
				//Vector3f[] normals = newNormals(4);
				//v3a.sub(ap3[1], ap3[0]);
				//v3b.sub(ap3[11], ap3[0]);
				//normals[0].cross(v3b, v3a);
				//v3a.sub(ap3[4], ap3[3]);
				//v3b.sub(ap3[2], ap3[3]);
				//normals[1].cross(v3b, v3a);
				//v3a.sub(ap3[7], ap3[6]);
				//v3b.sub(ap3[5], ap3[6]);
				//normals[2].cross(v3b, v3a);
				//v3a.sub(ap3[10], ap3[9]);
				//v3b.sub(ap3[8], ap3[9]);
				//normals[3].cross(v3b, v3a);
				//normals[0].normalize();
				//normals[1].normalize();
				//normals[2].normalize();
				//normals[3].normalize();
//				colors[0] = lighting.shade(ap3[0], av3[0], mp);
//				colors[1] = lighting.shade(ap3[3], av3[1], mp);
//				colors[2] = lighting.shade(ap3[6], av3[2], mp);
//				colors[3] = lighting.shade(ap3[9], av3[3], mp);
				drawFlatHashPatch4(ap3, flat, ail);
				//drawRectHashPatchGourad(ap3, av3, mp, 0);
			}
			break;
			case 15: {
				///* set up corner normals */
				//Vector3f[] cn = newNormals(5);
				//v3a.sub(ap3[1], ap3[0]);
				//v3b.sub(ap3[14], ap3[0]);
				//cn[0].cross(v3b, v3a);
				//v3a.sub(ap3[4], ap3[3]);
				//v3b.sub(ap3[2], ap3[3]);
				//cn[1].cross(v3b, v3a);
				//v3a.sub(ap3[7], ap3[6]);
				//v3b.sub(ap3[5], ap3[6]);
				//cn[2].cross(v3b, v3a);
				//v3a.sub(ap3[10], ap3[9]);
				//v3b.sub(ap3[8], ap3[9]);
				//cn[3].cross(v3b, v3a);
				//v3a.sub(ap3[13], ap3[12]);
				//v3b.sub(ap3[11], ap3[12]);
				//cn[4].cross(v3b, v3a);
				//cn[0].normalize();
				//cn[1].normalize();
				//cn[2].normalize();
				//cn[3].normalize();
				//cn[4].normalize();
					
				/* set up midpoint normals */
					
				/* compute corner-colors */
//				int[] ccol = new int[] {
//					lighting.shade(ap3[0], av3[0], mp),
//					lighting.shade(ap3[3], av3[1], mp),
//					lighting.shade(ap3[6], av3[2], mp),
//					lighting.shade(ap3[9], av3[3], mp),
//					lighting.shade(ap3[12], av3[4], mp)
//				};
					
				Point3f A = Functions.parallelogram(ap3[0],ap3[1],ap3[14]);
				Point3f B = Functions.parallelogram(ap3[3],ap3[4],ap3[2]);
				Point3f C = Functions.parallelogram(ap3[6],ap3[7],ap3[5]);
				Point3f D = Functions.parallelogram(ap3[9],ap3[10],ap3[8]);
				Point3f E = Functions.parallelogram(ap3[12],ap3[13],ap3[11]);
				
				Point3f F = Functions.average(A,ap3[0],ap3[1],ap3[14]);
				Point3f H = Functions.average(B,ap3[3],ap3[4],ap3[2]);
				Point3f J = Functions.average(C,ap3[6],ap3[7],ap3[5]);
				Point3f L = Functions.average(D,ap3[9],ap3[10],ap3[8]);
				Point3f N = Functions.average(E,ap3[12],ap3[13],ap3[11]);
				
				Point3f G = Functions.average(A,B,ap3[1],ap3[2]);
				Point3f I = Functions.average(B,C,ap3[4],ap3[5]);
				Point3f K = Functions.average(C,D,ap3[7],ap3[8]);
				Point3f M = Functions.average(D,E,ap3[10],ap3[11]);
				Point3f O = Functions.average(E,A,ap3[13],ap3[14]);
				
				Point3f U = Functions.average(A,B,C,D,E);
				
				Point3f P = Functions.average(U,O,F,G);
				Point3f Q = Functions.average(U,G,H,I);
				Point3f R = Functions.average(U,I,J,K);
				Point3f S = Functions.average(U,K,L,M);
				Point3f T = Functions.average(U,M,N,O);
				
				Point3f V = Functions.average(T,P);
				Point3f W = Functions.average(P,Q);
				Point3f X = Functions.average(Q,R);
				Point3f Y = Functions.average(R,S);
				Point3f Z = Functions.average(S,T);
				
				Point3f Center = Functions.average(P,Q,R,S,T);
				
//				Vector3f vc = Functions.vector(V, Center);
//				Vector3f wc = Functions.vector(W, Center);
//				Vector3f xc = Functions.vector(X, Center);
//				Vector3f yc = Functions.vector(Y, Center);
//				Vector3f zc = Functions.vector(Z, Center);
				
				Point3f[][] aap3Boundary = new Point3f[5][7];
				
				aap3Boundary[0] = Bezier.deCasteljau(ap3[0],ap3[1],ap3[2],ap3[3],0.5f);
				aap3Boundary[1] = Bezier.deCasteljau(ap3[3],ap3[4],ap3[5],ap3[6],0.5f);
				aap3Boundary[2] = Bezier.deCasteljau(ap3[6],ap3[7],ap3[8],ap3[9],0.5f);
				aap3Boundary[3] = Bezier.deCasteljau(ap3[9],ap3[10],ap3[11],ap3[12],0.5f);
				aap3Boundary[4] = Bezier.deCasteljau(ap3[12],ap3[13],ap3[14],ap3[0],0.5f);
				
				/* compute midpoint colors */
//				int[] mcol = new int[] {
//					lighting.shade(aap3Boundary[0][3], mn[0], mp),
//					lighting.shade(aap3Boundary[1][3], mn[1], mp),
//					lighting.shade(aap3Boundary[2][3], mn[2], mp),
//					lighting.shade(aap3Boundary[3][3], mn[3], mp),
//					lighting.shade(aap3Boundary[4][3], mn[4], mp)
//				};
				
//				int centerColor = lighting.shade(Center, centerNormal, mp);
				
				Point3f[] ap3Patches = new Point3f[12];
				
				int[] levels = new int[4];
				
				ap3Patches[0] = new Point3f(aap3Boundary[0][0]);
				ap3Patches[1] = new Point3f(aap3Boundary[0][1]);
				ap3Patches[2] = new Point3f(aap3Boundary[0][2]);
				ap3Patches[3] = new Point3f(aap3Boundary[0][3]);
				ap3Patches[11] = new Point3f(aap3Boundary[4][5]);
				ap3Patches[4] = new Point3f(aap3Boundary[0][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[14])));
				ap3Patches[10] = new Point3f(aap3Boundary[4][4]);
				ap3Patches[5] = new Point3f(W);
				ap3Patches[9] = new Point3f(aap3Boundary[4][3]);
				ap3Patches[8] = new Point3f(aap3Boundary[4][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[12],ap3[11])));
				ap3Patches[7] = new Point3f(V);
				ap3Patches[6] = new Point3f(Center);
//				normals[0].set(av3[0]);
//				normals[1].set(mn[0]);
//				normals[2].set(centerNormal);
//				normals[3].set(mn[4]);
//				colors[0] = ccol[0];
//				colors[1] = mcol[0];
//				colors[2] = centerColor;
//				colors[3] = mcol[4];
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[0] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[4] + 1;
				drawFlatHashPatch4(ap3Patches, flat, levels);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				    
				ap3Patches[0].set(aap3Boundary[1][0]);
				ap3Patches[1].set(aap3Boundary[1][1]);
				ap3Patches[2].set(aap3Boundary[1][2]);
				ap3Patches[3].set(aap3Boundary[1][3]);
				ap3Patches[11].set(aap3Boundary[0][5]);
				ap3Patches[4].set(aap3Boundary[1][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2])));
				ap3Patches[10].set(aap3Boundary[0][4]);
				ap3Patches[5].set(X);
				ap3Patches[9].set(aap3Boundary[0][3]);
				ap3Patches[8].set(aap3Boundary[0][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[14])));
				ap3Patches[7].set(W);
				ap3Patches[6].set(Center);
//				normals[0].set(av3[1]);
//				normals[1].set(mn[1]);
//				normals[2].set(centerNormal);
//				normals[3].set(mn[0]);
//				colors[0] = ccol[1];
//				colors[1] = mcol[1];
//				colors[2] = centerColor;
//				colors[3] = mcol[0];
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[1] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[0] + 1;
				drawFlatHashPatch4(ap3Patches, flat, levels);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				
				ap3Patches[0].set(aap3Boundary[2][0]);
				ap3Patches[1].set(aap3Boundary[2][1]);
				ap3Patches[2].set(aap3Boundary[2][2]);
				ap3Patches[3].set(aap3Boundary[2][3]);
				ap3Patches[11].set(aap3Boundary[1][5]);
				ap3Patches[4].set(aap3Boundary[2][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[9],ap3[10]),Functions.vector(ap3[6],ap3[5])));
				ap3Patches[10] = new Point3f(aap3Boundary[1][4]);
				ap3Patches[5].set(Y);
				ap3Patches[9].set(aap3Boundary[1][3]);
				ap3Patches[8].set(aap3Boundary[1][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2])));
				ap3Patches[7].set(X);
				ap3Patches[6].set(Center);
//				normals[0].set(av3[2]);
//				normals[1].set(mn[2]);
//				normals[2].set(centerNormal);
//				normals[3].set(mn[1]);
//				colors[0] = ccol[2];
//				colors[1] = mcol[2];
//				colors[2] = centerColor;
//				colors[3] = mcol[1];
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[2] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[1] + 1;
				drawFlatHashPatch4(ap3Patches, flat, levels);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				
				ap3Patches[0].set(aap3Boundary[3][0]);
				ap3Patches[1].set(aap3Boundary[3][1]);
				ap3Patches[2].set(aap3Boundary[3][2]);
				ap3Patches[3].set(aap3Boundary[3][3]);
				ap3Patches[11].set(aap3Boundary[2][5]);
				ap3Patches[4].set(aap3Boundary[3][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[9],ap3[8]),Functions.vector(ap3[12],ap3[13])));
				ap3Patches[10].set(aap3Boundary[2][4]);
				ap3Patches[5].set(Z);
				ap3Patches[9].set(aap3Boundary[2][3]);
				ap3Patches[8].set(aap3Boundary[2][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[9],ap3[10]),Functions.vector(ap3[6],ap3[5])));
				ap3Patches[7].set(Y);
				ap3Patches[6].set(Center);
//				normals[0].set(av3[3]);
//				normals[1].set(mn[3]);
//				normals[2].set(centerNormal);
//				normals[3].set(mn[2]);
//				colors[0] = ccol[3];
//				colors[1] = mcol[3];
//				colors[2] = centerColor;
//				colors[3] = mcol[2];
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[3] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[2] + 1;
				drawFlatHashPatch4(ap3Patches, flat, levels);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				
				ap3Patches[0].set(aap3Boundary[4][0]);
				ap3Patches[1].set(aap3Boundary[4][1]);
				ap3Patches[2].set(aap3Boundary[4][2]);
				ap3Patches[3].set(aap3Boundary[4][3]);
				ap3Patches[11].set(aap3Boundary[3][5]);
				ap3Patches[4].set(aap3Boundary[4][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[12],ap3[11]),Functions.vector(ap3[0],ap3[1])));
				ap3Patches[10].set(aap3Boundary[3][4]);
				ap3Patches[5].set(V);
				ap3Patches[9].set(aap3Boundary[3][3]);
				ap3Patches[8].set(aap3Boundary[3][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[9],ap3[8]),Functions.vector(ap3[12],ap3[13])));
				ap3Patches[7].set(Z);
				ap3Patches[6].set(Center);
//				normals[0].set(av3[4]);
//				normals[1].set(mn[4]);
//				normals[2].set(centerNormal);
//				normals[3].set(mn[3]);
//				colors[0] = ccol[4];
//				colors[1] = mcol[4];
//				colors[2] = centerColor;
//				colors[3] = mcol[3];
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[4] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[3] + 1;
				drawFlatHashPatch4(ap3Patches, flat, levels);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
			}
			break;
		}
	}
	
	public void drawLitHashPatch(Point3f[] ap3, Vector3f[] av3, int[] ail) {
		boolean[] flat = new boolean[] { false, false, false, false };
		switch (ap3.length) {
			case 9: {
				
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
				
				Vector3f[] n = new Vector3f[] {av3[0], av3[1], av3[2], new Vector3f(av3[0])};
				
				Point3f[] p = new Point3f[] {ap3[0], ap3[1], ap3[2], ap3[3], ap3[4], ap3[5], ap3[6], ap3[7], ap3[8], new Point3f(ap3[0]), new Point3f(ap3[0]), new Point3f(ap3[0])};
				
				//p[0].set(ap3[0]);
				//p[1].set(ap3[1]);
				//p[2].set(ap3[2]);
				//p[3].set(ap3[3]);
				//p[4].set(ap3[4]);
				//p[5].set(ap3[5]);
				//p[6].set(ap3[6]);
				//p[7].set(ap3[7]);
				//p[8].set(ap3[8]);
				//p[9].set(ap3[0]);
				//p[10].set(ap3[0]);
				//p[11].set(ap3[0]);
				//
				//drawRectHashPatchGourad(p, n, mp, 0);
				int[] levels = new int[] { ail[0], ail[1], ail[2], 0 };
				drawLitHashPatch4(p, n, flat, levels);
				//if (true) return;
				//
				/////* set up corner normals */
				////Vector3f[] cn = newNormals(3);
				////v3a.sub(ap3[1], ap3[0]);
				////v3b.sub(ap3[8], ap3[0]);
				////cn[0].cross(v3b, v3a);
				////v3a.sub(ap3[4], ap3[3]);
				////v3b.sub(ap3[2], ap3[3]);
				////cn[1].cross(v3b, v3a);
				////v3a.sub(ap3[7], ap3[6]);
				////v3b.sub(ap3[5], ap3[6]);
				////cn[2].cross(v3b, v3a);
				////cn[0].normalize();
				////cn[1].normalize();
				////cn[2].normalize();
				//
				////drawHashPatch3(ap3, cn, 0, mp);
				////if (true) return;
				//
				///* set up midpoint normals */
				//Vector3f[] mn = new Vector3f[5];
				//mn[0] = interpolateNormal(cn[0], cn[1], ap3[0], ap3[1], ap3[2], ap3[3]);
				//mn[1] = interpolateNormal(cn[1], cn[2], ap3[3], ap3[4], ap3[5], ap3[6]);
				//mn[2] = interpolateNormal(cn[2], cn[0], ap3[6], ap3[7], ap3[8], ap3[0]);
				//
				//Point3f A = Functions.parallelogram(ap3[0],ap3[1],ap3[8]);
				//Point3f B = Functions.parallelogram(ap3[3],ap3[4],ap3[2]);
				//Point3f C = Functions.parallelogram(ap3[6],ap3[7],ap3[5]);
				//
				//Point3f F = Functions.average(A,ap3[0],ap3[1],ap3[8]);
				//Point3f H = Functions.average(B,ap3[3],ap3[4],ap3[2]);
				//Point3f J = Functions.average(C,ap3[6],ap3[7],ap3[5]);
				//
				//Point3f G = Functions.average(A,B,ap3[1],ap3[2]);
				//Point3f I = Functions.average(B,C,ap3[4],ap3[5]);
				//Point3f K = Functions.average(C,A,ap3[7],ap3[8]);
				//
				//Point3f U = Functions.average(A,B,C);
				//
				//Point3f P = Functions.average(U,K,F,G);
				//Point3f Q = Functions.average(U,G,H,I);
				//Point3f R = Functions.average(U,I,J,K);
				//
				//Point3f V = Functions.average(R,P);
				//Point3f W = Functions.average(P,Q);
				//Point3f X = Functions.average(Q,R);
				//
				//Point3f Center = Functions.average(P,Q,R);
				//
				//Vector3f vc = Functions.vector(V, Center);
				//Vector3f wc = Functions.vector(W, Center);
				//Vector3f xc = Functions.vector(X, Center);
				//
				//Vector3f[] nc = newNormals(3);
				//nc[0].cross(wc, vc);
				//nc[1].cross(xc, wc);
				//nc[2].cross(vc, xc);
				//nc[0].normalize();
				//nc[1].normalize();
				//nc[2].normalize();
				//Vector3f centerNormal = Functions.vaverage(nc[0], nc[1], nc[2]);
				//centerNormal.normalize();
				//
				//Vector3f[] normals = newNormals(4);
				//
				//Point3f[][] aap3Boundary = new Point3f[3][7];
				//
				//aap3Boundary[0] = Bezier.deCasteljau(ap3[0],ap3[1],ap3[2],ap3[3],0.5f);
				//aap3Boundary[1] = Bezier.deCasteljau(ap3[3],ap3[4],ap3[5],ap3[6],0.5f);
				//aap3Boundary[2] = Bezier.deCasteljau(ap3[6],ap3[7],ap3[8],ap3[0],0.5f);
				//
				//Point3f[] ap3Patches = new Point3f[12];
				//
				//Vector3f v;
				//float sc = 0.33f;
				//
				//ap3Patches[0] = new Point3f(aap3Boundary[0][0]);
				//ap3Patches[1] = new Point3f(aap3Boundary[0][1]);
				//ap3Patches[2] = new Point3f(aap3Boundary[0][2]);
				//ap3Patches[3] = new Point3f(aap3Boundary[0][3]);
				//ap3Patches[11] = new Point3f(aap3Boundary[2][5]);
				//v = Functions.vaverage(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[8]));
				//v.scale(sc);
				//ap3Patches[4] = new Point3f(aap3Boundary[0][3]);
				//ap3Patches[4].add(v);
				////ap3Patches[4].add(Functions.average(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[8])));
				//ap3Patches[10] = new Point3f(aap3Boundary[2][4]);
				//ap3Patches[5] = new Point3f(W);
				//ap3Patches[9] = new Point3f(aap3Boundary[2][3]);
				//v = Functions.vaverage(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[6],ap3[5]));
				//v.scale(sc);
				//ap3Patches[8] = new Point3f(aap3Boundary[2][3]);
				//ap3Patches[8].add(v);
				////ap3Patches[8].add(Functions.average(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[6],ap3[5])));
				//ap3Patches[7] = new Point3f(V);
				//ap3Patches[6] = new Point3f(Center);
				//normals[0].set(cn[0]);
				//normals[1].set(mn[0]);
				//normals[2].set(centerNormal);
				//normals[3].set(mn[2]);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				//    
				//ap3Patches[0].set(aap3Boundary[1][0]);
				//ap3Patches[1].set(aap3Boundary[1][1]);
				//ap3Patches[2].set(aap3Boundary[1][2]);
				//ap3Patches[3].set(aap3Boundary[1][3]);
				//ap3Patches[11].set(aap3Boundary[0][5]);
				//v = Functions.vaverage(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2]));
				//v.scale(sc);
				//ap3Patches[4].add(aap3Boundary[1][3], v);
				////ap3Patches[4].add(Functions.average(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2])));
				//ap3Patches[10].set(aap3Boundary[0][4]);
				//ap3Patches[5].set(X);
				//ap3Patches[9].set(aap3Boundary[0][3]);
				//v = Functions.vaverage(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[8]));
				//v.scale(sc);
				//ap3Patches[8].add(aap3Boundary[0][3], v);
				////ap3Patches[8].add(Functions.average(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[8])));
				//ap3Patches[7].set(W);
				//ap3Patches[6].set(Center);
				//normals[0].set(cn[1]);
				//normals[1].set(mn[1]);
				//normals[2].set(centerNormal);
				//normals[3].set(mn[0]);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				//
				//ap3Patches[0].set(aap3Boundary[2][0]);
				//ap3Patches[1].set(aap3Boundary[2][1]);
				//ap3Patches[2].set(aap3Boundary[2][2]);
				//ap3Patches[3].set(aap3Boundary[2][3]);
				//ap3Patches[11].set(aap3Boundary[1][5]);
				//v = Functions.vaverage(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[6],ap3[5]));
				//v.scale(sc);
				//ap3Patches[4].add(aap3Boundary[2][3], v);
				////ap3Patches[4].add(Functions.average(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[6],ap3[5])));
				//ap3Patches[10] = new Point3f(aap3Boundary[1][4]);
				//ap3Patches[5].set(V);
				//ap3Patches[9].set(aap3Boundary[1][3]);
				//v = Functions.vaverage(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2]));
				//v.scale(sc);
				//ap3Patches[8].add(aap3Boundary[1][3], v);
				////ap3Patches[8].add(Functions.average(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2])));
				//ap3Patches[7].set(X);
				//ap3Patches[6].set(Center);
				//normals[0].set(cn[2]);
				//normals[1].set(mn[2]);
				//normals[2].set(centerNormal);
				//normals[3].set(mn[1]);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
			}
			break;
			case 12: {
				///* set up corner normals */
				//Vector3f[] normals = newNormals(4);
				//v3a.sub(ap3[1], ap3[0]);
				//v3b.sub(ap3[11], ap3[0]);
				//normals[0].cross(v3b, v3a);
				//v3a.sub(ap3[4], ap3[3]);
				//v3b.sub(ap3[2], ap3[3]);
				//normals[1].cross(v3b, v3a);
				//v3a.sub(ap3[7], ap3[6]);
				//v3b.sub(ap3[5], ap3[6]);
				//normals[2].cross(v3b, v3a);
				//v3a.sub(ap3[10], ap3[9]);
				//v3b.sub(ap3[8], ap3[9]);
				//normals[3].cross(v3b, v3a);
				//normals[0].normalize();
				//normals[1].normalize();
				//normals[2].normalize();
				//normals[3].normalize();
//				colors[0] = lighting.shade(ap3[0], av3[0], mp);
//				colors[1] = lighting.shade(ap3[3], av3[1], mp);
//				colors[2] = lighting.shade(ap3[6], av3[2], mp);
//				colors[3] = lighting.shade(ap3[9], av3[3], mp);
				drawLitHashPatch4(ap3, av3, flat, ail);
				//drawRectHashPatchGourad(ap3, av3, mp, 0);
			}
			break;
			case 15: {
				///* set up corner normals */
				//Vector3f[] cn = newNormals(5);
				//v3a.sub(ap3[1], ap3[0]);
				//v3b.sub(ap3[14], ap3[0]);
				//cn[0].cross(v3b, v3a);
				//v3a.sub(ap3[4], ap3[3]);
				//v3b.sub(ap3[2], ap3[3]);
				//cn[1].cross(v3b, v3a);
				//v3a.sub(ap3[7], ap3[6]);
				//v3b.sub(ap3[5], ap3[6]);
				//cn[2].cross(v3b, v3a);
				//v3a.sub(ap3[10], ap3[9]);
				//v3b.sub(ap3[8], ap3[9]);
				//cn[3].cross(v3b, v3a);
				//v3a.sub(ap3[13], ap3[12]);
				//v3b.sub(ap3[11], ap3[12]);
				//cn[4].cross(v3b, v3a);
				//cn[0].normalize();
				//cn[1].normalize();
				//cn[2].normalize();
				//cn[3].normalize();
				//cn[4].normalize();
					
				/* set up midpoint normals */
				Vector3f[] mn = new Vector3f[5];
				mn[0] = interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]);
				mn[1] = interpolateNormal(av3[1], av3[2], ap3[3], ap3[4], ap3[5], ap3[6]);
				mn[2] = interpolateNormal(av3[2], av3[3], ap3[6], ap3[7], ap3[8], ap3[9]);
				mn[3] = interpolateNormal(av3[3], av3[4], ap3[9], ap3[10], ap3[11], ap3[12]);
				mn[4] = interpolateNormal(av3[4], av3[0], ap3[12], ap3[13], ap3[14], ap3[0]);
				
				/* compute corner-colors */
//				int[] ccol = new int[] {
//					lighting.shade(ap3[0], av3[0], mp),
//					lighting.shade(ap3[3], av3[1], mp),
//					lighting.shade(ap3[6], av3[2], mp),
//					lighting.shade(ap3[9], av3[3], mp),
//					lighting.shade(ap3[12], av3[4], mp)
//				};
					
				Point3f A = Functions.parallelogram(ap3[0],ap3[1],ap3[14]);
				Point3f B = Functions.parallelogram(ap3[3],ap3[4],ap3[2]);
				Point3f C = Functions.parallelogram(ap3[6],ap3[7],ap3[5]);
				Point3f D = Functions.parallelogram(ap3[9],ap3[10],ap3[8]);
				Point3f E = Functions.parallelogram(ap3[12],ap3[13],ap3[11]);
				
				Point3f F = Functions.average(A,ap3[0],ap3[1],ap3[14]);
				Point3f H = Functions.average(B,ap3[3],ap3[4],ap3[2]);
				Point3f J = Functions.average(C,ap3[6],ap3[7],ap3[5]);
				Point3f L = Functions.average(D,ap3[9],ap3[10],ap3[8]);
				Point3f N = Functions.average(E,ap3[12],ap3[13],ap3[11]);
				
				Point3f G = Functions.average(A,B,ap3[1],ap3[2]);
				Point3f I = Functions.average(B,C,ap3[4],ap3[5]);
				Point3f K = Functions.average(C,D,ap3[7],ap3[8]);
				Point3f M = Functions.average(D,E,ap3[10],ap3[11]);
				Point3f O = Functions.average(E,A,ap3[13],ap3[14]);
				
				Point3f U = Functions.average(A,B,C,D,E);
				
				Point3f P = Functions.average(U,O,F,G);
				Point3f Q = Functions.average(U,G,H,I);
				Point3f R = Functions.average(U,I,J,K);
				Point3f S = Functions.average(U,K,L,M);
				Point3f T = Functions.average(U,M,N,O);
				
				Point3f V = Functions.average(T,P);
				Point3f W = Functions.average(P,Q);
				Point3f X = Functions.average(Q,R);
				Point3f Y = Functions.average(R,S);
				Point3f Z = Functions.average(S,T);
				
				Point3f Center = Functions.average(P,Q,R,S,T);
				
				Vector3f vc = Functions.vector(V, Center);
				Vector3f wc = Functions.vector(W, Center);
				Vector3f xc = Functions.vector(X, Center);
				Vector3f yc = Functions.vector(Y, Center);
				Vector3f zc = Functions.vector(Z, Center);
				
				Vector3f[] nc = newNormals(5);
//				nc[0].cross(wc, vc);
//				nc[1].cross(xc, wc);
//				nc[2].cross(yc, xc);
//				nc[3].cross(zc, yc);
//				nc[4].cross(vc, zc);
				nc[0].cross(vc, wc);
				nc[1].cross(wc, xc);
				nc[2].cross(xc, yc);
				nc[3].cross(yc, zc);
				nc[4].cross(zc, vc);
				nc[0].normalize();
				nc[1].normalize();
				nc[2].normalize();
				nc[3].normalize();
				nc[4].normalize();
				Vector3f centerNormal = Functions.vaverage(nc[0], nc[1], nc[2], nc[3], nc[4]);
				centerNormal.normalize();
				
				Vector3f[] normals = newNormals(4);
				
				Point3f[][] aap3Boundary = new Point3f[5][7];
				
				aap3Boundary[0] = Bezier.deCasteljau(ap3[0],ap3[1],ap3[2],ap3[3],0.5f);
				aap3Boundary[1] = Bezier.deCasteljau(ap3[3],ap3[4],ap3[5],ap3[6],0.5f);
				aap3Boundary[2] = Bezier.deCasteljau(ap3[6],ap3[7],ap3[8],ap3[9],0.5f);
				aap3Boundary[3] = Bezier.deCasteljau(ap3[9],ap3[10],ap3[11],ap3[12],0.5f);
				aap3Boundary[4] = Bezier.deCasteljau(ap3[12],ap3[13],ap3[14],ap3[0],0.5f);
				
				/* compute midpoint colors */
//				int[] mcol = new int[] {
//					lighting.shade(aap3Boundary[0][3], mn[0], mp),
//					lighting.shade(aap3Boundary[1][3], mn[1], mp),
//					lighting.shade(aap3Boundary[2][3], mn[2], mp),
//					lighting.shade(aap3Boundary[3][3], mn[3], mp),
//					lighting.shade(aap3Boundary[4][3], mn[4], mp)
//				};
				
//				int centerColor = lighting.shade(Center, centerNormal, mp);
				
				Point3f[] ap3Patches = new Point3f[12];
				
				int[] levels = new int[4];
				
				ap3Patches[0] = new Point3f(aap3Boundary[0][0]);
				ap3Patches[1] = new Point3f(aap3Boundary[0][1]);
				ap3Patches[2] = new Point3f(aap3Boundary[0][2]);
				ap3Patches[3] = new Point3f(aap3Boundary[0][3]);
				ap3Patches[11] = new Point3f(aap3Boundary[4][5]);
				ap3Patches[4] = new Point3f(aap3Boundary[0][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[14])));
				ap3Patches[10] = new Point3f(aap3Boundary[4][4]);
				ap3Patches[5] = new Point3f(W);
				ap3Patches[9] = new Point3f(aap3Boundary[4][3]);
				ap3Patches[8] = new Point3f(aap3Boundary[4][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[12],ap3[11])));
				ap3Patches[7] = new Point3f(V);
				ap3Patches[6] = new Point3f(Center);
				normals[0].set(av3[0]);
				normals[1].set(mn[0]);
				normals[2].set(centerNormal);
				normals[3].set(mn[4]);
//				colors[0] = ccol[0];
//				colors[1] = mcol[0];
//				colors[2] = centerColor;
//				colors[3] = mcol[4];
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[0] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[4] + 1;
				drawLitHashPatch4(ap3Patches, normals, flat, levels);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				    
				ap3Patches[0].set(aap3Boundary[1][0]);
				ap3Patches[1].set(aap3Boundary[1][1]);
				ap3Patches[2].set(aap3Boundary[1][2]);
				ap3Patches[3].set(aap3Boundary[1][3]);
				ap3Patches[11].set(aap3Boundary[0][5]);
				ap3Patches[4].set(aap3Boundary[1][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2])));
				ap3Patches[10].set(aap3Boundary[0][4]);
				ap3Patches[5].set(X);
				ap3Patches[9].set(aap3Boundary[0][3]);
				ap3Patches[8].set(aap3Boundary[0][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[14])));
				ap3Patches[7].set(W);
				ap3Patches[6].set(Center);
				normals[0].set(av3[1]);
				normals[1].set(mn[1]);
				normals[2].set(centerNormal);
				normals[3].set(mn[0]);
//				colors[0] = ccol[1];
//				colors[1] = mcol[1];
//				colors[2] = centerColor;
//				colors[3] = mcol[0];
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[1] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[0] + 1;
				drawLitHashPatch4(ap3Patches, normals, flat, levels);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				
				ap3Patches[0].set(aap3Boundary[2][0]);
				ap3Patches[1].set(aap3Boundary[2][1]);
				ap3Patches[2].set(aap3Boundary[2][2]);
				ap3Patches[3].set(aap3Boundary[2][3]);
				ap3Patches[11].set(aap3Boundary[1][5]);
				ap3Patches[4].set(aap3Boundary[2][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[9],ap3[10]),Functions.vector(ap3[6],ap3[5])));
				ap3Patches[10] = new Point3f(aap3Boundary[1][4]);
				ap3Patches[5].set(Y);
				ap3Patches[9].set(aap3Boundary[1][3]);
				ap3Patches[8].set(aap3Boundary[1][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2])));
				ap3Patches[7].set(X);
				ap3Patches[6].set(Center);
				normals[0].set(av3[2]);
				normals[1].set(mn[2]);
				normals[2].set(centerNormal);
				normals[3].set(mn[1]);
//				colors[0] = ccol[2];
//				colors[1] = mcol[2];
//				colors[2] = centerColor;
//				colors[3] = mcol[1];
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[2] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[1] + 1;
				drawLitHashPatch4(ap3Patches, normals, flat, levels);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				
				ap3Patches[0].set(aap3Boundary[3][0]);
				ap3Patches[1].set(aap3Boundary[3][1]);
				ap3Patches[2].set(aap3Boundary[3][2]);
				ap3Patches[3].set(aap3Boundary[3][3]);
				ap3Patches[11].set(aap3Boundary[2][5]);
				ap3Patches[4].set(aap3Boundary[3][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[9],ap3[8]),Functions.vector(ap3[12],ap3[13])));
				ap3Patches[10].set(aap3Boundary[2][4]);
				ap3Patches[5].set(Z);
				ap3Patches[9].set(aap3Boundary[2][3]);
				ap3Patches[8].set(aap3Boundary[2][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[9],ap3[10]),Functions.vector(ap3[6],ap3[5])));
				ap3Patches[7].set(Y);
				ap3Patches[6].set(Center);
				normals[0].set(av3[3]);
				normals[1].set(mn[3]);
				normals[2].set(centerNormal);
				normals[3].set(mn[2]);
//				colors[0] = ccol[3];
//				colors[1] = mcol[3];
//				colors[2] = centerColor;
//				colors[3] = mcol[2];
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[3] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[2] + 1;
				drawLitHashPatch4(ap3Patches, normals, flat, levels);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				
				ap3Patches[0].set(aap3Boundary[4][0]);
				ap3Patches[1].set(aap3Boundary[4][1]);
				ap3Patches[2].set(aap3Boundary[4][2]);
				ap3Patches[3].set(aap3Boundary[4][3]);
				ap3Patches[11].set(aap3Boundary[3][5]);
				ap3Patches[4].set(aap3Boundary[4][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[12],ap3[11]),Functions.vector(ap3[0],ap3[1])));
				ap3Patches[10].set(aap3Boundary[3][4]);
				ap3Patches[5].set(V);
				ap3Patches[9].set(aap3Boundary[3][3]);
				ap3Patches[8].set(aap3Boundary[3][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[9],ap3[8]),Functions.vector(ap3[12],ap3[13])));
				ap3Patches[7].set(Z);
				ap3Patches[6].set(Center);
				normals[0].set(av3[4]);
				normals[1].set(mn[4]);
				normals[2].set(centerNormal);
				normals[3].set(mn[3]);
//				colors[0] = ccol[4];
//				colors[1] = mcol[4];
//				colors[2] = centerColor;
//				colors[3] = mcol[3];
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[4] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[3] + 1;
				drawLitHashPatch4(ap3Patches, normals, flat, levels);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
			}
			break;
		}
	}
	
	public void drawShadedHashPatch(Point3f[] ap3, Vector3f[] av3, int[] ail, MaterialProperties mp) {
		boolean[] flat = new boolean[] { false, false, false, false };
		Color3f[] colors = newColors(4);
		switch (ap3.length) {
			case 9: {
				
				/* compute colors */
				viewDef.getLighting().shade(ap3[0], av3[0], mp, colors[0]);
				viewDef.getLighting().shade(ap3[3], av3[1], mp, colors[1]);
				viewDef.getLighting().shade(ap3[6], av3[2], mp, colors[2]);
				colors[3].set(colors[0]);
				
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
				
				Vector3f[] n = new Vector3f[] {av3[0], av3[1], av3[2], new Vector3f(av3[0])};
				
				Point3f[] p = new Point3f[] {ap3[0], ap3[1], ap3[2], ap3[3], ap3[4], ap3[5], ap3[6], ap3[7], ap3[8], new Point3f(ap3[0]), new Point3f(ap3[0]), new Point3f(ap3[0])};
				
				//p[0].set(ap3[0]);
				//p[1].set(ap3[1]);
				//p[2].set(ap3[2]);
				//p[3].set(ap3[3]);
				//p[4].set(ap3[4]);
				//p[5].set(ap3[5]);
				//p[6].set(ap3[6]);
				//p[7].set(ap3[7]);
				//p[8].set(ap3[8]);
				//p[9].set(ap3[0]);
				//p[10].set(ap3[0]);
				//p[11].set(ap3[0]);
				//
				//drawRectHashPatchGourad(p, n, mp, 0);
				int[] levels = new int[] { ail[0], ail[1], ail[2], 0 };
				drawShadedHashPatch4(p, n, colors, flat, levels, mp);
				//if (true) return;
				//
				/////* set up corner normals */
				////Vector3f[] cn = newNormals(3);
				////v3a.sub(ap3[1], ap3[0]);
				////v3b.sub(ap3[8], ap3[0]);
				////cn[0].cross(v3b, v3a);
				////v3a.sub(ap3[4], ap3[3]);
				////v3b.sub(ap3[2], ap3[3]);
				////cn[1].cross(v3b, v3a);
				////v3a.sub(ap3[7], ap3[6]);
				////v3b.sub(ap3[5], ap3[6]);
				////cn[2].cross(v3b, v3a);
				////cn[0].normalize();
				////cn[1].normalize();
				////cn[2].normalize();
				//
				////drawHashPatch3(ap3, cn, 0, mp);
				////if (true) return;
				//
				///* set up midpoint normals */
				//Vector3f[] mn = new Vector3f[5];
				//mn[0] = interpolateNormal(cn[0], cn[1], ap3[0], ap3[1], ap3[2], ap3[3]);
				//mn[1] = interpolateNormal(cn[1], cn[2], ap3[3], ap3[4], ap3[5], ap3[6]);
				//mn[2] = interpolateNormal(cn[2], cn[0], ap3[6], ap3[7], ap3[8], ap3[0]);
				//
				//Point3f A = Functions.parallelogram(ap3[0],ap3[1],ap3[8]);
				//Point3f B = Functions.parallelogram(ap3[3],ap3[4],ap3[2]);
				//Point3f C = Functions.parallelogram(ap3[6],ap3[7],ap3[5]);
				//
				//Point3f F = Functions.average(A,ap3[0],ap3[1],ap3[8]);
				//Point3f H = Functions.average(B,ap3[3],ap3[4],ap3[2]);
				//Point3f J = Functions.average(C,ap3[6],ap3[7],ap3[5]);
				//
				//Point3f G = Functions.average(A,B,ap3[1],ap3[2]);
				//Point3f I = Functions.average(B,C,ap3[4],ap3[5]);
				//Point3f K = Functions.average(C,A,ap3[7],ap3[8]);
				//
				//Point3f U = Functions.average(A,B,C);
				//
				//Point3f P = Functions.average(U,K,F,G);
				//Point3f Q = Functions.average(U,G,H,I);
				//Point3f R = Functions.average(U,I,J,K);
				//
				//Point3f V = Functions.average(R,P);
				//Point3f W = Functions.average(P,Q);
				//Point3f X = Functions.average(Q,R);
				//
				//Point3f Center = Functions.average(P,Q,R);
				//
				//Vector3f vc = Functions.vector(V, Center);
				//Vector3f wc = Functions.vector(W, Center);
				//Vector3f xc = Functions.vector(X, Center);
				//
				//Vector3f[] nc = newNormals(3);
				//nc[0].cross(wc, vc);
				//nc[1].cross(xc, wc);
				//nc[2].cross(vc, xc);
				//nc[0].normalize();
				//nc[1].normalize();
				//nc[2].normalize();
				//Vector3f centerNormal = Functions.vaverage(nc[0], nc[1], nc[2]);
				//centerNormal.normalize();
				//
				//Vector3f[] normals = newNormals(4);
				//
				//Point3f[][] aap3Boundary = new Point3f[3][7];
				//
				//aap3Boundary[0] = Bezier.deCasteljau(ap3[0],ap3[1],ap3[2],ap3[3],0.5f);
				//aap3Boundary[1] = Bezier.deCasteljau(ap3[3],ap3[4],ap3[5],ap3[6],0.5f);
				//aap3Boundary[2] = Bezier.deCasteljau(ap3[6],ap3[7],ap3[8],ap3[0],0.5f);
				//
				//Point3f[] ap3Patches = new Point3f[12];
				//
				//Vector3f v;
				//float sc = 0.33f;
				//
				//ap3Patches[0] = new Point3f(aap3Boundary[0][0]);
				//ap3Patches[1] = new Point3f(aap3Boundary[0][1]);
				//ap3Patches[2] = new Point3f(aap3Boundary[0][2]);
				//ap3Patches[3] = new Point3f(aap3Boundary[0][3]);
				//ap3Patches[11] = new Point3f(aap3Boundary[2][5]);
				//v = Functions.vaverage(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[8]));
				//v.scale(sc);
				//ap3Patches[4] = new Point3f(aap3Boundary[0][3]);
				//ap3Patches[4].add(v);
				////ap3Patches[4].add(Functions.average(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[8])));
				//ap3Patches[10] = new Point3f(aap3Boundary[2][4]);
				//ap3Patches[5] = new Point3f(W);
				//ap3Patches[9] = new Point3f(aap3Boundary[2][3]);
				//v = Functions.vaverage(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[6],ap3[5]));
				//v.scale(sc);
				//ap3Patches[8] = new Point3f(aap3Boundary[2][3]);
				//ap3Patches[8].add(v);
				////ap3Patches[8].add(Functions.average(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[6],ap3[5])));
				//ap3Patches[7] = new Point3f(V);
				//ap3Patches[6] = new Point3f(Center);
				//normals[0].set(cn[0]);
				//normals[1].set(mn[0]);
				//normals[2].set(centerNormal);
				//normals[3].set(mn[2]);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				//    
				//ap3Patches[0].set(aap3Boundary[1][0]);
				//ap3Patches[1].set(aap3Boundary[1][1]);
				//ap3Patches[2].set(aap3Boundary[1][2]);
				//ap3Patches[3].set(aap3Boundary[1][3]);
				//ap3Patches[11].set(aap3Boundary[0][5]);
				//v = Functions.vaverage(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2]));
				//v.scale(sc);
				//ap3Patches[4].add(aap3Boundary[1][3], v);
				////ap3Patches[4].add(Functions.average(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2])));
				//ap3Patches[10].set(aap3Boundary[0][4]);
				//ap3Patches[5].set(X);
				//ap3Patches[9].set(aap3Boundary[0][3]);
				//v = Functions.vaverage(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[8]));
				//v.scale(sc);
				//ap3Patches[8].add(aap3Boundary[0][3], v);
				////ap3Patches[8].add(Functions.average(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[8])));
				//ap3Patches[7].set(W);
				//ap3Patches[6].set(Center);
				//normals[0].set(cn[1]);
				//normals[1].set(mn[1]);
				//normals[2].set(centerNormal);
				//normals[3].set(mn[0]);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				//
				//ap3Patches[0].set(aap3Boundary[2][0]);
				//ap3Patches[1].set(aap3Boundary[2][1]);
				//ap3Patches[2].set(aap3Boundary[2][2]);
				//ap3Patches[3].set(aap3Boundary[2][3]);
				//ap3Patches[11].set(aap3Boundary[1][5]);
				//v = Functions.vaverage(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[6],ap3[5]));
				//v.scale(sc);
				//ap3Patches[4].add(aap3Boundary[2][3], v);
				////ap3Patches[4].add(Functions.average(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[6],ap3[5])));
				//ap3Patches[10] = new Point3f(aap3Boundary[1][4]);
				//ap3Patches[5].set(V);
				//ap3Patches[9].set(aap3Boundary[1][3]);
				//v = Functions.vaverage(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2]));
				//v.scale(sc);
				//ap3Patches[8].add(aap3Boundary[1][3], v);
				////ap3Patches[8].add(Functions.average(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2])));
				//ap3Patches[7].set(X);
				//ap3Patches[6].set(Center);
				//normals[0].set(cn[2]);
				//normals[1].set(mn[2]);
				//normals[2].set(centerNormal);
				//normals[3].set(mn[1]);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
			}
			break;
			case 12: {
				///* set up corner normals */
				//Vector3f[] normals = newNormals(4);
				//v3a.sub(ap3[1], ap3[0]);
				//v3b.sub(ap3[11], ap3[0]);
				//normals[0].cross(v3b, v3a);
				//v3a.sub(ap3[4], ap3[3]);
				//v3b.sub(ap3[2], ap3[3]);
				//normals[1].cross(v3b, v3a);
				//v3a.sub(ap3[7], ap3[6]);
				//v3b.sub(ap3[5], ap3[6]);
				//normals[2].cross(v3b, v3a);
				//v3a.sub(ap3[10], ap3[9]);
				//v3b.sub(ap3[8], ap3[9]);
				//normals[3].cross(v3b, v3a);
				//normals[0].normalize();
				//normals[1].normalize();
				//normals[2].normalize();
				//normals[3].normalize();
				viewDef.getLighting().shade(ap3[0], av3[0], mp, colors[0]);
				viewDef.getLighting().shade(ap3[3], av3[1], mp, colors[1]);
				viewDef.getLighting().shade(ap3[6], av3[2], mp, colors[2]);
				viewDef.getLighting().shade(ap3[9], av3[3], mp, colors[3]);
				drawShadedHashPatch4(ap3, av3, colors, flat, ail, mp);
				//drawRectHashPatchGourad(ap3, av3, mp, 0);
			}
			break;
			case 15: {
				///* set up corner normals */
				//Vector3f[] cn = newNormals(5);
				//v3a.sub(ap3[1], ap3[0]);
				//v3b.sub(ap3[14], ap3[0]);
				//cn[0].cross(v3b, v3a);
				//v3a.sub(ap3[4], ap3[3]);
				//v3b.sub(ap3[2], ap3[3]);
				//cn[1].cross(v3b, v3a);
				//v3a.sub(ap3[7], ap3[6]);
				//v3b.sub(ap3[5], ap3[6]);
				//cn[2].cross(v3b, v3a);
				//v3a.sub(ap3[10], ap3[9]);
				//v3b.sub(ap3[8], ap3[9]);
				//cn[3].cross(v3b, v3a);
				//v3a.sub(ap3[13], ap3[12]);
				//v3b.sub(ap3[11], ap3[12]);
				//cn[4].cross(v3b, v3a);
				//cn[0].normalize();
				//cn[1].normalize();
				//cn[2].normalize();
				//cn[3].normalize();
				//cn[4].normalize();
					
				/* set up midpoint normals */
				Vector3f[] mn = new Vector3f[5];
				mn[0] = interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]);
				mn[1] = interpolateNormal(av3[1], av3[2], ap3[3], ap3[4], ap3[5], ap3[6]);
				mn[2] = interpolateNormal(av3[2], av3[3], ap3[6], ap3[7], ap3[8], ap3[9]);
				mn[3] = interpolateNormal(av3[3], av3[4], ap3[9], ap3[10], ap3[11], ap3[12]);
				mn[4] = interpolateNormal(av3[4], av3[0], ap3[12], ap3[13], ap3[14], ap3[0]);
				
				/* compute corner-colors */
				Color3f[] ccol = newColors(5);
				
				viewDef.getLighting().shade(ap3[0], av3[0], mp, ccol[0]);
				viewDef.getLighting().shade(ap3[3], av3[1], mp, ccol[1]);
				viewDef.getLighting().shade(ap3[6], av3[2], mp, ccol[2]);
				viewDef.getLighting().shade(ap3[9], av3[3], mp, ccol[3]);
				viewDef.getLighting().shade(ap3[12], av3[4], mp, ccol[4]);
					
				Point3f A = Functions.parallelogram(ap3[0],ap3[1],ap3[14]);
				Point3f B = Functions.parallelogram(ap3[3],ap3[4],ap3[2]);
				Point3f C = Functions.parallelogram(ap3[6],ap3[7],ap3[5]);
				Point3f D = Functions.parallelogram(ap3[9],ap3[10],ap3[8]);
				Point3f E = Functions.parallelogram(ap3[12],ap3[13],ap3[11]);
				
				Point3f F = Functions.average(A,ap3[0],ap3[1],ap3[14]);
				Point3f H = Functions.average(B,ap3[3],ap3[4],ap3[2]);
				Point3f J = Functions.average(C,ap3[6],ap3[7],ap3[5]);
				Point3f L = Functions.average(D,ap3[9],ap3[10],ap3[8]);
				Point3f N = Functions.average(E,ap3[12],ap3[13],ap3[11]);
				
				Point3f G = Functions.average(A,B,ap3[1],ap3[2]);
				Point3f I = Functions.average(B,C,ap3[4],ap3[5]);
				Point3f K = Functions.average(C,D,ap3[7],ap3[8]);
				Point3f M = Functions.average(D,E,ap3[10],ap3[11]);
				Point3f O = Functions.average(E,A,ap3[13],ap3[14]);
				
				Point3f U = Functions.average(A,B,C,D,E);
				
				Point3f P = Functions.average(U,O,F,G);
				Point3f Q = Functions.average(U,G,H,I);
				Point3f R = Functions.average(U,I,J,K);
				Point3f S = Functions.average(U,K,L,M);
				Point3f T = Functions.average(U,M,N,O);
				
				Point3f V = Functions.average(T,P);
				Point3f W = Functions.average(P,Q);
				Point3f X = Functions.average(Q,R);
				Point3f Y = Functions.average(R,S);
				Point3f Z = Functions.average(S,T);
				
				Point3f Center = Functions.average(P,Q,R,S,T);
				
				Vector3f vc = Functions.vector(V, Center);
				Vector3f wc = Functions.vector(W, Center);
				Vector3f xc = Functions.vector(X, Center);
				Vector3f yc = Functions.vector(Y, Center);
				Vector3f zc = Functions.vector(Z, Center);
				
				Vector3f[] nc = newNormals(5);
//				nc[0].cross(wc, vc);
//				nc[1].cross(xc, wc);
//				nc[2].cross(yc, xc);
//				nc[3].cross(zc, yc);
//				nc[4].cross(vc, zc);
				nc[0].cross(vc, wc);
				nc[1].cross(wc, xc);
				nc[2].cross(xc, yc);
				nc[3].cross(yc, zc);
				nc[4].cross(zc, vc);
				nc[0].normalize();
				nc[1].normalize();
				nc[2].normalize();
				nc[3].normalize();
				nc[4].normalize();
				Vector3f centerNormal = Functions.vaverage(nc[0], nc[1], nc[2], nc[3], nc[4]);
				centerNormal.normalize();
				
				Vector3f[] normals = newNormals(4);
				
				Point3f[][] aap3Boundary = new Point3f[5][7];
				
				aap3Boundary[0] = Bezier.deCasteljau(ap3[0],ap3[1],ap3[2],ap3[3],0.5f);
				aap3Boundary[1] = Bezier.deCasteljau(ap3[3],ap3[4],ap3[5],ap3[6],0.5f);
				aap3Boundary[2] = Bezier.deCasteljau(ap3[6],ap3[7],ap3[8],ap3[9],0.5f);
				aap3Boundary[3] = Bezier.deCasteljau(ap3[9],ap3[10],ap3[11],ap3[12],0.5f);
				aap3Boundary[4] = Bezier.deCasteljau(ap3[12],ap3[13],ap3[14],ap3[0],0.5f);
				
				/* compute midpoint colors */
				//int[] mcol = new int[] {
				Color3f[] mcol = newColors(5);
				viewDef.getLighting().shade(aap3Boundary[0][3], mn[0], mp, mcol[0]);
				viewDef.getLighting().shade(aap3Boundary[1][3], mn[1], mp, mcol[1]);
				viewDef.getLighting().shade(aap3Boundary[2][3], mn[2], mp, mcol[2]);
				viewDef.getLighting().shade(aap3Boundary[3][3], mn[3], mp, mcol[3]);
				viewDef.getLighting().shade(aap3Boundary[4][3], mn[4], mp, mcol[4]);
				//};
				
				Color3f centerColor = new Color3f();
				viewDef.getLighting().shade(Center, centerNormal, mp, centerColor);
				
				Point3f[] ap3Patches = new Point3f[12];
				
				int[] levels = new int[4];
				
				ap3Patches[0] = new Point3f(aap3Boundary[0][0]);
				ap3Patches[1] = new Point3f(aap3Boundary[0][1]);
				ap3Patches[2] = new Point3f(aap3Boundary[0][2]);
				ap3Patches[3] = new Point3f(aap3Boundary[0][3]);
				ap3Patches[11] = new Point3f(aap3Boundary[4][5]);
				ap3Patches[4] = new Point3f(aap3Boundary[0][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[14])));
				ap3Patches[10] = new Point3f(aap3Boundary[4][4]);
				ap3Patches[5] = new Point3f(W);
				ap3Patches[9] = new Point3f(aap3Boundary[4][3]);
				ap3Patches[8] = new Point3f(aap3Boundary[4][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[12],ap3[11])));
				ap3Patches[7] = new Point3f(V);
				ap3Patches[6] = new Point3f(Center);
				normals[0].set(av3[0]);
				normals[1].set(mn[0]);
				normals[2].set(centerNormal);
				normals[3].set(mn[4]);
				colors[0].set(ccol[0]);
				colors[1].set(mcol[0]);
				colors[2].set(centerColor);
				colors[3].set(mcol[4]);
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[0] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[4] + 1;
				drawShadedHashPatch4(ap3Patches, normals, colors, flat, levels, mp);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				    
				ap3Patches[0].set(aap3Boundary[1][0]);
				ap3Patches[1].set(aap3Boundary[1][1]);
				ap3Patches[2].set(aap3Boundary[1][2]);
				ap3Patches[3].set(aap3Boundary[1][3]);
				ap3Patches[11].set(aap3Boundary[0][5]);
				ap3Patches[4].set(aap3Boundary[1][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2])));
				ap3Patches[10].set(aap3Boundary[0][4]);
				ap3Patches[5].set(X);
				ap3Patches[9].set(aap3Boundary[0][3]);
				ap3Patches[8].set(aap3Boundary[0][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[14])));
				ap3Patches[7].set(W);
				ap3Patches[6].set(Center);
				normals[0].set(av3[1]);
				normals[1].set(mn[1]);
				normals[2].set(centerNormal);
				normals[3].set(mn[0]);
				colors[0].set(ccol[1]);
				colors[1].set(mcol[1]);
				colors[2].set(centerColor);
				colors[3].set(mcol[0]);
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[1] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[0] + 1;
				drawShadedHashPatch4(ap3Patches, normals, colors, flat, levels, mp);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				
				ap3Patches[0].set(aap3Boundary[2][0]);
				ap3Patches[1].set(aap3Boundary[2][1]);
				ap3Patches[2].set(aap3Boundary[2][2]);
				ap3Patches[3].set(aap3Boundary[2][3]);
				ap3Patches[11].set(aap3Boundary[1][5]);
				ap3Patches[4].set(aap3Boundary[2][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[9],ap3[10]),Functions.vector(ap3[6],ap3[5])));
				ap3Patches[10] = new Point3f(aap3Boundary[1][4]);
				ap3Patches[5].set(Y);
				ap3Patches[9].set(aap3Boundary[1][3]);
				ap3Patches[8].set(aap3Boundary[1][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2])));
				ap3Patches[7].set(X);
				ap3Patches[6].set(Center);
				normals[0].set(av3[2]);
				normals[1].set(mn[2]);
				normals[2].set(centerNormal);
				normals[3].set(mn[1]);
				colors[0].set(ccol[2]);
				colors[1].set(mcol[2]);
				colors[2].set(centerColor);
				colors[3].set(mcol[1]);
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[2] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[1] + 1;
				drawShadedHashPatch4(ap3Patches, normals, colors, flat, levels, mp);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				
				ap3Patches[0].set(aap3Boundary[3][0]);
				ap3Patches[1].set(aap3Boundary[3][1]);
				ap3Patches[2].set(aap3Boundary[3][2]);
				ap3Patches[3].set(aap3Boundary[3][3]);
				ap3Patches[11].set(aap3Boundary[2][5]);
				ap3Patches[4].set(aap3Boundary[3][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[9],ap3[8]),Functions.vector(ap3[12],ap3[13])));
				ap3Patches[10].set(aap3Boundary[2][4]);
				ap3Patches[5].set(Z);
				ap3Patches[9].set(aap3Boundary[2][3]);
				ap3Patches[8].set(aap3Boundary[2][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[9],ap3[10]),Functions.vector(ap3[6],ap3[5])));
				ap3Patches[7].set(Y);
				ap3Patches[6].set(Center);
				normals[0].set(av3[3]);
				normals[1].set(mn[3]);
				normals[2].set(centerNormal);
				normals[3].set(mn[2]);
				colors[0].set(ccol[3]);
				colors[1].set(mcol[3]);
				colors[2].set(centerColor);
				colors[3].set(mcol[2]);
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[3] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[2] + 1;
				drawShadedHashPatch4(ap3Patches, normals, colors, flat, levels, mp);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				
				ap3Patches[0].set(aap3Boundary[4][0]);
				ap3Patches[1].set(aap3Boundary[4][1]);
				ap3Patches[2].set(aap3Boundary[4][2]);
				ap3Patches[3].set(aap3Boundary[4][3]);
				ap3Patches[11].set(aap3Boundary[3][5]);
				ap3Patches[4].set(aap3Boundary[4][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[12],ap3[11]),Functions.vector(ap3[0],ap3[1])));
				ap3Patches[10].set(aap3Boundary[3][4]);
				ap3Patches[5].set(V);
				ap3Patches[9].set(aap3Boundary[3][3]);
				ap3Patches[8].set(aap3Boundary[3][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[9],ap3[8]),Functions.vector(ap3[12],ap3[13])));
				ap3Patches[7].set(Z);
				ap3Patches[6].set(Center);
				normals[0].set(av3[4]);
				normals[1].set(mn[4]);
				normals[2].set(centerNormal);
				normals[3].set(mn[3]);
				colors[0].set(ccol[4]);
				colors[1].set(mcol[4]);
				colors[2].set(centerColor);
				colors[3].set(mcol[3]);
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[4] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[3] + 1;
				drawShadedHashPatch4(ap3Patches, normals, colors, flat, levels, mp);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
			}
			break;
		}
	}
	
	public void drawShadedHashPatchAlpha(Point3f[] ap3, Vector3f[] av3, int[] ail, MaterialProperties mp) {
		boolean[] flat = new boolean[] { false, false, false, false };
		Color4f[] colors = newColors4(4);
		switch (ap3.length) {
			case 9: {
				
				/* compute colors */
				viewDef.getLighting().shade(ap3[0], av3[0], mp, colors[0]);
				viewDef.getLighting().shade(ap3[3], av3[1], mp, colors[1]);
				viewDef.getLighting().shade(ap3[6], av3[2], mp, colors[2]);
				colors[3].set(colors[0]);
				
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
				
				Vector3f[] n = new Vector3f[] {av3[0], av3[1], av3[2], new Vector3f(av3[0])};
				
				Point3f[] p = new Point3f[] {ap3[0], ap3[1], ap3[2], ap3[3], ap3[4], ap3[5], ap3[6], ap3[7], ap3[8], new Point3f(ap3[0]), new Point3f(ap3[0]), new Point3f(ap3[0])};
				
				//p[0].set(ap3[0]);
				//p[1].set(ap3[1]);
				//p[2].set(ap3[2]);
				//p[3].set(ap3[3]);
				//p[4].set(ap3[4]);
				//p[5].set(ap3[5]);
				//p[6].set(ap3[6]);
				//p[7].set(ap3[7]);
				//p[8].set(ap3[8]);
				//p[9].set(ap3[0]);
				//p[10].set(ap3[0]);
				//p[11].set(ap3[0]);
				//
				//drawRectHashPatchGourad(p, n, mp, 0);
				int[] levels = new int[] { ail[0], ail[1], ail[2], 0 };
				drawShadedHashPatch4Alpha(p, n, colors, flat, levels, mp);
				//if (true) return;
				//
				/////* set up corner normals */
				////Vector3f[] cn = newNormals(3);
				////v3a.sub(ap3[1], ap3[0]);
				////v3b.sub(ap3[8], ap3[0]);
				////cn[0].cross(v3b, v3a);
				////v3a.sub(ap3[4], ap3[3]);
				////v3b.sub(ap3[2], ap3[3]);
				////cn[1].cross(v3b, v3a);
				////v3a.sub(ap3[7], ap3[6]);
				////v3b.sub(ap3[5], ap3[6]);
				////cn[2].cross(v3b, v3a);
				////cn[0].normalize();
				////cn[1].normalize();
				////cn[2].normalize();
				//
				////drawHashPatch3(ap3, cn, 0, mp);
				////if (true) return;
				//
				///* set up midpoint normals */
				//Vector3f[] mn = new Vector3f[5];
				//mn[0] = interpolateNormal(cn[0], cn[1], ap3[0], ap3[1], ap3[2], ap3[3]);
				//mn[1] = interpolateNormal(cn[1], cn[2], ap3[3], ap3[4], ap3[5], ap3[6]);
				//mn[2] = interpolateNormal(cn[2], cn[0], ap3[6], ap3[7], ap3[8], ap3[0]);
				//
				//Point3f A = Functions.parallelogram(ap3[0],ap3[1],ap3[8]);
				//Point3f B = Functions.parallelogram(ap3[3],ap3[4],ap3[2]);
				//Point3f C = Functions.parallelogram(ap3[6],ap3[7],ap3[5]);
				//
				//Point3f F = Functions.average(A,ap3[0],ap3[1],ap3[8]);
				//Point3f H = Functions.average(B,ap3[3],ap3[4],ap3[2]);
				//Point3f J = Functions.average(C,ap3[6],ap3[7],ap3[5]);
				//
				//Point3f G = Functions.average(A,B,ap3[1],ap3[2]);
				//Point3f I = Functions.average(B,C,ap3[4],ap3[5]);
				//Point3f K = Functions.average(C,A,ap3[7],ap3[8]);
				//
				//Point3f U = Functions.average(A,B,C);
				//
				//Point3f P = Functions.average(U,K,F,G);
				//Point3f Q = Functions.average(U,G,H,I);
				//Point3f R = Functions.average(U,I,J,K);
				//
				//Point3f V = Functions.average(R,P);
				//Point3f W = Functions.average(P,Q);
				//Point3f X = Functions.average(Q,R);
				//
				//Point3f Center = Functions.average(P,Q,R);
				//
				//Vector3f vc = Functions.vector(V, Center);
				//Vector3f wc = Functions.vector(W, Center);
				//Vector3f xc = Functions.vector(X, Center);
				//
				//Vector3f[] nc = newNormals(3);
				//nc[0].cross(wc, vc);
				//nc[1].cross(xc, wc);
				//nc[2].cross(vc, xc);
				//nc[0].normalize();
				//nc[1].normalize();
				//nc[2].normalize();
				//Vector3f centerNormal = Functions.vaverage(nc[0], nc[1], nc[2]);
				//centerNormal.normalize();
				//
				//Vector3f[] normals = newNormals(4);
				//
				//Point3f[][] aap3Boundary = new Point3f[3][7];
				//
				//aap3Boundary[0] = Bezier.deCasteljau(ap3[0],ap3[1],ap3[2],ap3[3],0.5f);
				//aap3Boundary[1] = Bezier.deCasteljau(ap3[3],ap3[4],ap3[5],ap3[6],0.5f);
				//aap3Boundary[2] = Bezier.deCasteljau(ap3[6],ap3[7],ap3[8],ap3[0],0.5f);
				//
				//Point3f[] ap3Patches = new Point3f[12];
				//
				//Vector3f v;
				//float sc = 0.33f;
				//
				//ap3Patches[0] = new Point3f(aap3Boundary[0][0]);
				//ap3Patches[1] = new Point3f(aap3Boundary[0][1]);
				//ap3Patches[2] = new Point3f(aap3Boundary[0][2]);
				//ap3Patches[3] = new Point3f(aap3Boundary[0][3]);
				//ap3Patches[11] = new Point3f(aap3Boundary[2][5]);
				//v = Functions.vaverage(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[8]));
				//v.scale(sc);
				//ap3Patches[4] = new Point3f(aap3Boundary[0][3]);
				//ap3Patches[4].add(v);
				////ap3Patches[4].add(Functions.average(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[8])));
				//ap3Patches[10] = new Point3f(aap3Boundary[2][4]);
				//ap3Patches[5] = new Point3f(W);
				//ap3Patches[9] = new Point3f(aap3Boundary[2][3]);
				//v = Functions.vaverage(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[6],ap3[5]));
				//v.scale(sc);
				//ap3Patches[8] = new Point3f(aap3Boundary[2][3]);
				//ap3Patches[8].add(v);
				////ap3Patches[8].add(Functions.average(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[6],ap3[5])));
				//ap3Patches[7] = new Point3f(V);
				//ap3Patches[6] = new Point3f(Center);
				//normals[0].set(cn[0]);
				//normals[1].set(mn[0]);
				//normals[2].set(centerNormal);
				//normals[3].set(mn[2]);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				//    
				//ap3Patches[0].set(aap3Boundary[1][0]);
				//ap3Patches[1].set(aap3Boundary[1][1]);
				//ap3Patches[2].set(aap3Boundary[1][2]);
				//ap3Patches[3].set(aap3Boundary[1][3]);
				//ap3Patches[11].set(aap3Boundary[0][5]);
				//v = Functions.vaverage(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2]));
				//v.scale(sc);
				//ap3Patches[4].add(aap3Boundary[1][3], v);
				////ap3Patches[4].add(Functions.average(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2])));
				//ap3Patches[10].set(aap3Boundary[0][4]);
				//ap3Patches[5].set(X);
				//ap3Patches[9].set(aap3Boundary[0][3]);
				//v = Functions.vaverage(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[8]));
				//v.scale(sc);
				//ap3Patches[8].add(aap3Boundary[0][3], v);
				////ap3Patches[8].add(Functions.average(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[8])));
				//ap3Patches[7].set(W);
				//ap3Patches[6].set(Center);
				//normals[0].set(cn[1]);
				//normals[1].set(mn[1]);
				//normals[2].set(centerNormal);
				//normals[3].set(mn[0]);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				//
				//ap3Patches[0].set(aap3Boundary[2][0]);
				//ap3Patches[1].set(aap3Boundary[2][1]);
				//ap3Patches[2].set(aap3Boundary[2][2]);
				//ap3Patches[3].set(aap3Boundary[2][3]);
				//ap3Patches[11].set(aap3Boundary[1][5]);
				//v = Functions.vaverage(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[6],ap3[5]));
				//v.scale(sc);
				//ap3Patches[4].add(aap3Boundary[2][3], v);
				////ap3Patches[4].add(Functions.average(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[6],ap3[5])));
				//ap3Patches[10] = new Point3f(aap3Boundary[1][4]);
				//ap3Patches[5].set(V);
				//ap3Patches[9].set(aap3Boundary[1][3]);
				//v = Functions.vaverage(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2]));
				//v.scale(sc);
				//ap3Patches[8].add(aap3Boundary[1][3], v);
				////ap3Patches[8].add(Functions.average(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2])));
				//ap3Patches[7].set(X);
				//ap3Patches[6].set(Center);
				//normals[0].set(cn[2]);
				//normals[1].set(mn[2]);
				//normals[2].set(centerNormal);
				//normals[3].set(mn[1]);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
			}
			break;
			case 12: {
				///* set up corner normals */
				//Vector3f[] normals = newNormals(4);
				//v3a.sub(ap3[1], ap3[0]);
				//v3b.sub(ap3[11], ap3[0]);
				//normals[0].cross(v3b, v3a);
				//v3a.sub(ap3[4], ap3[3]);
				//v3b.sub(ap3[2], ap3[3]);
				//normals[1].cross(v3b, v3a);
				//v3a.sub(ap3[7], ap3[6]);
				//v3b.sub(ap3[5], ap3[6]);
				//normals[2].cross(v3b, v3a);
				//v3a.sub(ap3[10], ap3[9]);
				//v3b.sub(ap3[8], ap3[9]);
				//normals[3].cross(v3b, v3a);
				//normals[0].normalize();
				//normals[1].normalize();
				//normals[2].normalize();
				//normals[3].normalize();
				viewDef.getLighting().shade(ap3[0], av3[0], mp, colors[0]);
				viewDef.getLighting().shade(ap3[3], av3[1], mp, colors[1]);
				viewDef.getLighting().shade(ap3[6], av3[2], mp, colors[2]);
				viewDef.getLighting().shade(ap3[9], av3[3], mp, colors[3]);
				drawShadedHashPatch4Alpha(ap3, av3, colors, flat, ail, mp);
				//drawRectHashPatchGourad(ap3, av3, mp, 0);
			}
			break;
			case 15: {
				///* set up corner normals */
				//Vector3f[] cn = newNormals(5);
				//v3a.sub(ap3[1], ap3[0]);
				//v3b.sub(ap3[14], ap3[0]);
				//cn[0].cross(v3b, v3a);
				//v3a.sub(ap3[4], ap3[3]);
				//v3b.sub(ap3[2], ap3[3]);
				//cn[1].cross(v3b, v3a);
				//v3a.sub(ap3[7], ap3[6]);
				//v3b.sub(ap3[5], ap3[6]);
				//cn[2].cross(v3b, v3a);
				//v3a.sub(ap3[10], ap3[9]);
				//v3b.sub(ap3[8], ap3[9]);
				//cn[3].cross(v3b, v3a);
				//v3a.sub(ap3[13], ap3[12]);
				//v3b.sub(ap3[11], ap3[12]);
				//cn[4].cross(v3b, v3a);
				//cn[0].normalize();
				//cn[1].normalize();
				//cn[2].normalize();
				//cn[3].normalize();
				//cn[4].normalize();
					
				/* set up midpoint normals */
				Vector3f[] mn = new Vector3f[5];
				mn[0] = interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]);
				mn[1] = interpolateNormal(av3[1], av3[2], ap3[3], ap3[4], ap3[5], ap3[6]);
				mn[2] = interpolateNormal(av3[2], av3[3], ap3[6], ap3[7], ap3[8], ap3[9]);
				mn[3] = interpolateNormal(av3[3], av3[4], ap3[9], ap3[10], ap3[11], ap3[12]);
				mn[4] = interpolateNormal(av3[4], av3[0], ap3[12], ap3[13], ap3[14], ap3[0]);
				
				/* compute corner-colors */
				Color4f[] ccol = newColors4(5);
				
				viewDef.getLighting().shade(ap3[0], av3[0], mp, ccol[0]);
				viewDef.getLighting().shade(ap3[3], av3[1], mp, ccol[1]);
				viewDef.getLighting().shade(ap3[6], av3[2], mp, ccol[2]);
				viewDef.getLighting().shade(ap3[9], av3[3], mp, ccol[3]);
				viewDef.getLighting().shade(ap3[12], av3[4], mp, ccol[4]);
					
				Point3f A = Functions.parallelogram(ap3[0],ap3[1],ap3[14]);
				Point3f B = Functions.parallelogram(ap3[3],ap3[4],ap3[2]);
				Point3f C = Functions.parallelogram(ap3[6],ap3[7],ap3[5]);
				Point3f D = Functions.parallelogram(ap3[9],ap3[10],ap3[8]);
				Point3f E = Functions.parallelogram(ap3[12],ap3[13],ap3[11]);
				
				Point3f F = Functions.average(A,ap3[0],ap3[1],ap3[14]);
				Point3f H = Functions.average(B,ap3[3],ap3[4],ap3[2]);
				Point3f J = Functions.average(C,ap3[6],ap3[7],ap3[5]);
				Point3f L = Functions.average(D,ap3[9],ap3[10],ap3[8]);
				Point3f N = Functions.average(E,ap3[12],ap3[13],ap3[11]);
				
				Point3f G = Functions.average(A,B,ap3[1],ap3[2]);
				Point3f I = Functions.average(B,C,ap3[4],ap3[5]);
				Point3f K = Functions.average(C,D,ap3[7],ap3[8]);
				Point3f M = Functions.average(D,E,ap3[10],ap3[11]);
				Point3f O = Functions.average(E,A,ap3[13],ap3[14]);
				
				Point3f U = Functions.average(A,B,C,D,E);
				
				Point3f P = Functions.average(U,O,F,G);
				Point3f Q = Functions.average(U,G,H,I);
				Point3f R = Functions.average(U,I,J,K);
				Point3f S = Functions.average(U,K,L,M);
				Point3f T = Functions.average(U,M,N,O);
				
				Point3f V = Functions.average(T,P);
				Point3f W = Functions.average(P,Q);
				Point3f X = Functions.average(Q,R);
				Point3f Y = Functions.average(R,S);
				Point3f Z = Functions.average(S,T);
				
				Point3f Center = Functions.average(P,Q,R,S,T);
				
				Vector3f vc = Functions.vector(V, Center);
				Vector3f wc = Functions.vector(W, Center);
				Vector3f xc = Functions.vector(X, Center);
				Vector3f yc = Functions.vector(Y, Center);
				Vector3f zc = Functions.vector(Z, Center);
				
				Vector3f[] nc = newNormals(5);
//				nc[0].cross(wc, vc);
//				nc[1].cross(xc, wc);
//				nc[2].cross(yc, xc);
//				nc[3].cross(zc, yc);
//				nc[4].cross(vc, zc);
				nc[0].cross(vc, wc);
				nc[1].cross(wc, xc);
				nc[2].cross(xc, yc);
				nc[3].cross(yc, zc);
				nc[4].cross(zc, vc);
				nc[0].normalize();
				nc[1].normalize();
				nc[2].normalize();
				nc[3].normalize();
				nc[4].normalize();
				Vector3f centerNormal = Functions.vaverage(nc[0], nc[1], nc[2], nc[3], nc[4]);
				centerNormal.normalize();
				
				Vector3f[] normals = newNormals(4);
				
				Point3f[][] aap3Boundary = new Point3f[5][7];
				
				aap3Boundary[0] = Bezier.deCasteljau(ap3[0],ap3[1],ap3[2],ap3[3],0.5f);
				aap3Boundary[1] = Bezier.deCasteljau(ap3[3],ap3[4],ap3[5],ap3[6],0.5f);
				aap3Boundary[2] = Bezier.deCasteljau(ap3[6],ap3[7],ap3[8],ap3[9],0.5f);
				aap3Boundary[3] = Bezier.deCasteljau(ap3[9],ap3[10],ap3[11],ap3[12],0.5f);
				aap3Boundary[4] = Bezier.deCasteljau(ap3[12],ap3[13],ap3[14],ap3[0],0.5f);
				
				/* compute midpoint colors */
				//int[] mcol = new int[] {
				Color4f[] mcol = newColors4(5);
				viewDef.getLighting().shade(aap3Boundary[0][3], mn[0], mp, mcol[0]);
				viewDef.getLighting().shade(aap3Boundary[1][3], mn[1], mp, mcol[1]);
				viewDef.getLighting().shade(aap3Boundary[2][3], mn[2], mp, mcol[2]);
				viewDef.getLighting().shade(aap3Boundary[3][3], mn[3], mp, mcol[3]);
				viewDef.getLighting().shade(aap3Boundary[4][3], mn[4], mp, mcol[4]);
				//};
				
				Color4f centerColor = new Color4f();
				viewDef.getLighting().shade(Center, centerNormal, mp, centerColor);
				
				Point3f[] ap3Patches = new Point3f[12];
				
				int[] levels = new int[4];
				
				ap3Patches[0] = new Point3f(aap3Boundary[0][0]);
				ap3Patches[1] = new Point3f(aap3Boundary[0][1]);
				ap3Patches[2] = new Point3f(aap3Boundary[0][2]);
				ap3Patches[3] = new Point3f(aap3Boundary[0][3]);
				ap3Patches[11] = new Point3f(aap3Boundary[4][5]);
				ap3Patches[4] = new Point3f(aap3Boundary[0][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[14])));
				ap3Patches[10] = new Point3f(aap3Boundary[4][4]);
				ap3Patches[5] = new Point3f(W);
				ap3Patches[9] = new Point3f(aap3Boundary[4][3]);
				ap3Patches[8] = new Point3f(aap3Boundary[4][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[0],ap3[1]),Functions.vector(ap3[12],ap3[11])));
				ap3Patches[7] = new Point3f(V);
				ap3Patches[6] = new Point3f(Center);
				normals[0].set(av3[0]);
				normals[1].set(mn[0]);
				normals[2].set(centerNormal);
				normals[3].set(mn[4]);
				colors[0].set(ccol[0]);
				colors[1].set(mcol[0]);
				colors[2].set(centerColor);
				colors[3].set(mcol[4]);
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[0] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[4] + 1;
				drawShadedHashPatch4Alpha(ap3Patches, normals, colors, flat, levels, mp);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				    
				ap3Patches[0].set(aap3Boundary[1][0]);
				ap3Patches[1].set(aap3Boundary[1][1]);
				ap3Patches[2].set(aap3Boundary[1][2]);
				ap3Patches[3].set(aap3Boundary[1][3]);
				ap3Patches[11].set(aap3Boundary[0][5]);
				ap3Patches[4].set(aap3Boundary[1][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2])));
				ap3Patches[10].set(aap3Boundary[0][4]);
				ap3Patches[5].set(X);
				ap3Patches[9].set(aap3Boundary[0][3]);
				ap3Patches[8].set(aap3Boundary[0][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[3],ap3[4]),Functions.vector(ap3[0],ap3[14])));
				ap3Patches[7].set(W);
				ap3Patches[6].set(Center);
				normals[0].set(av3[1]);
				normals[1].set(mn[1]);
				normals[2].set(centerNormal);
				normals[3].set(mn[0]);
				colors[0].set(ccol[1]);
				colors[1].set(mcol[1]);
				colors[2].set(centerColor);
				colors[3].set(mcol[0]);
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[1] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[0] + 1;
				drawShadedHashPatch4Alpha(ap3Patches, normals, colors, flat, levels, mp);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				
				ap3Patches[0].set(aap3Boundary[2][0]);
				ap3Patches[1].set(aap3Boundary[2][1]);
				ap3Patches[2].set(aap3Boundary[2][2]);
				ap3Patches[3].set(aap3Boundary[2][3]);
				ap3Patches[11].set(aap3Boundary[1][5]);
				ap3Patches[4].set(aap3Boundary[2][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[9],ap3[10]),Functions.vector(ap3[6],ap3[5])));
				ap3Patches[10] = new Point3f(aap3Boundary[1][4]);
				ap3Patches[5].set(Y);
				ap3Patches[9].set(aap3Boundary[1][3]);
				ap3Patches[8].set(aap3Boundary[1][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[6],ap3[7]),Functions.vector(ap3[3],ap3[2])));
				ap3Patches[7].set(X);
				ap3Patches[6].set(Center);
				normals[0].set(av3[2]);
				normals[1].set(mn[2]);
				normals[2].set(centerNormal);
				normals[3].set(mn[1]);
				colors[0].set(ccol[2]);
				colors[1].set(mcol[2]);
				colors[2].set(centerColor);
				colors[3].set(mcol[1]);
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[2] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[1] + 1;
				drawShadedHashPatch4Alpha(ap3Patches, normals, colors, flat, levels, mp);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				
				ap3Patches[0].set(aap3Boundary[3][0]);
				ap3Patches[1].set(aap3Boundary[3][1]);
				ap3Patches[2].set(aap3Boundary[3][2]);
				ap3Patches[3].set(aap3Boundary[3][3]);
				ap3Patches[11].set(aap3Boundary[2][5]);
				ap3Patches[4].set(aap3Boundary[3][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[9],ap3[8]),Functions.vector(ap3[12],ap3[13])));
				ap3Patches[10].set(aap3Boundary[2][4]);
				ap3Patches[5].set(Z);
				ap3Patches[9].set(aap3Boundary[2][3]);
				ap3Patches[8].set(aap3Boundary[2][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[9],ap3[10]),Functions.vector(ap3[6],ap3[5])));
				ap3Patches[7].set(Y);
				ap3Patches[6].set(Center);
				normals[0].set(av3[3]);
				normals[1].set(mn[3]);
				normals[2].set(centerNormal);
				normals[3].set(mn[2]);
				colors[0].set(ccol[3]);
				colors[1].set(mcol[3]);
				colors[2].set(centerColor);
				colors[3].set(mcol[2]);
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[3] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[2] + 1;
				drawShadedHashPatch4Alpha(ap3Patches, normals, colors, flat, levels, mp);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
				
				ap3Patches[0].set(aap3Boundary[4][0]);
				ap3Patches[1].set(aap3Boundary[4][1]);
				ap3Patches[2].set(aap3Boundary[4][2]);
				ap3Patches[3].set(aap3Boundary[4][3]);
				ap3Patches[11].set(aap3Boundary[3][5]);
				ap3Patches[4].set(aap3Boundary[4][3]);
				ap3Patches[4].add(Functions.average(Functions.vector(ap3[12],ap3[11]),Functions.vector(ap3[0],ap3[1])));
				ap3Patches[10].set(aap3Boundary[3][4]);
				ap3Patches[5].set(V);
				ap3Patches[9].set(aap3Boundary[3][3]);
				ap3Patches[8].set(aap3Boundary[3][3]);
				ap3Patches[8].add(Functions.average(Functions.vector(ap3[9],ap3[8]),Functions.vector(ap3[12],ap3[13])));
				ap3Patches[7].set(Z);
				ap3Patches[6].set(Center);
				normals[0].set(av3[4]);
				normals[1].set(mn[4]);
				normals[2].set(centerNormal);
				normals[3].set(mn[3]);
				colors[0].set(ccol[4]);
				colors[1].set(mcol[4]);
				colors[2].set(centerColor);
				colors[3].set(mcol[3]);
				flat[0] = flat[1] = flat[2] = flat[3] = false;
				levels[0] = ail[4] + 1;
				levels[1] = 1;
				levels[2] = 1;
				levels[3] = ail[3] + 1;
				drawShadedHashPatch4Alpha(ap3Patches, normals, colors, flat, levels, mp);
				//drawRectHashPatchGourad(ap3Patches, normals, mp, 1);
			}
			break;
		}
	}
	
	private void drawLitHashPatch4(Point3f[] ap3, Vector3f[] av3, boolean[] abFlat, int[] aiLevel) {
		
		//materialProperties = mp;
		//Point3d[] adp3 = new Point3d[ap3.length];
		//for (int i = 0; i < adp3.length; adp3[i] = new Point3d(ap3[i++]));
		//hashPatchSubdivision.subdivHashPatch4(adp3, adp3, av3, 2, new Point3d[][] { null, null, null, null }, new Vector3f[][] { null, null, null, null });
		//
		//if (true) return;
		
		boolean visible = false;
		int width = drawable.getComponent().getWidth() >> 1;
		int height = drawable.getComponent().getHeight() >> 1;
		loop:
		for (int i = 0; i < 12; i++) {
			if (ap3[i].x > -width && ap3[i].x < width && ap3[i].y > -height && ap3[i].y < height) {
				visible = true;
				break loop;
			}
		}
		if (!visible) return;
		//System.out.println(abFlat[0] + "\t" + abFlat[1] + "\t" + abFlat[2] + "\t" + abFlat[3]);
		
		/* check if we need to u-split */
		float u0 = subdiv(ap3[0], ap3[1], ap3[2], ap3[3], aiLevel[0] > 0);
		float u2 = subdiv(ap3[9], ap3[8], ap3[7], ap3[6], aiLevel[2] > 0);
		float v3 = subdiv(ap3[0], ap3[11], ap3[10], ap3[9], aiLevel[3] > 0);
		float v1 = subdiv(ap3[3], ap3[4], ap3[5], ap3[6], aiLevel[1] > 0);
		
		float ul = (aiLevel[0] >= iMaxSubdiv || aiLevel[2] >= iMaxSubdiv) ? -1 : (float) Math.max(u0, u2);
		float uv = (aiLevel[1] >= iMaxSubdiv || aiLevel[3] >= iMaxSubdiv) ? -1 : (float) Math.max(v1, v3);
		
		if (true && ul >= fFlatness && ul > uv * 1.1f) {
		//if (false) {
		//if (subdiv(ap3[0], ap3[1], ap3[2], ap3[3], uSplit) || subdiv(ap3[9], ap3[8], ap3[7], ap3[6], uSplit)) {
			
			/* flatten cubics if flat enough */
			if (!abFlat[0] && (u0 <= fFlatness)) abFlat[0] = makeFlat(ap3[0], ap3[1], ap3[2], ap3[3]);
			if (!abFlat[1] && (v1 <= fFlatness)) abFlat[1] = makeFlat(ap3[3], ap3[4], ap3[5], ap3[6]);
			if (!abFlat[2] && (u2 <= fFlatness)) abFlat[2] = makeFlat(ap3[9], ap3[8], ap3[7], ap3[6]);
			if (!abFlat[3] && (v3 <= fFlatness)) abFlat[3] = makeFlat(ap3[0], ap3[11], ap3[10], ap3[9]);
			
			
			/* compute new normals */
			Vector3f[] av3new = newNormals(4);
			av3new[1].set(av3[1]);
			av3new[0] = av3[1] = (aiLevel[0] > 0) ? interpolateNormal(av3[0], av3[1]) : interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]);
			av3new[2].set(av3[2]);
			av3new[3] = av3[2] = (aiLevel[2] > 0) ? interpolateNormal(av3[3], av3[2]) : interpolateNormal(av3[3], av3[2], ap3[9], ap3[8], ap3[7], ap3[6]);
			
			/* compute new patches */
			Point3f[] ap3new = newPatch(12);
			v3a.sub(ap3[11], ap3[0]);
			v3b.sub(ap3[4], ap3[3]);
			v3c.add(v3a, v3b);
			v3c.scale(0.5f);
			v3a.sub(ap3[10], ap3[9]);
			v3b.sub(ap3[5], ap3[6]);
			v3a.add(v3b);
			v3a.scale(0.5f);
			deCasteljauSplit(ap3[0], ap3[1], ap3[2], ap3[3], ap3new[0], ap3new[1], ap3new[2], ap3new[3]);
			deCasteljauSplit(ap3[9], ap3[8], ap3[7], ap3[6], ap3new[9], ap3new[8], ap3new[7], ap3new[6]);
			ap3new[4].set(ap3[4]);
			ap3new[5].set(ap3[5]);
			ap3[4].add(ap3[3], v3c);
			ap3[5].add(ap3[6], v3a);
			ap3new[11].set(ap3[4]);
			ap3new[10].set(ap3[5]);
			
			///* compute new normals */
			//Vector3f[] av3new = newNormals(4);
			//av3new[1].set(av3[1]);
			////av3new[0] = av3[1] = (uLevel > 99) ? interpolateNormal(av3[0], av3[1]) : interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]);
			//av3new[2].set(av3[2]);
			////av3new[3] = av3[2] = (uLevel > 99) ? interpolateNormal(av3[3], av3[2]) : interpolateNormal(av3[3], av3[2], ap3[9], ap3[8], ap3[7], ap3[6]);
			//
			//v3a.sub(ap3[4], ap3[3]);
			//v3b.sub(ap3[2], ap3[3]);
			//av3[1].cross(v3b, v3a);
			//av3[1].normalize();
			//av3new[0].set(av3[1]);
			//v3a.sub(ap3[5], ap3[6]);
			//v3b.sub(ap3[7], ap3[6]);
			//av3[2].cross(v3a, v3b);
			//av3[2].normalize();
			//av3new[3].set(av3[2]);
			
			/* compute new colors */
//			int[] acnew = new int[4];
//			acnew[1] = ac[1];
//			acnew[0] = ac[1] = (!abFlat[0]) ? lighting.shade(ap3[3], av3[1], mp) : interpolateColor(ac[0], ac[1]);
//			acnew[2] = ac[2];
//			acnew[3] = ac[2] = (!abFlat[2]) ? lighting.shade(ap3[6], av3[2], mp) : interpolateColor(ac[2], ac[3]);
			
			/* set up new flatenough flags */
			boolean[] abnew = new boolean[4];
			abnew[0] = abFlat[0];
			abnew[1] = abFlat[1];
			abnew[2] = abFlat[2];
			abnew[3] = abFlat[1] = false;
			
			///* compute new normals */
			//Vector3f[] av3new = newNormals();
			//av3new[1].set(av3[1]);
			//v3b.sub(ap3[3],ap3[2]);
			//av3[1].cross(v3b, v3c);
			//av3[1].normalize();
			//av3new[0].set(av3[1]);
			//av3new[2].set(av3[2]);
			//v3b.sub(ap3[7],ap3[6]);
			//av3[2].cross(v3b, v3a);
			//av3[2].normalize();
			//av3new[3].set(av3[2]);
			
			/* recurse */
			int l = (aiLevel[1] < aiLevel[3]) ? aiLevel[1] : aiLevel[3];
			aiLevel[0]++;
			aiLevel[2]++;
			int[] newLevels = new int[aiLevel.length];
			for (int i = 0; i < aiLevel.length; newLevels[i] = aiLevel[i++]);
			aiLevel[1] = newLevels[3] = l;
			drawLitHashPatch4(ap3, av3, abFlat, aiLevel);
			drawLitHashPatch4(ap3new, av3new, abnew, newLevels);
			return;
		}
		/* check if we need to v-split */
		else if (true && uv >= fFlatness) {
		//else if (false) {
		//else if (subdiv(ap3[0], ap3[11], ap3[10], ap3[9], vSplit) || subdiv(ap3[3], ap3[4], ap3[5], ap3[6], vSplit)) {
			/* flatten cubics if flat enough */
			if (!abFlat[0] && (u0 <= fFlatness)) abFlat[0] = makeFlat(ap3[0], ap3[1], ap3[2], ap3[3]);
			if (!abFlat[1] && (v1 <= fFlatness)) abFlat[1] = makeFlat(ap3[3], ap3[4], ap3[5], ap3[6]);
			if (!abFlat[2] && (u2 <= fFlatness)) abFlat[2] = makeFlat(ap3[9], ap3[8], ap3[7], ap3[6]);
			if (!abFlat[3] && (v3 <= fFlatness)) abFlat[3] = makeFlat(ap3[0], ap3[11], ap3[10], ap3[9]);
			
			
			/* compute new normals */
			Vector3f[] av3new = newNormals(4);
			av3new[3].set(av3[3]);
			av3new[0] = av3[3] = (aiLevel[3] > 0) ? interpolateNormal(av3[0], av3[3]) : interpolateNormal(av3[0], av3[3], ap3[0], ap3[11], ap3[10], ap3[9]);
			av3new[2].set(av3[2]);
			av3new[1] = av3[2] = (aiLevel[1] > 0) ? interpolateNormal(av3[1], av3[2]) : interpolateNormal(av3[1], av3[2], ap3[3], ap3[4], ap3[5], ap3[6]);
			
			/* compute new patches */
			Point3f[] ap3new = newPatch(12);
			v3a.sub(ap3[1], ap3[0]);
			v3b.sub(ap3[8], ap3[9]);
			v3c.add(v3a, v3b);
			v3c.scale(0.5f);
			v3a.sub(ap3[2], ap3[3]);
			v3b.sub(ap3[7], ap3[6]);
			v3a.add(v3b);
			v3a.scale(0.5f);
			deCasteljauSplit(ap3[0], ap3[11], ap3[10], ap3[9], ap3new[0], ap3new[11], ap3new[10], ap3new[9]);
			deCasteljauSplit(ap3[3], ap3[4], ap3[5], ap3[6], ap3new[3], ap3new[4], ap3new[5], ap3new[6]);
			ap3new[8].set(ap3[8]);
			ap3new[7].set(ap3[7]);
			ap3[8].add(ap3[9], v3c);
			ap3[7].add(ap3[6], v3a);
			ap3new[1].set(ap3[8]);
			ap3new[2].set(ap3[7]);
			
			///* compute new normals */
			//Vector3f[] av3new = newNormals(4);
			//av3new[3].set(av3[3]);
			////av3new[0] = av3[3] = (vLevel > 99) ? interpolateNormal(av3[0], av3[3]) : interpolateNormal(av3[0], av3[3], ap3[0], ap3[11], ap3[10], ap3[9]);
			//av3new[2].set(av3[2]);
			////av3new[1] = av3[2] = (vLevel > 99) ? interpolateNormal(av3[1], av3[2]) : interpolateNormal(av3[1], av3[2], ap3[3], ap3[4], ap3[5], ap3[6]);
			//
			//v3a.sub(ap3[10], ap3[9]);
			//v3b.sub(ap3[8], ap3[9]);
			//av3[3].cross(v3b, v3a);
			//av3[3].normalize();
			//av3new[0].set(av3[3]);
			//v3a.sub(ap3[5], ap3[6]);
			//v3b.sub(ap3[7], ap3[6]);
			//av3[2].cross(v3a, v3b);
			//av3[2].normalize();
			//av3new[1].set(av3[2]);
			
			/* compute new colors */
//			int[] acnew = new int[4];
//			acnew[3] = ac[3];
//			acnew[0] = ac[3] = (!abFlat[3]) ? lighting.shade(ap3[9], av3[3], mp) : interpolateColor(ac[0], ac[3]);
//			acnew[2] = ac[2];
//			acnew[1] = ac[2] = (!abFlat[1]) ? lighting.shade(ap3[6], av3[2], mp) : interpolateColor(ac[1], ac[2]);
			
			/* set up new flatenough flags */
			boolean[] abnew = new boolean[4];
			abnew[1] = abFlat[1];
			abnew[3] = abFlat[3];
			abnew[2] = abFlat[2];
			abnew[0] = abFlat[2] = false;
			
			///* compute new normals */
			//Vector3f[] av3new = newNormals();
			//av3new[3].set(av3[3]);
			//v3b.sub(ap3[10],ap3[9]);
			//av3[3].cross(v3b, v3c);
			//av3[3].normalize();
			//av3new[0].set(av3[3]);
			//av3new[2].set(av3[2]);
			//v3b.sub(ap3[6],ap3[5]);
			//av3[2].cross(v3b, v3a);
			//av3[2].normalize();
			//av3new[1].set(av3[2]);
			
			/* recurse */
			int l = (aiLevel[0] < aiLevel[2]) ? aiLevel[0] : aiLevel[2];
			aiLevel[1]++;
			aiLevel[3]++;
			int[] newLevels = new int[aiLevel.length];
			for (int i = 0; i < aiLevel.length; newLevels[i] = aiLevel[i++]);
			aiLevel[2] = newLevels[0] = l;
			drawLitHashPatch4(ap3, av3, abFlat, aiLevel);
			drawLitHashPatch4(ap3new, av3new, abnew, newLevels);
			return;
		}
		//}
		/* draw the patch */
		
		//System.out.println("drawPatch");
		//for (int i = 0; i < 12; System.out.println(ap3[i++]));
		
		//if (bBackfaceNormalFlip) flipBackfaceNormals(av3);
		//av3[0].normalize();
		//av3[1].normalize();
		//av3[2].normalize();
		//av3[3].normalize();
		
		//av3New[0].set(av3[0]);
		//av3New[1].set(av3[1]);
		//av3New[2].set(av3[2]);
		//av3New[3].set(av3[3]);
		//if (bBackfaceNormalFlip) flipBackfaceNormals(av3New);
		//int c0 = lighting.shade(ap3[0], av3New[0], mp);
		//int c1 = lighting.shade(ap3[3], av3New[1], mp);
		//int c2 = lighting.shade(ap3[6], av3New[2], mp);
		//int c3 = lighting.shade(ap3[9], av3New[3], mp);
		
		//drawLine3D(ap3[0], ap3[3]);
		//drawLine3D(ap3[3], ap3[6]);
		//drawLine3D(ap3[6], ap3[9]);
		//drawLine3D(ap3[9], ap3[0]);
		
		//for (int i = 0; i < 12; i++) {
		//	drawLine3D(ap3[i], ap3[(i + 1) % 12]);
		//	drawPoint3D(ap3[i],2);
		//}
		
		//Point3f p = new Point3f();
		//p.set(ap3[0]);
		//v3a.set(av3[0]);
		//v3a.scale(30f);
		//p.add(v3a);
		//drawLine3D(ap3[0],p);
		//
		//p.set(ap3[3]);
		//v3a.set(av3[1]);
		//v3a.scale(30f);
		//p.add(v3a);
		//drawLine3D(ap3[3],p);
		//
		//p.set(ap3[6]);
		//v3a.set(av3[2]);
		//v3a.scale(30f);
		//p.add(v3a);
		//drawLine3D(ap3[6],p);
		//
		//p.set(ap3[9]);
		//v3a.set(av3[3]);
		//v3a.scale(30f);
		//p.add(v3a);
		//drawLine3D(ap3[9],p);
		
		//int ca = 0xFF777777;
		//int cb = 0xFF999999;
		//
		//draw3DTriangleGourad(ap3[0], ap3[3], ap3[9], ca, ca, ca);
		//draw3DTriangleGourad(ap3[6], ap3[9], ap3[3], cb, cb, cb);
		
//		if (mp.isOpaque()) {
//			drawTriangleGourad(ap3[9], ap3[3], ap3[0], ac[3], ac[1], ac[0]);
//			drawTriangleGourad(ap3[3], ap3[9], ap3[6], ac[1], ac[3], ac[2]);
//		} else {
//			int transparency = (int) (Math.min(1f,mp.transmit + mp.filter) * 255f);
//			draw3DTriangleGouradTransparent(ap3[9], ap3[3], ap3[0], ac[3], ac[1], ac[0], transparency);
//			draw3DTriangleGouradTransparent(ap3[3], ap3[9], ap3[6], ac[1], ac[3], ac[2], transparency);
//		}
		drawable.drawTriangle(ap3[9], av3[3], ap3[3], av3[1], ap3[0], av3[0]);
		drawable.drawTriangle(ap3[3], av3[1], ap3[9], av3[3], ap3[6], av3[2]);
	}
	
	private void drawFlatHashPatch4(Point3f[] ap3, boolean[] abFlat, int[] aiLevel) {
		
		//materialProperties = mp;
		//Point3d[] adp3 = new Point3d[ap3.length];
		//for (int i = 0; i < adp3.length; adp3[i] = new Point3d(ap3[i++]));
		//hashPatchSubdivision.subdivHashPatch4(adp3, adp3, av3, 2, new Point3d[][] { null, null, null, null }, new Vector3f[][] { null, null, null, null });
		//
		//if (true) return;
		
		boolean visible = false;
		int width = drawable.getComponent().getWidth() >> 1;
		int height = drawable.getComponent().getHeight() >> 1;
		loop:
		for (int i = 0; i < 12; i++) {
			if (ap3[i].x > -width && ap3[i].x < width && ap3[i].y > -height && ap3[i].y < height) {
				visible = true;
				break loop;
			}
		}
		if (!visible) return;
		//System.out.println(abFlat[0] + "\t" + abFlat[1] + "\t" + abFlat[2] + "\t" + abFlat[3]);
		
		/* check if we need to u-split */
		float u0 = subdiv(ap3[0], ap3[1], ap3[2], ap3[3], aiLevel[0] > 0);
		float u2 = subdiv(ap3[9], ap3[8], ap3[7], ap3[6], aiLevel[2] > 0);
		float v3 = subdiv(ap3[0], ap3[11], ap3[10], ap3[9], aiLevel[3] > 0);
		float v1 = subdiv(ap3[3], ap3[4], ap3[5], ap3[6], aiLevel[1] > 0);
		
		float ul = (aiLevel[0] >= iMaxSubdiv || aiLevel[2] >= iMaxSubdiv) ? -1 : (float) Math.max(u0, u2);
		float uv = (aiLevel[1] >= iMaxSubdiv || aiLevel[3] >= iMaxSubdiv) ? -1 : (float) Math.max(v1, v3);
		
		if (true && ul >= fFlatness && ul > uv * 1.1f) {
		//if (false) {
		//if (subdiv(ap3[0], ap3[1], ap3[2], ap3[3], uSplit) || subdiv(ap3[9], ap3[8], ap3[7], ap3[6], uSplit)) {
			
			/* flatten cubics if flat enough */
			if (!abFlat[0] && (u0 <= fFlatness)) abFlat[0] = makeFlat(ap3[0], ap3[1], ap3[2], ap3[3]);
			if (!abFlat[1] && (v1 <= fFlatness)) abFlat[1] = makeFlat(ap3[3], ap3[4], ap3[5], ap3[6]);
			if (!abFlat[2] && (u2 <= fFlatness)) abFlat[2] = makeFlat(ap3[9], ap3[8], ap3[7], ap3[6]);
			if (!abFlat[3] && (v3 <= fFlatness)) abFlat[3] = makeFlat(ap3[0], ap3[11], ap3[10], ap3[9]);
			
			/* compute new patches */
			Point3f[] ap3new = newPatch(12);
			v3a.sub(ap3[11], ap3[0]);
			v3b.sub(ap3[4], ap3[3]);
			v3c.add(v3a, v3b);
			v3c.scale(0.5f);
			v3a.sub(ap3[10], ap3[9]);
			v3b.sub(ap3[5], ap3[6]);
			v3a.add(v3b);
			v3a.scale(0.5f);
			deCasteljauSplit(ap3[0], ap3[1], ap3[2], ap3[3], ap3new[0], ap3new[1], ap3new[2], ap3new[3]);
			deCasteljauSplit(ap3[9], ap3[8], ap3[7], ap3[6], ap3new[9], ap3new[8], ap3new[7], ap3new[6]);
			ap3new[4].set(ap3[4]);
			ap3new[5].set(ap3[5]);
			ap3[4].add(ap3[3], v3c);
			ap3[5].add(ap3[6], v3a);
			ap3new[11].set(ap3[4]);
			ap3new[10].set(ap3[5]);
			
			///* compute new normals */
			//Vector3f[] av3new = newNormals(4);
			//av3new[1].set(av3[1]);
			////av3new[0] = av3[1] = (uLevel > 99) ? interpolateNormal(av3[0], av3[1]) : interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]);
			//av3new[2].set(av3[2]);
			////av3new[3] = av3[2] = (uLevel > 99) ? interpolateNormal(av3[3], av3[2]) : interpolateNormal(av3[3], av3[2], ap3[9], ap3[8], ap3[7], ap3[6]);
			//
			//v3a.sub(ap3[4], ap3[3]);
			//v3b.sub(ap3[2], ap3[3]);
			//av3[1].cross(v3b, v3a);
			//av3[1].normalize();
			//av3new[0].set(av3[1]);
			//v3a.sub(ap3[5], ap3[6]);
			//v3b.sub(ap3[7], ap3[6]);
			//av3[2].cross(v3a, v3b);
			//av3[2].normalize();
			//av3new[3].set(av3[2]);
			
			/* set up new flatenough flags */
			boolean[] abnew = new boolean[4];
			abnew[0] = abFlat[0];
			abnew[1] = abFlat[1];
			abnew[2] = abFlat[2];
			abnew[3] = abFlat[1] = false;
			
			///* compute new normals */
			//Vector3f[] av3new = newNormals();
			//av3new[1].set(av3[1]);
			//v3b.sub(ap3[3],ap3[2]);
			//av3[1].cross(v3b, v3c);
			//av3[1].normalize();
			//av3new[0].set(av3[1]);
			//av3new[2].set(av3[2]);
			//v3b.sub(ap3[7],ap3[6]);
			//av3[2].cross(v3b, v3a);
			//av3[2].normalize();
			//av3new[3].set(av3[2]);
			
			/* recurse */
			int l = (aiLevel[1] < aiLevel[3]) ? aiLevel[1] : aiLevel[3];
			aiLevel[0]++;
			aiLevel[2]++;
			int[] newLevels = new int[aiLevel.length];
			for (int i = 0; i < aiLevel.length; newLevels[i] = aiLevel[i++]);
			aiLevel[1] = newLevels[3] = l;
			drawFlatHashPatch4(ap3, abFlat, aiLevel);
			drawFlatHashPatch4(ap3new, abnew, newLevels);
			return;
		}
		/* check if we need to v-split */
		else if (true && uv >= fFlatness) {
		//else if (false) {
		//else if (subdiv(ap3[0], ap3[11], ap3[10], ap3[9], vSplit) || subdiv(ap3[3], ap3[4], ap3[5], ap3[6], vSplit)) {
			/* flatten cubics if flat enough */
			if (!abFlat[0] && (u0 <= fFlatness)) abFlat[0] = makeFlat(ap3[0], ap3[1], ap3[2], ap3[3]);
			if (!abFlat[1] && (v1 <= fFlatness)) abFlat[1] = makeFlat(ap3[3], ap3[4], ap3[5], ap3[6]);
			if (!abFlat[2] && (u2 <= fFlatness)) abFlat[2] = makeFlat(ap3[9], ap3[8], ap3[7], ap3[6]);
			if (!abFlat[3] && (v3 <= fFlatness)) abFlat[3] = makeFlat(ap3[0], ap3[11], ap3[10], ap3[9]);
			
			
			/* compute new patches */
			Point3f[] ap3new = newPatch(12);
			v3a.sub(ap3[1], ap3[0]);
			v3b.sub(ap3[8], ap3[9]);
			v3c.add(v3a, v3b);
			v3c.scale(0.5f);
			v3a.sub(ap3[2], ap3[3]);
			v3b.sub(ap3[7], ap3[6]);
			v3a.add(v3b);
			v3a.scale(0.5f);
			deCasteljauSplit(ap3[0], ap3[11], ap3[10], ap3[9], ap3new[0], ap3new[11], ap3new[10], ap3new[9]);
			deCasteljauSplit(ap3[3], ap3[4], ap3[5], ap3[6], ap3new[3], ap3new[4], ap3new[5], ap3new[6]);
			ap3new[8].set(ap3[8]);
			ap3new[7].set(ap3[7]);
			ap3[8].add(ap3[9], v3c);
			ap3[7].add(ap3[6], v3a);
			ap3new[1].set(ap3[8]);
			ap3new[2].set(ap3[7]);
			
			///* compute new normals */
			//Vector3f[] av3new = newNormals(4);
			//av3new[3].set(av3[3]);
			////av3new[0] = av3[3] = (vLevel > 99) ? interpolateNormal(av3[0], av3[3]) : interpolateNormal(av3[0], av3[3], ap3[0], ap3[11], ap3[10], ap3[9]);
			//av3new[2].set(av3[2]);
			////av3new[1] = av3[2] = (vLevel > 99) ? interpolateNormal(av3[1], av3[2]) : interpolateNormal(av3[1], av3[2], ap3[3], ap3[4], ap3[5], ap3[6]);
			//
			//v3a.sub(ap3[10], ap3[9]);
			//v3b.sub(ap3[8], ap3[9]);
			//av3[3].cross(v3b, v3a);
			//av3[3].normalize();
			//av3new[0].set(av3[3]);
			//v3a.sub(ap3[5], ap3[6]);
			//v3b.sub(ap3[7], ap3[6]);
			//av3[2].cross(v3a, v3b);
			//av3[2].normalize();
			//av3new[1].set(av3[2]);
			
			/* compute new colors */
//			int[] acnew = new int[4];
//			acnew[3] = ac[3];
//			acnew[0] = ac[3] = (!abFlat[3]) ? lighting.shade(ap3[9], av3[3], mp) : interpolateColor(ac[0], ac[3]);
//			acnew[2] = ac[2];
//			acnew[1] = ac[2] = (!abFlat[1]) ? lighting.shade(ap3[6], av3[2], mp) : interpolateColor(ac[1], ac[2]);
			
			/* set up new flatenough flags */
			boolean[] abnew = new boolean[4];
			abnew[1] = abFlat[1];
			abnew[3] = abFlat[3];
			abnew[2] = abFlat[2];
			abnew[0] = abFlat[2] = false;
			
			///* compute new normals */
			//Vector3f[] av3new = newNormals();
			//av3new[3].set(av3[3]);
			//v3b.sub(ap3[10],ap3[9]);
			//av3[3].cross(v3b, v3c);
			//av3[3].normalize();
			//av3new[0].set(av3[3]);
			//av3new[2].set(av3[2]);
			//v3b.sub(ap3[6],ap3[5]);
			//av3[2].cross(v3b, v3a);
			//av3[2].normalize();
			//av3new[1].set(av3[2]);
			
			/* recurse */
			int l = (aiLevel[0] < aiLevel[2]) ? aiLevel[0] : aiLevel[2];
			aiLevel[1]++;
			aiLevel[3]++;
			int[] newLevels = new int[aiLevel.length];
			for (int i = 0; i < aiLevel.length; newLevels[i] = aiLevel[i++]);
			aiLevel[2] = newLevels[0] = l;
			drawFlatHashPatch4(ap3, abFlat, aiLevel);
			drawFlatHashPatch4(ap3new, abnew, newLevels);
			return;
		}
		//}
		/* draw the patch */
		
		//System.out.println("drawPatch");
		//for (int i = 0; i < 12; System.out.println(ap3[i++]));
		
		//if (bBackfaceNormalFlip) flipBackfaceNormals(av3);
		//av3[0].normalize();
		//av3[1].normalize();
		//av3[2].normalize();
		//av3[3].normalize();
		
		//av3New[0].set(av3[0]);
		//av3New[1].set(av3[1]);
		//av3New[2].set(av3[2]);
		//av3New[3].set(av3[3]);
		//if (bBackfaceNormalFlip) flipBackfaceNormals(av3New);
		//int c0 = lighting.shade(ap3[0], av3New[0], mp);
		//int c1 = lighting.shade(ap3[3], av3New[1], mp);
		//int c2 = lighting.shade(ap3[6], av3New[2], mp);
		//int c3 = lighting.shade(ap3[9], av3New[3], mp);
		
		//drawLine3D(ap3[0], ap3[3]);
		//drawLine3D(ap3[3], ap3[6]);
		//drawLine3D(ap3[6], ap3[9]);
		//drawLine3D(ap3[9], ap3[0]);
		
		//for (int i = 0; i < 12; i++) {
		//	drawLine3D(ap3[i], ap3[(i + 1) % 12]);
		//	drawPoint3D(ap3[i],2);
		//}
		
		//Point3f p = new Point3f();
		//p.set(ap3[0]);
		//v3a.set(av3[0]);
		//v3a.scale(30f);
		//p.add(v3a);
		//drawLine3D(ap3[0],p);
		//
		//p.set(ap3[3]);
		//v3a.set(av3[1]);
		//v3a.scale(30f);
		//p.add(v3a);
		//drawLine3D(ap3[3],p);
		//
		//p.set(ap3[6]);
		//v3a.set(av3[2]);
		//v3a.scale(30f);
		//p.add(v3a);
		//drawLine3D(ap3[6],p);
		//
		//p.set(ap3[9]);
		//v3a.set(av3[3]);
		//v3a.scale(30f);
		//p.add(v3a);
		//drawLine3D(ap3[9],p);
		
		//int ca = 0xFF777777;
		//int cb = 0xFF999999;
		//
		//draw3DTriangleGourad(ap3[0], ap3[3], ap3[9], ca, ca, ca);
		//draw3DTriangleGourad(ap3[6], ap3[9], ap3[3], cb, cb, cb);
		
//		if (mp.isOpaque()) {
			drawable.drawTriangle(ap3[9], ap3[3], ap3[0]);
			drawable.drawTriangle(ap3[3], ap3[9], ap3[6]);
//		} else {
//			int transparency = (int) (Math.min(1f,mp.transmit + mp.filter) * 255f);
//			drawTriangleGouradTransparent(ap3[9], ap3[3], ap3[0], ac[3], ac[1], ac[0], transparency);
//			drawTriangleGouradTransparent(ap3[3], ap3[9], ap3[6], ac[1], ac[3], ac[2], transparency);
//		}
	}

	private void drawShadedHashPatch4(Point3f[] ap3, Vector3f[] av3, Color3f[] ac, boolean[] abFlat, int[] aiLevel, MaterialProperties mp) {
		
		//materialProperties = mp;
		//Point3d[] adp3 = new Point3d[ap3.length];
		//for (int i = 0; i < adp3.length; adp3[i] = new Point3d(ap3[i++]));
		//hashPatchSubdivision.subdivHashPatch4(adp3, adp3, av3, 2, new Point3d[][] { null, null, null, null }, new Vector3f[][] { null, null, null, null });
		//
		//if (true) return;
		
		boolean visible = false;
		int width = drawable.getComponent().getWidth() >> 1;
		int height = drawable.getComponent().getHeight() >> 1;
		loop:
		for (int i = 0; i < 12; i++) {
			if (ap3[i].x > -width && ap3[i].x < width && ap3[i].y > -height && ap3[i].y < height) {
				visible = true;
				break loop;
			}
		}
		if (!visible) return;
		//System.out.println(abFlat[0] + "\t" + abFlat[1] + "\t" + abFlat[2] + "\t" + abFlat[3]);
		
		/* check if we need to u-split */
		float u0 = subdiv(ap3[0], ap3[1], ap3[2], ap3[3], aiLevel[0] > 0);
		float u2 = subdiv(ap3[9], ap3[8], ap3[7], ap3[6], aiLevel[2] > 0);
		float v3 = subdiv(ap3[0], ap3[11], ap3[10], ap3[9], aiLevel[3] > 0);
		float v1 = subdiv(ap3[3], ap3[4], ap3[5], ap3[6], aiLevel[1] > 0);
		
		float ul = (aiLevel[0] >= iMaxSubdiv || aiLevel[2] >= iMaxSubdiv) ? -1 : (float) Math.max(u0, u2);
		float uv = (aiLevel[1] >= iMaxSubdiv || aiLevel[3] >= iMaxSubdiv) ? -1 : (float) Math.max(v1, v3);
		
		if (true && ul >= fFlatness && ul > uv * 1.1f) {
		//if (false) {
		//if (subdiv(ap3[0], ap3[1], ap3[2], ap3[3], uSplit) || subdiv(ap3[9], ap3[8], ap3[7], ap3[6], uSplit)) {
			
			/* flatten cubics if flat enough */
			if (!abFlat[0] && (u0 <= fFlatness)) abFlat[0] = makeFlat(ap3[0], ap3[1], ap3[2], ap3[3]);
			if (!abFlat[1] && (v1 <= fFlatness)) abFlat[1] = makeFlat(ap3[3], ap3[4], ap3[5], ap3[6]);
			if (!abFlat[2] && (u2 <= fFlatness)) abFlat[2] = makeFlat(ap3[9], ap3[8], ap3[7], ap3[6]);
			if (!abFlat[3] && (v3 <= fFlatness)) abFlat[3] = makeFlat(ap3[0], ap3[11], ap3[10], ap3[9]);
			
			
			/* compute new normals */
			Vector3f[] av3new = newNormals(4);
			av3new[1].set(av3[1]);
			av3new[0] = av3[1] = (aiLevel[0] > 0) ? interpolateNormal(av3[0], av3[1]) : interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]);
			av3new[2].set(av3[2]);
			av3new[3] = av3[2] = (aiLevel[2] > 0) ? interpolateNormal(av3[3], av3[2]) : interpolateNormal(av3[3], av3[2], ap3[9], ap3[8], ap3[7], ap3[6]);
			
			/* compute new patches */
			Point3f[] ap3new = newPatch(12);
			v3a.sub(ap3[11], ap3[0]);
			v3b.sub(ap3[4], ap3[3]);
			v3c.add(v3a, v3b);
			v3c.scale(0.5f);
			v3a.sub(ap3[10], ap3[9]);
			v3b.sub(ap3[5], ap3[6]);
			v3a.add(v3b);
			v3a.scale(0.5f);
			deCasteljauSplit(ap3[0], ap3[1], ap3[2], ap3[3], ap3new[0], ap3new[1], ap3new[2], ap3new[3]);
			deCasteljauSplit(ap3[9], ap3[8], ap3[7], ap3[6], ap3new[9], ap3new[8], ap3new[7], ap3new[6]);
			ap3new[4].set(ap3[4]);
			ap3new[5].set(ap3[5]);
			ap3[4].add(ap3[3], v3c);
			ap3[5].add(ap3[6], v3a);
			ap3new[11].set(ap3[4]);
			ap3new[10].set(ap3[5]);
			
			///* compute new normals */
			//Vector3f[] av3new = newNormals(4);
			//av3new[1].set(av3[1]);
			////av3new[0] = av3[1] = (uLevel > 99) ? interpolateNormal(av3[0], av3[1]) : interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]);
			//av3new[2].set(av3[2]);
			////av3new[3] = av3[2] = (uLevel > 99) ? interpolateNormal(av3[3], av3[2]) : interpolateNormal(av3[3], av3[2], ap3[9], ap3[8], ap3[7], ap3[6]);
			//
			//v3a.sub(ap3[4], ap3[3]);
			//v3b.sub(ap3[2], ap3[3]);
			//av3[1].cross(v3b, v3a);
			//av3[1].normalize();
			//av3new[0].set(av3[1]);
			//v3a.sub(ap3[5], ap3[6]);
			//v3b.sub(ap3[7], ap3[6]);
			//av3[2].cross(v3a, v3b);
			//av3[2].normalize();
			//av3new[3].set(av3[2]);
			
			/* compute new colors */
			Color3f[] acnew = newColors(4);
			acnew[1].set(ac[1]);
			viewDef.getLighting().shade(ap3[3], av3[1], mp, ac[1]);
			acnew[0].set(ac[1]);
			acnew[2].set(ac[2]);
			viewDef.getLighting().shade(ap3[6], av3[2], mp, ac[2]);
			acnew[3].set(ac[2]);
//			acnew[0] = ac[1] = (!abFlat[0]) ? lighting.shade(ap3[3], av3[1], mp) : interpolateColor(ac[0], ac[1]);
//			acnew[2] = ac[2];
//			acnew[3] = ac[2] = (!abFlat[2]) ? lighting.shade(ap3[6], av3[2], mp) : interpolateColor(ac[2], ac[3]);
			
			/* set up new flatenough flags */
			boolean[] abnew = new boolean[4];
			abnew[0] = abFlat[0];
			abnew[1] = abFlat[1];
			abnew[2] = abFlat[2];
			abnew[3] = abFlat[1] = false;
			
			///* compute new normals */
			//Vector3f[] av3new = newNormals();
			//av3new[1].set(av3[1]);
			//v3b.sub(ap3[3],ap3[2]);
			//av3[1].cross(v3b, v3c);
			//av3[1].normalize();
			//av3new[0].set(av3[1]);
			//av3new[2].set(av3[2]);
			//v3b.sub(ap3[7],ap3[6]);
			//av3[2].cross(v3b, v3a);
			//av3[2].normalize();
			//av3new[3].set(av3[2]);
			
			/* recurse */
			int l = (aiLevel[1] < aiLevel[3]) ? aiLevel[1] : aiLevel[3];
			aiLevel[0]++;
			aiLevel[2]++;
			int[] newLevels = new int[aiLevel.length];
			for (int i = 0; i < aiLevel.length; newLevels[i] = aiLevel[i++]);
			aiLevel[1] = newLevels[3] = l;
			drawShadedHashPatch4(ap3, av3, ac, abFlat, aiLevel, mp);
			drawShadedHashPatch4(ap3new, av3new, acnew, abnew, newLevels, mp);
			return;
		}
		/* check if we need to v-split */
		else if (true && uv >= fFlatness) {
		//else if (false) {
		//else if (subdiv(ap3[0], ap3[11], ap3[10], ap3[9], vSplit) || subdiv(ap3[3], ap3[4], ap3[5], ap3[6], vSplit)) {
			/* flatten cubics if flat enough */
			if (!abFlat[0] && (u0 <= fFlatness)) abFlat[0] = makeFlat(ap3[0], ap3[1], ap3[2], ap3[3]);
			if (!abFlat[1] && (v1 <= fFlatness)) abFlat[1] = makeFlat(ap3[3], ap3[4], ap3[5], ap3[6]);
			if (!abFlat[2] && (u2 <= fFlatness)) abFlat[2] = makeFlat(ap3[9], ap3[8], ap3[7], ap3[6]);
			if (!abFlat[3] && (v3 <= fFlatness)) abFlat[3] = makeFlat(ap3[0], ap3[11], ap3[10], ap3[9]);
			
			
			/* compute new normals */
			Vector3f[] av3new = newNormals(4);
			av3new[3].set(av3[3]);
			av3new[0] = av3[3] = (aiLevel[3] > 0) ? interpolateNormal(av3[0], av3[3]) : interpolateNormal(av3[0], av3[3], ap3[0], ap3[11], ap3[10], ap3[9]);
			av3new[2].set(av3[2]);
			av3new[1] = av3[2] = (aiLevel[1] > 0) ? interpolateNormal(av3[1], av3[2]) : interpolateNormal(av3[1], av3[2], ap3[3], ap3[4], ap3[5], ap3[6]);
			
			/* compute new patches */
			Point3f[] ap3new = newPatch(12);
			v3a.sub(ap3[1], ap3[0]);
			v3b.sub(ap3[8], ap3[9]);
			v3c.add(v3a, v3b);
			v3c.scale(0.5f);
			v3a.sub(ap3[2], ap3[3]);
			v3b.sub(ap3[7], ap3[6]);
			v3a.add(v3b);
			v3a.scale(0.5f);
			deCasteljauSplit(ap3[0], ap3[11], ap3[10], ap3[9], ap3new[0], ap3new[11], ap3new[10], ap3new[9]);
			deCasteljauSplit(ap3[3], ap3[4], ap3[5], ap3[6], ap3new[3], ap3new[4], ap3new[5], ap3new[6]);
			ap3new[8].set(ap3[8]);
			ap3new[7].set(ap3[7]);
			ap3[8].add(ap3[9], v3c);
			ap3[7].add(ap3[6], v3a);
			ap3new[1].set(ap3[8]);
			ap3new[2].set(ap3[7]);
			
			///* compute new normals */
			//Vector3f[] av3new = newNormals(4);
			//av3new[3].set(av3[3]);
			////av3new[0] = av3[3] = (vLevel > 99) ? interpolateNormal(av3[0], av3[3]) : interpolateNormal(av3[0], av3[3], ap3[0], ap3[11], ap3[10], ap3[9]);
			//av3new[2].set(av3[2]);
			////av3new[1] = av3[2] = (vLevel > 99) ? interpolateNormal(av3[1], av3[2]) : interpolateNormal(av3[1], av3[2], ap3[3], ap3[4], ap3[5], ap3[6]);
			//
			//v3a.sub(ap3[10], ap3[9]);
			//v3b.sub(ap3[8], ap3[9]);
			//av3[3].cross(v3b, v3a);
			//av3[3].normalize();
			//av3new[0].set(av3[3]);
			//v3a.sub(ap3[5], ap3[6]);
			//v3b.sub(ap3[7], ap3[6]);
			//av3[2].cross(v3a, v3b);
			//av3[2].normalize();
			//av3new[1].set(av3[2]);
			
			/* compute new colors */
//			int[] acnew = new int[4];
//			acnew[3] = ac[3];
//			acnew[0] = ac[3] = (!abFlat[3]) ? lighting.shade(ap3[9], av3[3], mp) : interpolateColor(ac[0], ac[3]);
//			acnew[2] = ac[2];
//			acnew[1] = ac[2] = (!abFlat[1]) ? lighting.shade(ap3[6], av3[2], mp) : interpolateColor(ac[1], ac[2]);
			Color3f[] acnew = newColors(4);
			acnew[3].set(ac[3]);
			viewDef.getLighting().shade(ap3[9], av3[3], mp, ac[3]);
			acnew[0].set(ac[3]);
			acnew[2].set(ac[2]);
			viewDef.getLighting().shade(ap3[6], av3[2], mp, ac[2]);
			acnew[1].set(ac[2]);
			
			/* set up new flatenough flags */
			boolean[] abnew = new boolean[4];
			abnew[1] = abFlat[1];
			abnew[3] = abFlat[3];
			abnew[2] = abFlat[2];
			abnew[0] = abFlat[2] = false;
			
			///* compute new normals */
			//Vector3f[] av3new = newNormals();
			//av3new[3].set(av3[3]);
			//v3b.sub(ap3[10],ap3[9]);
			//av3[3].cross(v3b, v3c);
			//av3[3].normalize();
			//av3new[0].set(av3[3]);
			//av3new[2].set(av3[2]);
			//v3b.sub(ap3[6],ap3[5]);
			//av3[2].cross(v3b, v3a);
			//av3[2].normalize();
			//av3new[1].set(av3[2]);
			
			/* recurse */
			int l = (aiLevel[0] < aiLevel[2]) ? aiLevel[0] : aiLevel[2];
			aiLevel[1]++;
			aiLevel[3]++;
			int[] newLevels = new int[aiLevel.length];
			for (int i = 0; i < aiLevel.length; newLevels[i] = aiLevel[i++]);
			aiLevel[2] = newLevels[0] = l;
			drawShadedHashPatch4(ap3, av3, ac, abFlat, aiLevel, mp);
			drawShadedHashPatch4(ap3new, av3new, acnew, abnew, newLevels, mp);
			return;
		}
		//}
		/* draw the patch */
		
		//System.out.println("drawPatch");
		//for (int i = 0; i < 12; System.out.println(ap3[i++]));
		
		//if (bBackfaceNormalFlip) flipBackfaceNormals(av3);
		//av3[0].normalize();
		//av3[1].normalize();
		//av3[2].normalize();
		//av3[3].normalize();
		
		//av3New[0].set(av3[0]);
		//av3New[1].set(av3[1]);
		//av3New[2].set(av3[2]);
		//av3New[3].set(av3[3]);
		//if (bBackfaceNormalFlip) flipBackfaceNormals(av3New);
		//int c0 = lighting.shade(ap3[0], av3New[0], mp);
		//int c1 = lighting.shade(ap3[3], av3New[1], mp);
		//int c2 = lighting.shade(ap3[6], av3New[2], mp);
		//int c3 = lighting.shade(ap3[9], av3New[3], mp);
		
		//drawLine3D(ap3[0], ap3[3]);
		//drawLine3D(ap3[3], ap3[6]);
		//drawLine3D(ap3[6], ap3[9]);
		//drawLine3D(ap3[9], ap3[0]);
		
		//for (int i = 0; i < 12; i++) {
		//	drawLine3D(ap3[i], ap3[(i + 1) % 12]);
		//	drawPoint3D(ap3[i],2);
		//}
		
		//Point3f p = new Point3f();
		//p.set(ap3[0]);
		//v3a.set(av3[0]);
		//v3a.scale(30f);
		//p.add(v3a);
		//drawLine3D(ap3[0],p);
		//
		//p.set(ap3[3]);
		//v3a.set(av3[1]);
		//v3a.scale(30f);
		//p.add(v3a);
		//drawLine3D(ap3[3],p);
		//
		//p.set(ap3[6]);
		//v3a.set(av3[2]);
		//v3a.scale(30f);
		//p.add(v3a);
		//drawLine3D(ap3[6],p);
		//
		//p.set(ap3[9]);
		//v3a.set(av3[3]);
		//v3a.scale(30f);
		//p.add(v3a);
		//drawLine3D(ap3[9],p);
		
		//int ca = 0xFF777777;
		//int cb = 0xFF999999;
		//
		//draw3DTriangleGourad(ap3[0], ap3[3], ap3[9], ca, ca, ca);
		//draw3DTriangleGourad(ap3[6], ap3[9], ap3[3], cb, cb, cb);
		
//		if (mp.isOpaque()) {
			drawable.drawTriangle(ap3[9], ac[3], ap3[3], ac[1], ap3[0], ac[0]);
			drawable.drawTriangle(ap3[3], ac[1], ap3[9], ac[3], ap3[6], ac[2]);
//		} else {
//			int transparency = (int) (Math.min(1f,mp.transmit + mp.filter) * 255f);
//			drawTriangleGouradTransparent(ap3[9], ap3[3], ap3[0], ac[3], ac[1], ac[0], transparency);
//			drawTriangleGouradTransparent(ap3[3], ap3[9], ap3[6], ac[1], ac[3], ac[2], transparency);
//		}
	}

private void drawShadedHashPatch4Alpha(Point3f[] ap3, Vector3f[] av3, Color4f[] ac, boolean[] abFlat, int[] aiLevel, MaterialProperties mp) {
		
		//materialProperties = mp;
		//Point3d[] adp3 = new Point3d[ap3.length];
		//for (int i = 0; i < adp3.length; adp3[i] = new Point3d(ap3[i++]));
		//hashPatchSubdivision.subdivHashPatch4(adp3, adp3, av3, 2, new Point3d[][] { null, null, null, null }, new Vector3f[][] { null, null, null, null });
		//
		//if (true) return;
		
		boolean visible = false;
		int width = drawable.getComponent().getWidth() >> 1;
		int height = drawable.getComponent().getHeight() >> 1;
		loop:
		for (int i = 0; i < 12; i++) {
			if (ap3[i].x > -width && ap3[i].x < width && ap3[i].y > -height && ap3[i].y < height) {
				visible = true;
				break loop;
			}
		}
		if (!visible) return;
		//System.out.println(abFlat[0] + "\t" + abFlat[1] + "\t" + abFlat[2] + "\t" + abFlat[3]);
		
		/* check if we need to u-split */
		float u0 = subdiv(ap3[0], ap3[1], ap3[2], ap3[3], aiLevel[0] > 0);
		float u2 = subdiv(ap3[9], ap3[8], ap3[7], ap3[6], aiLevel[2] > 0);
		float v3 = subdiv(ap3[0], ap3[11], ap3[10], ap3[9], aiLevel[3] > 0);
		float v1 = subdiv(ap3[3], ap3[4], ap3[5], ap3[6], aiLevel[1] > 0);
		
		float ul = (aiLevel[0] >= iMaxSubdiv || aiLevel[2] >= iMaxSubdiv) ? -1 : (float) Math.max(u0, u2);
		float uv = (aiLevel[1] >= iMaxSubdiv || aiLevel[3] >= iMaxSubdiv) ? -1 : (float) Math.max(v1, v3);
		
		if (true && ul >= fFlatness && ul > uv * 1.1f) {
		//if (false) {
		//if (subdiv(ap3[0], ap3[1], ap3[2], ap3[3], uSplit) || subdiv(ap3[9], ap3[8], ap3[7], ap3[6], uSplit)) {
			
			/* flatten cubics if flat enough */
			if (!abFlat[0] && (u0 <= fFlatness)) abFlat[0] = makeFlat(ap3[0], ap3[1], ap3[2], ap3[3]);
			if (!abFlat[1] && (v1 <= fFlatness)) abFlat[1] = makeFlat(ap3[3], ap3[4], ap3[5], ap3[6]);
			if (!abFlat[2] && (u2 <= fFlatness)) abFlat[2] = makeFlat(ap3[9], ap3[8], ap3[7], ap3[6]);
			if (!abFlat[3] && (v3 <= fFlatness)) abFlat[3] = makeFlat(ap3[0], ap3[11], ap3[10], ap3[9]);
			
			
			/* compute new normals */
			Vector3f[] av3new = newNormals(4);
			av3new[1].set(av3[1]);
			av3new[0] = av3[1] = (aiLevel[0] > 0) ? interpolateNormal(av3[0], av3[1]) : interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]);
			av3new[2].set(av3[2]);
			av3new[3] = av3[2] = (aiLevel[2] > 0) ? interpolateNormal(av3[3], av3[2]) : interpolateNormal(av3[3], av3[2], ap3[9], ap3[8], ap3[7], ap3[6]);
			
			/* compute new patches */
			Point3f[] ap3new = newPatch(12);
			v3a.sub(ap3[11], ap3[0]);
			v3b.sub(ap3[4], ap3[3]);
			v3c.add(v3a, v3b);
			v3c.scale(0.5f);
			v3a.sub(ap3[10], ap3[9]);
			v3b.sub(ap3[5], ap3[6]);
			v3a.add(v3b);
			v3a.scale(0.5f);
			deCasteljauSplit(ap3[0], ap3[1], ap3[2], ap3[3], ap3new[0], ap3new[1], ap3new[2], ap3new[3]);
			deCasteljauSplit(ap3[9], ap3[8], ap3[7], ap3[6], ap3new[9], ap3new[8], ap3new[7], ap3new[6]);
			ap3new[4].set(ap3[4]);
			ap3new[5].set(ap3[5]);
			ap3[4].add(ap3[3], v3c);
			ap3[5].add(ap3[6], v3a);
			ap3new[11].set(ap3[4]);
			ap3new[10].set(ap3[5]);
			
			///* compute new normals */
			//Vector3f[] av3new = newNormals(4);
			//av3new[1].set(av3[1]);
			////av3new[0] = av3[1] = (uLevel > 99) ? interpolateNormal(av3[0], av3[1]) : interpolateNormal(av3[0], av3[1], ap3[0], ap3[1], ap3[2], ap3[3]);
			//av3new[2].set(av3[2]);
			////av3new[3] = av3[2] = (uLevel > 99) ? interpolateNormal(av3[3], av3[2]) : interpolateNormal(av3[3], av3[2], ap3[9], ap3[8], ap3[7], ap3[6]);
			//
			//v3a.sub(ap3[4], ap3[3]);
			//v3b.sub(ap3[2], ap3[3]);
			//av3[1].cross(v3b, v3a);
			//av3[1].normalize();
			//av3new[0].set(av3[1]);
			//v3a.sub(ap3[5], ap3[6]);
			//v3b.sub(ap3[7], ap3[6]);
			//av3[2].cross(v3a, v3b);
			//av3[2].normalize();
			//av3new[3].set(av3[2]);
			
			/* compute new colors */
			Color4f[] acnew = newColors4(4);
			acnew[1].set(ac[1]);
			viewDef.getLighting().shade(ap3[3], av3[1], mp, ac[1]);
			acnew[0].set(ac[1]);
			acnew[2].set(ac[2]);
			viewDef.getLighting().shade(ap3[6], av3[2], mp, ac[2]);
			acnew[3].set(ac[2]);
//			acnew[0] = ac[1] = (!abFlat[0]) ? lighting.shade(ap3[3], av3[1], mp) : interpolateColor(ac[0], ac[1]);
//			acnew[2] = ac[2];
//			acnew[3] = ac[2] = (!abFlat[2]) ? lighting.shade(ap3[6], av3[2], mp) : interpolateColor(ac[2], ac[3]);
			
			/* set up new flatenough flags */
			boolean[] abnew = new boolean[4];
			abnew[0] = abFlat[0];
			abnew[1] = abFlat[1];
			abnew[2] = abFlat[2];
			abnew[3] = abFlat[1] = false;
			
			///* compute new normals */
			//Vector3f[] av3new = newNormals();
			//av3new[1].set(av3[1]);
			//v3b.sub(ap3[3],ap3[2]);
			//av3[1].cross(v3b, v3c);
			//av3[1].normalize();
			//av3new[0].set(av3[1]);
			//av3new[2].set(av3[2]);
			//v3b.sub(ap3[7],ap3[6]);
			//av3[2].cross(v3b, v3a);
			//av3[2].normalize();
			//av3new[3].set(av3[2]);
			
			/* recurse */
			int l = (aiLevel[1] < aiLevel[3]) ? aiLevel[1] : aiLevel[3];
			aiLevel[0]++;
			aiLevel[2]++;
			int[] newLevels = new int[aiLevel.length];
			for (int i = 0; i < aiLevel.length; newLevels[i] = aiLevel[i++]);
			aiLevel[1] = newLevels[3] = l;
			drawShadedHashPatch4Alpha(ap3, av3, ac, abFlat, aiLevel, mp);
			drawShadedHashPatch4Alpha(ap3new, av3new, acnew, abnew, newLevels, mp);
			return;
		}
		/* check if we need to v-split */
		else if (true && uv >= fFlatness) {
		//else if (false) {
		//else if (subdiv(ap3[0], ap3[11], ap3[10], ap3[9], vSplit) || subdiv(ap3[3], ap3[4], ap3[5], ap3[6], vSplit)) {
			/* flatten cubics if flat enough */
			if (!abFlat[0] && (u0 <= fFlatness)) abFlat[0] = makeFlat(ap3[0], ap3[1], ap3[2], ap3[3]);
			if (!abFlat[1] && (v1 <= fFlatness)) abFlat[1] = makeFlat(ap3[3], ap3[4], ap3[5], ap3[6]);
			if (!abFlat[2] && (u2 <= fFlatness)) abFlat[2] = makeFlat(ap3[9], ap3[8], ap3[7], ap3[6]);
			if (!abFlat[3] && (v3 <= fFlatness)) abFlat[3] = makeFlat(ap3[0], ap3[11], ap3[10], ap3[9]);
			
			
			/* compute new normals */
			Vector3f[] av3new = newNormals(4);
			av3new[3].set(av3[3]);
			av3new[0] = av3[3] = (aiLevel[3] > 0) ? interpolateNormal(av3[0], av3[3]) : interpolateNormal(av3[0], av3[3], ap3[0], ap3[11], ap3[10], ap3[9]);
			av3new[2].set(av3[2]);
			av3new[1] = av3[2] = (aiLevel[1] > 0) ? interpolateNormal(av3[1], av3[2]) : interpolateNormal(av3[1], av3[2], ap3[3], ap3[4], ap3[5], ap3[6]);
			
			/* compute new patches */
			Point3f[] ap3new = newPatch(12);
			v3a.sub(ap3[1], ap3[0]);
			v3b.sub(ap3[8], ap3[9]);
			v3c.add(v3a, v3b);
			v3c.scale(0.5f);
			v3a.sub(ap3[2], ap3[3]);
			v3b.sub(ap3[7], ap3[6]);
			v3a.add(v3b);
			v3a.scale(0.5f);
			deCasteljauSplit(ap3[0], ap3[11], ap3[10], ap3[9], ap3new[0], ap3new[11], ap3new[10], ap3new[9]);
			deCasteljauSplit(ap3[3], ap3[4], ap3[5], ap3[6], ap3new[3], ap3new[4], ap3new[5], ap3new[6]);
			ap3new[8].set(ap3[8]);
			ap3new[7].set(ap3[7]);
			ap3[8].add(ap3[9], v3c);
			ap3[7].add(ap3[6], v3a);
			ap3new[1].set(ap3[8]);
			ap3new[2].set(ap3[7]);
			
			///* compute new normals */
			//Vector3f[] av3new = newNormals(4);
			//av3new[3].set(av3[3]);
			////av3new[0] = av3[3] = (vLevel > 99) ? interpolateNormal(av3[0], av3[3]) : interpolateNormal(av3[0], av3[3], ap3[0], ap3[11], ap3[10], ap3[9]);
			//av3new[2].set(av3[2]);
			////av3new[1] = av3[2] = (vLevel > 99) ? interpolateNormal(av3[1], av3[2]) : interpolateNormal(av3[1], av3[2], ap3[3], ap3[4], ap3[5], ap3[6]);
			//
			//v3a.sub(ap3[10], ap3[9]);
			//v3b.sub(ap3[8], ap3[9]);
			//av3[3].cross(v3b, v3a);
			//av3[3].normalize();
			//av3new[0].set(av3[3]);
			//v3a.sub(ap3[5], ap3[6]);
			//v3b.sub(ap3[7], ap3[6]);
			//av3[2].cross(v3a, v3b);
			//av3[2].normalize();
			//av3new[1].set(av3[2]);
			
			/* compute new colors */
//			int[] acnew = new int[4];
//			acnew[3] = ac[3];
//			acnew[0] = ac[3] = (!abFlat[3]) ? lighting.shade(ap3[9], av3[3], mp) : interpolateColor(ac[0], ac[3]);
//			acnew[2] = ac[2];
//			acnew[1] = ac[2] = (!abFlat[1]) ? lighting.shade(ap3[6], av3[2], mp) : interpolateColor(ac[1], ac[2]);
			Color4f[] acnew = newColors4(4);
			acnew[3].set(ac[3]);
			viewDef.getLighting().shade(ap3[9], av3[3], mp, ac[3]);
			acnew[0].set(ac[3]);
			acnew[2].set(ac[2]);
			viewDef.getLighting().shade(ap3[6], av3[2], mp, ac[2]);
			acnew[1].set(ac[2]);
			
			/* set up new flatenough flags */
			boolean[] abnew = new boolean[4];
			abnew[1] = abFlat[1];
			abnew[3] = abFlat[3];
			abnew[2] = abFlat[2];
			abnew[0] = abFlat[2] = false;
			
			///* compute new normals */
			//Vector3f[] av3new = newNormals();
			//av3new[3].set(av3[3]);
			//v3b.sub(ap3[10],ap3[9]);
			//av3[3].cross(v3b, v3c);
			//av3[3].normalize();
			//av3new[0].set(av3[3]);
			//av3new[2].set(av3[2]);
			//v3b.sub(ap3[6],ap3[5]);
			//av3[2].cross(v3b, v3a);
			//av3[2].normalize();
			//av3new[1].set(av3[2]);
			
			/* recurse */
			int l = (aiLevel[0] < aiLevel[2]) ? aiLevel[0] : aiLevel[2];
			aiLevel[1]++;
			aiLevel[3]++;
			int[] newLevels = new int[aiLevel.length];
			for (int i = 0; i < aiLevel.length; newLevels[i] = aiLevel[i++]);
			aiLevel[2] = newLevels[0] = l;
			drawShadedHashPatch4Alpha(ap3, av3, ac, abFlat, aiLevel, mp);
			drawShadedHashPatch4Alpha(ap3new, av3new, acnew, abnew, newLevels, mp);
			return;
		}
		//}
		/* draw the patch */
		
		//System.out.println("drawPatch");
		//for (int i = 0; i < 12; System.out.println(ap3[i++]));
		
		//if (bBackfaceNormalFlip) flipBackfaceNormals(av3);
		//av3[0].normalize();
		//av3[1].normalize();
		//av3[2].normalize();
		//av3[3].normalize();
		
		//av3New[0].set(av3[0]);
		//av3New[1].set(av3[1]);
		//av3New[2].set(av3[2]);
		//av3New[3].set(av3[3]);
		//if (bBackfaceNormalFlip) flipBackfaceNormals(av3New);
		//int c0 = lighting.shade(ap3[0], av3New[0], mp);
		//int c1 = lighting.shade(ap3[3], av3New[1], mp);
		//int c2 = lighting.shade(ap3[6], av3New[2], mp);
		//int c3 = lighting.shade(ap3[9], av3New[3], mp);
		
		//drawLine3D(ap3[0], ap3[3]);
		//drawLine3D(ap3[3], ap3[6]);
		//drawLine3D(ap3[6], ap3[9]);
		//drawLine3D(ap3[9], ap3[0]);
		
		//for (int i = 0; i < 12; i++) {
		//	drawLine3D(ap3[i], ap3[(i + 1) % 12]);
		//	drawPoint3D(ap3[i],2);
		//}
		
		//Point3f p = new Point3f();
		//p.set(ap3[0]);
		//v3a.set(av3[0]);
		//v3a.scale(30f);
		//p.add(v3a);
		//drawLine3D(ap3[0],p);
		//
		//p.set(ap3[3]);
		//v3a.set(av3[1]);
		//v3a.scale(30f);
		//p.add(v3a);
		//drawLine3D(ap3[3],p);
		//
		//p.set(ap3[6]);
		//v3a.set(av3[2]);
		//v3a.scale(30f);
		//p.add(v3a);
		//drawLine3D(ap3[6],p);
		//
		//p.set(ap3[9]);
		//v3a.set(av3[3]);
		//v3a.scale(30f);
		//p.add(v3a);
		//drawLine3D(ap3[9],p);
		
		//int ca = 0xFF777777;
		//int cb = 0xFF999999;
		//
		//draw3DTriangleGourad(ap3[0], ap3[3], ap3[9], ca, ca, ca);
		//draw3DTriangleGourad(ap3[6], ap3[9], ap3[3], cb, cb, cb);
		
//		if (mp.isOpaque()) {
			drawable.drawTriangle(ap3[9], ac[3], ap3[3], ac[1], ap3[0], ac[0]);
			drawable.drawTriangle(ap3[3], ac[1], ap3[9], ac[3], ap3[6], ac[2]);
//		} else {
//			int transparency = (int) (Math.min(1f,mp.transmit + mp.filter) * 255f);
//			drawTriangleGouradTransparent(ap3[9], ap3[3], ap3[0], ac[3], ac[1], ac[0], transparency);
//			drawTriangleGouradTransparent(ap3[3], ap3[9], ap3[6], ac[1], ac[3], ac[2], transparency);
//		}
	}

	private float subdiv(Point3f p0, Point3f p1, Point3f p2, Point3f p3, boolean simple) {
		if (!simple) {
			v3a.set(4 * p0.x - 6 *  p1.x + 2 * p3.x, 4 * p0.y - 6 *  p1.y + 2 * p3.y, 4 * p0.z - 6 *  p1.z + 2 * p3.z);
			v3b.set(2 * p0.x - 6 *  p2.x + 4 * p3.x, 2 * p0.y - 6 *  p2.y + 4 * p3.y, 2 * p0.z - 6 *  p2.z + 4 * p3.z);
			return v3a.length() + v3b.length();
		} else {
			v3a.set(p0.x - p1.x - p2.x + p3.x, p0.y - p1.y - p2.y + p3.y, p0.z - p1.z - p2.z + p3.z);
			return v3a.length();
		}
	}
	
//	private int interpolateColor(int c0, int c1) {
//		int r0 = (c0 & 0x00fe0000) >> 1;
//		int g0 = (c0 & 0x0000fe00) >> 1;
//		int b0 = (c0 & 0x000000fe) >> 1;
//		int r1 = (c1 & 0x00fe0000) >> 1;
//		int g1 = (c1 & 0x0000fe00) >> 1;
//		int b1 = (c1 & 0x000000fe) >> 1;
//		return (r0 + r1) | (g0 + g1) | (b0 + b1);
//	}
	
	public static Vector3f interpolateNormal(Vector3f n0, Vector3f n1) {
		//return new Vector3f((n0.x + n1.x) * 0.5f, (n0.y + n1.y) * 0.5f, (n0.z + n1.z) * 0.5f);
		Vector3f n = new Vector3f(n0.x + n1.x, n0.y + n1.y, n0.z + n1.z);
		n.normalize();
		return n;
	}

	public static Vector3f interpolateNormal(Vector3f n0, Vector3f n1, Point3f p0, Point3f p1, Point3f p2, Point3f p3) {
		v3a.set(p0.x + p1.x - p2.x - p3.x, p0.y + p1.y - p2.y - p3.y, p0.z + p1.z - p2.z - p3.z);
		Vector3f n = new Vector3f(n0.x + n1.x, n0.y + n1.y, n0.z + n1.z);
		if (v3a.x != 0 || v3a.y != 0 || v3a.z != 0) {
			v3a.normalize();
			n.cross(n, v3a);
			n.cross(n, v3a);
			n.scale(-1f / n.length());
		}
		else n.normalize();
		return n;

	}
	
	private Point3f[] newPatch(int n) {
		Point3f[] p = new Point3f[n];
		for (int i = 0; i < n; p[i++] = new Point3f());
		return p;
	}
	
	private Vector3f[] newNormals(int n) {
		Vector3f[] v = new Vector3f[n];
		for (int i = 0; i < n; v[i++] = new Vector3f());
		return v;
	}
	
	private Color3f[] newColors(int n) {
		Color3f[] c = new Color3f[n];
		for (int i = 0; i < n; c[i++] = new Color3f());
		return c;
	}
	
	private Color4f[] newColors4(int n) {
		Color4f[] c = new Color4f[n];
		for (int i = 0; i < n; c[i++] = new Color4f());
		return c;
	}
	
	private boolean makeFlat(Point3f p0, Point3f p1, Point3f p2, Point3f p3) {
		v3a.sub(p1, p0);
		v3b.sub(p2, p3);
		v3c.sub(p3, p0);
		
		if (v3c.x != 0 || v3c.y != 0 || v3c.z != 0) v3c.normalize();
		
		p1.set(v3c);
		p1.scale(v3a.length());
		p1.add(p0);
		
		p2.set(v3c);
		p2.scale(-v3b.length());
		p2.add(p3);
		//System.out.println("makeflat");
		return true;
	}
	
	private void deCasteljauSplit(Point3f p0, Point3f p1, Point3f p2, Point3f p3, Point3f pn0, Point3f pn1, Point3f pn2, Point3f pn3) {
		pn0.set((p1.x + p2.x) * 0.5f, (p1.y + p2.y) * 0.5f, (p1.z + p2.z) * 0.5f);
		pn3.set(p3);
		pn2.set((p2.x + p3.x) * 0.5f, (p2.y + p3.y) * 0.5f, (p2.z + p3.z) * 0.5f);
		pn1.set((pn2.x + pn0.x) * 0.5f, (pn2.y + pn0.y) * 0.5f, (pn2.z + pn0.z) * 0.5f);
		p1.set((p0.x + p1.x) * 0.5f, (p0.y + p1.y) * 0.5f, (p0.z + p1.z) * 0.5f);
		p2.set((p1.x + pn0.x) * 0.5f, (p1.y + pn0.y) * 0.5f, (p1.z + pn0.z) * 0.5f);
		p3.set((p2.x + pn1.x) * 0.5f, (p2.y + pn1.y) * 0.5f, (p2.z + pn1.z) * 0.5f);
		pn0.set(p3);
	}
	
	private class BoneRenderer {
		static final float R = 0.05f;
		static final float A = 0.2f;
		static final float B = A - 1;
		final int[] LINES = new int[] {
				0, 1,
				0, 2,
				0, 3,
				0, 4,
				5, 1,
				5, 2,
				5, 3,
				5, 4,
				1, 2,
				2, 3,
				3, 4,
				4, 1
		};
		final int[] TRIANGLES = new int[] {
//				0,1,2,
//				0,2,3,
//				0,3,4,
//				0,4,1,
//				5,2,1,
//				5,3,2,
//				5,4,3,
//				5,1,4
				2, 1, 0,
				3, 2, 0,
				4, 3, 0,
				1, 2, 5,
				2, 3, 5,
				3, 4, 5,
				1, 7, 0,
				7, 1, 5,
				6, 4, 0,
				4, 6, 5,
				7, 6, 0,
				6, 7, 5
		};
		final int[] NORMAL_INDICES = new int[] {
				0,
				1,
				2,
				4,
				5,
				6,
				3,
				7,
				3,
				7,
				3,
				7
		};
		final MaterialProperties mp = new MaterialProperties();
		final MaterialProperties mpBlack = new MaterialProperties(0, 0, 0);
		final Point3f[] ap3Points = new Point3f[8];
		final Vector3f[] av3Normals = new Vector3f[8];
		final Point3f p3Start = new Point3f();
		final Vector3f v3Extent = new Vector3f();
		final Color3f c3Selected = new Color3f(Settings.getInstance().colors.selectedPoints);
		final Color3f c3FreeEnd = new Color3f(Settings.getInstance().colors.points);
		final Color3f c3AttachedEnd = new Color3f(Settings.getInstance().colors.headPoints);
		public BoneRenderer() {
			for (int i = 0; i < 8; ap3Points[i++] = new Point3f());
			for (int i = 0; i < 8; av3Normals[i++] = new Vector3f());
			mp.metallic = 0.5f;
			mpBlack.metallic = 0.5f;
		}
		
		private void reset() {
			ap3Points[0].set( 0, 0, 0);
			ap3Points[1].set(-R, A,-R);
			ap3Points[2].set( R, A,-R);
			ap3Points[3].set( R, A, R);
			ap3Points[4].set(-R, A, R);
			ap3Points[5].set( 0, 1, 0);
			ap3Points[6].set(-R, A, R * 0.333333f);
			ap3Points[7].set(-R, A,-R * 0.333333f);
			av3Normals[0].set( 0,-R,-A);
			av3Normals[1].set( A,-R, 0);
			av3Normals[2].set( 0,-R, A);
			av3Normals[3].set(-A,-R, 0);
			av3Normals[4].set( 0, R, B);
			av3Normals[5].set(-B, R, 0);
			av3Normals[6].set( 0, R,-B);
			av3Normals[7].set( B, R, 0);
//			av3Normals[0].set( 0, R, A);
//			av3Normals[1].set(-A, R, 0);
//			av3Normals[2].set( 0, R,-A);
//			av3Normals[3].set( A, R, 0);
//			av3Normals[4].set( 0,-R,-B);
//			av3Normals[5].set( B,-R, 0);
//			av3Normals[6].set( 0,-R, B);
//			av3Normals[7].set(-B,-R, 0);
		}
		
		public void drawBone(JPatchDrawable2 drawable, ViewDefinition viewDef, Bone bone) {
			Selection selection = MainFrame.getInstance().getSelection();
			boolean selected = selection != null && selection.containsBone(bone);
//			System.out.println("drawBone bone=" + bone);
			reset();
			bone.getStart(p3Start);
//			bone.setExtent();
			v3Extent.set(bone.getEnd(null));
			v3Extent.sub(p3Start);
			float length = v3Extent.length();
			v3Extent.scale(1f / length);
			if (bone.getLastDof() != null)
				bone.getLastDof().getInvTransform().transform(v3Extent);
			Vector3f v3X = Utils3D.perpendicularVector(v3Extent);
			AxisAngle4f aaa = new AxisAngle4f(v3Extent, bone.getJointRotation() / 180 * (float) Math.PI);
			Matrix3f mmm = new Matrix3f();
			mmm.set(aaa);
			mmm.transform(v3X);
			Vector3f v3Z = new Vector3f();
			v3Z.cross(v3X, v3Extent);
			if (bone.getLastDof() != null) {
				bone.getLastDof().getTransform().transform(v3Extent);
				bone.getLastDof().getTransform().transform(v3X);
				bone.getLastDof().getTransform().transform(v3Z);
			}
			Matrix4f matrix = new Matrix4f(
					v3X.x * length, v3Extent.x * length, v3Z.x * length, p3Start.x,
					v3X.y * length, v3Extent.y * length, v3Z.y * length, p3Start.y,
					v3X.z * length, v3Extent.z * length, v3Z.z * length, p3Start.z,
					0f,    0f,    0f,   1f
			);
			Matrix4f m = new Matrix4f(m4View);
			m.mul(matrix);
			for (int i = 0; i < ap3Points.length; i++)
				m.transform(ap3Points[i]);
			
			if (bone.getDofs().size() == 0) {
				if (selected)
					drawable.setColor(new Color3f(0.75f, 0.75f, 0.75f));
				else
					drawable.setColor(new Color3f(1.0f, 1.0f, 1.0f));		
				for (int i = 0; i < LINES.length; )
					drawable.drawLine(ap3Points[LINES[i++]], ap3Points[LINES[i++]]);
				return;
			}
			
//			Vector3f vBone = new Vector3f(bone.getReferenceEnd());
//			vBone.sub(bone.getReferenceStart());
//			Vector3f vPerp = Utils3D.perpendicularVector(vBone);
//			vPerp.normalize();
//			vPerp.scale(vBone.length());
//			if (bone.getLastDof() != null)
//				bone.getLastDof().getTransform().transform(vPerp);
//			viewDef.getMatrix().transform(vPerp);
//			Point3f pp = new Point3f(ap3Points[0]);
//			pp.add(vPerp);
//			drawable.drawLine(ap3Points[0], pp);
			
			for (int i = 0; i < av3Normals.length; i++) {
//				Vector3f a = new Vector3f(ap3Points[TRIANGLES[i * 3 + 1]]);
//				Vector3f b = new Vector3f(ap3Points[TRIANGLES[i * 3 + 2]]);
//				a.sub(ap3Points[TRIANGLES[i * 3]]);
//				b.sub(ap3Points[TRIANGLES[i * 3]]);
//				av3Normals[NORMAL_INDICES[i]].cross(b, a);
				m.transform(av3Normals[NORMAL_INDICES[i]]);
				av3Normals[NORMAL_INDICES[i]].normalize();
			}
			if (drawable.isShadingSupported()) {
				Color3f color = bone.getColor();
				mp.red = color.x;
				mp.green = color.y;
				mp.blue = color.z;
//				mpBlack.red = color.x * 0.25f;
//				mpBlack.green = color.y * 0.25f;
//				mpBlack.blue = color.z * 0.25f;
				if (drawable.isLightingSupported()) {
					drawable.setLightingEnabled(true);
					drawable.setMaterial(mp);
					for (int i = 0, t = 0; i < NORMAL_INDICES.length; i++) {
						if (i == 10)
							drawable.setMaterial(mpBlack);
						Vector3f normal = av3Normals[NORMAL_INDICES[i]];
						if (normal.z < 0) {
							drawable.drawTriangle(
									ap3Points[TRIANGLES[t++]], normal,
									ap3Points[TRIANGLES[t++]], normal,
									ap3Points[TRIANGLES[t++]], normal
							);
//							t -= 3;
//							Point3f p1 = Functions.average(ap3Points[TRIANGLES[t++]], ap3Points[TRIANGLES[t++]], ap3Points[TRIANGLES[t++]]);
//							Point3f p2 = new Point3f(p1);
//							Vector3f n = new Vector3f(av3Normals[NORMAL_INDICES[i]]);
//							n.scale(100);
//							p2.add(n);
//							drawable.drawLine(p1, p2);
						} else {
							t += 3;
						}
					}
					drawable.setLightingEnabled(false);
				} else {
					Color3f c = new Color3f();
					MaterialProperties mat = mp;
					for (int i = 0, t = 0; i < NORMAL_INDICES.length; i++) {
						if (i == 10)
							mat = mpBlack;
						Vector3f normal = av3Normals[NORMAL_INDICES[i]];
						if (normal.z < 0) {
							viewDef.getLighting().shade(ap3Points[TRIANGLES[t]], normal, mat, c);
							drawable.setColor(c);
							drawable.drawTriangle(ap3Points[TRIANGLES[t++]], ap3Points[TRIANGLES[t++]], ap3Points[TRIANGLES[t++]]);
						} else {
							t += 3;
						}
					}
				}
			} else if (!selected) {
				drawable.setColor(bone.getColor());
				for (int i = 0; i < LINES.length; )
					drawable.drawLine(ap3Points[LINES[i++]], ap3Points[LINES[i++]]);
			}
			if (selected) {
				drawable.setColor(new Color3f(1, 1, 1));
				for (int i = 0; i < LINES.length; )
					drawable.drawLine(ap3Points[LINES[i++]], ap3Points[LINES[i++]]);
			}
			
//			if (bone.getParentBone() == null) {
//				drawable.setColor(c3FreeEnd);
//				drawable.drawPoint(ap3Points[0]);
//			}
			drawable.setPointSize(3);
			drawable.setColor(selection != null && selection.contains(bone.getBoneEnd()) ? c3Selected : (bone.getChildCount() == 0) ? c3FreeEnd: c3AttachedEnd);
			drawable.drawPoint(ap3Points[5]);
			if (bone.getParent() != null && selection != null && selection.contains(bone.getBoneStart())) {
				drawable.setColor(c3Selected);
				drawable.drawPoint(ap3Points[0]);
			}
			
			if (selected && selection.getMap().size() == 2) {
				int d = 0;
//				System.out.println(bone.getDofs().size());
				for (Iterator it = bone.getDofs().iterator(); it.hasNext(); ) {
					RotationDof dof = (RotationDof) it.next();
//					System.out.println("drawing " + bone + "." + dof + " " + d);
					Vector3f axis = dof.getAxis();
					if (dof.getParentDof() != null)
						dof.getParentDof().getTransform().transform(axis);
					axis.normalize();
					axis.scale(length * 0.25f);
					m4View.transform(axis);
					Point3f a = new Point3f(bone.getStart(null));
					m4View.transform(a);
					Point3f b = new Point3f(a);
					a.add(axis);
					b.sub(axis);
					switch(d) {
						case 0: drawable.setColor(new Color3f(1,0,0)); break;
						case 1: drawable.setColor(new Color3f(0,1,0)); break;
						case 2: drawable.setColor(new Color3f(0,0,1)); break;
					}
					drawable.drawLine(a, b);
					axis = dof.getAxis();
					if (dof.getParentDof() != null)
						dof.getParentDof().getTransform().transform(axis);
	//				if (bone.getParentBone() != null)
	//					bone.getParentBone().getLastDof().getTransform().transform(axis);
					axis.normalize();
					Matrix3f m3 = new Matrix3f();
					AxisAngle4f aa = new AxisAngle4f(axis, 0);
					Vector3f v = new Vector3f();
					Vector3f vv = new Vector3f();
					for (float alpha = dof.getMin(); alpha <= dof.getMax(); alpha += 5) {
						v.scale(length * 0.5f, v3Extent);
						aa.angle = (alpha - dof.getValue()) / 180 * (float) Math.PI;
						m3.set(aa);
						m3.transform(v);
						vv.scale(0.9f, v);
						bone.getStart(a);
						b.set(a);
						a.add(v);
						b.add(vv);
						m4View.transform(a);
						m4View.transform(b);
						drawable.drawLine(a, b);
					}
					v.scale(length * 0.5f, v3Extent);
					aa.angle = (dof.getMin() - dof.getValue()) / 180 * (float) Math.PI;
					m3.set(aa);
					m3.transform(v);
					vv.scale(0.5f, v);
					bone.getStart(a);
					b.set(a);
					a.add(v);
					b.add(vv);
					m4View.transform(a);
					m4View.transform(b);
					drawable.drawLine(a, b);
					v.scale(length * 0.5f, v3Extent);
					aa.angle = (dof.getMax() - dof.getValue()) / 180 * (float) Math.PI;
					m3.set(aa);
					m3.transform(v);
					vv.scale(0.5f, v);
					bone.getStart(a);
					b.set(a);
					a.add(v);
					b.add(vv);
					m4View.transform(a);
					m4View.transform(b);
					drawable.drawLine(a, b);
					
					d++;
				}
			}
		}
	}
}
