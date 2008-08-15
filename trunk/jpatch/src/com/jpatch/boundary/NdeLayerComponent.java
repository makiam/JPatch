package com.jpatch.boundary;

import com.jpatch.entity.sds2.*;

import javax.swing.*;

public class NdeLayerComponent {
	private JPanel panel = new JPanel();
	
	public JComponent getComponent() {
		return panel;
	}
	
	public void setSds(Sds sds) {
		panel.removeAll();
		NdeLayerManager ndelm = new NdeLayerManager(sds);
		panel.add(ndelm.getComponent());
	}
}
