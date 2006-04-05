/*
 * $Id: Command.java,v 1.30 2006/04/05 15:29:10 sascha_l Exp $
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

import java.awt.CheckboxMenuItem;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import jpatch.boundary.*;
import jpatch.boundary.laf.*;
import jpatch.boundary.settings.*;
import jpatch.boundary.ui.*;


public final class Command {
//	private static final boolean DEBUG = false;
	private static final Command INSTANCE = new Command();
	private Map<String, Action> commandActionMap = new HashMap<String, Action>();
	private Map<String, AbstractButton> commandButtonMap = new HashMap<String, AbstractButton>();
	private Map<String, JMenuItem> commandMenuItemMap = new HashMap<String, JMenuItem>();
	private Map<String, KeyStroke> commandKeyMap = new HashMap<String, KeyStroke>();
//	private Map keyCommandMap = new HashMap();
	private Map<KeyStroke, String> keyCommandMap = new HashMap<KeyStroke, String>();
//	private ActionMap actionMap = new ActionMap();
	
	private static Command getInstance() {
		return INSTANCE;
	}
	
	private static AbstractButton getButtonFor(String command) {
		AbstractButton button = (AbstractButton) INSTANCE.commandButtonMap.get(command);
		if (true)
			return button;
		AbstractButton newButton;
		if (button instanceof JPatchToggleButton)
			newButton = new JPatchToggleButton();//button.getAction());
		else
			newButton = new JPatchButton();//button.getAction());
		newButton.setModel(button.getModel());
		newButton.setText(button.getText());
		newButton.setIcon(button.getIcon());
		newButton.setSelectedIcon(button.getSelectedIcon());
		String toolTipText = (String) ((Action) INSTANCE.commandActionMap.get(command)).getValue(Action.SHORT_DESCRIPTION);
		if (toolTipText == null)
			toolTipText = command;
		KeyStroke keyStroke = INSTANCE.commandKeyMap.get(command);
		if (keyStroke != null) {
			String acceleratorText = "";
	        if (keyStroke != null) {
	        	String ks = keyStroke.toString();
	        	ks = ks.replaceAll("(typed|released|pressed)", "").trim();
	        	if (ks.matches("[A-Z]"))
	        		ks = "shift " + ks;
	        	else if (ks.matches("[a-z]"))
	        		ks = ks.toUpperCase();
	        	KeyStroke accelerator = KeyStroke.getKeyStroke(ks);
	        	
	            int modifiers = accelerator.getModifiers();
	            if (modifiers > 0) {
	                acceleratorText = KeyEvent.getKeyModifiersText(modifiers);
	                acceleratorText += "-";
//	                acceleratorText += acceleratorDelimiter;
	            }

	            int keyCode = accelerator.getKeyCode();
	            if (keyCode != 0) {
	                acceleratorText += KeyEvent.getKeyText(keyCode);
	            } else {
	                acceleratorText += accelerator.getKeyChar();
	            }
	        }
	        toolTipText = "<html>&nbsp;" + toolTipText + "&nbsp;&nbsp;&nbsp;<font color='gray'>" + acceleratorText + "</font>&nbsp;</html>";
			
		}
		newButton.setToolTipText(toolTipText);
		return newButton;
	}
	
	private static JMenuItem getMenuItemFor(String command) {
		JMenuItem menuItem = INSTANCE.commandMenuItemMap.get(command);
		JMenuItem newItem;
		if (menuItem instanceof JRadioButtonMenuItem)
			newItem = new JRadioButtonMenuItem();//menuItem.getAction());
		else if (menuItem instanceof JCheckBoxMenuItem)
			newItem = new JCheckBoxMenuItem();//menuItem.getAction());
		else
			newItem = new JMenuItem();//menuItem.getAction());
		String itemText = menuItem.getText();
		KeyStroke keyStroke = INSTANCE.commandKeyMap.get(command);
		if (keyStroke != null) {
			//String key = keyStroke.toString().replaceAll("(typed|pressed|released)", "");
			//itemText = itemText + " [" + key + "]";
			String ks = keyStroke.toString();
        	ks = ks.replaceAll("(typed|released|pressed)", "").trim();
        	if (ks.matches("[A-Z]"))
        		ks = "shift " + ks;
        	else if (ks.matches("[a-z]"))
        		ks = ks.toUpperCase();
        	KeyStroke accelerator = KeyStroke.getKeyStroke(ks);
			newItem.setAccelerator(accelerator);
		}
		newItem.setText(itemText);
		newItem.setIcon(menuItem.getIcon());
		newItem.setModel(menuItem.getModel());
		return newItem;
//		return (JMenuItem) INSTANCE.commandMenuItemMap.get(command);
	}
	
	private static Action getActionFor(String command) {
		return (Action) INSTANCE.commandActionMap.get(command);
	}
	
	private static void setViewDefinition(ViewDefinition viewDef) {
//		((JMenuItem) INSTANCE.commandMenuItemMap.get(viewDef.getViewName())).setSelected(true);
		((JMenuItem) INSTANCE.commandMenuItemMap.get("show points")).setSelected(viewDef.renderPoints());
		((JMenuItem) INSTANCE.commandMenuItemMap.get("show curves")).setSelected(viewDef.renderCurves());
		((JMenuItem) INSTANCE.commandMenuItemMap.get("show patches")).setSelected(viewDef.renderPatches());
		((JMenuItem) INSTANCE.commandMenuItemMap.get("show rotoscope")).setSelected(viewDef.showRotoscope());
		((JMenuItem) INSTANCE.commandMenuItemMap.get("lock view")).setSelected(viewDef.isLocked());
		INSTANCE.enableCommand("unlock view", viewDef.isLocked());
		INSTANCE.enableCommand("show patches", viewDef.getDrawable().isShadingSupported());
		INSTANCE.enableCommand("clear rotoscope image", MainFrame.getInstance().getModel() != null && MainFrame.getInstance().getModel().getRotoscope(viewDef.getView()) != null);
	}
	
	private Command() {
		LookAndFeel jpatch = null, crossplatform = null, system = null;
		try {
			jpatch = new SmoothLookAndFeel();
			crossplatform = (LookAndFeel) Class.forName(UIManager.getCrossPlatformLookAndFeelClassName()).newInstance();
			system = (LookAndFeel) Class.forName(UIManager.getSystemLookAndFeelClassName()).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/*
		 * Keyboard only
		 */
		put("delete",		new DeleteControlPointAction(),	new JMenuItem());
		put("remove",		new RemoveControlPointAction(),	new JMenuItem());
		put("insert point",	new InsertControlPointAction(),	new JMenuItem());
		put("next curve",	new NextCurveAction(1), new JMenuItem());
		put("prev curve",	new NextCurveAction(-1), new JMenuItem());
		
		/*
		 * Main toolbar buttons
		 */
		put("new model",				new NewModelAction(), 			new JMenuItem(),			new JPatchButton());
		put("new animation",			new NewAnimAction(), 			new JMenuItem(),			new JPatchButton());
		put("open",						new ImportJPatchAction(), 		new JMenuItem(),			new JPatchButton());
		put("save",						new SaveAsAction(false), 		new JMenuItem(),			new JPatchButton());
		put("single view",				new ViewSingleAction(), 		new JRadioButtonMenuItem(),	new JPatchToggleButton());
		put("horizontally split view",	new ViewSplitHorizontalAction(),new JRadioButtonMenuItem(),	new JPatchToggleButton());
		put("vertically split view",	new ViewSplitVerticalAction(), 	new JRadioButtonMenuItem(),	new JPatchToggleButton());
		put("quad view",				new ViewQuadAction(), 			new JRadioButtonMenuItem(),	new JPatchToggleButton());
		put("rotate view",				new ViewRotateAction(), 		new JRadioButtonMenuItem(),	new LockingToggleButton());
		put("move view",				new ViewMoveAction(), 			new JRadioButtonMenuItem(),	new LockingToggleButton());
		put("zoom view",				new ViewZoomAction(), 			new JRadioButtonMenuItem(),	new LockingToggleButton());
		put("zoom to fit",				new ZoomToFitAction(), 			new JMenuItem(),			new JPatchButton());
		put("undo",						new UndoAction(), 				new JMenuItem(),			new JPatchButton());
		put("redo",						new RedoAction(), 				new JMenuItem(),			new JPatchButton());
		put("lock x",					new XLockAction(), 				new JCheckBoxMenuItem(),	new JPatchToggleButton());
		put("lock y",					new YLockAction(), 				new JCheckBoxMenuItem(),	new JPatchToggleButton());
		put("lock z",					new ZLockAction(), 				new JCheckBoxMenuItem(),	new JPatchToggleButton());
		put("snap to grid",				new GridSnapAction(), 			new JCheckBoxMenuItem(),	new JPatchToggleButton());
		put("hide",						new HideAction(), 				new JCheckBoxMenuItem(),	new JPatchToggleButton());
		put("stop edit morph",			new StopEditMorphAction(),		new JMenuItem(),			new JPatchButton());
		put("select points",			new SelectPointsAction(),		new JCheckBoxMenuItem(),	new JPatchToggleButton());
		put("select bones",				new SelectBonesAction(),		new JCheckBoxMenuItem(),	new JPatchToggleButton());
		put("lock points",				new LockPointsAction(),			new JCheckBoxMenuItem(),	new JPatchToggleButton());
		put("lock bones",				new LockBonesAction(),			new JCheckBoxMenuItem(),	new JPatchToggleButton());
		
		/*
		 * Edit toolbar buttons
		 */
		put("default tool",				new SelectMoveAction(), 		new JRadioButtonMenuItem(),	new JPatchToggleButton());
		put("add curve segment",		new AddControlPointAction(), 	new JRadioButtonMenuItem(),	new LockingToggleButton());
		put("add bone",					new AddBoneAction(), 			new JRadioButtonMenuItem(),	new LockingToggleButton());
		put("rotate tool",				new RotateAction(), 			new JRadioButtonMenuItem(),	new JPatchToggleButton());
		put("weight selection tool",	new WeightSelectionAction(), 	new JRadioButtonMenuItem(),	new JPatchToggleButton());
		put("knife tool",				new KnifeAction(),				new JRadioButtonMenuItem(),	new JPatchToggleButton());
		put("detach",					new DetachControlPointsAction(),new JMenuItem(),			new JPatchButton());
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
		put("open animation",			new ImportJPatchAnimationAction(),	new JMenuItem());
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
		put("settings", 					new EditSettingsAction(),			new JMenuItem());
		put("grid spacing settings", 		new SetGridSpacingAction(),		new JMenuItem());
		put("install jogl", 				new InstallJoglAction(),		new JMenuItem());
