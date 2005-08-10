package jpatch.auxilary;

import java.io.*;
import java.util.*;
import javax.vecmath.*;

public class JarObjReader {
	
	public AliasWavefrontObj readObj(String objResource, String mtlResource) {
		try {
			//File objFile = new File(filename);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(objResource)));
			String line;
			String[] token;
			String[] field;
			Map mapMaterials = new HashMap();
			mapMaterials.putAll(readMat(mtlResource));
			ArrayList v = new ArrayList();
			ArrayList vn = new ArrayList();
			ArrayList mat = new ArrayList();
			ArrayList faces = null;
			
			int V = 0;
			int F = 0;
			
			while ((line = reader.readLine()) != null) {
				token = line.split("\\s");
				if (token[0].equals("mtllib")) {
					//File matFile = new File(objFile.getParent(), token[1]);
					//mapMaterials.putAll(readMat(matFile));
				}
				else if (token[0].equals("v")) {
					v.add(new Point3f(Float.parseFloat(token[1]), Float.parseFloat(token[2]), Float.parseFloat(token[3])));
					V++;
				}
				else if (token[0].equals("vn")) {
					vn.add(new Vector3f(Float.parseFloat(token[1]), Float.parseFloat(token[2]), Float.parseFloat(token[3])));
				}
				else if (token[0].equals("usemtl")) {
					mat.add(mapMaterials.get(token[1]));
					faces = new ArrayList();
					mat.add(faces);
				}
				else if (token[0].equals("f")) {
					int n = token.length - 1;
					int[] vi = new int[n];
					int[] ni = new int[n];
					for (int i = 0; i < n; i++) {
						field = token[i + 1].split("/");
						vi[i] = Integer.parseInt(field[0]) - 1;
						ni[i] = (field.length == 2) ? Integer.parseInt(field[1]) - 1 : Integer.parseInt(field[2]) - 1;
					}
					faces.add(vi);
					faces.add(ni);
					F++;
				}
			}
						
			reader.close();
			
			AliasWavefrontObj obj = new AliasWavefrontObj();
			obj.v = new Point3f[v.size()];
			for (int i = 0, n = v.size(); i < n; obj.v[i] = (Point3f) v.get(i++));
			obj.vn = new Vector3f[vn.size()];
			for (int i = 0, n = vn.size(); i < n; obj.vn[i] = (Vector3f) vn.get(i++));
			obj.mat = new AliasWavefrontMat[mat.size() / 2];
			obj.fv = new int[obj.mat.length][][];
			obj.fn = new int[obj.mat.length][][];
			int m = 0;
			for (int i = 0, n = mat.size(); i < n; ) {
				obj.mat[m] = (AliasWavefrontMat) mat.get(i++);
				faces = (ArrayList) mat.get(i++);
				int nf = faces.size() / 2;
				obj.fv[m] = new int[nf][];
				obj.fn[m] = new int[nf][];
				int f = 0;
				for (int j = 0, nn = faces.size(); j < nn; ) {
					obj.fv[m][f] = (int[]) faces.get(j++);
					obj.fn[m][f] = (int[]) faces.get(j++);
					f++;
				}
				m++;
			}
			
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
		
	private Map readMat(String resource) throws IOException, NumberFormatException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resource)));
		String line;
		String[] token;
		AliasWavefrontMat mat = null;
		Map map = new HashMap();
		while ((line = reader.readLine()) != null) {
			token = line.split("[\\s]+");
			if (token[0].equals("newmtl")) {
				mat = new AliasWavefrontMat();
				map.put(token[1], mat);
			}
			else if (token[0].equals("Ka")) {
				mat.Ka.set(Float.parseFloat(token[1]), Float.parseFloat(token[2]), Float.parseFloat(token[3]));
			}
			else if (token[0].equals("Kd")) {
				mat.Kd.set(Float.parseFloat(token[1]), Float.parseFloat(token[2]), Float.parseFloat(token[3]));
			}
			else if (token[0].equals("Ks")) {
				mat.Ks.set(Float.parseFloat(token[1]), Float.parseFloat(token[2]), Float.parseFloat(token[3]));
			}
			else if (token[0].equals("Ns")) {
				mat.Ns = Float.parseFloat(token[1]);
			}
		}
		reader.close();
		return map;
	}
	
	public static void main(String[] args) {
		ObjReader objReader = new ObjReader();
		AliasWavefrontObj obj = objReader.readObj(args[0]);
		System.out.println(obj);
	}
}
