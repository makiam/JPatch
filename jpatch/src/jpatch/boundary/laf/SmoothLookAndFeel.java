package jpatch.boundary.laf;

import javax.swing.plaf.metal.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Smooth Metal Look And Feel. An enhanced version of the standard
 * Metal Look And Feel by Sun Microsystems. This version uses the
 * capabilities of the Java 2D API to create better looking, fully
 * anti-aliased versions of the controls.
 * <p/>
 * It is mainly intended as a technology demonstration and research
 * project. The code is not optimized for speed or stability. Use it
 * at your own risk.
 *
 * @author Marcel Offermans
 */
public class SmoothLookAndFeel extends MetalLookAndFeel {

    protected static final String PACKAGE = "jpatch.boundary.laf.";

    protected void initComponentDefaults(UIDefaults uidefaults) {
        super.initComponentDefaults(uidefaults);

        // first line doesn't work, second does, third is a workaround
//		Object buttonBorder = new UIDefaults.ProxyLazyValue("smooth.metal.SmoothBorders", "getButtonBorder");
//		Object buttonBorder = new UIDefaults.ProxyLazyValue("javax.swing.plaf.metal.MetalBorders", "getButtonBorder");
        Object buttonBorder = SmoothBorders.getButtonBorder();

//        Object buttonBorder = new BorderUIResource.CompoundBorderUIResource(new EtchedBorder(), new BasicBorders.MarginBorder());
        
        // see above, don't use the ProxyLazyValue construction for now, because it
        // does not seem to work very well
        Object radioButtonIcon = SmoothIconFactory.getRadioButtonIcon();

        // create a map of all the features we want to modify and install them
        Object classMap[] =
                {
                    "Button.border", buttonBorder,
                    "ToggleButton.border", buttonBorder,
                    "ToolBar.rolloverBorder", buttonBorder,
                    "ToolBar.nonrolloverBorder", buttonBorder,
                    "RadioButton.icon", radioButtonIcon,

// add in dialog icons
                    "OptionPane.errorIcon", LookAndFeel.makeIcon(MetalLookAndFeel.class, "icons/Error.gif"),
                    "OptionPane.informationIcon", LookAndFeel.makeIcon(MetalLookAndFeel.class, "icons/Inform.gif"),
                    "OptionPane.warningIcon", LookAndFeel.makeIcon(MetalLookAndFeel.class, "icons/Warn.gif"),
                    "OptionPane.questionIcon", LookAndFeel.makeIcon(MetalLookAndFeel.class, "icons/Question.gif"),
                };
        uidefaults.putDefaults(classMap);
    }

    protected void initClassDefaults(UIDefaults uidefaults) {
        super.initClassDefaults(uidefaults);

        // create a map of all the classes we provide and install them
        final Object classMap[] =
                {
                    "ButtonUI", PACKAGE + "SmoothButtonUI",
                    "CheckBoxUI", PACKAGE + "SmoothCheckBoxUI",
                    "CheckBoxMenuItemUI", PACKAGE + "SmoothCheckBoxMenuItemUI",
                    "ComboBoxUI", PACKAGE + "SmoothComboBoxUI",
                    "DesktopIconUI", PACKAGE + "SmoothDesktopIconUI",
                    "EditorPaneUI", PACKAGE + "SmoothEditorPaneUI",
                    "FileChooserUI", PACKAGE + "SmoothFileChooserUI",
                    "FormattedTextFieldUI", PACKAGE + "SmoothFormattedTextFieldUI",
                    "InternalFrameUI", PACKAGE + "SmoothInternalFrameUI",
                    "LabelUI", PACKAGE + "SmoothLabelUI",
                    "MenuUI", PACKAGE + "SmoothMenuUI",
                    "MenuBarUI", PACKAGE + "SmoothMenuBarUI",
                    "MenuItemUI", PACKAGE + "SmoothMenuItemUI",
                    "PasswordFieldUI", PACKAGE + "SmoothPasswordFieldUI",
                    "PanelUI", PACKAGE + "SmoothPanelUI",
                    "ProgressBarUI", PACKAGE + "SmoothProgressBarUI",
                    "PopupMenuSeparatorUI", PACKAGE + "SmoothPopupMenuSeparatorUI",
                    "RadioButtonUI", PACKAGE + "SmoothRadioButtonUI",
                    "RadioButtonMenuItemUI", PACKAGE + "SmoothRadioButtonMenuItemUI",
                    "ScrollBarUI", PACKAGE + "SmoothScrollBarUI",
                    "ScrollPaneUI", PACKAGE + "SmoothScrollPaneUI",
                    "SplitPaneUI", PACKAGE + "SmoothSplitPaneUI",
                    "SliderUI", PACKAGE + "SmoothSliderUI",
                    "SeparatorUI", PACKAGE + "SmoothSeparatorUI",
                    "TabbedPaneUI", PACKAGE + "SmoothTabbedPaneUI",
                    "TextAreaUI", PACKAGE + "SmoothTextAreaUI",
                    "TextFieldUI", PACKAGE + "SmoothTextFieldUI",
                    "TextPaneUI", PACKAGE + "SmoothTextPaneUI",
                    "ToggleButtonUI", PACKAGE + "SmoothToggleButtonUI",
                    "ToolBarUI", PACKAGE + "SmoothToolBarUI",
                    "ToolTipUI", PACKAGE + "SmoothToolTipUI",
                    "TreeUI", PACKAGE + "SmoothTreeUI"
                };
        uidefaults.putDefaults(classMap);
    }
    
    public String getID() {
        return "SmoothMetal";
    }

    public String getDescription() {
        return "The Smooth Metal Look and Feel";
    }

    public String getName() {
        return "SmoothMetal";
    }

    public boolean isAntiAliasing() {
        return SmoothUtilities.isAntialias();
    }

    public void setAntiAliasing(boolean on) {
        SmoothUtilities.setAntialias(on);
    }
}