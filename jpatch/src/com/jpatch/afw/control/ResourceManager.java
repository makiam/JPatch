package com.jpatch.afw.control;


import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class ResourceManager {
	private static ResourceManager INSTANCE;
	private ResourceBundle strings;
	
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
	}
	
	public String getString(String key) {
		return strings.getString(key);
	}
	
	public void configureAction(JPatchAction action) {
		String name = getString(action.getName());
		action.setButtonText(name);
		action.setMenuText(name);
		action.setDisplayName(name);
	}
}