//		put("jpatch lookandfeel",			new SwitchLookAndFeelAction("JPatch", jpatch), 			new JRadioButtonMenuItem());
//		put("crossplatform lookandfeel",	new SwitchLookAndFeelAction("Metal", crossplatform),	new JRadioButtonMenuItem());
//		put("system lookandfeel",			new SwitchLookAndFeelAction("System", system),			new JRadioButtonMenuItem());
		put("phoneme morph mapping", 		new EditPhonemesAction(),								new JMenuItem());
		
		// Window
		put("show anim controls", 		new AnimControlsAction(),			new JMenuItem());
		
		// Test
		put("dump",						new DumpAction(),					new JMenuItem());
		put("dump xml",					new XmlDumpAction(),				new JMenuItem());
		put("dump undo stack",			new DumpUndoStackAction(),			new JMenuItem());
		put("check model",				new CheckModelAction(),				new JMenuItem());
		put("controlpoint browser",		new ControlPointBrowserAction(),	new JMenuItem());
		put("show reference",			new ShowReferenceAction(),			new JCheckBoxMenuItem());
		
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
		put("set rotoscope image",		new SetRotoscopeAction(),		new JMenuItem());
		put("clear rotoscope image",	new ClearRotoscopeAction(),		new JMenuItem());
		
		// Lock view
		put("lock view",				new SetViewLockAction(true),	new JCheckBoxMenuItem());
		put("unlock view",				new SetViewLockAction(false),	new JMenuItem());
		
		// Selection
		put("select none",				new SelectNoneAction(),			new JMenuItem());
		put("select all",				new SelectAllAction(),			new JMenuItem());
		put("invert selection",			new InvertSelectionAction(),	new JMenuItem());
		put("expand selection",			new ExtendSelectionAction(),	new JMenuItem());
		
		// Tools
		put("flip x",							new FlipAction(FlipAction.X),									new JMenuItem());
		put("flip y",							new FlipAction(FlipAction.Y),									new JMenuItem());
		put("flip z",							new FlipAction(FlipAction.Z),									new JMenuItem());
		put("flip patches",						new FlipPatchesAction(),										new JMenuItem());
		put("align patches",					new AlignPatchesAction(),										new JMenuItem());
		put("align controlpoints",				new AlignAction(),												new JMenuItem());
		put("automirror",						new AutoMirrorAction(),											new JMenuItem());
		put("add stubs",						new AddStubsAction(),											new JMenuItem());
		put("remove stubs",						new RemoveStubsAction(),										new JMenuItem());
		put("change tangents: round",			new ChangeTangentModeAction(ChangeTangentModeAction.JPATCH),	new JMenuItem());
		put("change tangents: peak",			new ChangeTangentModeAction(ChangeTangentModeAction.PEAK),		new JMenuItem());
		put("change tangents: spatch",			new ChangeTangentModeAction(ChangeTangentModeAction.SPATCH),	new JMenuItem());
		put("assign controlpoints to bones",	new AssignPointsToBonesAction(),								new JMenuItem());
		
		/*
		 * Pressed icons
		 */
		((AbstractButton) commandButtonMap.get("lock x")).setSelectedIcon(new ImageIcon(ClassLoader.getSystemResource("jpatch/images/xlocked.png")));
		((AbstractButton) commandButtonMap.get("lock y")).setSelectedIcon(new ImageIcon(ClassLoader.getSystemResource("jpatch/images/ylocked.png")));
		((AbstractButton) commandButtonMap.get("lock z")).setSelectedIcon(new ImageIcon(ClassLoader.getSystemResource("jpatch/images/zlocked.png")));
		((AbstractButton) commandButtonMap.get("snap to grid")).setSelectedIcon(new ImageIcon(ClassLoader.getSystemResource("jpatch/images/grid_snap.png")));
		((AbstractButton) commandButtonMap.get("hide")).setSelectedIcon(new ImageIcon(ClassLoader.getSystemResource("jpatch/images/hide2.png")));
		((AbstractButton) commandButtonMap.get("select points")).setSelectedIcon(new ImageIcon(ClassLoader.getSystemResource("jpatch/images/cp_selected.png")));
		((AbstractButton) commandButtonMap.get("select bones")).setSelectedIcon(new ImageIcon(ClassLoader.getSystemResource("jpatch/images/bone_selected.png")));
		((AbstractButton) commandButtonMap.get("lock points")).setSelectedIcon(new ImageIcon(ClassLoader.getSystemResource("jpatch/images/cp_locked.png")));
		((AbstractButton) commandButtonMap.get("lock bones")).setSelectedIcon(new ImageIcon(ClassLoader.getSystemResource("jpatch/images/bone_locked.png")));	
		
