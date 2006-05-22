package patterns;

public final class Functions {
	
	/**
	* THIS CLASS CONTAINS THE
	* JAVA REFERENCE IMPLEMENTATIONS OF IMPROVED NOISE
	* AND THE
	* JAVA REFERENCE IMPLEMENTATIONS OF IMPROVED NOISE IN 4D
	* COPYRIGHT 2002 KEN PERLIN
	**/
	
	private static final int p[] = new int[512], permutation[] = { 151,160,137,91,90,15,
	131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,8,99,37,240,21,10,23,
	190, 6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,35,11,32,57,177,33,
	88,237,149,56,87,174,20,125,136,171,168, 68,175,74,165,71,134,139,48,27,166,
	77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,55,46,245,40,244,
	102,143,54, 65,25,63,161, 1,216,80,73,209,76,132,187,208, 89,18,169,200,196,
	135,130,116,188,159,86,164,100,109,198,173,186, 3,64,52,217,226,250,124,123,
	5,202,38,147,118,126,255,82,85,212,207,206,59,227,47,16,58,17,182,189,28,42,
	223,183,170,213,119,248,152, 2,44,154,163, 70,221,153,101,155,167, 43,172,9,
	129,22,39,253, 19,98,108,110,79,113,224,232,178,185, 112,104,218,246,97,228,
	251,34,242,193,238,210,144,12,191,179,162,241, 81,51,145,235,249,14,239,107,
	49,192,214, 31,181,199,106,157,184, 84,204,176,115,121,50,45,127, 4,150,254,
	138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180
	};
	
	static {
		for (int i=0; i < 256 ; i++) p[256+i] = p[i] = permutation[i];
	}
	
	static public float fBm3f(float x, float y, float z, int octaves, float lacunarity, float gain) {
		float sum = 0;
		float amp = 1;
		for (int i = 0; i < octaves; i++) {
			sum += amp * noise3f(x, y, z);
			x *= lacunarity;
			y *= lacunarity;
			z *= lacunarity;
			amp *= gain;
		}
		return sum;
	}
	
	static public float turbulence3f(float x, float y, float z, int octaves, float lacunarity, float gain) {
		float sum = 0;
		float amp = 1;
		for (int i = 0; i < octaves; i++) {
			sum += amp * (float) Math.abs(noise3f(x, y, z));
			x *= lacunarity;
			y *= lacunarity;
			z *= lacunarity;
			amp *= gain;
		}
		return sum;
	}
	
	static public float noise3f(float x, float y, float z) {
		int X = (int) Math.floor(x) & 255,		// FIND UNIT CUBE THAT
		Y = (int) Math.floor(y) & 255,			// CONTAINS POINT.
		Z = (int) Math.floor(z) & 255;
		x -= (float) Math.floor(x);			// FIND RELATIVE X,Y,Z
		y -= (float) Math.floor(y);			// OF POINT IN CUBE.
		z -= (float) Math.floor(z);
		float u = fadef(x),				// COMPUTE FADE CURVES
		v = fadef(y),					// FOR EACH OF X,Y,Z.
		w = fadef(z);
		int A = p[X]+Y, AA = p[A]+Z, AB = p[A+1]+Z,	// HASH COORDINATES OF
		B = p[X+1]+Y, BA = p[B]+Z, BB = p[B+1]+Z;	// THE 8 CUBE CORNERS,
		
		return lerpf(w, lerpf(v, lerpf(u, grad3f(p[AA], x, y, z),	// AND ADD
			grad3f(p[BA], x-1, y, z)),				// BLENDED
			lerpf(u, grad3f(p[AB], x, y-1, z),			// RESULTS
			grad3f(p[BB], x-1, y-1, z))),				// FROM  8
			lerpf(v, lerpf(u, grad3f(p[AA+1], x, y, z-1),		// CORNERS
		 	grad3f(p[BA+1], x-1, y, z-1)),				// OF CUBE
			lerpf(u, grad3f(p[AB+1], x, y-1, z-1),
			grad3f(p[BB+1], x-1, y-1, z-1 ))));
	}
	
