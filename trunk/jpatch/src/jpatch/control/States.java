package jpatch.control;

import jpatch.entity.*;
import jpatch.entity.attributes2.AbstractStateMachine;

public class States {
	public static enum Tool {
		MOVE_VIEW,
		ROTATE_VIEW,
		ZOOM_VIEW
	}
	
	public final AbstractStateMachine<Tool> toolSm = new AbstractStateMachine<Tool>(Tool.class, Tool.MOVE_VIEW) {

		@Override
		protected void enterState(Tool state) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void exitState(Tool state) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
}
