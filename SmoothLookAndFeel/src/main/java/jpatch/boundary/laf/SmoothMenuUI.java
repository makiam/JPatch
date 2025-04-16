package jpatch.boundary.laf;

import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import javax.swing.*;

public class SmoothMenuUI extends BasicMenuUI {
    public static ComponentUI createUI(JComponent jcomponent) {
        return new SmoothMenuUI();
    }

    public void paint(Graphics g, JComponent c) {
        SmoothUtilities.configureGraphics(g);
        super.paint(g, c);
//        paintMenuItem(g, c, checkIcon, arrowIcon,
//                selectionBackground, selectionForeground,
//                1);
    }
}
