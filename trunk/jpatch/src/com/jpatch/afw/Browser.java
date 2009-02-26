package com.jpatch.afw;

import javax.swing.*;

public class Browser {
	private JComponent getViewFor(Object object) {
		JTable table = new JTable();
		ReflectionTable.setupTable(table, object);
		return table;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
