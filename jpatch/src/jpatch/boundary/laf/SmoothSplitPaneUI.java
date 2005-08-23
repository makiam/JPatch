package jpatch.boundary.laf;

import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;
import java.awt.*;
import javax.swing.*;

public class SmoothSplitPaneUI extends MetalSplitPaneUI {
    public static ComponentUI createUI(JComponent jcomponent) {
        return new SmoothSplitPaneUI();
    }

    public void paint(Graphics g, JComponent c) {
        SmoothUtilities.configureGraphics(g);
        super.paint(g, c);
    }
}
