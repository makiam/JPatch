package com.jpatch.boundary;

import java.awt.*;
import java.awt.event.*;

import com.jpatch.afw.*;
import com.jpatch.afw.attributes.*;
import com.jpatch.afw.ui.*;
import com.jpatch.boundary.headupdisplay.*;
import com.jpatch.entity.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

import jpatch.boundary.ui.*;




public class MorphComponent implements SpecialBinding.FormContainer {
	
	
	private static final Border TABLE_BORDER = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2), BorderFactory.createEtchedBorder());
	private final JPanel morphsPanel = new JPanel(new BorderLayout());
	private final JPanel morphsTablePanel = new JPanel(new BorderLayout());
	private final JComponent buttonBox = Box.createHorizontalBox();
	private final JButton newMorphButton = new JButton("new");
	private final JButton newTargetButton = new JButton("new");
	private JPatchFormContainer rootFormContainer = new JPatchFormContainer("Morphs", new BooleanAttr(), newMorphButton);
	private JPatchFormContainer advancedFormContainer = new JPatchFormContainer("Advanced", new BooleanAttr());
	private JPatchFormContainer dofFormContainer = new JPatchFormContainer("Degrees of freedom", new BooleanAttr());
	private JPatchFormContainer sliderFormContainer = new JPatchFormContainer("Sliders", new BooleanAttr());
	private JPatchFormContainer targetsFormContainer = new JPatchFormContainer("Targets", new BooleanAttr(), newTargetButton);
//	private JPatchFormContainer targetPositionFormContainer = new JPatchFormContainer("Target position", new BooleanAttr());
	private JPatchForm advancedForm = new JPatchForm();
	private JPatchForm sliderForm = new JPatchForm();
	private JTextField kValueTextField = new JTextField();
	
	private JLabel[] sliderLables = new JLabel[0];
	private JSlider[] sliders = new JSlider[0];
	
	private MorphInterpolator currentMorph;
	private MorphTarget currentTarget;
	private DoubleArrayAttr targetPositionAttr;
	
	private MorphController morphController;
	private SdsModel sdsModel;
	private MorphListModel morphListModel = new MorphListModel();
	private JList morphList = new JList(morphListModel);
	
	private final AttributePostChangeListener activeTargetListener = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
			targetsTableModel.fireTableDataChanged();
		}	
	};
	
	@SuppressWarnings("serial")
	private final AbstractTableModel dofTableModel = new JPatchTableModel(
			new String[] { "DOF", "min", "current", "max" },
			new Class[] { String.class, Double.class, Double.class, Double.class },
			new boolean[] { true, true, true, true }
	) {
		public int getRowCount() {
			return currentMorph == null ? 0 : currentMorph.getDegreesOfFreedom();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0: // DOF name
				return currentMorph.getDofNamesAttribute().getValue(rowIndex);
			case 1: // lower limit
				return currentMorph.getLowerLimitsAttribute().getDouble(rowIndex);
			case 2: // value
				return currentMorph.getPositionAttribute().getDouble(rowIndex);
			case 3: // upper limit
				return currentMorph.getUpperLimitsAttribute().getDouble(rowIndex);
			default:
				throw new AssertionError("should never get here");	
			}
		}
		
		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0: // DOF name
				currentMorph.getDofNamesAttribute().setValue(rowIndex, (String) value);
				break;
			case 1: // lower limit
				currentMorph.getLowerLimitsAttribute().setDouble(rowIndex, (Double) value);
				break;
			case 2: // value
				currentMorph.getPositionAttribute().setDouble(rowIndex, (Double) value);
				break;
			case 3: // upper limit
				currentMorph.getUpperLimitsAttribute().setDouble(rowIndex, (Double) value);
				break;
			default:
				throw new AssertionError("should never get here");	
			}
		}
	
	};
	private final JTable dofTable = new JTable(dofTableModel);
	
	@SuppressWarnings("serial")
	private final AbstractTableModel targetsTableModel = new AbstractTableModel() {

		public int getColumnCount() {
			return currentMorph == null ? 0 : currentMorph.getDegreesOfFreedom() + 2;
		}

		public int getRowCount() {
			return currentMorph == null ? 0 : currentMorph.getMorphTargets().size();
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return Boolean.class;
			case 1:
				return String.class;
			default:
				return Double.class;
			}
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "A";
			case 1:
				return "Name";
			default:
				return currentMorph.getDofNamesAttribute().getValue(column - 2);
			}
		}
		
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return currentMorph.getMorphTargets().get(rowIndex) == morphController.getActiveMorphTarget();
			case 1:
				return currentMorph.getMorphTargets().get(rowIndex).getNameAttribute().getValue();
			default:
				return currentMorph.getCenterPosition(rowIndex, columnIndex - 2);
			}
		}
		
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0: // active
				morphController.setActiveMorphTarget(currentMorph.getMorphTargets().get(rowIndex));
				morphController.apply();
				Main.getInstance().repaintViewports();
				break;
			case 1: // target name
				currentMorph.getMorphTargets().get(rowIndex).getNameAttribute().setValue((String) value);
				break;
			default:
				currentMorph.setCenterPosition(rowIndex, columnIndex - 2, (Double) value);
			}
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return rowIndex == 0 && columnIndex == 0 ? false : true;
		}
	};
	private final JTable targetsTable = new JTable(targetsTableModel);
	
