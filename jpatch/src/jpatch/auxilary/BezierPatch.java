package jpatch.auxilary;
import javax.vecmath.*;

/**
 *  Description of the Class
 *
 * @author     aledinsk
 * @created    18. M�rz 2003
 */
public final class BezierPatch {
	private static final float b0(float t) { return (1-t)*(1-t)*(1-t); }
	private static final float b1(float t) { return 3*t*(1-t)*(1-t); }
	private static final float b2(float t) { return 3*t*t*(1-t); }
	private static final float b3(float t) { return t*t*t; }
	
	private static final float db0(float t) { return -3*(1-t)*(1-t); }
	private static final float db1(float t) { return -6*t*(1-t) + 3*(1-t)*(1-t); }
	private static final float db2(float t) { return -3*t*t + 6*t*(1-t); }
	private static final float db3(float t) { return 3*t*t; }
	
	private static float cp0x;
	private static float cp1x;
	private static float cp2x;
	private static float cp3x;
	private static float cp4x;
	private static float cp5x;
	private static float cp6x;
	private static float cp7x;
	private static float cp8x;
	private static float cp9x;
	private static float cp10x;
	private static float cp11x;
	private static float cp12x;
	private static float cp13x;
	private static float cp14x;
	private static float cp15x;
	private static float cp0y;
	private static float cp1y;
	private static float cp2y;
	private static float cp3y;
	private static float cp4y;
	private static float cp5y;
	private static float cp6y;
	private static float cp7y;
	private static float cp8y;
	private static float cp9y;
	private static float cp10y;
	private static float cp11y;
	private static float cp12y;
	private static float cp13y;
	private static float cp14y;
	private static float cp15y;
	private static float cp0z;
	private static float cp1z;
	private static float cp2z;
	private static float cp3z;
	private static float cp4z;
	private static float cp5z;
	private static float cp6z;
	private static float cp7z;
	private static float cp8z;
	private static float cp9z;
	private static float cp10z;
	private static float cp11z;
	private static float cp12z;
	private static float cp13z;
	private static float cp14z;
	private static float cp15z;
	private static int is;
	private static int it;
	private static int rows;
	private static int cols;
	
	private static float s1;
	private static float s1_2;
	private static float s_2;
	private static float B0s;
	private static float B1s;
	private static float B2s;
	private static float B3s;
	private static float B0ds;
	private static float B1ds;
	private static float B2ds;
	private static float B3ds;
	private static float t1;
	private static float t1_2;
	private static float t_2;
	private static float B0t;
	private static float B1t;
	private static float B2t;
	private static float B3t;
	private static float B0dt;
	private static float B1dt;
	private static float B2dt;
	private static float B3dt;
	
	private static float px;
	private static float py;
	private static float pz;
	
	private static float s;
	private static float t;
	
	private static Vector3f v3ds = new Vector3f();
	private static Vector3f v3dt = new Vector3f();
	
	private static float x0;
	private static float x1;
	private static float x2;
	private static float x3;
	private static float y0;
	private static float y1;
	private static float y2;
	private static float y3;
	private static float z0;
	private static float z1;
	private static float z2;
	private static float z3;
	
