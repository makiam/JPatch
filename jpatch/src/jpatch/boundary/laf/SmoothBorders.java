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
    public final static Border buttonBorder = createBorder(new ButtonBorder());
    public final static Border textFieldBorder = createBorder(new TextFieldBorder());
    
//    public static class ButtonBorder extends MetalBorders.ButtonBorder {
//        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
//            SmoothUtilities.configureGraphics(g);
//            super.paintBorder(c, g, x, y, w, h);
//        }MetalUtils
//    }

    public static class ButtonBorder extends MetalBorders.ButtonBorder {
    	protected static Insets borderInsets = new Insets( 2, 2, 2, 2 );
    	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
    		SmoothUtilities.configureGraphics(g);
    		AbstractButton button = (AbstractButton) c;
    		if (!button.isBorderPainted())
    			return;
    		ButtonModel model = button.getModel();
    		if (model.isRollover()) {
    			g.setColor(Theme.rolloverBorderColor);
    			g.drawRoundRect(x + 1, y + 1, w - 3, h - 3, 7, 7);
    			g.setColor(new Color(0, 0, 0, 0.5f));
    			g.drawRoundRect(x, y, w - 1, h - 1, 9, 9);
    		} else if (model.isSelected() || model.isPressed()){
    			g.setColor(new Color(0, 0, 0, 0.5f));
    			g.drawRoundRect(x, y, w - 1, h - 1, 9, 9);
    		} else if (!(c.getParent() instanceof JToolBar)) {
    			g.setColor(new Color(0, 0, 0, 0.25f));
    			g.drawRoundRect(x, y, w - 1, h - 1, 9, 9);
    		}
    	}
    	
    	public Insets getBorderInsets( Component c ) {
            return borderInsets;
        }
    	
        public Insets getBorderInsets(Component c, Insets newInsets) {
        	newInsets.top = borderInsets.top;
        	newInsets.left = borderInsets.left;
        	newInsets.bottom = borderInsets.bottom;
        	newInsets.right = borderInsets.right;
        	return newInsets;
        }
    }
    
    public static class TextFieldBorder extends MetalBorders.TextFieldBorder {
    	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
    		SmoothUtilities.configureGraphics(g);
    		g.setColor(new Color(0, 0, 0, 0.5f));
    		g.drawRoundRect(x, y, w - 1, h - 1, 7, 7);
    	}	
    }
    
    private static Border createBorder(Border border) {
    	return new BorderUIResource.CompoundBorderUIResource(border, new BasicBorders.MarginBorder());
    }
}