package jpatch.boundary.laf;

import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

import java.awt.*;

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
    
    protected void paintFocus(Graphics g, AbstractButton b,
			Rectangle viewRect, Rectangle textRect, Rectangle iconRect){
		
		Rectangle focusRect = new Rectangle();
		String text = b.getText();
		boolean isIcon = b.getIcon() != null;
		
		// If there is text
		if ( text != null && !text.equals( "" ) ) {
			if ( !isIcon ) {
				focusRect.setBounds( textRect );
			}
			else {
				focusRect.setBounds( iconRect.union( textRect ) );
			}
		}
		// If there is an icon and no text
		else if ( isIcon ) {
			focusRect.setBounds( iconRect );
		}
		
		g.setColor(Color.BLACK);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		BasicStroke basicStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[] { 1 }, 0.5f);
		((Graphics2D) g).setStroke(basicStroke);
		g.drawRect((focusRect.x-2), (focusRect.y-2),
				focusRect.width+3, focusRect.height+3);
		
	}

	@Override
	public Dimension getMaximumSize(JComponent c) {
		// TODO Auto-generated method stub
		return super.getMaximumSize(c);
	}

	@Override
	public Dimension getMinimumSize(JComponent c) {
		// TODO Auto-generated method stub
		return super.getMinimumSize(c);
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		// TODO Auto-generated method stub
		return super.getPreferredSize(c);
	}
    
    // DefaultButtonModel
}
