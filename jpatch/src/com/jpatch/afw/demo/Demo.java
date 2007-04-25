package com.jpatch.afw.demo;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import com.jpatch.afw.attributes.StateMachine;
import com.jpatch.afw.attributes.Toggle;
import com.jpatch.afw.control.*;
import com.jpatch.afw.ui.JPatchCheckBoxMenuItem;
import com.jpatch.afw.ui.JPatchMenu;
import com.jpatch.afw.ui.JPatchMenuItem;
import com.jpatch.afw.ui.JPatchRadioButtonMenuItem;

public class Demo {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		System.setProperty("swing.boldMetal", "false");
		System.setProperty("swing.aatext", "true");
		System.setProperty("apple.laf.useScreenMenuBar", "true");
//		UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		new Demo();
	}
	
	public Demo() {
		Configuration.getInstance().put("locale", Locale.GERMAN);
		Configuration.getInstance().put("stringResource", "com/jpatch/afw/demo/Strings");
		StateMachine<Integer> stateMachine = new StateMachine<Integer>(new Integer[] { 1, 2, 3 }, 1) {
			@Override
			protected boolean performStateTransition(Integer newState) {
				System.out.println("Switching to state " + newState);
				return true;
			}
		};
		
		JPatchAction openAction = new JPatchAction(null, "OPEN") {
			public void actionPerformed(ActionEvent e) {
				System.out.println("open");
			}
		};
		JPatchAction saveAction = new JPatchAction(null, "SAVE") {
			public void actionPerformed(ActionEvent e) {
				System.out.println("save");
			}
		};
		
		Toggle[] toggles = new Toggle[] {
				new Toggle(),
				new Toggle()
		};
		
		ToggleAction[] ta = new ToggleAction[] {
				new ToggleAction(toggles[0], null, "TOGGLE_1"),
				new ToggleAction(toggles[1], null, "TOGGLE_2")
		};
		
		SwitchStateAction[] ssa = new SwitchStateAction[] {
				new SwitchStateAction(stateMachine, 1, null, "STATE_1"),
				new SwitchStateAction(stateMachine, 2, null, "STATE_2"),
				new SwitchStateAction(stateMachine, 3, null, "STATE_3")
		};
		
		openAction.setKeyboardShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		ssa[0].setKeyboardShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.SHIFT_DOWN_MASK));
		ssa[1].setKeyboardShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, KeyEvent.ALT_DOWN_MASK));
		ssa[2].setKeyboardShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.META_DOWN_MASK));
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		JPatchMenu fileMenu = new JPatchMenu("FILE");
		JPatchMenu stateMenu = new JPatchMenu("STATES");
		fileMenu.add(new JPatchMenuItem(openAction));
		fileMenu.add(new JPatchMenuItem(saveAction));
		stateMenu.add(new JPatchCheckBoxMenuItem(ta[0]));
		stateMenu.add(new JPatchCheckBoxMenuItem(ta[1]));
		stateMenu.add(new JSeparator());
		stateMenu.add(new JPatchRadioButtonMenuItem(ssa[0]));
		stateMenu.add(new JPatchRadioButtonMenuItem(ssa[1]));
		stateMenu.add(new JPatchRadioButtonMenuItem(ssa[2]));
		fileMenu.add(new JSeparator());
		fileMenu.add(stateMenu);
		menuBar.add(fileMenu);
		frame.setTitle("JPatch Application Framework");
		frame.setSize(640, 480);
		frame.setVisible(true);
	}

}
