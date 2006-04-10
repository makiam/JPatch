package test;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

import jpatch.boundary.ui.*;

public class UiTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Locale.setDefault(Locale.GERMANY);
		JFrame frame = new JFrame("UI-Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JToolBar toolBar1 = new JToolBar(JToolBar.HORIZONTAL);
		JToolBar toolBar2 = new JToolBar(JToolBar.VERTICAL);
		
		AbstractButton button1a = new JPatchToggleButton("1a");
		AbstractButton button1b = new JToggleButton("1b");
		AbstractButton button1c = new JToggleButton("1c");
		AbstractButton button1d = new JToggleButton("1d");
		AbstractButton button2a = new JToggleButton("2a");
		AbstractButton button2b = new JToggleButton("2b");
		AbstractButton button2c = new JToggleButton("2c");
		AbstractButton button2d = new JToggleButton("2d");
		
		final UnderlyingToggleButtonModel buttonModel1 = new UnderlyingToggleButtonModel();
		final UnderlyingToggleButtonModel buttonModel2 = new UnderlyingToggleButtonModel();
		final UnderlyingToggleButtonModel buttonModel3 = new UnderlyingToggleButtonModel();
		final UnderlyingToggleButtonModel buttonModel4 = new UnderlyingToggleButtonModel();
		
//		ActionListener actionListener = new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				System.out.println("\n*****\nactionPerformed(" + e + ")\n*****\n");
//				buttonModel2.setSelected(true);
//			}
//		};
		
		final ActionListener actionListener1 = new MyActionListener("1", buttonModel1);
		ActionListener actionListener2 = new MyActionListener("2", buttonModel2);
		ActionListener actionListener3 = new MyActionListener("3", buttonModel3);
		ActionListener actionListener4 = new MyActionListener("4", buttonModel4);
		
		buttonModel1.addActionListener(actionListener1);
		buttonModel2.addActionListener(actionListener2);
		buttonModel3.addActionListener(actionListener3);
		buttonModel4.addActionListener(actionListener4);
		
		button1a.setModel(new JPatchToggleButtonModel(buttonModel1));
		button1b.setModel(new JPatchToggleButtonModel(buttonModel2));
		button1c.setModel(new JPatchToggleButtonModel(buttonModel3));
		button1d.setModel(new JPatchToggleButtonModel(buttonModel4));
		button2a.setModel(new JPatchToggleButtonModel(buttonModel1));
		button2b.setModel(new JPatchToggleButtonModel(buttonModel2));
		button2c.setModel(new JPatchToggleButtonModel(buttonModel3));
		button2d.setModel(new JPatchToggleButtonModel(buttonModel4));
		
		toolBar1.add(button1a);
		toolBar1.add(button1b);
		toolBar1.add(button1c);
		toolBar1.add(button1d);
		
		toolBar2.add(button2a);
		toolBar2.add(button2b);
		toolBar2.add(button2c);
		toolBar2.add(button2d);
		
		final ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(button1a);
		buttonGroup.add(button1b);
		buttonGroup.add(button1c);
		buttonGroup.add(button1d);
		
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Menu");
		JPatchRadioButtonMenuItem menuItem1 = new JPatchRadioButtonMenuItem("item 1");
		JPatchRadioButtonMenuItem menuItem2 = new JPatchRadioButtonMenuItem("item 2");
		JPatchRadioButtonMenuItem menuItem3 = new JPatchRadioButtonMenuItem("item 3");
		JPatchRadioButtonMenuItem menuItem4 = new JPatchRadioButtonMenuItem("item 4");
		menuItem1.setModel(new JPatchToggleButtonModel(buttonModel1));
		menuItem2.setModel(new JPatchToggleButtonModel(buttonModel2));
		menuItem3.setModel(new JPatchToggleButtonModel(buttonModel3));
		menuItem4.setModel(new JPatchToggleButtonModel(buttonModel4));
		menu.add(menuItem1);
		menu.add(menuItem2);
		menu.add(menuItem3);
		menu.add(menuItem4);
//		menuItem1.setAccelerator(KeyStroke.getKeyStroke("typed a"));
//		menuItem1.getInputMap().put(KeyStroke.getKeyStroke('a'), "doClick");
//		menuItem2.setAccelerator(KeyStroke.getKeyStroke("shift A"));
//		menuItem3.setAccelerator(KeyStroke.getKeyStroke("control A"));
		menuItem4.setAccelerator(KeyStroke.getKeyStroke("ENTER"));
		menuItem1.setToolTipText("1st. Tool Tip Text");
		menuItem2.setToolTipText("2nd.Tool Tip Text");
		menuItem3.setToolTipText("3rd.Tool Tip Text");
		menuItem4.setToolTipText("4th.Tool Tip Text");
		
		button1b.setToolTipText("<html>ToolTip <font face='Dialog' style='color: blue; font-size: 80%;'>SHIFT-X</font></html>");
//		SwingUtilities.replaceUIInputMap(menuItem1, JComponent.WHEN_IN_FOCUSED_WINDOW, null);
//		SwingUtilities.replaceUIActionMap(menuItem1, null);
//		SwingUtilities.replaceUIInputMap(menuItem2, JComponent.WHEN_IN_FOCUSED_WINDOW, null);
//		SwingUtilities.replaceUIActionMap(menuItem2, null);
//		SwingUtilities.replaceUIInputMap(menuItem3, JComponent.WHEN_IN_FOCUSED_WINDOW, null);
//		SwingUtilities.replaceUIActionMap(menuItem3, null);
//		SwingUtilities.replaceUIInputMap(menuItem4, JComponent.WHEN_IN_FOCUSED_WINDOW, null);
//		SwingUtilities.replaceUIActionMap(menuItem4, null);
//		menuItem2.setAccelerator(KeyStroke.getKeyStroke('A'));
//		menuItem3.setAccelerator(KeyStroke.getKeyStroke("pressed A"));
//		menuItem4.setAccelerator(KeyStroke.getKeyStroke(' '));
		menuBar.add(menu);
		
		JPanel panel = new JPanel();
		panel.add(new JTextField(30));
		panel.add(new JTextField(30));
		panel.add(new JTextField(30));
		panel.add(new JTextField(30));
		
		final JPanel panel2 = new JPanel() {
			
		};
		panel2.setPreferredSize(new Dimension(300, 300));
		panel2.setFocusable(true);
		panel2.setBackground(Color.BLACK);
		panel2.addFocusListener(new FocusListener() {
			private Border border1 = BorderFactory.createLineBorder(Color.GRAY);
			private Border border2 = BorderFactory.createLineBorder(Color.YELLOW);
			public void focusGained(FocusEvent e) {
				panel2.setBorder(border2);
			}

			public void focusLost(FocusEvent e) {
				panel2.setBorder(border1);
			}
		});

		panel.add(panel2);
		
		frame.add(toolBar1, BorderLayout.NORTH);
		frame.add(toolBar2, BorderLayout.WEST);
		frame.add(panel, BorderLayout.CENTER);
		frame.setJMenuBar(menuBar);
		
		frame.setSize(800, 600);
		frame.setVisible(true);
		System.out.println(UIManager.getFont("MenuItem.acceleratorFont"));
		new JFileChooser().showSaveDialog(frame);
//		KeyEventDispatcher keyEventDispatcher = new KeyEventDispatcher() {
//			public boolean dispatchKeyEvent(KeyEvent e) {
//				//System.out.println(e);
////				Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
////				if (focusOwner instanceof JTextComponent)
////					return false;
////				switch (e.getID()) {
////				case KeyEvent.KEY_PRESSED:
////					return true;
//////		          break;
////		          case KeyEvent.KEY_RELEASED:
////		        	  return true;
//////		          break;
////		          case KeyEvent.KEY_TYPED:
////		        	  return false;
//////		          break;
////				}
//				System.out.println(e);
//				if (e.getID() == KeyEvent.KEY_PRESSED) {
//					if (e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
////						System.out.println("KEY_PRESSED, char defiend, sending KEY_TYPED...");
//						KeyEvent keyTypedEvent = new KeyEvent((Component) e.getSource(), KeyEvent.KEY_TYPED, e.getWhen(), 0, 0, e.getKeyChar());
////						System.out.println(keyTypedEvent);
//						KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(e.getComponent(), keyTypedEvent);
//						if (!keyTypedEvent.isConsumed()) {
//							System.out.println("Not consumed, sending original KEY_PRESSED...");
//							KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(e.getComponent(), e);
//						}
//						
//					} else {
//						KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(e.getComponent(), e);
//					}
////					System.out.println(e.isConsumed() + " " + keyTypedEvent.isConsumed());
//				}
//				System.out.println();
//				return true;
//			}
//		};
		
		
//		JPatchKeyEventPostProcessor kepp = new JPatchKeyEventPostProcessor();
//		kepp.addKeyBinding(KeyStroke.getKeyStroke("pressed A"), actionListener1);
//		kepp.addKeyBinding(KeyStroke.getKeyStroke("shift A"), actionListener2);
//		kepp.addKeyBinding(KeyStroke.getKeyStroke("control A"), actionListener3);
//		kepp.addKeyBinding(KeyStroke.getKeyStroke("control shift A"), actionListener4);
//		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(JPatchKeyEventPostProcessor.getInstance());
		
	}
	
	static class MyActionListener implements ActionListener {
		String name;
		ButtonModel buttonModel;
		
		MyActionListener(String name, ButtonModel buttonModel) {
			this.name = name;
			this.buttonModel = buttonModel;
		}
		
		public void actionPerformed(ActionEvent e) {
			System.out.println("action performed: " + e);
			buttonModel.setSelected(true);
		}
	}
}
