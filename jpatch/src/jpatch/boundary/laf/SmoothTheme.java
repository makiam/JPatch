package jpatch.boundary.laf;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.OceanTheme;

import sun.swing.PrintColorUIResource;

import java.awt.*;

public class SmoothTheme extends OceanTheme {
	private static final ColorUIResource PRIMARY1 = new ColorUIResource(Color.BLACK);
	private static final ColorUIResource PRIMARY2 = new ColorUIResource(0.8f, 0.6f, 0.4f);
	private static final ColorUIResource PRIMARY3 = new ColorUIResource(0.8f, 0.6f, 0.4f);

	protected ColorUIResource getPrimary1() {
        return PRIMARY1;
    } 
	
	protected ColorUIResource getPrimary2() {
        return PRIMARY2;
    } 
	
	protected ColorUIResource getPrimary3() {
        return PRIMARY3;
    } 
}
