/*
 * $Id: Command.java,v 1.2 2005/11/03 20:58:50 sascha_l Exp $
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jpatch.boundary.action;

/**
 * @author sascha
 */

import java.awt.ItemSelectable;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import sun.security.action.OpenFileInputStreamAction;

import jpatch.boundary.*;
import jpatch.boundary.laf.SmoothLookAndFeel;
import jpatch.boundary.mouse.RotateViewMotionListener;

public final class Command implements KeyListener {
	private static final boolean DEBUG = true;
	private static final Command INSTANCE = new Command();
	private Map commandActionMap = new HashMap();
	private Map commandButtonMap = new HashMap();
	private Map commandMenuItemMap = new HashMap();
	private Map keyCommandMap = new HashMap();
	
	public static Command getInstance() {
		return INSTANCE;
	}
	
	public static AbstractButton getButtonFor(String command) {
		AbstractButton button = (AbstractButton) INSTANCE.commandButtonMap.get(command);
		AbstractButton newButton;
		if (button instanceof JPatchToggleButton)
			newButton = new JPatchToggleButton();//button.getAction());
		else
			newButton = new JPatchButton();//button.getAction());
		newButton.setModel(button.getModel());
		newButton.setText(button.getText());
		newButton.setIcon(button.getIcon());
		newButton.setSelectedIcon(button.getSelectedIcon());
		return newButton;
	}
	
	public static JMenuItem getMenuItemFor(String command) {
		JMenuItem menuItem = (JMenuItem) INSTANCE.commandMenuItemMap.get(command);
		JMenuItem newItem;
		if (menuItem instanceof JRadioButtonMenuItem)
			newItem = new JRadioButtonMenuItem();//menuItem.getAction());
		else if (menuItem instanceof JCheckBoxMenuItem)
			newItem = new JCheckBoxMenuItem();//menuItem.getAction());
		else
			newItem = new JMenuItem();//menuItem.getAction());
		newItem.setText(menuItem.getText());
		newItem.setIcon(menuItem.getIcon());
		newItem.setModel(menuItem.getModel());
		return newItem;
//		return (JMenuItem) INSTANCE.commandMenuItemMap.get(command);
	}
	
	public static Action getActionFor(String command) {
		return (Action) INSTANCE.commandActionMap.get(command);
	}
	
	public static void setViewDefinition(ViewDefinition viewDef) {
		((JMenuItem) INSTANCE.commandMenuItemMap.get(viewDef.getViewName())).setSelected(true);
		((JMenuItem) INSTANCE.commandMenuItemMap.get("show points")).setSelected(viewDef.renderPoints());
		((JMenuItem) INSTANCE.commandMenuItemMap.get("show curves")).setSelected(viewDef.renderCurves());
		((JMenuItem) INSTANCE.commandMenuItemMap.get("show patches")).setSelected(viewDef.renderPatches());
		((JMenuItem) INSTANCE.commandMenuItemMap.get("show rotoscope")).setSelected(viewDef.showRotoscope());
	}
	
