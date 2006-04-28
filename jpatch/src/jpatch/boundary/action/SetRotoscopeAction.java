package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import java.io.*;

import jpatch.auxilary.FileFilters;
import jpatch.boundary.*;
import jpatch.boundary.settings.Settings;

public final class SetRotoscopeAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SetRotoscopeAction() {
		super("set image");
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getActiveViewport().getViewDefinition();
		JFileChooser fileChooser = new JFileChooser(Settings.getInstance().directories.rotoscopeFiles);
		fileChooser.addChoosableFileFilter(FileFilters.IMAGES);
		if (fileChooser.showOpenDialog(MainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			String filename = file.getPath();
			Rotoscope rotoscope = new Rotoscope(filename);
			if (rotoscope.isValid()) {
				//rotoscope = null;
				MainFrame.getInstance().getModel().setRotoscope(viewDef.getView(),rotoscope);
				MainFrame.getInstance().getJPatchScreen().update_all();
				Settings.getInstance().directories.rotoscopeFiles = file.getParentFile();
			}
		}
	}
}

