package com.jpatch.afw.ui;

import com.jpatch.afw.control.ResourceManager;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

public class PlatformUtils {
	public static enum Platform { LINUX, MAC_OS_X, WINDOWS, SOLARIS, UNKNOWN_OTHER }
	private static final Platform platform = detectPlatform();
	private static String KEY_SEPARATOR = "+";
	private static String ALT = ResourceManager.getInstance().getKeyName(KeyEvent.VK_ALT) + KEY_SEPARATOR;
	private static String CRTL = ResourceManager.getInstance().getKeyName(KeyEvent.VK_CONTROL) + KEY_SEPARATOR;
	private static String SHIFT = ResourceManager.getInstance().getKeyName(KeyEvent.VK_SHIFT) + KEY_SEPARATOR;
	
	private static Platform detectPlatform() {
		String osName = System.getProperties().getProperty("os.name");
		if (osName.startsWith("Windows")) {
			return Platform.WINDOWS;
		} else if (osName.equals("Linux")) {
			return Platform.LINUX;
		} else if (osName.equals("Mac OS X")) {
			return Platform.MAC_OS_X;
		} else if (osName.equals("Solaris")) {
			return Platform.SOLARIS;
		} else {
			return Platform.UNKNOWN_OTHER;
		}
	}
	
	public static Platform getPlatform() {
		return platform;
	}
	
	public static String getAcceleratorString(KeyStroke keyStroke) {
		if (keyStroke == null) {
			return null;
		}
		if (keyStroke.getKeyEventType() == KeyEvent.KEY_TYPED) {
			return new String(new char[] { keyStroke.getKeyChar() });
		}
		StringBuilder sb = new StringBuilder();
		switch(platform) {
		case MAC_OS_X:
			if ((keyStroke.getModifiers() & KeyEvent.SHIFT_DOWN_MASK) != 0) {
				sb.append("\u21e7");
			}
			if ((keyStroke.getModifiers() & KeyEvent.META_DOWN_MASK) != 0) {
				sb.append("\u2318");
			}
			if ((keyStroke.getModifiers() & KeyEvent.ALT_DOWN_MASK) != 0) {
				sb.append("\u2325");
			}
			if ((keyStroke.getModifiers() & KeyEvent.CTRL_DOWN_MASK) != 0) {
				sb.append("\u2303");
			}
			break;
		default:
			if ((keyStroke.getModifiers() & KeyEvent.SHIFT_DOWN_MASK) != 0) {
				sb.append(SHIFT);
			}
			if ((keyStroke.getModifiers() & KeyEvent.CTRL_DOWN_MASK) != 0) {
				sb.append(CRTL);
			}
			if ((keyStroke.getModifiers() & KeyEvent.ALT_DOWN_MASK) != 0) {
				sb.append(ALT);
			}
			break;
		}
		sb.append(ResourceManager.getInstance().getKeyName(keyStroke.getKeyCode()));
		return sb.toString();
	}
}
