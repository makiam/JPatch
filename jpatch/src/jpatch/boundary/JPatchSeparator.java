package jpatch.boundary;

import javax.swing.*;
import java.awt.*;

public class JPatchSeparator {
	private static final Dimension dimH = new Dimension(10,0);
	private static final Dimension dimV = new Dimension(0,10);
	
	static JSeparator createHorizontalSeparator() {
		JSeparator separator = new JSeparator();
		separator.setMaximumSize(dimH);
		separator.setMinimumSize(dimH);
		separator.setPreferredSize(dimH);
		return separator;
	}
	static JSeparator createVerticalSeparator() {
		JSeparator separator = new JSeparator();
		separator.setMaximumSize(dimV);
		separator.setMinimumSize(dimV);
		separator.setPreferredSize(dimV);
		return separator;
	}
}
