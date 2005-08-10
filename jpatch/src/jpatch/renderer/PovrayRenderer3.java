package jpatch.renderer;

import java.io.*;
import java.util.*;
import javax.vecmath.*;
import jpatch.entity.*;
import jpatch.boundary.*;

public class PovrayRenderer3 {
	
	private PatchTesselator3 patchTesselator = new PatchTesselator3();
	
	public void writeFrame(List animModels, Camera camera, List lights, String include, BufferedWriter file) throws IOException {
		JPatchSettings settings = JPatchSettings.getInstance();

		/* Header */
		
		file.write("/*\n");
		file.write(" * JPatch animator POV-Ray scene file\n");
		file.write(" */\n");
		file.write("\n");
		
		/* POV-Ray global include block */
		
		/* Frame number */
		
		file.write("/*\n");
		file.write(" * Frame number\n");
		file.write(" */\n");
		file.write("#declare jpatchFrame = " + Animator.getInstance().getPosition() + ";\n\n");
		
		file.write("/*\n");
		file.write(" * Include block\n");
		file.write(" */\n");
		file.write("\n");
		//file.write(JPatchSettings.getInstance().povraySettings.strInclude);
		file.write(include);
		
		/* Background */
		
		file.write("\n\n");
		file.write("/*\n");
		file.write(" * Background\n");
		file.write(" */\n");
		file.write("\n");
		file.write("background {\n");
		float[] rgb = settings.cBackgroundColor.getRGBColorComponents(new float[3]);
		file.write("\tcolor rgb <" + rgb[0] + "," + rgb[1] + "," + rgb[2] + ">\n");
		file.write("}\n\n");
		
		/* Camera */
		
		file.write("/*\n");
		file.write(" * Camera " + camera.getName() + "\n");
		file.write(" */\n");
		file.write("\n");
		file.write("camera {\n");
		Matrix4d cam = camera.getTransform();
		file.write("\tperspective\n");
		file.write("\tright x * " + JPatchSettings.getInstance().fRenderAspectWidth + "\n");
		file.write("\tup y * " + JPatchSettings.getInstance().fRenderAspectHeight + "\n");
		file.write("\tmatrix <" + cam.m00 + ", " + cam.m10 + ", " + cam.m20 + ", " +
					  cam.m01 + ", " + cam.m11 + ", " + cam.m21 + ", " +
					  cam.m02 + ", " + cam.m12 + ", " + cam.m22 + ", " +
					  cam.m03 + ", " + cam.m13 + ", " + cam.m23 + ">\n");
		file.write("\tangle " + camera.getFieldOfView() + "\n");
		file.write("}\n");
		file.write("\n");
		
		/* Light sources */
		
		for (int i = 0, n = lights.size(); i < n; i++) {
			AnimLight light = (AnimLight) lights.get(i);
			if (light.isActive()) {
				file.write("/*\n");
				file.write(" * Lightsource " + light.getName() + "\n");
				file.write(" */\n");
				file.write("\n");
				file.write(light(light));
			}
		}
		
		/* Models */
		
		for (Iterator it = animModels.iterator(); it.hasNext(); ) {
			AnimModel animModel = (AnimModel) it.next();
			Model model = animModel.getModel();
			
			file.write("/*\n");
			file.write(" * Model " + model.getName() + "\n");
			file.write(" */\n");
			file.write("\n");
					  				
			writeModel(model, animModel.getTransform(), animModel.getRenderString("povray", ""), animModel.getSubdivisionOffset(), file);
		}
	}