	private static final void prepare(Point3f[] cp) {
		cp0x = cp[0].x;
		cp1x = cp[1].x;
		cp2x = cp[2].x;
		cp3x = cp[3].x;
		cp4x = cp[4].x;
		cp5x = cp[5].x;
		cp6x = cp[6].x;
		cp7x = cp[7].x;
		cp8x = cp[8].x;
		cp9x = cp[9].x;
		cp10x = cp[10].x;
		cp11x = cp[11].x;
		cp12x = cp[12].x;
		cp13x = cp[13].x;
		cp14x = cp[14].x;
		cp15x = cp[15].x;
		cp0y = cp[0].y;
		cp1y = cp[1].y;
		cp2y = cp[2].y;
		cp3y = cp[3].y;
		cp4y = cp[4].y;
		cp5y = cp[5].y;
		cp6y = cp[6].y;
		cp7y = cp[7].y;
		cp8y = cp[8].y;
		cp9y = cp[9].y;
		cp10y = cp[10].y;
		cp11y = cp[11].y;
		cp12y = cp[12].y;
		cp13y = cp[13].y;
		cp14y = cp[14].y;
		cp15y = cp[15].y;
		cp0z = cp[0].z;
		cp1z = cp[1].z;
		cp2z = cp[2].z;
		cp3z = cp[3].z;
		cp4z = cp[4].z;
		cp5z = cp[5].z;
		cp6z = cp[6].z;
		cp7z = cp[7].z;
		cp8z = cp[8].z;
		cp9z = cp[9].z;
		cp10z = cp[10].z;
		cp11z = cp[11].z;
		cp12z = cp[12].z;
		cp13z = cp[13].z;
		cp14z = cp[14].z;
		cp15z = cp[15].z;
	}
	
	public static final int evalPatch(Point3f[] cp,Point3f[] positions, Vector3f[] normals, int subdiv, int i) {
		prepare(cp);
		cols = subdiv;
		rows = subdiv;
		for (is = 0; is <= cols; is++) {
			s = (float)is/cols;
			s1 = 1 - s;
			s1_2 = s1 * s1;
			s_2 = s * s;
			B0s = s1_2 * s1;
			B1s = 3 * s * s1_2;
			B2s = 3 * s_2 * s1;
			B3s = s_2 * s;
			B0ds = -3 * s1_2;
			B1ds = -6 * s * s1 + 3 * s1_2;
			B2ds = -3 * s_2 + 6 * s * s1;
			B3ds = 3 * s_2;
			for (it = 0; it <= rows; it++) {
				t = (float)it/cols;
				t1 = 1 - t;
				t1_2 = t1 * t1;
				t_2 = t * t;
				B0t = t1_2 * t1;
				B1t = 3 * t * t1_2;
				B2t = 3 * t_2 * t1;
				B3t = t_2 * t;
				B0dt = -3 * t1_2;
				B1dt = -6 * t * t1 + 3 * t1_2;
				B2dt = -3 * t_2 + 6 * t * t1;
				B3dt = 3 * t_2;
				x0 = cp0x * B0t + cp1x * B1t + cp2x * B2t + cp3x * B3t;
				x1 = cp4x * B0t + cp5x * B1t + cp6x * B2t + cp7x * B3t;
				x2 = cp8x * B0t + cp9x * B1t + cp10x * B2t + cp11x * B3t;
				x3 = cp12x * B0t + cp13x * B1t + cp14x * B2t + cp15x * B3t;
				y0 = cp0y * B0t + cp1y * B1t + cp2y * B2t + cp3y * B3t;
				y1 = cp4y * B0t + cp5y * B1t + cp6y * B2t + cp7y * B3t;
				y2 = cp8y * B0t + cp9y * B1t + cp10y * B2t + cp11y * B3t;
				y3 = cp12y * B0t + cp13y * B1t + cp14y * B2t + cp15y * B3t;
				z0 = cp0z * B0t + cp1z * B1t + cp2z * B2t + cp3z * B3t;
				z1 = cp4z * B0t + cp5z * B1t + cp6z * B2t + cp7z * B3t;
				z2 = cp8z * B0t + cp9z * B1t + cp10z * B2t + cp11z * B3t;
				z3 = cp12z * B0t + cp13z * B1t + cp14z * B2t + cp15z * B3t;
				px = B0s * x0 + B1s * x1 + B2s * x2 + B3s * x3;
				py = B0s * y0 + B1s * y1 + B2s * y2 + B3s * y3;
				pz = B0s * z0 + B1s * z1 + B2s * z2 + B3s * z3;
				positions[i].set(px,py,pz);
				px = B0ds * x0 + B1ds * x1 + B2ds * x2 + B3ds * x3;
				py = B0ds * y0 + B1ds * y1 + B2ds * y2 + B3ds * y3;
				pz = B0ds * z0 + B1ds * z1 + B2ds * z2 + B3ds * z3;
				v3ds.set(px,py,pz);
				px = 	B0s * (cp0x * B0dt + cp1x * B1dt + cp2x * B2dt + cp3x * B3dt) +
					B1s * (cp4x * B0dt + cp5x * B1dt + cp6x * B2dt + cp7x * B3dt) +
					B2s * (cp8x * B0dt + cp9x * B1dt + cp10x * B2dt + cp11x * B3dt) +
					B3s * (cp12x * B0dt + cp13x * B1dt + cp14x * B2dt + cp15x * B3dt);
				py = 	B0s * (cp0y * B0dt + cp1y * B1dt + cp2y * B2dt + cp3y * B3dt) +
					B1s * (cp4y * B0dt + cp5y * B1dt + cp6y * B2dt + cp7y * B3dt) +
					B2s * (cp8y * B0dt + cp9y * B1dt + cp10y * B2dt + cp11y * B3dt) +
					B3s * (cp12y * B0dt + cp13y * B1dt + cp14y * B2dt + cp15y * B3dt);
				pz = 	B0s * (cp0z * B0dt + cp1z * B1dt + cp2z * B2dt + cp3z * B3dt) +
					B1s * (cp4z * B0dt + cp5z * B1dt + cp6z * B2dt + cp7z * B3dt) +
					B2s * (cp8z * B0dt + cp9z * B1dt + cp10z * B2dt + cp11z * B3dt) +
					B3s * (cp12z * B0dt + cp13z * B1dt + cp14z * B2dt + cp15z * B3dt);
				v3dt.set(px,py,pz);
				normals[i].cross(v3dt,v3ds);
				normals[i++].normalize();
			}
		}
		return i;
	}	
		
		

