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
			//System.out.println(filename + " " + JPatchUtils.getFileExtension(file.getName()));
			if (FileFilters.getExtension(file).equals("")) {
				file = new File(file.getPath() +  ".jpt");
			}
			if (write(file)) {
//				JPatchUserSettings.getInstance().strJPatchFile = filename;
				return true;
			}
		}
		return false;
	}
		
		
	public boolean save() {
		File file;
		if (MainFrame.getInstance().getAnimation() != null)
			file = MainFrame.getInstance().getAnimation().getFile();
		else
			file = MainFrame.getInstance().getModel().getFile();
		if (file != null)
			return write(file);
		else
			return saveAs();
	}
	
	private boolean write(File file) {
		String filename = file.getPath();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(byteArrayOutputStream);
		
		
		try {
			// create xml representation
			StringBuffer xml = null;
			if (MainFrame.getInstance().getAnimation() != null) {
				MainFrame.getInstance().getAnimation().xml(out, "\t");
			} else {
				xml = MainFrame.getInstance().getModel().xml("\t");
			}
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
			if (xml != null)
				writer.write(xml.toString());
			else
				writer.write(byteArrayOutputStream.toString());
			writer.write("</jpatch>\n");
			writer.close();
			MainFrame.getInstance().getUndoManager().setChange(false);
			//System.out.println("file " + filename + " written.");
			if (MainFrame.getInstance().getAnimation() != null)
				MainFrame.getInstance().getAnimation().setFile(file);
			else
				MainFrame.getInstance().getModel().setFile(file);
			MainFrame.getInstance().setFilename(file.getName());
			return true;
		} catch (IOException ioException) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(),"Unable to save file \"" + filename + "\"\n" + ioException, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
}
