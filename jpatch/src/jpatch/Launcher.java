package jpatch;

import buoy.widget.*;
import buoy.event.*;
import javax.swing.*;

import jpatch.boundary.*;
import jpatch.entity.*;

public final class Launcher {
	private static BFrame frame;
	/* private static GlTest gltest; */
	public static void main(String[] args) {
		if (true) {
			try {
				UIManager.setLookAndFeel(JPatchSettings.getInstance().strPlafClassName);
			} catch (Exception e) { }
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
			else System.out.println("usage java -jpatch.jar [-animator] [-modeler]");
		}
		else {
			try {
				UIManager.setLookAndFeel(JPatchSettings.getInstance().strPlafClassName);
			} catch (Exception e) {
			}
			frame = new BFrame("JPatch Launcher");
			frame.addEventLink(WindowClosingEvent.class, frame, "dispose");
			ColumnContainer container = new ColumnContainer();
			BButton buttonModeler = new BButton("Start JPatch Modeler");
			BButton buttonAnimator = new BButton("Start JPatch Animator");
			buttonModeler.addEventLink(CommandEvent.class, new Launcher(), "launchModeler");
			buttonAnimator.addEventLink(CommandEvent.class, new Launcher(), "launchAnimator");
			BLabel labelInfo1 = new BLabel("You can start the modeler\n or animator directly by");
			BLabel labelInfo2 = new BLabel("using the -modeler or -animator commandline options");
			container.add(buttonModeler);
			container.add(buttonAnimator);
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
}