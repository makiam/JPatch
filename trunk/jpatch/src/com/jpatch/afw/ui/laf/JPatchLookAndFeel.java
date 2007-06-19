package com.jpatch.afw.ui.laf;

import java.awt.Color;

import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

import jpatch.boundary.laf.SmoothBorders;
import jpatch.boundary.laf.SmoothIconFactory;

public class JPatchLookAndFeel extends MetalLookAndFeel {
	private static final String PACKAGE = "com.jpatch.afw.ui.laf.";
	
	protected void initComponentDefaults(UIDefaults uidefaults) {
		super.initComponentDefaults(uidefaults);
		Object classMap[] = {
				    "ComboBox.background", Color.WHITE,
//                    "Button.border", SmoothBorders.buttonBorder,
//                    "ToggleButton.border", SmoothBorders.buttonBorder,
//                    "ToolBar.rolloverBorder", SmoothBorders.buttonBorder,
//                    "ToolBar.nonrolloverBorder", SmoothBorders.buttonBorder,
//                    "RadioButton.icon", radioButtonIcon,
//                    "TextField.border", SmoothBorders.textFieldBorder,
//                    
//// add in dialog icons
//                    "OptionPane.errorIcon", LookAndFeel.makeIcon(MetalLookAndFeel.class, "icons/Error.gif"),
//                    "OptionPane.informationIcon", LookAndFeel.makeIcon(MetalLookAndFeel.class, "icons/Inform.gif"),
//                    "OptionPane.warningIcon", LookAndFeel.makeIcon(MetalLookAndFeel.class, "icons/Warn.gif"),
//                    "OptionPane.questionIcon", LookAndFeel.makeIcon(MetalLookAndFeel.class, "icons/Question.gif"),
                    
				    "TextField.inactiveForeground", new ColorUIResource(new Color(0x999999)),
				    "TextField.border", new TextFieldBorder(),
                    "ToolTip.background", new ColorUIResource(new Color(1.0f, 1.0f, 0.75f)),
                    "ToolTip.foreground", new ColorUIResource(Color.BLACK),
                    "ToolTip.border", new BorderUIResource(new LineBorder(Color.BLACK)),
                    "ToolTip.backgroundInactive", new ColorUIResource(new Color(0.7f, 0.7f, 0.7f)),
                    "ToolTip.foregroundInactive", new ColorUIResource(new Color(0.3f, 0.3f, 0.3f)),
                    "ToolTip.borderInactive", new BorderUIResource(new LineBorder(new Color(0.3f, 0.3f, 0.3f))),
                };
        uidefaults.putDefaults(classMap);
    }
	
	protected void initClassDefaults(UIDefaults uidefaults) {
        super.initClassDefaults(uidefaults);
        Object classMap[] = {
        		"SliderUI", PACKAGE + "SliderUI",
        		"CheckBoxUI", PACKAGE + "CheckBoxUI"
        };
        uidefaults.putDefaults(classMap);
    }
	
	public String getID() {
        return "JPatch";
    }

    public String getDescription() {
        return "The JPatch Look and Feel";
    }

    public String getName() {
        return "JPatch";
    }
}
