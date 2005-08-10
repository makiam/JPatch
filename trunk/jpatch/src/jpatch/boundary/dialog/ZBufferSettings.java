package jpatch.boundary.dialog;

import java.util.*;
import javax.swing.*;
import buoy.widget.*;
import buoy.event.*;
import jpatch.boundary.*;

public class ZBufferSettings extends BDialog {
	private JPatchSettings settings = JPatchSettings.getInstance();
	
	private ColumnContainer content = new ColumnContainer();
	private FormContainer form = new FormContainer(2, 2);
	
	private RowContainer buttons = new RowContainer();
	private BButton buttonOk = new BButton("OK");
	private BButton buttonCancel = new BButton("Cancel");
	private BSlider sliderTesselationQuality = new BSlider(settings.iTesselationQuality,0,4,BSlider.HORIZONTAL);
	private BuoyUtils.RadioSelector rsBackface = new BuoyUtils.RadioSelector(new String[] { "do nothing", "cull", "highlight" }, settings.iBackfaceMode);
	
	public ZBufferSettings() {
		super("ZBuffer rendering options");
		setModal(true);
		setResizable(false);
		addEventLink(WindowClosingEvent.class, this, "cancel");
		
		buttonCancel.addEventLink(CommandEvent.class, this, "cancel");
		buttonOk.addEventLink(CommandEvent.class, this, "ok");
		buttons.add(buttonOk);
		buttons.add(buttonCancel);
		
		sliderTesselationQuality.setMinorTickSpacing(1);
		sliderTesselationQuality.setMajorTickSpacing(1);
		sliderTesselationQuality.setShowTicks(true);
		sliderTesselationQuality.setShowLabels(true);
		sliderTesselationQuality.setSnapToTicks(true);
		Dictionary dict = new Hashtable();
		dict.put(new Integer(0), new JLabel("low"));
		dict.put(new Integer(2), new JLabel("medium"));
		dict.put(new Integer(4), new JLabel("high"));
		((JSlider) sliderTesselationQuality.getComponent()).setLabelTable(dict);
				
		//LayoutInfo northeast = new LayoutInfo(LayoutInfo.NORTHEAST, LayoutInfo.NONE);
		LayoutInfo east = new LayoutInfo(LayoutInfo.EAST, LayoutInfo.NONE);
		LayoutInfo west = new LayoutInfo(LayoutInfo.NORTHWEST, LayoutInfo.NONE);
		
		int i = 0;
		form.add(new BLabel("Tesselation quality:    "), 0, i, east);
		form.add(sliderTesselationQuality, 1, i++, west);
		form.add(new BLabel("Backface mode:    "), 0, i, east);
		form.add(rsBackface, 1, i++, west);
		
		content.add(form);
		content.add(buttons);
		setContent(content);
		pack();
	}
	
	private void cancel() {
		dispose();
	}
	
	private void ok() {
		settings.iTesselationQuality = sliderTesselationQuality.getValue();
		settings.iBackfaceMode = rsBackface.getSelectedIndex();
		JPatchDrawableZBuffer.setQuality(settings.iTesselationQuality);
		dispose();
		MainFrame.getInstance().getJPatchScreen().update_all();
	}
}
