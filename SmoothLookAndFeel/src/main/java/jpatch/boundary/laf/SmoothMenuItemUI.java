package jpatch.boundary.laf;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

public class SmoothMenuItemUI extends BasicMenuItemUI {
    public static ComponentUI createUI(JComponent jcomponent) {
        return new SmoothMenuItemUI();
    }

//    public void paint(Graphics g, JComponent c) {
//        SmoothUtilities.configureGraphics(g);
//        super.paint(g, c);
//    }
    
    @Override
    public void paint(Graphics g, JComponent c) {
    	SmoothUtilities.configureGraphics(g);
    	super.paint(g, c);
//        paintMenuItem(g, c, checkIcon, arrowIcon,
//                      selectionBackground, selectionForeground,
//                      1);
    }
}
