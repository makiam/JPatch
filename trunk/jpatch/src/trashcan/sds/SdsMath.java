package trashcan.sds;

public class SdsMath {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int n = 5;
		int depth = 100;
		LinearCombination<String>[][] edge = new LinearCombination[depth][n];
		LinearCombination<String>[][] face = new LinearCombination[depth][n];
		LinearCombination<String>[] vertex = new LinearCombination[depth];
		
		vertex[0] = new LinearCombination<String>();
		vertex[0].add("v", 1);
		for (int i = 0; i < n; i++) {
			edge[0][i] = new LinearCombination<String>();
			edge[0][i].add("e" + i, 1);
			face[0][i] = new LinearCombination<String>();
			face[0][i].add("f" + i, 1);
		}
		System.out.println("v(" + 0 + ") = " + vertex[0]);
		double wv = (n - 2.0) / n;
		double wpf = 1.0 / (n * n);
		System.out.println(wv + ", " + wpf);
		for (int level = 1; level < depth; level++) {
			vertex[level] = new LinearCombination<String>();
			vertex[level].addScaled(vertex[level - 1], wv);
			for (int i = 0; i < n; i++) {
				edge[level][i] = new LinearCombination<String>();
				edge[level][i].addScaled(edge[level - 1][i], 0.25);
				edge[level][i].addScaled(vertex[level - 1], 0.25);
				edge[level][i].addScaled(face[level - 1][i], 0.25);
				edge[level][i].addScaled(face[level - 1][(i + 1) % n], 0.25);
				
				vertex[level].addScaled(edge[level - 1][i], wpf);
				vertex[level].addScaled(face[level - 1][i], wpf);
				
				
				
			}
			for (int i = 0; i < n; i++) {
				
				face[level][i] = new LinearCombination<String>();
				face[level][i].addScaled(face[level - 1][i], 0.25);
				face[level][i].addScaled(vertex[level], 0.25);
				face[level][i].addScaled(edge[level][i], 0.25);
				face[level][i].addScaled(edge[level][(i + n - 1) % n], 0.25);
				
				
				
			}
			System.out.println("v(" + level + ") = " + vertex[level]);
		}
	}

}
