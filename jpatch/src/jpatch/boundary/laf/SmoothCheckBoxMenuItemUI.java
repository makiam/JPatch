package jpatch.boundary.laf;

import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import javax.swing.*;

public class SmoothCheckBoxMenuItemUI extends BasicCheckBoxMenuItemUI {
    public static ComponentUI createUI(JComponent jcomponent) {
        return new SmoothCheckBoxMenuItemUI();
    }

    public void paint(Graphics g, JComponent c) {
        SmoothUtilities.configureGraphics(g);
        super.paint(g, c);
    }
}
