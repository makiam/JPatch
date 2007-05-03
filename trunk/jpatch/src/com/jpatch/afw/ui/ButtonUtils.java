package com.jpatch.afw.ui;

import com.jpatch.afw.attributes.Attribute;
import com.jpatch.afw.attributes.AttributeAdapter;
import com.jpatch.afw.attributes.BooleanAttr;
import com.jpatch.afw.control.JPatchAction;
import com.jpatch.afw.icons.IconSet;
import com.jpatch.afw.icons.IconSet.Style;
import com.jpatch.afw.icons.IconSet.Type;

import java.io.ObjectInputStream;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.metal.MetalButtonUI;


public class ButtonUtils {
	private final IconSet iconSet;
	
	public ButtonUtils() {
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(ClassLoader.getSystemResourceAsStream("com/jpatch/afw/icons/buttonBorders.iconset"));
			iconSet = (IconSet) ois.readObject();
			ois.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void configureActionButton(JPatchActionButton button, Style style) {
		JPatchAction action = button.getJPatchAction();
		Icon icon = action.getIcon().getObject();
		iconSet.configureButton(button, style, Type.ROUND, icon);
	}
	
	public void configureButtons(Style style, JPatchButton... buttons) {
		for (int i = 0; i < buttons.length; i++) {
			final AbstractButton button = (AbstractButton) buttons[i];
			JPatchAction action = buttons[i].getJPatchAction();
			Icon icon = action.getIcon().getObject();
			boolean first = i == 0;
			boolean last = i == buttons.length - 1;
			if (first && last) {
				if (buttons[i] instanceof JPatchToggleButton) {
					iconSet.configureButton((AbstractButton) buttons[i], style, Type.SINGLE, icon);
				} else {
					iconSet.configureButton((AbstractButton) buttons[i], style, Type.ROUND, icon);
				}
			} else if (first) {
				iconSet.configureButton((AbstractButton) buttons[i], style, Type.LEFT, icon);
			} else if (last) {
				iconSet.configureButton((AbstractButton) buttons[i], style, Type.RIGHT, icon);
			} else {
				iconSet.configureButton((AbstractButton) buttons[i], style, Type.CENTER, icon);
			}
			button.setEnabled(action.getEnabled().getBoolean());
			action.getEnabled().addAttributeListener(new AttributeAdapter() {
				@Override
				public void attributeHasChanged(Attribute source) {
					button.setEnabled(((BooleanAttr) source).getBoolean());
				}
			});
		}
	}
}
