package jpatch.boundary.dialog;

import java.util.*;
import javax.swing.*;
import buoy.widget.*;
import buoy.event.*;
import jpatch.boundary.*;

public class ZBufferSettings extends BDialog {
	private JPatchSettings settings = JPatchSettings.getInstance();
	private String[] rendererName = new String[] { "Java2D", "Software Z-Buffer", "OpenGL" };
	
	private ColumnContainer content = new ColumnContainer();
	private FormContainer form = new FormContainer(2, 3);
	
	private RowContainer buttons = new RowContainer();
	private BButton buttonOk = new BButton("OK");
	private BButton buttonCancel = new BButton("Cancel");
	private BSlider sliderTesselationQuality = new BSlider(settings.iTesselationQuality,0,4,BSlider.HORIZONTAL);
	private BuoyUtils.RadioSelector rsBackface = new BuoyUtils.RadioSelector(new String[] { "ignore", "cull", "highlight" }, settings.iBackfaceMode);
	private BuoyUtils.RadioSelector rsRenderer = new BuoyUtils.RadioSelector(rendererName, settings.iRealtimeRenderer);
	
	public ZBufferSettings() {
		super("Realtime renderer settings");
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
		LayoutInfo east = new LayoutInfo(LayoutInfo.NORTHEAST, LayoutInfo.NONE);
		LayoutInfo west = new LayoutInfo(LayoutInfo.NORTHWEST, LayoutInfo.NONE);
		
		int i = 0;
		form.add(new BLabel("Renderer:    "), 0, i, east);
		form.add(rsRenderer, 1, i++, west);
		form.add(new BLabel("Backfacing polygons:    "), 0, i, east);
		form.add(rsBackface, 1, i++, west);
		form.add(new BLabel("Tesselation quality:    "), 0, i, east);
		form.add(sliderTesselationQuality, 1, i++, west);
		
		
		content.add(form);
		content.add(buttons);
		setContent(content);
		pack();
	}
	
	private void cancel() {
		dispose();
	}
	
	private void ok() {
		if (settings.iRealtimeRenderer != rsRenderer.getSelectedIndex()) {
			JOptionPane.showMessageDialog(this.getComponent(),
					"You have changed the realtime-renderer from " + rendererName[settings.iRealtimeRenderer] +
					" to " + rendererName[rsRenderer.getSelectedIndex()] + ".\nThe new setting will take effect" +
					" next time you start JPatch."
			);
			settings.iRealtimeRenderer = rsRenderer.getSelectedIndex();
		}
		settings.iTesselationQuality = sliderTesselationQuality.getValue();
		settings.iBackfaceMode = rsBackface.getSelectedIndex();
		Viewport2.setQuality(settings.iTesselationQuality);
		dispose();
		MainFrame.getInstance().getJPatchScreen().update_all();
	}
}
