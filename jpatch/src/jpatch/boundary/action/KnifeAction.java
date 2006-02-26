package jpatch.boundary.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import jpatch.boundary.MainFrame;
import jpatch.boundary.Selection;
import jpatch.boundary.mouse.KnifeMouseAdapter;


/**
 * gives access to the knife tool.
 * @author torf
 * @see jpatch.boundary.mouse.KnifeMouseAdapter
 */

public class KnifeAction extends AbstractAction {
	
	private static final long serialVersionUID = 1L;
	
	public KnifeAction() {		
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/gear.png")));
		putValue(Action.SHORT_DESCRIPTION, "cut through selected curves");
	}

	public void actionPerformed(ActionEvent arg0) {
		//System.out.println("Knife tool activated");
		
		Selection selection = MainFrame.getInstance().getSelection();
		if (selection!=null && !selection.isSingle()) {
			MainFrame.getInstance().setHelpText("Click and drag mouse to draw a cutting line. Release mouse button to cut. Press right mouse button to cancel.");
			MainFrame.getInstance().getJPatchScreen().enablePopupMenu(false);
			MainFrame.getInstance().getJPatchScreen().setTool(null);	// remove other tools
			MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners(); //Remove other mouselisteners
			MainFrame.getInstance().getJPatchScreen().addMouseListeners(new KnifeMouseAdapter()); //Activate knife tool
		}
	}

}
