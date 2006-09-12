package jpatch.boundary.newaction;

import java.awt.event.*;

import jpatch.boundary.*;
import jpatch.boundary.newtools.*;

public final class ChangeToolActions {
	
	private ChangeToolActions() { }	// private constructor to make this class uninstatiable
	
	public static JPatchAction createMoveViewAction() {
		return new SetToolAction(ChangeViewTool.createMoveViewTool());
	}
	
	public static JPatchAction createRotateViewAction() {
		return new SetToolAction(ChangeViewTool.createRotateViewTool());
	}
	
	public static JPatchAction createZoomViewAction() {
		return new SetToolAction(ChangeViewTool.createZoomViewTool());
	}
	
	public static JPatchAction createTranslateAction() {
		return new SetToolAction(new TranslateTool());
	}
	
	public static JPatchAction createAddCurveAction() {
		return new SetToolAction(new AddCurveTool());
	}
	
	private static final class SetToolAction extends JPatchAction {
		JPatchTool tool;
		
		private SetToolAction(JPatchTool tool) {
			this.tool = tool;
		}
		
		public void actionPerformed(ActionEvent e) {
			Main.getInstance().setTool(tool);
		}
	}
}
