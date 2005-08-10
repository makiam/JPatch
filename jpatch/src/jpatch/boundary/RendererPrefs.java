package jpatch.boundary;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import buoy.widget.*;
import buoy.event.*;

public class RendererPrefs extends BDialog {

	private JPatchSettings settings = JPatchSettings.getInstance();
	
	private BTabbedPane tabPane = new BTabbedPane();
	private ColumnContainer content = new ColumnContainer();
	private RowContainer buttonPane = new RowContainer();
	private BButton buttonOk = new BButton("OK");
	private BButton buttonCancel = new BButton("Cancel");
	
	private FormContainer formCommon = new FormContainer(2, 6);
	private BTextField textWidth = new BTextField("" + settings.iRenderWidth, 5);
	private BTextField textHeight = new BTextField("" + settings.iRenderHeight, 5);
	private BTextField textAspectWidth = new BTextField("" + settings.fRenderAspectWidth, 5);
	private BTextField textAspectHeight = new BTextField("" + settings.fRenderAspectHeight, 5);
	private BuoyUtils.FileSelector fsWorkingDir = new BuoyUtils.FileSelector(settings.strWorkingDir, "Select working directory", true, this);
	private BCheckBox cbDelete = new BCheckBox(null, settings.bDeleteSources);
	private BuoyUtils.ColorSelector colorBackground = new BuoyUtils.ColorSelector(settings.cBackgroundColor, this);
	private BComboBox comboRenderer = BuoyUtils.createComboBox(new String[] { "Inyo", "POV-Ray", "RenderMan" }, settings.iRenderer);
	
	private FormContainer formPovray = new FormContainer(2, 10);
	private BuoyUtils.FileSelector fsPovrayPath = new BuoyUtils.FileSelector(settings.povraySettings.strExecutable, "Locate POV-Ray executable", false, this);
	private BTextField textPovrayEnv = new BTextField(settings.povraySettings.strEnv, 30);
	private BuoyUtils.RadioSelector rsPovrayVersion = new BuoyUtils.RadioSelector(new String[] { "Unix/Cygwin", "Windows" }, settings.povraySettings.iVersion);
	private BuoyUtils.RadioSelector rsPovrayAaMethod = new BuoyUtils.RadioSelector(new String[] { "off", "method 1", "method 2" }, settings.povraySettings.iAaMethod);
	private BSlider sliderPovrayAaLevel = new BSlider(settings.povraySettings.iAaLevel, 1, 4, BSlider.HORIZONTAL);
	private BSlider sliderPovrayAaThreshold = new BSlider((int) (settings.povraySettings.fAaThreshold * 100), 0, 100, BSlider.HORIZONTAL);
	private BTextArea textPovrayInclude = new BTextArea(settings.povraySettings.strInclude, 8, 30);
	private BComboBox comboPovrayOutput = BuoyUtils.createComboBox(new String[] { "Triangles", "Bicubic patches (experimental)" }, settings.povraySettings.iOutputMode);
	private BSlider sliderPovrayTesselationQuality = new BSlider(settings.povraySettings.iSubdivMode,1,4,BSlider.HORIZONTAL);
	private BSlider sliderPovrayAaJitter = new BSlider((int) (settings.povraySettings.fAaJitter * 100),0,100,BSlider.HORIZONTAL);
	
