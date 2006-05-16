package jpatch.boundary.sidebar;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.boundary.action.*;
import jpatch.boundary.ui.*;

public class CameraPanel extends SidePanel
implements ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6771633567920818871L;
	JPatchInput inputName;
	Camera camera;
	public CameraPanel(Camera camera) {
		this.camera = camera;
		add(new JButton(new EditAnimObjectAction(camera)));
		add(new JButton(new DeleteAnimObjectAction(camera)));
		JPatchInput.setDimensions(50,150,20);
		inputName = new JPatchInput("Name:",camera.getName());
		JPanel detailPanel = MainFrame.getInstance().getSideBar().getDetailPanel();
		detailPanel.removeAll();
		detailPanel.add(inputName);
		inputName.addChangeListener(this);
		detailPanel.repaint();
	}
	
	public void stateChanged(ChangeEvent changeEvent) {
		camera.setName(inputName.getStringValue());
		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodeChanged(camera);
		MainFrame.getInstance().requestFocus();
	}
}
