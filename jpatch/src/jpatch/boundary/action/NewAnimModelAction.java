package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import jpatch.entity.*;
import jpatch.auxilary.FileFilters;
import jpatch.boundary.*;
import jpatch.boundary.settings.Settings;
import jpatch.control.edit.AtomicAddRemoveAnimObject;
import jpatch.control.edit.JPatchRootEdit;
import jpatch.control.importer.JPatchImport;

public final class NewAnimModelAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NewAnimModelAction() {
		super("Load new model...");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		JFileChooser fileChooser = new JFileChooser(Settings.getInstance().export.modelDirectory);
		fileChooser.setFileFilter(FileFilters.JPATCH);
		if (fileChooser.showOpenDialog(MainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
			String filename = fileChooser.getSelectedFile().getName();
			Model model = new Model();
			(new JPatchImport()).importModel(model, fileChooser.getSelectedFile().getPath());
			
			AnimObject animObject = new AnimModel(model, filename);
			JPatchRootEdit edit = new AtomicAddRemoveAnimObject(animObject, false);
			MainFrame.getInstance().getUndoManager().addEdit(edit);
			MainFrame.getInstance().getJPatchScreen().update_all();
			MainFrame.getInstance().getTimelineEditor().repaint();
		}
	}
}