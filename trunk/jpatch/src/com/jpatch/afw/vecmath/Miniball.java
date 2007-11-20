package com.jpatch.afw.vecmath;

import javax.vecmath.Point3d;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Java Porting of the Miniball.h code of <B>Bernd Gaertner</B>.
 * Look at http://www.inf.ethz.ch/personal/gaertner/miniball.html<br>
 * and related work at
 * http://www.inf.ethz.ch/personal/gaertner/texts/own_work/esa99_final.pdf<br>
 * for reading about the algorithm and the implementation of it.<p>
 * <p>
 * If interested in Bounding Sphere algorithms read also published work of <B>Emo Welzl</B> "Smallest enclosing disks (balls<br>
 * and Ellipsoid)" and
 * the work of <B>Jack Ritter</B> on "Efficient Bounding Spheres"  at<br>
 * http://tog.acm.org/GraphicsGems/gems/BoundSphere.c?searchterm=calc<p>
 * <p><p>
 * For Licencing Info report to Bernd Gaertner's one reported below:<p>
 *
 * Copright (C) 1999-2006, Bernd Gaertner<br>
 *   $Revision: 1.3 $<br>
 *   $Date: 2006/11/16 08:01:52 $<br>
 *<br>
 *This program is free software; you can redistribute it and/or modify<br>
 *it under the terms of the GNU General Public License as published by<br>
 *the Free Software Foundation; either version 2 of the License, or<br>
 *(at your option) any later version.<br>
 *<br>
 *This program is distributed in the hope that it will be useful,<br>
 *but WITHOUT ANY WARRANTY; without even the implied warranty of<br>
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<br>
 *GNU General Public License for more details.<br>
 *<br>
 *You should have received a copy of the GNU General Public License<br>
 *along with this program; if not, write to the Free Software<br>
 *Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA,<br>
 *or download the License terms from prep.ai.mit.edu/pub/gnu/COPYING-2.0.<br>
 *<br>
 *Contact:<br>
 *--------<br>
 *Bernd Gaertner<br>
 *Institute of Theoretical Computer Science<br>
 *ETH Zuerich<br>
 *CAB G32.2<br>
 *CH-8092 Zuerich, Switzerland<br>
 *http://www.inf.ethz.ch/personal/gaertner<br>

 *
 * <p>
 * <p>
 * <B>Example of Usage:</B><p>
 * import javax.vecmath.Point3d;<br>
 * ... <br>
 * Point3d v;<br>
 * Miniball mb=new Miniball();<br>
 * String[] r=new String[3];<br>
 * <br>
 * mb.clear(); //Clears Miniball ArrayList of Point3d in Miniball<br>
 * <br>
 *  while (.... read vertexes coordinate from some place ...){<br>
         v=new Point3d(Double.parseDouble(r[0]),Double.parseDouble(r[1]),Double.parseDouble(r[2]));<br>
         mb.check_in(v);<br>
         }<br>
   <br>      
   // calculate Bounding Sphere (Miniball) according to Bernd Gaertner algorithm<br>
   // should be called always when changed List of vertexes or when one or more vertexes<br>
   // change their coordinates<br> 
   mb.build();   
   <br>
   Point3d mbcenter=mb.center();  //get center of Bounding Sphere<br>
   double mbradius=mb.radius();     // get radius of Bounding Sphere<br>
   <br>
   // Don't forget to call mb.clear() to clear ArrayList of vertexes to avoid memory problems<br>
   mb.clear();<br>
   ....<br>
   ....<br>
 *
 * @author Paolo Perissinotto for Jpatch Project by <B>Sascha Ledinsky</B>
 *
 * @version 1.0
 * {@literal Date: 2007/11/18 21:57}
 *
 *
 */

public class Miniball {
//	typedef typename std::list<Point<d> >::iterator         It;
//	typedef typename std::list<Point<d> >::const_iterator   Cit;

//	private:
	// data members
//	std::list<Point<d> > L;            // internal point set
	ArrayList<Point3d> pointList;
	// Miniball_b<d>        B;            // the current ball
//	Miniball_b        B=new Miniball_b();
	int support_end=0;
	static final int DIMENSIONS=3;
//	Arrays it;


	// It                   support_end;  // past-the-end iterator of support set

	// private methods
//	void        mtf_mb (It k);
//	void        pivot_mb (It k);
//	void        move_to_front (It j);
//	double      max_excess (It t, It i, It& pivot) const;

//	public:
	// creates an empty ball
//	Miniball() {}

	// copies p to the internal point set
//	void        check_in (const Point<d>& p);

	// builds the smallest enclosing ball of the internal point set
//	void        build ();

