package jpatch.boundary.action;

import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import jpatch.auxilary.FileFilters;
import jpatch.boundary.MainFrame;
import jpatch.boundary.settings.Settings;
import jpatch.renderer.RibRenderer4;

@SuppressWarnings("serial")
public final class ExportRibAction extends AbstractAction {
	public void actionPerformed(ActionEvent actionEvent) {
		JFileChooser fileChooser = new JFileChooser(Settings.getInstance().directories.rendermanFiles);
		fileChooser.addChoosableFileFilter(FileFilters.RIB);
		if (fileChooser.showSaveDialog(MainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			if (FileFilters.getExtension(file).equals(""))
				file = new File(file.getPath() + ".rib");
			if (Settings.getInstance().directories.rememberLastDirectories && !file.getParentFile().equals(Settings.getInstance().directories.rendermanFiles))
				Settings.getInstance().directories.rendermanFiles = file.getParentFile();
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				new RibRenderer4().writeModel(MainFrame.getInstance().getModel(), null, "", 0, writer);
				writer.close();
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
	}
}

