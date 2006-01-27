package jpatch;

import java.awt.*;
import buoy.widget.*;
import buoy.event.*;
import javax.swing.*;

import jpatch.boundary.*;
import jpatch.boundary.settings.Settings;
import jpatch.entity.*;

public final class Launcher {
	private static BFrame frame;
	/* private static GlTest gltest; */
	public static void main(String[] args) {
		if (true) {
//			try {
//				UIManager.setLookAndFeel(Settings.getInstance().lookAndFeelClassname);
//			} catch (Exception e) { }
			SplashScreen splash = new SplashScreen();
			splash.showSplash(true);
			try {
				Thread.sleep(100);
			} catch (Exception e) { }
			launchModeler();
			try {
				Thread.sleep(900);
			} catch (Exception e) { }
			splash.clearSplash();
			return;
		}
		if (args.length >=1) {
			if (args[0].equals("-animator")) launchAnimator();
			else if (args[0].equals("-modeler")) launchModeler();
			else if (args[0].equals("-settings")) settings();
			else System.out.println("usage java -jpatch.jar [-animator | -modeler | -settings]");
		}
		else {
			try {
				UIManager.setLookAndFeel(Settings.getInstance().lookAndFeelClassname);
			} catch (Exception e) {
			}
			frame = new BFrame("JPatch Launcher");
			frame.addEventLink(WindowClosingEvent.class, frame, "dispose");
			ColumnContainer container = new ColumnContainer();
			BButton buttonModeler = new BButton("Start JPatch Modeler");
			BButton buttonAnimator = new BButton("Start JPatch Animator");
			BButton buttonSettings = new BButton("Edit settings");
			buttonModeler.addEventLink(CommandEvent.class, new Launcher(), "launchModeler");
			buttonAnimator.addEventLink(CommandEvent.class, new Launcher(), "launchAnimator");
			buttonSettings.addEventLink(CommandEvent.class, new Launcher(), "settings");
			BLabel labelInfo1 = new BLabel("You can start the modeler\n or animator directly by");
			BLabel labelInfo2 = new BLabel("using the -modeler or -animator commandline options");
			container.add(buttonModeler);
//			container.add(buttonAnimator);
			container.add(buttonSettings);
			container.add(labelInfo1);
			container.add(labelInfo2);
			frame.setContent(container);
			frame.pack();
			((java.awt.Window) frame.getComponent()).setLocationRelativeTo(null);
			frame.setVisible(true);
			frame.layoutChildren();
		}
	}
	
	private static void launchAnimator() {
		Animator.getInstance();
		if (frame != null) frame.dispose();
	}
	
	private static void launchModeler() {
		Model model = new Model();
		new MainFrame(model);
		if (frame != null) frame.dispose();
	}
	
	private static void settings() {
		Component parent = (frame != null) ? frame.getComponent() : null;
		Settings.getInstance().showDialog(parent);
		if (frame != null) frame.dispose();
	}
}
