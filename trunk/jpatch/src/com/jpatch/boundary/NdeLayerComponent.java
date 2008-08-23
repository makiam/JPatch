package com.jpatch.boundary;

import java.awt.*;
import java.awt.event.*;

import com.jpatch.entity.*;
import com.jpatch.entity.sds2.*;

import javax.swing.*;

public class NdeLayerComponent implements SpecialBinding {
	private final JPanel panel = new JPanel(new BorderLayout());
	private final JPanel tablePanel = new JPanel(new BorderLayout());
	private final JComponent buttonBox = Box.createHorizontalBox();
	private final JButton newButton = new JButton("New");
	private NdeLayerManager ndeLayerManager;
	private SdsModel sdsModel;
	
	public NdeLayerComponent() {
		panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		tablePanel.setBorder(BorderFactory.createEtchedBorder());
		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ndeLayerManager.createMorphTarget();
				panel.revalidate();
				panel.repaint();
			}
		});
		buttonBox.add(newButton);
		buttonBox.setOpaque(false);
		panel.add(buttonBox, BorderLayout.NORTH);
		panel.add(tablePanel, BorderLayout.CENTER);
	}
	
	public JComponent getComponent() {
		return panel;
	}
	
	public void bindTo(Object binding) {
		sdsModel = (SdsModel) binding;
		tablePanel.removeAll();
		ndeLayerManager = sdsModel.getSds().getNdeLayerManager();
		tablePanel.add(ndeLayerManager.getComponent(), BorderLayout.CENTER);
		
		
		
	}
}