//	@SuppressWarnings("serial")
//	private final AbstractTableModel targetPositionTableModel = new JPatchTableModel(
//			new String[] { "DOF", "position" },
//			new Class[] { String.class, Double.class },
//			new boolean[] { false, true }
//	) {
//
//		public int getRowCount() {
//			return currentTarget == null ? 0 : currentMorph.getDegreesOfFreedom();
//		}
//
//		public Object getValueAt(int rowIndex, int columnIndex) {
//			switch (columnIndex) {
//			case 0:
//				return currentMorph.getDofNamesAttribute().getValue(rowIndex);
//			case 1:
//				return targetPositionAttr.getAttr(rowIndex).getDouble();
//			default:
//				throw new AssertionError("should never get here");
//			}
//		}
//		
//		@Override
//		public void setValueAt(Object value, int rowIndex, int columnIndex) {
//			switch (columnIndex) {
//			case 1: // target name
//				targetPositionAttr.getAttr(rowIndex).setDouble((Double) value);
//				break;
//			default:
//				throw new AssertionError("should never get here");	
//			}
//		}
//	};
//	private final JTable targetPositionTable = new JTable(targetPositionTableModel);
	
	public MorphComponent() {
		morphsPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		morphsTablePanel.setBorder(TABLE_BORDER);
		newMorphButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JPanel newMorphDialog = new JPanel();
				newMorphDialog.setLayout(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();
				JTextField nameTextField = new JTextField("new morph", 15);
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.insets = new Insets(2, 2, 2, 2);
				gbc.gridx = 0;
				gbc.gridy = 0;
				newMorphDialog.add(new JLabel("Name:"), gbc);
				gbc.gridx = 1;
				gbc.gridy = 0;
				newMorphDialog.add(nameTextField, gbc);
				JComboBox dimensionsComboBox = new JComboBox(new Integer[] { 1, 2, 3, 4, 5, 6 } );
				gbc.gridx = 0;
				gbc.gridy = 1;
				newMorphDialog.add(new JLabel("Dimensions:"), gbc);
				gbc.gridx = 1;
				gbc.gridy = 1;
				newMorphDialog.add(dimensionsComboBox, gbc);
				Dimension d = dimensionsComboBox.getPreferredSize();
				nameTextField.setPreferredSize(d);
				
				int option = JPatchDialog.showDialog(
						Main.getInstance().getFrame(),
						"Create new morph",
						JPatchDialog.QUESTION,
						"Enter parameters for new morph:",
						newMorphDialog,
						new String[] { "Create morph", null, "Cancel" },
						0,
						"200");
				
				if (option == 0) {
					morphController.addMorph(new MorphInterpolator((Integer) dimensionsComboBox.getSelectedItem(), morphController, nameTextField.getText()));
					morphListModel.fireIntervalAdded(morphListModel, morphController.getNumberOfMorphs(), morphController.getNumberOfMorphs() + 1);
					morphList.setSelectedIndex(morphController.getNumberOfMorphs() - 1);
				}
			}
		});
		newTargetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				currentMorph.createMorphTarget();
				targetsTableModel.fireTableDataChanged();
			}
		});
//		buttonBox.add(newButton);
//		buttonBox.setOpaque(false);
//		morphsPanel.add(buttonBox, BorderLayout.NORTH);
		morphsPanel.add(morphsTablePanel, BorderLayout.CENTER);
		rootFormContainer.add(morphsPanel);
		rootFormContainer.add(advancedFormContainer);
		rootFormContainer.add(dofFormContainer);
		rootFormContainer.add(sliderFormContainer);
		rootFormContainer.add(targetsFormContainer);
		
		newTargetButton.setEnabled(false);
		
		morphList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		morphList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				setCurrentMorph(morphController.getMorph(morphList.getSelectedIndex()));
			}		
		});
		
		advancedForm.addRow(new JLabel("k value"), kValueTextField);
		advancedFormContainer.add(advancedForm);
		
		sliderForm.addRow(new JLabel("Test"), new JSlider());
		sliderFormContainer.add(sliderForm);
		
		/* Dof Table */
		dofTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		dofTable.setSelectionModel(Utils.NULL_SELECTION_MODEL);
		JPanel dofTablePanel = new JPanel(new BorderLayout());
		dofTablePanel.setBorder(TABLE_BORDER);
		dofTablePanel.add(dofTable.getTableHeader(), BorderLayout.NORTH);
		dofTablePanel.add(dofTable);
		dofFormContainer.add(dofTablePanel);
		
		/* Targets table */
