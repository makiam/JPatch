package jpatch.boundary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import jpatch.boundary.settings.Settings;

public class GridDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JLabel labelSpacing = new JLabel("Spacing:");

	private JTextField textSpacing = new JTextField();

	private JButton buttonOK = new JButton("OK",new ImageIcon(getClass().getClassLoader().getResource("jpatch/images/ok.png")));
	private JButton buttonCancel = new JButton("Cancel",new ImageIcon(getClass().getClassLoader().getResource("jpatch/images/cancel.png")));
	
	public GridDialog(Frame owner) {
		super(owner,"oOoOoOo Grid settings",true);
		
		if (MainFrame.getInstance().getModel() != null)
			textSpacing.setText(Float.toString(Settings.getInstance().viewports.modelerGridSpacing));
		else
			textSpacing.setText(Float.toString(Settings.getInstance().viewports.animatorGridSpacing));
		
		GridLayout layout = new GridLayout(2,2);
		layout.setHgap(10);
		layout.setVgap(10);
		
		getContentPane().setLayout(layout);

		getContentPane().add(labelSpacing);
		getContentPane().add(textSpacing);
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
//			MainFrame.getInstance().getJPatchScreen().setGridSpacing(new Float(textSpacing.getText()).floatValue());
			if (MainFrame.getInstance().getModel() != null)
				Settings.getInstance().viewports.modelerGridSpacing = new Float(textSpacing.getText()).floatValue();
			else
				Settings.getInstance().viewports.animatorGridSpacing = new Float(textSpacing.getText()).floatValue();
			
			MainFrame.getInstance().getJPatchScreen().update_all();
			
			System.out.println(textSpacing.getText() + " " + Settings.getInstance().viewports.modelerGridSpacing);
		}
	}
}
