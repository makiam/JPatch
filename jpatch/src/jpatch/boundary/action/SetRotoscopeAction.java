package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import java.io.*;

import jpatch.boundary.*;
import jpatch.boundary.filefilters.*;

public final class SetRotoscopeAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ViewDefinition viewDefinition;
	
	public SetRotoscopeAction(ViewDefinition viewDefinition) {
		super("set image");
		this.viewDefinition = viewDefinition;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		JFileChooser fileChooser = new JFileChooser(JPatchSettings.getInstance().strRotoscopePath);
		fileChooser.addChoosableFileFilter(new ImageFilter());
		if (fileChooser.showOpenDialog(MainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			String filename = file.getPath();
			Rotoscope rotoscope = new Rotoscope(filename);
			if (rotoscope.isValid()) {
				//rotoscope = null;
				MainFrame.getInstance().getModel().setRotoscope(viewDefinition.getView(),rotoscope);
				MainFrame.getInstance().getJPatchScreen().update_all();
				JPatchSettings.getInstance().strRotoscopePath = file.getParent();
			}
		}
	}
}

