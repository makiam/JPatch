
// package vgp.tutor.firstGraph;

package javaview_test;

import java.applet.Applet;
import java.awt.*;
import java.awt.Color;

import jv.geom.PgElementSet;
import jv.geom.PgUtil;
import jv.object.PsConfig;
import jv.viewer.PvDisplay;

import jv.vecmath.PdVector;
import jv.vecmath.PiVector;

import java.util.*;

/**
 * Demo applet shows own data as a graph over the x-y plane.
 * Own data is given as an array of z-values which determine
 * the height of each vertex.
 * 
 * @author		Konrad Polthier
 * @version		29.12.01, 1.00 revised (kp)<br>
 *					29.12.01, 1.00 created (kp)
 */
public class surf_demo extends Applet {
	/**
	 * Applet support. Configure and initialize the viewer,
	 * load geometry and add display.
	 */
	public void init() {
		// JavaView's configuration class needs to know if running as applet or application.
		PsConfig.init(this, null);
		
		// Create viewer for viewing 3d geometries, and register applet.
		PvDisplay disp = new PvDisplay();

		// Create a simple geometry. PgElementSet is the base class
		// for surfaces. See jv.geom.* for more geometry classes.
		
		Triangulierung tri = new Triangulierung(8,10);
		
		
		PgElementSet geom1 = tri.makeGeom();
		Color col = new Color(0.5f, 0.5f, 0.5f, 1.0f);
		for (int i=0; i<geom1.getNumElements(); ++i)
			geom1.setElementColor(i, col);
		// geom.makeElementColorsFromXYZ();
		geom1.showElementColors(true);

		
		Random rand = new Random();
		
		for (int i=0; i< tri.getNumOfVertices(); ++i)
		{
			double x = tri.getX(i);
			double y = tri.getY(i);
			
			double z = .3+ 0.2*x + 0.3*y;
			// double z =  0.25 - 0.25*rand.nextDouble();
			
			tri.setZ(i, z);
		}
		
		PgElementSet geom2=tri.makeGeom();
		
		col = new Color(1.0f, 0.0f, 0.0f, 0.2f);
		for (int i=0; i<geom2.getNumElements(); ++i)
			geom2.setElementColor(i, col);
		// geom.makeElementColorsFromXYZ();
		geom2.showElementColors(true);
		
		
		for (int i=0; i< tri.getNumOfVertices(); ++i)
		{
			double x = tri.getX(i);
			double y = tri.getY(i);
			
			double z = .3 + 0.2*x + 0.3*y + .1 - .2*rand.nextDouble();
			// double z =  0.25 - 0.25*rand.nextDouble();
			
			tri.setZ(i, z);
		}
        PgElementSet geom3=tri.makeGeom();
		
		col = new Color(0.5f, 0.9f, 0.5f, 0.8f);
		for (int i=0; i<geom3.getNumElements(); ++i)
			geom3.setElementColor(i, col);
		// geom.makeElementColorsFromXYZ();
		geom3.showElementColors(true);
		
		
		// Register the geometry in the display, and make it active.
		disp.addGeometry(geom1);
		disp.addGeometry(geom2);
		disp.addGeometry(geom3);
		
		disp.showAxes(true);
		// The selected geometry receives pick events. Usually one needs
		// to select a geometry only if more geometries are registered
		// in a display.
		disp.selectGeometry(geom1);
		
		
		// Standard Java technique to add a component to an applet.
		setLayout(new BorderLayout());
		add(disp, BorderLayout.CENTER);
	}
}
