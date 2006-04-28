package jpatch.boundary.action;

import java.io.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import jpatch.auxilary.*;
import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.boundary.settings.Settings;
import jpatch.control.*;
import jpatch.control.importer.*;

public final class ImportJPatchAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean bNewModel;
	
	public ImportJPatchAction() {
		this(true);
	}
		
	public ImportJPatchAction(boolean newModel) {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/open.png")));
		bNewModel = newModel;
		putValue(Action.SHORT_DESCRIPTION,bNewModel ? "Open a new model" : "Merge models");
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		if (bNewModel && MainFrame.getInstance().getUndoManager().hasChanged()) {
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
		JFileChooser fileChooser = new JFileChooser(Settings.getInstance().directories.jpatchFiles);
		fileChooser.addChoosableFileFilter(FileFilters.JPATCH);
		fileChooser.addChoosableFileFilter(FileFilters.AM_MODELS);
		fileChooser.addChoosableFileFilter(FileFilters.SPATCH);
		fileChooser.setFileFilter(FileFilters.JPATCH);
		if (fileChooser.showOpenDialog(MainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			String filename = file.getPath();
			if (bNewModel) MainFrame.getInstance().newModel();
			ModelImporter modelImporter;
			String extension = FileFilters.getExtension(file);
			//System.out.println(extension);
			if (extension.equals("spt")) {
				modelImporter = new SPatchImport();
				Settings.getInstance().directories.spatchFiles = file.getParentFile();
			} else if (extension.equals("mdl")) {
				modelImporter = new AnimationMasterImport();
				Settings.getInstance().directories.animationmasterFiles = file.getParentFile();
			} else {
				modelImporter = new JPatchImport();
				Settings.getInstance().directories.jpatchFiles = file.getParentFile();
//				JPatchUserSettings.getInstance().strJPatchFile = filename;
			}
			if (!bNewModel) {
				Model model = MainFrame.getInstance().getModel();
				String name = model.getName();
				ArrayList headsBeforeImport = model.allHeads();
				modelImporter.importModel(model,filename);
				
				HashSet pointSet = new HashSet();
				for (Iterator it = model.allHeads().iterator(); it.hasNext(); ) {
					ControlPoint cp = (ControlPoint) it.next();
					if (!headsBeforeImport.contains(cp)) {
						pointSet.add(cp);
					}
				}
				Selection selection = new Selection(pointSet);
				selection.setName(file.getName());
				model.addSelection(selection);
				model.setName(name);
				MainFrame.getInstance().setSelection(selection);
				MainFrame.getInstance().getUndoManager().clear();
			} else {
				modelImporter.importModel(MainFrame.getInstance().getModel(),filename);
			}
		}
		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).reload();
		MainFrame.getInstance().getJPatchScreen().zoomToFit_all();
	}
}

