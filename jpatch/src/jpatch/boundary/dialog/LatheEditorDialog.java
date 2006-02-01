/*
 * $Id: LatheEditorDialog.java,v 1.4 2006/02/01 21:11:28 sascha_l Exp $ 
 */
package jpatch.boundary.dialog;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import jpatch.control.edit.*;

/**
 * 
 * @author lois
 * @version $Revision: 1.4 $
 *
 */
public class LatheEditorDialog extends JDialog implements ActionListener, ChangeListener {

	private static final long serialVersionUID = 1L;
	public static final int SEGMENTS = 8;
	
	private int iSegments = SEGMENTS;
	private int iForm = 0;
	private int iFill = 50;
	private int iSize = 100;
	private int iDistance = 0;
	private int iRotation = 0;
	private boolean bCloseTop = false;
	private boolean bCloseBottom = false;
	
	private javax.swing.JButton buttonCancel;
	private javax.swing.JButton buttonOK;
	private javax.swing.JCheckBox checkBoxCloseBottom;
	private javax.swing.JCheckBox checkBoxCloseTop;
	private javax.swing.JLabel curveLabel;
	private CurvePanel curvePanel;
	private javax.swing.JLabel distanceLabel;
	private javax.swing.JSlider distanceSlider;
	private javax.swing.JLabel fillLabel;
	private javax.swing.JSlider fillSlider;
	private javax.swing.JLabel formLabel;
	private javax.swing.JSlider formSlider;
	private javax.swing.JLabel rotateLabel;
	private javax.swing.JSlider rotateSlider;
	private javax.swing.JLabel segmentsLabel;
	private javax.swing.JTextField segmentsText;
	private javax.swing.JLabel sizeLabel;
	private javax.swing.JSlider sizeSlider;
	private JLabel labelPresets;
	private JComboBox comboBoxPresets;
	
	//private boolean okPressed = false;
	private JPatchRootEdit edit;

	/** the Lathe CurvePanel */
	private class CurvePanel extends JPanel {
		private static final long serialVersionUID = 6691264073850143334L;
		public void paintComponent(Graphics g1) {
			super.paintComponent(g1);
			Graphics2D g = (Graphics2D)g1;
			// background
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, 400, 400);
			// border
			g.setColor(new Color(0x00, 0xA0, 0xFF));
			g.drawRect(0, 0, 399, 399);
			// cross
			g.drawLine(300, 0, 300, 400);
			g.drawLine(0, 200, 400, 200);
			
			// lathe curve
			g.setColor(Color.BLACK);
			
