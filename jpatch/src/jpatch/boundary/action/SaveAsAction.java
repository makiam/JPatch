package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;

import jpatch.VersionInfo;
import jpatch.auxilary.*;
import jpatch.boundary.*;
import jpatch.boundary.settings.Settings;

public final class SaveAsAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean bSaveAs;
	
	public SaveAsAction(boolean saveAs) {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/save.png")));
		bSaveAs = saveAs;
		if (saveAs) {
			putValue(Action.SHORT_DESCRIPTION,"Save As...");
		} else {
			putValue(Action.SHORT_DESCRIPTION,"Save");
		}
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		////System.out.println(MainFrame.getInstance().getModel().xml(0));
		//JFileChooser fileChooser = new JFileChooser(JPatchUserSettings.getInstance().strJPatchPath);
		//if (fileChooser.showSaveDialog(MainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
		//	File file = fileChooser.getSelectedFile();
		//	String filename = file.getPath();
		//	if (JPatchUtils.getFileExtension(filename).equals("")) {
		//		filename += ".jpx";
		//	}
		//	try {
		//		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		//		writer.write(MainFrame.getInstance().getModel().xml(0).toString());
		//		writer.close();
		//		MainFrame.getInstance().getUndoManager().setChange(false);
		//	} catch (IOException e) {
		//		;
		//	}
		//	JPatchUserSettings.getInstance().strJPatchPath = file.getParent();
		//}
		if (bSaveAs) {
			saveAs();
		} else {
			save();
		}
	}
	
	public boolean saveAs() {
		JFileChooser fileChooser = new JFileChooser(Settings.getInstance().directories.jpatchFiles);
		fileChooser.addChoosableFileFilter(FileFilters.JPATCH);
		if (fileChooser.showSaveDialog(MainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			String filename = file.getPath();
			//System.out.println(filename + " " + JPatchUtils.getFileExtension(file.getName()));
			if (FileFilters.getExtension(file).equals("")) {
				filename = filename + ".jpt";
			}
			if (write(filename)) {
//				JPatchUserSettings.getInstance().strJPatchFile = filename;
				return true;
			}
		}
		return false;
	}
		
		
	public boolean save() {
		return saveAs();
//		String filename = JPatchUserSettings.getInstance().strJPatchFile;
//		if (filename.equals("")) {
//			return saveAs();
//		} else {
//			return write(filename);
//		}
	}
	
	private boolean write(String filename) {
		try {
			// create xml representation
			StringBuffer xml;
			if (MainFrame.getInstance().getAnimation() != null)
				xml = MainFrame.getInstance().getAnimation().xml("\t");
			else
				xml = MainFrame.getInstance().getModel().xml("\t");
			
			File file = new File(filename);
			// make backup
			if (file.exists()) {
				BufferedReader reader = new BufferedReader(new FileReader(filename));
				BufferedWriter writer = new BufferedWriter(new FileWriter(filename + "~"));
				char[] buffer = new char[65536];
				int charsRead = 0;
				while ((charsRead = reader.read(buffer)) > 0)
					writer.write(buffer, 0, charsRead);
				reader.close();
				writer.close();
			}
			// write to file
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			writer.write("<jpatch version=\"" + VersionInfo.ver + "\">\n");
			writer.write(xml.toString());
			writer.write("</jpatch>\n");
			writer.close();
			MainFrame.getInstance().getUndoManager().setChange(false);
			//System.out.println("file " + filename + " written.");
			return true;
		} catch (IOException ioException) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(),"Unable to save file \"" + filename + "\"\n" + ioException, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
}
