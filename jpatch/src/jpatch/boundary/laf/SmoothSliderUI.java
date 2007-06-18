package jpatch.boundary.laf;

import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;
import java.awt.*;

import javax.swing.*;

public class SmoothSliderUI extends MetalSliderUI {
    public static ComponentUI createUI(JComponent jcomponent) {
        return new SmoothSliderUI();
    }

    public void paint(Graphics g, JComponent c) {
        SmoothUtilities.configureGraphics(g);
        super.paint(g, c);
    }
    
    @Override
	public void paintThumb(Graphics g) {
		System.out.println("*");
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int cx = thumbRect.x + thumbRect.width / 2;
		int cy = thumbRect.y + thumbRect.height / 2;
		g2.setColor(new Color(0x666666));
        g2.fillOval(cx - 6, cy - 6, 12, 12);
        g2.setPaint(new GradientPaint(0, cy - 4, new Color(0xffffff), 0, cy + 4, new Color(0x888888)));
        g2.fillOval(cx - 5, cy - 5, 10, 10);
        
//		g.setColor(Color.BLACK);
//        g.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
	}

	@Override
	public void paintTrack(Graphics g) {
		System.out.println("*");
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int c = trackRect.height / 2;
		g2.setPaint(new GradientPaint(0, trackRect.y + c - 2, new Color(0x80000000, true), 0, trackRect.y + c + 2, new Color(0x10000000, true)));
        g2.fillRoundRect(trackRect.x, trackRect.y + c - 2, trackRect.width, 4, 4, 4);
	}
}
