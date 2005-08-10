package jpatch.boundary;

import javax.swing.*;
import buoy.widget.*;
import buoy.event.*;
import jpatch.entity.*;

public class PhonemeMappingEditor extends BDialog {
	String[] astrPhoneme = new String[] {
		"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "closed"
	};
	BComboBox[] cbMorphs = new BComboBox[astrPhoneme.length];
	Object[] aMorphs;
	
	public PhonemeMappingEditor() {
		super("Phoneme to morph mapping");
		((JDialog) getComponent()).setModal(true);
		Model model = MainFrame.getInstance().getModel();
		aMorphs = new Object[model.getMorphList().size() + 1];
		aMorphs[0] = "neutral";
		for (int i = 1; i < aMorphs.length; i++) {
			aMorphs[i] = (Morph) model.getMorphList().get(i - 1);
		}
		FormContainer form = new FormContainer(6, astrPhoneme.length / 3);
		for (int i = 0; i < astrPhoneme.length; i++) {
			cbMorphs[i] = new BComboBox(aMorphs);
			Morph morph = model.getMorphFor(astrPhoneme[i]);
			if (morph != null) cbMorphs[i].setSelectedValue(morph);
			if (i < astrPhoneme.length / 3) {
				form.add(new BLabel("     " + astrPhoneme[i] + ": "), 0, i);
				form.add(cbMorphs[i], 1, i);
			} else if (i < astrPhoneme.length * 2 / 3) {
				form.add(new BLabel("     " + astrPhoneme[i] + ": "), 2, i - astrPhoneme.length / 3);
				form.add(cbMorphs[i], 3, i - astrPhoneme.length / 3);
			} else {
				form.add(new BLabel("     " + astrPhoneme[i] + ": "), 4, i - astrPhoneme.length * 2 / 3);
				form.add(cbMorphs[i], 5, i - astrPhoneme.length * 2/ 3);
			}
		}
		RowContainer buttons = new RowContainer();
		BButton buttonOK = new BButton("OK");
		BButton buttonCancel = new BButton("Cancel");
		buttons.add(buttonOK);
		buttons.add(buttonCancel);
		ColumnContainer content = new ColumnContainer();
		content.add(form);
		content.add(buttons);
		
		setContent(content);
		pack();
		
		addEventLink(WindowClosingEvent.class, this, "dispose");
		buttonCancel.addEventLink(CommandEvent.class, this, "dispose");
		buttonOK.addEventLink(CommandEvent.class, this, "set");
		setVisible(true);
	}
	
	private void set() {
		for (int i = 0; i < astrPhoneme.length; i++) {
			Object o = cbMorphs[i].getSelectedValue();
			if (!(o instanceof Morph)) o = null;
			Morph morph = (Morph) o;
			MainFrame.getInstance().getModel().setMorphFor(astrPhoneme[i], morph);
		}
		dispose();
	}
}
