package com.jpatch.afw.control;


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
			keyNames.put(keyCode, kn.getString(key));
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
		action.setButtonText(name);
		action.setMenuText(name);
		action.setDisplayName(name);
//		System.out.println(action.getName() + " " + action.getMenuText() + " " + action.getButtonText() + " " + action.getDisplayName());
	}
}
