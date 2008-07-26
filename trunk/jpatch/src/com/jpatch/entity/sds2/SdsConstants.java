package com.jpatch.entity.sds2;

public class SdsConstants {
	public static final int MAX_LEVEL = 5;
	public static final int MAX_VALENCE = 32;
	
	public static double[][] COSINUS = new double[MAX_VALENCE + 1][];
	
	static {
		for (int valence = 3; valence <= MAX_VALENCE; valence++) {
			COSINUS[valence] = new double[valence + 1];
			for (int i = 0; i <= valence; i++) {
				COSINUS[valence][i] = Math.cos(2 * Math.PI * i / valence);
			}
		}
	}
}
