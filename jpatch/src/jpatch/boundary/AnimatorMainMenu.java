package jpatch.boundary;

import buoy.widget.*;
import buoy.event.*;
import java.io.*;
import javax.swing.*;
import javax.sound.sampled.*;
import jpatch.boundary.filefilters.*;
import jpatch.entity.*;
import jpatch.control.*;
import jpatch.control.importer.*;

public class AnimatorMainMenu extends BMenuBar {
	BMenu menuFile = new BMenu("File");
	BMenuItem miNew = new BMenuItem("New");
	BMenuItem miProperties = new BMenuItem("Properties...");
	BMenuItem miExit = new BMenuItem("Exit");
	BMenuItem miLoadTimesheet = new BMenuItem("Load JLipsync timesheet...");
	BMenuItem miRenderFrame = new BMenuItem("Render current frame");
	BMenuItem miRenderAnim = new BMenuItem("Render animation...");
	BMenuItem miLoadAudioClip = new BMenuItem("Load audio clip...");
	BMenuItem miDumpXml = new BMenuItem("Dump XML...");
	BMenuItem miLoad = new BMenuItem("Open...");
	BMenuItem miSave = new BMenuItem("Save...");
	
	BMenu menuOptions = new BMenu("Options");
	BMenuItem miPreferences = new BMenuItem("Preferences...");
	BMenuItem miRendererOptions = new BMenuItem("Renderer options...");
	
	BMenu menuObjects = new BMenu("Objects");
	BMenuItem miAddModel = new BMenuItem("Add model...");
	BMenuItem miAddLight = new BMenuItem("Add lightsource");
	
	public AnimatorMainMenu() {
		Object eventNew = new Object() {
			private void processEvent() {
				if (JOptionPane.showConfirmDialog(Animator.getInstance().getComponent(), "Are you sure?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) Animator.getInstance().NEW();
			}
		};
		miNew.addEventLink(CommandEvent.class, eventNew);
		miExit.addEventLink(CommandEvent.class, Animator.getInstance(), "quit");
		miAddModel.addEventLink(CommandEvent.class, this, "addModel");
		miAddLight.addEventLink(CommandEvent.class, this, "addLight");
		miLoadTimesheet.addEventLink(CommandEvent.class, this, "loadTimesheet");
		miRenderFrame.addEventLink(CommandEvent.class, Animator.getInstance(), "renderCurrentFrame");
		miLoadAudioClip.addEventLink(CommandEvent.class, this, "loadAudioClip");
		miRenderAnim.addEventLink(CommandEvent.class, Animator.getInstance(), "renderAnimation");
		miPreferences.addEventLink(CommandEvent.class, Animator.getInstance(), "preferences");
		miProperties.addEventLink(CommandEvent.class, Animator.getInstance(), "properties");
		miDumpXml.addEventLink(CommandEvent.class, this, "dumpXml");
		miLoad.addEventLink(CommandEvent.class, this, "load");
		miSave.addEventLink(CommandEvent.class, this, "save");
		menuFile.add(miNew);
		menuFile.add(miLoad);
		menuFile.add(miSave);
		menuFile.add(new BMenuSeparator());
		menuFile.add(miLoadTimesheet);
		menuFile.add(miLoadAudioClip);
		menuFile.add(new BMenuSeparator());
		menuFile.add(miRenderFrame);
		menuFile.add(miRenderAnim);
		menuFile.add(new BMenuSeparator());
		//menuFile.add(miDumpXml);
		menuFile.add(miProperties);
		menuFile.add(new BMenuSeparator());
		menuFile.add(miExit);
		add(menuFile);
		
		miRendererOptions.addEventLink(CommandEvent.class, this, "rendererOptions");
		menuOptions.add(miPreferences);
		menuOptions.add(miRendererOptions);
		add(menuOptions);
		
		menuObjects.add(miAddModel);
		menuObjects.add(miAddLight);
		add(menuObjects);
	}
	
	private void addModel() {
		JFileChooser fileChooser = new JFileChooser(JPatchUserSettings.getInstance().export.modelDirectory);
		javax.swing.filechooser.FileFilter defaultFileFilter = new JPatchFilter();
		fileChooser.setFileFilter(defaultFileFilter);
		if (fileChooser.showOpenDialog(MainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
			String filename = fileChooser.getSelectedFile().getName();
			Model model = new Model();
			(new JPatchImport()).importModel(model, fileChooser.getSelectedFile().getPath());
			AnimObject newObject = new AnimModel(model);
			Animator.getInstance().addObject(newObject, filename);
			Animator.getInstance().setActiveObject(newObject);
		}
	}
	
	private void addLight() {
		AnimLight light = new AnimLight();
		Animator.getInstance().addObject(light);
		Animator.getInstance().setActiveObject(light);
	}
	
	private void rendererOptions() {
//		RendererPrefs rendererPrefs = new RendererPrefs();
//		((JDialog) rendererPrefs.getComponent()).setLocationRelativeTo(Animator.getInstance().getComponent());
//		rendererPrefs.setVisible(true);
	}
	
	private void dumpXml() {
		System.out.println(Animator.getInstance().xml());
	}
	
	private void load() {
		JFileChooser fileChooser = new JFileChooser(JPatchUserSettings.getInstance().export.modelDirectory);
		if (fileChooser.showOpenDialog(MainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
			String filename = fileChooser.getSelectedFile().getPath();
			Animator.getInstance().NEW();
			(new AnimationImporter()).loadAnimation(filename);
		}
		Animator.getInstance().setPosition(Animator.getInstance().getStart());
		Animator.getInstance().setActiveObject(Animator.getInstance().getActiveCamera());
	}
	
	private void save() {
		JFileChooser fileChooser = new JFileChooser(JPatchUserSettings.getInstance().directories.jpatchFiles);
		if (fileChooser.showSaveDialog(MainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
			String filename = fileChooser.getSelectedFile().getPath();
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
				writer.write(Animator.getInstance().xml().toString());
				writer.close();
			} catch (IOException ioException) {
				JOptionPane.showMessageDialog(MainFrame.getInstance(),"Unable to save file \"" + filename + "\"\n" + ioException, "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private void loadTimesheet() {
		JFileChooser fileChooser = new JFileChooser(JPatchUserSettings.getInstance().directories.jpatchFiles);
		//javax.swing.filechooser.FileFilter defaultFileFilter = new JPatchFilter();
		//fileChooser.setFileFilter(defaultFileFilter);
		if (fileChooser.showOpenDialog(MainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
			String filename = fileChooser.getSelectedFile().getPath();
			Animator.getInstance().parseTimesheet(filename);
		}
	}
	
	private void loadAudioClip() {
		//Clip clip = Animator.getInstance().getClip();
		//if (clip != null)
		JFileChooser fileChooser = new JFileChooser(JPatchUserSettings.getInstance().directories.jpatchFiles);
		if (fileChooser.showOpenDialog(MainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
			try {
				AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(fileChooser.getSelectedFile());
				DataLine.Info info = new DataLine.Info(Clip.class, audioInputStream.getFormat());
				if (!AudioSystem.isLineSupported(info)) {
					System.err.println("audioformat not supported");
				} else {
					Clip clip = (Clip) AudioSystem.getLine(info);
					clip.open(audioInputStream);
					Animator.getInstance().setClip(clip);
				}
			} catch(Exception exception) {
				exception.printStackTrace();
			}
		}
	}
}
