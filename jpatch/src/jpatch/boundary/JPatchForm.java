/*
 * $Id: JPatchForm.java,v 1.3 2006/05/22 10:46:19 sascha_l Exp $
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package jpatch.boundary;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.*;
import java.util.*;

/**
 * A component that lays out its children like in a form. It uses a customized GridBag layout.
 * Add entries using the addEntry(String label, Component component) method and call populate() when finished.
 * @author sascha
 * @version $Revision: 1.3 $
 */
public class JPatchForm extends JPanel {

	public static class FillOrder { }
	
	public static final FillOrder COLUMN = new FillOrder();
	public static final FillOrder ROW = new FillOrder();
	
	private static final long serialVersionUID = -2355759241601901784L;
	
	private FillOrder fillOrder;
	private int iColumns;
	private int iLabelAlignment = javax.swing.SwingConstants.LEFT;
	private Insets insets = new Insets(2, 10, 2, 10);
	private ArrayList list = new ArrayList();
	
	public JPatchForm() {
		this(1, COLUMN);
	}
	
	/**
	 * @param columns The number of columns. Each row in each column will hold one entry (a label/component pair).
	 * @param fillOrder The fill order (JPatchForm.COLUMN or JPatchForm.ROW)
	 */
	public JPatchForm(int columns, FillOrder fillOrder) {
		iColumns = columns;
		this.fillOrder = fillOrder;
		setLayout(new GridBagLayout());		// use GridBagLayout
	}
	
	/**
	 * Adds a new entry to the form. Don't forget to call populate() befor showing the form.
	 * @param label The compoment's label
	 * @param component The component to add
	 */
	public void addEntry(String label, Component component) {
		list.add(label);
		list.add(component);
	}
	
	/**
	 * This method must be called after all entries have been added and before the form is shown.
	 */
	public void populate() {
		/*
		 * compute how many rows are needed
		 */
		int n = list.size() / 2;
		int rows = n / iColumns;
		if (n % iColumns != 0)
			rows++;
		
		/*
		 * start with upper left element
		 */
		int row = 0;
		int column = 0;
		
		/*
		 * Iterate through list and add entries
		 */
		for (Iterator it = list.iterator(); it.hasNext(); ) {
			JLabel label = new JLabel((String) it.next());		// get next label
			Component component = (Component) it.next();		// get next component
			GridBagConstraints gbc;
			/*
			 * configure GridBagConstraints for label
			 */
			gbc = new GridBagConstraints();
			gbc.gridx = column * 2;
			gbc.gridy = row;
			gbc.insets = insets;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			label.setHorizontalAlignment(iLabelAlignment);		// use right alignment for label
			add(label, gbc);									// add the label
			
			/*
			 * configure GridBagConstraints for component
			 */
			gbc = new GridBagConstraints();
			gbc.gridx = column * 2 + 1;
			gbc.gridy = row;
			gbc.insets = insets;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			add(component, gbc);								// add component
			
			/*
			 * compute next row/column depending on fill order
			 */
			if (fillOrder == COLUMN) {
				row++;
				if (row >= rows) {
					row = 0;
					column++;
				}
			}
			else if (fillOrder == ROW) {
				column++;
				if (column >= iColumns) {
					column = 0;
					row++;
				}
			}
		}
	}
}