	private FormContainer formInyo = new FormContainer(2, 13);
	private BuoyUtils.FileSelector fsInyoTexturePath = new BuoyUtils.FileSelector(settings.inyoSettings.strTexturePath, "Select Inyo texture directory", true, this);
	private BSlider sliderInyoSupersampling = new BSlider(settings.inyoSettings.iSupersample, 1, 4, BSlider.HORIZONTAL);
	private BuoyUtils.RadioSelector rsInyoSupersamplingMode = new BuoyUtils.RadioSelector(new String[] { "Adaptive", "Everything" }, settings.inyoSettings.iSamplingMode);
	private BSlider sliderInyoRecursion = new BSlider(settings.inyoSettings.iRecursion, 1, 20, BSlider.HORIZONTAL);
	private BSlider sliderInyoShadowSamples = new BSlider(settings.inyoSettings.iShadowSamples, 0, 100, BSlider.HORIZONTAL);
	private BCheckBox cbInyoTransparentShadows = new BCheckBox(null, settings.inyoSettings.bTransparentShadows);
	private BCheckBox cbInyoEnableCaustics = new BCheckBox(null, settings.inyoSettings.bEnableCaustics);
	private BCheckBox cbInyoOversampleCaustics = new BCheckBox(null, settings.inyoSettings.bOversampleCaustics);
	private BCheckBox cbInyoEnableAmbientOcclusion = new BCheckBox(null, settings.inyoSettings.bEnableAmbientOcclusion);
	private BTextField textInyoAmbientOcclusionDistance = new BTextField("" + settings.inyoSettings.fAmbientOcclusionDistance, 20);
	private BSlider sliderInyoAmbientOcclusionSamples = new BSlider(settings.inyoSettings.iAmbientOcclusionSamples, 1, 100, BSlider.HORIZONTAL);
	private BSlider sliderInyoAmbientOcclusionColorbleed = new BSlider((int) (settings.inyoSettings.fAmbientOcclusionColorbleed * 100), 0, 100, BSlider.HORIZONTAL);
	private BSlider sliderInyoTesselationQuality = new BSlider(settings.inyoSettings.iSubdivMode,1,4,BSlider.HORIZONTAL);
	
