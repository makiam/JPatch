package jpatch.boundary.laf;

import javax.swing.border.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.metal.*;

import java.awt.*;
import java.awt.geom.*;

/**
 * Border factory. Modelled after the MetalBorders, from
 * which it inherits. The <code>paintBorder()</code> method
 * is overridden to turn on anti-aliasing. This is very
 * similar to what's done in UI delegates.
 */
public class SmoothBorders extends MetalBorders {
    private static Border buttonBorder;

//    public static class ButtonBorder extends MetalBorders.ButtonBorder {
//        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
//            SmoothUtilities.configureGraphics(g);
//            super.paintBorder(c, g, x, y, w, h);
//        }MetalUtils
//    }

    public static class ButtonBorder extends MetalBorders.ButtonBorder {
    	
    	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
    		SmoothUtilities.configureGraphics(g);
    		AbstractButton button = (AbstractButton) c;
    		ButtonModel model = button.getModel();
    		Graphics2D g2 = (Graphics2D) g;
    		if (model.isRollover()) {
    			g2.setColor(Theme.rolloverBorderColor);
    			g2.draw(new RoundRectangle2D.Float(x + 2, y + 2, w - 5, h - 5, 5, 5));
    			g2.setColor(c.getBackground().darker().darker());
    			g2.draw(new RoundRectangle2D.Float(x + 1, y + 1, w - 3, h - 3, 7, 7));
//    			Area area = new Area();
//    			area.add(new Area(new RoundRectangle2D.Float(x, y, w, h, 9, 9)));
//    			area.subtract(new Area(new RoundRectangle2D.Float(x + 3, y + 3, w - 6, h - 6, 6, 6)));
//    			g2.fill(area);
//    			g2.setColor(c.getBackground().darker().darker());
//    			g2.draw(new RoundRectangle2D.Float(x + 1, y + 1, w - 3, h - 3, 7, 7));
    			
    		} else if (model.isSelected() || model.isPressed() || !(c.getParent() instanceof JToolBar)){
    			g2.setColor(c.getBackground().darker().darker());
    			g2.draw(new RoundRectangle2D.Float(x + 1, y + 1, w - 3, h - 3, 7, 7));
    		}
    	}	
    }
    
    public static Border getButtonBorder() {
        if (buttonBorder == null) {
            buttonBorder = new BorderUIResource.CompoundBorderUIResource(new ButtonBorder(), new BasicBorders.MarginBorder());
        }
        return buttonBorder;
    }
}