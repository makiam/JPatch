package jpatch.boundary.laf;

import javax.swing.plaf.*;
import javax.swing.plaf.basic.BasicFormattedTextFieldUI;
import java.awt.*;
import javax.swing.*;

public class SmoothFormattedTextFieldUI extends BasicFormattedTextFieldUI {

    public static ComponentUI createUI(final JComponent jcomponent) {
        return new SmoothFormattedTextFieldUI();
    }

    protected void paintSafely(Graphics g) {
        SmoothUtilities.configureGraphics(g);
        super.paintSafely(g);
    }
}
