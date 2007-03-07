package test;

import java.util.Enumeration;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import jpatch.boundary.settings.*;

public class SettingsTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		Settings.getInstance().showDialog(null);
		UIDefaults uiDefaults = UIManager.getDefaults();
//		System.out.println(uiDefaults);
		Enumeration e = uiDefaults.keys();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			if (key.endsWith(".border")) {
				System.out.println(key + " => " + uiDefaults.get(key));
			}
		}
	}
}