	public static final Point3f evalPoint(float s, float t, Point3f[] cp) {
		Point3f p = new Point3f();
		p.x	= b0(s)*(cp[0].x*b0(t) + cp[1].x*b1(t) + cp[2].x*b2(t) + cp[3].x*b3(t))
			+ b1(s)*(cp[4].x*b0(t) + cp[5].x*b1(t) + cp[6].x*b2(t) + cp[7].x*b3(t))
			+ b2(s)*(cp[8].x*b0(t) + cp[9].x*b1(t) + cp[10].x*b2(t) + cp[11].x*b3(t))
			+ b3(s)*(cp[12].x*b0(t) + cp[13].x*b1(t) + cp[14].x*b2(t) + cp[15].x*b3(t));
		p.y 	= b0(s)*(cp[0].y*b0(t) + cp[1].y*b1(t) + cp[2].y*b2(t) + cp[3].y*b3(t))
			+ b1(s)*(cp[4].y*b0(t) + cp[5].y*b1(t) + cp[6].y*b2(t) + cp[7].y*b3(t))
			+ b2(s)*(cp[8].y*b0(t) + cp[9].y*b1(t) + cp[10].y*b2(t) + cp[11].y*b3(t))
			+ b3(s)*(cp[12].y*b0(t) + cp[13].y*b1(t) + cp[14].y*b2(t) + cp[15].y*b3(t));
		p.z 	= b0(s)*(cp[0].z*b0(t) + cp[1].z*b1(t) + cp[2].z*b2(t) + cp[3].z*b3(t))
			+ b1(s)*(cp[4].z*b0(t) + cp[5].z*b1(t) + cp[6].z*b2(t) + cp[7].z*b3(t))
			+ b2(s)*(cp[8].z*b0(t) + cp[9].z*b1(t) + cp[10].z*b2(t) + cp[11].z*b3(t))
			+ b3(s)*(cp[12].z*b0(t) + cp[13].z*b1(t) + cp[14].z*b2(t) + cp[15].z*b3(t));	 
		return p;
	}


