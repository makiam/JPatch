package com.jpatch;

import com.jpatch.afw.control.Configuration;
import com.jpatch.afw.ui.PlatformUtils;
import com.jpatch.boundary.Main;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import jpatch.auxilary.NativeLibraryHelper;

public class Launcher {
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InterruptedException, InvocationTargetException {
		Configuration.getInstance().put("locale", Locale.ENGLISH);
		Configuration.getInstance().put("stringResource", "com/jpatch/resources/Strings");
		Configuration.getInstance().put("iconDir", "com/jpatch/icons/");
		Configuration.getInstance().put("settingsUserRoot", "com/jpatch/settings/preferences");
		PlatformUtils.setupSwing();
		
		while (!new NativeLibraryHelper().checkLibraries(null));
		
		EventQueue.invokeAndWait(new Runnable() {
			public void run() {
				Main.getInstance();
			}
		});
		
	}
}
