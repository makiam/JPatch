package jpatch.boundary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import jpatch.control.edit.*;

public class AlignOptions extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel labelPlane = new JLabel("Plane:");
	private JLabel labelPosition = new JLabel("Position:");
	private JTextField textPosition = new JTextField("0");
	private JComboBox comboPlane = new JComboBox();

	private JButton buttonOK = new JButton("OK",new ImageIcon(getClass().getClassLoader().getResource("jpatch/images/ok.png")));
	private JButton buttonCancel = new JButton("Cancel",new ImageIcon(getClass().getClassLoader().getResource("jpatch/images/cancel.png")));

	private JPanel buttonPanel = new JPanel();
	private JPanel optionsPanel = new JPanel();
	
	public AlignOptions(Frame owner) {
		super(owner,"Align options",true);
		
		GridLayout layout = new GridLayout(3,2);
		layout.setHgap(10);
		layout.setVgap(10);
		
		comboPlane.addItem("X");
		comboPlane.addItem("Y");
		comboPlane.addItem("Z");
		
		optionsPanel.setLayout(layout);

		optionsPanel.add(labelPlane);
		optionsPanel.add(comboPlane);
		optionsPanel.add(labelPosition);
		optionsPanel.add(textPosition);
		optionsPanel.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
		
		buttonPanel.add(buttonOK);
		buttonPanel.add(buttonCancel);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
		
		buttonCancel.addActionListener(this);
		buttonOK.addActionListener(this);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(optionsPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		pack();
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent actionEvent) {
		if (actionEvent.getSource() == buttonCancel) {
			setVisible(false);
			dispose();
		} else if (actionEvent.getSource() == buttonOK) {
			setVisible(false);
			dispose();
			float position = (new Float(textPosition.getText())).floatValue();
			int plane = comboPlane.getSelectedIndex() + 1;
			JPatchCompoundEdit edit = new AlignControlPointsEdit(MainFrame.getInstance().getPointSelection().getControlPointArray(), plane, position);
			MainFrame.getInstance().getUndoManager().addEdit(edit);
		}
	}
	
	//public static void main(String[] args) {
	//	JDialog dialog = new AlignOptions(null);
	//}
}
