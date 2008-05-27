
// package firstApplet;
package javaview_test;

import java.applet.Applet;
import java.awt.BorderLayout;

import jv.geom.PgElementSet;
import jv.object.PsConfig;
import jv.viewer.PvDisplay;

/**
 * Demo applet shows how to include a JavaView display in own code.
 * 
 * @see			jv.viewer.PvDisplay
 * @author		Konrad Polthier
 * @version		10.05.03, 2.00 revised (kp) Renamed to MyApplet from main.<br>
 *					29.12.01, 1.20 revised (kp) Applet further simplified by removing PvViewer.<br>
 *					25.12.99, 1.10 revised (kp) Geometry is added directly to display rather than to default project.<br>
 *					04.08.99, 1.00 created (kp)
 */
public class MyApplet extends Applet {
	/**
	 * Applet support. Configure and initialize the viewer,
	 * load geometry and add display.
	 */
	public void init() {
		// JavaView's configuration class needs to know if running as applet or application.
		PsConfig.init(this, null);
		
		// Create a display for viewing 3d geometries. Note, more advanced
		// applets obtain display(s) from an instance of jv.viewer.PvViewer.
		PvDisplay disp = new PvDisplay();

		// Create a simple geometry. PgElementSet is the base class
		// for surfaces. See jv.geom.* for more geometry classes.
		PgElementSet geom = new PgElementSet(3);
		geom.setName("Torus");
		// Compute coordinates and mesh of a geometry. Other tutorials
		// show more details how to create one geometries.
		geom.computeTorus(10, 10, 2., 1.);

		// Register the geometry in the display.
		disp.addGeometry(geom);
		// The selected geometry receives pick events. Usually one needs
		// to select a geometry only if more geometries are registered
		// in a display.
		disp.selectGeometry(geom);

		// Standard Java technique to add a component to an applet.
		// BorderLayout ensures that the display fills the whole applet.
		setLayout(new BorderLayout());
		add(disp, BorderLayout.CENTER);
	}
}
