package jpatch.boundary.newaction;

import java.awt.event.*;

import jpatch.boundary.*;

public class ChangeViewActions {

	public static JPatchAction createViewportS1Action() {
		return new ViewportLayoutAction(Main.Layout.S1);
	}
	
	public static JPatchAction createViewportS2Action() {
		return new ViewportLayoutAction(Main.Layout.S2);
	}
	
	public static  JPatchAction createViewportS3Action() {
		return new ViewportLayoutAction(Main.Layout.S3);
	}
	
	public static  JPatchAction createViewportS4Action() {
		return new ViewportLayoutAction(Main.Layout.S4);
	}
	
	public static  JPatchAction createViewportH12Action() {
		return new ViewportLayoutAction(Main.Layout.H12);
	}
	
	public static  JPatchAction createViewportH34Action() {
		return new ViewportLayoutAction(Main.Layout.H34);
	}
	
	public static  JPatchAction createViewportV13Action() {
		return new ViewportLayoutAction(Main.Layout.V13);
	}
	
	public static  JPatchAction createViewportV24Action() {
		return new ViewportLayoutAction(Main.Layout.V24);
	}
	
	public static  JPatchAction createViewportQuadAction() {
		return new ViewportLayoutAction(Main.Layout.QUAD);
	}
	
	public static  JPatchAction createViewportSingleAction() {
		return new ViewportLayoutAction(Main.Layout.SINGLE);
	}
	
	public static  JPatchAction createViewportSplitVAction() {
		return new ViewportLayoutAction(Main.Layout.V_SPLIT);
	}
	
	public static  JPatchAction createViewportSplitHAction() {
		return new ViewportLayoutAction(Main.Layout.H_SPLIT);
	}
	
	public static JPatchAction createViewportLayoutAction() {
		return new JPatchAction() {
			public void actionPerformed(ActionEvent e) { }	// does nothing
		};
	}
	
	private static class ViewportLayoutAction extends JPatchAction {
		private Main.Layout layout;
		
		private ViewportLayoutAction(Main.Layout layout) {
			this.layout = layout;
		}
		
		public void actionPerformed(ActionEvent e) {
			System.out.println(e);
			Thread.dumpStack();
			Main.getInstance().setLayout(layout);
		}
	}
}