	public Command() {
		LookAndFeel jpatch = null, crossplatform = null, system = null;
		try {
			jpatch = new SmoothLookAndFeel();
			crossplatform = (LookAndFeel) Class.forName(UIManager.getCrossPlatformLookAndFeelClassName()).newInstance();
			system = (LookAndFeel) Class.forName(UIManager.getSystemLookAndFeelClassName()).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/*
		 * Main toolbar buttons
		 */
		put("new",						new NewAction(), 				new JMenuItem(),			new JPatchButton());
		put("open",						new ImportJPatchAction(), 		new JMenuItem(),			new JPatchButton());
		put("save",						new SaveAsAction(false), 		new JMenuItem(),			new JPatchButton());
		put("single view",				new ViewSingleAction(), 		new JRadioButtonMenuItem(),	new JPatchToggleButton());
		put("horizontally split view",	new ViewSplitHorizontalAction(),new JRadioButtonMenuItem(),	new JPatchToggleButton());
		put("vertically split view",	new ViewSplitVerticalAction(), 	new JRadioButtonMenuItem(),	new JPatchToggleButton());
		put("quad view",				new ViewQuadAction(), 			new JRadioButtonMenuItem(),	new JPatchToggleButton());
		put("rotate view",				new ViewRotateAction(), 		new JRadioButtonMenuItem(),	new JPatchToggleButton());
		put("move view",				new ViewMoveAction(), 			new JRadioButtonMenuItem(),	new JPatchToggleButton());
		put("zoom view",				new ViewZoomAction(), 			new JRadioButtonMenuItem(),	new JPatchToggleButton());
		put("zoom to fit",				new ZoomToFitAction(), 			new JMenuItem(),			new JPatchButton());
		put("undo",						new UndoAction(), 				new JMenuItem(),			new JPatchButton());
		put("redo",						new RedoAction(), 				new JMenuItem(),			new JPatchButton());
		put("lock x",					new XLockAction(), 				new JCheckBoxMenuItem(),	new JPatchToggleButton());
		put("lock y",					new YLockAction(), 				new JCheckBoxMenuItem(),	new JPatchToggleButton());
		put("lock z",					new ZLockAction(), 				new JCheckBoxMenuItem(),	new JPatchToggleButton());
		put("snap to grid",				new GridSnapAction(), 			new JCheckBoxMenuItem(),	new JPatchToggleButton());
		put("hide",						new HideAction(), 				new JCheckBoxMenuItem(),	new JPatchToggleButton());

		/*
		 * Edit toolbar buttons
		 */
		put("default tool",				new SelectMoveAction(), 		new JRadioButtonMenuItem(),	new JPatchToggleButton());
		put("add curve segment",		new AddControlPointAction(), 	new JRadioButtonMenuItem(),	new JPatchToggleButton());
		put("add bone",					new AddBoneAction(), 			new JRadioButtonMenuItem(),	new JPatchToggleButton());
		put("rotate tool",				new RotateAction(), 			new JRadioButtonMenuItem(),	new JPatchToggleButton());
		put("weight selection tool",	new WeightSelectionAction(), 	new JRadioButtonMenuItem(),	new JPatchToggleButton());
		put("rotoscope tool",			new RotoscopeAction(), 			new JRadioButtonMenuItem(),	new JPatchToggleButton());
		put("tangent tool",				new TangentAction(), 			new JCheckBoxMenuItem(),	new JPatchToggleButton());
		put("peak tangents",			new PeakAction(), 				new JCheckBoxMenuItem(),	new JPatchToggleButton());
		put("round tangents",			new RoundAction(), 				new JCheckBoxMenuItem(),	new JPatchToggleButton());
		put("clone",					new CloneAction(), 				new JMenuItem(),			new JPatchButton());
		put("extrude",					new ExtrudeAction(), 			new JMenuItem(),			new JPatchButton());
		put("lathe",					new LatheAction(), 				new JMenuItem(),			new JPatchButton());
		put("lathe editor",				new LatheEditorAction(), 		new JMenuItem(),			new JPatchButton());
		put("make patch",				new MakeFivePointPatchAction(), new JMenuItem(),			new JPatchButton());
		put("compute patches",			new ComputePatchesAction(), 	new JMenuItem(),			new JPatchButton());
		
		/*
		 * Main menu commands
		 */
		
		// File
		put("append",					new ImportJPatchAction(false),		new JMenuItem());
		put("save as",					new SaveAsAction(true),				new JMenuItem());
		put("import spatch",			new ImportSPatchAction(),			new JMenuItem());
		put("import animationmaster",	new ImportAnimationMasterAction(),	new JMenuItem());
		put("export aliaswavefront",	new ExportWavefrontAction(),		new JMenuItem());
		put("export povray",			new ExportPovrayAction(),			new JMenuItem());
		put("export renderman",			new ExportRibAction(),				new JMenuItem());
		put("quit",						new QuitAction(),					new JMenuItem());
		
		// Options
		put("synchronize viewports", 		new SyncScreensAction(),		new JCheckBoxMenuItem());
		put("colors settings", 				new ColorPreferencesAction(),	new JMenuItem());
		put("grid spacing settings", 		new SetGridSpacingAction(),		new JMenuItem());
		put("realtime renderer settings",	new ZBufferQualityAction(),		new JMenuItem());
		put("install jogl", 				new InstallJoglAction(),		new JMenuItem());
		put("jpatch lookandfeel",			new SwitchLookAndFeelAction("JPatch", jpatch), 			new JRadioButtonMenuItem());
		put("crossplatform lookandfeel",	new SwitchLookAndFeelAction("Metal", crossplatform),	new JRadioButtonMenuItem());
		put("system lookandfeel",			new SwitchLookAndFeelAction("System", system),			new JRadioButtonMenuItem());
		put("phoneme morph mapping", 		new EditPhonemesAction(),		new JMenuItem());
		
		// Test
		put("dump",						new DumpAction(),					new JMenuItem());
		put("dump undo stack",			new DumpUndoStackAction(),			new JMenuItem());
		put("check model",				new CheckModelAction(),				new JMenuItem());
		put("controlpoint browser",		new ControlPointBrowserAction(),	new JCheckBoxMenuItem());
		
		// Help
		put("show about",				new AboutAction(),					new JMenuItem());
		put("show splashscreen",		new ShowSplashAction(),				new JMenuItem());
		
		/*
		 * Popup menu commands
		 */
		
		// Show
		put("show points",				new ShowPointsAction(),				new JCheckBoxMenuItem());
		put("show curves",				new ShowCurvesAction(),				new JCheckBoxMenuItem());
		put("show patches",				new ShowPatchesAction(),			new JCheckBoxMenuItem());
		put("show rotoscope",			new ShowRotoscopeAction(),			new JCheckBoxMenuItem());
		
		// View
		put("front view",				new ViewAction(ViewDefinition.FRONT),		new JRadioButtonMenuItem());
		put("rear view",				new ViewAction(ViewDefinition.REAR),		new JRadioButtonMenuItem());
		put("top view",					new ViewAction(ViewDefinition.TOP),			new JRadioButtonMenuItem());
		put("bottom view",				new ViewAction(ViewDefinition.BOTTOM),		new JRadioButtonMenuItem());
		put("left view",				new ViewAction(ViewDefinition.LEFT),		new JRadioButtonMenuItem());
		put("right view",				new ViewAction(ViewDefinition.RIGHT),		new JRadioButtonMenuItem());
		put("bird's eye view",			new ViewAction(ViewDefinition.BIRDS_EYE),	new JRadioButtonMenuItem());
		
		// Rotoscope
		put("set rotoscope image",		new SetRotoscopeAction(),	new JMenuItem());
		put("clear rotoscope image",	new ClearRotoscopeAction(),	new JMenuItem());
		
		/*
		 * Pressed icons
		 */
		((AbstractButton) commandButtonMap.get("lock x")).setSelectedIcon(new ImageIcon(ClassLoader.getSystemResource("jpatch/images/xlocked.png")));
		((AbstractButton) commandButtonMap.get("lock y")).setSelectedIcon(new ImageIcon(ClassLoader.getSystemResource("jpatch/images/ylocked.png")));
		((AbstractButton) commandButtonMap.get("lock z")).setSelectedIcon(new ImageIcon(ClassLoader.getSystemResource("jpatch/images/zlocked.png")));
		((AbstractButton) commandButtonMap.get("snap to grid")).setSelectedIcon(new ImageIcon(ClassLoader.getSystemResource("jpatch/images/grid_snap.png")));
		((AbstractButton) commandButtonMap.get("hide")).setSelectedIcon(new ImageIcon(ClassLoader.getSystemResource("jpatch/images/hide2.png")));
		
		/*
		 * ButtonGroups
		 */
		createGroup(new String[] {
				"default tool",
				"add curve segment",
				"add bone",
				"weight selection tool",
				"rotate tool",
				"rotoscope tool",
				"rotate view",
				"move view",
				"zoom view"
		}, 0);
		
		createGroup(new String[] {
				"single view",
				"horizontally split view",
				"vertically split view",
				"quad view"
		}, JPatchSettings.getInstance().iScreenMode - 1);
		
		createGroup(new String[] {
				"front view",
				"rear view",
				"top view",
				"bottom view",
				"left view",
				"right view",
				"bird's eye view"
		}, 0);
	}

	public void executeCommand(String command) {
		AbstractButton button = (AbstractButton) commandButtonMap.get(command);
		if (button != null) {
			button.doClick();
			return;
		}
		Action action = (Action) commandActionMap.get(command);
		if (action != null) {
			action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, (String) action.getValue(Action.ACTION_COMMAND_KEY))); 
		}
	}
	
	/*
	 * KeyListener interface implementation start
	 */
	
	public void keyTyped(KeyEvent e) {
		String command = (String) keyCommandMap.get(KeyStroke.getKeyStrokeForEvent(e));
		if (DEBUG)
			System.out.println(KeyStroke.getKeyStrokeForEvent(e) + ": Command = " + command);
	}


	public void keyPressed(KeyEvent e) {
		String command = (String) keyCommandMap.get(KeyStroke.getKeyStrokeForEvent(e));
		if (DEBUG)
			System.out.println(KeyStroke.getKeyStrokeForEvent(e) + ": Command = " + command);
		if (command != null) {
			executeCommand(command);
		}
	}

	public void keyReleased(KeyEvent e) { }
	
	/*
	 * KeyListener interface implementation end
	 */
	
	private void put(String command, Action action, JMenuItem menuItem) {
		put(command, action, menuItem, null);
	}
	
	private void put(String command, Action action, JMenuItem menuItem, AbstractButton button) {
		commandActionMap.put(command, action);
		if (button != null) {
//			button.setAction(action);
//			button.setText((String) action.getValue(Action.SHORT_DESCRIPTION));
			button.setIcon((Icon) action.getValue(Action.SMALL_ICON));
			button.setModel(menuItem.getModel());
			commandButtonMap.put(command, button);
		}
		if (menuItem != null) {
			menuItem.setAction(action);
			commandMenuItemMap.put(command, menuItem);
			menuItem.setText((String) action.getValue(Action.SHORT_DESCRIPTION));
			if (menuItem.getText() == null)
				menuItem.setText(command);
		}
		String keyString = (String) JPatchSettings.getInstance().commandKeyMap.get(command);
		if (keyString != null)
			keyCommandMap.put(KeyStroke.getKeyStroke(keyString), command);
	}
	
	private void createGroup(String[] commands, int selectedIndex) {
		ButtonGroup items = new ButtonGroup();
		for (int i = 0; i < commands.length; i++)
			items.add((JMenuItem) commandMenuItemMap.get(commands[i]));
		items.setSelected(((JMenuItem) commandMenuItemMap.get(commands[selectedIndex])).getModel(), true);
	}
}
