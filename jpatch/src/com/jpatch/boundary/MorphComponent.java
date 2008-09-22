package com.jpatch.boundary;

import java.awt.*;
import java.awt.event.*;

import com.jpatch.afw.*;
import com.jpatch.afw.attributes.*;
import com.jpatch.afw.ui.*;
import com.jpatch.entity.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;



public class MorphComponent implements SpecialBinding.FormContainer {
	private static final Border TABLE_BORDER = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2), BorderFactory.createEtchedBorder());
	private final JPanel morphsPanel = new JPanel(new BorderLayout());
	private final JPanel morphsTablePanel = new JPanel(new BorderLayout());
	private final JComponent buttonBox = Box.createHorizontalBox();
	private final JButton newButton = new JButton("New");
	private JPatchFormContainer rootFormContainer = new JPatchFormContainer("Morphs", new BooleanAttr());
	private JPatchFormContainer advancedFormContainer = new JPatchFormContainer("Advanced", new BooleanAttr());
	private JPatchFormContainer dofFormContainer = new JPatchFormContainer("Degrees of freedom", new BooleanAttr());
	private JPatchFormContainer sliderFormContainer = new JPatchFormContainer("Sliders", new BooleanAttr());
	private JPatchFormContainer targetsFormContainer = new JPatchFormContainer("Targets", new BooleanAttr());
	private JPatchFormContainer targetPositionFormContainer = new JPatchFormContainer("Target position", new BooleanAttr());
	private JPatchForm advancedForm = new JPatchForm();
	private JPatchForm sliderForm = new JPatchForm();
	private JTextField kValueTextField = new JTextField();
	
	private JLabel[] sliderLables = new JLabel[0];
	private JSlider[] sliders = new JSlider[0];
	
	private MorphInterpolator currentMorph;
	
	private MorphController morphController;
	private SdsModel sdsModel;
	private MorphListModel morphListModel = new MorphListModel();
	private JList morphList = new JList(morphListModel);
	
	private DofTableModel dofTableModel = new DofTableModel();
	private JTable dofTable = new JTable(dofTableModel);
	
	private TargetsTableModel targetsTableModel = new TargetsTableModel();
	private JTable targetsTable = new JTable(targetsTableModel);
	
	public MorphComponent() {
		morphsPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		morphsTablePanel.setBorder(TABLE_BORDER);
		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				morphController.addMorph(new MorphInterpolator(3, morphController, "new morph"));
				morphListModel.fireIntervalAdded(morphListModel, morphController.getNumberOfMorphs(), morphController.getNumberOfMorphs() + 1);
				morphList.setSelectedIndex(morphController.getNumberOfMorphs() - 1);
			}
		});
		
		buttonBox.add(newButton);
		buttonBox.setOpaque(false);
		morphsPanel.add(buttonBox, BorderLayout.NORTH);
		morphsPanel.add(morphsTablePanel, BorderLayout.CENTER);
		rootFormContainer.add(morphsPanel);
		rootFormContainer.add(advancedFormContainer);
		rootFormContainer.add(dofFormContainer);
		rootFormContainer.add(sliderFormContainer);
		rootFormContainer.add(targetsFormContainer);
		
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
		targetsTable.getColumnModel().getColumn(0).setMaxWidth(20);
		targetsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JPanel targetsTablePanel = new JPanel(new BorderLayout());
		targetsTablePanel.setBorder(TABLE_BORDER);
		targetsTablePanel.add(targetsTable.getTableHeader(), BorderLayout.NORTH);
		targetsTablePanel.add(targetsTable);
		targetsFormContainer.add(targetsTablePanel);
		
		targetsFormContainer.add(targetPositionFormContainer);
		
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
			}
		} else {
			
			sliders = new JSlider[0];
			sliderLables = new JLabel[0];
		}
		dofTableModel.fireTableDataChanged();
		targetsTableModel.fireTableDataChanged();
	}
	
	
	
	public void bindTo(Object binding) {
		setCurrentMorph(null);
		sdsModel = (SdsModel) binding;
		morphsTablePanel.removeAll();
		morphController = sdsModel.getSds().getMorphController();
		morphsTablePanel.add(morphList, BorderLayout.CENTER);
	}

	private class MorphListModel extends AbstractListModel {

		public Object getElementAt(int index) {
			return morphController.getMorph(index).getNameAttribute();
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
	
	private class TargetsTableModel extends AbstractTableModel {
		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return currentMorph == null ? 0 : currentMorph.getMorphTargets().size();
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0: // DOF name
				return "A";
			case 1: // lower limit
				return "Target";
			default:
				throw new AssertionError("should never get here");
			}
		}
		
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return currentMorph.getMorphTargets().get(rowIndex) == morphController.getActiveMorphTarget();
			case 1:
				return currentMorph.getMorphTargets().get(rowIndex).getNameAttribute().getValue();
			default:
				throw new AssertionError("should never get here");
			}
		}
		
		@Override
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
				throw new AssertionError("should never get here");	
			}
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return Boolean.class;
			case 1:
				return String.class;
			default:
				throw new AssertionError("should never get here");
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}
	}
	
	private class DofTableModel extends AbstractTableModel {

		public int getColumnCount() {
			return 4;
		}

		public int getRowCount() {
			return currentMorph == null ? 0 : currentMorph.getDegreesOfFreedom();
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0: // DOF name
				return "DOF";
			case 1: // lower limit
				return "min";
			case 2: // value
				return "current";
			case 3: // upper limit
				return "max";
			default:
				throw new AssertionError("should never get here");
			}
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
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return String.class;
			case 1:
			case 2:	//fallthrough intentional
			case 3: //fallthrough intentional
				return Double.class;
			default:
				throw new AssertionError("should never get here");
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}
	}
}
