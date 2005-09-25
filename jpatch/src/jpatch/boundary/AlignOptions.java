package jpatch.boundary;

import javax.swing.*;
import javax.vecmath.*;

import java.awt.*;
import java.awt.event.*;
import jpatch.control.edit.*;
import jpatch.entity.*;

public class AlignOptions extends JDialog implements ActionListener {
	public static final int XPLANE = 1;
	public static final int YPLANE = 2;
	public static final int ZPLANE = 3;
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
			float value = (new Float(textPosition.getText())).floatValue();
			int plane = comboPlane.getSelectedIndex() + 1;
			
			JPatchActionEdit edit = new JPatchActionEdit("align controlpoints");
			ControlPoint[] acp = MainFrame.getInstance().getSelection().getControlPointArray();
			edit.addEdit(new AtomicMoveControlPoints(acp));
			for (int i = 0; i < acp.length; i++) {
				Point3f p3 = acp[i].getPosition();
				switch (plane) {
					case XPLANE:
						p3.x = value;
						break;
					case YPLANE:
						p3.y = value;
						break;
					case ZPLANE:
						p3.z = value;
						break;
				}
				acp[i].setPosition(p3);
			}
			MainFrame.getInstance().getUndoManager().addEdit(edit);
		}
	}
	
	//public static void main(String[] args) {
	//	JDialog dialog = new AlignOptions(null);
	//}
}
