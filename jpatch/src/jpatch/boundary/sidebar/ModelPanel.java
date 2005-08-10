package jpatch.boundary.sidebar;

//import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import jpatch.entity.*;
import jpatch.boundary.*;

public class ModelPanel extends SidePanel
implements ChangeListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7041600166377802101L;
	JPatchInput inputName;
	Model model;
	
	public ModelPanel(Model model) {
		this.model = model;
		JPatchInput.setDimensions(50,150,20);
		inputName = new JPatchInput("Name:",model.getName());
		JPanel detailPanel = MainFrame.getInstance().getSideBar().getDetailPanel();
		detailPanel.removeAll();
		detailPanel.add(inputName);
		detailPanel.repaint();
		inputName.addChangeListener(this);
	}
	
	public void stateChanged(ChangeEvent changeEvent) {
		model.setName(inputName.getStringValue());
		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodeChanged(model);
		MainFrame.getInstance().requestFocus();
	}
}

