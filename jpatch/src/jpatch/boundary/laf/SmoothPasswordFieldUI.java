package jpatch.boundary.laf;

import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import javax.swing.*;

public class SmoothPasswordFieldUI extends BasicPasswordFieldUI {
    public static ComponentUI createUI(JComponent jcomponent) {
        return new SmoothPasswordFieldUI();
    }

    protected void paintSafely(Graphics g) {
        SmoothUtilities.configureGraphics(g);
        super.paintSafely(g);
    }
}
