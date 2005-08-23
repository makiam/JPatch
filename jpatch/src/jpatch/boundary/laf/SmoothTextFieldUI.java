package jpatch.boundary.laf;

import javax.swing.text.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;
import java.awt.*;

import javax.swing.*;

public class SmoothTextFieldUI extends MetalTextFieldUI {
	JTextComponent editor;
	
    public static ComponentUI createUI(JComponent jcomponent) {
        return new SmoothTextFieldUI();
    }

    public void installUI(JComponent c) {
        editor = (JTextComponent) c;
        super.installUI(c);
    }
            
    protected void paintSafely(Graphics g) {
        SmoothUtilities.configureGraphics(g);
        super.paintSafely(g);
    }
    
    protected void paintBackground(Graphics g) {
        g.setColor(editor.getBackground());
        g.fillRoundRect(0, 0, editor.getWidth(), editor.getHeight(), 7, 7);
    }
}
