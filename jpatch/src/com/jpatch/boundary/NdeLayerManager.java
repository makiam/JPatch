package com.jpatch.boundary;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import com.jpatch.afw.*;
import com.jpatch.entity.*;
import com.jpatch.entity.sds2.*;

public class NdeLayerManager extends Morph<NdeLayer> {
	private final NdeTableModel tableModel = new NdeTableModel();
	private final JTable table = new JTable(tableModel);
	private int selectedLayerIndex = 0;
	
	public NdeLayerManager(MorphController morphController) {
		super(NdeLayer.class, morphController);
		morphController.setNdeLayerManager(this);
		
		NdeLayer defaultLayer = createMorphTarget();
		defaultLayer.getNameAttribute().setValue("Default Layer");
		morphController.setActiveMorphTarget(defaultLayer);
		
//		table.getColumnModel().getColumn(0).setCellRenderer(new TableCellRenderer() {
//			private JCheckBox checkBox = new JCheckBox();
//			public Component getTableCellRendererComponent(JTable table,
//					Object value, boolean isSelected, boolean hasFocus,
//					int row, int column) {
//				checkBox.setSelected(layers.get(row).getEnabledAttribute().getBoolean());
//				return checkBox;
//			}
//			
//		});
//		JComboBox comboBox = new JComboBox();
//		comboBox.addItem("Snowboarding");
//		comboBox.addItem("Rowing");
//		comboBox.addItem("Chasing toddlers");
//		comboBox.addItem("Speed reading");
//		comboBox.addItem("Teaching high school");
//		comboBox.addItem("None");

		table.setShowGrid(false);
		table.getColumnModel().getColumn(0).setMaxWidth(22);
		table.getColumnModel().getColumn(1).setMaxWidth(22);
		
		
		table.addMouseMotionListener(new MouseMotionAdapter() {
			private final Point p = new Point();
			@Override
			public void mouseMoved(MouseEvent e) {
				p.setLocation(e.getX(), e.getY());
				int row = table.rowAtPoint(p);
				int col = table.columnAtPoint(p);
				switch (col) {
				case 0:
					table.setToolTipText("select active layer");
					break;
				case 1:
					table.setToolTipText("activate/deactivate layer");
					break;
				default:
					table.setToolTipText(null);	
				}
			}
		});
		
		table.setSelectionModel(Utils.NULL_SELECTION_MODEL);
	}
	
	public JComponent getComponent() {
		return table;
	}
	
	
	@Override
	public NdeLayer createMorphTarget() {
		NdeLayer ndeLayer = super.createMorphTarget();
		tableModel.fireTableRowsInserted(morphTargets.size() - 1, morphTargets.size() - 1);
		return ndeLayer;
	}


	class NdeTableModel extends AbstractTableModel {

		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return morphTargets.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return rowIndex == selectedLayerIndex;
			case 1:
				return morphTargets.get(rowIndex).getEnabledAttribute().getBoolean();
			case 2:
				return morphTargets.get(rowIndex).getNameAttribute().getValue();
			default:
				throw new RuntimeException();
			}
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				int tmp = selectedLayerIndex;
				selectedLayerIndex = rowIndex;
				fireTableCellUpdated(tmp, 0);
				fireTableCellUpdated(selectedLayerIndex, 0);
				morphController.setActiveMorphTarget(morphTargets.get(rowIndex));
				morphController.apply();
				Main.getInstance().repaintViewports();
				break;
			case 1:
				morphTargets.get(rowIndex).getEnabledAttribute().setBoolean((Boolean) value);
				fireTableCellUpdated(rowIndex, 0);
				morphController.apply();
				Main.getInstance().repaintViewports();
				break;
			case 2:
				morphTargets.get(rowIndex).getNameAttribute().setValue((String) value);
				break;
			default:
				throw new RuntimeException();
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return Boolean.class;
			case 1:
				return Boolean.class;
			case 2:
				return String.class;
			default:
				throw new RuntimeException();
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			NdeLayer layer = morphTargets.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return layer.getEnabledAttribute().getBoolean();
			case 1:
				return rowIndex != selectedLayerIndex;
			case 2:
				return true;
			default:
				throw new RuntimeException();
			}
		}
		
		
	}
	
//	public static void main(String[] args) {
//		JFrame frame = new JFrame("NDE Layer List test");
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		NdeLayerManager manager = new NdeLayerManager(null);
//		manager.addLayer(new NdeLayer("layer 1"));
//		manager.addLayer(new NdeLayer("layer 2"));
//		manager.addLayer(new NdeLayer("layer 3"));
//		frame.add(manager.table);
//		frame.pack();
//		frame.setVisible(true);
//	}
}
