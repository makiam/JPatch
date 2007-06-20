package com.jpatch.afw.ui;

import com.jpatch.afw.control.ResourceManager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.Set;

import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;

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
	
	public static void setupSwing() {
		System.setProperty("swing.boldMetal", "false");
		System.setProperty("swing.aatext", "true");
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		
		if (platform != Platform.MAC_OS_X) {
			try {
				UIManager.setLookAndFeel("com.jpatch.afw.ui.laf.JPatchLookAndFeel");
//				UIManager.setLookAndFeel("jpatch.boundary.laf.SmoothLookAndFeel");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			UIManager.getLookAndFeelDefaults().put("ComboBox.background", Color.WHITE);
//			System.out.println(UIManager.getLookAndFeelDefaults().get("SliderUI"));
//			UIManager.getLookAndFeelDefaults().put("Slider.paintThumbArrowShape", Boolean.TRUE);
			
			Set x = UIManager.getLookAndFeelDefaults().entrySet();
			for (Object key : x) {
				if (key.toString().contains("background"))
					System.out.println(key + " => " + UIManager.getLookAndFeelDefaults().get(key));
			}
			
			
//			com.sun.java.swing.plaf.windows.WindowsSliderUI
//			UIManager.getUI(null);
//			UIManager.getLookAndFeelDefaults().put("SliderUI", "com.jpatch.afw.ui.SliderUI");
			
//			UIManager.getDefaults().put("SliderUI", "com.jpatch.afw.ui.SliderUI");
//			System.out.println(UIManager.getDefaults().get("SliderUI"));
//			UIManager.getLookAndFeelDefaults().put("TextField.border", new Border() {
//				private final Color BORDER_COLOR = UIManager.getDefaults().getColor("Button.darkShadow");
//				private final Color SHADOW1_COLOR = new Color(0x40000000, true);
//				private final Color SHADOW2_COLOR = new Color(0x20000000, true);
//				private final Color SHADOW3_COLOR = new Color(0x0c000000, true);
//				private final Insets INSETS = new Insets(2, 4, 1, 1);
//				
//				public Insets getBorderInsets(Component c) {
//					return INSETS;
//				}
//	
//				public boolean isBorderOpaque() {
//					return true;
//				}
//	
//				public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
//					g.setColor(SHADOW1_COLOR);
//					g.drawRect(x + 1, y + 1, width, height);
//					g.setColor(SHADOW2_COLOR);
//					g.drawRect(x + 2, y + 2, width, height);
//					g.setColor(SHADOW3_COLOR);
//					g.drawRect(x + 3, y + 3, width, height);
//					g.setColor(BORDER_COLOR);
//					g.drawRect(x, y, width - 1, height - 1);
//				}
//			});
			
		}
	}
}
