//package jpatch.boundary.dialog;
//
//import javax.swing.JOptionPane;
//
//import jpatch.boundary.JPatchSettings;
//import jpatch.boundary.MainFrame;
//import buoy.event.CommandEvent;
//import buoy.event.FocusLostEvent;
//import buoy.event.KeyReleasedEvent;
//import buoy.event.WindowClosingEvent;
//import buoy.widget.BButton;
//import buoy.widget.BDialog;
//import buoy.widget.BLabel;
//import buoy.widget.BTextField;
//import buoy.widget.ColumnContainer;
//import buoy.widget.FormContainer;
//import buoy.widget.LayoutInfo;
//import buoy.widget.RowContainer;
//
//public class KeyMapDialog extends BDialog {
//
//	private JPatchSettings settings = JPatchSettings.getInstance();
//	private String[] keyMap = JPatchSettings.getInstance().astrKeyMap;
//	private ColumnContainer content = new ColumnContainer();
//	private RowContainer buttonPane = new RowContainer();
//	private BButton buttonOk = new BButton("OK");
//	private BButton buttonCancel = new BButton("Cancel");
//	private BButton buttonReset = new BButton("Reset");
//	private BButton buttonResetToDefault = new BButton("Default");
//	private BTextField[] keyField;
//
//	private FormContainer form; // = new FormContainer(2, 3);
//	
//	public KeyMapDialog() {
//		super("Key Map");
//		setModal(true);
//		setResizable(false);
//		addEventLink(WindowClosingEvent.class, this, "cancel");
//		
//		buttonCancel.addEventLink(CommandEvent.class, this, "cancel");
//		buttonOk.addEventLink(CommandEvent.class, this, "ok");
//		buttonReset.addEventLink(CommandEvent.class, this, "reset");
//		buttonResetToDefault.addEventLink(CommandEvent.class, this, "resetToDefault");
//		buttonPane.add(buttonOk);
//		buttonPane.add(buttonCancel);
//		buttonPane.add(buttonReset);
//		buttonPane.add(buttonResetToDefault);
//		
//		LayoutInfo west = new LayoutInfo(LayoutInfo.NORTHWEST, LayoutInfo.NONE);
//		
//
//		keyField = new BTextField[keyMap.length];
//		
//		form = new FormContainer(4, keyMap.length+5);
//		form.add(new BLabel("            "), 0, 0, west);
//		form.add(new BLabel("            "), 1, 0, west);
//		form.add(new BLabel("   Action                            "), 0, 1, west);
//		form.add(new BLabel("Key                "), 1, 1, west);
//		form.add(new BLabel("   Action                         "), 2, 1, west);
//		form.add(new BLabel("Key                "), 3, 1, west);
//		form.add(new BLabel("            "), 0, 2, west);
//		form.add(new BLabel("            "), 1, 2, west);
//		for (int i = 0; i < keyMap.length/2+1; i++ ) {
//			String[] tmp = keyMap[i].split("\t+");
//			form.add(new BLabel("   " + tmp[0]), 0, i+3, west);
//			form.add(keyField[i] = new BTextField(tmp[2], 11), 1, i+3, west);
//			keyField[i].addEventLink(FocusLostEvent.class, this, "focusLostEvent");
//			keyField[i].addEventLink(KeyReleasedEvent.class, this, "keyEvent");
//		}
//		for (int i = keyMap.length/2+1; i < keyMap.length; i++ ) {
//			String[] tmp = keyMap[i].split("\t+");
//			form.add(new BLabel("   " + tmp[0]), 2, i-keyMap.length/2+2, west);
//			form.add(keyField[i] = new BTextField(tmp[2], 11), 3, i-keyMap.length/2+2, west);
//			keyField[i].addEventLink(FocusLostEvent.class, this, "focusLostEvent");
//			keyField[i].addEventLink(KeyReleasedEvent.class, this, "keyEvent");
//		}
//		form.add(new BLabel("            "), 0, keyMap.length+4, west);
//		form.add(new BLabel("            "), 1, keyMap.length+4, west);
//		
//		content.add(form, west);
//		content.add(buttonPane);
//		setContent(content);
//		pack();
//		
//	}
//	
//	private void focusLostEvent(FocusLostEvent ev) {
//		BTextField btf = ((BTextField)ev.getSource());
//		if (btf.getText().equals("")) {
//			JOptionPane.showMessageDialog(this.component,"Enter a valid key","Error",JOptionPane.ERROR_MESSAGE);
//			btf.requestFocus();
//		}
//		for (int i = 0; i < keyMap.length; i++) 
//			if (keyField[i] != null && keyField[i] != btf) 
//				if (keyField[i].getText().equals(btf.getText())) 
//					JOptionPane.showMessageDialog(this.component, "The key " + btf.getText() + " is already used",
//						"Warning:",JOptionPane.WARNING_MESSAGE);
//		
//	}
//	
//	private void keyEvent(KeyReleasedEvent ev) {
//		ev.consume();
//		int kk = ev.getKeyCode();
//		String str = "";
//		if (ev.isShiftDown()) str += "SHIFT-";
//		else if (ev.isControlDown()) str += "CTRL-";
//		else if (ev.isAltDown()) str += "ALT-";
//		if ((kk > 64 && kk < 91) ||	(kk > 47 && kk < 58)) str += Character.toString((char)kk); // A..Z, 0..9
//		else if (kk > 95 && kk < 106) str += "NUM" + (kk-96); // NUM0..NUM9
//		else if (kk > 111 && kk < 124)  str += "F" + (kk-111); // F0..F9
//		else switch (kk) {
//		case  27 : str += "ESC"; break; 
//		case 127 : str += "DEL"; break; 
//		case  10 : str += "ENTER"; break; 
//		case   8 : str += "BACKSPACE"; break; 
//		case 521 : str += "PLUS"; break; 
//		case 107 : str += "NUM+"; break; 
//		case 155 : str += "INS"; break;
//		case 16: case 17: case 18: break;
//		default  : str = "error";
//		}
//
//		if (str.equals("error")) {
//			JOptionPane.showMessageDialog(this.component,"This key or key-combination\nis not supported","Error",JOptionPane.ERROR_MESSAGE);
//			((BTextField)ev.getSource()).setText("");
//		} else
//			if (!str.equals("") && !str.matches(".*-$")) ((BTextField)ev.getSource()).setText(str);
//
//	}
//	
//	private void reset() {
//		for (int i = 0; i < keyMap.length; i++) {
//			String[] tmp = keyMap[i].split("\t+");
//			keyField[i].setText(tmp[2]);
//		}
//	}
//
//	private void resetToDefault() {
//		for (int i = 0; i < keyMap.length; i++) {
//			String[] tmp = settings.astrKeyMapDefault[i].split("\t+");
//			keyField[i].setText(tmp[2]);
//		}
//	}
//	
//	private void cancel() {
//		dispose();
//	}
//	
//	private void ok() {
//		// check for key-duplicates
//		boolean pass = true;
//		String dblKeys = "";
//		for (int i = 0; i < keyMap.length; i++) {
//			for (int j = i; j < keyMap.length; j++) {
//				if (i != j && keyField[i].getText().equals(keyField[j].getText())) {
//					pass = false;
//					dblKeys += " - " + keyField[i].getText();
//				}
//			}
//		}
//		if (pass) {
//			for (int i = 0; i < keyMap.length; i++) {
//				String[] tmp = keyMap[i].split("\t+");
//				String newMap = new String(tmp[0] + "\t" + tmp[1] + "\t" + keyField[i].getText().toUpperCase());
//				keyMap[i] = null;
//				keyField[i] = null;
//				keyMap[i] = newMap;
//				// refresh key-bindings
//				MainFrame.getInstance().getKeyAdapter().removeAllKeys();
//				MainFrame.getInstance().getMeshToolBar().addKeyBindings();
//				MainFrame.getInstance().getMainMenu().addKeyBindings();
//				MainFrame.getInstance().getMainToolBar().addKeyBindings();
//				//MainFrame.getInstance().getMorphToolBar().addKeyBindings();
//			}
//			dispose();
//		} else {
//			JOptionPane.showMessageDialog(this.component,"The key(s)" + dblKeys + " - are ambiguous","Error",JOptionPane.ERROR_MESSAGE);		
//		}
//	}
//}
