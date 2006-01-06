package jpatch.boundary.action;

import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import jpatch.auxilary.*;
import jpatch.boundary.*;
import jpatch.boundary.filefilters.*;
import jpatch.boundary.settings.Settings;

import jpatch.control.*;
import jpatch.control.importer.*;

public final class ImportSPatchAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImportSPatchAction() {
		super("sPatch [.spt]");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		if (MainFrame.getInstance().getUndoManager().hasChanged()) {
			int option = JPatchUtils.showSaveDialog();switch (option) {
				
				case JOptionPane.YES_OPTION:
					SaveAsAction saveAsAction = new SaveAsAction(false);
					if (saveAsAction.save()) {
						load();
					}
					break;
				
				case JOptionPane.NO_OPTION:
					load();
			}
		} else {
			load();
		}
	}
	
	private void load() {
		JFileChooser fileChooser = new JFileChooser(Settings.getInstance().directories.spatchFiles);
		fileChooser.addChoosableFileFilter(new SPatchFilter());
		if (fileChooser.showOpenDialog(MainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			String filename = file.getPath();
			MainFrame.getInstance().newModel();
			ModelImporter modelImporter = new SPatchImport();
			modelImporter.importModel(MainFrame.getInstance().getModel(),filename);
			//MainFrame.getInstance().getModel().computePatches();
			Settings.getInstance().directories.spatchFiles = file.getParentFile();
		}
		MainFrame.getInstance().getJPatchScreen().zoomToFit_all();
	}
}

