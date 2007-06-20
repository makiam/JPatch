package com.jpatch;

import com.jpatch.afw.control.Configuration;
import com.jpatch.afw.ui.PlatformUtils;
import com.jpatch.boundary.Main;

import java.util.Locale;

public class Launcher {
	public static void main(String[] args) {
		Configuration.getInstance().put("locale", Locale.GERMAN);
		Configuration.getInstance().put("stringResource", "com/jpatch/resources/Strings");
		Configuration.getInstance().put("iconDir", "com/jpatch/icons/");
		Configuration.getInstance().put("settingsUserRoot", "com/jpatch/settings/preferences");
		PlatformUtils.setupSwing();
		Main.getInstance();
	}
}
