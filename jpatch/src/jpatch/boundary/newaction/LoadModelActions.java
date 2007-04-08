package jpatch.boundary.newaction;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import jpatch.boundary.*;
import jpatch.boundary.ui.JPatchDialog;
import sds.JptLoader;
import sds.Sds;

public class LoadModelActions {
	public static Action createLoadModelAction() {
		return new JPatchAction() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileFilter() {
					@Override
					public boolean accept(File f) {
						return (f.isDirectory() || f.getName().endsWith(".off") || f.getName().endsWith(".jpt"));
					}
					@Override
					public String getDescription() {
						return ".jpt .off";
					}
				});
				if (fileChooser.showOpenDialog(MainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					if (file.getName().endsWith(".jpt")) {
						try {
							Main.getInstance().setActiveSds(new JptLoader().importModel(new FileInputStream(file)));
						} catch (Exception e) {
							e.printStackTrace();
							JPatchDialog.showDialog(Main.getInstance().getFrame(), "Error", JPatchDialog.ERROR, "<b>Unable to load the model <i>" + file.getName() + "</i>:</b><p>" + e.toString() + "</p>", null, new String[] { "Ok" }, 0, "320");
						}
						Main.getInstance().repaintViewports();
					} else if (file.getName().endsWith(".off")) {
						try {
							Main.getInstance().setActiveSds(new Sds(new FileInputStream(file)));
						} catch (Exception e) {
							e.printStackTrace();
							JPatchDialog.showDialog(Main.getInstance().getFrame(), "Error", JPatchDialog.ERROR, "<b>Unable to load the model <i>" + file.getName() + "</i>:</b><p>" + e.toString() + "</p>", null, new String[] { "Ok" }, 0, "320");
						}
						Main.getInstance().repaintViewports();
					} 
				}
			}
		};
	}
}
