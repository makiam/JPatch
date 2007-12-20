package com.jpatch.afw.ui;

import java.awt.*;
import javax.swing.*;

public class AttributeEditorPanel {
	JComponent box = Box.createVerticalBox();
	JComponent panel = new EditorPanel(box);
	
	public void add(AttributeEditor ae, int index) {
		box.add(ae.getRootContainer().getComponent(), index);
	}
	
	public void remove(int index) {
		box.remove(index);
	}
	
	public JComponent getComponent() {
		return panel;
	}
	
	@SuppressWarnings("serial")
	private static class EditorPanel extends JPanel implements Scrollable {
		private EditorPanel(JComponent child) {
			super(new BorderLayout());
//			setBackground(Color.YELLOW);
			setOpaque(false);
			add(child, BorderLayout.NORTH);
		}
			
		public Dimension getPreferredScrollableViewportSize() {
			return new Dimension(0, 0);
		}
		
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 20;
		}
		
		public boolean getScrollableTracksViewportHeight() {
			return false;
		}
		
		public boolean getScrollableTracksViewportWidth() {
			return true;
		}
		
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 20;
		}
	}
}
