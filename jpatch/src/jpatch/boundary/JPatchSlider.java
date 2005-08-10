package jpatch.boundary;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;

public class JPatchSlider extends JComponent
implements ChangeListener, ActionListener, FocusListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final static int LINEAR = 1;
	public final static int EXPONENTIAL = 2;
	
	private static final Color RED = new Color(255,0,0);
	private static final Color WHITE = new Color(255,255,255);
	private static NumberFormat defaultNumberFormat = new DecimalFormat("##0.00");
	private static int LABEL = 90;
	private static int HEIGHT = 20;
	private static int SLIDER = 100;
	private static int TEXT = 50;
	
	private static Dimension dimLabel = new Dimension(LABEL,HEIGHT);
	private static Dimension dimSlider = new Dimension(SLIDER,HEIGHT);
	private static Dimension dimText = new Dimension(TEXT,HEIGHT);
	
	private String strLabel;
	private float fMin;
	private float fMax;
	private float fSliderMin;
	private float fSliderMax;
	private float fValue;
	private int iScaling;
	
	private JLabel label;
	private JSlider slider;
	private JTextField textField;
	
	private boolean bUpdate = true;
	
	private NumberFormat numberFormat = defaultNumberFormat;
	
	public JPatchSlider(String name, float min, float max, float value) {
		this(name,min,max,value,LINEAR);
	}
	
	public JPatchSlider(String name, float min, float max, float value, int scaling) {
		strLabel = name;
		fMin = min;
		fMax = max;
		fSliderMin = min;
		fSliderMax = max;
		fValue = value;
		iScaling = scaling;
		
		//setSize(LABEL + SLIDER + TEXT,HEIGHT);
		//setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
		
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		label = new JLabel(strLabel);
		slider = new JSlider(0,SLIDER);
		slider.setFocusable(false);
		textField = new JTextField();
		
		springLayout.putConstraint(SpringLayout.WEST,label,0,SpringLayout.WEST,this);
		springLayout.putConstraint(SpringLayout.WEST,slider,0,SpringLayout.EAST,label);
		springLayout.putConstraint(SpringLayout.WEST,textField,0,SpringLayout.EAST,slider);
		springLayout.putConstraint(SpringLayout.EAST,textField,0,SpringLayout.EAST,this);
		
		updateTextField();
		updateSlider();
		slider.addChangeListener(this);
		textField.addActionListener(this);
		textField.addFocusListener(this);
		add(label);
		add(slider);
		add(textField);
		label.setPreferredSize(dimLabel);
		slider.setPreferredSize(dimSlider);
		textField.setPreferredSize(dimText);
		/*
		label.setBounds(0,0,LABEL,HEIGHT);
		slider.setBounds(LABEL,0,SLIDER,HEIGHT);
		textField.setBounds(LABEL + SLIDER,0,TEXT,HEIGHT);
		*/
		//numberFormat.getDecimalFormatSymbols().setDecimalSeparator('.');
	}

	public static void setNumberFormat(NumberFormat numberFormat) {
		defaultNumberFormat = numberFormat;
	}
	
	public static void setDimensions(int label, int slider, int text, int height) {
		LABEL = label;
		SLIDER = slider;
		TEXT = text;
		HEIGHT = height;dimLabel = new Dimension(LABEL,HEIGHT);
		dimSlider = new Dimension(SLIDER,HEIGHT);
		dimText = new Dimension(TEXT,HEIGHT);
	}
	
	public void setRange(float min, float max) {
		fMin = min;
		fMax = max;
		fSliderMin = min;
		fSliderMax = max;
		updateSlider();
	}
	
	public void setEnabled(boolean enabled) {
		label.setEnabled(enabled);
		slider.setEnabled(enabled);
		textField.setEnabled(enabled);
	}
	
	public void addChangeListener(ChangeListener changeListener) {
		listenerList.add(ChangeListener.class, changeListener);
	}
	
	public void removeChangeListener(ChangeListener changeListener) {
		listenerList.remove(ChangeListener.class, changeListener);
	}
	
	public ChangeListener[] getChangeListeners() {
		return (ChangeListener[])listenerList.getListeners(ChangeListener.class);
	}

	public float getValue() {
		return fValue;
	}
	
	public void setValue(float value) {
		fValue = value;
		updateSlider();
		updateTextField();
	}
	
	protected void updateSlider() {
		bUpdate = false;
		slider.setValue(sliderPosition());
		bUpdate = true;
	}
	
	protected void updateTextField() {
		textField.setText(numberFormat.format(fValue));
		//textField.setText(new Float(fValue).toString());
	}
	
	protected void fireStateChanged() {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i]==ChangeListener.class) {
				//if (changeEvent == null) {
					ChangeEvent changeEvent = new ChangeEvent(this);
				//}
				((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
			}
		}
	}
	
	protected int sliderPosition() {
		int pos = 0;
		switch(iScaling) {
			case LINEAR:
				pos = (int)((fValue - fSliderMin) / (fSliderMax - fSliderMin) * SLIDER);
			break;
			case EXPONENTIAL:
				pos = (int)(Math.log(fValue/fMin)/Math.log(fMax/fMin)*SLIDER);
			break;
		}
		if (pos < 0) {
			pos = 0;
		} else if (pos > SLIDER) {
			pos = SLIDER;
		}
		return pos;
	}
	
	protected float sliderValue() {
		switch (iScaling) {
			case LINEAR:
				return fMin + slider.getValue() * (fMax - fMin) / SLIDER;
			case EXPONENTIAL:
				return (float)(Math.exp((float)slider.getValue()/SLIDER*Math.log(fMax/fMin))*fMin);
		}
		return -1;
	}
	
	protected void clampValue() {
		if (fValue < fMin) {
			fValue = fMin;
		} else if (fValue > fMax) {
			fValue = fMax;
		}
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		try {
			fValue = numberFormat.parse(textField.getText()).floatValue();
			textField.setBackground(WHITE);
		} catch(ParseException parseException) {
			fValue = 0;
			textField.setBackground(RED);
		}
		//clampValue();
		updateTextField();
		updateSlider();
		fireStateChanged();
	}
	
	public void stateChanged(ChangeEvent changeEvent) {
		if (bUpdate) {
			fValue = sliderValue();
			updateTextField();
			fireStateChanged();
		}
	}
	
	public void focusGained(FocusEvent focusEvent) {
	}
	
	public void focusLost(FocusEvent focusEvent) {
		actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,""));
	}
}

