package com.jpatch.afw.vecmath;

/**
 * Static utility methods for linear algebra
 * Based on code from the javax.vecmath package released under the GNU General Public License.
 */
public class MatrixUtils {

	
	public static final void printMatrix(double[] m) {
		final int dim = dim(m);
		int i = 0;
		for (int row = 0; row < dim; row++) {
			for (int col = 0; col < dim; col++) {
				System.out.print(m[i++]);
				System.out.print(" ");
			}
			System.out.println();
		}
	}
	
	private static final int dim(double[] m) {
		int i = 0;
		int d = m.length;
		while(i * i < d) {
			i++;
		}
		if (i * i == d) {
			return i;
		}
		throw new IllegalArgumentException("matrix not square");
	}
	
	/**
     * Solves a set of linear equations.  The input parameters "matrix",
     * and "row_perm" come from luDecompostion and do not change
     * here.  The parameter "b" is a set of column vectors assembled
     * into a nxm matrix of floating-point values.  The procedure takes each
     * column of "b" in turn and treats it as the right-hand side of the
     * matrix equation Ax = LUx = b.  The solution vector replaces the
     * original column of the matrix.
     */
	private static void luBacksubstitution(int dim, double[] matrix, int[] row_perm, double[] b, double[] x) {
		final int size = x.length / dim;
		System.arraycopy(b, 0, x, 0, b.length);
		
		// rp = row_perm;
		int rp = 0;

		// For each column vector of matrix x ... 
		for (int k = 0; k < size; k++) {
			// cv = &(matrix2[0][k]);
			int cv = k;
			int ii = -1;

			// Forward substitution 
			for (int i = 0; i < dim; i++) {
				double sum;

				int ip = row_perm[rp+i];
				sum = x[cv+size*ip];
				x[cv+size*ip] = x[cv+size*i];
				if (ii >= 0) {
					// rv = &(matrix1[i][0]);
					int rv = i*dim;
					for (int j = ii; j <= i-1; j++) {
						sum -= matrix[rv+j] * x[cv+size*j];
					}
				}
				else if (sum != 0.0) {
					ii = i;
				}
				x[cv+size*i] = sum;
			}

			// Backsubstitution 
			for (int i = 0; i < dim; i++) {
				int ri = (dim-1-i);
				int rv = dim*(ri);
				double tt = 0.0;
				for(int j=1;j<=i;j++) {
					tt += matrix[rv+dim-j] * x[cv+size*(dim-j)]; 	  
				}
				x[cv+size*ri]= (x[cv+size*ri] - tt) / matrix[rv+ri];
			}
		}
	}
	
	/**
     * Solves a set of linear equations. The parameter "b" is a set of column vectors assembled
     * into a nxm matrix of floating-point values.  The procedure takes each
     * column of "b" in turn and treats it as the right-hand side of the
     * matrix equation Ax = LUx = b.  The solution vector replaces the
     * original column of the matrix.
     */
	public static void solve(double[] A, double[] b, double[] x) {
		int dim = dim(A);
		int[] row_perm = new int[dim];
		int[] even_row_xchg = new int[1];
		if (!luDecomposition(dim, A, row_perm, even_row_xchg)) {
			throw new IllegalArgumentException("Singular matrix"); 
		}
		luBacksubstitution(dim, A, row_perm, b, x);
	}
	
	/**
	 * Given a nxn array "matrix0", this function replaces it with the 
	 * LU decomposition of a row-wise permutation of itself.  The input 
	 * parameters are "matrix0" and "dim".  The array "matrix0" is also 
	 * an output parameter.  The vector "row_perm[]" is an output 
	 * parameter that contains the row permutations resulting from partial 
	 * pivoting.  The output parameter "even_row_xchg" is 1 when the 
	 * number of row exchanges is even, or -1 otherwise.  Assumes data 
	 * type is always double.
	 *
	 * @return true if the matrix is nonsingular, or false otherwise.
	 */
	//
	// Reference: Press, Flannery, Teukolsky, Vetterling, 
	//	      _Numerical_Recipes_in_C_, Cambridge University Press, 
	//	      1988, pp 40-45.
	//
	public static final boolean luDecomposition(int dim, double[] matrix0,	int[] row_perm, int[] even_row_xchg) {

		double row_scale[] = new double[dim];

		// Determine implicit scaling information by looping over rows 
		int i, j;
		int ptr, rs, mtx;
		double big, temp;

		ptr = 0;
		rs = 0;
		even_row_xchg[0] = 1;

		// For each row ... 
		i = dim;
		while (i-- != 0) {
			big = 0.0;

			// For each column, find the largest element in the row 
			j = dim;
			while (j-- != 0) {
				temp = matrix0[ptr++];
				temp = Math.abs(temp);
				if (temp > big) {
					big = temp;
				}
			}

			// Is the matrix singular? 
			if (big == 0.0) {
				return false;
			}
			row_scale[rs++] = 1.0 / big;
		}

		// For all columns, execute Crout's method 
		mtx = 0;
		for (j = 0; j < dim; j++) {
			int imax, k;
			int target, p1, p2;
			double sum;

			// Determine elements of upper diagonal matrix U 
			for (i = 0; i < j; i++) {
				target = mtx + (dim*i) + j;
				sum = matrix0[target];
				k = i;
				p1 = mtx + (dim*i);
				p2 = mtx + j;
				while (k-- != 0) {
					sum -= matrix0[p1] * matrix0[p2];
					p1++;
					p2 += dim;
				}
				matrix0[target] = sum;
			}

			// Search for largest pivot element and calculate
			// intermediate elements of lower diagonal matrix L.
			big = 0.0;
			imax = -1;
			for (i = j; i < dim; i++) {
				target = mtx + (dim*i) + j;
				sum = matrix0[target];
				k = j;
				p1 = mtx + (dim*i);
				p2 = mtx + j;
				while (k-- != 0) {
					sum -= matrix0[p1] * matrix0[p2];
					p1++;
					p2 += dim;
				}
				matrix0[target] = sum;

				// Is this the best pivot so far? 
				if ((temp = row_scale[i] * Math.abs(sum)) >= big) {
					big = temp;
					imax = i;
				}
			}

			if (imax < 0) {
				throw new RuntimeException("Logic error: imax < 0");
			}

			// Is a row exchange necessary? 
			if (j != imax) {
				// Yes: exchange rows 
				k = dim;
				p1 = mtx + (dim*imax);
				p2 = mtx + (dim*j);
				while (k-- != 0) {
					temp = matrix0[p1];
					matrix0[p1++] = matrix0[p2];
					matrix0[p2++] = temp;
				}

				// Record change in scale factor 
				row_scale[imax] = row_scale[j];
				even_row_xchg[0] = -even_row_xchg[0]; // change exchange parity
			}

			// Record row permutation 
			row_perm[j] = imax;

			// Is the matrix singular 
			if (matrix0[(mtx + (dim*j) + j)] == 0.0) {
				return false;
			}

			// Divide elements of lower diagonal matrix L by pivot 
			if (j != (dim-1)) {
				temp = 1.0 / (matrix0[(mtx + (dim*j) + j)]);
				target = mtx + (dim*(j+1)) + j;
				i = (dim-1) - j;
				while (i-- != 0) {
					matrix0[target] *= temp;
					target += dim;
				}
			}
		}
		return true;
	}
}