	public void writeModel(Model model, Matrix4d m, String renderString, int subdivOffset, BufferedWriter file) throws IOException {
		file.write("union {\n");
		file.write("\t" + renderString + "\n");
		if (JPatchSettings.getInstance().povraySettings.iOutputMode == 0) {
			int subdiv = JPatchSettings.getInstance().povraySettings.iSubdivMode + subdivOffset;
			if (subdiv < 1) subdiv = 1;
			if (subdiv > 5) subdiv = 5;
			patchTesselator.tesselate(model, subdiv, null, true);
		}
		for (Iterator itMat = model.getMaterialList().iterator(); itMat.hasNext(); ) {
			JPatchMaterial material = (JPatchMaterial) itMat.next();
			switch (JPatchSettings.getInstance().povraySettings.iOutputMode) {
				
				/* Generate tesselated Hash-patch output */
				
				case 0: {
					PatchTesselator3.Vertex[] vtx = patchTesselator.getPerMaterialVertexArray(material);
					if (vtx.length > 0) {
						file.write("\t/*\n");
						file.write("\t * Material " + material.getName() + "\n");
						file.write("\t */\n");
						file.write("\n");
						file.write("\tmesh2 {\n");
					
						file.write("\t\tvertex_vectors {\n");
						file.write("\t\t\t" + vtx.length + ",\n");
						for (int v = 0; v < vtx.length; v++) file.write("\t\t\t" + toPovVector(vtx[v].p) + ",\n");
						file.write("\t\t}\n");
						
						file.write("\t\tnormal_vectors {\n");
						file.write("\t\t\t" + vtx.length + ",\n");
						for (int v = 0; v < vtx.length; v++) file.write("\t\t\t" + toPovVector(vtx[v].n) + ",\n");
						file.write("\t\t}\n");
						
						//file.write("\t\tuv_vectors {\n");
						//file.write("\t\t\t" + vtx.length + ",\n");
						//for (int v = 0; v < vtx.length; v++) file.write("\t\t\t<" + vtx[v].r.x + ", " + vtx[v].r.z + ">,\n");
						
						//for (int v = 0; v < vtx.length; v++) {
						//	double x = vtx[v].r.x;
						//	double y = vtx[v].r.z;
						//	double l = Math.sqrt(x * x + y * y);
						//	x /= l;
						//	y /= l;
						//	//double a = (((x > y) ? Math.acos(x) : Math.asin(y)) + 2 * Math.PI) % (Math.PI / 2);
						//	double a = Math.acos(x);
						//	//if (y > 0 && x < 0) a += Math.PI / 2;
						//	//else if (y < 0 && x < 0) a += Math.PI;
						//	//else if (y < 0 && x > 0) a += 3 * Math.PI / 2;
						//	if (y < 0) a+= Math.PI / 2;
						//	a /= (2 * Math.PI);
						//	if (Double.isNaN(a))
						//		a = 0;
						//	file.write("\t\t\t<" + a + ", " + vtx[v].r.y + ">,\n");
						//}
						
						//file.write("\t\t}\n");
						
						int[][] triangles = patchTesselator.getPerMaterialTriangleArray();
						file.write("\t\tface_indices {\n");
						file.write("\t\t\t" + triangles.length + ",\n");
						for (int t = 0; t < triangles.length; t++) {
							file.write("\t\t\t<" + triangles[t][0] + "," + triangles[t][1] + "," + triangles[t][2] + ">,\n");
						}
						file.write("\t\t}\n");
						file.write("" + AbstractRenderer.shader(material.getMaterialProperties(), material.getRenderString("povray","")));
						file.write("}\n");
					}
				}
				break;
				
				/* generate Bezier-patch output */
				
				case 1: {
					boolean active = false;
					int steps = JPatchSettings.getInstance().povraySettings.iSubdivMode;
					for (Patch patch = model.getFirstPatch();patch != null; patch = patch.getNext()) {
						if (patch.getMaterial() == material) {
							if (!active) {
								file.write("\t/*\n");
								file.write("\t * Material " + material.getName() + "\n");
								file.write("\t */\n");
								file.write("\t \n");
								file.write("\tunion {\n");
								active = true;
							}
							Point3f[][] bicubicPatches = patch.bicubicPatches();
							for (int p = 0; p < bicubicPatches.length; p++) {
								Point3f[] bezierControlPoints = bicubicPatches[p];
								file.write("\t\tbicubic_patch { type 1 flatness 0 u_steps " + steps + " v_steps " + steps + " ");
								String strCPs = "";
								for (int c = 0; c < 16; c++) {
									strCPs += ("<" + bezierControlPoints[c].x + "," + bezierControlPoints[c].y + "," + bezierControlPoints[c].z + ">");
									if (c != 15) {
										strCPs += (",");
									}
								}
								file.write(strCPs + " }\n");
							}
						}
					}
					if (active) {
						file.write("" + AbstractRenderer.shader(material.getMaterialProperties(), material.getRenderString("povray","")));
						file.write("}\n");
					}
				}
				break;
			}
		}
		if (m != null) {
			file.write(
				"\tmatrix <" + m.m00 + ", " + m.m10 + ", " + m.m20 + ", " +
				m.m01 + ", " + m.m11 + ", " + m.m21 + ", " +
				m.m02 + ", " + m.m12 + ", " + m.m22 + ", " +
				m.m03 + ", " + m.m13 + ", " + m.m23 + ">\n"
			);
		}
		file.write("}\n");
	}
	
	private static String toPovVector(Tuple3d t) {
		return "<" + t.x + ", " + t.y + ", " + t.z + ">";
	}
	
	private static String toPovVector(Tuple3f t) {
		return "<" + t.x + ", " + t.y + ", " + t.z + ">";
	}
	
	public static String light(AnimLight light) {
		String s = light.getRenderString("povray", "");
		s = AbstractRenderer.light(light, s);
		s = s.replaceAll("\\$position",toPovVector(light.getPosition()));
		s = s.replaceAll("\\$color",toPovVector(light.getColor()));
		return s;
	}
}
