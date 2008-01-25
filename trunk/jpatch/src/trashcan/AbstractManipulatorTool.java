package trashcan;

import javax.vecmath.*;

import trashcan.*;

import com.jpatch.boundary.*;
import com.jpatch.boundary.actions.*;
import com.jpatch.boundary.tools.*;
import com.jpatch.entity.*;

public abstract class AbstractManipulatorTool implements JPatchTool {

	protected TransformNode getSelectedNode() {
		return Main.getInstance().getSelection().getNode();
		if (isObjectMode()) {
			return (TransformNode) Main.getInstance().getSelectionManager().getSelectedObjectAttribute().getValue();
		} else {
			return Main.getInstance().getSelection().getSelectedSdsModelAttribute().getValue();
		}
	}
	
	protected boolean isObjectMode() {
		return Main.getInstance().getActions().sdsModeSM.getValue() == Actions.SdsMode.OBJECT_MODE;
	}
	
	protected void resetPivot(Point3d pivot) {
		if (isObjectMode()) {
			pivot.set(0, 0, 0);
		} else {
			Main.getInstance().getSelection().getCenter(pivot, null);
		}
	}
}
