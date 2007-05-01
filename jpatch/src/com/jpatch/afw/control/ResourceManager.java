package com.jpatch.afw.control;


import com.jpatch.afw.ui.PlatformUtils;

import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class ResourceManager {
	private static ResourceManager INSTANCE;
	private ResourceBundle strings;
	private Map<Integer, String> keyNames = new HashMap<Integer, String>();
	
	public static ResourceManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ResourceManager();
		}
		return INSTANCE;
	}
	
	private ResourceManager() {
		setLocale((Locale) Configuration.getInstance().get("locale"));
	}
	
	public void setLocale(Locale locale) {
		strings = PropertyResourceBundle.getBundle(Configuration.getInstance().getString("stringResource"), locale);
		ResourceBundle kn = PropertyResourceBundle.getBundle("com/jpatch/afw/resources/KeyNames", locale);
		for (Enumeration e = kn.getKeys(); e.hasMoreElements(); ) {
			String key = (String) e.nextElement();
			int keyCode = Integer.parseInt(key);
			String keyString = kn.getString(key);
			if (PlatformUtils.getPlatform() == PlatformUtils.Platform.MAC_OS_X) {
				/*
				 * override text with symbols for special keys
				 */
				if (keyCode == KeyEvent.VK_ENTER) {
					keyString = "\u23ce";
				} else if (keyCode == KeyEvent.VK_DELETE) {
					keyString = "\u2326";
				} else if (keyCode == KeyEvent.VK_BACK_SPACE) {
					keyString = "\u232b";
				} else if (keyCode == KeyEvent.VK_TAB) {
					keyString = "\u21e5";
				}
			}
			keyNames.put(keyCode, keyString);
		}
	}
	
	public String getString(String key) {
		if (key == null) {
			return "";
		}
		return strings.getString(key);
	}
	
	public String getKeyName(int keyCode) {
		return keyNames.get(keyCode);
	}
	
	public void configureAction(JPatchAction action) {
		String name = getString(action.getName());
		action.getButtonText().setObject(name);
		action.getMenuText().setObject(name);
		action.getDisplayName().setObject(name);
	}
}
