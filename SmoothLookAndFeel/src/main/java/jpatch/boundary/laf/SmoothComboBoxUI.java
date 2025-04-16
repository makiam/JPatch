package jpatch.boundary.laf;

import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

import java.awt.*;

import javax.swing.*;

public class SmoothComboBoxUI extends MetalComboBoxUI {
	public static ComponentUI createUI(JComponent jcomponent) {
		return new SmoothComboBoxUI();
	}
	
	public void paint(Graphics g, JComponent c) {
		SmoothUtilities.configureGraphics(g);
		super.paint(g, c);
	}
	
	public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
//		This is really only called if we're using ocean.
//		if (MetalLookAndFeel.usingOcean()) {
			bounds.x += 0;
			bounds.y += 0;
			bounds.width -= 1;
			bounds.height -= 0;
			super.paintCurrentValue(g, bounds, hasFocus);
//		}
//		else if (g == null || bounds == null) {
//			throw new NullPointerException(
//			"Must supply a non-null Graphics and Rectangle");
//		}
	}
	
	public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
		g.setColor(new Color(0, 0, 0, 0.25f));
		g.drawRoundRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1, 7, 7);
//		if (true) return;
//		This is really only called if we're using ocean.
//		if (MetalLookAndFeel.usingOcean()) {
//		g.setColor(MetalLookAndFeel.getControlDarkShadow());
//		g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height - 1);
//		g.setColor(MetalLookAndFeel.getControlShadow());
//		g.drawRect(bounds.x + 1, bounds.y + 1, bounds.width - 2,
//		bounds.height - 3);
//		}
//		else if (g == null || bounds == null) {
//		throw new NullPointerException(
//		"Must supply a non-null Graphics and Rectangle");
//		}
	}
	
}
