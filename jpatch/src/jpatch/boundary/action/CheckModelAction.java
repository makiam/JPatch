package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.entity.*;
import jpatch.auxilary.ModelTester;
import jpatch.boundary.*;

public final class CheckModelAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9136346937644423552L;
	public CheckModelAction() {
		super("Check model");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		System.out.println("Testing model integrity...");
		Model model = MainFrame.getInstance().getModel();
		ModelTester modelTester = new ModelTester();
		if (modelTester.test(model)) {
			System.out.println("The test completed successfuly");
		} else {
			System.out.println("*** THE TEST FAILED ***");
		}
	}
}

