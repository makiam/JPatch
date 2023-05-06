package com.jpatch.afw.resources;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;


public class CreateKeynamesTemplate {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		for (Field field : KeyEvent.class.getFields()) {
			String name = field.getName();
			if (name.startsWith("VK_")) {
				String key = name.substring(3);
				System.out.println("# " + key);
				int keyCode = field.getInt(null);
				System.out.println(keyCode + "=" + key);
			}
		}
	}
}
