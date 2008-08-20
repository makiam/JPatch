package com.jpatch.boundary;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import com.jpatch.entity.*;
import com.jpatch.entity.sds2.*;

public class NdeLayerManager {
	private final Sds sds;
	private final List<NdeLayer> layers = new ArrayList<NdeLayer>();
	private final NdeTableModel tableModel = new NdeTableModel();
	private final JTable table = new JTable(tableModel);
	private int selectedLayerIndex = 0;
	
	public NdeLayerManager(Sds sds) {
		this.sds = sds;
		addLayer((NdeLayer) sds.getActiveNdeLayer());
		addLayer(new NdeLayer("test layer"));
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
		
		JCheckBox checkBox0 = new JCheckBox();
		checkBox0.setToolTipText("test");
		
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
//		table.getColumnModel().getColumn(0).setCellRenderer(new TableCellRenderer() {
//			private JRadioButton radioButton = new JRadioButton();
//			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//				radioButton.setSelected((Boolean) value);
//				NdeLayer layer = layers.get(row);
//				radioButton.setEnabled(layer.getEnabledAttribute().getBoolean());
//				radioButton.setOpaque(false);
//				return radioButton;
//			}
//			
//		});
		
//		final JRadioButton radioButton = new JRadioButton();
//		
//		checkBox0.setUI(new javax.swing.plaf.basic.BasicRadioButtonUI());
//		table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(checkBox0));
		
		table.setSelectionModel(new DefaultListSelectionModel() {

			@Override
			public int getAnchorSelectionIndex() {
				return -1;
			}

			@Override
			public int getLeadSelectionIndex() {
				return -1;
			}

			@Override
			public int getMaxSelectionIndex() {
				return -1;
			}

			@Override
			public int getMinSelectionIndex() {
				return -1;
			}

			@Override
			public boolean isSelectedIndex(int index) {
				return false;
			}

			@Override
			public boolean isSelectionEmpty() {
				return true;
			}
			
		});
//		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
//
//			public void valueChanged(ListSelectionEvent e) {
//				if (!e.getValueIsAdjusting()) {
//					ListSelectionModel lsm = (ListSelectionModel) e.getSource();
//					System.out.println(lsm.getLeadSelectionIndex());
//					NdeLayer selectedLayer = layers.get(table.getSelectedRow());
//					System.out.println(selectedLayer.getNameAttribute().getValue());
//				}
//			}
//			
//		});
//		table.getColumnModel().getColumn(0).getCellEditor().addCellEditorListener(new CellEditorListener() {

//			public void editingCanceled(ChangeEvent arg0) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			public void editingStopped(ChangeEvent arg0) {
//				System.out.println(arg0.getSource());
//			}
//			
//		});
	}
	
	public void addLayer(NdeLayer layer) {
		layers.add(layer);
		
	}
	
	public JComponent getComponent() {
		return table;
	}
	
	private void apply() {
		Set<Object> objects = new HashSet<Object>();
		for (NdeLayer layer : layers) {
			layer.reset();
			objects.addAll(layer.getObjects());
		}
		for (NdeLayer layer : layers) {
			layer.apply();
		}
		for (Object object : objects) {
			if (object instanceof BaseVertex) {
				((BaseVertex) object).validateLocalPosition();
			}
		}
		Main.getInstance().repaintViewports();
	}
	
	class NdeTableModel extends AbstractTableModel {

		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return layers.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return rowIndex == selectedLayerIndex;
			case 1:
				return layers.get(rowIndex).getEnabledAttribute().getBoolean();
			case 2:
				return layers.get(rowIndex).getNameAttribute().getValue();
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
				sds.setActiveNdeLayer(layers.get(rowIndex));
				break;
			case 1:
				layers.get(rowIndex).getEnabledAttribute().setBoolean((Boolean) value);
				fireTableCellUpdated(rowIndex, 0);
				apply();
				break;
			case 2:
				layers.get(rowIndex).getNameAttribute().setValue((String) value);
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
			NdeLayer layer = layers.get(rowIndex);
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
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("NDE Layer List test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		NdeLayerManager manager = new NdeLayerManager(null);
		manager.addLayer(new NdeLayer("layer 1"));
		manager.addLayer(new NdeLayer("layer 2"));
		manager.addLayer(new NdeLayer("layer 3"));
		frame.add(manager.table);
		frame.pack();
		frame.setVisible(true);
	}
}
