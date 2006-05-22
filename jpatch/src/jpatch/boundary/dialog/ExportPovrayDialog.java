//package jpatch.boundary.dialog;
//
//import java.util.*;
//import java.io.*;
//import javax.swing.*;
//import buoy.widget.*;
//import buoy.event.*;
//
//import jpatch.auxilary.*;
//import jpatch.boundary.*;
//import jpatch.renderer.*;
//
//public class ExportPovrayDialog extends BDialog {
//	
//	private JPatchUserSettings settings = JPatchUserSettings.getInstance();
//	
//	private ColumnContainer content = new ColumnContainer();
//	private RowContainer buttonPane = new RowContainer();
//	private BButton buttonOk = new BButton("OK");
//	private BButton buttonCancel = new BButton("Cancel");
//	
//	private FormContainer form = new FormContainer(2, 3);
//	private BuoyUtils.FileSelector fsFile = new BuoyUtils.FileSelector(settings.povraySettings.strPath + File.separator, "Select file", false, this, settings.povraySettings.strPath);
//	private BComboBox comboMode = BuoyUtils.createComboBox(new String[] { "Triangles (mesh2)", "Bicubic patches (experimental)" }, settings.povraySettings.iOutputMode);
//	private BSlider sliderTesselationQuality = new BSlider(settings.povraySettings.iSubdivMode,1,4,BSlider.HORIZONTAL);
//	//private BCheckBox cbExportNormals = new BCheckBox(null, settings.wavefrontSettings.bExportNormals);
//	//private BCheckBox cbAverageNormals = new BCheckBox(null, settings.wavefrontSettings.bAverageNormals);
//	
//	public ExportPovrayDialog() {
//		super("Export POV-Ray [.inc] file");
//		setModal(true);
//		setResizable(false);
//		addEventLink(WindowClosingEvent.class, this, "cancel");
//		
//		buttonCancel.addEventLink(CommandEvent.class, this, "cancel");
//		buttonOk.addEventLink(CommandEvent.class, this, "ok");
//		buttonPane.add(buttonOk);
//		buttonPane.add(buttonCancel);
//		
//		//LayoutInfo northeast = new LayoutInfo(LayoutInfo.NORTHEAST, LayoutInfo.NONE);
//		LayoutInfo east = new LayoutInfo(LayoutInfo.EAST, LayoutInfo.NONE);
//		LayoutInfo west = new LayoutInfo(LayoutInfo.NORTHWEST, LayoutInfo.NONE);
//		
//		int i = 0;
//		form.add(new BLabel("File:    "), 0, i, east);
//		form.add(fsFile, 1, i++, west);
//		form.add(new BLabel("Output mode:    "), 0, i, east);
//		form.add(comboMode, 1, i++, west);
//		form.add(new BLabel("Mesh density:    "), 0, i, east);
//		form.add(sliderTesselationQuality, 1, i++, west);
//		//form.add(new BLabel("Export normals:    "), 0, i, east);
//		//form.add(cbExportNormals, 1, i++, west);
//		//form.add(new BLabel("Average normals:    "), 0, i, east);
//		//form.add(cbAverageNormals, 1, i++, west);
//		
//		sliderTesselationQuality.setSnapToTicks(true);
//		sliderTesselationQuality.setMinorTickSpacing(1);
//		sliderTesselationQuality.setMajorTickSpacing(2);
//		sliderTesselationQuality.setShowTicks(true);
//		sliderTesselationQuality.setShowLabels(true);
//		
//		Dictionary dict = new Hashtable();
//		dict.put(new Integer(1), new JLabel("4"));
//		dict.put(new Integer(2), new JLabel("16"));
//		dict.put(new Integer(3), new JLabel("64"));
//		dict.put(new Integer(4), new JLabel("256"));
//		((JSlider) sliderTesselationQuality.getComponent()).setLabelTable(dict);
//		
//		content.add(form, west);
//		content.add(buttonPane);
//		setContent(content);
//		pack();
//	}
//	
//	private void cancel() {
//		dispose();
//	}
//	
//	private void ok() {
//		try {
//			if ((new File(fsFile.getText())).isDirectory()) {
//				JOptionPane.showMessageDialog(this.getComponent(), "Please select a file");
//				return;
//			}
//			String filename = fsFile.getText();
//			if (JPatchUtils.getFileExtension(filename).equals("")) filename += ".inc";
//			
//			File file = new File(fsFile.getText());
//			String dir = file.getParent();
//			if (dir != null) settings.povraySettings.strPath = dir;
//			settings.povraySettings.iOutputMode = comboMode.getSelectedIndex();
//			settings.povraySettings.iSubdivMode = sliderTesselationQuality.getValue();
//			dispose();
//			
//			try {
//				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
//				PovrayRenderer3 povrayExport = new PovrayRenderer3();
//				povrayExport.writeModel(MainFrame.getInstance().getModel(), null, "", 0, writer);
//				writer.close();
//			} catch (IOException exception) {
//				exception.printStackTrace();
//			}
//			
//		} catch (NumberFormatException e) {
//		}
//	}
//}
//
