package jpatch;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.*;

import jpatch.boundary.*;
import jpatch.boundary.laf.*;
import jpatch.boundary.settings.*;
import jpatch.entity.*;

public final class Launcher {
	public static void main(String[] args) {
		System.out.println("Stating JPatch...");
		SplashScreen splash = new SplashScreen();
		splash.showSplash(true);
		try {
			switch (Settings.getInstance().lookAndFeel) {
			case CROSS_PLATFORM:
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
				UIManager.put("swing.boldMetal", false);
				break;
			case SYSTEM:
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				break;
			case JPATCH:
				UIManager.setLookAndFeel(new SmoothLookAndFeel());
				UIManager.put("swing.boldMetal", false);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		if (args.length >=1) {
			if (args[0].equals("-animator")) launchAnimator();
			else if (args[0].equals("-modeler")) launchModeler();
			else if (args[0].equals("-settings")) settings();
			else System.out.println("usage java -jpatch.jar [-animator | -modeler | -settings]");
		}
		else {
			switch (Settings.getInstance().startup) {
			case ANIMATOR:
				launchAnimator();
				break;
			case MODELER:
				launchModeler();
				break;
			}
		}
		if (SplashScreen.instance != null) {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				public void run() {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							if (SplashScreen.instance != null)
								SplashScreen.instance.clearSplash();
						}
					});
				}
			}, 500);
		}
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
	}
}
