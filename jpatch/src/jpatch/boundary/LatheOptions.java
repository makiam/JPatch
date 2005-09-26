package jpatch.boundary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import jpatch.control.edit.*;

public class LatheOptions extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private float fEpsilon;
	
	private JLabel labelSegments = new JLabel("Segments:");

	private JTextField textSegments = new JTextField("8");

	private JButton buttonOK = new JButton("OK",new ImageIcon(getClass().getClassLoader().getResource("jpatch/images/ok.png")));
	private JButton buttonCancel = new JButton("Cancel",new ImageIcon(getClass().getClassLoader().getResource("jpatch/images/cancel.png")));
	
	public LatheOptions(Frame owner, float epsilon) {
		super(owner,"Lathe options",true);
		
		fEpsilon = epsilon;
		GridLayout layout = new GridLayout(2,2);
		layout.setHgap(10);
		layout.setVgap(10);
		
		getContentPane().setLayout(layout);

		getContentPane().add(labelSegments);
		getContentPane().add(textSegments);
		getContentPane().add(buttonOK);
		getContentPane().add(buttonCancel);
		
		
		buttonCancel.addActionListener(this);
		buttonOK.addActionListener(this);
		
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
			int segments = (new Integer(textSegments.getText())).intValue();
			if (segments >=3 && segments <=256) {
				CompoundLathe edit = new CompoundLathe(MainFrame.getInstance().getSelection().getControlPointArray(), segments, fEpsilon);
				if (edit.size() > 0) {
					MainFrame.getInstance().getUndoManager().addEdit(edit);
				}
				MainFrame.getInstance().getJPatchScreen().update_all();
			}
		}
	}
	
	public static void main(String[] args) {
		while(true) {
			new LatheOptions(null, 0);
		}
	}
}
