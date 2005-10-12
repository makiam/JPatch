//package jpatch.boundary.action;
//
//import java.awt.event.ActionEvent;
//
//import javax.swing.AbstractAction;
//import javax.swing.JDialog;
//
//import jpatch.boundary.MainFrame;
//import jpatch.boundary.dialog.KeyMapDialog;
//
//
//public class KeyMapAction extends AbstractAction {
//
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = 6484761366205611306L;
//
//	public KeyMapAction() {
//		super("Key Map...");
//	}
//	
//	public void actionPerformed(ActionEvent actionEvent) {
//		KeyMapDialog dialog = new KeyMapDialog();
//		((JDialog) dialog.getComponent()).setLocationRelativeTo(MainFrame.getInstance());
//		dialog.setVisible(true);
//	}
//}
