package com.jpatch.boundary;

import java.awt.*;
import java.awt.event.*;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.ui.*;
import com.jpatch.entity.*;
import com.jpatch.entity.sds2.*;

import javax.swing.*;

public class NdeLayerComponent implements SpecialBinding.FormContainer {
	private final JPanel panel = new JPanel(new BorderLayout());
	private final JPanel tablePanel = new JPanel(new BorderLayout());
	private final JButton newButton = new JButton("New");
	private final JPatchFormContainer formContainer = new JPatchFormContainer("NDE Layers", new BooleanAttr(), newButton);
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
		panel.add(tablePanel, BorderLayout.CENTER);
		formContainer.add(panel);
	}
	
	public JPatchFormContainer getFormContainer() {
		return formContainer;
	}
	
	public void bindTo(Object binding) {
		sdsModel = (SdsModel) binding;
		tablePanel.removeAll();
		ndeLayerManager = sdsModel.getSds().getNdeLayerManager();
		tablePanel.add(ndeLayerManager.getTable().getTableHeader(), BorderLayout.NORTH);
		tablePanel.add(ndeLayerManager.getTable(), BorderLayout.CENTER);
		
		
		
	}

	
}