	private FormContainer formRib = new FormContainer(2, 9);
	private BuoyUtils.FileSelector fsRibPath = new BuoyUtils.FileSelector(settings.ribSettings.strExecutable, "Locate RenderMan-renderer executable", false, this);
	private BTextField textRibEnv = new BTextField(settings.ribSettings.strEnv, 30);
	private BComboBox comboRibOutput = BuoyUtils.createComboBox(new String[] { "Triangles", "Quadrilaterals", "Subdivision surface", "Bicubic patches (experimental)"}, settings.ribSettings.iOutputMode);
	private BSlider sliderRibTesselationQuality = new BSlider(settings.ribSettings.iSubdivMode,1,4,BSlider.HORIZONTAL);
	private BComboBox comboRibFilter = BuoyUtils.createComboBox(new String[] { "Box", "Triangle", "CatmullRom", "Gaussian", "Sinc" }, settings.ribSettings.iPixelFilter);
	private BTextField textRibPixelSamplesX = new BTextField("" + settings.ribSettings.iPixelSamplesX, 3);
	private BTextField textRibPixelSamplesY = new BTextField("" + settings.ribSettings.iPixelSamplesY, 3);
	private BTextField textRibPixelFilterX = new BTextField("" + settings.ribSettings.iPixelFilterX, 3);
	private BTextField textRibPixelFilterY = new BTextField("" + settings.ribSettings.iPixelFilterY, 3);
	private BTextField textRibShadingRate = new BTextField("" + settings.ribSettings.fShadingRate, 5);
	private BComboBox comboRibShadingInterpolation = BuoyUtils.createComboBox(new String[] { "Constant", "Smooth" }, settings.ribSettings.iShadingInterpolation);
	private BTextField textRibExposure = new BTextField("" + settings.ribSettings.fExposure, 5);
	
	
	public RendererPrefs() {
		super("Renderer settings");
		setModal(true);
		setResizable(false);
		addEventLink(WindowClosingEvent.class, this, "cancel");
		
		RowContainer rc;
		
		buttonCancel.addEventLink(CommandEvent.class, this, "cancel");
		buttonOk.addEventLink(CommandEvent.class, this, "ok");
		buttonPane.add(buttonOk);
		buttonPane.add(buttonCancel);
		
		LayoutInfo northeast = new LayoutInfo(LayoutInfo.NORTHEAST, LayoutInfo.NONE);
		LayoutInfo east = new LayoutInfo(LayoutInfo.EAST, LayoutInfo.NONE);
		LayoutInfo west = new LayoutInfo(LayoutInfo.NORTHWEST, LayoutInfo.NONE);
		
		int i = 0;
		formRib.add(new BLabel("Path to RIB-Renderer executable:    "), 0, i, east);
		formRib.add(fsRibPath, 1, i++, west);
		formRib.add(new BLabel("RIB-Renderer environment variables:    "), 0, i, east);
		formRib.add(textRibEnv, 1, i++, west);
		formRib.add(new BLabel("Output mesh as:    "), 0, i, east);
		formRib.add(comboRibOutput, 1, i++, west);
		formRib.add(new BLabel("Mesh density:    "), 0, i, northeast);
		formRib.add(sliderRibTesselationQuality, 1, i++, west);
		rc = new RowContainer();
		rc.add(textRibPixelSamplesX, east);
		rc.add(new BLabel(" "));
		rc.add(textRibPixelSamplesY);
		formRib.add(new BLabel("Pixel samples:    "), 0, i, east);
		formRib.add(rc, 1, i++, west);
		rc = new RowContainer();
		rc.add(comboRibFilter, east);
		rc.add(new BLabel(" "));
		rc.add(textRibPixelFilterX);
		rc.add(new BLabel(" "));
		rc.add(textRibPixelFilterY);
		formRib.add(new BLabel("Pixel filter:    "), 0, i, east);
		formRib.add(rc, 1, i++, west);
		//formRib.add(new BLabel("Exposure:    "), 0, i, east);
		//formRib.add(textRibExposure, 1, i++, west);
		formRib.add(new BLabel("Shading rate:    "), 0, i, east);
		formRib.add(textRibShadingRate, 1, i++, west);
		formRib.add(new BLabel("Shading interpolation:    "), 0, i, east);
		formRib.add(comboRibShadingInterpolation, 1, i++, west);
		
		
		
		i = 0;
		formInyo.add(new BLabel("Texture path:    "), 0, i, east);
		formInyo.add(fsInyoTexturePath, 1, i++, west);
		formInyo.add(new BLabel("Mesh density:    "), 0, i, northeast);
		formInyo.add(sliderInyoTesselationQuality, 1, i++, west);
		formInyo.add(new BLabel("Supersampling level:    "), 0, i, northeast);
		formInyo.add(sliderInyoSupersampling, 1, i++, west);
		formInyo.add(new BLabel("Supersampling method:    "), 0, i, east);
		formInyo.add(rsInyoSupersamplingMode, 1, i++, west);
		formInyo.add(new BLabel("Soft-Shadow samples:    "), 0, i, northeast);
		formInyo.add(sliderInyoShadowSamples, 1, i++, west);
		formInyo.add(new BLabel("Transparent shadows:    "), 0, i, east);
		formInyo.add(cbInyoTransparentShadows, 1, i++, west);
		formInyo.add(new BLabel("Caustics:    "), 0, i, east);
		formInyo.add(cbInyoEnableCaustics, 1, i++, west);
		formInyo.add(new BLabel("Oversample caustics:    "), 0, i, east);
		formInyo.add(cbInyoOversampleCaustics, 1, i++, west);
		formInyo.add(new BLabel("Ambient occlusion:    "), 0, i, east);
		formInyo.add(cbInyoEnableAmbientOcclusion, 1, i++, west);
		formInyo.add(new BLabel("Ambient occlusion max distance:    "), 0, i, east);
		formInyo.add(textInyoAmbientOcclusionDistance, 1, i++, west);
		formInyo.add(new BLabel("Ambient occlusion samples:    "), 0, i, northeast);
		formInyo.add(sliderInyoAmbientOcclusionSamples, 1, i++, west);
		formInyo.add(new BLabel("Ambient occlusion colorbleed:    "), 0, i, northeast);
		formInyo.add(sliderInyoAmbientOcclusionColorbleed, 1, i++, west);
		formInyo.add(new BLabel("Max. recursions:    "), 0, i, northeast);
		formInyo.add(sliderInyoRecursion, 1, i++, west);
		
		i = 0;
		rc = new RowContainer();
		rc.add(textWidth, west);
		rc.add(new BLabel(" x "), west);
		rc.add(textHeight, west);
		formCommon.add(new BLabel("Resolution:    "), 0, i, east);
		formCommon.add(rc, 1, i++, west);
		rc = new RowContainer();
		rc.add(textAspectWidth, west);
		rc.add(new BLabel(" : "), west);
		rc.add(textAspectHeight, west);
		formCommon.add(new BLabel("Aspect ratio:    "), 0, i, east);
		formCommon.add(rc, 1, i++, west);
		formCommon.add(new BLabel("Background color:    "), 0, i, east);
		formCommon.add(colorBackground, 1, i++, west);
		formCommon.add(new BLabel("Preferred Renderer:    "), 0, i, east);
		formCommon.add(comboRenderer, 1, i++, west);
		formCommon.add(new BLabel("Working Directory:    "), 0, i, east);
		formCommon.add(fsWorkingDir, 1, i++, west);
		formCommon.add(new BLabel("Delete per-frame files after rendering:    "), 0, i, east);
		formCommon.add(cbDelete, 1, i++, west);
		
		i = 0;
		formPovray.add(new BLabel("Path to POV-Ray executable:    "), 0, i, east);
		formPovray.add(fsPovrayPath, 1, i++, west);
		formPovray.add(new BLabel("POV-Ray environment variables:    "), 0, i, east);
		formPovray.add(textPovrayEnv, 1, i++, west);
		formPovray.add(new BLabel("POV-Ray version:    "), 0, i, east);
		formPovray.add(rsPovrayVersion, 1, i++, west);
		formPovray.add(new BLabel("Patch output:    "), 0, i, east);
		formPovray.add(comboPovrayOutput, 1, i++, west);
		formPovray.add(new BLabel("Mesh density:    "), 0, i, northeast);
		formPovray.add(sliderPovrayTesselationQuality, 1, i++, west);
		formPovray.add(new BLabel("Antialiasing method:    "), 0, i, east);
		formPovray.add(rsPovrayAaMethod, 1, i++, west);
		formPovray.add(new BLabel("Antialiasing level:    "), 0, i, northeast);
		formPovray.add(sliderPovrayAaLevel, 1, i++, west);
		formPovray.add(new BLabel("Antialiasing threshold:    "), 0, i, northeast);
		formPovray.add(sliderPovrayAaThreshold, 1, i++, west);
		formPovray.add(new BLabel("Jitter amount:    "), 0, i, northeast);
		formPovray.add(sliderPovrayAaJitter, 1, i++, west);
		//formPovray.add(new BLabel("Include:    "), 0, i, northeast);
		//formPovray.add(new BScrollPane(textPovrayInclude), 1, i++, west);
		
		sliderRibTesselationQuality.setSnapToTicks(true);
		sliderRibTesselationQuality.setMinorTickSpacing(1);
		sliderRibTesselationQuality.setMajorTickSpacing(2);
		sliderRibTesselationQuality.setShowTicks(true);
		sliderRibTesselationQuality.setShowLabels(true);
		
		sliderPovrayAaLevel.setSnapToTicks(true);
		sliderPovrayAaLevel.setMinorTickSpacing(1);
		sliderPovrayAaLevel.setMajorTickSpacing(3);
		sliderPovrayAaLevel.setShowTicks(true);
		sliderPovrayAaLevel.setShowLabels(true);
		sliderPovrayAaThreshold.setMinorTickSpacing(10);
		sliderPovrayAaThreshold.setMajorTickSpacing(50);
		sliderPovrayAaThreshold.setShowTicks(true);
		sliderPovrayAaThreshold.setShowLabels(true);
		sliderPovrayTesselationQuality.setSnapToTicks(true);
		sliderPovrayTesselationQuality.setMinorTickSpacing(1);
		sliderPovrayTesselationQuality.setMajorTickSpacing(2);
		sliderPovrayTesselationQuality.setShowTicks(true);
		sliderPovrayTesselationQuality.setShowLabels(true);
		sliderPovrayAaJitter.setSnapToTicks(false);
		sliderPovrayAaJitter.setMinorTickSpacing(10);
		sliderPovrayAaJitter.setMajorTickSpacing(100);
		sliderPovrayAaJitter.setShowTicks(true);
		sliderPovrayAaJitter.setShowLabels(true);
		
		sliderInyoTesselationQuality.setSnapToTicks(true);
		sliderInyoTesselationQuality.setMinorTickSpacing(1);
		sliderInyoTesselationQuality.setMajorTickSpacing(2);
		sliderInyoTesselationQuality.setShowTicks(true);
		sliderInyoTesselationQuality.setShowLabels(true);
		sliderInyoSupersampling.setSnapToTicks(true);
		sliderInyoSupersampling.setMinorTickSpacing(1);
		sliderInyoSupersampling.setMajorTickSpacing(4);
		sliderInyoSupersampling.setShowTicks(true);
		sliderInyoSupersampling.setShowLabels(true);
		sliderInyoShadowSamples.setSnapToTicks(false);
		sliderInyoShadowSamples.setMinorTickSpacing(10);
		sliderInyoShadowSamples.setMajorTickSpacing(99);
		sliderInyoShadowSamples.setShowTicks(true);
		sliderInyoShadowSamples.setShowLabels(true);
		sliderInyoAmbientOcclusionSamples.setSnapToTicks(false);
		sliderInyoAmbientOcclusionSamples.setMinorTickSpacing(10);
		sliderInyoAmbientOcclusionSamples.setMajorTickSpacing(99);
		sliderInyoAmbientOcclusionSamples.setShowTicks(true);
		sliderInyoAmbientOcclusionSamples.setShowLabels(true);
		sliderInyoRecursion.setSnapToTicks(true);
		sliderInyoRecursion.setMinorTickSpacing(1);
		sliderInyoRecursion.setMajorTickSpacing(19);
		sliderInyoRecursion.setShowTicks(true);
		sliderInyoRecursion.setShowLabels(true);
		sliderInyoAmbientOcclusionColorbleed.setSnapToTicks(false);
		sliderInyoAmbientOcclusionColorbleed.setMinorTickSpacing(10);
		sliderInyoAmbientOcclusionColorbleed.setMajorTickSpacing(50);
		sliderInyoAmbientOcclusionColorbleed.setShowTicks(true);
		sliderInyoAmbientOcclusionColorbleed.setShowLabels(true);
		
		Dictionary dict;
		dict = new Hashtable();
		dict.put(new Integer(1), new JLabel("1"));
		dict.put(new Integer(2), new JLabel("2"));
		dict.put(new Integer(3), new JLabel("3"));
		dict.put(new Integer(4), new JLabel("4"));
		((JSlider) sliderPovrayAaLevel.getComponent()).setLabelTable(dict);
		
		dict = new Hashtable();
		dict.put(new Integer(0), new JLabel("0.0"));
		dict.put(new Integer(50), new JLabel("0.5"));
		dict.put(new Integer(100), new JLabel("1.0"));
		((JSlider) sliderPovrayAaThreshold.getComponent()).setLabelTable(dict);
		((JSlider) sliderPovrayAaJitter.getComponent()).setLabelTable(dict);
		((JSlider) sliderInyoAmbientOcclusionColorbleed.getComponent()).setLabelTable(dict);
		
		dict = new Hashtable();
		dict.put(new Integer(1), new JLabel("4"));
		dict.put(new Integer(2), new JLabel("16"));
		dict.put(new Integer(3), new JLabel("64"));
		dict.put(new Integer(4), new JLabel("256"));
		((JSlider) sliderPovrayTesselationQuality.getComponent()).setLabelTable(dict);
		((JSlider) sliderRibTesselationQuality.getComponent()).setLabelTable(dict);
		((JSlider) sliderInyoTesselationQuality.getComponent()).setLabelTable(dict);
		
		dict = new Hashtable();
		dict.put(new Integer(1), new JLabel("1"));
		dict.put(new Integer(10), new JLabel("10"));
		dict.put(new Integer(20), new JLabel("20"));
		((JSlider) sliderInyoRecursion.getComponent()).setLabelTable(dict);
		
		dict = new Hashtable();
		dict.put(new Integer(1), new JLabel("1"));
		dict.put(new Integer(4), new JLabel("4"));
		((JSlider) sliderInyoSupersampling.getComponent()).setLabelTable(dict);
		
		dict = new Hashtable();
		dict.put(new Integer(0), new JLabel("0"));
		dict.put(new Integer(50), new JLabel("50"));
		dict.put(new Integer(100), new JLabel("100"));
		((JSlider) sliderInyoShadowSamples.getComponent()).setLabelTable(dict);
		((JSlider) sliderInyoAmbientOcclusionSamples.getComponent()).setLabelTable(dict);
		
		//tabPane.add(formCommon,"Common settings");
		
		tabPane.add(wrap(formInyo), "Inyo");
		tabPane.add(wrap(formPovray), "POV-Ray");
		tabPane.add(wrap(formRib), "RenderMan");
		tabPane.setSelectedTab(settings.iRenderer);
		
		content.add(formCommon, west);
		content.add(tabPane);
		content.add(buttonPane);
		setContent(content);
		pack();
		//setVisible(true);
	}
	