//		((LockingToggleButton) commandButtonMap.get("add curve segment")).createLockedIcons(ImageIconFactory.Position.BOTTOM_RIGHT);
//		((LockingToggleButton) commandButtonMap.get("add bone")).createLockedIcons(ImageIconFactory.Position.BOTTOM_RIGHT);
//		((LockingToggleButton) commandButtonMap.get("move view")).createLockedIcons(ImageIconFactory.Position.BOTTOM_RIGHT);
//		((LockingToggleButton) commandButtonMap.get("zoom view")).createLockedIcons(ImageIconFactory.Position.BOTTOM_RIGHT);
//		((LockingToggleButton) commandButtonMap.get("rotate view")).createLockedIcons(ImageIconFactory.Position.BOTTOM_RIGHT);
		
		/*
		 * ButtonGroups
		 */
		LockingButtonGroup toolButtonGroup = new LockingButtonGroup();
		addToButtonGroup(toolButtonGroup, new String[] {
				"default tool",
				"add curve segment",
				"add bone",
				"weight selection tool",
				"rotate tool",
				"rotoscope tool",
				"rotate view",
				"move view",
				"zoom view",
				"knife tool"
		}, 0);
//		toolButtonGroup.setDefaultButton(commandButtonMap.get("default tool"));
		
		addToButtonGroup(new ButtonGroup(), new String[] {
				"single view",
				"horizontally split view",
				"vertically split view",
				"quad view"
		}, Settings.getInstance().viewports.viewportMode.ordinal());
		
		addToButtonGroup(new ButtonGroup(), new String[] {
				"front view",
				"rear view",
				"top view",
				"bottom view",
				"left view",
				"right view",
				"bird's eye view"
		}, 0);
		
		String laf = UIManager.getLookAndFeel().getClass().getName();
		int i = 0;
		if (laf.equals(UIManager.getCrossPlatformLookAndFeelClassName()))
			i = 1;
		else if (laf.equals(UIManager.getSystemLookAndFeelClassName()))
			i = 2;
		addToButtonGroup(new ButtonGroup(), new String[] {
				"jpatch lookandfeel",
				"crossplatform lookandfeel",
				"system lookandfeel"
		}, i);
		
		/*
		 * Disable commands
		 */
		enableCommand("clear rotoscope image", false);
