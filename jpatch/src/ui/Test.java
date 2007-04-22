package ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.*;

import jpatch.boundary.settings.Settings;
import jpatch.boundary.ui.KeyBindingHelper;
import jpatch.entity.attributes2.Attribute;
import jpatch.entity.attributes2.AttributeListener;
import jpatch.entity.attributes2.StateMachine;

public class Test {
	public static void main(String[] args) throws Exception {
		System.setProperty("swing.boldMetal", Settings.JPATCH_ROOT_NODE.get("metalBoldText", "false"));
		System.setProperty("swing.aatext", Settings.JPATCH_ROOT_NODE.get("fontSmoothing", "true"));
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		new Test();
	}
	
	@SuppressWarnings("serial")
	Test() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		JToolBar toolBar = new JToolBar();
		JPanel panel = new JPanel();
		
		JToggleButton tb1 = new JToggleButton("1");
		JToggleButton tb2 = new JToggleButton("2");
		JToggleButton tb3 = new JToggleButton("3");
		
		JMenuItem mi1 = new JMenuItem();
		mi1.setIcon(new MenuIcon(mi1, null, "Test", "SHIFT A"));
		JMenuItem mi2 = new JMenuItem();
		mi2.setIcon(new MenuIcon(mi1, null, "Another Test", "B"));
		JMenuItem mi3 = new JMenuItem();
		mi3.setIcon(new MenuIcon(mi1, null, "Don't know", null));

//			@Override
//			public void paintComponent(Graphics g) {
//				g.setColor(getBackground());
//				Rectangle r = g.getClipBounds();
//				g.fillRect(r.x, r.y, r.width, r.height);
////				g.drawString("aaa", 5, 16);
//			}
			
//			@Override
//			public Dimension getPreferredSize() {
//				return new Dimension(200, 200);
//			}
	
		
//		mi1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.META_DOWN_MASK));
//		mi1.setActionMap(null);
//		mi1.removeAll();
//		mi1.setLayout(new BorderLayout());
//		mi1.add(new JLabel("\u2713"), BorderLayout.WEST);
//		mi1.add(new JLabel("test"), BorderLayout.CENTER);
//		mi1.add(new JLabel("SHIFT X"), BorderLayout.EAST);
//		
//		Font font = new Font("sans serif", Font.PLAIN, 12);
//		FontMetrics fm = new JLabel().getFontMetrics(font);
//		System.out.println(fm.stringWidth("test"));
		
		
		final StateMachine<Integer> sm = new StateMachine<Integer>(new Integer[] {1, 2, 3}, 1);
		JPatchButtons.configureRadioButton(tb1, sm, 1);
		JPatchButtons.configureRadioButton(tb2, sm, 2);
		JPatchButtons.configureRadioButton(tb3, sm, 3);
		JPatchButtons.configureRadioButton(mi1, sm, 1);
		JPatchButtons.configureRadioButton(mi2, sm, 2);
		JPatchButtons.configureRadioButton(mi3, sm, 3);
		sm.setDefaultState(1);
		sm.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute source) {
				System.out.println(((StateMachine) source).getState());
			}
		});
		toolBar.add(tb1);
		toolBar.add(tb2);
		toolBar.add(tb3);
		
		/**
		 * This is a workaround for the following problem:
		 * Some Swing components like JTextFields only consume KEY_TYPED events, but not teir preceding KEY_PRESSED events.
		 * The result of this is that if one is listening for KEY_PRESSED events, events will be reported even if something like
		 * a JTextField has focus and the KeyEvent belongs to some text entered by the user. Listening only for KEY_TYPED events
		 * doesn't work because some keys don't produce KEY_TYPED events, and (worse), KEY_TYPED events have only keyChar information,
		 * and unfortunately no keyCode information.
		 * 
		 * Here's what this workaround does:
		 * A KeyEventPostProcessor reveives all KeyEvents from the KeyboardFocusManager.
		 * If the KeyEvent has already been consumed, it does nothing.
		 * Otherwise, if the KeyEvent has no character assigned (e.getKeyChar() == KeyEvent.CHAR_UNDEFINED), the event
		 * is being processed. If on the other hand a character is assigned, it stores the event (in the assumption that
		 * a subsequent KEY_TYPED event will follow.
		 * This KEY_TYPED event can only get so far if it hasn't been consumed already (e.g. by a JTextField), so it checks
		 * wheter it corresponds to the stored KEY_PRESSED event, and if that's the case, it processes the stored KEY_PRESSED
		 * event (remember that the keyCode is only stored in this KEY_PRESSED event, not in the KEY_TYPED event). Otherwise
		 * it will process the KEY_TYPED event.
		 */
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(new KeyEventPostProcessor() {
			KeyEvent storedEvent;
			
			public boolean postProcessKeyEvent(KeyEvent e) {
				if (e.isConsumed()) {
					return false;
				}
				if (e.getID() == KeyEvent.KEY_PRESSED) {
					if (e.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
						processEvent(e);
					} else {
						storedEvent = e;
					}
				} else if (e.getID() == KeyEvent.KEY_TYPED) {
					if (storedEvent != null && e.getKeyChar() == storedEvent.getKeyChar()) {
						processEvent(storedEvent);
					} else {
						processEvent(e);
					}
				}
				
				return false;
				
				
			}
			private void processEvent(KeyEvent e) {
				if (KeyStroke.getKeyStrokeForEvent(e).equals(KeyStroke.getKeyStroke("pressed A"))) {
					sm.setState(1);
				}
			}
			
		});
