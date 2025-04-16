package jpatch.boundary.laf;

import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;
import java.awt.*;
import javax.swing.*;

public class SmoothRadioButtonUI extends MetalRadioButtonUI {
    private static final ComponentUI ui = new SmoothRadioButtonUI();

    public static ComponentUI createUI(JComponent jcomponent) {
        return ui;
    }

    public void paint(Graphics g, JComponent c) {
        SmoothUtilities.configureGraphics(g);
        super.paint(g, c);
    }
}
