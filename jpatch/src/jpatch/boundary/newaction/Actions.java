/*
 * $Id$
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
package jpatch.boundary.newaction;

import java.awt.event.ActionEvent;
import java.net.*;
import java.util.*;
import java.util.prefs.Preferences;

import javax.swing.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import jpatch.auxilary.KeyStrokeUtils;
import jpatch.boundary.*;
import jpatch.boundary.settings.*;
import jpatch.boundary.ui.*;



/**
 * @author sascha
 *
 */
public class Actions extends DefaultHandler {
	private static final URL URL = ClassLoader.getSystemResource("jpatch/boundary/newaction/actions.xml");
	private static Actions INSTANCE = new Actions(URL);
	private Map<String, ActionDescriptor> actionMap = new HashMap<String, ActionDescriptor>();
	private Map<String, ButtonGroup> buttonGroupMap = new HashMap<String, ButtonGroup>();
	private ActionDescriptor actionDescriptor;
	
	public static Actions getInstance() {
		return INSTANCE;
	}
	
	private Actions(URL url) {
		XMLReader xmlReader = null;
		try {
			xmlReader = XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			try {
				xmlReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
			} catch (SAXException ee) {
				ee.printStackTrace();
			}
		}
		try {
			xmlReader.setContentHandler(this);
			System.out.println("Loading actions...");
			if (SplashScreen.instance != null)
				SplashScreen.instance.setText("Loading actions");
			xmlReader.parse(new InputSource(url.toString()));
			
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		
		System.out.println("Loading key bindings...");
		if (SplashScreen.instance != null)
			SplashScreen.instance.setText("Loading key bindings");
		loadKeySettings();
		
		
		/*
		 * Disable commands
		 */
//		enableAction("clear rotoscope image", false);
//		enableAction("stop edit morph", false);
		
		/*
		 * Set toggle button states
		 */
//		actionMap.get("select points").buttonModel.setSelected(true);
//		actionMap.get("select bones").buttonModel.setSelected(true);
//		actionMap.get("snap to grid").buttonModel.setSelected(Settings.getInstance().viewports.snapToGrid);	
//		actionMap.get("round tangents").buttonModel.setSelected(true);
//		actionMap.get("synchronize viewports").buttonModel.setSelected(Settings.getInstance().viewports.synchronizeViewports);
	}
	
	public void enableAction(String key, boolean enable) {
		getAction(key).setEnabled(enable);
	}
	
	public void enableActions(String[] keys, boolean enable) {
		for (int i = 0; i < keys.length; i++)
			enableAction(keys[i], enable);
	}
	
//	public void setViewDefinition(ViewDefinition viewDef) {
//		OLDCamera camera = viewDef.getCamera();
//		if (camera != null)
//			actionMap.get("camera" + camera.hashCode()).buttonModel.setSelected(true);
//		else
//			actionMap.get(viewDef.getViewName()).buttonModel.setSelected(true);
//		actionMap.get("show points").buttonModel.setSelected(viewDef.renderPoints());
//		actionMap.get("show curves").buttonModel.setSelected(viewDef.renderCurves());
//		actionMap.get("show patches").buttonModel.setSelected(viewDef.renderPatches());
//		actionMap.get("show bones").buttonModel.setSelected(viewDef.renderBones());
//		actionMap.get("show rotoscope").buttonModel.setSelected(viewDef.showRotoscope());
//		actionMap.get("lock view").buttonModel.setSelected(viewDef.isLocked());
//		enableAction("unlock view", viewDef.isLocked());
//		enableAction("show patches", viewDef.getDrawable().isShadingSupported());
//		enableAction("clear rotoscope image", MainFrame.getInstance().getModel() != null && MainFrame.getInstance().getModel().getRotoscope(viewDef.getView()) != null);
//	}
	
	public ButtonGroup getButtonGroup(String key) {
		ButtonGroup buttonGroup = buttonGroupMap.get(key);
		if (buttonGroup == null)
			throw new IllegalArgumentException("no buttongroup for key " + key);
		return buttonGroup;
	}
	
	public Action getAction(String key) {
		ActionDescriptor actionDescriptor = actionMap.get(key);
		if (actionDescriptor == null)
			throw new IllegalArgumentException("Action for key " + key + " not found!");
		if (actionDescriptor.action == null)
			throw new RuntimeException("no Action for key " + key + "!");
		return actionDescriptor.action;
	}
	
//	public void addAction(String key, Action action, DefaultButtonModel buttonModel) {
//		ActionDescriptor actionDescriptor = new ActionDescriptor(action);
//		actionDescriptor.buttonModel = buttonModel;
//		actionMap.put(key, actionDescriptor);
//	}
	
//	public void removeAction(String key) {
//		actionMap.remove(key);
//	}
	
	public ButtonModel getButtonModel(String key) {
		ActionDescriptor actionDescriptor = actionMap.get(key);
		if (actionDescriptor == null)
			throw new IllegalArgumentException("Action for key " + key + " not found!");
		if (actionDescriptor.buttonModel == null)
			throw new RuntimeException("no ButtonModel for key " + key + "!");
		return actionDescriptor.buttonModel;
	}
	
	public Map<String, KeyStroke> getKeyMapping() {
		Map<String, KeyStroke> map = new HashMap<String, KeyStroke>();
		for (String key : actionMap.keySet()) {
			map.put(key, KeyStroke.getKeyStroke((String) actionMap.get(key).action.getValue(JPatchAction.ACCELERATOR)));
		}
		return map;
	}
	
	public KeyStroke getDefaultKeyStroke(String key) {
		return actionMap.get(key).defaultAccelerator;
	}
	
//	public Set<String> getKeys() {
//		return actionMap.keySet();
//	}
	
	public AbstractButton getButton(String key) {
		ActionDescriptor actionDescriptor = actionMap.get(key);
		if (actionDescriptor == null)
			throw new IllegalArgumentException("Action for key " + key + " not found!");
		AbstractButton button = null;
		if (actionDescriptor.buttonModel instanceof JPatchLockingToggleButtonModel.UnderlyingModel)
			button = new JPatchLockingToggleButton((JPatchLockingToggleButtonModel.UnderlyingModel) actionDescriptor.buttonModel);
		else if (actionDescriptor.buttonModel instanceof JPatchMenuButton.MenuButtonModel)
			button = new JPatchMenuButton((JPatchMenuButton.MenuButtonModel) actionDescriptor.buttonModel);
		else if (actionDescriptor.buttonModel instanceof JPatchMenuButton.ComboButtonModel)
			button = new JPatchMenuButton((JPatchMenuButton.ComboButtonModel) actionDescriptor.buttonModel);
		else if (actionDescriptor.buttonModel instanceof JToggleButton.ToggleButtonModel)
			button = new JPatchToggleButton((JPatchToggleButton.ToggleButtonModel) actionDescriptor.buttonModel);
		else
			button = new JPatchButton(actionDescriptor.buttonModel);
		button.setAction(actionDescriptor.action);
		actionDescriptor.bound = true;
		return button;
	}
	
	public JMenuItem getMenuItem(String key, boolean bind) {
		ActionDescriptor actionDescriptor = actionMap.get(key);
		if (actionDescriptor == null)
			throw new IllegalArgumentException("Action for key " + key + " not found!");
		JMenuItem menuItem = null;
		if (actionDescriptor.buttonModel instanceof JPatchLockingToggleButtonModel.UnderlyingModel) {
			if (actionDescriptor.buttonModel.getGroup() == null)
				throw new IllegalArgumentException("LockingToggleButton is not part of a buttongroup!");
			else
				menuItem = new JPatchLockingRadioButtonMenuItem((JPatchLockingToggleButtonModel.UnderlyingModel) actionDescriptor.buttonModel);
		} else if (actionDescriptor.buttonModel instanceof JToggleButton.ToggleButtonModel) {
			if (actionDescriptor.buttonModel.getGroup() == null)
				menuItem = new JPatchCheckBoxMenuItem((JToggleButton.ToggleButtonModel) actionDescriptor.buttonModel);
			else
				menuItem = new JPatchRadioButtonMenuItem((JToggleButton.ToggleButtonModel) actionDescriptor.buttonModel);
		} else {
			menuItem = new JPatchMenuItem(actionDescriptor.buttonModel);
		}
//		menuItem.setModel(actionDescriptor.buttonModel);
		menuItem.setAction(actionDescriptor.action);
		actionDescriptor.bound = bind;
		return menuItem;
	}

	public Set<Action> getUnboundActions() {
		Set<Action> unboundActions = new HashSet<Action>();
		for (String key : actionMap.keySet()) {
			ActionDescriptor actionDescriptor = actionMap.get(key);
			if (!actionDescriptor.bound)
				unboundActions.add(actionDescriptor.action);
		}
		return unboundActions;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		System.out.print("<" + localName);
		for (int i = 0, n = attributes.getLength(); i < n; i++)
			System.out.print(" " + attributes.getLocalName(i) + "=\"" + attributes.getValue(i) + "\"");
		System.out.println(">");
		if (localName.equals("instantiate")) {
			try {
				Class actionClass = Class.forName(attributes.getValue("class"));
				String methodName = attributes.getValue("method");
				System.out.println(actionClass + "." + methodName);
				if (methodName != null) {
					actionDescriptor.action = (Action) actionClass.getMethod(methodName, (Class[]) null).invoke(null, (Object[]) null);
				} else {
					actionDescriptor.action = (Action) actionClass.newInstance();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (localName.equals("buttongroup")) {
			if (attributes.getValue("type").equals("standard"))
				buttonGroupMap.put(attributes.getValue("name"), new ButtonGroup());
			else if (attributes.getValue("type").equals("locking"))
				buttonGroupMap.put(attributes.getValue("name"), new LockingButtonGroup());
		} else if (localName.equals("action")) {
			actionDescriptor = new ActionDescriptor();
			actionMap.put(attributes.getValue("name"), actionDescriptor);
			if (actionDescriptor == null)
				throw new IllegalArgumentException("No actionDescriptor for key " + attributes.getValue("name") + "!");
			String model = attributes.getValue("model");
			if (model.equals("default"))
				actionDescriptor.buttonModel = new DefaultButtonModel();
			else if (model.equals("radio")) {
				actionDescriptor.buttonModel = new JToggleButton.ToggleButtonModel();
				getButtonGroup(attributes.getValue("group")).add(new JPatchDummyButton(actionDescriptor.buttonModel));
			} else if (model.equals("locking radio")) {
				actionDescriptor.buttonModel = new JPatchLockingToggleButtonModel.UnderlyingModel(); // FIXME
				getButtonGroup(attributes.getValue("group")).add(new JPatchDummyButton(actionDescriptor.buttonModel));
			} else if (model.equals("check")) {
				actionDescriptor.buttonModel = new JToggleButton.ToggleButtonModel();
			} else if (model.equals("menu")) {
				actionDescriptor.buttonModel = new JPatchMenuButton.MenuButtonModel();
			} else if (model.equals("combo")) {
				actionDescriptor.buttonModel = new JPatchMenuButton.ComboButtonModel();
			}
			String isDefault = attributes.getValue("default");
			if (isDefault != null && isDefault.equals("true"))
				((LockingButtonGroup) getButtonGroup(attributes.getValue("group"))).setDefaultButtonModel(actionDescriptor.buttonModel);
		} else if (localName.equals("property")) {
			actionDescriptor.action.putValue(attributes.getValue("key"), attributes.getValue("value"));
			if (attributes.getValue("key").equals(JPatchAction.ACCELERATOR))
				actionDescriptor.defaultAccelerator = KeyStroke.getKeyStroke(attributes.getValue("value"));
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (localName.equals("action")) {
//			if (actionDescriptor.button != null) {
//				System.out.println("*** button != null, setting action ***");
//				actionDescriptor.button.setAction(null);
//				actionDescriptor.button.setAction(actionDescriptor.action);
//			}
//			if (actionDescriptor.menuItem != null)
//				actionDescriptor.menuItem.setAction(actionDescriptor.action);
			actionDescriptor = null;
		} else if (localName.equals("instantiate")) {
			
		}
	}
	
	public void saveKeySettings() {
		Preferences node = Preferences.userRoot().node("/JPatch/settings/keyBindings");
		for (String key : actionMap.keySet()) {
			String accelerator = (String) actionMap.get(key).action.getValue(JPatchAction.ACCELERATOR);
			if (accelerator == null)
				accelerator = "null";
			node.put(key, accelerator);
		}
	}
	
	private void loadKeySettings() {
		Preferences node = Settings.getInstance().getRootNode().node("keyBindings");
		for (String key : actionMap.keySet()) {
			Action action = actionMap.get(key).action;
			KeyStroke ks = getDefaultKeyStroke(key);
			String defaultAccelerator = ks == null ? "null" : KeyStrokeUtils.keyStrokeToString(ks);
			String accelerator = node.get(key, defaultAccelerator);
			if (accelerator.equals("null"))
				accelerator = null;
			action.putValue(JPatchAction.ACCELERATOR, accelerator);
		}
	}
	
	static class ActionDescriptor {
		Action action;
		DefaultButtonModel buttonModel;
		KeyStroke defaultAccelerator;
		boolean bound;
		
//		ActionDescriptor(Action action) {
//			this.action = action;
////			defaultAccelerator = KeyStroke.getKeyStroke((String) action.getValue(JPatchAction.ACCELERATOR));
//		}
		
		@Override
		public String toString() {
			return getClass().getName() + "@" + hashCode() + "\naction=" + action + "\nbuttonModel=" + buttonModel + "\nkeyStroke=" + defaultAccelerator + "\nbound=" + bound;
		}
	}
	
	static class DummyAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) { } // do nothing
	}
}