//		KeyboardFocusManager.getCurrentKeyboardFocusManager().setDefaultFocusTraversalKeys(id, keystrokes)
		panel.add(new JLabel("test:"));
		panel.add(new JTextField(20));
		JPanel p2 = new JPanel();
		p2.setBackground(Color.BLACK);
		p2.setPreferredSize(new Dimension(320, 240));
		panel.add(p2);
		
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Test");
		
		menu.add(mi1);
		menu.add(mi2);
		menu.add(mi3);
		menuBar.add(menu);
		frame.setJMenuBar(menuBar);
		frame.add(toolBar, BorderLayout.NORTH);
		frame.add(panel, BorderLayout.CENTER);
		
		frame.pack();
		frame.setVisible(true);
	}
	
	private static class MenuIcon implements Icon {
		private static final int ICON_WIDTH = 16;
		private static final int MENU_HEIGHT = 16;
		private static final int GAP_WIDTH = 4;
		private static class SimpleIcon implements Icon {
			public int getIconHeight() {
				return MENU_HEIGHT;
			}
			public int getIconWidth() {
				return ICON_WIDTH;
			}
			public void paintIcon(Component c, Graphics g, int x, int y) { }
		};
		private static final Icon UNCHECKED_ICON = new SimpleIcon();
		private static final Icon CHECKED_ICON = new SimpleIcon() {
			public void paintIcon(Component c, Graphics g, int x, int y) {
				g.translate(x, y);
				g.setColor(Color.GRAY);
				g.fillOval(4, 4, 8, 8);
			}
		};
		private static final Icon SUBMENU_ICON = new SimpleIcon() {
			public void paintIcon(Component c, Graphics g, int x, int y) {
				g.translate(x, y);
				g.setColor(Color.GRAY);
				g.fillPolygon(new int[] {4, 12, 12}, new int[] {8, 4, 12}, 3);
			}
		};
		private JMenuItem menuItem;
		private Icon icon;
		private JLabel text;
		private JLabel accelerator;
		private int minWidth;
		
		MenuIcon(JMenuItem menuItem, Icon icon, String text, String accelerator) {
			this.menuItem = menuItem;
			this.text = new JLabel(text);
			minWidth = ICON_WIDTH + GAP_WIDTH + this.text.getMinimumSize().width;
			if (accelerator != null) {
				this.accelerator = new JLabel(accelerator);
				minWidth += GAP_WIDTH + this.accelerator.getMinimumSize().width;
			}
			if (menuItem instanceof JMenu) {
				minWidth += GAP_WIDTH + ICON_WIDTH;
			}
		}
		
		public int getIconHeight() {
			return MENU_HEIGHT;
		}

		public int getIconWidth() {
//			System.out.println("getIconWidth");
			Component parent = menuItem.getParent();
//			System.out.println("parent=" + parent);
			if (parent instanceof JPopupMenu) {
				int width = 0;
				for (Component c : ((JPopupMenu) parent).getComponents()) {
					if (c instanceof JMenuItem) {
						Icon icon = ((JMenuItem) c).getIcon();
						if (icon instanceof MenuIcon) {
							int w = ((MenuIcon) icon).getMinimumWidth();
//							System.out.println(w);
							if (w > width) {
								width = w;
							}
						}
					}
				}
//				System.out.println("width=" + width);
				return width;
			} else {
//				System.out.println("minwidth=" + getMinimumWidth());
				return getMinimumWidth();
			}
		}

		public int getMinimumWidth() {
			return minWidth;
		}
		
		public void paintIcon(Component c, Graphics g, int x, int y) {
//			g.translate(x, y);
			System.out.println(text.getText() + " " + menuItem.isSelected());
			if (menuItem.isSelected()) {
				CHECKED_ICON.paintIcon(menuItem, g, 0, 0);
			} else {
				UNCHECKED_ICON.paintIcon(menuItem, g, 0, 0);
			}
			
//			if (icon != null) {
//				icon.paintIcon(menuItem, g, x, y);
//			}
//			g.translate(GAP_WIDTH, 0);
			
			text.setBounds(0, 0, text.getMinimumSize().width, MENU_HEIGHT);
			g.translate(ICON_WIDTH + GAP_WIDTH, 0);
			text.paint(g);
			if (accelerator != null) {
				int w = accelerator.getMinimumSize().width;
//				System.out.println("comp width=" + c.getWidth() + " accel width=" + w);
				accelerator.setBounds(0, 0, w, MENU_HEIGHT);
				g.translate(c.getWidth() - ICON_WIDTH - GAP_WIDTH - w, 0);
				accelerator.paint(g);
			}
		}
		
	}
	
	
}
