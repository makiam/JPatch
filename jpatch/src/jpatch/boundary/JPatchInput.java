package jpatch.boundary;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;

public class JPatchInput extends JComponent
implements ActionListener, FocusListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Color RED = new Color(255,0,0);
	private static final Color WHITE = new Color(255,255,255);
	
	public static final int STRING = 1;
	public static final int FLOAT = 2;
	public static final int INTEGER = 3;
	
	private static int LABEL = 100;
	private static int HEIGHT = 20;
	private static int TEXT = 100;
	
	private static Dimension dimLabel = new Dimension(LABEL,HEIGHT);
	private static Dimension dimText = new Dimension(TEXT,HEIGHT);
		
	private JLabel label;
	private JTextField textField;
	
	private int iType;
	
	private float fValue;
	private int iValue;
	
	private NumberFormat numberFormat = null;
	
	public JPatchInput(String name, float value) {
		this(name,"");
		iType = FLOAT;
		numberFormat = new DecimalFormat("##0.00");
		fValue = value;
		updateTextField();
	}
	
	public JPatchInput(String name, int value) {
		this(name,"");
		iType = INTEGER;
		numberFormat = new DecimalFormat("#####0");
		iValue = value;
		updateTextField();
	}
	
	public JPatchInput(String name, String text) {
		label = new JLabel(name);
		textField = new JTextField(text);
		iType = STRING;
		
		//setSize(LABEL + SLIDER + TEXT,HEIGHT);
		setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
		//label = new JLabel(strLabel);
		//textField = new JTextField();
		
		textField.addActionListener(this);
		textField.addFocusListener(this);
		add(label);
		add(textField);
		label.setPreferredSize(dimLabel);
		textField.setPreferredSize(dimText);
		/*
		label.setBounds(0,0,LABEL,HEIGHT);
		slider.setBounds(LABEL,0,SLIDER,HEIGHT);
		textField.setBounds(LABEL + SLIDER,0,TEXT,HEIGHT);
		*/
		//numberFormat.getDecimalFormatSymbols().setDecimalSeparator('.');
	}

	public static void setDimensions(int label, int text, int height) {
		LABEL = label;
		TEXT = text;
		HEIGHT = height;
		dimLabel = new Dimension(LABEL,HEIGHT);
		dimText = new Dimension(TEXT,HEIGHT);
	}
	
	public void setEnabled(boolean enabled) {
		label.setEnabled(enabled);
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

	public float getFloatValue() {
		return fValue;
	}
	
	public int getIntValue() {
		return iValue;
	}
	
	public String getStringValue() {
		return textField.getText();
	}
	
	protected void updateTextField() {
		switch (iType) {
			case FLOAT:
				textField.setText(numberFormat.format(fValue));
				break;
			case INTEGER:
				textField.setText(numberFormat.format(iValue));
				break;
		}
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
	
	public void actionPerformed(ActionEvent actionEvent) {
		try {
			switch (iType) {
				case FLOAT:
					fValue = numberFormat.parse(textField.getText()).floatValue();
					break;
				case INTEGER:
					iValue = numberFormat.parse(textField.getText()).intValue();
					break;
			}
			textField.setBackground(WHITE);
		} catch(ParseException parseException) {
			fValue = 0;
			iValue = 0;
			textField.setBackground(RED);
		}
		//clampValue();
		updateTextField();
		fireStateChanged();
	}
	
	public void focusGained(FocusEvent focusEvent) {
	}
	
	public void focusLost(FocusEvent focusEvent) {
		actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,""));
	}
}

