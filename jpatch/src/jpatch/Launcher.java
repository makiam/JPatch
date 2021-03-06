package jpatch;

import java.lang.reflect.*;
import java.awt.*;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;

import jpatch.auxilary.NativeLibraryHelper;
import jpatch.boundary.*;
import jpatch.boundary.laf.*;
import jpatch.boundary.settings.*;
import jpatch.boundary.ui.JPatchDialog;
import jpatch.entity.*;

public final class Launcher {
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		
		
		
		
		
		
		System.setProperty("swing.boldMetal", Settings.JPATCH_ROOT_NODE.get("metalBoldText", "false"));
		System.setProperty("swing.aatext", Settings.JPATCH_ROOT_NODE.get("fontSmoothing", "true"));
		System.setProperty("apple.laf.useScreenMenuBar", "true");
//		System.setProperty("apple.awt.brushMetalLook", "true");
//		System.setProperty("apple.awt.brushMetalRounded", "true");
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		UIManager.put("ToolTip.background", new Color(0xff, 0xff, 0xaa));
		UIManager.put("ToolTip.border", new LineBorder(Color.BLACK));
//		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		try {
			while (!new NativeLibraryHelper().checkLibraries(null));
		} catch (Exception e) {
			String message = "<b>An error occured while checking the native libraries installation:</b><p>" +
					"<font color='red'>" + e.getMessage() + "</font><p>JPatch will now exit.";
			JPatchDialog.showDialog(null, "JOGL native libraries installation", JPatchDialog.WARNING, message, null, new String[] { "Quit" }, 1, "400");
			System.exit(0);
		} 
		
		Main.getInstance();
		
//		
//		if (args.length >=1) {
//			if (args[0].equals("-settings")) {
//				settings();
//			} else if (!args[0].equals("-animator") && !args[0].equals("-modeler")) {
//				System.out.println("usage java -jpatch.jar [-animator | -modeler | -settings]");
//				System.exit(0);
//			}
//		}
//		System.out.println("Stating JPatch...");
//		SplashScreen splash = new SplashScreen();
//		splash.showSplash(true);
//		try {
//			switch (Settings.getInstance().lookAndFeel) {
//			case CROSS_PLATFORM:
//				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
//				break;
//			case SYSTEM:
//				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//				break;
//			case JPATCH:
//				UIManager.setLookAndFeel(new SmoothLookAndFeel());
//				break;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		if (args.length >=1) {
//			if (args[0].equals("-animator"))
//				launchAnimator();
//			else if (args[0].equals("-modeler"))
//				launchModeler();
//		} else {
////			switch (Settings.getInstance().startup) {
////			case ANIMATOR:
////				launchAnimator();
////				break;
////			case MODELER:
//				launchModeler();
////				break;
////			}
//		}
//		if (SplashScreen.instance != null) {
//			Timer timer = new Timer();
//			timer.schedule(new TimerTask() {
//				public void run() {
//					EventQueue.invokeLater(new Runnable() {
//						public void run() {
//							if (SplashScreen.instance != null)
//								SplashScreen.instance.clearSplash();
//						}
//					});
//				}
//			}, 250);
//		}
	}
	
	private static void launchAnimator() {
		new MainFrame();
		if (SplashScreen.instance != null)
			SplashScreen.instance.setText("Setting up new animation");
		MainFrame.getInstance().newAnimation();
	}
	
	private static void launchModeler() {
		new MainFrame();
		if (SplashScreen.instance != null)
			SplashScreen.instance.setText("Setting up new model");
		MainFrame.getInstance().newModel();
	}
	
	private static void settings() {
		Settings.getInstance().showDialog(null);
		System.exit(0);
	}
}
