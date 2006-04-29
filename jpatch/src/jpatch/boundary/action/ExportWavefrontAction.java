package jpatch.boundary.action;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import jpatch.auxilary.FileFilters;
import jpatch.boundary.MainFrame;
import jpatch.boundary.settings.Settings;
import jpatch.renderer.WavefrontExport3;

@SuppressWarnings("serial")
public final class ExportWavefrontAction extends AbstractAction {
	public void actionPerformed(ActionEvent actionEvent) {
		JFileChooser fileChooser = new JFileChooser(Settings.getInstance().directories.objFiles);
		fileChooser.addChoosableFileFilter(FileFilters.OBJ);
		if (fileChooser.showSaveDialog(MainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
			File objFile = fileChooser.getSelectedFile();
			if (FileFilters.getExtension(objFile).equals(""))
				objFile = new File(objFile.getPath() + ".obj");
			if (Settings.getInstance().directories.rememberLastDirectories && !objFile.getParentFile().equals(Settings.getInstance().directories.objFiles))
				Settings.getInstance().directories.objFiles = objFile.getParentFile();
			File mtlFile = new File(objFile.getPath().replaceAll("\\.obj$", ".mtl"));
			new WavefrontExport3().writeToFile(
				objFile,
				mtlFile,
				Settings.getInstance().export.aliaswavefront.subdivisionLevel,
				Settings.getInstance().export.aliaswavefront.exportNormals,
				Settings.getInstance().export.aliaswavefront.outputMode
			);
		}
	}
}