package jpatch.boundary.laf;

import javax.swing.plaf.*;
import javax.swing.plaf.basic.BasicPanelUI;
import java.awt.*;
import javax.swing.*;

public class SmoothPanelUI extends BasicPanelUI {

    public static ComponentUI createUI(final JComponent jcomponent) {
        return new SmoothPanelUI();
    }

    public void paint(Graphics g, JComponent c) {
//        SmoothUtilities.configureGraphics(g);
        super.paint(g, c);
    }
}