	static public double noise3d(double x, double y, double z) {
		int X = (int) Math.floor(x) & 255,		// FIND UNIT CUBE THAT
		Y = (int) Math.floor(y) & 255,			// CONTAINS POINT.
		Z = (int) Math.floor(z) & 255;
		x -= Math.floor(x);				// FIND RELATIVE X,Y,Z
		y -= Math.floor(y);				// OF POINT IN CUBE.
		z -= Math.floor(z);
		double u = faded(x),				// COMPUTE FADE CURVES
		v = faded(y),					// FOR EACH OF X,Y,Z.
		w = faded(z);
		int A = p[X  ]+Y, AA = p[A]+Z, AB = p[A+1]+Z,	// HASH COORDINATES OF
		B = p[X+1]+Y, BA = p[B]+Z, BB = p[B+1]+Z;	// THE 8 CUBE CORNERS,
		
		return lerpd(w, lerpd(v, lerpd(u, grad3d(p[AA], x, y, z),	// AND ADD
			grad3d(p[BA], x-1, y, z)),				// BLENDED
			lerpd(u, grad3d(p[AB], x, y-1, z),			// RESULTS
			grad3d(p[BB], x-1, y-1, z))),				// FROM  8
			lerpd(v, lerpd(u, grad3d(p[AA+1], x, y, z-1),		// CORNERS
		 	grad3d(p[BA+1], x-1, y, z-1)),				// OF CUBE
			lerpd(u, grad3d(p[AB+1], x, y-1, z-1),
			grad3d(p[BB+1], x-1, y-1, z-1 ))));
	}

	static public float noise4f(float x, float y, float z, float w) {
		int X = (int) Math.floor(x) & 255,		// FIND UNIT HYPERCUBE
		Y = (int) Math.floor(y) & 255,			// THAT CONTAINS POINT.
		Z = (int) Math.floor(z) & 255,
		W = (int) Math.floor(w) & 255;
		
		x -= (float) Math.floor(x);			// FIND RELATIVE X,Y,Z,W
		y -= (float) Math.floor(y);			// OF POINT IN CUBE.
		z -= (float) Math.floor(z);
		w -= (float) Math.floor(w);
		
		float a = fadef(x),				// COMPUTE FADE CURVES
		b = fadef(y),					// FOR EACH OF X,Y,Z,W.
		c = fadef(z),
		d = fadef(w);
		
		int A = p[X  ]+Y, AA = p[A]+Z, AB = p[A+1]+Z,	// HASH COORDINATES OF
		B = p[X+1]+Y, BA = p[B]+Z, BB = p[B+1]+Z,	// THE 16 CORNERS OF
		AAA = p[AA]+W, AAB = p[AA+1]+W,			// THE HYPERCUBE.
		ABA = p[AB]+W, ABB = p[AB+1]+W,
		BAA = p[BA]+W, BAB = p[BA+1]+W,
		BBA = p[BB]+W, BBB = p[BB+1]+W;
	
		return lerpf(d,					 // INTERPOLATE DOWN.
			lerpf(c,lerpf(b,lerpf(a,grad4f(p[AAA], x, y, z, w), 
				grad4f(p[BAA], x-1, y, z, w)),
				lerpf(a,grad4f(p[ABA], x, y-1, z, w), 
				grad4f(p[BBA], x-1, y-1, z, w))),
	
				lerpf(b,lerpf(a,grad4f(p[AAB], x, y, z-1, w), 
				grad4f(p[BAB], x-1, y, z-1, w)),
				lerpf(a,grad4f(p[ABB], x, y-1, z-1, w),
				grad4f(p[BBB], x-1, y-1, z-1, w)))),
	
			lerpf(c,lerpf(b,lerpf(a,grad4f(p[AAA+1], x, y, z, w-1), 
				grad4f(p[BAA+1], x-1, y, z, w-1)),
				lerpf(a,grad4f(p[ABA+1], x, y-1, z, w-1), 
				grad4f(p[BBA+1], x-1, y-1, z, w-1))),
	
				lerpf(b,lerpf(a,grad4f(p[AAB+1], x, y, z-1, w-1), 
				grad4f(p[BAB+1], x-1, y  , z-1, w-1)),
				lerpf(a,grad4f(p[ABB+1], x	 , y-1, z-1, w-1),
				grad4f(p[BBB+1], x-1, y-1, z-1, w-1)))));
	}
	
