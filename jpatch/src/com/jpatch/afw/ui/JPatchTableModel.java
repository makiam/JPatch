package com.jpatch.afw.ui;

import javax.swing.table.*;

public abstract class JPatchTableModel extends AbstractTableModel {
	private final int columnCount;
	private final String[] columnNames;
	private final Class<?>[] columnClasses;
	private final boolean[] editable;
	
	public JPatchTableModel(String[] columnNames, Class<?>[] columnClasses, boolean editable[]) {
		this.columnCount = columnNames.length;
		if (columnClasses.length != columnCount || editable.length != columnCount) {
			throw new IllegalArgumentException("array lenghts must be equal");
		}
		this.columnNames = columnNames.clone();
		this.columnClasses = columnClasses.clone();
		this.editable = editable.clone();	
	}
	
	public int getColumnCount() {
		return columnCount;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columnClasses[columnIndex];
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return editable[columnIndex];
	}

	public abstract void setValueAt(Object object, int rowIndex, int columnIndex);
}
