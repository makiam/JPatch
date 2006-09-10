package jpatch.boundary.newaction;

import java.awt.event.*;

import jpatch.boundary.*;
import jpatch.boundary.newtools.*;

public final class EditModelActions {
	
	private EditModelActions() { }	// private constructor to make this class uninstatiable
	
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
