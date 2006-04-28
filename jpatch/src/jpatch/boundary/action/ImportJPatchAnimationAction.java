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

public final class ImportJPatchAnimationAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean bNewModel;
	
//	public ImportJPatchAnimationAction {
//		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/open.png")));
////		bNewModel = newModel;
//		putValue(Action.SHORT_DESCRIPTION,bNewModel ? "Open a new model" : "Merge models");
//	}
	
	public ImportJPatchAnimationAction() {
		super("open animation",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/open.png")));
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
//		javax.swing.filechooser.FileFilter defaultFileFilter = new JPatchFilter();
//		fileChooser.addChoosableFileFilter(defaultFileFilter);
//		fileChooser.addChoosableFileFilter(new AMFilter());
//		fileChooser.addChoosableFileFilter(new SPatchFilter());
//		fileChooser.setFileFilter(defaultFileFilter);
		if (fileChooser.showOpenDialog(MainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			String filename = file.getPath();
			new AnimationImporter().loadAnimation(filename);
			MainFrame.getInstance().getAnimation().setPosition(MainFrame.getInstance().getAnimation().getStart());
			MainFrame.getInstance().getJPatchScreen().update_all();
//			if (bNewModel) MainFrame.getInstance().newModel();
//			ModelImporter modelImporter;
//			String extension = JPatchUtils.getFileExtension(filename);
//			//System.out.println(extension);
//			if (extension.equals("spt")) {
//				modelImporter = new SPatchImport();
//				Settings.getInstance().directories.spatchFiles = file.getParentFile();
//			} else if (extension.equals("mdl")) {
//				modelImporter = new AnimationMasterImport();
//				Settings.getInstance().directories.animationmasterFiles = file.getParentFile();
//			} else {
//				modelImporter = new JPatchImport();
//				Settings.getInstance().directories.jpatchFiles = file.getParentFile();
////				JPatchUserSettings.getInstance().strJPatchFile = filename;
//			}
//			if (!bNewModel) {
//				Model model = MainFrame.getInstance().getModel();
//				String name = model.getName();
//				ArrayList headsBeforeImport = model.allHeads();
//				modelImporter.importModel(model,filename);
//				
//				HashSet pointSet = new HashSet();
//				for (Iterator it = model.allHeads().iterator(); it.hasNext(); ) {
//					ControlPoint cp = (ControlPoint) it.next();
//					if (!headsBeforeImport.contains(cp)) {
//						pointSet.add(cp);
//					}
//				}
//				Selection selection = new Selection(pointSet);
//				selection.setName(file.getName());
//				model.addSelection(selection);
//				model.setName(name);
//				MainFrame.getInstance().setSelection(selection);
//				MainFrame.getInstance().getUndoManager().clear();
//			} else {
//				modelImporter.importModel(MainFrame.getInstance().getModel(),filename);
//			}
		}
//		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).reload();
//		MainFrame.getInstance().getJPatchScreen().zoomToFit_all();
	}
}

