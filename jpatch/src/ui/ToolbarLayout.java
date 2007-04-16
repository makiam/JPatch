package ui;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ToolbarLayout implements LayoutManager2 {
	public static enum Position { LEFT, CENTER, RIGHT };
	
	private static final int NORMAL = 0;
	private static final int DISABLED = 1;
	
	private boolean hideText = true;
	private Color textColor = Color.WHITE;
	private Font textFont = new Font("sans-serif", Font.PLAIN, 8);
	
	private List<ToolTipComponent> componentList = new ArrayList<ToolTipComponent>();
	private Dimension dimension = new Dimension();
	
	public void addLayoutComponent(Component comp, Object constraints) {
		if (comp instanceof ToolTipComponent) {
			return;
		}
		componentList.add(new ToolTipComponent(comp, (Position) constraints));
		computeSize();
	}

	public float getLayoutAlignmentX(Container target) {
		// TODO Auto-generated method stub
		return 0;
	}

	public float getLayoutAlignmentY(Container target) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void invalidateLayout(Container target) {
		// TODO Auto-generated method stub

	}

	public Dimension maximumLayoutSize(Container target) {
		return dimension;
	}

	public void addLayoutComponent(String name, Component comp) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	public void layoutContainer(Container parent) {
		List<ToolTipComponent>[] lists = new List[] {
				new ArrayList<ToolTipComponent>(),
				new ArrayList<ToolTipComponent>(),
				new ArrayList<ToolTipComponent>()
		};
		for (ToolTipComponent comp : componentList) {
			lists[comp.getPosition()].add(comp);
		}
		for (int i = 0; i < 3; i++) {
			int xPos = 0, xOff = 0;
			for (int j = 0, n = lists[i].size(); j < n; j++) {
				ToolTipComponent comp = lists[i].get(j);
				if (j == 0) {
					xOff = comp.getOffsetWidth() - comp.component.getWidth() / 2;
				}
				comp.component.setBounds(xPos + xOff, 0, comp.component.getWidth(), comp.component.getHeight());
				comp.setBounds(xPos, comp.component.getHeight(), comp.getWidth(), comp.getHeight());
				xPos += (j == n - 1 ? comp.getOffsetWidth() : comp.component.getWidth());
				System.out.println(comp.getClass() + " " + comp.getBounds());
			}
		}
	}

	public Dimension minimumLayoutSize(Container parent) {
		return dimension;
	}
	

	public Dimension preferredLayoutSize(Container parent) {
		return dimension;
	}

	public void removeLayoutComponent(Component comp) {
		for (Iterator<ToolTipComponent> it = componentList.iterator(); it.hasNext(); ) {
			if (it.next().component == comp) {
				it.remove();
			}
		}
		computeSize();
	}

	
	@SuppressWarnings("unchecked")
	private void computeSize() {
		int width = 0, height = 0;
		List<ToolTipComponent>[] lists = new List[] {
				new ArrayList<ToolTipComponent>(),
				new ArrayList<ToolTipComponent>(),
				new ArrayList<ToolTipComponent>()
		};
		for (ToolTipComponent comp : componentList) {
			lists[comp.getPosition()].add(comp);
		}
		for (int i = 0; i < 3; i++) {
			for (int j = 0, n = lists[i].size(); j < n; j++) {
				ToolTipComponent comp = lists[i].get(j);
				width += (j == 0 || j == n - 1) ? comp.getOffsetWidth() : comp.component.getWidth();
				int h = comp.component.getHeight() + comp.getHeight();
				if (h > height) {
					height = h;
				}
			}
		}
		dimension.width = width;
		dimension.height = height;
	}
	
	private class ToolTipComponent extends JComponent implements ChangeListener {
		private Component component;
		private Position pos;
		private Image enabledToolTip;
		private Image disabledToolTip;
		
		ToolTipComponent(Component component, Position pos) {
			this.component = component;
			this.pos = pos;
			if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) component;
				String text = button.getToolTipText();
				button.setToolTipText(null);
				if (text != null) {
					enabledToolTip = ImageUtils.createEtchedIcon(ImageUtils.createTextIcon(textFont, textColor, text));
					disabledToolTip = ImageUtils.createDisabledIcon(enabledToolTip);
				}
				button.getModel().addChangeListener(this);
			}
			setEnabled(false);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			Image image = component.isEnabled() ? enabledToolTip : disabledToolTip;
			if (image != null) {
				g.drawImage(image, 0, 0, null);
			}
		}
		
		public void stateChanged(ChangeEvent e) {
			ButtonModel buttonModel = ((AbstractButton) component).getModel();
			setVisible(buttonModel.isRollover());
		}
		
		public int getOffsetWidth() {
			int bw = component.getWidth();
			int lw = enabledToolTip == null ? 0 : enabledToolTip.getWidth(null);
			if (lw > bw) {
				return (lw + bw) / 2;
			} else {
				return bw;
			}
		}
		
		public int getWidth() {
			return enabledToolTip == null ? 0 : enabledToolTip.getWidth(null);
		}
		
		public int getHeight() {
			return enabledToolTip == null ? 0 : enabledToolTip.getHeight(null);
		}
		
		public int getPosition() {
			switch(pos) {
			case LEFT:
				return 0;
			case CENTER:
				return 1;
			case RIGHT:
				return 2;
			}
			throw new IllegalStateException();
		}
	}
}
