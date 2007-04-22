package ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.*;

import jpatch.boundary.ui.KeyBindingHelper;
import jpatch.entity.attributes2.Attribute;
import jpatch.entity.attributes2.AttributeListener;
import jpatch.entity.attributes2.StateMachine;

public class Test {
	public static void main(String[] args) {
		new Test();
	}
	
	Test() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		JToolBar toolBar = new JToolBar();
		JPanel panel = new JPanel();
		
		JToggleButton tb1 = new JToggleButton("1");
		JToggleButton tb2 = new JToggleButton("2");
		JToggleButton tb3 = new JToggleButton("3");
		
		JMenuItem mi1 = new JMenuItem("a") {

			@Override
			public void paintComponent(Graphics g) {
				g.setColor(getBackground());
				Rectangle r = g.getClipBounds();
				g.fillRect(r.x, r.y, r.width, r.height);
//				g.drawString("aaa", 5, 16);
			}
			
//			@Override
//			public Dimension getPreferredSize() {
//				return new Dimension(200, 200);
//			}
		};
		
		mi1.removeAll();
		mi1.setLayout(new BorderLayout());
		mi1.add(new JLabel("\u2713"), BorderLayout.WEST);
		mi1.add(new JLabel("test"), BorderLayout.CENTER);
		mi1.add(new JLabel("SHIFT X"), BorderLayout.EAST);
		
		Font font = new Font("sans serif", Font.PLAIN, 12);
		FontMetrics fm = new JLabel().getFontMetrics(font);
		System.out.println(fm.stringWidth("test"));
		JMenuItem mi2 = new JCheckBoxMenuItem("2 skdfj asdfkjhskdjfh kasjdhf");
		JMenuItem mi3 = new JCheckBoxMenuItem("3");
		
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
				System.out.println(e.getKeyCode() + " " + e.getModifiersEx() + " " + e);
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
}
