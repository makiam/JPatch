package jpatch.boundary.laf;

import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import javax.swing.*;

public class SmoothEditorPaneUI extends BasicEditorPaneUI {
    public static ComponentUI createUI(JComponent jcomponent) {
        return new SmoothEditorPaneUI();
    }

    protected void paintSafely(Graphics g) {
        SmoothUtilities.configureGraphics(g);
        super.paintSafely(g);
    }
}