//		enableCommand("lock x", false);
//		enableCommand("lock y", false);
//		enableCommand("lock z", false);
		enableCommand("stop edit morph", false);
		
		/*
		 * Set toggle button states
		 */
		((AbstractButton) commandMenuItemMap.get("select points")).setSelected(true);
		((AbstractButton) commandMenuItemMap.get("select bones")).setSelected(true);
		((AbstractButton) commandMenuItemMap.get("snap to grid")).setSelected(Settings.getInstance().viewports.snapToGrid);
		
		/*
		 * Add change listeners to toggle buttons
		 */
		((AbstractButton) commandMenuItemMap.get("select points")).getModel().addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				MainFrame.getInstance().getJPatchScreen().setSelectPoints(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		
		((AbstractButton) commandMenuItemMap.get("select bones")).getModel().addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				MainFrame.getInstance().getJPatchScreen().setSelectBones(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		
		((AbstractButton) commandMenuItemMap.get("lock points")).getModel().addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				boolean selected = e.getStateChange() == ItemEvent.SELECTED;
				MainFrame.getInstance().getJPatchScreen().setLockPoints(selected);
				((Action) commandActionMap.get("add curve segment")).setEnabled(!selected);
			}
		});
		
		((AbstractButton) commandMenuItemMap.get("lock bones")).getModel().addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				boolean selected = e.getStateChange() == ItemEvent.SELECTED;
				MainFrame.getInstance().getJPatchScreen().setLockBones(selected);
				((Action) commandActionMap.get("add bone")).setEnabled(!selected);
			}
		});
	}
	
	private void executeCommand(String command) {
		checkCommand(command);
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
	
	private void enableCommand(String command, boolean enable) {
		checkCommand(command);
		((Action) commandActionMap.get(command)).setEnabled(enable);
	}
	
	private void enableCommands(String[] commands, boolean enable) {
		for (int i = 0; i < commands.length; i++)
			enableCommand(commands[i], enable);
	}
	
	private boolean isCommandEnabled(String command) {
		checkCommand(command);
		return ((Action) commandActionMap.get(command)).isEnabled();
	}
	
//	/*
//	 * KeyListener interface implementation start
//	 */
//	
//	public void keyTyped(KeyEvent e) {
//		String command = (String) inputMap.get(KeyStroke.getKeyStrokeForEvent(e));
//		if (DEBUG)
//			System.out.println(KeyStroke.getKeyStrokeForEvent(e) + ": Command = " + command);
//	}
//
//
//	public void keyPressed(KeyEvent e) {
//		String command = (String) inputMap.get(KeyStroke.getKeyStrokeForEvent(e));
//		if (DEBUG)
//			System.out.println(KeyStroke.getKeyStrokeForEvent(e) + ": Command = " + command);
//		if (command != null) {
//			executeCommand(command);
//		}
//	}
//
//	public void keyReleased(KeyEvent e) { }
//	
//	/*
//	 * KeyListener interface implementation end
//	 */
	
	private void put(String command, Action action, JMenuItem menuItem) {
		put(command, action, menuItem, null);
	}
	
	private void put(String command, Action action, JMenuItem menuItem, AbstractButton button) {
		commandActionMap.put(command, action);
		if (button != null) {
			button.setAction(action);
//			button.setText((String) action.getValue(Action.SHORT_DESCRIPTION));
			button.setIcon((Icon) action.getValue(Action.SMALL_ICON));
//			button.setModel(menuItem.getModel());
			menuItem.setModel(button.getModel());
			commandButtonMap.put(command, button);
		}
		if (menuItem != null) {
			menuItem.setAction(action);
			if (menuItem.getIcon() == null && !(menuItem instanceof JCheckBoxMenuItem) && !(menuItem instanceof JRadioButtonMenuItem))
			menuItem.setIcon(new Icon() {

				public void paintIcon(Component c, Graphics g, int x, int y) {
					// TODO Auto-generated method stub
					
				}

				public int getIconWidth() {
					// TODO Auto-generated method stub
					return 15;
				}

				public int getIconHeight() {
					// TODO Auto-generated method stub
					return 1;
				}
				
			});
			commandMenuItemMap.put(command, menuItem);
			menuItem.setText((String) action.getValue(Action.SHORT_DESCRIPTION));
			if (menuItem.getText() == null)
				menuItem.setText(command);
		}
//		actionMap.put(command, new CommandAction(command));
//		String keyString = (String) JPatchSettings.getInstance().commandKeyMap.get(command);
//		if (keyString != null) {
//			keyCommandMap.put(KeyStroke.getKeyStroke(keyString), command);
//			commandKeyMap.put(command, keyString);
//		}
	}
	
	private void setKeyBinding(String key, String command) {
		keyCommandMap.put(KeyStroke.getKeyStroke(key), command);
		commandKeyMap.put(command, KeyStroke.getKeyStroke(key));
//		JMenuItem mi = INSTANCE.commandMenuItemMap.get(command);
//		if (mi != null)
//			mi.setText(mi.getText() + "\t" + key);
	}
	
	private void addToButtonGroup(ButtonGroup bg, String[] commands, int selectedIndex) {
		for (int i = 0; i < commands.length; i++) {
			checkCommand(commands[i]);
			bg.add((JMenuItem) commandMenuItemMap.get(commands[i]));
		}
		bg.setSelected(((JMenuItem) commandMenuItemMap.get(commands[selectedIndex])).getModel(), true);
	}
	
	private void checkCommand(String command) {
		if (!commandActionMap.containsKey(command))
			throw new IllegalArgumentException("unknown command: " + command);
	}
	
//	public Map<KeyStroke, String> getInputMapping() {
//		return inputMapping;
//	}
//	
//	public ActionMap getActionMap() {
//		return actionMap;
//	}
	
	private void mapKeys(InputMap inputMap, ActionMap actionMap) {
		for (KeyStroke keyStroke : keyCommandMap.keySet()) {
			String command = keyCommandMap.get(keyStroke);
			inputMap.put(keyStroke, command);
			actionMap.put(command, new CommandAction(command));
		}
	}
	
	@SuppressWarnings("serial")
	private static class CommandAction extends AbstractAction {
		private String command;
		public CommandAction(String command) {
			this.command = command;
		}
		public void actionPerformed(ActionEvent e) {
//			System.out.println(command);
			INSTANCE.executeCommand(command);
		}
	}
}