			int ox = 0, oy = 0;
			boolean first = true;
			for (int i = 0; i < 180; i++) {
				// fill, size
				double dx = Math.sin(Math.PI*i*(100-iFill)/100/89)*iSize;
				double dy = Math.cos(Math.PI*i*(100-iFill)/100/89)*iSize;
				// form
				if (iForm >= 0) 
					dx = dx * (90-iForm)/90;
				else 
					dy = dy * (90+iForm)/90;
				// rotation
			    double dxr = dx * Math.cos(Math.PI*iRotation/180) - dy * Math.sin(Math.PI*iRotation/180);
			    double dyr = dx * Math.sin(Math.PI*iRotation/180) + dy * Math.cos(Math.PI*iRotation/180);				
				// distance
				dxr = dxr + iDistance;
				// draw line
				int ix = (int)dxr; int iy = (int)dyr;
				if (first) { 
					ox = ix; oy = iy; first = false;
					if (bCloseTop)
						g.drawLine(300-ix, 200-iy, 300, 200-iy);
				} 
				g.drawLine(300-ox, 200-oy, 300-ix, 200-iy);
				
				if (300-ix < 0) { // draw no-more-visible-mark
					g.drawLine(5, 10, 40, 10);
					g.drawLine(5, 10, 15, 5);
					g.drawLine(5, 10, 15, 15);
				}
									
				ox = ix; oy = iy;
			}
			if (bCloseBottom) 
				g.drawLine(300-ox, 200-oy, 300, 200-oy);

		}
	}

	public LatheEditorDialog(Frame owner) {
		super(owner,"Lathe Editor",true);
		
		initComponents();
		
		buttonCancel.addActionListener(this);
		buttonOK.addActionListener(this);
		formSlider.addChangeListener(this);
		fillSlider.addChangeListener(this);
		distanceSlider.addChangeListener(this);
		sizeSlider.addChangeListener(this);
		rotateSlider.addChangeListener(this);
		checkBoxCloseTop.addChangeListener(this);
		checkBoxCloseBottom.addChangeListener(this);
		comboBoxPresets.addActionListener(this);
		
		//pack();
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	public void stateChanged(ChangeEvent evt) {
		if (evt.getSource() instanceof JSlider) {
			int val = ((JSlider)evt.getSource()).getValue();
			if (evt.getSource() == formSlider) {
				formLabel.setText("<html>Form: " + val + "&deg;</html>");
				iForm = val;
			} else if (evt.getSource() == fillSlider) {
				fillLabel.setText("Fill: " + val + "%");
				iFill = 100 - val;
			} else if (evt.getSource() == distanceSlider ) {
				distanceLabel.setText("Distance: " + val);
				iDistance = val;
			} else if (evt.getSource() == sizeSlider ) {
				sizeLabel.setText("Size: " + val);
				iSize = val;
			} else if (evt.getSource() == rotateSlider ) {
				rotateLabel.setText("<html>Rotation: " + val + "&deg;</html>");
				iRotation = val;
			}
		} else if (evt.getSource() instanceof JCheckBox) {
			boolean isSelected = ((JCheckBox)evt.getSource()).isSelected();
			if (evt.getSource() == checkBoxCloseTop) 
				bCloseTop = isSelected;
			else if (evt.getSource() == checkBoxCloseBottom)
				bCloseBottom = isSelected;
		}
		curvePanel.repaint();
	}
	
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() instanceof JButton) {
		setVisible(false);
		dispose();
			if (evt.getSource() == buttonOK) {
				iSegments = (new Integer(segmentsText.getText())).intValue();
				//okPressed = true;
				// do the edit
				edit = new CompoundLatheEditor(iSegments, iForm, iFill, iSize, iDistance, iRotation, bCloseTop, bCloseBottom);
			}
		} else if (evt.getSource() instanceof JComboBox){
			String sel = (String)((JComboBox)evt.getSource()).getSelectedItem();
			if (sel.equals("Sphere")) {
				formSlider.setValue(0);
				distanceSlider.setValue(0);
				fillSlider.setValue(50);
				rotateSlider.setValue(0);
				segmentsText.setText(Integer.toString(iSegments));
			} else if (sel.equals("Cube")) {
				formSlider.setValue(90);
				distanceSlider.setValue(100);
				sizeSlider.setValue(100);
				fillSlider.setValue(50);
				rotateSlider.setValue(0);
				iSegments = 4;
				segmentsText.setText(Integer.toString(iSegments));
				checkBoxCloseTop.setSelected(true);
				checkBoxCloseBottom.setSelected(true);
			} else if (sel.equals("Tube")) {
				formSlider.setValue(90);
				distanceSlider.setValue(100);
				sizeSlider.setValue(100);
				fillSlider.setValue(50);
				rotateSlider.setValue(0);
				segmentsText.setText(Integer.toString(iSegments));
				//checkBoxCloseTop.setSelected(true);
				//checkBoxCloseBottom.setSelected(true);
			}else if (sel.equals("Cone")) {
				formSlider.setValue(90);
				distanceSlider.setValue(70);
				sizeSlider.setValue(100);
				fillSlider.setValue(50);
				rotateSlider.setValue(45);
				segmentsText.setText(Integer.toString(iSegments));
				//checkBoxCloseBottom.setSelected(true);
			}
		}
	}

	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		segmentsLabel = new javax.swing.JLabel();
		segmentsText = new javax.swing.JTextField();
		curveLabel = new javax.swing.JLabel();
		curvePanel = new CurvePanel();
		formLabel = new javax.swing.JLabel();
		formSlider = new javax.swing.JSlider();
		fillLabel = new javax.swing.JLabel();
		fillSlider = new javax.swing.JSlider();
		buttonOK = new JButton("OK", new ImageIcon(getClass()
				.getClassLoader().getResource("jpatch/images/ok.png")));
		buttonCancel = new JButton("Cancel", new ImageIcon(getClass()
				.getClassLoader().getResource("jpatch/images/cancel.png")));
		sizeLabel = new javax.swing.JLabel();
		sizeSlider = new javax.swing.JSlider();
		rotateLabel = new javax.swing.JLabel();
		rotateSlider = new javax.swing.JSlider();
		distanceLabel = new javax.swing.JLabel();
		distanceSlider = new javax.swing.JSlider();
		checkBoxCloseTop = new javax.swing.JCheckBox();
		checkBoxCloseBottom = new javax.swing.JCheckBox();
		labelPresets = new JLabel();
		comboBoxPresets = new JComboBox();

		getContentPane().setLayout(new java.awt.GridBagLayout());

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		segmentsLabel.setText("Segments:");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(10, 6, 10, 6);
		getContentPane().add(segmentsLabel, gridBagConstraints);

		segmentsText.setHorizontalAlignment(javax.swing.JTextField.CENTER);
		segmentsText.setText(Integer.toString(iSegments));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		getContentPane().add(segmentsText, gridBagConstraints);

		curveLabel.setText("Lathe Curve:");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 6);
		getContentPane().add(curveLabel, gridBagConstraints);

		curvePanel.setBackground(new java.awt.Color(255, 255, 255));
		curvePanel.setMinimumSize(new java.awt.Dimension(400, 400));
		curvePanel.setPreferredSize(new java.awt.Dimension(400, 400));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.insets = new java.awt.Insets(3, 6, 3, 6);
		getContentPane().add(curvePanel, gridBagConstraints);

		formLabel.setText("<html>Form: " + iForm + "&deg;</html>");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 2;
		getContentPane().add(formLabel, gridBagConstraints);

		formSlider.setMaximum(90);
		formSlider.setMinimum(-90);
		formSlider.setValue(iForm);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.gridwidth = 2;
		getContentPane().add(formSlider, gridBagConstraints);

		fillLabel.setText("Fill: " + iFill + "%");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 2;
		getContentPane().add(fillLabel, gridBagConstraints);

		fillSlider.setValue(iFill);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.gridwidth = 2;
		getContentPane().add(fillSlider, gridBagConstraints);

		buttonOK.setText("OK");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 8;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.insets = new java.awt.Insets(15, 0, 15, 0);
		getContentPane().add(buttonOK, gridBagConstraints);

		buttonCancel.setText("Cancel");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 8;
		gridBagConstraints.gridwidth = 2;
		getContentPane().add(buttonCancel, gridBagConstraints);

		sizeLabel.setText("Size: " + iSize);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.gridwidth = 2;
		getContentPane().add(sizeLabel, gridBagConstraints);

		sizeSlider.setMaximum(400);
		sizeSlider.setValue(iSize);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.gridwidth = 2;
		getContentPane().add(sizeSlider, gridBagConstraints);

		rotateLabel.setText("<html>Rotation: " + iRotation + "&deg;</html>");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.gridwidth = 2;
		getContentPane().add(rotateLabel, gridBagConstraints);

		rotateSlider.setMinimum(-180);
		rotateSlider.setMaximum(180);
		rotateSlider.setValue(iRotation);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.gridwidth = 2;
		getContentPane().add(rotateSlider, gridBagConstraints);

		distanceLabel.setText("Distance: " + iDistance);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 6;
		gridBagConstraints.gridwidth = 2;
		getContentPane().add(distanceLabel, gridBagConstraints);

		distanceSlider.setValue(iDistance);
		distanceSlider.setMaximum(400);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 7;
		gridBagConstraints.gridwidth = 2;
		getContentPane().add(distanceSlider, gridBagConstraints);

		checkBoxCloseTop.setText("Close Top");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 6;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(0, 35, 0, 0);
		getContentPane().add(checkBoxCloseTop, gridBagConstraints);

		checkBoxCloseBottom.setText("Close Bottom");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 7;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(0, 35, 0, 0);
		getContentPane().add(checkBoxCloseBottom, gridBagConstraints);

        labelPresets.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        labelPresets.setText("Presets:");
        labelPresets.setPreferredSize(new java.awt.Dimension(100, 15));
        getContentPane().add(labelPresets, new java.awt.GridBagConstraints());

        comboBoxPresets.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sphere", "Cube", "Tube", "Cone" }));
        comboBoxPresets.setPreferredSize(new java.awt.Dimension(150, 23));
        getContentPane().add(comboBoxPresets, new java.awt.GridBagConstraints());

		pack();
	}

	public JPatchRootEdit getEdit() {
		return edit;
	}

}
