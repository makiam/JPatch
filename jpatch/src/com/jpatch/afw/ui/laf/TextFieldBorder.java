package com.jpatch.afw.ui.laf;

import java.awt.*;
import javax.swing.border.*;

public class TextFieldBorder implements Border {
	private final Color BORDER_COLOR = new Color(0xaaaaaa);
	private final Color SHADOW1_COLOR = new Color(0x40000000, true);
	private final Color SHADOW2_COLOR = new Color(0x20000000, true);
	private final Color SHADOW3_COLOR = new Color(0x0c000000, true);
	private final Insets INSETS = new Insets(0, 1, 0, 1);
	
	public Insets getBorderInsets(Component c) {
		return INSETS;
	}

	public boolean isBorderOpaque() {
		return true;
	}

	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		g.setColor(SHADOW1_COLOR);
		g.drawRect(x + 1, y + 1, width, height);
		g.setColor(SHADOW2_COLOR);
		g.drawRect(x + 2, y + 2, width, height);
		g.setColor(SHADOW3_COLOR);
		g.drawRect(x + 3, y + 3, width, height);
		g.setColor(BORDER_COLOR);
		g.drawRect(x, y, width - 1, height - 1);
	}
}
