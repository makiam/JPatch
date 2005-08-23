package test;

import java.util.*;
import javax.swing.*;

public class UiTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("*");
		UIDefaults defaults = UIManager.getDefaults();
		List keys = Collections.list(defaults.keys());
		for (Iterator it = keys.iterator(); it.hasNext(); ) {
			String key = (String) it.next();
			if (key.contains("control"))
				System.out.println(key + " = " + defaults.get(key));
		}
	}
}
