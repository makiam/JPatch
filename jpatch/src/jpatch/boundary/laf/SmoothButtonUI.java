package jpatch.boundary.laf;

import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import javax.swing.*;

/**
 * Smooth Button UI delegate. Responsible for drawing a
 * button. Most UI delegates are similar to this one, the
 * <code>paint()</code> method turns on anti-aliasing and
 * the other methods are merely to install this delegate.
 */
public class SmoothButtonUI extends BasicButtonUI {
    private static final ComponentUI ui = new SmoothButtonUI();

    public static ComponentUI createUI(JComponent jcomponent) {
    	jcomponent.setOpaque(false);
        return ui;
    }

    public void update(Graphics g, JComponent c) {
    	SmoothUtilities.configureGraphics(g);
		AbstractButton button = (AbstractButton) c;
		ButtonModel model = button.getModel();
		Graphics2D g2 = (Graphics2D) g;
		if (button.isContentAreaFilled())
			Theme.paintButtonBackground(button, g2);
		//g.setColor(c.getBackground());
		//g.fillRect(0, 0, c.getWidth(), c.getHeight());
		
//		if (model.isSelected()) {
//			g.setColor(c.getBackground().darker());
//			g2.fill(new RoundRectangle2D.Float(1, 1, c.getWidth() - 2, c.getHeight() - 2, 7, 7));
//			paint(g, c);
//			return;
//		}
//        if ((c.getBackground() instanceof UIResource) &&
//                  button.isContentAreaFilled() && c.isEnabled()) {
//            ButtonModel model = button.getModel();
//            if (!MetalUtils.isToolBarButton(c)) {
//                if (!model.isArmed() && !model.isPressed() &&
//                        MetalUtils.drawGradient(
//                        c, g, "Button.gradient", 0, 0, c.getWidth(),
//                        c.getHeight(), true)) {
//                    paint(g, c);
//                    return;
//                }
//            }
//            else if (model.isRollover() && MetalUtils.drawGradient(
//                        c, g, "Button.gradient", 0, 0, c.getWidth(),
//                        c.getHeight(), true)) {
//                paint(g, c);
//                return;
//            }
//        }
		super.update(g, c);
    }
    
    public void paint(Graphics g, JComponent c) {
        SmoothUtilities.configureGraphics(g);
//        JButton button = (JButton) c;
//        ButtonModel model = button.getModel();
//        g.setColor(Color.YELLOW);
//        g.drawRect(0, 0, button.getWidth(), button.getHeight());
        super.paint(g, c);
    }
    
    // DefaultButtonModel
}