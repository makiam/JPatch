package jpatch.boundary.laf;

import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;
import java.awt.*;
import javax.swing.*;

public class SmoothComboBoxUI extends MetalComboBoxUI {
    public static ComponentUI createUI(JComponent jcomponent) {
        return new SmoothComboBoxUI();
    }

    public void paint(Graphics g, JComponent c) {
        SmoothUtilities.configureGraphics(g);
        super.paint(g, c);
    }
}
