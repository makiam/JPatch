package jpatch.boundary.sidebar;

import javax.swing.*;

import jpatch.boundary.*;
import jpatch.boundary.action.*;
import jpatch.entity.*;

public class SelectionsPanel extends SidePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3589871917606717466L;

	public SelectionsPanel(Model model) {
		JButton buttonAddSelection = new JButton(new NewSelectionAction(model));
//		buttonAddSelection.setFocusable(false);
		add(buttonAddSelection);

		JButton buttonSortSelection = new JButton(new SortSelectionAction());
//		buttonSortSelection.setFocusable(false);
		add(buttonSortSelection);
		JButton buttonDelDefSelection = new JButton(new DeleteDefaultSelectionsAction());
//		buttonSortSelection.setFocusable(false);
		add(buttonDelDefSelection);
		
		MainFrame.getInstance().getSideBar().clearDetailPanel();		

		//add(new JButton("test2"));
		//add(new JButton("test3"));
		//add(new JButton("test4"));
	}
}

