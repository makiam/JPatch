package jpatch.boundary.laf;

import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import java.awt.*;

import javax.swing.*;

public class SmoothMenuItemUI extends BasicMenuItemUI {
    public static ComponentUI createUI(JComponent jcomponent) {
    	SmoothMenuItemUI smoothMenuItemUI = new SmoothMenuItemUI();
    	smoothMenuItemUI.defaultTextIconGap = 10;
        return smoothMenuItemUI;
    }

//    public void paint(Graphics g, JComponent c) {
//        SmoothUtilities.configureGraphics(g);
//        super.paint(g, c);
//    }
    
    public void paint(Graphics g, JComponent c) {
    	SmoothUtilities.configureGraphics(g);
    	super.paint(g, c);
//        paintMenuItem(g, c, checkIcon, arrowIcon,
//                      selectionBackground, selectionForeground,
//                      1);
    }
}
