package jpatch.boundary.action;

import javax.swing.*;

import java.awt.event.*;
import java.io.*;

import jpatch.auxilary.FileFilters;
import jpatch.boundary.MainFrame;
import jpatch.boundary.settings.Settings;
import jpatch.renderer.PovrayRenderer3;;

@SuppressWarnings("serial")
public final class ExportPovrayAction extends AbstractAction {
	
	public void actionPerformed(ActionEvent actionEvent) {
		JFileChooser fileChooser = new JFileChooser(Settings.getInstance().directories.povrayFiles);
		fileChooser.addChoosableFileFilter(FileFilters.POVRAY);
		if (fileChooser.showSaveDialog(MainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			if (FileFilters.getExtension(file).equals(""))
				file = new File(file.getPath() + ".pov");
			if (Settings.getInstance().directories.rememberLastDirectories && !file.getParentFile().equals(Settings.getInstance().directories.povrayFiles))
				Settings.getInstance().directories.povrayFiles = file.getParentFile();
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				PovrayRenderer3 povrayExport = new PovrayRenderer3();
				povrayExport.writeModel(MainFrame.getInstance().getModel(), null, "", 0, writer);
				writer.close();
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
	}
}

