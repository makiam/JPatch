package jpatch.boundary;

import java.text.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;

import jpatch.entity.*;

public class MaterialEditor extends JDialog
implements ChangeListener, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MaterialProperties materialProperties;
	private MaterialProperties oldMaterialProperties;
	private String strPovRay;
	private String strRenderMan;
	private JPatchMaterial material;
	private JButton buttonOk;
	private JButton buttonCancel;
	private JButton buttonApply;
	private JButton buttonColor;
	
	private JPatchSlider sliderRed;
	private JPatchSlider sliderGreen;
	private JPatchSlider sliderBlue;
	private JPatchSlider sliderTransmit;
	private JPatchSlider sliderFilter;
	private JPatchSlider sliderAmbient;
	private JPatchSlider sliderDiffuse;
	private JPatchSlider sliderBrilliance;
	private JPatchSlider sliderSpecular;
	private JPatchSlider sliderRoughness;
	private JPatchSlider sliderMetallic;
	private JPatchSlider sliderReflectionMin;
	private JPatchSlider sliderReflectionMax;
	private JPatchSlider sliderReflectionFalloff;
	private JPatchSlider sliderRefraction;
	private RealtimeRaytracer trace;
	private JCheckBox checkboxLoRes = new JCheckBox("Lo-Res",true);
	private JCheckBox checkboxConserveEnergy = new JCheckBox("conserve energy",true);
	JTextArea[] atextArea = new JTextArea[3];
	
	public MaterialEditor(Frame owner, JPatchMaterial material) {
		super(MainFrame.getInstance(),"JPatch material editor - " + material.getName(),true);
		//setUndecorated(true);
		//getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
		//SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(new BorderLayout());
		this.material = material;
		materialProperties = new MaterialProperties(material.getMaterialProperties());
		oldMaterialProperties = new MaterialProperties(materialProperties);
		buttonOk = new JButton("OK");
		buttonCancel = new JButton("Cancel");
		buttonApply = new JButton("Apply");
		buttonColor = new JButton("Pick a color...");
		JPanel panelSliders = new JPanel();
		JPanel panelTop = new JPanel();
		JPanel panelTrace = new JPanel();
		JPanel panelButtons = new JPanel();
		JTabbedPane tabPane = new JTabbedPane();
		strPovRay = material.getRenderString("povray","3.5");
		strRenderMan = material.getRenderString("renderman","3.1");
		atextArea[0] = new JTextArea(material.getRenderString("povray","3.5"));
		atextArea[1] = new JTextArea(material.getRenderString("renderman","3.1"));
		atextArea[2] = new JTextArea(material.getRenderString("inyo","0.0"));
		for (int i = 0; i < atextArea.length; i++) {
			atextArea[i].setRows(15);
			//atextArea[i].setFont(new Font("Monospaced",Font.PLAIN,14));
		}
		panelSliders.setLayout(new GridLayout(8,2,20,0));
		
		//panelTrace.setLayout(new BoxLayout(panelTrace,BoxLayout.Y_AXIS));
		JPatchSlider.setDimensions(90,100,50,20);
		JPatchSlider.setNumberFormat(new DecimalFormat("##0.00###"));
		sliderRed = new JPatchSlider("red",0,1,materialProperties.red);
		sliderGreen = new JPatchSlider("green",0,1,materialProperties.green);
		sliderBlue = new JPatchSlider("blue",0,1,materialProperties.blue);
		sliderTransmit= new JPatchSlider("transmit",0,1,materialProperties.transmit);
		sliderFilter= new JPatchSlider("filter",0,1,materialProperties.filter);
		sliderAmbient = new JPatchSlider("ambient",0,1,materialProperties.ambient);
		sliderDiffuse = new JPatchSlider("diffuse",0,1,materialProperties.diffuse);
		sliderBrilliance = new JPatchSlider("brilliance",0.1f,10f,materialProperties.brilliance,JPatchSlider.EXPONENTIAL);
		sliderSpecular = new JPatchSlider("specular",0,5,materialProperties.specular);
		sliderRoughness = new JPatchSlider("roughness",0.0001f,0.1f,materialProperties.roughness,JPatchSlider.EXPONENTIAL);
		sliderMetallic = new JPatchSlider("metallic",0,1,materialProperties.metallic);
		sliderReflectionMin = new JPatchSlider("reflection min",0,1,materialProperties.reflectionMin);
		sliderReflectionMax = new JPatchSlider("reflection max",0,1,materialProperties.reflectionMax);
		sliderReflectionFalloff = new JPatchSlider("refl. falloff",0.1f,10f,materialProperties.reflectionFalloff,JPatchSlider.EXPONENTIAL);
		sliderRefraction = new JPatchSlider("refraction idx.",1,2.5f,materialProperties.refraction);
		trace = new RealtimeRaytracer(materialProperties);
		panelTrace.setBorder(BorderFactory.createEtchedBorder());
		panelSliders.add(sliderRed);
		panelSliders.add(sliderSpecular);
		panelSliders.add(sliderGreen);
		panelSliders.add(sliderRoughness);
		panelSliders.add(sliderBlue);
		panelSliders.add(sliderMetallic);
		panelSliders.add(sliderFilter);
		panelSliders.add(sliderReflectionMin);
		panelSliders.add(sliderTransmit);
		panelSliders.add(sliderReflectionMax);
		panelSliders.add(sliderAmbient);
		panelSliders.add(sliderReflectionFalloff);
		panelSliders.add(sliderDiffuse);
		panelSliders.add(sliderRefraction);
		panelSliders.add(sliderBrilliance);
		panelSliders.add(checkboxConserveEnergy);
		panelSliders.setBorder(BorderFactory.createEtchedBorder());
		sliderRed.addChangeListener(this);
		sliderGreen.addChangeListener(this);
		sliderBlue.addChangeListener(this);
		sliderTransmit.addChangeListener(this);
		sliderFilter.addChangeListener(this);
		sliderAmbient.addChangeListener(this);
		sliderDiffuse.addChangeListener(this);
		sliderBrilliance.addChangeListener(this);
		sliderSpecular.addChangeListener(this);
		sliderRoughness.addChangeListener(this);
		sliderMetallic.addChangeListener(this);
		sliderReflectionMin.addChangeListener(this);
		sliderReflectionMax.addChangeListener(this);
		sliderReflectionFalloff.addChangeListener(this);
		sliderRefraction.addChangeListener(this);
		checkboxLoRes.addChangeListener(this);
		checkboxConserveEnergy.setSelected(materialProperties.conserveEnergy);
		checkboxConserveEnergy.addChangeListener(this);
		//addComponentBounds(trace,260,0,160,160);
		//addComponentBounds(checkboxLoRes,260,160,160,20);
		//addComponentBounds(checkboxConserveEnergy,260,180,160,20);
		panelTop.setLayout(new BorderLayout());
		panelTop.add(panelSliders, BorderLayout.CENTER);
		panelTrace.setLayout(new BorderLayout());
		panelTrace.add(trace, BorderLayout.CENTER);
		panelTrace.add(checkboxLoRes, BorderLayout.SOUTH);
		panelTop.add(panelTrace, BorderLayout.EAST);
		getContentPane().add(panelTop, BorderLayout.NORTH);
		//tabPane.addTab("Color chooser", new JColorChooser());
		tabPane.addTab("POV-Ray",new JScrollPane(atextArea[0]));
		tabPane.addTab("RenderMan",new JScrollPane(atextArea[1]));
		tabPane.addTab("Inyo",new JScrollPane(atextArea[2]));
		getContentPane().add(tabPane, BorderLayout.CENTER);
		
		panelButtons.add(buttonOk);
		panelButtons.add(buttonApply);
		panelButtons.add(buttonColor);
		panelButtons.add(buttonCancel);
		getContentPane().add(panelButtons, BorderLayout.SOUTH);
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		buttonOk.addActionListener(this);
		buttonCancel.addActionListener(this);
		buttonApply.addActionListener(this);
		buttonColor.addActionListener(this);
		
		//springLayout.putConstraint(SpringLayout.NORTH,panelTop,0,SpringLayout.NORTH,getContentPane());
		//springLayout.putConstraint(SpringLayout.SOUTH,panelTop,0,SpringLayout.NORTH,tabPane);
		//springLayout.putConstraint(SpringLayout.SOUTH,tabPane,0,SpringLayout.NORTH,panelButtons);
		//springLayout.putConstraint(SpringLayout.SOUTH,panelButtons,0,SpringLayout.SOUTH,getContentPane());
		//Component[] aComp = getContentPane().getComponents();
		//for (int c = 0; c < aComp.length; c++) {
		//	springLayout.putConstraint(SpringLayout.WEST,aComp[c],0,SpringLayout.WEST,getContentPane());
		//	springLayout.putConstraint(SpringLayout.EAST,aComp[c],0,SpringLayout.EAST,getContentPane());
		//}
		//SpringLayout topLayout = new SpringLayout();
		//panelTop.setLayout(topLayout);
		//topLayout.putConstraint(SpringLayout.WEST,panelSliders,0,SpringLayout.WEST,panelTop);
		//topLayout.putConstraint(SpringLayout.EAST,panelSliders,0,SpringLayout.WEST,panelTrace);
		//topLayout.putConstraint(SpringLayout.EAST,panelTrace,0,SpringLayout.EAST,panelTop);
		//
		//setDefaultLookAndFeelDecorated(false);
		setSize(700,500);
		setLocationRelativeTo(owner);
	}
	
	public void stateChanged(ChangeEvent changeEvent) {
		if (changeEvent.getSource() == sliderRed) {
			materialProperties.red = ((JPatchSlider)changeEvent.getSource()).getValue();
		} else if (changeEvent.getSource() == sliderGreen) {
			materialProperties.green = ((JPatchSlider)changeEvent.getSource()).getValue();
		} else if (changeEvent.getSource() == sliderBlue) {
			materialProperties.blue = ((JPatchSlider)changeEvent.getSource()).getValue();
		} else if (changeEvent.getSource() == sliderTransmit) {
			materialProperties.transmit = ((JPatchSlider)changeEvent.getSource()).getValue();
		} else if (changeEvent.getSource() == sliderFilter) {
			materialProperties.filter = ((JPatchSlider)changeEvent.getSource()).getValue();
		} else if (changeEvent.getSource() == sliderAmbient) {
			materialProperties.ambient = ((JPatchSlider)changeEvent.getSource()).getValue();
		} else if (changeEvent.getSource() == sliderDiffuse) {
			materialProperties.diffuse = ((JPatchSlider)changeEvent.getSource()).getValue();
		} else if (changeEvent.getSource() == sliderBrilliance) {
			materialProperties.brilliance = ((JPatchSlider)changeEvent.getSource()).getValue();
		} else if (changeEvent.getSource() == sliderSpecular) {
			materialProperties.specular = ((JPatchSlider)changeEvent.getSource()).getValue();
		} else if (changeEvent.getSource() == sliderRoughness) {
			materialProperties.roughness = ((JPatchSlider)changeEvent.getSource()).getValue();
		} else if (changeEvent.getSource() == sliderMetallic) {
			materialProperties.metallic = ((JPatchSlider)changeEvent.getSource()).getValue();
		} else if (changeEvent.getSource() == sliderReflectionMin) {
			materialProperties.reflectionMin = ((JPatchSlider)changeEvent.getSource()).getValue();
		} else if (changeEvent.getSource() == sliderReflectionMax) {
			materialProperties.reflectionMax = ((JPatchSlider)changeEvent.getSource()).getValue();
		} else if (changeEvent.getSource() == sliderReflectionFalloff) {
			materialProperties.reflectionFalloff = ((JPatchSlider)changeEvent.getSource()).getValue();
		} else if (changeEvent.getSource() == sliderRefraction) {
			materialProperties.refraction = ((JPatchSlider)changeEvent.getSource()).getValue();
		} else if (changeEvent.getSource() == checkboxLoRes) {
			trace.setLoRes(((JCheckBox)changeEvent.getSource()).isSelected());
		} else if (changeEvent.getSource() == checkboxConserveEnergy) {
			materialProperties.conserveEnergy = ((AbstractButton)changeEvent.getSource()).isSelected();
			//trace.setConserveEnergy(((JCheckBox)changeEvent.getSource()).isSelected());
		}
		trace.repaint();
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		if (actionEvent.getSource() == buttonOk) {
			commit();
			dispose();
		} else if (actionEvent.getSource() == buttonCancel) {
			revert();
			dispose();
		} else if (actionEvent.getSource() == buttonApply) {
			commit();
		} else if (actionEvent.getSource() == buttonColor) {
			Color color = JColorChooser.showDialog(this, "", materialProperties.getColor());
			if (color != null) {
				sliderRed.setValue((float) color.getRed() / 255f);
				sliderGreen.setValue((float) color.getGreen() / 255f);
				sliderBlue.setValue((float) color.getBlue() / 255f);
				materialProperties.red = sliderRed.getValue();
				materialProperties.green = sliderGreen.getValue();
				materialProperties.blue = sliderBlue.getValue();
				trace.repaint();
			}
		}
	}
	
	private void commit() {
		material.getMaterialProperties().set(materialProperties);
		material.setRenderString("povray","3.5",atextArea[0].getText());
		material.setRenderString("renderman","3.1",atextArea[1].getText());
		material.setRenderString("inyo","0.0",atextArea[2].getText());
		MainFrame.getInstance().getJPatchScreen().update_all();
	}
	
	private void revert() {
		material.getMaterialProperties().set(oldMaterialProperties);
		material.setRenderString("povray","3.5",strPovRay);
		material.setRenderString("renderman","3.1",strRenderMan);
		material.setRenderString("inyo","0.0",strRenderMan);
		MainFrame.getInstance().getJPatchScreen().update_all();
	}
}