	// returns center of the ball (undefined if ball is empty)
//	Point<d>       center() const;

	// returns squared_radius of the ball (-1 if ball is empty)
//	double      squared_radius () const;

	// returns size of internal point set
//	int         nr_points () const;

	// returns begin- and past-the-end iterators for internal point set
//	Cit         points_begin () const;
//	Cit         points_end () const;

	// returns size of support point set; this set has the following properties:
	// - there are at most d+1 support points,
	// - all support points are on the boundary of the computed ball, and
	// - the smallest enclosing ball of the support point set equals the
	//   smallest enclosing ball of the internal point set
//	int         nr_support_points () const;

	// returns begin- and past-the-end iterators for internal point set
//	Cit         support_points_begin () const;
//	Cit         support_points_end () const;

	// assesses the quality of the computed ball. The return value is the
	// maximum squared distance of any support point or point outside the
	// ball to the boundary of the ball, divided by the squared radius of
	// the ball. If everything went fine, this will be less than e-15 and
	// says that the computed ball approximately contains all the internal
	// points and has all the support points on the boundary.
	//
	// The slack parameter that is set by the method says something about
	// whether the computed ball is really the *smallest* enclosing ball
	// of the support points; if everything went fine, this value will be 0;
	// a positive value may indicate that the ball is not smallest possible,
	// with the deviation from optimality growing with the slack
//	double      accuracy (double& slack) const;

	// returns true if the accuracy is below the given tolerance and the
	// slack is 0
//	bool        is_valid (double tolerance = 1e-15) const;


//	Miniball
//	--------

//	void Miniball<d>::check_in (const Point<d>& p)
//	{
//	L.push_back(p);
//	}


//	class Miniball_b {
//	Miniball_b
//	----------
	private int                 m;   // size and number of support points
	// int d=3;
	private double[]              q0=new double[DIMENSIONS];

	// double              z[d+1];
	private double[]   z=new double[DIMENSIONS + 1];
	//double              f[d+1];
	private double[]   f=new double[DIMENSIONS + 1];
	//double              v[d+1][d];
	private double[][] v=new double[DIMENSIONS + 1][DIMENSIONS];
	//double              a[d+1][d];
	private double[][] a=new double[DIMENSIONS + 1][DIMENSIONS];

	//double              c[d+1][d];
	private double[][] c=new double[DIMENSIONS + 1][DIMENSIONS];
	//double              sqr_r[d+1];
	private double[] sqr_r=new double[DIMENSIONS + 1];

	//double*             current_c;      // refers to some c[j]
	private double[]      current_c=new double[DIMENSIONS];
	//double              current_sqr_r;
	private double              current_sqr_r;

	// public:
	// Miniball_b() {reset();}

	// access
	//const double*       center() const;
	//double              squared_radius() const;
	//int                 size() const;
	//int                 support_size() const;
	//double              excess (const Point<d>& p) const;

	// modification
	//void                reset(); // generates empty sphere with m=s=0

	// bool                push (const Point<d>& p);
	//void                pop ();

	// checking
	//double              slack() const;

//	Miniball_b
//	----------

	private double[] getCenter()
	{
		return(current_c);
	}


	private double squared_radius()
	{
		return current_sqr_r;
	}
	

	private double excess (Point3d sp)
	{
		double e = -current_sqr_r;
		double[] p=new double[3];
		p[0]=sp.x;
		p[1]=sp.y;
		p[2]=sp.z;
		for (int k=0; k<3; ++k)
			e += squared(p[k]-current_c[k]);
		return e;
	}



	private void reset ()
	{
		m = 0;
//		s = 0;
		// we misuse c[0] for the center of the empty sphere
		for (int j=0; j<DIMENSIONS; j++)
			c[0][j]=0;
		current_c = c[0];
		current_sqr_r = -1;
	}


	private void pop ()	{
		//System.out.println("Miniball_b:pop");
		--m;
	}


	private double[] getarr(Point3d sp, double[] p) {
		p[0]=sp.x;
		p[1]=sp.y;
		p[2]=sp.z;
		return(p);
	}

