package jpatch.boundary.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jpatch.boundary.dialog.*;

public class KeyMappingAction extends AbstractAction {

	public void actionPerformed(ActionEvent e) {
		new KeyMappingDialog();
	}

}