//		targetsTable.getColumnModel().getColumn(0).setMaxWidth(20);
		targetsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		JScrollPane scrollPane = new JScrollPane(targetsTable, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JPanel targetsTablePanel = new JPanel(new BorderLayout());
		targetsTablePanel.setBorder(TABLE_BORDER);
		targetsTablePanel.add(targetsTable.getTableHeader(), BorderLayout.NORTH);
		targetsTablePanel.add(targetsTable);
		targetsFormContainer.add(targetsTablePanel);
		
//		targetsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
//			public void valueChanged(ListSelectionEvent arg0) {
//				int row = targetsTable.getSelectedRow();
//				if (row != -1) {
//					setCurrentTarget(currentMorph.getMorphTargets().get(targetsTable.getSelectedRow()));
//				} else {
//					setCurrentTarget(null);
//				}
//			}		
//		});
		
//		targetsFormContainer.add(targetPositionFormContainer);
		
//		/* Target position table */
//		targetPositionTable.setSelectionModel(Utils.NULL_SELECTION_MODEL);
//		JPanel targetPositionPanel = new JPanel(new BorderLayout());
//		targetPositionPanel.setBorder(TABLE_BORDER);
//		targetPositionPanel.add(targetPositionTable.getTableHeader(), BorderLayout.NORTH);
//		targetPositionPanel.add(targetPositionTable);
//		targetPositionFormContainer.add(targetPositionPanel);
		
		
//		dofTable.setDefaultRenderer(GenericAttr.class, new DefaultTableCellRenderer() {
//
//			@Override
//			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//				setText(((GenericAttr<String>) value).getValue());
//				return this;
//			}
//			
//		});
	}
	
	public JPatchFormContainer getFormContainer() {
		return rootFormContainer;
	}
	
	private void setCurrentMorph(MorphInterpolator morph) {
		this.currentMorph = morph;
		final AttributeManager attributeManager = AttributeManager.getInstance();
		
		attributeManager.unbind(kValueTextField);
		attributeManager.unbind(dofTable);
		for (int i = 0; i < sliders.length; i++) {
			attributeManager.unbind(sliders[i]);
			attributeManager.unbind(sliderLables[i]);
		}
		
		targetsTableModel.fireTableStructureChanged();
		
		sliderForm.removeAll();
		if (currentMorph != null) {
			attributeManager.bindTextFieldToAttribute(currentMorph, kValueTextField, currentMorph.getKAttribute());
			attributeManager.bindTableToAttribute(currentMorph, dofTable, currentMorph.getPositionAttribute());
			sliders = new JSlider[currentMorph.getDegreesOfFreedom()];
			sliderLables = new JLabel[currentMorph.getDegreesOfFreedom()];
			for (int i = 0; i < currentMorph.getDegreesOfFreedom(); i++) {
				sliders[i] = new JSlider();
				sliderLables[i] = new JLabel();
				DoubleAttr doubleAttr = currentMorph.getPositionAttribute().getAttr(i);
				attributeManager.bindSliderToAttribute(currentMorph, sliders[i], doubleAttr, IdentityMapping.getInstance());
				attributeManager.bindLabelToAttribute(currentMorph, sliderLables[i], currentMorph.getDofNamesAttribute().getAttr(i));
				sliderForm.addRow(sliderLables[i], sliders[i]);
				sliders[i].addChangeListener(new ChangeListener() {

					public void stateChanged(ChangeEvent arg0) {
						morphController.apply();
						Main.getInstance().repaintViewports();
					}
					
				});
			}
			targetsTable.getColumnModel().getColumn(0).setMaxWidth(20);
			targetsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		} else {
			
			sliders = new JSlider[0];
			sliderLables = new JLabel[0];
		}
		newTargetButton.setEnabled(currentMorph != null);
		
		dofTableModel.fireTableDataChanged();
		
		if (morph != null) {
			Main.getInstance().slider.link(Slider.Axis.X_AXIS, morph.getPositionAttribute().getAttr(0));
			Main.getInstance().slider.link(Slider.Axis.Y_AXIS, morph.getPositionAttribute().getAttr(1));
		}
//		
	}
	
//	private void setCurrentTarget(MorphTarget target) {
//		currentTarget = target;
//		if (currentTarget != null) {
//			targetPositionAttr = currentMorph.createCenterPositionAttribute(currentTarget);
//		}
//		targetPositionTableModel.fireTableDataChanged();
//	}
	
	
	public void bindTo(Object binding) {
		setCurrentMorph(null);
		sdsModel = (SdsModel) binding;
		morphsTablePanel.removeAll();
		morphController = sdsModel.getSds().getMorphController();
		morphsTablePanel.add(morphList, BorderLayout.CENTER);
		morphController.getActiveMorphTargetAttribute().removeAttributePostChangeListener(activeTargetListener);
		morphController.getActiveMorphTargetAttribute().addAttributePostChangeListener(activeTargetListener);
	}

	private class MorphListModel extends AbstractListModel {

		public Object getElementAt(int index) {
			return morphController.getMorph(index).getNameAttribute().getValue();
		}

		public int getSize() {
			return morphController.getNumberOfMorphs();
		}

		@Override
		protected void fireIntervalAdded(Object source, int index0, int index1) {
			// TODO Auto-generated method stub
			super.fireIntervalAdded(source, index0, index1);
		}
		
	}
}