	private boolean push( Point3d sp) {
		//System.out.println("Miniball_b:push");
		int i, j;
		double eps = 1e-32;
		double[] p=getarr(sp, new double[3]);
		if (m==0) {
			for (i=0; i<DIMENSIONS; ++i)
				q0[i] = p[i];
			for (i=0; i<DIMENSIONS; ++i)
				c[0][i] = q0[i];
			sqr_r[0] = 0;
		} else {
			// set v_m to Q_m
			for (i=0; i<DIMENSIONS; ++i)
				v[m][i] = p[i]-q0[i];

			// compute the a_{m,i}, i< m
			for (i=1; i<m; ++i) {
				a[m][i] = 0;
				for (j=0; j<DIMENSIONS; ++j)
					a[m][i] += v[i][j] * v[m][j];
				a[m][i]*=(2/z[i]);
			}

			// update v_m to Q_m-\bar{Q}_m
			for (i=1; i<m; ++i) {
				for (j=0; j<DIMENSIONS; ++j)
					v[m][j] -= a[m][i]*v[i][j];
			}

			// compute z_m
			z[m]=0;
			for (j=0; j<DIMENSIONS; ++j)
				z[m] += squared(v[m][j]);
			z[m]*=2;

			// reject push if z_m too small
			if (z[m]<eps*current_sqr_r) {
				return false;
			}

			// update c, sqr_r
			double e = -sqr_r[m-1];
			for (i=0; i<DIMENSIONS; ++i)
				e += squared(p[i]-c[m-1][i]);
			f[m]=e/z[m];

			for (i=0; i<DIMENSIONS; ++i)
				c[m][i] = c[m-1][i]+f[m]*v[m][i];
			sqr_r[m] = sqr_r[m-1] + e*f[m]/2;
		}
		current_c = c[m];
		current_sqr_r = sqr_r[m];
		m++;
		return true;
	}

	



	/**
	 * Recalculate Miniball parameter Center and Radius
	 *
	 */
	public void build(Collection<Point3d> points) {
		pointList = new ArrayList<Point3d>(points);
		reset();
		support_end = 0;
		pivot_mb(pointList.size());
	}


	private void mtf_mb (int i)	{
		support_end = 0;
		if (m == DIMENSIONS + 1) {
			return;
		}
		for (int k = 0; k < i; k++) {
			Point3d sp=(Point3d) pointList.get(k);
			if (excess(sp) > 0) {
				if (push(sp)) {
					mtf_mb (k);
					pop();
					move_to_front(k);
				}
			}
		}
	}

	private void move_to_front(int j) {

		if (support_end <= j) {
			support_end++;
		}
		//   L.splice (L.begin(), L, j);
		Point3d sp=(Point3d) pointList.get(j);
		pointList.remove(j);
		pointList.add(0,sp);
	}


	private void pivot_mb (int i) {
		int t=1;
		mtf_mb(t);
		double max_e=0.0, old_sqr_r = -1;
		Pivot pivot=new Pivot();
		do {
			max_e = max_excess (t, i, pivot);
			if (max_e > 0) {
				t = support_end;
				if (t==pivot.getVal()) ++t;
				old_sqr_r = squared_radius();
				Point3d sp=(Point3d) pointList.get(pivot.getVal());
				push(sp);
				mtf_mb (support_end);
				pop();
				move_to_front (pivot.getVal());
			} 
		} while((max_e > 0) && (squared_radius() > old_sqr_r));
	} 

	private double max_excess (int t, int i, Pivot pivot) {
		double[] c = getCenter();
		double sqr_r = squared_radius();
		double e, max_e = 0;
		double[] p = new double[DIMENSIONS];
		for (int k=t; k!=i; ++k) {
			Point3d sp=(Point3d) pointList.get(k);

			getarr(sp, p);
			e = -sqr_r;
			for (int j=0; j<DIMENSIONS; ++j)
				e += squared(p[j]-c[j]);
			if (e > max_e) {
				max_e = e;
				pivot.setVal(k);
			}
		}
		return max_e;
	}

	/**
	 * Return the center of the Miniball
	 * @return The center (Point3d)
	 */      

	public Point3d center () {
		return(new Point3d(getCenter()));
	}
	/**
	 * Return the sqaured Radius of the miniball
	 * @return The square radius
	 */

	/**
	 * Return the Radius of the miniball
	 * @return The radius
	 */

	public double radius() {
		return (1 + 0.00001) * Math.sqrt(squared_radius());
	}
	
	private static double squared(double r) {
		return r*r;
	}

	private static class Pivot {
		int val;

		void setVal(int i){
			val=i;
		}

		int getVal(){
			return(val);
		}
	}
	
	public static void main(String[] args) {
		Collection<Point3d> points = new ArrayList<Point3d>();
		for (int i = 0; i < 1000000; i++) {
			points.add(new Point3d(Math.random(), Math.random(), Math.random()));
		}
		Miniball mb = new Miniball();
		mb.build(points);
		System.out.println("center = " + mb.center());
		System.out.println("radius = " + mb.radius());
		long t = System.currentTimeMillis();
		mb.build(points);
		System.out.println("center = " + mb.center());
		System.out.println("radius = " + mb.radius());
		System.out.println((System.currentTimeMillis() - t) + "ms");
	}
}
