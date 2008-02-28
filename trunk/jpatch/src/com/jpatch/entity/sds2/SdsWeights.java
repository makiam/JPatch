package com.jpatch.entity.sds2;

import static java.lang.Math.*;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;
import static com.jpatch.entity.sds2.SdsConstants.MAX_VALENCE;

public class SdsWeights {
	static final int MAX_VALENCE = 64;
	static final double[][] TANGENT_FACE_WEIGHTS = new double[MAX_VALENCE + 1][];			// [valence][index]
	static final double[][] TANGENT_PAIR_WEIGHTS = new double[MAX_VALENCE + 1][];
	static final double[][] IRREGULAR_TANGENT_WEIGHTS = new double[MAX_VALENCE + 1][];
	static final double[][] BOUNDARY_TANGENT_WEIGHTS = new double[MAX_VALENCE + 1][];
	static final double CREASE0 = 3.0f / 4.0f;
	static final double CREASE1 = 1.0f / 8.0f;
	static final double CREASE_LIMIT0 = 2.0f / 3.0f;
	static final double CREASE_LIMIT1 = 1.0f / 6.0f;
	static final double[] VERTEX_LIMIT_RIM_WEIGHTS = new double[MAX_VALENCE + 1];
	static final double[] VERTEX_LIMIT_CENTER_WEIGHTS = new double[MAX_VALENCE + 1];
	
	static {
		for (int valence = 3; valence <= MAX_VALENCE; valence++) {
			TANGENT_FACE_WEIGHTS[valence] = new double[valence];
			TANGENT_PAIR_WEIGHTS[valence] = new double[valence];
			double An = 1 + cos(2 * PI / valence) + cos(PI / valence) * sqrt(2 * (9 + cos(2 * PI / valence)));
			for (int j = 0; j < valence; j++) {
				TANGENT_PAIR_WEIGHTS[valence][j] = (cos(2 * PI * j / valence) + cos(2 * PI * (j + 1) / valence)) / 4.0;
			}
			for (int j = 0; j < valence; j++) {
				int prev = j - 1;
				if (prev < 0) {
					prev = valence - 1;
				}
				int next = j + 1;
				if (next >= valence) {
					next = 0;
				}
				TANGENT_FACE_WEIGHTS[valence][j] = An * cos(2 * PI  * j / valence)
					+ TANGENT_PAIR_WEIGHTS[valence][prev]
					+ TANGENT_PAIR_WEIGHTS[valence][next];
			}
			
			IRREGULAR_TANGENT_WEIGHTS[valence] = new double[valence];
			BOUNDARY_TANGENT_WEIGHTS[valence] = new double[valence];
			for (int i = 0; i < valence; i++) {
				IRREGULAR_TANGENT_WEIGHTS[valence][i] = cos(2 * PI * i / valence);
				BOUNDARY_TANGENT_WEIGHTS[valence][i] = sin(PI * i / valence);
			}
			
			VERTEX_LIMIT_RIM_WEIGHTS[valence] = 1.0 / (valence * valence);
			VERTEX_LIMIT_CENTER_WEIGHTS[valence] = (valence - 2.0) / valence;
		}
	}
	
	
//	static final double FACE0 = 1.0f / 4.0f;
//	static final double EDGE0 = 3.0f / 8.0f;
//	static final double EDGE1 = 1.0f / 16.0f;
//	static final double VERTEX0 = 9.0f / 16.0f;
//	static final double VERTEX1 = 3.0f / 32.0f;
//	static final double VERTEX2 = 1.0f / 64.0f;
//	static final double LIMIT0 = 16.0f / 36.0f;
//	static final double LIMIT1 = 4.0f / 36.0f;
//	static final double LIMIT2 = 1.0f / 36.0f;
//	static final double CREASE0 = 3.0f / 4.0f;
//	static final double CREASE1 = 1.0f / 8.0f;
//	static final double CREASE_LIMIT0 = 2.0f / 3.0f;
//	static final double CREASE_LIMIT1 = 1.0f / 6.0f;
//	
//	static final double[] VERTEX_EDGE = new double[MAX_VALENCE + 1];
//	static final double[] VERTEX_FACE = new double[MAX_VALENCE + 1];
//	static final double[] VERTEX_POINT = new double[MAX_VALENCE + 1];
//	
//	// weights for face points
//	static final double[] LIMIT_EDGE_WEIGHTS = new double[MAX_VALENCE + 1];
//	static final double[] LIMIT_CORNER_WEIGHTS = new double[MAX_VALENCE + 1];
//	static final double[] LIMIT_CENTER_WEIGHTS = new double[MAX_VALENCE + 1];
//	
//	// weights for vertex points
//	static final double[] VERTEX_LIMIT_RIM_WEIGHTS = new double[MAX_VALENCE + 1];
//	static final double[] VERTEX_LIMIT_CENTER_WEIGHTS = new double[MAX_VALENCE + 1];
//	
//	static final double[][] TANGENT_CORNER_WEIGHTS = new double[MAX_VALENCE + 1][];			// [valence][index]
//	static final double[][] TANGENT_EDGE_WEIGHTS = new double[MAX_VALENCE + 1][];			// [valence][index]
//
//	
//	static {
//		for (int valence = 3; valence <= SdsConstants.MAX_VALENCE; valence++) {
//			VERTEX_EDGE[valence] = 1.50 / (valence * valence);
//			VERTEX_FACE[valence] = 0.25 / (valence * valence);
//			VERTEX_POINT[valence] = (valence - 1.75) / valence;
//			
//			LIMIT_EDGE_WEIGHTS[valence] = 4.0 / (valence * (valence + 5.0));
//			LIMIT_CORNER_WEIGHTS[valence] = 1.0 / (valence * (valence + 5.0));
//			LIMIT_CENTER_WEIGHTS[valence] = valence / (valence + 5.0);
//			
//			TANGENT_CORNER_WEIGHTS[valence] = new double[valence];
//			TANGENT_EDGE_WEIGHTS[valence] = new double[valence];
//			double An = 1 + cos(2 * PI / valence) + cos(PI / valence) * sqrt(2 * (9 + cos(2 * PI / valence)));
//			for (int j = 0; j < valence; j++) {
//				TANGENT_CORNER_WEIGHTS[valence][j] = An * cos(2 * PI  * j / valence);
//				TANGENT_EDGE_WEIGHTS[valence][j] = cos(2 * PI * j / valence) + cos(2 * PI * (j + 1) / valence);
//			}
//			
//			VERTEX_LIMIT_RIM_WEIGHTS[valence] = 1.0 / (valence * valence);
//			VERTEX_LIMIT_CENTER_WEIGHTS[valence] = (valence - 2.0) / valence;
//		}
//	}
}
