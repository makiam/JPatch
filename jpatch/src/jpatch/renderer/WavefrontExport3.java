package jpatch.renderer;

import java.io.*;
import java.util.*;
import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.boundary.settings.AliasWavefrontSettings;

public class WavefrontExport3 {
	public static final int TRIANGLES = 0;
	public static final int QUADS = 1;
	
	public void writeToFile(File objfile, File mtlfile, int subdiv, boolean exportNormals, AliasWavefrontSettings.Mode mode) {
		
		BufferedWriter file;
		Model model = MainFrame.getInstance().getModel();
		
		/* write materials file */
		try {
			file = new BufferedWriter(new FileWriter(mtlfile));
			file.write("#generated with JPatch (http://www.jpatch.com)");
			file.newLine();
			for (Iterator iterator = model.getMaterialList().iterator(); iterator.hasNext();) {
				JPatchMaterial material = (JPatchMaterial)iterator.next();
				MaterialProperties mp = material.getMaterialProperties();
				//System.out.println(material.getName());
				file.newLine();
				
				/* material name */
				file.write("newmtl " + material.getName().replace(' ','_'));
				file.newLine();
				
				/* ambient color */
				file.write("Ka " + mp.red * mp.ambient + " " + mp.green * mp.ambient + " " + mp.blue * mp.ambient);
				file.newLine();
		
				/* diffuse color */
				file.write("Kd " + mp.red * mp.diffuse + " " + mp.green * mp.diffuse + " " + mp.blue * mp.diffuse);
				file.newLine();
				
				/* specular color */
				if (mp.specular != 0) {
					file.write("Ks " + mp.specular * (1f - mp.metallic * (1f - mp.red)) + " " + mp.specular * (1f - mp.metallic * (1f - mp.green)) + " " + mp.specular * (1f - mp.metallic * (1f - mp.blue)));
					file.newLine();
					file.write("Ns " + mp.roughness);
					file.newLine();
				}
				
				/* opacity */
				float d = 1 - (mp.transmit + mp.filter) / 2f;
				if (d != 1) {
					file.write("d " + d);
					file.newLine();
				}
			}
			file.close();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		
		/* write object file */
		
		PatchTesselator3 patchTesselator = new PatchTesselator3();
		patchTesselator.tesselate(model, subdiv, null, exportNormals);
		
		try {
			file = new BufferedWriter(new FileWriter(objfile));
			file.write("#generated with JPatch (http://www.jpatch.com)");
			file.newLine();
			file.write("mtllib " + mtlfile.getName());
			file.newLine();
			file.newLine();
			/*
			 * output all coordinates
			 */
			
			StringBuffer vertices = new StringBuffer();
			StringBuffer normals = new StringBuffer();
			
			PatchTesselator3.Vertex[] av = patchTesselator.getVertexArray();
			for (int i = 0; i < av.length; i++) {
				vertices.append("v " + av[i].p.x + " " + av[i].p.y + " " + (-av[i].p.z) + "\n");
				if (exportNormals) normals.append("vn " + av[i].n.x + " " + av[i].n.y + " " + (-av[i].n.z) + "\n");
			}
			
			file.write(vertices.toString());
			if (exportNormals) file.write(normals.toString());
			
			/*
			 * output all quads, sorted by material
			 */
			for (Iterator iterator = model.getMaterialList().iterator(); iterator.hasNext();) {
				JPatchMaterial material = (JPatchMaterial)iterator.next();
				file.write("usemtl " + material.getName().replace(' ','_'));
				file.newLine();
				switch (mode) {
					case TRIANGLES: {
						int[][] triangle = patchTesselator.getMaterialTriangleArray(material);
						for (int i = 0; i < triangle.length; i++) {
							if (exportNormals) file.write("f " + (triangle[i][0] + 1) + "/" + (triangle[i][0] + 1) + " " + (triangle[i][1] + 1) + "/" + (triangle[i][1] + 1) + " " + (triangle[i][2] + 1) + "/" + (triangle[i][2] + 1) + "\n");
							else file.write("f " + (triangle[i][0] + 1) + " " + (triangle[i][1] + 1) + " " + (triangle[i][2] + 1) + "\n");
						}
					} break;
					case QUADRILATERALS: {
						int[][] quad = patchTesselator.getMaterialQuadArray(material);
						for (int i = 0; i < quad.length; i++) {
							if (quad[i].length == 3) {
								if (exportNormals) file.write("f " + (quad[i][0] + 1) + "/" + (quad[i][0] + 1) + " " + (quad[i][1] + 1) + "/" + (quad[i][1] + 1) + " " + (quad[i][2] + 1) + "/" + (quad[i][2] + 1) + "\n");
								else file.write("f " + (quad[i][0] + 1) + " " + (quad[i][1] + 1) + " " + (quad[i][2] + 1) + "\n");
							} else {
								if (exportNormals) file.write("f " + (quad[i][0] + 1) + "/" + (quad[i][0] + 1) + " " + (quad[i][1] + 1) + "/" + (quad[i][1] + 1) + " " + (quad[i][2] + 1) + "/" + (quad[i][2] + 1) + " " + (quad[i][3] + 1) + "/" + (quad[i][3] + 1) + "\n");
								else file.write("f " + (quad[i][0] + 1) + " " + (quad[i][1] + 1) + " " + (quad[i][2] + 1) + " " + (quad[i][3] + 1) + "\n");
							}
						}
					} break;
				}
			}
			
			file.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}		
}
