package jpatch.boundary.laf;

import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;
import java.awt.*;
import javax.swing.*;

public class SmoothScrollBarUI extends MetalScrollBarUI {
    public static ComponentUI createUI(JComponent jcomponent) {
        return new SmoothScrollBarUI();
    }

    public void paint(Graphics g, JComponent c) {
//        SmoothUtilities.configureGraphics(g);
        super.paint(g, c);
    }
}