	static public double noise4d(double x, double y, double z, double w) {
		int X = (int) Math.floor(x) & 255,		// FIND UNIT HYPERCUBE
		Y = (int) Math.floor(y) & 255,			// THAT CONTAINS POINT.
		Z = (int) Math.floor(z) & 255,
		W = (int) Math.floor(w) & 255;
		
		x -= Math.floor(x);				// FIND RELATIVE X,Y,Z,W
		y -= Math.floor(y);				// OF POINT IN CUBE.
		z -= Math.floor(z);
		w -= Math.floor(w);
		
		double a = faded(x),				// COMPUTE FADE CURVES
		b = faded(y),					// FOR EACH OF X,Y,Z,W.
		c = faded(z),
		d = faded(w);
		
		int A = p[X  ]+Y, AA = p[A]+Z, AB = p[A+1]+Z,	// HASH COORDINATES OF
		B = p[X+1]+Y, BA = p[B]+Z, BB = p[B+1]+Z,	// THE 16 CORNERS OF
		AAA = p[AA]+W, AAB = p[AA+1]+W,			// THE HYPERCUBE.
		ABA = p[AB]+W, ABB = p[AB+1]+W,
		BAA = p[BA]+W, BAB = p[BA+1]+W,
		BBA = p[BB]+W, BBB = p[BB+1]+W;
	
		return lerpd(d,					 // INTERPOLATE DOWN.
			lerpd(c,lerpd(b,lerpd(a,grad4d(p[AAA], x, y, z, w), 
				grad4d(p[BAA], x-1, y, z, w)),
				lerpd(a,grad4d(p[ABA], x, y-1, z, w), 
				grad4d(p[BBA], x-1, y-1, z, w))),
	
				lerpd(b,lerpd(a,grad4d(p[AAB], x, y, z-1, w), 
				grad4d(p[BAB], x-1, y, z-1, w)),
				lerpd(a,grad4d(p[ABB], x, y-1, z-1, w),
				grad4d(p[BBB], x-1, y-1, z-1, w)))),
	
			lerpd(c,lerpd(b,lerpd(a,grad4d(p[AAA+1], x, y, z, w-1), 
				grad4d(p[BAA+1], x-1, y, z, w-1)),
				lerpd(a,grad4d(p[ABA+1], x, y-1, z, w-1), 
				grad4d(p[BBA+1], x-1, y-1, z, w-1))),
	
				lerpd(b,lerpd(a,grad4d(p[AAB+1], x, y, z-1, w-1), 
				grad4d(p[BAB+1], x-1, y  , z-1, w-1)),
				lerpd(a,grad4d(p[ABB+1], x	 , y-1, z-1, w-1),
				grad4d(p[BBB+1], x-1, y-1, z-1, w-1)))));
	}
	
	private static float fadef(float t) {
		return t * t * t * (t * (t * 6 - 15) + 10);
	}
	
	private static double faded(double t) {
		return t * t * t * (t * (t * 6 - 15) + 10);
	}
	
	private static float lerpf(float t, float a, float b) {
		return a + t * (b - a);
	}
	
	private static double lerpd(double t, double a, double b) {
		return a + t * (b - a);
	}
	
	private static float grad3f(int hash, float x, float y, float z) {
		int h = hash & 15;			// CONVERT LO 4 BITS OF HASH CODE
		float u = h<8 ? x : y,			// INTO 12 GRADIENT DIRECTIONS.
		v = h<4 ? y : h==12||h==14 ? x : z;
		return ((h&1) == 0 ? u : -u) + ((h&2) == 0 ? v : -v);
	}
	
	private static double grad3d(int hash, double x, double y, double z) {
		int h = hash & 15;			// CONVERT LO 4 BITS OF HASH CODE
		double u = h<8 ? x : y,			// INTO 12 GRADIENT DIRECTIONS.
		v = h<4 ? y : h==12||h==14 ? x : z;
		return ((h&1) == 0 ? u : -u) + ((h&2) == 0 ? v : -v);
	}
	
	private static float grad4f(int hash, float x, float y, float z, float w) {
		int h = hash & 31; 	// CONVERT LO 5 BITS OF HASH TO 32 GRAD DIRECTIONS.
		float a=y,b=z,c=w;	// X,Y,Z
		switch (h >> 3) {			// OR, DEPENDING ON HIGH ORDER 2 BITS:
			case 1: a=w;b=x;c=y;break;	// W,X,Y
			case 2: a=z;b=w;c=x;break;	// Z,W,X
			case 3: a=y;b=z;c=w;break;	// Y,Z,W
		}
		return ((h&4)==0 ? -a:a) + ((h&2)==0 ? -b:b) + ((h&1)==0 ? -c:c);
	}
	
	private static double grad4d(int hash, double x, double y, double z, double w) {
		int h = hash & 31; 	// CONVERT LO 5 BITS OF HASH TO 32 GRAD DIRECTIONS.
		double a=y,b=z,c=w;	// X,Y,Z
		switch (h >> 3) {			// OR, DEPENDING ON HIGH ORDER 2 BITS:
			case 1: a=w;b=x;c=y;break;	// W,X,Y
			case 2: a=z;b=w;c=x;break;	// Z,W,X
			case 3: a=y;b=z;c=w;break;	// Y,Z,W
		}
		return ((h&4)==0 ? -a:a) + ((h&2)==0 ? -b:b) + ((h&1)==0 ? -c:c);
	}
}