	public static final Vector3f evalNormal(float s, float t, Point3f[] cp) {
		
		
		
		Vector3f dt = new Vector3f();
		dt.x = b0(s)*(cp[0].x*db0(t) + cp[1].x*db1(t) + cp[2].x*db2(t) + cp[3].x*db3(t))
			 + b1(s)*(cp[4].x*db0(t) + cp[5].x*db1(t) + cp[6].x*db2(t) + cp[7].x*db3(t))
			 + b2(s)*(cp[8].x*db0(t) + cp[9].x*db1(t) + cp[10].x*db2(t) + cp[11].x*db3(t))
			 + b3(s)*(cp[12].x*db0(t) + cp[13].x*db1(t) + cp[14].x*db2(t) + cp[15].x*db3(t));
		dt.y = b0(s)*(cp[0].y*db0(t) + cp[1].y*db1(t) + cp[2].y*db2(t) + cp[3].y*db3(t))
			 + b1(s)*(cp[4].y*db0(t) + cp[5].y*db1(t) + cp[6].y*db2(t) + cp[7].y*db3(t))
			 + b2(s)*(cp[8].y*db0(t) + cp[9].y*db1(t) + cp[10].y*db2(t) + cp[11].y*db3(t))
			 + b3(s)*(cp[12].y*db0(t) + cp[13].y*db1(t) + cp[14].y*db2(t) + cp[15].y*db3(t));
		dt.z = b0(s)*(cp[0].z*db0(t) + cp[1].z*db1(t) + cp[2].z*db2(t) + cp[3].z*db3(t))
			 + b1(s)*(cp[4].z*db0(t) + cp[5].z*db1(t) + cp[6].z*db2(t) + cp[7].z*db3(t))
			 + b2(s)*(cp[8].z*db0(t) + cp[9].z*db1(t) + cp[10].z*db2(t) + cp[11].z*db3(t))
			 + b3(s)*(cp[12].z*db0(t) + cp[13].z*db1(t) + cp[14].z*db2(t) + cp[15].z*db3(t));
			 
		Vector3f ds = new Vector3f();
		ds.x = db0(s)*(cp[0].x*b0(t) + cp[1].x*b1(t) + cp[2].x*b2(t) + cp[3].x*b3(t))
			 + db1(s)*(cp[4].x*b0(t) + cp[5].x*b1(t) + cp[6].x*b2(t) + cp[7].x*b3(t))
			 + db2(s)*(cp[8].x*b0(t) + cp[9].x*b1(t) + cp[10].x*b2(t) + cp[11].x*b3(t))
			 + db3(s)*(cp[12].x*b0(t) + cp[13].x*b1(t) + cp[14].x*b2(t) + cp[15].x*b3(t));
		ds.y = db0(s)*(cp[0].y*b0(t) + cp[1].y*b1(t) + cp[2].y*b2(t) + cp[3].y*b3(t))
			 + db1(s)*(cp[4].y*b0(t) + cp[5].y*b1(t) + cp[6].y*b2(t) + cp[7].y*b3(t))
			 + db2(s)*(cp[8].y*b0(t) + cp[9].y*b1(t) + cp[10].y*b2(t) + cp[11].y*b3(t))
			 + db3(s)*(cp[12].y*b0(t) + cp[13].y*b1(t) + cp[14].y*b2(t) + cp[15].y*b3(t));
		ds.z = db0(s)*(cp[0].z*b0(t) + cp[1].z*b1(t) + cp[2].z*b2(t) + cp[3].z*b3(t))
			 + db1(s)*(cp[4].z*b0(t) + cp[5].z*b1(t) + cp[6].z*b2(t) + cp[7].z*b3(t))
			 + db2(s)*(cp[8].z*b0(t) + cp[9].z*b1(t) + cp[10].z*b2(t) + cp[11].z*b3(t))
			 + db3(s)*(cp[12].z*b0(t) + cp[13].z*b1(t) + cp[14].z*b2(t) + cp[15].z*b3(t));	 
		
		Vector3f normal = new Vector3f();
		//normal.set(ds);
		normal.cross(dt,ds);
		normal.normalize();
		return normal;
	}

