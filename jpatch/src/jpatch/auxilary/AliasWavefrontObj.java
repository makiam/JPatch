package jpatch.auxilary;

import javax.vecmath.*;

public class AliasWavefrontObj {
	public Point3f[] v;
	public Vector3f[] vn;
	public AliasWavefrontMat[] mat;
	public int[][][] fv;
	public int[][][] fn;
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < v.length; sb.append("v " + v[i].x + " " + v[i].y + " " + v[i++].z + "\n"));
		for (int i = 0; i < vn.length; sb.append("vn " + vn[i].x + " " + vn[i].y + " " + vn[i++].z + "\n"));
		for (int i = 0; i < fv.length; i++) {
			sb.append("usemtl mat" + i + "\n");
			//sb.append(mat[i].toString());
			for (int j = 0; j < fv[i].length; j++) {
				sb.append("f");
				for (int k = 0; k < fv[i][j].length; k++) {
					sb.append(" " + fv[i][j][k] + "/" + fn[i][j][k]);
				}
				sb.append("\n");
			}
		}
		return new String(sb);
	}
}
