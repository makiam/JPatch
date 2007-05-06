package com.jpatch;

import com.jpatch.afw.control.Configuration;
import com.jpatch.boundary.Main;

import java.util.Locale;

public class Launcher {
	public static void main(String[] args) {
		System.setProperty("swing.boldMetal", "false");
		System.setProperty("swing.aatext", "true");
		System.setProperty("apple.laf.useScreenMenuBar", "true");	
		Configuration.getInstance().put("locale", Locale.GERMAN);
		Configuration.getInstance().put("stringResource", "com/jpatch/resources/Strings");
		Configuration.getInstance().put("iconDir", "com/jpatch/icons/");
		Configuration.getInstance().put("settingsUserRoot", "com/jpatch/settings/preferences");
		Main.getInstance();
	}
}