	public static final void computeInnerControlPoints(Point3f[] ap3) {
		
		ap3[5] = (Point3f)Functions.parallelogram(ap3[0],ap3[1],ap3[4]);
		ap3[6] = (Point3f)Functions.parallelogram(ap3[3],ap3[2],ap3[7]);
		ap3[9] = (Point3f)Functions.parallelogram(ap3[12],ap3[8],ap3[13]);
		ap3[10] = (Point3f)Functions.parallelogram(ap3[15],ap3[11],ap3[14]);
		
		
		
		//Vector3f v3_1 = Functions.vector(ap3[0],ap3[1]);
		//Vector3f v3_2 = Functions.vector(ap3[3],ap3[2]);
		//Vector3f v3_7 = Functions.vector(ap3[3],ap3[7]);
		//Vector3f v3_11 = Functions.vector(ap3[15],ap3[11]);
		//Vector3f v3_14 = Functions.vector(ap3[15],ap3[14]);
		//Vector3f v3_13 = Functions.vector(ap3[12],ap3[13]);
		//Vector3f v3_8 = Functions.vector(ap3[12],ap3[8]);
		//Vector3f v3_4 = Functions.vector(ap3[0],ap3[4]);
                //
		//Vector3f v3_1_ = Functions.vector(v3_4,v3_7,1f/3f);
		//Vector3f v3_2_ = Functions.vector(v3_4,v3_7,2f/3f);
		//Vector3f v3_7_ = Functions.vector(v3_2,v3_14,1f/3f);
       		//Vector3f v3_11_ = Functions.vector(v3_2,v3_14,2f/3f);
		//Vector3f v3_14_ = Functions.vector(v3_11,v3_8,1f/3f);
		//Vector3f v3_13_ = Functions.vector(v3_11,v3_8,2f/3f);
		//Vector3f v3_8_ = Functions.vector(v3_13,v3_1,1f/3f);
		//Vector3f v3_4_ = Functions.vector(v3_13,v3_1,2f/3f);
                //
		//v3_1_.scale(0.5f);
		//v3_2_.scale(0.5f);
		//v3_7_.scale(0.5f);
		//v3_11_.scale(0.5f);
		//v3_14_.scale(0.5f);
		//v3_13_.scale(0.5f);
		//v3_8_.scale(0.5f);
		//v3_4_.scale(0.5f);
		//Point3f p3_1_ = new Point3f(ap3[1]);
		//Point3f p3_2_ = new Point3f(ap3[2]);
		//Point3f p3_7_ = new Point3f(ap3[7]);
		//Point3f p3_11_ = new Point3f(ap3[11]);
		//Point3f p3_14_ = new Point3f(ap3[14]);
		//Point3f p3_13_ = new Point3f(ap3[13]);
		//Point3f p3_8_ = new Point3f(ap3[8]);
		//Point3f p3_4_ = new Point3f(ap3[4]);
                //
		//p3_1_.add(v3_1_);
		//p3_2_.add(v3_2_);
		//p3_7_.add(v3_7_);
		//p3_11_.add(v3_11_);
		//p3_14_.add(v3_14_);
		//p3_13_.add(v3_13_);
		//p3_8_.add(v3_8_);
		//p3_4_.add(v3_4_);

		//Plane plane0 = new Plane(ap3[0],ap3[1],ap3[4]);
		//Plane plane3 = new Plane(ap3[3],ap3[7],ap3[2]);
		//Plane plane15 = new Plane(ap3[15],ap3[14],ap3[11]);
		//Plane plane12 = new Plane(ap3[12],ap3[8],ap3[13]);
                //
		//Point3f p3_1__ = plane0.projectedPoint(p3_1_);
		//Point3f p3_2__ = plane3.projectedPoint(p3_2_);
		//Point3f p3_7__ = plane3.projectedPoint(p3_7_);
		//Point3f p3_11__ = plane15.projectedPoint(p3_11_);
		//Point3f p3_14__ = plane15.projectedPoint(p3_14_);
		//Point3f p3_13__ = plane12.projectedPoint(p3_13_);
		//Point3f p3_8__ = plane12.projectedPoint(p3_8_);
		//Point3f p3_4__ = plane0.projectedPoint(p3_4_);
                //
		//Line2f line1 = new Line2f(plane0.getP2(ap3[1]),plane0.getP2(p3_1__));
		//Line2f line2 = new Line2f(plane3.getP2(ap3[2]),plane3.getP2(p3_2__));
		//Line2f line7 = new Line2f(plane3.getP2(ap3[7]),plane3.getP2(p3_7__));
		//Line2f line11 = new Line2f(plane15.getP2(ap3[11]),plane15.getP2(p3_11__));
		//Line2f line14 = new Line2f(plane15.getP2(ap3[14]),plane15.getP2(p3_14__));
		//Line2f line13 = new Line2f(plane12.getP2(ap3[13]),plane12.getP2(p3_13__));
		//Line2f line8 = new Line2f(plane12.getP2(ap3[8]),plane12.getP2(p3_8__));
		//Line2f line4 = new Line2f(plane0.getP2(ap3[4]),plane0.getP2(p3_4__));
                //
		//Point2f p2_5 = line1.intersection(line4);
		//Point2f p2_6 = line2.intersection(line7);
		//Point2f p2_9 = line8.intersection(line13);
		//Point2f p2_10 = line11.intersection(line14);
                //
		//ap3[5] = plane0.getP3(p2_5);
		//ap3[6] = plane3.getP3(p2_6);
		//ap3[9] = plane12.getP3(p2_9);
		//ap3[10] = plane15.getP3(p2_10);
		//ap3[5] = new Point3f(ap3[0]);
		//ap3[6] = new Point3f(ap3[3]);
		//ap3[9] = new Point3f(ap3[12]);
		//ap3[10] = new Point3f(ap3[15]);
		//
		//ap3[5].add(v3_1_);
		//ap3[5].add(v3_4_);
		//ap3[6].add(v3_2_);
		//ap3[6].add(v3_7_);
		//ap3[9].add(v3_8_);
		//ap3[9].add(v3_13_);
		//ap3[10].add(v3_11_);
		//ap3[10].add(v3_14_);
		/*
		System.out.println(ap3[5] + " " + ap3[6] + " " + ap3[9] + " " + ap3[10]);
		System.out.println(ap3[9].x);
		System.out.println(" == NaN " + (ap3[9].x == Float.NaN));
		System.out.println(" == 0 " + (ap3[9].x == 0));
		System.out.println(" > 0 " + (ap3[9].x > 0));
		System.out.println(" < 0 " + (ap3[9].x < 0));
		*/
		
		/*
		if (!(ap3[5].x > 0) && !(ap3[5].x < 0) && !(ap3[5].x == 0)) {
			ap3[5] = (Point3f)Functions.parallelogram(ap3[0],ap3[1],ap3[4]);
		}
		if (!(ap3[6].x > 0) && !(ap3[6].x < 0) && !(ap3[6].x == 0)) {
			ap3[6] = (Point3f)Functions.parallelogram(ap3[3],ap3[2],ap3[7]);
		}
		if (!(ap3[9].x > 0) && !(ap3[9].x < 0) && !(ap3[9].x == 0)) {
			ap3[9] = (Point3f)Functions.parallelogram(ap3[12],ap3[8],ap3[13]);
		}
		if (!(ap3[10].x > 0) && !(ap3[10].x < 0) && !(ap3[10].x == 0)) {
			ap3[10] = (Point3f)Functions.parallelogram(ap3[15],ap3[11],ap3[14]);
		}
		*/
	}
}
