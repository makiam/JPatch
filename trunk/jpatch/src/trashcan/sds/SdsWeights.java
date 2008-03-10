package trashcan.sds;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;
import static trashcan.sds.SdsConstants.*;

public class SdsWeights {
	static final float FACE0 = 1.0f / 4.0f;
	static final float EDGE0 = 3.0f / 8.0f;
	static final float EDGE1 = 1.0f / 16.0f;
	static final float VERTEX0 = 9.0f / 16.0f;
	static final float VERTEX1 = 3.0f / 32.0f;
	static final float VERTEX2 = 1.0f / 64.0f;
	static final float LIMIT0 = 16.0f / 36.0f;
	static final float LIMIT1 = 4.0f / 36.0f;
	static final float LIMIT2 = 1.0f / 36.0f;
	static final float CREASE0 = 3.0f / 4.0f;
	static final float CREASE1 = 1.0f / 8.0f;
	static final float CREASE_LIMIT0 = 2.0f / 3.0f;
	static final float CREASE_LIMIT1 = 1.0f / 6.0f;
	
	static final float[] VERTEX_EDGE = new float[MAX_VALENCE + 1];
	static final float[] VERTEX_FACE = new float[MAX_VALENCE + 1];
	static final float[] VERTEX_POINT = new float[MAX_VALENCE + 1];
	
	static final float[] VERTEX_EDGE_LIMIT = new float[MAX_VALENCE + 1];
	static final float[] VERTEX_FACE_LIMIT = new float[MAX_VALENCE + 1];
	static final float[] VERTEX_POINT_LIMIT = new float[MAX_VALENCE + 1];
	
	static final float[][] TANGENT_FACE_WEIGHT = new float[MAX_VALENCE + 1][];			// [valence][index]
	static final float[][] TANGENT_EDGE_WEIGHT = new float[MAX_VALENCE + 1][];			// [valence][index]

	
	static {
		for (int valence = 3; valence <= SdsConstants.MAX_VALENCE; valence++) {
			VERTEX_EDGE[valence] = 1.50f / (valence * valence);
			VERTEX_FACE[valence] = 0.25f / (valence * valence);
			VERTEX_POINT[valence] = (valence - 1.75f) / valence;
			
			VERTEX_EDGE_LIMIT[valence] = 4.0f / (valence * (valence + 5));
			VERTEX_FACE_LIMIT[valence] = 1.0f / (valence * (valence + 5));
			VERTEX_POINT_LIMIT[valence] = (float) valence / (valence + 5);
			
			TANGENT_FACE_WEIGHT[valence] = new float[valence];
			TANGENT_EDGE_WEIGHT[valence] = new float[valence];
			float An = (float) (1 + cos(2 * PI / valence) + cos(PI / valence) * sqrt(2 * (9 + cos(2 * PI / valence))));
			for (int j = 0; j < valence; j++) {
				TANGENT_EDGE_WEIGHT[valence][j] = (float) (An * cos(2 * PI  * j / valence));
				TANGENT_FACE_WEIGHT[valence][j] = (float) (cos(2 * PI * j / valence) + cos(2 * PI * (j + 1) / valence));
			}
		}
	}
}
