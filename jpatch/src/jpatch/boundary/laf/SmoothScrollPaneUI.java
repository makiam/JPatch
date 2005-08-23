package jpatch.boundary.laf;

import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;
import java.awt.*;
import javax.swing.*;

public class SmoothScrollPaneUI extends MetalScrollPaneUI {
    public static ComponentUI createUI(JComponent jcomponent) {
        return new SmoothScrollPaneUI();
    }

    public void paint(Graphics g, JComponent c) {
        SmoothUtilities.configureGraphics(g);
        super.paint(g, c);
    }
}
