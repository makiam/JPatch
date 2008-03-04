package test;

import java.awt.*;
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
			Object key = e.nextElement();
//			if (key.endsWith(".border")) {
			if (key instanceof String && ((String) key).toLowerCase().contains("double")) {
				System.out.println(key + " => " + uiDefaults.get(key));
			}
		}
		
		for (Object key : System.getProperties().keySet()) {
			System.out.println(key + " => " + System.getProperty(key.toString()));
		}
	}
}
