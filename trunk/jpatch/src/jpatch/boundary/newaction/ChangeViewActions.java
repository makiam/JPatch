package jpatch.boundary.newaction;

import java.awt.event.*;

import jpatch.boundary.*;

public class ChangeViewActions {

	public JPatchAction createViewportS1Action() {
		return new ViewportLayoutAction(Main.Layout.S1);
	}
	
	public JPatchAction createViewportS2Action() {
		return new ViewportLayoutAction(Main.Layout.S2);
	}
	
	public JPatchAction createViewportS3Action() {
		return new ViewportLayoutAction(Main.Layout.S3);
	}
	
	public JPatchAction createViewportS4Action() {
		return new ViewportLayoutAction(Main.Layout.S4);
	}
	
	public JPatchAction createViewportH12Action() {
		return new ViewportLayoutAction(Main.Layout.H12);
	}
	
	public JPatchAction createViewportH34Action() {
		return new ViewportLayoutAction(Main.Layout.H34);
	}
	
	public JPatchAction createViewportV13Action() {
		return new ViewportLayoutAction(Main.Layout.V13);
	}
	
	public JPatchAction createViewportV24Action() {
		return new ViewportLayoutAction(Main.Layout.V24);
	}
	
	public JPatchAction createViewportQuadAction() {
		return new ViewportLayoutAction(Main.Layout.QUAD);
	}
	
	public JPatchAction createViewportSingleAction() {
		return new ViewportLayoutAction(Main.Layout.SINGLE);
	}
	
	public JPatchAction createViewportSplitVAction() {
		return new ViewportLayoutAction(Main.Layout.V_SPLIT);
	}
	
	public JPatchAction createViewportSplitHAction() {
		return new ViewportLayoutAction(Main.Layout.H_SPLIT);
	}
	
	private static class ViewportLayoutAction extends JPatchAction {
		private Main.Layout layout;
		
		private ViewportLayoutAction(Main.Layout layout) {
			this.layout = layout;
		}
		
		public void actionPerformed(ActionEvent e) {
			Main.getInstance().setLayout(layout);
		}
	}
}