	private static Widget wrap(Widget w) {
		LayoutInfo l = new LayoutInfo(LayoutInfo.NORTHWEST, LayoutInfo.NONE);
		ColumnContainer c = new ColumnContainer();
		c.add(w, l);
		BScrollPane sp = new BScrollPane(c);
		sp.setPreferredViewSize(new Dimension(700, 300));
		return sp;
	}
	
	private void cancel() {
		dispose();
	}
	
	private void ok() {
		try {
			settings.iRenderer = comboRenderer.getSelectedIndex();
			settings.iRenderWidth = Integer.parseInt(textWidth.getText());
			settings.iRenderHeight = Integer.parseInt(textHeight.getText());
			settings.fRenderAspectWidth = Float.parseFloat(textAspectWidth.getText());
			settings.fRenderAspectHeight = Float.parseFloat(textAspectHeight.getText());
			settings.strWorkingDir = fsWorkingDir.getText();
			settings.cBackgroundColor = colorBackground.getColor();
			settings.bDeleteSources = cbDelete.getState();
			
			settings.ribSettings.strExecutable = fsRibPath.getText();
			settings.ribSettings.strEnv = textRibEnv.getText();
			settings.ribSettings.iSubdivMode = sliderRibTesselationQuality.getValue();
			settings.ribSettings.iOutputMode = comboRibOutput.getSelectedIndex();
			settings.ribSettings.iPixelSamplesX = Integer.parseInt(textRibPixelSamplesX.getText());
			settings.ribSettings.iPixelSamplesY = Integer.parseInt(textRibPixelSamplesY.getText());
			settings.ribSettings.iPixelFilterX = Integer.parseInt(textRibPixelFilterX.getText());
			settings.ribSettings.iPixelFilterY = Integer.parseInt(textRibPixelFilterY.getText());
			settings.ribSettings.iPixelFilter = comboRibFilter.getSelectedIndex();
			settings.ribSettings.iShadingInterpolation = comboRibShadingInterpolation.getSelectedIndex();
			settings.ribSettings.fShadingRate = Float.parseFloat(textRibShadingRate.getText());
			settings.ribSettings.fExposure = Float.parseFloat(textRibExposure.getText());
			
			settings.povraySettings.strExecutable = fsPovrayPath.getText();
			settings.povraySettings.strEnv = textPovrayEnv.getText();
			settings.povraySettings.iSubdivMode = sliderPovrayTesselationQuality.getValue();
			settings.povraySettings.iOutputMode = comboPovrayOutput.getSelectedIndex();
			settings.povraySettings.iAaMethod = rsPovrayAaMethod.getSelectedIndex();
			settings.povraySettings.iAaLevel = sliderPovrayAaLevel.getValue();
			settings.povraySettings.fAaJitter = (float) sliderPovrayAaJitter.getValue() / 100f;
			settings.povraySettings.fAaThreshold = (float) sliderPovrayAaThreshold.getValue() / 100f;
			settings.povraySettings.strInclude = textPovrayInclude.getText();
			settings.povraySettings.iVersion = rsPovrayVersion.getSelectedIndex();
			
			settings.inyoSettings.strTexturePath = fsInyoTexturePath.getText();
			settings.inyoSettings.iSupersample = sliderInyoSupersampling.getValue();
			settings.inyoSettings.iSamplingMode = rsInyoSupersamplingMode.getSelectedIndex();
			settings.inyoSettings.iSubdivMode = sliderInyoTesselationQuality.getValue();
			settings.inyoSettings.iRecursion = sliderInyoRecursion.getValue();
			settings.inyoSettings.iShadowSamples = sliderInyoShadowSamples.getValue();
			settings.inyoSettings.bTransparentShadows = cbInyoTransparentShadows.getState();
			settings.inyoSettings.bEnableCaustics = cbInyoEnableCaustics.getState();
			settings.inyoSettings.bOversampleCaustics = cbInyoOversampleCaustics.getState();
			settings.inyoSettings.bEnableAmbientOcclusion = cbInyoEnableAmbientOcclusion.getState();
			settings.inyoSettings.fAmbientOcclusionDistance = Float.parseFloat(textInyoAmbientOcclusionDistance.getText());
			settings.inyoSettings.iAmbientOcclusionSamples = sliderInyoAmbientOcclusionSamples.getValue();
			settings.inyoSettings.fAmbientOcclusionColorbleed = (float) sliderInyoAmbientOcclusionColorbleed.getValue() / 100f;
			
			settings.saveSettings();
			dispose();
			Animator.getInstance().rerenderViewports();
		} catch (NumberFormatException e) {
		}
	}
	
	public static void main(String[] args) {
		RendererPrefs rendererPrefs = new RendererPrefs();
		rendererPrefs.setVisible(true);
	}
}
