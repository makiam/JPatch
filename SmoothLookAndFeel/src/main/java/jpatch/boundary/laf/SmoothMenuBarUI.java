package jpatch.boundary.laf;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

public class SmoothMenuBarUI extends BasicMenuBarUI {
    public static ComponentUI createUI(JComponent jcomponent) {
        return new SmoothMenuBarUI();
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        SmoothUtilities.configureGraphics(g);
        super.paint(g, c);
    }

    
}
